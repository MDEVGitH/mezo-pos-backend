package com.mezo.pos.team.infrastructure.web.dto;

import com.mezo.pos.team.domain.entity.TeamMember;
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
public class TeamMemberResponse {

    private UUID userId;
    private String email;
    private String role;
    private UUID businessId;
    private UUID invitedBy;
    private LocalDateTime createdAt;

    public static TeamMemberResponse fromEntity(TeamMember member, String email) {
        return TeamMemberResponse.builder()
                .userId(member.getUserId())
                .email(email)
                .role(member.getRole().name())
                .businessId(member.getBusinessId())
                .invitedBy(member.getInvitedBy())
                .createdAt(member.getCreatedAt())
                .build();
    }
}
