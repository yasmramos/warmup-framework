package io.warmup.framework.examples.services;

/**
 * Servicio de email usando SMTP.
 */
public class SmtpEmailService implements EmailService {
    
    @Override
    public void sendEmail(String to, String subject, String body) {
        System.out.println("📧 SMTP: Sending email to " + to);
        System.out.println("   Subject: " + subject);
        System.out.println("   Body: " + body.substring(0, Math.min(50, body.length())) + "...");
    }
    
    @Override
    public String getProviderInfo() {
        return "SMTP Email Service - Traditional SMTP protocol";
    }
}