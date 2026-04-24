package com.mezo.pos.plan.application;

import com.mezo.pos.auth.domain.entity.User;
import com.mezo.pos.auth.domain.port.UserRepository;
import com.mezo.pos.plan.domain.entity.Plan;
import com.mezo.pos.plan.domain.entity.Subscription;
import com.mezo.pos.plan.domain.port.PlanRepository;
import com.mezo.pos.plan.domain.port.SubscriptionRepository;
import com.mezo.pos.plan.infrastructure.web.dto.PlanResponse;
import com.mezo.pos.shared.domain.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class GetCurrentPlanUseCase {

    private final UserRepository userRepository;
    private final PlanRepository planRepository;
    private final SubscriptionRepository subscriptionRepository;

    @Transactional(readOnly = true)
    public PlanResponse execute(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found: " + userId));

        Plan plan = planRepository.findByType(user.getPlan())
                .orElseThrow(() -> new NotFoundException("Plan not found: " + user.getPlan()));

        Subscription subscription = subscriptionRepository.findByUserId(userId).orElse(null);

        boolean isTrialActive = user.getPlanExpiresAt() != null
                && LocalDateTime.now().isBefore(user.getPlanExpiresAt())
                && (subscription == null || subscription.getStatus() == com.mezo.pos.plan.domain.enums.SubscriptionStatus.TRIAL);

        PlanResponse response = new PlanResponse();
        response.setType(plan.getType().name());
        response.setMaxTables(plan.getMaxTables());
        response.setMaxProducts(plan.getMaxProducts());
        response.setMaxCategories(plan.getMaxCategories());
        response.setMaxEmployees(plan.getMaxEmployees());
        response.setMaxBusinesses(plan.getMaxBusinesses());
        response.setReportsEnabled(plan.isReportsEnabled());
        response.setPrice(plan.getPrice().getAmount());
        response.setCurrency(plan.getPrice().getCurrency().name());
        response.setTrialDays(plan.getTrialDays());
        response.setPlanStartedAt(user.getPlanStartedAt());
        response.setPlanExpiresAt(user.getPlanExpiresAt());
        response.setTrialActive(isTrialActive);

        if (subscription != null) {
            PlanResponse.SubscriptionInfo subInfo = new PlanResponse.SubscriptionInfo();
            subInfo.setStatus(subscription.getStatus().name());
            subInfo.setWompiSubscriptionId(subscription.getWompiSubscriptionId());
            response.setSubscription(subInfo);
        }

        return response;
    }
}
