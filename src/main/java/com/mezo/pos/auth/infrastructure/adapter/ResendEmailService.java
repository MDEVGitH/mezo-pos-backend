package com.mezo.pos.auth.infrastructure.adapter;

import com.mezo.pos.auth.domain.port.EmailService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.Map;

@Component
@Slf4j
public class ResendEmailService implements EmailService {

    private final RestClient restClient;
    private final String apiKey;
    private final String fromEmail;

    public ResendEmailService(
            RestClient restClient,
            @Value("${mezo.resend.api-key}") String apiKey,
            @Value("${mezo.resend.from-email}") String fromEmail) {
        this.restClient = restClient;
        this.apiKey = apiKey;
        this.fromEmail = fromEmail;
    }

    @Override
    public void sendOtp(String to, String code) {
        String html = "<div style='font-family: sans-serif; padding: 20px;'>"
                + "<h2>Tu codigo de verificacion</h2>"
                + "<p>Tu codigo es: <strong style='font-size: 24px;'>" + code + "</strong></p>"
                + "<p>Este codigo expira en 10 minutos.</p>"
                + "</div>";

        sendEmail(to, "Tu codigo de verificacion - Mezo", html);
    }

    @Override
    public void sendInvitation(String to, String businessName, String role) {
        String html = "<div style='font-family: sans-serif; padding: 20px;'>"
                + "<h2>Te invitaron a " + businessName + "</h2>"
                + "<p>Has sido invitado a unirte al equipo de <strong>" + businessName + "</strong> como <strong>" + role + "</strong> en Mezo POS.</p>"
                + "<p>Registrate en <a href='https://app.mezo.co'>mezo.co</a> para aceptar la invitacion.</p>"
                + "</div>";

        sendEmail(to, "Invitacion a " + businessName + " - Mezo", html);
    }

    @Override
    public void sendTeamAddedNotification(String to, String businessName, String role) {
        String html = "<div style='font-family: sans-serif; padding: 20px;'>"
                + "<h2>Te agregaron a " + businessName + "</h2>"
                + "<p>Has sido agregado al equipo de <strong>" + businessName + "</strong> con el rol de <strong>" + role + "</strong>.</p>"
                + "<p>Inicia sesion en <a href='https://app.mezo.co'>mezo.co</a> para empezar.</p>"
                + "</div>";

        sendEmail(to, "Agregado a " + businessName + " - Mezo", html);
    }

    private void sendEmail(String to, String subject, String html) {
        try {
            Map<String, Object> body = Map.of(
                    "from", fromEmail,
                    "to", new String[]{to},
                    "subject", subject,
                    "html", html
            );

            restClient.post()
                    .uri("https://api.resend.com/emails")
                    .header("Authorization", "Bearer " + apiKey)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(body)
                    .retrieve()
                    .toBodilessEntity();

            log.info("Email sent to {} with subject: {}", to, subject);
        } catch (Exception e) {
            log.error("Failed to send email to {}: {}", to, e.getMessage());
        }
    }
}
