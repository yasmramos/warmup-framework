package io.warmup.framework.async;

import io.warmup.framework.annotation.Async;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Gestor de ejecución asíncrona mejorado
 */
public class AsyncExecutor implements Executor {

    private static final AsyncExecutor INSTANCE = new AsyncExecutor();
    private final ConcurrentHashMap<String, ExecutorService> executors = new ConcurrentHashMap<>();

    private AsyncExecutor() {
        // Executor por defecto
        executors.put("default", createExecutor("default-async"));
    }

    public static AsyncExecutor getInstance() {
        return INSTANCE;
    }

    public CompletableFuture<Object> executeAsync(String executorName, Callable<Object> task,
            int timeout, Async.ExceptionHandling exceptionHandling) {
        ExecutorService executor = executors.computeIfAbsent(executorName,
                name -> createExecutor(name + "-async"));

        CompletableFuture<Object> future = new CompletableFuture<>();
        
        executor.submit(() -> {
            try {
                System.out.println("🔥 DEBUG: AsyncExecutor ejecutando tarea...");
                Object result = task.call();
                System.out.println("🔥 DEBUG: AsyncExecutor tarea completada exitosamente, resultado: " + result);
                if (exceptionHandling == Async.ExceptionHandling.RETURN_NULL && result == null) {
                    // Si RETURN_NULL y el resultado es null, completamos normalmente
                    future.complete(result);
                } else {
                    future.complete(result);
                }
            } catch (Exception e) {
                System.out.println("🔥 DEBUG: Caught Exception in AsyncExecutor: " + e.getMessage() + " - handling with: " + exceptionHandling);
                // Manejar según el tipo de excepción
                if (exceptionHandling == Async.ExceptionHandling.RETURN_NULL) {
                    System.out.println("🔥 DEBUG: Completando future con null (RETURN_NULL)");
                    future.complete(null);
                } else {
                    // Para COMPLETE_EXCEPTIONALLY, completar excepcionalmente
                    System.out.println("🔥 DEBUG: Completando future excepcionalmente");
                    future.completeExceptionally(e);
                }
            } catch (Throwable e) {
                System.out.println("🔥 DEBUG: Caught Throwable in AsyncExecutor: " + e.getMessage() + " - handling with: " + exceptionHandling);
                if (exceptionHandling == Async.ExceptionHandling.RETURN_NULL) {
                    System.out.println("🔥 DEBUG: Completando future con null (RETURN_NULL)");
                    future.complete(null);
                } else {
                    System.out.println("🔥 DEBUG: Completando future excepcionalmente");
                    future.completeExceptionally(e);
                }
            }
        });

        // Aplicar timeout si es necesario (compatible con Java 8)
        if (timeout > 0) {
            ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
            
            // Crear un future para el timeout
            CompletableFuture<Object> timeoutFuture = new CompletableFuture<>();
            
            // Programar timeout
            ScheduledFuture<?> timeoutTask = scheduler.schedule(() -> {
                if (!future.isDone()) {
                    timeoutFuture.completeExceptionally(new TimeoutException("Task timeout after " + timeout + " milliseconds"));
                }
            }, timeout, TimeUnit.MILLISECONDS);
            
            // Combinar el future original con el timeout
            CompletableFuture<Object> combinedFuture = future.applyToEither(timeoutFuture, result -> result);
            
            // Asegurar que el scheduler se cierre cuando el future se complete
            combinedFuture.whenComplete((result, throwable) -> {
                if (!timeoutTask.isDone()) {
                    timeoutTask.cancel(false);
                }
                scheduler.shutdown();
            });
            
            return combinedFuture;
        }

        return future;
    }

    private Object handleException(Throwable throwable, Async.ExceptionHandling exceptionHandling) {
        switch (exceptionHandling) {
            case RETURN_NULL:
                // Para RETURN_NULL, devolver null en lugar de lanzar la excepción
                return null;
            case RETRY:
                // Futura implementación de retry logic
                throw new CompletionException("Retry not yet implemented", throwable);
            case COMPLETE_EXCEPTIONALLY:
            default:
                // Para COMPLETE_EXCEPTIONALLY, propagar la excepción envuelta en CompletionException
                if (throwable instanceof CompletionException) {
                    throw (CompletionException) throwable;
                } else {
                    throw new CompletionException(throwable);
                }
        }
    }

    private Object handleTimeout(Async.ExceptionHandling exceptionHandling) {
        switch (exceptionHandling) {
            case RETURN_NULL:
                return null;
            case RETRY:
                throw new CompletionException(new TimeoutException("Operation timed out"));
            case COMPLETE_EXCEPTIONALLY:
            default:
                throw new CompletionException(new TimeoutException("Operation timed out"));
        }
    }

    private ExecutorService createExecutor(String name) {
        AtomicInteger counter = new AtomicInteger();
        return new ThreadPoolExecutor(
                4, 16, 60L, TimeUnit.SECONDS,
                new LinkedBlockingQueue<>(100),
                r -> {
                    Thread thread = new Thread(r, name + "-" + counter.incrementAndGet());
                    thread.setDaemon(true);
                    return thread;
                },
                new ThreadPoolExecutor.CallerRunsPolicy()
        );
    }

    public void shutdown() {
        executors.values().forEach(ExecutorService::shutdown);
    }

    @Override
    public void execute(Runnable command) {
        ExecutorService defaultExecutor = executors.computeIfAbsent("default",
                name -> createExecutor(name + "-async"));
        defaultExecutor.execute(command);
    }

    // Método para obtener estadísticas de los executors
    public void printExecutorStats() {
        executors.forEach((name, executor) -> {
            if (executor instanceof ThreadPoolExecutor) {
                ThreadPoolExecutor tpe = (ThreadPoolExecutor) executor;
                System.out.printf("Executor '%s': PoolSize=%d, Active=%d, Queue=%d%n",
                        name, tpe.getPoolSize(), tpe.getActiveCount(), tpe.getQueue().size());
            }
        });
    }
}
