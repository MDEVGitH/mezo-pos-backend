package com.mezo.pos.sale.infrastructure.web.dto;

import com.mezo.pos.order.infrastructure.web.dto.OrderLineRequest;
import jakarta.validation.Valid;
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
public class CloseSaleRequest {

    private UUID orderId;

    private String paymentMethod;

    private BigDecimal tip;

    @Valid
    private List<OrderLineRequest> lines;
}
