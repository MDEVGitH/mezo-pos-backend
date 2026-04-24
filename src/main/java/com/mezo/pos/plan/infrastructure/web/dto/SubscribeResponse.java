package com.mezo.pos.plan.infrastructure.web.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SubscribeResponse {
    private String paymentUrl;
    private String planType;
    private BigDecimal price;
    private String currency;
}
