package com.mezo.pos.order.application;

import com.mezo.pos.order.domain.entity.Order;
import com.mezo.pos.order.domain.entity.OrderLine;
import com.mezo.pos.order.domain.port.OrderRepository;
import com.mezo.pos.order.infrastructure.web.dto.AddOrderLinesRequest;
import com.mezo.pos.order.infrastructure.web.dto.OrderLineRequest;
import com.mezo.pos.order.infrastructure.web.dto.OrderResponse;
import com.mezo.pos.product.domain.entity.Product;
import com.mezo.pos.product.domain.port.ProductRepository;
import com.mezo.pos.shared.domain.exception.DomainException;
import com.mezo.pos.shared.domain.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AddOrderLinesUseCase {

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;

    @Transactional
    public OrderResponse execute(UUID orderId, AddOrderLinesRequest request, UUID businessId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new NotFoundException("Order not found: " + orderId));

        if (!order.getBusinessId().equals(businessId)) {
            throw new DomainException("Order does not belong to this business");
        }

        for (OrderLineRequest lineReq : request.getLines()) {
            Product product = productRepository.findById(lineReq.getProductId())
                    .orElseThrow(() -> new NotFoundException("Product not found: " + lineReq.getProductId()));

            if (!product.isAvailable()) {
                throw new DomainException("Product is not available: " + product.getName());
            }
            if (!product.getBusinessId().equals(businessId)) {
                throw new DomainException("Product does not belong to this business: " + product.getName());
            }

            OrderLine line = new OrderLine(
                    product.getId(),
                    product.getName(),
                    product.getPrice(),
                    lineReq.getQuantity()
            );
            order.addLine(line);
        }

        Order saved = orderRepository.save(order);
        return OrderResponse.fromEntity(saved);
    }
}
