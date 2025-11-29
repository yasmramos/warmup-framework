package io.warmup.examples.startup.config;

import java.util.Map;

/**
 * Proporciona acceso directo y eficiente a datos de configuración cargados en memoria.
 * Elimina el overhead de parsing durante runtime mediante acceso directo a datos estructurados.
 */
public class ConfigDataAccessor {
    
    private final String configKey;
    private final MappedConfigData configData;
    
    // Cache de valores frecuentemente accedidos
    private final java.util.concurrent.ConcurrentHashMap<String, Object> valueCache;
    
    // Estadísticas de rendimiento
    private volatile long cacheHitCount = 0;
    private volatile long directAccessCount = 0;
    private volatile long totalAccessTimeNs = 0;
    
    public ConfigDataAccessor(String configKey, MappedConfigData configData) {
        this.configKey = configKey;
        this.configData = configData;
        this.valueCache = new java.util.concurrent.ConcurrentHashMap<>();
    }
    
    /**
     * Obtiene un valor con cache automático para acceso frecuente
     */
    public Object get(String key) {
        long startTime = System.nanoTime();
        
        try {
            // Intentar cache primero
            Object cachedValue = valueCache.get(key);
            if (cachedValue != null) {
                cacheHitCount++;
                return cachedValue;
            }
            
            // Acceso directo a datos estructurados
            Object value = configData.getValue(key);
            
            // Cachear valores no nulos para futuras consultas
            if (value != null) {
                valueCache.put(key, value);
            }
            
            directAccessCount++;
            return value;
            
        } finally {
            long endTime = System.nanoTime();
            totalAccessTimeNs += (endTime - startTime);
        }
    }
    
    /**
     * Obtiene un valor como String
     */
    public String getString(String key) {
        Object value = get(key);
        return value != null ? value.toString() : null;
    }
    
    /**
     * Obtiene un valor como String con valor por defecto
     */
    public String getString(String key, String defaultValue) {
        String value = getString(key);
        return value != null ? value : defaultValue;
    }
    
    /**
     * Obtiene un valor como Integer
     */
    public Integer getInteger(String key) {
        return configData.getInteger(key);
    }
    
    /**
     * Obtiene un valor como Integer con valor por defecto
     */
    public Integer getInteger(String key, Integer defaultValue) {
        Integer value = getInteger(key);
        return value != null ? value : defaultValue;
    }
    
    /**
     * Obtiene un valor como Long
     */
    public Long getLong(String key) {
        return configData.getLong(key);
    }
    
    /**
     * Obtiene un valor como Long con valor por defecto
     */
    public Long getLong(String key, Long defaultValue) {
        Long value = getLong(key);
        return value != null ? value : defaultValue;
    }
    
    /**
     * Obtiene un valor como Double
     */
    public Double getDouble(String key) {
        return configData.getDouble(key);
    }
    
    /**
     * Obtiene un valor como Double con valor por defecto
     */
    public Double getDouble(String key, Double defaultValue) {
        Double value = getDouble(key);
        return value != null ? value : defaultValue;
    }
    
    /**
     * Obtiene un valor como Boolean
     */
    public Boolean getBoolean(String key) {
        return configData.getBoolean(key);
    }
    
    /**
     * Obtiene un valor como Boolean con valor por defecto
     */
    public Boolean getBoolean(String key, Boolean defaultValue) {
        Boolean value = getBoolean(key);
        return value != null ? value : defaultValue;
    }
    
    /**
     * Verifica si existe una clave
     */
    public boolean hasKey(String key) {
        return configData.hasKey(key);
    }
    
    /**
     * Obtiene todas las claves disponibles
     */
    public java.util.Set<String> getAllKeys() {
        return configData.getAllKeys();
    }
    
    /**
     * Obtiene múltiples valores como mapa
     */
    public Map<String, Object> getValues(String... keys) {
        Map<String, Object> result = new java.util.HashMap<>();
        for (String key : keys) {
            result.put(key, get(key));
        }
        return result;
    }
    
    /**
     * Prefija todas las claves con un prefijo y obtiene los valores
     */
    public Map<String, Object> getPrefixedValues(String prefix) {
        Map<String, Object> result = new java.util.HashMap<>();
        for (String key : configData.getAllKeys()) {
            if (key.startsWith(prefix)) {
                String cleanKey = key.substring(prefix.length());
                if (cleanKey.startsWith(".")) {
                    cleanKey = cleanKey.substring(1);
                }
                result.put(cleanKey, get(key));
            }
        }
        return result;
    }
    
    /**
     * Evalúa una expresión de configuración (ej: "db.host" -> "localhost")
     */
    public Object evaluateExpression(String expression) {
        // Soporte para expresiones simples con puntos
        if (expression.contains(".")) {
            return get(expression);
        }
        return get(expression);
    }
    
    /**
     * Obtiene estadísticas de rendimiento
     */
    public AccessPerformanceStats getPerformanceStats() {
        long totalAccesses = cacheHitCount + directAccessCount;
        double avgAccessTimeNs = totalAccesses > 0 ? 
            (double) totalAccessTimeNs / totalAccesses : 0;
        double cacheHitRate = totalAccesses > 0 ? 
            (double) cacheHitCount / totalAccesses * 100 : 0;
        
        return new AccessPerformanceStats(
            configKey,
            totalAccesses,
            cacheHitCount,
            directAccessCount,
            cacheHitRate,
            avgAccessTimeNs,
            totalAccessTimeNs,
            valueCache.size()
        );
    }
    
    /**
     * Limpia el cache de valores
     */
    public void clearCache() {
        valueCache.clear();
    }
    
    /**
     * Obtiene información sobre la configuración
     */
    public ConfigInfo getConfigInfo() {
        MappedConfigData.AccessStats accessStats = configData.getAccessStats();
        
        return new ConfigInfo(
            configKey,
            configData.getConfigType(),
            configData.getKeyCount(),
            configData.getByteSize(),
            accessStats.getTotalAccessCount(),
            accessStats.getFormattedLastAccessTime(),
            valueCache.size()
        );
    }
    
    /**
     * Estadísticas de rendimiento de acceso
     */
    public static class AccessPerformanceStats {
        private final String configKey;
        private final long totalAccesses;
        private final long cacheHits;
        private final long directAccesses;
        private final double cacheHitRate;
        private final double avgAccessTimeNs;
        private final long totalAccessTimeNs;
        private final int cacheSize;
        
        public AccessPerformanceStats(String configKey, long totalAccesses, long cacheHits,
                                    long directAccesses, double cacheHitRate,
                                    double avgAccessTimeNs, long totalAccessTimeNs, int cacheSize) {
            this.configKey = configKey;
            this.totalAccesses = totalAccesses;
            this.cacheHits = cacheHits;
            this.directAccesses = directAccesses;
            this.cacheHitRate = cacheHitRate;
            this.avgAccessTimeNs = avgAccessTimeNs;
            this.totalAccessTimeNs = totalAccessTimeNs;
            this.cacheSize = cacheSize;
        }
        
        // Getters
        public String getConfigKey() { return configKey; }
        public long getTotalAccesses() { return totalAccesses; }
        public long getCacheHits() { return cacheHits; }
        public long getDirectAccesses() { return directAccesses; }
        public double getCacheHitRate() { return cacheHitRate; }
        public double getAvgAccessTimeNs() { return avgAccessTimeNs; }
        public double getAvgAccessTimeUs() { return avgAccessTimeNs / 1000; }
        public double getAvgAccessTimeMs() { return avgAccessTimeNs / 1_000_000; }
        public long getTotalAccessTimeNs() { return totalAccessTimeNs; }
        public int getCacheSize() { return cacheSize; }
        
        @Override
        public String toString() {
            return String.format(
                "AccessStats{key='%s', total=%d, cacheHits=%d (%.1f%%), avgTime=%.2fµs, cacheSize=%d}",
                configKey, totalAccesses, cacheHits, cacheHitRate, getAvgAccessTimeUs(), cacheSize
            );
        }
    }
    
    /**
     * Información general de la configuración
     */
    public static class ConfigInfo {
        private final String configKey;
        private final String configType;
        private final int keyCount;
        private final int byteSize;
        private final long accessCount;
        private final String lastAccessTime;
        private final int cacheSize;
        
        public ConfigInfo(String configKey, String configType, int keyCount, int byteSize,
                         long accessCount, String lastAccessTime, int cacheSize) {
            this.configKey = configKey;
            this.configType = configType;
            this.keyCount = keyCount;
            this.byteSize = byteSize;
            this.accessCount = accessCount;
            this.lastAccessTime = lastAccessTime;
            this.cacheSize = cacheSize;
        }
        
        // Getters
        public String getConfigKey() { return configKey; }
        public String getConfigType() { return configType; }
        public int getKeyCount() { return keyCount; }
        public int getByteSize() { return byteSize; }
        public long getAccessCount() { return accessCount; }
        public String getLastAccessTime() { return lastAccessTime; }
        public int getCacheSize() { return cacheSize; }
        
        @Override
        public String toString() {
            return String.format(
                "ConfigInfo{key='%s', type=%s, keys=%d, bytes=%d, accesses=%d, last=%s, cache=%d}",
                configKey, configType, keyCount, byteSize, accessCount, lastAccessTime, cacheSize
            );
        }
    }
    
    @Override
    public String toString() {
        return String.format("ConfigAccessor{key='%s', type=%s, keys=%d}", 
            configKey, configData.getConfigType(), configData.getKeyCount());
    }
}