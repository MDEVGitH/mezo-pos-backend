package com.mezo.pos.auth.application;

import com.mezo.pos.auth.domain.entity.OtpToken;
import com.mezo.pos.auth.domain.port.EmailService;
import com.mezo.pos.auth.domain.port.OtpRepository;
import com.mezo.pos.auth.domain.port.UserRepository;
import com.mezo.pos.shared.domain.exception.DomainException;
import com.mezo.pos.shared.domain.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class ResendOtpUseCase {

    private final UserRepository userRepository;
    private final OtpRepository otpRepository;
    private final EmailService emailService;

    @Value("${mezo.otp.expiration-minutes}")
    private int otpExpirationMinutes;

    @Value("${mezo.otp.block-duration-minutes}")
    private int blockDurationMinutes;

    @Value("${mezo.otp.max-attempts}")
    private int maxAttempts;

    private static final SecureRandom RANDOM = new SecureRandom();

    @Transactional
    public void execute(String email) {
        // Verify user exists
        userRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("User not found with email: " + email));

        // Check rate limiting
        LocalDateTime blockWindow = LocalDateTime.now().minusMinutes(blockDurationMinutes);
        long recentAttempts = otpRepository.countByEmailAndCreatedAtAfter(email, blockWindow);
        if (recentAttempts >= maxAttempts) {
            throw new DomainException(
                    "Too many OTP requests. Please wait " + blockDurationMinutes + " minutes."
            );
        }

        // Generate new OTP
        String code = String.valueOf(100000 + RANDOM.nextInt(900000));

        OtpToken otpToken = OtpToken.builder()
                .code(code)
                .email(email)
                .expiresAt(LocalDateTime.now().plusMinutes(otpExpirationMinutes))
                .used(false)
                .build();
        otpRepository.save(otpToken);

        emailService.sendOtp(email, code);
    }
}
