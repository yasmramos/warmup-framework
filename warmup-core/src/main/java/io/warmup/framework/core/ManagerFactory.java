package io.warmup.framework.core;

import io.warmup.framework.config.PropertySource;
import io.warmup.framework.health.HealthCheckManager;
import io.warmup.framework.metrics.MetricsManager;
import io.warmup.framework.cache.ASMCacheManager;
import io.warmup.framework.cache.CacheConfig;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Logger;
import java.util.logging.Level;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

/**
 * 🚀 MANAGER FACTORY CON CACHING
 * Factory pattern con cache para crear y reutilizar managers del WarmupContainer
 * Elimina el overhead de reflexión y mejora significativamente el rendimiento baseline
 * 
 * Optimizaciones implementadas:
 * - Cache de managers creados (evita recreación)
 * - Lazy initialization de managers no críticos
 * - Singleton pattern para managers stateless
 * - Thread-safe concurrent access
 */
public final class ManagerFactory {
    
    private static final Logger log = Logger.getLogger(ManagerFactory.class.getName());
    
    // ✅ CACHE DE MANAGERS - ConcurrentHashMap para thread-safety
    private static final Map<Class<?>, Supplier<?>> MANAGER_CACHE = new ConcurrentHashMap<>();
    private static final Map<Class<?>, Object> SINGLETON_CACHE = new ConcurrentHashMap<>();
    
    // ✅ MANAGERS CRÍTICOS (se inicializan eagerly)
    private static final Class<?>[] CRITICAL_MANAGERS = {
        DependencyRegistry.class,
        AopHandler.class,
        EventManager.class,
        MetricsManager.class
    };
    
    // ✅ MANAGERS NO CRÍTICOS (lazy loading)
    private static final Class<?>[] LAZY_MANAGERS = {
        AsyncHandler.class,
        ShutdownManager.class,
        ProfileManager.class,
        ModuleManager.class,
        HealthCheckManager.class,
        ASMCacheManager.class,
        WebScopeContext.class,
        ConfigurationProcessor.class
    };
    
    /**
     * 🚀 OBTENER MANAGER CON LAZY LOADING INTELIGENTE
     * Implementa lazy loading basado en criticidad del manager
     */
    public static <T> T getManager(Class<T> managerClass, Object... dependencies) {
        try {
            // 1. Verificar cache de singletons primero (para managers stateless)
            if (isStatelessManager(managerClass)) {
                @SuppressWarnings("unchecked")
                T singleton = (T) SINGLETON_CACHE.get(managerClass);
                if (singleton != null) {
                    log.log(Level.FINE, "✅ Manager singleton cacheado: {0}", managerClass.getSimpleName());
                    return singleton;
                }
            }
            
            // 2. Verificar cache de suppliers
            @SuppressWarnings("unchecked")
            Supplier<T> cachedSupplier = (Supplier<T>) MANAGER_CACHE.get(managerClass);
            if (cachedSupplier != null) {
                T instance = cachedSupplier.get();
                
                // Cachear singleton si es stateless
                if (isStatelessManager(managerClass)) {
                    SINGLETON_CACHE.put(managerClass, instance);
                }
                
                log.log(Level.FINE, "✅ Manager desde cache: {0}", managerClass.getSimpleName());
                return instance;
            }
            
            // 3. Crear nuevo manager con factory optimizado
            T newManager = createManagerOptimized(managerClass, dependencies);
            
            // 4. Cachear supplier para reutilización
            Supplier<T> newSupplier = () -> {
                try {
                    return createManagerOptimized(managerClass, dependencies);
                } catch (Exception e) {
                    throw new RuntimeException("Error creating manager " + managerClass.getSimpleName(), e);
                }
            };
            MANAGER_CACHE.put(managerClass, newSupplier);
            
            // 5. Cachear singleton si es stateless
            if (isStatelessManager(managerClass)) {
                SINGLETON_CACHE.put(managerClass, newManager);
            }
            
            log.log(Level.FINE, "🎯 Manager creado y cacheado: {0}", managerClass.getSimpleName());
            return newManager;
            
        } catch (Exception e) {
            log.log(Level.WARNING, "⚠️ Error obteniendo manager {0}: {1}", 
                   new Object[]{managerClass.getSimpleName(), e.getMessage()});
            
            try {
                // Fallback: usar reflexión como última opción
                return createWithReflectionFallback(managerClass, dependencies);
            } catch (Exception reflectionException) {
                throw new RuntimeException("Failed to create manager " + managerClass.getSimpleName() + " with both optimized and fallback methods", reflectionException);
            }
        }
    }
    
    /**
     * ⚡ CREACIÓN OPTIMIZADA DE MANAGERS
     * Reemplaza reflexión costosa con factory methods directos
     */
    private static <T> T createManagerOptimized(Class<T> managerClass, Object[] dependencies) throws Exception {
        
        // ✅ FACTORY METHODS DIRECTOS - Evitan reflexión
        // ⚠️ NOTA: Los parámetros reales de los constructores según el código fuente
        
        if (managerClass == DependencyRegistry.class) {
            // Constructor: DependencyRegistry(WarmupContainer container, PropertySource propertySource, Set<String> activeProfiles)
            return managerClass.cast(new DependencyRegistry(
                (WarmupContainer) dependencies[0], 
                (PropertySource) dependencies[1], 
                (Set<String>) dependencies[2]));
        }
        
        if (managerClass == AopHandler.class) {
            // Constructor: AopHandler(WarmupContainer container)
            return managerClass.cast(new AopHandler((WarmupContainer) dependencies[0]));
        }
        
        if (managerClass == EventManager.class) {
            // Constructor: EventManager() - default constructor
            return managerClass.cast(new EventManager());
        }
        
        if (managerClass == AsyncHandler.class) {
            // Constructor: AsyncHandler(DependencyRegistry dependencyRegistry)
            return managerClass.cast(new AsyncHandler((DependencyRegistry) dependencies[0]));
        }
        
        if (managerClass == ShutdownManager.class) {
            // Constructor: ShutdownManager(WarmupContainer container, DependencyRegistry dependencyRegistry)
            return managerClass.cast(new ShutdownManager(
                (WarmupContainer) dependencies[0], 
                (DependencyRegistry) dependencies[1]));
        }
        
        if (managerClass == ProfileManager.class) {
            // Constructor: ProfileManager(PropertySource propertySource, String[] profiles)
            return managerClass.cast(new ProfileManager(
                (PropertySource) dependencies[0], 
                (String[]) dependencies[1]));
        }
        
        if (managerClass == ModuleManager.class) {
            // Constructor: ModuleManager(WarmupContainer container, PropertySource propertySource)
            return managerClass.cast(new ModuleManager(
                (WarmupContainer) dependencies[0], 
                (PropertySource) dependencies[1]));
        }
        
        if (managerClass == HealthCheckManager.class) {
            // Constructor: HealthCheckManager(WarmupContainer container)
            return managerClass.cast(new HealthCheckManager((WarmupContainer) dependencies[0]));
        }
        
        if (managerClass == MetricsManager.class) {
            // Constructor: MetricsManager(WarmupContainer container)
            return managerClass.cast(new MetricsManager((WarmupContainer) dependencies[0]));
        }
        
        if (managerClass == ASMCacheManager.class) {
            // ASMCacheManager es un singleton, usar reflexión para acceder al holder
            try {
                java.lang.reflect.Field holderField = managerClass.getDeclaredField("Holder");
                holderField.setAccessible(true);
                Object holder = holderField.get(null);
                java.lang.reflect.Field instanceField = holder.getClass().getDeclaredField("INSTANCE");
                instanceField.setAccessible(true);
                return managerClass.cast(instanceField.get(holder));
            } catch (Exception e) {
                // Fallback a reflexión general
                return createWithReflectionFallback(managerClass, dependencies);
            }
        }
        
        if (managerClass == WebScopeContext.class) {
            // Constructor: WebScopeContext(WarmupContainer container)
            return managerClass.cast(new WebScopeContext((WarmupContainer) dependencies[0]));
        }
        
        if (managerClass == ConfigurationProcessor.class) {
            // Constructor: ConfigurationProcessor(WarmupContainer container)
            return managerClass.cast(new ConfigurationProcessor((WarmupContainer) dependencies[0]));
        }
        
        // ✅ FALLBACK: Solo usar reflexión para managers no conocidos
        log.log(Level.FINE, "🔄 Usando fallback de reflexión para: {0}", managerClass.getSimpleName());
        return createWithReflectionFallback(managerClass, dependencies);
    }
    
    /**
     * 🔍 FALLBACK DE REFLEXIÓN (solo para managers no estándar)
     */
    private static <T> T createWithReflectionFallback(Class<T> managerClass, Object[] dependencies) throws Exception {
        try {
            // Intentar constructor con parámetros
            if (dependencies != null && dependencies.length > 0) {
                Class<?>[] paramTypes = new Class[dependencies.length];
                for (int i = 0; i < dependencies.length; i++) {
                    paramTypes[i] = dependencies[i].getClass();
                }
                
                java.lang.reflect.Constructor<T> constructor = managerClass.getDeclaredConstructor(paramTypes);
                return constructor.newInstance(dependencies);
            }
            
            // Constructor sin parámetros
            return managerClass.getDeclaredConstructor().newInstance();
            
        } catch (Exception e) {
            log.log(Level.SEVERE, "❌ Fallback de reflexión falló para {0}: {1}", 
                   new Object[]{managerClass.getSimpleName(), e.getMessage()});
            throw e;
        }
    }
    
    /**
     * 🎯 DETERMINAR SI ES MANAGER STATELESS
     * Los managers stateless pueden ser singletons
     */
    private static boolean isStatelessManager(Class<?> managerClass) {
        // ✅ MANAGERS CONOCIDOS COMO STATELESS
        return managerClass == ProfileManager.class ||
               managerClass == ModuleManager.class ||
               managerClass == ASMCacheManager.class ||
               managerClass == ConfigurationProcessor.class;
    }
    
    /**
     * 🚀 INICIALIZACIÓN INTELIGENTE
     * Pre-carga managers críticos, lazy load para no críticos
     */
    public static void initializeCriticalManagers() {
        log.log(Level.INFO, "🚀 Inicializando managers críticos con cache...");
        
        for (Class<?> managerClass : CRITICAL_MANAGERS) {
            try {
                getManager(managerClass);
                log.log(Level.FINE, "✅ Manager crítico precargado: {0}", managerClass.getSimpleName());
            } catch (Exception e) {
                log.log(Level.WARNING, "⚠️ Error precargando manager crítico {0}: {1}", 
                       new Object[]{managerClass.getSimpleName(), e.getMessage()});
            }
        }
    }
    
    /**
     * 🎯 PRECARGAR MANAGERS COMUNES
     * Optimización para casos de uso frecuentes
     */
    public static void preloadCommonManagers() {
        log.log(Level.FINE, "🎯 Precargando managers comunes...");
        
        Class<?>[] commonManagers = {
            DependencyRegistry.class,
            AopHandler.class,
            EventManager.class,
            HealthCheckManager.class,
            MetricsManager.class
        };
        
        for (Class<?> managerClass : commonManagers) {
            try {
                getManager(managerClass);
            } catch (Exception e) {
                log.log(Level.WARNING, "⚠️ Error precargando manager común {0}: {1}", 
                       new Object[]{managerClass.getSimpleName(), e.getMessage()});
            }
        }
    }
    
    /**
     * 🧹 LIMPIAR CACHE DE MANAGERS
     * Útil para testing y memoria
     */
    public static void clearCache() {
        MANAGER_CACHE.clear();
        SINGLETON_CACHE.clear();
        log.info("🧹 Cache de managers limpiado");
    }
    
    /**
     * 📊 ESTADÍSTICAS DEL CACHE
     */
    public static Map<String, Object> getCacheStats() {
        Map<String, Object> stats = new ConcurrentHashMap<>();
        stats.put("cached_suppliers", MANAGER_CACHE.size());
        stats.put("cached_singletons", SINGLETON_CACHE.size());
        stats.put("total_cached_managers", MANAGER_CACHE.size() + SINGLETON_CACHE.size());
        
        return stats;
    }
    
    /**
     * ✅ VERIFICAR ESTADO DEL FACTORY
     */
    public static boolean isInitialized() {
        return !MANAGER_CACHE.isEmpty() || !SINGLETON_CACHE.isEmpty();
    }
}