package com.mezo.pos.analytics.domain.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TimeSeriesPoint {

    private String label;
    private String date;
    private BigDecimal total;
    private long count;
}
