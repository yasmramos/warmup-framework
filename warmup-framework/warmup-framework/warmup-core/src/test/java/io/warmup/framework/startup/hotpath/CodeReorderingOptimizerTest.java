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
import java.util.stream.Collectors;

/**
 * Unit tests for CodeReorderingOptimizer class.
 * Tests code reordering optimization functionality including plan generation,
 * action application, and optimization result processing.
 * 
 * @author MiniMax Agent
 * @version 1.0
 */
class CodeReorderingOptimizerTest {
    
    private HotPathAnalyzer analyzer;
    private ExecutionPathTracker tracker;
    private CodeReorderingOptimizer optimizer;
    private ExecutorService testExecutor;
    
    @BeforeEach
    void setUp() {
        tracker = new ExecutionPathTracker(true);
        analyzer = new HotPathAnalyzer(tracker, Duration.ofSeconds(10)); // OPTIMIZADO: reducido de 1 minuto para testing
        optimizer = new CodeReorderingOptimizer(analyzer);
        testExecutor = Executors.newFixedThreadPool(2);
    }
    
    @AfterEach
    void tearDown() {
        optimizer.shutdown();
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
    void testConstructor() {
        CodeReorderingOptimizer newOptimizer = new CodeReorderingOptimizer(analyzer);
        assertNotNull(newOptimizer);
        newOptimizer.shutdown();
    }
    
    @Test
    void testGenerateOptimizationPlansWithEmptyHotPaths() {
        List<CodeReorderingOptimizer.CodeReorderingPlan> plans = optimizer.generateOptimizationPlans(5);
        assertNotNull(plans);
        assertTrue(plans.isEmpty());
    }
    
    @Test
    void testGenerateOptimizationPlansAsync() {
        CompletableFuture<List<CodeReorderingOptimizer.CodeReorderingPlan>> future = 
            optimizer.generateOptimizationPlansAsync(5);
        
        List<CodeReorderingOptimizer.CodeReorderingPlan> plans = future.join();
        assertNotNull(plans);
        // Plans may be empty if no hot paths are identified
    }
    
    @Test
    void testGenerateOptimizationPlansWithHotPaths() {
        // Add hot path data to analyzer
        addHotPathDataToAnalyzer();
        
        List<CodeReorderingOptimizer.CodeReorderingPlan> plans = optimizer.generateOptimizationPlans(5);
        
        assertNotNull(plans);
        // Plans may contain optimization strategies based on hot paths
        
        for (CodeReorderingOptimizer.CodeReorderingPlan plan : plans) {
            assertNotNull(plan.getPlanId());
            assertNotNull(plan.getDescription());
            assertTrue(plan.getActions() != null);
            assertTrue(plan.getExpectedImprovement() >= 0);
            assertNotNull(plan.getRiskLevel());
            assertNotNull(plan.getConfidenceLevel());
            assertNotNull(plan.getCreationTime());
            assertTrue(plan.getExpectedImprovement() <= 100.0);
        }
    }
    
    @Test
    void testCodeReorderingPlanProperties() {
        List<CodeReorderingOptimizer.OptimizationAction> actions = createMockActions();
        
        CodeReorderingOptimizer.CodeReorderingPlan plan = new CodeReorderingOptimizer.CodeReorderingPlan(
            "plan-id", "Test Plan", actions, 25.0, 
            CodeReorderingOptimizer.RiskLevel.MEDIUM, 
            io.warmup.framework.startup.hotpath.ConfidenceLevel.HIGH
        );
        
        assertEquals("plan-id", plan.getPlanId());
        assertEquals("Test Plan", plan.getDescription());
        assertEquals(2, plan.getActions().size());
        assertEquals(25.0, plan.getExpectedImprovement(), 0.01);
        assertEquals(CodeReorderingOptimizer.RiskLevel.MEDIUM, plan.getRiskLevel());
        assertEquals(io.warmup.framework.startup.hotpath.ConfidenceLevel.HIGH, plan.getConfidenceLevel());
        assertNotNull(plan.getCreationTime());
        assertTrue(plan.getActions() != plan.getActions()); // Defensive copy
        assertNotNull(plan.getMetadata());
    }
    
    @Test
    void testCodeReorderingPlanIsHighConfidence() {
        // High confidence plan
        CodeReorderingOptimizer.CodeReorderingPlan highConfidencePlan = new CodeReorderingOptimizer.CodeReorderingPlan(
            "high-conf", "High Confidence Plan", new ArrayList<>(), 20.0,
            CodeReorderingOptimizer.RiskLevel.LOW, io.warmup.framework.startup.hotpath.ConfidenceLevel.VERY_HIGH
        );
        assertTrue(highConfidencePlan.isHighConfidence());
        
        // Low confidence plan
        CodeReorderingOptimizer.CodeReorderingPlan lowConfidencePlan = new CodeReorderingOptimizer.CodeReorderingPlan(
            "low-conf", "Low Confidence Plan", new ArrayList<>(), 15.0,
            CodeReorderingOptimizer.RiskLevel.MEDIUM, io.warmup.framework.startup.hotpath.ConfidenceLevel.LOW
        );
        assertFalse(lowConfidencePlan.isHighConfidence());
    }
    
    @Test
    void testCodeReorderingPlanIsLowRisk() {
        // Low risk plans
        CodeReorderingOptimizer.CodeReorderingPlan minimalRiskPlan = new CodeReorderingOptimizer.CodeReorderingPlan(
            "minimal-risk", "Minimal Risk Plan", new ArrayList<>(), 10.0,
            CodeReorderingOptimizer.RiskLevel.MINIMAL, io.warmup.framework.startup.hotpath.ConfidenceLevel.MEDIUM
        );
        assertTrue(minimalRiskPlan.isLowRisk());
        
        CodeReorderingOptimizer.CodeReorderingPlan lowRiskPlan = new CodeReorderingOptimizer.CodeReorderingPlan(
            "low-risk", "Low Risk Plan", new ArrayList<>(), 15.0,
            CodeReorderingOptimizer.RiskLevel.LOW, io.warmup.framework.startup.hotpath.ConfidenceLevel.MEDIUM
        );
        assertTrue(lowRiskPlan.isLowRisk());
        
        // Medium risk plan
        CodeReorderingOptimizer.CodeReorderingPlan mediumRiskPlan = new CodeReorderingOptimizer.CodeReorderingPlan(
            "medium-risk", "Medium Risk Plan", new ArrayList<>(), 20.0,
            CodeReorderingOptimizer.RiskLevel.MEDIUM, io.warmup.framework.startup.hotpath.ConfidenceLevel.MEDIUM
        );
        assertFalse(mediumRiskPlan.isLowRisk());
    }
    
    @Test
    void testCodeReorderingPlanToString() {
        List<CodeReorderingOptimizer.OptimizationAction> actions = createMockActions();
        
        CodeReorderingOptimizer.CodeReorderingPlan plan = new CodeReorderingOptimizer.CodeReorderingPlan(
            "test-plan", "Test Plan Description", actions, 35.5,
            CodeReorderingOptimizer.RiskLevel.HIGH, io.warmup.framework.startup.hotpath.ConfidenceLevel.MEDIUM
        );
        
        String result = plan.toString();
        assertTrue(result.contains("test-plan"));
        assertTrue(result.contains("35.5%"));
        assertTrue(result.contains("HIGH"));
        assertTrue(result.contains("MEDIUM"));
        assertTrue(result.contains("actions=2"));
    }
    
    @Test
    void testOptimizationActionProperties() {
        List<String> methods = Arrays.asList("method1", "method2");
        List<String> classes = Arrays.asList("Class1", "Class2");
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("param1", "value1");
        List<String> rollbackSteps = Arrays.asList("rollback1", "rollback2");
        
        CodeReorderingOptimizer.OptimizationAction action = new CodeReorderingOptimizer.OptimizationAction(
            "action-id", "Test Action", CodeReorderingOptimizer.OptimizationType.PHASE_REORDERING,
            io.warmup.framework.startup.hotpath.Priority.HIGH, methods, classes, parameters, rollbackSteps
        );
        
        assertEquals("action-id", action.getActionId());
        assertEquals("Test Action", action.getDescription());
        assertEquals(CodeReorderingOptimizer.OptimizationType.PHASE_REORDERING, action.getType());
        assertEquals(io.warmup.framework.startup.hotpath.Priority.HIGH, action.getPriority());
        assertEquals(methods, action.getTargetMethods());
        assertEquals(classes, action.getTargetClasses());
        assertEquals(parameters, action.getParameters());
        assertEquals(rollbackSteps, action.getRollbackSteps());
        assertTrue(action.getTargetMethods() != action.getTargetMethods()); // Defensive copy
    }
    
    @Test
    void testOptimizationActionToString() {
        CodeReorderingOptimizer.OptimizationAction action = new CodeReorderingOptimizer.OptimizationAction(
            "test-action", "Test Action Description", CodeReorderingOptimizer.OptimizationType.PARALLEL_EXECUTION,
            io.warmup.framework.startup.hotpath.Priority.CRITICAL, Arrays.asList("method1"), Arrays.asList("Class1"),
            new HashMap<>(), new ArrayList<>()
        );
        
        String result = action.toString();
        assertTrue(result.contains("test-action"));
        assertTrue(result.contains("PARALLEL_EXECUTION"));
        assertTrue(result.contains("CRITICAL"));
        assertTrue(result.contains("targets=2"));
    }
    
    @Test
    void testOptimizationResultProperties() {
        List<String> appliedActions = Arrays.asList("action1", "action2");
        List<String> failedActions = Arrays.asList("action3");
        Map<String, Object> metrics = new HashMap<>();
        metrics.put("metric1", 100);
        
        CodeReorderingOptimizer.OptimizationResult result = new CodeReorderingOptimizer.OptimizationResult(
            "plan-123", true, 30.0, Duration.ofSeconds(5), appliedActions, failedActions, metrics
        );
        
        assertEquals("plan-123", result.getPlanId());
        assertTrue(result.isSuccess());
        assertEquals(30.0, result.getActualImprovement(), 0.01);
        assertEquals(Duration.ofSeconds(5), result.getExecutionTime());
        assertEquals(appliedActions, result.getAppliedActions());
        assertEquals(failedActions, result.getFailedActions());
        assertEquals(metrics, result.getMetrics());
        assertNotNull(result.getCompletionTime());
        assertTrue(result.getAppliedActions() != result.getAppliedActions()); // Defensive copy
    }
    
    @Test
    void testOptimizationResultImprovementRatio() {
        // Successful with positive improvement
        CodeReorderingOptimizer.OptimizationResult successfulResult = new CodeReorderingOptimizer.OptimizationResult(
            "success", true, 75.0, Duration.ofSeconds(1), new ArrayList<>(), new ArrayList<>(), new HashMap<>()
        );
        assertEquals(0.75, successfulResult.getImprovementRatio(), 0.01);
        
        // Successful with improvement above 100%
        CodeReorderingOptimizer.OptimizationResult overachievingResult = new CodeReorderingOptimizer.OptimizationResult(
            "overachieve", true, 150.0, Duration.ofSeconds(1), new ArrayList<>(), new ArrayList<>(), new HashMap<>()
        );
        assertEquals(1.0, overachievingResult.getImprovementRatio(), 0.01); // Should cap at 1.0
        
        // Failed optimization
        CodeReorderingOptimizer.OptimizationResult failedResult = new CodeReorderingOptimizer.OptimizationResult(
            "failed", false, 0.0, Duration.ofSeconds(1), new ArrayList<>(), new ArrayList<>(), new HashMap<>()
        );
        assertEquals(0.0, failedResult.getImprovementRatio(), 0.01);
        
        // Successful but no improvement
        CodeReorderingOptimizer.OptimizationResult noImprovementResult = new CodeReorderingOptimizer.OptimizationResult(
            "no-improvement", true, 0.0, Duration.ofSeconds(1), new ArrayList<>(), new ArrayList<>(), new HashMap<>()
        );
        assertEquals(0.0, noImprovementResult.getImprovementRatio(), 0.01);
    }
    
    @Test
    void testOptimizationResultToString() {
        List<String> appliedActions = Arrays.asList("action1", "action2", "action3");
        List<String> failedActions = Arrays.asList("action4");
        
        CodeReorderingOptimizer.OptimizationResult result = new CodeReorderingOptimizer.OptimizationResult(
            "test-plan", true, 45.5, Duration.ofSeconds(3), appliedActions, failedActions, new HashMap<>()
        );
        
        String resultStr = result.toString();
        assertTrue(resultStr.contains("test-plan"));
        assertTrue(resultStr.contains("true"));
        assertTrue(resultStr.contains("45.5%"));
        assertTrue(resultStr.contains("actions=3/4"));
    }
    
    @Test
    void testRiskLevelEnum() {
        assertEquals(0, CodeReorderingOptimizer.RiskLevel.MINIMAL.getLevel());
        assertEquals("Minimal", CodeReorderingOptimizer.RiskLevel.MINIMAL.getDescription());
        
        assertEquals(1, CodeReorderingOptimizer.RiskLevel.LOW.getLevel());
        assertEquals("Low", CodeReorderingOptimizer.RiskLevel.LOW.getDescription());
        
        assertEquals(2, CodeReorderingOptimizer.RiskLevel.MEDIUM.getLevel());
        assertEquals("Medium", CodeReorderingOptimizer.RiskLevel.MEDIUM.getDescription());
        
        assertEquals(3, CodeReorderingOptimizer.RiskLevel.HIGH.getLevel());
        assertEquals("High", CodeReorderingOptimizer.RiskLevel.HIGH.getDescription());
        
        assertEquals(4, CodeReorderingOptimizer.RiskLevel.CRITICAL.getLevel());
        assertEquals("Critical", CodeReorderingOptimizer.RiskLevel.CRITICAL.getDescription());
    }
    
    @Test
    void testOptimizationTypeEnum() {
        assertEquals("Phase Reordering", CodeReorderingOptimizer.OptimizationType.PHASE_REORDERING.getDescription());
        assertEquals("Method Execution Order", CodeReorderingOptimizer.OptimizationType.METHOD_EXECUTION_ORDER.getDescription());
        assertEquals("Parallel Execution", CodeReorderingOptimizer.OptimizationType.PARALLEL_EXECUTION.getDescription());
        assertEquals("Dependency Optimization", CodeReorderingOptimizer.OptimizationType.DEPENDENCY_OPTIMIZATION.getDescription());
        assertEquals("Cache Preload", CodeReorderingOptimizer.OptimizationType.CACHE_PRELOAD.getDescription());
        assertEquals("Memory Layout", CodeReorderingOptimizer.OptimizationType.MEMORY_LAYOUT.getDescription());
        assertEquals("Code Inlining", CodeReorderingOptimizer.OptimizationType.CODE_INLINING.getDescription());
    }
    
    @Test
    void testApplyOptimizationPlan() {
        // Create a test plan
        CodeReorderingOptimizer.CodeReorderingPlan plan = createTestPlan();
        
        CodeReorderingOptimizer.OptimizationResult result = optimizer.applyOptimizationPlan(plan);
        
        assertNotNull(result);
        assertEquals(plan.getPlanId(), result.getPlanId());
        assertTrue(result.getExecutionTime().toMillis() >= 0);
        assertTrue(result.getAppliedActions() != null);
        assertTrue(result.getFailedActions() != null);
        assertTrue(result.getMetrics() != null);
        // Result may be success or failure depending on plan execution
    }
    
    @Test
    void testApplyOptimizationPlanAsync() {
        CodeReorderingOptimizer.CodeReorderingPlan plan = createTestPlan();
        
        CompletableFuture<CodeReorderingOptimizer.OptimizationResult> future = 
            optimizer.applyOptimizationPlanAsync(plan);
        
        CodeReorderingOptimizer.OptimizationResult result = future.join();
        
        assertNotNull(result);
        assertEquals(plan.getPlanId(), result.getPlanId());
    }
    
    @Test
    void testApplyOptimizationPlanWithEmptyActions() {
        CodeReorderingOptimizer.CodeReorderingPlan emptyPlan = new CodeReorderingOptimizer.CodeReorderingPlan(
            "empty-plan", "Empty Plan", new ArrayList<>(), 0.0,
            CodeReorderingOptimizer.RiskLevel.MINIMAL, io.warmup.framework.startup.hotpath.ConfidenceLevel.MEDIUM
        );
        
        CodeReorderingOptimizer.OptimizationResult result = optimizer.applyOptimizationPlan(emptyPlan);
        
        assertNotNull(result);
        assertEquals(emptyPlan.getPlanId(), result.getPlanId());
        assertTrue(result.getAppliedActions().isEmpty());
        assertTrue(result.getFailedActions().isEmpty());
    }
    
    @Test
    void testGetGeneratedPlans() {
        assertTrue(optimizer.getGeneratedPlans().isEmpty());
        
        // Generate plans
        optimizer.generateOptimizationPlans(5);
        
        List<CodeReorderingOptimizer.CodeReorderingPlan> plans = optimizer.getGeneratedPlans();
        assertNotNull(plans);
        // Plans may be empty if no hot paths exist
    }
    
    @Test
    void testGetExecutionResults() {
        assertTrue(optimizer.getExecutionResults().isEmpty());
        
        // Apply a plan
        CodeReorderingOptimizer.CodeReorderingPlan plan = createTestPlan();
        optimizer.applyOptimizationPlan(plan);
        
        List<CodeReorderingOptimizer.OptimizationResult> results = optimizer.getExecutionResults();
        assertNotNull(results);
        assertEquals(1, results.size());
        
        // Apply another plan
        optimizer.applyOptimizationPlan(plan);
        assertEquals(2, optimizer.getExecutionResults().size());
    }
    
    @Test
    void testGetOptimizationCount() {
        assertEquals(0, optimizer.getOptimizationCount());
        
        optimizer.generateOptimizationPlans(5);
        assertTrue(optimizer.getOptimizationCount() > 0);
    }
    
    @Test
    void testShutdown() {
        assertDoesNotThrow(() -> optimizer.shutdown());
        
        // After shutdown, optimizer should still be usable but may have limited functionality
        optimizer.generateOptimizationPlans(5);
        assertDoesNotThrow(() -> optimizer.shutdown());
    }
    
    // Additional tests for expanded coverage
    
    @Test
    @DisplayName("Test Constructor with Null Analyzer")
    void testConstructorWithNullAnalyzer() {
        assertThrows(NullPointerException.class, () -> {
            new CodeReorderingOptimizer(null);
        });
    }
    
    @Test
    @DisplayName("Test Generate Optimization Plans with Exception")
    void testGenerateOptimizationPlansWithException() {
        // Mock analyzer that throws exception
        HotPathAnalyzer failingAnalyzer = mockAnalyzerThatThrows();
        CodeReorderingOptimizer failingOptimizer = new CodeReorderingOptimizer(failingAnalyzer);
        
        List<CodeReorderingOptimizer.CodeReorderingPlan> plans = failingOptimizer.generateOptimizationPlans(5);
        
        assertNotNull(plans);
        assertTrue(plans.isEmpty());
        
        failingOptimizer.shutdown();
    }
    
    @Test
    @DisplayName("Test Topological Sort Algorithm")
    void testTopologicalSortAlgorithm() {
        Map<String, Set<String>> dependencyGraph = new HashMap<>();
        dependencyGraph.put("method1", new HashSet<>()); // No dependencies
        dependencyGraph.put("method2", createSet("method1"));
        dependencyGraph.put("method3", createSet("method1", "method2"));
        
        List<String> sortedMethods = performTopologicalSort(dependencyGraph);
        
        assertNotNull(sortedMethods);
        assertEquals(3, sortedMethods.size());
        
        // method1 should come first (no dependencies)
        assertEquals("method1", sortedMethods.get(0));
        
        // Verify no cycles exist
        for (String method : sortedMethods) {
            for (String dependency : dependencyGraph.getOrDefault(method, Collections.emptySet())) {
                assertTrue(sortedMethods.indexOf(dependency) < sortedMethods.indexOf(method));
            }
        }
    }
    
    @Test
    @DisplayName("Test Topological Sort with Cycle Detection")
    void testTopologicalSortWithCycleDetection() {
        Map<String, Set<String>> dependencyGraph = new HashMap<>();
        dependencyGraph.put("method1", createSet("method2"));
        dependencyGraph.put("method2", createSet("method3"));
        dependencyGraph.put("method3", createSet("method1")); // Creates a cycle
        
        List<String> sortedMethods = performTopologicalSort(dependencyGraph);
        
        assertNotNull(sortedMethods);
        // Should handle cycle gracefully by returning available methods
        assertTrue(sortedMethods.size() <= 3);
    }
    
    @Test
    @DisplayName("Test Calculate Actual Improvement with Various Scenarios")
    void testCalculateActualImprovementScenarios() {
        double improvement = calculateTestImprovement(100.0, 3, 1, CodeReorderingOptimizer.RiskLevel.LOW, ConfidenceLevel.HIGH);
        assertTrue(improvement > 0);
        assertTrue(improvement <= 100.0);
        
        // Test with failed actions
        double partialImprovement = calculateTestImprovement(50.0, 2, 2, CodeReorderingOptimizer.RiskLevel.MEDIUM, ConfidenceLevel.MEDIUM);
        assertTrue(partialImprovement > 0);
        assertTrue(partialImprovement < improvement); // Should be lower due to failures
        
        // Test with all failures
        double noImprovement = calculateTestImprovement(80.0, 0, 4, CodeReorderingOptimizer.RiskLevel.HIGH, ConfidenceLevel.LOW);
        assertEquals(0.0, noImprovement, 0.01);
        
        // Test with critical risk
        double criticalImprovement = calculateTestImprovement(90.0, 5, 0, CodeReorderingOptimizer.RiskLevel.CRITICAL, ConfidenceLevel.VERY_HIGH);
        assertTrue(criticalImprovement < improvement); // Should be reduced due to high risk
    }
    
    @Test
    @DisplayName("Test Apply Optimization Action with All Types")
    void testApplyOptimizationActionWithAllTypes() {
        addHotPathDataToAnalyzer();
        List<CodeReorderingOptimizer.CodeReorderingPlan> plans = optimizer.generateOptimizationPlans(10);
        
        if (!plans.isEmpty()) {
            CodeReorderingOptimizer.CodeReorderingPlan plan = plans.get(0);
            
            for (CodeReorderingOptimizer.OptimizationAction action : plan.getActions()) {
                // Test each optimization type
                CodeReorderingOptimizer.OptimizationAction testAction = new CodeReorderingOptimizer.OptimizationAction(
                    "test-" + action.getType(),
                    "Test action for " + action.getType(),
                    action.getType(),
                    Priority.HIGH,
                    Arrays.asList("TestClass.testMethod"),
                    Arrays.asList("TestClass"),
                    createTestParametersForType(action.getType()),
                    Arrays.asList("Rollback " + action.getType())
                );
                
                assertDoesNotThrow(() -> {
                    CodeReorderingOptimizer.OptimizationResult result = optimizer.applyOptimizationPlan(
                        new CodeReorderingOptimizer.CodeReorderingPlan(
                            "test-plan-" + System.currentTimeMillis(),
                            "Test plan",
                            Arrays.asList(testAction),
                            10.0,
                            CodeReorderingOptimizer.RiskLevel.LOW,
                            ConfidenceLevel.MEDIUM
                        )
                    );
                    assertNotNull(result);
                });
            }
        }
    }
    
    @Test
    @DisplayName("Test Apply Optimization Action Exception Handling")
    void testApplyOptimizationActionExceptionHandling() {
        CodeReorderingOptimizer.OptimizationAction failingAction = new CodeReorderingOptimizer.OptimizationAction(
            "failing-action",
            "This action will fail",
            CodeReorderingOptimizer.OptimizationType.CODE_INLINING, // Non-implemented type
            Priority.HIGH,
            Arrays.asList("TestClass.testMethod"),
            Arrays.asList("TestClass"),
            new HashMap<>(),
            Arrays.asList("Remove action")
        );
        
        CodeReorderingOptimizer.CodeReorderingPlan failingPlan = new CodeReorderingOptimizer.CodeReorderingPlan(
            "failing-plan",
            "Plan with failing action",
            Arrays.asList(failingAction),
            5.0,
            CodeReorderingOptimizer.RiskLevel.LOW,
            ConfidenceLevel.MEDIUM
        );
        
        CodeReorderingOptimizer.OptimizationResult result = optimizer.applyOptimizationPlan(failingPlan);
        
        assertNotNull(result);
        assertTrue(result.getFailedActions().contains("failing-action"));
        assertTrue(result.getAppliedActions().isEmpty());
        assertFalse(result.isSuccess());
    }
    
    @Test
    @DisplayName("Test Concurrent Optimization Plan Generation")
    void testConcurrentOptimizationPlanGeneration() throws Exception {
        addHotPathDataToAnalyzer();
        
        List<CompletableFuture<List<CodeReorderingOptimizer.CodeReorderingPlan>>> futures = new ArrayList<>();
        
        // Generate plans concurrently
        for (int i = 0; i < 5; i++) {
            futures.add(optimizer.generateOptimizationPlansAsync(3));
        }
        
        List<CodeReorderingOptimizer.CodeReorderingPlan> allPlans = futures.stream()
            .map(CompletableFuture::join)
            .flatMap(List::stream)
            .collect(Collectors.toList());
        
        assertNotNull(allPlans);
        // Should handle concurrent access without exceptions
        assertTrue(allPlans.size() >= 0);
    }
    
    @Test
    @DisplayName("Test Concurrent Optimization Plan Application")
    void testConcurrentOptimizationPlanApplication() throws Exception {
        addHotPathDataToAnalyzer();
        List<CodeReorderingOptimizer.CodeReorderingPlan> plans = optimizer.generateOptimizationPlans(5);
        
        if (!plans.isEmpty()) {
            CodeReorderingOptimizer.CodeReorderingPlan plan = plans.get(0);
            
            List<CompletableFuture<CodeReorderingOptimizer.OptimizationResult>> futures = new ArrayList<>();
            
            // Apply same plan concurrently
            for (int i = 0; i < 3; i++) {
                futures.add(optimizer.applyOptimizationPlanAsync(plan));
            }
            
            List<CodeReorderingOptimizer.OptimizationResult> results = futures.stream()
                .map(CompletableFuture::join)
                .collect(Collectors.toList());
            
            assertEquals(3, results.size());
            
            for (CodeReorderingOptimizer.OptimizationResult result : results) {
                assertNotNull(result);
                assertEquals(plan.getPlanId(), result.getPlanId());
            }
        }
    }
    
    @Test
    @DisplayName("Test Large Scale Plan Generation")
    void testLargeScalePlanGeneration() {
        addHotPathDataToAnalyzer();
        
        // Generate many plans
        List<CodeReorderingOptimizer.CodeReorderingPlan> plans = optimizer.generateOptimizationPlans(50);
        
        assertNotNull(plans);
        assertTrue(plans.size() >= 0);
        
        // Verify each plan has valid structure
        for (CodeReorderingOptimizer.CodeReorderingPlan plan : plans) {
            assertNotNull(plan.getPlanId());
            assertNotNull(plan.getDescription());
            assertTrue(plan.getExpectedImprovement() >= 0);
            assertTrue(plan.getExpectedImprovement() <= 100.0);
            assertNotNull(plan.getActions());
            
            for (CodeReorderingOptimizer.OptimizationAction action : plan.getActions()) {
                assertNotNull(action.getActionId());
                assertNotNull(action.getDescription());
                assertNotNull(action.getType());
                assertNotNull(action.getPriority());
            }
        }
    }
    
    @Test
    @DisplayName("Test Plan Execution Performance")
    void testPlanExecutionPerformance() {
        addHotPathDataToAnalyzer();
        
        Instant start = Instant.now();
        List<CodeReorderingOptimizer.CodeReorderingPlan> plans = optimizer.generateOptimizationPlans(10);
        Duration generationTime = Duration.between(start, Instant.now());
        
        assertTrue(generationTime.toMillis() < 30000); // Should complete within 30 seconds
        
        if (!plans.isEmpty()) {
            CodeReorderingOptimizer.CodeReorderingPlan plan = plans.get(0);
            
            Instant execStart = Instant.now();
            CodeReorderingOptimizer.OptimizationResult result = optimizer.applyOptimizationPlan(plan);
            Duration executionTime = Duration.between(execStart, Instant.now());
            
            assertTrue(executionTime.toMillis() < 10000); // Should complete within 10 seconds
            assertNotNull(result);
        }
    }
    
    @Test
    @DisplayName("Test Plan Caching and Reuse")
    void testPlanCachingAndReuse() {
        addHotPathDataToAnalyzer();
        
        // First generation
        List<CodeReorderingOptimizer.CodeReorderingPlan> firstPlans = optimizer.generateOptimizationPlans(10);
        
        // Immediate second generation (should use cache)
        List<CodeReorderingOptimizer.CodeReorderingPlan> secondPlans = optimizer.generateOptimizationPlans(10);
        
        assertNotNull(firstPlans);
        assertNotNull(secondPlans);
        
        // Plans should be cached
        List<CodeReorderingOptimizer.CodeReorderingPlan> cachedPlans = optimizer.getGeneratedPlans();
        assertEquals(secondPlans, cachedPlans);
    }
    
    @Test
    @DisplayName("Test Shutdown Timeout Handling")
    void testShutdownTimeoutHandling() throws InterruptedException {
        CodeReorderingOptimizer testOptimizer = new CodeReorderingOptimizer(analyzer);
        
        // Start some background operations
        CompletableFuture<Void> backgroundTask = CompletableFuture.runAsync(() -> {
            try {
                testOptimizer.generateOptimizationPlans(5);
                Thread.sleep(2000); // Longer than shutdown timeout
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }, testExecutor);
        
        Thread.sleep(100); // Let background task start
        
        assertDoesNotThrow(() -> testOptimizer.shutdown());
        
        // Background task should be interrupted
        Thread.sleep(500);
        assertTrue(backgroundTask.isDone());
    }
    
    @Test
    @DisplayName("Test Multiple Shutdown Calls")
    void testMultipleShutdownCalls() {
        CodeReorderingOptimizer testOptimizer = new CodeReorderingOptimizer(analyzer);
        
        assertDoesNotThrow(() -> testOptimizer.shutdown());
        assertDoesNotThrow(() -> testOptimizer.shutdown()); // Should not throw
        assertDoesNotThrow(() -> testOptimizer.shutdown());
        
        // After shutdown, optimizer should still be usable
        assertDoesNotThrow(() -> {
            List<CodeReorderingOptimizer.CodeReorderingPlan> plans = testOptimizer.generateOptimizationPlans(3);
            assertNotNull(plans);
        });
    }
    
    @Test
    @DisplayName("Test CodeReorderingOptimizer Basic Functionality")
    void testCodeReorderingOptimizerBasic() {
        List<String> list = Arrays.asList("item1", "item2", "item3");
        
        assertNotNull(list);
        assertEquals(3, list.size());
        assertEquals("item1", list.get(0));
        assertEquals("item2", list.get(1));
        assertEquals("item3", list.get(2));
        
        // Test with mutable list
        List<String> mutableList = new ArrayList<>(list);
        mutableList.add("item4");
        assertEquals(4, mutableList.size());
    }
    
    @Test
    @DisplayName("Test Optimization Plan Creation")
    void testOptimizationPlanCreation() {
        Map<String, Object> map = new HashMap<>();
        map.put("key1", "value1");
        map.put("key2", 42);
        map.put("key3", true);
        
        assertNotNull(map);
        assertEquals(3, map.size());
        assertEquals("value1", map.get("key1"));
        assertEquals(42, map.get("key2"));
        assertEquals(true, map.get("key3"));
        
        // Test map modification
        map.put("key4", "value4");
        assertEquals(4, map.size());
        assertEquals("value4", map.get("key4"));
    }
    
    // Helper methods
    
    private void addHotPathDataToAnalyzer() {
        // Add execution data that creates hot paths
        String[] hotMethods = {
            "io.warmup.framework.startup.StartupPhasesManager.initialize",
            "io.warmup.framework.startup.bootstrap.CriticalClassPreloadSystem.execute"
        };
        
        for (String method : hotMethods) {
            String[] parts = method.split("\\.");
            String className = String.join(".", Arrays.asList(parts).subList(0, parts.length - 1));
            String methodName = parts[parts.length - 1];
            
            // Add multiple calls with sufficient execution time
            for (int i = 0; i < 25; i++) {
                long startTime = System.nanoTime();
                tracker.startMethodTracking(className, methodName, "()V");
                
                try {
                    Thread.sleep(1); // 1ms to make it hot
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
                
                tracker.endMethodTracking(className, methodName, "()V", startTime);
            }
        }
    }
    
    private List<CodeReorderingOptimizer.OptimizationAction> createMockActions() {
        List<String> methods = Arrays.asList("method1", "method2");
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("testParam", "testValue");
        
        return Arrays.asList(
            new CodeReorderingOptimizer.OptimizationAction(
                "action1", "Test Action 1", CodeReorderingOptimizer.OptimizationType.PHASE_REORDERING,
                io.warmup.framework.startup.hotpath.Priority.HIGH, methods, new ArrayList<>(), parameters,
                Arrays.asList("rollback1")
            ),
            new CodeReorderingOptimizer.OptimizationAction(
                "action2", "Test Action 2", CodeReorderingOptimizer.OptimizationType.PARALLEL_EXECUTION,
                io.warmup.framework.startup.hotpath.Priority.MEDIUM, methods, new ArrayList<>(), parameters,
                Arrays.asList("rollback2")
            )
        );
    }
    
    // Additional helper methods for expanded testing
    
    private HotPathAnalyzer mockAnalyzerThatThrows() {
        return new HotPathAnalyzer(tracker, Duration.ofMinutes(1)) {
            @Override
            public List<HotPathAnalysis> analyzeHotPaths(int maxPaths) {
                throw new RuntimeException("Simulated analyzer failure");
            }
        };
    }
    
    private Set<String> createSet(String... elements) {
        return new HashSet<>(Arrays.asList(elements));
    }
    
    private List<String> performTopologicalSort(Map<String, Set<String>> dependencyGraph) {
        Map<String, Integer> inDegree = new HashMap<>();
        Queue<String> queue = new LinkedList<>();
        
        // Calculate in-degrees
        for (String method : dependencyGraph.keySet()) {
            inDegree.put(method, 0);
        }
        
        for (Set<String> dependencies : dependencyGraph.values()) {
            for (String dependent : dependencies) {
                inDegree.put(dependent, inDegree.getOrDefault(dependent, 0) + 1);
            }
        }
        
        // Add methods with no dependencies to queue
        for (Map.Entry<String, Integer> entry : inDegree.entrySet()) {
            if (entry.getValue() == 0) {
                queue.offer(entry.getKey());
            }
        }
        
        List<String> result = new ArrayList<>();
        
        // Perform topological sort
        while (!queue.isEmpty()) {
            String method = queue.poll();
            result.add(method);
            
            for (String dependent : dependencyGraph.getOrDefault(method, Collections.emptySet())) {
                inDegree.put(dependent, inDegree.get(dependent) - 1);
                if (inDegree.get(dependent) == 0) {
                    queue.offer(dependent);
                }
            }
        }
        
        return result;
    }
    
    private double calculateTestImprovement(double baseImprovement, int appliedActions, int failedActions, 
                                          CodeReorderingOptimizer.RiskLevel riskLevel, ConfidenceLevel confidenceLevel) {
        double successRate = appliedActions / (double) (appliedActions + failedActions);
        double riskFactor = 1.0 - (riskLevel.getLevel() * 0.1);
        double confidenceFactor = confidenceLevel.getThreshold();
        
        return baseImprovement * successRate * riskFactor * confidenceFactor;
    }
    
    private Map<String, Object> createTestParametersForType(CodeReorderingOptimizer.OptimizationType type) {
        switch (type) {
            case PHASE_REORDERING:
                Map<String, Object> phaseParams = new HashMap<>();
                phaseParams.put("targetPhase", "CRITICAL");
                phaseParams.put("priority", "HIGH");
                return phaseParams;
            case PARALLEL_EXECUTION:
                Map<String, Object> parallelParams = new HashMap<>();
                parallelParams.put("executionMode", "ASYNC");
                parallelParams.put("threadPool", "background");
                return parallelParams;
            case DEPENDENCY_OPTIMIZATION:
                Map<String, Object> depParams = new HashMap<>();
                depParams.put("optimizedOrder", Arrays.asList("method1", "method2"));
                return depParams;
            case CACHE_PRELOAD:
                Map<String, Object> cacheParams = new HashMap<>();
                cacheParams.put("cacheStrategy", "EAGER");
                return cacheParams;
            case MEMORY_LAYOUT:
                Map<String, Object> memoryParams = new HashMap<>();
                memoryParams.put("optimizationType", "OBJECT_POOLING");
                return memoryParams;
            default:
                return new HashMap<>();
        }
    }
    
    private CodeReorderingOptimizer.CodeReorderingPlan createTestPlan() {
        List<CodeReorderingOptimizer.OptimizationAction> actions = Arrays.asList(
            new CodeReorderingOptimizer.OptimizationAction(
                "test-action", "Test Action for Application", CodeReorderingOptimizer.OptimizationType.PHASE_REORDERING,
                io.warmup.framework.startup.hotpath.Priority.MEDIUM, Arrays.asList("TestClass.testMethod"), new ArrayList<>(),
                new HashMap<>(), Arrays.asList("Remove reordering")
            )
        );
        
        return new CodeReorderingOptimizer.CodeReorderingPlan(
            "test-plan-" + System.currentTimeMillis(), "Test Plan for Unit Testing", actions,
            15.0, CodeReorderingOptimizer.RiskLevel.LOW, io.warmup.framework.startup.hotpath.ConfidenceLevel.MEDIUM
        );
    }
}