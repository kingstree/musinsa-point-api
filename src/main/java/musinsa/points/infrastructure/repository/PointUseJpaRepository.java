package musinsa.points.infrastructure.repository;


import musinsa.points.domain.entity.PointUse;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface PointUseJpaRepository extends JpaRepository<PointUse, UUID> {


}
