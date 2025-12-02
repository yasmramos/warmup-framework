package io.warmup.framework.benchmark;

import java.util.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Benchmark para validar optimizaciones O(1) en HotReloadManager.
 * Compara implementación optimizada (AtomicLong + TTL caches) vs secuencial.
 * 
 * Operaciones testadas:
 * - getTotalReloadsCount(): O(1) atomic read vs O(n) iteration
 * - getTotalFailedReloadsCount(): O(1) atomic read vs O(n) iteration
 * - getHotReloadExtremeStartupMetrics(): O(1) calculation vs O(n) sum
 * - getAllReloadStatuses(): TTL cache hit/miss simulation
 * 
 * Expected: Linear improvement en escalas altas (10x-100x)
 */
public class HotReloadManagerO1Benchmark {
    
    // =============================
    // SECUENTIAL IMPLEMENTATION (Baseline)
    // =============================
    
    static class SequentialHotReloadManager {
        private final Map<String, HotReloadStatus> reloadStatuses = new ConcurrentHashMap<>();
        private int totalReloadsCount = 0;
        private int totalFailedReloadsCount = 0;
        
        public void reloadClass(String className, boolean success) {
            reloadStatuses.put(className, new HotReloadStatus(className, System.currentTimeMillis(), success));
            totalReloadsCount++;
            if (!success) {
                totalFailedReloadsCount++;
            }
        }
        
        public long getTotalReloadsCount() {
            // O(n) - iterates over all entries
            long count = 0;
            for (HotReloadStatus status : reloadStatuses.values()) {
                if (status.isSuccess()) count++;
            }
            return count;
        }
        
        public long getTotalFailedReloadsCount() {
            // O(n) - iterates over all entries
            long count = 0;
            for (HotReloadStatus status : reloadStatuses.values()) {
                if (!status.isSuccess()) count++;
            }
            return count;
        }
        
        public Map<String, HotReloadStatus> getAllReloadStatuses() {
            // O(n) - creates new map copy
            Map<String, HotReloadStatus> copy = new HashMap<>();
            copy.putAll(reloadStatuses);
            return copy;
        }
        
        public Map<String, Object> getStartupMetrics() {
            // O(n) - iterates over all entries
            Map<String, Object> metrics = new HashMap<>();
            long successCount = 0;
            long failCount = 0;
            long totalTime = 0;
            
            for (HotReloadStatus status : reloadStatuses.values()) {
                if (status.isSuccess()) {
                    successCount++;
                } else {
                    failCount++;
                }
                totalTime += status.getReloadTime();
            }
            
            metrics.put("totalReloads", successCount + failCount);
            metrics.put("successRate", reloadStatuses.size() > 0 ? (double)successCount / reloadStatuses.size() : 0.0);
            metrics.put("averageReloadTime", reloadStatuses.size() > 0 ? (double)totalTime / reloadStatuses.size() : 0.0);
            metrics.put("startupTimestamp", System.currentTimeMillis());
            
            return metrics;
        }
    }
    
    // =============================
    // OPTIMIZED IMPLEMENTATION (O(1))
    // =============================
    
    static class OptimizedHotReloadManager {
        private final Map<String, HotReloadStatus> reloadStatuses = new ConcurrentHashMap<>();
        
        // O(1) Atomic counters
        private final AtomicLong totalReloadsCount = new AtomicLong(0);
        private final AtomicLong totalFailedReloadsCount = new AtomicLong(0);
        
        // TTL cache fields
        private volatile long allStatusesCacheTimestamp = 0;
        private volatile Map<String, HotReloadStatus> cachedAllStatuses = null;
        private static final long ALL_STATUSES_CACHE_TTL_MS = 30000;
        
        public void reloadClass(String className, boolean success) {
            reloadStatuses.put(className, new HotReloadStatus(className, System.currentTimeMillis(), success));
            totalReloadsCount.incrementAndGet();
            if (!success) {
                totalFailedReloadsCount.incrementAndGet();
            }
            // Invalidate cache
            cachedAllStatuses = null;
        }
        
        // O(1) atomic read
        public long getTotalReloadsCount() {
            return totalReloadsCount.get();
        }
        
        // O(1) atomic read
        public long getTotalFailedReloadsCount() {
            return totalFailedReloadsCount.get();
        }
        
        // O(1) or O(n) only on cache miss
        public Map<String, HotReloadStatus> getAllReloadStatuses() {
            long now = System.currentTimeMillis();
            
            // Check cache validity (O(1))
            if (cachedAllStatuses != null && (now - allStatusesCacheTimestamp) < ALL_STATUSES_CACHE_TTL_MS) {
                return cachedAllStatuses;
            }
            
            // Cache miss - O(n) operation
            Map<String, HotReloadStatus> copy = new HashMap<>();
            copy.putAll(reloadStatuses);
            
            // Update cache (O(1))
            cachedAllStatuses = copy;
            allStatusesCacheTimestamp = now;
            
            return copy;
        }
        
        // O(1) calculation with atomic reads
        public Map<String, Object> getStartupMetrics() {
            Map<String, Object> metrics = new HashMap<>();
            long total = totalReloadsCount.get();
            long failed = totalFailedReloadsCount.get();
            long success = total - failed;
            
            metrics.put("totalReloads", total);
            metrics.put("successCount", success);
            metrics.put("failedCount", failed);
            metrics.put("successRate", total > 0 ? (double)success / total : 0.0);
            metrics.put("startupTimestamp", System.currentTimeMillis());
            
            return metrics;
        }
    }
    
    // =============================
    // SUPPORT CLASSES
    // =============================
    
    static class HotReloadStatus {
        private final String className;
        private final long reloadTime;
        private final boolean success;
        
        public HotReloadStatus(String className, long reloadTime, boolean success) {
            this.className = className;
            this.reloadTime = reloadTime;
            this.success = success;
        }
        
        public String getClassName() { return className; }
        public long getReloadTime() { return reloadTime; }
        public boolean isSuccess() { return success; }
    }
    
    // =============================
    // BENCHMARK EXECUTION
    // =============================
    
    public static void main(String[] args) {
        System.out.println("🚀 HotReloadManager O(1) Optimization Benchmark");
        System.out.println("==================================================");
        
        int[] scales = {10, 50, 100, 500, 1000};
        
        for (int scale : scales) {
            System.out.println("\n📊 Testing at scale: " + scale);
            for (int i = 0; i < 50; i++) {
                System.out.print("-");
            }
            System.out.println();
            
            benchmarkHotReloadManager(scale);
        }
        
        System.out.println("\n✅ Benchmark completed!");
    }
    
    private static void benchmarkHotReloadManager(int scale) {
        // Setup data
        String[] classNames = new String[scale];
        boolean[] successFlags = new boolean[scale];
        Random random = new Random(42);
        
        for (int i = 0; i < scale; i++) {
            classNames[i] = "com.example.Class" + i;
            successFlags[i] = random.nextBoolean(); // 50% success rate
        }
        
        // Sequential (baseline)
        SequentialHotReloadManager sequential = new SequentialHotReloadManager();
        long seqStart = System.nanoTime();
        
        for (int i = 0; i < scale; i++) {
            sequential.reloadClass(classNames[i], successFlags[i]);
        }
        
        long seqGetTotalReloads = measureGetTotalReloads(sequential, scale);
        long seqGetTotalFailed = measureGetTotalFailedReloads(sequential, scale);
        long seqGetStartupMetrics = measureGetStartupMetrics(sequential, scale);
        long seqGetAllStatuses = measureGetAllStatuses(sequential, scale);
        long seqEnd = System.nanoTime();
        long seqTotal = seqEnd - seqStart;
        
        // Optimized (O(1))
        OptimizedHotReloadManager optimized = new OptimizedHotReloadManager();
        long optStart = System.nanoTime();
        
        for (int i = 0; i < scale; i++) {
            optimized.reloadClass(classNames[i], successFlags[i]);
        }
        
        // Warm up cache
        optimized.getAllReloadStatuses();
        optimized.getAllReloadStatuses();
        
        long optGetTotalReloads = measureGetTotalReloads(optimized, scale);
        long optGetTotalFailed = measureGetTotalFailedReloads(optimized, scale);
        long optGetStartupMetrics = measureGetStartupMetrics(optimized, scale);
        long optGetAllStatuses = measureGetAllStatuses(optimized, scale);
        long optEnd = System.nanoTime();
        long optTotal = optEnd - optStart;
        
        // Results
        printResults(scale, seqGetTotalReloads, optGetTotalReloads, 
                    seqGetTotalFailed, optGetTotalFailed,
                    seqGetStartupMetrics, optGetStartupMetrics,
                    seqGetAllStatuses, optGetAllStatuses,
                    seqTotal, optTotal);
    }
    
    private static long measureGetTotalReloads(SequentialHotReloadManager manager, int iterations) {
        long start = System.nanoTime();
        for (int i = 0; i < iterations; i++) {
            manager.getTotalReloadsCount();
        }
        long end = System.nanoTime();
        return (end - start) / iterations;
    }
    
    private static long measureGetTotalReloads(OptimizedHotReloadManager manager, int iterations) {
        long start = System.nanoTime();
        for (int i = 0; i < iterations; i++) {
            manager.getTotalReloadsCount();
        }
        long end = System.nanoTime();
        return (end - start) / iterations;
    }
    
    private static long measureGetTotalFailedReloads(SequentialHotReloadManager manager, int iterations) {
        long start = System.nanoTime();
        for (int i = 0; i < iterations; i++) {
            manager.getTotalFailedReloadsCount();
        }
        long end = System.nanoTime();
        return (end - start) / iterations;
    }
    
    private static long measureGetTotalFailedReloads(OptimizedHotReloadManager manager, int iterations) {
        long start = System.nanoTime();
        for (int i = 0; i < iterations; i++) {
            manager.getTotalFailedReloadsCount();
        }
        long end = System.nanoTime();
        return (end - start) / iterations;
    }
    
    private static long measureGetStartupMetrics(SequentialHotReloadManager manager, int iterations) {
        long start = System.nanoTime();
        for (int i = 0; i < iterations; i++) {
            manager.getStartupMetrics();
        }
        long end = System.nanoTime();
        return (end - start) / iterations;
    }
    
    private static long measureGetStartupMetrics(OptimizedHotReloadManager manager, int iterations) {
        long start = System.nanoTime();
        for (int i = 0; i < iterations; i++) {
            manager.getStartupMetrics();
        }
        long end = System.nanoTime();
        return (end - start) / iterations;
    }
    
    private static long measureGetAllStatuses(SequentialHotReloadManager manager, int iterations) {
        long start = System.nanoTime();
        for (int i = 0; i < iterations; i++) {
            manager.getAllReloadStatuses();
        }
        long end = System.nanoTime();
        return (end - start) / iterations;
    }
    
    private static long measureGetAllStatuses(OptimizedHotReloadManager manager, int iterations) {
        long start = System.nanoTime();
        for (int i = 0; i < iterations; i++) {
            manager.getAllReloadStatuses();
        }
        long end = System.nanoTime();
        return (end - start) / iterations;
    }
    
    private static void printResults(int scale, 
                                   long seqGetTotalReloads, long optGetTotalReloads,
                                   long seqGetTotalFailed, long optGetTotalFailed,
                                   long seqGetStartupMetrics, long optGetStartupMetrics,
                                   long seqGetAllStatuses, long optGetAllStatuses,
                                   long seqTotal, long optTotal) {
        
        double improvementTotalReloads = (double)seqGetTotalReloads / optGetTotalReloads;
        double improvementTotalFailed = (double)seqGetTotalFailed / optGetTotalFailed;
        double improvementStartupMetrics = (double)seqGetStartupMetrics / optGetStartupMetrics;
        double improvementAllStatuses = (double)seqGetAllStatuses / optGetAllStatuses;
        double totalImprovement = (double)seqTotal / optTotal;
        
        System.out.printf("📈 getTotalReloadsCount():     %,d ns → %,d ns (%.1fx faster)\n", 
                         seqGetTotalReloads, optGetTotalReloads, improvementTotalReloads);
        System.out.printf("📈 getTotalFailedReloadsCount(): %,d ns → %,d ns (%.1fx faster)\n", 
                         seqGetTotalFailed, optGetTotalFailed, improvementTotalFailed);
        System.out.printf("📈 getStartupMetrics():         %,d ns → %,d ns (%.1fx faster)\n", 
                         seqGetStartupMetrics, optGetStartupMetrics, improvementStartupMetrics);
        System.out.printf("📈 getAllReloadStatuses():      %,d ns → %,d ns (%.1fx faster)\n", 
                         seqGetAllStatuses, optGetAllStatuses, improvementAllStatuses);
        System.out.printf("🎯 Total Improvement:           %.1fx faster\n", totalImprovement);
        
        // O(1) validation
        if (improvementTotalReloads >= 5.0 && improvementTotalFailed >= 5.0) {
            System.out.println("✅ O(1) operations validated!");
        }
        
        if (scale >= 100 && improvementAllStatuses >= 2.0) {
            System.out.println("✅ Cache effectiveness validated!");
        }
    }
}