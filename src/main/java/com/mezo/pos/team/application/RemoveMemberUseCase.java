package com.mezo.pos.team.application;

import com.mezo.pos.business.domain.entity.Business;
import com.mezo.pos.business.domain.port.BusinessRepository;
import com.mezo.pos.shared.domain.exception.DomainException;
import com.mezo.pos.shared.domain.exception.NotFoundException;
import com.mezo.pos.team.domain.entity.TeamMember;
import com.mezo.pos.team.domain.port.TeamRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RemoveMemberUseCase {

    private final TeamRepository teamRepository;
    private final BusinessRepository businessRepository;

    @Transactional
    public void execute(UUID userId, UUID businessId, UUID requesterId) {
        TeamMember member = teamRepository.findByUserIdAndBusinessId(userId, businessId)
                .orElseThrow(() -> new NotFoundException("Team member not found"));

        if (userId.equals(requesterId)) {
            throw new DomainException("Cannot remove yourself from the team");
        }

        Business business = businessRepository.findById(businessId)
                .orElseThrow(() -> new NotFoundException("Business not found: " + businessId));

        if (business.getOwnerId().equals(userId)) {
            throw new DomainException("Cannot remove the owner of the business");
        }

        teamRepository.deleteByUserIdAndBusinessId(userId, businessId);
    }
}
