package com.mezo.pos.product.infrastructure.web;

import com.mezo.pos.product.application.*;
import com.mezo.pos.product.domain.entity.Category;
import com.mezo.pos.product.infrastructure.web.dto.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/businesses/{businessId}/categories")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class CategoryController {

    private final CreateCategoryUseCase createCategoryUseCase;
    private final UpdateCategoryUseCase updateCategoryUseCase;
    private final DeleteCategoryUseCase deleteCategoryUseCase;
    private final ListCategoriesUseCase listCategoriesUseCase;

    @PostMapping
    public ResponseEntity<CategoryResponse> create(
            @PathVariable UUID businessId,
            @Valid @RequestBody CreateCategoryRequest request) {

        Category category = createCategoryUseCase.execute(
                businessId,
                request.getName(),
                request.getIcon()
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(category));
    }

    @GetMapping
    public ResponseEntity<List<CategoryResponse>> list(@PathVariable UUID businessId) {
        List<Category> categories = listCategoriesUseCase.execute(businessId);
        List<CategoryResponse> response = categories.stream()
                .map(this::toResponse)
                .toList();

        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<CategoryResponse> update(
            @PathVariable UUID businessId,
            @PathVariable UUID id,
            @Valid @RequestBody UpdateCategoryRequest request) {

        Category category = updateCategoryUseCase.execute(
                businessId,
                id,
                request.getName(),
                request.getIcon()
        );

        return ResponseEntity.ok(toResponse(category));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @PathVariable UUID businessId,
            @PathVariable UUID id) {

        deleteCategoryUseCase.execute(businessId, id);
        return ResponseEntity.noContent().build();
    }

    private CategoryResponse toResponse(Category category) {
        return CategoryResponse.builder()
                .id(category.getId())
                .name(category.getName())
                .icon(category.getIcon())
                .sortOrder(category.getSortOrder())
                .businessId(category.getBusinessId())
                .createdAt(category.getCreatedAt())
                .build();
    }
}
