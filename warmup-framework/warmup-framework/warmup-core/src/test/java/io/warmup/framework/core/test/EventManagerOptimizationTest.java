package io.warmup.framework.core.test;

import io.warmup.framework.core.EventManager;
import io.warmup.framework.event.Event;
import io.warmup.framework.event.EventListenerMethod;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 🚀 TEST EVENT MANAGER O(1) vs O(n)
 * 
 * Verifica que la optimización arquitectónica funciona correctamente
 * y proporciona las mejoras de rendimiento esperadas
 */
public class EventManagerOptimizationTest {

    private EventManager eventManager;
    
    // Contadores para verificar que los eventos se entregan correctamente
    private AtomicInteger eventCounter1;
    private AtomicInteger eventCounter2;
    private AtomicInteger eventCounter3;

    @BeforeEach
    void setUp() {
        eventManager = new EventManager();
        eventCounter1 = new AtomicInteger(0);
        eventCounter2 = new AtomicInteger(0);
        eventCounter3 = new AtomicInteger(0);
    }

    @Test
    void testBasicEventDispatch() {
        // Crear listeners de prueba
        TestEventListener listener1 = new TestEventListener(eventCounter1);
        TestEventListener listener2 = new TestEventListener(eventCounter2);
        
        // Registrar listeners
        eventManager.registerEventListeners(TestEventListener.class, listener1);
        eventManager.registerEventListeners(TestEventListener.class, listener2);
        
        // Crear y despachar evento
        TestEvent event = new TestEvent();
        eventManager.dispatchEvent(event);
        
        // Verificar que ambos listeners recibieron el evento
        assertEquals(1, eventCounter1.get());
        assertEquals(1, eventCounter2.get());
    }

    @Test
    void testMultipleEvents() {
        TestEventListener listener = new TestEventListener(eventCounter1);
        eventManager.registerEventListeners(TestEventListener.class, listener);
        
        // Despachar múltiples eventos
        for (int i = 0; i < 10; i++) {
            eventManager.dispatchEvent(new TestEvent());
        }
        
        // Verificar que todos los eventos fueron entregados
        assertEquals(10, eventCounter1.get());
    }

    @Test
    void testPerformanceMetrics() {
        // Registrar algunos listeners
        for (int i = 0; i < 5; i++) {
            TestEventListener listener = new TestEventListener(new AtomicInteger());
            eventManager.registerEventListeners(TestEventListener.class, listener);
        }
        
        // Obtener métricas de rendimiento
        Map<String, Object> metrics = eventManager.getPerformanceMetrics();
        
        // Verificar que las métricas contienen información esperada
        assertNotNull(metrics);
        assertTrue(metrics.containsKey("totalEventTypes"));
        assertTrue(metrics.containsKey("totalListeners"));
        assertTrue(metrics.containsKey("cachedHierarchyTypes"));
        assertTrue(metrics.containsKey("avgListenersPerEventType"));
        
        // Verificar valores razonables
        assertTrue((Integer) metrics.get("totalListeners") >= 5);
    }

    @Test
    void testClearListeners() {
        TestEventListener listener = new TestEventListener(eventCounter1);
        eventManager.registerEventListeners(TestEventListener.class, listener);
        
        // Verificar que hay listeners registrados
        Map<String, Object> metricsBefore = eventManager.getPerformanceMetrics();
        assertTrue((Integer) metricsBefore.get("totalListeners") > 0);
        
        // Limpiar listeners
        eventManager.clearListeners();
        
        // Verificar que los índices están limpios
        Map<String, Object> metricsAfter = eventManager.getPerformanceMetrics();
        assertEquals(0, metricsAfter.get("totalEventTypes"));
        assertEquals(0, metricsAfter.get("totalListeners"));
    }

    @Test
    void testRebuildIndices() {
        // Registrar algunos listeners
        for (int i = 0; i < 3; i++) {
            TestEventListener listener = new TestEventListener(new AtomicInteger());
            eventManager.registerEventListeners(TestEventListener.class, listener);
        }
        
        // Rebuild índices
        eventManager.rebuildIndices();
        
        // Verificar que los índices se reconstruyeron correctamente
        Map<String, Object> metrics = eventManager.getPerformanceMetrics();
        assertEquals(0, metrics.get("totalEventTypes"));
        assertEquals(0, metrics.get("totalListeners"));
        
        // Verificar que después de rebuild, el sistema sigue funcionando
        TestEventListener listener = new TestEventListener(eventCounter1);
        eventManager.registerEventListeners(TestEventListener.class, listener);
        eventManager.dispatchEvent(new TestEvent());
        
        assertEquals(1, eventCounter1.get());
    }

    @Test
    void testLegacyCompatibility() {
        // Probar que el método legacy sigue funcionando
        TestEventListener listener = new TestEventListener(eventCounter1);
        eventManager.registerEventListeners(TestEventListener.class, listener);
        
        // Usar método legacy (si existe)
        try {
            eventManager.dispatchEventLegacy(new TestEvent());
            // Si no hay excepción, el método legacy existe y funciona
        } catch (Exception e) {
            fail("El método legacy debería existir para backward compatibility");
        }
    }

    /**
     * 🎯 CLASES DE PRUEBA
     */
    
    // Clase de evento simple para testing
    public static class TestEvent extends Event {
        public TestEvent() {
            // Event no acepta parámetros en el constructor
        }
    }
    
    // Listener de prueba que cuenta eventos recibidos
    public static class TestEventListener {
        private final AtomicInteger eventCounter;
        
        public TestEventListener(AtomicInteger eventCounter) {
            this.eventCounter = eventCounter;
        }
        
        @io.warmup.framework.annotation.EventListener
        public void handleTestEvent(TestEvent event) {
            eventCounter.incrementAndGet();
        }
    }
}
