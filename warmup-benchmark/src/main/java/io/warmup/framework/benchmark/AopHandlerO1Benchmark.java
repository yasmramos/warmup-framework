package io.warmup.framework.benchmark;

import io.warmup.framework.aop.AspectInfo;
import io.warmup.framework.core.AopHandler;
import io.warmup.framework.core.WarmupContainer;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.results.format.ResultFormatType;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.io.File;
import java.lang.reflect.Method;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * JMH Benchmark for AopHandler O(1) optimizations
 * 
 * Tests the performance improvements from:
 * - O(1) aspects cache lookup
 * - O(1) method cache resolution  
 * - O(1) parameter types cache
 * - Atomic counters for metrics
 */
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@State(Scope.Benchmark)
@Fork(value = 2, jvmArgs = {"-Xmx2G", "-Xms1G"})
@Warmup(iterations = 3, time = 1, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 5, time = 2, timeUnit = TimeUnit.SECONDS)
public class AopHandlerO1Benchmark {

    private AopHandler optimizedAopHandler;
    private TestService testService;
    private Class<TestService> testServiceClass = TestService.class;
    
    // Test method signatures
    private static final String TEST_METHOD = "processData";
    private static final String TEST_METHOD_NO_PARAMS = "getStatus";
    private static final Object[] TEST_ARGS = {"test", 123, true};
    private static final Object[] NO_ARGS = {};
    
    @Setup
    public void setup() {
        WarmupContainer container = new WarmupContainer();
        optimizedAopHandler = new AopHandler(container);
        
        // Initialize test service
        testService = new TestService();
        
        // Pre-warm caches for fair comparison
        warmupCaches();
    }
    
    private void warmupCaches() {
        // Warm up method caches
        for (int i = 0; i < 100; i++) {
            try {
                optimizedAopHandler.invokeMethodWithAspects(testService, TEST_METHOD, TEST_ARGS);
                optimizedAopHandler.invokeMethodWithAspects(testService, TEST_METHOD_NO_PARAMS, NO_ARGS);
            } catch (Throwable e) {
                // Ignore exceptions during warmup
                e.printStackTrace();
            }
        }
        
        // Warm up parameter type caches
        for (int i = 0; i < 100; i++) {
            try {
                optimizedAopHandler.invokeMethodWithAspects(testService, TEST_METHOD, TEST_ARGS);
                optimizedAopHandler.invokeMethodWithAspects(testService, TEST_METHOD_NO_PARAMS, NO_ARGS);
            } catch (Throwable e) {
                // Ignore exceptions during warmup
                e.printStackTrace();
            }
        }
    }
    
    /**
     * Benchmark applyAopIfNeeded with O(1) aspects cache
     */
    @Benchmark
    public Object benchmarkApplyAopIfNeeded() {
        return optimizedAopHandler.applyAopIfNeeded(testService, testServiceClass);
    }
    
    /**
     * Benchmark applyAopToObject with O(1) aspects cache
     */
    @Benchmark
    public Object benchmarkApplyAopToObject() {
        return optimizedAopHandler.applyAopToObject(testService);
    }
    
    /**
     * Benchmark invokeMethodWithAspects with cached method resolution
     */
    @Benchmark
    public Object benchmarkInvokeMethodWithAspects() throws Throwable {
        return optimizedAopHandler.invokeMethodWithAspects(testService, TEST_METHOD, TEST_ARGS);
    }
    
    /**
     * Benchmark invokeMethodWithAspects with no parameters
     */
    @Benchmark
    public Object benchmarkInvokeMethodWithAspectsNoParams() throws Throwable {
        return optimizedAopHandler.invokeMethodWithAspects(testService, TEST_METHOD_NO_PARAMS, NO_ARGS);
    }
    
    /**
     * Benchmark findMethodReflection with O(1) method cache
     */
    @Benchmark
    public Object benchmarkFindMethodReflection() throws Throwable {
        Class<?>[] paramTypes = {String.class, Integer.class, Boolean.class};
        return optimizedAopHandler.getClass().getDeclaredMethod("findMethodReflection", 
            Class.class, String.class, Class[].class)
            .invoke(optimizedAopHandler, testServiceClass, TEST_METHOD, paramTypes);
    }
    
    /**
     * Benchmark getParameterTypesFromArgs with O(1) cache
     */
    @Benchmark
    public Class<?>[][] benchmarkGetParameterTypesFromArgs() throws Throwable {
        Class<?>[][] result = new Class[10][];
        for (int i = 0; i < 10; i++) {
            Object[] args = {"str" + i, i, i % 2 == 0};
            Class<?>[] types = (Class<?>[]) optimizedAopHandler.getClass()
                .getDeclaredMethod("getParameterTypesFromArgs", Object[].class)
                .invoke(optimizedAopHandler, (Object) args);
            result[i] = types;
        }
        return result;
    }
    
    /**
     * Benchmark matchesPointcut with atomic counter
     */
    @Benchmark
    public boolean benchmarkMatchesPointcut() throws Throwable {
        java.lang.reflect.Method method = testServiceClass.getDeclaredMethod("processData", String.class, Integer.class, Boolean.class);
        return (boolean) optimizedAopHandler.getClass()
            .getDeclaredMethod("matchesPointcut", java.lang.reflect.Method.class, String.class)
            .invoke(optimizedAopHandler, method, "execution(* TestService.*(..))");
    }
    
    /**
     * Benchmark registerAspects with cache updates
     */
    @Benchmark
    public void benchmarkRegisterAspects() {
        Method invokeMethod;
        try {
            invokeMethod = TestAspect.class.getMethod("invoke");
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
        
        AspectInfo aspectInfo = new AspectInfo(
            new TestAspect(),
            invokeMethod,
            "execution(* *(..))",
            Object.class,
            "testPointcut",
            "result",
            "error",
            1
        );
            
        optimizedAopHandler.registerAspects(TestAspect.class, new TestAspect());
    }
    
    /**
     * Benchmark getStatistics with O(1) atomic counters
     */
    @Benchmark
    public Object benchmarkGetStatistics() {
        return optimizedAopHandler.getAopHandlerStatistics();
    }
    
    /**
     * Benchmark getExtremeStartupMetrics
     */
    @Benchmark
    public Object benchmarkGetExtremeStartupMetrics() {
        return optimizedAopHandler.getExtremeStartupMetrics();
    }
    
    /**
     * Test utility methods for internal benchmarking
     */
    public static void main(String[] args) throws RunnerException {
        Options opt = new OptionsBuilder()
                .include(AopHandlerO1Benchmark.class.getSimpleName())
                .result("aop_handler_benchmark_results.json")
                .resultFormat(ResultFormatType.JSON)
                .build();

        new Runner(opt).run();
    }
    
    // Test classes for benchmarking
    public static class TestService {
        public String processData(String data, Integer id, Boolean active) {
            return "Processed: " + data + " (ID: " + id + ", Active: " + active + ")";
        }
        
        public String getStatus() {
            return "OK";
        }
        
        public void noReturn() {
            // Empty method for testing
        }
    }
    
    public static class TestAspect {
        public Object invoke() {
            return null;
        }
    }
}
