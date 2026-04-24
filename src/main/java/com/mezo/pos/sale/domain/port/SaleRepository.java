package com.mezo.pos.sale.domain.port;

import com.mezo.pos.sale.domain.entity.Sale;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SaleRepository {

    Sale save(Sale sale);

    Optional<Sale> findById(UUID id);

    List<Sale> findByBusinessId(UUID businessId);

    List<Sale> findByBusinessIdAndCreatedAtBetween(UUID businessId, LocalDateTime from, LocalDateTime to);
}
