package io.warmup.examples;

import io.warmup.framework.annotation.Component;
import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * Servicio de notificaciones simplificado.
 * Gestiona el envío de notificaciones a través de diferentes canales.
 */
@Component
public class NotificationService {
    
    private final List<String> notificationChannels;
    
    public NotificationService() {
        this.notificationChannels = new ArrayList<>();
        initializeChannels();
        
        System.out.println("NotificationService inicializado:");
        System.out.println("   • Canales disponibles: " + notificationChannels.size());
        System.out.println("   • Canales: " + notificationChannels);
    }
    
    private void initializeChannels() {
        notificationChannels.addAll(Arrays.asList(
            "email", "sms", "push", "webhook"
        ));
    }
    
    public void notifyAll(String message) {
        System.out.println("\n--- Notificando a TODOS los canales (" + notificationChannels.size() + " canales) ---");
        for (String channel : notificationChannels) {
            sendToChannel(channel, message);
        }
    }
    
    public void notifyByChannel(String channel, String message) {
        System.out.println("\n--- Notificando por canal: '" + channel + "' ---");
        if (notificationChannels.contains(channel)) {
            System.out.println("✅ Canal encontrado: " + channel);
            sendToChannel(channel, message);
        } else {
            System.out.println("❌ Canal no encontrado: '" + channel + "'");
            System.out.println("   🔍 Canales disponibles: " + notificationChannels);
        }
    }
    
    private void sendToChannel(String channel, String message) {
        String formattedMessage = String.format("[%s] %s", channel.toUpperCase(), message);
        System.out.println("📢 Enviado: " + formattedMessage);
    }
    
    public List<String> getAvailableChannels() {
        return new ArrayList<>(notificationChannels);
    }
}
