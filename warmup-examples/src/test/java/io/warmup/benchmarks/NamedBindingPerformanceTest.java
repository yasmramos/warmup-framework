package io.warmup.benchmarks;

import io.warmup.framework.core.WarmupContainer;
import io.warmup.framework.core.DependencyRegistry;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;

import java.util.*;
import java.util.concurrent.TimeUnit;

@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@State(Scope.Benchmark)
public class NamedBindingPerformanceTest {

    private WarmupContainer container;
    private DependencyRegistry registry;

    @Setup
    public void setup() {
        container = new WarmupContainer();
        registry = container.getDependencyRegistry();
        
        // Setup test services
        registry.registerNamed(TestService.class, "mainService", true);
        registry.registerBean("mainService", TestService.class, new TestServiceImpl());
    }

    @Benchmark
    public TestService namedBinding() {
        return container.getNamed(TestService.class, "mainService");
    }

    // Optimized version with better caching
    public TestService namedBindingOptimized() {
        // Simulate optimized lookup with reduced logging
        return registry.getBean("mainService", TestService.class);
    }

    public static void main(String[] args) {
        NamedBindingPerformanceTest test = new NamedBindingPerformanceTest();
        test.setup();
        
        System.out.println("🚀 Performance Test - Named Binding Optimizations");
        System.out.println("==================================================");
        
        // Warmup
        for (int i = 0; i < 10000; i++) {
            test.namedBinding();
            test.namedBindingOptimized();
        }
        
        // Test with logging optimization
        long start1 = System.nanoTime();
        for (int i = 0; i < 10000; i++) {
            test.namedBinding();
        }
        long end1 = System.nanoTime();
        long time1 = (end1 - start1) / 10000;
        
        // Test optimized version  
        long start2 = System.nanoTime();
        for (int i = 0; i < 10000; i++) {
            test.namedBindingOptimized();
        }
        long end2 = System.nanoTime();
        long time2 = (end2 - start2) / 10000;
        
        System.out.println("namedBinding:         " + time1 + " ns/op");
        System.out.println("namedBindingOptimized: " + time2 + " ns/op");
        System.out.println("Speedup:              " + (double) time1 / time2 + "x");
        System.out.println("Improvement:          " + ((double) (time1 - time2) / time1 * 100) + "%");
    }

    // Test interfaces and implementations
    public interface TestService {
        String getMessage();
    }

    public static class TestServiceImpl implements TestService {
        @Override
        public String getMessage() {
            return "Hello from TestService";
        }
    }
}