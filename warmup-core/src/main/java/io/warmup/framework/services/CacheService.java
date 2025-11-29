package io.warmup.framework.services;

/**
 * Basic interface for cache operations.
 * Provides the core contract for cache implementations.
 * 
 * @author MiniMax Agent
 * @since 2.0
 */
public interface CacheService {
    
    /**
     * Put a value in the cache.
     * 
     * @param key the cache key
     * @param value the value to cache
     */
    void put(String key, Object value);
    
    /**
     * Get a value from the cache.
     * 
     * @param key the cache key
     * @return the cached value or null if not found
     */
    Object get(String key);
    
    /**
     * Remove a value from the cache.
     * 
     * @param key the cache key
     */
    void remove(String key);
}