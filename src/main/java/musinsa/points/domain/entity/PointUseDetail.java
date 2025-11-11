package musinsa.points.domain.entity;

import jakarta.persistence.*;
import lombok.*;
import musinsa.points.common.entity.BaseTimeEntity;

import java.util.UUID;

@Entity
@Table(name = "point_use_detail")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class PointUseDetail extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "use_detail_id", nullable = false)
    private UUID useDetailId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "use_id", referencedColumnName = "use_id", nullable = false)
    private PointUse pointUse;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "grant_id", referencedColumnName = "grant_id", nullable = false)
    private PointGrant pointGrant;

    @Column(name = "amount", nullable = false)
    private Long amount;
}
