package com.mezo.pos.business.infrastructure.web.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateBusinessRequest {

    @NotBlank(message = "Business name is required")
    private String name;

    @NotNull(message = "Business type is required")
    private String type;

    private String phone;
    private String nit;

    @NotBlank(message = "Address is required")
    private String address;

    @NotBlank(message = "City is required")
    private String city;

    @NotBlank(message = "Country is required")
    private String country;

    private LocalTime openAt;
    private LocalTime closeAt;

    @Min(value = 0, message = "Table count must be >= 0")
    private int tableCount = 0;
}
