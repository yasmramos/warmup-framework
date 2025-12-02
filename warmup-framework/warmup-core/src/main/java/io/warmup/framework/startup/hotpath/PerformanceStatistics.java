package io.warmup.framework.startup.hotpath;

import java.util.concurrent.atomic.AtomicLong;

/**
 * Statistical information about performance metrics for hot path optimization.
 * Tracks various performance indicators and measurements.
 * 
 * @author MiniMax Agent
 * @version 1.0
 */
public class PerformanceStatistics {
    
    private final AtomicLong totalMethodCalls = new AtomicLong(0);
    private final AtomicLong totalExecutionTime = new AtomicLong(0);
    private final AtomicLong minExecutionTime = new AtomicLong(Long.MAX_VALUE);
    private final AtomicLong maxExecutionTime = new AtomicLong(0);
    private final AtomicLong totalOptimizations = new AtomicLong(0);
    private final AtomicLong successfulOptimizations = new AtomicLong(0);
    private final AtomicLong failedOptimizations = new AtomicLong(0);
    
    public PerformanceStatistics() {
        // Constructor for initialization
    }
    
    /**
     * Record a method call with its execution time
     */
    public void recordMethodCall(long executionTimeNanos) {
        totalMethodCalls.incrementAndGet();
        totalExecutionTime.addAndGet(executionTimeNanos);
        
        // Update min/max
        while (true) {
            long currentMin = minExecutionTime.get();
            if (executionTimeNanos >= currentMin) break;
            if (minExecutionTime.compareAndSet(currentMin, executionTimeNanos)) break;
        }
        
        while (true) {
            long currentMax = maxExecutionTime.get();
            if (executionTimeNanos <= currentMax) break;
            if (maxExecutionTime.compareAndSet(currentMax, executionTimeNanos)) break;
        }
    }
    
    /**
     * Record an optimization attempt (successful or failed)
     */
    public void recordOptimization(boolean successful) {
        totalOptimizations.incrementAndGet();
        if (successful) {
            successfulOptimizations.incrementAndGet();
        } else {
            failedOptimizations.incrementAndGet();
        }
    }
    
    // Getters
    public long getTotalMethodCalls() {
        return totalMethodCalls.get();
    }
    
    public long getTotalExecutionTimeNanos() {
        return totalExecutionTime.get();
    }
    
    public long getMinExecutionTimeNanos() {
        long min = minExecutionTime.get();
        return min == Long.MAX_VALUE ? 0 : min;
    }
    
    public long getMaxExecutionTimeNanos() {
        return maxExecutionTime.get();
    }
    
    public double getAverageExecutionTimeNanos() {
        long calls = totalMethodCalls.get();
        return calls > 0 ? (double) totalExecutionTime.get() / calls : 0.0;
    }
    
    public long getTotalOptimizations() {
        return totalOptimizations.get();
    }
    
    public long getSuccessfulOptimizations() {
        return successfulOptimizations.get();
    }
    
    public long getFailedOptimizations() {
        return failedOptimizations.get();
    }
    
    public double getOptimizationSuccessRate() {
        long total = totalOptimizations.get();
        return total > 0 ? (double) successfulOptimizations.get() / total : 0.0;
    }
    
    /**
     * Reset all statistics (useful for new measurement cycles)
     */
    public void reset() {
        totalMethodCalls.set(0);
        totalExecutionTime.set(0);
        minExecutionTime.set(Long.MAX_VALUE);
        maxExecutionTime.set(0);
        totalOptimizations.set(0);
        successfulOptimizations.set(0);
        failedOptimizations.set(0);
    }
}