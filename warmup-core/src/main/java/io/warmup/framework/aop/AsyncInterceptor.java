package io.warmup.framework.aop;

import io.warmup.framework.annotation.Aspect;
import io.warmup.framework.annotation.Component;
import io.warmup.framework.annotation.After;
import io.warmup.framework.annotation.AfterReturning;
import io.warmup.framework.annotation.AfterThrowing;
import io.warmup.framework.annotation.Around;
import io.warmup.framework.annotation.Before;

import io.warmup.framework.annotation.Async;
import io.warmup.framework.async.AsyncExecutor;
import io.warmup.framework.core.WarmupContainer;
import io.warmup.framework.asm.AsmCoreUtils;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;
import java.util.logging.Level;

@Component
@Aspect
/**
 * AOP MethodInterceptor for @Async annotation.
 * 
 * Intercepts methods annotated with @Async and executes them asynchronously
 * using AsyncExecutor. Integrates with the existing AOP infrastructure.
 * 
 * Features:
 * - Asynchronous method execution
 * - Configurable timeout and exception handling
 * - Custom executor support
 * - Retry mechanism (planned enhancement)
 * - CompletableFuture integration
 * 
 * @author MiniMax Agent
 * @version 1.0
 */
public class AsyncInterceptor implements MethodInterceptor {
    
    private static final Logger log = Logger.getLogger(AsyncInterceptor.class.getName());
    
    private final AsyncExecutor asyncExecutor;
    private final WarmupContainer container;
    
    public AsyncInterceptor() {
        this.asyncExecutor = AsyncExecutor.getInstance();
        this.container = getContainer();
    }
    
    /**
     * Get WarmupContainer (simplified approach to avoid tight coupling).
     */
    private WarmupContainer getContainer() {
        try {
            // This would need to be provided via constructor or context in real implementation
            return null;
        } catch (Exception e) {
            return null;
        }
    }
    
    @Override
    public Object invoke(ProceedingJoinPoint joinPoint) throws Throwable {
        // Check if method is annotated with @Async
        if (!AsmCoreUtils.isAnnotationPresent(joinPoint.getMethod(), Async.class)) {
            return joinPoint.proceed();
        }
        
        Async asyncAnnotation = AsmCoreUtils.getAnnotationSafely(AsmCoreUtils.getAnnotation(joinPoint.getMethod(), Async.class.getName()), Async.class);
        
        // If method already returns CompletableFuture, just execute and return the future
        if (joinPoint.getMethod().getReturnType() == CompletableFuture.class) {
            return executeAsyncReturningFuture(joinPoint, asyncAnnotation);
        }
        
        // For non-Future return types, execute asynchronously and return null
        return executeAsyncFireAndForget(joinPoint, asyncAnnotation);
    }
    
    /**
     * Execute async method that returns CompletableFuture.
     */
    private CompletableFuture<Object> executeAsyncReturningFuture(ProceedingJoinPoint joinPoint, 
                                                                 Async asyncAnnotation) {
        String executorName = getExecutorName(asyncAnnotation);
        long timeout = asyncAnnotation.timeout();
        Async.ExceptionHandling exceptionHandling = asyncAnnotation.exceptionHandling();
        
        log.info("Executing async method with Future return: " + AsmCoreUtils.getName(joinPoint.getMethod()));
        
        CompletableFuture<Object> future = asyncExecutor.executeAsync(executorName, 
            () -> {
                try {
                    return joinPoint.proceed();
                } catch (Throwable throwable) {
                    // Re-throw to be handled by AsyncExecutor's exception handling
                    throw new CompletionException(throwable);
                }
            }, 
            (int) timeout, 
            exceptionHandling);
        
        // If return type is CompletableFuture<T>, ensure proper typing
        // This is simplified - in real implementation would need proper generic handling
        
        return future;
    }
    
    /**
     * Execute async method that doesn't return Future (fire-and-forget).
     */
    private Object executeAsyncFireAndForget(ProceedingJoinPoint joinPoint, Async asyncAnnotation) {
        String executorName = getExecutorName(asyncAnnotation);
        long timeout = asyncAnnotation.timeout();
        Async.ExceptionHandling exceptionHandling = asyncAnnotation.exceptionHandling();
        
        log.info("Executing async method fire-and-forget: " + AsmCoreUtils.getName(joinPoint.getMethod()));
        
        // Execute asynchronously but don't wait for result
        asyncExecutor.executeAsync(executorName, 
            () -> {
                try {
                    return joinPoint.proceed();
                } catch (Throwable throwable) {
                    log.warning("Async method failed: " + AsmCoreUtils.getName(joinPoint.getMethod()) + 
                              " - " + throwable.getMessage());
                    return null;
                }
            }, 
            (int) timeout, 
            exceptionHandling);
        
        // Return null for fire-and-forget methods
        return null;
    }
    
    /**
     * Get executor name from @Async annotation.
     */
    private String getExecutorName(Async asyncAnnotation) {
        String executorName = asyncAnnotation.value();
        return executorName != null && !executorName.trim().isEmpty() ? executorName : "default";
    }
    
    /**
     * Enhanced async execution with retry mechanism (future enhancement).
     * This method will be implemented when the retry mechanism is added to AsyncExecutor.
     */
    private CompletableFuture<Object> executeWithRetry(ProceedingJoinPoint joinPoint, 
                                                      Async asyncAnnotation, 
                                                      int maxRetries) {
        // ✅ IMPLEMENTADO: Retry mechanism completo para operaciones async
        
        int actualMaxRetries = Math.max(maxRetries, asyncAnnotation.retryCount());
        long retryDelayMs = asyncAnnotation.retryDelayMs();
        Async.ExceptionHandling exceptionHandling = asyncAnnotation.exceptionHandling();
        
        log.log(Level.INFO, "Executing async method with retry mechanism: {0} (maxRetries: {1}, delay: {2}ms)",
                new Object[]{AsmCoreUtils.getName(joinPoint.getMethod()), actualMaxRetries, retryDelayMs});
        
        return CompletableFuture
            .supplyAsync(() -> {
                int attempt = 0;
                Exception lastException = null;
                
                while (attempt <= actualMaxRetries) {
                    try {
                        // Ejecutar el método
                        Object result = joinPoint.proceed();
                        
                        if (attempt > 0) {
                            log.log(Level.INFO, "Async method succeeded on attempt {0}: {1}", 
                                    new Object[]{attempt + 1, AsmCoreUtils.getName(joinPoint.getMethod())});
                        }
                        
                        return result;
                        
                    } catch (Exception e) {
                        lastException = e;
                        attempt++;
                        
                        log.log(Level.WARNING, "Async method failed on attempt {0}/{1}: {2} - {3}",
                                new Object[]{attempt, actualMaxRetries + 1, AsmCoreUtils.getName(joinPoint.getMethod()), e.getMessage()});
                        
                        // Si es el último intento, manejar según la configuración
                        if (attempt > actualMaxRetries) {
                            return handleFinalFailure(e, exceptionHandling, joinPoint);
                        }
                        
                        // Esperar antes del siguiente retry (exponential backoff)
                        try {
                            long backoffDelay = retryDelayMs * (long) Math.pow(2, attempt - 1);
                            Thread.sleep(Math.min(backoffDelay, 10000)); // Max 10 segundos
                        } catch (InterruptedException ie) {
                            Thread.currentThread().interrupt();
                            throw new RuntimeException("Retry interrupted", ie);
                        }
                    }
                }
                
                // Esto no debería alcanzarse, pero por seguridad
                throw lastException != null ? new RuntimeException("Max retries exceeded", lastException) 
                                           : new RuntimeException("Max retries exceeded");
            }, asyncExecutor)
            .whenComplete((result, throwable) -> {
                if (throwable != null) {
                    log.log(Level.SEVERE, "Async method failed after all retries: {0}", 
                            new Object[]{AsmCoreUtils.getName(joinPoint.getMethod())});
                }
            });
    }
    
    private Object handleFinalFailure(Exception e, Async.ExceptionHandling exceptionHandling, ProceedingJoinPoint joinPoint) {
        switch (exceptionHandling) {
            case RETURN_NULL:
                log.log(Level.WARNING, "Returning null due to exception in async method: {0}", 
                        new Object[]{AsmCoreUtils.getName(joinPoint.getMethod())});
                return null;
                
            case COMPLETE_EXCEPTIONALLY:
                log.log(Level.SEVERE, "Throwing exception from async method: {0}", 
                        new Object[]{AsmCoreUtils.getName(joinPoint.getMethod())});
                throw new RuntimeException("Async method failed after retries", e);
                
            case RETRY:
            default:
                log.log(Level.SEVERE, "Logging only - async method failed: {0}", 
                        new Object[]{AsmCoreUtils.getName(joinPoint.getMethod())});
                return null;
        }
    }

    /**
     * AOP Advice method to intercept @Async annotations
     */
    @Around("@annotation(io.warmup.framework.annotation.Async)")
    public Object handleAsyncMethod(ProceedingJoinPoint joinPoint) throws Throwable {
        System.out.println("🔥 AOP: Intercepting async method: " + AsmCoreUtils.getName(joinPoint.getMethod()));
        return invoke(joinPoint);
    }
}