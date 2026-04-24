package com.mezo.pos.table.application;

import com.mezo.pos.table.domain.entity.RestaurantTable;
import com.mezo.pos.table.domain.port.TableRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CreateTableUseCase {

    private final TableRepository tableRepository;

    @Transactional
    public RestaurantTable execute(UUID businessId) {
        int maxNumber = tableRepository.findMaxNumberByBusinessId(businessId).orElse(0);

        RestaurantTable table = RestaurantTable.builder()
                .number(maxNumber + 1)
                .businessId(businessId)
                .build();

        return tableRepository.save(table);
    }
}
