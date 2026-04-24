package com.mezo.pos.analytics.infrastructure.web;

import com.mezo.pos.analytics.application.GetAnalyticsUseCase;
import com.mezo.pos.analytics.domain.model.TimeSeriesPoint;
import com.mezo.pos.analytics.infrastructure.web.dto.AnalyticsResponse;
import com.mezo.pos.shared.application.TimeRangeResolver;
import com.mezo.pos.shared.domain.valueobject.TimeRange;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/businesses/{businessId}/analytics")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AnalyticsController {

    private final GetAnalyticsUseCase getAnalyticsUseCase;
    private final TimeRangeResolver timeRangeResolver;

    @GetMapping
    public ResponseEntity<AnalyticsResponse> getAnalytics(
            @PathVariable UUID businessId,
            @RequestParam String time) {

        TimeRange range = timeRangeResolver.resolve(time);
        List<TimeSeriesPoint> data = getAnalyticsUseCase.execute(businessId, time);

        AnalyticsResponse response = AnalyticsResponse.builder()
                .time(range.getTimeParam())
                .currency("COP")
                .data(data)
                .build();

        return ResponseEntity.ok(response);
    }
}
