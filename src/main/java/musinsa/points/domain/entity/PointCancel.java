package musinsa.points.domain.entity;

import jakarta.persistence.*;
import lombok.*;
import musinsa.points.common.entity.BaseTimeEntity;

import java.util.UUID;

@Entity
@Table(name = "point_cancel")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class PointCancel extends BaseTimeEntity {

    @Id
    @GeneratedValue
    private UUID cancelId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "use_detail_id", nullable = false)
    private PointUseDetail pointUseDetail;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "use_id", nullable = false)
    private PointUse pointUse;

    @Column(nullable = false)
    private Long cancelAmount;

    @Column(nullable = false)
    private Long regrantNeededAmount;

}
