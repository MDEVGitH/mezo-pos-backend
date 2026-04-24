package com.mezo.pos.report.infrastructure.web;

import com.mezo.pos.report.application.*;
import com.mezo.pos.report.domain.model.*;
import com.mezo.pos.report.infrastructure.web.dto.*;
import com.mezo.pos.shared.application.TimeRangeResolver;
import com.mezo.pos.shared.domain.valueobject.TimeRange;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/businesses/{businessId}/reports")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class ReportController {

    private final GetSalesTotalUseCase getSalesTotalUseCase;
    private final GetSalesCountUseCase getSalesCountUseCase;
    private final GetPeakHourUseCase getPeakHourUseCase;
    private final GetTopProductsUseCase getTopProductsUseCase;
    private final GetPaymentMethodStatsUseCase getPaymentMethodStatsUseCase;
    private final TimeRangeResolver timeRangeResolver;

    @GetMapping("/sales")
    public ResponseEntity<SalesTotalResponse> getSalesTotal(
            @PathVariable UUID businessId,
            @RequestParam String time) {

        TimeRange range = timeRangeResolver.resolve(time);
        SalesTotal salesTotal = getSalesTotalUseCase.execute(businessId, time);

        SalesTotalResponse response = SalesTotalResponse.builder()
                .time(range.getTimeParam())
                .from(range.getFrom().toLocalDate().toString())
                .to(range.getTo().toLocalDate().toString())
                .totalSales(salesTotal.getTotalSales())
                .totalTips(salesTotal.getTotalTips())
                .currency(salesTotal.getCurrency())
                .salesCount(salesTotal.getSalesCount())
                .build();

        return ResponseEntity.ok(response);
    }

    @GetMapping("/count")
    public ResponseEntity<SalesCountResponse> getSalesCount(
            @PathVariable UUID businessId,
            @RequestParam String time) {

        TimeRange range = timeRangeResolver.resolve(time);
        long count = getSalesCountUseCase.execute(businessId, time);

        SalesCountResponse response = SalesCountResponse.builder()
                .time(range.getTimeParam())
                .from(range.getFrom().toLocalDate().toString())
                .to(range.getTo().toLocalDate().toString())
                .salesCount(count)
                .build();

        return ResponseEntity.ok(response);
    }

    @GetMapping("/peak-hour")
    public ResponseEntity<PeakHourResponse> getPeakHour(
            @PathVariable UUID businessId,
            @RequestParam String time) {

        TimeRange range = timeRangeResolver.resolve(time);
        PeakHour peakHour = getPeakHourUseCase.execute(businessId, time);

        PeakHourResponse response = PeakHourResponse.builder()
                .time(range.getTimeParam())
                .from(range.getFrom().toLocalDate().toString())
                .to(range.getTo().toLocalDate().toString())
                .hour(peakHour.getHour())
                .label(peakHour.getLabel())
                .salesCount(peakHour.getSalesCount())
                .total(peakHour.getTotal())
                .currency("COP")
                .build();

        return ResponseEntity.ok(response);
    }

    @GetMapping("/top-products")
    public ResponseEntity<TopProductResponse> getTopProducts(
            @PathVariable UUID businessId,
            @RequestParam String time,
            @RequestParam(defaultValue = "10") int limit) {

        TimeRange range = timeRangeResolver.resolve(time);
        List<TopProduct> products = getTopProductsUseCase.execute(businessId, time, limit);

        TopProductResponse response = TopProductResponse.builder()
                .time(range.getTimeParam())
                .from(range.getFrom().toLocalDate().toString())
                .to(range.getTo().toLocalDate().toString())
                .products(products)
                .build();

        return ResponseEntity.ok(response);
    }

    @GetMapping("/payment-methods")
    public ResponseEntity<PaymentMethodStatsResponse> getPaymentMethodStats(
            @PathVariable UUID businessId,
            @RequestParam String time) {

        TimeRange range = timeRangeResolver.resolve(time);
        List<PaymentMethodStats> methods = getPaymentMethodStatsUseCase.execute(businessId, time);

        PaymentMethodStatsResponse response = PaymentMethodStatsResponse.builder()
                .time(range.getTimeParam())
                .from(range.getFrom().toLocalDate().toString())
                .to(range.getTo().toLocalDate().toString())
                .methods(methods)
                .build();

        return ResponseEntity.ok(response);
    }
}
