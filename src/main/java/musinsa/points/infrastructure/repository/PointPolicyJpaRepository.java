package musinsa.points.infrastructure.repository;

import musinsa.points.domain.entity.PointPolicy;
import musinsa.points.domain.enums.PolicyScope;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface PointPolicyJpaRepository extends JpaRepository<PointPolicy, UUID> {

    // 인터페이스 내부 (마지막 } 전에) 추가
    @Query("""
    select p
      from PointPolicy p
     where p.scope = musinsa.points.domain.enums.PolicyScope.MEMBER
       and p.active = true
       and p.memberSeq is not null
""")
    List<PointPolicy> findDistinctActiveMemberSeqs();

    @Query("""
    select p
      from PointPolicy p
     where p.scope = musinsa.points.domain.enums.PolicyScope.GLOBAL
       and p.active = true
""")
    List<PointPolicy> findActiveGlobalPolicies();

    /**
     * 지정된 범위(scope)에서 활성화된 정책 목록 조회
     * 예: GLOBAL 정책 또는 MEMBER 정책
     */
    @Query("""
        select p
          from PointPolicy p
         where p.scope = :scope
           and p.active = true
    """)
    List<PointPolicy> findByScopeAndActiveTrue(@Param("scope") PolicyScope scope);

    /**
     * 특정 회원의 MEMBER 정책 중 활성화된 정책만 조회
     */
    @Query("""
        select p
          from PointPolicy p
         where p.scope = :scope
           and p.memberSeq = :memberSeq
           and p.active = true
    """)
    List<PointPolicy> findByScopeAndActiveTrueAndMemberSeq(
            @Param("scope") PolicyScope scope,
            @Param("memberSeq") Long memberSeq
    );
}
