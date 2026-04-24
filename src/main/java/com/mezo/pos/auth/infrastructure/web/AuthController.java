package com.mezo.pos.auth.infrastructure.web;

import com.mezo.pos.auth.application.LoginUseCase;
import com.mezo.pos.auth.application.RefreshTokenUseCase;
import com.mezo.pos.auth.application.RegisterUserUseCase;
import com.mezo.pos.auth.application.ResendOtpUseCase;
import com.mezo.pos.auth.application.VerifyOtpUseCase;
import com.mezo.pos.auth.infrastructure.web.dto.AuthResponse;
import com.mezo.pos.auth.infrastructure.web.dto.LoginRequest;
import com.mezo.pos.auth.infrastructure.web.dto.RefreshTokenRequest;
import com.mezo.pos.auth.infrastructure.web.dto.RegisterRequest;
import com.mezo.pos.auth.infrastructure.web.dto.VerifyOtpRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final RegisterUserUseCase registerUserUseCase;
    private final LoginUseCase loginUseCase;
    private final VerifyOtpUseCase verifyOtpUseCase;
    private final ResendOtpUseCase resendOtpUseCase;
    private final RefreshTokenUseCase refreshTokenUseCase;

    @PostMapping("/register")
    public ResponseEntity<Map<String, Object>> register(@Valid @RequestBody RegisterRequest request) {
        Map<String, Object> result = registerUserUseCase.execute(request.getEmail(), request.getPassword());
        return ResponseEntity.status(HttpStatus.CREATED).body(result);
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        AuthResponse response = loginUseCase.execute(request.getEmail(), request.getPassword());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/verify-otp")
    public ResponseEntity<Map<String, String>> verifyOtp(@Valid @RequestBody VerifyOtpRequest request) {
        verifyOtpUseCase.execute(request.getEmail(), request.getCode());
        return ResponseEntity.ok(Map.of("message", "Email verificado correctamente"));
    }

    @PostMapping("/resend-otp")
    public ResponseEntity<Map<String, String>> resendOtp(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        resendOtpUseCase.execute(email);
        return ResponseEntity.ok(Map.of("message", "OTP reenviado a " + email));
    }

    @PostMapping("/refresh")
    public ResponseEntity<Map<String, Object>> refresh(@Valid @RequestBody RefreshTokenRequest request) {
        Map<String, Object> result = refreshTokenUseCase.execute(request.getRefreshToken());
        return ResponseEntity.ok(result);
    }
}
