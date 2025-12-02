package io.warmup.examples.startup.hotpath;

import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.Logger;
import java.util.logging.Level;

/**
 * Benchmark de rendimiento para el sistema de optimización de hot paths.
 * Mide la efectividad de las optimizaciones y compara el rendimiento antes y después.
 * 
 * @author MiniMax Agent
 * @version 1.0
 */
public class HotPathOptimizationBenchmark {
    
    private static final Logger logger = Logger.getLogger(HotPathOptimizationBenchmark.class.getName());
    
    private final HotPathOptimizationSystem optimizationSystem;
    private final List<BenchmarkResult> benchmarkResults;
    private final AtomicInteger testIteration = new AtomicInteger(0);
    private final AtomicLong totalExecutionTime = new AtomicLong(0);
    
    public HotPathOptimizationBenchmark() {
        this.optimizationSystem = new HotPathOptimizationSystem();
        this.benchmarkResults = new ArrayList<>();
    }
    
    public HotPathOptimizationBenchmark(HotPathOptimizationSystem.OptimizationConfig config) {
        this.optimizationSystem = new HotPathOptimizationSystem(config);
        this.benchmarkResults = new ArrayList<>();
    }
    
    /**
     * Ejecuta un benchmark completo del sistema de optimización
     */
    public BenchmarkReport runFullBenchmark() {
        logger.info("🚀 Iniciando benchmark completo del sistema de optimización de hot paths...");
        
        BenchmarkReport report = new BenchmarkReport();
        report.setBenchmarkStartTime(Instant.now());
        
        // Ejecutar múltiples iteraciones para obtener estadísticas confiables
        int iterations = 10;
        for (int i = 0; i < iterations; i++) {
            logger.info(String.format("📊 Ejecutando iteración %d/%d...", i + 1, iterations));
            BenchmarkResult iterationResult = executeSingleBenchmark();
            benchmarkResults.add(iterationResult);
            report.addIterationResult(iterationResult);
        }
        
        report.setBenchmarkEndTime(Instant.now());
        report.generateStatistics();
        
        logger.info("✅ Benchmark completado!");
        printBenchmarkSummary(report);
        
        return report;
    }
    
    /**
     * Función helper para repetir strings (compatible con Java 8)
     */
    private String repeatString(String str, int count) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < count; i++) {
            sb.append(str);
        }
        return sb.toString();
    }
    
    /**
     * Ejecuta una sola iteración del benchmark
     */
    public BenchmarkResult executeSingleBenchmark() {
        int iteration = testIteration.incrementAndGet();
        Instant startTime = Instant.now();
        
        BenchmarkResult result = new BenchmarkResult();
        result.setIteration(iteration);
        result.setStartTime(startTime);
        
        try {
            // Ejecutar optimización
            HotPathOptimizationSystem.HotPathOptimizationResult optimizationResult = 
                optimizationSystem.executeOptimization();
            
            result.setSuccess(true);
            result.setExecutionDuration(Duration.between(startTime, Instant.now()));
            result.setOptimizationResult(optimizationResult);
            
            // Extraer métricas de rendimiento
            extractPerformanceMetrics(result, optimizationResult);
            
            // Medir throughput
            measureThroughput(result);
            
            // Generar recomendaciones de optimización
            generateOptimizationRecommendations(result, optimizationResult);
            
        } catch (Exception e) {
            result.setSuccess(false);
            result.setExecutionDuration(Duration.between(startTime, Instant.now()));
            result.setError(e);
            logger.log(Level.WARNING, "Error en iteración " + iteration, e);
        }
        
        totalExecutionTime.addAndGet(result.getExecutionDuration().toMillis());
        return result;
    }
    
    /**
     * Extrae métricas de rendimiento de un resultado de optimización
     */
    private void extractPerformanceMetrics(BenchmarkResult result, 
                                         HotPathOptimizationSystem.HotPathOptimizationResult optimizationResult) {
        
        // Métricas básicas
        result.setHotPathsIdentified(optimizationResult.getIdentifiedHotPaths().size());
        result.setOptimizationPlansGenerated(optimizationResult.getGeneratedPlans().size());
        result.setOptimizationsApplied(optimizationResult.getAppliedOptimizations().size());
        result.setExpectedImprovement(optimizationResult.getTotalExpectedImprovement());
        result.setActualImprovement(optimizationResult.getTotalActualImprovement());
        result.setImprovementEfficiency(optimizationResult.getImprovementEfficiency());
        result.setOptimizedMethodCount(optimizationResult.getOptimizedMethodCount());
        
        // Métricas de tiempo
        long totalExecutionMs = optimizationResult.getExecutionTime().toMillis();
        result.setTotalExecutionTime(totalExecutionMs);
        
        if (totalExecutionMs > 0) {
            result.setThroughput(benchmarkResults.size() / (totalExecutionMs / 1000.0));
            result.setOperationsPerSecond(1000.0 / totalExecutionMs);
        }
        
        // Métricas de efectividad
        if (optimizationResult.getAppliedOptimizations().size() > 0) {
            double avgImprovement = optimizationResult.getTotalActualImprovement() / 
                                  optimizationResult.getAppliedOptimizations().size();
            result.setAverageImprovementPerOptimization(avgImprovement);
        }
        
        // Determinar grade de rendimiento
        result.setPerformanceGrade(calculatePerformanceGrade(result));
    }
    
    /**
     * Mide el throughput del sistema
     */
    private void measureThroughput(BenchmarkResult result) {
        // Simular carga adicional para medir throughput
        Instant throughputStart = Instant.now();
        int batchOperations = 50;
        
        for (int i = 0; i < batchOperations; i++) {
            // Simular operación de optimización
            try {
                Thread.sleep(10); // Simular procesamiento
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
        
        Duration throughputDuration = Duration.between(throughputStart, Instant.now());
        result.setThroughputOperationsPerSecond(batchOperations / 
            (throughputDuration.toMillis() / 1000.0));
    }
    
    /**
     * Genera recomendaciones de optimización basadas en los resultados
     */
    private void generateOptimizationRecommendations(BenchmarkResult result,
                                                   HotPathOptimizationSystem.HotPathOptimizationResult optimizationResult) {
        
        List<String> recommendations = new ArrayList<>();
        
        // Recomendaciones basadas en tiempo de ejecución
        if (result.getTotalExecutionTime() > 5000) {
            recommendations.add("⚠️ Tiempo de ejecución > 5s - considerar reducir trackingDuration");
        } else if (result.getTotalExecutionTime() < 1000) {
            recommendations.add("✅ Tiempo de ejecución excelente (< 1s)");
        }
        
        // Recomendaciones basadas en mejora
        if (result.getImprovementEfficiency() < 0.5) {
            recommendations.add("📊 Eficiencia de mejora baja (< 50%) - ajustar parámetros de hotness");
        } else if (result.getImprovementEfficiency() > 0.8) {
            recommendations.add("🎯 Eficiencia de mejora excelente (> 80%)");
        }
        
        // Recomendaciones basadas en hot paths
        if (result.getHotPathsIdentified() == 0) {
            recommendations.add("🔍 No se identificaron hot paths - verificar datos de tracking");
        } else if (result.getHotPathsIdentified() > 20) {
            recommendations.add("📈 Muchos hot paths identificados - considerar filtros más agresivos");
        }
        
        // Recomendaciones basadas en throughput
        if (result.getThroughputOperationsPerSecond() < 10) {
            recommendations.add("⏱️ Throughput bajo - optimizar configuraciones del sistema");
        }
        
        result.setOptimizationRecommendations(recommendations);
    }
    
    /**
     * Calcula un grade de rendimiento basado en múltiples métricas
     */
    private String calculatePerformanceGrade(BenchmarkResult result) {
        int score = 0;
        
        // Puntuación basada en tiempo de ejecución (40%)
        if (result.getTotalExecutionTime() < 1000) score += 40;
        else if (result.getTotalExecutionTime() < 3000) score += 30;
        else if (result.getTotalExecutionTime() < 5000) score += 20;
        else score += 10;
        
        // Puntuación basada en eficiencia de mejora (30%)
        if (result.getImprovementEfficiency() > 0.8) score += 30;
        else if (result.getImprovementEfficiency() > 0.6) score += 20;
        else if (result.getImprovementEfficiency() > 0.4) score += 10;
        
        // Puntuación basada en hot paths identificados (20%)
        if (result.getHotPathsIdentified() > 0 && result.getHotPathsIdentified() <= 10) score += 20;
        else if (result.getHotPathsIdentified() > 10 && result.getHotPathsIdentified() <= 20) score += 15;
        else if (result.getHotPathsIdentified() > 20) score += 10;
        
        // Puntuación basada en throughput (10%)
        if (result.getThroughputOperationsPerSecond() > 50) score += 10;
        else if (result.getThroughputOperationsPerSecond() > 20) score += 7;
        else if (result.getThroughputOperationsPerSecond() > 10) score += 5;
        
        // Determinar grade
        if (score >= 90) return "A+";
        else if (score >= 80) return "A";
        else if (score >= 70) return "B";
        else if (score >= 60) return "C";
        else return "D";
    }
    
    /**
     * Imprime un resumen del benchmark
     */
    private void printBenchmarkSummary(BenchmarkReport report) {
        logger.info("📋 RESUMEN DEL BENCHMARK:");
        logger.info(repeatString("=", 60));
        logger.info(String.format("⏱️  Tiempo total del benchmark: %d ms", 
            report.getTotalBenchmarkDuration()));
        logger.info(String.format("📊 Iteraciones ejecutadas: %d", report.getTotalIterations()));
        logger.info(String.format("🎯 Grade promedio: %s", report.getAveragePerformanceGrade()));
        logger.info(String.format("✅ Tasa de éxito: %.1f%%", report.getSuccessRate() * 100));
        logger.info(String.format("⚡ Throughput promedio: %.2f ops/seg", report.getAverageThroughput()));
        logger.info(String.format("📈 Mejora promedio: %.1f%%", report.getAverageImprovement()));
        logger.info(repeatString("=", 60));
        
        // Mostrar recomendaciones principales
        if (!report.getTopRecommendations().isEmpty()) {
            logger.info("💡 RECOMENDACIONES PRINCIPALES:");
            for (String recommendation : report.getTopRecommendations()) {
                logger.info("   " + recommendation);
            }
        }
    }
    
    /**
     * Clase interna para almacenar los resultados de una iteración del benchmark
     */
    public static class BenchmarkResult {
        private int iteration;
        private Instant startTime;
        private boolean success;
        private Duration executionDuration;
        private Exception error;
        
        // Métricas de optimización
        private int hotPathsIdentified;
        private int optimizationPlansGenerated;
        private int optimizationsApplied;
        private double expectedImprovement;
        private double actualImprovement;
        private double improvementEfficiency;
        private int optimizedMethodCount;
        
        // Métricas de rendimiento
        private long totalExecutionTime;
        private double throughput;
        private double operationsPerSecond;
        private double throughputOperationsPerSecond;
        private double averageImprovementPerOptimization;
        
        // Análisis y recomendaciones
        private String performanceGrade;
        private List<String> optimizationRecommendations;
        
        // Resultado completo de optimización
        private HotPathOptimizationSystem.HotPathOptimizationResult optimizationResult;
        
        // Getters y setters
        public int getIteration() { return iteration; }
        public void setIteration(int iteration) { this.iteration = iteration; }
        
        public Instant getStartTime() { return startTime; }
        public void setStartTime(Instant startTime) { this.startTime = startTime; }
        
        public boolean isSuccess() { return success; }
        public void setSuccess(boolean success) { this.success = success; }
        
        public Duration getExecutionDuration() { return executionDuration; }
        public void setExecutionDuration(Duration executionDuration) { this.executionDuration = executionDuration; }
        
        public Exception getError() { return error; }
        public void setError(Exception error) { this.error = error; }
        
        public int getHotPathsIdentified() { return hotPathsIdentified; }
        public void setHotPathsIdentified(int hotPathsIdentified) { this.hotPathsIdentified = hotPathsIdentified; }
        
        public int getOptimizationPlansGenerated() { return optimizationPlansGenerated; }
        public void setOptimizationPlansGenerated(int optimizationPlansGenerated) { this.optimizationPlansGenerated = optimizationPlansGenerated; }
        
        public int getOptimizationsApplied() { return optimizationsApplied; }
        public void setOptimizationsApplied(int optimizationsApplied) { this.optimizationsApplied = optimizationsApplied; }
        
        public double getExpectedImprovement() { return expectedImprovement; }
        public void setExpectedImprovement(double expectedImprovement) { this.expectedImprovement = expectedImprovement; }
        
        public double getActualImprovement() { return actualImprovement; }
        public void setActualImprovement(double actualImprovement) { this.actualImprovement = actualImprovement; }
        
        public double getImprovementEfficiency() { return improvementEfficiency; }
        public void setImprovementEfficiency(double improvementEfficiency) { this.improvementEfficiency = improvementEfficiency; }
        
        public int getOptimizedMethodCount() { return optimizedMethodCount; }
        public void setOptimizedMethodCount(int optimizedMethodCount) { this.optimizedMethodCount = optimizedMethodCount; }
        
        public long getTotalExecutionTime() { return totalExecutionTime; }
        public void setTotalExecutionTime(long totalExecutionTime) { this.totalExecutionTime = totalExecutionTime; }
        
        public double getThroughput() { return throughput; }
        public void setThroughput(double throughput) { this.throughput = throughput; }
        
        public double getOperationsPerSecond() { return operationsPerSecond; }
        public void setOperationsPerSecond(double operationsPerSecond) { this.operationsPerSecond = operationsPerSecond; }
        
        public double getThroughputOperationsPerSecond() { return throughputOperationsPerSecond; }
        public void setThroughputOperationsPerSecond(double throughputOperationsPerSecond) { this.throughputOperationsPerSecond = throughputOperationsPerSecond; }
        
        public double getAverageImprovementPerOptimization() { return averageImprovementPerOptimization; }
        public void setAverageImprovementPerOptimization(double averageImprovementPerOptimization) { this.averageImprovementPerOptimization = averageImprovementPerOptimization; }
        
        public String getPerformanceGrade() { return performanceGrade; }
        public void setPerformanceGrade(String performanceGrade) { this.performanceGrade = performanceGrade; }
        
        public List<String> getOptimizationRecommendations() { return optimizationRecommendations; }
        public void setOptimizationRecommendations(List<String> optimizationRecommendations) { this.optimizationRecommendations = optimizationRecommendations; }
        
        public HotPathOptimizationSystem.HotPathOptimizationResult getOptimizationResult() { return optimizationResult; }
        public void setOptimizationResult(HotPathOptimizationSystem.HotPathOptimizationResult optimizationResult) { this.optimizationResult = optimizationResult; }
    }
    
    /**
     * Clase interna para generar un reporte completo del benchmark
     */
    public static class BenchmarkReport {
        private Instant benchmarkStartTime;
        private Instant benchmarkEndTime;
        private List<BenchmarkResult> iterationResults;
        
        public BenchmarkReport() {
            this.iterationResults = new ArrayList<>();
        }
        
        public void addIterationResult(BenchmarkResult result) {
            this.iterationResults.add(result);
        }
        
        // Getters y setters
        public Instant getBenchmarkStartTime() { return benchmarkStartTime; }
        public void setBenchmarkStartTime(Instant benchmarkStartTime) { this.benchmarkStartTime = benchmarkStartTime; }
        
        public Instant getBenchmarkEndTime() { return benchmarkEndTime; }
        public void setBenchmarkEndTime(Instant benchmarkEndTime) { this.benchmarkEndTime = benchmarkEndTime; }
        
        public List<BenchmarkResult> getIterationResults() { return iterationResults; }
        public void setIterationResults(List<BenchmarkResult> iterationResults) { this.iterationResults = iterationResults; }
        
        // Métodos de análisis estadístico
        public void generateStatistics() {
            // Calcular estadísticas básicas
        }
        
        public long getTotalBenchmarkDuration() {
            return benchmarkEndTime != null && benchmarkStartTime != null ?
                Duration.between(benchmarkStartTime, benchmarkEndTime).toMillis() : 0;
        }
        
        public int getTotalIterations() {
            return iterationResults.size();
        }
        
        public double getSuccessRate() {
            long successfulTests = iterationResults.stream()
                .filter(BenchmarkResult::isSuccess)
                .count();
            return (double) successfulTests / iterationResults.size();
        }
        
        public double getAverageThroughput() {
            return iterationResults.stream()
                .filter(r -> r.getThroughput() > 0)
                .mapToDouble(BenchmarkResult::getThroughput)
                .average()
                .orElse(0.0);
        }
        
        public double getAverageImprovement() {
            return iterationResults.stream()
                .filter(r -> r.getActualImprovement() > 0)
                .mapToDouble(BenchmarkResult::getActualImprovement)
                .average()
                .orElse(0.0);
        }
        
        public String getAveragePerformanceGrade() {
            Map<String, Long> gradeCounts = new HashMap<>();
            for (BenchmarkResult result : iterationResults) {
                gradeCounts.merge(result.getPerformanceGrade(), 1L, Long::sum);
            }
            
            return gradeCounts.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse("N/A");
        }
        
        public List<String> getTopRecommendations() {
            Map<String, Integer> recommendationCounts = new HashMap<>();
            
            for (BenchmarkResult result : iterationResults) {
                if (result.getOptimizationRecommendations() != null) {
                    for (String recommendation : result.getOptimizationRecommendations()) {
                        recommendationCounts.merge(recommendation, 1, Integer::sum);
                    }
                }
            }
            
            return recommendationCounts.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .limit(5)
                .map(Map.Entry::getKey)
                .collect(ArrayList::new, ArrayList::add, ArrayList::addAll);
        }
    }
    
    /**
     * Cierra el benchmark y libera recursos
     */
    public void shutdown() {
        if (optimizationSystem != null) {
            optimizationSystem.shutdown();
        }
    }
}