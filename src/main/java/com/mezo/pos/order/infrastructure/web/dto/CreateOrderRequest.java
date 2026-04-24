package com.mezo.pos.order.infrastructure.web.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateOrderRequest {

    private UUID tableId;

    private String paymentMethod;

    private BigDecimal tip;

    @NotEmpty(message = "Lines must not be empty")
    @Valid
    private List<OrderLineRequest> lines;
}
