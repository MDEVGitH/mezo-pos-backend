package com.mezo.pos.product.infrastructure.adapter;

import com.mezo.pos.product.domain.entity.Product;
import com.mezo.pos.product.domain.port.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class JpaProductRepository implements ProductRepository {

    private final SpringDataProductRepository springRepo;

    @Override
    public Product save(Product product) {
        return springRepo.save(product);
    }

    @Override
    public Optional<Product> findById(UUID id) {
        return springRepo.findByIdAndDeletedFalse(id);
    }

    @Override
    public List<Product> findByBusinessId(UUID businessId) {
        return springRepo.findByBusinessIdAndDeletedFalseOrderByNameAsc(businessId);
    }

    @Override
    public List<Product> findByBusinessIdAndCategoryId(UUID businessId, UUID categoryId) {
        return springRepo.findByBusinessIdAndCategoryIdAndDeletedFalseOrderByNameAsc(businessId, categoryId);
    }

    @Override
    public long countByBusinessId(UUID businessId) {
        return springRepo.countByBusinessIdAndDeletedFalse(businessId);
    }

    @Override
    public long countByBusinessIdAndCategoryId(UUID businessId, UUID categoryId) {
        return springRepo.countByBusinessIdAndCategoryIdAndDeletedFalse(businessId, categoryId);
    }

    @Override
    public void deleteById(UUID id) {
        springRepo.deleteById(id);
    }
}
