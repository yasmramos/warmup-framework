package io.warmup.framework.core;

import io.warmup.framework.annotation.*;
import io.warmup.framework.asm.AsmCoreUtils;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Central scope resolution system for handling different bean lifecycles.
 * 
 * This class provides static methods for:
 * - Determining the scope type of a bean
 * - Managing scope-specific bean instances
 * - Handling cleanup for scoped beans
 * 
 * Supported scopes:
 * - Singleton: One instance per container
 * - ApplicationScope: One instance per application (web)
 * - SessionScope: One instance per HTTP session
 * - RequestScope: One instance per HTTP request
 * - Prototype: New instance per injection
 * 
 * @author MiniMax Agent
 * @since 1.2
 */
public class ScopeManager {
    
    public enum ScopeType {
        SINGLETON("singleton"),
        APPLICATION_SCOPE("application"),
        SESSION_SCOPE("session"), 
        REQUEST_SCOPE("request"),
        PROTOTYPE("prototype");
        
        private final String value;
        
        ScopeType(String value) {
            this.value = value;
        }
        
        public String getValue() {
            return value;
        }
        
        @Override
        public String toString() {
            return value;
        }
    }
    
    // Cache for scope analysis per class
    private static final Map<Class<?>, ScopeInfo> scopeCache = new ConcurrentHashMap<>();
    
    /**
     * Gets the scope type for a given bean class.
     * 
     * @param beanClass the bean class
     * @return the ScopeType indicating the bean's lifecycle
     */
    public static ScopeType getScopeType(Class<?> beanClass) {
        if (beanClass == null) {
            return ScopeType.PROTOTYPE;
        }
        
        return getScopeInfo(beanClass).scopeType;
    }
    
    /**
     * Checks if a bean class is annotated with any scope annotation.
     * 
     * @param beanClass the bean class
     * @return true if the class has a scope annotation
     */
    public static boolean hasScopeAnnotation(Class<?> beanClass) {
        if (beanClass == null) {
            return false;
        }
        
        return getScopeInfo(beanClass).hasScopeAnnotation;
    }
    
    /**
     * Gets the scope name if specified, otherwise returns empty string.
     * 
     * @param beanClass the bean class
     * @return the scope name or empty string
     */
    public static String getScopeName(Class<?> beanClass) {
        if (beanClass == null) {
            return "";
        }
        
        return getScopeInfo(beanClass).scopeName;
    }
    
    /**
     * Determines if a scope should be managed by the container (vs. prototype).
     * 
     * @param beanClass the bean class
     * @return true if the scope should be container-managed
     */
    public static boolean isContainerManagedScope(Class<?> beanClass) {
        ScopeType scopeType = getScopeType(beanClass);
        return scopeType != ScopeType.PROTOTYPE;
    }
    
    /**
     * Gets the scope info for a bean class with caching.
     * 
     * @param beanClass the bean class
     * @return the ScopeInfo containing scope details
     */
    private static ScopeInfo getScopeInfo(Class<?> beanClass) {
        return scopeCache.computeIfAbsent(beanClass, ScopeManager::analyzeScope);
    }
    
    /**
     * Analyzes a class to determine its scope type and properties.
     * 
     * @param beanClass the class to analyze
     * @return ScopeInfo containing the analysis results
     */
    private static ScopeInfo analyzeScope(Class<?> beanClass) {
        if (beanClass == null) {
            return new ScopeInfo(ScopeType.PROTOTYPE, "", false);
        }
        
        // Check annotations in priority order
        if (beanClass.isAnnotationPresent(Singleton.class) || 
            beanClass.isAnnotationPresent(jakarta.inject.Singleton.class)) {
            return new ScopeInfo(ScopeType.SINGLETON, "", true);
        }
        
        if (beanClass.isAnnotationPresent(ApplicationScope.class)) {
            ApplicationScope scope = AsmCoreUtils.getAnnotationProgressive(beanClass, ApplicationScope.class);
            return new ScopeInfo(ScopeType.APPLICATION_SCOPE, scope != null ? scope.value() : "", true);
        }
        
        if (beanClass.isAnnotationPresent(SessionScope.class)) {
            SessionScope scope = AsmCoreUtils.getAnnotationProgressive(beanClass, SessionScope.class);
            return new ScopeInfo(ScopeType.SESSION_SCOPE, scope != null ? scope.value() : "", true);
        }
        
        if (beanClass.isAnnotationPresent(RequestScope.class)) {
            RequestScope scope = AsmCoreUtils.getAnnotationProgressive(beanClass, RequestScope.class);
            return new ScopeInfo(ScopeType.REQUEST_SCOPE, scope != null ? scope.value() : "", true);
        }
        
        // ✅ FIX: Use constructor-based logic to determine prototype vs singleton
        // Classes with constructor dependencies should be prototype scope
        if (hasConstructorWithInjectableParameters(beanClass)) {
            return new ScopeInfo(ScopeType.PROTOTYPE, "", false);
        }
        
        // Default to singleton for backward compatibility (existing behavior)
        return new ScopeInfo(ScopeType.SINGLETON, "", false);
    }
    
    /**
     * ✅ FIX: Verifies if a class has constructors with injectable parameters
     * This logic is used to determine prototype vs singleton scope
     */
    private static boolean hasConstructorWithInjectableParameters(Class<?> beanClass) {
        try {
            java.lang.reflect.Constructor<?>[] constructors = beanClass.getDeclaredConstructors();
            
            for (java.lang.reflect.Constructor<?> constructor : constructors) {
                if (constructor.getParameterCount() > 0) {
                    Class<?>[] paramTypes = constructor.getParameterTypes();
                    java.lang.annotation.Annotation[][] paramAnnotations = constructor.getParameterAnnotations();
                    
                    // Check if any parameter requires injection
                    for (int i = 0; i < paramTypes.length; i++) {
                        // If parameter has @Inject or is a class that likely needs injection
                        if (hasInjectAnnotation(paramAnnotations[i]) || isLikelyInjectableType(paramTypes[i])) {
                            return true;
                        }
                    }
                }
            }
            return false;
        } catch (Exception e) {
            // In case of error, assume no injectable parameters
            return false;
        }
    }
    
    /**
     * ✅ FIX: Checks if an annotation array contains @Inject
     */
    private static boolean hasInjectAnnotation(java.lang.annotation.Annotation[] annotations) {
        for (java.lang.annotation.Annotation annotation : annotations) {
            if (annotation.annotationType().equals(io.warmup.framework.annotation.Inject.class)) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * ✅ FIX: Checks if a type likely requires injection (not String, primitive, etc.)
     */
    private static boolean isLikelyInjectableType(Class<?> paramType) {
        // If it's a class (not primitive interface, not String, not basic type)
        return !paramType.isPrimitive() && 
               !paramType.equals(String.class) &&
               !paramType.equals(Integer.class) && 
               !paramType.equals(Long.class) &&
               !paramType.equals(Double.class) &&
               !paramType.equals(Float.class) &&
               !paramType.equals(Boolean.class) &&
               !paramType.equals(Character.class) &&
               !paramType.equals(Byte.class) &&
               !paramType.equals(Void.class);
    }
    
    /**
     * Cache structure for scope analysis results.
     */
    private static class ScopeInfo {
        final ScopeType scopeType;
        final String scopeName;
        final boolean hasScopeAnnotation;
        
        ScopeInfo(ScopeType scopeType, String scopeName, boolean hasScopeAnnotation) {
            this.scopeType = scopeType;
            this.scopeName = scopeName;
            this.hasScopeAnnotation = hasScopeAnnotation;
        }
    }
    
    /**
     * Gets cache statistics for monitoring and debugging.
     * 
     * @return map containing cache statistics
     */
    public static Map<String, Object> getCacheStatistics() {
        Map<String, Object> stats = new HashMap<>();
        
        Map<ScopeType, Long> scopeDistribution = new HashMap<>();
        long totalCached = 0;
        
        for (ScopeInfo info : scopeCache.values()) {
            scopeDistribution.merge(info.scopeType, 1L, Long::sum);
            totalCached++;
        }
        
        stats.put("total_cached_classes", totalCached);
        stats.put("scope_distribution", scopeDistribution);
        
        return stats;
    }
    
    /**
     * Clears the scope cache (useful for testing or memory management).
     */
    public static void clearCache() {
        scopeCache.clear();
    }
    
    /**
     * Clears all caches for testing purposes - ensures clean state between test runs.
     */
    public static void clearAllCachesForTesting() {
        clearCache();
    }
}