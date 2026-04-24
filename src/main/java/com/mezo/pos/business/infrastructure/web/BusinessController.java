package com.mezo.pos.business.infrastructure.web;

import com.mezo.pos.business.application.CreateBusinessUseCase;
import com.mezo.pos.business.application.GetBusinessUseCase;
import com.mezo.pos.business.application.UpdateBusinessUseCase;
import com.mezo.pos.business.infrastructure.web.dto.BusinessResponse;
import com.mezo.pos.business.infrastructure.web.dto.CreateBusinessRequest;
import com.mezo.pos.business.infrastructure.web.dto.UpdateBusinessRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/businesses")
@RequiredArgsConstructor
public class BusinessController {

    private final CreateBusinessUseCase createBusinessUseCase;
    private final UpdateBusinessUseCase updateBusinessUseCase;
    private final GetBusinessUseCase getBusinessUseCase;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<BusinessResponse> create(
            @Valid @RequestBody CreateBusinessRequest request,
            Authentication authentication) {
        UUID ownerId = UUID.fromString(authentication.getName());
        BusinessResponse response = createBusinessUseCase.execute(request, ownerId);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<List<BusinessResponse>> listMyBusinesses(Authentication authentication) {
        UUID ownerId = UUID.fromString(authentication.getName());
        List<BusinessResponse> businesses = getBusinessUseCase.findByOwnerId(ownerId);
        return ResponseEntity.ok(businesses);
    }

    @GetMapping("/{id}")
    public ResponseEntity<BusinessResponse> getById(@PathVariable UUID id) {
        BusinessResponse response = getBusinessUseCase.findById(id);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<BusinessResponse> update(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateBusinessRequest request) {
        BusinessResponse response = updateBusinessUseCase.execute(id, request);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<BusinessResponse> toggleStatus(@PathVariable UUID id) {
        BusinessResponse response = updateBusinessUseCase.toggleStatus(id);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        updateBusinessUseCase.delete(id);
        return ResponseEntity.noContent().build();
    }
}
