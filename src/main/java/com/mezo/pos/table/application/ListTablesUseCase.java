package com.mezo.pos.table.application;

import com.mezo.pos.table.domain.entity.RestaurantTable;
import com.mezo.pos.table.domain.port.TableRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ListTablesUseCase {

    private final TableRepository tableRepository;

    @Transactional(readOnly = true)
    public List<RestaurantTable> execute(UUID businessId) {
        return tableRepository.findByBusinessId(businessId);
    }
}
