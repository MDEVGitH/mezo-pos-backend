package com.mezo.pos.plan.infrastructure.adapter;

import com.mezo.pos.plan.domain.entity.Plan;
import com.mezo.pos.plan.domain.enums.PlanType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface SpringDataPlanRepository extends JpaRepository<Plan, UUID> {
    Optional<Plan> findByType(PlanType type);
}
