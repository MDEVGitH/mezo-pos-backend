package com.mezo.pos.product.infrastructure.web;

import com.mezo.pos.product.application.*;
import com.mezo.pos.product.domain.entity.Product;
import com.mezo.pos.product.infrastructure.web.dto.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/businesses/{businessId}/products")
@RequiredArgsConstructor
public class ProductController {

    private final CreateProductUseCase createProductUseCase;
    private final UpdateProductUseCase updateProductUseCase;
    private final DeleteProductUseCase deleteProductUseCase;
    private final ListProductsUseCase listProductsUseCase;
    private final GetProductUseCase getProductUseCase;
    private final ToggleProductUseCase toggleProductUseCase;
    private final UploadProductImageUseCase uploadProductImageUseCase;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ProductResponse> create(
            @PathVariable UUID businessId,
            @Valid @RequestBody CreateProductRequest request) {

        Product product = createProductUseCase.execute(
                businessId,
                request.getName(),
                request.getPrice(),
                request.getCurrency(),
                request.getDescription(),
                request.getIngredients(),
                request.getProductionCost(),
                request.getImageType(),
                request.getImage(),
                request.getCategoryId()
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(product));
    }

    @GetMapping
    public ResponseEntity<List<ProductResponse>> list(
            @PathVariable UUID businessId,
            @RequestParam(required = false) UUID categoryId) {

        List<Product> products = listProductsUseCase.execute(businessId, categoryId);
        List<ProductResponse> response = products.stream()
                .map(this::toResponse)
                .toList();

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductResponse> get(
            @PathVariable UUID businessId,
            @PathVariable UUID id) {

        Product product = getProductUseCase.execute(businessId, id);
        return ResponseEntity.ok(toResponse(product));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ProductResponse> update(
            @PathVariable UUID businessId,
            @PathVariable UUID id,
            @Valid @RequestBody UpdateProductRequest request) {

        Product product = updateProductUseCase.execute(
                businessId,
                id,
                request.getName(),
                request.getPrice(),
                request.getCurrency(),
                request.getDescription(),
                request.getIngredients(),
                request.getProductionCost(),
                request.getImageType(),
                request.getImage(),
                request.getCategoryId()
        );

        return ResponseEntity.ok(toResponse(product));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> delete(
            @PathVariable UUID businessId,
            @PathVariable UUID id) {

        deleteProductUseCase.execute(businessId, id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/toggle")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ProductResponse> toggle(
            @PathVariable UUID businessId,
            @PathVariable UUID id) {

        Product product = toggleProductUseCase.execute(businessId, id);
        return ResponseEntity.ok(toResponse(product));
    }

    @PostMapping(value = "/upload-image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ImageUploadResponse> uploadImage(
            @PathVariable UUID businessId,
            @RequestParam("file") MultipartFile file) throws IOException {

        String imageUrl = uploadProductImageUseCase.execute(
                file.getBytes(),
                file.getOriginalFilename(),
                businessId
        );

        return ResponseEntity.ok(ImageUploadResponse.builder().imageUrl(imageUrl).build());
    }

    private ProductResponse toResponse(Product product) {
        return ProductResponse.builder()
                .id(product.getId())
                .name(product.getName())
                .price(product.getPrice().getAmount())
                .currency(product.getPrice().getCurrency())
                .description(product.getDescription())
                .ingredients(product.getIngredients())
                .productionCost(product.getProductionCost() != null ? product.getProductionCost().getAmount() : null)
                .imageType(product.getImageType())
                .image(product.getImage())
                .available(product.isAvailable())
                .categoryId(product.getCategoryId())
                .businessId(product.getBusinessId())
                .createdAt(product.getCreatedAt())
                .build();
    }
}
