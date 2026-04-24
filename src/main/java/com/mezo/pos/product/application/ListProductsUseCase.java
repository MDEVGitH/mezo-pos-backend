package com.mezo.pos.product.application;

import com.mezo.pos.product.domain.entity.Product;
import com.mezo.pos.product.domain.port.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ListProductsUseCase {

    private final ProductRepository productRepository;

    @Transactional(readOnly = true)
    public List<Product> execute(UUID businessId, UUID categoryId) {
        if (categoryId != null) {
            return productRepository.findByBusinessIdAndCategoryId(businessId, categoryId);
        }
        return productRepository.findByBusinessId(businessId);
    }
}
