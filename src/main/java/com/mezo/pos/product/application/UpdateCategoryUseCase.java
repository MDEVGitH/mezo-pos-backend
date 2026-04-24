package com.mezo.pos.product.application;

import com.mezo.pos.product.domain.entity.Category;
import com.mezo.pos.product.domain.port.CategoryRepository;
import com.mezo.pos.shared.domain.exception.DomainException;
import com.mezo.pos.shared.domain.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UpdateCategoryUseCase {

    private final CategoryRepository categoryRepository;

    @Transactional
    public Category execute(UUID businessId, UUID categoryId, String name, String icon) {
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new NotFoundException("Category not found: " + categoryId));

        if (!category.getBusinessId().equals(businessId)) {
            throw new DomainException("Category does not belong to this business");
        }

        if (name != null && !name.equals(category.getName())) {
            if (categoryRepository.existsByBusinessIdAndName(businessId, name)) {
                throw new DomainException("A category with name '" + name + "' already exists in this business");
            }
            category.setName(name);
        }

        if (icon != null) {
            category.setIcon(icon);
        }

        return categoryRepository.save(category);
    }
}
