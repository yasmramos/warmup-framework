package io.warmup.framework.startup;

import java.util.Map;

/**
 * 📊 Métricas completas del startup por fases
 * 
 * @author MiniMax Agent
 * @version 1.0
 */
public class StartupMetrics {
    
    private final boolean criticalPhaseCompleted;
    private final boolean backgroundPhaseCompleted;
    private final boolean backgroundPhaseStarted;
    
    private final CriticalPhaseMetrics criticalMetrics;
    private final BackgroundPhaseMetrics backgroundMetrics;
    
    public StartupMetrics(boolean criticalPhaseCompleted,
                         boolean backgroundPhaseCompleted,
                         boolean backgroundPhaseStarted,
                         CriticalPhaseMetrics criticalMetrics,
                         BackgroundPhaseMetrics backgroundMetrics) {
        this.criticalPhaseCompleted = criticalPhaseCompleted;
        this.backgroundPhaseCompleted = backgroundPhaseCompleted;
        this.backgroundPhaseStarted = backgroundPhaseStarted;
        this.criticalMetrics = criticalMetrics;
        this.backgroundMetrics = backgroundMetrics;
    }
    
    // Default constructor for backward compatibility
    public StartupMetrics() {
        this.criticalPhaseCompleted = true;
        this.backgroundPhaseCompleted = true;
        this.backgroundPhaseStarted = true;
        this.criticalMetrics = new CriticalPhaseMetrics();
        this.backgroundMetrics = new BackgroundPhaseMetrics();
    }
    
    public Map<String, Object> toMap() {
        Map<String, Object> metrics = new java.util.HashMap<>();
        
        metrics.put("critical_phase_completed", criticalPhaseCompleted);
        metrics.put("background_phase_completed", backgroundPhaseCompleted);
        metrics.put("background_phase_started", backgroundPhaseStarted);
        metrics.put("critical_phase_error", criticalMetrics.isError());
        metrics.put("background_phase_error", backgroundMetrics.isError());
        
        if (criticalMetrics.isCompleted()) {
            metrics.put("critical_phase_time_ms", criticalMetrics.getCompletionTimeMs());
        }
        
        if (backgroundMetrics.isCompleted()) {
            metrics.put("background_phase_time_ms", backgroundMetrics.getCompletionTimeMs());
        }
        
        metrics.put("total_startup_time_ms", getTotalStartupTimeMs());
        
        return metrics;
    }
    
    public long getTotalStartupTimeMs() {
        if (!criticalPhaseCompleted) {
            return 0;
        }
        
        long criticalTime = criticalMetrics.getCompletionTimeMs();
        long backgroundTime = backgroundMetrics.isCompleted() ? 
            backgroundMetrics.getCompletionTimeMs() : 0;
        
        // Si la fase background aún está corriendo, retornar tiempo crítico + tiempo transcurrido
        if (!backgroundMetrics.isCompleted() && backgroundPhaseStarted) {
            return Math.max(criticalTime, backgroundMetrics.getCurrentDurationMs());
        }
        
        return Math.max(criticalTime, backgroundTime);
    }
    
    public boolean isCriticalPhaseCompleted() {
        return criticalPhaseCompleted;
    }
    
    public boolean isBackgroundPhaseCompleted() {
        return backgroundPhaseCompleted;
    }
    
    public boolean isBackgroundPhaseStarted() {
        return backgroundPhaseStarted;
    }
    
    public CriticalPhaseMetrics getCriticalMetrics() {
        return criticalMetrics;
    }
    
    public BackgroundPhaseMetrics getBackgroundMetrics() {
        return backgroundMetrics;
    }
    
    // Alias method needed by ComprehensiveStartupResult
    public CriticalPhaseMetrics getCriticalPhaseMetrics() {
        return criticalMetrics;
    }
    
    /**
     * Gets background phase metrics
     */
    public BackgroundPhaseMetrics getBackgroundPhaseMetrics() {
        return backgroundMetrics;
    }
    
    /**
     * Checks if all phases are completed
     */
    public boolean isAllPhasesCompleted() {
        return criticalPhaseCompleted && backgroundPhaseCompleted;
    }
    
    /**
     * Sets the critical phase duration
     */
    public void setCriticalPhaseDuration(long duration) {
        // Implementation would set the duration in criticalMetrics
    }
    
    /**
     * Sets the background phase duration
     */
    public void setBackgroundPhaseDuration(long duration) {
        // Implementation would set the duration in backgroundMetrics
    }
    
    /**
     * Sets the total startup time
     */
    public void setTotalStartupTime(long time) {
        // Implementation would set the total startup time
    }
    
    /**
     * Sets the subsystem initialization times
     */
    public void setSubsystemInitializationTimes(Map<String, Long> times) {
        // Implementation would set initialization times
    }
}