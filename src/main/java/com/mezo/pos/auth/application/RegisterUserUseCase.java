package com.mezo.pos.auth.application;

import com.mezo.pos.auth.domain.entity.OtpToken;
import com.mezo.pos.auth.domain.entity.User;
import com.mezo.pos.auth.domain.enums.Role;
import com.mezo.pos.auth.domain.port.EmailService;
import com.mezo.pos.auth.domain.port.OtpRepository;
import com.mezo.pos.auth.domain.port.UserRepository;
import com.mezo.pos.plan.domain.enums.PlanType;
import com.mezo.pos.shared.domain.exception.DomainException;
import com.mezo.pos.shared.domain.valueobject.Email;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RegisterUserUseCase {

    private final UserRepository userRepository;
    private final OtpRepository otpRepository;
    private final EmailService emailService;
    private final PasswordEncoder passwordEncoder;

    @Value("${mezo.otp.expiration-minutes}")
    private int otpExpirationMinutes;

    private static final SecureRandom RANDOM = new SecureRandom();

    @Transactional
    public Map<String, Object> execute(String emailStr, String password) {
        // Validate email format via Value Object
        Email email = new Email(emailStr);

        // Check if email already exists
        if (userRepository.existsByEmail(email.getValue())) {
            throw new DomainException("Email already registered: " + email.getValue());
        }

        // Validate password
        validatePassword(password);

        // Hash password
        String passwordHash = passwordEncoder.encode(password);

        // Create user with PRO plan and 30-day trial
        LocalDateTime now = LocalDateTime.now();
        User user = User.builder()
                .email(email)
                .passwordHash(passwordHash)
                .role(Role.ADMIN)
                .emailVerified(false)
                .plan(PlanType.PRO)
                .planStartedAt(now)
                .planExpiresAt(now.plusDays(30))
                .build();

        User savedUser = userRepository.save(user);

        // Generate 6-digit OTP
        String code = generateOtpCode();

        // Save OTP token
        OtpToken otpToken = OtpToken.builder()
                .code(code)
                .email(email.getValue())
                .expiresAt(now.plusMinutes(otpExpirationMinutes))
                .used(false)
                .build();
        otpRepository.save(otpToken);

        // Send OTP email
        emailService.sendOtp(email.getValue(), code);

        return Map.of(
                "message", "OTP enviado a " + email.getValue(),
                "userId", savedUser.getId()
        );
    }

    private void validatePassword(String password) {
        if (password == null || password.length() < 8) {
            throw new DomainException("Password must be at least 8 characters");
        }
        if (!password.matches(".*[A-Z].*")) {
            throw new DomainException("Password must contain at least one uppercase letter");
        }
        if (!password.matches(".*[0-9].*")) {
            throw new DomainException("Password must contain at least one number");
        }
    }

    private String generateOtpCode() {
        int code = 100000 + RANDOM.nextInt(900000);
        return String.valueOf(code);
    }
}
