package io.warmup.framework.startup.hotpath;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.AfterEach;
import static org.junit.jupiter.api.Assertions.*;

import java.util.*;
import java.util.concurrent.atomic.AtomicLong;
import java.time.Instant;

/**
 * Unit tests for ExecutionPathTracker class.
 * Tests method execution tracking, statistics collection, and hot path identification.
 * 
 * @author MiniMax Agent
 * @version 1.0
 */
class ExecutionPathTrackerTest {
    
    private ExecutionPathTracker tracker;
    private static final String TEST_CLASS = "TestClass";
    private static final String TEST_METHOD = "testMethod";
    private static final String TEST_SIGNATURE = "()V";
    
    @BeforeEach
    void setUp() {
        tracker = new ExecutionPathTracker(true);
    }
    
    @AfterEach
    void tearDown() {
        tracker.reset();
    }
    
    @Test
    void testConstructorActive() {
        ExecutionPathTracker activeTracker = new ExecutionPathTracker(true);
        assertTrue(activeTracker.isActive());
        activeTracker.reset();
    }
    
    @Test
    void testConstructorInactive() {
        ExecutionPathTracker inactiveTracker = new ExecutionPathTracker(false);
        assertFalse(inactiveTracker.isActive());
        inactiveTracker.reset();
    }
    
    @Test
    void testStartMethodTracking() {
        tracker.startMethodTracking(TEST_CLASS, TEST_METHOD, TEST_SIGNATURE);
        
        // Verify tracking started (basic check)
        ExecutionPathTracker.ExecutionStats stats = tracker.getMethodStats(TEST_CLASS, TEST_METHOD, TEST_SIGNATURE);
        assertNotNull(stats);
        assertTrue(stats.getCallCount() > 0);
    }
    
    @Test
    void testEndMethodTracking() {
        long startTime = System.nanoTime();
        
        tracker.startMethodTracking(TEST_CLASS, TEST_METHOD, TEST_SIGNATURE);
        
        // Simulate some execution time
        try {
            Thread.sleep(1); // 1ms
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return;
        }
        
        tracker.endMethodTracking(TEST_CLASS, TEST_METHOD, TEST_SIGNATURE, startTime);
        
        // Verify execution was recorded
        ExecutionPathTracker.ExecutionStats stats = tracker.getMethodStats(TEST_CLASS, TEST_METHOD, TEST_SIGNATURE);
        assertNotNull(stats);
        assertTrue(stats.getCallCount() > 0);
        assertTrue(stats.getTotalExecutionTime() > 0);
        assertTrue(stats.getAverageExecutionTime() > 0);
    }
    
    @Test
    void testMethodTrackerTryWithResources() {
        try (ExecutionPathTracker.MethodTracker methodTracker = 
             tracker.trackMethod(TEST_CLASS, TEST_METHOD, TEST_SIGNATURE)) {
            
            // Simulate method execution
            try {
                Thread.sleep(1);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            
        } // MethodTracker.close() called automatically
        
        // Verify execution was recorded
        ExecutionPathTracker.ExecutionStats stats = tracker.getMethodStats(TEST_CLASS, TEST_METHOD, TEST_SIGNATURE);
        assertNotNull(stats);
        assertEquals(1, stats.getCallCount());
        assertTrue(stats.getTotalExecutionTime() > 0);
    }
    
    @Test
    void testMultipleMethodExecutions() {
        int numExecutions = 5;
        
        for (int i = 0; i < numExecutions; i++) {
            try (ExecutionPathTracker.MethodTracker mt = 
                 tracker.trackMethod(TEST_CLASS, TEST_METHOD, TEST_SIGNATURE)) {
                
                try {
                    Thread.sleep(1);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }
        
        ExecutionPathTracker.ExecutionStats stats = tracker.getMethodStats(TEST_CLASS, TEST_METHOD, TEST_SIGNATURE);
        assertNotNull(stats);
        assertEquals(numExecutions, stats.getCallCount());
        assertTrue(stats.getTotalExecutionTime() > 0);
        assertTrue(stats.getAverageExecutionTime() > 0);
    }
    
    @Test
    void testGetMethodStatsNonExistent() {
        ExecutionPathTracker.ExecutionStats stats = tracker.getMethodStats("NonExistent", "method", "()V");
        assertNull(stats);
    }
    
    @Test
    void testGetClassStats() {
        // Add method executions to the class
        try (ExecutionPathTracker.MethodTracker mt = 
             tracker.trackMethod(TEST_CLASS, TEST_METHOD, TEST_SIGNATURE)) {
            // Simulate execution
        }
        
        ExecutionPathTracker.ExecutionStats classStats = tracker.getClassStats(TEST_CLASS);
        assertNotNull(classStats);
        assertTrue(classStats.getCallCount() > 0);
    }
    
    @Test
    void testGetClassStatsNonExistent() {
        ExecutionPathTracker.ExecutionStats stats = tracker.getClassStats("NonExistentClass");
        assertNull(stats);
    }
    
    @Test
    void testGetPackageStats() {
        // Add method execution
        try (ExecutionPathTracker.MethodTracker mt = 
             tracker.trackMethod(TEST_CLASS, TEST_METHOD, TEST_SIGNATURE)) {
            // Simulate execution
        }
        
        String packageName = "default"; // Since TEST_CLASS doesn't have a package
        ExecutionPathTracker.ExecutionStats packageStats = tracker.getPackageStats(packageName);
        assertNotNull(packageStats);
        assertTrue(packageStats.getCallCount() > 0);
    }
    
    @Test
    void testGetHotMethods() {
        // Add multiple method executions
        addSampleExecutions();
        
        List<Map.Entry<ExecutionPathTracker.MethodExecution, ExecutionPathTracker.ExecutionStats>> hotMethods = 
            tracker.getHotMethods(10);
        
        assertNotNull(hotMethods);
        assertTrue(hotMethods.size() > 0);
        
        for (Map.Entry<ExecutionPathTracker.MethodExecution, ExecutionPathTracker.ExecutionStats> entry : hotMethods) {
            assertNotNull(entry.getKey());
            assertNotNull(entry.getValue());
            assertNotNull(entry.getKey().getClassName());
            assertNotNull(entry.getKey().getMethodName());
            assertTrue(entry.getValue().getCallCount() >= 0);
        }
    }
    
    @Test
    void testGetHotClasses() {
        // Add method executions to different classes
        addSampleExecutions();
        
        List<Map.Entry<String, ExecutionPathTracker.ExecutionStats>> hotClasses = 
            tracker.getHotClasses(10);
        
        assertNotNull(hotClasses);
        
        for (Map.Entry<String, ExecutionPathTracker.ExecutionStats> entry : hotClasses) {
            assertNotNull(entry.getKey());
            assertNotNull(entry.getValue());
            assertTrue(entry.getValue().getCallCount() >= 0);
        }
    }
    
    @Test
    void testGetHotPackages() {
        // Add method executions
        addSampleExecutions();
        
        List<Map.Entry<String, ExecutionPathTracker.ExecutionStats>> hotPackages = 
            tracker.getHotPackages(10);
        
        assertNotNull(hotPackages);
        
        for (Map.Entry<String, ExecutionPathTracker.ExecutionStats> entry : hotPackages) {
            assertNotNull(entry.getKey());
            assertNotNull(entry.getValue());
            assertTrue(entry.getValue().getCallCount() >= 0);
        }
    }
    
    @Test
    void testGetRecentExecutions() {
        // Add some executions
        addSampleExecutions();
        
        List<ExecutionPathTracker.MethodExecution> recent = tracker.getRecentExecutions(5);
        assertNotNull(recent);
        assertTrue(recent.size() <= 5);
        
        for (ExecutionPathTracker.MethodExecution execution : recent) {
            assertNotNull(execution.getClassName());
            assertNotNull(execution.getMethodName());
            assertNotNull(execution.getTimestamp());
            assertTrue(execution.getThreadId() > 0);
        }
    }
    
    @Test
    void testGetRecentExecutionsWithLimit() {
        // Add more executions than limit
        addSampleExecutions();
        
        List<ExecutionPathTracker.MethodExecution> recent = tracker.getRecentExecutions(2);
        assertNotNull(recent);
        assertTrue(recent.size() <= 2);
    }
    
    @Test
    void testMethodExecutionProperties() {
        Instant timestamp = Instant.now();
        long threadId = 1L;
        StackTraceElement[] stackTrace = new StackTraceElement[0];
        
        ExecutionPathTracker.MethodExecution execution = new ExecutionPathTracker.MethodExecution(
            TEST_CLASS, TEST_METHOD, TEST_SIGNATURE, timestamp, threadId, stackTrace
        );
        
        assertEquals(TEST_CLASS, execution.getClassName());
        assertEquals(TEST_METHOD, execution.getMethodName());
        assertEquals(TEST_SIGNATURE, execution.getMethodSignature());
        assertEquals(timestamp, execution.getTimestamp());
        assertEquals(threadId, execution.getThreadId());
        assertEquals(stackTrace, execution.getCallStack());
        assertEquals(TEST_CLASS + "." + TEST_METHOD, execution.getFullMethodName());
        
        // Test metadata
        assertNotNull(execution.getMetadata());
        assertTrue(execution.getMetadata().isEmpty());
        
        execution.getMetadata().put("test", "value");
        assertEquals("value", execution.getMetadata().get("test"));
    }
    
    @Test
    void testMethodExecutionEqualsAndHashCode() {
        Instant timestamp = Instant.now();
        StackTraceElement[] stackTrace = new StackTraceElement[0];
        
        ExecutionPathTracker.MethodExecution execution1 = new ExecutionPathTracker.MethodExecution(
            TEST_CLASS, TEST_METHOD, TEST_SIGNATURE, timestamp, 1L, stackTrace
        );
        
        ExecutionPathTracker.MethodExecution execution2 = new ExecutionPathTracker.MethodExecution(
            TEST_CLASS, TEST_METHOD, TEST_SIGNATURE, timestamp, 2L, stackTrace
        );
        
        ExecutionPathTracker.MethodExecution execution3 = new ExecutionPathTracker.MethodExecution(
            "OtherClass", TEST_METHOD, TEST_SIGNATURE, timestamp, 1L, stackTrace
        );
        
        // Same signature should be equal regardless of thread ID or timestamp
        assertEquals(execution1, execution2);
        assertEquals(execution2, execution1);
        assertEquals(execution1.hashCode(), execution2.hashCode());
        
        // Different class should not be equal
        assertNotEquals(execution1, execution3);
        assertNotEquals(execution1.hashCode(), execution3.hashCode());
    }
    
    @Test
    void testMethodExecutionToString() {
        Instant timestamp = Instant.now();
        StackTraceElement[] stackTrace = new StackTraceElement[0];
        
        ExecutionPathTracker.MethodExecution execution = new ExecutionPathTracker.MethodExecution(
            TEST_CLASS, TEST_METHOD, TEST_SIGNATURE, timestamp, 1L, stackTrace
        );
        
        String result = execution.toString();
        assertTrue(result.contains("MethodExecution"));
        assertTrue(result.contains(TEST_CLASS));
        assertTrue(result.contains(TEST_METHOD));
    }
    
    @Test
    void testExecutionStatsProperties() {
        ExecutionPathTracker.ExecutionStats stats = new ExecutionPathTracker.ExecutionStats();
        
        assertEquals(0, stats.getCallCount());
        assertEquals(0, stats.getTotalExecutionTime());
        assertEquals(0, stats.getMinExecutionTime());
        assertEquals(0, stats.getMaxExecutionTime());
        assertEquals(0, stats.getThreadCount());
        assertEquals(0.0, stats.getAverageExecutionTime(), 0.01);
        assertEquals(0.0, stats.getConsistencyScore(), 0.01);
        assertEquals(Collections.emptyList(), stats.getExecutionTimes());
        assertEquals(Collections.emptyList(), stats.getDependencies());
    }
    
    @Test
    void testExecutionStatsRecordExecution() {
        ExecutionPathTracker.ExecutionStats stats = new ExecutionPathTracker.ExecutionStats();
        long threadId = 1L;
        long executionTime = 1000000L; // 1ms in nanoseconds
        
        stats.recordExecution(executionTime, threadId);
        
        assertEquals(1, stats.getCallCount());
        assertEquals(executionTime, stats.getTotalExecutionTime());
        assertEquals(executionTime, stats.getMinExecutionTime());
        assertEquals(executionTime, stats.getMaxExecutionTime());
        assertEquals(1, stats.getThreadCount());
        assertEquals(executionTime, stats.getAverageExecutionTime(), 0.01);
    }
    
    @Test
    void testExecutionStatsMultipleExecutions() {
        ExecutionPathTracker.ExecutionStats stats = new ExecutionPathTracker.ExecutionStats();
        
        // Add multiple executions with different times
        stats.recordExecution(1000000L, 1L); // 1ms
        stats.recordExecution(2000000L, 1L); // 2ms
        stats.recordExecution(1500000L, 2L); // 1.5ms
        
        assertEquals(3, stats.getCallCount());
        assertEquals(4500000L, stats.getTotalExecutionTime());
        assertEquals(1000000L, stats.getMinExecutionTime());
        assertEquals(2000000L, stats.getMaxExecutionTime());
        assertEquals(2, stats.getThreadCount());
        assertEquals(1500000.0, stats.getAverageExecutionTime(), 0.01);
    }
    
    @Test
    void testExecutionStatsConsistencyScore() {
        ExecutionPathTracker.ExecutionStats stats = new ExecutionPathTracker.ExecutionStats();
        
        // Add consistent executions (same time)
        for (int i = 0; i < 5; i++) {
            stats.recordExecution(1000000L, 1L); // 1ms each
        }
        
        // With consistent execution times, consistency score should be high
        assertTrue(stats.getConsistencyScore() > 0.8);
    }
    
    @Test
    void testExecutionStatsHotnessLevel() {
        ExecutionPathTracker.ExecutionStats stats = new ExecutionPathTracker.ExecutionStats();
        
        // Test cold level (no calls)
        assertEquals(ExecutionPathTracker.HotnessLevel.COLD, stats.getHotnessLevel());
        
        // Add calls that should make it warm/hot
        for (int i = 0; i < 15; i++) {
            stats.recordExecution(60000L, 1L); // 60µs, 15 calls = WARM
        }
        assertTrue(stats.getHotnessLevel().getScore() >= ExecutionPathTracker.HotnessLevel.WARM.getScore());
        
        // Add more calls to make it hot
        for (int i = 0; i < 10; i++) {
            stats.recordExecution(150000L, 1L); // 150µs, additional calls = HOT
        }
        assertTrue(stats.getHotnessLevel().getScore() >= ExecutionPathTracker.HotnessLevel.HOT.getScore());
    }
    
    @Test
    void testExecutionStatsIsHotPath() {
        ExecutionPathTracker.ExecutionStats stats = new ExecutionPathTracker.ExecutionStats();
        
        // Not a hot path yet
        assertFalse(stats.isHotPath());
        
        // Add calls but not enough time
        for (int i = 0; i < 15; i++) {
            stats.recordExecution(50000L, 1L); // 50µs (below threshold)
        }
        assertFalse(stats.isHotPath());
        
        // Add calls with sufficient time
        for (int i = 0; i < 5; i++) {
            stats.recordExecution(200000L, 1L); // 200µs (above threshold)
        }
        assertTrue(stats.isHotPath());
    }
    
    @Test
    void testExecutionStatsStandardDeviation() {
        ExecutionPathTracker.ExecutionStats stats = new ExecutionPathTracker.ExecutionStats();
        
        // Add consistent executions
        for (int i = 0; i < 5; i++) {
            stats.recordExecution(1000000L, 1L); // 1ms each
        }
        
        double stdDev = stats.getStandardDeviation();
        assertTrue(stdDev >= 0.0);
        
        // Add inconsistent executions
        ExecutionPathTracker.ExecutionStats inconsistentStats = new ExecutionPathTracker.ExecutionStats();
        inconsistentStats.recordExecution(1000000L, 1L);
        inconsistentStats.recordExecution(2000000L, 1L);
        inconsistentStats.recordExecution(500000L, 1L);
        
        double inconsistentStdDev = inconsistentStats.getStandardDeviation();
        assertTrue(inconsistentStdDev > stdDev);
    }
    
    @Test
    void testExecutionStatsToString() {
        ExecutionPathTracker.ExecutionStats stats = new ExecutionPathTracker.ExecutionStats();
        stats.recordExecution(1500000L, 1L); // 1.5ms
        
        String result = stats.toString();
        assertTrue(result.contains("ExecutionStats"));
        assertTrue(result.contains("calls="));
        assertTrue(result.contains("avgTime="));
        assertTrue(result.contains("hotness="));
        assertTrue(result.contains("threads="));
    }
    
    @Test
    void testHotnessLevelEnum() {
        assertEquals(100, ExecutionPathTracker.HotnessLevel.EXTREMELY_HOT.getScore());
        assertEquals(75, ExecutionPathTracker.HotnessLevel.VERY_HOT.getScore());
        assertEquals(50, ExecutionPathTracker.HotnessLevel.HOT.getScore());
        assertEquals(25, ExecutionPathTracker.HotnessLevel.WARM.getScore());
        assertEquals(0, ExecutionPathTracker.HotnessLevel.COLD.getScore());
    }
    
    @Test
    void testTrackingStatistics() {
        // Add some executions
        addSampleExecutions();
        
        assertTrue(tracker.getTotalTrackingTime() >= 0);
        assertTrue(tracker.getTrackingDuration() >= 0);
        assertTrue(tracker.getTrackedMethodCount() > 0);
        assertTrue(tracker.getTrackedClassCount() > 0);
        assertTrue(tracker.getTrackedPackageCount() > 0);
        assertTrue(tracker.isActive());
    }
    
    @Test
    void testAddMethodDependency() {
        // Add a method execution first
        try (ExecutionPathTracker.MethodTracker mt = 
             tracker.trackMethod("ClassA", "methodA", "()V")) {
            // Simulate execution
        }
        
        // Add dependency
        tracker.addMethodDependency("ClassA", "methodA", "ClassB", "methodB");
        
        ExecutionPathTracker.ExecutionStats stats = tracker.getMethodStats("ClassA", "methodA", "()V");
        assertNotNull(stats);
        assertTrue(stats.getDependencies().contains("ClassB.methodB"));
    }
    
    @Test
    void testReset() {
        // Add executions
        addSampleExecutions();
        
        // Verify we have data
        assertTrue(tracker.getTrackedMethodCount() > 0);
        assertTrue(tracker.getTotalTrackingTime() >= 0);
        
        // Reset
        tracker.reset();
        
        // Verify data is cleared
        assertEquals(0, tracker.getTrackedMethodCount());
        assertEquals(0, tracker.getTrackedClassCount());
        assertEquals(0, tracker.getTrackedPackageCount());
        assertEquals(0, tracker.getTotalTrackingTime());
        assertTrue(tracker.getRecentExecutions(10).isEmpty());
    }
    
    @Test
    void testGenerateAnalysisReport() {
        // Add some executions
        addSampleExecutions();
        
        String report = tracker.generateAnalysisReport();
        
        assertNotNull(report);
        assertTrue(report.contains("Execution Path Analysis Report"));
        assertTrue(report.contains("Tracking Duration"));
        assertTrue(report.contains("Tracked Methods"));
        assertTrue(report.contains("Top 10 Hot Methods"));
        assertTrue(report.contains("Top 10 Hot Classes"));
        assertTrue(report.contains("Package Analysis"));
    }
    
    @Test
    void testInactiveTrackerBehavior() {
        ExecutionPathTracker inactiveTracker = new ExecutionPathTracker(false);
        
        // Tracking should be no-ops when inactive
        inactiveTracker.startMethodTracking(TEST_CLASS, TEST_METHOD, TEST_SIGNATURE);
        inactiveTracker.endMethodTracking(TEST_CLASS, TEST_METHOD, TEST_SIGNATURE, System.nanoTime());
        
        // Should not track anything
        ExecutionPathTracker.ExecutionStats stats = inactiveTracker.getMethodStats(TEST_CLASS, TEST_METHOD, TEST_SIGNATURE);
        assertNull(stats);
        
        // Hot methods should be empty
        List<Map.Entry<ExecutionPathTracker.MethodExecution, ExecutionPathTracker.ExecutionStats>> hotMethods = 
            inactiveTracker.getHotMethods(10);
        assertTrue(hotMethods.isEmpty());
    }
    
    // Helper methods
    
    private void addSampleExecutions() {
        String[] classes = {"ClassA", "ClassB", "ClassC"};
        String[] methods = {"method1", "method2", "method3"};
        
        for (String className : classes) {
            for (String methodName : methods) {
                for (int i = 0; i < 3; i++) {
                    try (ExecutionPathTracker.MethodTracker mt = 
                         tracker.trackMethod(className, methodName, "()V")) {
                        
                        try {
                            Thread.sleep(1);
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                        }
                    }
                }
            }
        }
    }
}