package io.warmup.framework.health;

import io.warmup.framework.core.WarmupContainer;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.Level;
import java.util.logging.Logger;

public class HealthCheckManager {

    private static final Logger log = Logger.getLogger(HealthCheckManager.class.getName());
    
    // O(1) Atomic Counters for Real-time Statistics
    private final AtomicInteger healthCheckExecutions = new AtomicInteger(0);
    private final AtomicInteger cachedHealthResults = new AtomicInteger(0);
    private final AtomicInteger healthCheckRegistrations = new AtomicInteger(0);
    private final AtomicInteger healthyChecksCount = new AtomicInteger(0);
    private final AtomicInteger unhealthyChecksCount = new AtomicInteger(0);
    private final AtomicLong totalHealthCheckDuration = new AtomicLong(0);
    
    // O(1) TTL Caches - Health Results (5s), Health Status (5s)
    private Map<String, Object> healthResultsCache = new ConcurrentHashMap<>();
    private Map<String, Long> healthResultsExpiry = new ConcurrentHashMap<>();
    private static final long HEALTH_RESULTS_TTL = TimeUnit.SECONDS.toMillis(5);
    
    private Map<String, Object> healthStatusCache = new ConcurrentHashMap<>();
    private Map<String, Long> healthStatusExpiry = new ConcurrentHashMap<>();
    private static final long HEALTH_STATUS_TTL = TimeUnit.SECONDS.toMillis(5);
    
    // O(1) Cache Invalidation Flags
    private volatile boolean healthChecksDirty = false;
    private volatile boolean healthResultsDirty = false;
    private volatile boolean healthStatusDirty = false;
    
    private final Map<String, HealthCheck> healthChecks = new ConcurrentHashMap<>();
    private final ExecutorService healthCheckExecutor = Executors.newCachedThreadPool();
    private volatile Map<String, HealthResult> lastHealthResults;
    private volatile long lastHealthCheckTime = 0;
    private WarmupContainer container; // Ahora puede ser null inicialmente
    private static final long DEFAULT_HEALTH_CHECK_TIMEOUT = 30_000; // 30 seconds

    public HealthCheckManager(WarmupContainer container) {
        this.container = container;
    }
    
    public HealthCheckManager() {
        this.container = null;
    }

    public void setContainer(WarmupContainer container) {
        this.container = container;
    }

    public void registerHealthCheck(HealthCheck healthCheck) {
        healthChecks.put(healthCheck.getName(), healthCheck);
        healthCheckRegistrations.incrementAndGet();
        
        // O(1) Cache invalidation
        healthChecksDirty = true;
        invalidateHealthStatusCache();
        
        log.log(Level.INFO, "Health check registrado: {0}", healthCheck.getName());
    }

    public void registerHealthCheck(String name, HealthCheck healthCheck) {
        healthChecks.put(name, healthCheck);
        healthCheckRegistrations.incrementAndGet();
        
        // O(1) Cache invalidation
        healthChecksDirty = true;
        invalidateHealthStatusCache();
        
        log.log(Level.INFO, "Health check registrado: {0}", name);
    }

    // O(1) Optimized health check with TTL caching
    public Map<String, HealthResult> checkHealth() {
        healthCheckExecutions.incrementAndGet();
        long startTime = System.currentTimeMillis();
        
        String cacheKey = "allHealthResults";
        
        try {
            // Defensive validation - check if cache maps are properly initialized
            if (healthResultsCache == null || healthResultsExpiry == null) {
                log.warning("Health results cache maps are null, clearing and reinitializing");
                synchronized (this) {
                    if (healthResultsCache == null || healthResultsExpiry == null) {
                        // Use HashMap as fallback if ConcurrentHashMap fails
                        healthResultsCache = new ConcurrentHashMap<>();
                        healthResultsExpiry = new ConcurrentHashMap<>();
                    }
                }
            }
            
            // O(1) Cache check with TTL validation
            if (!healthResultsDirty && lastHealthResults != null && 
                isCacheValid(cacheKey, healthResultsCache, healthResultsExpiry)) {
                cachedHealthResults.incrementAndGet();
                log.info("Usando resultados cacheados de health checks");
                return new HashMap<>(lastHealthResults);
            }
        } catch (Exception e) {
            log.log(Level.WARNING, "Error accessing health results cache: " + e.getMessage(), e);
            // Force refresh by marking cache as dirty
            healthResultsDirty = true;
            lastHealthResults = null;
        }

        Map<String, HealthResult> results = new ConcurrentHashMap<>();
        List<Future<?>> futures = new ArrayList<>();

        for (Map.Entry<String, HealthCheck> entry : healthChecks.entrySet()) {
            String name = entry.getKey();
            HealthCheck check = entry.getValue();
            
            futures.add(healthCheckExecutor.submit(() -> {
                try {
                    HealthResult result = check.check();
                    results.put(name, result);
                    logHealthResult(name, result);
                } catch (Exception e) {
                    HealthResult errorResult = HealthResult.down("Health check execution failed", e);
                    results.put(name, errorResult);
                    log.log(Level.SEVERE, "Health check [{0}] failed: {1}", new Object[]{name, e.getMessage()});
                }
            }));
        }

        // Esperar a que todos terminen con timeout
        waitForFutures(futures);

        // Guardar en cache con O(1) operations
        try {
            lastHealthResults = new ConcurrentHashMap<>(results);
            lastHealthCheckTime = System.currentTimeMillis();
            
            // O(1) Cache storage with defensive checks - usar Boolean.TRUE como marker
            if (healthResultsCache != null) {
                healthResultsCache.put(cacheKey, Boolean.TRUE); // Store marker for TTL tracking
            }
            if (healthResultsExpiry != null) {
                healthResultsExpiry.put(cacheKey, System.currentTimeMillis() + HEALTH_RESULTS_TTL);
            }
            healthResultsDirty = false;
        } catch (Exception e) {
            log.log(Level.WARNING, "Error caching health results: " + e.getMessage(), e);
            // Force fresh cache on next call
            lastHealthResults = null;
            healthResultsDirty = true;
        }
        
        long duration = System.currentTimeMillis() - startTime;
        totalHealthCheckDuration.addAndGet(duration);
        log.log(Level.INFO, "Health checks completados en {0}ms", duration);
        
        return results;
    }

    private void logHealthResult(String name, HealthResult result) {
        if (result.isHealthy()) {
            log.log(Level.INFO, "[{0}] {1} - {2}", 
                    new Object[]{name, result.getStatus(), result.getMessage()});
        } else {
            log.log(Level.SEVERE, "[{0}] {1} - {2}", 
                    new Object[]{name, result.getStatus(), result.getMessage()});
            if (result.getError() != null) {
                log.log(Level.SEVERE, "Error en [{0}]: {1}", 
                        new Object[]{name, result.getError().getMessage()});
            }
        }
    }

    private void waitForFutures(List<Future<?>> futures) {
        for (Future<?> future : futures) {
            try {
                future.get(DEFAULT_HEALTH_CHECK_TIMEOUT, TimeUnit.MILLISECONDS);
            } catch (TimeoutException e) {
                log.log(Level.SEVERE, "Health check timeout: {0}", e.getMessage());
                future.cancel(true);
            } catch (InterruptedException e) {
                log.log(Level.SEVERE, "Health check interrumpido: {0}", e.getMessage());
                Thread.currentThread().interrupt();
                break;
            } catch (ExecutionException e) {
                log.log(Level.SEVERE, "Error ejecutando health check: {0}", e.getMessage());
            }
        }
    }
    
    // O(1) Cache invalidation helpers
    private void invalidateHealthStatusCache() {
        try {
            if (healthStatusCache != null) {
                healthStatusCache.clear();
            }
            if (healthStatusExpiry != null) {
                healthStatusExpiry.clear();
            }
            healthStatusDirty = true;
        } catch (Exception e) {
            log.log(Level.WARNING, "Error invalidating health status cache: " + e.getMessage(), e);
        }
    }
    
    private void invalidateHealthResultsCache() {
        try {
            if (healthResultsCache != null) {
                healthResultsCache.clear();
            }
            if (healthResultsExpiry != null) {
                healthResultsExpiry.clear();
            }
            healthResultsDirty = true;
        } catch (Exception e) {
            log.log(Level.WARNING, "Error invalidating health results cache: " + e.getMessage(), e);
        }
    }
    
    private boolean isCacheValid(String key, Map<String, ?> cache, Map<String, Long> expiry) {
        try {
            // Defensive checks for null parameters
            if (key == null || cache == null || expiry == null) {
                log.log(Level.FINE, "Cache validation failed: key={0}, cache={1}, expiry={2}", 
                        new Object[]{key != null, cache != null, expiry != null});
                return false;
            }
            
            // Check if cache key exists and has the expected marker
            if (!cache.containsKey(key)) {
                log.log(Level.FINE, "Cache key not found: {0}", key);
                return false;
            }
            
            // Verify the marker is present (Boolean.TRUE for health results)
            Object marker = cache.get(key);
            if (marker == null) {
                log.log(Level.FINE, "Cache marker is null for key: {0}", key);
                return false;
            }
            
            Long expireTime = expiry.get(key);
            if (expireTime == null) {
                return false;
            }
            
            long currentTime = System.currentTimeMillis();
            boolean valid = currentTime < expireTime;
            
            if (!valid) {
                log.log(Level.FINE, "Cache expired for key: {0}, expired at: {1}, current: {2}", 
                        new Object[]{key, expireTime, currentTime});
            }
            
            return valid;
        } catch (Exception e) {
            log.log(Level.WARNING, "Error validating cache for key: " + key + ": " + e.getMessage(), e);
            return false;
        }
    }

    public Map<String, HealthResult> checkHealthForceRefresh() {
        lastHealthResults = null;
        return checkHealth();
    }

    // O(1) Optimized health status with TTL caching
    public Map<String, Object> getHealthStatus() {
        String cacheKey = "healthStatus";
        
        // O(1) Cache check
        if (!healthStatusDirty && isCacheValid(cacheKey, healthStatusCache, healthStatusExpiry)) {
            @SuppressWarnings("unchecked")
            Map<String, Object> cached = (Map<String, Object>) healthStatusCache.get(cacheKey);
            if (cached != null) return cached;
        }
        
        Map<String, Object> status = getHealthStatus(checkHealth());
        
        // O(1) Cache storage
        healthStatusCache.put(cacheKey, status);
        healthStatusExpiry.put(cacheKey, System.currentTimeMillis() + HEALTH_STATUS_TTL);
        healthStatusDirty = false;
        
        return status;
    }

    public Map<String, Object> getHealthStatus(Map<String, HealthResult> healthResults) {
        Map<String, Object> status = new HashMap<>();
        status.put("timestamp", System.currentTimeMillis());
        
        boolean allHealthy = true;
        for (HealthResult result : healthResults.values()) {
            if (result.getStatus() != HealthStatus.UP) {
                allHealthy = false;
                break;
            }
        }
        
        status.put("status", allHealthy ? "UP" : "DOWN");
        status.put("checks", healthResults);
        
        // Crear mapa de métricas compatible con Java 8
        Map<String, Object> metrics = new HashMap<>();
        if (container != null) {
            metrics.put("uptime", container.getUptime());
            metrics.put("formattedUptime", container.getFormattedUptime());
        } else {
            metrics.put("uptime", "N/A");
            metrics.put("formattedUptime", "N/A");
        }
        status.put("metrics", metrics);
        
        return status;
    }

    // O(1) Optimized health summary with atomic counters
    public HealthCheckSummary getHealthSummary() {
        Map<String, HealthResult> results = checkHealth();
        long total = results.size();
        long healthy = 0;
        long unhealthy = 0;
        
        for (HealthResult result : results.values()) {
            if (result.isHealthy()) {
                healthy++;
                healthyChecksCount.incrementAndGet();
            } else {
                unhealthy++;
                unhealthyChecksCount.incrementAndGet();
            }
        }
        
        return new HealthCheckSummary(total, healthy, unhealthy, results);
    }

    public boolean isHealthy() {
        Map<String, HealthResult> results = checkHealth();
        for (HealthResult result : results.values()) {
            if (result.getStatus() != HealthStatus.UP) {
                return false;
            }
        }
        return true;
    }

    public HealthResult getHealthCheck(String name) {
        HealthCheck check = healthChecks.get(name);
        if (check != null) {
            try {
                return check.check();
            } catch (Exception e) {
                log.log(Level.SEVERE, "Health check [{0}] threw exception: {1}", new Object[]{name, e.getMessage()});
                return HealthResult.down("Health check failed with exception", e);
            }
        }
        return HealthResult.unknown("Health check not found: " + name);
    }

    public void removeHealthCheck(String name) {
        HealthCheck removed = healthChecks.remove(name);
        if (removed != null) {
            healthCheckRegistrations.decrementAndGet();
            
            // O(1) Cache invalidation
            healthChecksDirty = true;
            invalidateHealthResultsCache();
            invalidateHealthStatusCache();
            
            log.log(Level.INFO, "Health check removido: {0}", name);
        }
    }

    public Set<String> getHealthCheckNames() {
        return new HashSet<>(healthChecks.keySet());
    }

    public int getHealthCheckCount() {
        return healthChecks.size();
    }

    public void clearHealthChecks() {
        int count = healthChecks.size();
        healthChecks.clear();
        healthCheckRegistrations.set(0);
        
        // O(1) Clear all caches with defensive checks
        try {
            if (healthResultsCache != null) {
                healthResultsCache.clear();
            }
            if (healthResultsExpiry != null) {
                healthResultsExpiry.clear();
            }
            if (healthStatusCache != null) {
                healthStatusCache.clear();
            }
            if (healthStatusExpiry != null) {
                healthStatusExpiry.clear();
            }
        } catch (Exception e) {
            log.log(Level.WARNING, "Error clearing caches: " + e.getMessage(), e);
        }
        
        // O(1) Reset dirty flags
        healthChecksDirty = false;
        healthResultsDirty = false;
        healthStatusDirty = false;
        
        log.log(Level.INFO, "Todos los health checks removidos: {0} eliminados", count);
    }

    // O(1) Atomic counter getters
    public int getHealthCheckExecutions() {
        return healthCheckExecutions.get();
    }
    
    public int getCachedHealthResults() {
        return cachedHealthResults.get();
    }
    
    public int getHealthCheckRegistrations() {
        return healthCheckRegistrations.get();
    }
    
    public int getHealthyChecksCount() {
        return healthyChecksCount.get();
    }
    
    public int getUnhealthyChecksCount() {
        return unhealthyChecksCount.get();
    }
    
    public long getTotalHealthCheckDuration() {
        return totalHealthCheckDuration.get();
    }
    
    public void shutdown() {
        log.info("Apagando HealthCheckManager...");
        healthCheckExecutor.shutdown();
        try {
            if (!healthCheckExecutor.awaitTermination(5, TimeUnit.SECONDS)) {
                healthCheckExecutor.shutdownNow();
                log.warning("HealthCheckExecutor no terminó en 5 segundos, forzando cierre...");
            }
        } catch (InterruptedException e) {
            healthCheckExecutor.shutdownNow();
            Thread.currentThread().interrupt();
            log.severe("Interrumpido durante el apagado de HealthCheckManager");
        }
        log.info("HealthCheckManager apagado correctamente.");
    }

    /**
     * Initializes the health check manager
     */
    public void initialize() {
        log.log(Level.INFO, "Initializing HealthCheckManager with O(1) optimizations");
        
        // Initialize health check counters
        healthCheckExecutions.set(0);
        cachedHealthResults.set(0);
        healthCheckRegistrations.set(0);
        healthyChecksCount.set(0);
        unhealthyChecksCount.set(0);
        totalHealthCheckDuration.set(0);
        
        // Reset dirty flags
        healthChecksDirty = false;
        healthResultsDirty = false;
        healthStatusDirty = false;
        
        log.log(Level.FINE, "HealthCheckManager initialized successfully");
    }
    
    /**
     * Starts health monitoring
     */
    public void startHealthMonitoring() {
        log.log(Level.INFO, "Starting health monitoring");
        
        // Perform initial health check
        checkHealth();
        
        log.log(Level.FINE, "Health monitoring started");
    }
    
    /**
     * Stops health monitoring
     */
    public void stopHealthMonitoring() {
        log.log(Level.INFO, "Stopping health monitoring");
        
        // Perform final health check
        checkHealth();
        
        log.log(Level.FINE, "Health monitoring stopped");
    }

    /**
     * Check overall health across all registered health checks
     */
    public HealthResult checkOverallHealth() {
        Map<String, HealthResult> results = checkHealth();
        
        boolean allHealthy = true;
        StringBuilder message = new StringBuilder();
        int totalChecks = results.size();
        int healthyChecks = 0;
        int unhealthyChecks = 0;
        
        for (Map.Entry<String, HealthResult> entry : results.entrySet()) {
            HealthResult result = entry.getValue();
            if (result.isHealthy()) {
                healthyChecks++;
            } else {
                unhealthyChecks++;
                allHealthy = false;
                if (message.length() > 0) {
                    message.append("; ");
                }
                message.append(entry.getKey()).append(": ").append(result.getMessage());
            }
        }
        
        if (totalChecks == 0) {
            return HealthResult.unknown("No health checks registered");
        }
        
        if (allHealthy) {
            return HealthResult.up(String.format("All %d health checks are healthy", totalChecks));
        } else {
            return HealthResult.down(String.format("%d checks healthy, %d unhealthy: %s", 
                healthyChecks, unhealthyChecks, message.toString()));
        }
    }
}