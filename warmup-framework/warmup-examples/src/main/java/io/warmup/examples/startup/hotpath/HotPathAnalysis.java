package io.warmup.examples.startup.hotpath;

import java.time.Instant;
import java.util.List;

/**
 * Analysis result of hot path identification and evaluation.
 * Contains identified hot paths, metrics, and analysis data.
 * 
 * @author MiniMax Agent
 * @version 1.0
 */
public class HotPathAnalysis {
    
    private final String analysisId;
    private final List<HotPathExecutionData> hotPaths;
    private final List<String> methodProfiles;
    private final List<String> dependencyGraph;
    private final double averageHotnessScore;
    private final double maxHotnessScore;
    private final long analysisDuration;
    private final Instant creationTime;
    
    public HotPathAnalysis(String analysisId, List<HotPathExecutionData> hotPaths,
                          List<String> methodProfiles, List<String> dependencyGraph,
                          double averageHotnessScore, double maxHotnessScore,
                          long analysisDuration) {
        this.analysisId = analysisId;
        this.hotPaths = new java.util.ArrayList<>(hotPaths);
        this.methodProfiles = new java.util.ArrayList<>(methodProfiles);
        this.dependencyGraph = new java.util.ArrayList<>(dependencyGraph);
        this.averageHotnessScore = averageHotnessScore;
        this.maxHotnessScore = maxHotnessScore;
        this.analysisDuration = analysisDuration;
        this.creationTime = Instant.now();
    }
    
    public String getAnalysisId() { return analysisId; }
    public List<HotPathExecutionData> getHotPaths() { return hotPaths; }
    public List<String> getMethodProfiles() { return methodProfiles; }
    public List<String> getDependencyGraph() { return dependencyGraph; }
    public double getAverageHotnessScore() { return averageHotnessScore; }
    public double getMaxHotnessScore() { return maxHotnessScore; }
    public long getAnalysisDuration() { return analysisDuration; }
    public Instant getCreationTime() { return creationTime; }
    
    /**
     * Calculate expected improvement based on analysis
     */
    public double getExpectedImprovement() {
        return Math.min(50.0, maxHotnessScore * 2.0); // Cap at 50%
    }
    
    /**
     * Get confidence level for this analysis
     */
    public ConfidenceLevel getConfidenceLevel() {
        if (hotPaths.size() > 10 && averageHotnessScore > 0.7) {
            return ConfidenceLevel.VERY_HIGH;
        } else if (hotPaths.size() > 5 && averageHotnessScore > 0.5) {
            return ConfidenceLevel.HIGH;
        } else if (hotPaths.size() > 2) {
            return ConfidenceLevel.MEDIUM;
        } else {
            return ConfidenceLevel.LOW;
        }
    }
    
    /**
     * Get performance metrics for this analysis
     */
    public java.util.Map<String, Object> getPerformanceMetrics() {
        java.util.Map<String, Object> metrics = new java.util.HashMap<>();
        metrics.put("averageHotnessScore", averageHotnessScore);
        metrics.put("maxHotnessScore", maxHotnessScore);
        metrics.put("hotPathCount", hotPaths.size());
        metrics.put("expectedImprovement", getExpectedImprovement());
        return metrics;
    }
    
    /**
     * Get hotness level based on average score
     */
    public String getHotnessLevel() {
        if (averageHotnessScore > 0.8) return "VERY_HOT";
        else if (averageHotnessScore > 0.6) return "HOT";
        else if (averageHotnessScore > 0.4) return "WARM";
        else return "COOL";
    }
    
    /**
     * Get hotness score (generic method for compatibility)
     */
    public <T> double getHotnessScore(T parameter) {
        return averageHotnessScore; // Return average score regardless of parameter
    }
    
    @Override
    public String toString() {
        return String.format("HotPathAnalysis{id='%s', hotPaths=%d, avgScore=%.2f}",
                           analysisId, hotPaths.size(), averageHotnessScore);
    }
}