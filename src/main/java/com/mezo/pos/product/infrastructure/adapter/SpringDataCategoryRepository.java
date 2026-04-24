package com.mezo.pos.product.infrastructure.adapter;

import com.mezo.pos.product.domain.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SpringDataCategoryRepository extends JpaRepository<Category, UUID> {

    Optional<Category> findByIdAndDeletedFalse(UUID id);

    List<Category> findByBusinessIdAndDeletedFalseOrderBySortOrderAsc(UUID businessId);

    @Query("SELECT MAX(c.sortOrder) FROM Category c WHERE c.businessId = :businessId AND c.deleted = false")
    Optional<Integer> findMaxSortOrderByBusinessId(@Param("businessId") UUID businessId);

    long countByBusinessIdAndDeletedFalse(UUID businessId);

    boolean existsByBusinessIdAndNameAndDeletedFalse(UUID businessId, String name);
}
