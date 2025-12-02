package io.warmup.examples.config;

/**
 * Notification service implementation for sending notifications.
 */
public class NotificationServiceImpl {
    
    public void sendNotification(String message) {
        System.out.println("Sending notification: " + message);
    }
    
    public void sendBulkNotification(String[] messages) {
        for (String message : messages) {
            sendNotification(message);
        }
    }
}