package io.warmup.framework.startup.hotpath;

import java.util.List;

/**
 * Single optimization action within a code reordering plan.
 * Represents a specific optimization operation to be performed.
 * 
 * @author MiniMax Agent
 * @version 1.0
 */
public class OptimizationAction {
    
    private final String actionId;
    private final String description;
    private final OptimizationType type;
    private final List<String> involvedMethods;
    private final List<String> involvedClasses;
    private final List<String> rollbackSteps;
    private final double expectedImprovement;
    private final RiskLevel riskLevel;
    private final Priority priority;
    
    public OptimizationAction(String actionId, String description, OptimizationType type,
                            List<String> involvedMethods, List<String> involvedClasses,
                            List<String> rollbackSteps, double expectedImprovement,
                            RiskLevel riskLevel, Priority priority) {
        this.actionId = actionId;
        this.description = description;
        this.type = type;
        this.involvedMethods = new java.util.ArrayList<>(involvedMethods);
        this.involvedClasses = new java.util.ArrayList<>(involvedClasses);
        this.rollbackSteps = new java.util.ArrayList<>(rollbackSteps);
        this.expectedImprovement = expectedImprovement;
        this.riskLevel = riskLevel;
        this.priority = priority;
    }
    
    public String getActionId() { return actionId; }
    public String getDescription() { return description; }
    public OptimizationType getType() { return type; }
    public List<String> getInvolvedMethods() { return involvedMethods; }
    public List<String> getInvolvedClasses() { return involvedClasses; }
    public List<String> getRollbackSteps() { return rollbackSteps; }
    public double getExpectedImprovement() { return expectedImprovement; }
    public RiskLevel getRiskLevel() { return riskLevel; }
    public Priority getPriority() { return priority; }
    
    @Override
    public String toString() {
        return String.format("OptimizationAction{id='%s', type=%s, improvement=%.1f%%}",
                           actionId, type, expectedImprovement);
    }
}