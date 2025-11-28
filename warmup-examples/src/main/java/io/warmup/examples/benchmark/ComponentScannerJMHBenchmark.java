package io.warmup.examples.benchmark;

import io.warmup.framework.config.PropertySource;
import java.util.concurrent.TimeUnit;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.results.format.ResultFormatType;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@State(Scope.Benchmark)
@Warmup(iterations = 3, time = 2, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 5, time = 3, timeUnit = TimeUnit.SECONDS)
@Fork(2)
@Threads(1)
public class ComponentScannerJMHBenchmark {

    private PropertySource propertySource;

    @Setup(Level.Trial)
    public void setup() {
        propertySource = new PropertySource();
        propertySource.setProperty("app.name", "BenchmarkApp");
        propertySource.setProperty("profiles.active", "dev,benchmark");
        
        System.out.println("Setup completado para JMH Benchmark");
    }

    @TearDown(Level.Trial)
    public void tearDown() {
        System.out.println("TearDown completado");
    }


    public static void main(String[] args) throws RunnerException {
        System.out.println("🚀 Iniciando JMH Benchmark para Warmup Component Scanner");
        System.out.println("💻 Procesadores disponibles: " + Runtime.getRuntime().availableProcessors());
        System.out.println("📦 Memoria máxima: " + Runtime.getRuntime().maxMemory() / 1024 / 1024 + " MB\n");

        Options opt = new OptionsBuilder()
                .include(ComponentScannerJMHBenchmark.class.getSimpleName())
                .result("jmh-results.json")
                .resultFormat(ResultFormatType.JSON)
                .build();

        new Runner(opt).run();
    }
}