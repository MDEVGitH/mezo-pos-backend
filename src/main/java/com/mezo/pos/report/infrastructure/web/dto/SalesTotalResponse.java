package com.mezo.pos.report.infrastructure.web.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SalesTotalResponse {

    private String time;
    private String from;
    private String to;
    private BigDecimal totalSales;
    private BigDecimal totalTips;
    private String currency;
    private long salesCount;
}
