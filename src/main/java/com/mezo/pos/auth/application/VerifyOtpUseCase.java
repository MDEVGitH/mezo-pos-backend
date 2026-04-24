package com.mezo.pos.auth.application;

import com.mezo.pos.auth.domain.entity.OtpToken;
import com.mezo.pos.auth.domain.entity.User;
import com.mezo.pos.auth.domain.port.OtpRepository;
import com.mezo.pos.auth.domain.port.UserRepository;
import com.mezo.pos.shared.domain.exception.DomainException;
import com.mezo.pos.shared.domain.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class VerifyOtpUseCase {

    private final OtpRepository otpRepository;
    private final UserRepository userRepository;

    @Value("${mezo.otp.max-attempts}")
    private int maxAttempts;

    @Value("${mezo.otp.block-duration-minutes}")
    private int blockDurationMinutes;

    @Transactional
    public void execute(String email, String code) {
        // Check max attempts (count OTP lookups in last blockDurationMinutes)
        LocalDateTime blockWindow = LocalDateTime.now().minusMinutes(blockDurationMinutes);
        long recentAttempts = otpRepository.countByEmailAndCreatedAtAfter(email, blockWindow);
        if (recentAttempts > maxAttempts) {
            throw new DomainException(
                    "Too many attempts. Please wait " + blockDurationMinutes + " minutes before retrying."
            );
        }

        // Find valid OTP
        OtpToken otp = otpRepository.findByEmailAndCodeAndUsedFalse(email, code)
                .orElseThrow(() -> new DomainException("Invalid or expired OTP code"));

        // Check expiration
        if (LocalDateTime.now().isAfter(otp.getExpiresAt())) {
            throw new DomainException("OTP code has expired. Please request a new one.");
        }

        // Mark OTP as used
        otp.setUsed(true);
        otpRepository.save(otp);

        // Mark user as email verified
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("User not found with email: " + email));

        user.setEmailVerified(true);
        userRepository.save(user);
    }
}
