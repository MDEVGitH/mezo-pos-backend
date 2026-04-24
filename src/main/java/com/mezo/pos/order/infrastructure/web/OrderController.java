package com.mezo.pos.order.infrastructure.web;

import com.mezo.pos.order.application.*;
import com.mezo.pos.order.domain.enums.OrderStatus;
import com.mezo.pos.order.infrastructure.web.dto.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/businesses/{businessId}/orders")
@RequiredArgsConstructor
public class OrderController {

    private final CreateOrderUseCase createOrderUseCase;
    private final AddOrderLinesUseCase addOrderLinesUseCase;
    private final RemoveOrderLineUseCase removeOrderLineUseCase;
    private final UpdateOrderStatusUseCase updateOrderStatusUseCase;
    private final GetOrderUseCase getOrderUseCase;
    private final ListOrdersUseCase listOrdersUseCase;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'CASHIER', 'WAITER')")
    public ResponseEntity<OrderResponse> createOrder(
            @PathVariable UUID businessId,
            @Valid @RequestBody CreateOrderRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        UUID createdBy = UUID.fromString(userDetails.getUsername());
        OrderResponse response = createOrderUseCase.execute(request, businessId, createdBy);
        return ResponseEntity.status(201).body(response);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'CASHIER', 'WAITER', 'KITCHEN')")
    public ResponseEntity<List<OrderResponse>> listOrders(
            @PathVariable UUID businessId,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) UUID tableId) {
        OrderStatus orderStatus = null;
        if (status != null && !status.isBlank()) {
            orderStatus = OrderStatus.valueOf(status.toUpperCase());
        }
        List<OrderResponse> response = listOrdersUseCase.execute(businessId, orderStatus, tableId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'CASHIER', 'WAITER', 'KITCHEN')")
    public ResponseEntity<OrderResponse> getOrder(
            @PathVariable UUID businessId,
            @PathVariable UUID id) {
        OrderResponse response = getOrderUseCase.execute(id, businessId);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{id}/lines")
    @PreAuthorize("hasAnyRole('ADMIN', 'CASHIER', 'WAITER')")
    public ResponseEntity<OrderResponse> addLines(
            @PathVariable UUID businessId,
            @PathVariable UUID id,
            @Valid @RequestBody AddOrderLinesRequest request) {
        OrderResponse response = addOrderLinesUseCase.execute(id, request, businessId);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}/lines/{lineId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'CASHIER', 'WAITER')")
    public ResponseEntity<OrderResponse> removeLine(
            @PathVariable UUID businessId,
            @PathVariable UUID id,
            @PathVariable UUID lineId) {
        OrderResponse response = removeOrderLineUseCase.execute(id, lineId, businessId);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('ADMIN', 'CASHIER', 'KITCHEN')")
    public ResponseEntity<OrderResponse> updateStatus(
            @PathVariable UUID businessId,
            @PathVariable UUID id,
            @Valid @RequestBody UpdateOrderStatusRequest request) {
        OrderResponse response = updateOrderStatusUseCase.execute(id, request, businessId);
        return ResponseEntity.ok(response);
    }
}
