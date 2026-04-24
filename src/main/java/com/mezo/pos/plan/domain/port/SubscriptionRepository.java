package com.mezo.pos.plan.domain.port;

import com.mezo.pos.plan.domain.entity.Subscription;

import java.util.Optional;
import java.util.UUID;

public interface SubscriptionRepository {
    Subscription save(Subscription subscription);
    Optional<Subscription> findByUserId(UUID userId);
}
