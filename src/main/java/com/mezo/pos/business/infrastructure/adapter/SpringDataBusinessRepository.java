package com.mezo.pos.business.infrastructure.adapter;

import com.mezo.pos.business.domain.entity.Business;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SpringDataBusinessRepository extends JpaRepository<Business, UUID> {

    List<Business> findByOwnerIdAndDeletedFalse(UUID ownerId);

    Optional<Business> findByIdAndDeletedFalse(UUID id);

    boolean existsByIdAndOwnerIdAndDeletedFalse(UUID id, UUID ownerId);

    long countByOwnerIdAndDeletedFalse(UUID ownerId);
}
