package com.mezo.pos.plan.infrastructure.adapter;

import com.mezo.pos.plan.domain.enums.PlanType;
import com.mezo.pos.plan.domain.port.PaymentGateway;
import com.mezo.pos.shared.domain.valueobject.Money;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@Component
@Slf4j
public class WompiPaymentGateway implements PaymentGateway {

    private final RestClient restClient;
    private final String publicKey;
    private final String privateKey;
    private final String eventsSecret;
    private final String redirectUrl;

    public WompiPaymentGateway(
            RestClient restClient,
            @Value("${mezo.wompi.public-key}") String publicKey,
            @Value("${mezo.wompi.private-key}") String privateKey,
            @Value("${mezo.wompi.events-secret}") String eventsSecret,
            @Value("${mezo.wompi.redirect-url}") String redirectUrl) {
        this.restClient = restClient;
        this.publicKey = publicKey;
        this.privateKey = privateKey;
        this.eventsSecret = eventsSecret;
        this.redirectUrl = redirectUrl;
    }

    @Override
    public PaymentLink createPaymentLink(UUID userId, PlanType planType, Money price) {
        String reference = "sub_" + userId + "_" + planType + "_" + Instant.now().toEpochMilli();
        long amountInCents = price.getAmount().longValue() * 100;

        Map<String, Object> body = Map.of(
                "name", "Mezo POS - Plan " + planType.name(),
                "description", "Suscripcion al plan " + planType.name(),
                "single_use", false,
                "collect_shipping", false,
                "currency", price.getCurrency().name(),
                "amount_in_cents", amountInCents,
                "redirect_url", redirectUrl,
                "reference", reference
        );

        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> response = restClient.post()
                    .uri("https://production.wompi.co/v1/payment_links")
                    .header("Authorization", "Bearer " + privateKey)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(body)
                    .retrieve()
                    .body(Map.class);

            if (response != null && response.containsKey("data")) {
                @SuppressWarnings("unchecked")
                Map<String, Object> data = (Map<String, Object>) response.get("data");
                String id = (String) data.get("id");
                String url = "https://checkout.wompi.co/l/" + id;
                return new PaymentLink(id, url);
            }
        } catch (Exception e) {
            log.error("Error creating Wompi payment link: {}", e.getMessage());
        }

        return new PaymentLink(null, null);
    }

    @Override
    public boolean validateWebhookSignature(String payload, String signature) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            SecretKeySpec secretKeySpec = new SecretKeySpec(
                    eventsSecret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
            mac.init(secretKeySpec);
            byte[] hash = mac.doFinal(payload.getBytes(StandardCharsets.UTF_8));

            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }

            return hexString.toString().equals(signature);
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            log.error("Error validating webhook signature: {}", e.getMessage());
            return false;
        }
    }
}
