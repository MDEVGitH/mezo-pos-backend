package com.mezo.pos.table.application;

import com.mezo.pos.order.domain.entity.Order;
import com.mezo.pos.order.domain.entity.OrderLine;
import com.mezo.pos.order.domain.enums.OrderStatus;
import com.mezo.pos.order.domain.port.OrderRepository;
import com.mezo.pos.shared.domain.exception.NotFoundException;
import com.mezo.pos.table.domain.entity.RestaurantTable;
import com.mezo.pos.table.domain.port.TableRepository;
import com.mezo.pos.table.infrastructure.web.dto.TableSummaryResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;

@Service
@RequiredArgsConstructor
public class GetTableSummaryUseCase {

    private final TableRepository tableRepository;
    private final OrderRepository orderRepository;

    @Transactional(readOnly = true)
    public TableSummaryResponse execute(UUID businessId, UUID tableId) {
        RestaurantTable table = tableRepository.findById(tableId)
                .orElseThrow(() -> new NotFoundException("Table not found: " + tableId));

        List<OrderStatus> activeStatuses = List.of(
                OrderStatus.OPEN, OrderStatus.PREPARING, OrderStatus.READY, OrderStatus.DELIVERED);

        List<Order> activeOrders = orderRepository.findByTableIdAndStatusIn(tableId, activeStatuses);

        // Agrupar líneas por productId
        Map<UUID, TableSummaryResponse.ItemSummary> itemMap = new LinkedHashMap<>();

        BigDecimal totalTip = BigDecimal.ZERO;

        for (Order order : activeOrders) {
            if (order.getTip() != null) {
                totalTip = totalTip.add(order.getTip().getAmount());
            }

            for (OrderLine line : order.getLines()) {
                UUID productId = line.getProductId();
                TableSummaryResponse.ItemSummary existing = itemMap.get(productId);

                if (existing != null) {
                    existing.setQuantity(existing.getQuantity() + line.getQuantity());
                    existing.setSubtotal(existing.getSubtotal().add(line.getSubtotal().getAmount()));
                } else {
                    itemMap.put(productId, TableSummaryResponse.ItemSummary.builder()
                            .productId(productId)
                            .productName(line.getProductName())
                            .unitPrice(line.getUnitPrice().getAmount())
                            .quantity(line.getQuantity())
                            .subtotal(line.getSubtotal().getAmount())
                            .build());
                }
            }
        }

        BigDecimal grandTotal = itemMap.values().stream()
                .map(TableSummaryResponse.ItemSummary::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .add(totalTip);

        return TableSummaryResponse.builder()
                .tableId(tableId)
                .tableNumber(table.getNumber())
                .orderCount(activeOrders.size())
                .items(new ArrayList<>(itemMap.values()))
                .totalTip(totalTip)
                .grandTotal(grandTotal)
                .currency("COP")
                .build();
    }
}
