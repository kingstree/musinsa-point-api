package musinsa.points.domain.entity;

import jakarta.persistence.*;
import lombok.*;
import musinsa.points.common.entity.BaseTimeEntity;
import musinsa.points.domain.enums.PointPolicyType;
import musinsa.points.domain.enums.PolicyScope;
import org.hibernate.annotations.Comment;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "point_policy",
        indexes = {
                @Index(name = "ix_point_policy_global", columnList = "policy_type, scope, active"),
                @Index(name = "ix_point_policy_member", columnList = "member_seq, policy_type, active"),
                @Index(name = "ix_point_policy_member_active", columnList = "member_seq, active")
        })
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class PointPolicy extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "policy_id", nullable = false, updatable = false)
    @Comment("정책 식별자 (UUID)")
    private UUID policyId;

    @Enumerated(EnumType.STRING)
    @Column(name = "scope", nullable = false, length = 16)
    @Comment("정책 범위 (GLOBAL | MEMBER)")
    private PolicyScope scope;

    @Enumerated(EnumType.STRING)
    @Column(name = "policy_type", nullable = false, length = 32)
    @Comment("정책 유형 (MAX_GRANT_PER_TX | MAX_BALANCE_PER_MEMBER)")
    private PointPolicyType policyType;

    @Column(name = "member_seq")
    @Comment("회원별 정책일 경우 대상 회원 시퀀스")
    private Long memberSeq;

    @Column(name = "active", nullable = false)
    @Comment("활성화 여부")
    private boolean active;

    @Column(name = "policy_value", nullable = false)
    @Comment("정책 값 (금액, 한도 등)")
    private Long policyValue;


    // ---- 비즈니스 메서드 ---- //
    public boolean isGlobal() {
        return this.scope == PolicyScope.GLOBAL;
    }

    public boolean isMemberPolicy() {
        return this.scope == PolicyScope.MEMBER;
    }

    public void deactivate() {
        this.active = false;
    }

    public void updateValue(Long newValue, Long modifier) {
        this.policyValue = newValue;
    }

    public void setPolicyValue(Long policyValue) {
        if (policyValue == null || policyValue < 0) {
            throw new IllegalArgumentException("policyValue must be non-null and non-negative");
        }
        this.policyValue = policyValue;
    }
}
