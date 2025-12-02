package io.warmup.framework.startup.config;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Sistema principal de configuración precargada que coordina:
 * - Mapeo de archivos en memoria
 * - Acceso instantáneo via OS page cache
 * - Eliminación de parsing/IO durante runtime
 * 
 * Integra con los sistemas de inicialización paralela y zero cost startup.
 */
public class PreloadedConfigSystem {
    
    private final MemoryMappedConfigLoader configLoader;
    private final Map<String, ConfigDataAccessor> configAccessors;
    private final PreloadOptimizationMetrics optimizationMetrics;
    
    // Estados del sistema
    private volatile boolean isPreloading = false;
    private volatile boolean isReady = false;
    private volatile long preloadStartTime = 0;
    private volatile long totalStartupTimeSaved = 0;
    
    public PreloadedConfigSystem() {
        this.configLoader = new MemoryMappedConfigLoader();
        this.configAccessors = new ConcurrentHashMap<>();
        this.optimizationMetrics = new PreloadOptimizationMetrics();
        
        // Registrar configuraciones predeterminadas
        registerDefaultConfigurations();
    }
    
    /**
     * Registra configuraciones predeterminadas del framework
     */
    private void registerDefaultConfigurations() {
        // Configuraciones del framework Warmup
        configLoader.registerConfigFile("warmup.properties", 
            java.nio.file.Paths.get("src/main/resources/warmup.properties"));
        
        // Configuraciones de la aplicación
        configLoader.registerConfigFile("application.properties", 
            java.nio.file.Paths.get("src/main/resources/application.properties"));
        
        configLoader.registerConfigFile("application.yml", 
            java.nio.file.Paths.get("src/main/resources/application.yml"));
        
        // Configuraciones de runtime
        configLoader.registerConfigFile("runtime-config.json", 
            java.nio.file.Paths.get("src/main/resources/runtime-config.json"));
        
        // Configuraciones específicas del subsistema
        configLoader.registerConfigFile("database.config", 
            java.nio.file.Paths.get("src/main/resources/database.config"));
        
        configLoader.registerConfigFile("security.config", 
            java.nio.file.Paths.get("src/main/resources/security.config"));
        
        configLoader.registerConfigFile("logging.config", 
            java.nio.file.Paths.get("src/main/resources/logging.config"));
    }
    
    /**
     * Inicia la precarga de configuraciones en paralelo con otros subsistemas
     */
    public CompletableFuture<PreloadResult> preloadConfigurations() {
        if (isPreloading || isReady) {
            return CompletableFuture.completedFuture(
                PreloadResult.alreadyInProgressOrReady(isPreloading, isReady)
            );
        }
        
        this.isPreloading = true;
        this.preloadStartTime = System.currentTimeMillis();
        
        long estimatedStartupSavings = estimateStartupSavings();
        
        return configLoader.loadAllConfigsInParallel()
            .thenApply(results -> {
                long preloadEndTime = System.currentTimeMillis();
                long actualPreloadTime = preloadEndTime - preloadStartTime;
                
                // Crear accessors para configuraciones exitosas
                int successfulLoads = 0;
                for (Map.Entry<String, MemoryMappedConfigLoader.ConfigLoadingResult> entry : results.entrySet()) {
                    if (entry.getValue().isSuccess()) {
                        ConfigDataAccessor accessor = configLoader.getConfigAccessor(entry.getKey());
                        configAccessors.put(entry.getKey(), accessor);
                        successfulLoads++;
                    }
                }
                
                // Actualizar métricas de optimización
                optimizationMetrics.recordPreloadResults(
                    results, actualPreloadTime, estimatedStartupSavings
                );
                
                this.isPreloading = false;
                this.isReady = true;
                this.totalStartupTimeSaved = estimatedStartupSavings;
                
                return new PreloadResult(
                    successfulLoads,
                    results.size(),
                    actualPreloadTime,
                    estimatedStartupSavings,
                    optimizationMetrics.getOverallStats()
                );
            })
            .whenComplete((result, error) -> {
                if (error != null) {
                    this.isPreloading = false;
                    System.err.println("Error durante precarga de configuraciones: " + error.getMessage());
                }
            });
    }
    
    /**
     * Obtiene un accessor de configuración
     */
    public ConfigDataAccessor getConfigAccessor(String configKey) {
        if (!isReady) {
            throw new IllegalStateException("Sistema de configuración no está listo. Llamar preloadConfigurations() primero.");
        }
        
        ConfigDataAccessor accessor = configAccessors.get(configKey);
        if (accessor == null) {
            throw new IllegalArgumentException("Configuración no encontrada o no cargada: " + configKey);
        }
        
        return accessor;
    }
    
    /**
     * Obtiene múltiples accessors
     */
    public Map<String, ConfigDataAccessor> getConfigAccessors(String... configKeys) {
        Map<String, ConfigDataAccessor> result = new ConcurrentHashMap<>();
        for (String key : configKeys) {
            if (configAccessors.containsKey(key)) {
                result.put(key, configAccessors.get(key));
            }
        }
        return result;
    }
    
    /**
     * Obtiene todas las configuraciones cargadas
     */
    public Map<String, ConfigDataAccessor> getAllConfigAccessors() {
        return new ConcurrentHashMap<>(configAccessors);
    }
    
    /**
     * Verifica si el sistema está listo
     */
    public boolean isReady() {
        return isReady;
    }
    
    /**
     * Verifica si está precargando
     */
    public boolean isPreloading() {
        return isPreloading;
    }
    
    /**
     * Obtiene métricas de optimización
     */
    public PreloadOptimizationMetrics getOptimizationMetrics() {
        return optimizationMetrics;
    }
    
    /**
     * Estima el ahorro de tiempo de startup basado en configuraciones
     */
    private long estimateStartupSavings() {
        // Estimación basada en:
        // - Número de archivos de configuración
        // - Tamaños típicos de archivos
        // - Tiempo promedio de I/O y parsing
        
        long configCount = configAccessors.size() + 
            (isPreloading ? 0 : configLoader.getMappingStats().getTotalFilesMapped());
        
        // Estimación conservadora: ~10-50ms por archivo de configuración
        long estimatedSavings = configCount * 25; // 25ms promedio por archivo
        
        // Factor por tamaño de archivo (archivos más grandes toman más tiempo)
        ConfigMappingStats stats = configLoader.getMappingStats();
        if (stats.getTotalBytesMapped() > 1024 * 1024) { // > 1MB
            estimatedSavings *= 2; // Archivos grandes duplican el ahorro
        }
        
        return estimatedSavings;
    }
    
    /**
     * Registra una nueva configuración para precarga
     */
    public void registerConfiguration(String configKey, java.nio.file.Path filePath) {
        configLoader.registerConfigFile(configKey, filePath);
    }
    
    /**
     * Fuerza la recarga de todas las configuraciones
     */
    public CompletableFuture<PreloadResult> reloadConfigurations() {
        isReady = false;
        configAccessors.clear();
        optimizationMetrics.reset();
        
        return preloadConfigurations();
    }
    
    /**
     * Libera recursos del sistema
     */
    public void shutdown() {
        configLoader.shutdown();
        configAccessors.clear();
        optimizationMetrics.reset();
        isReady = false;
    }
    
    /**
     * Resultado de la precarga
     */
    public static class PreloadResult {
        private final int successfulLoads;
        private final int totalRequested;
        private final long actualPreloadTimeMs;
        private final long estimatedStartupSavingsMs;
        private final PreloadOptimizationMetrics.OverallOptimizationStats overallStats;
        private final boolean alreadyInProgress;
        private final boolean alreadyReady;
        
        private PreloadResult(int successfulLoads, int totalRequested, long actualPreloadTimeMs,
                            long estimatedStartupSavingsMs,
                            PreloadOptimizationMetrics.OverallOptimizationStats overallStats) {
            this.successfulLoads = successfulLoads;
            this.totalRequested = totalRequested;
            this.actualPreloadTimeMs = actualPreloadTimeMs;
            this.estimatedStartupSavingsMs = estimatedStartupSavingsMs;
            this.overallStats = overallStats;
            this.alreadyInProgress = false;
            this.alreadyReady = false;
        }
        
        private PreloadResult(boolean alreadyInProgress, boolean alreadyReady) {
            this.successfulLoads = 0;
            this.totalRequested = 0;
            this.actualPreloadTimeMs = 0;
            this.estimatedStartupSavingsMs = 0;
            this.overallStats = null;
            this.alreadyInProgress = alreadyInProgress;
            this.alreadyReady = alreadyReady;
        }
        
        public static PreloadResult alreadyInProgressOrReady(boolean isPreloading, boolean isReady) {
            return new PreloadResult(isPreloading, isReady);
        }
        
        // Getters
        public int getSuccessfulLoads() { return successfulLoads; }
        public int getTotalRequested() { return totalRequested; }
        public long getActualPreloadTimeMs() { return actualPreloadTimeMs; }
        public long getEstimatedStartupSavingsMs() { return estimatedStartupSavingsMs; }
        public PreloadOptimizationMetrics.OverallOptimizationStats getOverallStats() { return overallStats; }
        public boolean isAlreadyInProgress() { return alreadyInProgress; }
        public boolean isAlreadyReady() { return alreadyReady; }
        
        public double getSuccessRate() {
            return totalRequested > 0 ? (double) successfulLoads / totalRequested : 0;
        }
        
        public double getEfficiency() {
            return estimatedStartupSavingsMs > 0 ? 
                (double) estimatedStartupSavingsMs / actualPreloadTimeMs : 0;
        }
        
        @Override
        public String toString() {
            return String.format(
                "PreloadResult{success=%d/%d (%.1f%%), time=%dms, savings=%dms, efficiency=%.2f}",
                successfulLoads, totalRequested, getSuccessRate() * 100,
                actualPreloadTimeMs, estimatedStartupSavingsMs, getEfficiency()
            );
        }
    }
}