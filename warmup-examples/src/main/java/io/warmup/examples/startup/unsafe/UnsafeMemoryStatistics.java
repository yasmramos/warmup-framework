package io.warmup.examples.startup.unsafe;

/**
 * Statistics for unsafe memory operations
 */
public class UnsafeMemoryStatistics {
    private final long allocatedMemory;
    private final long freedMemory;
    private final long peakMemoryUsage;
    private final long allocationOperations;
    private final long freeOperations;
    
    public UnsafeMemoryStatistics(long allocatedMemory, long freedMemory, long peakMemoryUsage, 
                                long allocationOperations, long freeOperations) {
        this.allocatedMemory = allocatedMemory;
        this.freedMemory = freedMemory;
        this.peakMemoryUsage = peakMemoryUsage;
        this.allocationOperations = allocationOperations;
        this.freeOperations = freeOperations;
    }
    
    public long getAllocatedMemory() { return allocatedMemory; }
    public long getFreedMemory() { return freedMemory; }
    public long getPeakMemoryUsage() { return peakMemoryUsage; }
    public long getAllocationOperations() { return allocationOperations; }
    public long getFreeOperations() { return freeOperations; }
    public long getNetMemoryUsage() { return allocatedMemory - freedMemory; }
}