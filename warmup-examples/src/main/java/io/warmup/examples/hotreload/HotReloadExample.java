package io.warmup.examples.hotreload;

import io.warmup.framework.core.WarmupContainer;
import io.warmup.framework.event.EventBus;

import java.util.concurrent.TimeUnit;

/**
 * Ejemplo de uso del sistema de Hot Reload
 * 
 * @author MiniMax Agent
 * @since 1.0
 */
public class HotReloadExample {
    
    public static void main(String[] args) {
        try {
            System.out.println("🔥 Starting Warmup Framework with Hot Reload...");
            
            // Crear container
            WarmupContainer container = new WarmupContainer();
            
            // Obtener event bus
            EventBus eventBus = container.getBean(EventBus.class);
            
            // Crear y configurar hot reload manager
            HotReloadManager hotReloadManager = new HotReloadManager(container, eventBus);
            
            // Habilitar hot reload
            hotReloadManager.enable();
            
            // Monitorear directorio de clases
            String currentDir = System.getProperty("user.dir");
            System.out.println("📁 Monitoring directory: " + currentDir);
            hotReloadManager.monitorDirectory(currentDir);
            
            // Configurar listener de eventos de hot reload
            setupEventListener(eventBus);
            
            // Mostrar status cada 30 segundos
            setupStatusReporter(hotReloadManager);
            
            System.out.println("✅ Hot Reload system is active!");
            System.out.println("📝 Modify .java or .class files to see hot reload in action");
            System.out.println("⚡ Press Ctrl+C to stop");
            
            // Mantener aplicación ejecutándose
            keepAlive();
            
        } catch (Exception e) {
            System.err.println("❌ Failed to start Hot Reload system: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private static void setupEventListener(EventBus eventBus) {
        eventBus.subscribe(HotReloadEvent.class, event -> {
            HotReloadEvent hotReloadEvent = (HotReloadEvent) event;
            switch (hotReloadEvent.getType()) {
                case ENABLED:
                    System.out.println("🔥 " + hotReloadEvent.getMessage());
                    break;
                case CLASS_RELOADED:
                    System.out.println("✅ " + hotReloadEvent.getMessage());
                    break;
                case FAILURE:
                    System.out.println("❌ " + hotReloadEvent.getMessage());
                    break;
                case DIRECTORY_MONITORED:
                    System.out.println("📝 " + hotReloadEvent.getMessage());
                    break;
                default:
                    System.out.println("ℹ️  " + hotReloadEvent.getMessage());
            }
        });
    }
    
    private static void setupStatusReporter(HotReloadManager hotReloadManager) {
        new Thread(() -> {
            while (true) {
                try {
                    Thread.sleep(30000); // 30 segundos
                    
                    HotReloadStatus status = hotReloadManager.getStatus();
                    System.out.println("\n📊 Hot Reload Status: " + status);
                    
                } catch (InterruptedException e) {
                    break;
                }
            }
        }, "HotReloadStatusReporter").start();
    }
    
    private static void keepAlive() {
        try {
            // Mantener la aplicación viva
            while (true) {
                Thread.sleep(1000);
            }
        } catch (InterruptedException e) {
            System.out.println("🛑 Shutting down Hot Reload system...");
        }
    }
}