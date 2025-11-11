package musinsa.points.common.security.service;


import musinsa.points.domain.entity.Member;
import musinsa.points.domain.enums.UserRole;

import org.springframework.security.core.userdetails.UserDetails;

/**
 * CustomUserDetails
 * - Spring Security의 UserDetails에 프로젝트 도메인 속성을 얹은 확장 인터페이스
 * - 컨트롤러/서비스 계층에서 캐스팅 없이 userNo, userId, email, role 등을 안전하게 조회하기 위함
 */
public interface CustomUserDetails extends UserDetails {

    Member getMember();

    Long getMemberSeq();

    UserRole getRole();
}
