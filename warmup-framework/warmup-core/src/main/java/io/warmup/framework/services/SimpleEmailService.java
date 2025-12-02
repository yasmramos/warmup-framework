package io.warmup.framework.services;

import java.util.logging.Logger;

/**
 * Simple email service implementation that logs messages.
 * 
 * @author MiniMax Agent
 * @since 2.0
 */
public class SimpleEmailService implements EmailService {
    
    private static final Logger log = Logger.getLogger(SimpleEmailService.class.getName());

    @Override
    public void sendEmail(String to, String subject, String body) {
        log.info("EMAIL SENT: To=" + to + ", Subject=" + subject + ", Body=" + body);
    }

    @Override
    public void sendSimpleMessage(String message) {
        log.info("EMAIL MESSAGE: " + message);
    }
}