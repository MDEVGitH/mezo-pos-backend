package com.mezo.pos.order.application;

import com.mezo.pos.order.domain.entity.Order;
import com.mezo.pos.order.domain.port.OrderRepository;
import com.mezo.pos.order.infrastructure.web.dto.OrderResponse;
import com.mezo.pos.shared.domain.exception.DomainException;
import com.mezo.pos.shared.domain.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class GetOrderUseCase {

    private final OrderRepository orderRepository;

    @Transactional(readOnly = true)
    public OrderResponse execute(UUID orderId, UUID businessId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new NotFoundException("Order not found: " + orderId));

        if (!order.getBusinessId().equals(businessId)) {
            throw new DomainException("Order does not belong to this business");
        }

        return OrderResponse.fromEntity(order);
    }
}
