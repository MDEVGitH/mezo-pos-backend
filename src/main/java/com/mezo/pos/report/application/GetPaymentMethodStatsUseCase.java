package com.mezo.pos.report.application;

import com.mezo.pos.report.domain.model.PaymentMethodStats;
import com.mezo.pos.report.domain.port.ReportRepository;
import com.mezo.pos.shared.application.TimeRangeResolver;
import com.mezo.pos.shared.domain.valueobject.TimeRange;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class GetPaymentMethodStatsUseCase {

    private final ReportRepository reportRepository;
    private final TimeRangeResolver timeRangeResolver;

    @Transactional(readOnly = true)
    public List<PaymentMethodStats> execute(UUID businessId, String time) {
        TimeRange range = timeRangeResolver.resolve(time);
        return reportRepository.getPaymentMethodStats(businessId, range.getFrom(), range.getTo());
    }
}
