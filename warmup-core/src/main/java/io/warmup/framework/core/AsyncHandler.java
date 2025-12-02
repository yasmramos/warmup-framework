package io.warmup.framework.core;

import io.warmup.framework.async.AsyncExecutor;
import io.warmup.framework.proxy.AsyncProxyFactory;
import io.warmup.framework.asm.AsmCoreUtils;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class AsyncHandler {

    private static final Logger log = Logger.getLogger(AsyncHandler.class.getName());

    private boolean asyncEnabled = true; // Mantener la configuración aquí
    private DependencyRegistry dependencyRegistry; // Inyectar DependencyRegistry

    // Constructor con lazy initialization para romper dependencia circular
    public AsyncHandler(DependencyRegistry dependencyRegistry) {
        this.dependencyRegistry = dependencyRegistry; // Puede ser null inicialmente
    }
    
    /**
     * Set the DependencyRegistry reference after initialization to break circular dependency
     */
    public void setDependencyRegistry(DependencyRegistry dependencyRegistry) {
        this.dependencyRegistry = dependencyRegistry;
    }

    public void enableAsync(boolean enabled) {
        this.asyncEnabled = enabled;
        log.log(Level.INFO, "Ejecución asíncrona {0}", enabled ? "habilitada" : "deshabilitada");
    }

    public boolean isAsyncEnabled() {
        return asyncEnabled;
    }

    public boolean isEnabled() {
        return asyncEnabled;
    }

    /**
     * Verificar si una clase tiene métodos @Async
     */
    public boolean hasAsyncMethods(Class<?> clazz) {
        for (Method method : clazz.getDeclaredMethods()) {
            if (method.isAnnotationPresent(io.warmup.framework.annotation.Async.class)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Crear proxy asíncrono para un componente si es necesario
     */
    public <T> T createAsyncProxyIfNeeded(T instance, Class<T> type) {
        if (!asyncEnabled || !hasAsyncMethods(type)) {
            return instance;
        }
        try {
            log.log(Level.INFO, "Creando proxy @Async para: {0}", type.getSimpleName());
            T proxy = (T) AsyncProxyFactory.createAsyncProxy(instance);
            if (proxy == null) {
                log.log(Level.WARNING, "AsyncProxyFactory devolvió null para {0}, usando instancia original", type.getSimpleName());
                return instance;
            }
            return proxy;
        } catch (Exception e) {
            log.log(Level.WARNING, "Error creando proxy @Async para {0}: {1}",
                    new Object[]{type.getSimpleName(), e.getMessage()});
            return instance;
        }
    }

    /**
     * Aplicar @Async a una instancia si es necesario. Este método reemplaza la
     * lógica de getWithAsyncSupport.
     */
    public <T> T applyAsyncIfNeeded(T instance, Class<T> type) throws Exception {
        return createAsyncProxyIfNeeded(instance, type);
    }

    /**
     * Validar estructura de bean @Async sin crear proxy
     */
    public void validateAsyncBeanStructure(Class<?> beanType) throws Exception {
        // Verificar que los métodos @Async tienen tipos de retorno compatibles
        for (Method method : beanType.getDeclaredMethods()) {
            if (method.isAnnotationPresent(io.warmup.framework.annotation.Async.class)) {
                Class<?> returnType = method.getReturnType();
                if (returnType != void.class && returnType != java.util.concurrent.CompletableFuture.class) {
                    System.out.println("⚠️  Método @Async " + method.getName()
                            + " debería retornar void o CompletableFuture");
                }
            }
        }
    }

    /**
     * Apagar AsyncExecutor gracefulmente
     */
    public void shutdown() {
        try {
            AsyncExecutor asyncExecutor = AsyncExecutor.getInstance();
            asyncExecutor.shutdown();
            log.info("AsyncExecutor apagado");
        } catch (Exception e) {
            log.log(Level.WARNING, "Error apagando AsyncExecutor: {0}", e.getMessage());
        }
    }

    /**
     * Obtener estadísticas de ejecución asíncrona
     */
    public Map<String, Object> getStats() {
        try {
            AsyncExecutor asyncExecutor = AsyncExecutor.getInstance();
            // Usar reflexión para acceder a estadísticas internas si es necesario
            // Por ahora, retornar información básica
            Map<String, Object> stats = new HashMap<>();
            stats.put("asyncEnabled", asyncEnabled);
            stats.put("executorCount", 0); // Se puede mejorar con reflexión
            return stats;
        } catch (Exception e) {
            log.log(Level.WARNING, "Error obteniendo estadísticas async: {0}", e.getMessage());
            return Collections.emptyMap();
        }
    }

    /**
     * Imprimir información de configuración @Async
     */
    public void printConfig() {
        System.out.println("Configuración @Async:");
        System.out.println("   • Habilitado: " + asyncEnabled);
        // Usar el método que ahora puede acceder a DependencyRegistry
        System.out.println("   • Componentes con métodos @Async: " + countComponents());
    }

    /**
     * Contar componentes registrados que tienen métodos @Async. Ahora puede
     * acceder a DependencyRegistry directamente.
     */
    public int countComponents() {
        int count = 0;
        // Manejar el caso donde dependencyRegistry sea null (durante inicialización)
        if (dependencyRegistry == null) {
            return 0;
        }
        
        // Iterar sobre las dependencias registradas
        for (Dependency dependency : dependencyRegistry.getDependencies().values()) {
            if (hasAsyncMethods(dependency.getType())) {
                count++;
            }
        }
        for (Dependency dependency : dependencyRegistry.getNamedDependencies().values()) {
            if (hasAsyncMethods(dependency.getType())) {
                count++;
            }
        }
        return count;
    }
    
    public void initialize() {
        log.log(Level.INFO, "AsyncHandler initialized");
    }
}
