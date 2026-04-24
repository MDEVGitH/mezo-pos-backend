package com.mezo.pos.sale.application;

import com.mezo.pos.order.domain.entity.Order;
import com.mezo.pos.order.domain.entity.OrderLine;
import com.mezo.pos.order.domain.enums.OrderStatus;
import com.mezo.pos.order.domain.enums.PaymentMethod;
import com.mezo.pos.order.domain.port.OrderRepository;
import com.mezo.pos.order.infrastructure.web.dto.OrderLineRequest;
import com.mezo.pos.product.domain.entity.Product;
import com.mezo.pos.product.domain.port.ProductRepository;
import com.mezo.pos.sale.domain.entity.Sale;
import com.mezo.pos.sale.domain.port.SaleRepository;
import com.mezo.pos.sale.infrastructure.web.dto.CloseSaleRequest;
import com.mezo.pos.sale.infrastructure.web.dto.SaleResponse;
import com.mezo.pos.shared.domain.exception.DomainException;
import com.mezo.pos.shared.domain.exception.NotFoundException;
import com.mezo.pos.shared.domain.valueobject.Currency;
import com.mezo.pos.shared.domain.valueobject.Money;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CloseSaleUseCase {

    private final SaleRepository saleRepository;
    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;

    @Transactional
    public SaleResponse execute(CloseSaleRequest request, UUID businessId, UUID closedBy) {
        if (request.getOrderId() != null) {
            return closeFromExistingOrder(request, businessId, closedBy);
        } else {
            return closeDirectPOS(request, businessId, closedBy);
        }
    }

    private SaleResponse closeFromExistingOrder(CloseSaleRequest request, UUID businessId, UUID closedBy) {
        Order order = orderRepository.findById(request.getOrderId())
                .orElseThrow(() -> new NotFoundException("Order not found: " + request.getOrderId()));

        if (!order.getBusinessId().equals(businessId)) {
            throw new DomainException("Order does not belong to this business");
        }

        if (order.getStatus() != OrderStatus.DELIVERED) {
            throw new DomainException(
                    "Order must be DELIVERED to close as sale. Current status: " + order.getStatus()
            );
        }

        order.setStatus(OrderStatus.CLOSED);

        PaymentMethod paymentMethod = order.getPaymentMethod();
        if (request.getPaymentMethod() != null && !request.getPaymentMethod().isBlank()) {
            paymentMethod = PaymentMethod.valueOf(request.getPaymentMethod().toUpperCase());
        }
        if (paymentMethod == null) {
            throw new DomainException("Payment method is required");
        }

        Money tip = order.getTip();
        if (request.getTip() != null && request.getTip().compareTo(BigDecimal.ZERO) > 0) {
            tip = new Money(request.getTip(), Currency.COP);
            order.setTip(tip);
            order.recalculateTotal();
        }

        orderRepository.save(order);

        Sale sale = Sale.builder()
                .orderId(order.getId())
                .total(order.getTotal())
                .tip(tip)
                .paymentMethod(paymentMethod)
                .businessId(businessId)
                .tableId(order.getTableId())
                .closedBy(closedBy)
                .build();

        Sale saved = saleRepository.save(sale);
        return SaleResponse.fromEntity(saved);
    }

    private SaleResponse closeDirectPOS(CloseSaleRequest request, UUID businessId, UUID closedBy) {
        if (request.getLines() == null || request.getLines().isEmpty()) {
            throw new DomainException("Lines are required for direct POS sale");
        }

        if (request.getPaymentMethod() == null || request.getPaymentMethod().isBlank()) {
            throw new DomainException("Payment method is required");
        }

        PaymentMethod paymentMethod = PaymentMethod.valueOf(request.getPaymentMethod().toUpperCase());

        Money tip = null;
        if (request.getTip() != null && request.getTip().compareTo(BigDecimal.ZERO) > 0) {
            tip = new Money(request.getTip(), Currency.COP);
        }

        Order order = Order.builder()
                .lines(new ArrayList<>())
                .tip(tip)
                .total(new Money(BigDecimal.ZERO, Currency.COP))
                .paymentMethod(paymentMethod)
                .status(OrderStatus.CLOSED)
                .businessId(businessId)
                .tableId(null)
                .createdBy(closedBy)
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
            order.getLines().add(line);
        }

        BigDecimal linesSum = order.getLines().stream()
                .map(l -> l.getSubtotal().getAmount())
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal tipAmount = tip != null ? tip.getAmount() : BigDecimal.ZERO;
        order.setTotal(new Money(linesSum.add(tipAmount), Currency.COP));

        Order savedOrder = orderRepository.save(order);

        Sale sale = Sale.builder()
                .orderId(savedOrder.getId())
                .total(savedOrder.getTotal())
                .tip(tip)
                .paymentMethod(paymentMethod)
                .businessId(businessId)
                .tableId(null)
                .closedBy(closedBy)
                .build();

        Sale savedSale = saleRepository.save(sale);
        return SaleResponse.fromEntity(savedSale);
    }
}
