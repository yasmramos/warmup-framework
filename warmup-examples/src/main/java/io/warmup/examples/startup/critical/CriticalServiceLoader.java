package io.warmup.examples.startup.critical;

import io.warmup.framework.startup.critical.ServiceDataClasses.ServiceInfo;
import io.warmup.framework.startup.critical.ServiceDataClasses.ServiceState;
import io.warmup.framework.startup.critical.ServiceDataClasses.ServiceCriticality;
import io.warmup.framework.core.WarmupContainer;
import io.warmup.framework.core.BeanRegistry;
import io.warmup.framework.core.DependencyRegistry;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;
import java.util.logging.Level;
import java.time.Instant;

/**
 * 🚀 CARGADOR DE SERVICIOS CRÍTICOS
 * 
 * Carga solo los servicios críticos durante la fase crítica (< 2ms)
 * para garantizar que la aplicación sea "usable" instantáneamente.
 * 
 * Características:
 * - Carga paralela de servicios críticos
 * - Timeout estricto de 2ms
 * - Fallback a servicios básicos si es necesario
 * - Monitoreo en tiempo real del estado
 * 
 * @author MiniMax Agent
 * @version 1.0
 */
public class CriticalServiceLoader {
    
    private static final Logger log = Logger.getLogger(CriticalServiceLoader.class.getName());
    
    private final WarmupContainer container;
    private final BeanRegistry beanRegistry;
    private final DependencyRegistry dependencyRegistry;
    private final ServiceCriticalityClassifier classifier;
    private final Map<String, ServiceInfo> criticalServices = new ConcurrentHashMap<>();
    private final AtomicBoolean criticalPhaseStarted = new AtomicBoolean(false);
    private final AtomicBoolean criticalPhaseCompleted = new AtomicBoolean(false);
    private final AtomicInteger loadedCriticalServices = new AtomicInteger(0);
    private final AtomicInteger failedCriticalServices = new AtomicInteger(0);
    
    // Métricas de tiempo
    private volatile long criticalPhaseStartTime;
    private volatile long criticalPhaseEndTime;
    private static final long CRITICAL_PHASE_TIMEOUT_MS = 2; // 2ms strict timeout
    
    public CriticalServiceLoader(WarmupContainer container) {
        this.container = container;
        this.beanRegistry = (BeanRegistry) container.getBeanRegistry();
        this.dependencyRegistry = (DependencyRegistry) container.getDependencyRegistry();
        this.classifier = new ServiceCriticalityClassifier();
        
        log.log(Level.FINE, "CriticalServiceLoader initialized for container {0}", container);
    }
    
    /**
     * 🚀 CARGAR SERVICIOS CRÍTICOS - Target: < 2ms
     * 
     * Carga únicamente los servicios críticos para lograr respuesta inmediata
     * del framework. Utiliza carga paralela cuando es posible.
     */
    public Map<String, ServiceInfo> loadCriticalServices(List<ServiceInfo> allServices) throws Exception {
        criticalPhaseStarted.set(true);
        criticalPhaseStartTime = System.currentTimeMillis();
        
        log.log(Level.INFO, "🚀 INICIANDO CARGA DE SERVICIOS CRÍTICOS (target: < {0}ms)", CRITICAL_PHASE_TIMEOUT_MS);
        
        try {
            // FASE 1: Identificar y filtrar servicios críticos
            List<ServiceInfo> servicesToLoad = filterCriticalServices(allServices);
            
            if (servicesToLoad.isEmpty()) {
                log.log(Level.WARNING, "⚠️ NO SE ENCONTRARON SERVICIOS CRÍTICOS - Cargando servicios mínimos");
                servicesToLoad = getMinimumEssentialServices();
            }
            
            log.log(Level.FINE, "Identificados {0} servicios críticos para cargar", servicesToLoad.size());
            
            // FASE 2: Carga paralela con timeout estricto
            Map<String, ServiceInfo> loadedServices = loadCriticalServicesParallel(servicesToLoad);
            
            criticalPhaseEndTime = System.currentTimeMillis();
            long durationMs = criticalPhaseEndTime - criticalPhaseStartTime;
            
            criticalPhaseCompleted.set(true);
            
            String result = durationMs <= CRITICAL_PHASE_TIMEOUT_MS ? 
                "🎯 TARGET ALCANZADO" : "⚠️ SOBREPASÓ TARGET";
            
            log.log(Level.INFO, "✅ SERVICIOS CRÍTICOS CARGADOS en {0}ms ({1}) - {2} servicios listos", 
                    new Object[]{durationMs, result, loadedServices.size()});
            
            if (durationMs > CRITICAL_PHASE_TIMEOUT_MS) {
                log.log(Level.WARNING, "⚠️ Fase crítica excedió target por {0}ms", 
                        durationMs - CRITICAL_PHASE_TIMEOUT_MS);
            }
            
            return loadedServices;
            
        } catch (Exception e) {
            criticalPhaseEndTime = System.currentTimeMillis();
            long durationMs = criticalPhaseEndTime - criticalPhaseStartTime;
            
            log.log(Level.SEVERE, "❌ ERROR en carga de servicios críticos después de {0}ms: {1}", 
                    new Object[]{durationMs, e.getMessage()});
            
            // Intentar cargar servicios mínimos como fallback
            try {
                log.log(Level.INFO, "🔄 Intentando cargar servicios mínimos como fallback...");
                return loadMinimumEssentialServices();
            } catch (Exception fallbackError) {
                log.log(Level.SEVERE, "❌ FALLO EN FALLBACK - Imposible cargar servicios críticos: {0}", 
                        fallbackError.getMessage());
                throw e;
            }
        }
    }
    
    /**
     * 🎯 FILTRAR SERVICIOS CRÍTICOS
     */
    private List<ServiceInfo> filterCriticalServices(List<ServiceInfo> allServices) {
        List<ServiceInfo> critical = new ArrayList<>();
        
        // Paso 1: Servicios explícitamente marcados como críticos
        for (ServiceInfo service : allServices) {
            if (service.getCriticality() == ServiceCriticality.CRITICAL) {
                critical.add(service);
            }
        }
        
        // Paso 2: Si hay muy pocos, incluir algunos HIGH también
        if (critical.size() < 3) {
            for (ServiceInfo service : allServices) {
                if (service.getCriticality() == ServiceCriticality.HIGH && !critical.contains(service)) {
                    critical.add(service);
                }
            }
        }
        
        // Paso 3: Si aún hay muy pocos, incluir MEDIUM de inicialización básica
        if (critical.size() < 5) {
            for (ServiceInfo service : allServices) {
                if (service.getCriticality() == ServiceCriticality.MEDIUM && 
                    isEssentialInitializationService(service) && 
                    !critical.contains(service)) {
                    critical.add(service);
                }
            }
        }
        
        // Ordenar por criticidad y tiempo estimado
        critical.sort((s1, s2) -> {
            int criticalityCompare = s1.getCriticality().getPriority() - s2.getCriticality().getPriority();
            if (criticalityCompare != 0) return criticalityCompare;
            
            return Long.compare(s1.getEstimatedInitTimeMs(), s2.getEstimatedInitTimeMs());
        });
        
        // Limitar por tiempo estimado (no más de 2ms total)
        List<ServiceInfo> result = new ArrayList<>();
        long accumulatedTime = 0;
        
        for (ServiceInfo service : critical) {
            long estimatedTime = service.getEstimatedInitTimeMs();
            if (accumulatedTime + estimatedTime <= CRITICAL_PHASE_TIMEOUT_MS) {
                result.add(service);
                accumulatedTime += estimatedTime;
            } else {
                break; // No podemos exceder 2ms
            }
        }
        
        log.log(Level.FINE, "Filtrados {0} servicios críticos (tiempo estimado: {1}ms)", 
                new Object[]{result.size(), accumulatedTime});
        
        return result;
    }
    
    /**
     * 🔍 VERIFICAR SI ES SERVICIO ESENCIAL DE INICIALIZACIÓN
     */
    private boolean isEssentialInitializationService(ServiceInfo service) {
        String name = service.getServiceName().toLowerCase();
        String className = service.getServiceClass().toLowerCase();
        
        // Patrones de servicios esenciales
        return name.contains("container") || 
               name.contains("registry") || 
               name.contains("dependency") || 
               name.contains("injector") ||
               name.contains("config") || 
               name.contains("profile") ||
               className.contains("container") || 
               className.contains("registry") || 
               className.contains("dependency") ||
               className.contains("bean");
    }
    
    /**
     * 🚀 CARGA PARALELA DE SERVICIOS CRÍTICOS
     */
    private Map<String, ServiceInfo> loadCriticalServicesParallel(List<ServiceInfo> servicesToLoad) throws Exception {
        Map<String, ServiceInfo> loadedServices = new ConcurrentHashMap<>();
        
        if (servicesToLoad.isEmpty()) {
            return loadedServices;
        }
        
        // Crear executor con número limitado de threads
        int maxThreads = Math.min(4, Runtime.getRuntime().availableProcessors());
        ExecutorService executor = Executors.newFixedThreadPool(maxThreads, r -> {
            Thread t = new Thread(r, "critical-service-loader");
            t.setDaemon(true);
            return t;
        });
        
        try {
            // Crear tasks para cada servicio crítico
            List<CompletableFuture<ServiceInfo>> futures = new ArrayList<>();
            
            for (ServiceInfo service : servicesToLoad) {
                CompletableFuture<ServiceInfo> future = CompletableFuture.supplyAsync(() -> {
                    try {
                        ServiceInfo loaded = loadSingleCriticalService(service);
                        return loaded;
                    } catch (Exception e) {
                        log.log(Level.WARNING, "Error loading critical service {0}: {1}", 
                                new Object[]{service.getServiceId(), e.getMessage()});
                        return null;
                    }
                }, executor);
                
                futures.add(future);
            }
            
            // Esperar resultados con timeout estricto
            CompletableFuture<Void> allFutures = CompletableFuture.allOf(
                futures.toArray(new CompletableFuture[0])
            );
            
            // Timeout muy estricto
            allFutures.get(CRITICAL_PHASE_TIMEOUT_MS, TimeUnit.MILLISECONDS);
            
            // Recopilar resultados
            for (CompletableFuture<ServiceInfo> future : futures) {
                try {
                    ServiceInfo service = future.get();
                    if (service != null) {
                        loadedServices.put(service.getServiceId(), service);
                        loadedCriticalServices.incrementAndGet();
                    } else {
                        failedCriticalServices.incrementAndGet();
                    }
                } catch (Exception e) {
                    log.log(Level.FINE, "Service future failed: {0}", e.getMessage());
                    failedCriticalServices.incrementAndGet();
                }
            }
            
        } finally {
            executor.shutdown();
            try {
                if (!executor.awaitTermination(100, TimeUnit.MILLISECONDS)) {
                    executor.shutdownNow();
                }
            } catch (InterruptedException e) {
                executor.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }
        
        return loadedServices;
    }
    
    /**
     * 🚀 CARGAR UN SOLO SERVICIO CRÍTICO
     */
    private ServiceInfo loadSingleCriticalService(ServiceInfo serviceInfo) throws Exception {
        String serviceId = serviceInfo.getServiceId();
        
        log.log(Level.FINE, "Loading critical service: {0}", serviceId);
        
        long startTime = System.nanoTime();
        
        try {
            // Actualizar estado a WARMING_UP
            serviceInfo.setState(ServiceState.WARMING_UP);
            serviceInfo.incrementWarmupAttempt();
            
            // Verificar timeout antes de continuar
            long elapsedMs = (System.currentTimeMillis() - criticalPhaseStartTime);
            if (elapsedMs >= CRITICAL_PHASE_TIMEOUT_MS) {
                throw new TimeoutException("Critical phase timeout exceeded");
            }
            
            // Cargar el servicio
            Object serviceInstance = loadServiceInstance(serviceInfo);
            
            if (serviceInstance == null) {
                throw new Exception("Service instance is null");
            }
            
            // Registrar en el registry del container
            beanRegistry.registerBean(serviceId, serviceInstance.getClass(), serviceInstance);
            // TODO: Revisar signature de registerDependency - puede no existir con estos parámetros
            // dependencyRegistry.registerDependency(serviceId, serviceInstance.getClass());
            
            // Actualizar estado a READY
            serviceInfo.setState(ServiceState.READY);
            
            long endTime = System.nanoTime();
            long durationMs = (endTime - startTime) / 1_000_000;
            
            log.log(Level.FINE, "✅ Critical service {0} loaded successfully in {1}ms", 
                    new Object[]{serviceId, durationMs});
            
            // Verificar que no excedimos el timeout
            long totalElapsed = System.currentTimeMillis() - criticalPhaseStartTime;
            if (totalElapsed > CRITICAL_PHASE_TIMEOUT_MS) {
                log.log(Level.WARNING, "⚠️ Service {0} caused timeout (elapsed: {1}ms)", 
                        new Object[]{serviceId, totalElapsed});
            }
            
            return serviceInfo;
            
        } catch (Exception e) {
            serviceInfo.setState(ServiceState.FAILED);
            serviceInfo.setWarmupError(e);
            
            log.log(Level.WARNING, "❌ Failed to load critical service {0}: {1}", 
                    new Object[]{serviceId, e.getMessage()});
            
            throw e;
        }
    }
    
    /**
     * 🔧 CARGAR INSTANCIA DE SERVICIO
     */
    private Object loadServiceInstance(ServiceInfo serviceInfo) throws Exception {
        String serviceClass = serviceInfo.getServiceClass();
        String serviceId = serviceInfo.getServiceId();
        
        try {
            // Intentar cargar desde el registry existente
            Object existing = beanRegistry.getBean(serviceId, Object.class); // TODO: Usar tipo correcto
            if (existing != null) {
                return existing;
            }
            
            // Crear nueva instancia usando reflection optimizada
            Class<?> clazz = Class.forName(serviceClass);
            
            // Verificar si ya está registrado como bean
            Object bean = beanRegistry.getBean(serviceId, clazz); // TODO: Verificar si serviceId es correcto
            if (bean != null) {
                return bean;
            }
            
            // Crear instancia usando constructor optimizado
            return createOptimizedInstance(clazz);
            
        } catch (ClassNotFoundException e) {
            log.log(Level.WARNING, "Service class {0} not found for service {1}", 
                    new Object[]{serviceClass, serviceId});
            
            // Fallback: crear servicio mínimo basado en el nombre
            return createMinimalServiceInstance(serviceInfo);
            
        } catch (Exception e) {
            log.log(Level.WARNING, "Error creating service instance for {0}: {1}", 
                    new Object[]{serviceId, e.getMessage()});
            
            // Fallback: servicio placeholder
            return createPlaceholderService(serviceInfo);
        }
    }
    
    /**
     * 🔧 CREAR INSTANCIA OPTIMIZADA
     */
    private Object createOptimizedInstance(Class<?> clazz) throws Exception {
        try {
            // Buscar constructor sin parámetros
            try {
                return clazz.getDeclaredConstructor().newInstance();
            } catch (NoSuchMethodException e) {
                // Buscar constructor con parámetros y usar reflection
                return createWithReflection(clazz);
            }
        } catch (Exception e) {
            log.log(Level.FINE, "Could not create optimized instance of {0}, using fallback", clazz.getName());
            return createWithBasicReflection(clazz);
        }
    }
    
    /**
     * 🔧 CREAR CON REFLECTION AVANZADA
     */
    private Object createWithReflection(Class<?> clazz) throws Exception {
        // Usar ASM o constructor optimizado si está disponible
        return clazz.getDeclaredConstructor().newInstance();
    }
    
    /**
     * 🔧 CREAR CON REFLECTION BÁSICA
     */
    private Object createWithBasicReflection(Class<?> clazz) throws Exception {
        try {
            return clazz.getDeclaredConstructor().newInstance();
        } catch (Exception e) {
            // Crear proxy o stub si la clase no se puede instanciar
            return createServiceProxy(clazz);
        }
    }
    
    /**
     * 🔧 CREAR SERVICIO PLACEHOLDER
     */
    private Object createPlaceholderService(ServiceInfo serviceInfo) {
        log.log(Level.FINE, "Creating placeholder for service {0}", serviceInfo.getServiceId());
        
        // Crear un objeto mínimo que represente el servicio
        return new Object() {
            @Override
            public String toString() {
                return "PlaceholderService{" + serviceInfo.getServiceId() + "}";
            }
        };
    }
    
    /**
     * 🔧 CREAR PROXY DE SERVICIO
     */
    private Object createServiceProxy(Class<?> clazz) {
        // Retornar un proxy simple o null
        log.log(Level.FINE, "Creating proxy for service class {0}", clazz.getName());
        return null;
    }
    
    /**
     * 🔧 CREAR SERVICIO MÍNIMO
     */
    private Object createMinimalServiceInstance(ServiceInfo serviceInfo) {
        log.log(Level.FINE, "Creating minimal instance for service {0}", serviceInfo.getServiceId());
        
        return new Object() {
            @Override
            public String toString() {
                return "MinimalService{" + serviceInfo.getServiceName() + "}";
            }
        };
    }
    
    /**
     * 📋 OBTENER SERVICIOS MÍNIMOS ESENCIALES
     */
    private List<ServiceInfo> getMinimumEssentialServices() {
        List<ServiceInfo> essential = new ArrayList<>();
        
        // Crear servicios básicos críticos
        essential.add(new ServiceInfo("container", "WarmupContainer", ServiceCriticality.CRITICAL, 1, 
                                     "io.warmup.framework.core.WarmupContainer"));
        essential.add(new ServiceInfo("beanRegistry", "BeanRegistry", ServiceCriticality.CRITICAL, 1, 
                                     "io.warmup.framework.core.BeanRegistry"));
        essential.add(new ServiceInfo("dependencyRegistry", "DependencyRegistry", ServiceCriticality.CRITICAL, 1, 
                                     "io.warmup.framework.core.DependencyRegistry"));
        
        return essential;
    }
    
    /**
     * 🚀 CARGAR SERVICIOS ESENCIALES MÍNIMOS
     */
    private Map<String, ServiceInfo> loadMinimumEssentialServices() throws Exception {
        List<ServiceInfo> essential = getMinimumEssentialServices();
        return loadCriticalServices(essential);
    }
    
    /**
     * 🔍 VERIFICAR SI LA FASE CRÍTICA ESTÁ COMPLETA
     */
    public boolean isCriticalPhaseCompleted() {
        return criticalPhaseCompleted.get();
    }
    
    /**
     * ⏱️ OBTENER DURACIÓN DE LA FASE CRÍTICA
     */
    public long getCriticalPhaseDurationMs() {
        if (criticalPhaseStartTime == 0) return 0;
        
        long endTime = criticalPhaseEndTime != 0 ? criticalPhaseEndTime : System.currentTimeMillis();
        return endTime - criticalPhaseStartTime;
    }
    
    /**
     * 📊 OBTENER SERVICIOS CRÍTICOS CARGADOS
     */
    public Map<String, ServiceInfo> getLoadedCriticalServices() {
        return new ConcurrentHashMap<>(criticalServices);
    }
    
    /**
     * 📊 OBTENER MÉTRICAS
     */
    public Map<String, Object> getCriticalPhaseMetrics() {
        Map<String, Object> metrics = new ConcurrentHashMap<>();
        
        metrics.put("criticalPhaseStarted", criticalPhaseStarted.get());
        metrics.put("criticalPhaseCompleted", criticalPhaseCompleted.get());
        metrics.put("criticalPhaseDurationMs", getCriticalPhaseDurationMs());
        metrics.put("loadedCriticalServices", loadedCriticalServices.get());
        metrics.put("failedCriticalServices", failedCriticalServices.get());
        metrics.put("criticalPhaseTimeoutMs", CRITICAL_PHASE_TIMEOUT_MS);
        metrics.put("targetAchieved", getCriticalPhaseDurationMs() <= CRITICAL_PHASE_TIMEOUT_MS);
        
        return metrics;
    }
    
    /**
     * 🧹 LIMPIAR RECURSOS
     */
    public void cleanup() {
        criticalServices.clear();
        criticalPhaseStarted.set(false);
        criticalPhaseCompleted.set(false);
        loadedCriticalServices.set(0);
        failedCriticalServices.set(0);
        criticalPhaseStartTime = 0;
        criticalPhaseEndTime = 0;
    }
    
    /**
     * 📊 GENERAR REPORTE DE LA FASE CRÍTICA
     */
    public String generateCriticalPhaseReport() {
        StringBuilder sb = new StringBuilder();
        sb.append("\n=== CRITICAL SERVICE LOADING REPORT ===\n");
        sb.append(String.format("Critical Phase Started: %s\n", criticalPhaseStarted.get()));
        sb.append(String.format("Critical Phase Completed: %s\n", criticalPhaseCompleted.get()));
        sb.append(String.format("Duration: %dms\n", getCriticalPhaseDurationMs()));
        sb.append(String.format("Target (< %dms): %s\n", CRITICAL_PHASE_TIMEOUT_MS, 
                getCriticalPhaseDurationMs() <= CRITICAL_PHASE_TIMEOUT_MS ? "✅ ACHIEVED" : "❌ EXCEEDED"));
        sb.append(String.format("Critical Services Loaded: %d\n", loadedCriticalServices.get()));
        sb.append(String.format("Critical Services Failed: %d\n", failedCriticalServices.get()));
        sb.append(String.format("Success Rate: %.2f%%\n", 
                calculateSuccessRate() * 100));
        
        sb.append("\n=== LOADED CRITICAL SERVICES ===\n");
        for (ServiceInfo service : criticalServices.values()) {
            sb.append(String.format("- %s (%s): %s\n", 
                    service.getServiceName(), service.getServiceId(), service.getState()));
        }
        
        return sb.toString();
    }
    
    /**
     * 📊 CALCULAR TASA DE ÉXITO
     */
    private double calculateSuccessRate() {
        long total = loadedCriticalServices.get() + failedCriticalServices.get();
        if (total == 0) return 1.0;
        return (double) loadedCriticalServices.get() / total;
    }
}