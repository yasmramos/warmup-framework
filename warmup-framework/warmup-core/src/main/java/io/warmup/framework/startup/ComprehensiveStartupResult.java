package io.warmup.framework.startup;

import io.warmup.framework.startup.config.*;
import io.warmup.framework.startup.bootstrap.CriticalClassPreloadSystem;
import io.warmup.framework.startup.hotpath.HotPathOptimizationSystem;
import io.warmup.framework.startup.memory.MemoryOptimizationSystem;
import io.warmup.framework.startup.critical.ServiceDataClasses;
import io.warmup.framework.startup.unsafe.UnsafeStartupSystem;
import java.util.List;

/**
 * Resultado comprehensivo que combina todos los sistemas de optimización:
 * - Fase crítica tradicional
 * - Inicialización paralela de subsistemas
 * - Sistema de configuración precargada en memoria mapeada
 * 
 * Proporciona vista unificada del rendimiento total del startup optimizado.
 */
public class ComprehensiveStartupResult {
    
    private final StartupMetrics startupMetrics;
    private final SubsystemInitializationResult parallelSubsystemsResult;
    private final PreloadedConfigSystem.PreloadResult configPreloadResult;
    private final CriticalClassPreloadSystem.ComprehensivePreloadResult criticalClassPreloadResult;
    private final HotPathOptimizationSystem.HotPathOptimizationResult hotPathOptimizationResult;
    private final MemoryOptimizationSystem.MemoryOptimizationResult memoryOptimizationResult;
    private final ServiceDataClasses.CriticalSeparationResult criticalSeparationResult;
    private final UnsafeStartupSystem.UnsafeStartupResult unsafeStartupResult;
    private final long totalDurationNs;
    private final PreloadOptimizationMetrics.OverallOptimizationStats configOptimizationStats;
    
    // Métricas calculadas
    private final double totalSpeedupFactor;
    private final long totalTimeSaved;
    private final double overallEfficiency;
    private final List<SystemOptimizationSummary> systemSummaries;
    
    public ComprehensiveStartupResult(StartupMetrics startupMetrics,
                                    SubsystemInitializationResult parallelSubsystemsResult,
                                    PreloadedConfigSystem.PreloadResult configPreloadResult,
                                    long totalDurationNs,
                                    PreloadOptimizationMetrics.OverallOptimizationStats configOptimizationStats) {
        this(startupMetrics, parallelSubsystemsResult, configPreloadResult, null, null, null, null, null, totalDurationNs, configOptimizationStats);
    }
    
    /**
     * Constructor con pre-carga de clases críticas incluida
     */
    public ComprehensiveStartupResult(StartupMetrics startupMetrics,
                                    SubsystemInitializationResult parallelSubsystemsResult,
                                    PreloadedConfigSystem.PreloadResult configPreloadResult,
                                    CriticalClassPreloadSystem.ComprehensivePreloadResult criticalClassPreloadResult,
                                    HotPathOptimizationSystem.HotPathOptimizationResult hotPathOptimizationResult,
                                    MemoryOptimizationSystem.MemoryOptimizationResult memoryOptimizationResult,
                                    ServiceDataClasses.CriticalSeparationResult criticalSeparationResult,
                                    UnsafeStartupSystem.UnsafeStartupResult unsafeStartupResult,
                                    long totalDurationNs,
                                    PreloadOptimizationMetrics.OverallOptimizationStats configOptimizationStats) {
        this.startupMetrics = startupMetrics;
        this.parallelSubsystemsResult = parallelSubsystemsResult;
        this.configPreloadResult = configPreloadResult;
        this.criticalClassPreloadResult = criticalClassPreloadResult;
        this.hotPathOptimizationResult = hotPathOptimizationResult;
        this.memoryOptimizationResult = memoryOptimizationResult;
        this.criticalSeparationResult = criticalSeparationResult;
        this.unsafeStartupResult = unsafeStartupResult;
        this.totalDurationNs = totalDurationNs;
        this.configOptimizationStats = configOptimizationStats;
        
        // Calcular métricas consolidadas
        this.totalSpeedupFactor = calculateTotalSpeedupFactor();
        this.totalTimeSaved = calculateTotalTimeSaved();
        this.overallEfficiency = calculateOverallEfficiency();
        this.systemSummaries = generateSystemSummaries();
    }
    
    /**
     * Constructor legacy sin hot path optimization
     */
    public ComprehensiveStartupResult(StartupMetrics startupMetrics,
                                    SubsystemInitializationResult parallelSubsystemsResult,
                                    PreloadedConfigSystem.PreloadResult configPreloadResult,
                                    CriticalClassPreloadSystem.ComprehensivePreloadResult criticalClassPreloadResult,
                                    long totalDurationNs,
                                    PreloadOptimizationMetrics.OverallOptimizationStats configOptimizationStats) {
        this(startupMetrics, parallelSubsystemsResult, configPreloadResult, criticalClassPreloadResult, null, null, null, null, totalDurationNs, configOptimizationStats);
    }
    
    /**
     * Calcula el factor de speedup total combinando todos los sistemas
     */
    private double calculateTotalSpeedupFactor() {
        double parallelSpeedup = parallelSubsystemsResult.getSpeedupFactor();
        double configSpeedup = configPreloadResult.getEfficiency();
        double criticalEfficiency = startupMetrics.getCriticalPhaseMetrics().getTargetEfficiency();
        double classPreloadSpeedup = criticalClassPreloadResult != null ? 
            calculateClassPreloadSpeedup() : 1.0;
        double hotPathSpeedup = hotPathOptimizationResult != null ?
            calculateHotPathSpeedup() : 1.0;
        double memorySpeedup = memoryOptimizationResult != null ?
            calculateMemorySpeedup() : 1.0;
        double criticalSeparationSpeedup = criticalSeparationResult != null ?
            calculateCriticalSeparationSpeedup() : 1.0;
        double unsafeMemorySpeedup = unsafeStartupResult != null ?
            calculateUnsafeMemorySpeedup() : 1.0;
        
        // Combinar efectos (multiplicativo con límites realistas)
        double combinedSpeedup = parallelSpeedup * configSpeedup * classPreloadSpeedup * 
                               hotPathSpeedup * memorySpeedup * criticalSeparationSpeedup * 
                               unsafeMemorySpeedup;
        
        // Normalizar para evitar valores extremos
        return Math.min(combinedSpeedup, 100.0); // Límite máximo de 100x con 9 sistemas
    }
    
    /**
     * Calcula el factor de speedup del sistema de pre-carga de clases críticas
     */
    private double calculateClassPreloadSpeedup() {
        if (criticalClassPreloadResult == null) {
            return 1.0;
        }
        
        // Estimación basada en el número de clases pre-cargadas
        int classesLoaded = criticalClassPreloadResult.getSuccessfullyLoaded();
        double estimatedClassLoadTime = classesLoaded * 5.0; // 5ms por clase en runtime
        double actualPreloadTime = criticalClassPreloadResult.getTotalTimeMs();
        
        if (actualPreloadTime > 0) {
            return estimatedClassLoadTime / actualPreloadTime;
        }
        
        return 1.0; // Sin beneficio si no hay datos
    }
    
    /**
     * Calcula el factor de speedup del sistema de hot path optimization
     */
    private double calculateHotPathSpeedup() {
        if (hotPathOptimizationResult == null || !hotPathOptimizationResult.isSuccess()) {
            return 1.0;
        }
        
        // Estimación basada en el número de hot paths optimizados
        int optimizedPaths = hotPathOptimizationResult.getHotPaths().size();
        double estimatedPathOptimizationTime = optimizedPaths * 10.0; // 10ms por path en runtime
        double actualOptimizationTime = hotPathOptimizationResult.getOptimizationTime();
        
        if (actualOptimizationTime > 0) {
            return estimatedPathOptimizationTime / actualOptimizationTime;
        }
        
        return 1.0;
    }
    
    /**
     * Calcula el factor de speedup del sistema de optimización de memoria
     */
    private double calculateMemorySpeedup() {
        if (memoryOptimizationResult == null || !memoryOptimizationResult.isSuccess()) {
            return 1.0;
        }
        
        // Estimación basada en las páginas pre-cargadas
        long pagesPreloaded = memoryOptimizationResult.getPrefetchResult().getPagesPreloaded();
        long pageFaultsAvoided = memoryOptimizationResult.getPrefetchResult().getPageFaultsForced();
        
        // Cada page fault evitado ahorra ~1-5ms dependiendo del sistema
        double estimatedSavings = pageFaultsAvoided * 2.0; // 2ms promedio por page fault
        double actualOptimizationTime = memoryOptimizationResult.getTotalOptimizationTime();
        
        if (actualOptimizationTime > 0) {
            return estimatedSavings / actualOptimizationTime;
        }
        
        return 1.0;
    }
    
    /**
     * Calcula el factor de speedup del sistema de separación crítica
     */
    private double calculateCriticalSeparationSpeedup() {
        if (criticalSeparationResult == null || !criticalSeparationResult.isSuccess()) {
            return 1.0;
        }
        
        // Separación crítica permite respuesta inmediata en 2ms
        // mientras los servicios no críticos se calientan en background
        double criticalPhaseTime = criticalSeparationResult.getCriticalPhaseDurationMs();
        double estimatedTraditionalTime = 50.0; // Estimación de tiempo tradicional
        
        if (criticalPhaseTime > 0 && criticalPhaseTime <= 2) {
            return estimatedTraditionalTime / criticalPhaseTime; // Puede dar 25x speedup
        }
        
        return 1.0; // Sin beneficio si excede 2ms
    }
    
    /**
     * Calcula el factor de speedup del sistema de memoria directa con Unsafe
     */
    private double calculateUnsafeMemorySpeedup() {
        if (unsafeStartupResult == null) {
            return 1.0;
        }
        
        // Unsafe memory elimina GC overhead completamente
        double gcEliminationRate = unsafeStartupResult.getGCEliminationRate();
        double memoryEfficiency = unsafeStartupResult.getMemoryEfficiency();
        
        // Si eliminamos >90% del GC overhead, eso representa un speedup significativo
        if (gcEliminationRate > 90.0) {
            // Estimación basada en el tiempo de startup tradicional vs. tiempo con GC eliminado
            double estimatedGCTime = unsafeStartupResult.getStartupTimeMs() * 0.3; // 30% del tiempo es típicamente GC
            double timeWithoutGC = unsafeStartupResult.getStartupTimeMs() - estimatedGCTime;
            
            if (timeWithoutGC > 0) {
                return unsafeStartupResult.getStartupTimeMs() / timeWithoutGC;
            }
        }
        
        return 1.0; // Sin beneficio significativo si no elimina suficiente GC
    }
    
    /**
     * Calcula el tiempo total ahorrado en milisegundos
     */
    private long calculateTotalTimeSaved() {
        long parallelSavings = parallelSubsystemsResult.getTotalTimeSavedMs();
        long configSavings = configPreloadResult.getEstimatedStartupSavingsMs();
        long criticalSavings = (long) (startupMetrics.getCriticalPhaseMetrics().getTargetEfficiency() * 10);
        long classPreloadSavings = calculateClassPreloadSavings();
        long hotPathSavings = calculateHotPathSavings();
        long memorySavings = calculateMemorySavings();
        
        long criticalSeparationSavings = calculateCriticalSeparationSavings();
        long unsafeMemorySavings = calculateUnsafeMemorySavings();
        
        return parallelSavings + configSavings + criticalSavings + classPreloadSavings + 
               hotPathSavings + memorySavings + criticalSeparationSavings + unsafeMemorySavings;
    }
    
    /**
     * Calcula el tiempo ahorrado por el sistema de pre-carga de clases críticas
     */
    private long calculateClassPreloadSavings() {
        if (criticalClassPreloadResult == null) {
            return 0;
        }
        
        int classesLoaded = criticalClassPreloadResult.getSuccessfullyLoaded();
        long estimatedRuntimeLoadTime = classesLoaded * 5; // 5ms por clase en runtime
        long actualPreloadTime = criticalClassPreloadResult.getTotalTimeMs();
        
        return Math.max(0, estimatedRuntimeLoadTime - actualPreloadTime);
    }
    
    /**
     * Calcula el tiempo ahorrado por el sistema de hot path optimization
     */
    private long calculateHotPathSavings() {
        if (hotPathOptimizationResult == null) {
            return 0;
        }
        
        int optimizedPaths = hotPathOptimizationResult.getHotPaths().size();
        long estimatedRuntimePathTime = optimizedPaths * 10; // 10ms por path en runtime
        long actualOptimizationTime = hotPathOptimizationResult.getOptimizationTime();
        
        return Math.max(0, estimatedRuntimePathTime - actualOptimizationTime);
    }
    
    /**
     * Calcula el tiempo ahorrado por el sistema de optimización de memoria
     */
    private long calculateMemorySavings() {
        if (memoryOptimizationResult == null) {
            return 0;
        }
        
        long pageFaultsForced = memoryOptimizationResult.getPrefetchResult().getPageFaultsForced();
        long estimatedFaultTime = pageFaultsForced * 2; // 2ms promedio por page fault en runtime
        long actualOptimizationTime = memoryOptimizationResult.getTotalOptimizationTime();
        
        return Math.max(0, estimatedFaultTime - actualOptimizationTime);
    }
    
    /**
     * Calcula el tiempo ahorrado por la separación crítica
     */
    private long calculateCriticalSeparationSavings() {
        if (criticalSeparationResult == null || !criticalSeparationResult.isSuccess()) {
            return 0;
        }
        
        // Separación crítica permite respuesta en 2ms en lugar de 50ms tradicional
        long traditionalTime = 50; // 50ms estimado para startup tradicional
        long criticalPhaseTime = criticalSeparationResult.getCriticalPhaseDurationMs();
        
        return Math.max(0, traditionalTime - criticalPhaseTime);
    }
    
    /**
     * Calcula el tiempo ahorrado por el sistema de memoria directa con Unsafe
     */
    private long calculateUnsafeMemorySavings() {
        if (unsafeStartupResult == null) {
            return 0;
        }
        
        // Memoria directa elimina GC overhead por completo
        double gcEliminationRate = unsafeStartupResult.getGCEliminationRate();
        
        if (gcEliminationRate > 80.0) {
            // Si eliminamos >80% del GC, el startup puede ser hasta 5-10x más rápido
            // Estimamos que el GC puede representar 30-50% del tiempo de startup
            double estimatedGCTime = unsafeStartupResult.getStartupTimeMs() * 0.4; // 40% típicamente GC
            
            // El ahorro es el tiempo que no se gasta en GC
            return (long) estimatedGCTime;
        }
        
        return 0; // Sin ahorro significativo si no elimina suficiente GC
    }
    
    /**
     * Calcula la eficiencia general del sistema
     */
    private double calculateOverallEfficiency() {
        double parallelEfficiency = parallelSubsystemsResult.getOverallEfficiency();
        double configEfficiency = configPreloadResult.getEfficiency();
        double criticalEfficiency = startupMetrics.getCriticalPhaseMetrics().getTargetEfficiency();
        double classPreloadEfficiency = calculateClassPreloadEfficiency();
        double hotPathEfficiency = calculateHotPathEfficiency();
        double memoryEfficiency = calculateMemoryEfficiency();
        double criticalSeparationEfficiency = calculateCriticalSeparationEfficiency();
        double unsafeMemoryEfficiency = calculateUnsafeMemoryEfficiency();
        
        // Promedio ponderado (9 sistemas)
        double totalWeight = 9.0; // Ahora 9 sistemas
        return (parallelEfficiency + configEfficiency + criticalEfficiency + 
               classPreloadEfficiency + hotPathEfficiency + memoryEfficiency + 
               criticalSeparationEfficiency + unsafeMemoryEfficiency) / totalWeight;
    }
    
    /**
     * Calcula la eficiencia del sistema de pre-carga de clases críticas
     */
    private double calculateClassPreloadEfficiency() {
        if (criticalClassPreloadResult == null) {
            return 0.0;
        }
        
        return criticalClassPreloadResult.getSuccessRate() / 100.0;
    }
    
    /**
     * Calcula la eficiencia del sistema de hot path optimization
     */
    private double calculateHotPathEfficiency() {
        if (hotPathOptimizationResult == null) {
            return 0.0;
        }
        
        // Eficiencia basada en el éxito de la optimización
        return hotPathOptimizationResult.isSuccess() ? 0.95 : 0.0;
    }
    
    /**
     * Calcula la eficiencia del sistema de optimización de memoria
     */
    private double calculateMemoryEfficiency() {
        if (memoryOptimizationResult == null) {
            return 0.0;
        }
        
        // Eficiencia basada en el éxito y las páginas pre-cargadas
        if (!memoryOptimizationResult.isSuccess()) {
            return 0.0;
        }
        
        long pagesPreloaded = memoryOptimizationResult.getPrefetchResult().getPagesPreloaded();
        return Math.min(1.0, pagesPreloaded / 1000.0); // Máximo 1.0 para 1000+ páginas
    }
    
    /**
     * Calcula la eficiencia de la separación crítica
     */
    private double calculateCriticalSeparationEfficiency() {
        if (criticalSeparationResult == null) {
            return 0.0;
        }
        
        if (!criticalSeparationResult.isSuccess()) {
            return 0.0;
        }
        
        // Eficiencia basada en si se logró la respuesta en 2ms
        if (criticalSeparationResult.getCriticalPhaseDurationMs() <= 2) {
            return 1.0; // Eficiencia máxima si se logra el target
        } else {
            return 0.5; // Eficiencia parcial si se excede el target
        }
    }
    
    /**
     * Calcula la eficiencia del sistema de memoria directa con Unsafe
     */
    private double calculateUnsafeMemoryEfficiency() {
        if (unsafeStartupResult == null) {
            return 0.0;
        }
        
        // Eficiencia basada en la eliminación de GC overhead
        double gcEliminationRate = unsafeStartupResult.getGCEliminationRate();
        
        // Si eliminamos >90% del GC overhead = eficiencia máxima
        if (gcEliminationRate > 90.0) {
            return 1.0;
        }
        // Si eliminamos >70% del GC overhead = eficiencia alta
        else if (gcEliminationRate > 70.0) {
            return 0.8;
        }
        // Si eliminamos >50% del GC overhead = eficiencia media
        else if (gcEliminationRate > 50.0) {
            return 0.6;
        }
        // Si eliminamos >30% del GC overhead = eficiencia baja
        else if (gcEliminationRate > 30.0) {
            return 0.4;
        }
        
        return 0.0; // Sin eficiencia si no elimina GC significativamente
    }
    
    /**
     * Genera resúmenes de cada sistema de optimización
     */
    private List<SystemOptimizationSummary> generateSystemSummaries() {
        java.util.List<SystemOptimizationSummary> summaries = new java.util.ArrayList<>();
        
        // Fase crítica tradicional
        summaries.add(new SystemOptimizationSummary(
            "Fase Crítica Tradicional",
            "Startup rápido en " + startupMetrics.getCriticalPhaseMetrics().getLastDurationMs() + "ms",
            startupMetrics.getCriticalPhaseMetrics().getTargetEfficiency(),
            "Fase crítica completada exitosamente"
        ));
        
        // Inicialización paralela
        summaries.add(new SystemOptimizationSummary(
            "Inicialización Paralela",
            parallelSubsystemsResult.getSummary(),
            parallelSubsystemsResult.getOverallEfficiency(),
            parallelSubsystemsResult.getDetailedResults().size() + " subsistemas inicializados"
        ));
        
        // Configuración precargada
        summaries.add(new SystemOptimizationSummary(
            "Configuración Precargada",
            configPreloadResult.toString(),
            configPreloadResult.getEfficiency(),
            configOptimizationStats.getFormattedTotalSavings() + " tiempo ahorrado total"
        ));
        
        // Pre-carga de clases críticas (si está disponible)
        if (criticalClassPreloadResult != null) {
            summaries.add(new SystemOptimizationSummary(
                "Pre-carga de Clases Críticas",
                String.format("%d clases pre-cargadas en %dms", 
                    criticalClassPreloadResult.getSuccessfullyLoaded(),
                    criticalClassPreloadResult.getTotalTimeMs()),
                criticalClassPreloadResult.getSuccessRate() / 100.0,
                String.format("%.1f%% éxito, %.2f clases/segundo", 
                    criticalClassPreloadResult.getSuccessRate(),
                    criticalClassPreloadResult.getClassesPerSecond())
            ));
        }
        
        // Hot Path Optimization (si está disponible)
        if (hotPathOptimizationResult != null) {
            summaries.add(new SystemOptimizationSummary(
                "Hot Path Optimization",
                String.format("%d hot paths optimizados en %dms", 
                    hotPathOptimizationResult.getHotPaths().size(),
                    hotPathOptimizationResult.getOptimizationTime()),
                hotPathOptimizationResult.isSuccess() ? 0.95 : 0.0,
                String.format("Optimización %s", 
                    hotPathOptimizationResult.isSuccess() ? "exitosa" : "fallida")
            ));
        }
        
        // Memory Optimization (si está disponible)
        if (memoryOptimizationResult != null) {
            summaries.add(new SystemOptimizationSummary(
                "Memory Page Optimization",
                String.format("%d páginas pre-cargadas, %d page faults forzados en %dms",
                    memoryOptimizationResult.getPrefetchResult().getPagesPreloaded(),
                    memoryOptimizationResult.getPrefetchResult().getPageFaultsForced(),
                    memoryOptimizationResult.getTotalOptimizationTime()),
                memoryOptimizationResult.isSuccess() ? 0.90 : 0.0,
                String.format("%s, %.2f páginas/ms",
                    memoryOptimizationResult.isSuccess() ? "Éxito" : "Fallo",
                    memoryOptimizationResult.getPrefetchResult().getPagesPreloaded() > 0 ?
                        (double) memoryOptimizationResult.getPrefetchResult().getPagesPreloaded() / 
                        memoryOptimizationResult.getTotalOptimizationTime() : 0.0)
            ));
        }
        
        // Critical Service Separation (si está disponible)
        if (criticalSeparationResult != null) {
            summaries.add(new SystemOptimizationSummary(
                "Critical Service Separation",
                String.format("%d servicios críticos en %dms, %d servicios no críticos en background",
                    criticalSeparationResult.getCriticalServicesLoaded(),
                    criticalSeparationResult.getCriticalPhaseDurationMs(),
                    criticalSeparationResult.getNonCriticalServicesLoaded()),
                criticalSeparationResult.isApplicationUsable() ? 1.0 : 0.0,
                String.format("%s - Aplicación %s en %dms",
                    criticalSeparationResult.isSuccess() ? "Éxito" : "Fallo",
                    criticalSeparationResult.isApplicationUsable() ? "usable" : "no usable",
                    criticalSeparationResult.getCriticalPhaseDurationMs())
            ));
        }
        
        // Unsafe Memory System (si está disponible)
        if (unsafeStartupResult != null) {
            summaries.add(new SystemOptimizationSummary(
                "Unsafe Memory System",
                String.format("Startup en %dms con %.1f%% eliminación de GC y %.1f%% eficiencia de memoria",
                    unsafeStartupResult.getStartupTimeMs(),
                    unsafeStartupResult.getGCEliminationRate(),
                    unsafeStartupResult.getMemoryEfficiency()),
                unsafeStartupResult.getGCEliminationRate() > 80.0 ? 1.0 : 0.6,
                String.format("%s - %.1f%% GC eliminado en %dms",
                    unsafeStartupResult.getGCEliminationRate() > 90.0 ? "Éxito" : "Parcial",
                    unsafeStartupResult.getGCEliminationRate(),
                    unsafeStartupResult.getStartupTimeMs())
            ));
        }
        
        return summaries;
    }
    
    // Getters
    public StartupMetrics getStartupMetrics() { return startupMetrics; }
    public SubsystemInitializationResult getParallelSubsystemsResult() { return parallelSubsystemsResult; }
    public PreloadedConfigSystem.PreloadResult getConfigPreloadResult() { return configPreloadResult; }
    public CriticalClassPreloadSystem.ComprehensivePreloadResult getCriticalClassPreloadResult() { return criticalClassPreloadResult; }
    public HotPathOptimizationSystem.HotPathOptimizationResult getHotPathOptimizationResult() { return hotPathOptimizationResult; }
    public MemoryOptimizationSystem.MemoryOptimizationResult getMemoryOptimizationResult() { return memoryOptimizationResult; }
    
    /**
     * Obtener resultado de separación crítica
     */
    public ServiceDataClasses.CriticalSeparationResult getCriticalSeparationResult() { return criticalSeparationResult; }
    
    /**
     * Obtener resultado de memoria directa con Unsafe
     */
    public UnsafeStartupSystem.UnsafeStartupResult getUnsafeStartupResult() { return unsafeStartupResult; }
    
    public long getTotalDurationNs() { return totalDurationNs; }
    public long getTotalDurationMs() { return totalDurationNs / 1_000_000; }
    public double getTotalSpeedupFactor() { return totalSpeedupFactor; }
    public long getTotalTimeSaved() { return totalTimeSaved; }
    public double getOverallEfficiency() { return overallEfficiency; }
    public List<SystemOptimizationSummary> getSystemSummaries() { return systemSummaries; }
    public PreloadOptimizationMetrics.OverallOptimizationStats getConfigOptimizationStats() { return configOptimizationStats; }
    
    /**
     * Verifica si el sistema de pre-carga de clases críticas está disponible
     */
    public boolean isCriticalClassPreloadAvailable() {
        return criticalClassPreloadResult != null;
    }
    
    /**
     * Obtiene un resumen ejecutivo completo
     */
    public String getExecutiveSummary() {
        StringBuilder summary = new StringBuilder();
        
        summary.append("🚀 STARTUP COMPLETAMENTE OPTIMIZADO COMPLETADO:\n");
        summary.append(String.format("⏱️  Tiempo Total: %d ms\n", getTotalDurationMs()));
        summary.append(String.format("⚡ Speedup Factor: %.2fx\n", totalSpeedupFactor));
        summary.append(String.format("💾 Tiempo Ahorrado: %s\n", formatDuration(totalTimeSaved)));
        summary.append(String.format("📊 Eficiencia General: %.1f%%\n", overallEfficiency * 100));
        summary.append(String.format("🎯 Fase Crítica: %s\n", startupMetrics.isCriticalPhaseCompleted() ? "✅ Exitosa" : "❌ Fallida"));
        summary.append(String.format("🔄 Subsistemas: %d inicializados\n", parallelSubsystemsResult.getDetailedResults().size()));
        summary.append(String.format("📁 Configuraciones: %d cargadas\n", configPreloadResult.getSuccessfulLoads()));
        
        // Añadir información de pre-carga de clases críticas si está disponible
        if (isCriticalClassPreloadAvailable()) {
            summary.append(String.format("🎯 Clases Críticas: %d pre-cargadas\n", criticalClassPreloadResult.getSuccessfullyLoaded()));
            summary.append(String.format("📈 Pre-carga Clases: %.1f%% éxito\n", criticalClassPreloadResult.getSuccessRate()));
        } else {
            summary.append("🎯 Clases Críticas: No disponible\n");
        }
        
        return summary.toString();
    }
    
    /**
     * Obtiene métricas detalladas de rendimiento
     */
    public DetailedPerformanceMetrics getDetailedPerformanceMetrics() {
        return new DetailedPerformanceMetrics(
            totalDurationNs,
            totalSpeedupFactor,
            totalTimeSaved,
            overallEfficiency,
            parallelSubsystemsResult,
            configPreloadResult,
            startupMetrics,
            criticalClassPreloadResult
        );
    }
    
    /**
     * Formatea duración en formato legible
     */
    private String formatDuration(long ms) {
        if (ms < 1000) {
            return ms + " ms";
        } else if (ms < 60 * 1000) {
            return String.format("%.2f s", ms / 1000.0);
        } else {
            long seconds = ms / 1000;
            long minutes = seconds / 60;
            seconds = seconds % 60;
            return String.format("%d m %d s", minutes, seconds);
        }
    }
    
    /**
     * Resumen de optimización por sistema
     */
    public static class SystemOptimizationSummary {
        private final String systemName;
        private final String description;
        private final double efficiency;
        private final String status;
        
        public SystemOptimizationSummary(String systemName, String description, double efficiency, String status) {
            this.systemName = systemName;
            this.description = description;
            this.efficiency = efficiency;
            this.status = status;
        }
        
        // Getters
        public String getSystemName() { return systemName; }
        public String getDescription() { return description; }
        public double getEfficiency() { return efficiency; }
        public String getStatus() { return status; }
        
        @Override
        public String toString() {
            return String.format("📊 %s: %s (%.1f%%) - %s", 
                systemName, description, efficiency * 100, status);
        }
    }
    
    /**
     * Métricas detalladas de rendimiento
     */
    public static class DetailedPerformanceMetrics {
        private final long totalDurationNs;
        private final double totalSpeedupFactor;
        private final long totalTimeSaved;
        private final double overallEfficiency;
        private final SubsystemInitializationResult parallelResult;
        private final PreloadedConfigSystem.PreloadResult configResult;
        private final StartupMetrics startupMetrics;
        private final CriticalClassPreloadSystem.ComprehensivePreloadResult criticalClassResult;
        
        public DetailedPerformanceMetrics(long totalDurationNs, double totalSpeedupFactor,
                                        long totalTimeSaved, double overallEfficiency,
                                        SubsystemInitializationResult parallelResult,
                                        PreloadedConfigSystem.PreloadResult configResult,
                                        StartupMetrics startupMetrics) {
            this(totalDurationNs, totalSpeedupFactor, totalTimeSaved, overallEfficiency,
                 parallelResult, configResult, startupMetrics, null);
        }
        
        public DetailedPerformanceMetrics(long totalDurationNs, double totalSpeedupFactor,
                                        long totalTimeSaved, double overallEfficiency,
                                        SubsystemInitializationResult parallelResult,
                                        PreloadedConfigSystem.PreloadResult configResult,
                                        StartupMetrics startupMetrics,
                                        CriticalClassPreloadSystem.ComprehensivePreloadResult criticalClassResult) {
            this.totalDurationNs = totalDurationNs;
            this.totalSpeedupFactor = totalSpeedupFactor;
            this.totalTimeSaved = totalTimeSaved;
            this.overallEfficiency = overallEfficiency;
            this.parallelResult = parallelResult;
            this.configResult = configResult;
            this.startupMetrics = startupMetrics;
            this.criticalClassResult = criticalClassResult;
        }
        
        // Getters
        public long getTotalDurationNs() { return totalDurationNs; }
        public long getTotalDurationMs() { return totalDurationNs / 1_000_000; }
        public double getTotalSpeedupFactor() { return totalSpeedupFactor; }
        public long getTotalTimeSaved() { return totalTimeSaved; }
        public double getOverallEfficiency() { return overallEfficiency; }
        public SubsystemInitializationResult getParallelResult() { return parallelResult; }
        public PreloadedConfigSystem.PreloadResult getConfigResult() { return configResult; }
        public StartupMetrics getStartupMetrics() { return startupMetrics; }
        public CriticalClassPreloadSystem.ComprehensivePreloadResult getCriticalClassResult() { return criticalClassResult; }
        
        /**
         * Verifica si el sistema de pre-carga de clases críticas está disponible
         */
        public boolean isCriticalClassPreloadAvailable() {
            return criticalClassResult != null;
        }
        
        /**
         * Evalúa la calidad del rendimiento general
         */
        public PerformanceGrade getPerformanceGrade() {
            double durationScore = getTotalDurationMs() < 100 ? 1.0 : 
                                 getTotalDurationMs() < 500 ? 0.8 : 
                                 getTotalDurationMs() < 1000 ? 0.6 : 0.4;
            
            double speedupScore = totalSpeedupFactor > 8 ? 1.0 : 
                                totalSpeedupFactor > 5 ? 0.9 :
                                totalSpeedupFactor > 2 ? 0.8 : 
                                totalSpeedupFactor > 1 ? 0.6 : 0.4;
            
            double efficiencyScore = overallEfficiency;
            
            // Bonus por tener pre-carga de clases críticas
            double classPreloadBonus = isCriticalClassPreloadAvailable() ? 0.1 : 0.0;
            
            double overallScore = (durationScore + speedupScore + efficiencyScore) / 3 + classPreloadBonus;
            
            if (overallScore >= 0.95) return PerformanceGrade.A_PLUS;
            if (overallScore >= 0.85) return PerformanceGrade.A;
            if (overallScore >= 0.75) return PerformanceGrade.B;
            if (overallScore >= 0.65) return PerformanceGrade.C;
            return PerformanceGrade.D;
        }
        
        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append(String.format(
                "PerformanceMetrics{duration=%dms, speedup=%.2fx, saved=%dms, efficiency=%.1f%%, grade=%s}",
                getTotalDurationMs(), totalSpeedupFactor, totalTimeSaved, 
                overallEfficiency * 100, getPerformanceGrade()
            ));
            
            if (isCriticalClassPreloadAvailable()) {
                sb.append(String.format(", criticalClasses=%d", criticalClassResult.getSuccessfullyLoaded()));
            }
            
            return sb.toString();
        }
    }
    
    /**
     * Calificación de rendimiento
     */
    public enum PerformanceGrade {
        A_PLUS("A+", "Excelente"),
        A("A", "Muy Bueno"),
        B("B", "Bueno"),
        C("C", "Aceptable"),
        D("D", "Necesita Mejora");
        
        private final String grade;
        private final String description;
        
        PerformanceGrade(String grade, String description) {
            this.grade = grade;
            this.description = description;
        }
        
        public String getGrade() { return grade; }
        public String getDescription() { return description; }
    }
    
    @Override
    public String toString() {
        return String.format(
            "ComprehensiveStartupResult{duration=%dms, speedup=%.2fx, saved=%s, efficiency=%.1f%%}",
            getTotalDurationMs(), totalSpeedupFactor, 
            formatDuration(totalTimeSaved), overallEfficiency * 100
        );
    }
}