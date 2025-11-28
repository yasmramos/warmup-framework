package io.warmup.framework.examples.services;

import java.util.HashMap;
import java.util.Map;

/**
 * Implementación simple de cache en memoria.
 */
public class SimpleCacheService implements CacheService {
    private final Map<String, Object> cache = new HashMap<>();
    
    @Override
    public void put(String key, Object value) {
        cache.put(key, value);
        System.out.println("SimpleCache: Stored " + key);
    }
    
    @Override
    public Object get(String key) {
        Object value = cache.get(key);
        System.out.println("SimpleCache: Retrieved " + key + " = " + value);
        return value;
    }
    
    @Override
    public void remove(String key) {
        cache.remove(key);
        System.out.println("SimpleCache: Removed " + key);
    }
    
    @Override
    public String getInfo() {
        return "SimpleCache - Memory-based cache with " + cache.size() + " entries";
    }
}