package io.warmup.framework.core.optimized;

import io.warmup.framework.annotation.Component;
import io.warmup.framework.core.*;
import io.warmup.framework.event.EventBus;
import io.warmup.framework.event.EventPublisher;
import io.warmup.framework.health.*;
import io.warmup.framework.metrics.*;
import io.warmup.framework.cache.ASMCacheManager;
import io.warmup.framework.config.PropertySource;

import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentHashMap.KeySetView;
import java.util.Set;
import java.util.Map;
import java.util.Collections;
import java.util.HashSet;
import java.util.Arrays;
import java.util.logging.Logger;
import java.util.logging.Level;
import java.lang.ref.WeakReference;

/**
 * 🚀 CORE CONTAINER - Extracted core logic from WarmupContainer
 * 
 * This class contains the core business logic separated from infrastructure concerns.
 * Provides O(1) performance optimizations and clean separation of responsibilities.
 * 
 * Key Features:
 * - O(1) dependency resolution and instance tracking
 * - Weak reference based memory management
 * - Atomic counter based performance metrics
 * - Clean separation from infrastructure concerns
 * 
 * @author Warmup Framework
 * @version 2.0
 */
@Component
public class CoreContainer implements IContainer {
    
    private static final Logger log = Logger.getLogger(CoreContainer.class.getName());
    
    // ✅ CORE MANAGERS - Lightweight references
    private final DependencyRegistry dependencyRegistry;
    private final AopHandler aopHandler;
    private final EventManager eventManager;
    private final AsyncHandler asyncHandler;
    private final ShutdownManager shutdownManager;
    private final ProfileManager profileManager;
    private final ModuleManager moduleManager;
    private final HealthCheckManager healthCheckManager;
    private final MetricsManager metricsManager;
    private final ASMCacheManager cacheManager;
    private final WebScopeContext webScopeContext;
    
    // ✅ PERFORMANCE COUNTERS - O(1) atomic operations
    private final AtomicLong startupTime = new AtomicLong(System.currentTimeMillis());
    private final Map<Class<?>, AtomicLong> resolutionTimes = new ConcurrentHashMap<>();
    
    // ✅ PHASE 3: O(1) Active Instance Tracking - Direct atomic counters
    private final Map<Class<?>, AtomicInteger> activeInstanceCounts = new ConcurrentHashMap<>();
    private final Map<String, AtomicInteger> activeNamedInstanceCounts = new ConcurrentHashMap<>();
    private final Set<Class<?>> activeInstanceTypes = ConcurrentHashMap.newKeySet();
    private final Set<String> activeNamedInstanceNames = ConcurrentHashMap.newKeySet();
    
    // ✅ PHASE 3: Total Active Instance Counters - O(1) direct access
    private final AtomicLong totalActiveInstanceCount = new AtomicLong(0);
    private final AtomicLong totalActiveNamedInstanceCount = new AtomicLong(0);
    
    // ✅ WEAK REFERENCE REGISTRIES - Memory efficient instance tracking
    private final Map<Class<?>, WeakReference<Object>> weakInstanceRegistry = new ConcurrentHashMap<>();
    private final Map<String, WeakReference<Object>> weakNamedInstanceRegistry = new ConcurrentHashMap<>();
    
    // ✅ PERFORMANCE OPTIMIZATIONS
    private final Map<Class<?>, Boolean> registrationCache = new ConcurrentHashMap<>();
    private final Map<String, Boolean> namedRegistrationCache = new ConcurrentHashMap<>();
    
    // 🚨 CIRCULAR DEPENDENCY PROTECTION
    private static final Set<Class<?>> PROTECTED_CONTAINER_TYPES = Collections.unmodifiableSet(
        new HashSet<>(Arrays.asList(
            io.warmup.framework.core.optimized.CoreContainer.class,
            io.warmup.framework.core.optimized.ContainerCoordinator.class,
            io.warmup.framework.core.WarmupContainer.class,
            io.warmup.framework.core.DependencyRegistry.class
        ))
    );
    
    public CoreContainer(DependencyRegistry dependencyRegistry,
                        AopHandler aopHandler,
                        EventManager eventManager,
                        AsyncHandler asyncHandler,
                        ShutdownManager shutdownManager,
                        ProfileManager profileManager,
                        ModuleManager moduleManager,
                        HealthCheckManager healthCheckManager,
                        MetricsManager metricsManager,
                        ASMCacheManager cacheManager) {
        
        this.dependencyRegistry = dependencyRegistry;
        this.aopHandler = aopHandler;
        this.eventManager = eventManager;
        this.asyncHandler = asyncHandler;
        this.shutdownManager = shutdownManager;
        this.profileManager = profileManager;
        this.moduleManager = moduleManager;
        this.healthCheckManager = healthCheckManager;
        this.metricsManager = metricsManager;
        this.cacheManager = cacheManager;
        this.webScopeContext = new WebScopeContext(null); // Temp null, will be set later
        
        log.info("🚀 CoreContainer initialized with O(1) optimizations");
    }
    
    /**
     * 🚀 PHASE 3: O(1) Dependency Resolution
     * Replaces expensive stream operations with direct hash lookups
     */
    @SuppressWarnings("unchecked")
    public <T> T getInstance(Class<T> type) {
        long startTime = System.nanoTime();
        boolean success = false;
        
        // 🔍 [DEBUG] Add detailed logging for EventBus retrieval
        if (type == EventBus.class) {
            java.util.logging.Logger.getLogger(CoreContainer.class.getName()).info(
                "🔍 [DEBUG] CoreContainer.getInstance(EventBus) called - checking dependency registry"
            );
        }
        
        try {
            // ✅ O(1) Singleton cache check
            Dependency dependency = dependencyRegistry.getDependencies().get(type);
            
            // 🔍 [DEBUG] Log dependency check result
            if (type == EventBus.class) {
                java.util.logging.Logger.getLogger(CoreContainer.class.getName()).info(
                    "🔍 [DEBUG] dependencyRegistry.getDependencies().get(EventBus.class) = " + 
                    (dependency != null ? dependency.toString() : "NULL")
                );
                if (dependency != null) {
                    java.util.logging.Logger.getLogger(CoreContainer.class.getName()).info(
                        "🔍 [DEBUG] isInstanceCreated = " + dependency.isInstanceCreated() + 
                        ", shouldCacheInstance = " + dependency.shouldCacheInstance()
                    );
                    if (dependency.isInstanceCreated()) {
                        Object cachedInstance = dependency.getCachedInstance();
                        java.util.logging.Logger.getLogger(CoreContainer.class.getName()).info(
                            "🔍 [DEBUG] getCachedInstance() = " + 
                            (cachedInstance != null ? cachedInstance.getClass().getName() + "@" + Integer.toHexString(System.identityHashCode(cachedInstance)) : "NULL")
                        );
                    }
                }
            }
            if (dependency != null && dependency.isInstanceCreated() && dependency.shouldCacheInstance()) {
                Object cachedInstance = dependency.getCachedInstance();
                if (cachedInstance != null) {
                    success = true;
                    return (T) cachedInstance;
                }
            }
            
            // ✅ Enhanced interface-to-implementation resolution
            if (type.isInterface()) {
                try {
                    java.util.logging.Logger.getLogger(CoreContainer.class.getName()).info(
                        "🔍 [DEBUG] Calling dependencyRegistry.getBestImplementation(" + type.getName() + ") for interface"
                    );
                    return dependencyRegistry.getBestImplementation(type);
                } catch (IllegalStateException e) {
                    // Re-throw IllegalStateException as-is (e.g., multiple @Primary beans with same priority)
                    throw e;
                } catch (Exception e) {
                    java.util.logging.Logger.getLogger(CoreContainer.class.getName()).warning(
                        "❌ [DEBUG] getBestImplementation failed for " + type.getName() + ": " + e.getMessage()
                    );
                    throw new IllegalArgumentException("No implementation found for interface: " + type.getName(), e);
                }
            }
            
            // ✅ Create instance with AOP
            if (dependency == null) {
                throw new IllegalArgumentException("No dependency registered for: " + type.getName());
            }
            
            if (!dependency.isInstanceCreated()) {
                T instance = createInstance(type, dependency);
                if (dependency.isSingleton()) {
                    dependency.setInstance(instance);
                    registerInstance(dependency, instance);
                }
                success = true;
                return instance;
            }
            
            success = true;
            return (T) dependency.getCachedInstance();
            
        } finally {
            long duration = System.nanoTime() - startTime;
            metricsManager.getContainerMetrics().recordRequest(success);
            resolutionTimes.computeIfAbsent(type, k -> new AtomicLong()).addAndGet(duration);
        }
    }
    
    /**
     * 🚀 Create instance with enhanced error handling and performance tracking
     */
    @SuppressWarnings("unchecked")
    private <T> T createInstance(Class<T> type, Dependency dependency) {
        try {
            // ✅ NULL CHECK: Handle case where dependency is not registered
            if (dependency == null) {
                throw new IllegalArgumentException("No dependency registered for class: " + type.getName() + 
                    ". Make sure the class is registered with container.register() or has @Component annotation.");
            }
            
            // 🚨 CIRCULAR DEPENDENCY PROTECTION: Skip JIT optimization for container types
            if (PROTECTED_CONTAINER_TYPES.contains(type)) {
                log.log(Level.FINEST, "🛡️ Skipping JIT optimization for container type: {0}", type.getSimpleName());
                // Skip JIT optimization and fall through to reflection
            } else if (dependencyRegistry.isJitOptimized(type)) {
                return dependencyRegistry.getInstanceJitOptimized(type);
            }
            
            // ✅ Fallback to reflection with proper error handling
            Object instance = dependency.getInstance(this, new HashSet<>());
            
            if (type.isInstance(instance)) {
                return (T) instance;
            } else {
                throw new ClassCastException("Cannot cast instance of " + instance.getClass().getName() + " to " + type.getName());
            }
            
        } catch (Exception e) {
            throw new RuntimeException("Failed to create instance of: " + type.getName(), e);
        }
    }
    
    /**
     * 🚀 PHASE 3: O(1) Instance Registration
     * Direct atomic operations for performance
     */
    private void registerInstance(Dependency dependency, Object instance) {
        if (instance == null) {
            return;
        }
        
        Class<?> type = dependency.getType();
        activeInstanceTypes.add(type);
        
        // ✅ O(1) Counter update
        activeInstanceCounts.computeIfAbsent(type, k -> new AtomicInteger())
            .incrementAndGet();
        
        // ✅ O(1) Total counter update  
        totalActiveInstanceCount.incrementAndGet();
        
        // ✅ Memory efficient weak reference
        weakInstanceRegistry.put(type, new WeakReference<>(instance));
    }
    
    /**
     * 🚀 PHASE 3: O(1) Active Instance Count
     * Direct atomic counter access - eliminates stream O(n)
     */
    public int getActiveInstancesCount() {
        long total = totalActiveInstanceCount.get() + totalActiveNamedInstanceCount.get();
        return (int) total;
    }
    
    /**
     * 🚀 PHASE 3: O(1) Dependencies Info
     * Cache-based statistics for expensive calculations
     */
    public Map<String, Object> getDependencyStats() {
        Map<String, Object> stats = new ConcurrentHashMap<>();
        
        stats.put("totalDependencies", dependencyRegistry.getDependencies().size());
        stats.put("totalNamedDependencies", dependencyRegistry.getNamedDependencies().size());
        stats.put("activeInstances", getActiveInstancesCount());
        stats.put("activeInstanceTypes", activeInstanceTypes.size());
        stats.put("activeNamedInstanceNames", activeNamedInstanceNames.size());
        
        return stats;
    }
    
    /**
     * 🚀 Get optimized performance metrics
     */
    public Map<String, Object> getPerformanceMetrics() {
        Map<String, Object> metrics = new ConcurrentHashMap<>();
        
        metrics.put("uptime", System.currentTimeMillis() - startupTime.get());
        metrics.put("resolutionTimes", resolutionTimes.size());
        metrics.put("activeInstanceCounts", activeInstanceCounts.size());
        metrics.put("totalActiveInstances", getActiveInstancesCount());
        metrics.put("weakRegistrySize", weakInstanceRegistry.size());
        metrics.put("namedWeakRegistrySize", weakNamedInstanceRegistry.size());
        
        return metrics;
    }
    
    /**
     * 🚀 Memory efficient instance retrieval
     */
    @SuppressWarnings("unchecked")
    public <T> T getActiveInstance(Class<T> type) {
        WeakReference<Object> ref = weakInstanceRegistry.get(type);
        return ref != null ? (T) ref.get() : null;
    }
    
    /**
     * 🚀 Clear performance caches (for testing)
     */
    public void clearCaches() {
        resolutionTimes.clear();
        activeInstanceCounts.clear();
        activeNamedInstanceCounts.clear();
        activeInstanceTypes.clear();
        activeNamedInstanceNames.clear();
        totalActiveInstanceCount.set(0);
        totalActiveNamedInstanceCount.set(0);
        weakInstanceRegistry.clear();
        weakNamedInstanceRegistry.clear();
        registrationCache.clear();
        namedRegistrationCache.clear();
        
        log.info("🧹 All performance caches cleared");
    }
    
    // === GETTERS FOR MANAGERS ===
    
    public DependencyRegistry getDependencyRegistry() {
        return dependencyRegistry;
    }
    
    public AopHandler getAopHandler() {
        return aopHandler;
    }
    
    public EventManager getEventManager() {
        return eventManager;
    }
    
    public AsyncHandler getAsyncHandler() {
        return asyncHandler;
    }
    
    public ShutdownManager getShutdownManager() {
        return shutdownManager;
    }
    
    public ProfileManager getProfileManager() {
        return profileManager;
    }
    
    public ModuleManager getModuleManager() {
        return moduleManager;
    }
    
    public HealthCheckManager getHealthCheckManager() {
        return healthCheckManager;
    }
    
    public MetricsManager getMetricsManager() {
        return metricsManager;
    }
    
    public ASMCacheManager getCacheManager() {
        return cacheManager;
    }

    public WebScopeContext getWebScopeContext() {
        return webScopeContext;
    }
    
    public long getStartupTime() {
        return startupTime.get();
    }
    
    // === IMPLEMENTACIÓN DE IContainer ===
    
    @Override
    @SuppressWarnings("unchecked")
    public <T> T getDependency(Class<T> type, Set<Class<?>> dependencyChain) throws Exception {
        // ✅ Enhanced interface-to-implementation resolution for dependency injection
        if (type.isInterface()) {
            return dependencyRegistry.getBestImplementation(type);
        }
        return (T) createInstance(type, dependencyRegistry.getDependencies().get(type));
    }
    
    @Override
    public <T> T getNamedDependency(Class<T> type, String name, Set<Class<?>> dependencyChain) throws Exception {
        // TODO: Implementar resolución de dependencias con nombre para CoreContainer
        throw new UnsupportedOperationException("Named dependencies not yet implemented in CoreContainer");
    }
    
    @Override
    public String resolvePropertyValue(String expression) {
        // TODO: Implementar resolución de propiedades para CoreContainer
        // Por ahora retornar la expresión como está (fallback)
        return expression != null ? expression : "";
    }
    
    @Override
    @SuppressWarnings("unchecked")
    public <T> T getBestImplementation(Class<T> interfaceType) throws Exception {
        return (T) dependencyRegistry.getBestImplementation(interfaceType);
    }
    
    @Override
    public void registerEventListeners(Class<?> clazz, Object instance) {
        // TODO: Implementar registro de eventos para CoreContainer
        // Por ahora no hacer nada (placeholder)
    }
    
    // === MÉTODOS ADICIONALES PARA IContainer ===
    
    @Override
    @SuppressWarnings("unchecked")
    public <T> T get(Class<T> type) {
        try {
            return getDependency(type, new HashSet<>());
        } catch (Exception e) {
            throw new RuntimeException("Error getting dependency: " + type.getName(), e);
        }
    }
    
    @Override
    @SuppressWarnings("unchecked")
    public <T> T getNamed(Class<T> type, String name) {
        try {
            return getNamedDependency(type, name, new HashSet<>());
        } catch (Exception e) {
            throw new RuntimeException("Error getting named dependency: " + type.getName() + " with name: " + name, e);
        }
    }
    
    @Override
    public <T> void register(Class<T> type, boolean singleton) {
        // TODO: Implementar registro para CoreContainer
        throw new UnsupportedOperationException("Registration not yet implemented in CoreContainer");
    }
    
    @Override
    public <T> void registerNamed(Class<T> type, String name, boolean singleton) {
        // TODO: Implementar registro con nombre para CoreContainer
        throw new UnsupportedOperationException("Named registration not yet implemented in CoreContainer");
    }
    
    // === ADDITIONAL UTILITY METHODS ===
    
    /**
     * 🎯 Get dependencies set (Legacy compatibility)
     */
    public Set<Class<?>> getDependencies() {
        return dependencyRegistry.getDependencies().keySet();
    }
    
    /**
     * 🎯 Get aspects set (Legacy compatibility)
     */
    public Set<Object> getAspects() {
        // Basic implementation - would need proper AspectManager
        return new HashSet<>();
    }
    
    /**
     * 🎯 Get property source (Legacy compatibility)
     */
    public Object getPropertySource() {
        // Basic implementation - would need proper PropertySource
        return new Object();
    }
    
    /**
     * 🎯 Methods already exist - no duplicates needed
     */
    
    // ============ MÉTODOS FALTANTES PARA MAVEN COMPILATION ============
    
    /**
     * Check if has binding for type and name
     */
    public boolean hasBinding(Class<?> type, String name) {
        return dependencyRegistry.hasBinding(type, name);
    }
}