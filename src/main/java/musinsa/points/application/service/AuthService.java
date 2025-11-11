package musinsa.points.application.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import musinsa.points.common.exception.BusinessException;
import musinsa.points.common.exception.ErrorCode;
import musinsa.points.common.security.jwt.JwtUtil;
import musinsa.points.domain.entity.Member;
import musinsa.points.infrastructure.repository.MemberRepository;
import musinsa.points.presentation.dto.request.LoginRequest;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
@RequiredArgsConstructor
@Transactional
public class AuthService {

    private final MemberRepository memberRepository;
    private final JwtUtil jwtUtil;
    private final CacheManager cacheManager;

    public String signin(LoginRequest request) {
        Member member = memberRepository.findByMemberSeq(request.memberSeq())
            .orElseThrow(() -> new BusinessException(ErrorCode.UNAUTHORIZED, "로그인 정보가 올바르지 않습니다."));

        String token = jwtUtil.createToken(member.getMemberSeq(),member.getRole());

        // Cache warm-up for memberPolicy (maxPointBalance etc.) on login
        Cache cache = cacheManager.getCache("memberPolicy");
        if (cache != null) {
            Instant modified = member.getModifiedDate() != null ? member.getModifiedDate() : Instant.now();
            //cache.put(member.getMemberSeq(), new MemberPolicy(member.getMaxPointBalance(), modified));
        }

        return token;
    }

    public record MemberPolicy(Long maxPointBalance, Instant modifiedDate) {}
}
