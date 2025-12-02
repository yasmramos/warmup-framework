package io.warmup.examples;

import io.warmup.framework.annotation.Component;
import io.warmup.framework.annotation.Async;
import java.util.concurrent.CompletableFuture;

@Component
public class AsyncService {

    @Async
    public CompletableFuture<String> processData(String input) {
        // Simular procesamiento largo
        try {
            Thread.sleep(2000);
            return CompletableFuture.completedFuture("Procesado: " + input.toUpperCase());
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    @Async(timeout = 5000)
    public CompletableFuture<Integer> calculateAsync(int number) {
        // Cálculo intensivo
        int result = number * number;
        return CompletableFuture.completedFuture(result);
    }
}
