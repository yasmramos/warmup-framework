package io.warmup.framework.startup.hotpath;

import java.time.Instant;
import java.util.List;

/**
 * Standalone version of CodeReorderingPlan for external usage.
 * Represents an optimization plan for code reordering based on hot path analysis.
 * 
 * @author MiniMax Agent
 * @version 1.0
 */
public class CodeReorderingPlan {
    
    private final String planId;
    private final String description;
    private final List<OptimizationAction> actions;
    private final double expectedImprovement;
    private final RiskLevel riskLevel;
    private final ConfidenceLevel confidenceLevel;
    private final Instant creationTime;
    
    public CodeReorderingPlan(String planId, String description, List<OptimizationAction> actions,
                             double expectedImprovement, RiskLevel riskLevel, 
                             ConfidenceLevel confidenceLevel) {
        this.planId = planId;
        this.description = description;
        this.actions = new java.util.ArrayList<>(actions);
        this.expectedImprovement = expectedImprovement;
        this.riskLevel = riskLevel;
        this.confidenceLevel = confidenceLevel;
        this.creationTime = Instant.now();
    }
    
    public String getPlanId() { return planId; }
    public String getDescription() { return description; }
    public List<OptimizationAction> getActions() { return actions; }
    public double getExpectedImprovement() { return expectedImprovement; }
    public RiskLevel getRiskLevel() { return riskLevel; }
    public ConfidenceLevel getConfidenceLevel() { return confidenceLevel; }
    public Instant getCreationTime() { return creationTime; }
    
    @Override
    public String toString() {
        return String.format("CodeReorderingPlan{id='%s', improvement=%.1f%%, risk=%s}",
                           planId, expectedImprovement, riskLevel);
    }
}