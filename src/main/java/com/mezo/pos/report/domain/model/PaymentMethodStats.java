package com.mezo.pos.report.domain.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PaymentMethodStats {

    private String method;
    private long count;
    private BigDecimal total;
    private double percentage;
}
