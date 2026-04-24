package com.mezo.pos.sale.infrastructure.adapter;

import com.mezo.pos.sale.domain.entity.Sale;
import com.mezo.pos.sale.domain.port.SaleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class JpaSaleRepository implements SaleRepository {

    private final SpringDataSaleRepository springRepo;

    @Override
    public Sale save(Sale sale) {
        return springRepo.save(sale);
    }

    @Override
    public Optional<Sale> findById(UUID id) {
        return springRepo.findById(id)
                .filter(s -> !s.isDeleted());
    }

    @Override
    public List<Sale> findByBusinessId(UUID businessId) {
        return springRepo.findByBusinessIdAndDeletedFalse(businessId);
    }

    @Override
    public List<Sale> findByBusinessIdAndCreatedAtBetween(UUID businessId, LocalDateTime from, LocalDateTime to) {
        return springRepo.findByBusinessIdAndCreatedAtBetweenAndDeletedFalse(businessId, from, to);
    }
}
