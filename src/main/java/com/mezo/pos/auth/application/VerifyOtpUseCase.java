package com.mezo.pos.auth.application;

import com.mezo.pos.auth.domain.entity.OtpToken;
import com.mezo.pos.auth.domain.entity.User;
import com.mezo.pos.auth.domain.port.OtpRepository;
import com.mezo.pos.auth.domain.port.UserRepository;
import com.mezo.pos.business.domain.port.BusinessRepository;
import com.mezo.pos.shared.domain.exception.DomainException;
import com.mezo.pos.shared.domain.exception.NotFoundException;
import com.mezo.pos.shared.infrastructure.security.JwtProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class VerifyOtpUseCase {

    private final OtpRepository otpRepository;
    private final UserRepository userRepository;
    private final JwtProvider jwtProvider;
    private final BusinessRepository businessRepository;

    @Value("${mezo.otp.max-attempts}")
    private int maxAttempts;

    @Value("${mezo.otp.block-duration-minutes}")
    private int blockDurationMinutes;

    @Transactional
    public Map<String, Object> execute(String email, String code) {
        // Check max attempts (count OTP lookups in last blockDurationMinutes)
        LocalDateTime blockWindow = LocalDateTime.now().minusMinutes(blockDurationMinutes);
        long recentAttempts = otpRepository.countByEmailAndCreatedAtAfter(email, blockWindow);
        if (recentAttempts > maxAttempts) {
            throw new DomainException(
                    "Demasiados intentos. Espera " + blockDurationMinutes + " minutos antes de intentar de nuevo."
            );
        }

        // Find valid OTP
        OtpToken otp = otpRepository.findByEmailAndCodeAndUsedFalse(email, code)
                .orElseThrow(() -> new DomainException("Código incorrecto. Revisa tu correo e intenta de nuevo."));

        // Check expiration
        if (LocalDateTime.now().isAfter(otp.getExpiresAt())) {
            throw new DomainException("El código expiró. Solicita uno nuevo.");
        }

        // Mark OTP as used
        otp.setUsed(true);
        otpRepository.save(otp);

        // Mark user as email verified
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("User not found with email: " + email));

        user.setEmailVerified(true);
        userRepository.save(user);

        // Auto-login: generate tokens
        String accessToken = jwtProvider.generateAccessToken(
                user.getId(), user.getEmail().getValue(),
                user.getRole().name(), user.getPlan().name());
        String refreshToken = jwtProvider.generateRefreshToken(user.getId());

        // Load businesses
        var businesses = businessRepository.findByOwnerId(user.getId()).stream()
                .map(b -> Map.of("id", (Object) b.getId(), "name", (Object) b.getName()))
                .toList();

        return Map.of(
                "accessToken", accessToken,
                "refreshToken", refreshToken,
                "expiresIn", 28800,
                "user", Map.of(
                        "id", user.getId(),
                        "email", user.getEmail().getValue(),
                        "role", user.getRole().name(),
                        "plan", user.getPlan().name(),
                        "emailVerified", true,
                        "businesses", businesses
                )
        );
    }
}
