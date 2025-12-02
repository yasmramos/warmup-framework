package io.warmup.framework.exception;

/**
 * Base exception class for all Warmup Framework related errors.
 * This is the root exception from which all framework-specific exceptions inherit.
 * 
 * @author Warmup Framework
 * @version 1.0
 */
public class WarmupException extends RuntimeException {
    
    /**
     * Constructs a new WarmupException with no detail message.
     */
    public WarmupException() {
        super();
    }
    
    /**
     * Constructs a new WarmupException with the specified detail message.
     *
     * @param message the detail message
     */
    public WarmupException(String message) {
        super(message);
    }
    
    /**
     * Constructs a new WarmupException with the specified detail message and cause.
     *
     * @param message the detail message
     * @param cause the cause
     */
    public WarmupException(String message, Throwable cause) {
        super(message, cause);
    }
    
    /**
     * Constructs a new WarmupException with the specified cause.
     *
     * @param cause the cause
     */
    public WarmupException(Throwable cause) {
        super(cause);
    }
}