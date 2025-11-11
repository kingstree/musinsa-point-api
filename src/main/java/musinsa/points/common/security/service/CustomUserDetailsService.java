package musinsa.points.common.security.service;

import org.springframework.security.core.userdetails.UserDetailsService;

import java.util.UUID;

public interface CustomUserDetailsService extends UserDetailsService {
    CustomUserDetails loadUserByMemberSeq(Long memberSeq);
}
