package com.mezo.pos.plan.infrastructure.web.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PlanResponse {
    private String type;
    private int maxTables;
    private int maxProducts;
    private int maxCategories;
    private int maxEmployees;
    private int maxBusinesses;
    private boolean reportsEnabled;
    private BigDecimal price;
    private String currency;
    private int trialDays;
    private LocalDateTime planStartedAt;
    private LocalDateTime planExpiresAt;
    private boolean isTrialActive;
    private SubscriptionInfo subscription;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SubscriptionInfo {
        private String status;
        private String wompiSubscriptionId;
    }
}
