package com.mezo.pos.business.infrastructure.web.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BusinessResponse {
    private UUID id;
    private String name;
    private String type;
    private String phone;
    private String nit;
    private String address;
    private String city;
    private String country;
    private LocalTime openAt;
    private LocalTime closeAt;
    private long tableCount;
    private boolean open;
    private LocalDateTime createdAt;
}
