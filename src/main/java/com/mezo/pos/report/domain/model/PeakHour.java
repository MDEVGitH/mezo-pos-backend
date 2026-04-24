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
public class PeakHour {

    private int hour;
    private String label;
    private long salesCount;
    private BigDecimal total;
}
