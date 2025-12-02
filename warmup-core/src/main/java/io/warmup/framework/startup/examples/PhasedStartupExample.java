package io.warmup.framework.startup.examples;

import io.warmup.framework.core.WarmupContainer;
import io.warmup.framework.startup.StartupMetrics;
import io.warmup.framework.startup.StartupPhasesManager;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * 🎯 EJEMPLO DE STARTUP POR FASES
 * 
 * Demuestra cómo usar el sistema de startup optimizado:
 * - Fase crítica: < 2ms
 * - Fase background: No bloqueante
 * 
 * @author MiniMax Agent
 * @version 1.0
 */
public class PhasedStartupExample {
    
    private static final Logger log = Logger.getLogger(PhasedStartupExample.class.getName());
    
    public static void main(String[] args) {
        log.log(Level.INFO, "🎯 DEMO: Startup por fases optimizado");
        
        // ===== MODO 1: STARTUP AUTOMÁTICO POR FASES =====
        log.log(Level.INFO, "\n📋 MODO 1: Startup automático por fases");
        demoAutomaticPhasedStartup();
        
        // ===== MODO 2: CONTROL MANUAL DE FASES =====
        log.log(Level.INFO, "\n📋 MODO 2: Control manual de fases");
        demoManualPhasedStartup();
        
        // ===== MODO 3: MEDICIÓN DE RENDIMIENTO =====
        log.log(Level.INFO, "\n📋 MODO 3: Medición de rendimiento");
        demoPerformanceMeasurement();
    }
    
    /**
     * 📋 DEMO 1: Startup automático por fases
     */
    private static void demoAutomaticPhasedStartup() {
        try {
            // Crear container con startup por fases habilitado
            WarmupContainer container = new WarmupContainer("demo", new String[]{"demo"});
            
            log.log(Level.INFO, "✅ Container creado con startup por fases");
            
            // El container ya está listo para uso después de la fase crítica
            // Verificar que la fase crítica se completó
            if (container.isCriticalPhaseCompleted()) {
                log.log(Level.INFO, "🎯 FASE CRÍTICA completada - Container listo para uso");
            }
            
            // Obtener métricas de startup
            Object metrics = container.getStartupMetrics();
            if (metrics != null) {
                log.log(Level.INFO, "📊 Métricas de startup: {0}", metrics);
            }
            
            // Usar el container normalmente
            // MyService service = container.get(MyService.class);
            
            container.shutdown();
            
        } catch (Exception e) {
            log.log(Level.SEVERE, "❌ Error en demo automático: {0}", e.getMessage());
        }
    }
    
    /**
     * 📋 DEMO 2: Control manual de fases
     */
    private static void demoManualPhasedStartup() {
        try {
            // Crear container sin inicialización automática
            WarmupContainer container = new WarmupContainer(null, new String[]{"demo"});
            
            log.log(Level.INFO, "🔧 Ejecutando fase crítica manualmente...");
            
            // Ejecutar solo la fase crítica
            long criticalStart = System.nanoTime();
            container.executeCriticalPhaseOnly();
            long criticalDuration = (System.nanoTime() - criticalStart) / 1_000_000;
            
            log.log(Level.INFO, "🎯 FASE CRÍTICA completada en {0}ms", criticalDuration);
            
            // Iniciar la fase background manualmente
            log.log(Level.INFO, "🔄 Iniciando fase background...");
            container.startBackgroundPhase();
            
            // Hacer trabajo mientras la fase background se ejecuta
            log.log(Level.INFO, "💼 Haciendo trabajo mientras fase background se ejecuta...");
            
            // Esperar a que la fase background termine (simulado con sleep)
            try {
                Thread.sleep(100); // Dar tiempo a que la fase background se ejecute
                log.log(Level.INFO, "✅ Fase background completada");
            } catch (Exception e) {
                log.log(Level.WARNING, "⏰ Error en fase background: {0}", e.getMessage());
            }
            
            container.shutdown();
            
        } catch (Exception e) {
            log.log(Level.SEVERE, "❌ Error en demo manual: {0}", e.getMessage());
        }
    }
    
    /**
     * 📋 DEMO 3: Medición de rendimiento
     */
    private static void demoPerformanceMeasurement() {
        try {
            log.log(Level.INFO, "🔬 Iniciando benchmark de startup...");
            
            // Probar startup tradicional vs startup por fases
            benchmarkStartupComparison();
            
        } catch (Exception e) {
            log.log(Level.SEVERE, "❌ Error en demo de rendimiento: {0}", e.getMessage());
        }
    }
    
    /**
     * 🔬 Comparar rendimiento: startup tradicional vs startup por fases
     */
    private static void benchmarkStartupComparison() {
        
        // BENCHMARK 1: Startup tradicional
        long traditionalStart = System.nanoTime();
        try {
            WarmupContainer traditionalContainer = new WarmupContainer(null, new String[]{"benchmark"});
            traditionalContainer.shutdown();
        } catch (Exception e) {
            log.log(Level.WARNING, "Error en container tradicional: {0}", e.getMessage());
        }
        long traditionalDuration = (System.nanoTime() - traditionalStart) / 1_000_000;
        
        // BENCHMARK 2: Startup por fases
        long phasedStart = System.nanoTime();
        try {
            WarmupContainer phasedContainer = new WarmupContainer("benchmark", new String[]{"benchmark"});
            phasedContainer.shutdown();
        } catch (Exception e) {
            log.log(Level.WARNING, "Error en container por fases: {0}", e.getMessage());
        }
        long phasedDuration = (System.nanoTime() - phasedStart) / 1_000_000;
        
        // BENCHMARK 3: Solo fase crítica
        long criticalOnlyStart = System.nanoTime();
        try {
            WarmupContainer criticalOnlyContainer = new WarmupContainer(null, new String[]{"benchmark"});
            criticalOnlyContainer.executeCriticalPhaseOnly();
            criticalOnlyContainer.shutdown();
        } catch (Exception e) {
            log.log(Level.WARNING, "Error en container fase crítica: {0}", e.getMessage());
        }
        long criticalOnlyDuration = (System.nanoTime() - criticalOnlyStart) / 1_000_000;
        
        // Resultados del benchmark
        log.log(Level.INFO, "\n📊 BENCHMARK DE STARTUP:");
        log.log(Level.INFO, "   Startup tradicional: {0}ms", traditionalDuration);
        log.log(Level.INFO, "   Startup por fases: {0}ms", phasedDuration);
        log.log(Level.INFO, "   Solo fase crítica: {0}ms", criticalOnlyDuration);
        
        if (criticalOnlyDuration < 2) {
            log.log(Level.INFO, "🎯 TARGET ALCANZADO: Fase crítica < 2ms ✅");
        } else {
            log.log(Level.WARNING, "⚠️ TARGET NO ALCANZADO: Fase crítica {0}ms (> 2ms)", criticalOnlyDuration);
        }
        
        // Calcular mejora
        if (traditionalDuration > 0) {
            double improvement = ((double)(traditionalDuration - criticalOnlyDuration) / traditionalDuration) * 100;
            log.log(Level.INFO, "📈 Mejora estimada: {0}% más rápido", String.format("%.1f", improvement));
        }
    }
}