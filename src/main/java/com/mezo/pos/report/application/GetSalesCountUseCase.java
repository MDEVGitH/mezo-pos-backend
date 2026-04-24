package com.mezo.pos.report.application;

import com.mezo.pos.auth.domain.entity.User;
import com.mezo.pos.plan.domain.service.PlanEnforcer;
import com.mezo.pos.report.domain.port.ReportRepository;
import com.mezo.pos.shared.application.TimeRangeResolver;
import com.mezo.pos.shared.domain.valueobject.TimeRange;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class GetSalesCountUseCase {

    private final ReportRepository reportRepository;
    private final TimeRangeResolver timeRangeResolver;
    private final PlanEnforcer planEnforcer;

    @Transactional(readOnly = true)
    public long execute(UUID businessId, String time) {
        User owner = planEnforcer.resolveOwner(businessId);
        planEnforcer.validateCanAccessReports(owner);

        TimeRange range = timeRangeResolver.resolve(time);
        return reportRepository.getSalesCount(businessId, range.getFrom(), range.getTo());
    }
}
