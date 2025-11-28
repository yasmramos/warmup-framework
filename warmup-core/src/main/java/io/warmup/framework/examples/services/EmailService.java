package io.warmup.framework.examples.services;

/**
 * Interfaz para servicios de email.
 */
public interface EmailService {
    void sendEmail(String to, String subject, String body);
    String getProviderInfo();
}