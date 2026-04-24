package com.mezo.pos.product.application;

import com.mezo.pos.product.domain.entity.Category;
import com.mezo.pos.product.domain.port.CategoryRepository;
import com.mezo.pos.product.domain.port.ProductRepository;
import com.mezo.pos.shared.domain.exception.DomainException;
import com.mezo.pos.shared.domain.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DeleteCategoryUseCase {

    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;

    @Transactional
    public void execute(UUID businessId, UUID categoryId) {
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new NotFoundException("Category not found: " + categoryId));

        if (!category.getBusinessId().equals(businessId)) {
            throw new DomainException("Category does not belong to this business");
        }

        long productCount = productRepository.countByBusinessIdAndCategoryId(businessId, categoryId);
        if (productCount > 0) {
            throw new DomainException("Cannot delete category with " + productCount + " associated products. Remove or reassign products first.");
        }

        category.softDelete();
        categoryRepository.save(category);
    }
}
