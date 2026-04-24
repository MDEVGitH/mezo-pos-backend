package com.mezo.pos.auth.application;

import com.mezo.pos.auth.domain.entity.User;
import com.mezo.pos.auth.domain.port.UserRepository;
import com.mezo.pos.auth.infrastructure.web.dto.AuthResponse;
import com.mezo.pos.auth.infrastructure.web.dto.UserResponse;
import com.mezo.pos.business.domain.entity.Business;
import com.mezo.pos.business.domain.port.BusinessRepository;
import com.mezo.pos.business.infrastructure.web.dto.BusinessSummary;
import com.mezo.pos.shared.domain.exception.UnauthorizedException;
import com.mezo.pos.shared.infrastructure.security.JwtProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class LoginUseCase {

    private final UserRepository userRepository;
    private final BusinessRepository businessRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtProvider jwtProvider;

    @Transactional(readOnly = true)
    public AuthResponse execute(String email, String password) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UnauthorizedException("Invalid credentials"));

        if (!user.isEmailVerified()) {
            throw new UnauthorizedException("Email not verified. Please verify your email first.");
        }

        if (!passwordEncoder.matches(password, user.getPasswordHash())) {
            throw new UnauthorizedException("Invalid credentials");
        }

        String planStr = user.getPlan() != null ? user.getPlan().name() : null;

        String accessToken = jwtProvider.generateAccessToken(
                user.getId(),
                user.getEmail().getValue(),
                user.getRole().name(),
                planStr
        );
        String refreshToken = jwtProvider.generateRefreshToken(user.getId());

        List<Business> businesses = businessRepository.findByOwnerId(user.getId());
        List<BusinessSummary> businessSummaries = businesses.stream()
                .map(b -> new BusinessSummary(b.getId(), b.getName()))
                .toList();

        UserResponse userResponse = new UserResponse(
                user.getId(),
                user.getEmail().getValue(),
                user.getRole().name(),
                planStr,
                user.isEmailVerified(),
                businessSummaries
        );

        return new AuthResponse(
                accessToken,
                refreshToken,
                jwtProvider.getAccessTokenExpirationMs() / 1000,
                userResponse
        );
    }
}
