package com.mezo.pos.sale.domain.entity;

import com.mezo.pos.order.domain.enums.PaymentMethod;
import com.mezo.pos.shared.domain.entity.BaseEntity;
import com.mezo.pos.shared.domain.valueobject.Money;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Entity
@Table(name = "sales")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Sale extends BaseEntity {

    @Column(name = "order_id", nullable = false, unique = true)
    private UUID orderId;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "amount", column = @Column(name = "total_amount", nullable = false)),
            @AttributeOverride(name = "currency", column = @Column(name = "total_currency", nullable = false))
    })
    private Money total;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "amount", column = @Column(name = "tip_amount")),
            @AttributeOverride(name = "currency", column = @Column(name = "tip_currency"))
    })
    private Money tip;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_method", nullable = false)
    private PaymentMethod paymentMethod;

    @Column(name = "business_id", nullable = false)
    private UUID businessId;

    @Column(name = "table_id")
    private UUID tableId;

    @Column(name = "closed_by", nullable = false)
    private UUID closedBy;
}
