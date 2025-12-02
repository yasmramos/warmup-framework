package io.warmup.framework.services;

/**
 * Basic interface for email operations.
 * Provides the core contract for email service implementations.
 * 
 * @author MiniMax Agent
 * @since 2.0
 */
public interface EmailService {
    
    /**
     * Send an email.
     * 
     * @param to recipient email address
     * @param subject email subject
     * @param body email body content
     */
    void sendEmail(String to, String subject, String body);
    
    /**
     * Send a simple message.
     * 
     * @param message the message to send
     */
    void sendSimpleMessage(String message);
}