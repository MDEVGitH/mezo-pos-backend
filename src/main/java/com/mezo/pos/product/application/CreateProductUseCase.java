package com.mezo.pos.product.application;

import com.mezo.pos.product.domain.entity.Category;
import com.mezo.pos.product.domain.entity.Product;
import com.mezo.pos.product.domain.enums.ImageType;
import com.mezo.pos.product.domain.port.CategoryRepository;
import com.mezo.pos.product.domain.port.ProductRepository;
import com.mezo.pos.shared.domain.exception.DomainException;
import com.mezo.pos.shared.domain.exception.NotFoundException;
import com.mezo.pos.shared.domain.valueobject.Currency;
import com.mezo.pos.shared.domain.valueobject.Money;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CreateProductUseCase {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;

    @Transactional
    public Product execute(UUID businessId, String name, BigDecimal price, Currency currency,
                           String description, String ingredients, BigDecimal productionCost,
                           ImageType imageType, String image, UUID categoryId) {

        if (categoryId != null) {
            Category category = categoryRepository.findById(categoryId)
                    .orElseThrow(() -> new NotFoundException("Category not found: " + categoryId));
            if (!category.getBusinessId().equals(businessId)) {
                throw new DomainException("Category does not belong to this business");
            }
        }

        if (price == null || price.compareTo(BigDecimal.ZERO) <= 0) {
            throw new DomainException("Price must be greater than 0");
        }

        Product product = Product.builder()
                .name(name)
                .price(new Money(price, currency))
                .description(description)
                .ingredients(ingredients)
                .productionCost(productionCost != null ? new Money(productionCost, currency) : null)
                .imageType(imageType)
                .image(image)
                .available(true)
                .categoryId(categoryId)
                .businessId(businessId)
                .build();

        return productRepository.save(product);
    }
}
