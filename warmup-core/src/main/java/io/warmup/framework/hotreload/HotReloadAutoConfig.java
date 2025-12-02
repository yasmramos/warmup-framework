package io.warmup.framework.hotreload;

import io.warmup.framework.annotation.Component;
import io.warmup.framework.annotation.Profile;
import io.warmup.framework.core.WarmupContainer;
import io.warmup.framework.event.EventBus;

/**
 * Configuración automática para Hot Reload
 * Se activa automáticamente en modo desarrollo
 * 
 * @author MiniMax Agent
 * @since 1.0
 */
@Component
@Profile("development") // Solo activo en modo desarrollo
public class HotReloadAutoConfig {
    
    /**
     * Configuración automática del hot reload
     */
    public HotReloadManager configureHotReload(WarmupContainer container, EventBus eventBus) {
        HotReloadManager hotReloadManager = new HotReloadManager(container, eventBus);
        
        // Habilitar hot reload
        hotReloadManager.enable();
        
        // Monitorear directorios comunes de desarrollo
        hotReloadManager.monitorDirectory("src/main/java");
        hotReloadManager.monitorDirectory("src/test/java");
        hotReloadManager.monitorDirectory("target/classes");
        
        // Monitorear directorio de trabajo actual
        hotReloadManager.monitorDirectory(System.getProperty("user.dir"));
        
        return hotReloadManager;
    }
}