package com.mezo.pos.team.infrastructure.web.dto;

import com.mezo.pos.team.domain.entity.Invitation;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InvitationResponse {

    private UUID id;
    private String email;
    private String role;
    private UUID businessId;
    private String status;
    private LocalDateTime createdAt;

    public static InvitationResponse fromEntity(Invitation invitation) {
        return InvitationResponse.builder()
                .id(invitation.getId())
                .email(invitation.getEmail())
                .role(invitation.getRole().name())
                .businessId(invitation.getBusinessId())
                .status(invitation.getStatus().name())
                .createdAt(invitation.getCreatedAt())
                .build();
    }
}
