package com.mezo.pos.order.infrastructure.web.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AddOrderLinesRequest {

    @NotEmpty(message = "Lines must not be empty")
    @Valid
    private List<OrderLineRequest> lines;
}
