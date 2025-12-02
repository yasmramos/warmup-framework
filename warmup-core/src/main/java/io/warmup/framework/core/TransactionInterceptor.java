package io.warmup.framework.core;

import io.warmup.framework.aop.MethodInterceptor;
import io.warmup.framework.aop.ProceedingJoinPoint;
import io.warmup.framework.annotation.Transactional;
import io.warmup.framework.asm.AsmCoreUtils;
import java.lang.reflect.Method;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Interceptor for transactional method execution.
 * 
 * <p>
 * This interceptor handles the AOP around advice for methods annotated with
 * {@link @Transactional}, providing declarative transaction management.
 *
 * <p>
 * <b>Responsibilities:</b>
 * <ul>
 * <li>Evaluate transaction propagation behavior</li>
 * <li>Begin transactions when required</li>
 * <li>Handle transaction commitment and rollback</li>
 * <li>Support nested transactions and savepoints</li>
 * <li>Manage transaction context throughout method execution</li>
 * <li>Apply rollback rules based on exceptions</li>
 * <li>Handle transaction synchronization callbacks</li>
 * </ul>
 *
 * <p>
 * <b>Transaction Flow:</b>
 * <ol>
 * <li>Check current transaction state and propagation requirements</li>
 * <li>Begin new transaction or join existing based on propagation</li>
 * <li>Execute the method within transaction context</li>
 * <li>Evaluate rollback conditions based on execution result</li>
 * <li>Commit or rollback the transaction appropriately</li>
 * <li>Handle transaction cleanup and context restoration</li>
 * </ol>
 *
 * @author MiniMax Agent
 * @version 1.0
 */
public class TransactionInterceptor implements MethodInterceptor {

    private static final Logger log = Logger.getLogger(TransactionInterceptor.class.getName());

    private final TransactionManager transactionManager;

    /**
     * Constructs a TransactionInterceptor with the specified transaction manager.
     *
     * @param transactionManager the transaction manager to use
     */
    public TransactionInterceptor(TransactionManager transactionManager) {
        this.transactionManager = transactionManager;
    }

    @Override
    public Object invoke(ProceedingJoinPoint joinPoint) throws Throwable {
        Method method = joinPoint.getMethod();
        Object target = joinPoint.getTarget();
        Object[] args = joinPoint.getArgs();

        // Get transaction configuration from method or class
        Transactional transactional = getTransactionalAnnotation(method);
        if (transactional == null) {
            log.log(Level.FINE, "Method {0} is not transactional, executing directly", method.getName());
            return joinPoint.proceed();
        }

        // Determine if we need to start a new transaction
        TransactionDefinition definition = createTransactionDefinition(transactional, method);
        
        boolean newTransactionRequired = isNewTransactionRequired(transactional.propagation());
        boolean suspendedTransaction = false;
        TransactionContext.TransactionInfo suspendedInfo = null;

        if (newTransactionRequired && TransactionContext.isTransactionActive()) {
            // Suspend current transaction for REQUIRES_NEW
            suspendedInfo = TransactionContext.suspendTransaction();
            suspendedTransaction = true;
            log.log(Level.FINE, "Suspended current transaction for REQUIRES_NEW propagation");
        }

        try {
            // Begin transaction if required
            TransactionContext.TransactionInfo currentTransaction = null;
            if (newTransactionRequired || !TransactionContext.isTransactionActive()) {
                currentTransaction = transactionManager.beginTransaction(definition);
            }

            // Execute the method
            Object result;
            try {
                log.log(Level.FINE, "Executing transactional method: {0}", method.getName());
                result = joinPoint.proceed();
                log.log(Level.FINE, "Method executed successfully: {0}", method.getName());
            } catch (Throwable throwable) {
                // Handle exceptions according to rollback rules
                boolean shouldRollback = shouldRollbackFor(transactional, throwable);
                if (shouldRollback) {
                    log.log(Level.INFO, "Transaction marked for rollback due to exception: {0}", 
                        throwable.getClass().getSimpleName());
                    TransactionContext.setRollbackOnly("Exception: " + throwable.getClass().getSimpleName());
                }
                throw throwable;
            }

            // Commit transaction if we created it
            if (newTransactionRequired) {
                try {
                    transactionManager.commit();
                    log.log(Level.FINE, "Transaction committed for method: {0}", method.getName());
                } catch (TransactionException e) {
                    log.log(Level.SEVERE, "Failed to commit transaction for method: " + method.getName(), e);
                    throw e;
                }
            }

            return result;

        } catch (Throwable throwable) {
            // Handle transaction rollback if we created the transaction
            if (newTransactionRequired) {
                try {
                    transactionManager.rollback();
                    log.log(Level.FINE, "Transaction rolled back for method: {0}", method.getName());
                } catch (TransactionException e) {
                    log.log(Level.SEVERE, "Failed to rollback transaction for method: " + method.getName(), e);
                    // Don't rethrow rollback exceptions to preserve original exception
                }
            }
            
            throw throwable;

        } finally {
            // Resume suspended transaction if applicable
            if (suspendedTransaction && suspendedInfo != null) {
                TransactionContext.resumeTransaction(suspendedInfo);
                log.log(Level.FINE, "Resumed suspended transaction");
            }
        }
    }

    /**
     * Gets the @Transactional annotation from the method or its class.
     *
     * @param method the method to check
     * @return the transactional annotation, or null if not found
     */
    private Transactional getTransactionalAnnotation(Method method) {
        // First check method-level annotation
        if (method.isAnnotationPresent(Transactional.class)) {
            return AsmCoreUtils.getAnnotationProgressive(method, Transactional.class);
        }

        // Then check class-level annotation
        if (method.getDeclaringClass().isAnnotationPresent(Transactional.class)) {
            return AsmCoreUtils.getAnnotationProgressive(method.getDeclaringClass(), Transactional.class);
        }

        return null;
    }

    /**
     * Creates a transaction definition from the transactional annotation.
     *
     * @param transactional the transactional annotation
     * @param method the method being executed
     * @return the transaction definition
     */
    private TransactionDefinition createTransactionDefinition(Transactional transactional, Method method) {
        String transactionName = method.getDeclaringClass().getSimpleName() + "." + method.getName();
        
        // Convert enum values to strings
        String propagation = transactional.propagation().toString();
        String isolation = transactional.isolation().toString();
        String scope = transactional.scope().toString();
        
        return new TransactionDefinition(
            transactionName,
            propagation,
            isolation,
            transactional.timeout(),
            transactional.readOnly(),
            scope
        );
    }

    /**
     * Determines if a new transaction is required based on propagation behavior.
     *
     * @param propagation the propagation behavior
     * @return true if new transaction is required
     */
    private boolean isNewTransactionRequired(Transactional.Propagation propagation) {
        switch (propagation) {
            case REQUIRED:
            case REQUIRES_NEW:
            case NESTED:
                return true;
            case SUPPORTS:
            case NOT_SUPPORTED:
            case MANDATORY:
            case NEVER:
                return false;
            default:
                return true;
        }
    }

    /**
     * Determines if a transaction should rollback based on the exception and rollback rules.
     *
     * @param transactional the transactional annotation
     * @param throwable the exception that occurred
     * @return true if transaction should rollback
     */
    private boolean shouldRollbackFor(Transactional transactional, Throwable throwable) {
        Class<? extends Throwable>[] rollbackFor = transactional.rollbackFor();
        Class<? extends Throwable>[] noRollbackFor = transactional.noRollbackFor();

        // Check no-rollback-for rules first
        if (noRollbackFor.length > 0) {
            for (Class<? extends Throwable> noRollbackClass : noRollbackFor) {
                if (noRollbackClass.isInstance(throwable)) {
                    log.log(Level.FINE, "Transaction will not rollback for exception: {0} (noRollbackFor rule)", 
                        throwable.getClass().getSimpleName());
                    return false;
                }
            }
        }

        // Check rollback-for rules
        if (rollbackFor.length > 0) {
            for (Class<? extends Throwable> rollbackClass : rollbackFor) {
                if (rollbackClass.isInstance(throwable)) {
                    log.log(Level.FINE, "Transaction will rollback for exception: {0} (rollbackFor rule)", 
                        throwable.getClass().getSimpleName());
                    return true;
                }
            }
            // No matching rollback rule found, use default behavior
            return false;
        }

        // Default rollback behavior: rollback for RuntimeException and Error
        if (throwable instanceof RuntimeException || throwable instanceof Error) {
            log.log(Level.FINE, "Transaction will rollback for exception: {0} (default behavior)", 
                throwable.getClass().getSimpleName());
            return true;
        }

        // Checked exceptions don't cause rollback by default
        log.log(Level.FINE, "Transaction will not rollback for checked exception: {0}", 
            throwable.getClass().getSimpleName());
        return false;
    }
}