package com.mezo.pos.auth.domain.port;

import com.mezo.pos.auth.domain.entity.OtpToken;

import java.time.LocalDateTime;
import java.util.Optional;

public interface OtpRepository {
    OtpToken save(OtpToken otpToken);
    Optional<OtpToken> findByEmailAndCodeAndUsedFalse(String email, String code);
    long countByEmailAndCreatedAtAfter(String email, LocalDateTime after);
}
