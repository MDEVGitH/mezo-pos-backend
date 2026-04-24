package com.mezo.pos.auth.infrastructure.web.dto;

import com.mezo.pos.business.infrastructure.web.dto.BusinessSummary;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserResponse {
    private UUID id;
    private String email;
    private String role;
    private String plan;
    private boolean emailVerified;
    private List<BusinessSummary> businesses;
}
