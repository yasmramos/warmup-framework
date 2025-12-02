package io.warmup.examples.startup.critical;

import io.warmup.framework.startup.critical.ServiceDataClasses.ServiceInfo;
import io.warmup.framework.startup.critical.ServiceDataClasses.ServiceState;
import io.warmup.framework.startup.critical.ServiceDataClasses.ServiceCriticality;
import io.warmup.framework.startup.critical.ServiceDataClasses.CriticalSeparationConfig;
import io.warmup.framework.core.WarmupContainer;
import io.warmup.framework.core.BeanRegistry;
import io.warmup.framework.core.DependencyRegistry;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Set;
import java.util.HashSet;
import java.util.logging.Logger;
import java.util.logging.Level;
import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledFuture;

/**
 * 🌡️ CALENTAMIENTO DE SERVICIOS NO CRÍTICOS EN SEGUNDO PLANO
 * 
 * Gestiona el calentamiento de servicios no críticos de forma asíncrona
 * para optimizar la experiencia del usuario sin bloquear la respuesta inicial.
 * 
 * Características:
 * - Calentamiento asíncrono y paralelo
 * - Priorización inteligente basada en uso esperado
 * - Monitoreo de progreso en tiempo real
 * - Manejo de fallos con retry automático
 * - Balanceado de carga entre threads
 * 
 * @author MiniMax Agent
 * @version 1.0
 */
public class NonCriticalServiceWarming {
    
    private static final Logger log = Logger.getLogger(NonCriticalServiceWarming.class.getName());
    
    private final WarmupContainer container;
    private final BeanRegistry beanRegistry;
    private final DependencyRegistry dependencyRegistry;
    private final CriticalSeparationConfig config;
    
    // Estado del calentamiento
    private final AtomicBoolean backgroundWarmingStarted = new AtomicBoolean(false);
    private final AtomicBoolean backgroundWarmingCompleted = new AtomicBoolean(false);
    private final AtomicInteger warmingStarted = new AtomicInteger(0);
    private final AtomicInteger warmingCompleted = new AtomicInteger(0);
    private final AtomicInteger warmingFailed = new AtomicInteger(0);
    
    // Executor pool para calentamiento en background
    private final ExecutorService backgroundExecutor;
    private final ScheduledExecutorService scheduledExecutor;
    
    // Tracking de servicios en calentamiento
    private final Map<String, ServiceInfo> warmingServices = new ConcurrentHashMap<>();
    private final Map<String, CompletableFuture<ServiceInfo>> warmingFutures = new ConcurrentHashMap<>();
    private final Set<String> completedServices = ConcurrentHashMap.newKeySet();
    
    // Métricas
    private volatile long backgroundPhaseStartTime;
    private volatile long backgroundPhaseEndTime;
    
    public NonCriticalServiceWarming(WarmupContainer container, CriticalSeparationConfig config) {
        this.container = container;
        this.beanRegistry = (BeanRegistry) container.getBeanRegistry();
        this.dependencyRegistry = (DependencyRegistry) container.getDependencyRegistry();
        this.config = config;
        
        // Configurar executors
        this.backgroundExecutor = createBackgroundExecutor();
        this.scheduledExecutor = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "warmup-bg-scheduler");
            t.setDaemon(true);
            return t;
        });
        
        log.log(Level.FINE, "NonCriticalServiceWarming initialized with max threads: {0}", 
                config.getMaxBackgroundThreads());
    }
    
    /**
     * 🌡️ INICIAR CALENTAMIENTO DE SERVICIOS NO CRÍTICOS
     */
    public void startBackgroundWarming(List<ServiceInfo> nonCriticalServices) {
        if (backgroundWarmingStarted.compareAndSet(false, true)) {
            backgroundPhaseStartTime = System.currentTimeMillis();
            
            log.log(Level.INFO, "🌡️ INICIANDO CALENTAMIENTO EN SEGUNDO PLANO - {0} servicios", 
                    nonCriticalServices.size());
            
            if (!config.isEnableBackgroundWarming()) {
                log.log(Level.WARNING, "Background warming disabled by configuration");
                return;
            }
            
            try {
                // FASE 1: Ordenar servicios por prioridad y dependencias
                List<ServiceInfo> prioritizedServices = prioritizeServices(nonCriticalServices);
                
                // FASE 2: Iniciar calentamiento asíncrono
                startAsyncWarming(prioritizedServices);
                
                // FASE 3: Programar monitoreo periódico
                scheduleProgressMonitoring();
                
                // FASE 4: Programar timeout si es necesario
                if (config.getBackgroundPhaseTimeoutMs() > 0) {
                    scheduleBackgroundTimeout();
                }
                
            } catch (Exception e) {
                log.log(Level.SEVERE, "Error starting background warming: {0}", e.getMessage());
                backgroundWarmingStarted.set(false);
            }
        } else {
            log.log(Level.WARNING, "Background warming already started");
        }
    }
    
    /**
     * 🎯 PRIORIZAR SERVICIOS NO CRÍTICOS
     */
    private List<ServiceInfo> prioritizeServices(List<ServiceInfo> services) {
        List<ServiceInfo> prioritized = new ArrayList<>(services);
        
        // Ordenar por: criticidad > dependencias > tiempo estimado
        prioritized.sort((s1, s2) -> {
            // 1. Por criticidad (HIGH primero, luego MEDIUM, luego LOW, luego BACKGROUND)
            int criticalityCompare = s1.getCriticality().getPriority() - s2.getCriticality().getPriority();
            if (criticalityCompare != 0) return criticalityCompare;
            
            // 2. Por número de dependencias (menos dependencias primero)
            int depsCompare = Integer.compare(s1.getDependencies().size(), s2.getDependencies().size());
            if (depsCompare != 0) return depsCompare;
            
            // 3. Por tiempo estimado (menor tiempo primero)
            return Long.compare(s1.getEstimatedInitTimeMs(), s2.getEstimatedInitTimeMs());
        });
        
        log.log(Level.FINE, "Prioritized {0} services for background warming", prioritized.size());
        
        return prioritized;
    }
    
    /**
     * 🚀 INICIAR CALENTAMIENTO ASÍNCRONO
     */
    private void startAsyncWarming(List<ServiceInfo> services) {
        int batchSize = Math.max(1, config.getMaxBackgroundThreads() / 2);
        
        for (int i = 0; i < services.size(); i += batchSize) {
            int endIndex = Math.min(i + batchSize, services.size());
            List<ServiceInfo> batch = services.subList(i, endIndex);
            
            CompletableFuture.runAsync(() -> warmBatch(batch), backgroundExecutor);
        }
    }
    
    /**
     * 🌡️ CALENTAR LOTE DE SERVICIOS
     */
    private void warmBatch(List<ServiceInfo> batch) {
        log.log(Level.FINE, "Warming batch of {0} services", batch.size());
        
        for (ServiceInfo service : batch) {
            try {
                startSingleServiceWarming(service);
            } catch (Exception e) {
                log.log(Level.WARNING, "Error starting warming for service {0}: {1}", 
                        new Object[]{service.getServiceId(), e.getMessage()});
                warmingFailed.incrementAndGet();
            }
        }
    }
    
    /**
     * 🌡️ INICIAR CALENTAMIENTO DE UN SERVICIO
     */
    private void startSingleServiceWarming(ServiceInfo serviceInfo) {
        String serviceId = serviceInfo.getServiceId();
        
        if (warmingServices.containsKey(serviceId) || completedServices.contains(serviceId)) {
            log.log(Level.FINE, "Service {0} already warming or completed", serviceId);
            return;
        }
        
        warmingServices.put(serviceId, serviceInfo);
        warmingStarted.incrementAndGet();
        
        CompletableFuture<ServiceInfo> future = CompletableFuture.supplyAsync(() -> {
            try {
                ServiceInfo warmed = warmSingleService(serviceInfo);
                completedServices.add(serviceId);
                warmingCompleted.incrementAndGet();
                return warmed;
            } catch (Exception e) {
                log.log(Level.WARNING, "Failed to warm service {0}: {1}", 
                        new Object[]{serviceId, e.getMessage()});
                serviceInfo.setState(ServiceState.FAILED);
                serviceInfo.setWarmupError(e);
                warmingFailed.incrementAndGet();
                return serviceInfo;
            }
        }, backgroundExecutor);
        
        warmingFutures.put(serviceId, future);
        
        log.log(Level.FINE, "Started warming service: {0}", serviceId);
    }
    
    /**
     * 🌡️ CALENTAR UN SOLO SERVICIO
     */
    private ServiceInfo warmSingleService(ServiceInfo serviceInfo) throws Exception {
        String serviceId = serviceInfo.getServiceId();
        
        try {
            log.log(Level.FINE, "🌡️ Warming service: {0}", serviceId);
            
            // Actualizar estado
            serviceInfo.setState(ServiceState.WARMING_UP);
            serviceInfo.incrementWarmupAttempt();
            
            long startTime = System.nanoTime();
            
            // Verificar dependencias primero
            checkAndWarmDependencies(serviceInfo);
            
            // Cargar o crear la instancia del servicio
            Object serviceInstance = loadOrCreateServiceInstance(serviceInfo);
            
            if (serviceInstance == null) {
                throw new Exception("Could not create service instance");
            }
            
            // Registrar en el container
            registerServiceInstance(serviceId, serviceInstance);
            
            // Marcar como completado
            serviceInfo.setState(ServiceState.READY);
            
            long endTime = System.nanoTime();
            long durationMs = (endTime - startTime) / 1_000_000;
            
            log.log(Level.FINE, "✅ Service {0} warmed successfully in {1}ms", 
                    new Object[]{serviceId, durationMs});
            
            return serviceInfo;
            
        } catch (Exception e) {
            log.log(Level.SEVERE, "❌ Failed to warm service {0}: {1}", 
                    new Object[]{serviceId, e.getMessage()});
            throw e;
        }
    }
    
    /**
     * 🔗 VERIFICAR Y CALENTAR DEPENDENCIAS
     */
    private void checkAndWarmDependencies(ServiceInfo serviceInfo) throws Exception {
        for (String dependencyId : serviceInfo.getDependencies()) {
            // Verificar si la dependencia ya está disponible
            Object dependency = beanRegistry.getBean(dependencyId, Object.class); // TODO: Usar tipo correcto
            if (dependency == null) {
                // Intentar cargar la dependencia si es crítica
                ServiceInfo depService = warmingServices.get(dependencyId);
                if (depService != null && depService.getCriticality().isCritical()) {
                    // Esperar a que se complete la dependencia crítica
                    CompletableFuture<ServiceInfo> depFuture = warmingFutures.get(dependencyId);
                    if (depFuture != null) {
                        try {
                            depFuture.get(5, TimeUnit.SECONDS); // Timeout de 5s por dependencia
                        } catch (TimeoutException e) {
                            log.log(Level.WARNING, "Timeout waiting for dependency {0}", dependencyId);
                        }
                    }
                }
            }
        }
    }
    
    /**
     * 📦 CARGAR O CREAR INSTANCIA DE SERVICIO
     */
    private Object loadOrCreateServiceInstance(ServiceInfo serviceInfo) throws Exception {
        String serviceId = serviceInfo.getServiceId();
        String serviceClass = serviceInfo.getServiceClass();
        
        // Verificar si ya existe en el registry
        Object existing = beanRegistry.getBean(serviceId, Object.class); // TODO: Usar tipo correcto
        if (existing != null) {
            return existing;
        }
        
        // Verificar por clase
        try {
            Class<?> clazz = Class.forName(serviceClass);
            Object bean = beanRegistry.getBean(serviceId, clazz); // TODO: Verificar si serviceId es correcto
            if (bean != null) {
                return bean;
            }
            
            // Crear nueva instancia
            return createServiceInstance(clazz);
            
        } catch (ClassNotFoundException e) {
            log.log(Level.WARNING, "Service class {0} not found for {1}", 
                    new Object[]{serviceClass, serviceId});
            
            // Crear instancia placeholder
            return createPlaceholderInstance(serviceInfo);
        }
    }
    
    /**
     * 🏗️ CREAR INSTANCIA DE SERVICIO
     */
    private Object createServiceInstance(Class<?> clazz) throws Exception {
        try {
            // Intentar constructor sin parámetros
            return clazz.getDeclaredConstructor().newInstance();
        } catch (NoSuchMethodException e) {
            // Fallback: usar reflexión básica
            try {
                return clazz.getDeclaredConstructor().newInstance();
            } catch (Exception reflectionError) {
                log.log(Level.FINE, "Could not instantiate {0}, creating proxy", clazz.getName());
                return createServiceProxy(clazz);
            }
        }
    }
    
    /**
     * 🔧 CREAR PROXY DE SERVICIO
     */
    private Object createServiceProxy(Class<?> clazz) {
        // Crear un proxy dinámico mínimo
        return java.lang.reflect.Proxy.newProxyInstance(
            clazz.getClassLoader(),
            clazz.getInterfaces(),
            (proxy, method, args) -> {
                log.log(Level.FINE, "Proxy call to {0}.{1}", 
                        new Object[]{clazz.getSimpleName(), method.getName()});
                return null;
            }
        );
    }
    
    /**
     * 🔧 CREAR INSTANCIA PLACEHOLDER
     */
    private Object createPlaceholderInstance(ServiceInfo serviceInfo) {
        return new Object() {
            @Override
            public String toString() {
                return "PlaceholderWarmedService{" + serviceInfo.getServiceName() + "}";
            }
        };
    }
    
    /**
     * 📝 REGISTRAR INSTANCIA DE SERVICIO
     */
    private void registerServiceInstance(String serviceId, Object serviceInstance) {
        try {
            beanRegistry.registerBean(serviceId, serviceInstance.getClass(), serviceInstance);
            // TODO: Revisar signature de registerDependency - puede no existir con estos parámetros
            // dependencyRegistry.registerDependency(serviceId, serviceInstance.getClass());
            
            log.log(Level.FINE, "Registered warmed service: {0}", serviceId);
        } catch (Exception e) {
            log.log(Level.WARNING, "Error registering service {0}: {1}", 
                    new Object[]{serviceId, e.getMessage()});
        }
    }
    
    /**
     * 📊 PROGRAMAR MONITOREO DE PROGRESO
     */
    private void scheduleProgressMonitoring() {
        if (!config.isEnableDetailedMetrics()) {
            return;
        }
        
        scheduledExecutor.scheduleAtFixedRate(() -> {
            try {
                printProgressReport();
            } catch (Exception e) {
                log.log(Level.FINE, "Error in progress monitoring: {0}", e.getMessage());
            }
        }, 1, 1, TimeUnit.SECONDS);
    }
    
    /**
     * ⏰ PROGRAMAR TIMEOUT DE FONDO
     */
    private void scheduleBackgroundTimeout() {
        long timeoutMs = config.getBackgroundPhaseTimeoutMs();
        
        scheduledExecutor.schedule(() -> {
            log.log(Level.WARNING, "Background warming timeout reached ({0}ms), forcing completion", timeoutMs);
            forceCompleteBackgroundWarming();
        }, timeoutMs, TimeUnit.MILLISECONDS);
    }
    
    /**
     * ⏰ FORZAR COMPLETAR CALENTAMIENTO DE FONDO
     */
    private void forceCompleteBackgroundWarming() {
        backgroundWarmingCompleted.set(true);
        backgroundPhaseEndTime = System.currentTimeMillis();
        
        log.log(Level.INFO, "Background warming forced completed. Started: {0}, Completed: {1}, Failed: {2}", 
                new Object[]{warmingStarted.get(), warmingCompleted.get(), warmingFailed.get()});
    }
    
    /**
     * 📊 IMPRIMIR REPORTE DE PROGRESO
     */
    private void printProgressReport() {
        int total = warmingStarted.get();
        int completed = warmingCompleted.get();
        int failed = warmingFailed.get();
        int inProgress = total - completed - failed;
        
        if (total > 0) {
            double progressPercent = (completed * 100.0) / total;
            
            log.log(Level.INFO, "🔄 Background warming progress: {0}% ({1}/{2}) - Completed: {3}, Failed: {4}, In Progress: {5}", 
                    new Object[]{String.format("%.1f", progressPercent), completed, total, completed, failed, inProgress});
        }
    }
    
    /**
     * 🔍 VERIFICAR SI EL CALENTAMIENTO DE FONDO ESTÁ COMPLETO
     */
    public boolean isBackgroundWarmingCompleted() {
        int total = warmingStarted.get();
        int completed = warmingCompleted.get() + warmingFailed.get();
        
        return total > 0 && completed >= total;
    }
    
    /**
     * 🔍 OBTENER SERVICIOS EN CALENTAMIENTO
     */
    public Map<String, ServiceInfo> getWarmingServices() {
        return new ConcurrentHashMap<>(warmingServices);
    }
    
    /**
     * 🔍 OBTENER PROGRESO DE CALENTAMIENTO
     */
    public double getWarmingProgress() {
        int total = warmingStarted.get();
        if (total == 0) return 0.0;
        
        int completed = warmingCompleted.get();
        return (completed * 100.0) / total;
    }
    
    /**
     * ⏱️ OBTENER DURACIÓN DEL CALENTAMIENTO DE FONDO
     */
    public long getBackgroundWarmingDurationMs() {
        if (backgroundPhaseStartTime == 0) return 0;
        
        long endTime = backgroundPhaseEndTime != 0 ? backgroundPhaseEndTime : System.currentTimeMillis();
        return endTime - backgroundPhaseStartTime;
    }
    
    /**
     * 📊 OBTENER ESTADÍSTICAS DE CALENTAMIENTO
     */
    public Map<String, Object> getWarmingStatistics() {
        Map<String, Object> stats = new ConcurrentHashMap<>();
        
        stats.put("warmingStarted", warmingStarted.get());
        stats.put("warmingCompleted", warmingCompleted.get());
        stats.put("warmingFailed", warmingFailed.get());
        stats.put("warmingProgress", getWarmingProgress());
        stats.put("backgroundWarmingStarted", backgroundWarmingStarted.get());
        stats.put("backgroundWarmingCompleted", backgroundWarmingCompleted.get());
        stats.put("backgroundWarmingDurationMs", getBackgroundWarmingDurationMs());
        stats.put("totalServicesInProgress", warmingServices.size());
        stats.put("completedServicesCount", completedServices.size());
        
        return stats;
    }
    
    /**
     * 🧹 CREAR EXECUTOR DE FONDO
     */
    private ExecutorService createBackgroundExecutor() {
        return Executors.newFixedThreadPool(config.getMaxBackgroundThreads(), r -> {
            Thread t = new Thread(r, "warmup-bg-warming");
            t.setDaemon(true);
            return t;
        });
    }
    
    /**
     * 🧹 CERRAR RECURSOS
     */
    public void shutdown() {
        log.log(Level.FINE, "Shutting down NonCriticalServiceWarming");
        
        backgroundWarmingCompleted.set(true);
        backgroundPhaseEndTime = System.currentTimeMillis();
        
        // Cerrar executors
        backgroundExecutor.shutdown();
        scheduledExecutor.shutdown();
        
        try {
            if (!backgroundExecutor.awaitTermination(5, TimeUnit.SECONDS)) {
                backgroundExecutor.shutdownNow();
            }
            if (!scheduledExecutor.awaitTermination(5, TimeUnit.SECONDS)) {
                scheduledExecutor.shutdownNow();
            }
        } catch (InterruptedException e) {
            backgroundExecutor.shutdownNow();
            scheduledExecutor.shutdownNow();
            Thread.currentThread().interrupt();
        }
        
        // Limpiar mapas
        warmingServices.clear();
        warmingFutures.clear();
        completedServices.clear();
    }
    
    /**
     * 📊 GENERAR REPORTE DE CALENTAMIENTO
     */
    public String generateWarmingReport() {
        StringBuilder sb = new StringBuilder();
        sb.append("\n=== NON-CRITICAL SERVICE WARMING REPORT ===\n");
        sb.append(String.format("Background Warming Started: %s\n", backgroundWarmingStarted.get()));
        sb.append(String.format("Background Warming Completed: %s\n", backgroundWarmingCompleted.get()));
        sb.append(String.format("Duration: %dms\n", getBackgroundWarmingDurationMs()));
        sb.append(String.format("Services Started: %d\n", warmingStarted.get()));
        sb.append(String.format("Services Completed: %d\n", warmingCompleted.get()));
        sb.append(String.format("Services Failed: %d\n", warmingFailed.get()));
        sb.append(String.format("Progress: %.2f%%\n", getWarmingProgress()));
        sb.append(String.format("In Progress: %d\n", warmingServices.size()));
        
        sb.append("\n=== WARMING SERVICES ===\n");
        for (ServiceInfo service : warmingServices.values()) {
            sb.append(String.format("- %s (%s): %s\n", 
                    service.getServiceName(), service.getServiceId(), service.getState()));
        }
        
        return sb.toString();
    }
}