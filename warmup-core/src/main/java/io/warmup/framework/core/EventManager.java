package io.warmup.framework.core;

import io.warmup.framework.event.Event;
import io.warmup.framework.event.EventListenerMethod;
// import io.warmup.framework.jit.asm.SimpleASMUtils; // NOT USED - MIGRATED to AsmCoreUtils
import io.warmup.framework.asm.AsmCoreUtils;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class EventManager {

    private static final Logger log = Logger.getLogger(EventManager.class.getName());

    // 🚀 SISTEMA ÚNICO: EventIndexEngine para búsqueda O(1) de listeners
    // CONSOLIDADO: Un solo sistema sin duplicación, eliminando overhead O(n)
    private final EventIndexEngine eventIndexEngine = new EventIndexEngine();

    public void registerEventListeners(Class<?> clazz, Object instance) {
        // ✅ ASM: Usar AsmCoreUtils para obtener métodos con @EventListener usando bytecode
        List<Method> eventListenerMethods = AsmCoreUtils.getAnnotatedMethods(clazz, "io.warmup.framework.annotation.EventListener");
        
        for (Method method : eventListenerMethods) {
            Class<?>[] parameterTypes = method.getParameterTypes();
            if (parameterTypes.length == 1 && Event.class.isAssignableFrom(parameterTypes[0])) {
                @SuppressWarnings("unchecked")
                Class<? extends Event> eventType = (Class<? extends Event>) parameterTypes[0];
                EventListenerMethod listener = new EventListenerMethod(instance, method);
                
                // 🚀 SISTEMA ÚNICO: Registro optimizado O(1) usando EventIndexEngine
                // ELIMINADO: Sistema legacy duplicado para reducir memoria y overhead
                eventIndexEngine.registerListener(eventType, listener);
                        
            } else {
                log.log(Level.SEVERE, "Método @EventListener debe tener exactamente un parámetro de tipo Event: {0}", method.getName());
            }
        }
    }

    // 🚀 NUEVO: Publicar eventos con búsqueda O(1) de listeners
    public void dispatchEvent(Event event) {
        // ✅ OPTIMIZADO: Usar EventIndexEngine para búsqueda O(1)
        eventIndexEngine.dispatchEvent(event);
        
        // 📊 Métricas de rendimiento (opcional para debugging)
        if (log.isLoggable(Level.FINE)) {
            Map<String, Object> metrics = eventIndexEngine.getPerformanceMetrics();
            log.log(Level.FINE, "Event dispatch metrics: {0}", metrics);
        }
    }

    // 🚀 MÉTODO LEGACY: Solo para compatibilidad, redirige al sistema O(1)
    // DEPRECADO: Eliminar en próxima versión mayor
    public void dispatchEventLegacy(Event event) {
        log.log(Level.WARNING, "dispatchEventLegacy() está deprecated. Usar dispatchEvent() directamente.");
        dispatchEvent(event);
    }

    // Método para obtener listeners (solo para debug o introspección)
    // ÚTIL: Para verificar qué listeners están registrados para un tipo de evento
    public List<EventListenerMethod> getListenersForEvent(Class<? extends Event> eventType) {
        return eventIndexEngine.getListenersForEvent(eventType);
    }

    // Método para limpiar listeners (para reset o shutdown)
    // EFICIENTE: Limpia ambos índices en una sola operación
    public void clearListeners() {
        eventIndexEngine.clearCaches();
        eventIndexEngine.rebuildAllIndices(); // Limpieza completa
    }

    // 🚀 NUEVO: Obtener métricas de rendimiento O(1)
    public Map<String, Object> getPerformanceMetrics() {
        return eventIndexEngine.getPerformanceMetrics();
    }

    // 🚀 RECONSTRUIR ÍNDICES: Para consistencia después de cambios mayores
    // ÚTIL: En escenarios de hot-reload o cambios dinámicos de configuración
    public void rebuildIndices() {
        eventIndexEngine.rebuildAllIndices();
    }
    
    public void initialize() {
        log.log(Level.INFO, "EventManager initialized");
    }
    
    public void warmupEventSystem() {
        log.log(Level.INFO, "Warming up event system");
        rebuildIndices();
    }
}
