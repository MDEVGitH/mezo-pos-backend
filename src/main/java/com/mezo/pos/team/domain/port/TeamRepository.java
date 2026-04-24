package com.mezo.pos.team.domain.port;

import com.mezo.pos.team.domain.entity.TeamMember;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TeamRepository {

    TeamMember save(TeamMember teamMember);

    Optional<TeamMember> findByUserIdAndBusinessId(UUID userId, UUID businessId);

    List<TeamMember> findByBusinessId(UUID businessId);

    boolean existsByUserIdAndBusinessId(UUID userId, UUID businessId);

    long countByBusinessId(UUID businessId);

    void deleteByUserIdAndBusinessId(UUID userId, UUID businessId);
}
