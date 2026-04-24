package com.mezo.pos.auth.application;

import com.mezo.pos.auth.domain.entity.User;
import com.mezo.pos.auth.domain.port.UserRepository;
import com.mezo.pos.shared.domain.exception.UnauthorizedException;
import com.mezo.pos.shared.infrastructure.security.JwtProvider;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RefreshTokenUseCase {

    private final JwtProvider jwtProvider;
    private final UserRepository userRepository;

    public Map<String, Object> execute(String refreshToken) {
        if (!jwtProvider.validateToken(refreshToken)) {
            throw new UnauthorizedException("Invalid or expired refresh token");
        }

        Claims claims = jwtProvider.extractClaims(refreshToken);
        String type = claims.get("type", String.class);

        if (!"refresh".equals(type)) {
            throw new UnauthorizedException("Token is not a refresh token");
        }

        UUID userId = UUID.fromString(claims.getSubject());
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UnauthorizedException("User not found"));

        String planStr = user.getPlan() != null ? user.getPlan().name() : null;

        String newAccessToken = jwtProvider.generateAccessToken(
                user.getId(),
                user.getEmail().getValue(),
                user.getRole().name(),
                planStr
        );

        return Map.of(
                "accessToken", newAccessToken,
                "expiresIn", jwtProvider.getAccessTokenExpirationMs() / 1000
        );
    }
}
