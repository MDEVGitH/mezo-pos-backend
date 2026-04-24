package com.mezo.pos.table.infrastructure.web.dto;

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
public class TableResponse {

    private UUID id;
    private int number;
    private UUID businessId;
    private int activeOrderCount;
    private List<ActiveOrder> activeOrders;
    private LocalDateTime createdAt;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ActiveOrder {
        private UUID orderId;
        private String status;
        private BigDecimal total;
        private String currency;
        private LocalDateTime createdAt;
    }
}
