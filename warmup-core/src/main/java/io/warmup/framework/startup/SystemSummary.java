package io.warmup.framework.startup;

/**
 * System summary for startup phases.
 * Provides an overview of system startup metrics and status.
 * 
 * @author MiniMax Agent
 * @since 1.0
 */
public class SystemSummary {
    
    private final long totalStartupTime;
    private final int totalPhasesCompleted;
    private final boolean startupSuccessful;
    private final String summaryMessage;
    
    /**
     * Constructor for SystemSummary
     */
    public SystemSummary(long totalStartupTime, int totalPhasesCompleted, 
                        boolean startupSuccessful, String summaryMessage) {
        this.totalStartupTime = totalStartupTime;
        this.totalPhasesCompleted = totalPhasesCompleted;
        this.startupSuccessful = startupSuccessful;
        this.summaryMessage = summaryMessage;
    }
    
    /**
     * Gets the total startup time in milliseconds
     */
    public long getTotalStartupTime() {
        return totalStartupTime;
    }
    
    /**
     * Gets the number of phases completed
     */
    public int getTotalPhasesCompleted() {
        return totalPhasesCompleted;
    }
    
    /**
     * Checks if startup was successful
     */
    public boolean isStartupSuccessful() {
        return startupSuccessful;
    }
    
    /**
     * Gets the summary message
     */
    public String getSummaryMessage() {
        return summaryMessage;
    }
    
    @Override
    public String toString() {
        return String.format("SystemSummary{time=%dms, phases=%d, successful=%s, message='%s'}",
            totalStartupTime, totalPhasesCompleted, startupSuccessful, summaryMessage);
    }
}