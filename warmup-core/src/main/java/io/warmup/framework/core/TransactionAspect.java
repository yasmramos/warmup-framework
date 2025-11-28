package io.warmup.framework.core;

import io.warmup.framework.annotation.Around;
import io.warmup.framework.annotation.Aspect;
import io.warmup.framework.annotation.Transactional;
import io.warmup.framework.aop.ProceedingJoinPoint;
import java.lang.reflect.Method;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Aspect that manages transactional method execution.
 * 
 * <p>
 * This aspect provides declarative transaction management by intercepting
 * methods annotated with {@link @Transactional} and applying transaction
 * boundaries around their execution.
 *
 * @author MiniMax Agent
 * @version 1.0
 */
@Aspect
public class TransactionAspect {

    private static final Logger log = Logger.getLogger(TransactionAspect.class.getName());

    private final TransactionInterceptor transactionInterceptor;
    private final TransactionManager transactionManager;

    /**
     * Constructs a TransactionAspect with the specified transaction manager.
     *
     * @param transactionManager the transaction manager to use
     */
    public TransactionAspect(TransactionManager transactionManager) {
        this.transactionManager = transactionManager;
        this.transactionInterceptor = new TransactionInterceptor(transactionManager);
        log.log(Level.INFO, "TransactionAspect initialized");
    }

    /**
     * Intercepts transactional methods using around advice.
     *
     * @param joinPoint the join point representing the method call
     * @return the result of the method execution
     * @throws Throwable if method execution fails
     */
    @Around("@annotation(io.warmup.framework.annotation.Transactional)")
    public Object handleTransactionalMethod(ProceedingJoinPoint joinPoint) throws Throwable {
        // Delegate to transaction interceptor
        return transactionInterceptor.invoke(joinPoint);
    }

    /**
     * Gets the transaction manager used by this aspect.
     *
     * @return the transaction manager
     */
    public TransactionManager getTransactionManager() {
        return transactionManager;
    }

    /**
     * Gets the transaction interceptor used by this aspect.
     *
     * @return the transaction interceptor
     */
    public TransactionInterceptor getTransactionInterceptor() {
        return transactionInterceptor;
    }
}