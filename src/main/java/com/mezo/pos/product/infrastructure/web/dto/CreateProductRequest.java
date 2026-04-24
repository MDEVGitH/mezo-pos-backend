package com.mezo.pos.product.infrastructure.web.dto;

import com.mezo.pos.product.domain.enums.ImageType;
import com.mezo.pos.shared.domain.valueobject.Currency;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateProductRequest {

    @NotBlank(message = "Name is required")
    private String name;

    @NotNull(message = "Price is required")
    @Positive(message = "Price must be positive")
    private BigDecimal price;

    @NotNull(message = "Currency is required")
    private Currency currency;

    private String description;

    private String ingredients;

    @Positive(message = "Production cost must be positive")
    private BigDecimal productionCost;

    @NotNull(message = "Image type is required")
    private ImageType imageType;

    @NotBlank(message = "Image is required")
    private String image;

    private UUID categoryId;
}
