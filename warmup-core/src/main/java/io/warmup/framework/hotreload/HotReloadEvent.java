package io.warmup.framework.hotreload;

/**
 * Event class for hot reload operations.
 * Used to notify about hot reload status changes and operations.
 */
public class HotReloadEvent {
    
    /**
     * Types of hot reload events.
     */
    public enum EventType {
        ENABLED,
        DISABLED,
        STARTED,
        SUCCESS,
        FAILURE,
        CLASS_RELOADED,
        METHOD_RELOADED,
        DIRECTORY_MONITORED,
        CACHE_CLEARED,
        ERROR
    }
    
    private final EventType eventType;
    private final String className;
    private final String message;
    private final long timestamp;
    
    public HotReloadEvent(EventType eventType, String className, String message) {
        this.eventType = eventType;
        this.className = className;
        this.message = message;
        this.timestamp = System.currentTimeMillis();
    }
    
    public EventType getEventType() {
        return eventType;
    }
    
    public String getClassName() {
        return className;
    }
    
    public String getMessage() {
        return message;
    }
    
    public long getTimestamp() {
        return timestamp;
    }
    
    /**
     * Gets the event type (alias for getEventType for compatibility).
     * 
     * @return the event type
     */
    public EventType getType() {
        return eventType;
    }
    
    @Override
    public String toString() {
        return "HotReloadEvent{" +
                "eventType=" + eventType +
                ", className='" + className + '\'' +
                ", message='" + message + '\'' +
                ", timestamp=" + timestamp +
                '}';
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        
        HotReloadEvent that = (HotReloadEvent) o;
        
        if (timestamp != that.timestamp) return false;
        if (eventType != that.eventType) return false;
        if (className != null ? !className.equals(that.className) : that.className != null) return false;
        return message != null ? message.equals(that.message) : that.message == null;
    }
    
    @Override
    public int hashCode() {
        int result = eventType.hashCode();
        result = 31 * result + (className != null ? className.hashCode() : 0);
        result = 31 * result + (message != null ? message.hashCode() : 0);
        result = 31 * result + (int) (timestamp ^ (timestamp >>> 32));
        return result;
    }
}