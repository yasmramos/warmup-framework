package io.warmup.framework.asm;

import io.warmup.framework.core.EventIndexEngine;
import io.warmup.framework.event.EventListenerMethod;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import java.lang.reflect.Method;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Test específico para verificar la corrección del MethodHandle en EventIndexEngine
 */
public class EventIndexEngineDebugTest {
    
    private EventIndexEngine engine;
    
    @BeforeEach
    void setUp() {
        engine = new EventIndexEngine();
    }
    
    @Test
    void testSimpleEventDispatch() throws Throwable {
        System.out.println("=== TEST DE CORRECCIÓN DE METHODHANDLE ===");
        
        // Crear event listener simple
        TestEventListener listener = new TestEventListener();
        
        // Crear EventListenerMethod usando reflexión
        Method handleMethod = TestEventListener.class.getMethod("handle", TestEvent.class);
        EventListenerMethod eventListenerMethod = new EventListenerMethod(listener, handleMethod);
        
        // Registrar listener
        engine.registerListener(TestEvent.class, eventListenerMethod);
        
        System.out.println("Listener registrado correctamente");
        
        // Crear y despachar evento
        TestEvent event = new TestEvent();
        event.setData("test");
        
        System.out.println("Despachando evento...");
        engine.dispatchEvent(event);
        
        System.out.println("✅ Evento despachado sin errores de casting");
        assertEquals(1, listener.getCallCount(), "Listener debe haber sido llamado exactamente una vez");
    }
    
    // Clases de test simples
    static class TestEvent {
        private String data;
        
        public String getData() { return data; }
        public void setData(String data) { this.data = data; }
    }
    
    static class TestEventListener {
        private int callCount = 0;
        
        public void handle(TestEvent event) {
            callCount++;
            System.out.println("✅ Listener.handle() llamado con: " + event.getData());
        }
        
        public int getCallCount() { return callCount; }
    }
}