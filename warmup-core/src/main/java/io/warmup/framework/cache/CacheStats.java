package io.warmup.framework.cache;

import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.LongAdder;

class CacheStats {

    private final LongAdder totalRequests = new LongAdder();
    private final LongAdder memoryHits = new LongAdder();
    private final LongAdder diskHits = new LongAdder();
    private final LongAdder misses = new LongAdder();
    private final LongAdder diskErrors = new LongAdder();
    private final AtomicLong totalGenerationTimeNs = new AtomicLong();

    void incrementRequests() {
        totalRequests.increment();
    }

    void incrementMemoryHits() {
        memoryHits.increment();
    }

    void incrementDiskHits() {
        diskHits.increment();
    }

    void incrementMisses() {
        misses.increment();
    }

    void incrementDiskErrors() {
        diskErrors.increment();
    }

    public void recordGenerationTime(long nanos) {
        totalGenerationTimeNs.addAndGet(nanos);
    }

    public long getTotalRequests() {
        return totalRequests.sum();
    }

    public long getMemoryHits() {
        return memoryHits.sum();
    }

    public long getDiskHits() {
        return diskHits.sum();
    }

    public long getMisses() {
        return misses.sum();
    }

    public long getDiskErrors() {
        return diskErrors.sum();
    }

    public double getMemoryHitRate() {
        long total = getTotalRequests();
        return total > 0 ? (memoryHits.sum() * 100.0 / total) : 0;
    }

    public double getDiskHitRate() {
        long total = getTotalRequests();
        return total > 0 ? (diskHits.sum() * 100.0 / total) : 0;
    }

    public double getMissRate() {
        long total = getTotalRequests();
        return total > 0 ? (misses.sum() * 100.0 / total) : 0;
    }

    public double getOverallHitRate() {
        long total = getTotalRequests();
        return total > 0 ? ((memoryHits.sum() + diskHits.sum()) * 100.0 / total) : 0;
    }

    public double getAverageGenerationTimeMs() {
        long totalMisses = getMisses();
        if (totalMisses == 0) {
            return 0;
        }
        return totalGenerationTimeNs.get() / 1_000_000.0 / totalMisses;
    }

    /**
     * PHASE 3: O(1) Get Hit Rate - Sin iteración O(n)
     * 
     * @return tasa de aciertos del cache (0.0 - 1.0)
     */
    public double getHitRate() {
        // Retornar hit rate normalizado (0.0 - 1.0) en lugar de porcentaje
        return getOverallHitRate() / 100.0;
    }

    public void reset() {
        totalRequests.reset();
        memoryHits.reset();
        diskHits.reset();
        misses.reset();
        diskErrors.reset();
        totalGenerationTimeNs.set(0);
    }
}