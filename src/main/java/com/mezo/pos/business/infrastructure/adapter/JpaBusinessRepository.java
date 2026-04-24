package com.mezo.pos.business.infrastructure.adapter;

import com.mezo.pos.business.domain.entity.Business;
import com.mezo.pos.business.domain.port.BusinessRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class JpaBusinessRepository implements BusinessRepository {

    private final SpringDataBusinessRepository springRepo;

    @Override
    public Business save(Business business) {
        return springRepo.save(business);
    }

    @Override
    public Optional<Business> findById(UUID id) {
        return springRepo.findByIdAndDeletedFalse(id);
    }

    @Override
    public List<Business> findByOwnerId(UUID ownerId) {
        return springRepo.findByOwnerIdAndDeletedFalse(ownerId);
    }

    @Override
    public boolean existsByIdAndOwnerId(UUID id, UUID ownerId) {
        return springRepo.existsByIdAndOwnerIdAndDeletedFalse(id, ownerId);
    }

    @Override
    public long countByOwnerId(UUID ownerId) {
        return springRepo.countByOwnerIdAndDeletedFalse(ownerId);
    }
}
