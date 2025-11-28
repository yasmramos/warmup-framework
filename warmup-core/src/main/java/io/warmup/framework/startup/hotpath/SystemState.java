package io.warmup.framework.startup.hotpath;

/**
 * System states for Hot Path Optimization System.
 * Represents the current operational state of the optimization system.
 * 
 * @author MiniMax Agent
 * @version 1.0
 */
public enum SystemState {
    
    /**
     * System is initializing
     */
    INITIALIZING("System is initializing"),
    
    /**
     * System is running normally
     */
    RUNNING("System is running normally"),
    
    /**
     * System is optimizing hot paths
     */
    OPTIMIZING("System is optimizing hot paths"),
    
    /**
     * System encountered an error
     */
    ERROR("System encountered an error"),
    
    /**
     * System is shutting down
     */
    SHUTTING_DOWN("System is shutting down"),
    
    /**
     * System is stopped
     */
    STOPPED("System is stopped");
    
    private final String description;
    
    SystemState(String description) {
        this.description = description;
    }
    
    public String getDescription() { return description; }
    
    /**
     * Returns true if the system is operational (not error or stopped)
     */
    public boolean isOperational() {
        return this == RUNNING || this == OPTIMIZING || this == INITIALIZING;
    }
    
    /**
     * Returns true if the system has encountered an error
     */
    public boolean isError() {
        return this == ERROR;
    }
}