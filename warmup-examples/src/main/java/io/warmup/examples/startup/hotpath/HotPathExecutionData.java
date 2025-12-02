package io.warmup.examples.startup.hotpath;

import java.time.Instant;
import java.util.List;

/**
 * Execution data for a single hot path analysis.
 * Contains method call information, timing, and hotness metrics.
 * 
 * @author MiniMax Agent
 * @version 1.0
 */
public class HotPathExecutionData {
    
    private final String methodName;
    private final String className;
    private final String fullMethodName;
    private final String description;
    private final HotnessLevel hotnessLevel;
    private final double hotnessScore;
    private final List<String> involvedMethods;
    private final long executionCount;
    private final long totalExecutionTime;
    private final long averageExecutionTime;
    private final Instant firstExecution;
    private final Instant lastExecution;
    
    public HotPathExecutionData(String methodName, String className, String fullMethodName,
                               String description, HotnessLevel hotnessLevel, double hotnessScore,
                               List<String> involvedMethods, long executionCount,
                               long totalExecutionTime, long averageExecutionTime) {
        this.methodName = methodName;
        this.className = className;
        this.fullMethodName = fullMethodName;
        this.description = description;
        this.hotnessLevel = hotnessLevel;
        this.hotnessScore = hotnessScore;
        this.involvedMethods = new java.util.ArrayList<>(involvedMethods);
        this.executionCount = executionCount;
        this.totalExecutionTime = totalExecutionTime;
        this.averageExecutionTime = averageExecutionTime;
        this.firstExecution = Instant.now();
        this.lastExecution = Instant.now();
    }
    
    public String getMethodName() { return methodName; }
    public String getClassName() { return className; }
    public String getFullMethodName() { return fullMethodName; }
    public String getDescription() { return description; }
    public HotnessLevel getHotnessLevel() { return hotnessLevel; }
    public double getHotnessScore() { return hotnessScore; }
    public List<String> getInvolvedMethods() { return involvedMethods; }
    public long getExecutionCount() { return executionCount; }
    public long getTotalExecutionTime() { return totalExecutionTime; }
    public long getAverageExecutionTime() { return averageExecutionTime; }
    public Instant getFirstExecution() { return firstExecution; }
    public Instant getLastExecution() { return lastExecution; }
}