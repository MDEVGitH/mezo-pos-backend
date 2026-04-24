package com.mezo.pos.order.application;

import com.mezo.pos.order.domain.entity.Order;
import com.mezo.pos.order.domain.enums.OrderStatus;
import com.mezo.pos.order.domain.port.OrderRepository;
import com.mezo.pos.order.infrastructure.web.dto.OrderResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ListOrdersUseCase {

    private final OrderRepository orderRepository;

    @Transactional(readOnly = true)
    public List<OrderResponse> execute(UUID businessId, OrderStatus status, UUID tableId) {
        List<Order> orders;

        if (status != null && tableId != null) {
            orders = orderRepository.findByBusinessIdAndStatusAndTableId(businessId, status, tableId);
        } else if (status != null) {
            orders = orderRepository.findByBusinessIdAndStatus(businessId, status);
        } else if (tableId != null) {
            orders = orderRepository.findByBusinessIdAndTableId(businessId, tableId);
        } else {
            orders = orderRepository.findByBusinessId(businessId);
        }

        return orders.stream()
                .map(OrderResponse::fromEntity)
                .toList();
    }
}
