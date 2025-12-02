package io.warmup.framework.startup.hotpath;

import java.util.*;
import java.util.stream.Collectors;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicInteger;
import java.lang.reflect.Method;
import java.time.Instant;

/**
 * Tracks execution paths and method calls during startup to identify hot paths.
 * Uses thread-safe concurrent data structures for accurate multi-threaded tracking.
 * 
 * @author MiniMax Agent
 * @version 1.0
 */
public class ExecutionPathTracker {
    
    /**
     * Represents a method execution with its details and performance metrics.
     */
    public static class MethodExecution {
        private final String className;
        private final String methodName;
        private final String methodSignature;
        private final Instant timestamp;
        private final long threadId;
        private final StackTraceElement[] callStack;
        private final Map<String, Object> metadata;
        
        public MethodExecution(String className, String methodName, String methodSignature, 
                              Instant timestamp, long threadId, StackTraceElement[] callStack) {
            this.className = className;
            this.methodName = methodName;
            this.methodSignature = methodSignature;
            this.timestamp = timestamp;
            this.threadId = threadId;
            this.callStack = callStack;
            this.metadata = new ConcurrentHashMap<>();
        }
        
        public String getClassName() { return className; }
        public String getMethodName() { return methodName; }
        public String getMethodSignature() { return methodSignature; }
        public Instant getTimestamp() { return timestamp; }
        public long getThreadId() { return threadId; }
        public StackTraceElement[] getCallStack() { return callStack; }
        public Map<String, Object> getMetadata() { return metadata; }
        
        public String getFullMethodName() {
            return className + "." + methodName;
        }
        
        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof MethodExecution)) return false;
            MethodExecution that = (MethodExecution) o;
            return Objects.equals(className, that.className) &&
                   Objects.equals(methodName, that.methodName) &&
                   Objects.equals(methodSignature, that.methodSignature);
        }
        
        @Override
        public int hashCode() {
            return Objects.hash(className, methodName, methodSignature);
        }
        
        @Override
        public String toString() {
            return "MethodExecution{" +
                   "className='" + className + '\'' +
                   ", methodName='" + methodName + '\'' +
                   ", methodSignature='" + methodSignature + '\'' +
                   ", timestamp=" + timestamp +
                   ", threadId=" + threadId +
                   '}';
        }
    }
    
    /**
     * Execution statistics for a specific method.
     */
    public static class ExecutionStats {
        private final AtomicLong callCount = new AtomicLong(0);
        private final AtomicLong totalExecutionTime = new AtomicLong(0);
        private final AtomicLong minExecutionTime = new AtomicLong(Long.MAX_VALUE);
        private final AtomicLong maxExecutionTime = new AtomicLong(0);
        private final AtomicInteger threadCount = new AtomicInteger(0);
        private final Set<Long> observedThreadIds = ConcurrentHashMap.newKeySet();
        private final List<Long> executionTimes = new ArrayList<>();
        private final AtomicLong lastExecutionTime = new AtomicLong(0);
        private final List<String> dependencies = new ArrayList<>();
        
        public void recordExecution(long executionTime, long threadId) {
            callCount.incrementAndGet();
            totalExecutionTime.addAndGet(executionTime);
            minExecutionTime.updateAndGet(current -> Math.min(current, executionTime));
            maxExecutionTime.updateAndGet(current -> Math.max(current, executionTime));
            observedThreadIds.add(threadId);
            threadCount.set(observedThreadIds.size());
            lastExecutionTime.set(System.nanoTime());
            
            synchronized (executionTimes) {
                executionTimes.add(executionTime);
                // Keep only last 1000 measurements for memory efficiency
                if (executionTimes.size() > 1000) {
                    executionTimes.remove(0);
                }
            }
        }
        
        public void addDependency(String dependency) {
            synchronized (dependencies) {
                if (!dependencies.contains(dependency)) {
                    dependencies.add(dependency);
                }
            }
        }
        
        public long getCallCount() { return callCount.get(); }
        public long getTotalExecutionTime() { return totalExecutionTime.get(); }
        public long getMinExecutionTime() { return minExecutionTime.get() == Long.MAX_VALUE ? 0 : minExecutionTime.get(); }
        public long getMaxExecutionTime() { return maxExecutionTime.get(); }
        public int getThreadCount() { return threadCount.get(); }
        public List<Long> getExecutionTimes() { return new ArrayList<>(executionTimes); }
        public long getLastExecutionTime() { return lastExecutionTime.get(); }
        public List<String> getDependencies() { return new ArrayList<>(dependencies); }
        
        public double getAverageExecutionTime() {
            long count = callCount.get();
            return count > 0 ? (double) totalExecutionTime.get() / count : 0.0;
        }
        
        public double getStandardDeviation() {
            List<Long> times;
            synchronized (executionTimes) {
                times = new ArrayList<>(executionTimes);
            }
            
            if (times.isEmpty()) return 0.0;
            
            double mean = times.stream().mapToDouble(Long::doubleValue).average().orElse(0.0);
            double variance = times.stream()
                .mapToDouble(time -> Math.pow(time - mean, 2))
                .average()
                .orElse(0.0);
            
            return Math.sqrt(variance);
        }
        
        public double getConsistencyScore() {
            long count = callCount.get();
            if (count == 0) return 0.0;
            
            double avg = getAverageExecutionTime();
            double stdDev = getStandardDeviation();
            
            if (avg == 0) return 0.0;
            
            // Consistency score: higher is more consistent (lower variance)
            return Math.max(0.0, Math.min(1.0, 1.0 - (stdDev / avg)));
        }
        
        public boolean isHotPath() {
            return callCount.get() >= 10 && getAverageExecutionTime() > 100000; // 0.1ms average, 10+ calls
        }
        
        public HotnessLevel getHotnessLevel() {
            long calls = callCount.get();
            double avgTime = getAverageExecutionTime();
            
            if (calls >= 100 && avgTime >= 1000000) return HotnessLevel.EXTREMELY_HOT;
            if (calls >= 50 && avgTime >= 500000) return HotnessLevel.VERY_HOT;
            if (calls >= 20 && avgTime >= 100000) return HotnessLevel.HOT;
            if (calls >= 10 && avgTime >= 50000) return HotnessLevel.WARM;
            return HotnessLevel.COLD;
        }
        
        @Override
        public String toString() {
            return String.format("ExecutionStats{calls=%d, avgTime=%.2fµs, hotness=%s, threads=%d}",
                callCount.get(), getAverageExecutionTime() / 1000.0, getHotnessLevel(), threadCount.get());
        }
    }
    
    /**
     * Hotness levels for execution paths.
     */
    public enum HotnessLevel {
        EXTREMELY_HOT(100),
        VERY_HOT(75),
        HOT(50),
        WARM(25),
        COLD(0);
        
        private final int score;
        
        HotnessLevel(int score) {
            this.score = score;
        }
        
        public int getScore() { return score; }
    }
    
    private final Map<MethodExecution, ExecutionStats> methodStats = new ConcurrentHashMap<>();
    private final Map<String, ExecutionStats> classStats = new ConcurrentHashMap<>();
    private final Map<String, ExecutionStats> packageStats = new ConcurrentHashMap<>();
    private final AtomicLong totalTrackingTime = new AtomicLong(0);
    private final AtomicLong startTime = new AtomicLong(System.nanoTime());
    private final boolean isActive;
    private final List<MethodExecution> recentExecutions = new ArrayList<>();
    private final int maxRecentExecutions = 1000;
    
    public ExecutionPathTracker() {
        this(true);
    }
    
    public ExecutionPathTracker(boolean isActive) {
        this.isActive = isActive;
        if (!isActive) {
            System.out.println("ExecutionPathTracker: Tracking disabled");
        }
    }
    
    /**
     * Starts tracking a method execution.
     */
    public void startMethodTracking(String className, String methodName, String methodSignature) {
        if (!isActive) return;
        
        long startNanos = System.nanoTime();
        
        try {
            StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
            Instant timestamp = Instant.now();
            long threadId = Thread.currentThread().getId();
            
            MethodExecution execution = new MethodExecution(className, methodName, methodSignature,
                                                           timestamp, threadId, stackTrace);
            
            // Add to recent executions (thread-safe circular buffer)
            synchronized (recentExecutions) {
                recentExecutions.add(execution);
                if (recentExecutions.size() > maxRecentExecutions) {
                    recentExecutions.remove(0);
                }
            }
            
            // Update stats
            ExecutionStats stats = methodStats.computeIfAbsent(execution, k -> new ExecutionStats());
            stats.recordExecution(0, threadId); // Execution time will be updated in endMethodTracking
            
            // Update class-level stats
            ExecutionStats classStat = classStats.computeIfAbsent(className, k -> new ExecutionStats());
            classStat.recordExecution(0, threadId);
            
            // Update package-level stats
            String packageName = getPackageName(className);
            ExecutionStats packageStat = packageStats.computeIfAbsent(packageName, k -> new ExecutionStats());
            packageStat.recordExecution(0, threadId);
            
        } catch (Exception e) {
            // Log but don't throw to avoid impacting performance
            if (isActive) {
                System.err.println("Error in method tracking: " + e.getMessage());
            }
        } finally {
            totalTrackingTime.addAndGet(System.nanoTime() - startNanos);
        }
    }
    
    /**
     * Ends tracking a method execution and calculates execution time.
     */
    public void endMethodTracking(String className, String methodName, String methodSignature, 
                                 long startTimeNanos) {
        if (!isActive) return;
        
        long endTimeNanos = System.nanoTime();
        long executionTime = endTimeNanos - startTimeNanos;
        
        try {
            MethodExecution execution = new MethodExecution(className, methodName, methodSignature,
                                                           Instant.now(), Thread.currentThread().getId(),
                                                           Thread.currentThread().getStackTrace());
            
            ExecutionStats stats = methodStats.get(execution);
            if (stats != null) {
                stats.recordExecution(executionTime, Thread.currentThread().getId());
            }
            
            ExecutionStats classStat = classStats.get(className);
            if (classStat != null) {
                classStat.recordExecution(executionTime, Thread.currentThread().getId());
            }
            
            String packageName = getPackageName(className);
            ExecutionStats packageStat = packageStats.get(packageName);
            if (packageStat != null) {
                packageStat.recordExecution(executionTime, Thread.currentThread().getId());
            }
            
        } catch (Exception e) {
            if (isActive) {
                System.err.println("Error ending method tracking: " + e.getMessage());
            }
        }
    }
    
    /**
     * Helper method for tracking with automatic timing.
     */
    public static class MethodTracker implements AutoCloseable {
        private final ExecutionPathTracker tracker;
        private final String className;
        private final String methodName;
        private final String methodSignature;
        private final long startTime;
        
        public MethodTracker(ExecutionPathTracker tracker, String className, String methodName, String methodSignature) {
            this.tracker = tracker;
            this.className = className;
            this.methodName = methodName;
            this.methodSignature = methodSignature;
            this.startTime = System.nanoTime();
            tracker.startMethodTracking(className, methodName, methodSignature);
        }
        
        @Override
        public void close() {
            tracker.endMethodTracking(className, methodName, methodSignature, startTime);
        }
    }
    
    /**
     * Creates a method tracker for try-with-resources pattern.
     */
    public MethodTracker trackMethod(String className, String methodName, String methodSignature) {
        return new MethodTracker(this, className, methodName, methodSignature);
    }
    
    private String getPackageName(String className) {
        int lastDot = className.lastIndexOf('.');
        return lastDot > 0 ? className.substring(0, lastDot) : "default";
    }
    
    /**
     * Gets execution statistics for a specific method.
     */
    public ExecutionStats getMethodStats(String className, String methodName, String methodSignature) {
        MethodExecution execution = new MethodExecution(className, methodName, methodSignature,
                                                       Instant.now(), 0, new StackTraceElement[0]);
        return methodStats.get(execution);
    }
    
    /**
     * Gets execution statistics for a specific class.
     */
    public ExecutionStats getClassStats(String className) {
        return classStats.get(className);
    }
    
    /**
     * Gets execution statistics for a specific package.
     */
    public ExecutionStats getPackageStats(String packageName) {
        return packageStats.get(packageName);
    }
    
    /**
     * Gets all method statistics sorted by hotness.
     */
    public List<Map.Entry<MethodExecution, ExecutionStats>> getHotMethods(int limit) {
        return methodStats.entrySet().stream()
            .sorted((e1, e2) -> {
                HotnessLevel h1 = e1.getValue().getHotnessLevel();
                HotnessLevel h2 = e2.getValue().getHotnessLevel();
                int hotnessCompare = Integer.compare(h2.getScore(), h1.getScore());
                if (hotnessCompare != 0) return hotnessCompare;
                
                // Secondary sort by call count
                return Long.compare(e2.getValue().getCallCount(), e1.getValue().getCallCount());
            })
            .limit(limit)
            .collect(Collectors.toList());
    }
    
    /**
     * Gets all class statistics sorted by hotness.
     */
    public List<Map.Entry<String, ExecutionStats>> getHotClasses(int limit) {
        return classStats.entrySet().stream()
            .sorted((e1, e2) -> {
                HotnessLevel h1 = e1.getValue().getHotnessLevel();
                HotnessLevel h2 = e2.getValue().getHotnessLevel();
                int hotnessCompare = Integer.compare(h2.getScore(), h1.getScore());
                if (hotnessCompare != 0) return hotnessCompare;
                return Long.compare(e2.getValue().getCallCount(), e1.getValue().getCallCount());
            })
            .limit(limit)
            .collect(Collectors.toList());
    }
    
    /**
     * Gets all package statistics sorted by hotness.
     */
    public List<Map.Entry<String, ExecutionStats>> getHotPackages(int limit) {
        return packageStats.entrySet().stream()
            .sorted((e1, e2) -> {
                HotnessLevel h1 = e1.getValue().getHotnessLevel();
                HotnessLevel h2 = e2.getValue().getHotnessLevel();
                int hotnessCompare = Integer.compare(h2.getScore(), h1.getScore());
                if (hotnessCompare != 0) return hotnessCompare;
                return Long.compare(e2.getValue().getCallCount(), e1.getValue().getCallCount());
            })
            .limit(limit)
            .collect(Collectors.toList());
    }
    
    /**
     * Gets recent execution traces.
     */
    public List<MethodExecution> getRecentExecutions(int limit) {
        synchronized (recentExecutions) {
            int size = recentExecutions.size();
            if (limit >= size) {
                return new ArrayList<>(recentExecutions);
            } else {
                return new ArrayList<>(recentExecutions.subList(size - limit, size));
            }
        }
    }
    
    /**
     * Gets tracking statistics.
     */
    public long getTotalTrackingTime() { return totalTrackingTime.get(); }
    public long getTrackingDuration() { return System.nanoTime() - startTime.get(); }
    public int getTrackedMethodCount() { return methodStats.size(); }
    public int getTrackedClassCount() { return classStats.size(); }
    public int getTrackedPackageCount() { return packageStats.size(); }
    public boolean isActive() { return isActive; }
    
    /**
     * Adds a dependency relationship between methods.
     */
    public void addMethodDependency(String fromClass, String fromMethod, String toClass, String toMethod) {
        MethodExecution fromExecution = new MethodExecution(fromClass, fromMethod, "",
                                                          Instant.now(), 0, new StackTraceElement[0]);
        ExecutionStats fromStats = methodStats.get(fromExecution);
        if (fromStats != null) {
            fromStats.addDependency(toClass + "." + toMethod);
        }
    }
    
    /**
     * Resets all tracking data.
     */
    public void reset() {
        methodStats.clear();
        classStats.clear();
        packageStats.clear();
        totalTrackingTime.set(0);
        startTime.set(System.nanoTime());
        synchronized (recentExecutions) {
            recentExecutions.clear();
        }
    }
    
    /**
     * Generates a comprehensive analysis report.
     */
    public String generateAnalysisReport() {
        StringBuilder sb = new StringBuilder();
        sb.append("=== Execution Path Analysis Report ===\n");
        sb.append(String.format("Tracking Duration: %.2f ms\n", getTrackingDuration() / 1_000_000.0));
        sb.append(String.format("Total Tracking Overhead: %.2f ms\n", getTotalTrackingTime() / 1_000_000.0));
        sb.append(String.format("Tracked Methods: %d\n", getTrackedMethodCount()));
        sb.append(String.format("Tracked Classes: %d\n", getTrackedClassCount()));
        sb.append(String.format("Tracked Packages: %d\n\n", getTrackedPackageCount()));
        
        sb.append("=== Top 10 Hot Methods ===\n");
        List<Map.Entry<MethodExecution, ExecutionStats>> hotMethods = getHotMethods(10);
        for (Map.Entry<MethodExecution, ExecutionStats> entry : hotMethods) {
            sb.append(String.format("%s - %s\n", entry.getKey().getFullMethodName(), entry.getValue()));
        }
        
        sb.append("\n=== Top 10 Hot Classes ===\n");
        List<Map.Entry<String, ExecutionStats>> hotClasses = getHotClasses(10);
        for (Map.Entry<String, ExecutionStats> entry : hotClasses) {
            sb.append(String.format("%s - %s\n", entry.getKey(), entry.getValue()));
        }
        
        sb.append("\n=== Package Analysis ===\n");
        List<Map.Entry<String, ExecutionStats>> hotPackages = getHotPackages(10);
        for (Map.Entry<String, ExecutionStats> entry : hotPackages) {
            sb.append(String.format("%s - %s\n", entry.getKey(), entry.getValue()));
        }
        
        return sb.toString();
    }
}