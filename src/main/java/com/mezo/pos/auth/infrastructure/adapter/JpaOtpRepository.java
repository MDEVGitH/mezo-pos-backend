package com.mezo.pos.auth.infrastructure.adapter;

import com.mezo.pos.auth.domain.entity.OtpToken;
import com.mezo.pos.auth.domain.port.OtpRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class JpaOtpRepository implements OtpRepository {

    private final SpringDataOtpRepository springRepo;

    @Override
    public OtpToken save(OtpToken otpToken) {
        return springRepo.save(otpToken);
    }

    @Override
    public Optional<OtpToken> findByEmailAndCodeAndUsedFalse(String email, String code) {
        return springRepo.findByEmailAndCodeAndUsedFalse(email, code);
    }

    @Override
    public long countByEmailAndCreatedAtAfter(String email, LocalDateTime after) {
        return springRepo.countByEmailAndCreatedAtAfter(email, after);
    }
}
