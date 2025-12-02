package io.warmup.framework.startup.hotpath;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import static org.junit.jupiter.api.Assertions.*;

import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Unit tests for HotPathOptimizationSystem class.
 * Tests the complete hot path optimization workflow including tracking, analysis,
 * optimization generation, and result processing.
 * 
 * @author MiniMax Agent
 * @version 1.0
 */
class HotPathOptimizationSystemTest {
    
    private HotPathOptimizationSystem optimizationSystem;
    private ExecutorService testExecutor;
    
    @BeforeEach
    void setUp() {
        optimizationSystem = new HotPathOptimizationSystem();
        testExecutor = Executors.newFixedThreadPool(2);
    }
    
    @AfterEach
    void tearDown() {
        optimizationSystem.shutdown();
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
    void testConstructorWithDefaultConfig() {
        HotPathOptimizationSystem system = new HotPathOptimizationSystem();
        assertNotNull(system);
        assertNotNull(system.getConfig());
        assertEquals(HotPathOptimizationSystem.OptimizationStrategy.BALANCED, 
                    system.getConfig().getOptimizationStrategy());
        system.shutdown();
    }
    
    @Test
    void testConstructorWithCustomConfig() {
        HotPathOptimizationSystem.OptimizationConfig customConfig = 
            HotPathOptimizationSystem.OptimizationConfig.aggressiveConfig();
        
        HotPathOptimizationSystem system = new HotPathOptimizationSystem(customConfig);
        assertNotNull(system);
        assertSame(customConfig, system.getConfig());
        system.shutdown();
    }
    
    @Test
    void testExecuteOptimization() {
        HotPathOptimizationSystem.HotPathOptimizationResult result = optimizationSystem.executeOptimization();
        
        assertNotNull(result);
        assertNotNull(result.getExecutionId());
        assertTrue(result.getExecutionId().startsWith("HPO_"));
        assertNotNull(result.getExecutionTime());
        assertTrue(result.getExecutionTime().toMillis() >= 0);
        assertNotNull(result.getSystemState());
        assertTrue(result.getIdentifiedHotPaths() != null);
        assertTrue(result.getGeneratedPlans() != null);
        assertTrue(result.getAppliedOptimizations() != null);
        assertTrue(result.getMetrics() != null);
        assertTrue(result.getRecommendations() != null);
    }
    
    @Test
    void testExecuteOptimizationAsync() {
        CompletableFuture<HotPathOptimizationSystem.HotPathOptimizationResult> future = 
            optimizationSystem.executeOptimizationAsync();
        
        HotPathOptimizationSystem.HotPathOptimizationResult result = future.join();
        
        assertNotNull(result);
        assertNotNull(result.getExecutionId());
        assertTrue(result.getExecutionTime().toMillis() >= 0);
    }
    
    @Test
    void testHotPathOptimizationResultProperties() {
        List<HotPathAnalyzer.HotPathAnalysis> hotPaths = createMockHotPaths();
        List<CodeReorderingOptimizer.CodeReorderingPlan> plans = new ArrayList<>();
        List<CodeReorderingOptimizer.OptimizationResult> optimizations = new ArrayList<>();
        Map<String, Object> metrics = new HashMap<>();
        List<String> recommendations = Arrays.asList("Test recommendation");
        
        HotPathOptimizationSystem.HotPathOptimizationResult result = new HotPathOptimizationSystem.HotPathOptimizationResult(
            "test-id", true, Duration.ofSeconds(10), hotPaths, plans, optimizations,
            50.0, 45.0, metrics, recommendations, HotPathOptimizationSystem.SystemState.COMPLETED
        );
        
        assertEquals("test-id", result.getExecutionId());
        assertTrue(result.isSuccess());
        assertEquals(Duration.ofSeconds(10), result.getExecutionTime());
        assertEquals(2, result.getIdentifiedHotPaths().size());
        assertEquals(0, result.getGeneratedPlans().size());
        assertEquals(0, result.getAppliedOptimizations().size());
        assertEquals(50.0, result.getTotalExpectedImprovement(), 0.01);
        assertEquals(45.0, result.getTotalActualImprovement(), 0.01);
        assertEquals(metrics, result.getMetrics());
        assertEquals(1, result.getRecommendations().size());
        assertEquals(HotPathOptimizationSystem.SystemState.COMPLETED, result.getSystemState());
        assertNotNull(result.getCompletionTime());
    }
    
    @Test
    void testHotPathOptimizationResultImprovementEfficiency() {
        // Perfect efficiency
        HotPathOptimizationSystem.HotPathOptimizationResult perfectResult = 
            new HotPathOptimizationSystem.HotPathOptimizationResult(
                "perfect", true, Duration.ofSeconds(1), new ArrayList<>(), new ArrayList<>(), new ArrayList(),
                100.0, 100.0, new HashMap<>(), new ArrayList<>(), HotPathOptimizationSystem.SystemState.COMPLETED
            );
        assertEquals(1.0, perfectResult.getImprovementEfficiency(), 0.01);
        
        // Partial efficiency
        HotPathOptimizationSystem.HotPathOptimizationResult partialResult = 
            new HotPathOptimizationSystem.HotPathOptimizationResult(
                "partial", true, Duration.ofSeconds(1), new ArrayList<>(), new ArrayList<>(), new ArrayList(),
                100.0, 75.0, new HashMap<>(), new ArrayList<>(), HotPathOptimizationSystem.SystemState.COMPLETED
            );
        assertEquals(0.75, partialResult.getImprovementEfficiency(), 0.01);
        
        // Zero efficiency
        HotPathOptimizationSystem.HotPathOptimizationResult zeroResult = 
            new HotPathOptimizationSystem.HotPathOptimizationResult(
                "zero", true, Duration.ofSeconds(1), new ArrayList<>(), new ArrayList<>(), new ArrayList(),
                0.0, 0.0, new HashMap<>(), new ArrayList<>(), HotPathOptimizationSystem.SystemState.COMPLETED
            );
        assertEquals(0.0, zeroResult.getImprovementEfficiency(), 0.01);
    }
    
    @Test
    void testHotPathOptimizationResultOptimizedMethodCount() {
        List<CodeReorderingOptimizer.OptimizationResult> optimizations = Arrays.asList(
            createMockOptimizationResult("result1", 3, 1),
            createMockOptimizationResult("result2", 2, 0)
        );
        
        HotPathOptimizationSystem.HotPathOptimizationResult result = 
            new HotPathOptimizationSystem.HotPathOptimizationResult(
                "test", true, Duration.ofSeconds(1), new ArrayList<>(), new ArrayList<>(), optimizations,
                0.0, 0.0, new HashMap<>(), new ArrayList<>(), HotPathOptimizationSystem.SystemState.COMPLETED
            );
        
        assertEquals(5, result.getOptimizedMethodCount()); // 3 + 2
    }
    
    @Test
    void testHotPathOptimizationResultGenerateDetailedReport() {
        List<HotPathAnalyzer.HotPathAnalysis> hotPaths = createMockHotPaths();
        List<String> recommendations = Arrays.asList("Recommendation 1", "Recommendation 2");
        
        HotPathOptimizationSystem.HotPathOptimizationResult result = 
            new HotPathOptimizationSystem.HotPathOptimizationResult(
                "test-report", true, Duration.ofSeconds(5), hotPaths, new ArrayList<>(), new ArrayList<>(),
                75.0, 65.0, new HashMap<>(), recommendations, HotPathOptimizationSystem.SystemState.COMPLETED
            );
        
        String report = result.generateDetailedReport();
        
        assertNotNull(report);
        assertTrue(report.contains("Hot Path Optimization System Results"));
        assertTrue(report.contains("test-report"));
        assertTrue(report.contains("5s"));
        assertTrue(report.contains("true"));
        assertTrue(report.contains("Identified Hot Paths"));
        assertTrue(report.contains("Hot Paths Identified: 2"));
        assertTrue(report.contains("Expected Improvement"));
        assertTrue(report.contains("Recommendations"));
    }
    
    @Test
    void testHotPathOptimizationResultToString() {
        HotPathOptimizationSystem.HotPathOptimizationResult result = 
            new HotPathOptimizationSystem.HotPathOptimizationResult(
                "test-string", true, Duration.ofSeconds(1), new ArrayList<>(), new ArrayList<>(), new ArrayList<>(),
                0.0, 0.0, new HashMap<>(), new ArrayList<>(), HotPathOptimizationSystem.SystemState.COMPLETED
            );
        
        String resultStr = result.toString();
        assertTrue(resultStr.contains("test-string"));
        assertTrue(resultStr.contains("true"));
        assertTrue(resultStr.contains("0.0%"));
    }
    
    @Test
    void testOptimizationConfigDefault() {
        HotPathOptimizationSystem.OptimizationConfig config = 
            HotPathOptimizationSystem.OptimizationConfig.defaultConfig();
        
        assertNotNull(config);
        assertEquals(Duration.ofSeconds(3), config.getTrackingDuration());
        assertEquals(20, config.getMaxHotPaths());
        assertEquals(5, config.getMaxOptimizationPlans());
        assertEquals(30.0, config.getMinHotnessThreshold(), 0.01);
        assertFalse(config.isAutoApplyOptimizations());
        assertFalse(config.isEnableAggressiveOptimization());
        assertEquals(Duration.ofSeconds(2), config.getAnalysisTimeout());
        assertEquals(10, config.getMinMethodCallCount());
        assertTrue(config.isEnableParallelAnalysis());
        assertEquals(HotPathOptimizationSystem.RiskTolerance.MODERATE, config.getRiskTolerance());
        assertEquals(HotPathOptimizationSystem.OptimizationStrategy.BALANCED, config.getOptimizationStrategy());
    }
    
    @Test
    void testOptimizationConfigAggressive() {
        HotPathOptimizationSystem.OptimizationConfig config = 
            HotPathOptimizationSystem.OptimizationConfig.aggressiveConfig();
        
        assertNotNull(config);
        assertEquals(Duration.ofSeconds(5), config.getTrackingDuration());
        assertEquals(50, config.getMaxHotPaths());
        assertEquals(10, config.getMaxOptimizationPlans());
        assertEquals(20.0, config.getMinHotnessThreshold(), 0.01);
        assertTrue(config.isAutoApplyOptimizations());
        assertTrue(config.isEnableAggressiveOptimization());
        assertEquals(Duration.ofSeconds(3), config.getAnalysisTimeout());
        assertEquals(5, config.getMinMethodCallCount());
        assertTrue(config.isEnableParallelAnalysis());
        assertEquals(HotPathOptimizationSystem.RiskTolerance.HIGH, config.getRiskTolerance());
        assertEquals(HotPathOptimizationSystem.OptimizationStrategy.MAXIMUM_PERFORMANCE, config.getOptimizationStrategy());
    }
    
    @Test
    void testRiskToleranceEnum() {
        assertEquals(0.1, HotPathOptimizationSystem.RiskTolerance.CONSERVATIVE.getThreshold(), 0.01);
        assertEquals(0.3, HotPathOptimizationSystem.RiskTolerance.MODERATE.getThreshold(), 0.01);
        assertEquals(0.5, HotPathOptimizationSystem.RiskTolerance.AGGRESSIVE.getThreshold(), 0.01);
        assertEquals(0.7, HotPathOptimizationSystem.RiskTolerance.HIGH.getThreshold(), 0.01);
    }
    
    @Test
    void testOptimizationStrategyEnum() {
        assertNotNull(HotPathOptimizationSystem.OptimizationStrategy.CONSERVATIVE.getDescription());
        assertNotNull(HotPathOptimizationSystem.OptimizationStrategy.BALANCED.getDescription());
        assertNotNull(HotPathOptimizationSystem.OptimizationStrategy.PERFORMANCE_FOCUSED.getDescription());
        assertNotNull(HotPathOptimizationSystem.OptimizationStrategy.MAXIMUM_PERFORMANCE.getDescription());
        
        assertTrue(HotPathOptimizationSystem.OptimizationStrategy.CONSERVATIVE.getDescription().contains("stability"));
        assertTrue(HotPathOptimizationSystem.OptimizationStrategy.BALANCED.getDescription().contains("stability") || 
                   HotPathOptimizationSystem.OptimizationStrategy.BALANCED.getDescription().contains("performance"));
    }
    
    @Test
    void testSystemStateEnum() {
        assertEquals("Initializing", HotPathOptimizationSystem.SystemState.INITIALIZING.getDescription());
        assertEquals("Tracking Execution", HotPathOptimizationSystem.SystemState.TRACKING.getDescription());
        assertEquals("Analyzing Hot Paths", HotPathOptimizationSystem.SystemState.ANALYZING.getDescription());
        assertEquals("Generating Optimizations", HotPathOptimizationSystem.SystemState.OPTIMIZING.getDescription());
        assertEquals("Applying Optimizations", HotPathOptimizationSystem.SystemState.APPLYING.getDescription());
        assertEquals("Completed", HotPathOptimizationSystem.SystemState.COMPLETED.getDescription());
        assertEquals("Error State", HotPathOptimizationSystem.SystemState.ERROR.getDescription());
    }
    
    @Test
    void testGetTracker() {
        assertNotNull(optimizationSystem.getTracker());
        assertTrue(optimizationSystem.getTracker() instanceof ExecutionPathTracker);
    }
    
    @Test
    void testGetAnalyzer() {
        assertNotNull(optimizationSystem.getAnalyzer());
        assertTrue(optimizationSystem.getAnalyzer() instanceof HotPathAnalyzer);
    }
    
    @Test
    void testGetOptimizer() {
        assertNotNull(optimizationSystem.getOptimizer());
        assertTrue(optimizationSystem.getOptimizer() instanceof CodeReorderingOptimizer);
    }
    
    @Test
    void testGetExecutionHistory() {
        // Execute optimization to add to history
        optimizationSystem.executeOptimization();
        
        List<HotPathOptimizationSystem.HotPathOptimizationResult> history = optimizationSystem.getExecutionHistory();
        assertNotNull(history);
        assertTrue(history.size() > 0);
        
        // Execute again
        optimizationSystem.executeOptimization();
        assertTrue(optimizationSystem.getExecutionHistory().size() > history.size());
    }
    
    @Test
    void testExecutionStatistics() {
        assertEquals(0, optimizationSystem.getExecutionCount());
        assertEquals(0, optimizationSystem.getTotalExecutionTime());
        assertEquals(0.0, optimizationSystem.getAverageExecutionTime(), 0.01);
        
        // Execute optimization
        optimizationSystem.executeOptimization();
        
        assertEquals(1, optimizationSystem.getExecutionCount());
        assertTrue(optimizationSystem.getTotalExecutionTime() > 0);
        assertTrue(optimizationSystem.getAverageExecutionTime() > 0);
    }
    
    @Test
    void testIsActive() {
        assertFalse(optimizationSystem.isActive());
        
        optimizationSystem.start();
        assertTrue(optimizationSystem.isActive());
        
        optimizationSystem.stop();
        assertFalse(optimizationSystem.isActive());
    }
    
    @Test
    void testStartAndStop() {
        optimizationSystem.start();
        assertTrue(optimizationSystem.isActive());
        
        optimizationSystem.stop();
        assertFalse(optimizationSystem.isActive());
        
        // Test multiple start/stop cycles
        optimizationSystem.start();
        optimizationSystem.stop();
        optimizationSystem.start();
        
        assertTrue(optimizationSystem.isActive());
        optimizationSystem.stop();
    }
    
    @Test
    void testShutdown() {
        assertDoesNotThrow(() -> optimizationSystem.shutdown());
        
        // After shutdown, system should be in stopped state
        assertFalse(optimizationSystem.isActive());
        
        // Multiple shutdowns should be safe
        assertDoesNotThrow(() -> optimizationSystem.shutdown());
    }
    
    @Test
    void testExecuteOptimizationWithError() {
        // The system should handle errors gracefully
        HotPathOptimizationSystem.HotPathOptimizationResult result = optimizationSystem.executeOptimization();
        assertNotNull(result);
        // Result may be success or error depending on implementation
    }
    
    @Test
    void testMultipleExecutions() {
        // Execute optimization multiple times
        int numExecutions = 3;
        List<HotPathOptimizationSystem.HotPathOptimizationResult> results = new ArrayList<>();
        
        for (int i = 0; i < numExecutions; i++) {
            HotPathOptimizationSystem.HotPathOptimizationResult result = optimizationSystem.executeOptimization();
            assertNotNull(result);
            results.add(result);
        }
        
        // Verify all executions completed
        assertEquals(numExecutions, optimizationSystem.getExecutionCount());
        assertTrue(optimizationSystem.getExecutionHistory().size() >= numExecutions);
        
        // Verify each result is valid
        for (HotPathOptimizationSystem.HotPathOptimizationResult result : results) {
            assertNotNull(result.getExecutionId());
            assertNotNull(result.getExecutionTime());
            assertTrue(result.getExecutionTime().toMillis() > 0);
        }
    }
    
    @Test
    void testConcurrentOptimizations() {
        // Test multiple concurrent optimization executions
        CompletableFuture<HotPathOptimizationSystem.HotPathOptimizationResult> future1 = 
            optimizationSystem.executeOptimizationAsync();
        CompletableFuture<HotPathOptimizationSystem.HotPathOptimizationResult> future2 = 
            optimizationSystem.executeOptimizationAsync();
        
        HotPathOptimizationSystem.HotPathOptimizationResult result1 = future1.join();
        HotPathOptimizationSystem.HotPathOptimizationResult result2 = future2.join();
        
        assertNotNull(result1);
        assertNotNull(result2);
        assertNotEquals(result1.getExecutionId(), result2.getExecutionId());
    }
    
    @Test
    void testHotPathsAndPlansInResult() {
        HotPathOptimizationSystem.HotPathOptimizationResult result = optimizationSystem.executeOptimization();
        
        assertNotNull(result.getIdentifiedHotPaths());
        assertNotNull(result.getGeneratedPlans());
        assertNotNull(result.getAppliedOptimizations());
        assertNotNull(result.getMetrics());
        assertNotNull(result.getRecommendations());
        
        // Verify lists are defensive copies
        List<HotPathAnalyzer.HotPathAnalysis> hotPaths = result.getIdentifiedHotPaths();
        assertNotSame(hotPaths, result.getIdentifiedHotPaths()); // Should be a copy
    }
    
    @Test
    void testSystemLifeCycleManagement() {
        // Test start method
        optimizationSystem.start();
        // After start, system should be initialized
        
        // Test executeOptimization after start
        HotPathOptimizationSystem.HotPathOptimizationResult result = optimizationSystem.executeOptimization();
        assertNotNull(result);
        
        // Test stop method
        optimizationSystem.stop();
        // After stop, system should be in stopped state
        
        // Test multiple calls to start/stop (should be idempotent)
        optimizationSystem.start();
        optimizationSystem.start(); // Should not cause issues
        optimizationSystem.stop();
        optimizationSystem.stop(); // Should not cause issues
    }
    
    @Test
    void testShutdownMethod() {
        // Test shutdown
        optimizationSystem.shutdown();
        
        // After shutdown, system should be properly cleaned up
        // Try to execute optimization after shutdown
        // This might throw an exception or return an error result
        HotPathOptimizationSystem.HotPathOptimizationResult result = optimizationSystem.executeOptimization();
        
        // The behavior after shutdown depends on implementation
        // Either it throws exception or returns error result
        assertNotNull(result);
    }
    
    @Test
    void testDefaultConfiguration() {
        HotPathOptimizationSystem.OptimizationConfig defaultConfig = 
            HotPathOptimizationSystem.OptimizationConfig.defaultConfig();
        
        assertNotNull(defaultConfig);
        
        // Test that default config has reasonable defaults
        assertTrue(defaultConfig.getTrackingDuration().toMinutes() > 0);
        assertTrue(defaultConfig.getMaxHotPaths() > 0);
        assertTrue(defaultConfig.getMaxOptimizationPlans() > 0);
        
        // Test system creation with default config
        HotPathOptimizationSystem systemWithDefault = new HotPathOptimizationSystem(defaultConfig);
        assertSame(defaultConfig, systemWithDefault.getConfig());
        systemWithDefault.shutdown();
    }
    
    @Test
    void testAggressiveConfiguration() {
        HotPathOptimizationSystem.OptimizationConfig aggressiveConfig = 
            HotPathOptimizationSystem.OptimizationConfig.aggressiveConfig();
        
        assertNotNull(aggressiveConfig);
        
        // Test that aggressive config has more aggressive settings
        // Generally aggressive config should have longer tracking duration and more hot paths identified
        assertTrue(aggressiveConfig.getMaxHotPaths() >= 
                  HotPathOptimizationSystem.OptimizationConfig.defaultConfig().getMaxHotPaths());
        
        // Test system creation with aggressive config
        HotPathOptimizationSystem systemWithAggressive = new HotPathOptimizationSystem(aggressiveConfig);
        assertSame(aggressiveConfig, systemWithAggressive.getConfig());
        systemWithAggressive.shutdown();
    }
    
    @Test
    void testExecuteOptimizationAsyncWithMultipleConcurrency() {
        // Execute multiple async optimizations concurrently
        CompletableFuture<HotPathOptimizationSystem.HotPathOptimizationResult> future1 = 
            optimizationSystem.executeOptimizationAsync();
        CompletableFuture<HotPathOptimizationSystem.HotPathOptimizationResult> future2 = 
            optimizationSystem.executeOptimizationAsync();
        CompletableFuture<HotPathOptimizationSystem.HotPathOptimizationResult> future3 = 
            optimizationSystem.executeOptimizationAsync();
        
        // Wait for all to complete
        CompletableFuture.allOf(future1, future2, future3).join();
        
        // Verify all completed successfully
        assertTrue(future1.isDone());
        assertTrue(future2.isDone());
        assertTrue(future3.isDone());
        
        // Verify each has valid results
        assertNotNull(future1.join());
        assertNotNull(future2.join());
        assertNotNull(future3.join());
        
        // Each should have unique execution IDs
        assertNotEquals(future1.join().getExecutionId(), future2.join().getExecutionId());
        assertNotEquals(future2.join().getExecutionId(), future3.join().getExecutionId());
        assertNotEquals(future1.join().getExecutionId(), future3.join().getExecutionId());
    }
    
    @Test
    void testHotPathOptimizationResultWithEmptyCollections() {
        // Test result with all empty collections
        HotPathOptimizationSystem.HotPathOptimizationResult emptyResult = 
            new HotPathOptimizationSystem.HotPathOptimizationResult(
                "empty-test", true, Duration.ofSeconds(1), 
                new ArrayList<>(), new ArrayList<>(), new ArrayList<>(),
                0.0, 0.0, new HashMap<>(), new ArrayList<>(), 
                HotPathOptimizationSystem.SystemState.COMPLETED
            );
        
        assertNotNull(emptyResult);
        assertTrue(emptyResult.getIdentifiedHotPaths().isEmpty());
        assertTrue(emptyResult.getGeneratedPlans().isEmpty());
        assertTrue(emptyResult.getAppliedOptimizations().isEmpty());
        assertTrue(emptyResult.getRecommendations().isEmpty());
        assertTrue(emptyResult.getMetrics().isEmpty());
        assertEquals(0.0, emptyResult.getTotalExpectedImprovement(), 0.01);
        assertEquals(0.0, emptyResult.getTotalActualImprovement(), 0.01);
        assertEquals(0, emptyResult.getOptimizedMethodCount());
        assertEquals(0.0, emptyResult.getImprovementEfficiency(), 0.01);
    }
    
    @Test
    void testHotPathOptimizationResultWithDifferentSystemStates() {
        HotPathOptimizationSystem.HotPathOptimizationResult runningResult = 
            new HotPathOptimizationSystem.HotPathOptimizationResult(
                "running-test", true, Duration.ofSeconds(1), 
                new ArrayList<>(), new ArrayList<>(), new ArrayList<>(),
                0.0, 0.0, new HashMap<>(), new ArrayList<>(), 
                HotPathOptimizationSystem.SystemState.TRACKING
            );
        
        HotPathOptimizationSystem.HotPathOptimizationResult failedResult = 
            new HotPathOptimizationSystem.HotPathOptimizationResult(
                "failed-test", false, Duration.ofSeconds(1), 
                new ArrayList<>(), new ArrayList<>(), new ArrayList<>(),
                0.0, 0.0, new HashMap<>(), new ArrayList<>(), 
                HotPathOptimizationSystem.SystemState.ERROR
            );
        
        assertEquals(HotPathOptimizationSystem.SystemState.TRACKING, runningResult.getSystemState());
        assertEquals(HotPathOptimizationSystem.SystemState.ERROR, failedResult.getSystemState());
        assertFalse(runningResult.isSuccess()); // Even if state is RUNNING, might not be considered success
        assertFalse(failedResult.isSuccess());
    }
    
    @Test
    void testExecuteOptimizationWithEmptyConfiguration() {
        // Create system with default configuration
        HotPathOptimizationSystem.OptimizationConfig minimalConfig = 
            HotPathOptimizationSystem.OptimizationConfig.defaultConfig();
        
        HotPathOptimizationSystem minimalSystem = new HotPathOptimizationSystem(minimalConfig);
        
        // Should still work, just with very limited scope
        HotPathOptimizationSystem.HotPathOptimizationResult result = minimalSystem.executeOptimization();
        assertNotNull(result);
        
        minimalSystem.shutdown();
    }
    
    @Nested
    @DisplayName("OptimizationConfig Tests")
    class OptimizationConfigTests {
        
        @Test
        @DisplayName("Constructor with default parameters")
        void testOptimizationConfigDefaultParameters() {
            HotPathOptimizationSystem.OptimizationConfig config = 
                HotPathOptimizationSystem.OptimizationConfig.defaultConfig();
            
            assertNotNull(config.getTrackingDuration());
            assertTrue(config.getMaxHotPaths() > 0);
            assertTrue(config.getMaxOptimizationPlans() > 0);
        }
        
        @Test
        @DisplayName("Default configuration properties")
        void testDefaultConfigurationProperties() {
            HotPathOptimizationSystem.OptimizationConfig defaultConfig = 
                HotPathOptimizationSystem.OptimizationConfig.defaultConfig();
            
            assertNotNull(defaultConfig.getOptimizationStrategy());
            assertNotNull(defaultConfig.getRiskTolerance());
            assertTrue(defaultConfig.getTrackingDuration().toMinutes() > 0);
            assertTrue(defaultConfig.getMaxHotPaths() > 0);
        }
        
        @Test
        @DisplayName("Aggressive configuration properties")
        void testAggressiveConfigurationProperties() {
            HotPathOptimizationSystem.OptimizationConfig aggressiveConfig = 
                HotPathOptimizationSystem.OptimizationConfig.aggressiveConfig();
            
            assertNotNull(aggressiveConfig.getOptimizationStrategy());
            assertNotNull(aggressiveConfig.getRiskTolerance());
            
            // Aggressive should generally be more permissive
            assertTrue(aggressiveConfig.getMaxHotPaths() >= defaultConfig().getMaxHotPaths());
        }
        
        private HotPathOptimizationSystem.OptimizationConfig defaultConfig() {
            return HotPathOptimizationSystem.OptimizationConfig.defaultConfig();
        }
    }
    
    @Test
    void testMultipleSystemInstances() {
        // Create multiple system instances
        HotPathOptimizationSystem system1 = new HotPathOptimizationSystem();
        HotPathOptimizationSystem system2 = new HotPathOptimizationSystem();
        HotPathOptimizationSystem system3 = new HotPathOptimizationSystem();
        
        // Each should work independently
        HotPathOptimizationSystem.HotPathOptimizationResult result1 = system1.executeOptimization();
        HotPathOptimizationSystem.HotPathOptimizationResult result2 = system2.executeOptimization();
        HotPathOptimizationSystem.HotPathOptimizationResult result3 = system3.executeOptimization();
        
        assertNotNull(result1);
        assertNotNull(result2);
        assertNotNull(result3);
        
        // Each should have unique execution IDs
        assertNotEquals(result1.getExecutionId(), result2.getExecutionId());
        assertNotEquals(result2.getExecutionId(), result3.getExecutionId());
        
        // Clean up
        system1.shutdown();
        system2.shutdown();
        system3.shutdown();
    }
    
    @Test
    void testSystemStateTransitions() {
        // Test initial state
        HotPathOptimizationSystem.HotPathOptimizationResult initialResult = optimizationSystem.executeOptimization();
        assertNotNull(initialResult.getSystemState());
        
        // Test after start/stop cycles
        optimizationSystem.start();
        optimizationSystem.stop();
        optimizationSystem.start();
        
        HotPathOptimizationSystem.HotPathOptimizationResult afterCycles = optimizationSystem.executeOptimization();
        assertNotNull(afterCycles.getSystemState());
        
        // System should still be functional
        assertTrue(afterCycles.getExecutionTime().toMillis() >= 0);
    }
    
    @Test
    void testExecutionTimeAccuracy() {
        // Measure execution time for multiple runs
        List<Long> executionTimes = new ArrayList<>();
        
        for (int i = 0; i < 5; i++) {
            HotPathOptimizationSystem.HotPathOptimizationResult result = optimizationSystem.executeOptimization();
            assertTrue(result.getExecutionTime().toMillis() >= 0);
            executionTimes.add(result.getExecutionTime().toMillis());
        }
        
        // All execution times should be positive
        for (Long time : executionTimes) {
            assertTrue(time >= 0);
        }
    }
    
    @Test
    void testResultImmutability() {
        HotPathOptimizationSystem.HotPathOptimizationResult result = optimizationSystem.executeOptimization();
        
        // Verify that returned collections are copies, not references to internal state
        List<HotPathAnalyzer.HotPathAnalysis> hotPaths = result.getIdentifiedHotPaths();
        List<CodeReorderingOptimizer.CodeReorderingPlan> plans = result.getGeneratedPlans();
        List<CodeReorderingOptimizer.OptimizationResult> optimizations = result.getAppliedOptimizations();
        List<String> recommendations = result.getRecommendations();
        Map<String, Object> metrics = result.getMetrics();
        
        // Modify the returned collections
        hotPaths.add(null); // This should not affect internal state
        plans.add(null);
        optimizations.add(null);
        recommendations.add("test");
        metrics.put("test", "value");
        
        // Get the collections again - should be different instances
        List<HotPathAnalyzer.HotPathAnalysis> hotPaths2 = result.getIdentifiedHotPaths();
        List<CodeReorderingOptimizer.CodeReorderingPlan> plans2 = result.getGeneratedPlans();
        List<CodeReorderingOptimizer.OptimizationResult> optimizations2 = result.getAppliedOptimizations();
        List<String> recommendations2 = result.getRecommendations();
        Map<String, Object> metrics2 = result.getMetrics();
        
        // Should be different instances (not affected by our modifications)
        assertNotSame(hotPaths, hotPaths2);
        assertNotSame(plans, plans2);
        assertNotSame(optimizations, optimizations2);
        assertNotSame(recommendations, recommendations2);
        assertNotSame(metrics, metrics2);
    }
    
    @Test
    void testExceptionHandlingDuringExecution() {
        // This test verifies that the system handles various edge cases gracefully
        // Even if there are issues during optimization, it should return a valid result
        
        // Test with system in different states
        optimizationSystem.start();
        HotPathOptimizationSystem.HotPathOptimizationResult result = optimizationSystem.executeOptimization();
        assertNotNull(result);
        optimizationSystem.stop();
        
        optimizationSystem.start();
        HotPathOptimizationSystem.HotPathOptimizationResult result2 = optimizationSystem.executeOptimization();
        assertNotNull(result2);
        
        optimizationSystem.stop();
    }
    
    // Helper methods
    
    private List<HotPathAnalyzer.HotPathAnalysis> createMockHotPaths() {
        List<HotPathAnalyzer.HotPathAnalysis> hotPaths = new ArrayList<>();
        
        // Create mock hot path 1
        HotPathAnalyzer.PerformanceMetrics metrics1 = new HotPathAnalyzer.PerformanceMetrics(
            1000000L, 500000L, 100000L, 2000000L, 100L, 2, 0.8, 100.0
        );
        
        HotPathAnalyzer.HotPathAnalysis analysis1 = new HotPathAnalyzer.HotPathAnalysis(
            "path1", "Test Hot Path 1", HotnessLevel.HOT, 75.0, 
            Arrays.asList("method1", "method2"), new ArrayList<>(),
            metrics1, HotPathAnalyzer.ConfidenceLevel.HIGH
        );
        hotPaths.add(analysis1);
        
        // Create mock hot path 2
        HotPathAnalyzer.PerformanceMetrics metrics2 = new HotPathAnalyzer.PerformanceMetrics(
            2000000L, 800000L, 200000L, 3000000L, 150L, 3, 0.7, 120.0
        );
        
        HotPathAnalyzer.HotPathAnalysis analysis2 = new HotPathAnalyzer.HotPathAnalysis(
            "path2", "Test Hot Path 2", HotnessLevel.CRITICAL, 85.0,
            Arrays.asList("method3", "method4", "method5"), new ArrayList<>(),
            metrics2, HotPathAnalyzer.ConfidenceLevel.VERY_HIGH
        );
        hotPaths.add(analysis2);
        
        return hotPaths;
    }
    
    private CodeReorderingOptimizer.OptimizationResult createMockOptimizationResult(String id, int appliedCount, int failedCount) {
        List<String> appliedActions = new ArrayList<>();
        List<String> failedActions = new ArrayList<>();
        
        for (int i = 0; i < appliedCount; i++) {
            appliedActions.add("applied-action-" + i);
        }
        
        for (int i = 0; i < failedCount; i++) {
            failedActions.add("failed-action-" + i);
        }
        
        return new CodeReorderingOptimizer.OptimizationResult(
            id, true, 20.0, Duration.ofMillis(100), appliedActions, failedActions, new HashMap<>()
        );
    }
}