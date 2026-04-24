package com.mezo.pos.order.infrastructure.web.dto;

import com.mezo.pos.order.domain.entity.Order;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderResponse {

    private UUID id;
    private UUID tableId;
    private String status;
    private String paymentMethod;
    private BigDecimal tip;
    private BigDecimal total;
    private String currency;
    private List<OrderLineResponse> lines;
    private UUID createdBy;
    private LocalDateTime createdAt;

    public static OrderResponse fromEntity(Order order) {
        return OrderResponse.builder()
                .id(order.getId())
                .tableId(order.getTableId())
                .status(order.getStatus().name())
                .paymentMethod(order.getPaymentMethod() != null ? order.getPaymentMethod().name() : null)
                .tip(order.getTip() != null ? order.getTip().getAmount() : BigDecimal.ZERO)
                .total(order.getTotal().getAmount())
                .currency(order.getTotal().getCurrency().name())
                .lines(order.getLines().stream()
                        .map(OrderLineResponse::fromEntity)
                        .toList())
                .createdBy(order.getCreatedBy())
                .createdAt(order.getCreatedAt())
                .build();
    }
}
