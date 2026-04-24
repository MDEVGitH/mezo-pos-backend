package com.mezo.pos.table.application;

import com.mezo.pos.auth.domain.entity.User;
import com.mezo.pos.plan.domain.service.PlanEnforcer;
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
    private final PlanEnforcer planEnforcer;

    @Transactional
    public RestaurantTable execute(UUID businessId) {
        User owner = planEnforcer.resolveOwner(businessId);
        long currentCount = tableRepository.countByBusinessId(businessId);
        planEnforcer.validateCanCreateTable(owner, currentCount);

        int maxNumber = tableRepository.findMaxNumberByBusinessId(businessId).orElse(0);

        RestaurantTable table = RestaurantTable.builder()
                .number(maxNumber + 1)
                .businessId(businessId)
                .build();

        return tableRepository.save(table);
    }
}
