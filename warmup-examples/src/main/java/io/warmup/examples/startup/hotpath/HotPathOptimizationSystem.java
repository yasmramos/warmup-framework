package io.warmup.examples.startup.hotpath;

import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;
import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Main system for Hot Path Optimization that combines tracking, analysis, and code reordering.
 * Provides comprehensive startup optimization based on real execution data and intelligent reordering.
 * 
 * @author MiniMax Agent
 * @version 1.0
 */
public class HotPathOptimizationSystem {
    
    /**
     * Helper method to create lists in Java 8 compatible way
     */
    @SuppressWarnings("unchecked")
    private static <T> List<T> createList(T... items) {
        List<T> list = new ArrayList<>();
        for (T item : items) {
            list.add(item);
        }
        return list;
    }
    
    /**
     * Helper method to create maps in Java 8 compatible way
     */
    private static Map<String, Object> createMap(Object... keyValues) {
        Map<String, Object> map = new HashMap<>();
        for (int i = 0; i < keyValues.length; i += 2) {
            if (i + 1 < keyValues.length) {
                map.put(String.valueOf(keyValues[i]), keyValues[i + 1]);
            }
        }
        return map;
    }
    
    /**
     * Result of executing Hot Path Optimization System.
     */
    public static class HotPathOptimizationResult {
        private final String executionId;
        private final boolean success;
        private final Duration executionTime;
        private final List<HotPathAnalyzer.HotPathAnalysis> identifiedHotPaths;
        private final List<CodeReorderingOptimizer.CodeReorderingPlan> generatedPlans;
        private final List<CodeReorderingOptimizer.OptimizationResult> appliedOptimizations;
        private final double totalExpectedImprovement;
        private final double totalActualImprovement;
        private final Map<String, Object> metrics;
        private final Instant completionTime;
        private final List<String> recommendations;
        private final SystemState systemState;
        
        public HotPathOptimizationResult(String executionId, boolean success, Duration executionTime,
                                        List<HotPathAnalyzer.HotPathAnalysis> identifiedHotPaths,
                                        List<CodeReorderingOptimizer.CodeReorderingPlan> generatedPlans,
                                        List<CodeReorderingOptimizer.OptimizationResult> appliedOptimizations,
                                        double totalExpectedImprovement, double totalActualImprovement,
                                        Map<String, Object> metrics, List<String> recommendations,
                                        SystemState systemState) {
            this.executionId = executionId;
            this.success = success;
            this.executionTime = executionTime;
            this.identifiedHotPaths = new ArrayList<>(identifiedHotPaths);
            this.generatedPlans = new ArrayList<>(generatedPlans);
            this.appliedOptimizations = new ArrayList<>(appliedOptimizations);
            this.totalExpectedImprovement = totalExpectedImprovement;
            this.totalActualImprovement = totalActualImprovement;
            this.metrics = new HashMap<>(metrics);
            this.recommendations = new ArrayList<>(recommendations);
            this.completionTime = Instant.now();
            this.systemState = systemState;
        }
        
        public String getExecutionId() { return executionId; }
        public boolean isSuccess() { return success; }
        public Duration getExecutionTime() { return executionTime; }
        public List<HotPathAnalyzer.HotPathAnalysis> getIdentifiedHotPaths() { return new ArrayList<>(identifiedHotPaths); }
        public List<CodeReorderingOptimizer.CodeReorderingPlan> getGeneratedPlans() { return new ArrayList<>(generatedPlans); }
        public List<CodeReorderingOptimizer.OptimizationResult> getAppliedOptimizations() { return new ArrayList<>(appliedOptimizations); }
        public double getTotalExpectedImprovement() { return totalExpectedImprovement; }
        public double getTotalActualImprovement() { return totalActualImprovement; }
        public Map<String, Object> getMetrics() { return new HashMap<>(metrics); }
        public List<String> getRecommendations() { return new ArrayList<>(recommendations); }
        public Instant getCompletionTime() { return completionTime; }
        public SystemState getSystemState() { return systemState; }
        
        public double getImprovementEfficiency() {
            return totalExpectedImprovement > 0 ? 
                Math.min(1.0, totalActualImprovement / totalExpectedImprovement) : 0.0;
        }
        
        public int getOptimizedMethodCount() {
            return appliedOptimizations.stream()
                .mapToInt(result -> result.getAppliedActions().size())
                .sum();
        }
        
        public String generateDetailedReport() {
            StringBuilder sb = new StringBuilder();
            sb.append("=== Hot Path Optimization System Results ===\n");
            sb.append(String.format("Execution ID: %s\n", executionId));
            sb.append(String.format("Execution Time: %s\n", executionTime));
            sb.append(String.format("Success: %s\n", success));
            sb.append(String.format("Hot Paths Identified: %d\n", identifiedHotPaths.size()));
            sb.append(String.format("Optimization Plans Generated: %d\n", generatedPlans.size()));
            sb.append(String.format("Optimizations Applied: %d\n", appliedOptimizations.size()));
            sb.append(String.format("Total Expected Improvement: %.2f%%\n", totalExpectedImprovement));
            sb.append(String.format("Total Actual Improvement: %.2f%%\n", totalActualImprovement));
            sb.append(String.format("Improvement Efficiency: %.2f%%\n", getImprovementEfficiency() * 100));
            sb.append(String.format("Optimized Methods: %d\n\n", getOptimizedMethodCount()));
            
            sb.append("=== Identified Hot Paths ===\n");
            for (HotPathAnalyzer.HotPathAnalysis path : identifiedHotPaths.stream()
                    .sorted((a, b) -> Double.compare(b.getHotnessScore(), a.getHotnessScore()))
                    .limit(10)
                    .collect(Collectors.toList())) {
                sb.append(String.format("- %s\n", path.toString()));
                sb.append(String.format("  Expected Improvement: %.1f%%\n", path.getExpectedImprovement()));
                sb.append(String.format("  Confidence: %s\n\n", path.getConfidenceLevel()));
            }
            
            sb.append("=== Applied Optimizations ===\n");
            for (CodeReorderingOptimizer.OptimizationResult result : appliedOptimizations.stream()
                    .filter(CodeReorderingOptimizer.OptimizationResult::isSuccess)
                    .collect(Collectors.toList())) {
                sb.append(String.format("- %s\n", result.toString()));
                sb.append(String.format("  Improvement: %.1f%%\n", result.getActualImprovement()));
                sb.append(String.format("  Applied Actions: %d/%d\n", 
                    result.getAppliedActions().size(),
                    result.getAppliedActions().size() + result.getFailedActions().size()));
            }
            
            if (!recommendations.isEmpty()) {
                sb.append("\n=== Recommendations ===\n");
                recommendations.forEach(rec -> sb.append("- " + rec + "\n"));
            }
            
            return sb.toString();
        }
        
        @Override
        public String toString() {
            return String.format("HotPathOptimizationResult{id='%s', success=%s, improvement=%.1f%%, paths=%d, plans=%d, optimizations=%d}",
                executionId, success, totalActualImprovement, 
                identifiedHotPaths.size(), generatedPlans.size(), appliedOptimizations.size());
        }
        
        // Additional methods needed by ComprehensiveStartupResult
        public List<HotPathAnalyzer.HotPathAnalysis> getHotPaths() { 
            return new ArrayList<>(identifiedHotPaths); 
        }
        public long getOptimizationTime() { 
            return executionTime.toMillis(); 
        }
    }
    
    /**
     * Current state of the optimization system.
     */
    public enum SystemState {
        INITIALIZING("Initializing"),
        TRACKING("Tracking Execution"),
        ANALYZING("Analyzing Hot Paths"),
        OPTIMIZING("Generating Optimizations"),
        APPLYING("Applying Optimizations"),
        COMPLETED("Completed"),
        ERROR("Error State");
        
        private final String description;
        
        SystemState(String description) {
            this.description = description;
        }
        
        public String getDescription() { return description; }
    }
    
    /**
     * Configuration for Hot Path Optimization System.
     */
    public static class OptimizationConfig {
        private final Duration trackingDuration;
        private final int maxHotPaths;
        private final int maxOptimizationPlans;
        private final double minHotnessThreshold;
        private final boolean autoApplyOptimizations;
        private final boolean enableAggressiveOptimization;
        private final Duration analysisTimeout;
        private final int minMethodCallCount;
        private final boolean enableParallelAnalysis;
        private final RiskTolerance riskTolerance;
        private final OptimizationStrategy optimizationStrategy;
        
        public OptimizationConfig(Duration trackingDuration, int maxHotPaths, int maxOptimizationPlans,
                                 double minHotnessThreshold, boolean autoApplyOptimizations,
                                 boolean enableAggressiveOptimization, Duration analysisTimeout,
                                 int minMethodCallCount, boolean enableParallelAnalysis,
                                 RiskTolerance riskTolerance, OptimizationStrategy optimizationStrategy) {
            this.trackingDuration = trackingDuration;
            this.maxHotPaths = maxHotPaths;
            this.maxOptimizationPlans = maxOptimizationPlans;
            this.minHotnessThreshold = minHotnessThreshold;
            this.autoApplyOptimizations = autoApplyOptimizations;
            this.enableAggressiveOptimization = enableAggressiveOptimization;
            this.analysisTimeout = analysisTimeout;
            this.minMethodCallCount = minMethodCallCount;
            this.enableParallelAnalysis = enableParallelAnalysis;
            this.riskTolerance = riskTolerance;
            this.optimizationStrategy = optimizationStrategy;
        }
        
        public Duration getTrackingDuration() { return trackingDuration; }
        public int getMaxHotPaths() { return maxHotPaths; }
        public int getMaxOptimizationPlans() { return maxOptimizationPlans; }
        public double getMinHotnessThreshold() { return minHotnessThreshold; }
        public boolean isAutoApplyOptimizations() { return autoApplyOptimizations; }
        public boolean isEnableAggressiveOptimization() { return enableAggressiveOptimization; }
        public Duration getAnalysisTimeout() { return analysisTimeout; }
        public int getMinMethodCallCount() { return minMethodCallCount; }
        public boolean isEnableParallelAnalysis() { return enableParallelAnalysis; }
        public RiskTolerance getRiskTolerance() { return riskTolerance; }
        public OptimizationStrategy getOptimizationStrategy() { return optimizationStrategy; }
        
        public static OptimizationConfig defaultConfig() {
            return new OptimizationConfig(
                Duration.ofSeconds(3),     // trackingDuration - OPTIMIZADO: reducido de 2 minutos para testing
                20,                        // maxHotPaths
                5,                         // maxOptimizationPlans
                30.0,                      // minHotnessThreshold
                false,                     // autoApplyOptimizations
                false,                     // enableAggressiveOptimization
                Duration.ofSeconds(2),     // analysisTimeout - OPTIMIZADO: reducido de 1 minuto para testing
                10,                        // minMethodCallCount
                true,                      // enableParallelAnalysis
                RiskTolerance.MODERATE,    // riskTolerance
                OptimizationStrategy.BALANCED // optimizationStrategy
            );
        }
        
        public static OptimizationConfig aggressiveConfig() {
            return new OptimizationConfig(
                Duration.ofSeconds(5),     // trackingDuration - OPTIMIZADO: reducido de 5 minutos para testing
                50,                        // maxHotPaths
                10,                        // maxOptimizationPlans
                20.0,                      // minHotnessThreshold
                true,                      // autoApplyOptimizations
                true,                      // enableAggressiveOptimization
                Duration.ofSeconds(3),     // analysisTimeout - OPTIMIZADO: reducido de 3 minutos para testing
                5,                         // minMethodCallCount
                true,                      // enableParallelAnalysis
                RiskTolerance.HIGH,        // riskTolerance
                OptimizationStrategy.MAXIMUM_PERFORMANCE // optimizationStrategy
            );
        }
    }
    
    /**
     * Risk tolerance levels for optimizations.
     */
    public enum RiskTolerance {
        CONSERVATIVE(0.1),
        MODERATE(0.3),
        AGGRESSIVE(0.5),
        HIGH(0.7);
        
        private final double threshold;
        
        RiskTolerance(double threshold) {
            this.threshold = threshold;
        }
        
        public double getThreshold() { return threshold; }
    }
    
    /**
     * Optimization strategy options.
     */
    public enum OptimizationStrategy {
        CONSERVATIVE("Conservative optimization focusing on stability"),
        BALANCED("Balanced optimization for stability and performance"),
        PERFORMANCE_FOCUSED("Performance-focused optimization"),
        MAXIMUM_PERFORMANCE("Maximum performance optimization with higher risk");
        
        private final String description;
        
        OptimizationStrategy(String description) {
            this.description = description;
        }
        
        public String getDescription() { return description; }
    }
    
    private final ExecutionPathTracker tracker;
    private final HotPathAnalyzer analyzer;
    private final CodeReorderingOptimizer optimizer;
    private final ExecutorService systemExecutor;
    private final AtomicInteger executionCount = new AtomicInteger(0);
    private final AtomicLong totalExecutionTime = new AtomicLong(0);
    private final AtomicBoolean isActive = new AtomicBoolean(false);
    private final List<HotPathOptimizationResult> executionHistory;
    private final OptimizationConfig config;
    private final SystemState currentState = SystemState.INITIALIZING;
    
    public HotPathOptimizationSystem() {
        this(OptimizationConfig.defaultConfig());
    }
    
    public HotPathOptimizationSystem(OptimizationConfig config) {
        this.config = config;
        this.systemExecutor = Executors.newFixedThreadPool(
            Math.max(4, Runtime.getRuntime().availableProcessors()),
            r -> {
                Thread t = new Thread(r, "HotPathOptimizationSystem-Worker");
                t.setDaemon(true);
                return t;
            }
        );
        
        this.tracker = new ExecutionPathTracker(true);
        this.analyzer = new HotPathAnalyzer(tracker);
        this.optimizer = new CodeReorderingOptimizer(analyzer);
        this.executionHistory = new CopyOnWriteArrayList<>();
        
        System.out.println("Hot Path Optimization System initialized with configuration: " + config.getOptimizationStrategy());
    }
    
    /**
     * Executes complete hot path optimization cycle.
     */
    public CompletableFuture<HotPathOptimizationResult> executeOptimizationAsync() {
        return CompletableFuture.supplyAsync(() -> executeOptimization(), systemExecutor);
    }
    
    /**
     * Synchronous version of optimization execution.
     */
    public HotPathOptimizationResult executeOptimization() {
        Instant startTime = Instant.now();
        String executionId = "HPO_" + System.currentTimeMillis();
        executionCount.incrementAndGet();
        
        try {
            updateSystemState(SystemState.TRACKING);
            System.out.println("Starting Hot Path Optimization execution: " + executionId);
            
            // Phase 1: Track execution patterns
            Duration trackingTime = executeTrackingPhase();
            
            updateSystemState(SystemState.ANALYZING);
            System.out.println("Phase 1 completed: Tracking " + trackingTime);
            
            // Phase 2: Analyze hot paths
            List<HotPathAnalyzer.HotPathAnalysis> hotPaths = executeAnalysisPhase();
            
            updateSystemState(SystemState.OPTIMIZING);
            System.out.println("Phase 2 completed: Identified " + hotPaths.size() + " hot paths");
            
            // Phase 3: Generate optimization plans
            List<CodeReorderingOptimizer.CodeReorderingPlan> plans = executeOptimizationPhase(hotPaths);
            
            updateSystemState(SystemState.APPLYING);
            System.out.println("Phase 3 completed: Generated " + plans.size() + " optimization plans");
            
            // Phase 4: Apply optimizations
            List<CodeReorderingOptimizer.OptimizationResult> appliedOptimizations = executeApplicationPhase(plans);
            
            updateSystemState(SystemState.COMPLETED);
            
            // Calculate results
            double totalExpectedImprovement = plans.stream()
                .mapToDouble(CodeReorderingOptimizer.CodeReorderingPlan::getExpectedImprovement)
                .sum();
            
            double totalActualImprovement = appliedOptimizations.stream()
                .mapToDouble(CodeReorderingOptimizer.OptimizationResult::getActualImprovement)
                .sum();
            
            Map<String, Object> metrics = generateMetrics(executionId, trackingTime, hotPaths, plans, appliedOptimizations);
            List<String> recommendations = generateRecommendations(hotPaths, plans, appliedOptimizations);
            
            HotPathOptimizationResult result = new HotPathOptimizationResult(
                executionId, 
                !appliedOptimizations.isEmpty(),
                Duration.between(startTime, Instant.now()),
                hotPaths,
                plans,
                appliedOptimizations,
                totalExpectedImprovement,
                totalActualImprovement,
                metrics,
                recommendations,
                currentState
            );
            
            executionHistory.add(result);
            totalExecutionTime.addAndGet(result.getExecutionTime().toNanos());
            
            System.out.println("Hot Path Optimization completed: " + result.toString());
            return result;
            
        } catch (Exception e) {
            updateSystemState(SystemState.ERROR);
            System.err.println("Error during Hot Path Optimization: " + e.getMessage());
            
            Map<String, Object> errorMetrics = createMap("errorMessage", e.getMessage(), "executionId", executionId);
            List<String> errorRecommendations = createList("Review execution logs", "Check system configuration", "Verify input data quality");
            
            HotPathOptimizationResult errorResult = new HotPathOptimizationResult(
                executionId, false, Duration.between(startTime, Instant.now()),
                Collections.emptyList(), Collections.emptyList(), Collections.emptyList(),
                0.0, 0.0, errorMetrics, errorRecommendations, currentState
            );
            
            executionHistory.add(errorResult);
            return errorResult;
        }
    }
    
    /**
     * Phase 1: Track execution patterns.
     */
    private Duration executeTrackingPhase() {
        Instant trackingStart = Instant.now();
        
        // Simulate tracking for the configured duration
        Duration effectiveTracking = config.getTrackingDuration().minusSeconds(5); // Subtract processing time
        
        // In a real implementation, this would collect actual execution data
        simulateExecutionTracking(effectiveTracking);
        
        return Duration.between(trackingStart, Instant.now());
    }
    
    /**
     * Phase 2: Analyze hot paths.
     */
    private List<HotPathAnalyzer.HotPathAnalysis> executeAnalysisPhase() throws TimeoutException {
        try {
            CompletableFuture<List<HotPathAnalyzer.HotPathAnalysis>> analysisFuture = analyzer.analyzeHotPathsAsync(config.getMaxHotPaths());
            
            return analysisFuture.get(config.getAnalysisTimeout().getSeconds(), TimeUnit.SECONDS);
            
        } catch (TimeoutException e) {
            System.err.println("Analysis phase timed out after " + config.getAnalysisTimeout());
            throw e;
        } catch (Exception e) {
            System.err.println("Error during analysis phase: " + e.getMessage());
            return Collections.emptyList();
        }
    }
    
    /**
     * Phase 3: Generate optimization plans.
     */
    private List<CodeReorderingOptimizer.CodeReorderingPlan> executeOptimizationPhase(List<HotPathAnalyzer.HotPathAnalysis> hotPaths) {
        if (hotPaths.isEmpty()) {
            return Collections.emptyList();
        }
        
        try {
            CompletableFuture<List<CodeReorderingOptimizer.CodeReorderingPlan>> optimizationFuture = 
                optimizer.generateOptimizationPlansAsync(config.getMaxOptimizationPlans());
            
            return optimizationFuture.get(30, TimeUnit.SECONDS);
            
        } catch (Exception e) {
            System.err.println("Error during optimization phase: " + e.getMessage());
            return Collections.emptyList();
        }
    }
    
    /**
     * Phase 4: Apply optimizations.
     */
    private List<CodeReorderingOptimizer.OptimizationResult> executeApplicationPhase(List<CodeReorderingOptimizer.CodeReorderingPlan> plans) {
        List<CodeReorderingOptimizer.OptimizationResult> results = new ArrayList<>();
        
        if (!config.isAutoApplyOptimizations() || plans.isEmpty()) {
            return results;
        }
        
        // Filter plans based on risk tolerance and confidence
        List<CodeReorderingOptimizer.CodeReorderingPlan> applicablePlans = plans.stream()
            .filter(plan -> {
                CodeReorderingOptimizer.RiskLevel planRisk = plan.getRiskLevel();
                ConfidenceLevel planConfidence = plan.getConfidenceLevel();
                
                boolean riskAcceptable = planRisk.getLevel() <= config.getRiskTolerance().getThreshold() * 5;
                boolean confidenceAcceptable = planConfidence.getThreshold() >= 0.6;
                
                return riskAcceptable && confidenceAcceptable;
            })
            .limit(config.getMaxOptimizationPlans())
            .collect(Collectors.toList());
        
        for (CodeReorderingOptimizer.CodeReorderingPlan plan : applicablePlans) {
            try {
                CompletableFuture<CodeReorderingOptimizer.OptimizationResult> applyFuture = 
                    optimizer.applyOptimizationPlanAsync(plan);
                
                CodeReorderingOptimizer.OptimizationResult result = applyFuture.get(15, TimeUnit.SECONDS);
                results.add(result);
                
            } catch (Exception e) {
                System.err.println("Failed to apply optimization plan " + plan.getPlanId() + ": " + e.getMessage());
            }
        }
        
        return results;
    }
    
    /**
     * Simulates execution tracking for testing purposes.
     */
    private void simulateExecutionTracking(Duration duration) {
        System.out.println("Simulating execution tracking for " + duration);
        
        // Simulate method calls with realistic patterns
        String[] sampleMethods = {
            "io.warmup.framework.startup.StartupPhasesManager.initialize",
            "io.warmup.framework.startup.bootstrap.CriticalClassPreloadSystem.execute",
            "io.warmup.framework.config.PreloadedConfigSystem.load",
            "io.warmup.framework.lazy.ZeroStartupBeanLoader.initialize",
            "io.warmup.framework.startup.ParallelSubsystemInitializer.execute",
            "io.warmup.framework.startup.CriticalStartupPhase.execute",
            "io.warmup.framework.startup.BackgroundStartupPhase.execute"
        };
        
        Random random = new Random();
        
        // Simulate tracking for the duration
        long endTime = System.currentTimeMillis() + duration.toMillis();
        
        while (System.currentTimeMillis() < endTime) {
            // Randomly select methods to track
            int numMethods = random.nextInt(3) + 1;
            
            for (int i = 0; i < numMethods; i++) {
                String method = sampleMethods[random.nextInt(sampleMethods.length)];
                String[] parts = method.split("\\.");
                
                if (parts.length >= 2) {
                    String className = String.join(".", Arrays.asList(parts).subList(0, parts.length - 1));
                    String methodName = parts[parts.length - 1];
                    
                    // Simulate tracking with random execution time
                    try (ExecutionPathTracker.MethodTracker mt = tracker.trackMethod(className, methodName, "")) {
                        // Simulate method execution time
                        long execTime = ThreadLocalRandom.current().nextLong(50000) + 1000; // 1-50 µs
                        Thread.sleep(execTime / 1000000); // Convert to milliseconds for simulation
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                }
            }
            
            // Small delay between method calls
            try {
                Thread.sleep(random.nextInt(10) + 1);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }
    
    /**
     * Updates the current system state.
     */
    private void updateSystemState(SystemState newState) {
        // In a real implementation, this would update a proper state management system
        System.out.println("System state changed to: " + newState.getDescription());
    }
    
    /**
     * Generates comprehensive metrics for the execution.
     */
    private Map<String, Object> generateMetrics(String executionId, Duration trackingTime,
                                               List<HotPathAnalyzer.HotPathAnalysis> hotPaths,
                                               List<CodeReorderingOptimizer.CodeReorderingPlan> plans,
                                               List<CodeReorderingOptimizer.OptimizationResult> results) {
        Map<String, Object> metrics = new HashMap<>();
        
        metrics.put("executionId", executionId);
        metrics.put("trackingDuration", trackingTime.toMillis());
        metrics.put("hotPathsIdentified", hotPaths.size());
        metrics.put("plansGenerated", plans.size());
        metrics.put("optimizationsApplied", results.size());
        metrics.put("systemConfig", config.toString());
        
        if (!hotPaths.isEmpty()) {
            double avgHotnessScore = hotPaths.stream()
                .mapToDouble(HotPathAnalyzer.HotPathAnalysis::getHotnessScore)
                .average()
                .orElse(0.0);
            
            metrics.put("averageHotnessScore", avgHotnessScore);
            metrics.put("maxHotnessScore", hotPaths.stream()
                .mapToDouble(HotPathAnalyzer.HotPathAnalysis::getHotnessScore)
                .max()
                .orElse(0.0));
        }
        
        return metrics;
    }
    
    /**
     * Generates recommendations based on the optimization results.
     */
    private List<String> generateRecommendations(List<HotPathAnalyzer.HotPathAnalysis> hotPaths,
                                                List<CodeReorderingOptimizer.CodeReorderingPlan> plans,
                                                List<CodeReorderingOptimizer.OptimizationResult> results) {
        List<String> recommendations = new ArrayList<>();
        
        if (hotPaths.isEmpty()) {
            recommendations.add("Consider extending tracking duration to capture more execution patterns");
            recommendations.add("Verify that application is generating sufficient execution data");
            return recommendations;
        }
        
        if (plans.isEmpty()) {
            recommendations.add("No optimization opportunities identified - current code organization may already be optimal");
            recommendations.add("Consider adjusting hotness threshold if more optimization is needed");
        }
        
        if (results.stream().noneMatch(CodeReorderingOptimizer.OptimizationResult::isSuccess)) {
            recommendations.add("No optimizations were successfully applied - review system configuration");
            recommendations.add("Check risk tolerance settings - current settings may be too restrictive");
        }
        
        // Performance-based recommendations
        double avgCallCount = hotPaths.stream()
            .mapToLong(path -> path.getPerformanceMetrics().getCallCount())
            .average()
            .orElse(0.0);
        
        if (avgCallCount > 1000) {
            recommendations.add("High method call frequency detected - consider implementing aggressive caching strategies");
        }
        
        // Risk-based recommendations
        long highRiskPlans = plans.stream()
            .filter(plan -> plan.getRiskLevel() == CodeReorderingOptimizer.RiskLevel.HIGH || plan.getRiskLevel() == CodeReorderingOptimizer.RiskLevel.CRITICAL)
            .count();
        
        if (highRiskPlans > 0) {
            recommendations.add("High-risk optimization plans detected - ensure thorough testing before production deployment");
        }
        
        // Confidence-based recommendations
        long lowConfidencePlans = plans.stream()
            .filter(plan -> plan.getConfidenceLevel().getThreshold() < 0.6)
            .count();
        
        if (lowConfidencePlans > 0) {
            recommendations.add("Low-confidence optimization plans detected - consider collecting more execution data");
        }
        
        return recommendations;
    }
    
    /**
     * Gets the execution path tracker.
     */
    public ExecutionPathTracker getTracker() { return tracker; }
    
    /**
     * Gets the hot path analyzer.
     */
    public HotPathAnalyzer getAnalyzer() { return analyzer; }
    
    /**
     * Gets the code reordering optimizer.
     */
    public CodeReorderingOptimizer getOptimizer() { return optimizer; }
    
    /**
     * Gets execution history.
     */
    public List<HotPathOptimizationResult> getExecutionHistory() {
        return new ArrayList<>(executionHistory);
    }
    
    /**
     * Gets execution statistics.
     */
    public int getExecutionCount() { return executionCount.get(); }
    public long getTotalExecutionTime() { return totalExecutionTime.get(); }
    public double getAverageExecutionTime() {
        int count = executionCount.get();
        return count > 0 ? (double) totalExecutionTime.get() / count / 1_000_000.0 : 0.0;
    }
    
    /**
     * Checks if the system is currently active.
     */
    public boolean isActive() { return isActive.get(); }
    
    /**
     * Gets the current configuration.
     */
    public OptimizationConfig getConfig() { return config; }
    
    /**
     * Starts the system (activates tracking).
     */
    public void start() {
        isActive.set(true);
        System.out.println("Hot Path Optimization System started");
    }
    
    /**
     * Stops the system (deactivates tracking).
     */
    public void stop() {
        isActive.set(false);
        System.out.println("Hot Path Optimization System stopped");
    }
    
    /**
     * Shuts down the system and cleans up resources.
     */
    public void shutdown() {
        stop();
        
        tracker.reset();
        analyzer.shutdown();
        optimizer.shutdown();
        
        systemExecutor.shutdown();
        try {
            if (!systemExecutor.awaitTermination(30, TimeUnit.SECONDS)) {
                systemExecutor.shutdownNow();
            }
        } catch (InterruptedException e) {
            systemExecutor.shutdownNow();
            Thread.currentThread().interrupt();
        }
        
        System.out.println("Hot Path Optimization System shutdown completed");
    }
}