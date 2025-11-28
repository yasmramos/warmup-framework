package io.warmup.framework.event;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Dead letter event for failed event processing.
 * 
 * Represents an event that failed to be processed by a listener,
 * including details about the failure.
 * 
 * @author MiniMax Agent
 * @version 1.0
 */
public class DeadLetterEvent {
    
    private final Event originalEvent;
    private final IEventListener listener;
    private final Throwable error;
    private final LocalDateTime timestamp;
    private final String errorMessage;
    
    public DeadLetterEvent(Event originalEvent, IEventListener listener, Throwable error) {
        this.originalEvent = originalEvent;
        this.listener = listener;
        this.error = error;
        this.timestamp = LocalDateTime.now();
        this.errorMessage = error != null ? error.getMessage() : "Unknown error";
    }
    
    /**
     * Get the original event that failed to process.
     * 
     * @return the original event
     */
    public Event getEvent() {
        return originalEvent;
    }
    
    /**
     * Get the listener that failed to process the event.
     * 
     * @return the event listener
     */
    public IEventListener getListener() {
        return listener;
    }
    
    /**
     * Get the error that occurred during event processing.
     * 
     * @return the error
     */
    public Throwable getError() {
        return error;
    }
    
    /**
     * Get the timestamp when the event processing failed.
     * 
     * @return the failure timestamp
     */
    public LocalDateTime getTimestamp() {
        return timestamp;
    }
    
    /**
     * Get the formatted timestamp string.
     * 
     * @return formatted timestamp
     */
    public String getFormattedTimestamp() {
        return timestamp.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
    }
    
    /**
     * Get the error message.
     * 
     * @return the error message
     */
    public String getErrorMessage() {
        return errorMessage;
    }
    
    /**
     * Get the simple class name of the original event.
     * 
     * @return event class simple name
     */
    public String getEventType() {
        return originalEvent != null ? originalEvent.getClass().getSimpleName() : "Unknown";
    }
    
    /**
     * Get the simple class name of the listener.
     * 
     * @return listener class simple name
     */
    public String getListenerType() {
        return listener != null ? listener.getClass().getSimpleName() : "Unknown";
    }
    
    /**
     * Get a formatted string representation of this dead letter event.
     * 
     * @return formatted string
     */
    public String toFormattedString() {
        return String.format("[%s] Event: %s -> Listener: %s -> Error: %s",
                getFormattedTimestamp(),
                getEventType(),
                getListenerType(),
                getErrorMessage());
    }
    
    @Override
    public String toString() {
        return String.format("DeadLetterEvent{event=%s, listener=%s, error=%s, timestamp=%s}",
                getEventType(),
                getListenerType(),
                getErrorMessage(),
                getFormattedTimestamp());
    }
}