package com.mezo.pos.business.application;

import com.mezo.pos.auth.domain.entity.User;
import com.mezo.pos.auth.domain.port.UserRepository;
import com.mezo.pos.business.domain.entity.Business;
import com.mezo.pos.business.domain.enums.BusinessType;
import com.mezo.pos.business.domain.port.BusinessRepository;
import com.mezo.pos.business.infrastructure.web.dto.BusinessResponse;
import com.mezo.pos.business.infrastructure.web.dto.CreateBusinessRequest;
import com.mezo.pos.plan.domain.service.PlanEnforcer;
import com.mezo.pos.shared.domain.exception.NotFoundException;
import com.mezo.pos.shared.domain.valueobject.PhoneNumber;
import com.mezo.pos.table.domain.entity.RestaurantTable;
import com.mezo.pos.table.domain.port.TableRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CreateBusinessUseCase {

    private final BusinessRepository businessRepository;
    private final UserRepository userRepository;
    private final PlanEnforcer planEnforcer;
    private final TableRepository tableRepository;

    @Transactional
    public BusinessResponse execute(CreateBusinessRequest request, UUID ownerId) {
        User owner = userRepository.findById(ownerId)
                .orElseThrow(() -> new NotFoundException("User not found: " + ownerId));

        // Validate plan limits
        long currentCount = businessRepository.countByOwnerId(ownerId);
        planEnforcer.validateCanCreateBusiness(owner, currentCount);

        // Validate table count against plan
        planEnforcer.validateCanCreateTable(owner, request.getTableCount());

        Business business = Business.builder()
                .name(request.getName())
                .type(BusinessType.valueOf(request.getType()))
                .phone(request.getPhone() != null ? new PhoneNumber(request.getPhone()) : null)
                .nit(request.getNit())
                .address(request.getAddress())
                .city(request.getCity())
                .country(request.getCountry())
                .openAt(request.getOpenAt())
                .closeAt(request.getCloseAt())
                .open(false)
                .ownerId(ownerId)
                .build();

        Business saved = businessRepository.save(business);

        // Create tables numbered 1..N
        int tableCount = request.getTableCount();
        for (int i = 1; i <= tableCount; i++) {
            RestaurantTable table = RestaurantTable.builder()
                    .number(i)
                    .businessId(saved.getId())
                    .build();
            tableRepository.save(table);
        }

        return toResponse(saved, tableCount);
    }

    private BusinessResponse toResponse(Business b, long tableCount) {
        return BusinessResponse.builder()
                .id(b.getId())
                .name(b.getName())
                .type(b.getType().name())
                .phone(b.getPhone() != null ? b.getPhone().getValue() : null)
                .nit(b.getNit())
                .address(b.getAddress())
                .city(b.getCity())
                .country(b.getCountry())
                .openAt(b.getOpenAt())
                .closeAt(b.getCloseAt())
                .tableCount(tableCount)
                .open(b.isOpen())
                .createdAt(b.getCreatedAt())
                .build();
    }
}
