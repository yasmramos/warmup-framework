package io.warmup.framework.core;

import io.warmup.framework.annotation.PreShutdown;
import io.warmup.framework.asm.AsmCoreUtils;
// import io.warmup.framework.jit.asm.SimpleASMUtils; // MIGRATED to AsmCoreUtils
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ShutdownManager {
    private static final Logger log = Logger.getLogger(ShutdownManager.class.getName());

    // --- Campos movidos desde WarmupContainer ---
    private final Map<Object, List<Method>> preDestroyMethods = new ConcurrentHashMap<>();
    private boolean autoShutdownEnabled = true;
    private boolean gracefulShutdown = true;
    private long shutdownTimeoutMs = 30000; // 30 segundos
    private final long startTime = System.currentTimeMillis();
    private volatile ContainerState state = ContainerState.INITIALIZING;
    private boolean shutdownHookRegistered = false;
    private Thread shutdownHook;
    private WarmupContainer container; // Puede ser null inicialmente para romper dependencia circular
    private DependencyRegistry dependencyRegistry; // Puede ser null inicialmente para romper dependencia circular

    // Campos estáticos para el hook global
    private static final Set<ShutdownManager> ALL_MANAGERS = ConcurrentHashMap.newKeySet();
    private static final AtomicBoolean HOOK_REGISTERED = new AtomicBoolean(false);

    /**
     * Constructor con lazy initialization para romper dependencia circular
     */
    public ShutdownManager(WarmupContainer container, DependencyRegistry dependencyRegistry) {
        this.container = container; // Puede ser null inicialmente
        this.dependencyRegistry = dependencyRegistry; // Puede ser null inicialmente
    }
    
    /**
     * Set the WarmupContainer reference after initialization to break circular dependency
     */
    public void setContainer(WarmupContainer container) {
        this.container = container;
    }
    
    /**
     * Set the DependencyRegistry reference after initialization to break circular dependency
     */
    public void setDependencyRegistry(DependencyRegistry dependencyRegistry) {
        this.dependencyRegistry = dependencyRegistry;
    }

    public void registerPreDestroy(Object instance, List<Method> methods) {
        if (!methods.isEmpty()) {
            preDestroyMethods.put(instance, methods);
            System.out.println(" Registrados " + methods.size() + " métodos @PreDestroy para: " + instance.getClass().getSimpleName());
        }
    }

    public void setAutoShutdownEnabled(boolean enabled) {
        this.autoShutdownEnabled = enabled;
        if (enabled && !shutdownHookRegistered) {
            registerShutdownHook();
        } else if (!enabled && shutdownHookRegistered) {
            disableAutoShutdown();
        }
    }

    public void setGracefulShutdown(boolean graceful) {
        this.gracefulShutdown = graceful;
        log.log(Level.INFO, "Modo shutdown: {0}", graceful ? "GRACEFUL" : "FORZADO");
    }

    public void setShutdownTimeout(long timeoutMs) {
        this.shutdownTimeoutMs = timeoutMs;
        log.log(Level.INFO, "Timeout shutdown: {0}ms", timeoutMs);
    }

    public void printShutdownConfig() {
        System.out.println("Configuración de Shutdown:");
        System.out.println("   • Modo: " + (gracefulShutdown ? "Graceful" : "Forzado"));
        System.out.println("   • Timeout: " + shutdownTimeoutMs + "ms");
        System.out.println("   • Auto-shutdown: " + (shutdownHookRegistered ? "Habilitado" : "Deshabilitado"));
    }

    public ContainerState getState() {
        return state;
    }

    public boolean isRunning() {
        return state == ContainerState.RUNNING;
    }

    public boolean isShutdown() {
        return state == ContainerState.SHUTDOWN;
    }

    private void registerShutdownHook() {
        if (!autoShutdownEnabled) {
            return;
        }
        // ✅ Solo el primer manager registra el hook global
        if (HOOK_REGISTERED.compareAndSet(false, true)) {
            Thread globalHook = new Thread(() -> {
                System.out.println("Shutdown hook global - cerrando " + ALL_MANAGERS.size() + " managers...");
                for (ShutdownManager sm : ALL_MANAGERS) {
                    try {
                        sm.shutdown();
                    } catch (InterruptedException e) {
                        System.err.println("Error cerrando manager: " + e.getMessage());
                    }
                }
            });
            globalHook.setName("Warmup-Global-Shutdown-Hook");
            Runtime.getRuntime().addShutdownHook(globalHook);
            System.out.println("Shutdown hook global registrado automáticamente");
        }
        //Este manager entra en la lista global
        ALL_MANAGERS.add(this);
        this.shutdownHookRegistered = true; // Marcar este manager como registrado
    }

    public void disableAutoShutdown() {
        if (shutdownHookRegistered && shutdownHook != null) {
            try {
                Runtime.getRuntime().removeShutdownHook(shutdownHook);
                shutdownHookRegistered = false;
                System.out.println("Shutdown hook automático deshabilitado");
            } catch (IllegalStateException e) {
                System.out.println("️ No se puede remover shutdown hook - JVM ya se está cerrando");
            }
        }
    }

    public void shutdown() throws InterruptedException {
        if (state == ContainerState.SHUTTING_DOWN || state == ContainerState.SHUTDOWN) {
            log.info("Manager ya está en proceso de cierre");
            return;
        }
        state = ContainerState.SHUTTING_DOWN;
        log.log(Level.INFO, "Iniciando shutdown del manager (Modo: {0}, Timeout: {1}ms)",
                new Object[]{gracefulShutdown ? "GRACEFUL" : "FORZADO", shutdownTimeoutMs});

        /* ========== SILENCIO SI NO HAY NADA QUE HACER ========== */
        int components = getAllCreatedInstances().size(); // Usar método auxiliar que accede a DependencyRegistry
        int preDestroys = preDestroyMethods.size();
        if (components == 0 && preDestroys == 0) {
            // Nada que cerrar → salida silenciosa
            state = ContainerState.SHUTDOWN;
            log.info("Shutdown completado (sin componentes ni @PreDestroy)");
            return;
        }
        /* ======================================================= */

        try {
            if (gracefulShutdown) {
                shutdownGraceful(components, preDestroys);
            } else {
                shutdownForced();
            }
            state = ContainerState.SHUTDOWN;
            log.info("Shutdown completado exitosamente");
        } catch (java.util.concurrent.TimeoutException e) { // TimeoutException es subclase de Exception
            log.log(Level.SEVERE, "Timeout durante shutdown graceful, forzando cierre", e);
            shutdownForced();
            state = ContainerState.SHUTDOWN;
            throw new RuntimeException("Shutdown timeout excedido", e);
        } catch (InterruptedException e) {
            log.log(Level.SEVERE, "Error durante shutdown", e);
            shutdownForced();
            state = ContainerState.SHUTDOWN;
            throw e;
        }
    }

    /**
     * Notificar a los componentes sobre el inicio del shutdown
     */
    private void notifyShutdownStart() {
        int notifiedCount = 0;
        // Verificar que dependencyRegistry esté disponible
        if (dependencyRegistry == null) {
            System.out.println("DependencyRegistry no disponible, omitiendo notificaciones de shutdown");
            return;
        }
        
        // Notificar a todas las dependencias que tienen instancias creadas
        for (Dependency dependency : dependencyRegistry.getDependencies().values()) { // Usar DependencyRegistry
            try {
                Object instance = dependency.getCachedInstance();
                if (instance != null) {
                    notifyInstanceShutdownStart(instance);
                    notifiedCount++;
                }
            } catch (Exception e) {
                System.err.println("️ Error notificando shutdown a componente: " + e.getMessage());
            }
        }
        // También notificar dependencias con nombre que tienen instancias
        for (Dependency dependency : dependencyRegistry.getNamedDependencies().values()) { // Usar DependencyRegistry
            try {
                Object instance = dependency.getCachedInstance();
                if (instance != null) {
                    notifyInstanceShutdownStart(instance);
                    notifiedCount++;
                }
            } catch (Exception e) {
                System.err.println("Error notificando shutdown a componente nombrado: " + e.getMessage());
            }
        }
        System.out.println("" + notifiedCount + " componentes notificados sobre shutdown");
    }

    /**
     * Notificar a una instancia específica sobre el shutdown
     */
    private void notifyInstanceShutdownStart(Object instance) {
        try {
            // Buscar métodos con @PreShutdown (si existen)
            boolean notified = false;
            for (Method method : instance.getClass().getDeclaredMethods()) {
                if (method.isAnnotationPresent(PreShutdown.class)) {
                    // ✅ REFACTORIZADO: Usar ASM en lugar de reflexión
                    PreShutdown annotation = method.getAnnotation(PreShutdown.class);
                    // Verificar que el método sea válido (sin parámetros, void)
                    if (method.getParameterCount() == 0 && method.getReturnType() == void.class) {
                        try {
                            AsmCoreUtils.invokeMethod(instance, method.getName());
                            System.out.println("    " + instance.getClass().getSimpleName()
                                    + " notificado sobre shutdown (" + annotation.value() + ")");
                            notified = true;
                        } catch (Exception e) {
                            log.log(Level.WARNING, "Error ejecutando @PreShutdown " + method.getName() + 
                                    " en " + instance.getClass().getSimpleName(), e);
                            // Continuar con otros métodos aunque uno falle
                        }
                    }
                }
            }
            if (!notified) {
                // Intentar con método por convención si no hay anotación
                // ✅ REFACTORIZADO: Usar ASM con invocación silenciosa (método no encontrado es normal)
                Object result;
                try {
                    result = AsmCoreUtils.invokeMethod(instance, "onShutdown");
                } catch (Exception e) {
                    // Silencioso - método no encontrado es normal
                    result = null;
                }
                if (result != null) {
                    System.out.println("    " + instance.getClass().getSimpleName()
                            + " notificado sobre shutdown (método onShutdown)");
                }
                // Si result es null, el método onShutdown no existe - esto es normal y no requiere acción
            }
        } catch (Exception e) {
            System.err.println(" Error notificando shutdown a "
                    + instance.getClass().getSimpleName() + ": " + e.getMessage());
        }
    }

    /**
     * Ejecutar @PreDestroy methods con control de timeout
     */
    private void executePreDestroyMethodsWithTimeout(long startTime)
            throws java.util.concurrent.TimeoutException, InterruptedException {
        System.out.println(" Ejecutando métodos @PreDestroy con timeout...");
        List<Thread> preDestroyThreads = new ArrayList<>();
        CountDownLatch completionLatch = new CountDownLatch(preDestroyMethods.size());
        // Ejecutar cada @PreDestroy en un thread separado con timeout
        for (Map.Entry<Object, List<Method>> entry : preDestroyMethods.entrySet()) {
            Object instance = entry.getKey();
            List<Method> methods = entry.getValue();
            for (Method method : methods) {
                Thread thread = new Thread(() -> {
                    try {
                        executePreDestroyMethod(instance, method);
                        completionLatch.countDown();
                    } catch (Exception e) {
                        System.err.println(" Error en @PreDestroy: " + e.getMessage());
                        completionLatch.countDown(); // Contar incluso con error
                    }
                });
                thread.setName("PreDestroy-" + instance.getClass().getSimpleName() + "-" + method.getName());
                preDestroyThreads.add(thread);
                thread.start();
            }
        }

        // Esperar con timeout
        boolean completed = completionLatch.await(
                calculateRemainingTime(startTime),
                TimeUnit.MILLISECONDS
        );
        if (!completed) {
            // Timeout ocurrido, interrumpir threads
            System.err.println("Timeout en métodos @PreDestroy, interrumpiendo...");
            for (Thread thread : preDestroyThreads) {
                if (thread.isAlive()) {
                    thread.interrupt();
                }
            }
            throw new java.util.concurrent.TimeoutException("Timeout ejecutando métodos @PreDestroy");
        }
        System.out.println("Todos los métodos @PreDestroy completados");
    }

    /**
     * Calcular tiempo restante basado en el timeout configurado
     */
    private long calculateRemainingTime(long startTime) {
        long elapsed = System.currentTimeMillis() - startTime;
        return Math.max(0, shutdownTimeoutMs - elapsed);
    }

    /**
     * Ejecutar un método @PreDestroy individual
     */
    private void executePreDestroyMethod(Object instance, Method method) {
        try {
            System.out.println("   Ejecutando @PreDestroy: "
                    + instance.getClass().getSimpleName() + "." + method.getName());
            // ✅ USAR REFLEXIÓN DIRECTA PARA @PreDestroy (más compatible)
            method.setAccessible(true);
            // ✅ FASE 6: Invocación progresiva del método - ASM → MethodHandle → Reflection
            try {
                AsmCoreUtils.invokeMethodObjectProgressive(method, instance);
            } catch (Throwable e) {
                // Fallback to reflection
                method.invoke(instance);
            }
        } catch (Exception e) {
            System.err.println("   Error en @PreDestroy " + method.getName() + ": " + e.getMessage());
            throw new RuntimeException("Error en @PreDestroy", e);
        }
    }

    /**
     * Shutdown graceful con timeout
     */
    private synchronized void shutdownGraceful(int componentCount, int preDestroyCount)
            throws java.util.concurrent.TimeoutException, InterruptedException {
        long shutdownStartTime = System.currentTimeMillis();
        if (componentCount > 0) {
            System.out.println(" Notificando inicio de shutdown a " + componentCount + " componentes...");
            notifyShutdownStart();
        }
        if (preDestroyCount > 0) {
            System.out.println(" Ejecutando " + preDestroyCount + " métodos @PreDestroy con timeout...");
            executePreDestroyMethodsWithTimeout(shutdownStartTime);
        }
        cleanupResources();
        long duration = System.currentTimeMillis() - shutdownStartTime;
        log.log(Level.INFO, "Shutdown graceful completado en {0}ms", duration);
    }

    /**
     * Shutdown seguro que captura todas las excepciones
     */
    public void safeShutdown() {
        try {
            if (state != ContainerState.SHUTDOWN) {
                shutdown();
            }
        } catch (InterruptedException e) {
            System.err.println("Error durante shutdown, forzando limpieza: " + e.getMessage());
            shutdownForced();
        }
    }

    /**
     * Shutdown forzado que limpia recursos sin ejecutar @PreDestroy
     */
    private void shutdownForced() {
        System.out.println("Ejecutando shutdown forzado...");
        cleanupResources();
        System.out.println("Shutdown forzado completado");
    }

    /**
     * Limpieza completa de recursos incluyendo AsyncHandler
     */
    private void cleanupResources() {
        // Verificar que container esté disponible
        if (container == null) {
            System.out.println("Container no disponible, omitiendo limpieza de recursos");
            return;
        }
        
        ((AsyncHandler) container.getAsyncHandler()).shutdown();
        ((DependencyRegistry) container.getDependencyRegistry()).clear();
        ((EventManager) container.getEventManager()).clearListeners();
        container.getHealthCheckManager().shutdown();
        
        // ✅ WEB SCOPE CLEANUP
        ((WebScopeContext) container.getWebScopeContext()).shutdown();
        
        preDestroyMethods.clear(); // Limpiar mapa interno
        System.gc();
    }

    /**
     * Configurar shutdown con todas las opciones
     */
    public void configureShutdown(boolean graceful, long timeoutMs, boolean autoShutdown) {
        setGracefulShutdown(graceful);
        setShutdownTimeout(timeoutMs);
        setAutoShutdownEnabled(autoShutdown);
    }

    /**
     * Shutdown con configuración temporal
     */
    public void shutdown(boolean graceful, long timeoutMs) {
        boolean originalGraceful = this.gracefulShutdown;
        long originalTimeout = this.shutdownTimeoutMs;
        try {
            this.gracefulShutdown = graceful;
            this.shutdownTimeoutMs = timeoutMs;
            shutdown();
        } catch (InterruptedException ex) {
            log.severe(ex.getMessage());
        } finally {
            // Restaurar configuración original
            this.gracefulShutdown = originalGraceful;
            this.shutdownTimeoutMs = originalTimeout;
        }
    }

    /**
     * Verificar si el shutdown será graceful
     */
    public boolean isGracefulShutdown() {
        return gracefulShutdown;
    }

    /**
     * Obtener timeout configurado
     */
    public long getShutdownTimeout() {
        return shutdownTimeoutMs;
    }

    /**
     * Obtener el tiempo de inicio del container (Para uso interno en health checks y métricas)
     */
    public long getStartTime() {
        return startTime;
    }

    /**
     * Obtener el tiempo de actividad del container en milisegundos
     */
    public long getUptime() {
        return System.currentTimeMillis() - startTime;
    }

    /**
     * Obtener el tiempo de actividad formateado
     */
    public String getFormattedUptime() {
        long uptime = getUptime();
        long seconds = uptime / 1000;
        long minutes = seconds / 60;
        long hours = minutes / 60;
        return String.format("%02d:%02d:%02d", hours, minutes % 60, seconds % 60);
    }

    // Método auxiliar para obtener instancias creadas (necesario para shutdown)
    private List<Object> getAllCreatedInstances() {
        List<Object> instances = new ArrayList<>();
        
        // Verificar que dependencyRegistry esté disponible
        if (dependencyRegistry == null) {
            System.out.println("DependencyRegistry no disponible, retornando lista vacía");
            return instances;
        }
        
        for (Dependency dep : dependencyRegistry.getDependencies().values()) {
            Object instance = dep.getCachedInstance();
            if (instance != null) {
                instances.add(instance);
            }
        }
        for (Dependency dep : dependencyRegistry.getNamedDependencies().values()) {
            Object instance = dep.getCachedInstance();
            if (instance != null) {
                instances.add(instance);
            }
        }
        return instances;
    }
    
    public void initialize() {
        log.log(Level.INFO, "ShutdownManager initialized");
    }
}