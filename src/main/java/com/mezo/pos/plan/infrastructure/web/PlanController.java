package com.mezo.pos.plan.infrastructure.web;

import com.mezo.pos.plan.application.GetCurrentPlanUseCase;
import com.mezo.pos.plan.application.SubscribePlanUseCase;
import com.mezo.pos.plan.domain.entity.Plan;
import com.mezo.pos.plan.domain.port.PlanRepository;
import com.mezo.pos.plan.infrastructure.web.dto.PlanResponse;
import com.mezo.pos.plan.infrastructure.web.dto.SubscribeRequest;
import com.mezo.pos.plan.infrastructure.web.dto.SubscribeResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/plans")
@RequiredArgsConstructor
public class PlanController {

    private final PlanRepository planRepository;
    private final GetCurrentPlanUseCase getCurrentPlanUseCase;
    private final SubscribePlanUseCase subscribePlanUseCase;

    @GetMapping
    public ResponseEntity<List<Map<String, Object>>> listPlans() {
        List<Plan> plans = planRepository.findAll();
        List<Map<String, Object>> response = plans.stream()
                .map(p -> Map.<String, Object>of(
                        "type", p.getType().name(),
                        "maxTables", p.getMaxTables(),
                        "maxProducts", p.getMaxProducts(),
                        "maxCategories", p.getMaxCategories(),
                        "maxEmployees", p.getMaxEmployees(),
                        "maxBusinesses", p.getMaxBusinesses(),
                        "reportsEnabled", p.isReportsEnabled(),
                        "price", p.getPrice().getAmount(),
                        "currency", p.getPrice().getCurrency().name(),
                        "trialDays", p.getTrialDays()
                ))
                .toList();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/current")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PlanResponse> getCurrentPlan(Authentication authentication) {
        UUID userId = UUID.fromString(authentication.getName());
        PlanResponse response = getCurrentPlanUseCase.execute(userId);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/subscribe")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<SubscribeResponse> subscribe(
            @Valid @RequestBody SubscribeRequest request,
            Authentication authentication) {
        UUID userId = UUID.fromString(authentication.getName());
        SubscribeResponse response = subscribePlanUseCase.execute(request, userId);
        return ResponseEntity.ok(response);
    }
}
