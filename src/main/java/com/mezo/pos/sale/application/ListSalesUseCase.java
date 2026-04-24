package com.mezo.pos.sale.application;

import com.mezo.pos.sale.domain.entity.Sale;
import com.mezo.pos.sale.domain.port.SaleRepository;
import com.mezo.pos.sale.infrastructure.web.dto.SaleResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ListSalesUseCase {

    private final SaleRepository saleRepository;

    @Transactional(readOnly = true)
    public List<SaleResponse> execute(UUID businessId, LocalDate from, LocalDate to) {
        List<Sale> sales;

        if (from != null && to != null) {
            LocalDateTime fromDateTime = from.atStartOfDay();
            LocalDateTime toDateTime = to.atTime(LocalTime.MAX);
            sales = saleRepository.findByBusinessIdAndCreatedAtBetween(businessId, fromDateTime, toDateTime);
        } else {
            sales = saleRepository.findByBusinessId(businessId);
        }

        return sales.stream()
                .map(SaleResponse::fromEntity)
                .toList();
    }
}
