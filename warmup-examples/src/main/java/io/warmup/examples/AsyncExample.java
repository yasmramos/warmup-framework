package io.warmup.examples;

import io.warmup.framework.annotation.Component;
import io.warmup.framework.annotation.Inject;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

@Component
public class AsyncExample {

    @Inject
    private AsyncService asyncService;

    public void runAsyncExample() {
        System.out.println("Iniciando tareas asíncronas...");

        CompletableFuture<String> future1 = asyncService.processData("hello");
        CompletableFuture<Integer> future2 = asyncService.calculateAsync(42);

        // Combinar resultados
        CompletableFuture<Void> all = CompletableFuture.allOf(future1, future2);

        all.thenRun(() -> {
            try {
                System.out.println("Resultado 1: " + future1.get());
                System.out.println("Resultado 2: " + future2.get());
                System.out.println("¡Todas las tareas completadas!");
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
        });

        System.out.println("Método principal continúa ejecutándose...");
    }
}
