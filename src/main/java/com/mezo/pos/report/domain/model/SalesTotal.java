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
public class SalesTotal {

    private BigDecimal totalSales;
    private BigDecimal totalTips;
    private String currency;
    private long salesCount;
}
