package io.warmup.framework.core.optimized;

import io.warmup.framework.core.optimized.CoreContainer;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.Logger;
import java.util.Map;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 🚀 STATE MANAGER - Container lifecycle and state management
 * 
 * This manager handles:
 * - Container lifecycle state tracking
 * - Startup phase management
 * - Shutdown coordination
 * - Health state monitoring
 * - Performance state tracking
 * 
 * Features:
 * - Thread-safe state management
 * - Atomic operations for performance
 * - Comprehensive state tracking
 * - Graceful shutdown coordination
 * 
 * @author Warmup Framework
 * @version 2.0
 */
public class StateManager {
    
    private static final Logger log = Logger.getLogger(StateManager.class.getName());
    
    // ✅ CONTAINER STATE MANAGEMENT
    private final AtomicInteger containerState = new AtomicInteger(0); // 0=CREATED, 1=INITIALIZING, 2=READY, 3=SHUTTING_DOWN, 4=SHUTDOWN
    private final AtomicBoolean criticalPhaseCompleted = new AtomicBoolean(false);
    private final AtomicBoolean startupComplete = new AtomicBoolean(false);
    private final AtomicBoolean shutdownInitiated = new AtomicBoolean(false);
    private final AtomicBoolean healthChecksRegistered = new AtomicBoolean(false);
    private final AtomicBoolean methodInterceptorsRegistered = new AtomicBoolean(false);
    
    // ✅ PERFORMANCE STATE TRACKING
    private final AtomicLong totalOperations = new AtomicLong(0);
    private final AtomicLong successfulOperations = new AtomicLong(0);
    private final AtomicLong failedOperations = new AtomicLong(0);
    private final AtomicLong peakMemoryUsage = new AtomicLong(0);
    private final AtomicLong averageResponseTime = new AtomicLong(0);
    
    // ✅ STARTUP PHASES STATE
    private final AtomicLong criticalPhaseStartTime = new AtomicLong(0);
    private final AtomicLong criticalPhaseEndTime = new AtomicLong(0);
    private final AtomicLong backgroundPhaseStartTime = new AtomicLong(0);
    private final AtomicLong backgroundPhaseEndTime = new AtomicLong(0);
    private final AtomicLong totalStartupTime = new AtomicLong(0);
    
    // ✅ DEPENDENCIES
    private final CoreContainer coreContainer;
    private final StartupPhasesManager startupManager;
    
    // ✅ STATE METRICS
    private final Map<String, Object> stateMetrics = new ConcurrentHashMap<>();
    
    public StateManager(CoreContainer coreContainer, StartupPhasesManager startupManager) {
        this.coreContainer = coreContainer;
        this.startupManager = startupManager;
        
        // Initialize state
        containerState.set(0); // CREATED
        
        log.info("🚀 StateManager initialized");
    }
    
    // === STATE TRANSITION METHODS ===
    
    /**
     * 🚀 Transition to INITIALIZING state
     */
    public boolean transitionToInitializing() {
        return containerState.compareAndSet(0, 1);
    }
    
    /**
     * 🚀 Transition to READY state
     */
    public boolean transitionToReady() {
        boolean success = containerState.compareAndSet(1, 2);
        if (success) {
            startupComplete.set(true);
            totalStartupTime.set(System.currentTimeMillis() - criticalPhaseStartTime.get());
            recordStateChange("READY");
            log.info("✅ Container transitioned to READY state");
        }
        return success;
    }
    
    /**
     * 🚀 Transition to SHUTTING_DOWN state
     */
    public boolean transitionToShuttingDown() {
        boolean success = containerState.compareAndSet(2, 3);
        if (success) {
            shutdownInitiated.set(true);
            recordStateChange("SHUTTING_DOWN");
            log.info("🔄 Container transitioned to SHUTTING_DOWN state");
        }
        return success;
    }
    
    /**
     * 🚀 Transition to SHUTDOWN state
     */
    public boolean transitionToShutdown() {
        boolean success = containerState.compareAndSet(3, 4);
        if (success) {
            recordStateChange("SHUTDOWN");
            log.info("🛑 Container transitioned to SHUTDOWN state");
        }
        return success;
    }
    
    /**
     * 🚀 Check if container is in specific state
     */
    public boolean isInState(int state) {
        return containerState.get() == state;
    }
    
    /**
     * 🚀 Check if container is READY
     */
    public boolean isReady() {
        return containerState.get() == 2 && startupComplete.get();
    }
    
    /**
     * 🚀 Check if container is SHUTDOWN
     */
    public boolean isShutdown() {
        return containerState.get() == 4;
    }
    
    /**
     * 🚀 Check if shutdown has been initiated
     */
    public boolean isShutdownInitiated() {
        return shutdownInitiated.get();
    }
    
    // === STARTUP PHASE MANAGEMENT ===
    
    /**
     * 🚀 Mark critical phase as started
     */
    public void markCriticalPhaseStarted() {
        criticalPhaseStartTime.set(System.currentTimeMillis());
        criticalPhaseCompleted.set(false);
        log.fine("🚀 Critical phase started");
    }
    
    /**
     * 🚀 Mark critical phase as completed
     */
    public void markCriticalPhaseCompleted() {
        criticalPhaseEndTime.set(System.currentTimeMillis());
        criticalPhaseCompleted.set(true);
        recordOperation("critical_phase_complete", true);
        log.fine("✅ Critical phase completed");
    }
    
    /**
     * 🚀 Mark background phase as started
     */
    public void markBackgroundPhaseStarted() {
        backgroundPhaseStartTime.set(System.currentTimeMillis());
        log.fine("🚀 Background phase started");
    }
    
    /**
     * 🚀 Mark background phase as completed
     */
    public void markBackgroundPhaseCompleted() {
        backgroundPhaseEndTime.set(System.currentTimeMillis());
        recordOperation("background_phase_complete", true);
        log.fine("✅ Background phase completed");
    }
    
    // === PERFORMANCE TRACKING ===
    
    /**
     * 🚀 Record successful operation
     */
    public void recordOperation(String operationType, boolean success) {
        totalOperations.incrementAndGet();
        
        if (success) {
            successfulOperations.incrementAndGet();
        } else {
            failedOperations.incrementAndGet();
        }
        
        // Update state metrics
        stateMetrics.put("lastOperation", operationType);
        stateMetrics.put("lastOperationTime", System.currentTimeMillis());
        stateMetrics.put("operationSuccess", success);
    }
    
    /**
     * 🚀 Record memory usage
     */
    public void recordMemoryUsage(long usedMemory, long maxMemory) {
        if (usedMemory > peakMemoryUsage.get()) {
            peakMemoryUsage.set(usedMemory);
        }
        
        stateMetrics.put("currentMemoryUsage", usedMemory);
        stateMetrics.put("maxMemoryUsage", maxMemory);
        stateMetrics.put("memoryUsagePercent", (double) usedMemory / maxMemory * 100);
    }
    
    /**
     * 🚀 Record response time
     */
    public void recordResponseTime(long responseTimeNs) {
        // Update average response time (simple moving average)
        long currentAvg = averageResponseTime.get();
        long operations = totalOperations.get();
        long newAvg = (currentAvg * (operations - 1) + responseTimeNs) / operations;
        averageResponseTime.set(newAvg);
        
        stateMetrics.put("lastResponseTime", responseTimeNs);
        stateMetrics.put("averageResponseTime", newAvg);
    }
    
    // === HEALTH STATE MANAGEMENT ===
    
    /**
     * 🚀 Mark health checks as registered
     */
    public void markHealthChecksRegistered() {
        healthChecksRegistered.set(true);
        recordOperation("health_checks_registered", true);
        log.fine("✅ Health checks registered");
    }
    
    /**
     * 🚀 Mark method interceptors as registered
     */
    public void markMethodInterceptorsRegistered() {
        methodInterceptorsRegistered.set(true);
        recordOperation("method_interceptors_registered", true);
        log.fine("✅ Method interceptors registered");
    }
    
    // === STATE METRICS ===
    
    /**
     * 🚀 Get comprehensive state metrics
     */
    public Map<String, Object> getStateMetrics() {
        Map<String, Object> metrics = new ConcurrentHashMap<>(stateMetrics);
        
        // Add state information
        metrics.put("containerState", getStateName(containerState.get()));
        metrics.put("isReady", isReady());
        metrics.put("isShutdown", isShutdown());
        metrics.put("criticalPhaseCompleted", criticalPhaseCompleted.get());
        metrics.put("startupComplete", startupComplete.get());
        metrics.put("shutdownInitiated", shutdownInitiated.get());
        metrics.put("healthChecksRegistered", healthChecksRegistered.get());
        metrics.put("methodInterceptorsRegistered", methodInterceptorsRegistered.get());
        
        // Add performance metrics
        metrics.put("totalOperations", totalOperations.get());
        metrics.put("successfulOperations", successfulOperations.get());
        metrics.put("failedOperations", failedOperations.get());
        metrics.put("operationSuccessRate", calculateSuccessRate());
        metrics.put("peakMemoryUsage", peakMemoryUsage.get());
        metrics.put("averageResponseTime", averageResponseTime.get());
        
        // Add startup timing
        metrics.put("criticalPhaseDuration", getCriticalPhaseDuration());
        metrics.put("backgroundPhaseDuration", getBackgroundPhaseDuration());
        metrics.put("totalStartupDuration", totalStartupTime.get());
        
        return metrics;
    }
    
    /**
     * 🚀 Get state name from state value
     */
    private String getStateName(int state) {
        switch (state) {
            case 0: return "CREATED";
            case 1: return "INITIALIZING";
            case 2: return "READY";
            case 3: return "SHUTTING_DOWN";
            case 4: return "SHUTDOWN";
            default: return "UNKNOWN";
        }
    }
    
    /**
     * 🚀 Calculate operation success rate
     */
    private double calculateSuccessRate() {
        long total = totalOperations.get();
        if (total == 0) {
            return 100.0;
        }
        return (double) successfulOperations.get() / total * 100.0;
    }
    
    /**
     * 🚀 Get critical phase duration
     */
    private long getCriticalPhaseDuration() {
        long start = criticalPhaseStartTime.get();
        long end = criticalPhaseEndTime.get();
        return start > 0 && end > 0 ? end - start : 0;
    }
    
    /**
     * 🚀 Get background phase duration
     */
    private long getBackgroundPhaseDuration() {
        long start = backgroundPhaseStartTime.get();
        long end = backgroundPhaseEndTime.get();
        return start > 0 && end > 0 ? end - start : 0;
    }
    
    /**
     * 🚀 Record state change for monitoring
     */
    private void recordStateChange(String newState) {
        stateMetrics.put("lastStateChange", newState);
        stateMetrics.put("lastStateChangeTime", System.currentTimeMillis());
        stateMetrics.put("totalStateChanges", 
                        (Integer) stateMetrics.getOrDefault("totalStateChanges", 0) + 1);
    }
    
    // === HEALTH CHECKS ===
    
    /**
     * 🚀 Check if container state is healthy
     */
    public boolean isHealthy() {
        return isReady() && 
               !shutdownInitiated.get() && 
               !isShutdown() &&
               calculateSuccessRate() >= 95.0; // 95% success rate threshold
    }
    
    /**
     * 🚀 Get container readiness score
     */
    public double getReadinessScore() {
        double score = 0.0;
        
        // State readiness (40%)
        if (isReady()) score += 0.4;
        else if (containerState.get() == 1) score += 0.2; // Initializing
        
        // Critical phase completion (30%)
        if (criticalPhaseCompleted.get()) score += 0.3;
        
        // Health checks registered (15%)
        if (healthChecksRegistered.get()) score += 0.15;
        
        // Method interceptors registered (15%)
        if (methodInterceptorsRegistered.get()) score += 0.15;
        
        return Math.min(score, 1.0);
    }
    
    /**
     * 🚀 Get detailed health report
     */
    public Map<String, Object> getHealthReport() {
        Map<String, Object> report = new ConcurrentHashMap<>();
        
        report.put("overallHealth", isHealthy());
        report.put("readinessScore", getReadinessScore());
        report.put("containerState", getStateName(containerState.get()));
        report.put("criticalPhaseCompleted", criticalPhaseCompleted.get());
        report.put("startupComplete", startupComplete.get());
        report.put("operationSuccessRate", calculateSuccessRate());
        report.put("memoryUsagePeak", peakMemoryUsage.get());
        
        // State transitions
        report.put("totalOperations", totalOperations.get());
        report.put("successfulOperations", successfulOperations.get());
        report.put("failedOperations", failedOperations.get());
        
        // Performance
        report.put("averageResponseTimeNs", averageResponseTime.get());
        report.put("totalStartupTimeMs", totalStartupTime.get());
        
        return report;
    }
    
    // === CLEANUP ===
    
    /**
     * 🚀 Reset state (for testing)
     */
    public void resetState() {
        containerState.set(0);
        criticalPhaseCompleted.set(false);
        startupComplete.set(false);
        shutdownInitiated.set(false);
        healthChecksRegistered.set(false);
        methodInterceptorsRegistered.set(false);
        
        totalOperations.set(0);
        successfulOperations.set(0);
        failedOperations.set(0);
        peakMemoryUsage.set(0);
        averageResponseTime.set(0);
        
        criticalPhaseStartTime.set(0);
        criticalPhaseEndTime.set(0);
        backgroundPhaseStartTime.set(0);
        backgroundPhaseEndTime.set(0);
        totalStartupTime.set(0);
        
        stateMetrics.clear();
        
        log.info("🧹 StateManager reset to initial state");
    }
    
    /**
     * 🚀 Get current state value (for testing)
     */
    public int getCurrentState() {
        return containerState.get();
    }
    
    // ============ MÉTODOS FALTANTES PARA MAVEN COMPILATION ============
    
    /**
     * Get start time for health checks and metrics
     */
    public long getStartTime() {
        // Return current time as start time (would need proper initialization in real implementation)
        return System.currentTimeMillis() - totalStartupTime.get();
    }
    
    /**
     * Get uptime in milliseconds
     */
    public long getUptime() {
        return System.currentTimeMillis() - getStartTime();
    }
    
    /**
     * Get health status as map
     */
    public Map<String, Object> getHealthStatus() {
        Map<String, Object> status = new HashMap<>();
        status.put("state", containerState.get());
        status.put("healthy", isHealthy());
        status.put("uptime", getUptime());
        status.put("timestamp", System.currentTimeMillis());
        return status;
    }
}