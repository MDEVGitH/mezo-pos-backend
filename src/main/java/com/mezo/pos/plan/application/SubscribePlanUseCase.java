package com.mezo.pos.plan.application;

import com.mezo.pos.auth.domain.entity.User;
import com.mezo.pos.auth.domain.port.UserRepository;
import com.mezo.pos.plan.domain.entity.Plan;
import com.mezo.pos.plan.domain.entity.Subscription;
import com.mezo.pos.plan.domain.enums.PlanType;
import com.mezo.pos.plan.domain.enums.SubscriptionStatus;
import com.mezo.pos.plan.domain.port.PaymentGateway;
import com.mezo.pos.plan.domain.port.PlanRepository;
import com.mezo.pos.plan.domain.port.SubscriptionRepository;
import com.mezo.pos.plan.infrastructure.web.dto.SubscribeRequest;
import com.mezo.pos.plan.infrastructure.web.dto.SubscribeResponse;
import com.mezo.pos.shared.domain.exception.DomainException;
import com.mezo.pos.shared.domain.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SubscribePlanUseCase {

    private final UserRepository userRepository;
    private final PlanRepository planRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final PaymentGateway paymentGateway;

    @Transactional
    public SubscribeResponse execute(SubscribeRequest request, UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found: " + userId));

        PlanType targetPlanType = PlanType.valueOf(request.getPlanType());

        if (user.getPlan() == targetPlanType) {
            throw new DomainException("Ya estas suscrito al plan " + targetPlanType);
        }

        Plan targetPlan = planRepository.findByType(targetPlanType)
                .orElseThrow(() -> new NotFoundException("Plan not found: " + targetPlanType));

        // Create payment link via Wompi
        PaymentGateway.PaymentLink paymentLink = paymentGateway.createPaymentLink(
                userId, targetPlanType, targetPlan.getPrice()
        );

        // Create or update subscription
        LocalDateTime now = LocalDateTime.now();
        Subscription subscription = subscriptionRepository.findByUserId(userId)
                .orElse(Subscription.builder()
                        .userId(userId)
                        .currentPeriodStart(now)
                        .currentPeriodEnd(now.plusDays(30))
                        .build());

        subscription.setPlanType(targetPlanType);
        subscription.setStatus(SubscriptionStatus.TRIAL);
        subscriptionRepository.save(subscription);

        SubscribeResponse response = new SubscribeResponse();
        response.setPaymentUrl(paymentLink.url());
        response.setPlanType(targetPlanType.name());
        response.setPrice(targetPlan.getPrice().getAmount());
        response.setCurrency(targetPlan.getPrice().getCurrency().name());

        return response;
    }
}
