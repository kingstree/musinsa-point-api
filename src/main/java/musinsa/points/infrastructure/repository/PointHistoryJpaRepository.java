package musinsa.points.infrastructure.repository;


import musinsa.points.domain.entity.Member;
import musinsa.points.domain.entity.PointHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface PointHistoryJpaRepository extends JpaRepository<PointHistory, UUID> {


}
