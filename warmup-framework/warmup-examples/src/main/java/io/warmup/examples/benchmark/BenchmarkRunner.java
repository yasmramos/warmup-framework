package io.warmup.examples.benchmark;

import io.warmup.benchmarks.WarmupCoreBenchmark;
import io.warmup.benchmarks.WarmupStartupBenchmark;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

public class BenchmarkRunner {

    public static void main(String[] args) throws RunnerException, Exception {
        System.out.println("🎯 EJECUTANDO SUITE COMPLETA DE BENCHMARKS WARMUP\n");

        // Ejecutar benchmarks individuales
//        runBenchmark("ComponentScanner", ComponentScannerJMHBenchmark.class);
//        runBenchmark("SequentialVsParallel", SequentialVsParallelBenchmark.class);
//        runBenchmark("ParallelismLevel", ParallelismLevelBenchmark.class);
//        runBenchmark("Scalability", ScalabilityBenchmark.class);
        // runBenchmark("LargeProject", LargeProjectSimulationBenchmark.class);
        runBenchmark("WarmupCoreBenchmark", WarmupCoreBenchmark.class);
        runBenchmark("WarmupStartupBenchmark", WarmupStartupBenchmark.class);
        BenchmarkResultAnalyzer.main(args);
    }

    private static void runBenchmark(String name, Class<?> benchmarkClass) throws RunnerException {
        System.out.println("\nEjecutando: " + name);

        Options opt = new OptionsBuilder()
                .include(benchmarkClass.getSimpleName())
                .result("results/" + name.toLowerCase() + "-results.json")
                .resultFormat(org.openjdk.jmh.results.format.ResultFormatType.JSON)
                .build();

        new Runner(opt).run();
    }
}
