package com.mezo.pos.order.infrastructure.adapter;

import com.mezo.pos.order.domain.entity.Order;
import com.mezo.pos.order.domain.enums.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface SpringDataOrderRepository extends JpaRepository<Order, UUID> {

    List<Order> findByBusinessIdAndDeletedFalse(UUID businessId);

    List<Order> findByBusinessIdAndStatusAndDeletedFalse(UUID businessId, OrderStatus status);

    List<Order> findByBusinessIdAndTableIdAndDeletedFalse(UUID businessId, UUID tableId);

    List<Order> findByBusinessIdAndStatusAndTableIdAndDeletedFalse(UUID businessId, OrderStatus status, UUID tableId);
}
