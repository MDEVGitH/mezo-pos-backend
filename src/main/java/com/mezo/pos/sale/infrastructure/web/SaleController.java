package com.mezo.pos.sale.infrastructure.web;

import com.mezo.pos.sale.application.CloseSaleUseCase;
import com.mezo.pos.sale.application.GetSaleUseCase;
import com.mezo.pos.sale.application.ListSalesUseCase;
import com.mezo.pos.sale.infrastructure.web.dto.CloseSaleRequest;
import com.mezo.pos.sale.infrastructure.web.dto.SaleResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/businesses/{businessId}/sales")
@RequiredArgsConstructor
public class SaleController {

    private final CloseSaleUseCase closeSaleUseCase;
    private final GetSaleUseCase getSaleUseCase;
    private final ListSalesUseCase listSalesUseCase;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'CASHIER')")
    public ResponseEntity<SaleResponse> closeSale(
            @PathVariable UUID businessId,
            @Valid @RequestBody CloseSaleRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        UUID closedBy = UUID.fromString(userDetails.getUsername());
        SaleResponse response = closeSaleUseCase.execute(request, businessId, closedBy);
        return ResponseEntity.status(201).body(response);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'CASHIER')")
    public ResponseEntity<List<SaleResponse>> listSales(
            @PathVariable UUID businessId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        List<SaleResponse> response = listSalesUseCase.execute(businessId, from, to);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'CASHIER')")
    public ResponseEntity<SaleResponse> getSale(
            @PathVariable UUID businessId,
            @PathVariable UUID id) {
        SaleResponse response = getSaleUseCase.execute(id, businessId);
        return ResponseEntity.ok(response);
    }
}
