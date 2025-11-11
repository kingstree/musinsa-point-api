package musinsa.points.common.security.service;


import musinsa.points.domain.entity.Member;
import musinsa.points.domain.enums.UserRole;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.Collection;
import java.util.List;

public class CustomUserDetailsInfo implements CustomUserDetails{

    private final Member member;

    public CustomUserDetailsInfo(Member member) {
        this.member = member;
    }

    @Override
    public Member getMember() {
        return member;
    }

    @Override
    public Long getMemberSeq() { return member.getMemberSeq();}

    @Override
    public UserRole getRole() {
        return member.getRole();
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // user.getRole()이 null이 아니면 "ROLE_"를 붙여서 반환
        UserRole role = member.getRole();
        if (role == null) {
            return List.of();
        }
        return List.of(new SimpleGrantedAuthority("ROLE_" + role));
    }

    /*
     * 회원 가입 및 기타 로직 사용하지 않음으로 빈값 리턴
     */
     @Override
    public String getPassword() {
        return "";
    }
    /*
     * 회원 가입 및 기타 로직 사용하지 않음으로 빈값 리턴
     */
    @Override
    public String getUsername() {
        return "";
    }

}
