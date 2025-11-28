package io.warmup.framework.health;

/**
 * Health check for memory usage
 */
public class MemoryHealthCheck implements HealthCheck {
    
    @Override
    public HealthResult check() {
        Runtime runtime = Runtime.getRuntime();
        long totalMemory = runtime.totalMemory();
        long freeMemory = runtime.freeMemory();
        long usedMemory = totalMemory - freeMemory;
        long maxMemory = runtime.maxMemory();
        
        double usagePercent = (double) usedMemory / maxMemory * 100;
        
        if (usagePercent > 90) {
            return HealthResult.down("Memory usage critical: " + String.format("%.2f", usagePercent) + "%");
        } else if (usagePercent > 80) {
            return HealthResult.degraded("Memory usage high: " + String.format("%.2f", usagePercent) + "%");
        } else {
            return HealthResult.up("Memory usage normal: " + String.format("%.2f", usagePercent) + "%");
        }
    }
}