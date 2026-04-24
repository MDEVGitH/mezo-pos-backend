package com.mezo.pos.shared.infrastructure.security;

import com.mezo.pos.business.domain.port.BusinessRepository;
import com.mezo.pos.team.domain.port.TeamRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class DefaultBusinessAccessChecker implements BusinessAccessChecker {

    private final BusinessRepository businessRepository;
    private final TeamRepository teamRepository;

    @Override
    public boolean belongsToBusiness(UUID userId, UUID businessId) {
        // Check if user is the owner
        if (businessRepository.existsByIdAndOwnerId(businessId, userId)) {
            return true;
        }
        // Check if user is a team member
        return teamRepository.existsByUserIdAndBusinessId(userId, businessId);
    }
}
