package com.mezo.pos.product.infrastructure.adapter;

import com.mezo.pos.product.domain.entity.Category;
import com.mezo.pos.product.domain.port.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class JpaCategoryRepository implements CategoryRepository {

    private final SpringDataCategoryRepository springRepo;

    @Override
    public Category save(Category category) {
        return springRepo.save(category);
    }

    @Override
    public Optional<Category> findById(UUID id) {
        return springRepo.findByIdAndDeletedFalse(id);
    }

    @Override
    public List<Category> findByBusinessId(UUID businessId) {
        return springRepo.findByBusinessIdAndDeletedFalseOrderBySortOrderAsc(businessId);
    }

    @Override
    public Optional<Integer> findMaxSortOrderByBusinessId(UUID businessId) {
        return springRepo.findMaxSortOrderByBusinessId(businessId);
    }

    @Override
    public long countByBusinessId(UUID businessId) {
        return springRepo.countByBusinessIdAndDeletedFalse(businessId);
    }

    @Override
    public boolean existsByBusinessIdAndName(UUID businessId, String name) {
        return springRepo.existsByBusinessIdAndNameAndDeletedFalse(businessId, name);
    }

    @Override
    public void deleteById(UUID id) {
        springRepo.deleteById(id);
    }
}
