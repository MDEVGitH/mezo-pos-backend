package com.mezo.pos.auth.infrastructure.adapter;

import com.mezo.pos.auth.domain.entity.OtpToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

public interface SpringDataOtpRepository extends JpaRepository<OtpToken, UUID> {

    Optional<OtpToken> findByEmailAndCodeAndUsedFalse(String email, String code);

    long countByEmailAndCreatedAtAfter(String email, LocalDateTime after);
}
