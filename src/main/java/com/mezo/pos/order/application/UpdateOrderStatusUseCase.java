package com.mezo.pos.order.application;

import com.mezo.pos.order.domain.entity.Order;
import com.mezo.pos.order.domain.enums.OrderStatus;
import com.mezo.pos.order.domain.port.OrderRepository;
import com.mezo.pos.order.infrastructure.web.dto.OrderResponse;
import com.mezo.pos.order.infrastructure.web.dto.UpdateOrderStatusRequest;
import com.mezo.pos.shared.domain.exception.DomainException;
import com.mezo.pos.shared.domain.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UpdateOrderStatusUseCase {

    private static final Map<OrderStatus, Set<OrderStatus>> VALID_TRANSITIONS = Map.of(
            OrderStatus.OPEN, Set.of(OrderStatus.PREPARING, OrderStatus.CANCELLED),
            OrderStatus.PREPARING, Set.of(OrderStatus.READY, OrderStatus.CANCELLED),
            OrderStatus.READY, Set.of(OrderStatus.DELIVERED, OrderStatus.CANCELLED),
            OrderStatus.DELIVERED, Set.of(OrderStatus.CLOSED),
            OrderStatus.CLOSED, Set.of(),
            OrderStatus.CANCELLED, Set.of()
    );

    private final OrderRepository orderRepository;

    @Transactional
    public OrderResponse execute(UUID orderId, UpdateOrderStatusRequest request, UUID businessId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new NotFoundException("Order not found: " + orderId));

        if (!order.getBusinessId().equals(businessId)) {
            throw new DomainException("Order does not belong to this business");
        }

        OrderStatus newStatus = OrderStatus.valueOf(request.getStatus().toUpperCase());
        OrderStatus currentStatus = order.getStatus();

        Set<OrderStatus> allowed = VALID_TRANSITIONS.getOrDefault(currentStatus, Set.of());
        if (!allowed.contains(newStatus)) {
            throw new DomainException(
                    "Invalid status transition from " + currentStatus + " to " + newStatus
            );
        }

        order.setStatus(newStatus);
        Order saved = orderRepository.save(order);
        return OrderResponse.fromEntity(saved);
    }
}
