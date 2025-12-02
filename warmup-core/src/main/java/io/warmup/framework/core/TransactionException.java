package io.warmup.framework.core;

/**
 * Exception thrown when transaction operations fail.
 * 
 * <p>
 * This exception is thrown when transaction management operations fail,
 * including transaction creation, commit, rollback, or state management.
 *
 * @author MiniMax Agent
 * @version 1.0
 */
public class TransactionException extends RuntimeException {

    /**
     * Constructs a new TransactionException with no detail message.
     */
    public TransactionException() {
        super();
    }

    /**
     * Constructs a new TransactionException with the specified detail message.
     *
     * @param message the detail message
     */
    public TransactionException(String message) {
        super(message);
    }

    /**
     * Constructs a new TransactionException with the specified detail message and cause.
     *
     * @param message the detail message
     * @param cause the cause of this exception
     */
    public TransactionException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Constructs a new TransactionException with the specified cause.
     *
     * @param cause the cause of this exception
     */
    public TransactionException(Throwable cause) {
        super(cause);
    }
}