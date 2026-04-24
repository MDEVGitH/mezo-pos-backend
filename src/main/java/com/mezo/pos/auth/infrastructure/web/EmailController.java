package com.mezo.pos.auth.infrastructure.web;

import com.mezo.pos.auth.domain.port.EmailService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/email")
@RequiredArgsConstructor
public class EmailController {

    private final EmailService emailService;

    @PostMapping("/welcome")
    public ResponseEntity<Map<String, String>> welcome(@RequestBody Map<String, Object> request) {
        String email = (String) request.get("email");
        @SuppressWarnings("unchecked")
        Map<String, Object> negocio = (Map<String, Object>) request.get("negocio");
        String businessName = negocio != null ? (String) negocio.getOrDefault("name", "tu negocio") : "tu negocio";

        emailService.sendWelcome(email, businessName);
        return ResponseEntity.ok(Map.of("message", "Email de bienvenida enviado"));
    }
}
