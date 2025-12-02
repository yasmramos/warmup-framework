package io.warmup.examples.startup.config;

/**
 * Estadísticas del sistema de mapeo de configuración en memoria.
 * Proporciona métricas de rendimiento y eficiencia del sistema.
 */
public class ConfigMappingStats {
    
    private final long totalFilesMapped;
    private final long totalBytesMapped;
    private final long mappingDurationMs;
    private final long parsingDurationMs;
    private final int loadedConfigsCount;
    private final int registeredConfigsCount;
    
    // Estadísticas calculadas
    private final double avgFileSizeBytes;
    private final double mappingSpeedMBps;
    private final double parsingSpeedMBps;
    private final double efficiencyRatio;
    
    public ConfigMappingStats(long totalFilesMapped, long totalBytesMapped,
                            long mappingDurationMs, long parsingDurationMs,
                            int loadedConfigsCount, int registeredConfigsCount) {
        this.totalFilesMapped = totalFilesMapped;
        this.totalBytesMapped = totalBytesMapped;
        this.mappingDurationMs = mappingDurationMs;
        this.parsingDurationMs = parsingDurationMs;
        this.loadedConfigsCount = loadedConfigsCount;
        this.registeredConfigsCount = registeredConfigsCount;
        
        // Calcular métricas derivadas
        this.avgFileSizeBytes = totalFilesMapped > 0 ? 
            (double) totalBytesMapped / totalFilesMapped : 0;
        
        this.mappingSpeedMBps = mappingDurationMs > 0 ? 
            (totalBytesMapped / (1024.0 * 1024.0)) / (mappingDurationMs / 1000.0) : 0;
        
        this.parsingSpeedMBps = parsingDurationMs > 0 ? 
            (totalBytesMapped / (1024.0 * 1024.0)) / (parsingDurationMs / 1000.0) : 0;
        
        this.efficiencyRatio = (loadedConfigsCount > 0) ? 
            (double) loadedConfigsCount / registeredConfigsCount : 0;
    }
    
    /**
     * Obtiene el número total de archivos mapeados
     */
    public long getTotalFilesMapped() {
        return totalFilesMapped;
    }
    
    /**
     * Obtiene el total de bytes mapeados
     */
    public long getTotalBytesMapped() {
        return totalBytesMapped;
    }
    
    /**
     * Obtiene el total de bytes mapeados en formato legible
     */
    public String getFormattedTotalBytes() {
        return formatBytes(totalBytesMapped);
    }
    
    /**
     * Obtiene la duración del mapeo en milisegundos
     */
    public long getMappingDurationMs() {
        return mappingDurationMs;
    }
    
    /**
     * Obtiene la duración del mapeo en formato legible
     */
    public String getFormattedMappingDuration() {
        return formatDuration(mappingDurationMs);
    }
    
    /**
     * Obtiene la duración del parsing en milisegundos
     */
    public long getParsingDurationMs() {
        return parsingDurationMs;
    }
    
    /**
     * Obtiene la duración del parsing en formato legible
     */
    public String getFormattedParsingDuration() {
        return formatDuration(parsingDurationMs);
    }
    
    /**
     * Obtiene el número de configuraciones cargadas exitosamente
     */
    public int getLoadedConfigsCount() {
        return loadedConfigsCount;
    }
    
    /**
     * Obtiene el número de configuraciones registradas
     */
    public int getRegisteredConfigsCount() {
        return registeredConfigsCount;
    }
    
    /**
     * Obtiene el tamaño promedio de archivo en bytes
     */
    public double getAvgFileSizeBytes() {
        return avgFileSizeBytes;
    }
    
    /**
     * Obtiene el tamaño promedio de archivo en formato legible
     */
    public String getFormattedAvgFileSize() {
        return formatBytes((long) avgFileSizeBytes);
    }
    
    /**
     * Obtiene la velocidad de mapeo en MB/segundo
     */
    public double getMappingSpeedMBps() {
        return mappingSpeedMBps;
    }
    
    /**
     * Obtiene la velocidad de parsing en MB/segundo
     */
    public double getParsingSpeedMBps() {
        return parsingSpeedMBps;
    }
    
    /**
     * Obtiene la eficiencia de carga (configuraciones cargadas / registradas)
     */
    public double getEfficiencyRatio() {
        return efficiencyRatio;
    }
    
    /**
     * Obtiene la eficiencia como porcentaje
     */
    public double getEfficiencyPercentage() {
        return efficiencyRatio * 100;
    }
    
    /**
     * Obtiene el tiempo total de procesamiento
     */
    public long getTotalProcessingTimeMs() {
        return mappingDurationMs + parsingDurationMs;
    }
    
    /**
     * Obtiene el tiempo total en formato legible
     */
    public String getFormattedTotalProcessingTime() {
        return formatDuration(getTotalProcessingTimeMs());
    }
    
    /**
     * Obtiene estadísticas de rendimiento
     */
    public PerformanceMetrics getPerformanceMetrics() {
        return new PerformanceMetrics(
            mappingSpeedMBps,
            parsingSpeedMBps,
            efficiencyRatio,
            avgFileSizeBytes,
            getTotalProcessingTimeMs()
        );
    }
    
    /**
     * Obtiene un resumen ejecutivo de las estadísticas
     */
    public String getExecutiveSummary() {
        return String.format(
            "Config Mapping Summary: %d archivos, %s total, %s mapeo, %s parsing, %.1f%% eficiencia",
            totalFilesMapped,
            getFormattedTotalBytes(),
            getFormattedMappingDuration(),
            getFormattedParsingDuration(),
            getEfficiencyPercentage()
        );
    }
    
    /**
     * Formatea bytes en unidades legibles
     */
    private String formatBytes(long bytes) {
        if (bytes < 1024) {
            return bytes + " B";
        } else if (bytes < 1024 * 1024) {
            return String.format("%.1f KB", bytes / 1024.0);
        } else if (bytes < 1024 * 1024 * 1024) {
            return String.format("%.1f MB", bytes / (1024.0 * 1024.0));
        } else {
            return String.format("%.1f GB", bytes / (1024.0 * 1024.0 * 1024.0));
        }
    }
    
    /**
     * Formatea duración en milisegundos a formato legible
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
     * Métricas de rendimiento del sistema de mapeo
     */
    public static class PerformanceMetrics {
        private final double mappingSpeedMBps;
        private final double parsingSpeedMBps;
        private final double efficiencyRatio;
        private final double avgFileSizeBytes;
        private final long totalProcessingTimeMs;
        
        public PerformanceMetrics(double mappingSpeedMBps, double parsingSpeedMBps,
                                double efficiencyRatio, double avgFileSizeBytes,
                                long totalProcessingTimeMs) {
            this.mappingSpeedMBps = mappingSpeedMBps;
            this.parsingSpeedMBps = parsingSpeedMBps;
            this.efficiencyRatio = efficiencyRatio;
            this.avgFileSizeBytes = avgFileSizeBytes;
            this.totalProcessingTimeMs = totalProcessingTimeMs;
        }
        
        // Getters
        public double getMappingSpeedMBps() { return mappingSpeedMBps; }
        public double getParsingSpeedMBps() { return parsingSpeedMBps; }
        public double getEfficiencyRatio() { return efficiencyRatio; }
        public double getAvgFileSizeBytes() { return avgFileSizeBytes; }
        public long getTotalProcessingTimeMs() { return totalProcessingTimeMs; }
        
        /**
         * Evalúa la calidad del rendimiento
         */
        public PerformanceRating getPerformanceRating() {
            double speedScore = (mappingSpeedMBps + parsingSpeedMBps) / 2;
            double efficiencyScore = efficiencyRatio * 100;
            
            if (speedScore >= 100 && efficiencyScore >= 90) {
                return PerformanceRating.EXCELLENT;
            } else if (speedScore >= 50 && efficiencyScore >= 80) {
                return PerformanceRating.GOOD;
            } else if (speedScore >= 20 && efficiencyScore >= 60) {
                return PerformanceRating.ACCEPTABLE;
            } else {
                return PerformanceRating.NEEDS_IMPROVEMENT;
            }
        }
        
        @Override
        public String toString() {
            return String.format(
                "PerformanceMetrics{speed=%.1f MB/s, efficiency=%.1f%%, rating=%s}",
                (mappingSpeedMBps + parsingSpeedMBps) / 2, 
                efficiencyRatio * 100, 
                getPerformanceRating()
            );
        }
    }
    
    /**
     * Calificación de rendimiento
     */
    public enum PerformanceRating {
        EXCELLENT("Excelente"),
        GOOD("Bueno"),
        ACCEPTABLE("Aceptable"),
        NEEDS_IMPROVEMENT("Necesita mejora");
        
        private final String description;
        
        PerformanceRating(String description) {
            this.description = description;
        }
        
        public String getDescription() {
            return description;
        }
    }
    
    @Override
    public String toString() {
        return String.format(
            "ConfigMappingStats{files=%d, bytes=%s, mapping=%s, parsing=%s, efficiency=%.1f%%}",
            totalFilesMapped,
            getFormattedTotalBytes(),
            getFormattedMappingDuration(),
            getFormattedParsingDuration(),
            getEfficiencyPercentage()
        );
    }
}