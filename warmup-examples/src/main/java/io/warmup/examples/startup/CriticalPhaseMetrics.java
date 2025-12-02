package io.warmup.examples.startup;

/**
 * 📊 Métricas de la fase crítica de startup
 * 
 * @author MiniMax Agent
 * @version 1.0
 */
public class CriticalPhaseMetrics {
    
    private volatile long completionTimeMs;
    private volatile boolean completed = false;
    private volatile boolean error = false;
    private volatile String errorMessage;
    private volatile long errorTimeMs;
    
    private final long startTimestamp = System.nanoTime();
    
    public void recordPhaseCompletion(long durationMs) {
        this.completionTimeMs = durationMs;
        this.completed = true;
        this.error = false;
    }
    
    public void recordPhaseError(long durationMs, Exception error) {
        this.errorTimeMs = durationMs;
        this.completed = true;
        this.error = true;
        this.errorMessage = error.getMessage();
    }
    
    public long getCompletionTimeMs() {
        return completionTimeMs;
    }
    
    public boolean isCompleted() {
        return completed;
    }
    
    public boolean isError() {
        return error;
    }
    
    public String getErrorMessage() {
        return errorMessage;
    }
    
    public long getCurrentDurationMs() {
        return (System.nanoTime() - startTimestamp) / 1_000_000;
    }
    
    // Methods needed by ComprehensiveStartupResult
    public long getDurationMs() {
        return completionTimeMs;
    }
    
    public long getLastDurationMs() {
        return completionTimeMs;
    }
    
    public double getTargetEfficiency() {
        return 0.95; // Default target efficiency
    }
}