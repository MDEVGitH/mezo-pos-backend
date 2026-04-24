package com.mezo.pos.business.domain.port;

import com.mezo.pos.business.domain.entity.Business;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface BusinessRepository {
    Business save(Business business);
    Optional<Business> findById(UUID id);
    List<Business> findByOwnerId(UUID ownerId);
    boolean existsByIdAndOwnerId(UUID id, UUID ownerId);
    long countByOwnerId(UUID ownerId);
}
