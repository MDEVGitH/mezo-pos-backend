package com.mezo.pos.table.infrastructure.adapter;

import com.mezo.pos.table.domain.entity.RestaurantTable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SpringDataTableRepository extends JpaRepository<RestaurantTable, UUID> {

    Optional<RestaurantTable> findByIdAndDeletedFalse(UUID id);

    List<RestaurantTable> findByBusinessIdAndDeletedFalseOrderByNumberAsc(UUID businessId);

    @Query("SELECT MAX(t.number) FROM RestaurantTable t WHERE t.businessId = :businessId AND t.deleted = false")
    Optional<Integer> findMaxNumberByBusinessId(@Param("businessId") UUID businessId);

    long countByBusinessIdAndDeletedFalse(UUID businessId);
}
