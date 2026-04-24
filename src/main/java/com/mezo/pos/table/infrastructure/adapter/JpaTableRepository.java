package com.mezo.pos.table.infrastructure.adapter;

import com.mezo.pos.table.domain.entity.RestaurantTable;
import com.mezo.pos.table.domain.port.TableRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class JpaTableRepository implements TableRepository {

    private final SpringDataTableRepository springRepo;

    @Override
    public RestaurantTable save(RestaurantTable table) {
        return springRepo.save(table);
    }

    @Override
    public Optional<RestaurantTable> findById(UUID id) {
        return springRepo.findByIdAndDeletedFalse(id);
    }

    @Override
    public List<RestaurantTable> findByBusinessId(UUID businessId) {
        return springRepo.findByBusinessIdAndDeletedFalseOrderByNumberAsc(businessId);
    }

    @Override
    public Optional<Integer> findMaxNumberByBusinessId(UUID businessId) {
        return springRepo.findMaxNumberByBusinessId(businessId);
    }

    @Override
    public long countByBusinessId(UUID businessId) {
        return springRepo.countByBusinessIdAndDeletedFalse(businessId);
    }

    @Override
    public void deleteById(UUID id) {
        springRepo.deleteById(id);
    }
}
