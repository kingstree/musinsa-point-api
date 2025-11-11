package musinsa.points.infrastructure.repository;

import musinsa.points.domain.entity.PointGrant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface PointGrantJpaRepository extends JpaRepository<PointGrant, UUID> {

    /**
     * 회원의 잔여 무료포인트 합계(원장 기준, NULL -> 0 처리)
     */
    @Query("""
        select coalesce(sum(g.remainingAmount), 0)
              from PointGrant g
             where g.member.memberSeq = :memberSeq
    """)
    long sumRemainingByMember(@Param("memberSeq") long memberSeq);

    /**
     * 사용 가능한 그랜트(남은 금액 > 0, 만료 미도래)를 만료일 오름차순(FIFO)으로 조회.
     */
    @Query("""
        select g from PointGrant g
         where g.member.memberSeq = :memberSeq
           and g.remainingAmount > 0
           and g.expiresAt > CURRENT_TIMESTAMP
           and g.status = musinsa.points.domain.enums.PointStatus.ACTIVE
         order by case when g.grantType = musinsa.points.domain.enums.GrantType.MANUAL then 0 else 1 end,
                  g.expiresAt asc, g.grantId asc
    """)
    List<PointGrant> findSpendableGrantsOrderByExpiry(@Param("memberSeq") long memberSeq);

    /**
     * 남은금액 증감 (양수=증가, 음수=차감). 변경 행이 1이 아닐 경우 상위에서 예외 처리 권장.
     */
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
        update PointGrant g
           set g.remainingAmount = g.remainingAmount + :delta,
               g.modifiedDate = :now
         where g.grantId = :grantId
           and g.remainingAmount + :delta >= 0
    """)
    int adjustRemaining(@Param("grantId") UUID grantId, @Param("delta") long delta);
}
