package io.warmup.examples.startup.hotpath.examples;

import io.warmup.framework.startup.hotpath.*;
import io.warmup.framework.startup.hotpath.HotPathOptimizationSystem.OptimizationConfig;
import io.warmup.framework.startup.hotpath.HotPathOptimizationSystem.RiskTolerance;
import io.warmup.framework.startup.hotpath.HotPathOptimizationSystem.OptimizationStrategy;

import java.util.*;
import java.util.concurrent.*;
import java.time.Duration;
import java.time.Instant;

/**
 * Comprehensive examples demonstrating the Hot Path Optimization System.
 * Shows various usage patterns, configurations, and integration scenarios.
 * 
 * @author MiniMax Agent
 * @version 1.0
 */
public class HotPathOptimizationExample {
    
    /**
     * Simulated application startup class for demonstration.
     */
    public static class DemoApplication {
        private final ExecutionPathTracker tracker;
        
        public DemoApplication(ExecutionPathTracker tracker) {
            this.tracker = tracker;
        }
        
        public void startApplication() {
            System.out.println("=== Starting Demo Application ===");
            
            // Simulate critical startup phase
            try (ExecutionPathTracker.MethodTracker mt = tracker.trackMethod(
                    "DemoApplication", "initializeFramework", "()")) {
                initializeFramework();
            }
            
            try (ExecutionPathTracker.MethodTracker mt = tracker.trackMethod(
                    "DemoApplication", "loadConfiguration", "()")) {
                loadConfiguration();
            }
            
            try (ExecutionPathTracker.MethodTracker mt = tracker.trackMethod(
                    "DemoApplication", "initializeServices", "()")) {
                initializeServices();
            }
            
            // Simulate background tasks
            backgroundInitialization();
            
            System.out.println("=== Demo Application Started ===");
        }
        
        private void initializeFramework() {
            simulateWork(100); // 100ms
            System.out.println("Framework initialized");
        }
        
        private void loadConfiguration() {
            simulateWork(50); // 50ms
            System.out.println("Configuration loaded");
        }
        
        private void initializeServices() {
            simulateWork(200); // 200ms
            System.out.println("Services initialized");
        }
        
        private void backgroundInitialization() {
            simulateWork(300); // 300ms
            System.out.println("Background initialization completed");
        }
        
        private void simulateWork(long duration) {
            try {
                Thread.sleep(duration);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }
    
    /**
     * Example 1: Basic Hot Path Optimization Usage
     */
    public static void example1BasicUsage() {
        System.out.println("\n=== Example 1: Basic Hot Path Optimization ===");
        
        try {
            // Create the optimization system with default configuration
            HotPathOptimizationSystem optimizationSystem = new HotPathOptimizationSystem();
            
            // Start the system
            optimizationSystem.start();
            
            // Run the optimization cycle
            HotPathOptimizationSystem.HotPathOptimizationResult result = optimizationSystem.executeOptimization();
            
            // Display results
            System.out.println("Optimization completed:");
            System.out.println(result.generateDetailedReport());
            
            // Show improvement metrics
            System.out.printf("Total improvement: %.2f%%\n", result.getTotalActualImprovement());
            System.out.printf("Efficiency: %.2f%%\n", result.getImprovementEfficiency() * 100);
            
            // Shutdown
            optimizationSystem.shutdown();
            
        } catch (Exception e) {
            System.err.println("Error in basic usage example: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Example 2: Aggressive Optimization Configuration
     */
    public static void example2AggressiveOptimization() {
        System.out.println("\n=== Example 2: Aggressive Optimization Configuration ===");
        
        try {
            // Create aggressive configuration
            OptimizationConfig aggressiveConfig = OptimizationConfig.aggressiveConfig();
            
            HotPathOptimizationSystem optimizationSystem = new HotPathOptimizationSystem(aggressiveConfig);
            optimizationSystem.start();
            
            // Execute optimization
            HotPathOptimizationSystem.HotPathOptimizationResult result = optimizationSystem.executeOptimization();
            
            System.out.println("Aggressive optimization results:");
            System.out.println(result.generateDetailedReport());
            
            optimizationSystem.shutdown();
            
        } catch (Exception e) {
            System.err.println("Error in aggressive optimization example: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Example 3: Custom Configuration with Conservative Risk Tolerance
     */
    public static void example3CustomConfiguration() {
        System.out.println("\n=== Example 3: Custom Conservative Configuration ===");
        
        try {
            // Create custom configuration
            OptimizationConfig customConfig = new OptimizationConfig(
                Duration.ofMinutes(1),           // trackingDuration
                15,                              // maxHotPaths
                3,                               // maxOptimizationPlans
                40.0,                            // minHotnessThreshold
                false,                           // autoApplyOptimizations
                false,                           // enableAggressiveOptimization
                Duration.ofSeconds(30),          // analysisTimeout
                20,                              // minMethodCallCount
                true,                            // enableParallelAnalysis
                RiskTolerance.CONSERVATIVE,      // riskTolerance
                OptimizationStrategy.CONSERVATIVE // optimizationStrategy
            );
            
            HotPathOptimizationSystem optimizationSystem = new HotPathOptimizationSystem(customConfig);
            optimizationSystem.start();
            
            // Execute with manual optimization application
            HotPathOptimizationSystem.HotPathOptimizationResult result = optimizationSystem.executeOptimization();
            
            System.out.println("Custom configuration results:");
            System.out.println(result.generateDetailedReport());
            
            // Show recommendations
            System.out.println("\nRecommendations:");
            result.getRecommendations().forEach(rec -> System.out.println("- " + rec));
            
            optimizationSystem.shutdown();
            
        } catch (Exception e) {
            System.err.println("Error in custom configuration example: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Example 4: Real-time Tracking and Analysis
     */
    public static void example4RealTimeTracking() {
        System.out.println("\n=== Example 4: Real-time Tracking and Analysis ===");
        
        try {
            HotPathOptimizationSystem optimizationSystem = new HotPathOptimizationSystem();
            
            // Start tracking
            optimizationSystem.start();
            ExecutionPathTracker tracker = optimizationSystem.getTracker();
            
            // Simulate real application usage
            DemoApplication demoApp = new DemoApplication(tracker);
            
            System.out.println("Running application with real-time tracking...");
            demoApp.startApplication();
            
            // Analyze current state
            List<HotPathAnalyzer.HotPathAnalysis> hotPaths = optimizationSystem.getAnalyzer().analyzeHotPaths(10);
            
            System.out.println("\nHot paths identified during execution:");
            hotPaths.stream()
                .limit(5)
                .forEach(path -> System.out.println("- " + path.toString()));
            
            // Generate optimization plans
            List<CodeReorderingOptimizer.CodeReorderingPlan> plans = 
                optimizationSystem.getOptimizer().generateOptimizationPlans(5);
            
            System.out.println("\nOptimization plans generated:");
            plans.forEach(plan -> System.out.println("- " + plan.toString()));
            
            optimizationSystem.shutdown();
            
        } catch (Exception e) {
            System.err.println("Error in real-time tracking example: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Example 5: Metrics and Performance Analysis
     */
    public static void example5MetricsAnalysis() {
        System.out.println("\n=== Example 5: Metrics and Performance Analysis ===");
        
        try {
            HotPathOptimizationSystem optimizationSystem = new HotPathOptimizationSystem();
            optimizationSystem.start();
            
            // Run multiple optimization cycles to build metrics
            for (int i = 0; i < 3; i++) {
                System.out.println("Running optimization cycle " + (i + 1));
                optimizationSystem.executeOptimization();
                
                // Small delay between cycles
                Thread.sleep(1000);
            }
            
            // Analyze metrics
            HotPathOptimizationMetrics metrics = new HotPathOptimizationMetrics();
            HotPathOptimizationMetrics.MetricsSnapshot snapshot = metrics.createSnapshot();
            
            System.out.println("\nCurrent metrics snapshot:");
            System.out.println(snapshot.toString());
            System.out.println(snapshot.getPerformanceStats().toDetailedString());
            System.out.println(snapshot.getResourceStats().toDetailedString());
            
            // Perform trend analysis
            HotPathOptimizationMetrics.TrendAnalysis trendAnalysis = 
                metrics.performTrendAnalysis(Duration.ofMinutes(10));
            
            System.out.println("\n" + trendAnalysis.generateReport());
            
            optimizationSystem.shutdown();
            
        } catch (Exception e) {
            System.err.println("Error in metrics analysis example: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Example 6: Integration with StartupPhasesManager
     */
    public static void example6StartupManagerIntegration() {
        System.out.println("\n=== Example 6: StartupPhasesManager Integration ===");
        
        try {
            // This example demonstrates how Hot Path Optimization integrates
            // with the existing StartupPhasesManager system
            
            System.out.println("Simulating integration with StartupPhasesManager...");
            
            // Create optimization system
            HotPathOptimizationSystem optimizationSystem = new HotPathOptimizationSystem();
            optimizationSystem.start();
            
            // Simulate the integration workflow
            System.out.println("\n1. Initial tracking phase:");
            // In real implementation, this would integrate with actual startup phases
            
            System.out.println("2. Hot path analysis phase:");
            List<HotPathAnalyzer.HotPathAnalysis> hotPaths = optimizationSystem.getAnalyzer().analyzeHotPaths(20);
            System.out.println("   Found " + hotPaths.size() + " hot paths");
            
            System.out.println("3. Optimization planning phase:");
            List<CodeReorderingOptimizer.CodeReorderingPlan> plans = 
                optimizationSystem.getOptimizer().generateOptimizationPlans(10);
            System.out.println("   Generated " + plans.size() + " optimization plans");
            
            System.out.println("4. Integration recommendations:");
            List<String> integrationRecommendations = generateIntegrationRecommendations(hotPaths, plans);
            integrationRecommendations.forEach(rec -> System.out.println("   - " + rec));
            
            optimizationSystem.shutdown();
            
        } catch (Exception e) {
            System.err.println("Error in startup manager integration example: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Example 7: Multi-threaded Optimization Analysis
     */
    public static void example7MultiThreadedAnalysis() {
        System.out.println("\n=== Example 7: Multi-threaded Analysis ===");
        
        try {
            HotPathOptimizationSystem optimizationSystem = new HotPathOptimizationSystem();
            optimizationSystem.start();
            
            // Simulate concurrent optimization tasks
            CompletableFuture<HotPathOptimizationSystem.HotPathOptimizationResult> task1 = 
                optimizationSystem.executeOptimizationAsync();
            
            CompletableFuture<List<HotPathAnalyzer.HotPathAnalysis>> task2 = 
                CompletableFuture.supplyAsync(() -> {
                    try {
                        return optimizationSystem.getAnalyzer().analyzeHotPaths(15);
                    } catch (Exception e) {
                        return Collections.emptyList();
                    }
                });
            
            CompletableFuture<List<CodeReorderingOptimizer.CodeReorderingPlan>> task3 = 
                CompletableFuture.supplyAsync(() -> {
                    try {
                        return optimizationSystem.getOptimizer().generateOptimizationPlans(8);
                    } catch (Exception e) {
                        return Collections.emptyList();
                    }
                });
            
            // Wait for all tasks
            HotPathOptimizationSystem.HotPathOptimizationResult mainResult = task1.get();
            List<HotPathAnalyzer.HotPathAnalysis> hotPaths = task2.get();
            List<CodeReorderingOptimizer.CodeReorderingPlan> plans = task3.get();
            
            System.out.println("Multi-threaded analysis completed:");
            System.out.println("- Main optimization result: " + mainResult.isSuccess());
            System.out.println("- Hot paths found: " + hotPaths.size());
            System.out.println("- Optimization plans: " + plans.size());
            
            optimizationSystem.shutdown();
            
        } catch (Exception e) {
            System.err.println("Error in multi-threaded analysis example: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Helper method to generate integration recommendations
     */
    private static List<String> generateIntegrationRecommendations(
            List<HotPathAnalyzer.HotPathAnalysis> hotPaths,
            List<CodeReorderingOptimizer.CodeReorderingPlan> plans) {
        
        List<String> recommendations = new ArrayList<>();
        
        // Check if hot paths should be moved to critical phase
        long criticalHotPaths = hotPaths.stream()
            .filter(path -> path.getHotnessLevel() == HotnessLevel.EXTREMELY_HOT || 
                          path.getHotnessLevel() == HotnessLevel.VERY_HOT)
            .count();
        
        if (criticalHotPaths > 0) {
            recommendations.add(String.format("Move %d hot paths to critical startup phase for maximum parallelism", criticalHotPaths));
        }
        
        // Check for parallel execution opportunities
        long parallelPlans = plans.stream()
            .filter(plan -> plan.getActions().stream()
                .anyMatch(action -> action.getType() == CodeReorderingOptimizer.OptimizationType.PARALLEL_EXECUTION))
            .count();
        
        if (parallelPlans > 0) {
            recommendations.add(String.format("Implement parallel execution for %d optimization plans", parallelPlans));
        }
        
        // Check for background execution opportunities
        long backgroundPlans = plans.stream()
            .filter(plan -> plan.getActions().stream()
                .anyMatch(action -> action.getType() == CodeReorderingOptimizer.OptimizationType.PHASE_REORDERING))
            .count();
        
        if (backgroundPlans > 0) {
            recommendations.add(String.format("Move %d methods to background phase for better startup sequence", backgroundPlans));
        }
        
        return recommendations;
    }
    
    /**
     * Main method to run all examples
     */
    public static void main(String[] args) {
        System.out.println("=== Hot Path Optimization System - Comprehensive Examples ===");
        System.out.println("This demonstrates the sixth optimization system for the Warmup Framework");
        System.out.println("Using real execution data to reorder startup code for optimal performance");
        
        try {
            // Run all examples
            example1BasicUsage();
            example2AggressiveOptimization();
            example3CustomConfiguration();
            example4RealTimeTracking();
            example5MetricsAnalysis();
            example6StartupManagerIntegration();
            example7MultiThreadedAnalysis();
            
            System.out.println("\n=== All Examples Completed Successfully ===");
            System.out.println("Hot Path Optimization System demonstration complete!");
            
        } catch (Exception e) {
            System.err.println("Error running examples: " + e.getMessage());
            e.printStackTrace();
        }
    }
}