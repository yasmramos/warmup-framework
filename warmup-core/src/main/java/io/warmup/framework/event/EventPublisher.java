package io.warmup.framework.event;

import io.warmup.framework.annotation.Component;
import io.warmup.framework.annotation.Inject;

/**
 * Enhanced EventPublisher that uses the new EventBus for event publishing.
 * 
 * Provides both synchronous and asynchronous event publishing capabilities.
 * 
 * @author MiniMax Agent
 * @version 2.0
 */
@Component
public class EventPublisher {

    private final EventBus eventBus;

    @Inject
    public EventPublisher(EventBus eventBus) {
        this.eventBus = eventBus;
    }

    /**
     * Publish an event synchronously.
     * 
     * @param event the event to publish
     */
    public void publishEvent(Event event) {
        if (eventBus != null) {
            eventBus.publishEvent(event);
        } else {
            System.err.println("EventBus not available for publishing event");
        }
    }
    
    /**
     * Publish an event asynchronously.
     * 
     * @param event the event to publish
     */
    public void publishEventAsync(Event event) {
        if (eventBus != null) {
            eventBus.publishEventAsync(event);
        } else {
            System.err.println("EventBus not available for publishing async event");
        }
    }
    
    /**
     * Get the EventBus instance for direct access if needed.
     * 
     * @return the EventBus instance
     */
    public EventBus getEventBus() {
        return eventBus;
    }
    
    /**
     * Print event bus status report.
     */
    public void printEventStatus() {
        if (eventBus != null) {
            eventBus.printStatusReport();
        } else {
            System.out.println("EventBus not available for status reporting");
        }
    }
}
