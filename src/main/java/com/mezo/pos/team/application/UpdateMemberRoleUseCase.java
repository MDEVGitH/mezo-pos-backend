package com.mezo.pos.team.application;

import com.mezo.pos.auth.domain.enums.Role;
import com.mezo.pos.shared.domain.exception.DomainException;
import com.mezo.pos.shared.domain.exception.NotFoundException;
import com.mezo.pos.team.domain.entity.TeamMember;
import com.mezo.pos.team.domain.port.TeamRepository;
import com.mezo.pos.team.infrastructure.web.dto.TeamMemberResponse;
import com.mezo.pos.team.infrastructure.web.dto.UpdateRoleRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UpdateMemberRoleUseCase {

    private final TeamRepository teamRepository;

    @Transactional
    public TeamMemberResponse execute(UUID userId, UpdateRoleRequest request, UUID businessId, UUID requesterId) {
        TeamMember member = teamRepository.findByUserIdAndBusinessId(userId, businessId)
                .orElseThrow(() -> new NotFoundException("Team member not found"));

        if (userId.equals(requesterId)) {
            throw new DomainException("Cannot change your own role");
        }

        Role newRole = Role.valueOf(request.getRole().toUpperCase());
        member.setRole(newRole);

        TeamMember saved = teamRepository.save(member);
        return TeamMemberResponse.fromEntity(saved, null);
    }
}
