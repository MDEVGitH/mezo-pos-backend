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
public class UpdateProductUseCase {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;

    @Transactional
    public Product execute(UUID businessId, UUID productId, String name, BigDecimal price,
                           Currency currency, String description, String ingredients,
                           BigDecimal productionCost, ImageType imageType, String image,
                           UUID categoryId) {

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new NotFoundException("Product not found: " + productId));

        if (!product.getBusinessId().equals(businessId)) {
            throw new DomainException("Product does not belong to this business");
        }

        if (categoryId != null && !categoryId.equals(product.getCategoryId())) {
            Category category = categoryRepository.findById(categoryId)
                    .orElseThrow(() -> new NotFoundException("Category not found: " + categoryId));
            if (!category.getBusinessId().equals(businessId)) {
                throw new DomainException("Category does not belong to this business");
            }
            product.setCategoryId(categoryId);
        }

        if (price != null && price.compareTo(BigDecimal.ZERO) <= 0) {
            throw new DomainException("Price must be greater than 0");
        }

        product.setName(name);
        product.setPrice(new Money(price, currency));
        product.setDescription(description);
        product.setIngredients(ingredients);
        product.setProductionCost(productionCost != null ? new Money(productionCost, currency) : null);
        product.setImageType(imageType);
        product.setImage(image);

        return productRepository.save(product);
    }
}
