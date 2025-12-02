package io.warmup.framework.core.optimized;

import io.warmup.framework.core.*;
import io.warmup.framework.config.PropertySource;
import io.warmup.framework.cache.ASMCacheManager;
import io.warmup.framework.health.HealthCheckManager;
import io.warmup.framework.metrics.MetricsManager;
import io.warmup.framework.core.ModuleManager;
import io.warmup.framework.core.ShutdownManager;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.Logger;
import java.util.Set;
import java.util.Map;

/**
 * 🚀 MANAGER FACTORY - Optimized manager creation and lifecycle management
 * 
 * This factory creates and manages all container managers with:
 * - Proper dependency injection
 * - Performance optimizations
 * - Lazy initialization support
 * - Memory-efficient caching
 * 
 * Features:
 * - O(1) manager retrieval after creation
 * - Thread-safe concurrent access
 * - Automatic dependency resolution
 * - Performance metrics tracking
 * 
 * @author Warmup Framework
 * @version 2.0
 */
public class ManagerFactory {
    
    private static final Logger log = Logger.getLogger(ManagerFactory.class.getName());
    
    // ✅ MANAGER REGISTRIES - O(1) access after creation
    private static final Map<Class<?>, Object> managerRegistry = new ConcurrentHashMap<>();
    private static final Map<Class<?>, Long> managerCreationTimes = new ConcurrentHashMap<>();
    
    // ✅ PERFORMANCE TRACKING
    private static final AtomicLong totalManagersCreated = new AtomicLong(0);
    private static final Map<Class<?>, Long> managerResolutionTimes = new ConcurrentHashMap<>();
    
    /**
     * 🚀 Get or create manager with proper dependency injection
     */
    @SuppressWarnings("unchecked")
    public static <T> T getManager(Class<T> managerType, Object... dependencies) {
        long startTime = System.nanoTime();
        
        try {
            // ✅ FAST PATH: Check registry first
            Object existing = managerRegistry.get(managerType);
            if (existing != null) {
                log.fine("🚀 Manager retrieved from registry: " + managerType.getSimpleName());
                return (T) existing;
            }
            
            // ✅ SLOW PATH: Create new manager
            T manager = createManager(managerType, dependencies);
            
            // ✅ Register for fast access
            managerRegistry.put(managerType, manager);
            totalManagersCreated.incrementAndGet();
            
            long creationTime = (System.nanoTime() - startTime) / 1_000_000;
            managerCreationTimes.put(managerType, creationTime);
            managerResolutionTimes.put(managerType, creationTime);
            
            log.log(java.util.logging.Level.FINE, "✅ Manager created: {0} in {1}ms", 
                   new Object[]{managerType.getSimpleName(), creationTime});
            
            return manager;
            
        } catch (Exception e) {
            log.log(java.util.logging.Level.SEVERE, 
                   "❌ Failed to create manager: " + managerType.getSimpleName(), e);
            throw new RuntimeException("Failed to create manager: " + managerType.getName(), e);
        }
    }
    
    /**
     * 🚀 Create manager with dependency injection
     */
    @SuppressWarnings("unchecked")
    private static <T> T createManager(Class<T> managerType, Object[] dependencies) {
        try {
            // ✅ Handle DependencyRegistry with special dependency injection
            if (managerType == DependencyRegistry.class) {
                return (T) createDependencyRegistry(dependencies);
            }
            
            // ✅ Handle AopHandler
            if (managerType == AopHandler.class) {
                return (T) createAopHandler(dependencies);
            }
            
            // ✅ Handle EventManager  
            if (managerType == EventManager.class) {
                return (T) createEventManager();
            }
            
            // ✅ Handle AsyncHandler
            if (managerType == AsyncHandler.class) {
                return (T) createAsyncHandler(dependencies);
            }
            
            // ✅ Handle ShutdownManager
            if (managerType == ShutdownManager.class) {
                return (T) createShutdownManager(dependencies);
            }
            
            // ✅ Handle ProfileManager
            if (managerType == ProfileManager.class) {
                return (T) createProfileManager(dependencies);
            }
            
            // ✅ Handle ModuleManager
            if (managerType == ModuleManager.class) {
                return (T) createModuleManager(dependencies);
            }
            
            // ✅ Handle HealthCheckManager
            if (managerType == HealthCheckManager.class) {
                return (T) createHealthCheckManager(dependencies);
            }
            
            // ✅ Handle MetricsManager
            if (managerType == MetricsManager.class) {
                return (T) createMetricsManager(dependencies);
            }
            
            // ✅ Handle WebScopeContext
            if (managerType == WebScopeContext.class) {
                return (T) createWebScopeContext(dependencies);
            }
            
            // ✅ Handle ConfigurationProcessor
            if (managerType == ConfigurationProcessor.class) {
                return (T) createConfigurationProcessor(dependencies);
            }
            
            throw new IllegalArgumentException("Unknown manager type: " + managerType.getName());
            
        } catch (Exception e) {
            throw new RuntimeException("Failed to create manager: " + managerType.getName(), e);
        }
    }
    
    // === MANAGER CREATION METHODS ===
    
    /**
     * 🚀 Create DependencyRegistry with optional container and property source
     * Container can be null initially to break circular dependency
     */
    private static DependencyRegistry createDependencyRegistry(Object[] dependencies) {
        WarmupContainer container = null;
        PropertySource propertySource = null;
        Set<String> activeProfiles = null;
        
        for (Object dep : dependencies) {
            if (dep instanceof WarmupContainer) {
                container = (WarmupContainer) dep;
            } else if (dep instanceof PropertySource) {
                propertySource = (PropertySource) dep;
            } else if (dep instanceof Set && !((Set<?>) dep).isEmpty() && 
                      ((Set<?>) dep).iterator().next() instanceof String) {
                activeProfiles = (Set<String>) dep;
            }
        }
        
        // Container is optional - can be set later to break circular dependency
        DependencyRegistry registry = new DependencyRegistry(container, propertySource, activeProfiles);
        
        // ✅ CRITICAL FIX: Set container reference if available to fix NPE in PrimaryAlternativeResolver
        if (container != null) {
            registry.setContainer(container);
        }
        
        return registry;
    }
    
    /**
     * 🚀 Create AopHandler with optional container
     * Container can be null initially to break circular dependency
     */
    private static AopHandler createAopHandler(Object[] dependencies) {
        WarmupContainer container = null;
        for (Object dep : dependencies) {
            if (dep instanceof WarmupContainer) {
                container = (WarmupContainer) dep;
                break;
            }
        }
        
        // Container is optional - can be set later to break circular dependency
        return new AopHandler(container);
    }
    
    /**
     * 🚀 Create EventManager (no dependencies)
     */
    private static EventManager createEventManager() {
        return new EventManager();
    }
    
    /**
     * 🚀 Create AsyncHandler with dependency registry
     */
    /**
     * 🚀 Create AsyncHandler with lazy initialization for DependencyRegistry
     */
    private static AsyncHandler createAsyncHandler(Object[] dependencies) {
        DependencyRegistry dependencyRegistry = null;
        for (Object dep : dependencies) {
            if (dep instanceof DependencyRegistry) {
                dependencyRegistry = (DependencyRegistry) dep;
                break;
            }
        }
        
        // ✅ Crear AsyncHandler con lazy initialization (puede ser null inicialmente)
        AsyncHandler asyncHandler = new AsyncHandler(null);
        
        // ✅ Configurar DependencyRegistry si está disponible (lazy setup)
        if (dependencyRegistry != null) {
            asyncHandler.setDependencyRegistry(dependencyRegistry);
        }
        
        return asyncHandler;
    }
    
    /**
     * 🚀 Create ShutdownManager with container and dependency registry
     */
    /**
     * 🚀 Create ShutdownManager with lazy initialization for container and dependency registry
     */
    private static ShutdownManager createShutdownManager(Object[] dependencies) {
        WarmupContainer container = null;
        DependencyRegistry dependencyRegistry = null;
        
        for (Object dep : dependencies) {
            if (dep instanceof WarmupContainer) {
                container = (WarmupContainer) dep;
            } else if (dep instanceof DependencyRegistry) {
                dependencyRegistry = (DependencyRegistry) dep;
            }
        }
        
        // ✅ Crear ShutdownManager con lazy initialization (pueden ser null inicialmente)
        ShutdownManager shutdownManager = new ShutdownManager(null, null);
        
        // ✅ Configurar container si está disponible (lazy setup)
        if (container != null) {
            shutdownManager.setContainer(container);
        }
        
        // ✅ Configurar dependencyRegistry si está disponible (lazy setup)
        if (dependencyRegistry != null) {
            shutdownManager.setDependencyRegistry(dependencyRegistry);
        }
        
        return shutdownManager;
    }
    
    /**
     * 🚀 Create ProfileManager with property source and profiles
     */
    private static ProfileManager createProfileManager(Object[] dependencies) {
        PropertySource propertySource = null;
        String[] profiles = null;
        
        for (Object dep : dependencies) {
            if (dep instanceof PropertySource) {
                propertySource = (PropertySource) dep;
            } else if (dep instanceof String[]) {
                profiles = (String[]) dep;
            }
        }
        
        return new ProfileManager(propertySource, profiles);
    }
    
    /**
     * 🚀 Create ModuleManager with container and property source
     */
    private static ModuleManager createModuleManager(Object[] dependencies) {
        WarmupContainer container = null;
        PropertySource propertySource = null;
        
        for (Object dep : dependencies) {
            if (dep instanceof WarmupContainer) {
                container = (WarmupContainer) dep;
            } else if (dep instanceof PropertySource) {
                propertySource = (PropertySource) dep;
            }
        }
        
        // ✅ Crear ModuleManager con lazy initialization (pueden ser null inicialmente)
        ModuleManager moduleManager = new ModuleManager(null, null);
        
        // ✅ Configurar container si está disponible (lazy setup)
        if (container != null) {
            moduleManager.setContainer(container);
        }
        
        // Note: propertySource no tiene setter, se pasa en el constructor
        // Pero para romper la dependencia circular, permitimos null inicialmente
        
        return moduleManager;
    }
    
    /**
     * 🚀 Create HealthCheckManager with container
     */
    private static HealthCheckManager createHealthCheckManager(Object[] dependencies) {
        WarmupContainer container = null;
        for (Object dep : dependencies) {
            if (dep instanceof WarmupContainer) {
                container = (WarmupContainer) dep;
                break;
            }
        }
        
        // ✅ Crear HealthCheckManager con lazy initialization (puede ser null inicialmente)
        HealthCheckManager healthCheckManager = new HealthCheckManager(null);
        
        // ✅ Configurar container si está disponible (lazy setup)
        if (container != null) {
            healthCheckManager.setContainer(container);
        }
        
        return healthCheckManager;
    }
    
    /**
     * 🚀 Create MetricsManager with container
     */
    private static MetricsManager createMetricsManager(Object[] dependencies) {
        WarmupContainer container = null;
        for (Object dep : dependencies) {
            if (dep instanceof WarmupContainer) {
                container = (WarmupContainer) dep;
                break;
            }
        }
        
        return new MetricsManager(container);
    }
    
    /**
     * 🚀 Create WebScopeContext with container
     */
    private static WebScopeContext createWebScopeContext(Object[] dependencies) {
        WarmupContainer container = null;
        for (Object dep : dependencies) {
            if (dep instanceof WarmupContainer) {
                container = (WarmupContainer) dep;
                break;
            }
        }
        
        if (container == null) {
            throw new IllegalArgumentException("WarmupContainer is required for WebScopeContext");
        }
        
        return new WebScopeContext(container);
    }
    
    /**
     * 🚀 Create ConfigurationProcessor with container
     */
    private static ConfigurationProcessor createConfigurationProcessor(Object[] dependencies) {
        WarmupContainer container = null;
        for (Object dep : dependencies) {
            if (dep instanceof WarmupContainer) {
                container = (WarmupContainer) dep;
                break;
            }
        }
        
        if (container == null) {
            throw new IllegalArgumentException("WarmupContainer is required for ConfigurationProcessor");
        }
        
        return new ConfigurationProcessor(container);
    }
    
    /**
     * 🚀 Preload common managers for better performance
     */
    public static void preloadCommonManagers() {
        log.info("🚀 Preloading common managers...");
        
        // Preload most frequently used managers
        getManager(DependencyRegistry.class);
        getManager(AopHandler.class);
        getManager(EventManager.class);
        getManager(AsyncHandler.class);
        
        log.info("✅ Common managers preloaded");
    }
    
    /**
     * 🚀 Get manager creation statistics
     */
    public static Map<String, Object> getManagerStats() {
        Map<String, Object> stats = new ConcurrentHashMap<>();
        
        stats.put("totalManagersCreated", totalManagersCreated.get());
        stats.put("registeredManagers", managerRegistry.size());
        stats.put("averageCreationTimeMs", calculateAverageCreationTime());
        
        // Individual manager times
        Map<String, Long> individualStats = new ConcurrentHashMap<>();
        for (Map.Entry<Class<?>, Long> entry : managerCreationTimes.entrySet()) {
            individualStats.put(entry.getKey().getSimpleName(), entry.getValue());
        }
        stats.put("individualCreationTimes", individualStats);
        
        return stats;
    }
    
    /**
     * 🚀 Calculate average creation time for all managers
     */
    private static double calculateAverageCreationTime() {
        if (managerCreationTimes.isEmpty()) {
            return 0.0;
        }
        
        long totalTime = managerCreationTimes.values().stream()
            .mapToLong(Long::longValue)
            .sum();
        
        return (double) totalTime / managerCreationTimes.size();
    }
    
    /**
     * 🚀 Clear all manager caches (for testing)
     */
    public static void clearCaches() {
        managerRegistry.clear();
        managerCreationTimes.clear();
        managerResolutionTimes.clear();
        totalManagersCreated.set(0);
        
        log.info("🧹 ManagerFactory caches cleared");
    }
    
    /**
     * 🚀 Check if manager exists in registry
     */
    public static boolean hasManager(Class<?> managerType) {
        return managerRegistry.containsKey(managerType);
    }
}