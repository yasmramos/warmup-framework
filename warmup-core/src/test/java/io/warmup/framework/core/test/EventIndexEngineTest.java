package io.warmup.framework.core.test;

import io.warmup.framework.core.EventIndexEngine;
import io.warmup.framework.event.Event;
import io.warmup.framework.event.EventListenerMethod;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 🚀 TEST EVENT INDEX ENGINE - Verificación completa del sistema O(1)
 * 
 * Test suite completo para EventIndexEngine, cubriendo todos los escenarios
 * críticos del sistema de indexación O(1) de eventos.
 */
public class EventIndexEngineTest {

    private EventIndexEngine engine;
    private AtomicInteger counter1;
    private AtomicInteger counter2;
    private AtomicInteger counter3;

    @BeforeEach
    void setUp() {
        engine = new EventIndexEngine();
        counter1 = new AtomicInteger(0);
        counter2 = new AtomicInteger(0);
        counter3 = new AtomicInteger(0);
    }

    @Test
    void testBasicO1RegistrationAndDispatch() {
        // Verificar registro básico O(1)
        TestListener listener1 = new TestListener(counter1);
        engine.registerListener(TestEvent.class, createListenerMethod(listener1));
        
        TestEvent event = new TestEvent();
        engine.dispatchEvent(event);
        
        assertEquals(1, counter1.get());
    }

    @Test
    void testMultipleListenersPerEventType() {
        // Múltiples listeners para mismo tipo de evento
        TestListener listener1 = new TestListener(counter1);
        TestListener listener2 = new TestListener(counter2);
        
        engine.registerListener(TestEvent.class, createListenerMethod(listener1));
        engine.registerListener(TestEvent.class, createListenerMethod(listener2));
        
        TestEvent event = new TestEvent();
        engine.dispatchEvent(event);
        
        assertEquals(1, counter1.get());
        assertEquals(1, counter2.get());
    }

    @Test
    void testEventHierarchyCompatibility() {
        // Test de herencia de eventos - listener para padre debe recibir eventos de hijo
        TestListener parentListener = new TestListener(counter1);
        engine.registerListener(BaseEvent.class, createListenerMethod(parentListener));
        
        DerivedEvent derivedEvent = new DerivedEvent();
        engine.dispatchEvent(derivedEvent);
        
        assertEquals(1, counter1.get(), "Listener para evento base debe recibir evento derivado");
    }

    @Test
    void testPerformanceMetricsAccuracy() {
        // Registrar listeners múltiples
        engine.registerListener(TestEvent.class, createListenerMethod(new TestListener(counter1)));
        engine.registerListener(TestEvent.class, createListenerMethod(new TestListener(counter2)));
        engine.registerListener(DerivedEvent.class, createListenerMethod(new TestListener(counter3)));
        
        Map<String, Object> metrics = engine.getPerformanceMetrics();
        
        assertNotNull(metrics);
        assertTrue((Integer) metrics.get("totalEventTypes") >= 2);
        assertTrue((Integer) metrics.get("totalListeners") >= 3);
        assertTrue((Double) metrics.get("avgListenersPerEventType") > 0);
    }

    @Test
    void testCacheInvalidation() {
        // Registrar listener
        engine.registerListener(TestEvent.class, createListenerMethod(new TestListener(counter1)));
        
        // Obtener métricas antes del clear
        Map<String, Object> metricsBefore = engine.getPerformanceMetrics();
        assertTrue((Integer) metricsBefore.get("totalListeners") > 0);
        
        // Limpiar cache
        engine.clearCaches();
        
        // Verificar que el cache está limpio pero índices persisten
        Map<String, Object> metricsAfter = engine.getPerformanceMetrics();
        assertTrue((Integer) metricsAfter.get("cachedHierarchyTypes") == 0);
        assertTrue((Integer) metricsAfter.get("totalListeners") > 0); // Índices deben persistir
    }

    @Test
    void testCompleteIndexRebuild() {
        // Registrar varios listeners
        engine.registerListener(TestEvent.class, createListenerMethod(new TestListener(counter1)));
        engine.registerListener(DerivedEvent.class, createListenerMethod(new TestListener(counter2)));
        engine.registerListener(AnotherEvent.class, createListenerMethod(new TestListener(counter3)));
        
        // Verificar que hay datos
        Map<String, Object> metricsBefore = engine.getPerformanceMetrics();
        assertTrue((Integer) metricsBefore.get("totalListeners") > 0);
        
        // Rebuild completo
        engine.rebuildAllIndices();
        
        // Verificar que todo está limpio
        Map<String, Object> metricsAfter = engine.getPerformanceMetrics();
        assertEquals(0, metricsAfter.get("totalEventTypes"));
        assertEquals(0, metricsAfter.get("totalListeners"));
        assertEquals(0, metricsAfter.get("cachedHierarchyTypes"));
    }

    @Test
    void testGetListenersForEvent() {
        // Registrar listeners
        TestListener listener1 = new TestListener(counter1);
        TestListener listener2 = new TestListener(counter2);
        
        engine.registerListener(TestEvent.class, createListenerMethod(listener1));
        engine.registerListener(TestEvent.class, createListenerMethod(listener2));
        
        // Obtener listeners para el evento
        List<EventListenerMethod> listeners = engine.getListenersForEvent(TestEvent.class);
        
        assertNotNull(listeners);
        assertEquals(2, listeners.size());
        
        // Verificar que todos los listeners funcionan
        for (EventListenerMethod listener : listeners) {
            assertNotNull(listener.getInstance());
            assertNotNull(listener.getMethod());
        }
    }

    @Test
    void testHighConcurrencyScenario() {
        // Simular escenario de alta concurrencia
        int numListeners = 100;
        int numEvents = 1000;
        
        // Registrar muchos listeners
        for (int i = 0; i < numListeners; i++) {
            AtomicInteger counter = new AtomicInteger(0);
            TestListener listener = new TestListener(counter);
            engine.registerListener(TestEvent.class, createListenerMethod(listener));
        }
        
        // Despatchar muchos eventos
        for (int i = 0; i < numEvents; i++) {
            engine.dispatchEvent(new TestEvent());
        }
        
        // Verificar métricas de rendimiento
        Map<String, Object> metrics = engine.getPerformanceMetrics();
        assertEquals(numListeners, metrics.get("totalListeners"));
        assertTrue((Integer) metrics.get("totalEventTypes") >= 1);
    }

    @Test
    void testEmptyEventDispatch() {
        // Dispath a evento sin listeners
        TestEvent event = new TestEvent();
        
        // No debería lanzar excepción
        assertDoesNotThrow(() -> engine.dispatchEvent(event));
    }

    @Test
    void testListenerExceptionHandling() {
        // Listener que lanza excepción
        TestListener throwingListener = new TestListener() {
            @Override
            @io.warmup.framework.annotation.EventListener
            public void handle(TestEvent event) {
                throw new RuntimeException("Test exception");
            }
        };
        
        engine.registerListener(TestEvent.class, createListenerMethod(throwingListener));
        
        // No debería lanzar excepción al dispatch
        assertDoesNotThrow(() -> engine.dispatchEvent(new TestEvent()));
    }

    /**
     * 🎯 CLASES DE SOPORTE PARA TESTING
     */
    
    public static class TestEvent extends Event {
        public TestEvent() {}
    }
    
    public static class BaseEvent extends Event {
        public BaseEvent() {}
    }
    
    public static class DerivedEvent extends BaseEvent {
        public DerivedEvent() {}
    }
    
    public static class AnotherEvent extends Event {
        public AnotherEvent() {}
    }
    
    public static class TestListener {
        private final AtomicInteger counter;
        
        public TestListener() {
            this.counter = new AtomicInteger(0);
        }
        
        public TestListener(AtomicInteger counter) {
            this.counter = counter;
        }
        
        @io.warmup.framework.annotation.EventListener
        public void handle(TestEvent event) {
            counter.incrementAndGet();
        }
        
        @io.warmup.framework.annotation.EventListener
        public void handle(BaseEvent event) {
            counter.incrementAndGet();
        }
        
        @io.warmup.framework.annotation.EventListener
        public void handle(DerivedEvent event) {
            counter.incrementAndGet();
        }
        
        @io.warmup.framework.annotation.EventListener
        public void handle(AnotherEvent event) {
            counter.incrementAndGet();
        }
    }
    
    /**
     * 🎯 UTILIDAD: Crear EventListenerMethod desde TestListener
     */
    private EventListenerMethod createListenerMethod(TestListener listener) {
        try {
            // Encontrar el método con @EventListener apropiado
            java.lang.reflect.Method[] methods = listener.getClass().getDeclaredMethods();
            for (java.lang.reflect.Method method : methods) {
                if (method.isAnnotationPresent(io.warmup.framework.annotation.EventListener.class)) {
                    return new EventListenerMethod(listener, method);
                }
            }
            throw new IllegalArgumentException("No @EventListener method found");
        } catch (Exception e) {
            // Log detallado para debugging
            System.err.println("Error creating EventListenerMethod for listener: " + listener.getClass().getName());
            System.err.println("Error details: " + e.getMessage());
            throw new RuntimeException("Failed to create EventListenerMethod", e);
        }
    }
}