package com.mezo.pos.plan.application;

import com.mezo.pos.auth.domain.entity.User;
import com.mezo.pos.auth.domain.port.UserRepository;
import com.mezo.pos.plan.domain.entity.Subscription;
import com.mezo.pos.plan.domain.enums.PlanType;
import com.mezo.pos.plan.domain.enums.SubscriptionStatus;
import com.mezo.pos.plan.domain.port.PaymentGateway;
import com.mezo.pos.plan.domain.port.SubscriptionRepository;
import com.mezo.pos.shared.domain.exception.UnauthorizedException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class HandlePaymentWebhookUseCase {

    private final PaymentGateway paymentGateway;
    private final UserRepository userRepository;
    private final SubscriptionRepository subscriptionRepository;

    @Transactional
    @SuppressWarnings("unchecked")
    public void execute(Map<String, Object> payload, String signature) {
        // Validate webhook signature
        if (!paymentGateway.validateWebhookSignature(payload.toString(), signature)) {
            throw new UnauthorizedException("Invalid webhook signature");
        }

        String event = (String) payload.get("event");
        Map<String, Object> data = (Map<String, Object>) payload.get("data");

        if (data == null) {
            log.warn("Webhook payload has no data field");
            return;
        }

        Map<String, Object> transaction = (Map<String, Object>) data.get("transaction");
        if (transaction == null) {
            log.warn("Webhook data has no transaction field");
            return;
        }

        String status = (String) transaction.get("status");
        String reference = (String) transaction.get("reference");
        String transactionId = String.valueOf(transaction.get("id"));

        if (reference == null || !reference.startsWith("sub_")) {
            log.warn("Invalid reference format: {}", reference);
            return;
        }

        // Parse reference: sub_{userId}_{planType}_{timestamp}
        String[] parts = reference.split("_");
        if (parts.length < 3) {
            log.warn("Invalid reference parts: {}", reference);
            return;
        }

        UUID userId;
        PlanType planType;
        try {
            userId = UUID.fromString(parts[1]);
            planType = PlanType.valueOf(parts[2]);
        } catch (Exception e) {
            log.warn("Failed to parse reference: {}", reference);
            return;
        }

        if ("transaction.updated".equals(event)) {
            handleTransactionUpdated(userId, planType, status, transactionId);
        } else {
            log.info("Unhandled webhook event: {}", event);
        }
    }

    private void handleTransactionUpdated(UUID userId, PlanType planType, String status, String transactionId) {
        if ("APPROVED".equals(status)) {
            User user = userRepository.findById(userId).orElse(null);
            if (user == null) {
                log.error("User not found for webhook: {}", userId);
                return;
            }

            LocalDateTime now = LocalDateTime.now();

            user.setPlan(planType);
            user.setPlanStartedAt(now);
            user.setPlanExpiresAt(now.plusDays(30));
            userRepository.save(user);

            Subscription subscription = subscriptionRepository.findByUserId(userId)
                    .orElse(Subscription.builder()
                            .userId(userId)
                            .planType(planType)
                            .currentPeriodStart(now)
                            .currentPeriodEnd(now.plusDays(30))
                            .build());

            subscription.setStatus(SubscriptionStatus.ACTIVE);
            subscription.setWompiTransactionId(transactionId);
            subscription.setPlanType(planType);
            subscription.setCurrentPeriodStart(now);
            subscription.setCurrentPeriodEnd(now.plusDays(30));
            subscriptionRepository.save(subscription);

            log.info("Payment approved for user {} - plan {}", userId, planType);
        } else {
            log.info("Payment {} for reference with status: {}", status, transactionId);
        }
    }
}
