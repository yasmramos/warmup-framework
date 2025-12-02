package io.warmup.framework.startup.critical;

import io.warmup.framework.startup.critical.ServiceDataClasses.ServiceInfo;
import io.warmup.framework.startup.critical.ServiceDataClasses.ServiceState;
import io.warmup.framework.startup.critical.ServiceDataClasses.CriticalSeparationResult;
import io.warmup.framework.startup.critical.ServiceDataClasses.CriticalSeparationMetrics;
import io.warmup.framework.startup.critical.ServiceDataClasses.CriticalSeparationConfig;
import io.warmup.framework.startup.critical.ServiceDataClasses.ServiceCriticality;
import io.warmup.framework.core.WarmupContainer;
import io.warmup.framework.startup.StartupPhaseException;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;
import java.util.logging.Level;

/**
 * 🚀 SISTEMA DE SEPARACIÓN CRÍTICA DE SERVICIOS
 * 
 * Coordina la separación entre servicios críticos y no críticos para lograr:
 * - Aplicación "usable" en 2ms
 * - Calentamiento asíncrono de servicios no críticos
 * - Separación clara entre respuesta inmediata y optimización de fondo
 * 
 * Arquitectura:
 * 1. Fase Crítica: Solo servicios críticos en < 2ms
 * 2. Fase de Fondo: Servicios no críticos se calientan asíncronamente
 * 
 * @author MiniMax Agent
 * @version 1.0
 */
public class CriticalSeparationSystem {
    
    private static final Logger log = Logger.getLogger(CriticalSeparationSystem.class.getName());
    
    private final WarmupContainer container;
    private final CriticalSeparationConfig config;
    private final ServiceCriticalityClassifier classifier;
    private final CriticalServiceLoader criticalLoader;
    private final NonCriticalServiceWarming nonCriticalWarming;
    private final CriticalSeparationMetrics metrics;
    
    // Estado del sistema
    private final AtomicBoolean systemInitialized = new AtomicBoolean(false);
    private final AtomicBoolean criticalPhaseStarted = new AtomicBoolean(false);
    private final AtomicBoolean backgroundPhaseStarted = new AtomicBoolean(false);
    
    public CriticalSeparationSystem(WarmupContainer container) {
        this(container, CriticalSeparationConfig.balanced());
    }
    
    public CriticalSeparationSystem(WarmupContainer container, CriticalSeparationConfig config) {
        this.container = container;
        this.config = config;
        this.classifier = new ServiceCriticalityClassifier();
        this.criticalLoader = new CriticalServiceLoader(container);
        this.nonCriticalWarming = new NonCriticalServiceWarming(container, config);
        this.metrics = new CriticalSeparationMetrics();
        
        log.log(Level.FINE, "CriticalSeparationSystem initialized with config: {0}", 
                config.getClass().getSimpleName());
    }
    
    /**
     * 🚀 EJECUTAR SEPARACIÓN CRÍTICA COMPLETA
     * 
     * Ejecuta la separación completa de servicios críticos y no críticos:
     * 1. Clasificar todos los servicios
     * 2. Cargar servicios críticos en < 2ms
     * 3. Iniciar calentamiento de servicios no críticos en background
     */
    public CriticalSeparationResult executeCriticalSeparation(List<ServiceInfo> allServices) {
        if (systemInitialized.compareAndSet(false, true)) {
            try {
                log.log(Level.INFO, "🚀 INICIANDO SEPARACIÓN CRÍTICA DE SERVICIOS");
                
                // Verificar si la separación está habilitada
                if (!config.isEnableCriticalServiceSeparation()) {
                    log.log(Level.WARNING, "Critical service separation disabled by configuration");
                    return createDisabledResult();
                }
                
                // FASE 1: Clasificar servicios
                Map<String, ServiceInfo> classifiedServices = classifyAllServices(allServices);
                
                // FASE 2: Ejecutar fase crítica
                Map<String, ServiceInfo> criticalServices = executeCriticalPhase(classifiedServices);
                
                // FASE 3: Ejecutar fase de background
                executeBackgroundPhase(classifiedServices, criticalServices);
                
                // FASE 4: Generar resultado
                CriticalSeparationResult result = generateFinalResult(criticalServices, classifiedServices);
                
                log.log(Level.INFO, "✅ SEPARACIÓN CRÍTICA COMPLETADA - {0}", 
                        result.generateReport());
                
                return result;
                
            } catch (Exception e) {
                log.log(Level.SEVERE, "❌ Error en separación crítica: {0}", e.getMessage());
                return createErrorResult(e);
            }
        } else {
            log.log(Level.WARNING, "Sistema de separación crítica ya inicializado");
            return createAlreadyInitializedResult();
        }
    }
    
    /**
     * 🎯 CLASIFICAR TODOS LOS SERVICIOS
     */
    private Map<String, ServiceInfo> classifyAllServices(List<ServiceInfo> services) {
        log.log(Level.FINE, "Clasificando {0} servicios...", services.size());
        
        Map<String, ServiceInfo> classified = classifier.classifyServicesBatch(services);
        
        log.log(Level.FINE, "Clasificación completada: {0}", 
                classifier.generateClassificationReport());
        
        return classified;
    }
    
    /**
     * 🚀 EJECUTAR FASE CRÍTICA
     */
    private Map<String, ServiceInfo> executeCriticalPhase(Map<String, ServiceInfo> allServices) throws Exception {
        criticalPhaseStarted.set(true);
        metrics.startCriticalPhase();
        
        log.log(Level.INFO, "🚨 EJECUTANDO FASE CRÍTICA (target: < 2ms)");
        
        try {
            // Filtrar solo servicios críticos para la fase crítica
            List<ServiceInfo> criticalServiceList = new ArrayList<>();
            for (ServiceInfo service : allServices.values()) {
                if (service.getCriticality().isCritical()) {
                    criticalServiceList.add(service);
                }
            }
            
            // Si no hay suficientes servicios críticos, incluir algunos HIGH
            if (criticalServiceList.size() < 3) {
                for (ServiceInfo service : allServices.values()) {
                    if (service.getCriticality() == ServiceCriticality.HIGH && 
                        !criticalServiceList.contains(service)) {
                        criticalServiceList.add(service);
                    }
                }
            }
            
            // Cargar servicios críticos
            Map<String, ServiceInfo> loadedCriticalServices = 
                criticalLoader.loadCriticalServices(criticalServiceList);
            
            // Actualizar métricas
            metrics.endCriticalPhase();
            
            int successful = 0;
            int failed = 0;
            
            for (ServiceInfo service : loadedCriticalServices.values()) {
                if (service.isReady()) {
                    successful++;
                    metrics.incrementCriticalServicesLoaded();
                } else if (service.getState() == ServiceState.FAILED) {
                    failed++;
                    metrics.incrementCriticalServicesFailed();
                }
            }
            
            log.log(Level.INFO, "🎯 FASE CRÍTICA COMPLETADA - {0} servicios listos, {1} fallos", 
                    new Object[]{successful, failed});
            
            return loadedCriticalServices;
            
        } catch (Exception e) {
            metrics.endCriticalPhase();
            log.log(Level.SEVERE, "❌ Error en fase crítica: {0}", e.getMessage());
            throw e;
        }
    }
    
    /**
     * 🌡️ EJECUTAR FASE DE BACKGROUND
     */
    private void executeBackgroundPhase(Map<String, ServiceInfo> allServices, 
                                      Map<String, ServiceInfo> criticalServices) {
        backgroundPhaseStarted.set(true);
        metrics.startBackgroundPhase();
        
        log.log(Level.INFO, "🌡️ INICIANDO FASE DE BACKGROUND - Servicios no críticos");
        
        try {
            // Filtrar servicios no críticos
            List<ServiceInfo> nonCriticalServices = new ArrayList<>();
            
            for (ServiceInfo service : allServices.values()) {
                if (!service.getCriticality().isCritical() && 
                    !criticalServices.containsKey(service.getServiceId())) {
                    nonCriticalServices.add(service);
                }
            }
            
            log.log(Level.INFO, "🌡️ Iniciando calentamiento de {0} servicios no críticos", 
                    nonCriticalServices.size());
            
            // Iniciar calentamiento de servicios no críticos
            nonCriticalWarming.startBackgroundWarming(nonCriticalServices);
            
            // Actualizar métricas
            for (ServiceInfo service : nonCriticalServices) {
                metrics.incrementServicesByCriticality(service.getCriticality());
            }
            
            log.log(Level.FINE, "✅ Fase de background iniciada - Calentamiento asíncrono en progreso");
            
        } catch (Exception e) {
            metrics.startBackgroundPhase(); // Asegurar que las métricas estén configuradas
            log.log(Level.WARNING, "⚠️ Error iniciando fase de background: {0}", e.getMessage());
        }
    }
    
    /**
     * 📊 GENERAR RESULTADO FINAL
     */
    private CriticalSeparationResult generateFinalResult(Map<String, ServiceInfo> criticalServices,
                                                       Map<String, ServiceInfo> allServices) {
        metrics.endBackgroundPhase();
        
        boolean success = criticalServicesFailed() == 0;
        ServiceState overallState = calculateOverallState(criticalServices, allServices);
        
        return new CriticalSeparationResult(
            success,
            metrics.getCriticalPhaseDurationMs(),
            metrics.getBackgroundPhaseDurationMs(),
            (int) metrics.getCriticalServicesLoaded(),
            (int) metrics.getNonCriticalServicesLoaded(),
            (int) metrics.getCriticalServicesFailed(),
            (int) metrics.getNonCriticalServicesFailed(),
            overallState,
            metrics,
            allServices,
            null
        );
    }
    
    /**
     * 🔍 CALCULAR ESTADO GENERAL DE LA APLICACIÓN
     */
    private ServiceState calculateOverallState(Map<String, ServiceInfo> criticalServices,
                                             Map<String, ServiceInfo> allServices) {
        
        // Si hay servicios críticos fallando, estado FAILED
        for (ServiceInfo service : criticalServices.values()) {
            if (service.getState() == ServiceState.FAILED) {
                return ServiceState.FAILED;
            }
        }
        
        // Si todos los servicios críticos están listos, estado READY
        boolean allCriticalReady = criticalServices.values().stream()
            .allMatch(ServiceInfo::isReady);
        
        if (allCriticalReady) {
            // Verificar si el calentamiento de background está completo
            if (nonCriticalWarming.isBackgroundWarmingCompleted()) {
                return ServiceState.READY;
            } else {
                return ServiceState.DEGRADED; // Funcional pero optimizándose
            }
        }
        
        // Estado por defecto
        return ServiceState.WARMING_UP;
    }
    
    /**
     * 🔍 VERIFICAR SI HAY SERVICIOS CRÍTICOS FALLANDO
     */
    private long criticalServicesFailed() {
        return metrics.getCriticalServicesFailed();
    }
    
    /**
     * 📋 EJECUTAR FASE CRÍTICA SINCRÓNICA
     */
    public Map<String, ServiceInfo> executeCriticalPhaseSync(Map<String, ServiceInfo> services) throws Exception {
        log.log(Level.INFO, "🔄 Ejecutando fase crítica sincrónica...");
        
        return executeCriticalPhase(services);
    }
    
    /**
     * 📋 EJECUTAR FASE CRÍTICA ASÍNCRONA
     */
    public CompletableFuture<Map<String, ServiceInfo>> executeCriticalPhaseAsync(Map<String, ServiceInfo> services) {
        log.log(Level.INFO, "🔄 Ejecutando fase crítica asíncrona...");
        
        return CompletableFuture.supplyAsync(() -> {
            try {
                return executeCriticalPhase(services);
            } catch (Exception e) {
                log.log(Level.SEVERE, "Error en fase crítica asíncrona: {0}", e.getMessage());
                throw new RuntimeException(e);
            }
        });
    }
    
    /**
     * 📋 EJECUTAR SOLO CALENTAMIENTO DE BACKGROUND
     */
    public void executeBackgroundWarmingOnly(Map<String, ServiceInfo> services) {
        log.log(Level.INFO, "🌡️ Ejecutando solo calentamiento de background...");
        
        executeBackgroundPhase(services, new ConcurrentHashMap<>());
    }
    
    /**
     * 🔍 VERIFICAR SI LA FASE CRÍTICA ESTÁ COMPLETA
     */
    public boolean isCriticalPhaseCompleted() {
        return criticalLoader.isCriticalPhaseCompleted();
    }
    
    /**
     * 🔍 VERIFICAR SI LA FASE DE BACKGROUND ESTÁ COMPLETA
     */
    public boolean isBackgroundPhaseCompleted() {
        return nonCriticalWarming.isBackgroundWarmingCompleted();
    }
    
    /**
     * 🔍 VERIFICAR SI EL SISTEMA ESTÁ COMPLETAMENTE LISTO
     */
    public boolean isFullyReady() {
        return isCriticalPhaseCompleted() && isBackgroundPhaseCompleted();
    }
    
    /**
     * 🔍 VERIFICAR SI LA APLICACIÓN ES USABLE
     */
    public boolean isApplicationUsable() {
        return criticalPhaseStarted.get() && criticalServicesFailed() == 0;
    }
    
    /**
     * 📊 OBTENER MÉTRICAS DEL SISTEMA
     */
    public Map<String, Object> getSystemMetrics() {
        Map<String, Object> allMetrics = new ConcurrentHashMap<>();
        
        // Métricas básicas del sistema
        allMetrics.put("systemInitialized", systemInitialized.get());
        allMetrics.put("criticalPhaseStarted", criticalPhaseStarted.get());
        allMetrics.put("backgroundPhaseStarted", backgroundPhaseStarted.get());
        allMetrics.put("criticalPhaseCompleted", isCriticalPhaseCompleted());
        allMetrics.put("backgroundPhaseCompleted", isBackgroundPhaseCompleted());
        allMetrics.put("fullyReady", isFullyReady());
        allMetrics.put("applicationUsable", isApplicationUsable());
        
        // Métricas de clasificación
        allMetrics.putAll(classifier.getClassificationStatistics());
        
        // Métricas de fase crítica
        allMetrics.putAll(criticalLoader.getCriticalPhaseMetrics());
        
        // Métricas de calentamiento
        allMetrics.putAll(nonCriticalWarming.getWarmingStatistics());
        
        // Métricas de separación
        allMetrics.put("criticalPhaseDurationMs", metrics.getCriticalPhaseDurationMs());
        allMetrics.put("backgroundPhaseDurationMs", metrics.getBackgroundPhaseDurationMs());
        allMetrics.put("totalServices", metrics.getTotalServicesCount());
        
        return allMetrics;
    }
    
    /**
     * 🔍 OBTENER SERVICIOS CRÍTICOS CARGADOS
     */
    public Map<String, ServiceInfo> getLoadedCriticalServices() {
        return criticalLoader.getLoadedCriticalServices();
    }
    
    /**
     * 🔍 OBTENER SERVICIOS EN CALENTAMIENTO
     */
    public Map<String, ServiceInfo> getWarmingServices() {
        return nonCriticalWarming.getWarmingServices();
    }
    
    /**
     * 🧹 LIMPIAR RECURSOS DEL SISTEMA
     */
    public void cleanup() {
        log.log(Level.FINE, "Limpiando CriticalSeparationSystem...");
        
        // Limpiar componentes
        criticalLoader.cleanup();
        nonCriticalWarming.shutdown();
        
        // Resetear estado
        systemInitialized.set(false);
        criticalPhaseStarted.set(false);
        backgroundPhaseStarted.set(false);
    }
    
    /**
     * 📊 GENERAR REPORTE COMPLETO DEL SISTEMA
     */
    public String generateSystemReport() {
        StringBuilder sb = new StringBuilder();
        
        sb.append("\n=== CRITICAL SEPARATION SYSTEM REPORT ===\n");
        sb.append(String.format("System Initialized: %s\n", systemInitialized.get()));
        sb.append(String.format("Critical Phase Started: %s\n", criticalPhaseStarted.get()));
        sb.append(String.format("Background Phase Started: %s\n", backgroundPhaseStarted.get()));
        sb.append(String.format("Critical Phase Completed: %s\n", isCriticalPhaseCompleted()));
        sb.append(String.format("Background Phase Completed: %s\n", isBackgroundPhaseCompleted()));
        sb.append(String.format("Application Usable: %s\n", isApplicationUsable()));
        sb.append(String.format("Fully Ready: %s\n", isFullyReady()));
        
        // Reportes de componentes
        sb.append("\n=== CRITICAL SERVICE LOADER ===\n");
        sb.append(criticalLoader.generateCriticalPhaseReport());
        
        sb.append("\n=== NON-CRITICAL SERVICE WARMING ===\n");
        sb.append(nonCriticalWarming.generateWarmingReport());
        
        sb.append("\n=== SERVICE CLASSIFICATION ===\n");
        sb.append(classifier.generateClassificationReport());
        
        sb.append("\n=== SEPARATION METRICS ===\n");
        sb.append(metrics.generateSummaryReport());
        
        return sb.toString();
    }
    
    // ==================== MÉTODOS DE RESULTADO ====================
    
    private CriticalSeparationResult createDisabledResult() {
        return new CriticalSeparationResult(
            true,
            0, 0, 0, 0, 0, 0,
            ServiceState.READY,
            metrics,
            new ConcurrentHashMap<>(),
            null
        );
    }
    
    private CriticalSeparationResult createErrorResult(Exception error) {
        return new CriticalSeparationResult(
            false,
            metrics.getCriticalPhaseDurationMs(),
            metrics.getBackgroundPhaseDurationMs(),
            (int) metrics.getCriticalServicesLoaded(),
            (int) metrics.getNonCriticalServicesLoaded(),
            (int) metrics.getCriticalServicesFailed(),
            (int) metrics.getNonCriticalServicesFailed(),
            ServiceState.FAILED,
            metrics,
            new ConcurrentHashMap<>(),
            error
        );
    }
    
    private CriticalSeparationResult createAlreadyInitializedResult() {
        return new CriticalSeparationResult(
            false,
            0, 0, 0, 0, 0, 0,
            ServiceState.UNKNOWN,
            metrics,
            new ConcurrentHashMap<>(),
            new IllegalStateException("System already initialized")
        );
    }
}