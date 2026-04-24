package com.mezo.pos.analytics.application;

import com.mezo.pos.analytics.domain.model.TimeSeriesPoint;
import com.mezo.pos.analytics.domain.port.AnalyticsRepository;
import com.mezo.pos.shared.application.TimeRangeResolver;
import com.mezo.pos.shared.domain.valueobject.TimeRange;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class GetAnalyticsUseCase {

    private final AnalyticsRepository analyticsRepository;
    private final TimeRangeResolver timeRangeResolver;

    @Transactional(readOnly = true)
    public List<TimeSeriesPoint> execute(UUID businessId, String time) {
        TimeRange range = timeRangeResolver.resolve(time);
        return analyticsRepository.getTimeSeries(businessId, range.getTimeParam(), range.getFrom(), range.getTo());
    }
}
