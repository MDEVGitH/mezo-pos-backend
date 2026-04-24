package com.mezo.pos.product.domain.port;

import com.mezo.pos.product.domain.entity.Product;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ProductRepository {

    Product save(Product product);

    Optional<Product> findById(UUID id);

    List<Product> findByBusinessId(UUID businessId);

    List<Product> findByBusinessIdAndCategoryId(UUID businessId, UUID categoryId);

    long countByBusinessId(UUID businessId);

    long countByBusinessIdAndCategoryId(UUID businessId, UUID categoryId);

    void deleteById(UUID id);
}
