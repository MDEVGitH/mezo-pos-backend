package com.mezo.pos.product.domain.entity;

import com.mezo.pos.product.domain.enums.ImageType;
import com.mezo.pos.shared.domain.entity.BaseEntity;
import com.mezo.pos.shared.domain.valueobject.Money;
import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "products")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Product extends BaseEntity {

    @Column(nullable = false)
    private String name;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "amount", column = @Column(name = "price", nullable = false)),
            @AttributeOverride(name = "currency", column = @Column(name = "currency", nullable = false))
    })
    private Money price;

    private String description;

    private String ingredients;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "amount", column = @Column(name = "production_cost")),
            @AttributeOverride(name = "currency", column = @Column(name = "production_cost_currency"))
    })
    private Money productionCost;

    @Enumerated(EnumType.STRING)
    @Column(name = "image_type", nullable = false)
    private ImageType imageType;

    private String image;

    @Column(nullable = false)
    private boolean available = true;

    @Column(name = "category_id")
    private UUID categoryId;

    @Column(name = "business_id", nullable = false)
    private UUID businessId;
}
