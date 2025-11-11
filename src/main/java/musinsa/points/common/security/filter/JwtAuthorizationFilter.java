package musinsa.points.common.security.filter;


import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import musinsa.points.common.exception.BusinessException;
import musinsa.points.common.exception.ErrorCode;
import musinsa.points.common.security.jwt.JwtUtil;
import musinsa.points.common.security.service.UserDetailsServiceImpl;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;


@Slf4j(topic = "JWT 검증 및 인가")
public class JwtAuthorizationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final UserDetailsServiceImpl userDetailsService;

    public JwtAuthorizationFilter(JwtUtil jwtUtil,
                                  UserDetailsServiceImpl userDetailsService) {
        this.jwtUtil = jwtUtil;
        this.userDetailsService = userDetailsService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res, FilterChain filterChain) throws ServletException, IOException {

        String tokenValue = jwtUtil.getJwtFromHeader(req);

        if (StringUtils.hasText(tokenValue)) {

            if (!jwtUtil.validateToken(tokenValue)) {
                log.error("Token Error");
                return;
            }

            Claims info = jwtUtil.getUserInfoFromToken(tokenValue);

            try {
                setAuthentication(Long.parseLong(info.getSubject()));
            } catch (DisabledException de) {
                // 탈퇴/비활성 사용자: 인증 거부 및 401 응답
                log.warn("Blocked disabled user request: {}", de.getMessage());
                res.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                res.setContentType("application/json;charset=UTF-8");
                res.getWriter().write(ErrorCode.UNAUTHORIZED.getMessage());
                return;
            } catch (Exception e) {
                log.error(e.getMessage());
                return;
            }
        }

        filterChain.doFilter(req, res);
    }

    // 인증 처리
    public void setAuthentication(Long memberSeq) {
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        Authentication authentication = createAuthentication(memberSeq);
        context.setAuthentication(authentication);

        SecurityContextHolder.setContext(context);
    }

    // 인증 객체 생성
    private Authentication createAuthentication(Long memberSeq) {
        UserDetails userDetails = userDetailsService.loadUserByMemberSeq(memberSeq);
        if (userDetails == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "사용자 정보를 찾을 수 없습니다.");
        }
        /* if (!userDetails.isEnabled()) {
            // delete_date 등으로 비활성화된 사용자: 인증 차단
            throw new BusinessException(ErrorCode.NOT_FOUND, "이미 탈퇴했거나 존재하지 않는 회원입니다.");
        }*/
        return new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
    }
}
