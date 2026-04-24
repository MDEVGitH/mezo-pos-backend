package com.mezo.pos.analytics.domain.port;

import com.mezo.pos.analytics.domain.model.TimeSeriesPoint;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface AnalyticsRepository {

    List<TimeSeriesPoint> getTimeSeries(UUID businessId, String timeRange, LocalDateTime from, LocalDateTime to);
}
