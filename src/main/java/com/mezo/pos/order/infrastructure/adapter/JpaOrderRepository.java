package com.mezo.pos.order.infrastructure.adapter;

import com.mezo.pos.order.domain.entity.Order;
import com.mezo.pos.order.domain.enums.OrderStatus;
import com.mezo.pos.order.domain.port.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class JpaOrderRepository implements OrderRepository {

    private final SpringDataOrderRepository springRepo;

    @Override
    public Order save(Order order) {
        return springRepo.save(order);
    }

    @Override
    public Optional<Order> findById(UUID id) {
        return springRepo.findById(id)
                .filter(o -> !o.isDeleted());
    }

    @Override
    public List<Order> findByBusinessId(UUID businessId) {
        return springRepo.findByBusinessIdAndDeletedFalse(businessId);
    }

    @Override
    public List<Order> findByBusinessIdAndStatus(UUID businessId, OrderStatus status) {
        return springRepo.findByBusinessIdAndStatusAndDeletedFalse(businessId, status);
    }

    @Override
    public List<Order> findByBusinessIdAndTableId(UUID businessId, UUID tableId) {
        return springRepo.findByBusinessIdAndTableIdAndDeletedFalse(businessId, tableId);
    }

    @Override
    public List<Order> findByBusinessIdAndStatusAndTableId(UUID businessId, OrderStatus status, UUID tableId) {
        return springRepo.findByBusinessIdAndStatusAndTableIdAndDeletedFalse(businessId, status, tableId);
    }
}
