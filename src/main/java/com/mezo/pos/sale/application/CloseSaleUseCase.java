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
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CloseSaleUseCase {

    private final SaleRepository saleRepository;
    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;

    @Transactional
    public List<SaleResponse> execute(CloseSaleRequest request, UUID businessId, UUID closedBy) {
        if (request.getTableId() != null) {
            // Flujo B: cerrar todas las órdenes activas de una mesa
            return closeFromTable(request, businessId, closedBy);
        } else if (request.getOrderId() != null) {
            // Flujo A: cerrar una orden específica
            return List.of(closeFromExistingOrder(request, businessId, closedBy));
        } else {
            // Flujo C: venta directa POS (sin orden previa)
            return List.of(closeDirectPOS(request, businessId, closedBy));
        }
    }

    private SaleResponse closeFromExistingOrder(CloseSaleRequest request, UUID businessId, UUID closedBy) {
        Order order = orderRepository.findById(request.getOrderId())
                .orElseThrow(() -> new NotFoundException("Order not found: " + request.getOrderId()));

        if (!order.getBusinessId().equals(businessId)) {
            throw new DomainException("Order does not belong to this business");
        }

        return closeOrder(order, request.getPaymentMethod(), request.getTip(), businessId, closedBy);
    }

    private List<SaleResponse> closeFromTable(CloseSaleRequest request, UUID businessId, UUID closedBy) {
        List<OrderStatus> activeStatuses = List.of(
                OrderStatus.OPEN, OrderStatus.PREPARING, OrderStatus.READY, OrderStatus.DELIVERED);

        List<Order> activeOrders = orderRepository.findByTableIdAndStatusIn(
                request.getTableId(), activeStatuses);

        if (activeOrders.isEmpty()) {
            throw new DomainException("No active orders found for this table");
        }

        List<SaleResponse> sales = new ArrayList<>();
        for (Order order : activeOrders) {
            if (!order.getBusinessId().equals(businessId)) {
                continue;
            }
            SaleResponse sale = closeOrder(order, request.getPaymentMethod(), request.getTip(), businessId, closedBy);
            sales.add(sale);
        }

        return sales;
    }

    private SaleResponse closeOrder(Order order, String paymentMethodStr, BigDecimal tipAmount,
                                     UUID businessId, UUID closedBy) {
        if (order.getStatus() == OrderStatus.CLOSED) {
            throw new DomainException("Order is already closed: " + order.getId());
        }
        if (order.getStatus() == OrderStatus.CANCELLED) {
            throw new DomainException("Cannot close a cancelled order: " + order.getId());
        }

        order.setStatus(OrderStatus.CLOSED);

        PaymentMethod paymentMethod = order.getPaymentMethod();
        if (paymentMethodStr != null && !paymentMethodStr.isBlank()) {
            paymentMethod = PaymentMethod.valueOf(paymentMethodStr.toUpperCase());
        }
        if (paymentMethod == null) {
            throw new DomainException("Payment method is required");
        }

        Money tip = order.getTip();
        if (tipAmount != null && tipAmount.compareTo(BigDecimal.ZERO) > 0) {
            tip = new Money(tipAmount, Currency.COP);
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
        BigDecimal tipVal = tip != null ? tip.getAmount() : BigDecimal.ZERO;
        order.setTotal(new Money(linesSum.add(tipVal), Currency.COP));

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
