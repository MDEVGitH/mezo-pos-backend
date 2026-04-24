package com.mezo.pos.product.application;

import com.mezo.pos.auth.domain.entity.User;
import com.mezo.pos.plan.domain.service.PlanEnforcer;
import com.mezo.pos.product.domain.entity.Category;
import com.mezo.pos.product.domain.port.CategoryRepository;
import com.mezo.pos.shared.domain.exception.DomainException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CreateCategoryUseCase {

    private final CategoryRepository categoryRepository;
    private final PlanEnforcer planEnforcer;

    @Transactional
    public Category execute(UUID businessId, String name, String icon) {
        User owner = planEnforcer.resolveOwner(businessId);
        long currentCount = categoryRepository.countByBusinessId(businessId);
        planEnforcer.validateCanCreateCategory(owner, currentCount);

        if (categoryRepository.existsByBusinessIdAndName(businessId, name)) {
            throw new DomainException("A category with name '" + name + "' already exists in this business");
        }

        int maxSortOrder = categoryRepository.findMaxSortOrderByBusinessId(businessId).orElse(0);

        Category category = Category.builder()
                .name(name)
                .icon(icon)
                .sortOrder(maxSortOrder + 1)
                .businessId(businessId)
                .build();

        return categoryRepository.save(category);
    }
}
