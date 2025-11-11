package musinsa.points.infrastructure.repository;



import musinsa.points.domain.entity.PointUseDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface PointUseDetailJpaRepository extends JpaRepository<PointUseDetail, UUID> {

    @Query("""
    select d
      from PointUseDetail d
      join fetch d.pointGrant g
     where d.pointUse.useId = :useId
     order by case when g.grantType = musinsa.points.domain.enums.GrantType.MANUAL then 0 else 1 end,
              g.expiresAt asc,
              d.useDetailId asc
""")
    List<PointUseDetail> findOrderedByUseIdForCancel(@Param("useId") UUID useId);

}
