package io.warmup.examples.startup.hotpath;

import java.time.Instant;
import java.util.List;
import java.util.Map;

/**
 * Result of hot path optimization analysis and execution.
 * Contains optimization results, metrics, and recommendations.
 * 
 * @author MiniMax Agent
 * @version 1.0
 */
public class HotPathOptimizationResult {
    
    private final String resultId;
    private final String description;
    private final boolean success;
    private final List<String> optimizedMethods;
    private final List<String> failedOptimizations;
    private final double expectedImprovement;
    private final double actualImprovement;
    private final Map<String, Object> metrics;
    private final List<String> recommendations;
    private final Instant executionTime;
    private final SystemState state;
    
    public HotPathOptimizationResult(String resultId, String description, boolean success,
                                    List<String> optimizedMethods, List<String> failedOptimizations,
                                    double expectedImprovement, double actualImprovement,
                                    Map<String, Object> metrics, List<String> recommendations,
                                    SystemState state) {
        this.resultId = resultId;
        this.description = description;
        this.success = success;
        this.optimizedMethods = new java.util.ArrayList<>(optimizedMethods);
        this.failedOptimizations = new java.util.ArrayList<>(failedOptimizations);
        this.expectedImprovement = expectedImprovement;
        this.actualImprovement = actualImprovement;
        this.metrics = metrics;
        this.recommendations = new java.util.ArrayList<>(recommendations);
        this.executionTime = Instant.now();
        this.state = state;
    }
    
    public String getResultId() { return resultId; }
    public String getDescription() { return description; }
    public boolean isSuccess() { return success; }
    public List<String> getOptimizedMethods() { return optimizedMethods; }
    public List<String> getFailedOptimizations() { return failedOptimizations; }
    public double getExpectedImprovement() { return expectedImprovement; }
    public double getActualImprovement() { return actualImprovement; }
    public Map<String, Object> getMetrics() { return metrics; }
    public List<String> getRecommendations() { return recommendations; }
    public Instant getExecutionTime() { return executionTime; }
    public SystemState getState() { return state; }
    
    @Override
    public String toString() {
        return String.format("HotPathOptimizationResult{id='%s', success=%s, improvement=%.1f%%}",
                           resultId, success, actualImprovement);
    }
}