package com.mezo.pos.product.domain.port;

import com.mezo.pos.product.domain.entity.Category;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CategoryRepository {

    Category save(Category category);

    Optional<Category> findById(UUID id);

    List<Category> findByBusinessId(UUID businessId);

    Optional<Integer> findMaxSortOrderByBusinessId(UUID businessId);

    long countByBusinessId(UUID businessId);

    boolean existsByBusinessIdAndName(UUID businessId, String name);

    void deleteById(UUID id);
}
