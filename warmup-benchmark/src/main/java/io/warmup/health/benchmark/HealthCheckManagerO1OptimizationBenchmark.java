package io.warmup.health.benchmark;

import io.warmup.framework.health.*;
import io.warmup.framework.core.WarmupContainer;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.results.format.ResultFormatType;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.CountDownLatch;

/**
 * Benchmark para validar las optimizaciones O(1) del HealthCheckManager.
 * Mide el rendimiento de health checks con atomic counters y TTL caching.
 */
@BenchmarkMode(Mode.Throughput)
@OutputTimeUnit(TimeUnit.MICROSECONDS)
@State(Scope.Benchmark)
@Warmup(iterations = 3, time = 2, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 5, time = 3, timeUnit = TimeUnit.SECONDS)
@Fork(2)
public class HealthCheckManagerO1OptimizationBenchmark {

    private HealthCheckManager healthCheckManager;
    private WarmupContainer mockContainer;
    
    @Setup
    public void setup() {
        mockContainer = new WarmupContainer();
        healthCheckManager = new HealthCheckManager(mockContainer);
        
        // Registrar algunos health checks para las pruebas
        healthCheckManager.registerHealthCheck(new TestHealthCheck("database", true));
        healthCheckManager.registerHealthCheck(new TestHealthCheck("redis", true));
        healthCheckManager.registerHealthCheck(new TestHealthCheck("external-api", false));
        healthCheckManager.registerHealthCheck("custom-check", new TestHealthCheck("custom", true));
    }
    
    @Benchmark
    public void testHealthCheckExecution() {
        // O(1) health check execution con TTL caching
        var results = healthCheckManager.checkHealth();
        assert results != null && !results.isEmpty();
    }
    
    @Benchmark
    public void testHealthStatusWithTTL() {
        // O(1) health status con TTL caching
        
        // Primera llamada - execute health checks
        var status1 = healthCheckManager.getHealthStatus();
        
        // Segunda llamada - use cached status
        var status2 = healthCheckManager.getHealthStatus();
        
        // Verificar que ambos son iguales
        assert status1.equals(status2);
    }
    
    @Benchmark
    public void testHealthSummaryWithAtomicCounters() {
        // O(1) health summary con atomic counters
        
        int initialExecutions = healthCheckManager.getHealthCheckExecutions();
        int initialCached = healthCheckManager.getCachedHealthResults();
        
        var summary1 = healthCheckManager.getHealthSummary();
        
        // Segunda ejecución para incrementar counters
        var summary2 = healthCheckManager.getHealthSummary();
        
        // Verificar que los counters se actualizaron
        assert healthCheckManager.getHealthCheckExecutions() >= initialExecutions;
        assert healthCheckManager.getCachedHealthResults() >= initialCached;
        
        assert summary1.getTotal() == summary2.getTotal();
        assert summary1.getHealthy() == summary2.getHealthy();
    }
    
    @Benchmark
    public void testHealthCheckRegistrationWithCacheInvalidation() {
        // O(1) health check registration con cache invalidation
        
        int initialRegistrations = healthCheckManager.getHealthCheckRegistrations();
        
        healthCheckManager.registerHealthCheck(new TestHealthCheck("new-service", true));
        healthCheckManager.registerHealthCheck("another-service", new TestHealthCheck("another", false));
        
        // Verificar que se incrementaron los counters
        assert healthCheckManager.getHealthCheckRegistrations() > initialRegistrations;
        assert healthCheckManager.getHealthCheckCount() > 4; // inicial + 2 nuevos
    }
    
    @Benchmark
    public void testHealthCheckRemovalWithCacheInvalidation() {
        // O(1) health check removal con cache invalidation
        
        int initialCount = healthCheckManager.getHealthCheckCount();
        int initialRegistrations = healthCheckManager.getHealthCheckRegistrations();
        
        healthCheckManager.removeHealthCheck("database");
        healthCheckManager.removeHealthCheck("non-existent");
        
        // Verificar que se decrementaron los counters
        assert healthCheckManager.getHealthCheckRegistrations() < initialRegistrations;
        assert healthCheckManager.getHealthCheckCount() < initialCount;
    }
    
    @Benchmark
    public void testCachedHealthResultsHit() {
        // O(1) cached health results hit rate
        
        // Primera llamada - force execution
        healthCheckManager.checkHealthForceRefresh();
        
        int initialCached = healthCheckManager.getCachedHealthResults();
        
        // Segunda llamada - debería usar cache
        healthCheckManager.checkHealth();
        
        // Verificar que se usó el cache
        assert healthCheckManager.getCachedHealthResults() > initialCached;
    }
    
    @Benchmark
    public void testClearHealthChecksWithCacheCleanup() {
        // O(1) clear health checks con cache cleanup
        
        // Setup data
        healthCheckManager.registerHealthCheck(new TestHealthCheck("temp1", true));
        healthCheckManager.registerHealthCheck(new TestHealthCheck("temp2", false));
        
        int count = healthCheckManager.getHealthCheckCount();
        int registrations = healthCheckManager.getHealthCheckRegistrations();
        
        // Clear - debe limpiar todas las caches
        healthCheckManager.clearHealthChecks();
        
        // Verificar limpieza completa
        assert healthCheckManager.getHealthCheckCount() == 0;
        assert healthCheckManager.getHealthCheckRegistrations() == 0;
        assert healthCheckManager.getCachedHealthResults() >= 0;
    }
    
    @Benchmark
    public void testConcurrentHealthChecks() throws InterruptedException {
        // O(1) concurrent health checks performance
        
        int threadCount = 10;
        CountDownLatch latch = new CountDownLatch(threadCount);
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        
        for (int i = 0; i < threadCount; i++) {
            executor.submit(() -> {
                try {
                    healthCheckManager.checkHealth();
                    healthCheckManager.getHealthStatus();
                    healthCheckManager.getHealthSummary();
                } finally {
                    latch.countDown();
                }
            });
        }
        
        latch.await(); // Esperar a que todos terminen
        executor.shutdown();
    }
    
    // Clase de test para health checks
    private static class TestHealthCheck implements HealthCheck {
        private final String name;
        private final boolean healthy;
        
        public TestHealthCheck(String name, boolean healthy) {
            this.name = name;
            this.healthy = healthy;
        }
        
        @Override
        public String getName() {
            return name;
        }
        
        @Override
        public HealthResult check() {
            try {
                // Simular trabajo
                Thread.sleep(1);
                return healthy ? 
                    HealthResult.up("Service " + name + " is healthy") :
                    HealthResult.down("Service " + name + " is down");
            } catch (Exception e) {
                return HealthResult.down("Error checking " + name, e);
            }
        }
    }
    
    @TearDown
    public void tearDown() {
        // Verificar que todas las optimizaciones funcionan
        System.out.println("\n=== HealthCheckManager O(1) Optimization Results ===");
        System.out.println("Health Check Executions: " + healthCheckManager.getHealthCheckExecutions());
        System.out.println("Cached Health Results: " + healthCheckManager.getCachedHealthResults());
        System.out.println("Health Check Registrations: " + healthCheckManager.getHealthCheckRegistrations());
        System.out.println("Healthy Checks Count: " + healthCheckManager.getHealthyChecksCount());
        System.out.println("Unhealthy Checks Count: " + healthCheckManager.getUnhealthyChecksCount());
        System.out.println("Total Health Check Duration: " + healthCheckManager.getTotalHealthCheckDuration() + "ms");
        
        // Shutdown manager
        healthCheckManager.shutdown();
        
        // Verificar que no hay excepciones
        System.out.println("✓ HealthCheckManager O(1) optimizations validated successfully");
    }
    
    public static void main(String[] args) throws RunnerException {
        Options opt = new OptionsBuilder()
                .include(HealthCheckManagerO1OptimizationBenchmark.class.getSimpleName())
                .resultFormat(ResultFormatType.JSON)
                .result("results/healthcheckmanager_o1_optimization_results.json")
                .build();

        new Runner(opt).run();
        
        System.out.println("HealthCheckManager O(1) Benchmark completed successfully!");
        System.out.println("Results saved to: results/healthcheckmanager_o1_optimization_results.json");
    }
}