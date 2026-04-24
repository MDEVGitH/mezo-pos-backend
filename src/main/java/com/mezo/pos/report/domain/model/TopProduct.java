package com.mezo.pos.report.domain.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TopProduct {

    private UUID productId;
    private String name;
    private long totalSold;
    private BigDecimal revenue;
}
