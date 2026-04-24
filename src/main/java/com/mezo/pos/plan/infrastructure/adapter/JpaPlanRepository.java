package com.mezo.pos.plan.infrastructure.adapter;

import com.mezo.pos.plan.domain.entity.Plan;
import com.mezo.pos.plan.domain.enums.PlanType;
import com.mezo.pos.plan.domain.port.PlanRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class JpaPlanRepository implements PlanRepository {

    private final SpringDataPlanRepository springRepo;

    @Override
    public Plan save(Plan plan) {
        return springRepo.save(plan);
    }

    @Override
    public Optional<Plan> findByType(PlanType type) {
        return springRepo.findByType(type);
    }

    @Override
    public List<Plan> findAll() {
        return springRepo.findAll();
    }

    @Override
    public long count() {
        return springRepo.count();
    }
}
