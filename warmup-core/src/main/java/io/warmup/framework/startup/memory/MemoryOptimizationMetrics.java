package io.warmup.framework.startup.memory;

import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;
import java.util.logging.Logger;

/**
 * 📊 MÉTRICAS DEL SISTEMA DE OPTIMIZACIÓN DE MEMORIA
 * 
 * Sistema completo de métricas para tracking y análisis de la optimización de memoria.
 * Proporciona visibilidad detallada sobre el impacto de las optimizaciones.
 * 
 * @author MiniMax Agent
 * @version 1.0
 */
public class MemoryOptimizationMetrics {
    
    private static final Logger log = Logger.getLogger(MemoryOptimizationMetrics.class.getName());
    
    // 📈 Contadores globales
    private final AtomicLong totalOptimizations = new AtomicLong(0);
    private final AtomicLong successfulOptimizations = new AtomicLong(0);
    private final AtomicLong totalPagesAnalyzed = new AtomicLong(0);
    private final AtomicLong totalPagesPreloaded = new AtomicLong(0);
    private final AtomicLong totalPageFaultsForced = new AtomicLong(0);
    private final AtomicLong totalOptimizationTime = new AtomicLong(0);
    private final AtomicLong peakMemoryUsage = new AtomicLong(0);
    
    // 📊 Datos detallados
    private final Map<String, OptimizationSessionData> sessionData = new ConcurrentHashMap<>();
    private final List<PerformanceSnapshot> performanceHistory = new ArrayList<>();
    
    // ⚙️ Configuración de métricas
    private volatile boolean metricsEnabled = true;
    private volatile boolean detailedTracking = false;
    
    /**
     * 🚀 INICIALIZAR MÉTRICAS
     */
    public void initialize(MemoryOptimizationSystem.MemoryOptimizationConfig config) {
        this.metricsEnabled = config.isPrefetchEnabled();
        this.detailedTracking = config.isAggressiveAnalysis();
        
        log.info("📊 MemoryOptimizationMetrics inicializadas");
    }
    
    /**
     * 📊 REGISTRAR COMPLETACIÓN DE ANÁLISIS
     */
    public void recordAnalysisCompletion(MemoryPageAnalyzer.MemoryAnalysisResult result) {
        if (!metricsEnabled) return;
        
        totalPagesAnalyzed.addAndGet(result.getTotalPagesAnalyzed());
        
        if (detailedTracking) {
            logAnalysisDetails(result);
        }
    }
    
    /**
     * 📊 REGISTRAR COMPLETACIÓN DE PREFETCH
     */
    public void recordPrefetchCompletion(PageFaultPreloader.PrefetchResult result) {
        if (!metricsEnabled) return;
        
        totalPagesPreloaded.addAndGet(result.getPagesPreloaded());
        totalPageFaultsForced.addAndGet(result.getPageFaultsForced());
        
        if (result.isSuccess()) {
            successfulOptimizations.incrementAndGet();
        }
        
        totalOptimizations.incrementAndGet();
        
        if (detailedTracking) {
            logPrefetchDetails(result);
        }
    }
    
    /**
     * 📊 REGISTRAR SESIÓN DE OPTIMIZACIÓN
     */
    public void recordOptimizationSession(String sessionId, MemoryOptimizationSystem.MemoryOptimizationResult result) {
        if (!metricsEnabled) return;
        
        OptimizationSessionData sessionData = new OptimizationSessionData(
            sessionId,
            result.getStrategy(),
            result.getTotalOptimizationTime(),
            result.getPrefetchResult(),
            result.getAnalysisResult(),
            System.currentTimeMillis()
        );
        
        this.sessionData.put(sessionId, sessionData);
        totalOptimizationTime.addAndGet(result.getTotalOptimizationTime());
        
        // Actualizar memoria peak
        updatePeakMemoryUsage();
        
        // Agregar snapshot de performance
        addPerformanceSnapshot(sessionData);
        
        log.info(String.format("📊 Sesión %s registrada: %s", sessionId, result.getSummary()));
    }
    
    /**
     * 📊 OBTENER MÉTRICAS GLOBALES
     */
    public MemoryOptimizationMetricsData getOverallMetrics() {
        return new MemoryOptimizationMetricsData(
            totalOptimizations.get(),
            successfulOptimizations.get(),
            totalPagesAnalyzed.get(),
            totalPagesPreloaded.get(),
            totalPageFaultsForced.get(),
            totalOptimizationTime.get(),
            peakMemoryUsage.get(),
            sessionData.size(),
            performanceHistory.size(),
            calculateAverageOptimizationTime(),
            calculateSuccessRate(),
            calculatePagesPerSecond()
        );
    }
    
    /**
     * 📊 OBTENER MÉTRICAS DE SESIÓN
     */
    public OptimizationSessionData getSessionData(String sessionId) {
        return sessionData.get(sessionId);
    }
    
    /**
     * 📊 OBTENER HISTORIAL DE PERFORMANCE
     */
    public List<PerformanceSnapshot> getPerformanceHistory() {
        return new ArrayList<>(performanceHistory);
    }
    
    /**
     * 📈 CALCULAR TIEMPO PROMEDIO DE OPTIMIZACIÓN
     */
    private double calculateAverageOptimizationTime() {
        long total = totalOptimizations.get();
        if (total == 0) return 0.0;
        
        return (double) totalOptimizationTime.get() / total;
    }
    
    /**
     * 📈 CALCULAR TASA DE ÉXITO
     */
    private double calculateSuccessRate() {
        long total = totalOptimizations.get();
        if (total == 0) return 0.0;
        
        return (double) successfulOptimizations.get() / total * 100.0;
    }
    
    /**
     * 📈 CALCULAR PÁGINAS POR SEGUNDO
     */
    private double calculatePagesPerSecond() {
        long totalTime = totalOptimizationTime.get();
        if (totalTime == 0) return 0.0;
        
        return (double) totalPagesPreloaded.get() / (totalTime / 1000.0);
    }
    
    /**
     * 📊 ACTUALIZAR USO PEAK DE MEMORIA
     */
    private void updatePeakMemoryUsage() {
        Runtime runtime = Runtime.getRuntime();
        long usedMemory = runtime.totalMemory() - runtime.freeMemory();
        
        long current = peakMemoryUsage.get();
        if (usedMemory > current) {
            peakMemoryUsage.set(usedMemory);
        }
    }
    
    /**
     * 📸 AGREGAR SNAPSHOT DE PERFORMANCE
     */
    private void addPerformanceSnapshot(OptimizationSessionData sessionData) {
        PerformanceSnapshot snapshot = new PerformanceSnapshot(
            sessionData.getTimestamp(),
            sessionData.getOptimizationTime(),
            sessionData.getPrefetchResult().getPagesPreloaded(),
            sessionData.getPrefetchResult().getPageFaultsForced(),
            sessionData.getStrategy()
        );
        
        performanceHistory.add(snapshot);
        
        // Mantener solo los últimos 100 snapshots
        if (performanceHistory.size() > 100) {
            performanceHistory.remove(0);
        }
    }
    
    /**
     * 📋 LOG DETALLADO DE ANÁLISIS
     */
    private void logAnalysisDetails(MemoryPageAnalyzer.MemoryAnalysisResult result) {
        log.info("📊 ANÁLISIS DETALLADO DE MEMORIA:");
        log.info(String.format("  📄 Páginas analizadas: %d", result.getTotalPagesAnalyzed()));
        log.info(String.format("  🔥 Páginas calientes: %d", result.getHotPagesCount()));
        log.info(String.format("  ❄️ Páginas frías: %d", result.getColdPagesCount()));
        log.info(String.format("  🎯 Hotspots detectados: %d", result.getHotspots().size()));
        log.info(String.format("  ⏱️ Tiempo de análisis: %dms", result.getAnalysisTimeMs()));
    }
    
    /**
     * 📋 LOG DETALLADO DE PREFETCH
     */
    private void logPrefetchDetails(PageFaultPreloader.PrefetchResult result) {
        log.info("📊 PREFETCH DETALLADO:");
        log.info(String.format("  ✅ Estado: %s", result.isSuccess() ? "ÉXITO" : "FALLO"));
        log.info(String.format("  📄 Páginas pre-cargadas: %d", result.getPagesPreloaded()));
        log.info(String.format("  💥 Page faults forzados: %d", result.getPageFaultsForced()));
        log.info(String.format("  ⏱️ Tiempo de ejecución: %dms", result.getExecutionTimeMs()));
        log.info(String.format("  💬 Mensaje: %s", result.getMessage()));
    }
    
    /**
     * 📈 GENERAR REPORTE COMPLETO
     */
    public String generateFullReport() {
        MemoryOptimizationMetricsData data = getOverallMetrics();
        
        StringBuilder report = new StringBuilder();
        report.append("📊 REPORTE COMPLETO DE OPTIMIZACIÓN DE MEMORIA\n");
        report.append("==============================================\n\n");
        
        report.append("📊 MÉTRICAS GLOBALES:\n");
        report.append(String.format("  Total optimizaciones: %d\n", data.getTotalOptimizations()));
        report.append(String.format("  Optimizaciones exitosas: %d (%.1f%%)\n", 
            data.getSuccessfulOptimizations(), data.getSuccessRate()));
        report.append(String.format("  Tiempo promedio: %.2fms\n", data.getAverageOptimizationTime()));
        report.append(String.format("  Páginas por segundo: %.2f\n", data.getPagesPerSecond()));
        
        report.append("\n📄 PÁGINAS Y MEMORIA:\n");
        report.append(String.format("  Total páginas analizadas: %d\n", data.getTotalPagesAnalyzed()));
        report.append(String.format("  Total páginas pre-cargadas: %d\n", data.getTotalPagesPreloaded()));
        report.append(String.format("  Total page faults forzados: %d\n", data.getTotalPageFaultsForced()));
        report.append(String.format("  Peak memoria usada: %.2fMB\n", 
            data.getPeakMemoryUsage() / 1024.0 / 1024.0));
        
        report.append("\n⏱️ TIEMPO Y PERFORMANCE:\n");
        report.append(String.format("  Tiempo total de optimización: %dms\n", data.getTotalOptimizationTime()));
        report.append(String.format("  Tiempo promedio por optimización: %.2fms\n", data.getAverageOptimizationTime()));
        
        report.append("\n🏃 SESIONES Y HISTORIAL:\n");
        report.append(String.format("  Sesiones registradas: %d\n", data.getSessionsCount()));
        report.append(String.format("  Snapshots de performance: %d\n", data.getPerformanceSnapshotsCount()));
        
        if (!performanceHistory.isEmpty()) {
            report.append("\n📈 ÚLTIMOS 5 SNAPSHOTS:\n");
            int start = Math.max(0, performanceHistory.size() - 5);
            for (int i = start; i < performanceHistory.size(); i++) {
                PerformanceSnapshot snapshot = performanceHistory.get(i);
                report.append(String.format("  %d: %s - %d páginas en %dms\n", 
                    i + 1, snapshot.getStrategy(), snapshot.getPagesPreloaded(), 
                    snapshot.getOptimizationTime()));
            }
        }
        
        return report.toString();
    }
    
    /**
     * 🧹 LIMPIAR MÉTRICAS
     */
    public void clearMetrics() {
        totalOptimizations.set(0);
        successfulOptimizations.set(0);
        totalPagesAnalyzed.set(0);
        totalPagesPreloaded.set(0);
        totalPageFaultsForced.set(0);
        totalOptimizationTime.set(0);
        peakMemoryUsage.set(0);
        sessionData.clear();
        performanceHistory.clear();
        
        log.info("🧹 Métricas de memoria limpiadas");
    }
    
    /**
     * 🧹 CERRAR SISTEMA DE MÉTRICAS
     */
    public void shutdown() {
        if (metricsEnabled && !performanceHistory.isEmpty()) {
            log.info("📊 Generando reporte final antes del shutdown...");
            log.info(generateFullReport());
        }
        
        clearMetrics();
        metricsEnabled = false;
        
        log.info("🧹 MemoryOptimizationMetrics cerradas");
    }
    
    // ===== CLASES DE DATOS =====
    
    /**
     * 📊 DATOS COMPLETOS DE MÉTRICAS
     */
    public static class MemoryOptimizationMetricsData {
        private final long totalOptimizations;
        private final long successfulOptimizations;
        private final long totalPagesAnalyzed;
        private final long totalPagesPreloaded;
        private final long totalPageFaultsForced;
        private final long totalOptimizationTime;
        private final long peakMemoryUsage;
        private final int sessionsCount;
        private final int performanceSnapshotsCount;
        private final double averageOptimizationTime;
        private final double successRate;
        private final double pagesPerSecond;
        
        public MemoryOptimizationMetricsData(long totalOptimizations, long successfulOptimizations,
                                           long totalPagesAnalyzed, long totalPagesPreloaded,
                                           long totalPageFaultsForced, long totalOptimizationTime,
                                           long peakMemoryUsage, int sessionsCount,
                                           int performanceSnapshotsCount, double averageOptimizationTime,
                                           double successRate, double pagesPerSecond) {
            this.totalOptimizations = totalOptimizations;
            this.successfulOptimizations = successfulOptimizations;
            this.totalPagesAnalyzed = totalPagesAnalyzed;
            this.totalPagesPreloaded = totalPagesPreloaded;
            this.totalPageFaultsForced = totalPageFaultsForced;
            this.totalOptimizationTime = totalOptimizationTime;
            this.peakMemoryUsage = peakMemoryUsage;
            this.sessionsCount = sessionsCount;
            this.performanceSnapshotsCount = performanceSnapshotsCount;
            this.averageOptimizationTime = averageOptimizationTime;
            this.successRate = successRate;
            this.pagesPerSecond = pagesPerSecond;
        }
        
        // Getters
        public long getTotalOptimizations() { return totalOptimizations; }
        public long getSuccessfulOptimizations() { return successfulOptimizations; }
        public long getTotalPagesAnalyzed() { return totalPagesAnalyzed; }
        public long getTotalPagesPreloaded() { return totalPagesPreloaded; }
        public long getTotalPageFaultsForced() { return totalPageFaultsForced; }
        public long getTotalOptimizationTime() { return totalOptimizationTime; }
        public long getPeakMemoryUsage() { return peakMemoryUsage; }
        public int getSessionsCount() { return sessionsCount; }
        public int getPerformanceSnapshotsCount() { return performanceSnapshotsCount; }
        public double getAverageOptimizationTime() { return averageOptimizationTime; }
        public double getSuccessRate() { return successRate; }
        public double getPagesPerSecond() { return pagesPerSecond; }
    }
    
    /**
     * 📋 DATOS DE SESIÓN DE OPTIMIZACIÓN
     */
    public static class OptimizationSessionData {
        private final String sessionId;
        private final MemoryOptimizationSystem.OptimizationStrategy strategy;
        private final long optimizationTime;
        private final PageFaultPreloader.PrefetchResult prefetchResult;
        private final MemoryPageAnalyzer.MemoryAnalysisResult analysisResult;
        private final long timestamp;
        
        public OptimizationSessionData(String sessionId, MemoryOptimizationSystem.OptimizationStrategy strategy,
                                     long optimizationTime, PageFaultPreloader.PrefetchResult prefetchResult,
                                     MemoryPageAnalyzer.MemoryAnalysisResult analysisResult, long timestamp) {
            this.sessionId = sessionId;
            this.strategy = strategy;
            this.optimizationTime = optimizationTime;
            this.prefetchResult = prefetchResult;
            this.analysisResult = analysisResult;
            this.timestamp = timestamp;
        }
        
        // Getters
        public String getSessionId() { return sessionId; }
        public MemoryOptimizationSystem.OptimizationStrategy getStrategy() { return strategy; }
        public long getOptimizationTime() { return optimizationTime; }
        public PageFaultPreloader.PrefetchResult getPrefetchResult() { return prefetchResult; }
        public MemoryPageAnalyzer.MemoryAnalysisResult getAnalysisResult() { return analysisResult; }
        public long getTimestamp() { return timestamp; }
    }
    
    /**
     * 📸 SNAPSHOT DE PERFORMANCE
     */
    public static class PerformanceSnapshot {
        private final long timestamp;
        private final long optimizationTime;
        private final long pagesPreloaded;
        private final long pageFaultsForced;
        private final MemoryOptimizationSystem.OptimizationStrategy strategy;
        
        public PerformanceSnapshot(long timestamp, long optimizationTime, long pagesPreloaded,
                                 long pageFaultsForced, MemoryOptimizationSystem.OptimizationStrategy strategy) {
            this.timestamp = timestamp;
            this.optimizationTime = optimizationTime;
            this.pagesPreloaded = pagesPreloaded;
            this.pageFaultsForced = pageFaultsForced;
            this.strategy = strategy;
        }
        
        // Getters
        public long getTimestamp() { return timestamp; }
        public long getOptimizationTime() { return optimizationTime; }
        public long getPagesPreloaded() { return pagesPreloaded; }
        public long getPageFaultsForced() { return pageFaultsForced; }
        public MemoryOptimizationSystem.OptimizationStrategy getStrategy() { return strategy; }
    }
}