package io.warmup.framework.examples.services;

public class NotificationServiceImpl implements NotificationService {
    @Override
    public void sendNotification(String message) {
        System.out.println("🔔 Notification: " + message);
    }
    
    @Override
    public String getNotificationChannels() {
        return "Multi-channel Notification Service - Webhook, Slack, Discord";
    }
}