package com.mezo.pos.team.application;

import com.mezo.pos.auth.domain.entity.User;
import com.mezo.pos.auth.domain.enums.Role;
import com.mezo.pos.auth.domain.port.EmailService;
import com.mezo.pos.auth.domain.port.UserRepository;
import com.mezo.pos.business.domain.entity.Business;
import com.mezo.pos.business.domain.port.BusinessRepository;
import com.mezo.pos.plan.domain.service.PlanEnforcer;
import com.mezo.pos.shared.domain.exception.DomainException;
import com.mezo.pos.shared.domain.exception.NotFoundException;
import com.mezo.pos.team.domain.entity.Invitation;
import com.mezo.pos.team.domain.entity.TeamMember;
import com.mezo.pos.team.domain.enums.InvitationStatus;
import com.mezo.pos.team.domain.port.InvitationRepository;
import com.mezo.pos.team.domain.port.TeamRepository;
import com.mezo.pos.team.infrastructure.web.dto.InviteMemberRequest;
import com.mezo.pos.team.infrastructure.web.dto.InviteMemberResponse;
import com.mezo.pos.team.infrastructure.web.dto.InvitationResponse;
import com.mezo.pos.team.infrastructure.web.dto.TeamMemberResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class InviteMemberUseCase {

    private final TeamRepository teamRepository;
    private final InvitationRepository invitationRepository;
    private final UserRepository userRepository;
    private final EmailService emailService;
    private final PlanEnforcer planEnforcer;
    private final BusinessRepository businessRepository;

    @Transactional
    public InviteMemberResponse execute(InviteMemberRequest request, UUID businessId, UUID invitedBy) {
        User owner = planEnforcer.resolveOwner(businessId);
        planEnforcer.validatePlanNotExpired(owner);

        long currentMemberCount = teamRepository.countByBusinessId(businessId);
        planEnforcer.validateCanInviteMember(owner, currentMemberCount);

        Business business = businessRepository.findById(businessId)
                .orElseThrow(() -> new NotFoundException("Business not found: " + businessId));

        String email = request.getEmail().toLowerCase().trim();
        Role role = Role.valueOf(request.getRole().toUpperCase());

        Optional<Invitation> existingInvitation = invitationRepository.findByEmailAndBusinessId(email, businessId);
        if (existingInvitation.isPresent() && existingInvitation.get().getStatus() == InvitationStatus.PENDING) {
            throw new DomainException("There is already a pending invitation for this email");
        }

        Optional<User> existingUser = userRepository.findByEmail(email);
        if (existingUser.isPresent()) {
            User user = existingUser.get();
            if (teamRepository.existsByUserIdAndBusinessId(user.getId(), businessId)) {
                throw new DomainException("User is already a member of this business");
            }

            if (business.getOwnerId().equals(user.getId())) {
                throw new DomainException("Cannot invite the owner of the business");
            }

            if (user.isEmailVerified()) {
                TeamMember member = TeamMember.builder()
                        .userId(user.getId())
                        .businessId(businessId)
                        .role(role)
                        .invitedBy(invitedBy)
                        .build();
                TeamMember saved = teamRepository.save(member);

                emailService.sendTeamAddedNotification(email, business.getName(), role.name());

                return InviteMemberResponse.builder()
                        .status("ACTIVE")
                        .teamMember(TeamMemberResponse.fromEntity(saved, email))
                        .build();
            }
        }

        Invitation invitation = Invitation.builder()
                .email(email)
                .businessId(businessId)
                .role(role)
                .invitedBy(invitedBy)
                .status(InvitationStatus.PENDING)
                .build();
        Invitation saved = invitationRepository.save(invitation);

        emailService.sendInvitation(email, business.getName(), role.name());

        return InviteMemberResponse.builder()
                .status("PENDING")
                .invitation(InvitationResponse.fromEntity(saved))
                .build();
    }
}
