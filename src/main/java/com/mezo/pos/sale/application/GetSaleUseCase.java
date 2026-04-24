package com.mezo.pos.sale.application;

import com.mezo.pos.sale.domain.entity.Sale;
import com.mezo.pos.sale.domain.port.SaleRepository;
import com.mezo.pos.sale.infrastructure.web.dto.SaleResponse;
import com.mezo.pos.shared.domain.exception.DomainException;
import com.mezo.pos.shared.domain.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class GetSaleUseCase {

    private final SaleRepository saleRepository;

    @Transactional(readOnly = true)
    public SaleResponse execute(UUID saleId, UUID businessId) {
        Sale sale = saleRepository.findById(saleId)
                .orElseThrow(() -> new NotFoundException("Sale not found: " + saleId));

        if (!sale.getBusinessId().equals(businessId)) {
            throw new DomainException("Sale does not belong to this business");
        }

        return SaleResponse.fromEntity(sale);
    }
}
