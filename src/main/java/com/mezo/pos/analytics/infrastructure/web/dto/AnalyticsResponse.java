package com.mezo.pos.analytics.infrastructure.web.dto;

import com.mezo.pos.analytics.domain.model.TimeSeriesPoint;
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
public class AnalyticsResponse {

    private String time;
    private String currency;
    private List<TimeSeriesPoint> data;
}
