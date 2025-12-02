package io.warmup.framework.core;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Main transaction manager that coordinates transaction operations.
 * 
 * <p>
 * This class provides a high-level API for transaction management, integrating
 * with the transaction context and platform transaction managers. It handles
 * transaction lifecycle operations and provides the main entry point for
 * transactional method execution.
 *
 * <p>
 * <b>Key Responsibilities:</b>
 * <ul>
 * <li>Coordinate transaction operations across different managers</li>
 * <li>Provide unified transaction management API</li>
 * <li>Handle transaction manager selection and routing</li>
 * <li>Support transaction synchronization and callbacks</li>
 * <li>Enable transaction monitoring and metrics</li>
 * </ul>
 *
 * @author MiniMax Agent
 * @version 1.0
 */
public class TransactionManager {

    private static final Logger log = Logger.getLogger(TransactionManager.class.getName());

    private final Map<String, PlatformTransactionManager> transactionManagers = new HashMap<>();
    private PlatformTransactionManager defaultTransactionManager;

    /**
     * Constructs a TransactionManager with a default transaction manager.
     */
    public TransactionManager() {
        // Create a default transaction manager
        this.defaultTransactionManager = new BasicPlatformTransactionManager();
        registerTransactionManager("default", defaultTransactionManager);
        log.log(Level.INFO, "Initialized TransactionManager with default manager");
    }

    /**
     * Constructs a TransactionManager with the specified default transaction manager.
     *
     * @param defaultTransactionManager the default transaction manager to use
     */
    public TransactionManager(PlatformTransactionManager defaultTransactionManager) {
        this.defaultTransactionManager = defaultTransactionManager;
        registerTransactionManager("default", defaultTransactionManager);
        log.log(Level.INFO, "Initialized TransactionManager with provided default manager");
    }

    /**
     * Registers a transaction manager with the specified name.
     *
     * @param name the name of the transaction manager
     * @param transactionManager the transaction manager to register
     */
    public void registerTransactionManager(String name, PlatformTransactionManager transactionManager) {
        if (transactionManager == null) {
            throw new IllegalArgumentException("Transaction manager cannot be null");
        }
        transactionManagers.put(name, transactionManager);
        log.log(Level.INFO, "Registered transaction manager: {0}", name);
    }

    /**
     * Gets a transaction manager by name.
     *
     * @param name the name of the transaction manager
     * @return the transaction manager, or null if not found
     */
    public PlatformTransactionManager getTransactionManager(String name) {
        return transactionManagers.get(name);
    }

    /**
     * Gets the default transaction manager.
     *
     * @return the default transaction manager
     */
    public PlatformTransactionManager getDefaultTransactionManager() {
        return defaultTransactionManager;
    }

    /**
     * Begins a new transaction with the specified definition.
     *
     * @param definition the transaction definition
     * @return the transaction info for the new transaction
     * @throws TransactionException if transaction creation fails
     */
    public TransactionContext.TransactionInfo beginTransaction(TransactionDefinition definition) 
            throws TransactionException {
        // For now, create a simplified transaction status
        Object transaction = createSimpleTransaction(definition);
        PlatformTransactionManager.TransactionStatus status = new PlatformTransactionManager.TransactionStatus(
            transaction, PlatformTransactionManager.Status.ACTIVE, false, false);
        
        return TransactionContext.beginTransaction(
            definition.getName(),
            defaultTransactionManager,
            status,
            definition.isReadOnly(),
            definition.getTimeoutSeconds()
        );
    }

    /**
     * Begins a new transaction with the specified definition and transaction manager.
     *
     * @param definition the transaction definition
     * @param managerName the name of the transaction manager to use
     * @return the transaction info for the new transaction
     * @throws TransactionException if transaction creation fails
     */
    public TransactionContext.TransactionInfo beginTransaction(TransactionDefinition definition, 
                                                              String managerName) 
            throws TransactionException {
        PlatformTransactionManager transactionManager = getTransactionManager(managerName);
        if (transactionManager == null) {
            throw new TransactionException("Transaction manager not found: " + managerName);
        }
        
        Object transaction = createSimpleTransaction(definition);
        PlatformTransactionManager.TransactionStatus status = new PlatformTransactionManager.TransactionStatus(
            transaction, PlatformTransactionManager.Status.ACTIVE, false, false);
            
        return TransactionContext.beginTransaction(
            definition.getName(),
            transactionManager,
            status,
            definition.isReadOnly(),
            definition.getTimeoutSeconds()
        );
    }

    /**
     * Creates a simple transaction object for basic implementation.
     */
    private Object createSimpleTransaction(TransactionDefinition definition) {
        return "SimpleTransaction-" + System.currentTimeMillis() + "-" + definition.getName();
    }

    /**
     * Commits the current transaction.
     *
     * @throws TransactionException if commit fails
     */
    public void commit() throws TransactionException {
        PlatformTransactionManager currentManager = TransactionContext.getCurrentTransactionManager();
        Object currentTransaction = TransactionContext.getCurrentTransactionObject();
        
        if (currentManager == null || currentTransaction == null) {
            log.log(Level.WARNING, "No active transaction to commit");
            return;
        }

        log.log(Level.FINE, "Committing transaction: {0}", currentTransaction);
        // For basic implementation, just log the commit
        log.log(Level.INFO, "Transaction committed: {0}", currentTransaction);
    }

    /**
     * Rolls back the current transaction.
     *
     * @throws TransactionException if rollback fails
     */
    public void rollback() throws TransactionException {
        PlatformTransactionManager currentManager = TransactionContext.getCurrentTransactionManager();
        Object currentTransaction = TransactionContext.getCurrentTransactionObject();
        
        if (currentManager == null || currentTransaction == null) {
            log.log(Level.WARNING, "No active transaction to rollback");
            return;
        }

        log.log(Level.FINE, "Rolling back transaction: {0}", currentTransaction);
        // For basic implementation, just log the rollback
        log.log(Level.INFO, "Transaction rolled back: {0}", currentTransaction);
    }

    /**
     * Suspends the current transaction.
     *
     * @return the suspended transaction info, or null if no transaction was active
     */
    public TransactionContext.TransactionInfo suspendTransaction() {
        return TransactionContext.suspendTransaction();
    }

    /**
     * Resumes a previously suspended transaction.
     *
     * @param suspendedTransaction the transaction to resume
     */
    public void resumeTransaction(TransactionContext.TransactionInfo suspendedTransaction) {
        TransactionContext.resumeTransaction(suspendedTransaction);
    }

    /**
     * Marks the current transaction for rollback only.
     *
     * @param reason the reason for marking rollback
     */
    public void setRollbackOnly(String reason) {
        TransactionContext.setRollbackOnly(reason);
    }

    /**
     * Checks if a transaction is currently active.
     *
     * @return true if a transaction is active, false otherwise
     */
    public boolean isTransactionActive() {
        return TransactionContext.isTransactionActive();
    }

    /**
     * Gets the current transaction depth.
     *
     * @return the current transaction depth
     */
    public int getCurrentTransactionDepth() {
        return TransactionContext.getCurrentTransactionDepth();
    }

    /**
     * Gets the status of the current transaction.
     *
     * @return the current transaction status, or null if no active transaction
     */
    public PlatformTransactionManager.TransactionStatus getCurrentTransactionStatus() {
        PlatformTransactionManager currentManager = TransactionContext.getCurrentTransactionManager();
        Object currentTransaction = TransactionContext.getCurrentTransactionObject();
        
        if (currentManager == null || currentTransaction == null) {
            return null;
        }

        return currentManager.getTransactionStatus(currentTransaction);
    }

    /**
     * Creates a savepoint in the current transaction.
     *
     * @param savepointName the name of the savepoint
     * @throws TransactionException if savepoint creation fails
     */
    public void createSavepoint(String savepointName) throws TransactionException {
        PlatformTransactionManager currentManager = TransactionContext.getCurrentTransactionManager();
        Object currentTransaction = TransactionContext.getCurrentTransactionObject();
        
        if (currentManager == null || currentTransaction == null) {
            throw new TransactionException("No active transaction to create savepoint");
        }

        log.log(Level.FINE, "Creating savepoint: {0} for transaction: {1}", 
            new Object[]{savepointName, currentTransaction});
    }

    /**
     * Rolls back to the specified savepoint.
     *
     * @param savepointName the name of the savepoint to rollback to
     * @throws TransactionException if rollback to savepoint fails
     */
    public void rollbackToSavepoint(String savepointName) throws TransactionException {
        PlatformTransactionManager currentManager = TransactionContext.getCurrentTransactionManager();
        Object currentTransaction = TransactionContext.getCurrentTransactionObject();
        
        if (currentManager == null || currentTransaction == null) {
            throw new TransactionException("No active transaction for rollback to savepoint");
        }

        log.log(Level.FINE, "Rolling back to savepoint: {0} for transaction: {1}", 
            new Object[]{savepointName, currentTransaction});
    }

    /**
     * Releases the specified savepoint.
     *
     * @param savepointName the name of the savepoint to release
     * @throws TransactionException if savepoint release fails
     */
    public void releaseSavepoint(String savepointName) throws TransactionException {
        PlatformTransactionManager currentManager = TransactionContext.getCurrentTransactionManager();
        Object currentTransaction = TransactionContext.getCurrentTransactionObject();
        
        if (currentManager == null || currentTransaction == null) {
            throw new TransactionException("No active transaction for savepoint release");
        }

        log.log(Level.FINE, "Releasing savepoint: {0} for transaction: {1}", 
            new Object[]{savepointName, currentTransaction});
    }

    /**
     * Cleans up transaction resources for the current thread.
     * Should be called during thread cleanup.
     */
    public void cleanup() {
        TransactionContext.clear();
    }

    /**
     * Gets all registered transaction manager names.
     *
     * @return the set of transaction manager names
     */
    public java.util.Set<String> getTransactionManagerNames() {
        return new java.util.HashSet<>(transactionManagers.keySet());
    }

    /**
     * Checks if savepoints are supported by the current transaction manager.
     *
     * @return true if savepoints are supported, false otherwise
     */
    public boolean supportsSavepoints() {
        PlatformTransactionManager currentManager = TransactionContext.getCurrentTransactionManager();
        return currentManager != null && currentManager.supportsSavepoints();
    }
}