package musinsa.points.config;


import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.Optional;

@Configuration //이 클래스가 스프링 설정을 위한 클래스임을 나타낸다.
@EnableJpaAuditing(auditorAwareRef = "auditorAware")
public class JpaAuditConfig { // 지속성 엔티티에 대한 감사 활성화

    @Bean
    AuditorAware<Integer> auditorAware() {
        return () -> Optional.ofNullable(SecurityContextHolder.getContext())
                .map(SecurityContext::getAuthentication)
                .filter(auth -> auth != null && auth.isAuthenticated() && !(auth instanceof AnonymousAuthenticationToken))
                .map(auth -> {
                    Object principal = auth.getPrincipal();

                    // 1) CustomUserDetails 구현체
                    try {
                        Class<?> clazz = principal.getClass();
                        var method = clazz.getMethod("getMemberSeq");
                        Object val = method.invoke(principal);
                        if (val instanceof Integer) {
                            return (Integer) val;
                        }
                        if (val instanceof String s && s.matches("\\d+")) {
                            return Integer.parseInt(s);
                        }
                    } catch (Exception ignore) { /* no-op */ }

                    // 2) JWT 기반 인증 시 (클레임에 userNo, user_no, sub 중 숫자값이 있으면 사용)
                    if (principal instanceof Jwt jwt) {
                        String uno = jwt.getClaimAsString("memberSeq");

                        if (uno != null && uno.matches("\\d+")) {
                            return Integer.parseInt(uno);
                        }
                    }

                    // 3) 그 외 상황에서는 null 반환 (감사 필드 null 허용)
                    return null;
                });
    }
}
