package com.mezo.pos.order.application;

import com.mezo.pos.order.domain.entity.Order;
import com.mezo.pos.order.domain.entity.OrderLine;
import com.mezo.pos.order.domain.enums.OrderStatus;
import com.mezo.pos.order.domain.enums.PaymentMethod;
import com.mezo.pos.order.domain.port.OrderRepository;
import com.mezo.pos.order.infrastructure.web.dto.CreateOrderRequest;
import com.mezo.pos.order.infrastructure.web.dto.OrderLineRequest;
import com.mezo.pos.order.infrastructure.web.dto.OrderResponse;
import com.mezo.pos.product.domain.entity.Product;
import com.mezo.pos.product.domain.port.ProductRepository;
import com.mezo.pos.shared.domain.exception.DomainException;
import com.mezo.pos.shared.domain.exception.NotFoundException;
import com.mezo.pos.shared.domain.valueobject.Currency;
import com.mezo.pos.shared.domain.valueobject.Money;
import com.mezo.pos.table.domain.port.TableRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CreateOrderUseCase {

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final TableRepository tableRepository;

    @Transactional
    public OrderResponse execute(CreateOrderRequest request, UUID businessId, UUID createdBy) {
        if (request.getTableId() != null) {
            var table = tableRepository.findById(request.getTableId())
                    .orElseThrow(() -> new NotFoundException("Table not found: " + request.getTableId()));
            if (!table.getBusinessId().equals(businessId)) {
                throw new DomainException("Table does not belong to this business");
            }
        }

        if (request.getLines() == null || request.getLines().isEmpty()) {
            throw new DomainException("Order must have at least one line");
        }

        PaymentMethod paymentMethod = null;
        if (request.getPaymentMethod() != null && !request.getPaymentMethod().isBlank()) {
            paymentMethod = PaymentMethod.valueOf(request.getPaymentMethod().toUpperCase());
        }

        Money tip = null;
        if (request.getTip() != null && request.getTip().compareTo(BigDecimal.ZERO) > 0) {
            tip = new Money(request.getTip(), Currency.COP);
        }

        Order order = Order.builder()
                .lines(new ArrayList<>())
                .tip(tip)
                .total(new Money(BigDecimal.ZERO, Currency.COP))
                .paymentMethod(paymentMethod)
                .status(OrderStatus.OPEN)
                .businessId(businessId)
                .tableId(request.getTableId())
                .createdBy(createdBy)
                .build();

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
