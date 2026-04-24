package com.mezo.pos.team.infrastructure.adapter;

import com.mezo.pos.team.domain.entity.Invitation;
import com.mezo.pos.team.domain.enums.InvitationStatus;
import com.mezo.pos.team.domain.port.InvitationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class JpaInvitationRepository implements InvitationRepository {

    private final SpringDataInvitationRepository springRepo;

    @Override
    public Invitation save(Invitation invitation) {
        return springRepo.save(invitation);
    }

    @Override
    public List<Invitation> findByEmail(String email) {
        return springRepo.findByEmailAndDeletedFalse(email);
    }

    @Override
    public Optional<Invitation> findByEmailAndBusinessId(String email, UUID businessId) {
        return springRepo.findByEmailAndBusinessIdAndDeletedFalse(email, businessId);
    }

    @Override
    public List<Invitation> findByEmailAndStatus(String email, InvitationStatus status) {
        return springRepo.findByEmailAndStatusAndDeletedFalse(email, status);
    }
}
