package io.warmup.framework.examples.services;

/**
 * Servicio de email usando AWS SES.
 */
public class SesEmailService implements EmailService {
    
    @Override
    public void sendEmail(String to, String subject, String body) {
        System.out.println("📧 SES: AWS API call for email delivery");
        System.out.println("   Recipient: " + to);
        System.out.println("   Subject: " + subject);
    }
    
    @Override
    public String getProviderInfo() {
        return "AWS SES Email Service - Cloud-based email service";
    }
}