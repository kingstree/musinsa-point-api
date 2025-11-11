package musinsa.points.common.security.service;

import musinsa.points.domain.entity.Member;
import musinsa.points.infrastructure.repository.MemberRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class UserDetailsServiceImpl implements CustomUserDetailsService {

    private final MemberRepository memberRepository;

    public UserDetailsServiceImpl(MemberRepository memberRepository) {
        this.memberRepository = memberRepository;
    }

    @Override
    public CustomUserDetailsInfo loadUserByMemberSeq(Long memberSeq) throws UsernameNotFoundException {
        Member member = memberRepository.findByMemberSeq(memberSeq)
                .orElseThrow(() -> new UsernameNotFoundException("Not Found " + memberSeq));

        return new CustomUserDetailsInfo(member);
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return null;
    }
}
