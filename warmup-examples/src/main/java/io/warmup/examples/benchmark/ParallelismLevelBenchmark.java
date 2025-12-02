package io.warmup.examples.benchmark;

import io.warmup.framework.config.PropertySource;
import io.warmup.framework.core.WarmupContainer;
import java.util.concurrent.TimeUnit;
import org.openjdk.jmh.annotations.*;

@State(Scope.Benchmark)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@Warmup(iterations = 2, time = 1)
@Measurement(iterations = 3, time = 2)
@Fork(1)
public class ParallelismLevelBenchmark {

    @Param({"1", "2", "4", "8"})
    private int parallelismLevel;

    private PropertySource propertySource;

    @Setup
    public void setup() {
        propertySource = new PropertySource();
        propertySource.setProperty("app.name", "ParallelismTest");
        propertySource.setProperty("profiles.active", "dev");
    }

    private void configureParallelism(WarmupContainer container, int parallelism) {
        try {
            java.lang.reflect.Field scannerField = WarmupContainer.class.getDeclaredField("componentScanner");
            scannerField.setAccessible(true);
            Object scanner = scannerField.get(container);

            java.lang.reflect.Field executorField = scanner.getClass().getDeclaredField("parallelExecutor");
            executorField.setAccessible(true);

            Object currentExecutor = executorField.get(scanner);
            if (currentExecutor instanceof java.util.concurrent.ExecutorService) {
                ((java.util.concurrent.ExecutorService) currentExecutor).shutdown();
            }

            java.util.concurrent.ExecutorService newExecutor = java.util.concurrent.Executors.newFixedThreadPool(parallelism);
            executorField.set(scanner, newExecutor);

        } catch (IllegalAccessException | IllegalArgumentException | NoSuchFieldException e) {
            System.out.println("⚠️ No se pudo configurar paralelismo: " + e.getMessage());
        }
    }
}
