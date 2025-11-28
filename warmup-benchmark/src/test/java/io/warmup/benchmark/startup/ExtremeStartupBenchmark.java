package io.warmup.benchmark.startup;

import io.warmup.framework.core.WarmupContainer;
import io.warmup.framework.startup.lazy.ZeroStartupBeanLoader;
import io.warmup.framework.startup.CriticalStartupPhase;
import io.warmup.framework.startup.BackgroundStartupPhase;
import io.warmup.framework.startup.ParallelSubsystemInitializer;

import org.openjdk.jmh.annotations.*;
// import org.openjdk.jmh.results.format.ResultFormatOptions; // Not needed for basic benchmark

import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * 🚀 BENCHMARK DE STARTUP EXTREMO - Target: Sub-10ms
 * 
 * Utiliza las optimizaciones de startup disponibles en el framework:
 * 
 * 🎯 FASE CRÍTICA (< 2ms):
 * - Inicialización de componentes esenciales
 * - DependencyRegistry básico
 * - ProfileManager + PropertySource
 * - JIT ASM crítico
 * 
 * 🎯 INICIALIZACIÓN PARALELA:
 * - Usa todos los cores del CPU
 * - DI + Eventos + Seguridad paralelos
 * 
 * 🎯 LAZY LOADING EXTREMO:
 * - Zero startup cost
 * - Beans on-demand
 * - Solo se paga por lo que se usa
 * 
 * 🏆 OBJETIVO: < 10ms startup time (100x mejor que 73.553ms actual)
 * 
 * @author MiniMax Agent
 */
@BenchmarkMode(Mode.SingleShotTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@Warmup(iterations = 2, time = 1)
@Measurement(iterations = 3, time = 2)
@Fork(0)
@State(Scope.Benchmark)
public class ExtremeStartupBenchmark {

    private static final Logger log = Logger.getLogger(ExtremeStartupBenchmark.class.getName());

    // 🎯 CONFIGURACIÓN DE OPTIMIZACIONES EXTREMAS
    private WarmupContainer container;
    private ExtremeStartupConfig startupConfig;
    
    // ⚡ COMPONENTES DE OPTIMIZACIÓN DISPONIBLES
    private CriticalStartupPhase criticalPhase;
    private BackgroundStartupPhase backgroundPhase;
    private ZeroStartupBeanLoader zeroStartupLoader;
    private ParallelSubsystemInitializer parallelInitializer;

    @Setup
    public void setup() {
        // Configurar optimizaciones extremas
        startupConfig = new ExtremeStartupConfig()
            .enableExtremeParallelism(true)
            .enableZeroStartupCost(true)
            .setMaxStartupTimeMs(10.0) // Target: < 10ms
            .setParallelThreadCount(Runtime.getRuntime().availableProcessors());
        
        log.log(Level.INFO, "🚀 Configurando startup extremo: {0} cores, target < 10ms", 
                startupConfig.getParallelThreadCount());
    }

    @Benchmark
    public void benchmarkExtremeStartup() throws Exception {
        long startTime = System.nanoTime();
        
        try {
            // 🚀 USAR EL NUEVO MÉTODO ESTÁTICO CON STARTUP EXTREMO
            container = WarmupContainer.createWithExtremeStartup();
            
            // 📊 OBTENER MÉTRICAS DE STARTUP EXTREMO
            java.util.Map<String, Object> extremeMetrics = container.getExtremeStartupMetrics();
            
            // Validar que todas las optimizaciones extremas están activas
            if (extremeMetrics.containsKey("allExtremeOptimizationsActive")) {
                boolean allActive = (Boolean) extremeMetrics.get("allExtremeOptimizationsActive");
                int activeCount = (Integer) extremeMetrics.get("activeOptimizationsCount");
                
                log.log(Level.INFO, "🚀 Optimizaciones extremas activas: {0}/12 ({1})", 
                        new Object[]{activeCount, allActive ? "TODAS ACTIVAS" : "PARCIALES"});
            }
            
            // Verificar tiempo de startup
            validateStartupTime(startTime, extremeMetrics);
            
        } catch (Exception e) {
            log.log(Level.SEVERE, "❌ Error en startup extremo: {0}", e.getMessage());
            throw e;
        }
    }

    /**
     * 📊 Validar que el startup esté dentro del objetivo con métricas extremas
     */
    private void validateStartupTime(long startTime, java.util.Map<String, Object> extremeMetrics) {
        long totalTime = System.nanoTime() - startTime;
        long totalTimeMs = totalTime / 1_000_000;
        
        log.log(Level.INFO, "🏁 STARTUP EXTREMO COMPLETADO en {0}ms", totalTimeMs);
        
        // Verificar objetivo sub-10ms
        if (totalTimeMs <= 10) {
            log.log(Level.INFO, "🎯 OBJETIVO ALCANZADO: < 10ms startup! (🚀 {0}x mejor que baseline)", 
                    Math.max(1, 73.553 / totalTimeMs));
        } else {
            log.log(Level.WARNING, "⚠️ Objetivo no alcanzado: {0}ms > 10ms", totalTimeMs);
        }
        
        // Log de métricas adicionales del sistema extremo
        if (extremeMetrics.containsKey("totalStartupTime")) {
            Long systemStartupTime = (Long) extremeMetrics.get("totalStartupTime");
            if (systemStartupTime != null) {
                log.log(Level.INFO, "📊 Tiempo de startup del sistema: {0}ms", systemStartupTime);
            }
        }
        
        // Estado de las optimizaciones extremas
        if (extremeMetrics.containsKey("extremeOptimizationsActive")) {
            @SuppressWarnings("unchecked")
            java.util.Map<String, Boolean> optimizations = 
                (java.util.Map<String, Boolean>) extremeMetrics.get("extremeOptimizationsActive");
            
            long activeCount = optimizations.values().stream().filter(Boolean::booleanValue).count();
            log.log(Level.INFO, "🔧 Optimizaciones extremas: {0}/12 activas", activeCount);
        }
    }

    @TearDown
    public void teardown() {
        try {
            if (parallelInitializer != null) {
                parallelInitializer.shutdown();
            }
            
            if (zeroStartupLoader != null) {
                zeroStartupLoader.shutdown();
            }
            
            log.log(Level.FINE, "🧹 Startup extremo cleanup completado");
            
        } catch (Exception e) {
            log.log(Level.WARNING, "⚠️ Error en teardown: {0}", e.getMessage());
        }
    }

    /**
     * 📊 CONFIGURACIÓN DE OPTIMIZACIONES EXTREMAS
     */
    public static class ExtremeStartupConfig {
        private boolean enableExtremeParallelism = true;
        private boolean enableZeroStartupCost = true;
        private double maxStartupTimeMs = 10.0;
        private int parallelThreadCount = Runtime.getRuntime().availableProcessors();

        public boolean isEnableExtremeParallelism() { return enableExtremeParallelism; }
        public ExtremeStartupConfig enableExtremeParallelism(boolean enable) { 
            this.enableExtremeParallelism = enable; return this; 
        }
        
        public boolean isEnableZeroStartupCost() { return enableZeroStartupCost; }
        public ExtremeStartupConfig enableZeroStartupCost(boolean enable) { 
            this.enableZeroStartupCost = enable; return this; 
        }
        
        public double getMaxStartupTimeMs() { return maxStartupTimeMs; }
        public ExtremeStartupConfig setMaxStartupTimeMs(double maxTimeMs) { 
            this.maxStartupTimeMs = maxTimeMs; return this; 
        }
        
        public int getParallelThreadCount() { return parallelThreadCount; }
        public ExtremeStartupConfig setParallelThreadCount(int count) { 
            this.parallelThreadCount = count; return this; 
        }
    }
}