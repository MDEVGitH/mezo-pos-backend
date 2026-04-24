package com.mezo.pos.sale.infrastructure.adapter;

import com.mezo.pos.sale.domain.entity.Sale;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface SpringDataSaleRepository extends JpaRepository<Sale, UUID> {

    List<Sale> findByBusinessIdAndDeletedFalse(UUID businessId);

    List<Sale> findByBusinessIdAndCreatedAtBetweenAndDeletedFalse(
            UUID businessId, LocalDateTime from, LocalDateTime to);
}
