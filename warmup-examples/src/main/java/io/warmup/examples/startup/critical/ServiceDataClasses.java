package io.warmup.examples.startup.critical;

import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;
import java.time.Instant;
import java.util.logging.Logger;

/**
 * 📊 CLASES DE DATOS PARA SEPARACIÓN CRÍTICA DE SERVICIOS
 * 
 * Contiene todas las clases de datos, enums y estructuras para el sistema
 * de separación de servicios críticos vs no críticos.
 * 
 * @author MiniMax Agent
 * @version 1.0
 */
public class ServiceDataClasses {
    
    private static final Logger log = Logger.getLogger(ServiceDataClasses.class.getName());
    
    /**
     * 🎯 ENUM: Niveles de criticidad de servicios
     * Define los niveles de criticidad para la carga de servicios
     */
    public enum ServiceCriticality {
        CRITICAL(1, "Crítico - Requiere respuesta < 2ms"),
        HIGH(2, "Alta - Required para funcionalidad básica"),
        MEDIUM(3, "Media - Importante pero no bloqueante"),
        LOW(4, "Baja - Puede esperar en segundo plano"),
        BACKGROUND(5, "Background - Sin impacto en UX");
        
        private final int priority;
        private final String description;
        
        ServiceCriticality(int priority, String description) {
            this.priority = priority;
            this.description = description;
        }
        
        public int getPriority() { return priority; }
        public String getDescription() { return description; }
        
        public boolean isCritical() { return this == CRITICAL; }
        public boolean isHigh() { return this == HIGH || this == CRITICAL; }
        public boolean canWarmUpInBackground() { return this != CRITICAL; }
    }
    
    /**
     * 🚦 ENUM: Estados de servicios durante startup
     */
    public enum ServiceState {
        UNKNOWN("Desconocido"),
        COLD("Frío - No inicializado"),
        WARMING_UP("Calentándose - Inicialización en progreso"),
        READY("Listo - Completamente funcional"),
        DEGRADED("Degradado - Funcionando parcialmente"),
        FAILED("Fallo - Error durante inicialización");
        
        private final String description;
        
        ServiceState(String description) {
            this.description = description;
        }
        
        public String getDescription() { return description; }
        public boolean isFunctional() { return this == READY || this == DEGRADED; }
    }
    
    /**
     * 📋 CLASE: Información completa de un servicio
     */
    public static class ServiceInfo {
        private final String serviceId;
        private final String serviceName;
        private final ServiceCriticality criticality;
        private volatile ServiceState state = ServiceState.COLD;
        private final long estimatedInitTimeMs;
        private final String serviceClass;
        private final List<String> dependencies = new ArrayList<>();
        private final Map<String, Object> metadata = new ConcurrentHashMap<>();
        
        private volatile Instant createdAt;
        private volatile Instant warmingStarted;
        private volatile Instant warmingCompleted;
        private volatile AtomicLong warmupAttemptCount = new AtomicLong(0);
        private volatile Exception warmupError;
        
        public ServiceInfo(String serviceId, String serviceName, ServiceCriticality criticality, 
                          long estimatedInitTimeMs, String serviceClass) {
            this.serviceId = serviceId;
            this.serviceName = serviceName;
            this.criticality = criticality;
            this.estimatedInitTimeMs = estimatedInitTimeMs;
            this.serviceClass = serviceClass;
            this.createdAt = Instant.now();
        }
        
        // Getters
        public String getServiceId() { return serviceId; }
        public String getServiceName() { return serviceName; }
        
        /**
         * Gets the service name (alias for getServiceName)
         */
        public String getName() { return serviceName; }
        public ServiceCriticality getCriticality() { return criticality; }
        public ServiceState getState() { return state; }
        public long getEstimatedInitTimeMs() { return estimatedInitTimeMs; }
        public String getServiceClass() { return serviceClass; }
        public List<String> getDependencies() { return dependencies; }
        public Map<String, Object> getMetadata() { return metadata; }
        public Instant getCreatedAt() { return createdAt; }
        public Instant getWarmingStarted() { return warmingStarted; }
        public Instant getWarmingCompleted() { return warmingCompleted; }
        public long getWarmupAttemptCount() { return warmupAttemptCount.get(); }
        public Exception getWarmupError() { return warmupError; }
        
        // Setters con validación
        public void setState(ServiceState newState) {
            this.state = newState;
            
            if (newState == ServiceState.WARMING_UP && this.warmingStarted == null) {
                this.warmingStarted = Instant.now();
            } else if (newState == ServiceState.READY && this.warmingCompleted == null) {
                this.warmingCompleted = Instant.now();
            }
        }
        
        public void incrementWarmupAttempt() {
            this.warmupAttemptCount.incrementAndGet();
        }
        
        public void setWarmupError(Exception error) {
            this.warmupError = error;
        }
        
        public void addDependency(String dependencyId) {
            this.dependencies.add(dependencyId);
        }
        
        public boolean isCritical() {
            return criticality.isCritical();
        }
        
        public boolean isReady() {
            return state == ServiceState.READY;
        }
        
        public boolean isFunctional() {
            return state.isFunctional();
        }
        
        public long getWarmingTimeMs() {
            if (warmingStarted == null) return 0;
            Instant end = warmingCompleted != null ? warmingCompleted : Instant.now();
            return (end.toEpochMilli() - warmingStarted.toEpochMilli());
        }
        
        @Override
        public String toString() {
            return String.format("ServiceInfo{id='%s', name='%s', criticality=%s, state=%s, initTime=%dms}", 
                    serviceId, serviceName, criticality, state, estimatedInitTimeMs);
        }
    }
    
    /**
     * 📈 CLASE: Métricas de separación de servicios
     */
    public static class CriticalSeparationMetrics {
        private final AtomicLong criticalServicesLoaded = new AtomicLong(0);
        private final AtomicLong nonCriticalServicesLoaded = new AtomicLong(0);
        private final AtomicLong criticalServicesFailed = new AtomicLong(0);
        private final AtomicLong nonCriticalServicesFailed = new AtomicLong(0);
        private final AtomicLong backgroundWarmupsCompleted = new AtomicLong(0);
        private final AtomicLong backgroundWarmupsStarted = new AtomicLong(0);
        
        private volatile long criticalPhaseStartTime;
        private volatile long criticalPhaseEndTime;
        private volatile long backgroundPhaseStartTime;
        private volatile long backgroundPhaseEndTime;
        
        private final Map<ServiceCriticality, AtomicLong> servicesByCriticality = new ConcurrentHashMap<>();
        private final Map<ServiceState, AtomicLong> servicesByState = new ConcurrentHashMap<>();
        
        public CriticalSeparationMetrics() {
            for (ServiceCriticality criticality : ServiceCriticality.values()) {
                servicesByCriticality.put(criticality, new AtomicLong(0));
            }
            for (ServiceState state : ServiceState.values()) {
                servicesByState.put(state, new AtomicLong(0));
            }
        }
        
        // Getters
        public long getCriticalServicesLoaded() { return criticalServicesLoaded.get(); }
        public long getNonCriticalServicesLoaded() { return nonCriticalServicesLoaded.get(); }
        public long getCriticalServicesFailed() { return criticalServicesFailed.get(); }
        public long getNonCriticalServicesFailed() { return nonCriticalServicesFailed.get(); }
        public long getBackgroundWarmupsCompleted() { return backgroundWarmupsCompleted.get(); }
        public long getBackgroundWarmupsStarted() { return backgroundWarmupsStarted.get(); }
        public long getCriticalPhaseStartTime() { return criticalPhaseStartTime; }
        public long getCriticalPhaseEndTime() { return criticalPhaseEndTime; }
        public long getBackgroundPhaseStartTime() { return backgroundPhaseStartTime; }
        public long getBackgroundPhaseEndTime() { return backgroundPhaseEndTime; }
        
        // Timing methods
        public void startCriticalPhase() {
            this.criticalPhaseStartTime = System.currentTimeMillis();
        }
        
        public void endCriticalPhase() {
            this.criticalPhaseEndTime = System.currentTimeMillis();
        }
        
        public void startBackgroundPhase() {
            this.backgroundPhaseStartTime = System.currentTimeMillis();
        }
        
        public void endBackgroundPhase() {
            this.backgroundPhaseEndTime = System.currentTimeMillis();
        }
        
        // Counter methods
        public void incrementCriticalServicesLoaded() { criticalServicesLoaded.incrementAndGet(); }
        public void incrementNonCriticalServicesLoaded() { nonCriticalServicesLoaded.incrementAndGet(); }
        public void incrementCriticalServicesFailed() { criticalServicesFailed.incrementAndGet(); }
        public void incrementNonCriticalServicesFailed() { nonCriticalServicesFailed.incrementAndGet(); }
        public void incrementBackgroundWarmupsCompleted() { backgroundWarmupsCompleted.incrementAndGet(); }
        public void incrementBackgroundWarmupsStarted() { backgroundWarmupsStarted.incrementAndGet(); }
        
        public void incrementServicesByCriticality(ServiceCriticality criticality) {
            servicesByCriticality.get(criticality).incrementAndGet();
        }
        
        public void incrementServicesByState(ServiceState state) {
            servicesByState.get(state).incrementAndGet();
        }
        
        // Calculated metrics
        public long getCriticalPhaseDurationMs() {
            if (criticalPhaseStartTime == 0 || criticalPhaseEndTime == 0) return 0;
            return criticalPhaseEndTime - criticalPhaseStartTime;
        }
        
        public long getBackgroundPhaseDurationMs() {
            if (backgroundPhaseStartTime == 0 || backgroundPhaseEndTime == 0) return 0;
            return backgroundPhaseEndTime - backgroundPhaseStartTime;
        }
        
        public long getTotalServicesCount() {
            return criticalServicesLoaded.get() + nonCriticalServicesLoaded.get() + 
                   criticalServicesFailed.get() + nonCriticalServicesFailed.get();
        }
        
        public double getCriticalServicesSuccessRate() {
            long total = criticalServicesLoaded.get() + criticalServicesFailed.get();
            if (total == 0) return 1.0;
            return (double) criticalServicesLoaded.get() / total;
        }
        
        public double getNonCriticalServicesSuccessRate() {
            long total = nonCriticalServicesLoaded.get() + nonCriticalServicesFailed.get();
            if (total == 0) return 1.0;
            return (double) nonCriticalServicesLoaded.get() / total;
        }
        
        public String generateSummaryReport() {
            StringBuilder sb = new StringBuilder();
            sb.append("\n=== CRITICAL SERVICE SEPARATION METRICS ===\n");
            sb.append(String.format("Critical Phase Duration: %dms\n", getCriticalPhaseDurationMs()));
            sb.append(String.format("Background Phase Duration: %dms\n", getBackgroundPhaseDurationMs()));
            sb.append(String.format("Total Services: %d\n", getTotalServicesCount()));
            sb.append(String.format("Critical Services Loaded: %d\n", getCriticalServicesLoaded()));
            sb.append(String.format("Non-Critical Services Loaded: %d\n", getNonCriticalServicesLoaded()));
            sb.append(String.format("Critical Services Failed: %d\n", getCriticalServicesFailed()));
            sb.append(String.format("Non-Critical Services Failed: %d\n", getNonCriticalServicesFailed()));
            sb.append(String.format("Background Warmups Completed: %d\n", getBackgroundWarmupsCompleted()));
            sb.append(String.format("Critical Success Rate: %.2f%%\n", getCriticalServicesSuccessRate() * 100));
            sb.append(String.format("Non-Critical Success Rate: %.2f%%\n", getNonCriticalServicesSuccessRate() * 100));
            sb.append("\n=== SERVICES BY CRITICALITY ===\n");
            for (Map.Entry<ServiceCriticality, AtomicLong> entry : servicesByCriticality.entrySet()) {
                sb.append(String.format("%s: %d\n", entry.getKey(), entry.getValue().get()));
            }
            sb.append("\n=== SERVICES BY STATE ===\n");
            for (Map.Entry<ServiceState, AtomicLong> entry : servicesByState.entrySet()) {
                sb.append(String.format("%s: %d\n", entry.getKey(), entry.getValue().get()));
            }
            return sb.toString();
        }
    }
    
    /**
     * ⚙️ CONFIGURACIÓN del sistema de separación crítica
     */
    public static class CriticalSeparationConfig {
        private final boolean enableCriticalServiceSeparation;
        private final boolean enableBackgroundWarming;
        private final boolean enableFailureRecovery;
        private final int maxBackgroundThreads;
        private final long criticalPhaseTimeoutMs;
        private final long backgroundPhaseTimeoutMs;
        private final boolean enableDetailedMetrics;
        private final boolean enableServiceHealthChecks;
        
        public CriticalSeparationConfig(boolean enableCriticalServiceSeparation,
                                      boolean enableBackgroundWarming,
                                      boolean enableFailureRecovery,
                                      int maxBackgroundThreads,
                                      long criticalPhaseTimeoutMs,
                                      long backgroundPhaseTimeoutMs,
                                      boolean enableDetailedMetrics,
                                      boolean enableServiceHealthChecks) {
            this.enableCriticalServiceSeparation = enableCriticalServiceSeparation;
            this.enableBackgroundWarming = enableBackgroundWarming;
            this.enableFailureRecovery = enableFailureRecovery;
            this.maxBackgroundThreads = maxBackgroundThreads;
            this.criticalPhaseTimeoutMs = criticalPhaseTimeoutMs;
            this.backgroundPhaseTimeoutMs = backgroundPhaseTimeoutMs;
            this.enableDetailedMetrics = enableDetailedMetrics;
            this.enableServiceHealthChecks = enableServiceHealthChecks;
        }
        
        // Getters
        public boolean isEnableCriticalServiceSeparation() { return enableCriticalServiceSeparation; }
        public boolean isEnableBackgroundWarming() { return enableBackgroundWarming; }
        public boolean isEnableFailureRecovery() { return enableFailureRecovery; }
        public int getMaxBackgroundThreads() { return maxBackgroundThreads; }
        public long getCriticalPhaseTimeoutMs() { return criticalPhaseTimeoutMs; }
        public long getBackgroundPhaseTimeoutMs() { return backgroundPhaseTimeoutMs; }
        public boolean isEnableDetailedMetrics() { return enableDetailedMetrics; }
        public boolean isEnableServiceHealthChecks() { return enableServiceHealthChecks; }
        
        // Static factory methods
        public static CriticalSeparationConfig conservative() {
            return new CriticalSeparationConfig(
                true,  // enableCriticalServiceSeparation
                true,  // enableBackgroundWarming
                false, // enableFailureRecovery
                2,     // maxBackgroundThreads
                2,     // criticalPhaseTimeoutMs (2ms)
                10_000, // backgroundPhaseTimeoutMs (10s)
                false, // enableDetailedMetrics
                false  // enableServiceHealthChecks
            );
        }
        
        public static CriticalSeparationConfig balanced() {
            return new CriticalSeparationConfig(
                true,  // enableCriticalServiceSeparation
                true,  // enableBackgroundWarming
                true,  // enableFailureRecovery
                4,     // maxBackgroundThreads
                2,     // criticalPhaseTimeoutMs
                30_000, // backgroundPhaseTimeoutMs (30s)
                true,  // enableDetailedMetrics
                true   // enableServiceHealthChecks
            );
        }
        
        public static CriticalSeparationConfig aggressive() {
            return new CriticalSeparationConfig(
                true,  // enableCriticalServiceSeparation
                true,  // enableBackgroundWarming
                true,  // enableFailureRecovery
                Runtime.getRuntime().availableProcessors(),
                2,     // criticalPhaseTimeoutMs
                60_000, // backgroundPhaseTimeoutMs (60s)
                true,  // enableDetailedMetrics
                true   // enableServiceHealthChecks
            );
        }
    }
    
    /**
     * 📊 RESULTADO del sistema de separación crítica
     */
    public static class CriticalSeparationResult {
        private final boolean success;
        private final long criticalPhaseDurationMs;
        private final long backgroundPhaseDurationMs;
        private final int criticalServicesLoaded;
        private final int nonCriticalServicesLoaded;
        private final int criticalServicesFailed;
        private final int nonCriticalServicesFailed;
        private final ServiceState overallApplicationState;
        private final CriticalSeparationMetrics metrics;
        private final Map<String, ServiceInfo> allServices;
        private final Exception error;
        
        public CriticalSeparationResult(boolean success,
                                      long criticalPhaseDurationMs,
                                      long backgroundPhaseDurationMs,
                                      int criticalServicesLoaded,
                                      int nonCriticalServicesLoaded,
                                      int criticalServicesFailed,
                                      int nonCriticalServicesFailed,
                                      ServiceState overallApplicationState,
                                      CriticalSeparationMetrics metrics,
                                      Map<String, ServiceInfo> allServices,
                                      Exception error) {
            this.success = success;
            this.criticalPhaseDurationMs = criticalPhaseDurationMs;
            this.backgroundPhaseDurationMs = backgroundPhaseDurationMs;
            this.criticalServicesLoaded = criticalServicesLoaded;
            this.nonCriticalServicesLoaded = nonCriticalServicesLoaded;
            this.criticalServicesFailed = criticalServicesFailed;
            this.nonCriticalServicesFailed = nonCriticalServicesFailed;
            this.overallApplicationState = overallApplicationState;
            this.metrics = metrics;
            this.allServices = allServices;
            this.error = error;
        }
        
        // Getters
        public boolean isSuccess() { return success; }
        public long getCriticalPhaseDurationMs() { return criticalPhaseDurationMs; }
        public long getBackgroundPhaseDurationMs() { return backgroundPhaseDurationMs; }
        public int getCriticalServicesLoaded() { return criticalServicesLoaded; }
        public int getNonCriticalServicesLoaded() { return nonCriticalServicesLoaded; }
        public int getCriticalServicesFailed() { return criticalServicesFailed; }
        public int getNonCriticalServicesFailed() { return nonCriticalServicesFailed; }
        public ServiceState getOverallApplicationState() { return overallApplicationState; }
        public CriticalSeparationMetrics getMetrics() { return metrics; }
        public Map<String, ServiceInfo> getAllServices() { return allServices; }
        public Exception getError() { return error; }
        
        // Utility methods
        public int getTotalServicesLoaded() {
            return criticalServicesLoaded + nonCriticalServicesLoaded;
        }
        
        public int getTotalServicesFailed() {
            return criticalServicesFailed + nonCriticalServicesFailed;
        }
        
        public double getSuccessRate() {
            int total = getTotalServicesLoaded() + getTotalServicesFailed();
            if (total == 0) return 1.0;
            return (double) getTotalServicesLoaded() / total;
        }
        
        public boolean isApplicationUsable() {
            return success && criticalServicesFailed == 0 && overallApplicationState != ServiceState.FAILED;
        }
        
        public boolean isApplicationFullyReady() {
            return success && criticalServicesFailed == 0 && nonCriticalServicesFailed == 0 && 
                   overallApplicationState == ServiceState.READY;
        }
        
        public String generateReport() {
            StringBuilder sb = new StringBuilder();
            sb.append("\n=== CRITICAL SERVICE SEPARATION RESULT ===\n");
            sb.append(String.format("Success: %s\n", success));
            sb.append(String.format("Application Usable: %s\n", isApplicationUsable()));
            sb.append(String.format("Application Fully Ready: %s\n", isApplicationFullyReady()));
            sb.append(String.format("Overall State: %s\n", overallApplicationState.getDescription()));
            sb.append(String.format("Critical Phase Duration: %dms\n", criticalPhaseDurationMs));
            sb.append(String.format("Background Phase Duration: %dms\n", backgroundPhaseDurationMs));
            sb.append(String.format("Critical Services Loaded: %d\n", criticalServicesLoaded));
            sb.append(String.format("Non-Critical Services Loaded: %d\n", nonCriticalServicesLoaded));
            sb.append(String.format("Critical Services Failed: %d\n", criticalServicesFailed));
            sb.append(String.format("Non-Critical Services Failed: %d\n", nonCriticalServicesFailed));
            sb.append(String.format("Success Rate: %.2f%%\n", getSuccessRate() * 100));
            
            if (error != null) {
                sb.append(String.format("Error: %s\n", error.getMessage()));
            }
            
            if (metrics != null) {
                sb.append(metrics.generateSummaryReport());
            }
            
            return sb.toString();
        }
    }
}