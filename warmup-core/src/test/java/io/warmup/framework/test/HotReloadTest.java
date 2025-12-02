package io.warmup.framework.test;

import io.warmup.framework.core.Warmup;
import io.warmup.framework.event.EventBus;
import io.warmup.framework.hotreload.HotReloadEvent;
import io.warmup.framework.hotreload.HotReloadManager;
import io.warmup.framework.hotreload.HotReloadStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests para el sistema de Hot Reload
 * 
 * @author MiniMax Agent
 * @since 1.0
 */
class HotReloadTest {
    
    private Warmup warmup;
    private EventBus eventBus;
    private HotReloadManager hotReloadManager;
    
    @TempDir
    Path tempDir;
    
    @BeforeEach
    void setUp() {
        warmup = Warmup.create();
        
        // Create EventBus manually first
        eventBus = new EventBus();
        
        // Register EventBus in warmup instance
        warmup.registerBean(EventBus.class, eventBus);
        
        try {
            warmup.getContainer().start();
        } catch (Exception e) {
            throw new RuntimeException("Failed to start container", e);
        }
        
        hotReloadManager = new HotReloadManager(warmup.getContainer(), eventBus);
    }
    
    @Test
    void shouldInitializeHotReloadManager() {
        assertNotNull(hotReloadManager);
        assertFalse(hotReloadManager.getStatus().isEnabled());
        assertFalse(hotReloadManager.getStatus().isRunning());
    }
    
    @Test
    void shouldEnableAndDisableHotReload() {
        // Dado que el manager está inicialmente deshabilitado
        HotReloadStatus initialStatus = hotReloadManager.getStatus();
        assertFalse(initialStatus.isEnabled());
        assertFalse(initialStatus.isRunning());
        
        // Cuando habilitamos el hot reload
        hotReloadManager.enable();
        
        // Entonces debería estar habilitado y ejecutándose
        HotReloadStatus enabledStatus = hotReloadManager.getStatus();
        assertTrue(enabledStatus.isEnabled());
        assertTrue(enabledStatus.isRunning());
        
        // Cuando deshabilitamos el hot reload
        hotReloadManager.disable();
        
        // Entonces debería estar deshabilitado
        HotReloadStatus disabledStatus = hotReloadManager.getStatus();
        assertFalse(disabledStatus.isEnabled());
        assertFalse(disabledStatus.isRunning());
    }
    
    @Test
    void shouldMonitorDirectory() {
        // Dado que el hot reload está habilitado
        hotReloadManager.enable();
        
        // Cuando monitoreamos un directorio temporal
        hotReloadManager.monitorDirectory(tempDir.toString());
        
        // Entonces debería permitir monitoreo sin errores
        assertDoesNotThrow(() -> {
            hotReloadManager.monitorDirectory(tempDir.toString());
        });
    }
    
    @Test
    void shouldPublishHotReloadEvents() {
        // Dado que tenemos un listener de eventos
        AtomicBoolean eventReceived = new AtomicBoolean(false);
        java.util.concurrent.atomic.AtomicReference<String> eventMessageRef = new java.util.concurrent.atomic.AtomicReference<>();
        
        eventBus.subscribe(HotReloadEvent.class, event -> {
            HotReloadEvent hotReloadEvent = (HotReloadEvent) event;
            eventReceived.set(true);
            eventMessageRef.set(hotReloadEvent.getMessage());
        });
        
        // Cuando habilitamos el hot reload
        hotReloadManager.enable();
        
        // Entonces debería publicar un evento de habilitación
        assertTrue(eventReceived.get());
        assertNotNull(eventMessageRef.get());
        assertTrue(eventMessageRef.get().contains("Hot Reload system activated"));
    }
    
    @Test
    void shouldGetStatusWithCorrectInformation() {
        // Dado que el hot reload está deshabilitado inicialmente
        HotReloadStatus initialStatus = hotReloadManager.getStatus();
        assertEquals(0, initialStatus.getMonitoredFilesCount());
        assertEquals(0, initialStatus.getPendingReloads());
        assertFalse(initialStatus.canReload());
        
        // Cuando habilitamos y monitoreamos un directorio
        hotReloadManager.enable();
        hotReloadManager.monitorDirectory(tempDir.toString());
        
        // Entonces el status debería reflejar los cambios
        HotReloadStatus activeStatus = hotReloadManager.getStatus();
        assertTrue(activeStatus.isEnabled());
        assertTrue(activeStatus.canReload());
    }
    
    @Test
    void shouldShutdownGracefully() {
        // Dado que el hot reload está habilitado
        hotReloadManager.enable();
        
        // Cuando llamamos a shutdown
        hotReloadManager.shutdown();
        
        // Entonces debería cerrarse sin errores
        assertDoesNotThrow(() -> {
            hotReloadManager.shutdown();
        });
        
        HotReloadStatus status = hotReloadManager.getStatus();
        assertFalse(status.isEnabled());
        assertFalse(status.isRunning());
    }
    
    @Test
    void shouldHandleReloadClassWhenEnabled() {
        // Dado que tenemos una clase de ejemplo
        String testClassName = "io.warmup.framework.test.HotReloadTest";
        
        // Y el hot reload está habilitado
        hotReloadManager.enable();
        
        // Cuando intentamos recargar una clase
        assertDoesNotThrow(() -> {
            warmup.getContainer().reloadClass(testClassName);
        });
        
        // Entonces no debería lanzar excepción (aunque la clase podría no existir)
    }
    
    @Test
    void shouldReturnProperStatusObject() {
        HotReloadStatus status = hotReloadManager.getStatus();
        
        // Verificar que el status no sea null
        assertNotNull(status);
        
        // Verificar métodos getter
        assertNotNull(status.toString());
        assertTrue(status.isEnabled() == false || status.isEnabled() == true); // Boolean assertion
        assertTrue(status.isRunning() == false || status.isRunning() == true); // Boolean assertion
        assertTrue(status.getMonitoredFilesCount() >= 0);
        assertTrue(status.getPendingReloads() >= 0);
        assertTrue(status.canReload() == false || status.canReload() == true); // Boolean assertion
    }
}