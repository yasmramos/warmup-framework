package io.warmup.examples.startup.hotpath;

import java.time.Instant;
import java.util.List;

/**
 * Result of an optimization action execution.
 * Contains the outcome of a single optimization operation.
 * 
 * @author MiniMax Agent
 * @version 1.0
 */
public class OptimizationResult {
    
    private final String resultId;
    private final String actionId;
    private final boolean success;
    private final String message;
    private final double improvementAchieved;
    private final long executionTime;
    private final Instant executionTimeStamp;
    private final List<String> sideEffects;
    
    public OptimizationResult(String resultId, String actionId, boolean success,
                             String message, double improvementAchieved, long executionTime) {
        this.resultId = resultId;
        this.actionId = actionId;
        this.success = success;
        this.message = message;
        this.improvementAchieved = improvementAchieved;
        this.executionTime = executionTime;
        this.executionTimeStamp = Instant.now();
        this.sideEffects = new java.util.ArrayList<>();
    }
    
    public String getResultId() { return resultId; }
    public String getActionId() { return actionId; }
    public boolean isSuccess() { return success; }
    public String getMessage() { return message; }
    public double getImprovementAchieved() { return improvementAchieved; }
    public long getExecutionTime() { return executionTime; }
    public Instant getExecutionTimeStamp() { return executionTimeStamp; }
    public List<String> getSideEffects() { return sideEffects; }
    
    /**
     * Get applied actions for this result
     */
    public List<String> getAppliedActions() {
        return new java.util.ArrayList<>(sideEffects);
    }
    
    /**
     * Get actual improvement achieved (alias for getImprovementAchieved)
     */
    public double getActualImprovement() {
        return improvementAchieved;
    }
    
    /**
     * Get failed actions (actions that did not succeed)
     */
    public List<String> getFailedActions() {
        return success ? java.util.Collections.emptyList() : 
               java.util.Collections.singletonList(actionId);
    }
    
    @Override
    public String toString() {
        return String.format("OptimizationResult{id='%s', success=%s, improvement=%.1f%%}",
                           resultId, success, improvementAchieved);
    }
}