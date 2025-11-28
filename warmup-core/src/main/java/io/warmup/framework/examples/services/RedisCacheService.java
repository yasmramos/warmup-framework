package io.warmup.framework.examples.services;

/**
 * Implementación de cache usando Redis (simulado).
 */
public class RedisCacheService implements CacheService {
    
    private final String redisUrl;
    
    public RedisCacheService() {
        this.redisUrl = "localhost:6379";
    }
    
    public RedisCacheService(String redisUrl) {
        this.redisUrl = redisUrl;
    }
    
    @Override
    public void put(String key, Object value) {
        System.out.println("RedisCache: SET " + key + " = " + value);
    }
    
    @Override
    public Object get(String key) {
        System.out.println("RedisCache: GET " + key);
        return "redis_value_" + key;
    }
    
    @Override
    public void remove(String key) {
        System.out.println("RedisCache: DEL " + key);
    }
    
    @Override
    public String getInfo() {
        return "RedisCache - Distributed cache with Redis backend";
    }
}