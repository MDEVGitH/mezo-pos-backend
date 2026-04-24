package com.mezo.pos.order.domain.entity;

import com.mezo.pos.order.domain.enums.OrderStatus;
import com.mezo.pos.order.domain.enums.PaymentMethod;
import com.mezo.pos.shared.domain.entity.BaseEntity;
import com.mezo.pos.shared.domain.exception.DomainException;
import com.mezo.pos.shared.domain.exception.NotFoundException;
import com.mezo.pos.shared.domain.valueobject.Currency;
import com.mezo.pos.shared.domain.valueobject.Money;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "orders")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Order extends BaseEntity {

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "order_id")
    @Builder.Default
    private List<OrderLine> lines = new ArrayList<>();

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "amount", column = @Column(name = "tip_amount")),
            @AttributeOverride(name = "currency", column = @Column(name = "tip_currency"))
    })
    private Money tip;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "amount", column = @Column(name = "total_amount", nullable = false)),
            @AttributeOverride(name = "currency", column = @Column(name = "total_currency", nullable = false))
    })
    private Money total;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_method")
    private PaymentMethod paymentMethod;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private OrderStatus status = OrderStatus.OPEN;

    @Column(name = "business_id", nullable = false)
    private UUID businessId;

    @Column(name = "table_id")
    private UUID tableId;

    @Column(name = "created_by", nullable = false)
    private UUID createdBy;

    public void addLine(OrderLine line) {
        assertOpen();
        this.lines.add(line);
        recalculateTotal();
    }

    public void removeLine(UUID lineId) {
        assertOpen();
        boolean removed = this.lines.removeIf(l -> l.getId().equals(lineId));
        if (!removed) {
            throw new NotFoundException("OrderLine not found: " + lineId);
        }
        recalculateTotal();
    }

    public void cancel() {
        assertOpen();
        this.status = OrderStatus.CANCELLED;
    }

    public void assertOpen() {
        if (this.status != OrderStatus.OPEN) {
            throw new DomainException("Order must be OPEN to modify. Current status: " + this.status);
        }
    }

    public void recalculateTotal() {
        BigDecimal sum = lines.stream()
                .map(l -> l.getSubtotal().getAmount())
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal tipAmount = tip != null ? tip.getAmount() : BigDecimal.ZERO;
        this.total = new Money(sum.add(tipAmount), Currency.COP);
    }
}
