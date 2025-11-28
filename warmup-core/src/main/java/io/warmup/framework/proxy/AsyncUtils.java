package io.warmup.framework.proxy;

import java.util.concurrent.ExecutionException;

/**
 * Clase de utilidad para operaciones asincrónicas
 */
public class AsyncUtils {

    public static <T> java.util.concurrent.CompletableFuture<T> toCompletableFuture(
            java.util.concurrent.Future<T> future) {
        return java.util.concurrent.CompletableFuture.supplyAsync(() -> {
            try {
                return future.get();
            } catch (InterruptedException | ExecutionException e) {
                throw new RuntimeException(e);
            }
        });
    }
}
