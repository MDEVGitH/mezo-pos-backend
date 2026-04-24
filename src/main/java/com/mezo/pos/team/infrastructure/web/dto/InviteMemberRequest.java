package com.mezo.pos.team.infrastructure.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InviteMemberRequest {

    @NotBlank(message = "Email is required")
    private String email;

    @NotNull(message = "Role is required")
    private String role;
}
