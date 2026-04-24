package com.mezo.pos.auth.domain.port;

public interface EmailService {
    void sendOtp(String to, String code);
    void sendInvitation(String to, String businessName, String role);
    void sendTeamAddedNotification(String to, String businessName, String role);
}
