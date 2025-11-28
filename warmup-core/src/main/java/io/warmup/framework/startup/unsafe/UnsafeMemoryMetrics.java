/*
 * Warmup Framework - Ninth Optimization System
 * UnsafeMemoryMetrics.java
 * 
 * Ninth System: "Use Unsafe and direct memory for critical structures. 
 *                Eliminate garbage collector overhead in startup path"
 * 
 * Copyright (c) 2025 MiniMax Agent. All rights reserved.
 */

package io.warmup.framework.startup.unsafe;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.LongAdder;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.atomic.LongAdder;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;

/**
 * Ninth Optimization System: UnsafeMemoryMetrics
 * 
 * Provides comprehensive metrics collection and analysis for the
 * Unsafe Memory Management system.
 * 
 * Key Features:
 * - Real-time memory allocation tracking
 * - GC overhead measurement and elimination rates
 * - Performance analysis and trending
 * - Resource utilization metrics
 * - Executive summary reports
 * 
 * Metrics Categories:
 * 1. Memory Allocation Metrics
 * 2. GC Elimination Metrics  
 * 3. Performance Metrics
 * 4. Structure Usage Metrics
 * 5. System Health Metrics
 */
public class UnsafeMemoryMetrics {
    
    private static final Logger logger = Logger.getLogger(UnsafeMemoryMetrics.class.getName());
    
    /**
     * Memory allocation tracking
     */
    private static final AtomicLong totalDirectAllocations = new AtomicLong(0);
    private static final AtomicLong totalDirectMemoryAllocated = new AtomicLong(0);
    private static final AtomicLong totalDirectMemoryFreed = new AtomicLong(0);
    private static final AtomicLong poolAllocations = new AtomicLong(0);
    private static final AtomicLong directAllocations = new AtomicLong(0);
    
    /**
     * Performance metrics
     */
    private static final AtomicLong totalAllocationTime = new AtomicLong(0);
    private static final AtomicLong totalDeallocationTime = new AtomicLong(0);
    private static final AtomicLong totalReadOperations = new AtomicLong(0);
    private static final AtomicLong totalWriteOperations = new AtomicLong(0);
    
    /**
     * GC elimination metrics
     */
    private static final AtomicLong gcEventsRecorded = new AtomicLong(0);
    private static final AtomicLong gcTimeEliminated = new AtomicLong(0);
    private static final AtomicLong allocationsAvoided = new AtomicLong(0);
    private static final AtomicLong objectsReused = new AtomicLong(0);
    private static final AtomicLong memoryPreAllocated = new AtomicLong(0);
    
    /**
     * Structure usage metrics
     */
    private static final ConcurrentHashMap<String, LongAdder> structureCreations = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<String, LongAdder> structureDisposals = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<String, LongAdder> structureOperations = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<String, LongAdder> structureGrowths = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<String, LongAdder> structureResizes = new ConcurrentHashMap<>();
    
    /**
     * Pool metrics
     */
    private static final ConcurrentHashMap<String, LongAdder> poolCreations = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<String, LongAdder> poolOperations = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<String, LongAdder> poolDisposals = new ConcurrentHashMap<>();
    
    /**
     * String interning metrics
     */
    private static final LongAdder stringInternings = new LongAdder();
    private static final LongAdder stringCacheHits = new LongAdder();
    private static final LongAdder stringCacheMisses = new LongAdder();
    private static final AtomicLong stringMemoryUsed = new AtomicLong(0);
    
    /**
     * Configuration cache metrics
     */
    private static final ConcurrentHashMap<String, LongAdder> configCacheOperations = new ConcurrentHashMap<>();
    private static final AtomicLong configCacheHits = new AtomicLong(0);
    private static final AtomicLong configCacheMisses = new AtomicLong(0);
    
    /**
     * System startup metrics
     */
    private static final AtomicReference<SystemStartupMetrics> currentStartupMetrics = 
        new AtomicReference<>(new SystemStartupMetrics());
    
    /**
     * Performance snapshots for trending analysis
     */
    private static final ConcurrentHashMap<Long, UnsafeMemorySnapshot> performanceSnapshots = 
        new ConcurrentHashMap<>();
    private static final AtomicLong snapshotCounter = new AtomicLong(0);
    
    /**
     * Record memory allocation
     */
    public static void recordAllocation(long size, String allocationType) {
        totalDirectAllocations.incrementAndGet();
        totalDirectMemoryAllocated.addAndGet(size);
        
        if ("DIRECT".equals(allocationType)) {
            directAllocations.incrementAndGet();
        } else if (allocationType.startsWith("POOL_")) {
            poolAllocations.incrementAndGet();
        }
        
        logger.finest("Recorded allocation: " + size + " bytes, type: " + allocationType);
    }
    
    /**
     * Record memory deallocation
     */
    public static void recordFree(long size, String allocationType) {
        totalDirectMemoryFreed.addAndGet(size);
        logger.finest("Recorded deallocation: " + size + " bytes, type: " + allocationType);
    }
    
    /**
     * Record operation timing
     */
    public static void recordAllocationTime(long timeNanos) {
        totalAllocationTime.addAndGet(timeNanos);
    }
    
    public static void recordDeallocationTime(long timeNanos) {
        totalDeallocationTime.addAndGet(timeNanos);
    }
    
    public static void recordReadOperation() {
        totalReadOperations.incrementAndGet();
    }
    
    public static void recordWriteOperation() {
        totalWriteOperations.incrementAndGet();
    }
    
    /**
     * Record GC events
     */
    public static void recordGCEvent() {
        gcEventsRecorded.incrementAndGet();
    }
    
    public static void recordGCEvent(String gcName, long duration) {
        gcEventsRecorded.incrementAndGet();
        // Store GC event details for analysis
        String key = "GC_" + gcName;
        structureOperations.computeIfAbsent(key, k -> new LongAdder()).add(1);
    }
    
    /**
     * Record structure lifecycle events
     */
    public static void recordStructureCreation(String structureType, long memorySize, String details) {
        structureCreations.computeIfAbsent(structureType, k -> new LongAdder()).increment();
        logger.fine("Structure created: " + structureType + " (" + memorySize + " bytes) - " + details);
    }
    
    public static void recordStructureDisposal(String structureType, long memorySize) {
        structureDisposals.computeIfAbsent(structureType, k -> new LongAdder()).increment();
        logger.fine("Structure disposed: " + structureType + " (" + memorySize + " bytes)");
    }
    
    public static void recordStructureOperation(String structureType, String operation) {
        String key = structureType + "_" + operation;
        structureOperations.computeIfAbsent(key, k -> new LongAdder()).increment();
        logger.finest("Structure operation: " + operation + " on " + structureType);
    }
    
    public static void recordGrowth(String structureType, int newCapacity) {
        structureGrowths.computeIfAbsent(structureType, k -> new LongAdder()).increment();
        logger.fine("Structure grown: " + structureType + " to capacity " + newCapacity);
    }
    
    public static void recordResize(String structureType, int newCapacity) {
        structureResizes.computeIfAbsent(structureType, k -> new LongAdder()).increment();
        logger.fine("Structure resized: " + structureType + " to capacity " + newCapacity);
    }
    
    /**
     * Record pool events
     */
    public static void recordPoolCreation(String poolType, int initialSize) {
        poolCreations.computeIfAbsent(poolType, k -> new LongAdder()).increment();
        logger.fine("Pool created: " + poolType + " with size " + initialSize);
    }
    
    public static void recordPoolOperation(String operation, String poolType, boolean reused) {
        String key = operation + "_" + poolType;
        poolOperations.computeIfAbsent(key, k -> new LongAdder()).increment();
        objectsReused.addAndGet(reused ? 1 : 0);
        allocationsAvoided.addAndGet(reused ? 1 : 0);
    }
    
    public static void recordPoolDisposal(String poolType, int createdObjects, int reusedObjects) {
        poolDisposals.computeIfAbsent(poolType, k -> new LongAdder()).increment();
        logger.fine("Pool disposed: " + poolType + " (created: " + createdObjects + ", reused: " + reusedObjects + ")");
    }
    
    /**
     * Record string interning metrics
     */
    public static void recordStringInterning(int stringLength, boolean cacheHit) {
        stringInternings.increment();
        stringCacheHits.add(cacheHit ? 1 : 0);
        stringCacheMisses.add(cacheHit ? 0 : 1);
        stringMemoryUsed.addAndGet(stringLength * 2L); // 2 bytes per char in UTF-16
    }
    
    public static void recordStringTableDisposal(long totalStrings, long memoryUsed) {
        logger.fine("String table disposed: " + totalStrings + " strings, " + memoryUsed + " bytes");
    }
    
    /**
     * Record configuration cache metrics
     */
    public static void recordConfigCacheOperation(String operation, String key) {
        configCacheOperations.computeIfAbsent(operation + "_" + key, k -> new LongAdder()).increment();
    }
    
    public static void recordConfigCacheDisposal(long hits, long misses, int size) {
        configCacheHits.addAndGet(hits);
        configCacheMisses.addAndGet(misses);
        logger.fine("Config cache disposed: hits=" + hits + ", misses=" + misses + ", size=" + size);
    }
    
    /**
     * Record pre-allocation events
     */
    public static void recordPreAllocation(long totalSize, long smallPool, long mediumPool, long largePool) {
        memoryPreAllocated.addAndGet(totalSize);
        logger.info("Pre-allocation completed: total=" + totalSize + 
                   " (small=" + smallPool + ", medium=" + mediumPool + ", large=" + largePool + ")");
    }
    
    /**
     * Record system shutdown
     */
    public static void recordShutdown() {
        UnsafeMemoryStatistics finalMetrics = generateSystemSnapshot();
        logger.info("System metrics at shutdown: " + finalMetrics.toString());
    }
    
    /**
     * Generate current system metrics
     */
    public static UnsafeMemoryMetrics.UnsafeMemoryStatistics generateSystemSnapshot() {
        long currentTime = System.currentTimeMillis();
        return new UnsafeMemoryStatistics(
            totalDirectMemoryAllocated.get(),
            totalDirectMemoryFreed.get(),
            totalDirectMemoryAllocated.get() - totalDirectMemoryFreed.get(),
            totalDirectAllocations.get(),
            structureCreations.size() + poolCreations.size(),
            poolAllocations.get() + directAllocations.get(),
            currentTime
        );
    }
    
    /**
     * Generate performance snapshot for trending
     */
    public static void generatePerformanceSnapshot() {
        long snapshotId = snapshotCounter.incrementAndGet();
        UnsafeMemorySnapshot snapshot = new UnsafeMemorySnapshot(
            snapshotId,
            System.nanoTime(),
            generateSystemSnapshot(),
            getCurrentGCStats(),
            getStructureStats(),
            getPoolStats()
        );
        
        performanceSnapshots.put(snapshotId, snapshot);
        
        // Keep only last 100 snapshots to prevent memory leaks
        if (performanceSnapshots.size() > 100) {
            long oldestId = snapshotId - 100;
            performanceSnapshots.remove(oldestId);
        }
        
        logger.finest("Generated performance snapshot #" + snapshotId);
    }
    
    /**
     * Get detailed metrics report
     */
    public static io.warmup.framework.startup.unsafe.UnsafeMemoryReport generateDetailedReport() {
        // Create summary from system snapshot
        String systemSnapshot = String.format("Memory: %d MB, GC Rate: %.2f%%, Performance: %d ops/sec",
            generateSystemSnapshot().getTotalAllocated() / 1024 / 1024,
            getCurrentGCStats().getEliminationRate(),
            getPerformanceMetrics().getReadOperations() + getPerformanceMetrics().getWriteOperations());
        
        // Convert internal statistics to public statistics
        UnsafeMemoryStatistics internalStats = generateSystemSnapshot();
        io.warmup.framework.startup.unsafe.UnsafeMemoryStatistics publicStats = 
            new io.warmup.framework.startup.unsafe.UnsafeMemoryStatistics(
                internalStats.getTotalAllocated(),
                internalStats.getTotalFreed(),
                internalStats.getNetUsage(),
                internalStats.getActiveAllocations(),
                internalStats.getTotalOperations()
            );
        
        return new io.warmup.framework.startup.unsafe.UnsafeMemoryReport(
            "DETAILED_REPORT_" + System.currentTimeMillis(),
            publicStats, // Use converted public statistics
            System.currentTimeMillis(),
            systemSnapshot
        );
    }
    
    /**
     * Get executive summary report
     */
    public static ExecutiveSummary generateExecutiveSummary() {
        UnsafeMemoryStatistics internalStats = generateSystemSnapshot();
        io.warmup.framework.startup.unsafe.UnsafeMemoryStatistics stats = 
            new io.warmup.framework.startup.unsafe.UnsafeMemoryStatistics(
                internalStats.getTotalAllocated(),
                internalStats.getTotalFreed(),
                internalStats.getNetUsage(),
                internalStats.getActiveAllocations(),
                internalStats.getTotalOperations()
            );
        GCEliminationMetrics gcMetrics = getCurrentGCStats();
        
        double gcEliminationRate = gcMetrics.getEliminationRate();
        long totalMemoryManaged = stats.getAllocatedMemory();
        long netMemoryUsage = stats.getNetMemoryUsage();
        
        return new ExecutiveSummary(
            totalMemoryManaged,
            netMemoryUsage,
            gcEliminationRate,
            stats.getAllocationOperations(),
            structureCreations.size(),
            poolOperations.values().stream().mapToLong(LongAdder::longValue).sum(),
            getAverageOperationTime(),
            getSystemEfficiency()
        );
    }
    
    /**
     * Get current GC statistics
     */
    private static GCEliminationMetrics getCurrentGCStats() {
        return new GCEliminationMetrics(
            gcEventsRecorded.get(),
            gcTimeEliminated.get(),
            allocationsAvoided.get(),
            objectsReused.get(),
            memoryPreAllocated.get(),
            calculateGCEliminationRate()
        );
    }
    
    /**
     * Get structure usage statistics
     */
    private static StructureUsageMetrics getStructureStats() {
        return new StructureUsageMetrics(
            structureCreations,
            structureDisposals,
            structureOperations,
            structureGrowths,
            structureResizes
        );
    }
    
    /**
     * Get pool statistics
     */
    private static PoolMetrics getPoolStats() {
        return new PoolMetrics(
            poolCreations,
            poolOperations,
            poolDisposals
        );
    }
    
    /**
     * Calculate GC elimination rate
     */
    private static double calculateGCEliminationRate() {
        long eliminatedTime = gcTimeEliminated.get();
        long totalGCtime = getTotalGCtime();
        
        if (totalGCtime == 0) {
            return 100.0; // Perfect elimination if no GC occurred
        }
        
        return (double) eliminatedTime / totalGCtime * 100.0;
    }
    
    private static long getTotalGCtime() {
        // This would typically be obtained from ManagementFactory
        // For simplicity, using our recorded elimination
        return gcEventsRecorded.get() * 10; // Estimate 10ms average per GC
    }
    
    /**
     * Get performance metrics
     */
    private static PerformanceMetrics getPerformanceMetrics() {
        return new PerformanceMetrics(
            totalAllocationTime.get(),
            totalDeallocationTime.get(),
            totalReadOperations.get(),
            totalWriteOperations.get(),
            getAverageOperationTime()
        );
    }
    
    private static long getAverageOperationTime() {
        long totalOps = totalReadOperations.get() + totalWriteOperations.get();
        if (totalOps == 0) return 0;
        
        long totalTime = totalAllocationTime.get() + totalDeallocationTime.get();
        return totalTime / totalOps;
    }
    
    /**
     * Get pool metrics
     */
    private static PoolMetrics getPoolMetrics() {
        return new PoolMetrics(
            poolCreations,
            poolOperations,
            poolDisposals
        );
    }
    
    /**
     * Get string interning metrics
     */
    private static StringInterningMetrics getStringInterningMetrics() {
        return new StringInterningMetrics(
            stringInternings.sum(),
            stringCacheHits.sum(),
            stringCacheMisses.sum(),
            stringMemoryUsed.get(),
            getStringHitRate()
        );
    }
    
    private static double getStringHitRate() {
        long total = stringCacheHits.sum() + stringCacheMisses.sum();
        return total > 0 ? (double) stringCacheHits.sum() / total : 0.0;
    }
    
    /**
     * Get configuration cache metrics
     */
    private static ConfigCacheMetrics getConfigCacheMetrics() {
        return new ConfigCacheMetrics(
            configCacheHits.get(),
            configCacheMisses.get(),
            configCacheOperations.size(),
            getConfigHitRate()
        );
    }
    
    private static double getConfigHitRate() {
        long total = configCacheHits.get() + configCacheMisses.get();
        return total > 0 ? (double) configCacheHits.get() / total : 0.0;
    }
    
    /**
     * Get trend analysis
     */
    private static TrendAnalysis getTrendAnalysis() {
        return new TrendAnalysis(
            performanceSnapshots.values().stream()
                .limit(10)
                .collect(java.util.stream.Collectors.toList())
        );
    }
    
    /**
     * Calculate overall system efficiency
     */
    private static double getSystemEfficiency() {
        double gcEliminationRate = calculateGCEliminationRate();
        double memoryEfficiency = calculateMemoryEfficiency();
        double performanceScore = calculatePerformanceScore();
        
        return (gcEliminationRate + memoryEfficiency + performanceScore) / 3.0;
    }
    
    private static double calculateMemoryEfficiency() {
        long allocated = totalDirectMemoryAllocated.get();
        long freed = totalDirectMemoryFreed.get();
        long net = allocated - freed;
        
        if (allocated == 0) return 100.0;
        return (double) freed / allocated * 100.0;
    }
    
    private static double calculatePerformanceScore() {
        long totalOps = totalReadOperations.get() + totalWriteOperations.get();
        if (totalOps == 0) return 100.0;
        
        long avgOpTime = getAverageOperationTime();
        // Score based on operation speed (lower time = higher score)
        return Math.max(0, 100 - (avgOpTime / 1000000)); // Convert to ms, score decreases as time increases
    }
    
    /**
     * System startup metrics tracking
     */
    private static class SystemStartupMetrics {
        private final long startupTime;
        private final AtomicInteger startupPhase = new AtomicInteger(0);
        
        public SystemStartupMetrics() {
            this.startupTime = System.currentTimeMillis();
        }
        
        public long getStartupTime() { return startupTime; }
        public int getStartupPhase() { return startupPhase.get(); }
        public void incrementPhase() { startupPhase.incrementAndGet(); }
    }
    
    // Metric classes for structured data
    public static class UnsafeMemoryStatistics {
        private final long totalAllocated;
        private final long totalFreed;
        private final long netUsage;
        private final long activeAllocations;
        private final long totalStructures;
        private final long totalOperations;
        private final long timestamp;
        
        public UnsafeMemoryStatistics(long totalAllocated, long totalFreed, long netUsage,
                                    long activeAllocations, long totalStructures, long totalOperations,
                                    long timestamp) {
            this.totalAllocated = totalAllocated;
            this.totalFreed = totalFreed;
            this.netUsage = netUsage;
            this.activeAllocations = activeAllocations;
            this.totalStructures = totalStructures;
            this.totalOperations = totalOperations;
            this.timestamp = timestamp;
        }
        
        public long getTotalAllocated() { return totalAllocated; }
        public long getTotalFreed() { return totalFreed; }
        public long getNetUsage() { return netUsage; }
        public long getActiveAllocations() { return activeAllocations; }
        public long getTotalStructures() { return totalStructures; }
        public long getTotalOperations() { return totalOperations; }
        public long getTimestamp() { return timestamp; }
        
        @Override
        public String toString() {
            return String.format(
                "UnsafeMemoryStatistics{allocated=%d bytes, freed=%d bytes, net=%d bytes, allocations=%d, structures=%d, operations=%d}",
                totalAllocated, totalFreed, netUsage, activeAllocations, totalStructures, totalOperations
            );
        }
    }
    
    public static class GCEliminationMetrics {
        private final long gcEvents;
        private final long gcTimeEliminated;
        private final long allocationsAvoided;
        private final long objectsReused;
        private final long memoryPreAllocated;
        private final double eliminationRate;
        
        public GCEliminationMetrics(long gcEvents, long gcTimeEliminated, long allocationsAvoided,
                                  long objectsReused, long memoryPreAllocated, double eliminationRate) {
            this.gcEvents = gcEvents;
            this.gcTimeEliminated = gcTimeEliminated;
            this.allocationsAvoided = allocationsAvoided;
            this.objectsReused = objectsReused;
            this.memoryPreAllocated = memoryPreAllocated;
            this.eliminationRate = eliminationRate;
        }
        
        public long getGcEvents() { return gcEvents; }
        public long getGcTimeEliminated() { return gcTimeEliminated; }
        public long getAllocationsAvoided() { return allocationsAvoided; }
        public long getObjectsReused() { return objectsReused; }
        public long getMemoryPreAllocated() { return memoryPreAllocated; }
        public double getEliminationRate() { return eliminationRate; }
    }
    
    public static class StructureUsageMetrics {
        private final ConcurrentHashMap<String, LongAdder> creations;
        private final ConcurrentHashMap<String, LongAdder> disposals;
        private final ConcurrentHashMap<String, LongAdder> operations;
        private final ConcurrentHashMap<String, LongAdder> growths;
        private final ConcurrentHashMap<String, LongAdder> resizes;
        
        public StructureUsageMetrics(ConcurrentHashMap<String, LongAdder> creations,
                                   ConcurrentHashMap<String, LongAdder> disposals,
                                   ConcurrentHashMap<String, LongAdder> operations,
                                   ConcurrentHashMap<String, LongAdder> growths,
                                   ConcurrentHashMap<String, LongAdder> resizes) {
            this.creations = creations;
            this.disposals = disposals;
            this.operations = operations;
            this.growths = growths;
            this.resizes = resizes;
        }
        
        public ConcurrentHashMap<String, LongAdder> getCreations() { return creations; }
        public ConcurrentHashMap<String, LongAdder> getDisposals() { return disposals; }
        public ConcurrentHashMap<String, LongAdder> getOperations() { return operations; }
        public ConcurrentHashMap<String, LongAdder> getGrowths() { return growths; }
        public ConcurrentHashMap<String, LongAdder> getResizes() { return resizes; }
    }
    
    public static class PoolMetrics {
        private final ConcurrentHashMap<String, LongAdder> creations;
        private final ConcurrentHashMap<String, LongAdder> operations;
        private final ConcurrentHashMap<String, LongAdder> disposals;
        
        public PoolMetrics(ConcurrentHashMap<String, LongAdder> creations,
                         ConcurrentHashMap<String, LongAdder> operations,
                         ConcurrentHashMap<String, LongAdder> disposals) {
            this.creations = creations;
            this.operations = operations;
            this.disposals = disposals;
        }
        
        public ConcurrentHashMap<String, LongAdder> getCreations() { return creations; }
        public ConcurrentHashMap<String, LongAdder> getOperations() { return operations; }
        public ConcurrentHashMap<String, LongAdder> getDisposals() { return disposals; }
    }
    
    public static class PerformanceMetrics {
        private final long allocationTime;
        private final long deallocationTime;
        private final long readOperations;
        private final long writeOperations;
        private final long averageOperationTime;
        
        public PerformanceMetrics(long allocationTime, long deallocationTime, long readOperations,
                                long writeOperations, long averageOperationTime) {
            this.allocationTime = allocationTime;
            this.deallocationTime = deallocationTime;
            this.readOperations = readOperations;
            this.writeOperations = writeOperations;
            this.averageOperationTime = averageOperationTime;
        }
        
        public long getAllocationTime() { return allocationTime; }
        public long getDeallocationTime() { return deallocationTime; }
        public long getReadOperations() { return readOperations; }
        public long getWriteOperations() { return writeOperations; }
        public long getAverageOperationTime() { return averageOperationTime; }
    }
    
    public static class StringInterningMetrics {
        private final long totalInternings;
        private final long cacheHits;
        private final long cacheMisses;
        private final long memoryUsed;
        private final double hitRate;
        
        public StringInterningMetrics(long totalInternings, long cacheHits, long cacheMisses,
                                    long memoryUsed, double hitRate) {
            this.totalInternings = totalInternings;
            this.cacheHits = cacheHits;
            this.cacheMisses = cacheMisses;
            this.memoryUsed = memoryUsed;
            this.hitRate = hitRate;
        }
        
        public long getTotalInternings() { return totalInternings; }
        public long getCacheHits() { return cacheHits; }
        public long getCacheMisses() { return cacheMisses; }
        public long getMemoryUsed() { return memoryUsed; }
        public double getHitRate() { return hitRate; }
    }
    
    public static class ConfigCacheMetrics {
        private final long hits;
        private final long misses;
        private final long uniqueOperations;
        private final double hitRate;
        
        public ConfigCacheMetrics(long hits, long misses, long uniqueOperations, double hitRate) {
            this.hits = hits;
            this.misses = misses;
            this.uniqueOperations = uniqueOperations;
            this.hitRate = hitRate;
        }
        
        public long getHits() { return hits; }
        public long getMisses() { return misses; }
        public long getUniqueOperations() { return uniqueOperations; }
        public double getHitRate() { return hitRate; }
    }
    
    public static class UnsafeMemorySnapshot {
        private final long snapshotId;
        private final long timestamp;
        private final UnsafeMemoryStatistics memoryStats;
        private static GCEliminationMetrics gcMetrics;
        private static StructureUsageMetrics structureMetrics;
        private final PoolMetrics poolMetrics;
        
        public UnsafeMemorySnapshot(long snapshotId, long timestamp, UnsafeMemoryStatistics memoryStats,
                                  GCEliminationMetrics gcMetrics, StructureUsageMetrics structureMetrics,
                                  PoolMetrics poolMetrics) {
            this.snapshotId = snapshotId;
            this.timestamp = timestamp;
            this.memoryStats = memoryStats;
            this.gcMetrics = gcMetrics;
            this.structureMetrics = structureMetrics;
            this.poolMetrics = poolMetrics;
        }
        
        public long getSnapshotId() { return snapshotId; }
        public long getTimestamp() { return timestamp; }
        public UnsafeMemoryStatistics getMemoryStats() { return memoryStats; }
        public static GCEliminationMetrics getGcMetrics() { 
        return getCurrentGCStats(); 
    }
        public static StructureUsageMetrics getStructureMetrics() { 
        return getStructureStats(); 
    }
        public PoolMetrics getPoolMetrics() { return poolMetrics; }
    }
    
    public static class TrendAnalysis {
        private final java.util.List<UnsafeMemorySnapshot> recentSnapshots;
        
        public TrendAnalysis(java.util.List<UnsafeMemorySnapshot> recentSnapshots) {
            this.recentSnapshots = recentSnapshots;
        }
        
        public java.util.List<UnsafeMemorySnapshot> getRecentSnapshots() { return recentSnapshots; }
    }
    
    public static class UnsafeMemoryReport {
        private final UnsafeMemoryStatistics memoryStats;
        private static GCEliminationMetrics gcMetrics;
        private static StructureUsageMetrics structureMetrics;
        private final PerformanceMetrics performanceMetrics;
        private final PoolMetrics poolMetrics;
        private final StringInterningMetrics stringMetrics;
        private final ConfigCacheMetrics configMetrics;
        private final TrendAnalysis trendAnalysis;
        
        public UnsafeMemoryReport(UnsafeMemoryStatistics memoryStats, GCEliminationMetrics gcMetrics,
                                StructureUsageMetrics structureMetrics, PerformanceMetrics performanceMetrics,
                                PoolMetrics poolMetrics, StringInterningMetrics stringMetrics,
                                ConfigCacheMetrics configMetrics, TrendAnalysis trendAnalysis) {
            this.memoryStats = memoryStats;
            this.gcMetrics = gcMetrics;
            this.structureMetrics = structureMetrics;
            this.performanceMetrics = performanceMetrics;
            this.poolMetrics = poolMetrics;
            this.stringMetrics = stringMetrics;
            this.configMetrics = configMetrics;
            this.trendAnalysis = trendAnalysis;
        }
        
        public UnsafeMemoryStatistics getMemoryStats() { return memoryStats; }
        public static GCEliminationMetrics getGcMetrics() { 
        return getCurrentGCStats(); 
    }
        public static StructureUsageMetrics getStructureMetrics() { 
        return getStructureStats(); 
    }
        public PerformanceMetrics getPerformanceMetrics() { return performanceMetrics; }
        public PoolMetrics getPoolMetrics() { return poolMetrics; }
        public StringInterningMetrics getStringMetrics() { return stringMetrics; }
        public ConfigCacheMetrics getConfigMetrics() { return configMetrics; }
        public TrendAnalysis getTrendAnalysis() { return trendAnalysis; }
    }
    
    public static class ExecutiveSummary {
        private final long totalMemoryManaged;
        private final long netMemoryUsage;
        private final double gcEliminationRate;
        private final long activeAllocations;
        private final long totalStructures;
        private final long totalOperations;
        private final long averageOperationTime;
        private final double overallEfficiency;
        
        public ExecutiveSummary(long totalMemoryManaged, long netMemoryUsage, double gcEliminationRate,
                              long activeAllocations, long totalStructures, long totalOperations,
                              long averageOperationTime, double overallEfficiency) {
            this.totalMemoryManaged = totalMemoryManaged;
            this.netMemoryUsage = netMemoryUsage;
            this.gcEliminationRate = gcEliminationRate;
            this.activeAllocations = activeAllocations;
            this.totalStructures = totalStructures;
            this.totalOperations = totalOperations;
            this.averageOperationTime = averageOperationTime;
            this.overallEfficiency = overallEfficiency;
        }
        
        public long getTotalMemoryManaged() { return totalMemoryManaged; }
        public long getNetMemoryUsage() { return netMemoryUsage; }
        public double getGcEliminationRate() { return gcEliminationRate; }
        public long getActiveAllocations() { return activeAllocations; }
        public long getTotalStructures() { return totalStructures; }
        public long getTotalOperations() { return totalOperations; }
        public long getAverageOperationTime() { return averageOperationTime; }
        public double getOverallEfficiency() { return overallEfficiency; }
        
        @Override
        public String toString() {
            return String.format(
                "Executive Summary: %.1f%% GC elimination, %.1f%% overall efficiency, %d bytes managed, %d operations avg %dns",
                gcEliminationRate, overallEfficiency, totalMemoryManaged, totalOperations, averageOperationTime
            );
        }
    }
}