package io.warmup.framework.event;

import io.warmup.framework.core.DependencyRegistry;
import io.warmup.framework.core.Dependency;
import io.warmup.framework.core.WarmupContainer;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * 🎯 EVENTBUS SINGLETON RESOLVER - Optimizado v2.0
 * 
 * Resuelve el problema de EventBus bean resolution mediante:
 * - Singleton pattern con Thread-Safe initialization
 * - Cached bean resolution (O(1) lookup)
 * - Lazy initialization con double-checked locking
 * - Direct registration en DependencyRegistry
 * 
 * @author MiniMax Agent
 * @version 2.0
 */
public class EventBusResolver {
    
    private static final Logger log = Logger.getLogger(EventBusResolver.class.getName());
    
    // ⚡ Singleton EventBus (Thread-Safe)
    private static volatile EventBus INSTANCE;
    private static final Object LOCK = new Object();
    
    // ⚡ Cache de beans resueltos (O(1) lookup)
    private static final ConcurrentHashMap<Class<?>, Object> beanCache = 
        new ConcurrentHashMap<>(16, 0.75f, 2);
    
    // ⚡ Atomic flags para initialization tracking
    private static final AtomicBoolean INITIALIZED = new AtomicBoolean(false);
    
    /**
     * 🎯 Resolve EventBus singleton (Thread-Safe)
     * 
     * @param dependencyRegistry DependencyRegistry del container
     * @param container WarmupContainer reference
     * @return EventBus instance
     */
    public static EventBus resolveEventBus(DependencyRegistry dependencyRegistry, WarmupContainer container) {
        // Double-checked locking for thread-safe singleton
        if (INSTANCE == null) {
            synchronized (LOCK) {
                if (INSTANCE == null) {
                    INSTANCE = createEventBus(dependencyRegistry, container);
                    INITIALIZED.set(true);
                }
            }
        }
        
        return INSTANCE;
    }
    
    /**
     * 🎯 Ensure EventBus is registered in DependencyRegistry
     * 
     * @param dependencyRegistry DependencyRegistry
     * @param container WarmupContainer
     */
    public static void ensureEventBusRegistered(DependencyRegistry dependencyRegistry, WarmupContainer container) {
        System.out.println("🔍 [DEBUG] ensureEventBusRegistered() INICIADO");
        System.out.println("🔍 [DEBUG] - dependencyRegistry: " + (dependencyRegistry != null ? "NO_NULL" : "NULL"));
        System.out.println("🔍 [DEBUG] - container: " + (container != null ? "NO_NULL" : "NULL"));
        
        try {
            // ✅ FIX: Check if already registered directly in dependencies map (avoid getBean() which requires container)
            System.out.println("🔍 [DEBUG] Checking si EventBus ya está registrado en dependencies map...");
            Dependency existingDependency = dependencyRegistry.getDependency(EventBus.class);
            System.out.println("🔍 [DEBUG] getDependency(EventBus.class) resultado: " + (existingDependency != null ? "EXISTS" : "NULL"));
            
            if (existingDependency != null) {
                System.out.println("🔍 [DEBUG] EventBus ya registrado en dependencies, saliendo...");
                return; // Already registered
            }
            
            // Resolve EventBus (creates if needed)
            System.out.println("🔍 [DEBUG] Resolviendo EventBus...");
            EventBus eventBus = resolveEventBus(dependencyRegistry, container);
            System.out.println("🔍 [DEBUG] EventBus resuelto: " + (eventBus != null ? "NO_NULL" : "NULL"));
            
            if (eventBus == null) {
                System.out.println("❌ [DEBUG] EventBus resuelto es NULL!");
                return;
            }
            
            // Direct registration (no reflection)
            System.out.println("🔍 [DEBUG] Registrando EventBus en DependencyRegistry...");
            dependencyRegistry.register(EventBus.class, eventBus);
            System.out.println("🔍 [DEBUG] Register EventBus completado sin excepción aparente");
            
            // Also register EventPublisher
            System.out.println("🔍 [DEBUG] Registrando EventPublisher...");
            EventPublisher eventPublisher = new EventPublisher(eventBus);
            dependencyRegistry.register(EventPublisher.class, eventPublisher);
            System.out.println("🔍 [DEBUG] Register EventPublisher completado");
            
            // Cache registration for fast lookup
            beanCache.put(EventBus.class, eventBus);
            beanCache.put(EventPublisher.class, eventPublisher);
            
            System.out.println("✅ [DEBUG] EventBus y EventPublisher registrados exitosamente");
            log.log(Level.FINEST, "✅ EventBus y EventPublisher registrados exitosamente");
            
        } catch (Exception e) {
            System.out.println("❌ [DEBUG] Excepción capturada en ensureEventBusRegistered: " + e.getClass().getName() + " - " + e.getMessage());
            System.out.println("❌ [DEBUG] Stack trace:");
            e.printStackTrace();
            log.log(Level.WARNING, "⚠️ Error registrando EventBus: {0}", e.getMessage());
            log.log(Level.WARNING, "⚠️ Stack trace completo:", e);
            // Continue with minimal setup
        }
    }
    
    /**
     * 🎯 Get cached bean (O(1) lookup)
     */
    public static <T> T getCachedBean(Class<T> type) {
        return type.cast(beanCache.get(type));
    }
    
    /**
     * 🎯 Clear bean cache (useful for testing)
     */
    public static void clearBeanCache() {
        beanCache.clear();
    }
    
    /**
     * 🎯 Get initialization status
     */
    public static boolean isInitialized() {
        return INITIALIZED.get();
    }
    
    /**
     * 🔧 Create EventBus with optimal configuration
     */
    private static EventBus createEventBus(DependencyRegistry dependencyRegistry, WarmupContainer container) {
        try {
            // Create EventBus con optimal configuration
            System.out.println("🔧 [DEBUG] Creando nueva instancia de EventBus...");
            EventBus eventBus = new EventBus();
            System.out.println("🔧 [DEBUG] EventBus creado: " + (eventBus != null ? "NO_NULL" : "NULL"));
            
            // Register essential event listeners si existen
            registerEssentialEventListeners(eventBus, dependencyRegistry, container);
            
            log.log(Level.FINEST, "✅ EventBus singleton creado exitosamente");
            
            return eventBus;
            
        } catch (Exception e) {
            log.log(Level.WARNING, "⚠️ Error creando EventBus: {0}", e.getMessage());
            
            // Return basic EventBus on error
            return new EventBus();
        }
    }
    
    /**
     * 🔧 Register essential event listeners
     */
    private static void registerEssentialEventListeners(
            EventBus eventBus, 
            DependencyRegistry dependencyRegistry, 
            WarmupContainer container) {
        
        try {
            // Register container lifecycle listeners
            registerContainerLifecycleListeners(eventBus, container);
            
        } catch (Exception e) {
            log.log(Level.FINEST, "⚠️ No se pudieron registrar event listeners esenciales: {0}", e.getMessage());
            // Continue without listeners
        }
    }
    
    /**
     * 🔧 Register container lifecycle event listeners
     * 
     * Registra listeners esenciales para eventos del container lifecycle
     * Incluyendo startup, shutdown y health events
     */
    private static void registerContainerLifecycleListeners(EventBus eventBus, WarmupContainer container) {
        try {
            // Register startup completion listener
            registerStartupListener(eventBus);
            
            // Register shutdown initiation listener
            registerShutdownListener(eventBus);
            
            // Register health check listener
            registerHealthListener(eventBus);
            
            log.log(Level.FINEST, "✅ Container lifecycle listeners registrados");
            
        } catch (Exception e) {
            log.log(Level.FINEST, "⚠️ Error registrando lifecycle listeners: {0}", e.getMessage());
            // Continue without listeners - container should still work
        }
    }
    
    /**
     * 🎯 Register startup completion listener
     */
    private static void registerStartupListener(EventBus eventBus) {
        // Register a listener for container startup events
        // In a real implementation, this would listen for startup completion events
        eventBus.registerListener(Object.class, new EventListener<Object>() {
            @Override
            public void onEvent(Object event) {
                // Handle startup events (would be more specific in real implementation)
                log.log(Level.FINEST, "📡 Startup event received: {0}", event.getClass().getSimpleName());
            }
        });
    }
    
    /**
     * 🛑 Register shutdown initiation listener
     */
    private static void registerShutdownListener(EventBus eventBus) {
        // Register a listener for shutdown events
        eventBus.registerListener(Object.class, new EventListener<Object>() {
            @Override
            public void onEvent(Object event) {
                // Handle shutdown events (would be more specific in real implementation)
                String className = event.getClass().getSimpleName();
                if (className.contains("Shutdown") || className.contains("Close")) {
                    log.log(Level.FINEST, "🛑 Shutdown event received: {0}", className);
                }
            }
        });
    }
    
    /**
     * 🏥 Register health check listener
     */
    private static void registerHealthListener(EventBus eventBus) {
        // Register a listener for health check events
        eventBus.registerListener(Object.class, new EventListener<Object>() {
            @Override
            public void onEvent(Object event) {
                // Handle health check events (would be more specific in real implementation)
                String className = event.getClass().getSimpleName();
                if (className.contains("Health") || className.contains("Check")) {
                    log.log(Level.FINEST, "🏥 Health check event received: {0}", className);
                }
            }
        });
    }
    
    /**
     * 🧹 Reset singleton (useful for testing)
     */
    public static void reset() {
        synchronized (LOCK) {
            INSTANCE = null;
            INITIALIZED.set(false);
            clearBeanCache();
        }
    }
    
    /**
     * 🧹 Shutdown EventBus singleton
     */
    public static void shutdown() {
        synchronized (LOCK) {
            if (INSTANCE != null) {
                // Add shutdown cleanup if needed
                log.log(Level.FINEST, "🧹 EventBus singleton shutdown");
            }
            
            reset();
        }
    }
}