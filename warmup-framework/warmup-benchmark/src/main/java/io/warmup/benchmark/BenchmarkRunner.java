package io.warmup.benchmark;

import io.warmup.benchmark.startup.BaselineStartupBenchmark;
import io.warmup.benchmark.startup.ExtremeStartupBenchmark;
import io.warmup.benchmark.startup.SimpleStartupBenchmark;
import org.openjdk.jmh.results.format.ResultFormatType;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;
import org.openjdk.jmh.runner.options.VerboseMode;

import java.io.File;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * 🏃‍♂️ BENCHMARK RUNNER PRINCIPAL
 * 
 * Ejecuta todos los benchmarks del framework Warmup de manera organizada:
 * 
 * 📊 BENCHMARKS DISPONIBLES:
 * 1. BaselineStartupBenchmark - Rendimiento baseline sin optimizaciones extremas
 * 2. ExtremeStartupBenchmark - Rendimiento con todas las optimizaciones activas
 * 
 * 🎯 OBJETIVO:
 * Comparar startup times baseline vs optimizado para medir mejoras
 * 
 * 🚀 EJECUCIÓN:
 * java -cp target/classes:target/dependency/* io.warmup.benchmark.BenchmarkRunner
 * 
 * @author MiniMax Agent
 */
public class BenchmarkRunner {
    
    private static final Logger log = Logger.getLogger(BenchmarkRunner.class.getName());
    
    private static final String RESULTS_DIR = "benchmark-results";
    private static final String BASELINE_RESULTS = "baseline-startup-results.json";
    private static final String EXTREME_RESULTS = "extreme-startup-results.json";
    private static final String COMPARISON_RESULTS = "startup-comparison-results.json";
    
    public static void main(String[] args) {
        try {
            log.log(Level.INFO, "🚀 INICIANDO BENCHMARKS DEL FRAMEWORK WARMUP");
            log.log(Level.INFO, "================================================");
            
            // Crear directorio de resultados
            createResultsDirectory();
            
            // Ejecutar todos los benchmarks
            runAllBenchmarks();
            
            // Generar reporte final
            generateFinalReport();
            
            log.log(Level.INFO, "✅ TODOS LOS BENCHMARKS COMPLETADOS EXITOSAMENTE");
            log.log(Level.INFO, "📊 Resultados disponibles en: {0}", RESULTS_DIR);
            
        } catch (Exception e) {
            log.log(Level.SEVERE, "❌ Error ejecutando benchmarks: {0}", e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }
    
    /**
     * 🏁 Ejecutar todos los benchmarks de manera organizada
     */
    private static void runAllBenchmarks() throws RunnerException {
        
        // 🧪 0. BENCHMARK SIMPLE (sin WarmupContainer, para verificar que el sistema funciona)
        runSimpleBenchmark();
        
        // 📊 1. BENCHMARK BASELINE (sin optimizaciones extremas)
        runBaselineBenchmark();
        
        // 🚀 2. BENCHMARK EXTREMO (con todas las optimizaciones)
        runExtremeStartupBenchmark();
        
        // 📈 3. ANÁLISIS COMPARATIVO
        runComparisonAnalysis();
    }
    
    /**
     * 📊 Ejecutar benchmark baseline
     */
    private static void runBaselineBenchmark() throws RunnerException {
        log.log(Level.INFO, "📊 INICIANDO BENCHMARK BASELINE");
        log.log(Level.INFO, "Configuración: WarmupContainer estándar SIN optimizaciones extremas");
        
        Options baselineOptions = new OptionsBuilder()
                .include(BaselineStartupBenchmark.class.getSimpleName())
                .result(RESULTS_DIR + "/" + BASELINE_RESULTS)
                .resultFormat(ResultFormatType.JSON)
                .verbosity(VerboseMode.NORMAL)
                .build();
        
        new Runner(baselineOptions).run();
        
        log.log(Level.INFO, "✅ Benchmark baseline completado: {0}", BASELINE_RESULTS);
    }
    
    /**
     * 🧪 Ejecutar benchmark simple (sin WarmupContainer)
     */
    private static void runSimpleBenchmark() throws RunnerException {
        log.log(Level.INFO, "🧪 INICIANDO BENCHMARK SIMPLE");
        log.log(Level.INFO, "Configuración: Test básico SIN WarmupContainer para verificar funcionamiento");
        
        Options simpleOptions = new OptionsBuilder()
                .include(SimpleStartupBenchmark.class.getSimpleName())
                .result(RESULTS_DIR + "/simple-startup-results.json")
                .resultFormat(ResultFormatType.JSON)
                .verbosity(VerboseMode.NORMAL)
                .build();
        
        new Runner(simpleOptions).run();
        
        log.log(Level.INFO, "✅ Benchmark simple completado: simple-startup-results.json");
    }
    
    /**
     * 🚀 Ejecutar benchmark extremo
     */
    private static void runExtremeStartupBenchmark() throws RunnerException {
        log.log(Level.INFO, "🚀 INICIANDO BENCHMARK EXTREMO");
        log.log(Level.INFO, "Configuración: WarmupContainer con optimizaciones extremas activas");
        log.log(Level.INFO, "Target: < 10ms startup time");
        
        Options extremeOptions = new OptionsBuilder()
                .include(ExtremeStartupBenchmark.class.getSimpleName())
                .result(RESULTS_DIR + "/" + EXTREME_RESULTS)
                .resultFormat(ResultFormatType.JSON)
                .verbosity(VerboseMode.NORMAL)
                .build();
        
        new Runner(extremeOptions).run();
        
        log.log(Level.INFO, "✅ Benchmark extremo completado: {0}", EXTREME_RESULTS);
    }
    
    /**
     * 📈 Ejecutar análisis comparativo
     */
    private static void runComparisonAnalysis() throws RunnerException {
        log.log(Level.INFO, "📈 EJECUTANDO ANÁLISIS COMPARATIVO");
        
        Options comparisonOptions = new OptionsBuilder()
                .include(BaselineStartupBenchmark.class.getSimpleName() + ".*")
                .include(ExtremeStartupBenchmark.class.getSimpleName() + ".*")
                .result(RESULTS_DIR + "/" + COMPARISON_RESULTS)
                .resultFormat(ResultFormatType.JSON)
                .verbosity(VerboseMode.NORMAL)
                .build();
        
        new Runner(comparisonOptions).run();
        
        log.log(Level.INFO, "✅ Análisis comparativo completado: {0}", COMPARISON_RESULTS);
    }
    
    /**
     * 📁 Crear directorio de resultados
     */
    private static void createResultsDirectory() {
        File resultsDir = new File(RESULTS_DIR);
        if (!resultsDir.exists()) {
            boolean created = resultsDir.mkdirs();
            if (created) {
                log.log(Level.INFO, "📁 Directorio de resultados creado: {0}", RESULTS_DIR);
            }
        } else {
            log.log(Level.INFO, "📁 Directorio de resultados existe: {0}", RESULTS_DIR);
        }
    }
    
    /**
     * 📊 Generar reporte final
     */
    private static void generateFinalReport() {
        log.log(Level.INFO, "📊 GENERANDO REPORTE FINAL");
        log.log(Level.INFO, "================================================");
        log.log(Level.INFO, "📁 Archivos de resultados generados:");
        log.log(Level.INFO, "   • {0}", BASELINE_RESULTS);
        log.log(Level.INFO, "   • {0}", EXTREME_RESULTS);
        log.log(Level.INFO, "   • {0}", COMPARISON_RESULTS);
        log.log(Level.INFO, "================================================");
        log.log(Level.INFO, "🎯 Para analizar los resultados:");
        log.log(Level.INFO, "   1. Comparar baseline vs extreme startup times");
        log.log(Level.INFO, "   2. Verificar si se alcanzó objetivo < 10ms");
        log.log(Level.INFO, "   3. Medir mejoras de rendimiento");
        log.log(Level.INFO, "================================================");
    }
    
    /**
     * 🔧 MÉTODOS DE UTILIDAD
     */
    
    /**
     * Ejecutar solo benchmark baseline
     */
    public static void runBaselineOnly() throws RunnerException {
        log.log(Level.INFO, "📊 EJECUTANDO SOLO BENCHMARK BASELINE");
        createResultsDirectory();
        runBaselineBenchmark();
    }
    
    /**
     * Ejecutar solo benchmark extremo
     */
    public static void runExtremeOnly() throws RunnerException {
        log.log(Level.INFO, "🚀 EJECUTANDO SOLO BENCHMARK EXTREMO");
        createResultsDirectory();
        runExtremeStartupBenchmark();
    }
    
    /**
     * Obtener ruta del directorio de resultados
     */
    public static String getResultsDirectory() {
        return RESULTS_DIR;
    }
    
    /**
     * Verificar si los resultados existen
     */
    public static boolean resultsExist() {
        File baseline = new File(RESULTS_DIR + "/" + BASELINE_RESULTS);
        File extreme = new File(RESULTS_DIR + "/" + EXTREME_RESULTS);
        File comparison = new File(RESULTS_DIR + "/" + COMPARISON_RESULTS);
        
        return baseline.exists() && extreme.exists() && comparison.exists();
    }
}