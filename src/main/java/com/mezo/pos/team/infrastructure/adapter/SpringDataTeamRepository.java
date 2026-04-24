package com.mezo.pos.team.infrastructure.adapter;

import com.mezo.pos.team.domain.entity.TeamMember;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SpringDataTeamRepository extends JpaRepository<TeamMember, UUID> {

    Optional<TeamMember> findByUserIdAndBusinessIdAndDeletedFalse(UUID userId, UUID businessId);

    List<TeamMember> findByBusinessIdAndDeletedFalse(UUID businessId);

    boolean existsByUserIdAndBusinessIdAndDeletedFalse(UUID userId, UUID businessId);

    long countByBusinessIdAndDeletedFalse(UUID businessId);
}
