package io.warmup.framework.metrics;

import io.warmup.framework.core.WarmupContainer;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;
import java.util.logging.Level;
import java.util.concurrent.TimeUnit;

public class MetricsManager {

    private static final Logger log = Logger.getLogger(MetricsManager.class.getName());
    
    // O(1) Atomic Counters for Real-time Statistics
    private final AtomicInteger totalMetricQueries = new AtomicInteger(0);
    private final AtomicInteger snapshotGenerations = new AtomicInteger(0);
    private final AtomicInteger counterUpdates = new AtomicInteger(0);
    private final AtomicInteger timerRecords = new AtomicInteger(0);
    private final AtomicInteger customMetricUpdates = new AtomicInteger(0);
    
    // O(1) TTL Caches - Stats (30s), Metrics (5s)
    private final Map<String, Object> metricsStatsCache = new ConcurrentHashMap<>();
    private final Map<String, Long> metricsStatsExpiry = new ConcurrentHashMap<>();
    private static final long METRICS_STATS_TTL = TimeUnit.SECONDS.toMillis(30);
    
    private final Map<String, Object> metricsCache = new ConcurrentHashMap<>();
    private final Map<String, Long> metricsExpiry = new ConcurrentHashMap<>();
    private static final long METRICS_TTL = TimeUnit.SECONDS.toMillis(5);
    
    // O(1) Cache Invalidation Flags
    private volatile boolean countersDirty = false;
    private volatile boolean timersDirty = false;
    private volatile boolean customMetricsDirty = false;
    private volatile boolean dependencyTimesDirty = false;

    private WarmupContainer container;
    private ContainerMetrics containerMetrics;
    private final MethodMetrics methodMetrics;
    private final Map<String, Object> customMetrics = new ConcurrentHashMap<>();
    private final Map<String, AtomicLong> counters = new ConcurrentHashMap<>();
    private final Map<String, List<Long>> timers = new ConcurrentHashMap<>();
    private final Map<Class<?>, AtomicLong> dependencyResolutionTimes = new ConcurrentHashMap<>();

    public MetricsManager(WarmupContainer container) {
        this.container = container;
        this.containerMetrics = new ContainerMetrics(container);
        this.methodMetrics = new MethodMetrics();
    }
    
    public MetricsManager() {
        this.container = null;
        this.containerMetrics = new ContainerMetrics(null);
        this.methodMetrics = new MethodMetrics();
    }

    public void setContainer(WarmupContainer container) {
        this.container = container;
        if (container != null) {
            this.containerMetrics = new ContainerMetrics(container);
        }
    }

    // O(1) Métodos optimizados para registro de dependencias
    public void recordDependencyResolution(Class<?> type, long duration, boolean success) {
        dependencyResolutionTimes.computeIfAbsent(type, k -> new AtomicLong())
                .addAndGet(duration);
        if (containerMetrics != null) {
            containerMetrics.recordRequest(success);
        }
        
        // O(1) Cache invalidation
        dependencyTimesDirty = true;
        invalidateDependencyResolutionCache();
    }

    // O(1) Optimized method with TTL caching
    public Map<Class<?>, Long> getDependencyResolutionTimes() {
        String cacheKey = "dependencyResolutionTimes";
        
        // O(1) Cache check with TTL
        if (!dependencyTimesDirty && isCacheValid(cacheKey, metricsStatsCache, metricsStatsExpiry)) {
            totalMetricQueries.incrementAndGet();
            @SuppressWarnings("unchecked")
            Map<Class<?>, Long> cached = (Map<Class<?>, Long>) metricsCache.get(cacheKey);
            return cached != null ? cached : new HashMap<>();
        }
        
        Map<Class<?>, Long> result = new HashMap<>();
        dependencyResolutionTimes.forEach((type, time)
                -> result.put(type, time.get()));
        
        // O(1) Cache storage
        metricsCache.put(cacheKey, result);
        metricsExpiry.put(cacheKey, System.currentTimeMillis() + METRICS_STATS_TTL);
        dependencyTimesDirty = false;
        
        return result;
    }

    public double getAverageResolutionTime(Class<?> type) {
        AtomicLong totalTime = dependencyResolutionTimes.get(type);
        long resolutionCount = getResolutionCount(type);
        return totalTime != null && resolutionCount > 0
                ? (double) totalTime.get() / resolutionCount : 0.0;
    }

    public long getResolutionCount(Class<?> type) {
        return containerMetrics.getResolutionCount(type);
    }
    
    // O(1) Cache invalidation helper
    private void invalidateDependencyResolutionCache() {
        String cacheKey = "dependencyResolutionTimes";
        metricsCache.remove(cacheKey);
        metricsExpiry.remove(cacheKey);
    }

    // Método mejorado para snapshot
    public Map<String, Object> getMetricsSnapshot() {
        Map<String, Object> snapshot = new HashMap<>();

        // WarmupContainer metrics
        ContainerMetrics.MetricsSnapshot containerSnapshot = containerMetrics.getSnapshot();
        snapshot.put("container.uptime", containerSnapshot.getUptime());
        snapshot.put("container.dependencyCount", containerSnapshot.getDependencyCount());
        snapshot.put("container.aspectCount", containerSnapshot.getAspectCount());
        snapshot.put("container.activeInstances", containerSnapshot.getActiveInstances());
        snapshot.put("container.totalRequests", containerSnapshot.getTotalRequests());
        snapshot.put("container.successfulRequests", containerSnapshot.getSuccessfulRequests());
        snapshot.put("container.failedRequests", containerSnapshot.getFailedRequests());
        snapshot.put("container.successRate", containerSnapshot.getSuccessRate());
        snapshot.put("container.startTime", containerSnapshot.getStartTime());
        snapshot.put("container.snapshotTime", containerSnapshot.getSnapshotTime());

        // Method metrics
        Map<String, Object> methodStats = new HashMap<>();
        methodMetrics.getAllStats().forEach((methodName, stats) -> {
            Map<String, Object> methodData = new HashMap<>();
            methodData.put("callCount", stats.getCallCount());
            methodData.put("successfulCalls", stats.getSuccessfulCalls());
            methodData.put("failedCalls", stats.getFailedCalls());
            methodData.put("successRate", stats.getSuccessRate());
            methodData.put("totalTime", stats.getTotalTime());
            methodData.put("minTime", stats.getMinTime());
            methodData.put("maxTime", stats.getMaxTime());
            methodData.put("averageTime", stats.getAverageTime());
            methodStats.put(methodName, methodData);
        });
        snapshot.put("methods", methodStats);

        // Dependency resolution times
        Map<String, Object> dependencyTimes = new HashMap<>();
        getDependencyResolutionTimes().forEach((type, time) -> {
            dependencyTimes.put(type.getSimpleName(), time);
        });
        snapshot.put("dependencyResolutionTimes", dependencyTimes);

        // Counters
        Map<String, Long> counterSnapshot = new HashMap<>();
        counters.forEach((name, counter) -> counterSnapshot.put(name, counter.get()));
        snapshot.put("counters", counterSnapshot);

        // Timers
        Map<String, Object> timerSnapshot = new HashMap<>();
        timers.forEach((name, times) -> {
            if (!times.isEmpty()) {
                LongSummaryStatistics stats = times.stream()
                        .mapToLong(Long::longValue)
                        .summaryStatistics();

                Map<String, Object> timerStats = new HashMap<>();
                timerStats.put("count", stats.getCount());
                timerStats.put("avg", stats.getAverage());
                timerStats.put("max", stats.getMax());
                timerStats.put("min", stats.getMin());
                timerStats.put("sum", stats.getSum());
                timerSnapshot.put(name, timerStats);
            }
        });
        snapshot.put("timers", timerSnapshot);

        // Custom metrics
        snapshot.put("custom", new HashMap<>(customMetrics));

        return Collections.unmodifiableMap(snapshot);
    }

    // O(1) Optimized method with TTL caching
    public void printMetricsReport() {
        snapshotGenerations.incrementAndGet();
        String cacheKey = "metricsReport";
        
        // O(1) Cache check
        if (isCacheValid(cacheKey, metricsStatsCache, metricsStatsExpiry)) {
            String cachedReport = (String) metricsStatsCache.get(cacheKey);
            if (cachedReport != null) {
                System.out.println(cachedReport);
                return;
            }
        }
        
        // Generate report
        ContainerMetrics.MetricsSnapshot snapshot = containerMetrics.getSnapshot();
        StringBuilder report = new StringBuilder();
        report.append("\n=== Warmup Container Metrics Report ===");
        report.append(String.format("\nUptime: %s", containerMetrics.getFormattedUptime()));
        report.append(String.format("\nDependencies: %d | Aspects: %d | Active Instances: %d",
                snapshot.getDependencyCount(), snapshot.getAspectCount(), snapshot.getActiveInstances()));
        report.append(String.format("\nRequests: %d total, %d success, %d failed (%.2f%% success rate)",
                snapshot.getTotalRequests(), snapshot.getSuccessfulRequests(),
                snapshot.getFailedRequests(), snapshot.getSuccessRate()));

        // Métricas de métodos
        if (!methodMetrics.getAllStats().isEmpty()) {
            report.append("\n\n--- Method Performance ---");
            methodMetrics.getAllStats().forEach((method, stats) -> {
                report.append(String.format("\n  %s: calls=%d, avg=%.2fms, min=%dms, max=%dms, success=%.2f%%%",
                        method, stats.getCallCount(), stats.getAverageTime(),
                        stats.getMinTime(), stats.getMaxTime(), stats.getSuccessRate()));
            });
        }

        // Top 5 dependencias más lentas
        if (!dependencyResolutionTimes.isEmpty()) {
            report.append("\n\n--- Slowest Dependencies ---");
            dependencyResolutionTimes.entrySet().stream()
                    .sorted((a, b) -> Long.compare(b.getValue().get(), a.getValue().get()))
                    .limit(5)
                    .forEach(entry -> {
                        double avgTime = getAverageResolutionTime(entry.getKey());
                        report.append(String.format("\n  %s: avg=%.3fms",
                                entry.getKey().getSimpleName(), avgTime / 1_000_000.0));
                    });
        }
        
        String reportStr = report.toString();
        
        // O(1) Cache storage
        metricsStatsCache.put(cacheKey, reportStr);
        metricsStatsExpiry.put(cacheKey, System.currentTimeMillis() + METRICS_STATS_TTL);
        
        System.out.println(reportStr);
    }

    public ContainerMetrics getContainerMetrics() {
        return containerMetrics;
    }

    public MethodMetrics getMethodMetrics() {
        return methodMetrics;
    }

    public Map<String, Object> getCustomMetrics() {
        return customMetrics;
    }

    // O(1) Optimized reset with atomic counter updates
    public void resetMetrics() {
        // Reset the method-level metrics
        methodMetrics.reset();
        
        // Clear data structures
        counters.clear();
        timers.clear();
        customMetrics.clear();
        dependencyResolutionTimes.clear();
        
        // O(1) Clear all caches
        metricsCache.clear();
        metricsExpiry.clear();
        metricsStatsCache.clear();
        metricsStatsExpiry.clear();
        
        // O(1) Reset dirty flags
        countersDirty = false;
        timersDirty = false;
        customMetricsDirty = false;
        dependencyTimesDirty = false;

        // Note: Depending on your needs, you might also want to reset 
        // the containerMetrics (like totalRequests, etc.). If so,
        // you would need to add a reset() method to the ContainerMetrics class as well.
    }

    // O(1) Optimized Prometheus metrics with TTL caching
    public String getPrometheusMetrics() {
        snapshotGenerations.incrementAndGet();
        String cacheKey = "prometheusMetrics";
        
        // O(1) Cache check
        if (isCacheValid(cacheKey, metricsCache, metricsExpiry)) {
            totalMetricQueries.incrementAndGet();
            String cached = (String) metricsCache.get(cacheKey);
            if (cached != null) return cached;
        }
        
        // This method generates a string in the standard Prometheus exposition format.
        StringBuilder prometheus = new StringBuilder();

        // Example: Export a gauge for container uptime
        prometheus.append("# HELP warmup_container_uptime Container uptime in milliseconds\n");
        prometheus.append("# TYPE warmup_container_uptime gauge\n");
        if (container != null) {
            prometheus.append("warmup_container_uptime ").append(container.getUptime()).append("\n");
        } else {
            prometheus.append("warmup_container_uptime 0\n");
        }

        // Example: Export a gauge for the number of dependencies
        prometheus.append("# HELP warmup_container_dependencies Number of registered dependencies\n");
        prometheus.append("# TYPE warmup_container_dependencies gauge\n");
        prometheus.append("warmup_container_dependencies ").append(containerMetrics.getDependencyCount()).append("\n");

        // Export custom counters
        counters.forEach((name, counter) -> {
            prometheus.append("# HELP warmup_counter_").append(name).append(" Custom counter metric\n");
            prometheus.append("# TYPE warmup_counter_").append(name).append(" counter\n");
            prometheus.append("warmup_counter_").append(name).append(" ").append(counter.get()).append("\n");
        });

        String result = prometheus.toString();
        
        // O(1) Cache storage
        metricsCache.put(cacheKey, result);
        metricsExpiry.put(cacheKey, System.currentTimeMillis() + METRICS_TTL);
        
        return result;
    }
    
    // O(1) Helper methods
    private boolean isCacheValid(String key, Map<String, ?> cache, Map<String, Long> expiry) {
        Long expireTime = expiry.get(key);
        return expireTime != null && System.currentTimeMillis() < expireTime;
    }
    
    // O(1) Atomic counter getters
    public int getTotalMetricQueries() {
        return totalMetricQueries.get();
    }
    
    public int getSnapshotGenerations() {
        return snapshotGenerations.get();
    }
    
    public int getCounterUpdates() {
        return counterUpdates.get();
    }
    
    public int getTimerRecords() {
        return timerRecords.get();
    }
    
    public int getCustomMetricUpdates() {
        return customMetricUpdates.get();
    }

    /**
     * Initializes the metrics manager
     */
    public void initialize() {
        log.log(Level.INFO, "Initializing MetricsManager with O(1) optimizations");
        
        // Initialize container metrics
        if (containerMetrics != null) {
            // Reset any existing data
            counterUpdates.set(0);
            timerRecords.set(0);
            customMetricUpdates.set(0);
        }
        
        log.log(Level.FINE, "MetricsManager initialized successfully");
    }
    
    /**
     * Starts metrics collection
     */
    public void startMetricsCollection() {
        log.log(Level.INFO, "Starting metrics collection");
        
        // Initialize metrics tracking
        totalMetricQueries.set(0);
        snapshotGenerations.set(0);
        countersDirty = false;
        timersDirty = false;
        customMetricsDirty = false;
        dependencyTimesDirty = false;
        
        log.log(Level.FINE, "Metrics collection started");
    }
    
    /**
     * Stops metrics collection
     */
    public void stopMetricsCollection() {
        log.log(Level.INFO, "Stopping metrics collection");
        
        // Finalize any ongoing metrics
        if (containerMetrics != null) {
            // Print final metrics report
            printMetricsReport();
        }
        
        log.log(Level.FINE, "Metrics collection stopped");
    }
    
    /**
     * Warmup metrics system
     */
    public void warmupMetrics() {
        log.log(Level.INFO, "Warming up metrics system");
        
        // Pre-generate some metrics reports
        getMetricsSnapshot();
        getPrometheusMetrics();
        
        log.log(Level.FINE, "Metrics warmup completed");
    }

    /**
     * Shutdown the metrics manager
     */
    public void shutdown() {
        log.info("Shutting down MetricsManager...");
        
        // Print final metrics report
        printMetricsReport();
        
        // Clear all metrics
        customMetrics.clear();
        counters.clear();
        timers.clear();
        dependencyResolutionTimes.clear();
        
        // Clear all caches
        metricsCache.clear();
        metricsStatsCache.clear();
        metricsExpiry.clear();
        metricsStatsExpiry.clear();
        
        log.info("MetricsManager shut down successfully");
    }

}
