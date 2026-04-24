package com.mezo.pos.order.infrastructure.web.dto;

import com.mezo.pos.order.domain.entity.OrderLine;
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
public class OrderLineResponse {

    private UUID id;
    private UUID productId;
    private String productName;
    private BigDecimal unitPrice;
    private int quantity;
    private BigDecimal subtotal;

    public static OrderLineResponse fromEntity(OrderLine line) {
        return OrderLineResponse.builder()
                .id(line.getId())
                .productId(line.getProductId())
                .productName(line.getProductName())
                .unitPrice(line.getUnitPrice().getAmount())
                .quantity(line.getQuantity())
                .subtotal(line.getSubtotal().getAmount())
                .build();
    }
}
