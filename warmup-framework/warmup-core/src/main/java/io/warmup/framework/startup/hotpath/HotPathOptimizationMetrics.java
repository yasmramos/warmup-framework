package io.warmup.framework.startup.hotpath;

import java.util.*;
import java.util.stream.Collectors;
import java.util.concurrent.atomic.AtomicLong;
import java.lang.Math;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Comprehensive metrics collection and analysis for Hot Path Optimization System.
 * Tracks performance, efficiency, and optimization impact across multiple dimensions.
 * 
 * @author MiniMax Agent
 * @version 1.0
 */
public class HotPathOptimizationMetrics {
    
    /**
     * Metrics snapshot at a specific point in time.
     */
    public static class MetricsSnapshot {
        private final Instant timestamp;
        private final long trackingDuration;
        private final int methodsTracked;
        private final int hotPathsIdentified;
        private final double averageHotnessScore;
        private final double maxHotnessScore;
        private final Map<HotnessLevel, Integer> hotnessDistribution;
        private final Map<ConfidenceLevel, Integer> confidenceDistribution;
        private final Map<OptimizationType, Integer> optimizationTypes;
        private final Map<RiskLevel, Integer> riskDistribution;
        private final PerformanceStatistics performanceStats;
        private final ResourceUsageStats resourceStats;
        
        public MetricsSnapshot(Instant timestamp, long trackingDuration, int methodsTracked,
                              int hotPathsIdentified, double averageHotnessScore, double maxHotnessScore,
                              Map<HotnessLevel, Integer> hotnessDistribution,
                              Map<ConfidenceLevel, Integer> confidenceDistribution,
                              Map<OptimizationType, Integer> optimizationTypes,
                              Map<RiskLevel, Integer> riskDistribution,
                              PerformanceStatistics performanceStats,
                              ResourceUsageStats resourceStats) {
            this.timestamp = timestamp;
            this.trackingDuration = trackingDuration;
            this.methodsTracked = methodsTracked;
            this.hotPathsIdentified = hotPathsIdentified;
            this.averageHotnessScore = averageHotnessScore;
            this.maxHotnessScore = maxHotnessScore;
            this.hotnessDistribution = new HashMap<>(hotnessDistribution);
            this.confidenceDistribution = new HashMap<>(confidenceDistribution);
            this.optimizationTypes = new HashMap<>(optimizationTypes);
            this.riskDistribution = new HashMap<>(riskDistribution);
            this.performanceStats = performanceStats;
            this.resourceStats = resourceStats;
        }
        
        public Instant getTimestamp() { return timestamp; }
        public long getTrackingDuration() { return trackingDuration; }
        public int getMethodsTracked() { return methodsTracked; }
        public int getHotPathsIdentified() { return hotPathsIdentified; }
        public double getAverageHotnessScore() { return averageHotnessScore; }
        public double getMaxHotnessScore() { return maxHotnessScore; }
        public Map<HotnessLevel, Integer> getHotnessDistribution() { return new HashMap<>(hotnessDistribution); }
        public Map<ConfidenceLevel, Integer> getConfidenceDistribution() { return new HashMap<>(confidenceDistribution); }
        public Map<OptimizationType, Integer> getOptimizationTypes() { return new HashMap<>(optimizationTypes); }
        public Map<RiskLevel, Integer> getRiskDistribution() { return new HashMap<>(riskDistribution); }
        public PerformanceStatistics getPerformanceStats() { return performanceStats; }
        public ResourceUsageStats getResourceStats() { return resourceStats; }
        
        @Override
        public String toString() {
            return String.format("MetricsSnapshot{timestamp=%s, methods=%d, hotPaths=%d, avgHotness=%.1f, maxHotness=%.1f}",
                timestamp, methodsTracked, hotPathsIdentified, averageHotnessScore, maxHotnessScore);
        }
    }
    
    /**
     * Performance statistics for optimization operations.
     */
    public static class PerformanceStatistics {
        private final long totalOptimizationTime;
        private final long averageAnalysisTime;
        private final long averageOptimizationTime;
        private final double totalExpectedImprovement;
        private final double totalActualImprovement;
        private final double improvementEfficiency;
        private final int totalOptimizationsApplied;
        private final double successRate;
        private final long averageMethodCallReduction;
        
        public PerformanceStatistics(long totalOptimizationTime, long averageAnalysisTime,
                                    long averageOptimizationTime, double totalExpectedImprovement,
                                    double totalActualImprovement, double improvementEfficiency,
                                    int totalOptimizationsApplied, double successRate,
                                    long averageMethodCallReduction) {
            this.totalOptimizationTime = totalOptimizationTime;
            this.averageAnalysisTime = averageAnalysisTime;
            this.averageOptimizationTime = averageOptimizationTime;
            this.totalExpectedImprovement = totalExpectedImprovement;
            this.totalActualImprovement = totalActualImprovement;
            this.improvementEfficiency = improvementEfficiency;
            this.totalOptimizationsApplied = totalOptimizationsApplied;
            this.successRate = successRate;
            this.averageMethodCallReduction = averageMethodCallReduction;
        }
        
        public long getTotalOptimizationTime() { return totalOptimizationTime; }
        public long getAverageAnalysisTime() { return averageAnalysisTime; }
        public long getAverageOptimizationTime() { return averageOptimizationTime; }
        public double getTotalExpectedImprovement() { return totalExpectedImprovement; }
        public double getTotalActualImprovement() { return totalActualImprovement; }
        public double getImprovementEfficiency() { return improvementEfficiency; }
        public int getTotalOptimizationsApplied() { return totalOptimizationsApplied; }
        public double getSuccessRate() { return successRate; }
        public long getAverageMethodCallReduction() { return averageMethodCallReduction; }
        
        public String toDetailedString() {
            return String.format("PerformanceStats{optTime=%.2fms, analysisTime=%.2fms, expected=%.1f%%, actual=%.1f%%, efficiency=%.1f%%, success=%.1f%%}",
                totalOptimizationTime / 1_000_000.0, averageAnalysisTime / 1_000_000.0,
                totalExpectedImprovement, totalActualImprovement, improvementEfficiency * 100, successRate * 100);
        }
    }
    
    /**
     * Resource usage statistics.
     */
    public static class ResourceUsageStats {
        private final long peakMemoryUsage;
        private final long averageMemoryUsage;
        private final double cpuUtilization;
        private final int threadCount;
        private final long ioOperations;
        private final long networkOperations;
        private final Map<String, Long> operationBreakdown;
        
        public ResourceUsageStats(long peakMemoryUsage, long averageMemoryUsage,
                                 double cpuUtilization, int threadCount,
                                 long ioOperations, long networkOperations,
                                 Map<String, Long> operationBreakdown) {
            this.peakMemoryUsage = peakMemoryUsage;
            this.averageMemoryUsage = averageMemoryUsage;
            this.cpuUtilization = cpuUtilization;
            this.threadCount = threadCount;
            this.ioOperations = ioOperations;
            this.networkOperations = networkOperations;
            this.operationBreakdown = new HashMap<>(operationBreakdown);
        }
        
        public long getPeakMemoryUsage() { return peakMemoryUsage; }
        public long getAverageMemoryUsage() { return averageMemoryUsage; }
        public double getCpuUtilization() { return cpuUtilization; }
        public int getThreadCount() { return threadCount; }
        public long getIoOperations() { return ioOperations; }
        public long getNetworkOperations() { return networkOperations; }
        public Map<String, Long> getOperationBreakdown() { return new HashMap<>(operationBreakdown); }
        
        public String toDetailedString() {
            return String.format("ResourceStats{memory=%.2fMB, cpu=%.1f%%, threads=%d, io=%d, network=%d}",
                averageMemoryUsage / (1024.0 * 1024.0), cpuUtilization, threadCount, ioOperations, networkOperations);
        }
    }
    
    /**
     * Trend analysis for metrics over time.
     */
    public static class TrendAnalysis {
        private final TrendDirection hotnessTrend;
        private final TrendDirection performanceTrend;
        private final TrendDirection efficiencyTrend;
        private final double improvementVelocity;
        private final List<String> keyInsights;
        private final List<String> warnings;
        private final List<String> recommendations;
        
        public TrendAnalysis(TrendDirection hotnessTrend, TrendDirection performanceTrend,
                            TrendDirection efficiencyTrend, double improvementVelocity,
                            List<String> keyInsights, List<String> warnings,
                            List<String> recommendations) {
            this.hotnessTrend = hotnessTrend;
            this.performanceTrend = performanceTrend;
            this.efficiencyTrend = efficiencyTrend;
            this.improvementVelocity = improvementVelocity;
            this.keyInsights = new ArrayList<>(keyInsights);
            this.warnings = new ArrayList<>(warnings);
            this.recommendations = new ArrayList<>(recommendations);
        }
        
        public TrendDirection getHotnessTrend() { return hotnessTrend; }
        public TrendDirection getPerformanceTrend() { return performanceTrend; }
        public TrendDirection getEfficiencyTrend() { return efficiencyTrend; }
        public double getImprovementVelocity() { return improvementVelocity; }
        public List<String> getKeyInsights() { return new ArrayList<>(keyInsights); }
        public List<String> getWarnings() { return new ArrayList<>(warnings); }
        public List<String> getRecommendations() { return new ArrayList<>(recommendations); }
        
        public String generateReport() {
            StringBuilder sb = new StringBuilder();
            sb.append("=== Hot Path Optimization Trend Analysis ===\n");
            sb.append(String.format("Hotness Trend: %s\n", hotnessTrend));
            sb.append(String.format("Performance Trend: %s\n", performanceTrend));
            sb.append(String.format("Efficiency Trend: %s\n", efficiencyTrend));
            sb.append(String.format("Improvement Velocity: %.2f%%/minute\n", improvementVelocity));
            
            if (!keyInsights.isEmpty()) {
                sb.append("\n=== Key Insights ===\n");
                keyInsights.forEach(insight -> sb.append("- " + insight + "\n"));
            }
            
            if (!warnings.isEmpty()) {
                sb.append("\n=== Warnings ===\n");
                warnings.forEach(warning -> sb.append("⚠️ " + warning + "\n"));
            }
            
            if (!recommendations.isEmpty()) {
                sb.append("\n=== Recommendations ===\n");
                recommendations.forEach(rec -> sb.append("💡 " + rec + "\n"));
            }
            
            return sb.toString();
        }
    }
    
    /**
     * Trend directions for analysis.
     */
    public enum TrendDirection {
        IMPROVING("Improving"),
        STABLE("Stable"),
        DECLINING("Declining"),
        VOLATILE("Volatile"),
        INSUFFICIENT_DATA("Insufficient Data");
        
        private final String description;
        
        TrendDirection(String description) {
            this.description = description;
        }
        
        public String getDescription() { return description; }
    }
    
    // Metrics tracking
    private final AtomicLong totalTrackingTime = new AtomicLong(0);
    private final AtomicInteger totalMethodsTracked = new AtomicInteger(0);
    private final AtomicInteger totalHotPathsIdentified = new AtomicInteger(0);
    private final AtomicLong totalExpectedImprovement = new AtomicLong(0);
    private final AtomicLong totalActualImprovement = new AtomicLong(0);
    private final AtomicInteger totalOptimizationsApplied = new AtomicInteger(0);
    private final AtomicReference<Map<HotnessLevel, AtomicInteger>> hotnessDistribution = 
        new AtomicReference<>(new EnumMap<>(HotnessLevel.class));
    private final AtomicReference<Map<ConfidenceLevel, AtomicInteger>> confidenceDistribution =
        new AtomicReference<>(new EnumMap<>(ConfidenceLevel.class));
    private final AtomicReference<Map<OptimizationType, AtomicInteger>> optimizationTypeDistribution =
        new AtomicReference<>(new EnumMap<>(OptimizationType.class));
    private final AtomicReference<Map<RiskLevel, AtomicInteger>> riskDistribution =
        new AtomicReference<>(new EnumMap<>(RiskLevel.class));
    
    // Resource tracking
    private final AtomicLong peakMemoryUsage = new AtomicLong(0);
    private final AtomicLong totalMemoryUsage = new AtomicLong(0);
    private final AtomicInteger memoryReadings = new AtomicInteger(0);
    private final AtomicLong peakCpuUtilization = new AtomicLong(0);
    private final AtomicLong totalCpuUtilization = new AtomicLong(0);
    private final AtomicInteger cpuReadings = new AtomicInteger(0);
    
    // Historical data
    private final List<MetricsSnapshot> metricsHistory = new ArrayList<>();
    private final List<PerformanceStatistics> performanceHistory = new ArrayList<>();
    
    public HotPathOptimizationMetrics() {
        initializeCounters();
    }
    
    private void initializeCounters() {
        Map<HotnessLevel, AtomicInteger> hotnessMap = hotnessDistribution.get();
        Map<ConfidenceLevel, AtomicInteger> confidenceMap = confidenceDistribution.get();
        Map<OptimizationType, AtomicInteger> optimizationMap = optimizationTypeDistribution.get();
        Map<RiskLevel, AtomicInteger> riskMap = riskDistribution.get();
        
        for (HotnessLevel level : HotnessLevel.values()) {
            hotnessMap.put(level, new AtomicInteger(0));
        }
        
        for (ConfidenceLevel level : ConfidenceLevel.values()) {
            confidenceMap.put(level, new AtomicInteger(0));
        }
        
        for (OptimizationType type : OptimizationType.values()) {
            optimizationMap.put(type, new AtomicInteger(0));
        }
        
        for (RiskLevel level : RiskLevel.values()) {
            riskMap.put(level, new AtomicInteger(0));
        }
        
        hotnessDistribution.set(hotnessMap);
        confidenceDistribution.set(confidenceMap);
        optimizationTypeDistribution.set(optimizationMap);
        riskDistribution.set(riskMap);
    }
    
    /**
     * Records tracking metrics.
     */
    public void recordTracking(long trackingDuration, int methodsTracked) {
        totalTrackingTime.addAndGet(trackingDuration);
        totalMethodsTracked.addAndGet(methodsTracked);
        
        // Update memory usage
        Runtime runtime = Runtime.getRuntime();
        long usedMemory = runtime.totalMemory() - runtime.freeMemory();
        
        long currentPeak = peakMemoryUsage.get();
        while (usedMemory > currentPeak) {
            if (peakMemoryUsage.compareAndSet(currentPeak, usedMemory)) {
                break;
            }
            currentPeak = peakMemoryUsage.get();
        }
        
        totalMemoryUsage.addAndGet(usedMemory);
        memoryReadings.incrementAndGet();
    }
    
    /**
     * Records hot path analysis results.
     */
    public void recordHotPathAnalysis(List<HotPathAnalysis> hotPaths) {
        totalHotPathsIdentified.addAndGet(hotPaths.size());
        
        if (!hotPaths.isEmpty()) {
            // Update distributions
            for (HotPathAnalysis path : hotPaths) {
                hotnessDistribution.get().get(path.getHotnessLevel()).incrementAndGet();
                confidenceDistribution.get().get(path.getConfidenceLevel()).incrementAndGet();
            }
        }
    }
    
    /**
     * Records optimization plan generation.
     */
    public void recordOptimizationPlanGeneration(List<CodeReorderingPlan> plans) {
        for (CodeReorderingPlan plan : plans) {
            optimizationTypeDistribution.get()
                .values()
                .forEach(type -> type.incrementAndGet());
            
            riskDistribution.get().get(plan.getRiskLevel()).incrementAndGet();
            
            totalExpectedImprovement.addAndGet((long) plan.getExpectedImprovement());
        }
    }
    
    /**
     * Records optimization application results.
     */
    public void recordOptimizationApplication(List<OptimizationResult> results) {
        totalOptimizationsApplied.addAndGet(results.size());
        
        for (OptimizationResult result : results) {
            totalActualImprovement.addAndGet((long) result.getActualImprovement());
        }
    }
    
    /**
     * Records CPU utilization.
     */
    public void recordCpuUtilization(double cpuUsage) {
        long cpuUsageLong = (long) (cpuUsage * 100); // Convert to percentage
        
        long currentPeak = peakCpuUtilization.get();
        while (cpuUsageLong > currentPeak) {
            if (peakCpuUtilization.compareAndSet(currentPeak, cpuUsageLong)) {
                break;
            }
            currentPeak = peakCpuUtilization.get();
        }
        
        totalCpuUtilization.addAndGet(cpuUsageLong);
        cpuReadings.incrementAndGet();
    }
    
    /**
     * Creates a metrics snapshot.
     */
    public MetricsSnapshot createSnapshot() {
        Instant timestamp = Instant.now();
        
        Map<HotnessLevel, Integer> hotnessDist = hotnessDistribution.get().entrySet().stream()
            .collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue().get()));
        
        Map<ConfidenceLevel, Integer> confidenceDist = confidenceDistribution.get().entrySet().stream()
            .collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue().get()));
        
        Map<OptimizationType, Integer> optimizationTypes = optimizationTypeDistribution.get().entrySet().stream()
            .collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue().get()));
        
        Map<RiskLevel, Integer> riskDist = riskDistribution.get().entrySet().stream()
            .collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue().get()));
        
        // Calculate average scores
        List<HotPathAnalysis> recentAnalyses = getRecentAnalyses(100); // Last 100 analyses
        double avgHotnessScore = recentAnalyses.stream()
            .mapToDouble(h -> h.getAverageHotnessScore())
            .average()
            .orElse(0.0);
        
        double maxHotnessScore = recentAnalyses.stream()
            .mapToDouble(h -> h.getMaxHotnessScore())
            .max()
            .orElse(0.0);
        
        PerformanceStatistics performanceStats = calculatePerformanceStatistics();
        ResourceUsageStats resourceStats = calculateResourceUsageStats();
        
        MetricsSnapshot snapshot = new MetricsSnapshot(
            timestamp, totalTrackingTime.get(), totalMethodsTracked.get(),
            totalHotPathsIdentified.get(), avgHotnessScore, maxHotnessScore,
            hotnessDist, confidenceDist, optimizationTypes, riskDist,
            performanceStats, resourceStats
        );
        
        metricsHistory.add(snapshot);
        return snapshot;
    }
    
    /**
     * Performs trend analysis based on historical data.
     */
    public TrendAnalysis performTrendAnalysis(Duration analysisWindow) {
        List<MetricsSnapshot> recentSnapshots = getSnapshotsInWindow(analysisWindow);
        
        if (recentSnapshots.size() < 2) {
            return new TrendAnalysis(
                TrendDirection.INSUFFICIENT_DATA,
                TrendDirection.INSUFFICIENT_DATA,
                TrendDirection.INSUFFICIENT_DATA,
                0.0,
                Arrays.asList("Insufficient data for trend analysis"),
                Arrays.asList("Collect more data points for meaningful analysis"),
                Arrays.asList("Extend analysis window or run more optimization cycles")
            );
        }
        
        TrendDirection hotnessTrend = analyzeHotnessTrend(recentSnapshots);
        TrendDirection performanceTrend = analyzePerformanceTrend(recentSnapshots);
        TrendDirection efficiencyTrend = analyzeEfficiencyTrend(recentSnapshots);
        double improvementVelocity = calculateImprovementVelocity(recentSnapshots);
        
        List<String> insights = generateKeyInsights(recentSnapshots);
        List<String> warnings = generateWarnings(recentSnapshots);
        List<String> recommendations = generateRecommendations(hotnessTrend, performanceTrend, efficiencyTrend);
        
        return new TrendAnalysis(hotnessTrend, performanceTrend, efficiencyTrend, 
                               improvementVelocity, insights, warnings, recommendations);
    }
    
    private TrendDirection analyzeHotnessTrend(List<MetricsSnapshot> snapshots) {
        if (snapshots.size() < 3) {
            return TrendDirection.INSUFFICIENT_DATA;
        }
        
        double variance = calculateVariance(snapshots.stream()
            .mapToDouble(MetricsSnapshot::getAverageHotnessScore)
            .toArray());
        
        if (variance > 100) {
            return TrendDirection.VOLATILE;
        }
        
        double recentAvg = snapshots.subList(snapshots.size() - 3, snapshots.size()).stream()
            .mapToDouble(MetricsSnapshot::getAverageHotnessScore)
            .average()
            .orElse(0.0);
        
        double earlyAvg = snapshots.subList(0, 3).stream()
            .mapToDouble(MetricsSnapshot::getAverageHotnessScore)
            .average()
            .orElse(0.0);
        
        double change = recentAvg - earlyAvg;
        
        if (change > 10) return TrendDirection.IMPROVING;
        if (change < -10) return TrendDirection.DECLINING;
        return TrendDirection.STABLE;
    }
    
    private TrendDirection analyzePerformanceTrend(List<MetricsSnapshot> snapshots) {
        // Similar analysis for performance metrics
        if (snapshots.size() < 3) {
            return TrendDirection.INSUFFICIENT_DATA;
        }
        
        List<Double> efficiency = snapshots.stream()
            .map(s -> s.getPerformanceStats().getImprovementEfficiency())
            .collect(Collectors.toList());
        
        double variance = calculateVariance(efficiency.stream().mapToDouble(Double::doubleValue).toArray());
        
        if (variance > 0.1) {
            return TrendDirection.VOLATILE;
        }
        
        double recentAvg = efficiency.subList(efficiency.size() - 3, efficiency.size()).stream()
            .mapToDouble(Double::doubleValue)
            .average()
            .orElse(0.0);
        
        double earlyAvg = efficiency.subList(0, 3).stream()
            .mapToDouble(Double::doubleValue)
            .average()
            .orElse(0.0);
        
        double change = recentAvg - earlyAvg;
        
        if (change > 0.1) return TrendDirection.IMPROVING;
        if (change < -0.1) return TrendDirection.DECLINING;
        return TrendDirection.STABLE;
    }
    
    private TrendDirection analyzeEfficiencyTrend(List<MetricsSnapshot> snapshots) {
        return analyzePerformanceTrend(snapshots); // Simplified for now
    }
    
    private double calculateImprovementVelocity(List<MetricsSnapshot> snapshots) {
        if (snapshots.size() < 2) return 0.0;
        
        Instant firstTime = snapshots.get(0).getTimestamp();
        Instant lastTime = snapshots.get(snapshots.size() - 1).getTimestamp();
        
        Duration timeSpan = Duration.between(firstTime, lastTime);
        if (timeSpan.isZero()) return 0.0;
        
        double improvementChange = snapshots.get(snapshots.size() - 1).getPerformanceStats().getTotalActualImprovement() -
                                 snapshots.get(0).getPerformanceStats().getTotalActualImprovement();
        
        return improvementChange / (timeSpan.toMinutes() + 1); // Prevent division by zero
    }
    
    private List<String> generateKeyInsights(List<MetricsSnapshot> snapshots) {
        List<String> insights = new ArrayList<>();
        
        MetricsSnapshot latest = snapshots.get(snapshots.size() - 1);
        
        if (latest.getHotPathsIdentified() > 50) {
            insights.add("High number of hot paths identified - significant optimization opportunities detected");
        }
        
        if (latest.getPerformanceStats().getImprovementEfficiency() > 0.8) {
            insights.add("High optimization efficiency - optimization plans are highly effective");
        }
        
        if (latest.getResourceStats().getCpuUtilization() > 80) {
            insights.add("High CPU utilization during optimization - consider reducing optimization complexity");
        }
        
        return insights;
    }
    
    private List<String> generateWarnings(List<MetricsSnapshot> snapshots) {
        List<String> warnings = new ArrayList<>();
        
        MetricsSnapshot latest = snapshots.get(snapshots.size() - 1);
        
        if (latest.getPerformanceStats().getSuccessRate() < 0.6) {
            warnings.add("Low optimization success rate - review optimization strategies and risk tolerance");
        }
        
        if (latest.getResourceStats().getPeakMemoryUsage() > 500 * 1024 * 1024) { // 500MB
            warnings.add("High memory usage during optimization - may impact system stability");
        }
        
        return warnings;
    }
    
    private List<String> generateRecommendations(TrendDirection hotnessTrend, 
                                                TrendDirection performanceTrend,
                                                TrendDirection efficiencyTrend) {
        List<String> recommendations = new ArrayList<>();
        
        if (hotnessTrend == TrendDirection.IMPROVING && performanceTrend == TrendDirection.DECLINING) {
            recommendations.add("Hot paths are improving but performance is declining - review optimization strategy");
        }
        
        if (efficiencyTrend == TrendDirection.DECLINING) {
            recommendations.add("Efficiency declining - consider adjusting optimization parameters");
        }
        
        return recommendations;
    }
    
    private List<HotPathAnalysis> getRecentAnalyses(int count) {
        // In a real implementation, this would fetch from a metrics store
        return new ArrayList<>(); // Placeholder
    }
    
    private List<MetricsSnapshot> getSnapshotsInWindow(Duration window) {
        Instant cutoffTime = Instant.now().minus(window);
        return metricsHistory.stream()
            .filter(snapshot -> snapshot.getTimestamp().isAfter(cutoffTime))
            .collect(Collectors.toList());
    }
    
    private double calculateVariance(double[] values) {
        if (values.length == 0) return 0.0;
        
        double mean = Arrays.stream(values).average().orElse(0.0);
        double sumSquaredDiff = 0.0;
        for (double value : values) {
            sumSquaredDiff += Math.pow(value - mean, 2);
        }
        return sumSquaredDiff / values.length;
    }
    
    private PerformanceStatistics calculatePerformanceStatistics() {
        long totalOptTime = totalOptimizationsApplied.get() > 0 ? 
            totalTrackingTime.get() / totalOptimizationsApplied.get() : 0;
        
        double expectedImp = totalExpectedImprovement.get() > 0 ? 
            (double) totalExpectedImprovement.get() / 100.0 : 0.0;
        
        double actualImp = totalActualImprovement.get() > 0 ? 
            (double) totalActualImprovement.get() / 100.0 : 0.0;
        
        double efficiency = expectedImp > 0 ? actualImp / expectedImp : 0.0;
        
        double successRate = performanceHistory.isEmpty() ? 0.0 : 
            performanceHistory.stream()
                .mapToDouble(PerformanceStatistics::getSuccessRate)
                .average()
                .orElse(0.0);
        
        return new PerformanceStatistics(
            totalTrackingTime.get(), totalOptTime, totalOptTime,
            expectedImp, actualImp, efficiency,
            totalOptimizationsApplied.get(), successRate, 0L
        );
    }
    
    private ResourceUsageStats calculateResourceUsageStats() {
        long avgMemory = memoryReadings.get() > 0 ? 
            totalMemoryUsage.get() / memoryReadings.get() : 0;
        
        double avgCpu = cpuReadings.get() > 0 ? 
            (double) totalCpuUtilization.get() / cpuReadings.get() / 100.0 : 0.0;
        
        return new ResourceUsageStats(
            peakMemoryUsage.get(), avgMemory,
            avgCpu, Runtime.getRuntime().availableProcessors(),
            0L, 0L, new HashMap<>()
        );
    }
    
    /**
     * Gets current metrics snapshot.
     */
    public MetricsSnapshot getCurrentSnapshot() {
        return createSnapshot();
    }
    
    /**
     * Gets metrics history.
     */
    public List<MetricsSnapshot> getMetricsHistory() {
        return new ArrayList<>(metricsHistory);
    }
    
    /**
     * Gets overall statistics.
     */
    public Map<String, Object> getOverallStatistics() {
        Map<String, Object> stats = new HashMap<>();
        
        stats.put("totalTrackingTime", totalTrackingTime.get());
        stats.put("totalMethodsTracked", totalMethodsTracked.get());
        stats.put("totalHotPathsIdentified", totalHotPathsIdentified.get());
        stats.put("totalExpectedImprovement", totalExpectedImprovement.get());
        stats.put("totalActualImprovement", totalActualImprovement.get());
        stats.put("totalOptimizationsApplied", totalOptimizationsApplied.get());
        stats.put("peakMemoryUsage", peakMemoryUsage.get());
        stats.put("peakCpuUtilization", peakCpuUtilization.get() / 100.0);
        
        return stats;
    }
    
    /**
     * Resets all metrics (use with caution).
     */
    public void reset() {
        totalTrackingTime.set(0);
        totalMethodsTracked.set(0);
        totalHotPathsIdentified.set(0);
        totalExpectedImprovement.set(0);
        totalActualImprovement.set(0);
        totalOptimizationsApplied.set(0);
        peakMemoryUsage.set(0);
        totalMemoryUsage.set(0);
        memoryReadings.set(0);
        peakCpuUtilization.set(0);
        totalCpuUtilization.set(0);
        cpuReadings.set(0);
        metricsHistory.clear();
        performanceHistory.clear();
        initializeCounters();
    }
}