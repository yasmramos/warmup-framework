package io.warmup.examples.startup;

import java.util.concurrent.atomic.AtomicLong;

/**
 * 📊 Métricas de la fase background de startup
 * 
 * @author MiniMax Agent
 * @version 1.0
 */
public class BackgroundPhaseMetrics {
    
    private final AtomicLong completionTimeMs = new AtomicLong(0);
    private volatile boolean completed = false;
    private volatile boolean error = false;
    private volatile String errorMessage;
    private final AtomicLong errorTimeMs = new AtomicLong(0);
    
    private final long startTimestamp = System.nanoTime();
    
    public void recordPhaseCompletion(long durationMs) {
        completionTimeMs.set(durationMs);
        completed = true;
        error = false;
    }
    
    public void recordPhaseError(long durationMs, Exception error) {
        errorTimeMs.set(durationMs);
        completed = true;
        this.error = true;
        this.errorMessage = error.getMessage();
    }
    
    public long getCompletionTimeMs() {
        return completionTimeMs.get();
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
    
    public long getErrorTimeMs() {
        return errorTimeMs.get();
    }
    
    // Methods needed by ComprehensiveStartupResult
    public long getDurationMs() {
        return completionTimeMs.get();
    }
    
    public long getLastDurationMs() {
        return completionTimeMs.get();
    }
    
    public double getTargetEfficiency() {
        return 0.90; // Default target efficiency for background phase
    }
}