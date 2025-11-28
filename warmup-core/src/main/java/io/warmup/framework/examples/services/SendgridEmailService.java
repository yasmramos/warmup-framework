package io.warmup.framework.examples.services;

/**
 * Servicio de email usando SendGrid.
 */
public class SendgridEmailService implements EmailService {
    
    @Override
    public void sendEmail(String to, String subject, String body) {
        System.out.println("📧 SendGrid: API call to send email");
        System.out.println("   To: " + to);
        System.out.println("   Subject: " + subject);
    }
    
    @Override
    public String getProviderInfo() {
        return "SendGrid Email Service - REST API based";
    }
}