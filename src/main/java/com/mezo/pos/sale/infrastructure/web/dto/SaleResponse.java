package com.mezo.pos.sale.infrastructure.web.dto;

import com.mezo.pos.sale.domain.entity.Sale;
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
public class SaleResponse {

    private UUID id;
    private UUID orderId;
    private BigDecimal total;
    private BigDecimal tip;
    private String currency;
    private String paymentMethod;
    private UUID tableId;
    private UUID closedBy;
    private LocalDateTime createdAt;

    public static SaleResponse fromEntity(Sale sale) {
        return SaleResponse.builder()
                .id(sale.getId())
                .orderId(sale.getOrderId())
                .total(sale.getTotal().getAmount())
                .tip(sale.getTip() != null ? sale.getTip().getAmount() : BigDecimal.ZERO)
                .currency(sale.getTotal().getCurrency().name())
                .paymentMethod(sale.getPaymentMethod().name())
                .tableId(sale.getTableId())
                .closedBy(sale.getClosedBy())
                .createdAt(sale.getCreatedAt())
                .build();
    }
}
