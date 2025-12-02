package io.warmup.examples.startup.config;

import java.util.Map;

/**
 * Representa datos de configuración estructurados cargados desde memoria mapeada.
 * Proporciona acceso directo a valores sin parsing adicional durante runtime.
 */
public class MappedConfigData {
    
    private final String configType;
    private final Map<String, Object> dataMap;
    private final int byteSize;
    
    // Estadísticas de acceso
    private volatile long totalAccessCount = 0;
    private volatile long lastAccessTime = 0;
    private final Object accessLock = new Object();
    
    public MappedConfigData(String configType, Map<String, Object> dataMap, int byteSize) {
        this.configType = configType;
        this.dataMap = dataMap;
        this.byteSize = byteSize;
    }
    
    /**
     * Obtiene un valor de configuración por clave
     */
    public Object getValue(String key) {
        incrementAccessCount();
        return dataMap.get(key);
    }
    
    /**
     * Obtiene un valor como String
     */
    public String getString(String key) {
        Object value = getValue(key);
        return value != null ? value.toString() : null;
    }
    
    /**
     * Obtiene un valor como Integer
     */
    public Integer getInteger(String key) {
        Object value = getValue(key);
        if (value instanceof Integer) {
            return (Integer) value;
        } else if (value instanceof Number) {
            return ((Number) value).intValue();
        } else if (value instanceof String) {
            try {
                return Integer.parseInt((String) value);
            } catch (NumberFormatException e) {
                return null;
            }
        }
        return null;
    }
    
    /**
     * Obtiene un valor como Long
     */
    public Long getLong(String key) {
        Object value = getValue(key);
        if (value instanceof Long) {
            return (Long) value;
        } else if (value instanceof Number) {
            return ((Number) value).longValue();
        } else if (value instanceof String) {
            try {
                return Long.parseLong((String) value);
            } catch (NumberFormatException e) {
                return null;
            }
        }
        return null;
    }
    
    /**
     * Obtiene un valor como Double
     */
    public Double getDouble(String key) {
        Object value = getValue(key);
        if (value instanceof Double) {
            return (Double) value;
        } else if (value instanceof Number) {
            return ((Number) value).doubleValue();
        } else if (value instanceof String) {
            try {
                return Double.parseDouble((String) value);
            } catch (NumberFormatException e) {
                return null;
            }
        }
        return null;
    }
    
    /**
     * Obtiene un valor como Boolean
     */
    public Boolean getBoolean(String key) {
        Object value = getValue(key);
        if (value instanceof Boolean) {
            return (Boolean) value;
        } else if (value instanceof String) {
            return Boolean.parseBoolean((String) value);
        } else if (value instanceof Number) {
            return ((Number) value).intValue() != 0;
        }
        return null;
    }
    
    /**
     * Verifica si existe una clave
     */
    public boolean hasKey(String key) {
        return dataMap.containsKey(key);
    }
    
    /**
     * Obtiene todas las claves disponibles
     */
    public java.util.Set<String> getAllKeys() {
        return dataMap.keySet();
    }
    
    /**
     * Obtiene el mapa completo de datos (solo lectura)
     */
    public Map<String, Object> getDataMap() {
        return java.util.Collections.unmodifiableMap(dataMap);
    }
    
    /**
     * Incrementa contador de accesos para estadísticas
     */
    private void incrementAccessCount() {
        synchronized (accessLock) {
            totalAccessCount++;
            lastAccessTime = System.currentTimeMillis();
        }
    }
    
    /**
     * Obtiene estadísticas de acceso
     */
    public AccessStats getAccessStats() {
        synchronized (accessLock) {
            return new AccessStats(totalAccessCount, lastAccessTime);
        }
    }
    
    /**
     * Obtiene el tipo de configuración
     */
    public String getConfigType() {
        return configType;
    }
    
    /**
     * Obtiene el tamaño en bytes
     */
    public int getByteSize() {
        return byteSize;
    }
    
    /**
     * Obtiene el número de pares key-value
     */
    public int getKeyCount() {
        return dataMap.size();
    }
    
    /**
     * Estadísticas de acceso a configuración
     */
    public static class AccessStats {
        private final long totalAccessCount;
        private final long lastAccessTime;
        
        public AccessStats(long totalAccessCount, long lastAccessTime) {
            this.totalAccessCount = totalAccessCount;
            this.lastAccessTime = lastAccessTime;
        }
        
        public long getTotalAccessCount() {
            return totalAccessCount;
        }
        
        public long getLastAccessTime() {
            return lastAccessTime;
        }
        
        public String getFormattedLastAccessTime() {
            return lastAccessTime > 0 ? 
                new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS")
                    .format(new java.util.Date(lastAccessTime)) : "Nunca";
        }
        
        @Override
        public String toString() {
            return String.format("AccessStats{total=%d, last=%s}", 
                totalAccessCount, getFormattedLastAccessTime());
        }
    }
    
    @Override
    public String toString() {
        return String.format("MappedConfigData{type=%s, keys=%d, bytes=%d, accesses=%d}", 
            configType, getKeyCount(), byteSize, totalAccessCount);
    }
}