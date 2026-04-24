package com.mezo.pos.table.application;

import com.mezo.pos.order.domain.entity.Order;
import com.mezo.pos.order.domain.enums.OrderStatus;
import com.mezo.pos.order.domain.port.OrderRepository;
import com.mezo.pos.table.domain.entity.RestaurantTable;
import com.mezo.pos.table.domain.port.TableRepository;
import com.mezo.pos.table.infrastructure.web.dto.TableResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ListTablesUseCase {

    private final TableRepository tableRepository;
    private final OrderRepository orderRepository;

    @Transactional(readOnly = true)
    public List<TableResponse> execute(UUID businessId) {
        List<RestaurantTable> tables = tableRepository.findByBusinessId(businessId);

        List<OrderStatus> activeStatuses = List.of(
                OrderStatus.OPEN, OrderStatus.PREPARING, OrderStatus.READY, OrderStatus.DELIVERED);

        return tables.stream().map(table -> {
            List<Order> activeOrders = orderRepository.findByTableIdAndStatusIn(
                    table.getId(), activeStatuses);

            List<TableResponse.ActiveOrder> orderSummaries = activeOrders.stream()
                    .map(o -> TableResponse.ActiveOrder.builder()
                            .orderId(o.getId())
                            .status(o.getStatus().name())
                            .total(o.getTotal().getAmount())
                            .currency(o.getTotal().getCurrency().name())
                            .createdAt(o.getCreatedAt())
                            .build())
                    .toList();

            return TableResponse.builder()
                    .id(table.getId())
                    .number(table.getNumber())
                    .businessId(table.getBusinessId())
                    .activeOrderCount(orderSummaries.size())
                    .activeOrders(orderSummaries)
                    .createdAt(table.getCreatedAt())
                    .build();
        }).toList();
    }
}
