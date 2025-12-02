package io.warmup.framework.metrics;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Comprehensive tests for MetricsManager functionality.
 * Tests use the real MetricsManager API with recordMethodCall, getMethodStats, etc.
 * 
 * @author MiniMax Agent
 * @since 1.0
 */
class MetricsManagerTest {

    private MetricsManager metricsManager;
    private ExecutorService executorService;

    @BeforeEach
    void setUp() {
        // Use real constructor - MetricsManager has a no-args constructor
        metricsManager = new MetricsManager();
        executorService = Executors.newCachedThreadPool();
    }

    @AfterEach
    void tearDown() {
        if (executorService != null && !executorService.isShutdown()) {
            executorService.shutdown();
        }
        if (metricsManager != null) {
            metricsManager.resetMetrics();
        }
    }

    @Test
    void testMethodMetricsCollection() {
        // Test method execution metrics collection using real API
        String methodName = "testMethod";
        
        // Record some method executions using real API
        metricsManager.getMethodMetrics().recordMethodCall(methodName, 100, true);
        metricsManager.getMethodMetrics().recordMethodCall(methodName, 200, true);
        metricsManager.getMethodMetrics().recordMethodCall(methodName, 300, false); // One failed call
        
        // Get method metrics using real API
        MethodMetrics.MethodStats stats = metricsManager.getMethodMetrics().getMethodStats(methodName);
        
        assertNotNull(stats);
        assertEquals(3, stats.getCallCount());
        assertEquals(2, stats.getSuccessfulCalls());
        assertEquals(1, stats.getFailedCalls());
        assertTrue(stats.getMinTime() > 0);
        assertTrue(stats.getMaxTime() > 0);
        assertTrue(stats.getAverageTime() > 0);
    }

    @Test
    void testMultipleMethodMetrics() {
        // Test metrics for multiple methods
        String method1 = "method1";
        String method2 = "method2";
        
        metricsManager.getMethodMetrics().recordMethodCall(method1, 150, true);
        metricsManager.getMethodMetrics().recordMethodCall(method1, 250, true);
        
        metricsManager.getMethodMetrics().recordMethodCall(method2, 100, true);
        metricsManager.getMethodMetrics().recordMethodCall(method2, 200, true);
        metricsManager.getMethodMetrics().recordMethodCall(method2, 300, false);
        
        // Verify method1 stats
        MethodMetrics.MethodStats stats1 = metricsManager.getMethodMetrics().getMethodStats(method1);
        assertNotNull(stats1);
        assertEquals(2, stats1.getCallCount());
        assertEquals(2, stats1.getSuccessfulCalls());
        
        // Verify method2 stats
        MethodMetrics.MethodStats stats2 = metricsManager.getMethodMetrics().getMethodStats(method2);
        assertNotNull(stats2);
        assertEquals(3, stats2.getCallCount());
        assertEquals(2, stats2.getSuccessfulCalls());
        assertEquals(1, stats2.getFailedCalls());
    }

    @Test
    void testMetricsReset() {
        // Test metrics reset functionality
        String methodName = "testMethod";
        
        metricsManager.getMethodMetrics().recordMethodCall(methodName, 100, true);
        metricsManager.getMethodMetrics().recordMethodCall(methodName, 200, true);
        
        // Verify metrics exist
        MethodMetrics.MethodStats stats = metricsManager.getMethodMetrics().getMethodStats(methodName);
        assertNotNull(stats);
        assertEquals(2, stats.getCallCount());
        
        // Reset metrics
        metricsManager.resetMetrics();
        
        // Verify metrics are cleared
        MethodMetrics.MethodStats clearedStats = metricsManager.getMethodMetrics().getMethodStats(methodName);
        assertNull(clearedStats);
    }

    @Test
    void testGetAllMethodStats() {
        // Test getting all method statistics
        metricsManager.getMethodMetrics().recordMethodCall("method1", 100, true);
        metricsManager.getMethodMetrics().recordMethodCall("method2", 200, true);
        metricsManager.getMethodMetrics().recordMethodCall("method3", 300, true);
        
        java.util.concurrent.ConcurrentMap<String, io.warmup.framework.metrics.MethodMetrics.MethodStats> allStats = metricsManager.getMethodMetrics().getAllStats();
        
        assertNotNull(allStats);
        assertEquals(3, allStats.size());
        assertTrue(allStats.containsKey("method1"));
        assertTrue(allStats.containsKey("method2"));
        assertTrue(allStats.containsKey("method3"));
        
        // Verify each method has correct stats
        assertEquals(1, allStats.get("method1").getCallCount());
        assertEquals(1, allStats.get("method2").getCallCount());
        assertEquals(1, allStats.get("method3").getCallCount());
    }

    @Test
    void testDependencyResolutionMetrics() {
        // Test dependency resolution time tracking
        String className = "TestClass";
        Class<?> testClass = String.class;
        
        metricsManager.recordDependencyResolution(testClass, 150, true);
        metricsManager.recordDependencyResolution(testClass, 250, true);
        metricsManager.recordDependencyResolution(testClass, 100, false); // Failed resolution
        
        java.util.Map<Class<?>, Long> resolutionTimes = metricsManager.getDependencyResolutionTimes();
        
        assertNotNull(resolutionTimes);
        assertTrue(resolutionTimes.containsKey(testClass));
        assertTrue(resolutionTimes.get(testClass) > 0);
        
        // Test average resolution time
        double avgTime = metricsManager.getAverageResolutionTime(testClass);
        assertTrue(avgTime > 0);
        
        // Test resolution count
        long count = metricsManager.getResolutionCount(testClass);
        assertEquals(3, count);
    }

    @Test
    void testMetricsSnapshot() {
        // Test metrics snapshot generation
        metricsManager.getMethodMetrics().recordMethodCall("testMethod", 100, true);
        metricsManager.recordDependencyResolution(String.class, 50, true);
        
        java.util.Map<String, Object> snapshot = metricsManager.getMetricsSnapshot();
        
        assertNotNull(snapshot);
        assertFalse(snapshot.isEmpty());
        // Snapshot should contain various metrics
        assertTrue(snapshot.size() > 0);
    }

    @Test
    void testPrometheusMetrics() {
        // Test Prometheus metrics generation
        metricsManager.getMethodMetrics().recordMethodCall("prometheusTest", 100, true);
        
        String prometheusMetrics = metricsManager.getPrometheusMetrics();
        
        assertNotNull(prometheusMetrics);
        assertFalse(prometheusMetrics.isEmpty());
        // Should contain Prometheus format metrics
        assertTrue(prometheusMetrics.contains("# HELP") || prometheusMetrics.contains("# TYPE"));
    }

    @Test
    void testContainerMetrics() {
        // Test container metrics access
        ContainerMetrics containerMetrics = metricsManager.getContainerMetrics();
        
        assertNotNull(containerMetrics);
        // ContainerMetrics should be accessible
    }

    @Test
    void testCustomMetrics() {
        // Test custom metrics functionality
        String metricName = "customCounter";
        long value = 42;
        
        // Add custom metric
        java.util.Map<String, Object> customMetrics = metricsManager.getCustomMetrics();
        customMetrics.put(metricName, value);
        
        // Verify custom metric is stored
        assertEquals(value, customMetrics.get(metricName));
    }

    @Test
    @Timeout(5)
    void testConcurrentMetricsCollection() throws InterruptedException {
        // Test metrics collection from multiple threads
        int threadCount = 10;
        CountDownLatch latch = new CountDownLatch(threadCount);
        
        // Create threads that record metrics concurrently
        for (int i = 0; i < threadCount; i++) {
            final int threadIndex = i;
            executorService.submit(() -> {
                try {
                    String methodName = "concurrentMethod" + (threadIndex % 3); // 3 different methods
                    metricsManager.getMethodMetrics().recordMethodCall(methodName, 100 + threadIndex, true);
                    latch.countDown();
                } catch (Exception e) {
                    latch.countDown(); // Count down even on error to avoid hanging test
                }
            });
        }
        
        // Wait for all threads to complete
        assertTrue(latch.await(3, TimeUnit.SECONDS));
        
        // Verify that all metrics were recorded
        java.util.concurrent.ConcurrentMap<String, io.warmup.framework.metrics.MethodMetrics.MethodStats> allStats = metricsManager.getMethodMetrics().getAllStats();
        assertTrue(allStats.size() >= 3); // At least 3 different methods
        
        // Verify total call count
        long totalCalls = allStats.values().stream()
            .mapToLong(MethodMetrics.MethodStats::getCallCount)
            .sum();
        assertEquals(threadCount, totalCalls);
    }

    @Test
    void testMetricsInitialization() {
        // Test metrics manager initialization
        assertDoesNotThrow(() -> {
            metricsManager.initialize();
        });
        
        // Should be able to start metrics collection
        assertDoesNotThrow(() -> {
            metricsManager.startMetricsCollection();
        });
        
        // Should be able to stop metrics collection
        assertDoesNotThrow(() -> {
            metricsManager.stopMetricsCollection();
        });
    }

    @Test
    void testMetricsWarmup() {
        // Test metrics warmup functionality
        assertDoesNotThrow(() -> {
            metricsManager.warmupMetrics();
        });
    }

    @Test
    void testMetricsShutdown() {
        // Test metrics manager shutdown
        assertDoesNotThrow(() -> {
            metricsManager.shutdown();
        });
    }

    @Test
    void testMetricsReportPrinting() {
        // Test metrics report printing
        metricsManager.getMethodMetrics().recordMethodCall("reportTest", 100, true);
        
        // Should not throw exception
        assertDoesNotThrow(() -> {
            metricsManager.printMetricsReport();
        });
    }

    @Test
    void testAtomicCounterAccess() {
        // Test access to atomic counters (internal metrics)
        assertTrue(metricsManager.getTotalMetricQueries() >= 0);
        assertTrue(metricsManager.getSnapshotGenerations() >= 0);
        assertTrue(metricsManager.getCounterUpdates() >= 0);
        assertTrue(metricsManager.getTimerRecords() >= 0);
        assertTrue(metricsManager.getCustomMetricUpdates() >= 0);
    }

    @Test
    void testNonExistentMethodStats() {
        // Test getting stats for non-existent method
        MethodMetrics.MethodStats stats = metricsManager.getMethodMetrics().getMethodStats("nonExistentMethod");
        
        assertNull(stats);
    }

    @Test
    void testMethodStatsWithZeroCalls() {
        // Test MethodStats behavior with zero calls
        MethodMetrics.MethodStats stats = new MethodMetrics.MethodStats();
        
        assertEquals(0, stats.getCallCount());
        assertEquals(0, stats.getSuccessfulCalls());
        assertEquals(0, stats.getFailedCalls());
        assertEquals(0.0, stats.getAverageTime(), 0.001);
        // Min and max time might be Long.MAX_VALUE and 0 respectively for zero calls
    }

    @Test
    void testPerformanceMetrics() {
        // Test performance metrics for method calls
        String methodName = "performanceTest";
        
        // Record calls with various durations
        metricsManager.getMethodMetrics().recordMethodCall(methodName, 10, true);
        metricsManager.getMethodMetrics().recordMethodCall(methodName, 1000, true);
        metricsManager.getMethodMetrics().recordMethodCall(methodName, 100, true);
        
        MethodMetrics.MethodStats stats = metricsManager.getMethodMetrics().getMethodStats(methodName);
        
        assertNotNull(stats);
        assertEquals(3, stats.getCallCount());
        assertTrue(stats.getMinTime() <= 10);
        assertTrue(stats.getMaxTime() >= 1000);
        assertTrue(stats.getAverageTime() > 0);
    }
}