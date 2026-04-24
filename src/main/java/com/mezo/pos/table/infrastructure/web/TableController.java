package com.mezo.pos.table.infrastructure.web;

import com.mezo.pos.table.application.CreateTableUseCase;
import com.mezo.pos.table.application.DeleteTableUseCase;
import com.mezo.pos.table.application.GetTableSummaryUseCase;
import com.mezo.pos.table.application.ListTablesUseCase;
import com.mezo.pos.table.domain.entity.RestaurantTable;
import com.mezo.pos.table.infrastructure.web.dto.TableResponse;
import com.mezo.pos.table.infrastructure.web.dto.TableSummaryResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/businesses/{businessId}/tables")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class TableController {

    private final CreateTableUseCase createTableUseCase;
    private final DeleteTableUseCase deleteTableUseCase;
    private final ListTablesUseCase listTablesUseCase;
    private final GetTableSummaryUseCase getTableSummaryUseCase;

    @PostMapping
    public ResponseEntity<TableResponse> create(@PathVariable UUID businessId) {
        RestaurantTable table = createTableUseCase.execute(businessId);
        return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(table));
    }

    @GetMapping
    public ResponseEntity<List<TableResponse>> list(@PathVariable UUID businessId) {
        List<TableResponse> response = listTablesUseCase.execute(businessId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}/summary")
    public ResponseEntity<TableSummaryResponse> summary(
            @PathVariable UUID businessId,
            @PathVariable UUID id) {
        TableSummaryResponse response = getTableSummaryUseCase.execute(businessId, id);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @PathVariable UUID businessId,
            @PathVariable UUID id) {

        deleteTableUseCase.execute(businessId, id);
        return ResponseEntity.noContent().build();
    }

    private TableResponse toResponse(RestaurantTable table) {
        return TableResponse.builder()
                .id(table.getId())
                .number(table.getNumber())
                .businessId(table.getBusinessId())
                .createdAt(table.getCreatedAt())
                .build();
    }
}
