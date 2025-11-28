package io.warmup.framework.examples.services;

public interface NotificationService {
    void sendNotification(String message);
    String getNotificationChannels();
}