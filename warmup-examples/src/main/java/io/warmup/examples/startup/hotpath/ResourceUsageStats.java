package io.warmup.examples.startup.hotpath;

import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Statistics about resource usage during hot path optimization.
 * Tracks memory usage, CPU time, and other resource consumption metrics.
 * 
 * @author MiniMax Agent
 * @version 1.0
 */
public class ResourceUsageStats {
    
    private final AtomicLong totalMemoryAllocated = new AtomicLong(0);
    private final AtomicLong peakMemoryUsage = new AtomicLong(0);
    private final AtomicLong totalCpuTime = new AtomicLong(0);
    private final AtomicLong gcTime = new AtomicLong(0);
    private final AtomicLong allocationsCount = new AtomicLong(0);
    private final AtomicReference<String> currentPhase = new AtomicReference<>("INITIALIZATION");
    
    public ResourceUsageStats() {
        // Constructor for initialization
    }
    
    /**
     * Record memory allocation
     */
    public void recordMemoryAllocation(long bytes) {
        totalMemoryAllocated.addAndGet(bytes);
        allocationsCount.incrementAndGet();
        
        // Update peak memory usage
        while (true) {
            long currentPeak = peakMemoryUsage.get();
            if (bytes <= currentPeak) break;
            if (peakMemoryUsage.compareAndSet(currentPeak, bytes)) break;
        }
    }
    
    /**
     * Record CPU time consumption
     */
    public void recordCpuTime(long nanos) {
        totalCpuTime.addAndGet(nanos);
    }
    
    /**
     * Record garbage collection time
     */
    public void recordGcTime(long nanos) {
        gcTime.addAndGet(nanos);
    }
    
    /**
     * Set the current optimization phase
     */
    public void setCurrentPhase(String phase) {
        currentPhase.set(phase);
    }
    
    // Getters
    public long getTotalMemoryAllocated() {
        return totalMemoryAllocated.get();
    }
    
    public long getPeakMemoryUsage() {
        return peakMemoryUsage.get();
    }
    
    public long getTotalCpuTimeNanos() {
        return totalCpuTime.get();
    }
    
    public long getGcTimeNanos() {
        return gcTime.get();
    }
    
    public long getAllocationsCount() {
        return allocationsCount.get();
    }
    
    public String getCurrentPhase() {
        return currentPhase.get();
    }
    
    /**
     * Get average allocation size
     */
    public double getAverageAllocationSize() {
        long count = allocationsCount.get();
        return count > 0 ? (double) totalMemoryAllocated.get() / count : 0.0;
    }
    
    /**
     * Get GC overhead as percentage of total CPU time
     */
    public double getGcOverheadPercentage() {
        long cpuTime = totalCpuTime.get();
        return cpuTime > 0 ? ((double) gcTime.get() / cpuTime) * 100.0 : 0.0;
    }
    
    /**
     * Reset all statistics
     */
    public void reset() {
        totalMemoryAllocated.set(0);
        peakMemoryUsage.set(0);
        totalCpuTime.set(0);
        gcTime.set(0);
        allocationsCount.set(0);
        currentPhase.set("INITIALIZATION");
    }
}