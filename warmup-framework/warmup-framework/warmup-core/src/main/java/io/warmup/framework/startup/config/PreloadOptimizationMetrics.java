package io.warmup.framework.startup.config;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Métricas de optimización del sistema de configuración precargada.
 * Rastrea el ahorro de tiempo de startup y la eficiencia del sistema.
 */
public class PreloadOptimizationMetrics {
    
    // Métricas globales
    private final AtomicLong totalStartupTimeSaved = new AtomicLong(0);
    private final AtomicLong totalIooOperationsAvoided = new AtomicLong(0);
    private final AtomicLong totalParsingOperationsAvoided = new AtomicLong(0);
    private final AtomicLong totalConfigsPreloaded = new AtomicLong(0);
    private final AtomicLong totalPreloadOperations = new AtomicLong(0);
    
    // Métricas por operación de precarga
    private final Map<String, SinglePreloadMetrics> preloadOperationMetrics = new ConcurrentHashMap<>();
    
    // Timestamps
    private long firstPreloadTime = 0;
    private long lastPreloadTime = 0;
    
    /**
     * Registra resultados de una operación de precarga
     */
    public void recordPreloadResults(Map<String, MemoryMappedConfigLoader.ConfigLoadingResult> results,
                                   long actualPreloadTimeMs,
                                   long estimatedStartupSavingsMs) {
        
        long totalSuccessful = results.values().stream()
            .mapToLong(r -> r.isSuccess() ? 1 : 0)
            .sum();
        
        // Actualizar métricas globales
        totalStartupTimeSaved.addAndGet(estimatedStartupSavingsMs);
        totalIooOperationsAvoided.addAndGet(totalSuccessful * 3); // ~3 I/O ops por config
        totalParsingOperationsAvoided.addAndGet(totalSuccessful); // 1 parsing por config
        totalConfigsPreloaded.addAndGet(totalSuccessful);
        totalPreloadOperations.incrementAndGet();
        
        // Actualizar timestamps
        if (firstPreloadTime == 0) {
            firstPreloadTime = System.currentTimeMillis();
        }
        lastPreloadTime = System.currentTimeMillis();
        
        // Registrar métricas individuales
        String operationId = "preload_" + System.currentTimeMillis();
        SinglePreloadMetrics operationMetrics = new SinglePreloadMetrics(
            operationId, results, actualPreloadTimeMs, estimatedStartupSavingsMs
        );
        preloadOperationMetrics.put(operationId, operationMetrics);
        
        // Limpiar operaciones antiguas (mantener solo las últimas 100)
        if (preloadOperationMetrics.size() > 100) {
            preloadOperationMetrics.clear();
            preloadOperationMetrics.put(operationId, operationMetrics);
        }
    }
    
    /**
     * Obtiene estadísticas globales de optimización
     */
    public OverallOptimizationStats getOverallStats() {
        return new OverallOptimizationStats(
            totalStartupTimeSaved.get(),
            totalIooOperationsAvoided.get(),
            totalParsingOperationsAvoided.get(),
            totalConfigsPreloaded.get(),
            totalPreloadOperations.get(),
            firstPreloadTime,
            lastPreloadTime,
            getAveragePreloadTimeMs()
        );
    }
    
    /**
     * Obtiene estadísticas de la última operación
     */
    public SinglePreloadMetrics getLatestOperationMetrics() {
        if (preloadOperationMetrics.isEmpty()) {
            return null;
        }
        
        return preloadOperationMetrics.values().stream()
            .max((m1, m2) -> Long.compare(m1.getOperationTimestamp(), m2.getOperationTimestamp()))
            .orElse(null);
    }
    
    /**
     * Obtiene todas las operaciones de precarga
     */
    public Map<String, SinglePreloadMetrics> getAllOperationMetrics() {
        return new ConcurrentHashMap<>(preloadOperationMetrics);
    }
    
    /**
     * Calcula el tiempo promedio de precarga
     */
    private double getAveragePreloadTimeMs() {
        if (preloadOperationMetrics.isEmpty()) {
            return 0;
        }
        
        long totalTime = preloadOperationMetrics.values().stream()
            .mapToLong(SinglePreloadMetrics::getActualPreloadTimeMs)
            .sum();
        
        return (double) totalTime / preloadOperationMetrics.size();
    }
    
    /**
     * Obtiene el ahorro total de tiempo en formato legible
     */
    public String getFormattedTotalSavings() {
        return formatDuration(totalStartupTimeSaved.get());
    }
    
    /**
     * Formatea duración en milisegundos
     */
    private static String formatDuration(long ms) {
        if (ms < 1000) {
            return ms + " ms";
        } else if (ms < 60 * 1000) {
            return String.format("%.2f s", ms / 1000.0);
        } else {
            long seconds = ms / 1000;
            long minutes = seconds / 60;
            seconds = seconds % 60;
            long hours = minutes / 60;
            minutes = minutes % 60;
            
            if (hours > 0) {
                return String.format("%d h %d m %d s", hours, minutes, seconds);
            } else {
                return String.format("%d m %d s", minutes, seconds);
            }
        }
    }
    
    /**
     * Resetea todas las métricas
     */
    public void reset() {
        totalStartupTimeSaved.set(0);
        totalIooOperationsAvoided.set(0);
        totalParsingOperationsAvoided.set(0);
        totalConfigsPreloaded.set(0);
        totalPreloadOperations.set(0);
        preloadOperationMetrics.clear();
        firstPreloadTime = 0;
        lastPreloadTime = 0;
    }
    
    /**
     * Estadísticas globales de optimización
     */
    public static class OverallOptimizationStats {
        private final long totalStartupTimeSaved;
        private final long totalIooOperationsAvoided;
        private final long totalParsingOperationsAvoided;
        private final long totalConfigsPreloaded;
        private final long totalPreloadOperations;
        private final long firstOperationTime;
        private final long lastOperationTime;
        private final double averagePreloadTimeMs;
        
        public OverallOptimizationStats(long totalStartupTimeSaved,
                                      long totalIooOperationsAvoided,
                                      long totalParsingOperationsAvoided,
                                      long totalConfigsPreloaded,
                                      long totalPreloadOperations,
                                      long firstOperationTime,
                                      long lastOperationTime,
                                      double averagePreloadTimeMs) {
            this.totalStartupTimeSaved = totalStartupTimeSaved;
            this.totalIooOperationsAvoided = totalIooOperationsAvoided;
            this.totalParsingOperationsAvoided = totalParsingOperationsAvoided;
            this.totalConfigsPreloaded = totalConfigsPreloaded;
            this.totalPreloadOperations = totalPreloadOperations;
            this.firstOperationTime = firstOperationTime;
            this.lastOperationTime = lastOperationTime;
            this.averagePreloadTimeMs = averagePreloadTimeMs;
        }
        
        // Getters
        public long getTotalStartupTimeSaved() { return totalStartupTimeSaved; }
        public long getTotalIooOperationsAvoided() { return totalIooOperationsAvoided; }
        public long getTotalParsingOperationsAvoided() { return totalParsingOperationsAvoided; }
        public long getTotalConfigsPreloaded() { return totalConfigsPreloaded; }
        public long getTotalPreloadOperations() { return totalPreloadOperations; }
        public long getFirstOperationTime() { return firstOperationTime; }
        public long getLastOperationTime() { return lastOperationTime; }
        public double getAveragePreloadTimeMs() { return averagePreloadTimeMs; }
        
        /**
         * Obtiene el formato legible del tiempo ahorrado
         */
        public String getFormattedTotalSavings() {
            return formatDuration(totalStartupTimeSaved);
        }
        
        /**
         * Calcula la eficiencia promedio del sistema
         */
        public double getAverageEfficiency() {
            return totalPreloadOperations > 0 ? 
                (double) totalStartupTimeSaved / (averagePreloadTimeMs * totalPreloadOperations) : 0;
        }
        
        /**
         * Obtiene el período de actividad del sistema
         */
        public long getSystemActiveDurationMs() {
            if (firstOperationTime == 0 || lastOperationTime == 0) {
                return 0;
            }
            return lastOperationTime - firstOperationTime;
        }
        
        /**
         * Obtiene métricas de throughput
         */
        public ThroughputMetrics getThroughputMetrics() {
            long activeDurationMs = getSystemActiveDurationMs();
            return new ThroughputMetrics(
                totalConfigsPreloaded,
                activeDurationMs > 0 ? (double) totalConfigsPreloaded / (activeDurationMs / 1000.0) : 0,
                totalStartupTimeSaved,
                activeDurationMs > 0 ? (double) totalStartupTimeSaved / (activeDurationMs / 1000.0) : 0
            );
        }
        
        @Override
        public String toString() {
            return String.format(
                "OverallStats{saved=%s, configs=%d, ops=%d, efficiency=%.2f}",
                getFormattedTotalSavings(), totalConfigsPreloaded, 
                totalPreloadOperations, getAverageEfficiency()
            );
        }
    }
    
    /**
     * Métricas de una sola operación de precarga
     */
    public static class SinglePreloadMetrics {
        private final String operationId;
        private final long operationTimestamp;
        private final int successfulLoads;
        private final int totalRequested;
        private final long actualPreloadTimeMs;
        private final long estimatedStartupSavingsMs;
        private final double successRate;
        private final double efficiency;
        private final ConfigMappingStats mappingStats;
        
        public SinglePreloadMetrics(String operationId,
                                  Map<String, MemoryMappedConfigLoader.ConfigLoadingResult> results,
                                  long actualPreloadTimeMs,
                                  long estimatedStartupSavingsMs) {
            this.operationId = operationId;
            this.operationTimestamp = System.currentTimeMillis();
            
            this.successfulLoads = (int) results.values().stream()
                .mapToLong(r -> r.isSuccess() ? 1 : 0)
                .sum();
            this.totalRequested = results.size();
            this.actualPreloadTimeMs = actualPreloadTimeMs;
            this.estimatedStartupSavingsMs = estimatedStartupSavingsMs;
            this.successRate = totalRequested > 0 ? (double) successfulLoads / totalRequested : 0;
            this.efficiency = actualPreloadTimeMs > 0 ? 
                (double) estimatedStartupSavingsMs / actualPreloadTimeMs : 0;
            
            // Calcular estadísticas de mapeo para esta operación
            this.mappingStats = calculateOperationMappingStats(results);
        }
        
        private ConfigMappingStats calculateOperationMappingStats(
                Map<String, MemoryMappedConfigLoader.ConfigLoadingResult> results) {
            
            long totalFiles = results.size();
            long totalBytes = results.values().stream()
                .filter(r -> r.isSuccess())
                .mapToLong(MemoryMappedConfigLoader.ConfigLoadingResult::getFileSize)
                .sum();
            
            long totalMappingTime = results.values().stream()
                .filter(r -> r.isSuccess())
                .mapToLong(MemoryMappedConfigLoader.ConfigLoadingResult::getMappingTimeMs)
                .sum();
            
            long totalParsingTime = results.values().stream()
                .filter(r -> r.isSuccess())
                .mapToLong(MemoryMappedConfigLoader.ConfigLoadingResult::getParsingTimeMs)
                .sum();
            
            return new ConfigMappingStats(
                successfulLoads, totalBytes, totalMappingTime, totalParsingTime,
                successfulLoads, totalRequested
            );
        }
        
        // Getters
        public String getOperationId() { return operationId; }
        public long getOperationTimestamp() { return operationTimestamp; }
        public int getSuccessfulLoads() { return successfulLoads; }
        public int getTotalRequested() { return totalRequested; }
        public long getActualPreloadTimeMs() { return actualPreloadTimeMs; }
        public long getEstimatedStartupSavingsMs() { return estimatedStartupSavingsMs; }
        public double getSuccessRate() { return successRate; }
        public double getEfficiency() { return efficiency; }
        public ConfigMappingStats getMappingStats() { return mappingStats; }
        
        @Override
        public String toString() {
            return String.format(
                "PreloadMetrics{id=%s, success=%d/%d (%.1f%%), time=%dms, savings=%dms, efficiency=%.2f}",
                operationId, successfulLoads, totalRequested, successRate * 100,
                actualPreloadTimeMs, estimatedStartupSavingsMs, efficiency
            );
        }
    }
    
    /**
     * Métricas de throughput del sistema
     */
    public static class ThroughputMetrics {
        private final long totalConfigsProcessed;
        private final double configsPerSecond;
        private final long totalTimeSaved;
        private final double timeSavedPerSecond;
        
        public ThroughputMetrics(long totalConfigsProcessed, double configsPerSecond,
                               long totalTimeSaved, double timeSavedPerSecond) {
            this.totalConfigsProcessed = totalConfigsProcessed;
            this.configsPerSecond = configsPerSecond;
            this.totalTimeSaved = totalTimeSaved;
            this.timeSavedPerSecond = timeSavedPerSecond;
        }
        
        // Getters
        public long getTotalConfigsProcessed() { return totalConfigsProcessed; }
        public double getConfigsPerSecond() { return configsPerSecond; }
        public long getTotalTimeSaved() { return totalTimeSaved; }
        public double getTimeSavedPerSecond() { return timeSavedPerSecond; }
        
        @Override
        public String toString() {
            return String.format(
                "Throughput{configs=%.1f/s, timeSaved=%.1fms/s}",
                configsPerSecond, timeSavedPerSecond
            );
        }
    }
}