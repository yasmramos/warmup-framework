package io.warmup.framework.event;

import java.util.concurrent.atomic.AtomicLong;

/**
 * Statistics for event processing.
 * 
 * Tracks published, processed, and failed events for a specific event type.
 * 
 * @author MiniMax Agent
 * @version 1.0
 */
public class EventStatistics {
    
    private final Class<?> eventType;
    private final AtomicLong publishedCount = new AtomicLong(0);
    private final AtomicLong processedCount = new AtomicLong(0);
    private final AtomicLong failedCount = new AtomicLong(0);
    private final AtomicLong totalProcessingTime = new AtomicLong(0);
    
    public EventStatistics(Class<?> eventType) {
        this.eventType = eventType;
    }
    
    /**
     * Record an action for this event type.
     * 
     * @param action the action to record (published, processed, failed)
     */
    public void recordAction(String action) {
        switch (action.toLowerCase()) {
            case "published":
                publishedCount.incrementAndGet();
                break;
            case "processed":
                processedCount.incrementAndGet();
                break;
            case "failed":
                failedCount.incrementAndGet();
                break;
        }
    }
    
    /**
     * Record processing time for an event.
     * 
     * @param processingTime the time taken to process the event in milliseconds
     */
    public void recordProcessingTime(long processingTime) {
        totalProcessingTime.addAndGet(processingTime);
    }
    
    /**
     * Get the event type this statistics is for.
     * 
     * @return the event type
     */
    public Class<?> getEventType() {
        return eventType;
    }
    
    /**
     * Get the number of events published for this type.
     * 
     * @return published event count
     */
    public long getPublishedCount() {
        return publishedCount.get();
    }
    
    /**
     * Get the number of events successfully processed for this type.
     * 
     * @return processed event count
     */
    public long getProcessedCount() {
        return processedCount.get();
    }
    
    /**
     * Get the number of events that failed to process for this type.
     * 
     * @return failed event count
     */
    public long getFailedCount() {
        return failedCount.get();
    }
    
    /**
     * Get the total processing time for all events of this type.
     * 
     * @return total processing time in milliseconds
     */
    public long getTotalProcessingTime() {
        return totalProcessingTime.get();
    }
    
    /**
     * Get the average processing time for events of this type.
     * 
     * @return average processing time in milliseconds
     */
    public double getAverageProcessingTime() {
        long processed = processedCount.get();
        return processed > 0 ? (double) totalProcessingTime.get() / processed : 0.0;
    }
    
    /**
     * Get the success rate for event processing.
     * 
     * @return success rate as a percentage (0.0 to 1.0)
     */
    public double getSuccessRate() {
        long published = publishedCount.get();
        return published > 0 ? (double) processedCount.get() / published : 0.0;
    }
    
    /**
     * Get the failure rate for event processing.
     * 
     * @return failure rate as a percentage (0.0 to 1.0)
     */
    public double getFailureRate() {
        long published = publishedCount.get();
        return published > 0 ? (double) failedCount.get() / published : 0.0;
    }
    
    @Override
    public String toString() {
        return String.format("EventStatistics[%s]: published=%d, processed=%d, failed=%d, avgTime=%.2fms, successRate=%.2f%%",
                eventType.getSimpleName(),
                publishedCount.get(),
                processedCount.get(),
                failedCount.get(),
                getAverageProcessingTime(),
                getSuccessRate() * 100);
    }
}