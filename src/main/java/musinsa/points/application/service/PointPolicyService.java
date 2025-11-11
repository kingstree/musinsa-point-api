package musinsa.points.application.service;

import lombok.RequiredArgsConstructor;
import musinsa.points.domain.entity.Member;
import musinsa.points.domain.snapshot.PolicySnapshot;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import musinsa.points.domain.entity.PointPolicy;
import musinsa.points.domain.enums.PointPolicyType;
import musinsa.points.domain.enums.PolicyScope;
import musinsa.points.infrastructure.repository.PointPolicyJpaRepository;

import java.util.stream.Collectors;

import org.springframework.cache.CacheManager;

@Service
@RequiredArgsConstructor
public class PointPolicyService {

    private final Environment env;
    private final PointPolicyJpaRepository policyRepo;
    private final CacheManager cacheManager;

    // Simple local cache to avoid repeated environment lookups
    private final Map<String, String> cache = new ConcurrentHashMap<>();

    /** 임의 정책 키를 long으로 조회 (캐시 적용, 파싱 실패 시 기본값) */
    public long getLong(String key, long defaultValue) {
        String raw = cache.computeIfAbsent(key, env::getProperty);
        if (raw == null || raw.isBlank()) return defaultValue;
        try {
            return Long.parseLong(raw.trim());
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    /** 미리 여러 키를 워밍업 */
    public void warmupKeys(Collection<String> keys) {
        if (keys == null) return;
        for (String k : keys) getLong(k, 0L);
    }



    @Cacheable(cacheNames = "pointPolicy", key = "'GLOBAL'", sync = true)
    public Map<PointPolicyType, PolicySnapshot> resolveForGlobal(List<PointPolicy> GlobalPolicies) {

        var globalMap = new EnumMap<PointPolicyType, PolicySnapshot>(PointPolicyType.class);
        GlobalPolicies.stream()
                .filter(p -> p.getScope() == PolicyScope.GLOBAL)
                .forEach(p -> globalMap.put(p.getPolicyType(),
                        new PolicySnapshot(p.getScope(), p.getPolicyType(), null,
                                p.getPolicyValue(), p.getPolicyId())));
        return globalMap;
    }


    /**
     * 여러 회원 정책(여러 memberSeq) bulk 캐시 로더 (memberSeq별로 캐시 저장)
     * @param memberPolicies 정책 목록 (MEMBER)
     * @return memberSeq별 정책 맵
     */
    public Map<Long, Map<PointPolicyType, PolicySnapshot>> resolveForMember(List<PointPolicy> memberPolicies) {
        if (memberPolicies == null || memberPolicies.isEmpty()) {
            return Map.of();
        }

        // MEMBER 정책들을 memberSeq별로 그룹핑
        var byMember = memberPolicies.stream()
                .filter(p -> p.getScope() == PolicyScope.MEMBER && p.getMemberSeq() != null)
                .collect(Collectors.groupingBy(PointPolicy::getMemberSeq));

        var result = new HashMap<Long, Map<PointPolicyType, PolicySnapshot>>();

        for (var entry : byMember.entrySet()) {
            Long memberSeq = entry.getKey();
            var list = entry.getValue();

            var memberMap = new EnumMap<PointPolicyType, PolicySnapshot>(PointPolicyType.class);
            // 1) 회원 전용 정책 우선 반영
            for (var p : list) {
                memberMap.put(p.getPolicyType(), new PolicySnapshot(
                        p.getScope(), p.getPolicyType(), p.getMemberSeq(), p.getPolicyValue(), p.getPolicyId()));
            }

            // 캐시에 저장 (동일 빈 내부 self-invocation 문제 방지를 위해 수동 put)
            var springCache = cacheManager.getCache("pointPolicy");
            if (springCache != null) {
                springCache.put(memberSeq, memberMap);
            }
            result.put(memberSeq, memberMap);
        }

        return result;
    }

    /** 특정 키 캐시 제거 */
    public void evict(String key) {
        if (key != null) cache.remove(key);
    }

    /** 전체 캐시 클리어 */
    public void clear() {
        cache.clear();
    }

    /**
     * GLOBAL 정책에서 1회 최대 적립 한도 조회.
     * 우선 pointPolicy 캐시 → 미스 시 DB 로드 후 캐시에 저장 → 환경 기본값 폴백 순으로 조회한다.
     */
    public long getGlobalMaxGrantPerTx() {
        // 1) 캐시 히트 시 바로 반환
        var springCache = cacheManager.getCache("pointPolicy");
        if (springCache != null) {
            @SuppressWarnings("unchecked")
            Map<PointPolicyType, PolicySnapshot> cached = springCache.get("GLOBAL", Map.class);
            if (cached != null) {
                PolicySnapshot snap = cached.get(PointPolicyType.MAX_GRANT_PER_TX);
                if (snap != null) return snap.policyValue();
            }
        }

        // 2) 캐시 미스 → DB에서 GLOBAL 활성 정책 조회 후 캐시에 채움
        List<PointPolicy> globals = policyRepo.findByScopeAndActiveTrue(PolicyScope.GLOBAL);
        Map<PointPolicyType, PolicySnapshot> map = resolveForGlobal(globals);
        if (springCache != null) springCache.put("GLOBAL", map);

        PolicySnapshot snap = map.get(PointPolicyType.MAX_GRANT_PER_TX);
        if (snap != null) return snap.policyValue();

        // 3) 최종 폴백: 환경 기본값
        return getLong("points.policy.max-per-grant", 100_000L);
    }

    /**
     * MEMBER 정책에서 회원별 최대 보유 한도 조회.
     * 우선 pointPolicy 캐시 → 미스 시 DB 로드 후 캐시에 저장 → 환경 기본값 폴백 순으로 조회한다.
     */
    public long getMaxBalancePerMember(long memberSeq) {
        Objects.requireNonNull(memberSeq, "memberSeq must not be null");

        // 1) 캐시 확인
        var springCache = cacheManager.getCache("pointPolicy");
        if (springCache != null) {
            @SuppressWarnings("unchecked")
            Map<PointPolicyType, PolicySnapshot> cached = springCache.get(memberSeq, Map.class);
            if (cached != null) {
                PolicySnapshot snap = cached.get(PointPolicyType.MAX_BALANCE_PER_MEMBER);
                if (snap != null) return snap.policyValue();
            }
        }

        // 2) 캐시 미스 → 해당 회원의 MEMBER 정책만 조회하여 캐시 저장
        List<PointPolicy> memberPolicies = policyRepo.findByScopeAndActiveTrueAndMemberSeq(PolicyScope.MEMBER, memberSeq);
        resolveForMember(memberPolicies); // 내부에서 캐시에 put 수행

        if (springCache != null) {
            @SuppressWarnings("unchecked")
            Map<PointPolicyType, PolicySnapshot> cached = springCache.get(memberSeq, Map.class);
            if (cached != null) {
                PolicySnapshot snap = cached.get(PointPolicyType.MAX_BALANCE_PER_MEMBER);
                if (snap != null) return snap.policyValue();
            }
        }

        // 3) 최종 폴백: 환경 기본값
        return getLong("points.policy.max-balance", 1_000_000L);
    }
}
