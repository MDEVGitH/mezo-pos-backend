package com.mezo.pos.team.domain.port;

import com.mezo.pos.team.domain.entity.Invitation;
import com.mezo.pos.team.domain.enums.InvitationStatus;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface InvitationRepository {

    Invitation save(Invitation invitation);

    List<Invitation> findByEmail(String email);

    Optional<Invitation> findByEmailAndBusinessId(String email, UUID businessId);

    List<Invitation> findByEmailAndStatus(String email, InvitationStatus status);
}
