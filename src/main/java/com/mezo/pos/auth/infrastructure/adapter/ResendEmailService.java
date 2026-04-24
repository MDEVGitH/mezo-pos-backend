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

    @Override
    public void sendWelcome(String to, String businessName) {
        String html = "<div style='font-family: sans-serif; padding: 20px; max-width: 500px; margin: 0 auto;'>"
                + "<div style='text-align: center; margin-bottom: 24px;'>"
                + "<span style='font-size: 48px;'>🎉</span>"
                + "</div>"
                + "<h1 style='color: #C8903F; text-align: center; margin-bottom: 8px;'>Bienvenido a Mezo</h1>"
                + "<p style='text-align: center; color: #666; font-size: 16px; margin-bottom: 24px;'>"
                + "<strong>" + businessName + "</strong> ya esta en mezo"
                + "</p>"
                + "<div style='background: #f9f6f0; border-radius: 12px; padding: 20px; margin-bottom: 24px;'>"
                + "<p style='margin: 0 0 12px; font-size: 14px; color: #333;'>Esto es lo que puedes hacer ahora:</p>"
                + "<ul style='margin: 0; padding-left: 20px; color: #555; font-size: 14px; line-height: 1.8;'>"
                + "<li>Crear tu menu de productos</li>"
                + "<li>Configurar tus mesas</li>"
                + "<li>Empezar a vender desde el POS</li>"
                + "<li>Invitar a tu equipo (cajeros, meseros, cocina)</li>"
                + "<li>Ver reportes de ventas en tiempo real</li>"
                + "</ul>"
                + "</div>"
                + "<p style='font-size: 14px; color: #666; margin-bottom: 16px;'>"
                + "Tienes <strong>30 dias gratis</strong> del plan Pro. "
                + "Sin limites, sin tarjeta de credito, sin letra pequena."
                + "</p>"
                + "<div style='text-align: center; margin-bottom: 24px;'>"
                + "<a href='https://app.mezo.co' style='display: inline-block; background: #C8903F; color: white; "
                + "text-decoration: none; padding: 12px 32px; border-radius: 8px; font-weight: bold; font-size: 14px;'>"
                + "Ir a mi dashboard"
                + "</a>"
                + "</div>"
                + "<p style='font-size: 12px; color: #999; text-align: center;'>"
                + "Si tienes preguntas, responde este correo. Te leemos siempre."
                + "</p>"
                + "</div>";

        sendEmail(to, "Bienvenido a Mezo — " + businessName + " ya esta listo", html);
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
