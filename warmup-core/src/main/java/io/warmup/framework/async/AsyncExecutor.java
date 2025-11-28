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

        CompletableFuture<Object> future = CompletableFuture.supplyAsync(() -> {
            try {
                return task.call();
            } catch (Exception e) {
                System.out.println("Caught Exception in AsyncExecutor: " + e.getMessage() + " - handling with: " + exceptionHandling);
                // Para excepciones específicas según el manejo de errores
                return handleException(e, exceptionHandling);
            } catch (Throwable e) {
                System.out.println("Caught Throwable in AsyncExecutor: " + e.getMessage() + " - handling with: " + exceptionHandling);
                // Para otros tipos de errores
                return handleException(e, exceptionHandling);
            }
        }, executor);

        // Aplicar timeout si es necesario (compatible con Java 8)
        if (timeout > 0) {
            CompletableFuture<Object> timeoutFuture = new CompletableFuture<>();
            ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
            
            // Programar timeout
            ScheduledFuture<?> timeoutTask = scheduler.schedule(() -> {
                timeoutFuture.completeExceptionally(new TimeoutException("Task timeout after " + timeout + " milliseconds"));
                scheduler.shutdown();
            }, timeout, TimeUnit.MILLISECONDS);
            
            // Combinar con el future original
            return future.applyToEither(timeoutFuture, result -> result);
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
