package com.mezo.pos.product.infrastructure.web.dto;

import com.mezo.pos.product.domain.enums.ImageType;
import com.mezo.pos.shared.domain.valueobject.Currency;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductResponse {

    private UUID id;
    private String name;
    private BigDecimal price;
    private Currency currency;
    private String description;
    private String ingredients;
    private BigDecimal productionCost;
    private ImageType imageType;
    private String image;
    private boolean available;
    private UUID categoryId;
    private UUID businessId;
    private LocalDateTime createdAt;
}
