package io.warmup.framework.hotreload.advanced;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

/**
 * Global metrics for hot reload system.
 * Tracks overall performance and statistics across all hot reload operations.
 * 
 * @author MiniMax Agent
 * @since 2.0
 */
public class GlobalMetrics {
    
    private final AtomicLong totalReloads = new AtomicLong(0);
    private final AtomicLong successfulReloads = new AtomicLong(0);
    private final AtomicLong failedReloads = new AtomicLong(0);
    private final AtomicLong totalReloadTime = new AtomicLong(0);
    private final Map<String, AtomicLong> operationCounts = new ConcurrentHashMap<>();
    private final Map<String, Long> lastOperationTimes = new ConcurrentHashMap<>();
    
    public GlobalMetrics() {
        // Initialize operation counters
        operationCounts.put("class_reloads", new AtomicLong(0));
        operationCounts.put("method_reloads", new AtomicLong(0));
        operationCounts.put("bytecode_optimizations", new AtomicLong(0));
        operationCounts.put("state_preservations", new AtomicLong(0));
    }
    
    /**
     * Records a reload operation.
     * 
     * @param success whether the reload was successful
     * @param durationMs the duration in milliseconds
     * @param operationType the type of operation
     */
    public void recordReload(boolean success, long durationMs, String operationType) {
        totalReloads.incrementAndGet();
        totalReloadTime.addAndGet(durationMs);
        
        if (success) {
            successfulReloads.incrementAndGet();
        } else {
            failedReloads.incrementAndGet();
        }
        
        operationCounts.computeIfAbsent(operationType, k -> new AtomicLong(0)).incrementAndGet();
        lastOperationTimes.put(operationType, System.currentTimeMillis());
    }
    
    /**
     * Gets total number of reloads.
     * 
     * @return total reloads
     */
    public long getTotalReloads() {
        return totalReloads.get();
    }
    
    /**
     * Gets number of successful reloads.
     * 
     * @return successful reloads
     */
    public long getSuccessfulReloads() {
        return successfulReloads.get();
    }
    
    /**
     * Gets number of failed reloads.
     * 
     * @return failed reloads
     */
    public long getFailedReloads() {
        return failedReloads.get();
    }
    
    /**
     * Gets success rate as percentage.
     * 
     * @return success rate (0-100)
     */
    public double getSuccessRate() {
        long total = totalReloads.get();
        if (total == 0) return 0.0;
        return (successfulReloads.get() * 100.0) / total;
    }
    
    /**
     * Gets average reload time in milliseconds.
     * 
     * @return average reload time
     */
    public long getAverageReloadTime() {
        long total = totalReloads.get();
        if (total == 0) return 0;
        return totalReloadTime.get() / total;
    }
    
    /**
     * Gets operation counts.
     * 
     * @return map of operation counts
     */
    public Map<String, Long> getOperationCounts() {
        return operationCounts.entrySet().stream()
                .collect(java.util.stream.Collectors.toMap(
                    Map.Entry::getKey,
                    entry -> entry.getValue().get()
                ));
    }
    
    /**
     * Gets last operation times.
     * 
     * @return map of last operation times
     */
    public Map<String, Long> getLastOperationTimes() {
        return new ConcurrentHashMap<>(lastOperationTimes);
    }
    
    /**
     * Gets overall performance score.
     * 
     * @return performance score (0-100)
     */
    public double getOverallPerformance() {
        double successRate = getSuccessRate();
        long avgTime = getAverageReloadTime();
        
        // Simple performance calculation
        // Higher score for higher success rate and lower average time
        double timeScore = Math.max(0, 100 - (avgTime / 10)); // Penalize slow operations
        
        return (successRate * 0.7) + (timeScore * 0.3);
    }
    
    /**
     * Resets all metrics.
     */
    public void reset() {
        totalReloads.set(0);
        successfulReloads.set(0);
        failedReloads.set(0);
        totalReloadTime.set(0);
        
        operationCounts.values().forEach(counter -> counter.set(0));
        lastOperationTimes.clear();
    }
    
    @Override
    public String toString() {
        return String.format(
            "GlobalMetrics{total=%d, successful=%d, failed=%d, successRate=%.1f%%, avgTime=%dms, performance=%.1f}",
            getTotalReloads(),
            getSuccessfulReloads(),
            getFailedReloads(),
            getSuccessRate(),
            getAverageReloadTime(),
            getOverallPerformance()
        );
    }
}