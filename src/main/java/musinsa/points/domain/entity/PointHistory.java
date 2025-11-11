package musinsa.points.domain.entity;

import jakarta.persistence.*;
import lombok.*;

import musinsa.points.common.entity.BaseTimeEntity;
import musinsa.points.domain.enums.EventType;
import musinsa.points.domain.enums.RefType;

import java.util.UUID;

@Entity
@Table(name = "point_history")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class PointHistory extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "history_id", nullable = false)
    private UUID historyId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_seq", referencedColumnName = "member_seq", nullable = false)
    private Member member;

    @Enumerated(EnumType.STRING)
    @Column(name = "event_type", length = 16, nullable = false)
    private EventType eventType;

    @Enumerated(EnumType.STRING)
    @Column(name = "ref_type", length = 16, nullable = false)
    private RefType refType;

    @Column(name = "ref_id")
    private UUID refId;

    @Column(name = "delta", nullable = false)
    private Long delta;

    @Column(name = "balance_after", nullable = false)
    private Long balanceAfter;

}
