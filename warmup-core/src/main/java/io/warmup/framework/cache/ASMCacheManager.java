package io.warmup.framework.cache;

import java.io.Serializable;
import java.security.MessageDigest;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * ASM Cache Manager para el framework Warmup.
 * Gestiona el cache de bytecode generado por ASM para optimizar el rendimiento JIT.
 * 
 * @author MiniMax Agent
 * @since 2.0
 */
public class ASMCacheManager implements Serializable {
    
    private static final Logger log = Logger.getLogger(ASMCacheManager.class.getName());
    private static final long serialVersionUID = 1L;
    
    private static ASMCacheManager instance;
    
    // Cache principal: cacheKey -> CacheEntry
    private final ConcurrentMap<String, CacheEntry> bytecodeCache;
    
    // Estadísticas del cache
    private long hits = 0;
    private long misses = 0;
    private long entries = 0;
    
    /**
     * Constructor privado para patrón Singleton
     */
    private ASMCacheManager() {
        this.bytecodeCache = new ConcurrentHashMap<>();
        log.log(Level.INFO, "ASMCacheManager inicializado");
    }
    
    /**
     * Obtiene la instancia singleton del ASMCacheManager
     */
    public static synchronized ASMCacheManager getInstance() {
        if (instance == null) {
            instance = new ASMCacheManager();
        }
        return instance;
    }
    
    /**
     * Obtiene la instancia singleton del ASMCacheManager con configuración
     */
    public static synchronized ASMCacheManager getInstance(CacheConfig config) {
        if (instance == null) {
            instance = new ASMCacheManager();
        }
        return instance;
    }
    
    /**
     * Calcula el hash del código fuente original
     */
    public String calculateSourceHash(byte[] originalClassData) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(originalClassData);
            
            // Convertir a hexadecimal
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (Exception e) {
            log.log(Level.WARNING, "Error calculando hash de fuente: {0}", e.getMessage());
            return "error-hash";
        }
    }
    
    /**
     * Obtiene el bytecode cacheado para una clave específica
     */
    public byte[] getCachedBytecode(String cacheKey, String sourceHash) {
        CacheEntry entry = bytecodeCache.get(cacheKey);
        if (entry == null) {
            misses++;
            return null;
        }
        
        // Verificar si el hash coincide (invalidación por cambios en el código fuente)
        if (!entry.getSourceHash().equals(sourceHash)) {
            bytecodeCache.remove(cacheKey);
            entries--;
            misses++;
            return null;
        }
        
        hits++;
        log.log(Level.FINE, "Cache HIT para clave: {0}", cacheKey);
        return entry.getBytecode();
    }
    
    /**
     * Guarda bytecode en el cache
     */
    public void cacheBytecode(String cacheKey, String sourceHash, byte[] bytecode) {
        CacheEntry entry = new CacheEntry(sourceHash, bytecode);
        CacheEntry previous = bytecodeCache.put(cacheKey, entry);
        if (previous == null) {
            entries++;
        }
        
        log.log(Level.FINE, "Cache guardado para clave: {0}, entradas totales: {1}", 
                new Object[]{cacheKey, entries});
    }
    
    /**
     * Invalida una entrada específica del cache
     */
    public void invalidate(String cacheKey) {
        CacheEntry removed = bytecodeCache.remove(cacheKey);
        if (removed != null) {
            entries--;
            log.log(Level.FINE, "Entrada invalidada del cache: {0}", cacheKey);
        }
    }
    
    /**
     * Invalida todas las entradas que pertenezcan a un paquete específico
     */
    public void invalidatePackage(String packageName) {
        int invalidCount = 0;
        
        for (String key : bytecodeCache.keySet()) {
            if (key != null && key.startsWith(packageName)) {
                CacheEntry removed = bytecodeCache.remove(key);
                if (removed != null) {
                    invalidCount++;
                }
            }
        }
        
        entries -= invalidCount;
        log.log(Level.INFO, "Paquete invalidado: {0}. Entradas eliminadas: {1}", 
                new Object[]{packageName, invalidCount});
    }
    
    /**
     * Limpia todo el cache
     */
    public void clearCache() {
        int cleared = bytecodeCache.size();
        bytecodeCache.clear();
        entries = 0;
        log.log(Level.INFO, "Cache limpiado completamente. Entradas eliminadas: {0}", cleared);
    }
    
    /**
     * Obtiene las estadísticas del cache
     */
    public void getStats() {
        long total = hits + misses;
        double hitRate = total > 0 ? (double) hits / total * 100 : 0;
        
        String stats = String.format(
            "ASMCacheManager Stats - Entries: %d, Hits: %d, Misses: %d, Hit Rate: %.2f%%",
            entries, hits, misses, hitRate
        );
        
        log.log(Level.INFO, stats);
    }
    
    /**
     * Obtiene estadísticas como objeto para uso programático
     */
    public CacheStats getCacheStats() {
        long total = hits + misses;
        double hitRate = total > 0 ? (double) hits / total * 100 : 0;
        
        return new CacheStats(entries, hits, misses, hitRate);
    }
    
    /**
     * Clase interna para representar una entrada del cache
     */
    private static class CacheEntry implements Serializable {
        private static final long serialVersionUID = 1L;
        
        private final String sourceHash;
        private final byte[] bytecode;
        private final long timestamp;
        
        public CacheEntry(String sourceHash, byte[] bytecode) {
            this.sourceHash = sourceHash;
            this.bytecode = bytecode;
            this.timestamp = System.currentTimeMillis();
        }
        
        public String getSourceHash() {
            return sourceHash;
        }
        
        public byte[] getBytecode() {
            return bytecode;
        }
        
        public long getTimestamp() {
            return timestamp;
        }
    }
    
    /**
     * Clase para representar estadísticas del cache
     */
    public static class CacheStats implements Serializable {
        private static final long serialVersionUID = 1L;
        
        private final long entries;
        private final long hits;
        private final long misses;
        private final double hitRate;
        
        public CacheStats(long entries, long hits, long misses, double hitRate) {
            this.entries = entries;
            this.hits = hits;
            this.misses = misses;
            this.hitRate = hitRate;
        }
        
        public long getEntries() {
            return entries;
        }
        
        public long getHits() {
            return hits;
        }
        
        public long getMisses() {
            return misses;
        }
        
        public double getHitRate() {
            return hitRate;
        }
        
        @Override
        public String toString() {
            return String.format(
                "CacheStats{entries=%d, hits=%d, misses=%d, hitRate=%.2f%%}",
                entries, hits, misses, hitRate
            );
        }
    }
}