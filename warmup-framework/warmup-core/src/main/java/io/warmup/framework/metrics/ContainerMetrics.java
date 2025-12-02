package io.warmup.framework.metrics;

import io.warmup.framework.core.WarmupContainer;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

public class ContainerMetrics {

    private final WarmupContainer container;
    private final AtomicLong totalRequests = new AtomicLong(0);
    private final AtomicLong successfulRequests = new AtomicLong(0);
    private final AtomicLong failedRequests = new AtomicLong(0);
    private final long startTime;
    private final Map<Class<?>, AtomicLong> resolutionCounts = new ConcurrentHashMap<>();
    private final Map<Class<?>, AtomicLong> resolutionTimes = new ConcurrentHashMap<>();
    private final AtomicLong totalResolutionTime = new AtomicLong(0);
    private final PercentileCalculator percentileCalc = new PercentileCalculator();

    public ContainerMetrics(WarmupContainer container) {
        this.container = container;
        this.startTime = System.currentTimeMillis();
    }

    public long getUptime() {
        return System.currentTimeMillis() - startTime;
    }

    public String getFormattedUptime() {
        long uptime = getUptime();
        long seconds = uptime / 1000;
        long minutes = seconds / 60;
        long hours = minutes / 60;

        return String.format("%02d:%02d:%02d", hours, minutes % 60, seconds % 60);
    }

    public int getDependencyCount() {
        return container != null ? container.getDependencies().size() : 0;
    }

    public int getAspectCount() {
        return container != null ? container.getAspects().size() : 0;
    }

    public int getActiveInstances() {
        if (container == null) return 0;
        Set<?> dependencies = (Set<?>) container.getDependencies();
        return dependencies != null ? dependencies.size() : 0;
    }

    public long getTotalRequests() {
        return totalRequests.get();
    }

    public long getSuccessfulRequests() {
        return successfulRequests.get();
    }

    public long getFailedRequests() {
        return failedRequests.get();
    }

    public double getSuccessRate() {
        long total = totalRequests.get();
        return total > 0 ? (successfulRequests.get() * 100.0) / total : 100.0;
    }

    public void recordRequest(boolean success) {
        totalRequests.incrementAndGet();
        if (success) {
            successfulRequests.incrementAndGet();
        } else {
            failedRequests.incrementAndGet();
        }
    }

    public void recordSuccess() {
        recordRequest(true);
    }

    public void recordFailure() {
        recordRequest(false);
    }

    public MetricsSnapshot getSnapshot() {
        return new MetricsSnapshot(
                getUptime(),
                getDependencyCount(),
                getAspectCount(),
                getActiveInstances(),
                getTotalRequests(),
                getSuccessfulRequests(),
                getFailedRequests(),
                getSuccessRate(),
                getTotalResolutionTime(),
                getOverallAverageResolutionTime(),
                new Date(startTime),
                new Date(),
                getResolutionCounts(),
                getDependencyResolutionStats()
        );
    }

    public void recordResolution(Class<?> type, long durationNanos, boolean success) {
        resolutionCounts.computeIfAbsent(type, k -> new AtomicLong()).incrementAndGet();
        resolutionTimes.computeIfAbsent(type, k -> new AtomicLong()).addAndGet(durationNanos);
        totalResolutionTime.addAndGet(durationNanos);
        percentileCalc.add(durationNanos);
        recordRequest(success);
    }

    public long getResolutionCount(Class<?> type) {
        AtomicLong count = resolutionCounts.get(type);
        return count != null ? count.get() : 0;
    }

    public long getTotalResolutionTime() {
        return totalResolutionTime.get();
    }

    public double getOverallAverageResolutionTime() {
        long totalResolutions = getTotalRequests();
        return totalResolutions > 0
                ? (double) totalResolutionTime.get() / totalResolutions : 0.0;
    }

    public Map<Class<?>, Long> getResolutionCounts() {
        Map<Class<?>, Long> counts = new HashMap<>();
        resolutionCounts.forEach((type, count) -> counts.put(type, count.get()));
        return Collections.unmodifiableMap(counts);
    }

    public DependencyResolutionStats getDependencyResolutionStats() {
        if (resolutionTimes.isEmpty()) {
            return new DependencyResolutionStats();
        }

        Map<Class<?>, Double> averageTimes = new HashMap<>();
        Class<?> slowest = null;
        double slowestTime = 0.0;
        Class<?> fastest = null;
        double fastestTime = Double.MAX_VALUE;

        // 1. promedios y extremos
        for (Map.Entry<Class<?>, AtomicLong> entry : resolutionTimes.entrySet()) {
            Class<?> type = entry.getKey();
            long totalTime = entry.getValue().get();
            long count = resolutionCounts.get(type).get();
            double average = count > 0 ? (double) totalTime / count : 0.0;
            averageTimes.put(type, average);

            if (average > slowestTime) {
                slowestTime = average;
                slowest = type;
            }
            if (average < fastestTime && count > 0) {
                fastestTime = average;
                fastest = type;
            }
        }

        // 2. top 5
        List<Class<?>> topSlowest = averageTimes.entrySet().stream()
                .sorted(Map.Entry.<Class<?>, Double>comparingByValue().reversed())
                .limit(5)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());

        // 3. percentiles globales (usando tu PercentileCalculator)
        double p50 = getPercentile(0.50);
        double p95 = getPercentile(0.95);
        double p99 = getPercentile(0.99);

        // 4. construir con TODOS los parámetros
        return new DependencyResolutionStats(
                averageTimes, slowest, slowestTime, fastest, fastestTime, topSlowest,
                p50, p95, p99
        );
    }

    public double getPercentile(double p) {
        return percentileCalc.percentile(p);
    }

    public static class MetricsSnapshot {

        private final long uptime;
        private final int dependencyCount;
        private final int aspectCount;
        private final int activeInstances;
        private final long totalRequests;
        private final long successfulRequests;
        private final long failedRequests;
        private final double successRate;
        private final long totalResolutionTime;           // Nuevo
        private final double overallAverageResolutionTime; // Nuevo
        private final Date startTime;
        private final Date snapshotTime;
        private final Map<Class<?>, Long> resolutionCounts; // Nuevo
        private final DependencyResolutionStats resolutionStats; // Nuevo

        public MetricsSnapshot(long uptime, int dependencyCount, int aspectCount,
                int activeInstances, long totalRequests, long successfulRequests,
                long failedRequests, double successRate,
                long totalResolutionTime, double overallAverageResolutionTime, // Nuevos parámetros
                Date startTime, Date snapshotTime,
                Map<Class<?>, Long> resolutionCounts, // Nuevo
                DependencyResolutionStats resolutionStats) { // Nuevo

            this.uptime = uptime;
            this.dependencyCount = dependencyCount;
            this.aspectCount = aspectCount;
            this.activeInstances = activeInstances;
            this.totalRequests = totalRequests;
            this.successfulRequests = successfulRequests;
            this.failedRequests = failedRequests;
            this.successRate = successRate;
            this.totalResolutionTime = totalResolutionTime;
            this.overallAverageResolutionTime = overallAverageResolutionTime;
            this.startTime = startTime;
            this.snapshotTime = snapshotTime;
            this.resolutionCounts = resolutionCounts != null
                    ? Collections.unmodifiableMap(resolutionCounts) : Collections.emptyMap();
            this.resolutionStats = resolutionStats != null ? resolutionStats
                    : new DependencyResolutionStats();
        }

        // Getters existentes...
        public long getUptime() {
            return uptime;
        }

        public int getDependencyCount() {
            return dependencyCount;
        }

        public int getAspectCount() {
            return aspectCount;
        }

        public int getActiveInstances() {
            return activeInstances;
        }

        public long getTotalRequests() {
            return totalRequests;
        }

        public long getSuccessfulRequests() {
            return successfulRequests;
        }

        public long getFailedRequests() {
            return failedRequests;
        }

        public double getSuccessRate() {
            return successRate;
        }

        public Date getStartTime() {
            return startTime;
        }

        public Date getSnapshotTime() {
            return snapshotTime;
        }

        // Nuevos getters
        public long getTotalResolutionTime() {
            return totalResolutionTime;
        }

        public double getOverallAverageResolutionTime() {
            return overallAverageResolutionTime;
        }

        public Map<Class<?>, Long> getResolutionCounts() {
            return resolutionCounts;
        }

        public DependencyResolutionStats getResolutionStats() {
            return resolutionStats;
        }

        // Método helper para obtener tiempo promedio en ms
        public double getOverallAverageResolutionTimeMs() {
            return overallAverageResolutionTime / 1_000_000.0;
        }

        // Método helper para obtener tiempo total en ms
        public double getTotalResolutionTimeMs() {
            return totalResolutionTime / 1_000_000.0;
        }
    }
}
