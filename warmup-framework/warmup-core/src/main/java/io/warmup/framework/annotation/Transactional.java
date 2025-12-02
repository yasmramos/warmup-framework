package io.warmup.framework.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to mark a method as transactional, enabling declarative transaction management.
 * 
 * <p>
 * This annotation enables declarative transaction management for methods, allowing automatic
 * transaction begin, commit, and rollback based on the execution result and configured rules.
 * 
 * <p>
 * <b>Key Features:</b>
 * <ul>
 * <li>Automatic transaction management for method execution</li>
 * <li>Configurable propagation behavior</li>
 * <li>Customizable isolation levels</li>
 * <li>Exception-based and condition-based rollback rules</li>
 * <li>Read-only transaction optimization</li>
 * <li>Transaction timeout configuration</li>
 * <li>Support for nested transactions</li>
 * </ul>
 *
 * <p>
 * <b>Transaction Propagation:</b>
 * <ul>
 * <li><code>REQUIRED</code> - Default, joins existing or creates new transaction</li>
 * <li><code>REQUIRES_NEW</code> - Always creates new transaction, suspending existing</li>
 * <li><code>NESTED</code> - Creates nested transaction within existing transaction</li>
 * <li><code>SUPPORTS</code> - Executes within transaction if exists, otherwise non-transactional</li>
 * <li><code>NOT_SUPPORTED</code> - Always executes non-transactional, suspending existing</li>
 * <li><code>NEVER</code> - Must execute non-transactional, fails if transaction exists</li>
 * </ul>
 *
 * <p>
 * <b>Isolation Levels:</b>
 * <ul>
 * <li><code>DEFAULT</code> - Uses database default isolation level</li>
 * <li><code>READ_UNCOMMITTED</code> - Prevents dirty reads, allows non-repeatable reads</li>
 * <li><code>READ_COMMITTED</code> - Prevents dirty reads and non-repeatable reads</li>
 * <li><code>REPEATABLE_READ</code> - Prevents dirty reads, non-repeatable reads, and phantom reads</li>
 * <li><code>SERIALIZABLE</code> - Complete isolation, highest isolation level</li>
 * </ul>
 *
 * <p>
 * <b>Usage Examples:</b>
 * <pre>
 * // Basic transactional method
 * {@literal @}Transactional
 * public void transferMoney(Account from, Account to, BigDecimal amount) {
 *     from.withdraw(amount);
 *     to.deposit(amount);
 * }
 *
 * // Transaction with read-only optimization
 * {@literal @}Transactional(readOnly = true)
 * public Account findAccountById(Long id) {
 *     return accountRepository.findById(id);
 * }
 *
 * // Transaction with custom propagation and timeout
 * {@literal @}Transactional(
 *     propagation = TransactionScope.REQUIRES_NEW,
 *     timeout = 30
 * )
 * public void logOperation(String operation, String user) {
 *     auditLogger.log(operation, user);
 * }
 *
 * // Transaction with rollback rules
 * {@literal @}Transactional(
 *     rollbackFor = DataAccessException.class,
 *     noRollbackFor = ValidationException.class
 * )
 * public User saveUser(User user) {
 *     validateUser(user);
 *     return userRepository.save(user);
 * }
 *
 * // Nested transaction
 * {@literal @}Transactional(propagation = TransactionScope.NESTED)
 * public void processPaymentWithAudit(Payment payment) {
 *     paymentProcessor.process(payment); // May be within transaction
 *     auditLogger.logPayment(payment); // Always executes, may fail independently
 * }
 * </pre>
 *
 * <p>
 * <b>Transaction Interception:</b>
 * When a method annotated with {@code @Transactional} is called, the framework:
 * <ol>
 * <li>Checks if a transaction already exists (based on propagation)</li>
 * <li>Creates or joins existing transaction as needed</li>
 * <li>Executes the method within the transaction context</li>
 * <li>Commits the transaction on successful completion</li>
 * <li>Rolls back the transaction on exceptions (based on rollback rules)</li>
 * <li>Handles transaction completion and cleanup</li>
 * </ol>
 *
 * <p>
 * <b>Implementation Notes:</b>
 * <ul>
 * <li>Transactions are managed through PlatformTransactionManager abstraction</li>
 * <li>Transaction context is maintained in ThreadLocal for thread safety</li>
 * <li>Transaction boundaries are enforced through AOP interception</li>
 * <li>Nested transactions are supported where database allows savepoints</li>
 * <li>Transaction isolation and timeout are applied as specified</li>
 * </ul>
 *
 * @see TransactionScope
 * @see PlatformTransactionManager
 * @see TransactionInterceptor
 *
 * @author MiniMax Agent
 * @version 1.0
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.TYPE})
public @interface Transactional {

    /**
     * Default transaction propagation behavior when a method is called.
     * Determines how the transaction interacts with existing transactions.
     *
     * @return the propagation behavior, defaults to REQUIRED
     */
    Propagation propagation() default Propagation.REQUIRED;

    /**
     * Transaction isolation level for this method.
     * Defines the transaction locking and consistency behavior.
     *
     * @return the isolation level, defaults to DEFAULT
     */
    Isolation isolation() default Isolation.DEFAULT;

    /**
     * Transaction timeout in seconds.
     * If the transaction takes longer than this time, it will be rolled back.
     *
     * @return timeout in seconds, defaults to -1 (no timeout)
     */
    int timeout() default -1;

    /**
     * Whether this is a read-only transaction.
     * Allows for transaction optimization and appropriate locking.
     *
     * @return true if read-only transaction, defaults to false
     */
    boolean readOnly() default false;

    /**
     * Exception types that should cause transaction rollback.
     * If any of these exceptions occur, the transaction will be rolled back.
     * If none specified, default rollback behavior applies.
     *
     * @return array of exception classes that cause rollback
     */
    Class<? extends Throwable>[] rollbackFor() default {};

    /**
     * Exception types that should NOT cause transaction rollback.
     * These exceptions will allow the transaction to commit even if they occur.
     * Useful for business logic that should not invalidate the transaction.
     *
     * @return array of exception classes that do NOT cause rollback
     */
    Class<? extends Throwable>[] noRollbackFor() default {};

    /**
     * Transaction manager name for multi-transaction-manager scenarios.
     * When multiple transaction managers exist, this identifies which one to use.
     *
     * @return transaction manager name, defaults to empty (default manager)
     */
    String transactionManager() default "";

    /**
     * Transaction scope name for grouping related transaction configurations.
     * Allows for transaction profiles and context-specific behavior.
     *
     * @return transaction scope name, defaults to empty
     */
    String scope() default "";

    /**
     * Transaction propagation enumeration defining how transactions interact.
     */
    enum Propagation {
        /**
         * Default behavior. Creates a new transaction if none exists,
         * or joins the existing transaction if one is already active.
         * This is the most common propagation mode.
         */
        REQUIRED,

        /**
         * Always creates a new transaction, suspending any existing transaction.
         * The current transaction (if any) is temporarily suspended.
         * The new transaction will commit or rollback independently.
         */
        REQUIRES_NEW,

        /**
         * Executes within an existing transaction if one exists.
         * If no transaction exists, executes non-transactionally.
         * Never creates a new transaction or suspends existing ones.
         */
        SUPPORTS,

        /**
         * Always executes non-transactionally, suspending any existing transaction.
         * The current transaction (if any) is temporarily suspended.
         * This method never participates in any transaction.
         */
        NOT_SUPPORTED,

        /**
         * Executes within an existing transaction if one exists.
         * If no transaction exists, throws an exception.
         * Ensures the method always runs within a transaction.
         */
        MANDATORY,

        /**
         * Throws an exception if a transaction exists.
         * If no transaction exists, executes non-transactionally.
         * Ensures the method never participates in any transaction.
         */
        NEVER,

        /**
         * Creates a nested transaction within the existing transaction.
         * Uses database savepoints if supported, otherwise behaves like REQUIRED.
         * Allows for partial rollback of the nested transaction.
         */
        NESTED
    }

    /**
     * Transaction scope for grouping related transaction configurations.
     */
    enum Scope {
        /**
         * Default transaction scope with standard behavior.
         */
        DEFAULT,

        /**
         * Transaction scope for batch processing operations.
         */
        BATCH,

        /**
         * Transaction scope for real-time operations.
         */
        REALTIME,

        /**
         * Transaction scope for analytical operations.
         */
        ANALYTICAL,

        /**
         * Custom transaction scope for application-specific groupings.
         */
        CUSTOM
    }

    /**
     * Transaction isolation level defining the transaction locking behavior.
     */
    enum Isolation {
        /**
         * Uses the database's default isolation level.
         * This is usually the safest and most performant option.
         */
        DEFAULT,

        /**
         * Lowest isolation level. Prevents dirty reads but allows:
         * - Non-repeatable reads (same data appears different during transaction)
         * - Phantom reads (new rows appear during transaction)
         */
        READ_UNCOMMITTED,

        /**
         * Prevents dirty reads and non-repeatable reads, but allows phantom reads.
         * Good balance between consistency and performance.
         */
        READ_COMMITTED,

        /**
         * Prevents dirty reads, non-repeatable reads, and phantom reads.
         * Uses row-level locks for consistency.
         */
        REPEATABLE_READ,

        /**
         * Highest isolation level. Prevents all concurrency issues:
         * - Dirty reads, non-repeatable reads, phantom reads
         * - Uses table-level locks, lowest performance
         */
        SERIALIZABLE
    }
}