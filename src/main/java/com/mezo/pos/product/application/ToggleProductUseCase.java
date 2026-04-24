package com.mezo.pos.product.application;

import com.mezo.pos.product.domain.entity.Product;
import com.mezo.pos.product.domain.port.ProductRepository;
import com.mezo.pos.shared.domain.exception.DomainException;
import com.mezo.pos.shared.domain.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ToggleProductUseCase {

    private final ProductRepository productRepository;

    @Transactional
    public Product execute(UUID businessId, UUID productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new NotFoundException("Product not found: " + productId));

        if (!product.getBusinessId().equals(businessId)) {
            throw new DomainException("Product does not belong to this business");
        }

        product.setAvailable(!product.isAvailable());
        return productRepository.save(product);
    }
}
