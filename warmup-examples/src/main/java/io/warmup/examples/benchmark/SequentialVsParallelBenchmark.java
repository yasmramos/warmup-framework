package io.warmup.examples.benchmark;

import io.warmup.framework.config.PropertySource;
import io.warmup.framework.core.WarmupContainer;
import java.util.concurrent.TimeUnit;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.results.format.ResultFormatType;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

@BenchmarkMode(Mode.Throughput)
@OutputTimeUnit(TimeUnit.SECONDS)
@State(Scope.Benchmark)
@Warmup(iterations = 3, time = 2)
@Measurement(iterations = 5, time = 3)
@Fork(2)
public class SequentialVsParallelBenchmark {

    private PropertySource propertySource;

    @Setup
    public void setup() {
        propertySource = new PropertySource();
        propertySource.setProperty("app.name", "BenchmarkApp");
        propertySource.setProperty("profiles.active", "dev");

        System.out.println("⚡ Configuración preparada para benchmark comparativo");
    }

    // Método para forzar modo secuencial usando reflexión
    private void forceSequentialMode(WarmupContainer container) {
        try {
            // Usar reflexión para acceder al ComponentScanner y modificar su paralelismo
            java.lang.reflect.Field scannerField = WarmupContainer.class.getDeclaredField("componentScanner");
            scannerField.setAccessible(true);
            Object scanner = scannerField.get(container);

            java.lang.reflect.Field executorField = scanner.getClass().getDeclaredField("parallelExecutor");
            executorField.setAccessible(true);

            // Crear un executor con 1 hilo (secuencial)
            java.util.concurrent.ExecutorService sequentialExecutor = java.util.concurrent.Executors.newFixedThreadPool(1);
            executorField.set(scanner, sequentialExecutor);

        } catch (Exception e) {
            System.out.println("⚠️ No se pudo forzar modo secuencial: " + e.getMessage());
        }
    }

    public static void main(String[] args) throws RunnerException {
        System.out.println("🔬 Benchmark Comparativo: Secuencial vs Paralelo\n");

        Options opt = new OptionsBuilder()
                .include(SequentialVsParallelBenchmark.class.getSimpleName())
                .result("comparative-results.json")
                .resultFormat(ResultFormatType.JSON)
                .build();

        new Runner(opt).run();
    }
}
