package com.mezo.pos.business.infrastructure.web.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateBusinessRequest {
    private String name;
    private String type;
    private String phone;
    private String nit;
    private String address;
    private String city;
    private String country;
    private LocalTime openAt;
    private LocalTime closeAt;
}
