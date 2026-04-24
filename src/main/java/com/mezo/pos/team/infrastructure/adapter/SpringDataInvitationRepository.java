package com.mezo.pos.team.infrastructure.adapter;

import com.mezo.pos.team.domain.entity.Invitation;
import com.mezo.pos.team.domain.enums.InvitationStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SpringDataInvitationRepository extends JpaRepository<Invitation, UUID> {

    List<Invitation> findByEmailAndDeletedFalse(String email);

    Optional<Invitation> findByEmailAndBusinessIdAndDeletedFalse(String email, UUID businessId);

    List<Invitation> findByEmailAndStatusAndDeletedFalse(String email, InvitationStatus status);
}
