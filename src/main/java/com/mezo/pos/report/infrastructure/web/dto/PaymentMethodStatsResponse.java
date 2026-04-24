package com.mezo.pos.report.infrastructure.web.dto;

import com.mezo.pos.report.domain.model.PaymentMethodStats;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentMethodStatsResponse {

    private String time;
    private String from;
    private String to;
    private List<PaymentMethodStats> methods;
}
