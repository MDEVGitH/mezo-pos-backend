package com.mezo.pos.plan.infrastructure.web;

import com.mezo.pos.plan.application.HandlePaymentWebhookUseCase;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/webhooks")
@RequiredArgsConstructor
@Slf4j
public class WompiWebhookController {

    private final HandlePaymentWebhookUseCase handlePaymentWebhookUseCase;

    @PostMapping("/wompi")
    public ResponseEntity<Void> handleWompiWebhook(
            @RequestBody Map<String, Object> payload,
            @RequestHeader(value = "x-event-checksum", required = false) String signature) {

        try {
            handlePaymentWebhookUseCase.execute(payload, signature);
        } catch (Exception e) {
            log.error("Error processing Wompi webhook: {}", e.getMessage());
        }

        // Always return 200 to prevent Wompi from retrying
        return ResponseEntity.ok().build();
    }
}
