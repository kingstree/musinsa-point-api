package musinsa.points.domain.entity;

import jakarta.persistence.*;
import lombok.*;
import musinsa.points.common.entity.BaseTimeEntity;
import musinsa.points.common.exception.BusinessException;
import musinsa.points.common.exception.ErrorCode;
import musinsa.points.domain.enums.GrantType;
import musinsa.points.domain.enums.PointStatus;

import java.time.Instant;
import java.util.UUID;


@Entity
@Table(name = "point_grant")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class PointGrant extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "grant_id", nullable = false)
    private UUID grantId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_seq", referencedColumnName = "member_seq", nullable = false)
    private Member member;

    @Column(name = "amount", nullable = false)
    private Long amount;

    @Column(name = "remaining_amount", nullable = false)
    private Long remainingAmount;

    @Enumerated(EnumType.STRING)
    @Column(name = "grant_type", length = 16, nullable = false)
    private GrantType grantType;

    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 16, nullable = false)
    private PointStatus status;

    @Column(length = 255)
    private String note;


    /**
     * Factory method to create a new PointGrant and publish a GrantCreatedEvent.
     */
    public static PointGrant create(Member member,
                                    long amount,
                                    GrantType grantType,
                                    Instant grantedAt,
                                    Instant expiresAt,
                                    String note) {
        if (amount <= 0) throw new IllegalArgumentException("amount must be > 0");
        PointGrant grant = PointGrant.builder()
                .member(member)
                .amount(amount)
                .remainingAmount(amount)
                .grantType(grantType)
                .expiresAt(expiresAt)
                .status(PointStatus.ACTIVE)
                .note(note)
                .build();

        return grant;
    }

    public boolean isActive() {
        return this.status == PointStatus.ACTIVE && remainingAmount > 0;
    }

    public boolean canBeCancelled() {
        return this.status == PointStatus.ACTIVE && this.remainingAmount.equals(this.amount);
    }

    public void consume(long useAmount) {
        //if (useAmount > remainingAmount) throw new IllegalArgumentException("잔액 초과");
        this.remainingAmount -= useAmount;
       // registerEvent(new GrantConsumedEvent(this.grantId, this.member.getMemberSeq(), useAmount, this.remainingAmount));
    }

        public void restore(long cancelAmount) {
            this.remainingAmount += cancelAmount;
            //registerEvent(new GrantRestoredEvent(this.grantId, this.member.getMemberSeq(), cancelAmount, this.remainingAmount));
        }

        public void expire() {
            this.status = PointStatus.EXPIRED;
            this.remainingAmount = 0L;
            //registerEvent(new GrantExpiredEvent(this.grantId, this.member.getMemberSeq(), this.expiresAt));
        }

    public void cancel() {
        if (!canBeCancelled()) throw new BusinessException(ErrorCode.INVALID_REQUEST, "이미 일부 사용된 적립은 취소할 수 없습니다");
        this.status = PointStatus.CANCELLED;
        this.remainingAmount = 0L;
        //registerEvent(new GrantCancelledEvent(this.grantId, this.member.getMemberSeq()));
    }

    /*
    *
     ===== Domain Events (nested records for this aggregate) =====
    public record GrantCreatedEvent(UUID grantId, Long memberSeq, long amount, GrantType grantType, Instant grantedAt, Instant expiresAt) {}
    public record GrantConsumedEvent(UUID grantId, Long memberSeq, long consumedAmount, long remainingAmount) {}
    public record GrantRestoredEvent(UUID grantId, Long memberSeq, long restoredAmount, long remainingAmount) {}
    public record GrantExpiredEvent(UUID grantId, Long memberSeq, Instant expiredAt) {}
    public record GrantCancelledEvent(UUID grantId, Long memberSeq) {}
    */
}
