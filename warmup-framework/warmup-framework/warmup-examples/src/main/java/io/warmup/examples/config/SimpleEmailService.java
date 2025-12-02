package io.warmup.examples.config;

import io.warmup.framework.services.EmailService;

/**
 * Simple email service implementation for conditional property examples.
 */
public class SimpleEmailService implements EmailService {
    private final String type;
    
    public SimpleEmailService(String type) {
        this.type = type;
    }
    
    @Override
    public void sendEmail(String to, String subject, String body) {
        System.out.println("Email sent via " + type + " to: " + to);
    }
    
    @Override
    public void sendSimpleMessage(String message) {
        System.out.println("Simple message via " + type + ": " + message);
    }
}