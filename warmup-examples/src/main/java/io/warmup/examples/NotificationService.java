package io.warmup.examples;

import io.warmup.framework.annotation.Component;
import io.warmup.framework.annotation.Inject;
import java.util.List;
import java.util.Map;

@Component
public class NotificationService {

    private final List<Notifier> allNotifiers;
    private final Map<String, Notifier> namedNotifiers;

    @Inject
    public NotificationService(List<Notifier> allNotifiers, Map<String, Notifier> namedNotifiers) {
        this.allNotifiers = allNotifiers;
        this.namedNotifiers = namedNotifiers;

        System.out.println("NotificationService inicializado:");
        System.out.println("   • List: " + allNotifiers.size() + " notificadores");
        System.out.println("   • Map: " + namedNotifiers.size() + " entradas");
        System.out.println("   • Claves disponibles: " + namedNotifiers.keySet());
    }

    public void notifyAll(String message) {
        System.out.println("\n--- Notificando a TODOS (" + allNotifiers.size() + " notificadores) ---");
        for (Notifier notifier : allNotifiers) {
            notifier.send(message);
        }
    }

    public void notifyByName(String name, String message) {
        System.out.println("\n--- Notificando por nombre: '" + name + "' ---");
        Notifier notifier = namedNotifiers.get(name);
        if (notifier != null) {
            System.out.println("✅ Encontrado: " + notifier.getClass().getSimpleName());
            notifier.send(message);
        } else {
            System.out.println("❌ No encontrado: '" + name + "'");
            System.out.println("   🔍 Claves disponibles: " + namedNotifiers.keySet());
        }
    }
}
