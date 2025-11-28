package io.warmup.framework.startup.bootstrap.examples;

import java.util.List;

/**
 * Comprehensive result of startup optimization.
 * Contains detailed information about optimization results and improvements.
 * 
 * @author MiniMax Agent
 * @since 1.0
 */
public class ComprehensiveStartupOptimizationResult {
    
    private final boolean optimizationSuccessful;
    private final long optimizationTimeMs;
    private final double improvementPercentage;
    private final int phasesOptimized;
    private final String optimizationSummary;
    private final String detailedReport;
    
    /**
     * Constructor for ComprehensiveStartupOptimizationResult
     */
    public ComprehensiveStartupOptimizationResult(boolean optimizationSuccessful,
                                                 long optimizationTimeMs,
                                                 double improvementPercentage,
                                                 int phasesOptimized,
                                                 String optimizationSummary,
                                                 String detailedReport) {
        this.optimizationSuccessful = optimizationSuccessful;
        this.optimizationTimeMs = optimizationTimeMs;
        this.improvementPercentage = improvementPercentage;
        this.phasesOptimized = phasesOptimized;
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
     * Gets optimization time in milliseconds
     */
    public long getOptimizationTimeMs() {
        return optimizationTimeMs;
    }
    
    /**
     * Gets improvement percentage
     */
    public double getImprovementPercentage() {
        return improvementPercentage;
    }
    
    /**
     * Gets number of phases optimized
     */
    public int getPhasesOptimized() {
        return phasesOptimized;
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
     * Gets estimated startup improvement percentage
     */
    public double getEstimatedStartupImprovementPercent() {
        return improvementPercentage;
    }
    
    /**
     * Gets overall optimization score
     */
    public double getOverallOptimizationScore() {
        if (!optimizationSuccessful) {
            return 0.0;
        }
        return Math.min(100.0, improvementPercentage * 10); // Scale to 0-100
    }
    
    /**
     * Gets total optimization time in milliseconds
     */
    public long getTotalOptimizationTimeMs() {
        return optimizationTimeMs;
    }
    
    /**
     * Gets optimization recommendations
     */
    public List<String> getOptimizationRecommendations() {
        List<String> recommendations = new java.util.ArrayList<>();
        
        if (!optimizationSuccessful) {
            recommendations.add("Review the detailed report and fix identified issues before retrying optimization.");
            return recommendations;
        }
        
        recommendations.add("Optimization completed successfully.");
        
        if (improvementPercentage < 10) {
            recommendations.add("Consider more aggressive optimization strategies");
        }
        if (optimizationTimeMs > 1000) {
            recommendations.add("Optimization took longer than expected, consider caching");
        }
        if (phasesOptimized < 3) {
            recommendations.add("Optimize more startup phases for better results");
        }
        
        recommendations.add("Monitor startup performance in production");
        recommendations.add("Consider periodic re-optimization");
        
        return recommendations;
    }
    
    /**
     * Creates a successful result
     */
    public static ComprehensiveStartupOptimizationResult createSuccessful(
            long optimizationTimeMs, double improvementPercentage, int phasesOptimized) {
        return new ComprehensiveStartupOptimizationResult(
            true, optimizationTimeMs, improvementPercentage, phasesOptimized,
            String.format("Optimization completed successfully with %.2f%% improvement", improvementPercentage),
            generateDetailedReport(improvementPercentage, phasesOptimized, optimizationTimeMs)
        );
    }
    
    /**
     * Creates a failed result
     */
    public static ComprehensiveStartupOptimizationResult createFailed(String reason) {
        return new ComprehensiveStartupOptimizationResult(
            false, 0, 0.0, 0,
            "Optimization failed: " + reason,
            "Failed optimization: " + reason
        );
    }
    
    private static String generateDetailedReport(double improvement, int phases, long time) {
        return String.format(
            "Optimization Report:\n" +
            "- Improvement: %.2f%%\n" +
            "- Phases optimized: %d\n" +
            "- Processing time: %dms\n" +
            "- Status: SUCCESS",
            improvement, phases, time
        );
    }
    
    @Override
    public String toString() {
        return String.format("ComprehensiveStartupOptimizationResult{successful=%s, improvement=%.2f%%, phases=%d}",
            optimizationSuccessful, improvementPercentage, phasesOptimized);
    }
}