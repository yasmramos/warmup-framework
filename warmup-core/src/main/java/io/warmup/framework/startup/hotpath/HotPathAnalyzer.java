package io.warmup.framework.startup.hotpath;

import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;
import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Analyzes execution paths to identify optimization opportunities based on real runtime data.
 * Uses advanced algorithms to determine hot paths and recommend code reordering.
 * 
 * @author MiniMax Agent
 * @version 1.0
 */
public class HotPathAnalyzer {
    
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
     * Represents a hot path with optimization recommendations.
     */
    public static class HotPathAnalysis {
        private final String pathId;
        private final String description;
        private final HotnessLevel hotnessLevel;
        private final double hotnessScore;
        private final List<String> involvedMethods;
        private final List<OptimizationRecommendation> recommendations;
        private final PerformanceMetrics performanceMetrics;
        private final Instant analysisTimestamp;
        private final ConfidenceLevel confidenceLevel;
        
        public HotPathAnalysis(String pathId, String description, HotnessLevel hotnessLevel, 
                              double hotnessScore, List<String> involvedMethods,
                              List<OptimizationRecommendation> recommendations, 
                              PerformanceMetrics performanceMetrics,
                              ConfidenceLevel confidenceLevel) {
            this.pathId = pathId;
            this.description = description;
            this.hotnessLevel = hotnessLevel;
            this.hotnessScore = hotnessScore;
            this.involvedMethods = new ArrayList<>(involvedMethods);
            this.recommendations = new ArrayList<>(recommendations);
            this.performanceMetrics = performanceMetrics;
            this.analysisTimestamp = Instant.now();
            this.confidenceLevel = confidenceLevel;
        }
        
        public String getPathId() { return pathId; }
        public String getDescription() { return description; }
        public HotnessLevel getHotnessLevel() { return hotnessLevel; }
        public double getHotnessScore() { return hotnessScore; }
        public List<String> getInvolvedMethods() { return new ArrayList<>(involvedMethods); }
        public List<OptimizationRecommendation> getRecommendations() { return new ArrayList<>(recommendations); }
        public PerformanceMetrics getPerformanceMetrics() { return performanceMetrics; }
        public Instant getAnalysisTimestamp() { return analysisTimestamp; }
        public ConfidenceLevel getConfidenceLevel() { return confidenceLevel; }
        
        public boolean isSignificant() {
            return hotnessLevel.getLevel() >= HotnessLevel.HOT.getLevel();
        }
        
        public double getExpectedImprovement() {
            return recommendations.stream()
                .mapToDouble(OptimizationRecommendation::getExpectedImprovement)
                .sum();
        }
        
        @Override
        public String toString() {
            return String.format("HotPathAnalysis{id='%s', hotness=%s, score=%.2f, methods=%d, improvement=%.1f%%}",
                pathId, hotnessLevel, hotnessScore, involvedMethods.size(), getExpectedImprovement());
        }
    }
    
    /**
     * Optimization recommendation with specific actions.
     */
    public static class OptimizationRecommendation {
        private final String id;
        private final String description;
        private final RecommendationType type;
        private final Priority priority;
        private final double expectedImprovement;
        private final List<String> affectedMethods;
        private final List<String> implementationSteps;
        private final Map<String, Object> parameters;
        private final ConfidenceLevel confidenceLevel;
        
        public OptimizationRecommendation(String id, String description, RecommendationType type,
                                        Priority priority, double expectedImprovement,
                                        List<String> affectedMethods, List<String> implementationSteps,
                                        Map<String, Object> parameters, ConfidenceLevel confidenceLevel) {
            this.id = id;
            this.description = description;
            this.type = type;
            this.priority = priority;
            this.expectedImprovement = expectedImprovement;
            this.affectedMethods = new ArrayList<>(affectedMethods);
            this.implementationSteps = new ArrayList<>(implementationSteps);
            this.parameters = new HashMap<>(parameters);
            this.confidenceLevel = confidenceLevel;
        }
        
        public String getId() { return id; }
        public String getDescription() { return description; }
        public RecommendationType getType() { return type; }
        public Priority getPriority() { return priority; }
        public double getExpectedImprovement() { return expectedImprovement; }
        public List<String> getAffectedMethods() { return new ArrayList<>(affectedMethods); }
        public List<String> getImplementationSteps() { return new ArrayList<>(implementationSteps); }
        public Map<String, Object> getParameters() { return new HashMap<>(parameters); }
        public ConfidenceLevel getConfidenceLevel() { return confidenceLevel; }
        
        @Override
        public String toString() {
            return String.format("OptimizationRecommendation{id='%s', type=%s, priority=%s, improvement=%.1f%%}",
                id, type, priority, expectedImprovement);
        }
    }
    
    /**
     * Performance metrics for a hot path.
     */
    public static class PerformanceMetrics {
        private final long totalExecutionTime;
        private final long averageExecutionTime;
        private final long minExecutionTime;
        private final long maxExecutionTime;
        private final long callCount;
        private final int threadCount;
        private final double consistencyScore;
        private final double throughput;
        
        public PerformanceMetrics(long totalExecutionTime, long averageExecutionTime,
                                 long minExecutionTime, long maxExecutionTime, long callCount,
                                 int threadCount, double consistencyScore, double throughput) {
            this.totalExecutionTime = totalExecutionTime;
            this.averageExecutionTime = averageExecutionTime;
            this.minExecutionTime = minExecutionTime;
            this.maxExecutionTime = maxExecutionTime;
            this.callCount = callCount;
            this.threadCount = threadCount;
            this.consistencyScore = consistencyScore;
            this.throughput = throughput;
        }
        
        public long getTotalExecutionTime() { return totalExecutionTime; }
        public long getAverageExecutionTime() { return averageExecutionTime; }
        public long getMinExecutionTime() { return minExecutionTime; }
        public long getMaxExecutionTime() { return maxExecutionTime; }
        public long getCallCount() { return callCount; }
        public int getThreadCount() { return threadCount; }
        public double getConsistencyScore() { return consistencyScore; }
        public double getThroughput() { return throughput; }
        
        public String toDetailedString() {
            return String.format("PerformanceMetrics{total=%.2fms, avg=%.2fµs, min=%.2fµs, max=%.2fms, " +
                               "calls=%d, threads=%d, consistency=%.2f, throughput=%.2fops/s}",
                totalExecutionTime / 1_000_000.0, averageExecutionTime / 1000.0,
                minExecutionTime / 1000.0, maxExecutionTime / 1_000_000.0,
                callCount, threadCount, consistencyScore, throughput);
        }
    }
    
    /**
     * Confidence level for analysis results.
     */
    public enum ConfidenceLevel {
        VERY_HIGH(0.95, "Very High"),
        HIGH(0.80, "High"),
        MEDIUM(0.60, "Medium"),
        LOW(0.40, "Low"),
        VERY_LOW(0.20, "Very Low");
        
        private final double threshold;
        private final String description;
        
        ConfidenceLevel(double threshold, String description) {
            this.threshold = threshold;
            this.description = description;
        }
        
        public double getThreshold() { return threshold; }
        public String getDescription() { return description; }
    }
    
    /**
     * Types of optimization recommendations.
     */
    public enum RecommendationType {
        CODE_REORDERING("Code Reordering"),
        METHOD_INLINING("Method Inlining"),
        CACHE_OPTIMIZATION("Cache Optimization"),
        PARALLEL_EXECUTION("Parallel Execution"),
        EARLY_INITIALIZATION("Early Initialization"),
        LAZY_EVALUATION("Lazy Evaluation"),
        MEMORY_OPTIMIZATION("Memory Optimization"),
        ALGORITHM_OPTIMIZATION("Algorithm Optimization");
        
        private final String description;
        
        RecommendationType(String description) {
            this.description = description;
        }
        
        public String getDescription() { return description; }
    }
    
    /**
     * Priority levels for recommendations.
     */
    public enum Priority {
        CRITICAL(4, "Critical"),
        HIGH(3, "High"),
        MEDIUM(2, "Medium"),
        LOW(1, "Low");
        
        private final int level;
        private final String description;
        
        Priority(int level, String description) {
            this.level = level;
            this.description = description;
        }
        
        public int getLevel() { return level; }
        public String getDescription() { return description; }
    }
    
    private final ExecutionPathTracker tracker;
    private final ExecutorService analysisExecutor;
    private final AtomicInteger analysisCount = new AtomicInteger(0);
    private final List<HotPathAnalysis> cachedAnalysis;
    private final Duration analysisCacheTimeout;
    
    public HotPathAnalyzer(ExecutionPathTracker tracker) {
        this(tracker, Duration.ofMinutes(5));
    }
    
    public HotPathAnalyzer(ExecutionPathTracker tracker, Duration analysisCacheTimeout) {
        this.tracker = tracker;
        this.analysisCacheTimeout = analysisCacheTimeout;
        this.analysisExecutor = Executors.newFixedThreadPool(
            Runtime.getRuntime().availableProcessors(),
            r -> {
                Thread t = new Thread(r, "HotPathAnalyzer-Worker");
                t.setDaemon(true);
                return t;
            }
        );
        this.cachedAnalysis = new CopyOnWriteArrayList<>();
    }
    
    /**
     * Analyzes execution paths to identify hot paths and optimization opportunities.
     */
    public CompletableFuture<List<HotPathAnalysis>> analyzeHotPathsAsync(int maxHotPaths) {
        return CompletableFuture.supplyAsync(() -> analyzeHotPaths(maxHotPaths), analysisExecutor);
    }
    
    /**
     * Synchronous version of hot path analysis.
     */
    public List<HotPathAnalysis> analyzeHotPaths(int maxHotPaths) {
        long analysisStart = System.nanoTime();
        analysisCount.incrementAndGet();
        
        try {
            // Collect execution data
            List<ExecutionPathTracker.MethodExecution> recentExecutions = tracker.getRecentExecutions(maxHotPaths * 100);
            List<Map.Entry<ExecutionPathTracker.MethodExecution, ExecutionPathTracker.ExecutionStats>> methodStats = 
                tracker.getHotMethods(maxHotPaths * 5);
            
            if (methodStats.isEmpty()) {
                return Collections.emptyList();
            }
            
            // Identify hot paths
            List<HotPathAnalysis> hotPaths = identifyHotPaths(methodStats);
            
            // Generate optimization recommendations
            hotPaths.forEach(this::generateRecommendations);
            
            // Cache analysis results
            cachedAnalysis.clear();
            cachedAnalysis.addAll(hotPaths);
            
            // Sort by hotness score
            hotPaths.sort((a, b) -> Double.compare(b.getHotnessScore(), a.getHotnessScore()));
            
            long analysisTime = System.nanoTime() - analysisStart;
            System.out.printf("Hot path analysis completed in %.2fms: %d paths identified%n",
                analysisTime / 1_000_000.0, hotPaths.size());
            
            return hotPaths;
            
        } catch (Exception e) {
            System.err.println("Error during hot path analysis: " + e.getMessage());
            return Collections.emptyList();
        }
    }
    
    /**
     * Identifies hot paths from execution statistics.
     */
    private List<HotPathAnalysis> identifyHotPaths(List<Map.Entry<ExecutionPathTracker.MethodExecution, ExecutionPathTracker.ExecutionStats>> methodStats) {
        List<HotPathAnalysis> hotPaths = new ArrayList<>();
        
        // Group methods by execution patterns
        Map<String, List<Map.Entry<ExecutionPathTracker.MethodExecution, ExecutionPathTracker.ExecutionStats>>> methodGroups = 
            methodStats.stream()
                .collect(Collectors.groupingBy(entry -> {
                    String className = entry.getKey().getClassName();
                    // Group by package and class patterns
                    if (className.contains("framework") || className.contains("core")) {
                        return "framework";
                    } else if (className.contains("config") || className.contains("Config")) {
                        return "config";
                    } else if (className.contains("startup") || className.contains("Startup")) {
                        return "startup";
                    } else {
                        return "application";
                    }
                }));
        
        // Analyze each group
        for (Map.Entry<String, List<Map.Entry<ExecutionPathTracker.MethodExecution, ExecutionPathTracker.ExecutionStats>>> group : methodGroups.entrySet()) {
            HotPathAnalysis hotPath = analyzeMethodGroup(group.getKey(), group.getValue());
            if (hotPath != null) {
                hotPaths.add(hotPath);
            }
        }
        
        // Also analyze individual hot methods
        for (Map.Entry<ExecutionPathTracker.MethodExecution, ExecutionPathTracker.ExecutionStats> entry : methodStats.stream()
                .filter(e -> e.getValue().getHotnessLevel().compareTo(ExecutionPathTracker.HotnessLevel.HOT) >= 0)
                .limit(10)
                .collect(Collectors.toList())) {
            
            HotPathAnalysis hotPath = analyzeSingleMethod(entry);
            if (hotPath != null) {
                hotPaths.add(hotPath);
            }
        }
        
        return hotPaths;
    }
    
    /**
     * Analyzes a group of related methods as a hot path.
     */
    private HotPathAnalysis analyzeMethodGroup(String groupId, List<Map.Entry<ExecutionPathTracker.MethodExecution, ExecutionPathTracker.ExecutionStats>> methodStats) {
        if (methodStats.isEmpty()) return null;
        
        // Calculate aggregated metrics
        long totalCalls = methodStats.stream()
            .mapToLong(e -> e.getValue().getCallCount())
            .sum();
        
        double avgExecutionTime = methodStats.stream()
            .mapToDouble(e -> e.getValue().getAverageExecutionTime())
            .average()
            .orElse(0.0);
        
        double totalExecutionTime = methodStats.stream()
            .mapToDouble(e -> e.getValue().getTotalExecutionTime())
            .sum();
        
        int totalThreads = methodStats.stream()
            .mapToInt(e -> e.getValue().getThreadCount())
            .max()
            .orElse(1);
        
        double consistencyScore = methodStats.stream()
            .mapToDouble(e -> e.getValue().getConsistencyScore())
            .average()
            .orElse(0.0);
        
        // Calculate hotness score
        double hotnessScore = calculateHotnessScore(totalCalls, avgExecutionTime, totalExecutionTime, totalThreads);
        HotnessLevel hotnessLevel = determineHotnessLevel(hotnessScore);
        
        if (hotnessLevel.getLevel() < HotnessLevel.WARM.getLevel()) {
            return null; // Not hot enough to be considered
        }
        
        List<String> involvedMethods = methodStats.stream()
            .map(e -> e.getKey().getFullMethodName())
            .collect(Collectors.toList());
        
        PerformanceMetrics performanceMetrics = new PerformanceMetrics(
            (long) totalExecutionTime, (long) avgExecutionTime,
            methodStats.stream().mapToLong(e -> e.getValue().getMinExecutionTime()).min().orElse(0),
            methodStats.stream().mapToLong(e -> e.getValue().getMaxExecutionTime()).max().orElse(0),
            totalCalls, totalThreads, consistencyScore,
            totalCalls / (totalExecutionTime / 1_000_000_000.0) // throughput
        );
        
        ConfidenceLevel confidenceLevel = calculateConfidenceLevel(methodStats, totalCalls);
        
        return new HotPathAnalysis(
            "group_" + groupId + "_" + System.currentTimeMillis(),
            "Hot path in " + groupId + " with " + methodStats.size() + " methods",
            hotnessLevel, hotnessScore, involvedMethods, new ArrayList<>(),
            performanceMetrics, confidenceLevel
        );
    }
    
    /**
     * Analyzes a single hot method as a hot path.
     */
    private HotPathAnalysis analyzeSingleMethod(Map.Entry<ExecutionPathTracker.MethodExecution, ExecutionPathTracker.ExecutionStats> methodStat) {
        ExecutionPathTracker.MethodExecution execution = methodStat.getKey();
        ExecutionPathTracker.ExecutionStats stats = methodStat.getValue();
        
        double hotnessScore = calculateHotnessScore(stats.getCallCount(), stats.getAverageExecutionTime(), 
                                                  stats.getTotalExecutionTime(), stats.getThreadCount());
        ExecutionPathTracker.HotnessLevel hotnessLevel = stats.getHotnessLevel();
        
        if (hotnessLevel.compareTo(ExecutionPathTracker.HotnessLevel.HOT) < 0) {
            return null;
        }
        
        PerformanceMetrics performanceMetrics = new PerformanceMetrics(
            stats.getTotalExecutionTime(), (long) stats.getAverageExecutionTime(),
            stats.getMinExecutionTime(), stats.getMaxExecutionTime(),
            stats.getCallCount(), stats.getThreadCount(), stats.getConsistencyScore(),
            stats.getCallCount() / (stats.getTotalExecutionTime() / 1_000_000_000.0)
        );
        
        ConfidenceLevel confidenceLevel = calculateConfidenceLevel(stats);
        
        return new HotPathAnalysis(
            "method_" + execution.getClassName() + "." + execution.getMethodName(),
            "Hot method: " + execution.getFullMethodName(),
            convertToLocalHotnessLevel(hotnessLevel), hotnessScore, createList(execution.getFullMethodName()),
            new ArrayList<>(), performanceMetrics, confidenceLevel
        );
    }
    
    /**
     * Calculates hotness score based on execution characteristics.
     */
    private double calculateHotnessScore(long callCount, double avgExecutionTime, double totalExecutionTime, int threadCount) {
        // Weighted scoring algorithm
        double callWeight = Math.min(callCount / 100.0, 1.0); // Normalized call frequency
        double timeWeight = Math.min(avgExecutionTime / 1_000_000.0, 1.0); // Normalized execution time
        double totalTimeWeight = Math.min(totalExecutionTime / 10_000_000_000.0, 1.0); // Normalized total time
        double threadWeight = Math.min(threadCount / 8.0, 1.0); // Normalized thread usage
        
        // Weights: call frequency (40%), execution time (30%), total time (20%), thread usage (10%)
        return (callWeight * 0.4 + timeWeight * 0.3 + totalTimeWeight * 0.2 + threadWeight * 0.1) * 100;
    }
    
    /**
     * Determines hotness level from score.
     */
    private HotnessLevel determineHotnessLevel(double hotnessScore) {
        if (hotnessScore >= 80) return HotnessLevel.CRITICAL;
        if (hotnessScore >= 60) return HotnessLevel.HOT;
        if (hotnessScore >= 40) return HotnessLevel.WARM;
        if (hotnessScore >= 20) return HotnessLevel.LUKEWARM;
        return HotnessLevel.COLD;
    }
    
    /**
     * Converts ExecutionPathTracker.HotnessLevel to HotnessLevel
     */
    private HotnessLevel convertToLocalHotnessLevel(ExecutionPathTracker.HotnessLevel execLevel) {
        if (execLevel == null) return HotnessLevel.COLD;
        
        switch (execLevel) {
            case EXTREMELY_HOT: return HotnessLevel.CRITICAL;
            case VERY_HOT: return HotnessLevel.CRITICAL;
            case HOT: return HotnessLevel.HOT;
            case WARM: return HotnessLevel.WARM;
            case COLD: return HotnessLevel.COLD;
            default: return HotnessLevel.COLD;
        }
    }
    
    /**
     * Calculates confidence level for analysis.
     */
    private ConfidenceLevel calculateConfidenceLevel(List<Map.Entry<ExecutionPathTracker.MethodExecution, ExecutionPathTracker.ExecutionStats>> methodStats, long totalCalls) {
        int methodCount = methodStats.size();
        double avgConsistency = methodStats.stream()
            .mapToDouble(e -> e.getValue().getConsistencyScore())
            .average()
            .orElse(0.0);
        
        if (methodCount >= 10 && totalCalls >= 1000 && avgConsistency >= 0.8) {
            return ConfidenceLevel.VERY_HIGH;
        } else if (methodCount >= 5 && totalCalls >= 500 && avgConsistency >= 0.6) {
            return ConfidenceLevel.HIGH;
        } else if (methodCount >= 3 && totalCalls >= 100 && avgConsistency >= 0.4) {
            return ConfidenceLevel.MEDIUM;
        } else if (totalCalls >= 50) {
            return ConfidenceLevel.LOW;
        } else {
            return ConfidenceLevel.VERY_LOW;
        }
    }
    
    private ConfidenceLevel calculateConfidenceLevel(ExecutionPathTracker.ExecutionStats stats) {
        long callCount = stats.getCallCount();
        double consistency = stats.getConsistencyScore();
        
        if (callCount >= 1000 && consistency >= 0.8) {
            return ConfidenceLevel.VERY_HIGH;
        } else if (callCount >= 500 && consistency >= 0.6) {
            return ConfidenceLevel.HIGH;
        } else if (callCount >= 100 && consistency >= 0.4) {
            return ConfidenceLevel.MEDIUM;
        } else if (callCount >= 50) {
            return ConfidenceLevel.LOW;
        } else {
            return ConfidenceLevel.VERY_LOW;
        }
    }
    
    /**
     * Generates optimization recommendations for a hot path.
     */
    private void generateRecommendations(HotPathAnalysis hotPath) {
        List<OptimizationRecommendation> recommendations = new ArrayList<>();
        
        // Analyze pattern and generate recommendations
        if (hotPath.getHotnessLevel() == HotnessLevel.CRITICAL || hotPath.getHotnessLevel() == HotnessLevel.HOT) {
            recommendations.addAll(generateCriticalOptimizations(hotPath));
        } else if (hotPath.getHotnessLevel() == HotnessLevel.HOT) {
            recommendations.addAll(generateStandardOptimizations(hotPath));
        } else {
            recommendations.addAll(generateLightOptimizations(hotPath));
        }
        
        hotPath.recommendations.addAll(recommendations);
    }
    
    /**
     * Generates recommendations for extremely hot paths.
     */
    private List<OptimizationRecommendation> generateCriticalOptimizations(HotPathAnalysis hotPath) {
        List<OptimizationRecommendation> recommendations = new ArrayList<>();
        
        // Critical optimizations
        recommendations.add(new OptimizationRecommendation(
            "CRITICAL_001", "Move to critical startup phase", RecommendationType.CODE_REORDERING,
            Priority.CRITICAL, 25.0, hotPath.getInvolvedMethods(),
            Arrays.asList("Move methods to CriticalStartupPhase", "Ensure parallel execution"),
            createMap("phase", "CRITICAL", "priority", "HIGHEST"), ConfidenceLevel.VERY_HIGH
        ));
        
        recommendations.add(new OptimizationRecommendation(
            "CRITICAL_002", "Implement parallel execution", RecommendationType.PARALLEL_EXECUTION,
            Priority.CRITICAL, 30.0, hotPath.getInvolvedMethods(),
            Arrays.asList("Split into independent tasks", "Execute in parallel subsystems"),
            createMap("parallelism", true, "threadCount", "available"), ConfidenceLevel.VERY_HIGH
        ));
        
        // Cache optimization if methods are called frequently
        if (hotPath.getPerformanceMetrics().getCallCount() > 1000) {
            recommendations.add(new OptimizationRecommendation(
                "CRITICAL_003", "Implement aggressive caching", RecommendationType.CACHE_OPTIMIZATION,
                Priority.HIGH, 40.0, hotPath.getInvolvedMethods(),
                Arrays.asList("Cache results", "Use memory-mapped files", "Preload frequently used data"),
                createMap("cacheSize", "unbounded", "strategy", "LRU"), ConfidenceLevel.HIGH
            ));
        }
        
        return recommendations;
    }
    
    /**
     * Generates recommendations for hot paths.
     */
    private List<OptimizationRecommendation> generateStandardOptimizations(HotPathAnalysis hotPath) {
        List<OptimizationRecommendation> recommendations = new ArrayList<>();
        
        recommendations.add(new OptimizationRecommendation(
            "STANDARD_001", "Optimize execution order", RecommendationType.CODE_REORDERING,
            Priority.HIGH, 15.0, hotPath.getInvolvedMethods(),
            Arrays.asList("Reorder methods by dependency", "Minimize blocking operations"),
            createMap("reorderStrategy", "dependency-based"), ConfidenceLevel.HIGH
        ));
        
        recommendations.add(new OptimizationRecommendation(
            "STANDARD_002", "Consider method inlining", RecommendationType.METHOD_INLINING,
            Priority.MEDIUM, 10.0, hotPath.getInvolvedMethods(),
            Arrays.asList("Inline small methods", "Reduce method call overhead"),
            createMap("inlineThreshold", 50), ConfidenceLevel.MEDIUM
        ));
        
        return recommendations;
    }
    
    /**
     * Generates recommendations for warm paths.
     */
    private List<OptimizationRecommendation> generateLightOptimizations(HotPathAnalysis hotPath) {
        List<OptimizationRecommendation> recommendations = new ArrayList<>();
        
        recommendations.add(new OptimizationRecommendation(
            "LIGHT_001", "Move to background phase", RecommendationType.EARLY_INITIALIZATION,
            Priority.MEDIUM, 5.0, hotPath.getInvolvedMethods(),
            Arrays.asList("Move to BackgroundStartupPhase", "Execute asynchronously"),
            createMap("phase", "BACKGROUND"), ConfidenceLevel.LOW
        ));
        
        return recommendations;
    }
    
    /**
     * Gets cached analysis results.
     */
    public List<HotPathAnalysis> getCachedAnalysis() {
        return new ArrayList<>(cachedAnalysis);
    }
    
    /**
     * Clears analysis cache.
     */
    public void clearCache() {
        cachedAnalysis.clear();
    }
    
    /**
     * Gets analysis statistics.
     */
    public int getAnalysisCount() { return analysisCount.get(); }
    
    /**
     * Gets the tracker used for analysis.
     */
    public ExecutionPathTracker getTracker() { return tracker; }
    
    /**
     * Shuts down the analyzer and cleans up resources.
     */
    public void shutdown() {
        analysisExecutor.shutdown();
        try {
            if (!analysisExecutor.awaitTermination(30, TimeUnit.SECONDS)) {
                analysisExecutor.shutdownNow();
            }
        } catch (InterruptedException e) {
            analysisExecutor.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
}