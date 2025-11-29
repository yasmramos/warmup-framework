package io.warmup.examples.startup.examples;

/**
 * System optimization summary for comprehensive startup example.
 * Provides aggregated information about system optimization results.
 * 
 * @author MiniMax Agent
 * @since 1.0
 */
public class SystemOptimizationSummary {
    
    private final boolean optimizationSuccessful;
    private final double totalImprovementPercentage;
    private final long totalOptimizationTimeMs;
    private final int systemsOptimized;
    private final String optimizationSummary;
    private final String detailedReport;
    
    /**
     * Constructor for SystemOptimizationSummary
     */
    public SystemOptimizationSummary(boolean optimizationSuccessful,
                                   double totalImprovementPercentage,
                                   long totalOptimizationTimeMs,
                                   int systemsOptimized,
                                   String optimizationSummary,
                                   String detailedReport) {
        this.optimizationSuccessful = optimizationSuccessful;
        this.totalImprovementPercentage = totalImprovementPercentage;
        this.totalOptimizationTimeMs = totalOptimizationTimeMs;
        this.systemsOptimized = systemsOptimized;
        this.optimizationSummary = optimizationSummary;
        this.detailedReport = detailedReport;
    }
    
    /**
     * Checks if optimization was successful
     */
    public boolean isOptimizationSuccessful() {
        return optimizationSuccessful;
    }
    
    /**
     * Gets total improvement percentage
     */
    public double getTotalImprovementPercentage() {
        return totalImprovementPercentage;
    }
    
    /**
     * Gets total optimization time in milliseconds
     */
    public long getTotalOptimizationTimeMs() {
        return totalOptimizationTimeMs;
    }
    
    /**
     * Gets number of systems optimized
     */
    public int getSystemsOptimized() {
        return systemsOptimized;
    }
    
    /**
     * Gets optimization summary
     */
    public String getOptimizationSummary() {
        return optimizationSummary;
    }
    
    /**
     * Gets detailed report
     */
    public String getDetailedReport() {
        return detailedReport;
    }
    
    /**
     * Creates a successful summary
     */
    public static SystemOptimizationSummary createSuccessful(
            double improvementPercentage, int systemsOptimized, long timeMs) {
        return new SystemOptimizationSummary(
            true, improvementPercentage, timeMs, systemsOptimized,
            String.format("System optimization completed successfully with %.2f%% improvement", improvementPercentage),
            generateDetailedReport(improvementPercentage, systemsOptimized, timeMs)
        );
    }
    
    /**
     * Creates a failed summary
     */
    public static SystemOptimizationSummary createFailed(String reason) {
        return new SystemOptimizationSummary(
            false, 0.0, 0, 0,
            "System optimization failed: " + reason,
            "Failed system optimization: " + reason
        );
    }
    
    private static String generateDetailedReport(double improvement, int systems, long time) {
        return String.format(
            "System Optimization Report:\n" +
            "- Total improvement: %.2f%%\n" +
            "- Systems optimized: %d\n" +
            "- Processing time: %dms\n" +
            "- Status: SUCCESS",
            improvement, systems, time
        );
    }
    
    @Override
    public String toString() {
        return String.format("SystemOptimizationSummary{successful=%s, improvement=%.2f%%, systems=%d}",
            optimizationSuccessful, totalImprovementPercentage, systemsOptimized);
    }
}