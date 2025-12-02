/*
 * Copyright (c) 2025 Warmup Framework. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.warmup.framework.startup.bootstrap;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * Comprehensive metrics and statistics tracking for critical class preloading.
 * This class provides detailed performance analysis, memory usage tracking,
 * and optimization insights for the bootstrap class loading system.
 * 
 * Key Features:
 * - Real-time performance monitoring
 * - Memory usage analysis
 * - Cache effectiveness metrics
 * - Load time distribution analysis
 * - Optimization recommendations
 * - Historical performance trends
 * 
 * @author Warmup Framework Team
 * @version 1.0.0
 */
public final class ClassPreloadMetrics {
    
    private static final Logger logger = Logger.getLogger(ClassPreloadMetrics.class.getName());
    
    // Core performance metrics
    private final AtomicReference<PreloadSession> currentSession = new AtomicReference<>();
    private final List<PreloadSession> historicalSessions = Collections.synchronizedList(new ArrayList<>());
    
    // Detailed timing and performance data
    private final Map<String, ClassLoadMetrics> classMetrics = new ConcurrentHashMap<>();
    private final Map<Integer, TierMetrics> tierMetrics = new ConcurrentHashMap<>();
    
    // Memory and resource tracking
    private final MemoryTracker memoryTracker = new MemoryTracker();
    
    // Performance thresholds and optimization
    private final PerformanceThresholds thresholds = new PerformanceThresholds();
    private final OptimizationAdvisor advisor = new OptimizationAdvisor();
    
    /**
     * Starts a new preload metrics session.
     * 
     * @param sessionId Unique identifier for this preload session
     * @param classCount Total number of classes to preload
     */
    public void startPreloadSession(String sessionId, int classCount) {
        PreloadSession session = new PreloadSession(sessionId, classCount);
        
        if (currentSession.compareAndSet(null, session)) {
            logger.info(String.format("Started preload metrics session: %s (targeting %d classes)", 
                    sessionId, classCount));
        } else {
            logger.warning(String.format("Preload session already active, overriding with: %s", sessionId));
            currentSession.set(session);
        }
        
        // Initialize tier metrics
        for (int tier = 1; tier <= 3; tier++) {
            tierMetrics.put(tier, new TierMetrics(tier));
        }
    }
    
    /**
     * Records the start of a tier preload operation.
     * 
     * @param tier The priority tier being loaded
     * @param classCount Number of classes in this tier
     */
    public void recordTierStart(int tier, int classCount) {
        PreloadSession session = currentSession.get();
        if (session != null) {
            session.recordTierStart(tier, classCount);
            TierMetrics tierMetric = tierMetrics.get(tier);
            if (tierMetric != null) {
                tierMetric.recordStart(classCount);
            }
        }
    }
    
    /**
     * Records the completion of a class load operation.
     * 
     * @param className The loaded class name
     * @param loadTimeNanos Time taken to load the class
     * @param tier The priority tier of this class
     * @param success Whether the load was successful
     */
    public void recordClassLoad(String className, long loadTimeNanos, int tier, boolean success) {
        PreloadSession session = currentSession.get();
        if (session != null) {
            session.recordClassLoad(className, loadTimeNanos, tier, success);
        }
        
        // Update class-specific metrics
        classMetrics.computeIfAbsent(className, k -> new ClassLoadMetrics(className))
                   .recordLoad(loadTimeNanos, success);
        
        // Update tier metrics
        TierMetrics tierMetric = tierMetrics.get(tier);
        if (tierMetric != null) {
            tierMetric.recordLoad(loadTimeNanos, success);
        }
        
        // Update memory tracking
        memoryTracker.recordClassLoad(success);
    }
    
    /**
     * Records the completion of a tier preload operation.
     * 
     * @param tier The completed priority tier
     * @param loadedClasses Number of successfully loaded classes
     * @param failedClasses Number of failed classes
     * @param totalTimeNanos Total time taken for the tier
     */
    public void recordTierComplete(int tier, int loadedClasses, int failedClasses, long totalTimeNanos) {
        PreloadSession session = currentSession.get();
        if (session != null) {
            session.recordTierComplete(tier, loadedClasses, failedClasses, totalTimeNanos);
        }
        
        TierMetrics tierMetric = tierMetrics.get(tier);
        if (tierMetric != null) {
            tierMetric.recordComplete(loadedClasses, failedClasses, totalTimeNanos);
        }
    }
    
    /**
     * Completes the current preload session and finalizes metrics.
     * 
     * @return Complete preload metrics report
     */
    public PreloadMetricsReport finalizeSession() {
        PreloadSession session = currentSession.getAndSet(null);
        if (session == null) {
            logger.warning("No active preload session to finalize");
            return new PreloadMetricsReport();
        }
        
        session.finalizeSession();
        historicalSessions.add(session);
        
        logger.info(String.format("Finalized preload session: %s with %d classes in %dms",
                session.getSessionId(), session.getTotalClasses(), 
                session.getTotalTimeMs()));
        
        return generateReport(session);
    }
    
    /**
     * Generates a comprehensive preload metrics report.
     * 
     * @param session The preload session to analyze
     * @return Detailed metrics report
     */
    private PreloadMetricsReport generateReport(PreloadSession session) {
        PreloadMetricsReport report = new PreloadMetricsReport();
        
        // Basic session information
        report.setSessionId(session.getSessionId());
        report.setSessionStartTime(session.getStartTime());
        report.setSessionEndTime(session.getEndTime());
        report.setTotalTimeMs(session.getTotalTimeMs());
        
        // Overall performance metrics
        report.setTotalClasses(session.getTotalClasses());
        report.setSuccessfullyLoaded(session.getSuccessfullyLoaded());
        report.setFailedLoads(session.getFailedLoads());
        report.setSuccessRate(session.getSuccessRate());
        
        // Performance distribution
        report.setAverageLoadTimeMs(session.getAverageLoadTimeMs());
        report.setMinLoadTimeMs(session.getMinLoadTimeMs());
        report.setMaxLoadTimeMs(session.getMaxLoadTimeMs());
        report.setMedianLoadTimeMs(session.getMedianLoadTimeMs());
        report.setLoadTimeStdDev(session.getLoadTimeStdDev());
        
        // Throughput metrics
        report.setClassesPerSecond(session.getClassesPerSecond());
        report.setThroughputPercentile(session.getThroughputPercentile());
        
        // Tier-specific analysis
        Map<Integer, TierAnalysis> tierAnalysis = new HashMap<>();
        for (Map.Entry<Integer, TierMetrics> entry : tierMetrics.entrySet()) {
            tierAnalysis.put(entry.getKey(), entry.getValue().generateAnalysis());
        }
        report.setTierAnalysis(tierAnalysis);
        
        // Memory usage analysis
        report.setMemoryUsageReport(memoryTracker.generateReport());
        
        // Performance thresholds analysis
        report.setThresholdAnalysis(thresholds.analyze(session));
        
        // Optimization recommendations
        report.setOptimizationRecommendations(advisor.generateRecommendations(session, tierAnalysis));
        
        // Historical comparison
        report.setHistoricalComparison(generateHistoricalComparison(session));
        
        return report;
    }
    
    /**
     * Compares current session with historical performance.
     * 
     * @param currentSession The current session to compare
     * @return Historical comparison data
     */
    private HistoricalComparison generateHistoricalComparison(PreloadSession currentSession) {
        HistoricalComparison comparison = new HistoricalComparison();
        
        if (historicalSessions.isEmpty()) {
            comparison.setIsFirstSession(true);
            return comparison;
        }
        
        // Calculate historical averages from last 10 sessions
        List<PreloadSession> recentSessions = historicalSessions.stream()
                .skip(Math.max(0, historicalSessions.size() - 10))
                .collect(Collectors.toList());
        
        double avgTotalTime = recentSessions.stream()
                .mapToDouble(PreloadSession::getTotalTimeMs)
                .average().orElse(0.0);
        
        double avgSuccessRate = recentSessions.stream()
                .mapToDouble(PreloadSession::getSuccessRate)
                .average().orElse(0.0);
        
        double avgClassesPerSecond = recentSessions.stream()
                .mapToDouble(PreloadSession::getClassesPerSecond)
                .average().orElse(0.0);
        
        comparison.setHistoricalAverageTimeMs(avgTotalTime);
        comparison.setHistoricalAverageSuccessRate(avgSuccessRate);
        comparison.setHistoricalAverageThroughput(avgClassesPerSecond);
        
        // Performance deltas
        double timeDelta = currentSession.getTotalTimeMs() - avgTotalTime;
        double successRateDelta = currentSession.getSuccessRate() - avgSuccessRate;
        double throughputDelta = currentSession.getClassesPerSecond() - avgClassesPerSecond;
        
        comparison.setTimePerformanceDelta(timeDelta);
        comparison.setSuccessRateDelta(successRateDelta);
        comparison.setThroughputDelta(throughputDelta);
        
        // Performance rating
        String performanceRating;
        if (timeDelta < -10 && successRateDelta > 0) {
            performanceRating = "Excellent";
        } else if (timeDelta < 0 && successRateDelta >= 0) {
            performanceRating = "Good";
        } else if (Math.abs(timeDelta) <= 10 && successRateDelta >= 0) {
            performanceRating = "Average";
        } else {
            performanceRating = "Needs Improvement";
        }
        comparison.setPerformanceRating(performanceRating);
        
        return comparison;
    }
    
    /**
     * Gets the current session metrics (if active).
     * 
     * @return Current preload session or null if none active
     */
    public PreloadSession getCurrentSession() {
        return currentSession.get();
    }
    
    /**
     * Gets historical performance data.
     * 
     * @return List of historical preload sessions
     */
    public List<PreloadSession> getHistoricalSessions() {
        return Collections.unmodifiableList(new ArrayList<>(historicalSessions));
    }
    
    /**
     * Resets all metrics and clears historical data.
     */
    public void resetMetrics() {
        currentSession.set(null);
        historicalSessions.clear();
        classMetrics.clear();
        tierMetrics.clear();
        memoryTracker.reset();
        
        logger.info("Class preload metrics reset completed");
    }
    
    // ======== Supporting Classes ========
    
    /**
     * Represents a single preload session with detailed metrics.
     */
    public static final class PreloadSession {
        private final String sessionId;
        private final long startTime;
        private final int targetClassCount;
        
        private final List<Long> loadTimes = Collections.synchronizedList(new ArrayList<>());
        private final Map<Integer, TierProgress> tierProgress = new ConcurrentHashMap<>();
        
        private int successfullyLoaded = 0;
        private int failedLoads = 0;
        private long endTime;
        
        public PreloadSession(String sessionId, int targetClassCount) {
            this.sessionId = sessionId;
            this.startTime = System.currentTimeMillis();
            this.targetClassCount = targetClassCount;
        }
        
        public void recordTierStart(int tier, int classCount) {
            tierProgress.put(tier, new TierProgress(tier, classCount));
        }
        
        public void recordClassLoad(String className, long loadTimeNanos, int tier, boolean success) {
            loadTimes.add(loadTimeNanos / 1_000_000); // Convert to milliseconds
            
            TierProgress progress = tierProgress.get(tier);
            if (progress != null) {
                progress.recordLoad(loadTimeNanos, success);
            }
            
            if (success) {
                successfullyLoaded++;
            } else {
                failedLoads++;
            }
        }
        
        public void recordTierComplete(int tier, int loadedClasses, int failedClasses, long totalTimeNanos) {
            TierProgress progress = tierProgress.get(tier);
            if (progress != null) {
                progress.recordComplete(loadedClasses, failedClasses, totalTimeNanos);
            }
        }
        
        public void finalizeSession() {
            this.endTime = System.currentTimeMillis();
            // Sort load times for percentile calculations
            Collections.sort(loadTimes);
        }
        
        // Getters and calculated metrics
        public String getSessionId() { return sessionId; }
        public long getStartTime() { return startTime; }
        public long getEndTime() { return endTime; }
        public int getTotalClasses() { return successfullyLoaded + failedLoads; }
        public int getSuccessfullyLoaded() { return successfullyLoaded; }
        public int getFailedLoads() { return failedLoads; }
        public long getTotalTimeMs() { return endTime - startTime; }
        
        public double getSuccessRate() {
            int total = getTotalClasses();
            return total > 0 ? (successfullyLoaded * 100.0) / total : 0.0;
        }
        
        public double getAverageLoadTimeMs() {
            return loadTimes.stream().mapToLong(Long::longValue).average().orElse(0.0);
        }
        
        public long getMinLoadTimeMs() {
            return loadTimes.stream().mapToLong(Long::longValue).min().orElse(0L);
        }
        
        public long getMaxLoadTimeMs() {
            return loadTimes.stream().mapToLong(Long::longValue).max().orElse(0L);
        }
        
        public double getMedianLoadTimeMs() {
            if (loadTimes.isEmpty()) return 0.0;
            int middle = loadTimes.size() / 2;
            return loadTimes.get(middle).doubleValue();
        }
        
        public double getLoadTimeStdDev() {
            if (loadTimes.size() < 2) return 0.0;
            
            double mean = getAverageLoadTimeMs();
            double variance = loadTimes.stream()
                    .mapToDouble(time -> Math.pow(time - mean, 2))
                    .average().orElse(0.0);
            
            return Math.sqrt(variance);
        }
        
        public double getClassesPerSecond() {
            double seconds = getTotalTimeMs() / 1000.0;
            return seconds > 0 ? getTotalClasses() / seconds : 0.0;
        }
        
        public double getThroughputPercentile() {
            if (loadTimes.isEmpty()) return 0.0;
            int index95 = (int) (loadTimes.size() * 0.95);
            return loadTimes.get(Math.min(index95, loadTimes.size() - 1));
        }
        
        public Map<Integer, TierProgress> getTierProgress() {
            return Collections.unmodifiableMap(new HashMap<>(tierProgress));
        }
    }
    
    /**
     * Tracks progress and metrics for a specific priority tier.
     */
    public static final class TierProgress {
        private final int tier;
        private final int expectedClassCount;
        private final List<Long> loadTimes = Collections.synchronizedList(new ArrayList<>());
        
        private int loadedCount = 0;
        private int failedCount = 0;
        private long tierStartTime;
        private long tierEndTime;
        
        public TierProgress(int tier, int expectedClassCount) {
            this.tier = tier;
            this.expectedClassCount = expectedClassCount;
            this.tierStartTime = System.nanoTime();
        }
        
        public void recordLoad(long loadTimeNanos, boolean success) {
            loadTimes.add(loadTimeNanos / 1_000_000); // Convert to milliseconds
            
            if (success) {
                loadedCount++;
            } else {
                failedCount++;
            }
        }
        
        public void recordComplete(int loadedClasses, int failedClasses, long totalTimeNanos) {
            this.loadedCount = loadedClasses;
            this.failedCount = failedClasses;
            this.tierEndTime = System.nanoTime();
        }
        
        // Getters
        public int getTier() { return tier; }
        public int getExpectedClassCount() { return expectedClassCount; }
        public int getLoadedCount() { return loadedCount; }
        public int getFailedCount() { return failedCount; }
        public long getTierTimeMs() { 
            return tierEndTime > 0 ? (tierEndTime - tierStartTime) / 1_000_000 : 0; 
        }
        public double getAverageLoadTimeMs() {
            return loadTimes.stream().mapToLong(Long::longValue).average().orElse(0.0);
        }
    }
    
    /**
     * Metrics for a specific priority tier.
     */
    public static final class TierMetrics {
        private final int tier;
        private final AtomicReference<TierProgress> currentProgress = new AtomicReference<>();
        
        public TierMetrics(int tier) {
            this.tier = tier;
        }
        
        public void recordStart(int classCount) {
            currentProgress.set(new TierProgress(tier, classCount));
        }
        
        public void recordLoad(long loadTimeNanos, boolean success) {
            TierProgress progress = currentProgress.get();
            if (progress != null) {
                progress.recordLoad(loadTimeNanos, success);
            }
        }
        
        public void recordComplete(int loadedClasses, int failedClasses, long totalTimeNanos) {
            TierProgress progress = currentProgress.get();
            if (progress != null) {
                progress.recordComplete(loadedClasses, failedClasses, totalTimeNanos);
            }
        }
        
        public TierAnalysis generateAnalysis() {
            TierProgress progress = currentProgress.get();
            if (progress == null) {
                return new TierAnalysis(tier, 0, 0, 0, 0.0);
            }
            
            return new TierAnalysis(
                tier,
                progress.getLoadedCount(),
                progress.getFailedCount(),
                progress.getTierTimeMs(),
                progress.getAverageLoadTimeMs()
            );
        }
    }
    
    /**
     * Analysis result for a priority tier.
     */
    public static final class TierAnalysis {
        private final int tier;
        private final int loadedClasses;
        private final int failedClasses;
        private final long totalTimeMs;
        private final double averageLoadTimeMs;
        
        public TierAnalysis(int tier, int loadedClasses, int failedClasses, long totalTimeMs, double averageLoadTimeMs) {
            this.tier = tier;
            this.loadedClasses = loadedClasses;
            this.failedClasses = failedClasses;
            this.totalTimeMs = totalTimeMs;
            this.averageLoadTimeMs = averageLoadTimeMs;
        }
        
        public int getTier() { return tier; }
        public int getLoadedClasses() { return loadedClasses; }
        public int getFailedClasses() { return failedClasses; }
        public long getTotalTimeMs() { return totalTimeMs; }
        public double getAverageLoadTimeMs() { return averageLoadTimeMs; }
        public double getSuccessRate() {
            int total = loadedClasses + failedClasses;
            return total > 0 ? (loadedClasses * 100.0) / total : 0.0;
        }
    }
    
    /**
     * Memory usage tracking for preloading operations.
     */
    public static final class MemoryTracker {
        private long peakMemoryUsed = 0;
        private long totalMemoryAllocated = 0;
        private int classLoadsProcessed = 0;
        
        public void recordClassLoad(boolean success) {
            classLoadsProcessed++;
            
            Runtime runtime = Runtime.getRuntime();
            long usedMemory = runtime.totalMemory() - runtime.freeMemory();
            peakMemoryUsed = Math.max(peakMemoryUsed, usedMemory);
            totalMemoryAllocated += usedMemory;
        }
        
        public void reset() {
            peakMemoryUsed = 0;
            totalMemoryAllocated = 0;
            classLoadsProcessed = 0;
        }
        
        public MemoryUsageReport generateReport() {
            return new MemoryUsageReport(
                peakMemoryUsed,
                totalMemoryAllocated,
                classLoadsProcessed,
                classLoadsProcessed > 0 ? peakMemoryUsed / classLoadsProcessed : 0
            );
        }
    }
    
    /**
     * Memory usage analysis report.
     */
    public static final class MemoryUsageReport {
        private final long peakMemoryUsed;
        private final long totalMemoryAllocated;
        private final int classLoadsProcessed;
        private final long averageMemoryPerClass;
        
        public MemoryUsageReport(long peakMemoryUsed, long totalMemoryAllocated, 
                               int classLoadsProcessed, long averageMemoryPerClass) {
            this.peakMemoryUsed = peakMemoryUsed;
            this.totalMemoryAllocated = totalMemoryAllocated;
            this.classLoadsProcessed = classLoadsProcessed;
            this.averageMemoryPerClass = averageMemoryPerClass;
        }
        
        public long getPeakMemoryUsed() { return peakMemoryUsed; }
        public long getPeakMemoryUsedMB() { return peakMemoryUsed / (1024 * 1024); }
        public long getTotalMemoryAllocated() { return totalMemoryAllocated; }
        public int getClassLoadsProcessed() { return classLoadsProcessed; }
        public long getAverageMemoryPerClass() { return averageMemoryPerClass; }
        public long getAverageMemoryPerClassKB() { return averageMemoryPerClass / 1024; }
    }
    
    /**
     * Performance thresholds for optimization analysis.
     */
    public static final class PerformanceThresholds {
        private static final long SLOW_CLASS_THRESHOLD_MS = 5; // Classes taking >5ms to load
        private static final double LOW_SUCCESS_RATE_THRESHOLD = 95.0; // <95% success rate
        
        public Map<String, Object> analyze(PreloadSession session) {
            Map<String, Object> analysis = new HashMap<>();
            
            // Check success rate
            if (session.getSuccessRate() < LOW_SUCCESS_RATE_THRESHOLD) {
                analysis.put("successRateIssue", String.format("Low success rate: %.1f%%", session.getSuccessRate()));
            }
            
            // Check for slow classes
            if (session.getAverageLoadTimeMs() > SLOW_CLASS_THRESHOLD_MS) {
                analysis.put("performanceIssue", String.format("Slow average load time: %.2fms", session.getAverageLoadTimeMs()));
            }
            
            return analysis;
        }
    }
    
    /**
     * Optimization advisor for performance improvements.
     */
    public static final class OptimizationAdvisor {
        public List<String> generateRecommendations(PreloadSession session, Map<Integer, TierAnalysis> tierAnalysis) {
            List<String> recommendations = new ArrayList<>();
            
            // Success rate recommendations
            if (session.getSuccessRate() < 95.0) {
                recommendations.add("Consider reviewing dependency graph - high failure rate detected");
            }
            
            // Performance recommendations
            if (session.getAverageLoadTimeMs() > 3.0) {
                recommendations.add("Consider reducing critical class count or optimizing class loading order");
            }
            
            // Tier-specific recommendations
            tierAnalysis.values().forEach(tier -> {
                if (tier.getTotalTimeMs() > 1000) { // >1 second per tier
                    recommendations.add(String.format("Tier %d is slow (%.2fms) - consider optimizing load order", 
                            tier.getTier(), tier.getTotalTimeMs()));
                }
            });
            
            return recommendations;
        }
    }
    
    /**
     * Complete preload metrics report.
     */
    public static final class PreloadMetricsReport {
        // Session information
        private String sessionId;
        private long sessionStartTime;
        private long sessionEndTime;
        private long totalTimeMs;
        
        // Overall performance
        private int totalClasses;
        private int successfullyLoaded;
        private int failedLoads;
        private double successRate;
        
        // Performance distribution
        private double averageLoadTimeMs;
        private long minLoadTimeMs;
        private long maxLoadTimeMs;
        private double medianLoadTimeMs;
        private double loadTimeStdDev;
        
        // Throughput metrics
        private double classesPerSecond;
        private double throughputPercentile;
        
        // Detailed analysis
        private Map<Integer, TierAnalysis> tierAnalysis;
        private MemoryUsageReport memoryUsageReport;
        private Map<String, Object> thresholdAnalysis;
        private List<String> optimizationRecommendations;
        private HistoricalComparison historicalComparison;
        
        // Getters and setters
        public String getSessionId() { return sessionId; }
        public void setSessionId(String sessionId) { this.sessionId = sessionId; }
        public long getSessionStartTime() { return sessionStartTime; }
        public void setSessionStartTime(long sessionStartTime) { this.sessionStartTime = sessionStartTime; }
        public long getSessionEndTime() { return sessionEndTime; }
        public void setSessionEndTime(long sessionEndTime) { this.sessionEndTime = sessionEndTime; }
        public long getTotalTimeMs() { return totalTimeMs; }
        public void setTotalTimeMs(long totalTimeMs) { this.totalTimeMs = totalTimeMs; }
        public int getTotalClasses() { return totalClasses; }
        public void setTotalClasses(int totalClasses) { this.totalClasses = totalClasses; }
        public int getSuccessfullyLoaded() { return successfullyLoaded; }
        public void setSuccessfullyLoaded(int successfullyLoaded) { this.successfullyLoaded = successfullyLoaded; }
        public int getFailedLoads() { return failedLoads; }
        public void setFailedLoads(int failedLoads) { this.failedLoads = failedLoads; }
        public double getSuccessRate() { return successRate; }
        public void setSuccessRate(double successRate) { this.successRate = successRate; }
        public double getAverageLoadTimeMs() { return averageLoadTimeMs; }
        public void setAverageLoadTimeMs(double averageLoadTimeMs) { this.averageLoadTimeMs = averageLoadTimeMs; }
        public long getMinLoadTimeMs() { return minLoadTimeMs; }
        public void setMinLoadTimeMs(long minLoadTimeMs) { this.minLoadTimeMs = minLoadTimeMs; }
        public long getMaxLoadTimeMs() { return maxLoadTimeMs; }
        public void setMaxLoadTimeMs(long maxLoadTimeMs) { this.maxLoadTimeMs = maxLoadTimeMs; }
        public double getMedianLoadTimeMs() { return medianLoadTimeMs; }
        public void setMedianLoadTimeMs(double medianLoadTimeMs) { this.medianLoadTimeMs = medianLoadTimeMs; }
        public double getLoadTimeStdDev() { return loadTimeStdDev; }
        public void setLoadTimeStdDev(double loadTimeStdDev) { this.loadTimeStdDev = loadTimeStdDev; }
        public double getClassesPerSecond() { return classesPerSecond; }
        public void setClassesPerSecond(double classesPerSecond) { this.classesPerSecond = classesPerSecond; }
        public double getThroughputPercentile() { return throughputPercentile; }
        public void setThroughputPercentile(double throughputPercentile) { this.throughputPercentile = throughputPercentile; }
        public Map<Integer, TierAnalysis> getTierAnalysis() { return tierAnalysis; }
        public void setTierAnalysis(Map<Integer, TierAnalysis> tierAnalysis) { this.tierAnalysis = tierAnalysis; }
        public MemoryUsageReport getMemoryUsageReport() { return memoryUsageReport; }
        public void setMemoryUsageReport(MemoryUsageReport memoryUsageReport) { this.memoryUsageReport = memoryUsageReport; }
        public Map<String, Object> getThresholdAnalysis() { return thresholdAnalysis; }
        public void setThresholdAnalysis(Map<String, Object> thresholdAnalysis) { this.thresholdAnalysis = thresholdAnalysis; }
        public List<String> getOptimizationRecommendations() { return optimizationRecommendations; }
        public void setOptimizationRecommendations(List<String> optimizationRecommendations) { this.optimizationRecommendations = optimizationRecommendations; }
        public HistoricalComparison getHistoricalComparison() { return historicalComparison; }
        public void setHistoricalComparison(HistoricalComparison historicalComparison) { this.historicalComparison = historicalComparison; }
        
        @Override
        public String toString() {
            return String.format("PreloadMetricsReport[sessionId=%s, classes=%d, successRate=%.1f%%, avgTime=%.2fms]",
                    sessionId, totalClasses, successRate, averageLoadTimeMs);
        }
    }
    
    /**
     * Historical performance comparison.
     */
    public static final class HistoricalComparison {
        private boolean isFirstSession = false;
        private double historicalAverageTimeMs = 0;
        private double historicalAverageSuccessRate = 0;
        private double historicalAverageThroughput = 0;
        private double timePerformanceDelta = 0;
        private double successRateDelta = 0;
        private double throughputDelta = 0;
        private String performanceRating = "Unknown";
        
        public boolean isFirstSession() { return isFirstSession; }
        public void setIsFirstSession(boolean isFirstSession) { this.isFirstSession = isFirstSession; }
        public double getHistoricalAverageTimeMs() { return historicalAverageTimeMs; }
        public void setHistoricalAverageTimeMs(double historicalAverageTimeMs) { this.historicalAverageTimeMs = historicalAverageTimeMs; }
        public double getHistoricalAverageSuccessRate() { return historicalAverageSuccessRate; }
        public void setHistoricalAverageSuccessRate(double historicalAverageSuccessRate) { this.historicalAverageSuccessRate = historicalAverageSuccessRate; }
        public double getHistoricalAverageThroughput() { return historicalAverageThroughput; }
        public void setHistoricalAverageThroughput(double historicalAverageThroughput) { this.historicalAverageThroughput = historicalAverageThroughput; }
        public double getTimePerformanceDelta() { return timePerformanceDelta; }
        public void setTimePerformanceDelta(double timePerformanceDelta) { this.timePerformanceDelta = timePerformanceDelta; }
        public double getSuccessRateDelta() { return successRateDelta; }
        public void setSuccessRateDelta(double successRateDelta) { this.successRateDelta = successRateDelta; }
        public double getThroughputDelta() { return throughputDelta; }
        public void setThroughputDelta(double throughputDelta) { this.throughputDelta = throughputDelta; }
        public String getPerformanceRating() { return performanceRating; }
        public void setPerformanceRating(String performanceRating) { this.performanceRating = performanceRating; }
    }
    
    /**
     * Metrics for individual class loading operations.
     */
    public static final class ClassLoadMetrics {
        private final String className;
        private long loadCount = 0;
        private long totalLoadTimeNanos = 0;
        private long successfulLoads = 0;
        private long failedLoads = 0;
        private long lastLoadTimeNanos = 0;
        
        public ClassLoadMetrics(String className) {
            this.className = className;
        }
        
        public void recordLoad(long loadTimeNanos, boolean success) {
            loadCount++;
            totalLoadTimeNanos += loadTimeNanos;
            lastLoadTimeNanos = loadTimeNanos;
            if (success) {
                successfulLoads++;
            } else {
                failedLoads++;
            }
        }
        
        public String getClassName() { return className; }
        public long getLoadCount() { return loadCount; }
        public long getTotalLoadTimeNanos() { return totalLoadTimeNanos; }
        public long getSuccessfulLoads() { return successfulLoads; }
        public long getFailedLoads() { return failedLoads; }
        public long getLastLoadTimeNanos() { return lastLoadTimeNanos; }
        public double getAverageLoadTimeMs() { 
            return loadCount > 0 ? (totalLoadTimeNanos / 1_000_000.0) / loadCount : 0.0; 
        }
        public double getSuccessRate() { 
            return loadCount > 0 ? (successfulLoads * 100.0) / loadCount : 0.0; 
        }
    }
}