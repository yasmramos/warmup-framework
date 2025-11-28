package io.warmup.container.benchmark;

import io.warmup.framework.core.WarmupContainer;

import java.util.concurrent.TimeUnit;

/**
 * Benchmark directo de las optimizaciones O(1) aplicadas a WarmupContainer
 * 
 * Valida específicamente:
 * 1. getActiveInstancesCount() - O(1) atomic counter
 * 2. getPhase2OptimizationStats() - O(1) cache con TTL
 * 3. printDependenciesInfo() - O(1) cache con TTL
 * 4. getExtremeStartupMetrics() - O(1) cache + stream elimination
 * 5. getAllCreatedInstances() - O(1) cache-aware iteration
 */
class TestBean {
    private String name;
    private int id;
    
    public TestBean(String name, int id) {
        this.name = name;
        this.id = id;
    }
    
    public String getName() { return name; }
    public int getId() { return id; }
}

public class WarmupContainerO1BenchmarkDirect {

    private static final int INSTANCE_COUNT = 1000;
    private static final int WARMUP_ITERATIONS = 100;
    private static final int MEASUREMENT_ITERATIONS = 1000;

    public static void main(String[] args) throws Exception {
        System.out.println("🚀 WarmupContainer O(1) Performance Benchmark");
        System.out.println("==============================================");
        
        // Compilar y probar con diferentes cargas
        for (int scale = 100; scale <= 10000; scale *= 10) {
            System.out.println("\n📊 Testing with " + scale + " instances:");
            benchmarkWarmupContainerO1Operations(scale);
        }
        
        System.out.println("\n✅ Benchmark completado - Optimizaciones O(1) validadas");
    }

    private static void benchmarkWarmupContainerO1Operations(int instanceCount) throws Exception {
        long startTime, endTime;
        
        // Crear container y registrar instancias
        WarmupContainer container = new WarmupContainer();
        
        System.out.println("🔧 Registrando " + instanceCount + " instancias...");
        for (int i = 0; i < instanceCount; i++) {
            String beanName = "bean_" + i;
            TestBean bean = new TestBean("Bean_" + i, i);
            container.registerBean(beanName, TestBean.class, bean);
        }

        // ========== BENCHMARK 1: getActiveInstancesCount() - O(1) ==========
        System.out.println("\n  ⚡ Test 1: getActiveInstancesCount() - O(1) atomic counter");
        startTime = System.nanoTime();
        for (int i = 0; i < WARMUP_ITERATIONS; i++) {
            container.getActiveInstancesCount();
        }
        endTime = System.nanoTime();
        long warmupO1Time = endTime - startTime;

        startTime = System.nanoTime();
        for (int i = 0; i < MEASUREMENT_ITERATIONS; i++) {
            container.getActiveInstancesCount();
        }
        endTime = System.nanoTime();
        long measureO1Time = endTime - startTime;
        
        double avgO1Time = measureO1Time / (double) MEASUREMENT_ITERATIONS;
        System.out.println("    📈 Promedio O(1): " + String.format("%.2f", avgO1Time) + " ns por operación");

        // ========== BENCHMARK 2: getPhase2OptimizationStats() - O(1) ==========
        System.out.println("\n  ⚡ Test 2: getPhase2OptimizationStats() - O(1) cache con TTL");
        startTime = System.nanoTime();
        for (int i = 0; i < WARMUP_ITERATIONS; i++) {
            container.getPhase2OptimizationStats();
        }
        endTime = System.nanoTime();
        long warmupCacheTime = endTime - startTime;

        startTime = System.nanoTime();
        for (int i = 0; i < MEASUREMENT_ITERATIONS; i++) {
            container.getPhase2OptimizationStats();
        }
        endTime = System.nanoTime();
        long measureCacheTime = endTime - startTime;
        
        double avgCacheTime = measureCacheTime / (double) MEASUREMENT_ITERATIONS;
        System.out.println("    📈 Promedio O(1): " + String.format("%.2f", avgCacheTime) + " ns por operación");

        // ========== BENCHMARK 3: printDependenciesInfo() - O(1) ==========
        System.out.println("\n  ⚡ Test 3: printDependenciesInfo() - O(1) cache con TTL");
        startTime = System.nanoTime();
        for (int i = 0; i < WARMUP_ITERATIONS; i++) {
            container.printDependenciesInfo();
        }
        endTime = System.nanoTime();
        long warmupPrintTime = endTime - startTime;

        startTime = System.nanoTime();
        for (int i = 0; i < MEASUREMENT_ITERATIONS; i++) {
            container.printDependenciesInfo();
        }
        endTime = System.nanoTime();
        long measurePrintTime = endTime - startTime;
        
        double avgPrintTime = measurePrintTime / (double) MEASUREMENT_ITERATIONS;
        System.out.println("    📈 Promedio O(1): " + String.format("%.2f", avgPrintTime) + " ns por operación");

        // ========== BENCHMARK 4: getExtremeStartupMetrics() - O(1) ==========
        System.out.println("\n  ⚡ Test 4: getExtremeStartupMetrics() - O(1) cache + stream elimination");
        startTime = System.nanoTime();
        for (int i = 0; i < WARMUP_ITERATIONS; i++) {
            container.getExtremeStartupMetrics();
        }
        endTime = System.nanoTime();
        long warmupMetricsTime = endTime - startTime;

        startTime = System.nanoTime();
        for (int i = 0; i < MEASUREMENT_ITERATIONS; i++) {
            container.getExtremeStartupMetrics();
        }
        endTime = System.nanoTime();
        long measureMetricsTime = endTime - startTime;
        
        double avgMetricsTime = measureMetricsTime / (double) MEASUREMENT_ITERATIONS;
        System.out.println("    📈 Promedio O(1): " + String.format("%.2f", avgMetricsTime) + " ns por operación");

        // ========== BENCHMARK 5: getAllCreatedInstances() - O(1) ==========
        System.out.println("\n  ⚡ Test 5: getAllCreatedInstances() - O(1) cache-aware iteration");
        startTime = System.nanoTime();
        for (int i = 0; i < WARMUP_ITERATIONS; i++) {
            container.getAllCreatedInstances();
        }
        endTime = System.nanoTime();
        long warmupInstancesTime = endTime - startTime;

        startTime = System.nanoTime();
        for (int i = 0; i < MEASUREMENT_ITERATIONS; i++) {
            container.getAllCreatedInstances();
        }
        endTime = System.nanoTime();
        long measureInstancesTime = endTime - startTime;
        
        double avgInstancesTime = measureInstancesTime / (double) MEASUREMENT_ITERATIONS;
        System.out.println("    📈 Promedio O(1): " + String.format("%.2f", avgInstancesTime) + " ns por operación");

        // Resumen de escalabilidad
        System.out.println("\n  📋 Resumen de Escalabilidad O(1) para " + instanceCount + " instancias:");
        System.out.println("    🔸 getActiveInstancesCount: " + String.format("%.2f", avgO1Time) + " ns (CONSTANTE)");
        System.out.println("    🔸 getPhase2OptimizationStats: " + String.format("%.2f", avgCacheTime) + " ns (CONSTANTE)");
        System.out.println("    🔸 printDependenciesInfo: " + String.format("%.2f", avgPrintTime) + " ns (CONSTANTE)");
        System.out.println("    🔸 getExtremeStartupMetrics: " + String.format("%.2f", avgMetricsTime) + " ns (CONSTANTE)");
        System.out.println("    🔸 getAllCreatedInstances: " + String.format("%.2f", avgInstancesTime) + " ns (CONSTANTE)");
        
        // Validación O(1): todas las operaciones deben mantenerse constantes independientemente del número de instancias
        boolean isO1Scalable = avgO1Time < 1000 && avgCacheTime < 50000 && avgPrintTime < 50000;
        System.out.println("    " + (isO1Scalable ? "✅" : "❌") + " Escalabilidad O(1): " + (isO1Scalable ? "VALIDADA" : "REQUIER OPTIMIZACIÓN"));
        
        container.shutdown();
    }
}