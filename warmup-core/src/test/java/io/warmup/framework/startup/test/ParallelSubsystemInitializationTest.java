package io.warmup.framework.startup.test;

import io.warmup.framework.core.WarmupContainer;
import io.warmup.framework.startup.*;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * 🧪 PRUEBAS DEL SISTEMA DE INICIALIZACIÓN PARALELA
 * 
 * Valida que el sistema de inicialización paralela funcione correctamente
 * usando todos los cores del CPU y inicializando subsistemas concurrentemente.
 * 
 * @author MiniMax Agent
 * @version 1.0
 */
public class ParallelSubsystemInitializationTest {
    
    private static final Logger log = Logger.getLogger(ParallelSubsystemInitializationTest.class.getName());
    
    /**
     * 🧪 EJECUTAR TODAS LAS PRUEBAS
     */
    public static void main(String[] args) {
        log.log(Level.INFO, "🧪 INICIANDO PRUEBAS DEL SISTEMA DE INICIALIZACIÓN PARALELA");
        
        boolean allTestsPassed = true;
        
        try {
            // Crear container para las pruebas
            WarmupContainer container = new WarmupContainer();
            
            // Ejecutar pruebas
            allTestsPassed &= testParallelSubsystemInitialization(container);
            allTestsPassed &= testParallelizationStatistics(container);
            allTestsPassed &= testParallelVsSequentialPerformance(container);
            allTestsPassed &= testCombinedStartup(container);
            allTestsPassed &= testSystemResourceUtilization(container);
            
            // Resultado final
            if (allTestsPassed) {
                log.log(Level.INFO, "✅ TODAS LAS PRUEBAS PASARON EXITOSAMENTE");
            } else {
                log.log(Level.SEVERE, "❌ ALGUNAS PRUEBAS FALLARON");
            }
            
        } catch (Exception e) {
            log.log(Level.SEVERE, "❌ Error ejecutando pruebas: " + e.getMessage());
            allTestsPassed = false;
        }
        
        System.exit(allTestsPassed ? 0 : 1);
    }
    
    /**
     * 🧪 PRUEBA 1: Inicialización paralela de subsistemas
     */
    private static boolean testParallelSubsystemInitialization(WarmupContainer container) {
        log.log(Level.INFO, "\n🧪 PRUEBA 1: Inicialización Paralela de Subsistemas");
        log.log(Level.INFO, "===================================================");
        
        try {
            long startTime = System.nanoTime();
            
            StartupPhasesManager phasesManager = new StartupPhasesManager(container);
            CompletableFuture<SubsystemInitializationResult> future = 
                phasesManager.executeParallelSubsystemInitialization();
            
            SubsystemInitializationResult result = future.get(30, TimeUnit.SECONDS);
            
            long duration = System.nanoTime() - startTime;
            
            // Validaciones
            boolean test1 = result.getSubsystemResults() != null;
            log.log(Level.INFO, "  ✓ Subsistemas inicializados: " + (test1 ? "✅" : "❌"));
            
            boolean test2 = result.getSubsystemResults().size() > 0;
            log.log(Level.INFO, "  ✓ Al menos un subsistema: " + (test2 ? "✅" : "❌"));
            
            boolean test3 = result.getAvailableCores() > 0;
            log.log(Level.INFO, "  ✓ Cores detectados: " + result.getAvailableCores() + " " + (test3 ? "✅" : "❌"));
            
            boolean test4 = result.getThreadPoolSize() > 0;
            log.log(Level.INFO, "  ✓ Pool de threads creado: " + result.getThreadPoolSize() + " " + (test4 ? "✅" : "❌"));
            
            boolean test5 = duration > 0;
            log.log(Level.INFO, "  ✓ Tiempo medido: " + (duration / 1_000_000) + "ms " + (test5 ? "✅" : "❌"));
            
            log.log(Level.INFO, "  📊 Detalle de " + result.getSubsystemResults().size() + " subsistemas:");
            for (SubsystemMetrics metrics : result.getSubsystemResults()) {
                log.log(Level.INFO, "    " + metrics.getFormattedStats());
            }
            
            phasesManager.shutdown();
            
            boolean passed = test1 && test2 && test3 && test4 && test5;
            log.log(Level.INFO, "  Resultado: " + (passed ? "✅ PASÓ" : "❌ FALLÓ"));
            
            return passed;
            
        } catch (Exception e) {
            log.log(Level.SEVERE, "  ❌ Error en prueba: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * 🧪 PRUEBA 2: Estadísticas de paralelización
     */
    private static boolean testParallelizationStatistics(WarmupContainer container) {
        log.log(Level.INFO, "\n🧪 PRUEBA 2: Estadísticas de Paralelización");
        log.log(Level.INFO, "===========================================");
        
        try {
            StartupPhasesManager phasesManager = new StartupPhasesManager(container);
            
            // Ejecutar inicialización paralela
            phasesManager.executeParallelSubsystemInitialization().get();
            
            // Obtener estadísticas
            ParallelizationStats stats = phasesManager.getParallelizationStats();
            
            // Validaciones
            boolean test1 = stats.getAvailableCores() > 0;
            log.log(Level.INFO, "  ✓ Cores detectados: " + stats.getAvailableCores() + " (" + (test1 ? "✅" : "❌") + ")");
            
            boolean test2 = stats.getThreadPoolSize() > 0;
            log.log(Level.INFO, "  ✓ Threads en pool: " + stats.getThreadPoolSize() + " (" + (test2 ? "✅" : "❌") + ")");
            
            boolean test3 = stats.getSubsystemCount() > 0;
            log.log(Level.INFO, "  ✓ Subsistemas contados: " + stats.getSubsystemCount() + " (" + (test3 ? "✅" : "❌") + ")");
            
            boolean test4 = stats.getCoreUtilization() >= 0.0 && stats.getCoreUtilization() <= 1.0;
            log.log(Level.INFO, "  ✓ Utilización de cores válida: " + String.format("%.2f", stats.getCoreUtilization()) + " (" + (test4 ? "✅" : "❌") + ")");
            
            boolean test5 = stats.getThreadUtilization() >= 0.0 && stats.getThreadUtilization() <= 1.0;
            log.log(Level.INFO, "  ✓ Utilización de threads válida: " + String.format("%.2f", stats.getThreadUtilization()) + " (" + (test5 ? "✅" : "❌") + ")");
            
            // Mostrar reporte detallado
            log.log(Level.INFO, "\n📊 ESTADÍSTICAS DETALLADAS:");
            log.log(Level.INFO, stats.generateStatsReport());
            
            phasesManager.shutdown();
            
            boolean passed = test1 && test2 && test3 && test4 && test5;
            log.log(Level.INFO, "  Resultado: " + (passed ? "✅ PASÓ" : "❌ FALLÓ"));
            
            return passed;
            
        } catch (Exception e) {
            log.log(Level.SEVERE, "  ❌ Error en prueba: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * 🧪 PRUEBA 3: Comparación rendimiento paralelo vs secuencial
     */
    private static boolean testParallelVsSequentialPerformance(WarmupContainer container) {
        log.log(Level.INFO, "\n🧪 PRUEBA 3: Rendimiento Paralelo vs Secuencial");
        log.log(Level.INFO, "===============================================");
        
        try {
            // Test paralelo
            StartupPhasesManager phasesManager = new StartupPhasesManager(container);
            
            long parallelStart = System.nanoTime();
            SubsystemInitializationResult parallelResult = 
                phasesManager.executeParallelSubsystemInitialization().get();
            long parallelDuration = System.nanoTime() - parallelStart;
            
            phasesManager.shutdown();
            
            // Calcular tiempo secuencial estimado
            long sequentialTime = parallelResult.getSubsystemResults().stream()
                .mapToLong(SubsystemMetrics::getDurationNs)
                .sum();
            
            double speedup = (double) sequentialTime / parallelDuration;
            
            // Validaciones
            boolean test1 = parallelDuration > 0;
            log.log(Level.INFO, "  ✓ Tiempo paralelo medido: " + (parallelDuration / 1_000_000) + "ms (" + (test1 ? "✅" : "❌") + ")");
            
            boolean test2 = sequentialTime > 0;
            log.log(Level.INFO, "  ✓ Tiempo secuencial calculado: " + (sequentialTime / 1_000_000) + "ms (" + (test2 ? "✅" : "❌") + ")");
            
            boolean test3 = speedup > 1.0; // Debe ser más rápido
            log.log(Level.INFO, "  ✓ Speedup achieved: " + String.format("%.2f", speedup) + "x (" + (test3 ? "✅" : "❌") + ")");
            
            boolean test4 = parallelResult.calculateSpeedup() > 1.0;
            log.log(Level.INFO, "  ✓ Speedup reportado: " + String.format("%.2f", parallelResult.calculateSpeedup()) + "x (" + (test4 ? "✅" : "❌") + ")");
            
            log.log(Level.INFO, "\n📈 COMPARACIÓN DE RENDIMIENTO:");
            log.log(Level.INFO, "  • Tiempo paralelo: " + (parallelDuration / 1_000_000) + "ms");
            log.log(Level.INFO, "  • Tiempo secuencial (estimado): " + (sequentialTime / 1_000_000) + "ms");
            log.log(Level.INFO, "  • Speedup achieved: " + String.format("%.2f", speedup) + "x");
            log.log(Level.INFO, "  • Eficiencia paralela: " + String.format("%.1f", parallelResult.calculateParallelizationEfficiency() * 100) + "%");
            
            boolean passed = test1 && test2 && test3 && test4;
            log.log(Level.INFO, "  Resultado: " + (passed ? "✅ PASÓ" : "❌ FALLÓ"));
            
            return passed;
            
        } catch (Exception e) {
            log.log(Level.SEVERE, "  ❌ Error en prueba: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * 🧪 PRUEBA 4: Startup combinado
     */
    private static boolean testCombinedStartup(WarmupContainer container) {
        log.log(Level.INFO, "\n🧪 PRUEBA 4: Startup Combinado");
        log.log(Level.INFO, "================================");
        
        try {
            StartupPhasesManager phasesManager = new StartupPhasesManager(container);
            
            long startTime = System.nanoTime();
            CompletableFuture<CombinedStartupResult> future = 
                phasesManager.executeCombinedStartup();
            
            CombinedStartupResult result = future.get(60, TimeUnit.SECONDS);
            long duration = System.nanoTime() - startTime;
            
            // Validaciones
            boolean test1 = result.getTraditionalMetrics() != null;
            log.log(Level.INFO, "  ✓ Métricas tradicionales: " + (test1 ? "✅" : "❌"));
            
            boolean test2 = result.getParallelResult() != null;
            log.log(Level.INFO, "  ✓ Resultado paralelo: " + (test2 ? "✅" : "❌"));
            
            boolean test3 = duration > 0;
            log.log(Level.INFO, "  ✓ Tiempo total medido: " + (duration / 1_000_000) + "ms (" + (test3 ? "✅" : "❌") + ")");
            
            boolean test4 = result.getTotalDurationMs() > 0;
            log.log(Level.INFO, "  ✓ Duración total calculada: " + result.getTotalDurationMs() + "ms (" + (test4 ? "✅" : "❌") + ")");
            
            log.log(Level.INFO, "\n📊 RESULTADOS DEL STARTUP COMBINADO:");
            log.log(Level.INFO, "  • Tiempo total: " + (duration / 1_000_000) + "ms");
            log.log(Level.INFO, "  • Todas las fases exitosas: " + (result.isAllSuccessful() ? "Sí" : "No"));
            
            if (result.getParallelResult() != null) {
                log.log(Level.INFO, "  • Subsistemas exitosos: " + result.getParallelResult().getSuccessCount() + "/" + result.getParallelResult().getSubsystemResults().size());
            }
            
            phasesManager.shutdown();
            
            boolean passed = test1 && test2 && test3 && test4;
            log.log(Level.INFO, "  Resultado: " + (passed ? "✅ PASÓ" : "❌ FALLÓ"));
            
            return passed;
            
        } catch (Exception e) {
            log.log(Level.SEVERE, "  ❌ Error en prueba: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * 🧪 PRUEBA 5: Utilización de recursos del sistema
     */
    private static boolean testSystemResourceUtilization(WarmupContainer container) {
        log.log(Level.INFO, "\n🧪 PRUEBA 5: Utilización de Recursos del Sistema");
        log.log(Level.INFO, "=================================================");
        
        try {
            int cores = Runtime.getRuntime().availableProcessors();
            
            StartupPhasesManager phasesManager = new StartupPhasesManager(container);
            
            // Ejecutar inicialización paralela
            phasesManager.executeParallelSubsystemInitialization().get();
            
            ParallelizationStats stats = phasesManager.getParallelizationStats();
            
            // Validaciones
            boolean test1 = stats.getAvailableCores() == cores;
            log.log(Level.INFO, "  ✓ Cores detectados correctamente: " + stats.getAvailableCores() + " == " + cores + " (" + (test1 ? "✅" : "❌") + ")");
            
            boolean test2 = stats.getThreadPoolSize() > 0;
            log.log(Level.INFO, "  ✓ Pool de threads creado: " + stats.getThreadPoolSize() + " threads (" + (test2 ? "✅" : "❌") + ")");
            
            boolean test3 = stats.getThreadPoolSize() <= cores * 2; // No over-threading excesivo
            log.log(Level.INFO, "  ✓ Threads dentro de límites: " + stats.getThreadPoolSize() + " <= " + (cores * 2) + " (" + (test3 ? "✅" : "❌") + ")");
            
            boolean test4 = stats.isUsingAllCores() || stats.getThreadPoolSize() >= cores - 1;
            log.log(Level.INFO, "  ✓ Utilización efectiva de cores: " + (stats.isUsingAllCores() ? "Usando todos" : "Casi todos") + " (" + (test4 ? "✅" : "❌") + ")");
            
            log.log(Level.INFO, "\n🖥️ INFORMACIÓN DEL SISTEMA:");
            log.log(Level.INFO, "  • Cores físicos: " + cores);
            log.log(Level.INFO, "  • Threads asignados: " + stats.getThreadPoolSize());
            log.log(Level.INFO, "  • Utilización de cores: " + String.format("%.1f", stats.getCoreUtilization() * 100) + "%");
            log.log(Level.INFO, "  • Configuración eficiente: " + (stats.getConfigurationEfficiency() > 0.8 ? "✅ Sí" : "⚠️ Posible optimización"));
            
            // Mostrar recomendaciones
            log.log(Level.INFO, "\n🎯 RECOMENDACIONES:");
            log.log(Level.INFO, stats.getRecommendations().getRecommendationsText());
            
            phasesManager.shutdown();
            
            boolean passed = test1 && test2 && test3 && test4;
            log.log(Level.INFO, "  Resultado: " + (passed ? "✅ PASÓ" : "❌ FALLÓ"));
            
            return passed;
            
        } catch (Exception e) {
            log.log(Level.SEVERE, "  ❌ Error en prueba: " + e.getMessage());
            return false;
        }
    }
}