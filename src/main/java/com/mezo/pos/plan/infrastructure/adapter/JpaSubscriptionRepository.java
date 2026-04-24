package com.mezo.pos.plan.infrastructure.adapter;

import com.mezo.pos.plan.domain.entity.Subscription;
import com.mezo.pos.plan.domain.port.SubscriptionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class JpaSubscriptionRepository implements SubscriptionRepository {

    private final SpringDataSubscriptionRepository springRepo;

    @Override
    public Subscription save(Subscription subscription) {
        return springRepo.save(subscription);
    }

    @Override
    public Optional<Subscription> findByUserId(UUID userId) {
        return springRepo.findByUserId(userId);
    }
}
