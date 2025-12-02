package io.warmup.benchmark;

import org.openjdk.jmh.results.format.ResultFormatType;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;
import org.openjdk.jmh.runner.options.VerboseMode;

import java.io.File;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Comprehensive Benchmark Runner for Warmup Framework
 * 
 * This runner executes all benchmark suites and provides organized results
 * with detailed performance analysis and comparisons.
 * 
 * @author MiniMax Agent
 * @version 1.0.0
 */
public class BenchmarkRunner {
    
    private static final String RESULTS_DIR = "benchmark-results";
    private static final DateTimeFormatter TIMESTAMP_FORMAT = 
        DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss");
    
    public static void main(String[] args) throws RunnerException {
        System.out.println("================================================================================");
        System.out.println("WARMUP FRAMEWORK BENCHMARK SUITE");
        System.out.println("Comprehensive Performance Analysis & O(1) Optimization Validation");
        System.out.println("================================================================================");
        System.out.println("Timestamp: " + LocalDateTime.now().format(TIMESTAMP_FORMAT));
        System.out.println();
        
        // Ensure results directory exists
        new File(RESULTS_DIR).mkdirs();
        
        // Determine benchmark mode from arguments
        BenchmarkMode mode = parseBenchmarkMode(args);
        
        switch (mode) {
            case ALL:
                runAllBenchmarks();
                break;
            case FRAMEWORK_COMPARISON:
                runFrameworkComparisonBenchmarks();
                break;
            case AOP_OPTIMIZATION:
                runAOPOptimizationBenchmarks();
                break;
            case CONTAINER_OPTIMIZATION:
                runContainerOptimizationBenchmarks();
                break;
            case STARTUP_BENCHMARKS:
                runStartupBenchmarks();
                break;
            case QUICK:
                runQuickBenchmarks();
                break;
            default:
                runAllBenchmarks();
                break;
        }
    }
    
    /**
     * Run all benchmark suites (comprehensive analysis)
     */
    private static void runAllBenchmarks() throws RunnerException {
        System.out.println("🚀 Running ALL Benchmark Suites...");
        System.out.println("Duration: ~15-20 minutes");
        System.out.println();
        
        String timestamp = LocalDateTime.now().format(TIMESTAMP_FORMAT);
        String resultsFile = RESULTS_DIR + "/warmup_framework_complete_" + timestamp + ".json";
        
        Options opt = new OptionsBuilder()
            .include(".*") // Include all benchmarks
            .exclude(".*Hot.*") // Exclude hot path tests (integration tests)
            .warmupIterations(5)
            .measurementIterations(10)
            .forks(2)
            .threads(1)
            .jvmArgs("-Xms512m", "-Xmx1024m", "-XX:+UseG1GC")
            .resultFormat(ResultFormatType.JSON)
            .result(resultsFile)
            .verbosity(VerboseMode.EXTRA)
            .build();
        
        new Runner(opt).run();
        
        printSummary(resultsFile);
    }
    
    /**
     * Run framework comparison benchmarks (Warmup vs Traditional DI)
     */
    private static void runFrameworkComparisonBenchmarks() throws RunnerException {
        System.out.println("📊 Running Framework Comparison Benchmarks...");
        System.out.println("Comparing Warmup Framework vs Spring, Guice, Dagger");
        System.out.println("Duration: ~8-12 minutes");
        System.out.println();
        
        String timestamp = LocalDateTime.now().format(TIMESTAMP_FORMAT);
        String resultsFile = RESULTS_DIR + "/framework_comparison_" + timestamp + ".json";
        
        Options opt = new OptionsBuilder()
            .include(".*Framework.*|.*ProductionFramework.*|.*WarmupOnly.*")
            .warmupIterations(3)
            .measurementIterations(8)
            .forks(2)
            .threads(1)
            .jvmArgs("-Xms512m", "-Xmx1024m", "-XX:+UseG1GC")
            .resultFormat(ResultFormatType.JSON)
            .result(resultsFile)
            .verbosity(VerboseMode.EXTRA)
            .build();
        
        new Runner(opt).run();
        
        printSummary(resultsFile);
    }
    
    /**
     * Run AOP optimization benchmarks (O(1) vs O(n) analysis)
     */
    private static void runAOPOptimizationBenchmarks() throws RunnerException {
        System.out.println("⚡ Running AOP Optimization Benchmarks...");
        System.out.println("Validating O(1) vs O(n) performance improvements");
        System.out.println("Duration: ~5-8 minutes");
        System.out.println();
        
        String timestamp = LocalDateTime.now().format(TIMESTAMP_FORMAT);
        String resultsFile = RESULTS_DIR + "/aop_optimization_" + timestamp + ".json";
        
        Options opt = new OptionsBuilder()
            .include(".*AOP.*|.*AspectManager.*|.*EventIndexEngine.*")
            .warmupIterations(2)
            .measurementIterations(6)
            .forks(2)
            .threads(1)
            .jvmArgs("-Xms256m", "-Xmx512m")
            .resultFormat(ResultFormatType.JSON)
            .result(resultsFile)
            .verbosity(VerboseMode.EXTRA)
            .build();
        
        new Runner(opt).run();
        
        printSummary(resultsFile);
    }
    
    /**
     * Run Container optimization benchmarks
     */
    private static void runContainerOptimizationBenchmarks() throws RunnerException {
        System.out.println("🏗️ Running Container Optimization Benchmarks...");
        System.out.println("Validating WarmupContainer O(1) optimizations");
        System.out.println("Duration: ~4-6 minutes");
        System.out.println();
        
        String timestamp = LocalDateTime.now().format(TIMESTAMP_FORMAT);
        String resultsFile = RESULTS_DIR + "/container_optimization_" + timestamp + ".json";
        
        Options opt = new OptionsBuilder()
            .include(".*Container.*|.*WarmupContainer.*")
            .warmupIterations(2)
            .measurementIterations(5)
            .forks(1)
            .threads(1)
            .jvmArgs("-Xms256m", "-Xmx512m")
            .resultFormat(ResultFormatType.JSON)
            .result(resultsFile)
            .verbosity(VerboseMode.EXTRA)
            .build();
        
        new Runner(opt).run();
        
        printSummary(resultsFile);
    }
    
    /**
     * Run startup performance benchmarks
     */
    private static void runStartupBenchmarks() throws RunnerException {
        System.out.println("🚀 Running Startup Performance Benchmarks...");
        System.out.println("Application startup time analysis and optimization");
        System.out.println("Duration: ~3-5 minutes");
        System.out.println();
        
        String timestamp = LocalDateTime.now().format(TIMESTAMP_FORMAT);
        String resultsFile = RESULTS_DIR + "/startup_performance_" + timestamp + ".json";
        
        Options opt = new OptionsBuilder()
            .include(".*Startup.*|.*Baseline.*|.*ExtremeStartup.*")
            .warmupIterations(2)
            .measurementIterations(5)
            .forks(1)
            .threads(1)
            .jvmArgs("-Xms256m", "-Xmx512m")
            .resultFormat(ResultFormatType.JSON)
            .result(resultsFile)
            .verbosity(VerboseMode.EXTRA)
            .build();
        
        new Runner(opt).run();
        
        printSummary(resultsFile);
    }
    
    /**
     * Run quick benchmark suite for development/testing
     */
    private static void runQuickBenchmarks() throws RunnerException {
        System.out.println("⚡ Running Quick Benchmark Suite...");
        System.out.println("Fast validation for development and testing");
        System.out.println("Duration: ~1-2 minutes");
        System.out.println();
        
        String timestamp = LocalDateTime.now().format(TIMESTAMP_FORMAT);
        String resultsFile = RESULTS_DIR + "/quick_benchmark_" + timestamp + ".json";
        
        Options opt = new OptionsBuilder()
            .include(".*O1.*|.*Simple.*|.*Direct.*")
            .warmupIterations(1)
            .measurementIterations(3)
            .forks(1)
            .threads(1)
            .jvmArgs("-Xms128m", "-Xmx256m")
            .resultFormat(ResultFormatType.JSON)
            .result(resultsFile)
            .verbosity(VerboseMode.NORMAL)
            .build();
        
        new Runner(opt).run();
        
        printSummary(resultsFile);
    }
    
    /**
     * Parse benchmark mode from command line arguments
     */
    private static BenchmarkMode parseBenchmarkMode(String[] args) {
        if (args.length == 0) {
            return BenchmarkMode.ALL;
        }
        
        String modeArg = args[0].toLowerCase();
        switch (modeArg) {
            case "all":
                return BenchmarkMode.ALL;
            case "comparison":
                return BenchmarkMode.FRAMEWORK_COMPARISON;
            case "aop":
                return BenchmarkMode.AOP_OPTIMIZATION;
            case "container":
                return BenchmarkMode.CONTAINER_OPTIMIZATION;
            case "startup":
                return BenchmarkMode.STARTUP_BENCHMARKS;
            case "quick":
                return BenchmarkMode.QUICK;
            default:
                return BenchmarkMode.ALL;
        }
    }
    
    /**
     * Print benchmark execution summary
     */
    private static void printSummary(String resultsFile) {
        System.out.println();
        System.out.println("================================================================================");
        System.out.println("✅ BENCHMARK EXECUTION COMPLETED");
        System.out.println("================================================================================");
        System.out.println("Results saved to: " + resultsFile);
        System.out.println();
        System.out.println("📊 Quick Analysis Commands:");
        System.out.println("  cat " + resultsFile + " | jq '.results[].primaryMetric.score'");
        System.out.println("  cat " + resultsFile + " | jq '.results[] | {name, score}'");
        System.out.println();
        System.out.println("🎯 Key Performance Indicators:");
        System.out.println("  - O(1) vs O(n) complexity validation");
        System.out.println("  - Framework startup time comparisons");
        System.out.println("  - Memory usage optimization analysis");
        System.out.println("  - Throughput and latency metrics");
        System.out.println();
        System.out.println("Completed at: " + LocalDateTime.now().format(TIMESTAMP_FORMAT));
        System.out.println("================================================================================");
    }
    
    /**
     * Benchmark execution modes
     */
    private enum BenchmarkMode {
        ALL("All benchmark suites"),
        FRAMEWORK_COMPARISON("Framework comparison benchmarks"),
        AOP_OPTIMIZATION("AOP optimization benchmarks"),
        CONTAINER_OPTIMIZATION("Container optimization benchmarks"),
        STARTUP_BENCHMARKS("Startup performance benchmarks"),
        QUICK("Quick development benchmarks");
        
        private final String description;
        
        BenchmarkMode(String description) {
            this.description = description;
        }
        
        public String getDescription() {
            return description;
        }
    }
}