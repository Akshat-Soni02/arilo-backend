package com.project_x.project_x_backend.repository;

import com.project_x.project_x_backend.entity.Plan;
import com.project_x.project_x_backend.enums.PlanTypes;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface PlanRepository extends JpaRepository<Plan, UUID> {
    Optional<Plan> findByNameAndIsActiveTrue(PlanTypes name);
}
