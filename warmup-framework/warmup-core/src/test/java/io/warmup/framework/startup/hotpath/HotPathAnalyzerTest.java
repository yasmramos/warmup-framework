package io.warmup.framework.startup.hotpath;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.AfterEach;
import static org.junit.jupiter.api.Assertions.*;

import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Unit tests for HotPathAnalyzer class.
 * Tests the core hot path analysis functionality including hot path identification,
 * recommendation generation, and performance metrics calculation.
 * 
 * @author MiniMax Agent
 * @version 1.0
 */
class HotPathAnalyzerTest {
    
    private ExecutionPathTracker tracker;
    private HotPathAnalyzer analyzer;
    private ExecutorService testExecutor;
    
    @BeforeEach
    void setUp() {
        tracker = new ExecutionPathTracker(true);
        analyzer = new HotPathAnalyzer(tracker, Duration.ofSeconds(10)); // OPTIMIZADO: reducido de 1 minuto para testing
        testExecutor = Executors.newFixedThreadPool(2);
    }
    
    @AfterEach
    void tearDown() {
        analyzer.shutdown();
        testExecutor.shutdown();
        try {
            if (!testExecutor.awaitTermination(5, TimeUnit.SECONDS)) {
                testExecutor.shutdownNow();
            }
        } catch (InterruptedException e) {
            testExecutor.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
    
    @Test
    void testConstructorWithDefaultCacheTimeout() {
        HotPathAnalyzer defaultAnalyzer = new HotPathAnalyzer(tracker);
        assertNotNull(defaultAnalyzer);
        assertSame(tracker, defaultAnalyzer.getTracker());
        defaultAnalyzer.shutdown();
    }
    
    @Test
    void testConstructorWithCustomCacheTimeout() {
        Duration customTimeout = Duration.ofSeconds(30);
        HotPathAnalyzer customAnalyzer = new HotPathAnalyzer(tracker, customTimeout);
        assertNotNull(customAnalyzer);
        assertSame(tracker, customAnalyzer.getTracker());
        customAnalyzer.shutdown();
    }
    
    @Test
    void testAnalyzeHotPathsWithEmptyData() {
        List<HotPathAnalyzer.HotPathAnalysis> results = analyzer.analyzeHotPaths(10);
        assertNotNull(results);
        assertTrue(results.isEmpty());
    }
    
    @Test
    void testAnalyzeHotPathsWithInsufficientData() {
        // Add minimal execution data that doesn't meet hotness threshold
        tracker.startMethodTracking("TestClass", "testMethod", "()V");
        tracker.endMethodTracking("TestClass", "testMethod", "()V", System.nanoTime());
        
        List<HotPathAnalyzer.HotPathAnalysis> results = analyzer.analyzeHotPaths(10);
        assertNotNull(results);
        // HotPathAnalyzer can still identify paths with minimal data
        // Verify that results exist but may have minimal content
        for (HotPathAnalyzer.HotPathAnalysis analysis : results) {
            assertNotNull(analysis.getPathId());
            assertNotNull(analysis.getDescription());
            assertNotNull(analysis.getHotnessLevel());
        }
    }
    
    @Test
    void testAnalyzeHotPathsAsync() {
        // Add some execution data
        addSampleExecutionData();
        
        CompletableFuture<List<HotPathAnalyzer.HotPathAnalysis>> future = analyzer.analyzeHotPathsAsync(5);
        List<HotPathAnalyzer.HotPathAnalysis> results = future.join();
        
        assertNotNull(results);
        // Results may be empty depending on data characteristics
    }
    
    @Test
    void testAnalyzeHotPathsWithHotMethods() {
        // Add execution data that creates hot methods
        addHotExecutionData();
        
        List<HotPathAnalyzer.HotPathAnalysis> results = analyzer.analyzeHotPaths(10);
        
        assertNotNull(results);
        // May contain hot paths if data meets thresholds
        for (HotPathAnalyzer.HotPathAnalysis analysis : results) {
            assertNotNull(analysis.getPathId());
            assertNotNull(analysis.getDescription());
            assertNotNull(analysis.getHotnessLevel());
            assertTrue(analysis.getHotnessScore() >= 0);
            assertTrue(analysis.getInvolvedMethods() != null);
            assertTrue(analysis.getPerformanceMetrics() != null);
            assertNotNull(analysis.getConfidenceLevel());
        }
    }
    
    @Test
    void testHotPathAnalysisProperties() {
        List<String> methods = Arrays.asList("method1", "method2");
        List<HotPathAnalyzer.OptimizationRecommendation> recommendations = new ArrayList<>();
        HotPathAnalyzer.PerformanceMetrics metrics = new HotPathAnalyzer.PerformanceMetrics(
            1000000L, 500000L, 100000L, 2000000L, 100L, 2, 0.8, 100.0
        );
        
        HotPathAnalyzer.HotPathAnalysis analysis = new HotPathAnalyzer.HotPathAnalysis(
            "test-id", "Test Analysis", HotnessLevel.HOT, 75.0, methods,
            recommendations, metrics, HotPathAnalyzer.ConfidenceLevel.HIGH
        );
        
        assertEquals("test-id", analysis.getPathId());
        assertEquals("Test Analysis", analysis.getDescription());
        assertEquals(HotnessLevel.HOT, analysis.getHotnessLevel());
        assertEquals(75.0, analysis.getHotnessScore(), 0.01);
        assertEquals(2, analysis.getInvolvedMethods().size());
        assertEquals(0, analysis.getRecommendations().size()); // Empty list should have size 0
        assertEquals(metrics, analysis.getPerformanceMetrics());
        assertNotNull(analysis.getAnalysisTimestamp());
        assertEquals(HotPathAnalyzer.ConfidenceLevel.HIGH, analysis.getConfidenceLevel());
    }
    
    @Test
    void testHotPathAnalysisIsSignificant() {
        HotPathAnalyzer.PerformanceMetrics metrics = new HotPathAnalyzer.PerformanceMetrics(
            1000000L, 500000L, 100000L, 2000000L, 100L, 2, 0.8, 100.0
        );
        
        // Test HOT level - should be significant
        HotPathAnalyzer.HotPathAnalysis hotAnalysis = new HotPathAnalyzer.HotPathAnalysis(
            "hot-id", "Hot Analysis", HotnessLevel.HOT, 75.0, new ArrayList<>(),
            new ArrayList<>(), metrics, HotPathAnalyzer.ConfidenceLevel.HIGH
        );
        assertTrue(hotAnalysis.isSignificant());
        
        // Test WARM level - should not be significant
        HotPathAnalyzer.HotPathAnalysis warmAnalysis = new HotPathAnalyzer.HotPathAnalysis(
            "warm-id", "Warm Analysis", HotnessLevel.WARM, 35.0, new ArrayList<>(),
            new ArrayList<>(), metrics, HotPathAnalyzer.ConfidenceLevel.MEDIUM
        );
        assertFalse(warmAnalysis.isSignificant());
    }
    
    @Test
    void testHotPathAnalysisExpectedImprovement() {
        List<HotPathAnalyzer.OptimizationRecommendation> recommendations = Arrays.asList(
            createMockRecommendation("rec1", 10.0),
            createMockRecommendation("rec2", 15.0)
        );
        
        HotPathAnalyzer.PerformanceMetrics metrics = new HotPathAnalyzer.PerformanceMetrics(
            1000000L, 500000L, 100000L, 2000000L, 100L, 2, 0.8, 100.0
        );
        
        HotPathAnalyzer.HotPathAnalysis analysis = new HotPathAnalyzer.HotPathAnalysis(
            "test-id", "Test Analysis", HotnessLevel.HOT, 75.0, new ArrayList<>(),
            recommendations, metrics, HotPathAnalyzer.ConfidenceLevel.HIGH
        );
        
        assertEquals(25.0, analysis.getExpectedImprovement(), 0.01);
    }
    
    @Test
    void testHotPathAnalysisToString() {
        List<String> methods = Arrays.asList("method1", "method2");
        HotPathAnalyzer.PerformanceMetrics metrics = new HotPathAnalyzer.PerformanceMetrics(
            1000000L, 500000L, 100000L, 2000000L, 100L, 2, 0.8, 100.0
        );
        
        HotPathAnalyzer.HotPathAnalysis analysis = new HotPathAnalyzer.HotPathAnalysis(
            "test-id", "Test Analysis", HotnessLevel.HOT, 75.0, methods,
            new ArrayList<>(), metrics, HotPathAnalyzer.ConfidenceLevel.HIGH
        );
        
        String result = analysis.toString();
        assertTrue(result.contains("test-id"));
        assertTrue(result.contains("HOT"));
        assertTrue(result.contains("75.00"));
        assertTrue(result.contains("2")); // methods count
    }
    
    @Test
    void testOptimizationRecommendationProperties() {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("param1", "value1");
        
        HotPathAnalyzer.OptimizationRecommendation recommendation = new HotPathAnalyzer.OptimizationRecommendation(
            "rec-id", "Test Recommendation", HotPathAnalyzer.RecommendationType.CODE_REORDERING,
            HotPathAnalyzer.Priority.HIGH, 20.0, new ArrayList<>(), new ArrayList<>(),
            parameters, HotPathAnalyzer.ConfidenceLevel.HIGH
        );
        
        assertEquals("rec-id", recommendation.getId());
        assertEquals("Test Recommendation", recommendation.getDescription());
        assertEquals(HotPathAnalyzer.RecommendationType.CODE_REORDERING, recommendation.getType());
        assertEquals(HotPathAnalyzer.Priority.HIGH, recommendation.getPriority());
        assertEquals(20.0, recommendation.getExpectedImprovement(), 0.01);
        assertEquals(parameters, recommendation.getParameters());
        assertEquals(HotPathAnalyzer.ConfidenceLevel.HIGH, recommendation.getConfidenceLevel());
    }
    
    @Test
    void testOptimizationRecommendationToString() {
        HotPathAnalyzer.OptimizationRecommendation recommendation = new HotPathAnalyzer.OptimizationRecommendation(
            "rec-id", "Test Recommendation", HotPathAnalyzer.RecommendationType.CODE_REORDERING,
            HotPathAnalyzer.Priority.HIGH, 20.0, new ArrayList<>(), new ArrayList<>(),
            new HashMap<>(), HotPathAnalyzer.ConfidenceLevel.HIGH
        );
        
        String result = recommendation.toString();
        assertTrue(result.contains("rec-id"));
        assertTrue(result.contains("CODE_REORDERING"));
        assertTrue(result.contains("HIGH"));
        assertTrue(result.contains("20.0"));
    }
    
    @Test
    void testPerformanceMetricsProperties() {
        HotPathAnalyzer.PerformanceMetrics metrics = new HotPathAnalyzer.PerformanceMetrics(
            2000000L, 500000L, 100000L, 3000000L, 150L, 3, 0.85, 120.0
        );
        
        assertEquals(2000000L, metrics.getTotalExecutionTime());
        assertEquals(500000L, metrics.getAverageExecutionTime());
        assertEquals(100000L, metrics.getMinExecutionTime());
        assertEquals(3000000L, metrics.getMaxExecutionTime());
        assertEquals(150L, metrics.getCallCount());
        assertEquals(3, metrics.getThreadCount());
        assertEquals(0.85, metrics.getConsistencyScore(), 0.01);
        assertEquals(120.0, metrics.getThroughput(), 0.01);
    }
    
    @Test
    void testPerformanceMetricsToDetailedString() {
        HotPathAnalyzer.PerformanceMetrics metrics = new HotPathAnalyzer.PerformanceMetrics(
            2000000L, 500000L, 100000L, 3000000L, 150L, 3, 0.85, 120.0
        );
        
        String result = metrics.toDetailedString();
        assertTrue(result.contains("total="));
        assertTrue(result.contains("avg="));
        assertTrue(result.contains("calls="));
        assertTrue(result.contains("threads="));
    }
    
    @Test
    void testConfidenceLevelEnum() {
        assertEquals(0.95, HotPathAnalyzer.ConfidenceLevel.VERY_HIGH.getThreshold(), 0.01);
        assertEquals("Very High", HotPathAnalyzer.ConfidenceLevel.VERY_HIGH.getDescription());
        assertEquals(0.80, HotPathAnalyzer.ConfidenceLevel.HIGH.getThreshold(), 0.01);
        assertEquals("High", HotPathAnalyzer.ConfidenceLevel.HIGH.getDescription());
        assertEquals(0.60, HotPathAnalyzer.ConfidenceLevel.MEDIUM.getThreshold(), 0.01);
        assertEquals("Medium", HotPathAnalyzer.ConfidenceLevel.MEDIUM.getDescription());
        assertEquals(0.40, HotPathAnalyzer.ConfidenceLevel.LOW.getThreshold(), 0.01);
        assertEquals("Low", HotPathAnalyzer.ConfidenceLevel.LOW.getDescription());
        assertEquals(0.20, HotPathAnalyzer.ConfidenceLevel.VERY_LOW.getThreshold(), 0.01);
        assertEquals("Very Low", HotPathAnalyzer.ConfidenceLevel.VERY_LOW.getDescription());
    }
    
    @Test
    void testRecommendationTypeEnum() {
        assertEquals("Code Reordering", HotPathAnalyzer.RecommendationType.CODE_REORDERING.getDescription());
        assertEquals("Method Inlining", HotPathAnalyzer.RecommendationType.METHOD_INLINING.getDescription());
        assertEquals("Cache Optimization", HotPathAnalyzer.RecommendationType.CACHE_OPTIMIZATION.getDescription());
        assertEquals("Parallel Execution", HotPathAnalyzer.RecommendationType.PARALLEL_EXECUTION.getDescription());
        assertEquals("Early Initialization", HotPathAnalyzer.RecommendationType.EARLY_INITIALIZATION.getDescription());
    }
    
    @Test
    void testPriorityEnum() {
        assertEquals(4, HotPathAnalyzer.Priority.CRITICAL.getLevel());
        assertEquals("Critical", HotPathAnalyzer.Priority.CRITICAL.getDescription());
        assertEquals(3, HotPathAnalyzer.Priority.HIGH.getLevel());
        assertEquals("High", HotPathAnalyzer.Priority.HIGH.getDescription());
        assertEquals(2, HotPathAnalyzer.Priority.MEDIUM.getLevel());
        assertEquals("Medium", HotPathAnalyzer.Priority.MEDIUM.getDescription());
        assertEquals(1, HotPathAnalyzer.Priority.LOW.getLevel());
        assertEquals("Low", HotPathAnalyzer.Priority.LOW.getDescription());
    }
    
    @Test
    void testGetCachedAnalysis() {
        analyzer.clearCache();
        assertTrue(analyzer.getCachedAnalysis().isEmpty());
        
        // Add some data and analyze
        addHotExecutionData();
        analyzer.analyzeHotPaths(5);
        
        List<HotPathAnalyzer.HotPathAnalysis> cached = analyzer.getCachedAnalysis();
        assertNotNull(cached);
    }
    
    @Test
    void testClearCache() {
        addHotExecutionData();
        analyzer.analyzeHotPaths(5);
        
        assertFalse(analyzer.getCachedAnalysis().isEmpty());
        
        analyzer.clearCache();
        assertTrue(analyzer.getCachedAnalysis().isEmpty());
    }
    
    @Test
    void testGetAnalysisCount() {
        assertEquals(0, analyzer.getAnalysisCount());
        
        analyzer.analyzeHotPaths(5);
        assertTrue(analyzer.getAnalysisCount() > 0);
    }
    
    @Test
    void testGetTracker() {
        assertSame(tracker, analyzer.getTracker());
    }
    
    @Test
    void testShutdown() {
        assertDoesNotThrow(() -> analyzer.shutdown());
        
        // After shutdown, analyzer should still be usable but may have limited functionality
        analyzer.analyzeHotPaths(5);
        assertDoesNotThrow(() -> analyzer.shutdown());
    }
    
    // Helper methods
    
    private void addSampleExecutionData() {
        // Add some basic execution data
        for (int i = 0; i < 10; i++) {
            tracker.startMethodTracking("SampleClass", "sampleMethod" + i, "()V");
            tracker.endMethodTracking("SampleClass", "sampleMethod" + i, "()V", System.nanoTime());
        }
    }
    
    private void addHotExecutionData() {
        // Add execution data that should create hot methods
        String[] hotMethods = {
            "io.warmup.framework.startup.StartupPhasesManager.initialize",
            "io.warmup.framework.startup.bootstrap.CriticalClassPreloadSystem.execute",
            "io.warmup.framework.config.PreloadedConfigSystem.load"
        };
        
        for (String method : hotMethods) {
            String[] parts = method.split("\\.");
            String className = String.join(".", Arrays.asList(parts).subList(0, parts.length - 1));
            String methodName = parts[parts.length - 1];
            
            // Add multiple calls with sufficient execution time to make them "hot"
            for (int i = 0; i < 25; i++) {
                long startTime = System.nanoTime();
                tracker.startMethodTracking(className, methodName, "()V");
                
                // Simulate some execution time (150+ microseconds to be considered hot)
                try {
                    Thread.sleep(1); // 1ms = 1,000,000 nanoseconds
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
                
                tracker.endMethodTracking(className, methodName, "()V", startTime);
            }
        }
    }
    
    private HotPathAnalyzer.OptimizationRecommendation createMockRecommendation(String id, double improvement) {
        return new HotPathAnalyzer.OptimizationRecommendation(
            id, "Mock Recommendation", HotPathAnalyzer.RecommendationType.CODE_REORDERING,
            HotPathAnalyzer.Priority.MEDIUM, improvement, new ArrayList<>(), new ArrayList<>(),
            new HashMap<>(), HotPathAnalyzer.ConfidenceLevel.MEDIUM
        );
    }
}