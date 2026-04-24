package com.mezo.pos.team.infrastructure.web;

import com.mezo.pos.team.application.InviteMemberUseCase;
import com.mezo.pos.team.application.ListTeamUseCase;
import com.mezo.pos.team.application.RemoveMemberUseCase;
import com.mezo.pos.team.application.UpdateMemberRoleUseCase;
import com.mezo.pos.team.infrastructure.web.dto.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/businesses/{businessId}/team")
@RequiredArgsConstructor
public class TeamController {

    private final InviteMemberUseCase inviteMemberUseCase;
    private final ListTeamUseCase listTeamUseCase;
    private final UpdateMemberRoleUseCase updateMemberRoleUseCase;
    private final RemoveMemberUseCase removeMemberUseCase;

    @PostMapping("/invite")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<InviteMemberResponse> invite(
            @PathVariable UUID businessId,
            @Valid @RequestBody InviteMemberRequest request) {
        UUID invitedBy = UUID.fromString((String) SecurityContextHolder.getContext().getAuthentication().getPrincipal());
        InviteMemberResponse response = inviteMemberUseCase.execute(request, businessId, invitedBy);
        return ResponseEntity.status(201).body(response);
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<TeamMemberResponse>> listTeam(
            @PathVariable UUID businessId) {
        List<TeamMemberResponse> response = listTeamUseCase.execute(businessId);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{userId}/role")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<TeamMemberResponse> updateRole(
            @PathVariable UUID businessId,
            @PathVariable UUID userId,
            @Valid @RequestBody UpdateRoleRequest request) {
        UUID requesterId = UUID.fromString((String) SecurityContextHolder.getContext().getAuthentication().getPrincipal());
        TeamMemberResponse response = updateMemberRoleUseCase.execute(userId, request, businessId, requesterId);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> removeMember(
            @PathVariable UUID businessId,
            @PathVariable UUID userId) {
        UUID requesterId = UUID.fromString((String) SecurityContextHolder.getContext().getAuthentication().getPrincipal());
        removeMemberUseCase.execute(userId, businessId, requesterId);
        return ResponseEntity.noContent().build();
    }
}
