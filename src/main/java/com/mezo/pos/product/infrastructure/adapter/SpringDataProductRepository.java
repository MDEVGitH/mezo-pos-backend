package com.mezo.pos.product.infrastructure.adapter;

import com.mezo.pos.product.domain.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SpringDataProductRepository extends JpaRepository<Product, UUID> {

    Optional<Product> findByIdAndDeletedFalse(UUID id);

    List<Product> findByBusinessIdAndDeletedFalseOrderByNameAsc(UUID businessId);

    List<Product> findByBusinessIdAndCategoryIdAndDeletedFalseOrderByNameAsc(UUID businessId, UUID categoryId);

    long countByBusinessIdAndDeletedFalse(UUID businessId);

    long countByBusinessIdAndCategoryIdAndDeletedFalse(UUID businessId, UUID categoryId);
}
