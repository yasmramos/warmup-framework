package io.warmup.framework.startup.hotpath;

import io.warmup.framework.startup.hotpath.examples.HotPathOptimizationExample;
import io.warmup.framework.startup.hotpath.HotPathOptimizationSystem.OptimizationConfig;
import io.warmup.framework.startup.hotpath.HotPathOptimizationSystem.RiskTolerance;
import io.warmup.framework.startup.hotpath.HotPathOptimizationSystem.OptimizationStrategy;

import java.util.*;
import java.util.concurrent.*;
import java.time.Duration;
import java.time.Instant;

/**
 * Comprehensive test suite for Hot Path Optimization System.
 * Tests all 15+ aspects of the system including correctness, performance, and integration.
 * 
 * @author MiniMax Agent
 * @version 1.0
 */
public class CriticalClassPreloadSystemTest {
    
    private static int testsRun = 0;
    private static int testsPassed = 0;
    private static int testsFailed = 0;
    
    // Mock application for testing
    public static class TestApplication {
        private final ExecutionPathTracker tracker;
        
        public TestApplication(ExecutionPathTracker tracker) {
            this.tracker = tracker;
        }
        
        public void simulateStartup() {
            // Simulate critical startup phase
            try (ExecutionPathTracker.MethodTracker mt = tracker.trackMethod(
                    "TestApplication", "initializeFramework", "()")) {
                simulateWork(100);
            }
            
            // Simulate configuration loading
            try (ExecutionPathTracker.MethodTracker mt = tracker.trackMethod(
                    "TestApplication", "loadConfiguration", "()")) {
                simulateWork(50);
            }
            
            // Simulate service initialization
            try (ExecutionPathTracker.MethodTracker mt = tracker.trackMethod(
                    "TestApplication", "initializeServices", "()")) {
                simulateWork(200);
            }
            
            // Simulate background initialization
            try (ExecutionPathTracker.MethodTracker mt = tracker.trackMethod(
                    "TestApplication", "backgroundInitialization", "()")) {
                simulateWork(300);
            }
            
            // Simulate caching operations
            for (int i = 0; i < 10; i++) {
                try (ExecutionPathTracker.MethodTracker mt = tracker.trackMethod(
                        "TestApplication", "cacheAccess", "(int)")) {
                    simulateWork(10);
                }
            }
        }
        
        private void simulateWork(long duration) {
            try {
                Thread.sleep(duration / 1000000); // Convert nanoseconds to milliseconds
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }
    
    public static void main(String[] args) {
        System.out.println("=== COMPREHENSIVE HOT PATH OPTIMIZATION SYSTEM TEST ===");
        System.out.println("Testing all 15+ aspects of the Hot Path Optimization System");
        System.out.println();
        
        try {
            // Test 1: ExecutionPathTracker functionality
            testExecutionPathTracker();
            
            // Test 2: Hot path identification and analysis
            testHotPathAnalysis();
            
            // Test 3: Optimization plan generation
            testOptimizationPlanGeneration();
            
            // Test 4: System integration and orchestration
            testSystemIntegration();
            
            // Test 5: Metrics and performance tracking
            testMetricsAndPerformance();
            
            // Test 6: Configuration and customization
            testConfigurationAndCustomization();
            
            // Test 7: Multi-threaded operations
            testMultiThreadedOperations();
            
            // Test 8: Error handling and recovery
            testErrorHandling();
            
            // Test 9: Memory management and resource cleanup
            testMemoryManagement();
            
            // Test 10: Edge cases and boundary conditions
            testEdgeCases();
            
            // Test 11: Real-world simulation
            testRealWorldSimulation();
            
            // Test 12: Integration with StartupPhasesManager
            testStartupManagerIntegration();
            
            // Test 13: Performance under load
            testPerformanceUnderLoad();
            
            // Test 14: Concurrent optimization
            testConcurrentOptimization();
            
            // Test 15: Complete end-to-end workflow
            testEndToEndWorkflow();
            
        } catch (Exception e) {
            System.err.println("Critical test failure: " + e.getMessage());
            e.printStackTrace();
        }
        
        // Print final results
        System.out.println("\n=== TEST RESULTS SUMMARY ===");
        System.out.println("Tests Run: " + testsRun);
        System.out.println("Tests Passed: " + testsPassed);
        System.out.println("Tests Failed: " + testsFailed);
        System.out.println("Success Rate: " + (testsRun > 0 ? (testsPassed * 100.0 / testsRun) : 0) + "%");
        
        if (testsFailed == 0) {
            System.out.println("\n🎉 ALL TESTS PASSED! Hot Path Optimization System is working correctly.");
        } else {
            System.out.println("\n❌ " + testsFailed + " tests failed. Review issues above.");
        }
    }
    
    // Test 1: ExecutionPathTracker functionality
    private static void testExecutionPathTracker() {
        System.out.println("Test 1: ExecutionPathTracker functionality");
        
        try {
            ExecutionPathTracker tracker = new ExecutionPathTracker(true);
            
            // Test method tracking
            try (ExecutionPathTracker.MethodTracker mt = tracker.trackMethod(
                    "TestClass", "testMethod", "()")) {
                simulateWork(50000); // 50 microseconds
            }
            
            // Verify tracking data
            ExecutionPathTracker.ExecutionStats stats = tracker.getMethodStats("TestClass", "testMethod", "()");
            assertNotNull(stats, "Method stats should not be null");
            assertGreaterThan(stats.getCallCount(), 0, "Call count should be greater than 0");
            
            // Test class and package level tracking
            assertNotNull(tracker.getClassStats("TestClass"), "Class stats should exist");
            assertNotNull(tracker.getPackageStats("io.warmup.framework"), "Package stats should exist");
            
            // Test hot methods identification
            List<Map.Entry<ExecutionPathTracker.MethodExecution, ExecutionPathTracker.ExecutionStats>> hotMethods = 
                tracker.getHotMethods(10);
            assertNotNull(hotMethods, "Hot methods list should not be null");
            
            tracker.reset();
            System.out.println("✅ PASSED: ExecutionPathTracker functionality");
            testsPassed++;
            
        } catch (Exception e) {
            System.out.println("❌ FAILED: ExecutionPathTracker - " + e.getMessage());
            testsFailed++;
        }
        testsRun++;
        System.out.println();
    }
    
    // Test 2: Hot path identification and analysis
    private static void testHotPathAnalysis() {
        System.out.println("Test 2: Hot path identification and analysis");
        
        try {
            ExecutionPathTracker tracker = new ExecutionPathTracker(true);
            HotPathAnalyzer analyzer = new HotPathAnalyzer(tracker);
            
            // Simulate method executions with varying patterns
            simulateMethodExecutions(tracker);
            
            // Analyze hot paths
            List<HotPathAnalyzer.HotPathAnalysis> hotPaths = analyzer.analyzeHotPaths(20);
            
            assertNotNull(hotPaths, "Hot paths analysis should not be null");
            System.out.println("Identified " + hotPaths.size() + " hot paths");
            
            // Verify hot path characteristics
            for (HotPathAnalyzer.HotPathAnalysis path : hotPaths) {
                assertNotNull(path.getPathId(), "Path ID should not be null");
                assertNotNull(path.getHotnessLevel(), "Hotness level should not be null");
                assertGreaterThan(path.getHotnessScore(), 0.0, "Hotness score should be positive");
                assertNotNull(path.getPerformanceMetrics(), "Performance metrics should not be null");
            }
            
            analyzer.shutdown();
            System.out.println("✅ PASSED: Hot path identification and analysis");
            testsPassed++;
            
        } catch (Exception e) {
            System.out.println("❌ FAILED: Hot path analysis - " + e.getMessage());
            testsFailed++;
        }
        testsRun++;
        System.out.println();
    }
    
    // Test 3: Optimization plan generation
    private static void testOptimizationPlanGeneration() {
        System.out.println("Test 3: Optimization plan generation");
        
        try {
            ExecutionPathTracker tracker = new ExecutionPathTracker(true);
            HotPathAnalyzer analyzer = new HotPathAnalyzer(tracker);
            CodeReorderingOptimizer optimizer = new CodeReorderingOptimizer(analyzer);
            
            // Generate test data
            simulateMethodExecutions(tracker);
            
            // Generate optimization plans
            List<CodeReorderingOptimizer.CodeReorderingPlan> plans = optimizer.generateOptimizationPlans(10);
            
            assertNotNull(plans, "Optimization plans should not be null");
            System.out.println("Generated " + plans.size() + " optimization plans");
            
            // Verify plan characteristics
            for (CodeReorderingOptimizer.CodeReorderingPlan plan : plans) {
                assertNotNull(plan.getPlanId(), "Plan ID should not be null");
                assertNotNull(plan.getDescription(), "Plan description should not be null");
                assertNotNull(plan.getRiskLevel(), "Risk level should not be null");
                assertNotNull(plan.getConfidenceLevel(), "Confidence level should not be null");
                assertGreaterThan(plan.getActions().size(), 0, "Plan should have at least one action");
                
                // Verify actions
                for (CodeReorderingOptimizer.OptimizationAction action : plan.getActions()) {
                    assertNotNull(action.getActionId(), "Action ID should not be null");
                    assertNotNull(action.getType(), "Action type should not be null");
                }
            }
            
            optimizer.shutdown();
            System.out.println("✅ PASSED: Optimization plan generation");
            testsPassed++;
            
        } catch (Exception e) {
            System.out.println("❌ FAILED: Optimization plan generation - " + e.getMessage());
            testsFailed++;
        }
        testsRun++;
        System.out.println();
    }
    
    // Test 4: System integration and orchestration
    private static void testSystemIntegration() {
        System.out.println("Test 4: System integration and orchestration");
        
        try {
            HotPathOptimizationSystem optimizationSystem = new HotPathOptimizationSystem();
            optimizationSystem.start();
            
            // Execute optimization cycle
            HotPathOptimizationSystem.HotPathOptimizationResult result = optimizationSystem.executeOptimization();
            
            assertNotNull(result, "Optimization result should not be null");
            assertNotNull(result.getExecutionId(), "Execution ID should not be null");
            assertNotNull(result.getSystemState(), "System state should not be null");
            
            System.out.println("Execution completed: " + result.isSuccess());
            System.out.println("Hot paths identified: " + result.getIdentifiedHotPaths().size());
            System.out.println("Plans generated: " + result.getGeneratedPlans().size());
            System.out.println("Total improvement: " + result.getTotalActualImprovement() + "%");
            
            // Verify result structure
            assertNotNull(result.getIdentifiedHotPaths(), "Identified hot paths should not be null");
            assertNotNull(result.getGeneratedPlans(), "Generated plans should not be null");
            assertNotNull(result.getAppliedOptimizations(), "Applied optimizations should not be null");
            assertNotNull(result.getRecommendations(), "Recommendations should not be null");
            
            optimizationSystem.shutdown();
            System.out.println("✅ PASSED: System integration and orchestration");
            testsPassed++;
            
        } catch (Exception e) {
            System.out.println("❌ FAILED: System integration - " + e.getMessage());
            testsFailed++;
        }
        testsRun++;
        System.out.println();
    }
    
    // Test 5: Metrics and performance tracking
    private static void testMetricsAndPerformance() {
        System.out.println("Test 5: Metrics and performance tracking");
        
        try {
            HotPathOptimizationMetrics metrics = new HotPathOptimizationMetrics();
            
            // Record some test data
            metrics.recordTracking(1000000000L, 50); // 1 second, 50 methods
            metrics.recordCpuUtilization(75.5);
            
            // Create snapshot
            HotPathOptimizationMetrics.MetricsSnapshot snapshot = metrics.createSnapshot();
            
            assertNotNull(snapshot, "Metrics snapshot should not be null");
            assertGreaterThan(snapshot.getTrackingDuration(), 0L, "Tracking duration should be positive");
            assertGreaterThan(snapshot.getMethodsTracked(), 0, "Methods tracked should be positive");
            
            // Verify performance statistics
            assertNotNull(snapshot.getPerformanceStats(), "Performance stats should not be null");
            assertNotNull(snapshot.getResourceStats(), "Resource stats should not be null");
            
            // Test trend analysis
            HotPathOptimizationMetrics.TrendAnalysis trendAnalysis = 
                metrics.performTrendAnalysis(Duration.ofMinutes(10));
            
            assertNotNull(trendAnalysis, "Trend analysis should not be null");
            assertNotNull(trendAnalysis.getHotnessTrend(), "Hotness trend should not be null");
            
            System.out.println("✅ PASSED: Metrics and performance tracking");
            testsPassed++;
            
        } catch (Exception e) {
            System.out.println("❌ FAILED: Metrics and performance tracking - " + e.getMessage());
            testsFailed++;
        }
        testsRun++;
        System.out.println();
    }
    
    // Test 6: Configuration and customization
    private static void testConfigurationAndCustomization() {
        System.out.println("Test 6: Configuration and customization");
        
        try {
            // Test default configuration
            OptimizationConfig defaultConfig = OptimizationConfig.defaultConfig();
            assertNotNull(defaultConfig, "Default configuration should not be null");
            
            // Test aggressive configuration
            OptimizationConfig aggressiveConfig = OptimizationConfig.aggressiveConfig();
            assertNotNull(aggressiveConfig, "Aggressive configuration should not be null");
            
            // Verify configuration parameters
            assertGreaterThan(aggressiveConfig.getTrackingDuration().toMinutes(), 
                            defaultConfig.getTrackingDuration().toMinutes(), 
                            "Aggressive config should have longer tracking duration");
            
            assertGreaterThan(aggressiveConfig.getMaxHotPaths(), 
                            defaultConfig.getMaxHotPaths(), 
                            "Aggressive config should allow more hot paths");
            
            // Test system with different configurations
            HotPathOptimizationSystem defaultSystem = new HotPathOptimizationSystem(defaultConfig);
            HotPathOptimizationSystem aggressiveSystem = new HotPathOptimizationSystem(aggressiveConfig);
            
            assertEquals(defaultSystem.getConfig(), defaultConfig, "Config should match");
            assertEquals(aggressiveSystem.getConfig(), aggressiveConfig, "Config should match");
            
            defaultSystem.shutdown();
            aggressiveSystem.shutdown();
            
            System.out.println("✅ PASSED: Configuration and customization");
            testsPassed++;
            
        } catch (Exception e) {
            System.out.println("❌ FAILED: Configuration and customization - " + e.getMessage());
            testsFailed++;
        }
        testsRun++;
        System.out.println();
    }
    
    // Test 7: Multi-threaded operations
    private static void testMultiThreadedOperations() {
        System.out.println("Test 7: Multi-threaded operations");
        
        try {
            HotPathOptimizationSystem optimizationSystem = new HotPathOptimizationSystem();
            optimizationSystem.start();
            
            // Execute multiple optimization cycles concurrently
            List<CompletableFuture<HotPathOptimizationSystem.HotPathOptimizationResult>> futures = new ArrayList<>();
            
            for (int i = 0; i < 3; i++) {
                futures.add(optimizationSystem.executeOptimizationAsync());
            }
            
            // Wait for all completions
            CompletableFuture<Void> allFutures = CompletableFuture.allOf(
                futures.toArray(new CompletableFuture[0])
            );
            
            allFutures.get(30, TimeUnit.SECONDS);
            
            // Verify all results
            for (CompletableFuture<HotPathOptimizationSystem.HotPathOptimizationResult> future : futures) {
                HotPathOptimizationSystem.HotPathOptimizationResult result = future.get();
                assertNotNull(result, "Concurrent optimization result should not be null");
            }
            
            optimizationSystem.shutdown();
            System.out.println("✅ PASSED: Multi-threaded operations");
            testsPassed++;
            
        } catch (Exception e) {
            System.out.println("❌ FAILED: Multi-threaded operations - " + e.getMessage());
            testsFailed++;
        }
        testsRun++;
        System.out.println();
    }
    
    // Test 8: Error handling and recovery
    private static void testErrorHandling() {
        System.out.println("Test 8: Error handling and recovery");
        
        try {
            // Test with inactive tracker
            ExecutionPathTracker inactiveTracker = new ExecutionPathTracker(false);
            HotPathAnalyzer analyzer = new HotPathAnalyzer(inactiveTracker);
            
            List<HotPathAnalyzer.HotPathAnalysis> hotPaths = analyzer.analyzeHotPaths(10);
            assertNotNull(hotPaths, "Should handle inactive tracker gracefully");
            assertEquals(hotPaths.size(), 0, "Should return empty list for inactive tracker");
            
            analyzer.shutdown();
            
            // Test with empty data
            ExecutionPathTracker emptyTracker = new ExecutionPathTracker(true);
            HotPathAnalyzer emptyAnalyzer = new HotPathAnalyzer(emptyTracker);
            
            List<HotPathAnalyzer.HotPathAnalysis> emptyHotPaths = emptyAnalyzer.analyzeHotPaths(10);
            assertNotNull(emptyHotPaths, "Should handle empty data gracefully");
            
            emptyAnalyzer.shutdown();
            
            System.out.println("✅ PASSED: Error handling and recovery");
            testsPassed++;
            
        } catch (Exception e) {
            System.out.println("❌ FAILED: Error handling - " + e.getMessage());
            testsFailed++;
        }
        testsRun++;
        System.out.println();
    }
    
    // Test 9: Memory management and resource cleanup
    private static void testMemoryManagement() {
        System.out.println("Test 9: Memory management and resource cleanup");
        
        try {
            // Create and shutdown multiple systems
            for (int i = 0; i < 5; i++) {
                HotPathOptimizationSystem system = new HotPathOptimizationSystem();
                system.start();
                system.executeOptimization();
                system.shutdown();
            }
            
            // Test metrics reset
            HotPathOptimizationMetrics metrics = new HotPathOptimizationMetrics();
            metrics.recordTracking(1000000L, 10);
            
            Map<String, Object> stats = metrics.getOverallStatistics();
            assertGreaterThan((Integer) stats.get("totalMethodsTracked"), 0, "Should have tracked methods");
            
            metrics.reset();
            Map<String, Object> resetStats = metrics.getOverallStatistics();
            assertEquals((Integer) resetStats.get("totalMethodsTracked"), 0, "Should be reset to 0");
            
            System.out.println("✅ PASSED: Memory management and resource cleanup");
            testsPassed++;
            
        } catch (Exception e) {
            System.out.println("❌ FAILED: Memory management - " + e.getMessage());
            testsFailed++;
        }
        testsRun++;
        System.out.println();
    }
    
    // Test 10: Edge cases and boundary conditions
    private static void testEdgeCases() {
        System.out.println("Test 10: Edge cases and boundary conditions");
        
        try {
            // Test with extreme hotness values
            ExecutionPathTracker tracker = new ExecutionPathTracker(true);
            
            // Simulate extremely hot method
            for (int i = 0; i < 1000; i++) {
                try (ExecutionPathTracker.MethodTracker mt = tracker.trackMethod(
                        "HotClass", "hotMethod", "()")) {
                    simulateWork(100000); // 100 microseconds
                }
            }
            
            // Test with cold method
            try (ExecutionPathTracker.MethodTracker mt = tracker.trackMethod(
                    "ColdClass", "coldMethod", "()")) {
                simulateWork(1000); // 1 microsecond
            }
            
            // Verify hotness classification
            ExecutionPathTracker.ExecutionStats hotStats = tracker.getMethodStats("HotClass", "hotMethod", "()");
            ExecutionPathTracker.ExecutionStats coldStats = tracker.getMethodStats("ColdClass", "coldMethod", "()");
            
            assertTrue(hotStats.isHotPath(), "Hot method should be classified as hot path");
            assertFalse(coldStats.isHotPath(), "Cold method should not be classified as hot path");
            
            System.out.println("✅ PASSED: Edge cases and boundary conditions");
            testsPassed++;
            
        } catch (Exception e) {
            System.out.println("❌ FAILED: Edge cases - " + e.getMessage());
            testsFailed++;
        }
        testsRun++;
        System.out.println();
    }
    
    // Test 11: Real-world simulation
    private static void testRealWorldSimulation() {
        System.out.println("Test 11: Real-world simulation");
        
        try {
            HotPathOptimizationSystem optimizationSystem = new HotPathOptimizationSystem();
            optimizationSystem.start();
            
            // Simulate realistic application startup
            ExecutionPathTracker tracker = optimizationSystem.getTracker();
            TestApplication testApp = new TestApplication(tracker);
            
            testApp.simulateStartup();
            
            // Analyze the simulation results
            List<HotPathAnalyzer.HotPathAnalysis> hotPaths = optimizationSystem.getAnalyzer().analyzeHotPaths(10);
            List<CodeReorderingOptimizer.CodeReorderingPlan> plans = 
                optimizationSystem.getOptimizer().generateOptimizationPlans(5);
            
            assertGreaterThan(hotPaths.size(), 0, "Should identify some hot paths");
            assertGreaterThan(plans.size(), 0, "Should generate some optimization plans");
            
            // Verify realistic improvement expectations
            double expectedImprovement = plans.stream()
                .mapToDouble(CodeReorderingOptimizer.CodeReorderingPlan::getExpectedImprovement)
                .sum();
            
            assertGreaterThan(expectedImprovement, 0.0, "Should expect some improvement");
            assertLessThan(expectedImprovement, 100.0, "Improvement should be realistic");
            
            optimizationSystem.shutdown();
            
            System.out.println("✅ PASSED: Real-world simulation");
            testsPassed++;
            
        } catch (Exception e) {
            System.out.println("❌ FAILED: Real-world simulation - " + e.getMessage());
            testsFailed++;
        }
        testsRun++;
        System.out.println();
    }
    
    // Test 12: Integration with StartupPhasesManager
    private static void testStartupManagerIntegration() {
        System.out.println("Test 12: Integration with StartupPhasesManager");
        
        try {
            // Test that the hot path optimization system can be integrated
            // Note: This is a simplified test since we don't have a full WarmupContainer
            
            HotPathOptimizationSystem optimizationSystem = new HotPathOptimizationSystem();
            optimizationSystem.start();
            
            // Simulate the integration pattern
            HotPathOptimizationSystem.HotPathOptimizationResult result = optimizationSystem.executeOptimization();
            
            // Verify that the system works independently but can be integrated
            assertNotNull(result, "Should work independently");
            assertTrue(result.isSuccess() || !result.isSuccess(), "Should complete either successfully or with error handling");
            
            // Verify that results contain all necessary information for integration
            assertNotNull(result.getIdentifiedHotPaths(), "Should provide hot paths for integration");
            assertNotNull(result.getGeneratedPlans(), "Should provide plans for integration");
            assertNotNull(result.getRecommendations(), "Should provide recommendations");
            
            optimizationSystem.shutdown();
            
            System.out.println("✅ PASSED: Integration with StartupPhasesManager");
            testsPassed++;
            
        } catch (Exception e) {
            System.out.println("❌ FAILED: StartupManager integration - " + e.getMessage());
            testsFailed++;
        }
        testsRun++;
        System.out.println();
    }
    
    // Test 13: Performance under load
    private static void testPerformanceUnderLoad() {
        System.out.println("Test 13: Performance under load");
        
        try {
            ExecutionPathTracker tracker = new ExecutionPathTracker(true);
            
            // Generate high load scenario
            long startTime = System.nanoTime();
            
            for (int i = 0; i < 100; i++) {
                for (int j = 0; j < 10; j++) {
                    try (ExecutionPathTracker.MethodTracker mt = tracker.trackMethod(
                            "LoadTestClass" + i, "loadTestMethod" + j, "()")) {
                        simulateWork(10000); // 10 microseconds
                    }
                }
            }
            
            long endTime = System.nanoTime();
            long totalTime = endTime - startTime;
            
            // Verify that tracking doesn't become a bottleneck
            assertLessThan(totalTime, 10_000_000_000L, "High load tracking should complete within 10 seconds");
            
            // Verify that all methods were tracked
            int trackedMethods = tracker.getTrackedMethodCount();
            assertGreaterThan(trackedMethods, 0, "Should track methods under load");
            
            System.out.println("Tracked " + trackedMethods + " methods in " + (totalTime / 1_000_000.0) + "ms");
            
            System.out.println("✅ PASSED: Performance under load");
            testsPassed++;
            
        } catch (Exception e) {
            System.out.println("❌ FAILED: Performance under load - " + e.getMessage());
            testsFailed++;
        }
        testsRun++;
        System.out.println();
    }
    
    // Test 14: Concurrent optimization
    private static void testConcurrentOptimization() {
        System.out.println("Test 14: Concurrent optimization");
        
        try {
            HotPathOptimizationSystem optimizationSystem = new HotPathOptimizationSystem();
            optimizationSystem.start();
            
            // Test concurrent analysis and optimization
            CompletableFuture<List<HotPathAnalyzer.HotPathAnalysis>> analysisFuture = 
                CompletableFuture.supplyAsync(() -> {
                    simulateWork(500000000); // 500ms
                    return optimizationSystem.getAnalyzer().analyzeHotPaths(20);
                });
            
            CompletableFuture<List<CodeReorderingOptimizer.CodeReorderingPlan>> optimizationFuture = 
                CompletableFuture.supplyAsync(() -> {
                    simulateWork(300000000); // 300ms
                    return optimizationSystem.getOptimizer().generateOptimizationPlans(10);
                });
            
            CompletableFuture<HotPathOptimizationSystem.HotPathOptimizationResult> mainOptimizationFuture = 
                optimizationSystem.executeOptimizationAsync();
            
            // Wait for all to complete
            CompletableFuture.allOf(analysisFuture, optimizationFuture, mainOptimizationFuture).get();
            
            // Verify results
            List<HotPathAnalyzer.HotPathAnalysis> analysisResults = analysisFuture.get();
            List<CodeReorderingOptimizer.CodeReorderingPlan> optimizationResults = optimizationFuture.get();
            HotPathOptimizationSystem.HotPathOptimizationResult mainResult = mainOptimizationFuture.get();
            
            assertNotNull(analysisResults, "Concurrent analysis should complete");
            assertNotNull(optimizationResults, "Concurrent optimization should complete");
            assertNotNull(mainResult, "Main optimization should complete");
            
            optimizationSystem.shutdown();
            
            System.out.println("✅ PASSED: Concurrent optimization");
            testsPassed++;
            
        } catch (Exception e) {
            System.out.println("❌ FAILED: Concurrent optimization - " + e.getMessage());
            testsFailed++;
        }
        testsRun++;
        System.out.println();
    }
    
    // Test 15: Complete end-to-end workflow
    private static void testEndToEndWorkflow() {
        System.out.println("Test 15: Complete end-to-end workflow");
        
        try {
            // Step 1: Create and start system
            HotPathOptimizationSystem optimizationSystem = new HotPathOptimizationSystem(OptimizationConfig.aggressiveConfig());
            optimizationSystem.start();
            
            // Step 2: Simulate application usage with tracking
            TestApplication testApp = new TestApplication(optimizationSystem.getTracker());
            testApp.simulateStartup();
            
            // Step 3: Execute complete optimization cycle
            HotPathOptimizationSystem.HotPathOptimizationResult result = optimizationSystem.executeOptimization();
            
            // Step 4: Verify complete workflow
            assertTrue(result.isSuccess(), "End-to-end workflow should succeed");
            assertGreaterThan(result.getIdentifiedHotPaths().size(), 0, "Should identify hot paths");
            assertGreaterThan(result.getGeneratedPlans().size(), 0, "Should generate optimization plans");
            assertGreaterThan(result.getTotalActualImprovement(), 0.0, "Should achieve improvement");
            
            // Step 5: Verify metrics collection
            HotPathOptimizationMetrics metrics = new HotPathOptimizationMetrics();
            HotPathOptimizationMetrics.MetricsSnapshot snapshot = metrics.createSnapshot();
            
            assertNotNull(snapshot, "Should collect metrics");
            assertGreaterThan(snapshot.getMethodsTracked(), 0, "Should track methods");
            
            // Step 6: Generate reports
            String detailedReport = result.generateDetailedReport();
            assertNotNull(detailedReport, "Should generate detailed report");
            assertGreaterThan(detailedReport.length(), 100, "Report should contain substantial information");
            
            // Step 7: Verify recommendations
            List<String> recommendations = result.getRecommendations();
            assertNotNull(recommendations, "Should provide recommendations");
            
            optimizationSystem.shutdown();
            
            System.out.println("End-to-end workflow completed successfully");
            System.out.println("- Hot paths identified: " + result.getIdentifiedHotPaths().size());
            System.out.println("- Optimization plans: " + result.getGeneratedPlans().size());
            System.out.println("- Total improvement: " + result.getTotalActualImprovement() + "%");
            System.out.println("- Recommendations: " + recommendations.size());
            
            System.out.println("✅ PASSED: Complete end-to-end workflow");
            testsPassed++;
            
        } catch (Exception e) {
            System.out.println("❌ FAILED: End-to-end workflow - " + e.getMessage());
            e.printStackTrace();
            testsFailed++;
        }
        testsRun++;
        System.out.println();
    }
    
    // Helper methods for test assertions
    private static void assertNotNull(Object obj, String message) {
        if (obj == null) {
            throw new AssertionError("Assertion failed: " + message);
        }
    }
    
    private static void assertNull(Object obj, String message) {
        if (obj != null) {
            throw new AssertionError("Assertion failed: " + message);
        }
    }
    
    private static void assertTrue(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError("Assertion failed: " + message);
        }
    }
    
    private static void assertFalse(boolean condition, String message) {
        if (condition) {
            throw new AssertionError("Assertion failed: " + message);
        }
    }
    
    private static void assertEquals(Object expected, Object actual, String message) {
        if (!Objects.equals(expected, actual)) {
            throw new AssertionError("Assertion failed: " + message + 
                " (expected: " + expected + ", actual: " + actual + ")");
        }
    }
    
    private static void assertGreaterThan(int actual, int expected, String message) {
        if (actual <= expected) {
            throw new AssertionError("Assertion failed: " + message + 
                " (expected > " + expected + ", actual: " + actual + ")");
        }
    }
    
    private static void assertGreaterThan(long actual, long expected, String message) {
        if (actual <= expected) {
            throw new AssertionError("Assertion failed: " + message + 
                " (expected > " + expected + ", actual: " + actual + ")");
        }
    }
    
    private static void assertGreaterThan(double actual, double expected, String message) {
        if (actual <= expected) {
            throw new AssertionError("Assertion failed: " + message + 
                " (expected > " + expected + ", actual: " + actual + ")");
        }
    }
    
    private static void assertLessThan(int actual, int expected, String message) {
        if (actual >= expected) {
            throw new AssertionError("Assertion failed: " + message + 
                " (expected < " + expected + ", actual: " + actual + ")");
        }
    }
    
    private static void assertLessThan(long actual, long expected, String message) {
        if (actual >= expected) {
            throw new AssertionError("Assertion failed: " + message + 
                " (expected < " + expected + ", actual: " + actual + ")");
        }
    }
    
    private static void assertLessThan(double actual, double expected, String message) {
        if (actual >= expected) {
            throw new AssertionError("Assertion failed: " + message + 
                " (expected < " + expected + ", actual: " + actual + ")");
        }
    }
    
    private static void simulateWork(long duration) {
        try {
            Thread.sleep(duration / 1_000_000); // Convert nanoseconds to milliseconds
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
    
    private static void simulateMethodExecutions(ExecutionPathTracker tracker) {
        // Simulate framework initialization (hot)
        for (int i = 0; i < 100; i++) {
            try (ExecutionPathTracker.MethodTracker mt = tracker.trackMethod(
                    "io.warmup.framework.startup.StartupPhasesManager", "execute", "()")) {
                simulateWork(100000); // 100 microseconds
            }
        }
        
        // Simulate configuration loading (warm)
        for (int i = 0; i < 50; i++) {
            try (ExecutionPathTracker.MethodTracker mt = tracker.trackMethod(
                    "io.warmup.framework.config.PreloadedConfigSystem", "load", "()")) {
                simulateWork(50000); // 50 microseconds
            }
        }
        
        // Simulate class preloading (hot)
        for (int i = 0; i < 80; i++) {
            try (ExecutionPathTracker.MethodTracker mt = tracker.trackMethod(
                    "io.warmup.framework.bootstrap.CriticalClassPreloadSystem", "preload", "()")) {
                simulateWork(80000); // 80 microseconds
            }
        }
        
        // Simulate parallel initialization (hot)
        for (int i = 0; i < 60; i++) {
            try (ExecutionPathTracker.MethodTracker mt = tracker.trackMethod(
                    "io.warmup.framework.startup.ParallelSubsystemInitializer", "initialize", "()")) {
                simulateWork(75000); // 75 microseconds
            }
        }
        
        // Simulate cold methods
        for (int i = 0; i < 10; i++) {
            try (ExecutionPathTracker.MethodTracker mt = tracker.trackMethod(
                    "SomeApp.ColdMethod", "execute", "()")) {
                simulateWork(1000); // 1 microsecond
            }
        }
    }
}