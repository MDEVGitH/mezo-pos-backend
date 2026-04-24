package com.mezo.pos.table.infrastructure.web.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TableSummaryResponse {

    private UUID tableId;
    private int tableNumber;
    private int orderCount;
    private List<ItemSummary> items;
    private BigDecimal totalTip;
    private BigDecimal grandTotal;
    private String currency;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ItemSummary {
        private UUID productId;
        private String productName;
        private BigDecimal unitPrice;
        private int quantity;
        private BigDecimal subtotal;
    }
}
