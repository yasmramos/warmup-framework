package io.warmup.framework.hotreload.advanced;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * System health status for the hot reload dashboard.
 * Tracks overall system health and provides status information.
 * 
 * @author MiniMax Agent
 * @since 2.0
 */
public class SystemHealthStatus {
    
    private HealthStatus overallStatus = HealthStatus.UNKNOWN;
    private Instant lastUpdate = Instant.now();
    private Map<HealthMetric, Double> metrics = new ConcurrentHashMap<>();
    private Map<String, String> systemInfo = new ConcurrentHashMap<>();
    private String statusMessage = "System status unknown";
    private boolean isHealthy = false;
    
    /**
     * Health status enumeration.
     */
    public enum HealthStatus {
        HEALTHY("Healthy"),
        WARNING("Warning"),
        CRITICAL("Critical"),
        UNKNOWN("Unknown");
        
        private final String displayName;
        
        HealthStatus(String displayName) {
            this.displayName = displayName;
        }
        
        public String getDisplayName() {
            return displayName;
        }
    }
    
    /**
     * Health metrics enumeration.
     */
    public enum HealthMetric {
        CPU_USAGE("CPU Usage"),
        MEMORY_USAGE("Memory Usage"),
        RELOAD_SUCCESS_RATE("Reload Success Rate"),
        AVERAGE_RELOAD_TIME("Average Reload Time"),
        ACTIVE_CONNECTIONS("Active Connections"),
        ERROR_RATE("Error Rate");
        
        private final String displayName;
        
        HealthMetric(String displayName) {
            this.displayName = displayName;
        }
        
        public String getDisplayName() {
            return displayName;
        }
    }
    
    public SystemHealthStatus() {
        initializeDefaultMetrics();
    }
    
    private void initializeDefaultMetrics() {
        metrics.put(HealthMetric.CPU_USAGE, 0.0);
        metrics.put(HealthMetric.MEMORY_USAGE, 0.0);
        metrics.put(HealthMetric.RELOAD_SUCCESS_RATE, 100.0);
        metrics.put(HealthMetric.AVERAGE_RELOAD_TIME, 0.0);
        metrics.put(HealthMetric.ACTIVE_CONNECTIONS, 0.0);
        metrics.put(HealthMetric.ERROR_RATE, 0.0);
    }
    
    /**
     * Updates the overall health status.
     * 
     * @param status the health status
     * @param message the status message
     */
    public void updateStatus(HealthStatus status, String message) {
        this.overallStatus = status;
        this.statusMessage = message;
        this.lastUpdate = Instant.now();
        this.isHealthy = status == HealthStatus.HEALTHY;
    }
    
    /**
     * Updates a specific health metric.
     * 
     * @param metric the metric to update
     * @param value the metric value
     */
    public void updateMetric(HealthMetric metric, double value) {
        metrics.put(metric, value);
        this.lastUpdate = Instant.now();
        
        // Recalculate overall status based on metrics
        recalculateOverallStatus();
    }
    
    /**
     * Updates system information.
     * 
     * @param key the information key
     * @param value the information value
     */
    public void updateSystemInfo(String key, String value) {
        systemInfo.put(key, value);
        this.lastUpdate = Instant.now();
    }
    
    private void recalculateOverallStatus() {
        double cpuUsage = metrics.getOrDefault(HealthMetric.CPU_USAGE, 0.0);
        double memoryUsage = metrics.getOrDefault(HealthMetric.MEMORY_USAGE, 0.0);
        double successRate = metrics.getOrDefault(HealthMetric.RELOAD_SUCCESS_RATE, 100.0);
        double errorRate = metrics.getOrDefault(HealthMetric.ERROR_RATE, 0.0);
        
        if (cpuUsage > 90 || memoryUsage > 90 || errorRate > 10 || successRate < 70) {
            overallStatus = HealthStatus.CRITICAL;
            statusMessage = "Critical issues detected";
        } else if (cpuUsage > 70 || memoryUsage > 70 || errorRate > 5 || successRate < 85) {
            overallStatus = HealthStatus.WARNING;
            statusMessage = "Performance degradation detected";
        } else {
            overallStatus = HealthStatus.HEALTHY;
            statusMessage = "System operating normally";
        }
        
        isHealthy = overallStatus == HealthStatus.HEALTHY;
    }
    
    /**
     * Gets the overall health status.
     * 
     * @return the health status
     */
    public HealthStatus getOverallStatus() {
        return overallStatus;
    }
    
    /**
     * Gets the status message.
     * 
     * @return the status message
     */
    public String getStatusMessage() {
        return statusMessage;
    }
    
    /**
     * Gets the last update time.
     * 
     * @return the last update time
     */
    public Instant getLastUpdate() {
        return lastUpdate;
    }
    
    /**
     * Gets a specific metric value.
     * 
     * @param metric the metric
     * @return the metric value
     */
    public double getMetric(HealthMetric metric) {
        return metrics.getOrDefault(metric, 0.0);
    }
    
    /**
     * Gets all metrics.
     * 
     * @return the metrics map
     */
    public Map<HealthMetric, Double> getMetrics() {
        return new ConcurrentHashMap<>(metrics);
    }
    
    /**
     * Gets system information.
     * 
     * @return the system information map
     */
    public Map<String, String> getSystemInfo() {
        return new ConcurrentHashMap<>(systemInfo);
    }
    
    /**
     * Checks if the system is healthy.
     * 
     * @return true if system is healthy
     */
    public boolean isHealthy() {
        return isHealthy;
    }
    
    /**
     * Checks if the system has warnings.
     * 
     * @return true if system has warnings
     */
    public boolean hasWarnings() {
        return overallStatus == HealthStatus.WARNING;
    }
    
    /**
     * Checks if the system is in critical state.
     * 
     * @return true if system is critical
     */
    public boolean isCritical() {
        return overallStatus == HealthStatus.CRITICAL;
    }
    
    /**
     * Gets CPU usage percentage.
     * 
     * @return CPU usage percentage
     */
    public double getCpuUsage() {
        return getMetric(HealthMetric.CPU_USAGE);
    }
    
    /**
     * Gets memory usage percentage.
     * 
     * @return memory usage percentage
     */
    public double getMemoryUsage() {
        return getMetric(HealthMetric.MEMORY_USAGE);
    }
    
    /**
     * Gets reload success rate percentage.
     * 
     * @return success rate percentage
     */
    public double getReloadSuccessRate() {
        return getMetric(HealthMetric.RELOAD_SUCCESS_RATE);
    }
    
    /**
     * Gets average reload time in milliseconds.
     * 
     * @return average reload time
     */
    public double getAverageReloadTime() {
        return getMetric(HealthMetric.AVERAGE_RELOAD_TIME);
    }
    
    /**
     * Gets error rate percentage.
     * 
     * @return error rate percentage
     */
    public double getErrorRate() {
        return getMetric(HealthMetric.ERROR_RATE);
    }
    
    /**
     * Gets active connections count.
     * 
     * @return active connections count
     */
    public double getActiveConnections() {
        return getMetric(HealthMetric.ACTIVE_CONNECTIONS);
    }
    
    @Override
    public String toString() {
        return String.format(
            "SystemHealthStatus{status=%s, healthy=%s, message='%s', lastUpdate=%s, metrics=%s}",
            overallStatus.getDisplayName(),
            isHealthy,
            statusMessage,
            lastUpdate,
            metrics
        );
    }
}