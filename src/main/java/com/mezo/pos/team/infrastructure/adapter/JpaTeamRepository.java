package com.mezo.pos.team.infrastructure.adapter;

import com.mezo.pos.shared.domain.exception.NotFoundException;
import com.mezo.pos.team.domain.entity.TeamMember;
import com.mezo.pos.team.domain.port.TeamRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class JpaTeamRepository implements TeamRepository {

    private final SpringDataTeamRepository springRepo;

    @Override
    public TeamMember save(TeamMember teamMember) {
        return springRepo.save(teamMember);
    }

    @Override
    public Optional<TeamMember> findByUserIdAndBusinessId(UUID userId, UUID businessId) {
        return springRepo.findByUserIdAndBusinessIdAndDeletedFalse(userId, businessId);
    }

    @Override
    public List<TeamMember> findByBusinessId(UUID businessId) {
        return springRepo.findByBusinessIdAndDeletedFalse(businessId);
    }

    @Override
    public boolean existsByUserIdAndBusinessId(UUID userId, UUID businessId) {
        return springRepo.existsByUserIdAndBusinessIdAndDeletedFalse(userId, businessId);
    }

    @Override
    public long countByBusinessId(UUID businessId) {
        return springRepo.countByBusinessIdAndDeletedFalse(businessId);
    }

    @Override
    public void deleteByUserIdAndBusinessId(UUID userId, UUID businessId) {
        TeamMember member = springRepo.findByUserIdAndBusinessIdAndDeletedFalse(userId, businessId)
                .orElseThrow(() -> new NotFoundException("Team member not found"));
        member.softDelete();
        springRepo.save(member);
    }
}
