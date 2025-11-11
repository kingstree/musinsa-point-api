package musinsa.points.domain.entity;

import jakarta.persistence.*;
import lombok.*;

import musinsa.points.common.entity.BaseTimeEntity;
import musinsa.points.domain.enums.UserRole;


@Entity
@Table(name = "members")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Member extends BaseTimeEntity {

    /** 내부 시스템에서 사용하는 고유 식별자 (PK) */
    @Id
    @Column(name = "member_seq")
    @GeneratedValue
    private Long memberSeq;

    /** 외부 시스템(또는 로그인용) 아이디 */
    @Column(name = "member_id", length = 36)
    private String memberId;

    /** 회원 이름 */
    @Column(name = "name", length = 100)
    private String name;

    /** 사용자 역할 (기본 CUSTOMER, MANAGER 허용) */
    @Enumerated(EnumType.STRING)
    @Column(name = "role", length = 30, nullable = false)
    @Builder.Default
    private UserRole role = UserRole.CUSTOMER;
}
