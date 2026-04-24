package com.mezo.pos.order.domain.entity;

import com.mezo.pos.shared.domain.entity.BaseEntity;
import com.mezo.pos.shared.domain.valueobject.Money;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Entity
@Table(name = "order_lines")
@Getter
@Setter
@NoArgsConstructor
public class OrderLine extends BaseEntity {

    @Column(name = "product_id", nullable = false)
    private UUID productId;

    @Column(name = "product_name", nullable = false)
    private String productName;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "amount", column = @Column(name = "unit_price", nullable = false)),
            @AttributeOverride(name = "currency", column = @Column(name = "unit_currency", nullable = false))
    })
    private Money unitPrice;

    @Column(nullable = false)
    private int quantity;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "amount", column = @Column(name = "subtotal_amount", nullable = false)),
            @AttributeOverride(name = "currency", column = @Column(name = "subtotal_currency", nullable = false))
    })
    private Money subtotal;

    public OrderLine(UUID productId, String productName, Money unitPrice, int quantity) {
        this.productId = productId;
        this.productName = productName;
        this.unitPrice = unitPrice;
        this.quantity = quantity;
        this.subtotal = unitPrice.multiply(quantity);
    }
}
