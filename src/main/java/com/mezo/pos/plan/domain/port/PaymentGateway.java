package com.mezo.pos.plan.domain.port;

import com.mezo.pos.plan.domain.enums.PlanType;
import com.mezo.pos.shared.domain.valueobject.Money;

import java.util.UUID;

public interface PaymentGateway {
    PaymentLink createPaymentLink(UUID userId, PlanType planType, Money price);
    boolean validateWebhookSignature(String payload, String signature);

    record PaymentLink(String id, String url) {}
}
