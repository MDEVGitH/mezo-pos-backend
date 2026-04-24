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
public class PeakHourResponse {

    private String time;
    private String from;
    private String to;
    private int hour;
    private String label;
    private long salesCount;
    private BigDecimal total;
    private String currency;
}
