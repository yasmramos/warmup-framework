package io.warmup.framework.core;

/**
 * Transaction definition for transactional operations.
 * 
 * <p>
 * This class encapsulates the configuration for a transaction including
 * propagation, isolation, timeout, and other transactional properties.
 *
 * @author MiniMax Agent
 * @version 1.0
 */
public class TransactionDefinition {
    
    private final String name;
    private final String propagationBehavior;
    private final String isolationLevel;
    private final long timeoutSeconds;
    private final boolean readOnly;
    private final String scope;

    public TransactionDefinition(String name,
                               String propagationBehavior,
                               String isolationLevel,
                               long timeoutSeconds,
                               boolean readOnly,
                               String scope) {
        this.name = name;
        this.propagationBehavior = propagationBehavior;
        this.isolationLevel = isolationLevel;
        this.timeoutSeconds = timeoutSeconds;
        this.readOnly = readOnly;
        this.scope = scope;
    }

    public String getName() {
        return name;
    }

    public String getPropagationBehavior() {
        return propagationBehavior;
    }

    public String getIsolationLevel() {
        return isolationLevel;
    }

    public long getTimeoutSeconds() {
        return timeoutSeconds;
    }

    public boolean isReadOnly() {
        return readOnly;
    }

    public String getScope() {
        return scope;
    }

    @Override
    public String toString() {
        return String.format("TransactionDefinition{name='%s', propagation=%s, isolation=%s, timeout=%ds, readOnly=%s, scope=%s}",
            name, propagationBehavior, isolationLevel, timeoutSeconds, readOnly, scope);
    }
}