package com.mezo.pos.product.application;

import com.mezo.pos.product.domain.entity.Category;
import com.mezo.pos.product.domain.port.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ListCategoriesUseCase {

    private final CategoryRepository categoryRepository;

    @Transactional(readOnly = true)
    public List<Category> execute(UUID businessId) {
        return categoryRepository.findByBusinessId(businessId);
    }
}
