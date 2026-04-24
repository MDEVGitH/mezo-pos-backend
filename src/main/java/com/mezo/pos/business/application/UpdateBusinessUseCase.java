package com.mezo.pos.business.application;

import com.mezo.pos.business.domain.entity.Business;
import com.mezo.pos.business.domain.enums.BusinessType;
import com.mezo.pos.business.domain.port.BusinessRepository;
import com.mezo.pos.business.infrastructure.web.dto.BusinessResponse;
import com.mezo.pos.business.infrastructure.web.dto.UpdateBusinessRequest;
import com.mezo.pos.shared.domain.exception.NotFoundException;
import com.mezo.pos.shared.domain.valueobject.PhoneNumber;
import com.mezo.pos.table.domain.port.TableRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UpdateBusinessUseCase {

    private final BusinessRepository businessRepository;
    private final TableRepository tableRepository;

    @Transactional
    public BusinessResponse execute(UUID businessId, UpdateBusinessRequest request) {
        Business business = businessRepository.findById(businessId)
                .orElseThrow(() -> new NotFoundException("Business not found: " + businessId));

        if (request.getName() != null) {
            business.setName(request.getName());
        }
        if (request.getType() != null) {
            business.setType(BusinessType.valueOf(request.getType()));
        }
        if (request.getPhone() != null) {
            business.setPhone(new PhoneNumber(request.getPhone()));
        }
        if (request.getNit() != null) {
            business.setNit(request.getNit());
        }
        if (request.getAddress() != null) {
            business.setAddress(request.getAddress());
        }
        if (request.getCity() != null) {
            business.setCity(request.getCity());
        }
        if (request.getCountry() != null) {
            business.setCountry(request.getCountry());
        }
        if (request.getOpenAt() != null) {
            business.setOpenAt(request.getOpenAt());
        }
        if (request.getCloseAt() != null) {
            business.setCloseAt(request.getCloseAt());
        }

        Business saved = businessRepository.save(business);
        return toResponse(saved);
    }

    @Transactional
    public BusinessResponse toggleStatus(UUID businessId) {
        Business business = businessRepository.findById(businessId)
                .orElseThrow(() -> new NotFoundException("Business not found: " + businessId));

        business.setOpen(!business.isOpen());
        Business saved = businessRepository.save(business);
        return toResponse(saved);
    }

    @Transactional
    public void delete(UUID businessId) {
        Business business = businessRepository.findById(businessId)
                .orElseThrow(() -> new NotFoundException("Business not found: " + businessId));

        business.softDelete();
        businessRepository.save(business);
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
