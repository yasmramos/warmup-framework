package io.warmup.framework.cache;

import java.io.Serializable;

/**
 * Configuración para el sistema de cache del framework Warmup.
 * Utiliza el patrón Builder para configuración fluida.
 * 
 * @author MiniMax Agent
 * @since 2.0
 */
public class CacheConfig implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    // Configuraciones por defecto
    public static final int DEFAULT_MAX_MEMORY_SIZE = 1000;
    public static final long DEFAULT_MAX_AGE = 300000; // 5 minutos
    public static final boolean DEFAULT_COMPRESSION = true;
    public static final boolean DEFAULT_PERSISTENCE = false;
    public static final int DEFAULT_MAX_ENTRIES = 10000;
    
    // Configuraciones
    private int maxMemorySize = DEFAULT_MAX_MEMORY_SIZE;
    private long maxAge = DEFAULT_MAX_AGE;
    private boolean compressionEnabled = DEFAULT_COMPRESSION;
    private boolean persistenceEnabled = DEFAULT_PERSISTENCE;
    private int maxEntries = DEFAULT_MAX_ENTRIES;
    private String cacheName = "WarmupCache";
    private boolean enableStatistics = true;
    private boolean enableMetrics = true;
    
    /**
     * Constructor privado para patrón Builder
     */
    private CacheConfig() {}
    
    /**
     * Constructor con nombre específico
     */
    private CacheConfig(String cacheName) {
        this.cacheName = cacheName;
    }
    
    /**
     * Crea una nueva instancia de CacheConfig con configuración por defecto
     */
    public static CacheConfig create() {
        return new CacheConfig();
    }
    
    /**
     * Crea una nueva instancia de CacheConfig con nombre específico
     */
    public static CacheConfig create(String cacheName) {
        return new CacheConfig(cacheName);
    }
    
    /**
     * Establece el tamaño máximo de memoria en bytes
     */
    public CacheConfig withMaxMemorySize(int maxMemorySize) {
        if (maxMemorySize <= 0) {
            throw new IllegalArgumentException("El tamaño de memoria debe ser mayor que 0");
        }
        this.maxMemorySize = maxMemorySize;
        return this;
    }
    
    /**
     * Establece la edad máxima en milisegundos antes de que una entrada expire
     */
    public CacheConfig withMaxAge(long maxAge) {
        if (maxAge <= 0) {
            throw new IllegalArgumentException("La edad máxima debe ser mayor que 0");
        }
        this.maxAge = maxAge;
        return this;
    }
    
    /**
     * Habilita o deshabilita la compresión del cache
     */
    public CacheConfig withCompression(boolean compressionEnabled) {
        this.compressionEnabled = compressionEnabled;
        return this;
    }
    
    /**
     * Habilita o deshabilita la persistencia del cache
     */
    public CacheConfig withPersistence(boolean persistenceEnabled) {
        this.persistenceEnabled = persistenceEnabled;
        return this;
    }
    
    /**
     * Establece el número máximo de entradas en el cache
     */
    public CacheConfig withMaxEntries(int maxEntries) {
        if (maxEntries <= 0) {
            throw new IllegalArgumentException("El número máximo de entradas debe ser mayor que 0");
        }
        this.maxEntries = maxEntries;
        return this;
    }
    
    /**
     * Establece el nombre del cache
     */
    public CacheConfig withCacheName(String cacheName) {
        if (cacheName == null || cacheName.trim().isEmpty()) {
            throw new IllegalArgumentException("El nombre del cache no puede estar vacío");
        }
        this.cacheName = cacheName;
        return this;
    }
    
    /**
     * Habilita o deshabilita las estadísticas del cache
     */
    public CacheConfig withStatistics(boolean enableStatistics) {
        this.enableStatistics = enableStatistics;
        return this;
    }
    
    /**
     * Habilita o deshabilita las métricas del cache
     */
    public CacheConfig withMetrics(boolean enableMetrics) {
        this.enableMetrics = enableMetrics;
        return this;
    }
    
    // Getters
    
    public int getMaxMemorySize() {
        return maxMemorySize;
    }
    
    public long getMaxAge() {
        return maxAge;
    }
    
    public boolean isCompressionEnabled() {
        return compressionEnabled;
    }
    
    public boolean isPersistenceEnabled() {
        return persistenceEnabled;
    }
    
    public int getMaxEntries() {
        return maxEntries;
    }
    
    public String getCacheName() {
        return cacheName;
    }
    
    public boolean isStatisticsEnabled() {
        return enableStatistics;
    }
    
    public boolean isMetricsEnabled() {
        return enableMetrics;
    }
    
    /**
     * Crea una copia de la configuración
     */
    public CacheConfig copy() {
        CacheConfig copy = new CacheConfig(this.cacheName);
        copy.maxMemorySize = this.maxMemorySize;
        copy.maxAge = this.maxAge;
        copy.compressionEnabled = this.compressionEnabled;
        copy.persistenceEnabled = this.persistenceEnabled;
        copy.maxEntries = this.maxEntries;
        copy.enableStatistics = this.enableStatistics;
        copy.enableMetrics = this.enableMetrics;
        return copy;
    }
    
    /**
     * Valida que la configuración sea válida
     */
    public boolean isValid() {
        return maxMemorySize > 0 && 
               maxAge > 0 && 
               maxEntries > 0 && 
               cacheName != null && 
               !cacheName.trim().isEmpty();
    }
    
    /**
     * Obtiene una representación en string de la configuración
     */
    @Override
    public String toString() {
        return String.format(
            "CacheConfig{name='%s', maxMemorySize=%d, maxAge=%d, maxEntries=%d, compression=%s, persistence=%s, statistics=%s, metrics=%s}",
            cacheName, maxMemorySize, maxAge, maxEntries, 
            compressionEnabled, persistenceEnabled, enableStatistics, enableMetrics
        );
    }
    
    /**
     * Valida si esta configuración es igual a otra
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        
        CacheConfig that = (CacheConfig) obj;
        return maxMemorySize == that.maxMemorySize &&
               maxAge == that.maxAge &&
               compressionEnabled == that.compressionEnabled &&
               persistenceEnabled == that.persistenceEnabled &&
               maxEntries == that.maxEntries &&
               enableStatistics == that.enableStatistics &&
               enableMetrics == that.enableMetrics &&
               cacheName.equals(that.cacheName);
    }
    
    /**
     * Genera hashcode para la configuración
     */
    @Override
    public int hashCode() {
        int result = maxMemorySize;
        result = 31 * result + (int) (maxAge ^ (maxAge >>> 32));
        result = 31 * result + (compressionEnabled ? 1 : 0);
        result = 31 * result + (persistenceEnabled ? 1 : 0);
        result = 31 * result + maxEntries;
        result = 31 * result + (enableStatistics ? 1 : 0);
        result = 31 * result + (enableMetrics ? 1 : 0);
        result = 31 * result + cacheName.hashCode();
        return result;
    }
}