package io.warmup.framework.metrics;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

public class MethodMetrics {

    private final ConcurrentMap<String, MethodStats> methodStats = new ConcurrentHashMap<>();

    // ✅ CONSTRUCTOR PÚBLICO EXPLÍCITO PARA JIT SUPPLIER
    public MethodMetrics() {
        // Constructor público sin parámetros para optimizar generación JIT
    }

    public void recordMethodCall(String methodName, long duration, boolean success) {
        MethodStats stats = methodStats.computeIfAbsent(methodName, k -> new MethodStats());
        stats.recordCall(duration, success);
    }

    public MethodStats getMethodStats(String methodName) {
        return methodStats.get(methodName);
    }

    public ConcurrentMap<String, MethodStats> getAllStats() {
        return new ConcurrentHashMap<>(methodStats);
    }

    public void reset() {
        methodStats.clear();
    }

    public static class MethodStats {

        // ✅ CONSTRUCTOR PÚBLICO EXPLÍCITO PARA JIT SUPPLIER
        public MethodStats() {
            // Constructor público sin parámetros para optimización JIT
        }

        private final AtomicLong callCount = new AtomicLong(0);
        private final AtomicLong successfulCalls = new AtomicLong(0);
        private final AtomicLong totalTime = new AtomicLong(0);
        private final AtomicLong minTime = new AtomicLong(Long.MAX_VALUE);
        private final AtomicLong maxTime = new AtomicLong(0);
        private final AtomicReference<Double> averageTime = new AtomicReference<>(0.0);

        public void recordCall(long duration, boolean success) {
            callCount.incrementAndGet();
            if (success) {
                successfulCalls.incrementAndGet();
            }

            totalTime.addAndGet(duration);

            // Update min time
            long currentMin;
            do {
                currentMin = minTime.get();
            } while (duration < currentMin && !minTime.compareAndSet(currentMin, duration));

            // Update max time
            long currentMax;
            do {
                currentMax = maxTime.get();
            } while (duration > currentMax && !maxTime.compareAndSet(currentMax, duration));

            // Update average
            averageTime.set((double) totalTime.get() / callCount.get());
        }

        public long getCallCount() {
            return callCount.get();
        }

        public long getSuccessfulCalls() {
            return successfulCalls.get();
        }

        public long getFailedCalls() {
            return callCount.get() - successfulCalls.get();
        }

        public double getSuccessRate() {
            return callCount.get() > 0 ? (successfulCalls.get() * 100.0) / callCount.get() : 0.0;
        }

        public long getTotalTime() {
            return totalTime.get();
        }

        public long getMinTime() {
            return minTime.get() == Long.MAX_VALUE ? 0 : minTime.get();
        }

        public long getMaxTime() {
            return maxTime.get();
        }

        public double getAverageTime() {
            return averageTime.get();
        }
    }
}
