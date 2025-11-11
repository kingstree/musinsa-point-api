package musinsa.points.domain.entity;

import jakarta.persistence.*;
import lombok.*;

import musinsa.points.common.entity.BaseTimeEntity;
import musinsa.points.domain.enums.UseStatus;
import java.util.UUID;

@Entity
@Table(name = "point_use")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class PointUse extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "use_id", nullable = false)
    private UUID useId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_seq", referencedColumnName = "member_seq", nullable = false)
    private Member member;

    @Column(name = "order_no", length = 64, nullable = false)
    private String orderNo;

    @Column(name = "used_amount", nullable = false)
    private Long usedAmount;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 24, nullable = false)
    private UseStatus status;


    public boolean isCancellable() {
        return status == UseStatus.USED || status == UseStatus.PARTIAL_CANCELLED;
    }

    public void markCancelled() {
        this.status = UseStatus.CANCELLED;
    }

    public void markPartialCancelled() {
        this.status = UseStatus.PARTIAL_CANCELLED;
    }
}
