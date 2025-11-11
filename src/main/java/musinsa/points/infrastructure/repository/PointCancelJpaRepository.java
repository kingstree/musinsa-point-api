package musinsa.points.infrastructure.repository;

import musinsa.points.domain.entity.PointCancel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.UUID;

public interface PointCancelJpaRepository extends JpaRepository<PointCancel, UUID> {

    @Query("""
        select coalesce(sum(c.cancelAmount), 0)
        from PointCancel c
        where c.pointUse.useId = :useId
    """)
    long sumCanceledAmountByUseId(@Param("useId") UUID useId);

    @Query("""
        select coalesce(sum(c.cancelAmount), 0)
          from PointCancel c
         where c.pointUseDetail.useDetailId = :useDetailId
    """)
    long sumCanceledByUseDetailId(@Param("useDetailId") UUID useDetailId);

}
