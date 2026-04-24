package com.mezo.pos.plan.domain.port;

import com.mezo.pos.plan.domain.entity.Plan;
import com.mezo.pos.plan.domain.enums.PlanType;

import java.util.List;
import java.util.Optional;

public interface PlanRepository {
    Plan save(Plan plan);
    Optional<Plan> findByType(PlanType type);
    List<Plan> findAll();
    long count();
}
