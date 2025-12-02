package io.warmup.framework.core;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;
import java.util.logging.Level;
import java.util.Map;
import java.util.Set;
import java.util.List;
import java.util.ArrayList;

/**
 * 🚀 COMPONENT POOL PATTERN
 * Pool de componentes reutilizables para operaciones de alta frecuencia
 * Reduce la overhead de creación/destrucción de objetos en el baseline
 * 
 * Optimizaciones implementadas:
 * - Object pooling para componentes frecuentes
 * - Reset en lugar de recreate (reducción de GC)
 * - Pool size management dinámico
 * - Thread-safe concurrent access
 */
public final class ComponentPool {
    
    private static final Logger log = Logger.getLogger(ComponentPool.class.getName());
    
    // ✅ CONFIGURACIÓN DEL POOL
    private static final int DEFAULT_POOL_SIZE = 10;
    private static final int MAX_POOL_SIZE = 50;
    private static final int MIN_POOL_SIZE = 2;
    
    // ✅ POOLS POR TIPO DE COMPONENTE
    private final Map<Class<?>, ComponentPoolManager<?>> componentPools = new ConcurrentHashMap<>();
    
    // ✅ MÉTRICAS DEL POOL
    private static final AtomicLong TOTAL_BORROWS = new AtomicLong(0);
    private static final AtomicLong TOTAL_RETURNS = new AtomicLong(0);
    private static final AtomicLong TOTAL_CREATES = new AtomicLong(0);
    private static final AtomicLong PEAK_USAGE = new AtomicLong(0);
    
    // ✅ FACTORY PARA CREAR COMPONENTES
    @FunctionalInterface
    public interface ComponentFactory<T> {
        T create();
    }
    
    // ✅ INTERFAZ PARA COMPONENTES POOLED
    public interface PooledComponent<T> {
        void reset();
        T getComponent();
        boolean isActive();
        void setActive(boolean active);
    }
    
    // ✅ IMPLEMENTACIÓN DE COMPONENTE POOLED
    private static class SimplePooledComponent<T> implements PooledComponent<T> {
        private final T component;
        private final ComponentFactory<T> factory;
        private volatile boolean active = true;
        
        public SimplePooledComponent(T component, ComponentFactory<T> factory) {
            this.component = component;
            this.factory = factory;
        }
        
        @Override
        public void reset() {
            // Resetear estado del componente para reutilización
            if (component instanceof Resettable) {
                ((Resettable) component).reset();
            }
            this.active = true;
        }
        
        @Override
        public T getComponent() {
            return component;
        }
        
        @Override
        public boolean isActive() {
            return active;
        }
        
        @Override
        public void setActive(boolean active) {
            this.active = active;
        }
    }
    
    // ✅ INTERFAZ PARA COMPONENTES RESETEABLES
    public interface Resettable {
        void reset();
    }
    
    // ✅ POOL MANAGER INTERNO
    private static class ComponentPoolManager<T> {
        private final List<PooledComponent<T>> available = new ArrayList<>();
        private final List<PooledComponent<T>> inUse = new ArrayList<>();
        private final ComponentFactory<T> factory;
        private final int maxSize;
        private final AtomicInteger currentSize = new AtomicInteger(0);
        
        public ComponentPoolManager(ComponentFactory<T> factory, int maxSize) {
            this.factory = factory;
            this.maxSize = maxSize;
            
            // Pre-crear componentes mínimos
            for (int i = 0; i < Math.min(MIN_POOL_SIZE, maxSize); i++) {
                available.add(createNewComponent());
            }
        }
        
        public PooledComponent<T> borrow() {
            synchronized (available) {
                if (!available.isEmpty()) {
                    PooledComponent<T> component = available.remove(available.size() - 1);
                    inUse.add(component);
                    updateMetrics();
                    return component;
                }
            }
            
            // Crear nuevo componente si no hay disponibles
            if (currentSize.get() < maxSize) {
                PooledComponent<T> component = createNewComponent();
                synchronized (inUse) {
                    inUse.add(component);
                }
                updateMetrics();
                return component;
            }
            
            // Pool agotado - crear sin pool
            log.log(Level.FINE, "Pool agotado para {0}, creando componente sin pool", 
                   factory.getClass().getSimpleName());
            return new SimplePooledComponent<>(factory.create(), factory);
        }
        
        public void returnComponent(PooledComponent<T> component) {
            synchronized (available) {
                if (available.size() < maxSize) {
                    component.reset();
                    available.add(component);
                    synchronized (inUse) {
                        inUse.remove(component);
                    }
                    TOTAL_RETURNS.incrementAndGet();
                } else {
                    // Pool está lleno, descartar componente
                    synchronized (inUse) {
                        inUse.remove(component);
                    }
                }
            }
            updateMetrics();
        }
        
        private PooledComponent<T> createNewComponent() {
            T newComponent = factory.create();
            TOTAL_CREATES.incrementAndGet();
            currentSize.incrementAndGet();
            return new SimplePooledComponent<>(newComponent, factory);
        }
        
        private void updateMetrics() {
            int total = inUse.size() + available.size();
            long currentPeak = PEAK_USAGE.get();
            if (total > currentPeak) {
                PEAK_USAGE.set(total);
            }
        }
        
        public Map<String, Object> getStats() {
            Map<String, Object> stats = new ConcurrentHashMap<>();
            synchronized (available) {
                stats.put("available", available.size());
                synchronized (inUse) {
                    stats.put("in_use", inUse.size());
                }
                stats.put("total_components", currentSize.get());
            }
            stats.put("max_size", maxSize);
            return stats;
        }
        
        public void clear() {
            synchronized (available) {
                available.clear();
                synchronized (inUse) {
                    inUse.clear();
                }
            }
            currentSize.set(0);
        }
    }
    
    // ✅ INSTANCIA ÚNICA DEL POOL (Singleton)
    private static final ComponentPool INSTANCE = new ComponentPool();
    
    private ComponentPool() {
        // Singleton pattern
    }
    
    public static ComponentPool getInstance() {
        return INSTANCE;
    }
    
    /**
     * 🚀 OBTENER COMPONENTE DESDE POOL
     */
    public <T> PooledComponent<T> getComponent(Class<T> componentClass, ComponentFactory<T> factory) {
        return getComponent(componentClass, factory, DEFAULT_POOL_SIZE);
    }
    
    /**
     * 🎯 OBTENER COMPONENTE DESDE POOL CON TAMAÑO PERSONALIZADO
     */
    @SuppressWarnings("unchecked")
    public <T> PooledComponent<T> getComponent(Class<T> componentClass, ComponentFactory<T> factory, int poolSize) {
        poolSize = Math.max(MIN_POOL_SIZE, Math.min(poolSize, MAX_POOL_SIZE));
        
        @SuppressWarnings("unchecked")
        ComponentPoolManager<T> poolManager = (ComponentPoolManager<T>) componentPools.get(componentClass);
        if (poolManager == null) {
            poolManager = new ComponentPoolManager<>(factory, poolSize);
            componentPools.put(componentClass, poolManager);
            log.log(Level.FINE, "🆕 Pool creado para {0} con tamaño {1}", 
                   new Object[]{componentClass.getSimpleName(), poolSize});
        }
        
        TOTAL_BORROWS.incrementAndGet();
        return poolManager.borrow();
    }
    
    /**
     * 📤 DEVOLVER COMPONENTE AL POOL
     */
    @SuppressWarnings("unchecked")
    public <T> void returnComponent(Class<T> componentClass, PooledComponent<T> component) {
        ComponentPoolManager<T> poolManager = (ComponentPoolManager<T>) componentPools.get(componentClass);
        if (poolManager != null) {
            poolManager.returnComponent(component);
        }
    }
    
    /**
     * 🧹 LIMPIAR POOL PARA UNA CLASE
     */
    @SuppressWarnings("unchecked")
    public <T> void clearPool(Class<T> componentClass) {
        ComponentPoolManager<T> poolManager = (ComponentPoolManager<T>) componentPools.get(componentClass);
        if (poolManager != null) {
            poolManager.clear();
            log.log(Level.FINE, "🧹 Pool limpiado para: {0}", componentClass.getSimpleName());
        }
    }
    
    /**
     * 🧹 LIMPIAR TODOS LOS POOLS
     */
    @SuppressWarnings("unchecked")
    public void clearAllPools() {
        componentPools.values().forEach(poolManager -> {
            ComponentPoolManager<?> typedManager = (ComponentPoolManager<?>) poolManager;
            typedManager.clear();
        });
        log.info("🧹 Todos los pools limpiados");
    }
    
    /**
     * 📊 OBTENER ESTADÍSTICAS DEL POOL
     */
    public Map<String, Object> getPoolStats() {
        Map<String, Object> globalStats = new ConcurrentHashMap<>();
        
        // Estadísticas globales
        globalStats.put("total_borrows", TOTAL_BORROWS.get());
        globalStats.put("total_returns", TOTAL_RETURNS.get());
        globalStats.put("total_creates", TOTAL_CREATES.get());
        globalStats.put("peak_usage", PEAK_USAGE.get());
        globalStats.put("active_pools", componentPools.size());
        
        // Estadísticas por pool
        Map<String, Map<String, Object>> poolStats = new ConcurrentHashMap<>();
        componentPools.forEach((clazz, poolManager) -> {
            @SuppressWarnings("unchecked")
            ComponentPoolManager<Object> typedManager = (ComponentPoolManager<Object>) poolManager;
            poolStats.put(clazz.getSimpleName(), typedManager.getStats());
        });
        globalStats.put("pool_details", poolStats);
        
        return globalStats;
    }
    
    /**
     * ✅ VERIFICAR SI EL POOL ESTÁ HABILITADO
     */
    public boolean isEnabled() {
        return !componentPools.isEmpty();
    }
    
    /**
     * 🎯 CREAR FACTORY SIMPLE PARA CLASES CON CONSTRUCTOR SIN PARÁMETROS
     */
    public static <T> ComponentFactory<T> simpleFactory(Class<T> clazz) {
        return () -> {
            try {
                return clazz.getDeclaredConstructor().newInstance();
            } catch (Exception e) {
                throw new RuntimeException("Error creando instancia de " + clazz.getSimpleName(), e);
            }
        };
    }
    
    /**
     * 🔧 FACTORY CON PARÁMETROS PERSONALIZADOS
     */
    public static <T> ComponentFactory<T> parameterizedFactory(java.util.function.Supplier<T> supplier) {
        return supplier::get;
    }
    
    // ✅ MÉTODOS DE HELP PARA COMPONENTES COMUNES
    
    /**
     * 🚀 POOL PARA DEPENDENCY REGISTRY
     */
    public PooledComponent<io.warmup.framework.core.DependencyRegistry> getDependencyRegistryPool(WarmupContainer container) {
        ComponentFactory<io.warmup.framework.core.DependencyRegistry> factory = () -> {
            // ⚠️ NOTA: DependencyRegistry requiere PropertySource y activeProfiles
            // Usamos reflexión como fallback ya que no tenemos acceso a esos parámetros
            try {
                return io.warmup.framework.core.DependencyRegistry.class.getDeclaredConstructor(WarmupContainer.class, io.warmup.framework.config.PropertySource.class, Set.class)
                    .newInstance(container, container.getPropertySource(), container.getActiveProfiles());
            } catch (Exception e) {
                // Fallback a constructor sin parámetros si existe
                try {
                    return io.warmup.framework.core.DependencyRegistry.class.getDeclaredConstructor().newInstance();
                } catch (Exception ex) {
                    throw new RuntimeException("Error creando DependencyRegistry pool", ex);
                }
            }
        };
        return getComponent(io.warmup.framework.core.DependencyRegistry.class, factory, 5);
    }
    
    /**
     * 📊 POOL PARA METRICS MANAGER
     */
    public PooledComponent<io.warmup.framework.metrics.MetricsManager> getMetricsManagerPool(WarmupContainer container) {
        ComponentFactory<io.warmup.framework.metrics.MetricsManager> factory = () -> 
            new io.warmup.framework.metrics.MetricsManager(container);
        return getComponent(io.warmup.framework.metrics.MetricsManager.class, factory, 3);
    }
    
    /**
     * 🏥 POOL PARA HEALTH CHECK MANAGER
     */
    public PooledComponent<io.warmup.framework.health.HealthCheckManager> getHealthCheckManagerPool(WarmupContainer container) {
        ComponentFactory<io.warmup.framework.health.HealthCheckManager> factory = () -> 
            new io.warmup.framework.health.HealthCheckManager(container);
        return getComponent(io.warmup.framework.health.HealthCheckManager.class, factory, 3);
    }
}