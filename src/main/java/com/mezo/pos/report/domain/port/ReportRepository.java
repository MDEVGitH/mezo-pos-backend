package com.mezo.pos.report.domain.port;

import com.mezo.pos.report.domain.model.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface ReportRepository {

    SalesTotal getSalesTotal(UUID businessId, LocalDateTime from, LocalDateTime to);

    long getSalesCount(UUID businessId, LocalDateTime from, LocalDateTime to);

    PeakHour getPeakHour(UUID businessId, LocalDateTime from, LocalDateTime to);

    List<TopProduct> getTopProducts(UUID businessId, LocalDateTime from, LocalDateTime to, int limit);

    List<PaymentMethodStats> getPaymentMethodStats(UUID businessId, LocalDateTime from, LocalDateTime to);
}
