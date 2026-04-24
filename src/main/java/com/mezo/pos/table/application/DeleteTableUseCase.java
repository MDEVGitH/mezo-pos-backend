package com.mezo.pos.table.application;

import com.mezo.pos.table.domain.entity.RestaurantTable;
import com.mezo.pos.table.domain.port.TableRepository;
import com.mezo.pos.shared.domain.exception.DomainException;
import com.mezo.pos.shared.domain.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DeleteTableUseCase {

    private final TableRepository tableRepository;

    @Transactional
    public void execute(UUID businessId, UUID tableId) {
        RestaurantTable table = tableRepository.findById(tableId)
                .orElseThrow(() -> new NotFoundException("Table not found: " + tableId));

        if (!table.getBusinessId().equals(businessId)) {
            throw new DomainException("Table does not belong to this business");
        }

        table.softDelete();
        tableRepository.save(table);
    }
}
