package io.warmup.framework.examples.services;

/**
 * Interfaz para servicios de cache.
 */
public interface CacheService {
    void put(String key, Object value);
    Object get(String key);
    void remove(String key);
    String getInfo();
}