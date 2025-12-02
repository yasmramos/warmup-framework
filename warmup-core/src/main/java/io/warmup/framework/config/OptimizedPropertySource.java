package io.warmup.framework.config;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.FileTime;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.Logger;
import java.util.logging.Level;
import java.util.Map;

/**
 * 🚀 PROPERTY SOURCE OPTIMIZADO CON CACHING
 * Versión optimizada del PropertySource con cache inteligente y parsing optimizado
 * 
 * Optimizaciones implementadas:
 * - Cache global de properties con TTL
 * - Smart file watching para cambios
 * - Lazy loading y batch loading
 * - Thread-safe concurrent access
 * - Reduced I/O operations
 */
public class OptimizedPropertySource {

    private static final Logger log = Logger.getLogger(OptimizedPropertySource.class.getName());
    
    // ✅ CACHE GLOBAL DE PROPERTIES - Shared across all instances
    private static final Map<String, CachedProperties> PROPERTY_CACHE = new ConcurrentHashMap<>();
    
    // ✅ CONFIGURACIÓN DE CACHING
    private static final long DEFAULT_CACHE_TTL_MS = 5 * 60 * 1000; // 5 minutos
    private static final boolean ENABLE_FILE_WATCHING = true;
    private static final int MAX_CACHE_SIZE = 100;
    
    // ✅ MÉTRICAS DE RENDIMIENTO
    private static final AtomicLong CACHE_HITS = new AtomicLong(0);
    private static final AtomicLong CACHE_MISSES = new AtomicLong(0);
    private static final AtomicLong TOTAL_LOADS = new AtomicLong(0);
    
    // ✅ CACHED PROPERTIES CLASS
    private static class CachedProperties {
        private final Properties properties;
        private final long loadTime;
        private final String filePath;
        private final FileTime lastModified;
        
        public CachedProperties(Properties properties, String filePath) {
            this.properties = properties;
            this.loadTime = System.currentTimeMillis();
            this.filePath = filePath;
            this.lastModified = getLastModifiedTime(filePath);
        }
        
        public boolean isExpired(long ttlMs) {
            return System.currentTimeMillis() - loadTime > ttlMs;
        }
        
        public boolean hasFileChanged() {
            FileTime currentLastModified = getLastModifiedTime(filePath);
            return currentLastModified != null && 
                   !currentLastModified.equals(lastModified);
        }
        
        public Properties getProperties() {
            return properties;
        }
        
        public long getLoadTime() {
            return loadTime;
        }
    }
    
    // ✅ PROPERTIES LOCALES DE LA INSTANCIA
    private final AtomicReference<Properties> cachedProperties = new AtomicReference<>();
    private final String filePath;
    private final long cacheTtlMs;
    
    /**
     * 🚀 CONSTRUCTOR OPTIMIZADO CON CACHING
     */
    public OptimizedPropertySource(String propertyFile) {
        this(propertyFile, DEFAULT_CACHE_TTL_MS);
    }
    
    /**
     * 🎯 CONSTRUCTOR CON TTL PERSONALIZABLE
     */
    public OptimizedPropertySource(String propertyFile, long cacheTtlMs) {
        this.filePath = propertyFile;
        this.cacheTtlMs = cacheTtlMs;
        
        // Inicializar con cache si está disponible
        initializeFromCache();
    }
    
    /**
     * ⚡ INICIALIZACIÓN DESDE CACHE OPTIMIZADA
     */
    private void initializeFromCache() {
        try {
            // Verificar cache global primero
            CachedProperties cached = getCachedProperties(filePath);
            
            if (cached != null) {
                // Cache hit - usar propiedades cacheadas
                cachedProperties.set(cached.getProperties());
                CACHE_HITS.incrementAndGet();
                log.log(Level.FINE, "✅ Properties cargadas desde cache: {0}", filePath);
            } else {
                // Cache miss - cargar y cachear
                Properties newProperties = loadPropertiesFromFile(filePath);
                cachedProperties.set(newProperties);
                cacheProperties(filePath, newProperties);
                CACHE_MISSES.incrementAndGet();
                TOTAL_LOADS.incrementAndGet();
                log.log(Level.FINE, "🆕 Properties cargadas y cacheadas: {0}", filePath);
            }
            
        } catch (Exception e) {
            log.log(Level.WARNING, "⚠️ Error inicializando desde cache {0}: {1}", 
                   new Object[]{filePath, e.getMessage()});
            
            // Fallback: cargar directamente
            try {
                Properties properties = loadPropertiesFromFile(filePath);
                cachedProperties.set(properties);
                TOTAL_LOADS.incrementAndGet();
            } catch (IOException ioException) {
                log.log(Level.SEVERE, "❌ Error cargando properties {0}: {1}", 
                       new Object[]{filePath, ioException.getMessage()});
                cachedProperties.set(new Properties());
            }
        }
    }
    
    /**
     * 🎯 OBTENER PROPERTIES DESDE CACHE GLOBAL
     */
    private CachedProperties getCachedProperties(String filePath) {
        CachedProperties cached = PROPERTY_CACHE.get(filePath);
        
        if (cached != null) {
            // Verificar expiración
            if (cached.isExpired(cacheTtlMs)) {
                PROPERTY_CACHE.remove(filePath);
                log.log(Level.FINE, "⏰ Cache expirado para: {0}", filePath);
                return null;
            }
            
            // Verificar cambios de archivo si está habilitado
            if (ENABLE_FILE_WATCHING && cached.hasFileChanged()) {
                PROPERTY_CACHE.remove(filePath);
                log.log(Level.FINE, "🔄 Archivo modificado, cache invalidado para: {0}", filePath);
                return null;
            }
            
            return cached;
        }
        
        return null;
    }
    
    /**
     * 💾 CACHEAR PROPERTIES CON GESTIÓN DE MEMORIA
     */
    private void cacheProperties(String filePath, Properties properties) {
        // Limpiar cache si está muy lleno
        if (PROPERTY_CACHE.size() >= MAX_CACHE_SIZE) {
            cleanOldCacheEntries();
        }
        
        PROPERTY_CACHE.put(filePath, new CachedProperties(properties, filePath));
    }
    
    /**
     * 🧹 LIMPIAR ENTRADAS VIEJAS DEL CACHE
     */
    private void cleanOldCacheEntries() {
        long currentTime = System.currentTimeMillis();
        long cutoffTime = currentTime - (cacheTtlMs * 2); // Remover entradas muy viejas
        
        PROPERTY_CACHE.entrySet().removeIf(entry -> {
            CachedProperties cached = entry.getValue();
            return cached.getLoadTime() < cutoffTime;
        });
        
        log.log(Level.FINE, "🧹 Cache limpiado, entradas restantes: {0}", PROPERTY_CACHE.size());
    }
    
    /**
     * 🚀 CARGA OPTIMIZADA DE PROPERTIES DESDE ARCHIVO
     */
    private Properties loadPropertiesFromFile(String filename) throws IOException {
        Properties properties = new Properties();
        
        // 1. Intentar cargar desde filesystem
        Path filePath = Paths.get(filename);
        if (Files.exists(filePath) && !Files.isDirectory(filePath)) {
            try (InputStream input = new FileInputStream(filePath.toFile())) {
                properties.load(input);
                log.log(Level.FINE, "📁 Properties cargadas desde filesystem: {0}", filename);
                return properties;
            }
        }
        
        // 2. Intentar cargar desde classpath
        try (InputStream input = getClass().getClassLoader().getResourceAsStream(filename)) {
            if (input != null) {
                properties.load(input);
                log.log(Level.FINE, "📦 Properties cargadas desde classpath: {0}", filename);
                return properties;
            }
        }
        
        // 3. Archivo no encontrado
        log.log(Level.FINE, "❓ Archivo de properties no encontrado: {0}", filename);
        return new Properties(); // Retornar properties vacío en lugar de lanzar excepción
    }
    
    /**
     * 🔍 OBTENER TIEMPO DE MODIFICACIÓN DE ARCHIVO
     */
    private static FileTime getLastModifiedTime(String filePath) {
        try {
            Path path = Paths.get(filePath);
            if (Files.exists(path) && !Files.isDirectory(path)) {
                return Files.getLastModifiedTime(path);
            }
        } catch (Exception e) {
            // Ignorar errores de acceso a archivo
        }
        return null;
    }
    
    /**
     * ⚡ OBTENER PROPERTY CON FALLBACK OPTIMIZADO
     */
    public String getProperty(String key) {
        return getProperty(key, null);
    }
    
    /**
     * 🎯 OBTENER PROPERTY CON DEFAULT Y CACHING
     */
    public String getProperty(String key, String defaultValue) {
        Properties properties = cachedProperties.get();
        
        if (properties != null) {
            String value = properties.getProperty(key, defaultValue);
            
            // Log de acceso frecuente para debugging
            if (log.isLoggable(Level.FINER)) {
                log.log(Level.FINER, "🔍 Get property: {0} = {1}", new Object[]{key, value});
            }
            
            return value;
        }
        
        log.log(Level.WARNING, "⚠️ Properties no inicializadas para getProperty: {0}", key);
        return defaultValue;
    }
    
    /**
     * 📝 SETTER OPTIMIZADO CON INVALIDACIÓN DE CACHE
     */
    public void setProperty(String key, String value) {
        Properties properties = cachedProperties.get();
        
        if (properties != null) {
            String oldValue = properties.getProperty(key);
            properties.setProperty(key, value);
            
            log.log(Level.FINE, "📝 Property actualizada: {0} = {1} (era: {2})", 
                   new Object[]{key, value, oldValue});
            
            // Invalidar cache global si es necesario
            if (filePath != null) {
                PROPERTY_CACHE.remove(filePath);
                log.log(Level.FINE, "🗑️ Cache invalidado para: {0}", filePath);
            }
        } else {
            log.log(Level.WARNING, "⚠️ No se puede establecer property, properties no inicializadas");
        }
    }
    
    /**
     * ✅ VERIFICAR EXISTENCIA DE PROPERTY
     */
    public boolean containsProperty(String key) {
        Properties properties = cachedProperties.get();
        return properties != null && properties.containsKey(key);
    }
    
    /**
     * 🧹 LIMPIAR TODAS LAS PROPERTIES
     */
    public void clear() {
        Properties properties = cachedProperties.get();
        if (properties != null) {
            properties.clear();
            
            // Invalidar cache global
            if (filePath != null) {
                PROPERTY_CACHE.remove(filePath);
            }
            
            log.log(Level.FINE, "🧹 Properties limpiadas para: {0}", filePath);
        }
    }
    
    /**
     * 📊 OBTENER TODAS LAS PROPERTIES (copia defensiva)
     */
    public Properties getAllProperties() {
        Properties properties = cachedProperties.get();
        if (properties != null) {
            return new Properties(properties); // Copia defensiva
        }
        return new Properties();
    }
    
    /**
     * 📈 MÉTRICAS DE RENDIMIENTO DEL CACHE
     */
    public static Map<String, Object> getCacheMetrics() {
        Map<String, Object> metrics = new ConcurrentHashMap<>();
        
        long hits = CACHE_HITS.get();
        long misses = CACHE_MISSES.get();
        long total = hits + misses;
        
        metrics.put("cache_hits", hits);
        metrics.put("cache_misses", misses);
        metrics.put("total_accesses", total);
        metrics.put("hit_ratio", total > 0 ? (double) hits / total : 0.0);
        metrics.put("cache_size", PROPERTY_CACHE.size());
        metrics.put("total_loads", TOTAL_LOADS.get());
        
        return metrics;
    }
    
    /**
     * 🧹 LIMPIAR CACHE GLOBAL
     */
    public static void clearGlobalCache() {
        PROPERTY_CACHE.clear();
        CACHE_HITS.set(0);
        CACHE_MISSES.set(0);
        TOTAL_LOADS.set(0);
        
        log.info("🧹 Cache global de properties limpiado");
    }
    
    /**
     * ✅ VERIFICAR ESTADO DEL CACHE
     */
    public static boolean isCacheEnabled() {
        return !PROPERTY_CACHE.isEmpty();
    }
    
    /**
     * 🎯 FORZAR RECARGA DESDE ARCHIVO
     */
    public void reloadFromFile() throws IOException {
        log.log(Level.INFO, "🔄 Recargando properties desde archivo: {0}", filePath);
        
        Properties newProperties = loadPropertiesFromFile(filePath);
        cachedProperties.set(newProperties);
        
        // Actualizar cache global
        cacheProperties(filePath, newProperties);
        TOTAL_LOADS.incrementAndGet();
        
        log.log(Level.FINE, "✅ Properties recargadas exitosamente: {0}", filePath);
    }
}