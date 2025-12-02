package io.warmup.examples.startup;

import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.stream.Collectors;

/**
 * 🎯 RESULTADO DE INICIALIZACIÓN PARALELA DE SUBSISTEMAS
 * 
 * Contiene los resultados completos de la inicialización paralela de todos los
 * subsistemas del framework, incluyendo métricas detalladas y estadísticas
 * de rendimiento.
 * 
 * @author MiniMax Agent
 * @version 1.0
 */
public class SubsystemInitializationResult {
    
    private final List<SubsystemMetrics> subsystemResults;
    private final long totalDurationNs;
    private final int availableCores;
    private final int threadPoolSize;
    
    public SubsystemInitializationResult(List<SubsystemMetrics> subsystemResults, 
                                       long totalDurationNs, 
                                       int availableCores, 
                                       int threadPoolSize) {
        this.subsystemResults = subsystemResults;
        this.totalDurationNs = totalDurationNs;
        this.availableCores = availableCores;
        this.threadPoolSize = threadPoolSize;
    }
    
    /**
     * 🎯 OBTENER RESULTADOS DE TODOS LOS SUBSISTEMAS
     */
    public List<SubsystemMetrics> getSubsystemResults() {
        return subsystemResults;
    }
    
    /**
     * ⏱️ OBTENER DURACIÓN TOTAL EN NANOSEGUNDOS
     */
    public long getTotalDurationNs() {
        return totalDurationNs;
    }
    
    /**
     * ⏱️ OBTENER DURACIÓN TOTAL EN MILISEGUNDOS
     */
    public long getTotalDurationMs() {
        return totalDurationNs / 1_000_000;
    }
    
    /**
     * 🖥️ OBTENER NÚMERO DE CORES DISPONIBLES
     */
    public int getAvailableCores() {
        return availableCores;
    }
    
    /**
     * 🧵 OBTENER TAMAÑO DEL POOL DE THREADS
     */
    public int getThreadPoolSize() {
        return threadPoolSize;
    }
    
    /**
     * ✅ VERIFICAR SI TODOS LOS SUBSISTEMAS SE INICIALIZARON EXITOSAMENTE
     */
    public boolean isAllSuccessful() {
        return subsystemResults.stream().allMatch(SubsystemMetrics::isSuccess);
    }
    
    /**
     * ✅ CONTAR SUBSISTEMAS EXITOSOS
     */
    public long getSuccessCount() {
        return subsystemResults.stream().filter(SubsystemMetrics::isSuccess).count();
    }
    
    /**
     * ❌ CONTAR SUBSISTEMAS FALLIDOS
     */
    public long getFailureCount() {
        return subsystemResults.stream().filter(result -> !result.isSuccess()).count();
    }
    
    /**
     * 📊 OBTENER SUBSISTEMAS EXITOSOS
     */
    public List<SubsystemMetrics> getSuccessfulSubsystems() {
        return subsystemResults.stream()
            .filter(SubsystemMetrics::isSuccess)
            .collect(Collectors.toList());
    }
    
    /**
     * 📊 OBTENER SUBSISTEMAS FALLIDOS
     */
    public List<SubsystemMetrics> getFailedSubsystems() {
        return subsystemResults.stream()
            .filter(result -> !result.isSuccess())
            .collect(Collectors.toList());
    }
    
    /**
     * 📊 OBTENER MAPA DE MÉTRICAS POR NOMBRE
     */
    public Map<String, SubsystemMetrics> getSubsystemMetricsMap() {
        return subsystemResults.stream()
            .collect(Collectors.toMap(
                SubsystemMetrics::getName, 
                result -> result,
                (existing, replacement) -> replacement
            ));
    }
    
    /**
     * 🚀 CALCULAR SPEEDUP ESPERADO VS INICIALIZACIÓN SECUENCIAL
     */
    public double calculateSpeedup() {
        long totalSequentialTime = subsystemResults.stream()
            .mapToLong(SubsystemMetrics::getDurationNs)
            .sum();
        
        if (totalSequentialTime == 0) return 1.0;
        
        return (double) totalSequentialTime / totalDurationNs;
    }
    
    /**
     * 📊 CALCULAR EFICIENCIA DE PARALELIZACIÓN (0.0 - 1.0)
     */
    public double calculateParallelizationEfficiency() {
        double idealSpeedup = Math.min(threadPoolSize, subsystemResults.size());
        double actualSpeedup = calculateSpeedup();
        
        if (idealSpeedup == 0) return 0.0;
        
        return Math.min(1.0, actualSpeedup / idealSpeedup);
    }
    
    // Methods needed by ComprehensiveStartupResult
    public double getSpeedupFactor() {
        return calculateSpeedup();
    }
    
    public long getTotalTimeSavedMs() {
        long totalSequentialTime = subsystemResults.stream()
            .mapToLong(SubsystemMetrics::getDurationNs)
            .sum();
        long timeSaved = totalSequentialTime - totalDurationNs;
        return Math.max(0, timeSaved / 1_000_000);
    }
    
    public double getOverallEfficiency() {
        return calculateParallelizationEfficiency();
    }
    
    public Map<String, Object> getDetailedResults() {
        return getSubsystemMetricsMap().entrySet().stream()
            .collect(Collectors.toMap(
                Map.Entry::getKey,
                entry -> entry.getValue().getFormattedStats()
            ));
    }
    
    public String getSummary() {
        return String.format("Subsystem Initialization: %d/%d successful, %.2fx speedup", 
            getSuccessCount(), subsystemResults.size(), calculateSpeedup());
    }
    
    public Map<String, Object> getParallelizationStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("availableCores", availableCores);
        stats.put("threadPoolSize", threadPoolSize);
        stats.put("speedupFactor", calculateSpeedup());
        stats.put("efficiency", calculateParallelizationEfficiency());
        stats.put("successfulSubsystems", getSuccessCount());
        stats.put("totalSubsystems", subsystemResults.size());
        return stats;
    }
    
    /**
     * 📈 OBTENER ESTADÍSTICAS DE RENDIMIENTO
     */
    public PerformanceStats getPerformanceStats() {
        long minTime = subsystemResults.stream()
            .mapToLong(SubsystemMetrics::getDurationNs)
            .min()
            .orElse(0);
            
        long maxTime = subsystemResults.stream()
            .mapToLong(SubsystemMetrics::getDurationNs)
            .max()
            .orElse(0);
            
        double avgTime = subsystemResults.stream()
            .mapToLong(SubsystemMetrics::getDurationNs)
            .average()
            .orElse(0.0);
        
        return new PerformanceStats(minTime, maxTime, avgTime, totalDurationNs);
    }
    
    /**
     * 🎯 OBTENER SUBSISTEMA MÁS LENTO
     */
    public SubsystemMetrics getSlowestSubsystem() {
        return subsystemResults.stream()
            .max(SubsystemMetrics::compareSpeed)
            .orElse(null);
    }
    
    /**
     * 🎯 OBTENER SUBSISTEMA MÁS RÁPIDO
     */
    public SubsystemMetrics getFastestSubsystem() {
        return subsystemResults.stream()
            .min(SubsystemMetrics::compareSpeed)
            .orElse(null);
    }
    
    /**
     * 📊 GENERAR REPORTE DETALLADO
     */
    public String generateDetailedReport() {
        StringBuilder report = new StringBuilder();
        
        report.append("🚀 REPORTE DE INICIALIZACIÓN PARALELA\n");
        report.append("=====================================\n\n");
        
        report.append(String.format("📈 RENDIMIENTO GENERAL:\n"));
        report.append(String.format("  • Cores disponibles: %d\n", availableCores));
        report.append(String.format("  • Threads en pool: %d\n", threadPoolSize));
        report.append(String.format("  • Total subsistemas: %d\n", subsystemResults.size()));
        report.append(String.format("  • Éxitos: %d\n", getSuccessCount()));
        report.append(String.format("  • Fallos: %d\n", getFailureCount()));
        report.append(String.format("  • Tiempo total: %dms\n", getTotalDurationMs()));
        report.append(String.format("  • Speedup: %.2fx\n", calculateSpeedup()));
        report.append(String.format("  • Eficiencia: %.1f%%\n\n", calculateParallelizationEfficiency() * 100));
        
        report.append("📊 DETALLE POR SUBSISTEMA:\n");
        report.append("--------------------------\n");
        
        for (SubsystemMetrics metrics : subsystemResults) {
            report.append(String.format("  %s\n", metrics.getFormattedStats()));
        }
        
        PerformanceStats perfStats = getPerformanceStats();
        report.append(String.format("\n📈 ESTADÍSTICAS DE TIEMPO:\n"));
        report.append(String.format("  • Más rápido: %dms\n", perfStats.getMinTimeMs()));
        report.append(String.format("  • Más lento: %dms\n", perfStats.getMaxTimeMs()));
        report.append(String.format("  • Promedio: %.1fms\n", perfStats.getAvgTimeMs()));
        report.append(String.format("  • Tiempo total: %dms\n", perfStats.getTotalTimeMs()));
        
        if (getFailureCount() > 0) {
            report.append(String.format("\n❌ ERRORES DETECTADOS:\n"));
            for (SubsystemMetrics failed : getFailedSubsystems()) {
                report.append(String.format("  • %s: %s\n", 
                    failed.getName(), 
                    failed.getErrorMessage()));
            }
        }
        
        return report.toString();
    }
    
    /**
     * 📊 CLASE PARA ESTADÍSTICAS DE RENDIMIENTO
     */
    public static class PerformanceStats {
        private final long minTimeNs;
        private final long maxTimeNs;
        private final double avgTimeNs;
        private final long totalTimeNs;
        
        public PerformanceStats(long minTimeNs, long maxTimeNs, double avgTimeNs, long totalTimeNs) {
            this.minTimeNs = minTimeNs;
            this.maxTimeNs = maxTimeNs;
            this.avgTimeNs = avgTimeNs;
            this.totalTimeNs = totalTimeNs;
        }
        
        public long getMinTimeMs() { return minTimeNs / 1_000_000; }
        public long getMaxTimeMs() { return maxTimeNs / 1_000_000; }
        public double getAvgTimeMs() { return avgTimeNs / 1_000_000.0; }
        public long getTotalTimeMs() { return totalTimeNs / 1_000_000; }
    }
    
    @Override
    public String toString() {
        return generateDetailedReport();
    }
}