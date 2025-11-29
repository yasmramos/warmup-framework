package io.warmup.examples.startup.memory.examples;

import io.warmup.framework.startup.memory.*;
import io.warmup.framework.startup.memory.MemoryOptimizationSystem.*;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Logger;

/**
 * 🎯 EJEMPLOS DE OPTIMIZACIÓN DE MEMORIA
 * 
 * Demuestra diferentes casos de uso del sistema de optimización de memoria
 * para pre-loading de páginas y minimización de page faults.
 * 
 * Ejemplos incluidos:
 * 1. Optimización básica de memoria
 * 2. Optimización agresiva para máximo performance
 * 3. Optimización conservadora para startup rápido
 * 4. Integración con otros sistemas de optimización
 * 5. Optimización en background durante startup
 * 6. Análisis detallado de memoria
 * 7. Optimización continua durante runtime
 * 
 * @author MiniMax Agent
 * @version 1.0
 */
public class MemoryOptimizationExample {
    
    private static final Logger log = Logger.getLogger(MemoryOptimizationExample.class.getName());
    
    /**
     * 🎯 EJEMPLO 1: OPTIMIZACIÓN BÁSICA DE MEMORIA
     * Caso más simple - usar configuración por defecto
     */
    public static void example1BasicMemoryOptimization() {
        log.info("🎯 EJEMPLO 1: Optimización básica de memoria");
        
        try {
            // Crear sistema con configuración por defecto
            MemoryOptimizationSystem memoryOptimizer = new MemoryOptimizationSystem();
            
            // Ejecutar optimización completa
            MemoryOptimizationResult result = memoryOptimizer.executeOptimization();
            
            // Procesar resultado
            if (result.isSuccess()) {
                log.info("✅ Optimización exitosa:");
                log.info(String.format("  📄 Páginas analizadas: %d", 
                    result.getAnalysisResult().getTotalPagesAnalyzed()));
                log.info(String.format("  🎯 Páginas pre-cargadas: %d", 
                    result.getPrefetchResult().getPagesPreloaded()));
                log.info(String.format("  💥 Page faults forzados: %d", 
                    result.getPrefetchResult().getPageFaultsForced()));
                log.info(String.format("  ⏱️ Tiempo total: %dms", 
                    result.getTotalOptimizationTime()));
            } else {
                log.warning("⚠️ Optimización falló: " + result.getPrefetchResult().getMessage());
            }
            
            // Limpiar recursos
            memoryOptimizer.shutdown();
            
        } catch (Exception e) {
            log.severe("❌ Error en optimización básica: " + e.getMessage());
        }
    }
    
    /**
     * 🚀 EJEMPLO 2: OPTIMIZACIÓN AGRESIVA
     * Para aplicaciones que necesitan máximo performance
     */
    public static void example2AggressiveOptimization() {
        log.info("🚀 EJEMPLO 2: Optimización agresiva");
        
        try {
            // Configuración agresiva
            MemoryOptimizationConfig aggressiveConfig = new MemoryOptimizationConfigBuilder()
                .comprehensive() // Análisis agresivo + pre-loading completo
                .build();
            
            MemoryOptimizationSystem optimizer = new MemoryOptimizationSystem(aggressiveConfig);
            
            // Ejecutar con estrategia agresiva
            MemoryOptimizationResult result = optimizer.executeOptimization(OptimizationStrategy.AGGRESSIVE);
            
            log.info("🎯 Resultado optimización agresiva:");
            log.info(String.format("  Estrategia: %s", result.getStrategy()));
            log.info(String.format("  Éxito: %s", result.isSuccess()));
            log.info(String.format("  Páginas pre-cargadas: %d", 
                result.getPrefetchResult().getPagesPreloaded()));
            log.info(String.format("  Tiempo: %dms", result.getTotalOptimizationTime()));
            
            optimizer.shutdown();
            
        } catch (Exception e) {
            log.severe("❌ Error en optimización agresiva: " + e.getMessage());
        }
    }
    
    /**
     * ⚡ EJEMPLO 3: OPTIMIZACIÓN CONSERVADORA
     * Para startup ultra-rápido con mínimo overhead
     */
    public static void example3ConservativeOptimization() {
        log.info("⚡ EJEMPLO 3: Optimización conservadora");
        
        try {
            // Configuración para startup rápido
            MemoryOptimizationConfig fastConfig = new MemoryOptimizationConfigBuilder()
                .fastStartup() // Solo páginas críticas, máximo 5 segundos
                .build();
            
            MemoryOptimizationSystem optimizer = new MemoryOptimizationSystem(fastConfig);
            
            long startTime = System.currentTimeMillis();
            
            // Ejecutar optimización rápida
            MemoryOptimizationResult result = optimizer.executeOptimization(OptimizationStrategy.CONSERVATIVE);
            
            long totalTime = System.currentTimeMillis() - startTime;
            
            log.info("⚡ Resultado optimización conservadora:");
            log.info(String.format("  Estrategia: %s", result.getStrategy()));
            log.info(String.format("  Tiempo total: %dms", totalTime));
            log.info(String.format("  Páginas pre-cargadas: %d", 
                result.getPrefetchResult().getPagesPreloaded()));
            log.info(String.format("  Page faults evitados: %d", 
                result.getPrefetchResult().getPageFaultsForced()));
            
            optimizer.shutdown();
            
        } catch (Exception e) {
            log.severe("❌ Error en optimización conservadora: " + e.getMessage());
        }
    }
    
    /**
     * 🔄 EJEMPLO 4: OPTIMIZACIÓN ASÍNCRONA
     * Para integración con otros sistemas de optimización
     */
    public static void example4AsyncOptimization() {
        log.info("🔄 EJEMPLO 4: Optimización asíncrona");
        
        try {
            MemoryOptimizationSystem optimizer = new MemoryOptimizationSystem();
            
            // Ejecutar optimización en background
            CompletableFuture<MemoryOptimizationResult> future = 
                optimizer.executeOptimizationAsync(OptimizationStrategy.BALANCED);
            
            log.info("🚀 Optimización iniciada en background, esperando resultado...");
            
            // Hacer otras operaciones mientras tanto
            simulateOtherWork();
            
            // Esperar resultado
            MemoryOptimizationResult result = future.get();
            
            log.info("✅ Optimización asíncrona completada:");
            log.info(String.format("  Tiempo total: %dms", result.getTotalOptimizationTime()));
            log.info(String.format("  Éxito: %s", result.isSuccess()));
            
            optimizer.shutdown();
            
        } catch (Exception e) {
            log.severe("❌ Error en optimización asíncrona: " + e.getMessage());
        }
    }
    
    /**
     * 📊 EJEMPLO 5: ANÁLISIS DETALLADO DE MEMORIA
     * Para obtener insights profundos sobre patrones de memoria
     */
    public static void example5DetailedMemoryAnalysis() {
        log.info("📊 EJEMPLO 5: Análisis detallado de memoria");
        
        try {
            // Configuración con análisis detallado
            MemoryOptimizationConfig detailedConfig = new MemoryOptimizationConfigBuilder()
                .aggressiveAnalysis()
                .balanced()
                .build();
            
            MemoryOptimizationSystem optimizer = new MemoryOptimizationSystem(detailedConfig);
            
            // Obtener analizador para análisis manual
            MemoryPageAnalyzer analyzer = new MemoryPageAnalyzer();
            
            // Análisis completo independiente
            MemoryPageAnalyzer.MemoryAnalysisResult analysisResult = analyzer.analyzeMemoryPatterns();
            
            log.info("📊 Resultados del análisis detallado:");
            log.info(String.format("  Páginas analizadas: %d", analysisResult.getTotalPagesAnalyzed()));
            log.info(String.format("  Páginas calientes: %d", analysisResult.getHotPagesCount()));
            log.info(String.format("  Hotspots detectados: %d", analysisResult.getHotspots().size()));
            
            // Mostrar hotspots más críticos
            if (!analysisResult.getHotspots().isEmpty()) {
                log.info("🔥 Top 3 hotspots más críticos:");
                analysisResult.getHotspots().stream()
                    .sorted((h1, h2) -> Long.compare(h2.getAccessCount(), h1.getAccessCount()))
                    .limit(3)
                    .forEach(hotspot -> 
                        log.info(String.format("    0x%X: %d accesos, %dns promedio", 
                            hotspot.getAddress(), hotspot.getAccessCount(), hotspot.getAverageAccessTime()))
                    );
            }
            
            // Reporte detallado
            MemoryPageAnalyzer.MemoryAnalysisReport report = analyzer.generateDetailedReport();
            log.info("📋 Reporte generado: " + report.getTotalPagesAnalyzed() + " páginas procesadas");
            
            optimizer.shutdown();
            
        } catch (Exception e) {
            log.severe("❌ Error en análisis detallado: " + e.getMessage());
        }
    }
    
    /**
     * 🔄 EJEMPLO 6: INTEGRACIÓN CON STARTUP MANAGER
     * Muestra cómo integrar con otros sistemas de optimización
     */
    public static void example6StartupManagerIntegration() {
        log.info("🔄 EJEMPLO 6: Integración con StartupPhasesManager");
        
        try {
            // Simulación de integración con StartupPhasesManager
            log.info("🚀 Simulando integración con otros sistemas de optimización...");
            
            // 1. Parallel Subsystem Initialization (ya existe)
            log.info("  ✅ ParallelSubsystemInitializer: Listo");
            
            // 2. Preloaded Config System (ya existe)  
            log.info("  ✅ PreloadedConfigSystem: Listo");
            
            // 3. Critical Class Preload System (ya existe)
            log.info("  ✅ CriticalClassPreloadSystem: Listo");
            
            // 4. Hot Path Optimization System (ya existe)
            log.info("  ✅ HotPathOptimizationSystem: Listo");
            
            // 5. NUEVO: Memory Optimization System
            MemoryOptimizationSystem memoryOptimizer = new MemoryOptimizationSystem();
            
            // Ejecutar en paralelo con otros sistemas
            CompletableFuture<MemoryOptimizationResult> memoryFuture = 
                memoryOptimizer.executeOptimizationAsync(OptimizationStrategy.BALANCED);
            
            // Simular otros sistemas trabajando
            simulateParallelStartup();
            
            // Esperar resultados de memoria
            MemoryOptimizationResult memoryResult = memoryFuture.get();
            
            log.info("🎯 Integración completada:");
            log.info(String.format("  Memoria: %d páginas en %dms", 
                memoryResult.getPrefetchResult().getPagesPreloaded(),
                memoryResult.getTotalOptimizationTime()));
            
            memoryOptimizer.shutdown();
            
        } catch (Exception e) {
            log.severe("❌ Error en integración: " + e.getMessage());
        }
    }
    
    /**
     * 🔄 EJEMPLO 7: OPTIMIZACIÓN CONTINUA
     * Para aplicaciones de larga duración
     */
    public static void example7ContinuousOptimization() {
        log.info("🔄 EJEMPLO 7: Optimización continua durante runtime");
        
        try {
            MemoryOptimizationSystem optimizer = new MemoryOptimizationSystem();
            
            int optimizationRounds = 3;
            long totalOptimizationTime = 0;
            long totalPagesPreloaded = 0;
            
            for (int round = 1; round <= optimizationRounds; round++) {
                log.info(String.format("🔄 Ronda %d de optimización", round));
                
                long roundStart = System.currentTimeMillis();
                
                // Ejecutar optimización
                MemoryOptimizationResult result = optimizer.executeOptimization(OptimizationStrategy.BALANCED);
                
                long roundTime = System.currentTimeMillis() - roundStart;
                totalOptimizationTime += roundTime;
                totalPagesPreloaded += result.getPrefetchResult().getPagesPreloaded();
                
                log.info(String.format("  ✅ Ronda %d: %d páginas en %dms", 
                    round, result.getPrefetchResult().getPagesPreloaded(), roundTime));
                
                // Simular trabajo entre optimizaciones
                simulateRuntimeWork();
            }
            
            // Resumen final
            log.info("📊 Resumen optimización continua:");
            log.info(String.format("  Total rondas: %d", optimizationRounds));
            log.info(String.format("  Tiempo total: %dms", totalOptimizationTime));
            log.info(String.format("  Páginas totales: %d", totalPagesPreloaded));
            log.info(String.format("  Promedio por ronda: %.2f páginas/ms", 
                (double) totalPagesPreloaded / totalOptimizationTime));
            
            // Métricas finales
            MemoryOptimizationMetrics metrics = optimizer.getMetrics();
            log.info("📈 Métricas del sistema: " + metrics.getOverallMetrics().getTotalOptimizations() + 
                " optimizaciones realizadas");
            
            optimizer.shutdown();
            
        } catch (Exception e) {
            log.severe("❌ Error en optimización continua: " + e.getMessage());
        }
    }
    
    // ===== MÉTODOS AUXILIARES =====
    
    /**
     * 🔄 SIMULAR TRABAJO PARALELO
     * Simula otros sistemas de optimización trabajando
     */
    private static void simulateParallelStartup() {
        log.info("🔄 Simulando trabajo de otros sistemas...");
        try {
            Thread.sleep(100); // Simular trabajo
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
    
    /**
     * 🔄 SIMULAR TRABAJO MIENTRAS ESPERA
     * Simula trabajo que se puede hacer mientras espera la optimización
     */
    private static void simulateOtherWork() {
        log.info("🔄 Haciendo trabajo en paralelo...");
        for (int i = 0; i < 5; i++) {
            try {
                Thread.sleep(50);
                log.info(String.format("  Trabajo %d completado", i + 1));
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }
    
    /**
     * 🔄 SIMULAR TRABAJO DE RUNTIME
     * Simula trabajo de aplicación entre optimizaciones
     */
    private static void simulateRuntimeWork() {
        try {
            // Simular operaciones de aplicación
            Thread.sleep(200);
            
            // Simular operaciones de memoria intensivas
            int[] bigArray = new int[10000];
            for (int i = 0; i < bigArray.length; i++) {
                bigArray[i] = i * 2;
            }
            
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
    
    /**
     * 🚀 MÉTODO PRINCIPAL PARA DEMOSTRACIÓN
     */
    public static void main(String[] args) {
        log.info("🎯 INICIANDO DEMOSTRACIÓN DE OPTIMIZACIÓN DE MEMORIA");
        log.info("====================================================");
        
        try {
            // Ejecutar todos los ejemplos
            example1BasicMemoryOptimization();
            Thread.sleep(500);
            
            example2AggressiveOptimization();
            Thread.sleep(500);
            
            example3ConservativeOptimization();
            Thread.sleep(500);
            
            example4AsyncOptimization();
            Thread.sleep(500);
            
            example5DetailedMemoryAnalysis();
            Thread.sleep(500);
            
            example6StartupManagerIntegration();
            Thread.sleep(500);
            
            example7ContinuousOptimization();
            
            log.info("🎯 DEMOSTRACIÓN COMPLETADA - Todos los ejemplos ejecutados exitosamente");
            
        } catch (Exception e) {
            log.severe("❌ Error durante la demostración: " + e.getMessage());
            e.printStackTrace();
        }
    }
}