package io.warmup.examples.startup.examples;

import io.warmup.framework.core.WarmupContainer;
import io.warmup.framework.startup.*;

import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * 🚀 EJEMPLO DE INICIALIZACIÓN PARALELA CON TODOS LOS CORES
 * 
 * Demuestra cómo usar el nuevo sistema de startup paralelo que utiliza
 * todos los cores del CPU para inicializar subsistemas concurrentemente.
 * 
 * Este ejemplo muestra tres estrategias diferentes:
 * 1. Solo inicialización paralela (más rápido para subsistemas)
 * 2. Startup combinado (paralelo + tradicional)
 * 3. Startup híbrido (fases críticas + paralelo de subsistemas)
 * 
 * @author MiniMax Agent
 * @version 1.0
 */
public class ParallelStartupExample {
    
    private static final Logger log = Logger.getLogger(ParallelStartupExample.class.getName());
    
    /**
     * 🎯 EJEMPLO PRINCIPAL
     */
    public static void main(String[] args) {
        log.log(Level.INFO, "🚀 Iniciando ejemplo de startup paralelo...");
        
        try {
            // Crear container del framework
            WarmupContainer container = new WarmupContainer();
            
            // 🎯 ESTRATEGIA 1: Solo inicialización paralela (más rápida)
            demonstrateParallelOnlyStrategy(container);
            
            // Pausa entre ejemplos
            Thread.sleep(1000);
            
            // 🎯 ESTRATEGIA 2: Startup combinado (paralelo + tradicional)
            demonstrateCombinedStrategy(container);
            
            // Pausa entre ejemplos  
            Thread.sleep(1000);
            
            // 🎯 ESTRATEGIA 3: Startup híbrido (fases críticas + paralelo)
            demonstrateHybridStrategy(container);
            
            log.log(Level.INFO, "✅ Ejemplo completado exitosamente");
            
        } catch (Exception e) {
            log.log(Level.SEVERE, "❌ Error en ejemplo: {0}", e.getMessage());
        }
    }
    
    /**
     * 🎯 ESTRATEGIA 1: SOLO INICIALIZACIÓN PARALELA
     * 
     * La forma más rápida de startup cuando solo necesitas los subsistemas
     * funcionando sin las fases tradicionales.
     */
    private static void demonstrateParallelOnlyStrategy(WarmupContainer container) throws Exception {
        log.log(Level.INFO, "\n🎯 ESTRATEGIA 1: Solo Inicialización Paralela");
        log.log(Level.INFO, "=============================================");
        
        long startTime = System.nanoTime();
        
        // Crear manager de fases con el sistema paralelo
        StartupPhasesManager phasesManager = new StartupPhasesManager(container);
        
        // Ejecutar solo inicialización paralela
        CompletableFuture<SubsystemInitializationResult> parallelFuture = 
            phasesManager.executeParallelSubsystemInitialization();
        
        // Esperar a que complete
        SubsystemInitializationResult result = parallelFuture.get();
        
        long duration = System.nanoTime() - startTime;
        
        // Mostrar resultados
        log.log(Level.INFO, "✅ INICIALIZACIÓN PARALELA COMPLETADA");
        log.log(Level.INFO, "  • Tiempo total: {0}ms", duration / 1_000_000);
        log.log(Level.INFO, "  • Subsistemas exitosos: {0}/{1}", 
                new Object[]{result.getSuccessCount(), result.getSubsystemResults().size()});
        log.log(Level.INFO, "  • Speedup achieved: {0:.2f}x", result.calculateSpeedup());
        log.log(Level.INFO, "  • Eficiencia paralela: {0:.1f}%", 
                result.calculateParallelizationEfficiency() * 100);
        
        // Mostrar estadísticas de paralelización
        ParallelizationStats stats = phasesManager.getParallelizationStats();
        log.log(Level.INFO, "📊 ESTADÍSTICAS DE PARALELIZACIÓN:");
        log.log(Level.INFO, "  • Cores disponibles: {0}", stats.getAvailableCores());
        log.log(Level.INFO, "  • Threads en pool: {0}", stats.getThreadPoolSize());
        log.log(Level.INFO, "  • Utilización de cores: {0:.1f}%", stats.getCoreUtilization() * 100);
        
        // Cleanup
        phasesManager.shutdown();
    }
    
    /**
     * 🎯 ESTRATEGIA 2: STARTUP COMBINADO
     * 
     * Combina el sistema tradicional (fases crítica/background) con el
     * nuevo sistema de inicialización paralela para máxima cobertura.
     */
    private static void demonstrateCombinedStrategy(WarmupContainer container) throws Exception {
        log.log(Level.INFO, "\n🎯 ESTRATEGIA 2: Startup Combinado");
        log.log(Level.INFO, "===================================");
        
        long startTime = System.nanoTime();
        
        // Crear manager de fases
        StartupPhasesManager phasesManager = new StartupPhasesManager(container);
        
        // Ejecutar startup combinado (tradicional + paralelo)
        CompletableFuture<CombinedStartupResult> combinedFuture = 
            phasesManager.executeCombinedStartup();
        
        // Esperar a que complete
        CombinedStartupResult result = combinedFuture.get();
        
        long duration = System.nanoTime() - startTime;
        
        // Mostrar resultados consolidados
        log.log(Level.INFO, "✅ STARTUP COMBINADO COMPLETADO");
        log.log(Level.INFO, "  • Tiempo total: {0}ms", duration / 1_000_000);
        log.log(Level.INFO, "  • Todas las fases exitosas: {0}", result.isAllSuccessful() ? "Sí" : "No");
        log.log(Level.INFO, "  • Mejora de rendimiento: {0:.1f}%", 
                result.calculatePerformanceImprovement() * 100);
        
        if (result.getParallelResult() != null) {
            log.log(Level.INFO, "  • Subsistemas paralelos exitosos: {0}/{1}", 
                    new Object[]{result.getParallelResult().getSuccessCount(), 
                    result.getParallelResult().getSubsystemResults().size()});
            log.log(Level.INFO, "  • Speedup paralelo: {0:.2f}x", 
                    result.getParallelResult().calculateSpeedup());
        }
        
        // Mostrar reporte completo
        log.log(Level.INFO, "\n📊 REPORTE COMPLETO:");
        log.log(Level.INFO, result.generateCompleteReport());
        
        // Cleanup
        phasesManager.shutdown();
    }
    
    /**
     * 🎯 ESTRATEGIA 3: STARTUP HÍBRIDO
     * 
     * Estrategia avanzada que ejecuta primero las fases críticas tradicionales
     * para funcionalidad básica, y luego inicializa subsistemas en paralelo
     * para funcionalidad avanzada.
     */
    private static void demonstrateHybridStrategy(WarmupContainer container) throws Exception {
        log.log(Level.INFO, "\n🎯 ESTRATEGIA 3: Startup Híbrido");
        log.log(Level.INFO, "=================================");
        
        long globalStartTime = System.nanoTime();
        
        // Crear manager de fases
        StartupPhasesManager phasesManager = new StartupPhasesManager(container);
        
        // PASO 1: Ejecutar solo fase crítica (lo más rápido posible)
        log.log(Level.INFO, "🔥 Ejecutando fase crítica tradicional...");
        long criticalStartTime = System.nanoTime();
        phasesManager.executeCriticalPhase();
        long criticalDuration = System.nanoTime() - criticalStartTime;
        
        log.log(Level.INFO, "✅ Fase crítica completada en {0}ms", criticalDuration / 1_000_000);
        
        // PASO 2: Inicializar subsistemas en paralelo (sin bloquear)
        log.log(Level.INFO, "🚀 Inicializando subsistemas en paralelo...");
        CompletableFuture<SubsystemInitializationResult> parallelFuture = 
            phasesManager.executeParallelSubsystemInitialization();
        
        // PASO 3: Ejecutar fase background tradicional (opcional)
        log.log(Level.INFO, "🔄 Ejecutando fase background tradicional...");
        CompletableFuture<Void> backgroundFuture = phasesManager.executeBackgroundPhaseAsync();
        
        // PASO 4: Esperar a que todo complete
        CompletableFuture.allOf(parallelFuture, backgroundFuture).get();
        
        long globalDuration = System.nanoTime() - globalStartTime;
        
        // Obtener resultados
        SubsystemInitializationResult parallelResult = parallelFuture.get();
        StartupMetrics traditionalMetrics = phasesManager.getStartupMetrics();
        
        // Mostrar resultados
        log.log(Level.INFO, "✅ STARTUP HÍBRIDO COMPLETADO");
        log.log(Level.INFO, "  • Tiempo global: {0}ms", globalDuration / 1_000_000);
        log.log(Level.INFO, "  • Tiempo fase crítica: {0}ms", criticalDuration / 1_000_000);
        log.log(Level.INFO, "  • Fase crítica < 2ms target: {0}", 
                (criticalDuration / 1_000_000) < 2 ? "✅ Sí" : "❌ No");
        
        if (traditionalMetrics.isAllPhasesCompleted()) {
            log.log(Level.INFO, "  • Todas las fases tradicionales: ✅ Completadas");
        }
        
        if (parallelResult.isAllSuccessful()) {
            log.log(Level.INFO, "  • Todos los subsistemas paralelos: ✅ Exitosos");
        }
        
        log.log(Level.INFO, "  • Speedup paralelo: {0:.2f}x", parallelResult.calculateSpeedup());
        log.log(Level.INFO, "  • Eficiencia paralela: {0:.1f}%", 
                parallelResult.calculateParallelizationEfficiency() * 100);
        
        // Comparación de estrategias
        log.log(Level.INFO, "\n📊 COMPARACIÓN DE ESTRATEGIAS:");
        log.log(Level.INFO, "  • Solo paralela: ⚡ Máxima velocidad para subsistemas");
        log.log(Level.INFO, "  • Combinada: 🔄 Máxima cobertura con tradisional + paralelo");
        log.log(Level.INFO, "  • Híbrida: 🎯 Balance óptimo: crítico rápido + paralelo completo");
        
        // Cleanup
        phasesManager.shutdown();
    }
    
    /**
     * 📊 UTILIDAD: Mostrar información del sistema
     */
    private static void displaySystemInfo() {
        int cores = Runtime.getRuntime().availableProcessors();
        long maxMemory = Runtime.getRuntime().maxMemory();
        
        log.log(Level.INFO, "🖥️ INFORMACIÓN DEL SISTEMA:");
        log.log(Level.INFO, "  • Cores disponibles: {0}", cores);
        log.log(Level.INFO, "  • Memoria máxima: {0}MB", maxMemory / (1024 * 1024));
        
        log.log(Level.INFO, "\n🎯 RECOMENDACIONES DE USO:");
        log.log(Level.INFO, "  • Sistemas con 2-4 cores: Usar estrategia híbrida");
        log.log(Level.INFO, "  • Sistemas con 4-8 cores: Usar estrategia combinada");
        log.log(Level.INFO, "  • Sistemas con 8+ cores: Usar estrategia paralela");
    }
}