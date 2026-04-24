package com.mezo.pos.table.domain.port;

import com.mezo.pos.table.domain.entity.RestaurantTable;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TableRepository {

    RestaurantTable save(RestaurantTable table);

    Optional<RestaurantTable> findById(UUID id);

    List<RestaurantTable> findByBusinessId(UUID businessId);

    Optional<Integer> findMaxNumberByBusinessId(UUID businessId);

    long countByBusinessId(UUID businessId);

    void deleteById(UUID id);
}
