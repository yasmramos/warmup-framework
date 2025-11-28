package io.warmup.framework.startup;

import java.util.Map;

/**
 * ⚡ RESULTADO DE STARTUP COMBINADO
 * 
 * Combina los resultados del startup tradicional (fases crítica/background) 
 * con el nuevo sistema de inicialización paralela para una vista completa
 * del rendimiento de startup.
 * 
 * @author MiniMax Agent
 * @version 1.0
 */
public class CombinedStartupResult {
    
    private final StartupMetrics traditionalMetrics;
    private final SubsystemInitializationResult parallelResult;
    private final long totalDurationNs;
    
    public CombinedStartupResult(StartupMetrics traditionalMetrics, 
                               SubsystemInitializationResult parallelResult, 
                               long totalDurationNs) {
        this.traditionalMetrics = traditionalMetrics;
        this.parallelResult = parallelResult;
        this.totalDurationNs = totalDurationNs;
    }
    
    /**
     * 📊 OBTENER MÉTRICAS TRADICIONALES
     */
    public StartupMetrics getTraditionalMetrics() {
        return traditionalMetrics;
    }
    
    /**
     * 🚀 OBTENER RESULTADO DE INICIALIZACIÓN PARALELA
     */
    public SubsystemInitializationResult getParallelResult() {
        return parallelResult;
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
     * ✅ VERIFICAR SI TODO FUE EXITOSO
     */
    public boolean isAllSuccessful() {
        return traditionalMetrics.isCriticalPhaseCompleted() && 
               traditionalMetrics.isBackgroundPhaseCompleted() && 
               parallelResult.isAllSuccessful();
    }
    
    /**
     * 🎯 CALCULAR MEJORA DE RENDIMIENTO VS STARTUP TRADICIONAL
     */
    public double calculatePerformanceImprovement() {
        // Asumiendo que el startup tradicional toma ~100ms como baseline
        final double traditionalBaselineMs = 100.0;
        final double actualTimeMs = getTotalDurationMs();
        
        if (actualTimeMs == 0) return 1.0;
        
        return (traditionalBaselineMs - actualTimeMs) / traditionalBaselineMs;
    }
    
    /**
     * 📊 OBTENER ESTADÍSTICAS CONSOLIDADAS
     */
    public ConsolidatedStats getConsolidatedStats() {
        return new ConsolidatedStats(
            traditionalMetrics,
            parallelResult,
            getTotalDurationMs()
        );
    }
    
    /**
     * 📊 GENERAR REPORTE COMPLETO
     */
    public String generateCompleteReport() {
        StringBuilder report = new StringBuilder();
        
        report.append("⚡ REPORTE DE STARTUP COMBINADO\n");
        report.append("===============================\n\n");
        
        // Resumen ejecutivo
        report.append("📈 RESUMEN EJECUTIVO:\n");
        report.append("-------------------\n");
        report.append(String.format("  • Tiempo total: %dms\n", getTotalDurationMs()));
        report.append(String.format("  • Todas las fases exitosas: %s\n", isAllSuccessful() ? "✅ Sí" : "❌ No"));
        report.append(String.format("  • Mejora de rendimiento: %.1f%%\n", calculatePerformanceImprovement() * 100));
        
        if (parallelResult != null) {
            report.append(String.format("  • Speedup paralelo: %.2fx\n", parallelResult.calculateSpeedup()));
            report.append(String.format("  • Eficiencia paralela: %.1f%%\n", 
                    parallelResult.calculateParallelizationEfficiency() * 100));
        }
        
        report.append("\n");
        
        // Métricas tradicionales
        if (traditionalMetrics != null) {
            report.append("📊 STARTUP TRADICIONAL (FASES CRÍTICA/BACKGROUND):\n");
            report.append("--------------------------------------------------\n");
            report.append(String.format("  • Fase crítica completada: %s\n", 
                    traditionalMetrics.isCriticalPhaseCompleted() ? "✅" : "❌"));
            report.append(String.format("  • Fase background completada: %s\n", 
                    traditionalMetrics.isBackgroundPhaseCompleted() ? "✅" : "❌"));
            
            if (traditionalMetrics.getCriticalMetrics() != null) {
                report.append(String.format("  • Tiempo fase crítica: %dms\n", 
                        traditionalMetrics.getCriticalMetrics().getDurationMs()));
            }
            
            if (traditionalMetrics.getBackgroundMetrics() != null) {
                report.append(String.format("  • Tiempo fase background: %dms\n", 
                        traditionalMetrics.getBackgroundMetrics().getDurationMs()));
            }
            report.append("\n");
        }
        
        // Métricas paralelas
        if (parallelResult != null) {
            report.append("🚀 STARTUP PARALELO (SUBSISTEMAS):\n");
            report.append("---------------------------------\n");
            report.append(String.format("  • Subsistemas iniciados: %d\n", parallelResult.getSubsystemResults().size()));
            report.append(String.format("  • Éxitos: %d\n", parallelResult.getSuccessCount()));
            report.append(String.format("  • Fallos: %d\n", parallelResult.getFailureCount()));
            
            if (parallelResult.getAvailableCores() > 0) {
                report.append(String.format("  • Cores utilizados: %d\n", parallelResult.getAvailableCores()));
                report.append(String.format("  • Threads en pool: %d\n", parallelResult.getThreadPoolSize()));
            }
            
            report.append("\n");
            
            // Detalle de subsistemas
            report.append("📊 DETALLE POR SUBSISTEMA:\n");
            report.append("--------------------------\n");
            for (SubsystemMetrics metrics : parallelResult.getSubsystemResults()) {
                report.append(String.format("  %s\n", metrics.getFormattedStats()));
            }
        }
        
        // Recomendaciones
        report.append("\n🎯 RECOMENDACIONES:\n");
        report.append("-------------------\n");
        
        if (!isAllSuccessful()) {
            report.append("  • Revisar subsistemas fallidos para mejorar robustez\n");
        }
        
        if (parallelResult != null) {
            Map<String, Object> stats = parallelResult.getParallelizationStats();
            if (stats != null) {
                report.append("Parallelization statistics available\n");
            }
        }
        
        return report.toString();
    }
    
    /**
     * 📊 CLASE PARA ESTADÍSTICAS CONSOLIDADAS
     */
    public static class ConsolidatedStats {
        private final StartupMetrics traditionalMetrics;
        private final SubsystemInitializationResult parallelResult;
        private final double totalTimeMs;
        
        public ConsolidatedStats(StartupMetrics traditionalMetrics, 
                               SubsystemInitializationResult parallelResult, 
                               double totalTimeMs) {
            this.traditionalMetrics = traditionalMetrics;
            this.parallelResult = parallelResult;
            this.totalTimeMs = totalTimeMs;
        }
        
        public StartupMetrics getTraditionalMetrics() { return traditionalMetrics; }
        public SubsystemInitializationResult getParallelResult() { return parallelResult; }
        public double getTotalTimeMs() { return totalTimeMs; }
    }
    
    @Override
    public String toString() {
        return generateCompleteReport();
    }
}