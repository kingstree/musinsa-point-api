package musinsa.points.infrastructure.repository;


import musinsa.points.domain.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;


public interface MemberRepository extends JpaRepository<Member, Long> {

    Optional<Member> findByMemberSeq(Long userId);

}
