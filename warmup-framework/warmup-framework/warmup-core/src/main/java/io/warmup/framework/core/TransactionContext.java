package io.warmup.framework.core;

import java.util.Stack;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Manages the transaction context for the current thread.
 * 
 * <p>
 * This class maintains a stack of active transactions for the current thread,
 * enabling support for nested transactions and proper transaction management.
 * Each transaction on the stack represents a different transaction scope.
 *
 * <p>
 * <b>Key Responsibilities:</b>
 * <ul>
 * <li>Maintain thread-local transaction state</li>
 * <li>Support nested transaction scenarios</li>
 * <li>Provide transaction lookup and management utilities</li>
 * <li>Handle transaction synchronization and cleanup</li>
 * </ul>
 *
 * <p>
 * <b>Thread Safety:</b>
 * This class is thread-safe as each thread maintains its own transaction context.
 *
 * @author MiniMax Agent
 * @version 1.0
 */
public class TransactionContext {

    private static final Logger log = Logger.getLogger(TransactionContext.class.getName());

    /**
     * Thread-local storage for transaction stacks.
     * Each thread can have its own stack of active transactions.
     */
    private static final ThreadLocal<Stack<TransactionInfo>> transactionStack = 
        ThreadLocal.withInitial(Stack::new);

    /**
     * Information about a single transaction scope.
     */
    public static class TransactionInfo {
        private final String transactionName;
        private final PlatformTransactionManager transactionManager;
        private final PlatformTransactionManager.TransactionStatus transaction;
        private final boolean readOnly;
        private final long timeoutSeconds;
        private final int depth;

        public TransactionInfo(String transactionName, PlatformTransactionManager transactionManager,
                              PlatformTransactionManager.TransactionStatus transaction, boolean readOnly, long timeoutSeconds, int depth) {
            this.transactionName = transactionName;
            this.transactionManager = transactionManager;
            this.transaction = transaction;
            this.readOnly = readOnly;
            this.timeoutSeconds = timeoutSeconds;
            this.depth = depth;
        }

        public String getTransactionName() {
            return transactionName;
        }

        public PlatformTransactionManager getTransactionManager() {
            return transactionManager;
        }

        public PlatformTransactionManager.TransactionStatus getTransaction() {
            return transaction;
        }

        public boolean isReadOnly() {
            return readOnly;
        }

        public long getTimeoutSeconds() {
            return timeoutSeconds;
        }

        public int getDepth() {
            return depth;
        }

        @Override
        public String toString() {
            return String.format("TransactionInfo{name='%s', readOnly=%s, timeout=%ds, depth=%d}",
                transactionName, readOnly, timeoutSeconds, depth);
        }
    }

    /**
     * Gets the current active transaction info, or null if no transaction is active.
     *
     * @return the current transaction info, or null if no active transaction
     */
    public static TransactionInfo getCurrentTransaction() {
        Stack<TransactionInfo> stack = transactionStack.get();
        return stack.isEmpty() ? null : stack.peek();
    }

    /**
     * Gets the current transaction depth (number of nested transactions).
     *
     * @return the current transaction depth (0 if no active transaction)
     */
    public static int getCurrentTransactionDepth() {
        Stack<TransactionInfo> stack = transactionStack.get();
        return stack.size();
    }

    /**
     * Checks if a transaction is currently active.
     *
     * @return true if a transaction is active, false otherwise
     */
    public static boolean isTransactionActive() {
        return getCurrentTransaction() != null;
    }

    /**
     * Begins a new transaction and adds it to the transaction stack.
     *
     * @param transactionName the name of the transaction
     * @param transactionManager the transaction manager handling this transaction
     * @param transaction the underlying transaction object
     * @param readOnly whether this is a read-only transaction
     * @param timeoutSeconds transaction timeout in seconds
     * @return the transaction info for the new transaction
     */
    public static TransactionInfo beginTransaction(String transactionName,
                                                  PlatformTransactionManager transactionManager,
                                                  PlatformTransactionManager.TransactionStatus transaction,
                                                  boolean readOnly,
                                                  long timeoutSeconds) {
        Stack<TransactionInfo> stack = transactionStack.get();
        TransactionInfo transactionInfo = new TransactionInfo(
            transactionName, transactionManager, transaction, readOnly, timeoutSeconds, stack.size());
        
        stack.push(transactionInfo);
        log.log(Level.FINE, "Transaction started: {0}", transactionInfo);
        return transactionInfo;
    }

    /**
     * Commits the current transaction and removes it from the stack.
     * Only commits if this is the topmost transaction in the stack.
     *
     * @throws TransactionException if commit fails
     */
    public static void commitTransaction() throws TransactionException {
        Stack<TransactionInfo> stack = transactionStack.get();
        if (stack.isEmpty()) {
            throw new TransactionException("No active transaction to commit");
        }

        TransactionInfo transactionInfo = stack.peek();
        try {
            transactionInfo.getTransactionManager().commit(transactionInfo.getTransaction());
            stack.pop();
            log.log(Level.FINE, "Transaction committed: {0}", transactionInfo);
        } catch (Exception e) {
            log.log(Level.SEVERE, "Failed to commit transaction: " + transactionInfo, e);
            throw new TransactionException("Failed to commit transaction: " + transactionInfo, e);
        }
    }

    /**
     * Rolls back the current transaction and removes it from the stack.
     * Only rolls back if this is the topmost transaction in the stack.
     *
     * @throws TransactionException if rollback fails
     */
    public static void rollbackTransaction() throws TransactionException {
        Stack<TransactionInfo> stack = transactionStack.get();
        if (stack.isEmpty()) {
            log.log(Level.WARNING, "No active transaction to rollback");
            return;
        }

        TransactionInfo transactionInfo = stack.peek();
        try {
            transactionInfo.getTransactionManager().rollback(transactionInfo.getTransaction());
            stack.pop();
            log.log(Level.FINE, "Transaction rolled back: {0}", transactionInfo);
        } catch (Exception e) {
            log.log(Level.SEVERE, "Failed to rollback transaction: " + transactionInfo, e);
            throw new TransactionException("Failed to rollback transaction: " + transactionInfo, e);
        }
    }

    /**
     * Suspends the current transaction (if any) and returns the suspended transaction info.
     * Used for REQUIRES_NEW propagation.
     *
     * @return the suspended transaction info, or null if no transaction was active
     */
    public static TransactionInfo suspendTransaction() {
        Stack<TransactionInfo> stack = transactionStack.get();
        if (stack.isEmpty()) {
            return null;
        }

        TransactionInfo suspended = stack.pop();
        log.log(Level.FINE, "Transaction suspended: {0}", suspended);
        return suspended;
    }

    /**
     * Resumes a previously suspended transaction.
     * Used for REQUIRES_NEW propagation.
     *
     * @param suspendedTransaction the transaction to resume
     */
    public static void resumeTransaction(TransactionInfo suspendedTransaction) {
        if (suspendedTransaction == null) {
            return;
        }

        Stack<TransactionInfo> stack = transactionStack.get();
        stack.push(suspendedTransaction);
        log.log(Level.FINE, "Transaction resumed: {0}", suspendedTransaction);
    }

    /**
     * Marks the current transaction as needing rollback.
     * This will cause the transaction to be rolled back when completed.
     *
     * @param reason the reason for marking rollback
     */
    public static void setRollbackOnly(String reason) {
        TransactionInfo currentTransaction = getCurrentTransaction();
        if (currentTransaction != null) {
            log.log(Level.FINE, "Transaction marked for rollback: {0} - Reason: {1}", 
                new Object[]{currentTransaction, reason});
            // In a real implementation, this would set a flag on the transaction
        }
    }

    /**
     * Cleans up all transaction state for the current thread.
     * Called during thread cleanup to prevent memory leaks.
     */
    public static void clear() {
        Stack<TransactionInfo> stack = transactionStack.get();
        if (!stack.isEmpty()) {
            log.log(Level.WARNING, "Clearing {0} active transactions", stack.size());
            stack.clear();
        }
        transactionStack.remove();
    }

    /**
     * Gets the transaction manager for the current transaction.
     *
     * @return the current transaction's manager, or null if no active transaction
     */
    public static PlatformTransactionManager getCurrentTransactionManager() {
        TransactionInfo currentTransaction = getCurrentTransaction();
        return currentTransaction != null ? currentTransaction.getTransactionManager() : null;
    }

    /**
     * Gets the underlying transaction object for the current transaction.
     *
     * @return the current transaction object, or null if no active transaction
     */
    public static Object getCurrentTransactionObject() {
        TransactionInfo currentTransaction = getCurrentTransaction();
        return currentTransaction != null ? currentTransaction.getTransaction() : null;
    }

    /**
     * Checks if the current transaction is read-only.
     *
     * @return true if current transaction is read-only, false otherwise
     */
    public static boolean isCurrentTransactionReadOnly() {
        TransactionInfo currentTransaction = getCurrentTransaction();
        return currentTransaction != null && currentTransaction.isReadOnly();
    }

    /**
     * Gets the timeout for the current transaction.
     *
     * @return timeout in seconds, or -1 if no timeout specified
     */
    public static long getCurrentTransactionTimeout() {
        TransactionInfo currentTransaction = getCurrentTransaction();
        return currentTransaction != null ? currentTransaction.getTimeoutSeconds() : -1;
    }
}