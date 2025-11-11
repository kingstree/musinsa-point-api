package musinsa.points.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import musinsa.points.application.service.PointPolicyService;
import musinsa.points.domain.entity.PointPolicy;
import musinsa.points.infrastructure.repository.PointPolicyJpaRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationRunner;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import java.util.List;
import java.util.concurrent.TimeUnit;

@Configuration
@EnableCaching
public class CacheConfig {
    @Bean
    @Primary
    public CacheManager cacheManager() {
        // ✅ 필요한 캐시 이름들을 모두 여기에 등록
        CaffeineCacheManager manager = new CaffeineCacheManager(
                "authTokens",    // 기존 인증용 캐시
                "userDetails",   // 사용자 정보 캐시
                "pointPolicy"    // ✅ 새로 추가된 포인트 정책 캐시
        );

        manager.setCaffeine(
                Caffeine.newBuilder()
                        .expireAfterWrite(24, TimeUnit.HOURS) // TTL 설정 (포인트 정책 캐시 포함)
                        .maximumSize(10000)
                        .recordStats()
        );
        return manager;
    }

    // 클래스 시작부에 로거
    private static final Logger log = LoggerFactory.getLogger(CacheConfig.class);

    // 클래스 끝쪽에 웜업 빈 추가
    @Bean
    public ApplicationRunner pointPolicyWarmup(PointPolicyService policyService,
                                               PointPolicyJpaRepository policyRepo) {
        return args -> {
            try {
                // 1) GLOBAL 먼저
                List<PointPolicy> GlobalPolicies = policyRepo.findActiveGlobalPolicies();
                policyService.resolveForGlobal(GlobalPolicies);

                // 2) MEMBER 대상들
                List<PointPolicy> members = policyRepo.findDistinctActiveMemberSeqs();
                policyService.resolveForMember(members);

            } catch (Exception e) {
                log.warn("[PolicyCacheWarmup] warmup skipped due to exception", e);
            }
        };
    }
}
