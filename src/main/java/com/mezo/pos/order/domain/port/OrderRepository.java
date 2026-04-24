package com.mezo.pos.order.domain.port;

import com.mezo.pos.order.domain.entity.Order;
import com.mezo.pos.order.domain.enums.OrderStatus;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface OrderRepository {

    Order save(Order order);

    Optional<Order> findById(UUID id);

    List<Order> findByBusinessId(UUID businessId);

    List<Order> findByBusinessIdAndStatus(UUID businessId, OrderStatus status);

    List<Order> findByBusinessIdAndTableId(UUID businessId, UUID tableId);

    List<Order> findByBusinessIdAndStatusAndTableId(UUID businessId, OrderStatus status, UUID tableId);
}
