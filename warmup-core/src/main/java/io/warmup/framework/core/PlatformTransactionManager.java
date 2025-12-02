package io.warmup.framework.core;

import io.warmup.framework.annotation.Transactional;
import java.util.logging.Logger;

/**
 * Interface for transaction management abstraction.
 * 
 * <p>
 * This interface provides a consistent abstraction for transaction management,
 * regardless of the underlying transaction resource (JDBC, JMS, JTA, etc.).
 * It allows the framework to work with different transaction types uniformly.
 *
 * <p>
 * <b>Key Responsibilities:</b>
 * <ul>
 * <li>Define transaction lifecycle operations (begin, commit, rollback)</li>
 * <li>Provide transaction status and state information</li>
 * <li>Support transaction synchronization callbacks</li>
 * <li>Enable resource-specific transaction management</li>
 * </ul>
 *
 * <p>
 * <b>Transaction Lifecycle:</b>
 * <ol>
 * <li>Begin transaction with specified properties</li>
 * <li>Execute transactional work within transaction context</li *>
 * <li>Check transaction status for commit decision</li>
 * <li>Commit or rollback based on execution result</li>
 * <li>Handle synchronization callbacks</li>
 * </ol>
 *
 * @author MiniMax Agent
 * @version 1.0
 */
public interface PlatformTransactionManager {

    Logger log = Logger.getLogger(PlatformTransactionManager.class.getName());

    /**
     * Begins a new transaction with the specified definition.
     *
     * @param definition the transaction definition containing properties
     * @return TransactionStatus object representing the new transaction
     * @throws TransactionException if transaction creation fails
     */
    TransactionStatus beginTransaction(TransactionDefinition definition) throws TransactionException;

    /**
     * Commits the specified transaction.
     *
     * @param status the transaction status to commit
     * @throws TransactionException if commit fails
     */
    void commit(TransactionStatus status) throws TransactionException;

    /**
     * Rolls back the specified transaction.
     *
     * @param status the transaction status to rollback
     * @throws TransactionException if rollback fails
     */
    void rollback(TransactionStatus status) throws TransactionException;

    /**
     * Gets the transaction status for the specified transaction object.
     * This method allows querying the state of an ongoing transaction.
     *
     * @param transaction the transaction object to get status for
     * @return TransactionStatus object representing the transaction state
     */
    TransactionStatus getTransactionStatus(Object transaction);

    /**
     * Gets the name of this transaction manager.
     * Used to identify the manager in multi-manager scenarios.
     *
     * @return the transaction manager name
     */
    String getName();

    /**
     * Checks if this transaction manager supports savepoints.
     * Savepoints are required for nested transaction support.
     *
     * @return true if savepoints are supported, false otherwise
     */
    boolean supportsSavepoints();

    /**
     * Creates a savepoint within the specified transaction.
     * Only applicable if savepoints are supported.
     *
     * @param status the transaction status
     * @param savepointName the name for the savepoint
     * @throws TransactionException if savepoint creation fails
     */
    void createSavepoint(TransactionStatus status, String savepointName) throws TransactionException;

    /**
     * Rolls back to the specified savepoint.
     * Only applicable if savepoints are supported.
     *
     * @param status the transaction status
     * @param savepointName the name of the savepoint to rollback to
     * @throws TransactionException if rollback to savepoint fails
     */
    void rollbackToSavepoint(TransactionStatus status, String savepointName) throws TransactionException;

    /**
     * Releases the specified savepoint.
     * Only applicable if savepoints are supported.
     *
     * @param status the transaction status
     * @param savepointName the name of the savepoint to release
     * @throws TransactionException if savepoint release fails
     */
    void releaseSavepoint(TransactionStatus status, String savepointName) throws TransactionException;

    /**
     * Enumeration representing the different transaction states.
     * Used to determine the current state and appropriate actions.
     */
    enum Status {
        /**
         * Transaction is in the process of creation but not yet started.
         */
        NEW,

        /**
         * Transaction is active and running.
         */
        ACTIVE,

        /**
         * Transaction has been committed successfully.
         */
        COMMITTED,

        /**
         * Transaction has been rolled back.
         */
        ROLLED_BACK,

        /**
         * Transaction is in the process of being committed.
         */
        COMMITTING,

        /**
         * Transaction is in the process of being rolled back.
         */
        ROLLING_BACK,

        /**
         * Transaction has been marked for rollback only.
         */
        MARKED_ROLLBACK,

        /**
         * Transaction has encountered an error during execution.
         */
        FAILED,

        /**
         * Transaction state is unknown or cannot be determined.
         */
        UNKNOWN
    }

    /**
     * Represents the definition of a transaction.
     * Contains all properties and settings for a transaction.
     */
    static class TransactionDefinition {

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

    /**
     * Represents the status of a transaction.
     * Provides information about transaction state and lifecycle.
     */
    class TransactionStatus {

        private final Object transaction;
        private final boolean completed;
        private final boolean hasSavepoint;
        private final boolean rollbackOnly;
        private Status status;

        public TransactionStatus(Object transaction, Status status, boolean hasSavepoint, boolean rollbackOnly) {
            this.transaction = transaction;
            this.status = status;
            this.hasSavepoint = hasSavepoint;
            this.rollbackOnly = rollbackOnly;
            this.completed = status == Status.COMMITTED || status == Status.ROLLED_BACK;
        }

        public Object getTransaction() {
            return transaction;
        }

        public Status getStatus() {
            return status;
        }

        public void setStatus(Status status) {
            this.status = status;
        }

        public boolean isCompleted() {
            return completed;
        }

        public boolean isNewTransaction() {
            return status == Status.NEW;
        }

        public boolean isActive() {
            return status == Status.ACTIVE;
        }

        public boolean isCommitted() {
            return status == Status.COMMITTED;
        }

        public boolean isRolledBack() {
            return status == Status.ROLLED_BACK;
        }

        public boolean hasSavepoint() {
            return hasSavepoint;
        }

        public boolean isRollbackOnly() {
            return rollbackOnly;
        }

        public void setRollbackOnly() {
            // In real implementation, this would set rollback flag on underlying transaction
            log.fine("Transaction marked as rollback only: " + transaction);
        }

        @Override
        public String toString() {
            return String.format("TransactionStatus{transaction=%s, status=%s, completed=%s, rollbackOnly=%s, hasSavepoint=%s}",
                transaction, status, completed, rollbackOnly, hasSavepoint);
        }
    }
}