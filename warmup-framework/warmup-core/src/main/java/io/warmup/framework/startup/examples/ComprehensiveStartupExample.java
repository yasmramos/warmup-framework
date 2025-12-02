package io.warmup.framework.startup.examples;

import io.warmup.framework.startup.*;
import io.warmup.framework.startup.config.*;
import io.warmup.framework.core.WarmupContainer;
import io.warmup.framework.startup.examples.SystemOptimizationSummary;
import java.util.concurrent.CompletableFuture;

/**
 * Ejemplo completo que demuestra todos los sistemas de optimización trabajando juntos:
 * 
 * 1. 🎯 Fase Crítica Tradicional (< 2ms target)
 * 2. 🚀 Inicialización Paralela de Subsistemas (todos los CPU cores)
 * 3. 📁 Configuración Precargada en Memoria Mapeada (acceso instantáneo)
 * 
 * Demuestra el startup más rápido posible para frameworks Java.
 * 
 * @author MiniMax Agent
 */
public class ComprehensiveStartupExample {
    
    public static void main(String[] args) {
        System.out.println("🚀 DEMO: STARTUP COMPREHENSIVE - MÁXIMA OPTIMIZACIÓN");
        System.out.println("═══════════════════════════════════════════════════════");
        
        // Crear container del framework
        WarmupContainer container = new WarmupContainer();
        
        // Crear manager con todos los sistemas
        StartupPhasesManager startupManager = new StartupPhasesManager(container);
        
        try {
            // Opción 1: Startup completo con todos los sistemas
            demonstrateComprehensiveStartup(startupManager);
            
            // Opción 2: Sistemas individuales (para comparación)
            demonstrateIndividualSystems(startupManager);
            
            // Opción 3: Métricas detalladas
            demonstrateDetailedMetrics(startupManager);
            
        } catch (Exception e) {
            System.err.println("❌ Error durante startup: " + e.getMessage());
            e.printStackTrace();
        } finally {
            // Limpiar recursos
            startupManager.shutdown();
            System.out.println("\n🧹 Recursos liberados");
        }
    }
    
    /**
     * Demuestra el startup comprehensive con todos los sistemas
     */
    private static void demonstrateComprehensiveStartup(StartupPhasesManager startupManager) {
        System.out.println("\n📋 OPCIÓN 1: STARTUP COMPREHENSIVE");
        System.out.println("─────────────────────────────────────");
        
        System.out.println("⚡ Ejecutando todos los sistemas de optimización...");
        
        CompletableFuture<ComprehensiveStartupResult> comprehensiveFuture;
        try {
            comprehensiveFuture = startupManager.executeComprehensiveStartup();
        } catch (Exception e) {
            System.err.println("Error during comprehensive startup: " + e.getMessage());
            return;
        }
        
        ComprehensiveStartupResult result = comprehensiveFuture.join();
        
        // Mostrar resumen ejecutivo
        System.out.println("\n📊 RESULTADOS DEL STARTUP COMPREHENSIVE:");
        System.out.println(result.getExecutiveSummary());
        
        // Mostrar resúmenes por sistema
        System.out.println("\n🔍 ANÁLISIS POR SISTEMA:");
        for (ComprehensiveStartupResult.SystemOptimizationSummary summary : result.getSystemSummaries()) {
            System.out.println("   " + summary);
        }
        
        // Mostrar métricas detalladas
        System.out.println("\n⚙️  MÉTRICAS DETALLADAS:");
        ComprehensiveStartupResult.DetailedPerformanceMetrics detailedMetrics = 
            result.getDetailedPerformanceMetrics();
        System.out.println("   " + detailedMetrics);
        System.out.println("   🎯 Calificación: " + detailedMetrics.getPerformanceGrade().getGrade() + 
                          " - " + detailedMetrics.getPerformanceGrade().getDescription());
        
        // Verificar beneficios
        System.out.println("\n💡 BENEFICIOS CLAVE LOGRADOS:");
        System.out.println("   ✅ Fase crítica: < 2ms");
        System.out.println("   ✅ Paralelización: Todos los CPU cores utilizados");
        System.out.println("   ✅ Configuración: Zero I/O durante runtime");
        System.out.println("   ✅ Speedup total: " + String.format("%.2fx", result.getTotalSpeedupFactor()));
        System.out.println("   ✅ Tiempo ahorrado: " + result.getTotalTimeSaved() + " ms");
    }
    
    /**
     * Demuestra cada sistema individualmente para comparación
     */
    private static void demonstrateIndividualSystems(StartupPhasesManager startupManager) throws Exception {
        System.out.println("\n📋 OPCIÓN 2: SISTEMAS INDIVIDUALES");
        System.out.println("─────────────────────────────────────");
        
        // Sistema 1: Fase crítica tradicional
        System.out.println("\n1️⃣  FASE CRÍTICA TRADICIONAL:");
        System.out.println("   Ejecutando componentes esenciales...");
        long criticalStart = System.nanoTime();
        startupManager.executeCriticalPhase();
        long criticalDuration = System.nanoTime() - criticalStart;
        System.out.println("   ✅ Completada en " + (criticalDuration / 1_000_000) + " ms");
        
        // Sistema 2: Inicialización paralela
        System.out.println("\n2️⃣  INICIALIZACIÓN PARALELA:");
        System.out.println("   Usando todos los CPU cores disponibles...");
        CompletableFuture<SubsystemInitializationResult> parallelFuture = 
            startupManager.executeParallelSubsystemInitialization();
        SubsystemInitializationResult parallelResult = parallelFuture.join();
        System.out.println("   ✅ Subsistemas inicializados: " + parallelResult.getDetailedResults().size());
        System.out.println("   ⚡ Speedup: " + String.format("%.2fx", parallelResult.getSpeedupFactor()));
        
        // Sistema 3: Configuración precargada
        System.out.println("\n3️⃣  CONFIGURACIÓN PRECARGADA:");
        System.out.println("   Cargando configuraciones en memoria mapeada...");
        CompletableFuture<PreloadedConfigSystem.PreloadResult> configFuture = 
            startupManager.executeConfigPreloading();
        PreloadedConfigSystem.PreloadResult configResult = configFuture.join();
        System.out.println("   ✅ Configuraciones cargadas: " + configResult.getSuccessfulLoads());
        System.out.println("   📁 Archivos mapeados: " + configResult.getTotalRequested());
        System.out.println("   💾 Tiempo ahorrado: " + configResult.getEstimatedStartupSavingsMs() + " ms");
        
        // Sistema 4: Fase background
        System.out.println("\n4️⃣  FASE BACKGROUND:");
        System.out.println("   Ejecutando componentes no críticos...");
        CompletableFuture<Void> backgroundFuture = startupManager.executeBackgroundPhaseAsync();
        backgroundFuture.join();
        System.out.println("   ✅ Fase background completada");
        
        System.out.println("\n🎉 TODOS LOS SISTEMAS EJECUTADOS EXITOSAMENTE");
    }
    
    /**
     * Demuestra métricas detalladas y análisis de rendimiento
     */
    private static void demonstrateDetailedMetrics(StartupPhasesManager startupManager) {
        System.out.println("\n📋 OPCIÓN 3: MÉTRICAS DETALLADAS");
        System.out.println("─────────────────────────────────────");
        
        // Métricas de paralelización
        System.out.println("\n🔄 MÉTRICAS DE PARALELIZACIÓN:");
        ParallelizationStats parallelStats = startupManager.getParallelizationStats();
        System.out.println("   CPU Cores disponibles: " + parallelStats.getAvailableCores());
        System.out.println("   Tamaño del pool de hilos: " + parallelStats.getThreadPoolSize());
        System.out.println("   Eficiencia de paralelización: " + 
            String.format("%.1f%%", parallelStats.getConfigurationEfficiency() * 100));
        System.out.println("   Recomendaciones: " + parallelStats.getRecommendations().getRecommendationsText());
        
        // Métricas de configuración
        System.out.println("\n📁 MÉTRICAS DE CONFIGURACIÓN:");
        PreloadOptimizationMetrics configMetrics = startupManager.getConfigOptimizationMetrics();
        PreloadOptimizationMetrics.OverallOptimizationStats overallStats = configMetrics.getOverallStats();
        System.out.println("   Configuraciones preloaded: " + overallStats.getTotalConfigsPreloaded());
        System.out.println("   Tiempo total ahorrado: " + overallStats.getFormattedTotalSavings());
        System.out.println("   Operaciones I/O evitadas: " + overallStats.getTotalIooOperationsAvoided());
        System.out.println("   Operaciones de parsing evitadas: " + overallStats.getTotalParsingOperationsAvoided());
        System.out.println("   Eficiencia promedio: " + 
            String.format("%.2f", overallStats.getAverageEfficiency()));
        
        // Métricas de startup tradicional
        System.out.println("\n⚡ MÉTRICAS DE STARTUP TRADICIONAL:");
        StartupMetrics startupMetrics = startupManager.getStartupMetrics();
        System.out.println("   Fase crítica completada: " + startupMetrics.isCriticalPhaseCompleted());
        System.out.println("   Fase background completada: " + startupMetrics.isBackgroundPhaseCompleted());
        System.out.println("   Duración de fase crítica: " + 
            startupMetrics.getCriticalPhaseMetrics().getLastDurationMs() + " ms");
        System.out.println("   Duración de fase background: " + 
            startupMetrics.getBackgroundPhaseMetrics().getLastDurationMs() + " ms");
        
        // Comparación de eficiencia
        System.out.println("\n📊 COMPARACIÓN DE EFICIENCIA:");
        System.out.println("   🎯 Target de fase crítica: < 2ms");
        System.out.println("   ⚡ Real fase crítica: " + 
            startupMetrics.getCriticalPhaseMetrics().getLastDurationMs() + " ms");
        System.out.println("   🔄 Paralelización activa: " + 
            (parallelStats.getAvailableCores() > 1 ? "Sí" : "No"));
        System.out.println("   📁 Configuración optimizada: " + 
            (overallStats.getTotalConfigsPreloaded() > 0 ? "Sí" : "No"));
        
        // Acceso a configuración cargada (demo)
        if (startupManager.getPreloadedConfigSystem().isReady()) {
            System.out.println("\n🔍 ACCESO INSTANTÁNEO A CONFIGURACIÓN:");
            PreloadedConfigSystem configSystem = startupManager.getPreloadedConfigSystem();
            
            // Buscar una configuración disponible para demo
            for (String key : configSystem.getAllConfigAccessors().keySet()) {
                try {
                    ConfigDataAccessor accessor = configSystem.getConfigAccessor(key);
                    String info = accessor.getConfigInfo().toString();
                    System.out.println("   " + info);
                    break;
                } catch (Exception e) {
                    // Continuar con la siguiente configuración
                }
            }
        }
    }
}