package io.warmup.framework.validation;

import io.warmup.framework.annotation.validation.*;
import io.warmup.framework.validation.cache.ValidationCache;
import io.warmup.framework.validation.optimization.OptimizedReflectionUtil;

import java.lang.annotation.Annotation;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

/**
 * Optimized factory for creating and managing validator instances with performance enhancements.
 * Uses caching, reflection optimization, and lazy loading for improved performance.
 * 
 * @author MiniMax Agent
 * @since 1.0
 */
public class OptimizedValidatorFactory implements Validator {
    
    private static final Logger logger = Logger.getLogger(OptimizedValidatorFactory.class.getName());
    
    private final DefaultValidator delegate;
    private final ValidationCache cache;
    private final OptimizedReflectionUtil reflectionUtil;
    private final OptimizedValidatorConfig config;
    
    public OptimizedValidatorFactory() {
        this(new OptimizedValidatorConfig());
    }
    
    public OptimizedValidatorFactory(OptimizedValidatorConfig config) {
        this.delegate = new DefaultValidator();
        this.cache = new ValidationCache();
        this.reflectionUtil = new OptimizedReflectionUtil(cache);
        this.config = config;
        
        logger.info("OptimizedValidatorFactory initialized with config: " + config);
    }
    
    /**
     * Create a new lazy validator for async validation.
     * 
     * @return lazy validator instance
     */
    public LazyValidator createLazyValidator() {
        return new LazyValidator(delegate, config.isEnableParallelValidation());
    }
    
    /**
     * Get optimized field accessor for a class field.
     * 
     * @param clazz the class containing the field
     * @param fieldName the field name
     * @return optimized field accessor
     */
    public OptimizedReflectionUtil.FieldAccessor getFieldAccessor(Class<?> clazz, String fieldName) {
        return reflectionUtil.getFieldAccessor(clazz, fieldName);
    }
    
    /**
     * Get cached validation statistics.
     * 
     * @return cache statistics
     */
    public ValidationCache.CacheStatistics getCacheStatistics() {
        return cache.getStatistics();
    }
    
    /**
     * Clear all caches.
     */
    public void clearCaches() {
        cache.clearAll();
        reflectionUtil.clearCaches();
        logger.info("All caches cleared");
    }
    
    /**
     * Configuration for optimized validator factory.
     */
    public static class OptimizedValidatorConfig {
        private final boolean enableParallelValidation;
        private final boolean enableLazyValidation;
        private final boolean enableReflectionOptimization;
        private final boolean enablePatternCaching;
        private final int maxCacheSize;
        private final long cacheTimeoutMs;
        
        public OptimizedValidatorConfig() {
            this(true, true, true, true, 1000, 300000); // 5 minutes default timeout
        }
        
        public OptimizedValidatorConfig(boolean enableParallelValidation, boolean enableLazyValidation,
                                       boolean enableReflectionOptimization, boolean enablePatternCaching,
                                       int maxCacheSize, long cacheTimeoutMs) {
            this.enableParallelValidation = enableParallelValidation;
            this.enableLazyValidation = enableLazyValidation;
            this.enableReflectionOptimization = enableReflectionOptimization;
            this.enablePatternCaching = enablePatternCaching;
            this.maxCacheSize = maxCacheSize;
            this.cacheTimeoutMs = cacheTimeoutMs;
        }
        
        public boolean isEnableParallelValidation() { return enableParallelValidation; }
        public boolean isEnableLazyValidation() { return enableLazyValidation; }
        public boolean isEnableReflectionOptimization() { return enableReflectionOptimization; }
        public boolean isEnablePatternCaching() { return enablePatternCaching; }
        public int getMaxCacheSize() { return maxCacheSize; }
        public long getCacheTimeoutMs() { return cacheTimeoutMs; }
        
        @Override
        public String toString() {
            return String.format("OptimizedConfig{parallel=%s, lazy=%s, reflection=%s, patternCache=%s, maxSize=%d, timeout=%dms}",
                enableParallelValidation, enableLazyValidation, enableReflectionOptimization, 
                enablePatternCaching, maxCacheSize, cacheTimeoutMs);
        }
    }
    
    // Delegate to DefaultValidator for standard validation methods
    @Override
    public <T> java.util.List<ConstraintViolation<T>> validate(T object, Class<?>... validationGroups) {
        return delegate.validate(object, validationGroups);
    }
    
    @Override
    public <T> java.util.List<ConstraintViolation<T>> validateProperty(T object, String propertyName, Class<?>... validationGroups) {
        return delegate.validateProperty(object, propertyName, validationGroups);
    }
    
    @Override
    public <T> boolean isValid(T object, Class<?>... validationGroups) {
        return delegate.isValid(object, validationGroups);
    }
    
    @Override
    public <T> ViolationReport<T> getViolationReport(T object, Class<?>... validationGroups) {
        return delegate.getViolationReport(object, validationGroups);
    }
    
    /**
     * Get performance metrics for monitoring.
     * 
     * @return performance metrics
     */
    public PerformanceMetrics getPerformanceMetrics() {
        ValidationCache.CacheStatistics cacheStats = cache.getStatistics();
        OptimizedReflectionUtil.AccessorCacheStatistics accessorStats = reflectionUtil.getStatistics();
        
        return new PerformanceMetrics(
            cacheStats,
            accessorStats,
            config,
            System.currentTimeMillis()
        );
    }
    
    /**
     * Performance metrics holder.
     */
    public static class PerformanceMetrics {
        private final ValidationCache.CacheStatistics cacheStats;
        private final OptimizedReflectionUtil.AccessorCacheStatistics accessorStats;
        private final OptimizedValidatorConfig config;
        private final long timestamp;
        
        public PerformanceMetrics(ValidationCache.CacheStatistics cacheStats,
                                OptimizedReflectionUtil.AccessorCacheStatistics accessorStats,
                                OptimizedValidatorConfig config,
                                long timestamp) {
            this.cacheStats = cacheStats;
            this.accessorStats = accessorStats;
            this.config = config;
            this.timestamp = timestamp;
        }
        
        public ValidationCache.CacheStatistics getCacheStats() { return cacheStats; }
        public OptimizedReflectionUtil.AccessorCacheStatistics getAccessorStats() { return accessorStats; }
        public OptimizedValidatorConfig getConfig() { return config; }
        public long getTimestamp() { return timestamp; }
        
        public double getEfficiencyScore() {
            double cacheEfficiency = cacheStats.getHitRate();
            double accessorEfficiency = Math.min(1.0, (accessorStats.getFieldAccessorCount() + accessorStats.getMethodAccessorCount()) / 100.0);
            
            return (cacheEfficiency + accessorEfficiency) / 2.0;
        }
        
        @Override
        public String toString() {
            return String.format("PerformanceMetrics{efficiency=%.2f%%, cache=%s, accessors=%s}",
                getEfficiencyScore() * 100, cacheStats, accessorStats);
        }
    }
}