package io.warmup.benchmark.startup;

import org.openjdk.jmh.annotations.*;
import java.util.concurrent.TimeUnit;

@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MICROSECONDS)
@Warmup(iterations = 2, time = 1)
@Measurement(iterations = 3, time = 1)
@Fork(0)
@State(Scope.Benchmark)
public class SimpleStartupBenchmark {

    @Benchmark
    public void simpleBenchmark() {
        // Simular trabajo básico sin WarmupContainer
        int sum = 0;
        for (int i = 0; i < 100; i++) {
            sum += i * 2;
        }
        if (sum < 0) {
            throw new RuntimeException("Unexpected result");
        }
    }
}