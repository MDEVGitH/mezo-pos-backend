package com.mezo.pos.business.application;

import com.mezo.pos.business.domain.entity.Business;
import com.mezo.pos.business.domain.port.BusinessRepository;
import com.mezo.pos.business.infrastructure.web.dto.BusinessResponse;
import com.mezo.pos.shared.domain.exception.NotFoundException;
import com.mezo.pos.table.domain.port.TableRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class GetBusinessUseCase {

    private final BusinessRepository businessRepository;
    private final TableRepository tableRepository;

    @Transactional(readOnly = true)
    public BusinessResponse findById(UUID id) {
        Business business = businessRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Business not found: " + id));
        return toResponse(business);
    }

    @Transactional(readOnly = true)
    public List<BusinessResponse> findByOwnerId(UUID ownerId) {
        return businessRepository.findByOwnerId(ownerId).stream()
                .map(this::toResponse)
                .toList();
    }

    private BusinessResponse toResponse(Business b) {
        long tableCount = tableRepository.countByBusinessId(b.getId());
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
