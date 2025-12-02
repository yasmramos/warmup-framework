package io.warmup.framework.startup.hotpath;

/**
 * Priority levels for optimization actions.
 * Higher values indicate higher priority.
 * 
 * @author MiniMax Agent
 * @version 1.0
 */
public enum Priority {
    
    LOWEST(0, "Lowest Priority"),
    LOW(1, "Low Priority"),
    MEDIUM(2, "Medium Priority"),
    HIGH(3, "High Priority"),
    CRITICAL(4, "Critical Priority");
    
    private final int value;
    private final String description;
    
    Priority(int value, String description) {
        this.value = value;
        this.description = description;
    }
    
    public int getValue() { return value; }
    public String getDescription() { return description; }
    
    /**
     * Returns true if this priority is considered high (HIGH or CRITICAL)
     */
    public boolean isHigh() {
        return this == HIGH || this == CRITICAL;
    }
    
    /**
     * Returns true if this priority is critical
     */
    public boolean isCritical() {
        return this == CRITICAL;
    }
}