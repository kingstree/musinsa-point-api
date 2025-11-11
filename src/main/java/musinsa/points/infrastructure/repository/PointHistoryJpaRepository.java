package musinsa.points.infrastructure.repository;


import musinsa.points.domain.entity.PointHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface PointHistoryJpaRepository extends JpaRepository<PointHistory, UUID> {


}
