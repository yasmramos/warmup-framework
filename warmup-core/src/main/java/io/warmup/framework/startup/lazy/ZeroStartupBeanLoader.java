package io.warmup.framework.startup.lazy;

import io.warmup.framework.core.WarmupContainer;
import io.warmup.framework.startup.ParallelSubsystemInitializer;
import io.warmup.framework.startup.StartupPhasesManager;

import java.lang.reflect.Constructor;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * 🚀 CARGADOR DE BEANS CON CERO INICIALIZACIÓN
 * 
 * Implementa el concepto de "cero inicialización hasta el primer uso real".
 * Características:
 * - ✅ ZERO startup cost: No crea beans hasta que se soliciten
 * - ✅ On-demand loading: Beans se crean solo cuando se necesitan
 * - ✅ Parallel infrastructure: Infraestructura se inicia en paralelo
 * - ✅ Lazy injection: Inyección de dependencias lazy
 * - ✅ Smart caching: Reutilización inteligente de instancias
 * - ✅ Automatic profiles: Configuración automática por perfiles
 * 
 * Estrategia:
 * 1. 🚀 Inicializar infraestructura crítica en paralelo
 * 2. 📝 Registrar todos los beans como lazy suppliers
 * 3. ⚡ Zero cost startup - framework listo sin crear beans
 * 4. 🎯 Crear beans solo cuando se solicitan
 * 5. 📊 Monitoreo y estadísticas de uso real
 * 
 * @author MiniMax Agent
 * @version 1.0
 */
public class ZeroStartupBeanLoader {
    
    private static final Logger log = Logger.getLogger(ZeroStartupBeanLoader.class.getName());
    
    private final WarmupContainer container;
    private final OnDemandInitializationContext onDemandContext;
    private final LazyBeanRegistry lazyBeanRegistry;
    private final StartupPhasesManager phasesManager;
    private final ParallelSubsystemInitializer parallelInitializer;
    
    // 📊 CONFIGURACIÓN DE LAZY LOADING
    private final Map<String, LazyBeanConfig> beanConfigs = new ConcurrentHashMap<>();
    private final Set<String> criticalInfrastructureBeans = new HashSet<>();
    private final Set<String> eagerBeans = new HashSet<>();
    
    // 📈 ESTADÍSTICAS DE STARTUP CERO
    private final AtomicInteger zeroStartupSavings = new AtomicInteger(0);
    private final AtomicInteger onDemandCreations = new AtomicInteger(0);
    private final AtomicInteger parallelInfrastructureInit = new AtomicInteger(0);
    
    // ⚙️ CONFIGURACIÓN
    private boolean enableParallelInfrastructure = true;
    private boolean enableSmartCaching = true;
    private boolean enableProfileAutoConfig = true;
    private boolean enableMetrics = true;
    
    public ZeroStartupBeanLoader(WarmupContainer container) {
        this.container = container;
        this.onDemandContext = new OnDemandInitializationContext(container);
        this.lazyBeanRegistry = onDemandContext.getLazyBeanRegistry();
        this.phasesManager = new StartupPhasesManager(container);
        this.parallelInitializer = new ParallelSubsystemInitializer(container);
        
        log.log(Level.INFO, "🚀 ZeroStartupBeanLoader inicializado - ZERO COST STARTUP habilitado");
    }
    
    /**
     * ⚡ EJECUTAR ZERO COST STARTUP
     * 
     * Estrategia:
     * 1. Inicializar infraestructura crítica en paralelo
     * 2. Registrar beans como lazy sin crearlos
     * 3. Framework listo sin costo de beans
     */
    public CompletableFuture<ZeroStartupResult> executeZeroCostStartup() {
        log.log(Level.INFO, "⚡ INICIANDO ZERO COST STARTUP");
        
        long globalStartTime = System.nanoTime();
        
        // FASE 1: Inicializar infraestructura crítica en paralelo
        CompletableFuture<InfrastructureInitResult> infrastructureFuture = 
            initializeInfrastructureInParallel();
        
        // FASE 2: Registrar beans como lazy sin crearlos
        CompletableFuture<Void> registrationFuture = CompletableFuture.runAsync(() -> {
            registerAllBeansAsLazy();
        });
        
        // Combinar fases
        return CompletableFuture.allOf(infrastructureFuture, registrationFuture)
            .thenApply(v -> {
                long globalDuration = System.nanoTime() - globalStartTime;
                
                ZeroStartupResult result = new ZeroStartupResult(
                    globalDuration,
                    infrastructureFuture.join(),
                    getZeroStartupStats(),
                    enableParallelInfrastructure
                );
                
                log.log(Level.INFO, "✅ ZERO COST STARTUP COMPLETADO en {0}ms", globalDuration / 1_000_000);
                log.log(Level.INFO, "🎯 Startup cost: $0 - Beans se crearán on-demand cuando se soliciten");
                
                return result;
            });
    }
    
    /**
     * 🚀 INICIALIZAR INFRAESTRUCTURA EN PARALELO
     */
    private CompletableFuture<InfrastructureInitResult> initializeInfrastructureInParallel() {
        if (!enableParallelInfrastructure) {
            return CompletableFuture.completedFuture(new InfrastructureInitResult(
                Collections.emptyList(), System.nanoTime(), false
            ));
        }
        
        log.log(Level.INFO, "🚀 Inicializando infraestructura en paralelo...");
        
        long startTime = System.nanoTime();
        
        return phasesManager.executeParallelSubsystemInitialization()
            .thenApply(parallelResult -> {
                parallelInfrastructureInit.incrementAndGet();
                
                List<String> initializedComponents = parallelResult.getSubsystemResults().stream()
                    .filter(result -> result.isSuccess())
                    .map(result -> result.getName())
                    .collect(ArrayList::new, ArrayList::add, ArrayList::addAll);
                
                log.log(Level.INFO, "✅ Infraestructura inicializada: {0} componentes", initializedComponents.size());
                
                return new InfrastructureInitResult(
                    initializedComponents,
                    startTime,
                    parallelResult.isAllSuccessful()
                );
            })
            .exceptionally(throwable -> {
                log.log(Level.WARNING, "⚠️ Error inicializando infraestructura en paralelo: {0}", 
                        throwable.getMessage());
                return new InfrastructureInitResult(
                    Collections.emptyList(), 
                    startTime, 
                    false
                );
            });
    }
    
    /**
     * 📝 REGISTRAR TODOS LOS BEANS COMO LAZY
     */
    private void registerAllBeansAsLazy() {
        log.log(Level.FINE, "📝 Registrando beans como lazy (sin crear)...");
        
        try {
            // Registrar beans de infraestructura crítica como eager
            registerCriticalInfrastructureBeans();
            
            // Registrar beans de aplicación como lazy
            registerApplicationBeans();
            
            // Registrar beans de configuración como eager
            registerConfigurationBeans();
            
            log.log(Level.INFO, "📝 Beans registrados: {0} lazy, {1} eager", 
                    new Object[]{getLazyBeanCount(), getEagerBeanCount()});
            
        } catch (Exception e) {
            log.log(Level.SEVERE, "❌ Error registrando beans como lazy: {0}", e.getMessage());
        }
    }
    
    /**
     * 🏗️ REGISTRAR BEANS DE INFRAESTRUCTURA CRÍTICA
     */
    private void registerCriticalInfrastructureBeans() {
        // Estos beans son críticos y se deben inicializar rápido
        String[] criticalBeans = {
            "DependencyRegistry", "PropertySource", "ProfileManager",
            "EventBus", "EventPublisher", "HealthCheckManager"
        };
        
        for (String beanName : criticalBeans) {
            eagerBeans.add(beanName);
            
            // Crear supplier que use la infraestructura ya inicializada
            LazyBeanSupplier<Object> eagerSupplier = new LazyBeanSupplier<>(
                beanName,
                () -> createCriticalInfrastructureBean(beanName),
                true // eager = true
            );
            
            // Registrar con tipo genérico por ahora
            lazyBeanRegistry.registerLazyBean(beanName, Object.class, eagerSupplier);
            criticalInfrastructureBeans.add(beanName);
        }
    }
    
    /**
     * 🏗️ CREAR BEAN DE INFRAESTRUCTURA CRÍTICA
     */
    private Object createCriticalInfrastructureBean(String beanName) {
        switch (beanName) {
            case "DependencyRegistry":
                return container.getDependencyRegistry();
            case "PropertySource":
                return container.getPropertySource();
            case "ProfileManager":
                return container.getProfileManager();
            case "EventBus":
                return container.getBean(io.warmup.framework.event.EventBus.class);
            case "EventPublisher":
                return container.getBean(io.warmup.framework.event.EventPublisher.class);
            case "HealthCheckManager":
                return container.getBean(io.warmup.framework.health.HealthCheckManager.class);
            default:
                throw new IllegalArgumentException("Unknown critical infrastructure bean: " + beanName);
        }
    }
    
    /**
     * 📝 REGISTRAR BEANS DE APLICACIÓN COMO LAZY
     */
    private void registerApplicationBeans() {
        // Aquí se registrarían todos los beans de la aplicación
        // Como ejemplo, registramos algunos tipos comunes
        
        // Service beans
        registerLazyBeanClass("UserService", "io.warmup.example.service.UserService");
        registerLazyBeanClass("OrderService", "io.warmup.example.service.OrderService");
        registerLazyBeanClass("PaymentService", "io.warmup.example.service.PaymentService");
        
        // Repository beans
        registerLazyBeanClass("UserRepository", "io.warmup.example.repository.UserRepository");
        registerLazyBeanClass("OrderRepository", "io.warmup.example.repository.OrderRepository");
        
        // Controller beans
        registerLazyBeanClass("UserController", "io.warmup.example.controller.UserController");
        registerLazyBeanClass("OrderController", "io.warmup.example.controller.OrderController");
    }
    
    /**
     * 📝 REGISTRAR BEAN CLASS COMO LAZY
     */
    private void registerLazyBeanClass(String beanName, String className) {
        try {
            Class<?> beanClass = Class.forName(className);
            Constructor<?> constructor = findBestConstructor(beanClass);
            
            if (constructor != null) {
                @SuppressWarnings("unchecked")
                LazyBeanSupplier<Object> lazySupplier = new LazyBeanSupplier<>(
                    beanName,
                    () -> onDemandContext.createBeanOnDemand(beanName, beanClass, constructor)
                );
                
                lazyBeanRegistry.registerLazyBean(beanName, (Class<Object>) beanClass, lazySupplier);
            }
        } catch (ClassNotFoundException e) {
            log.log(Level.FINE, "⚠️ Clase no encontrada para bean lazy {0}: {1}", 
                    new Object[]{beanName, className});
        } catch (Exception e) {
            log.log(Level.WARNING, "⚠️ Error registrando bean lazy {0}: {1}", 
                    new Object[]{beanName, e.getMessage()});
        }
    }
    
    /**
     * 🏗️ REGISTRAR BEANS DE CONFIGURACIÓN COMO EAGER
     */
    private void registerConfigurationBeans() {
        // Beans de configuración se inicializan eager para disponibilidad inmediata
        eagerBeans.add("AppConfig");
        eagerBeans.add("DatabaseConfig");
        eagerBeans.add("SecurityConfig");
    }
    
    /**
     * 🔍 ENCONTRAR MEJOR CONSTRUCTOR
     */
    private Constructor<?> findBestConstructor(Class<?> beanClass) {
        Constructor<?>[] constructors = beanClass.getDeclaredConstructors();
        
        // Prioridad 1: Constructor con @Inject
        for (Constructor<?> constructor : constructors) {
            if (constructor.isAnnotationPresent(jakarta.inject.Inject.class)) {
                return constructor;
            }
        }
        
        // Prioridad 2: Constructor público con menos parámetros
        Constructor<?> best = null;
        int minParams = Integer.MAX_VALUE;
        
        for (Constructor<?> constructor : constructors) {
            if (java.lang.reflect.Modifier.isPublic(constructor.getModifiers())) {
                int paramCount = constructor.getParameterCount();
                if (paramCount < minParams) {
                    minParams = paramCount;
                    best = constructor;
                }
            }
        }
        
        return best;
    }
    
    /**
     * 🎯 OBTENER BEAN LAZY
     */
    @SuppressWarnings("unchecked")
    public <T> T getBean(String beanName, Class<T> expectedType) {
        T bean = (T) lazyBeanRegistry.getLazyBean(beanName, expectedType);
        
        if (bean != null) {
            onDemandCreations.incrementAndGet();
            log.log(Level.FINEST, "🎯 Bean lazy obtenido: {0}", beanName);
        }
        
        return bean;
    }
    
    /**
     * 📊 OBTENER ESTADÍSTICAS DE ZERO STARTUP
     */
    public ZeroStartupStats getZeroStartupStats() {
        return new ZeroStartupStats(
            getLazyBeanCount(),
            getEagerBeanCount(),
            onDemandCreations.get(),
            parallelInfrastructureInit.get(),
            zeroStartupSavings.get(),
            enableParallelInfrastructure
        );
    }
    
    /**
     * 📊 GENERAR REPORTE COMPLETO DE ZERO STARTUP
     */
    public String generateZeroStartupReport() {
        ZeroStartupStats stats = getZeroStartupStats();
        LazyBeanRegistry.GlobalLazyStats globalStats = lazyBeanRegistry.getGlobalStats();
        
        StringBuilder report = new StringBuilder();
        
        report.append("⚡ REPORTE DE ZERO COST STARTUP\n");
        report.append("=================================\n\n");
        
        report.append("🎯 CONCEPTO ZERO STARTUP:\n");
        report.append("  • ✅ Infraestructura se inicializa en paralelo\n");
        report.append("  • ✅ Beans de aplicación se crean on-demand\n");
        report.append("  • ✅ Zero cost inicial para beans no usados\n");
        report.append("  • ✅ Solo se paga por lo que realmente se usa\n\n");
        
        report.append("📊 ESTADÍSTICAS DE BEANS:\n");
        report.append(String.format("  • Beans lazy registrados: %d\n", stats.getLazyBeanCount()));
        report.append(String.format("  • Beans eager registrados: %d\n", stats.getEagerBeanCount()));
        report.append(String.format("  • Beans creados on-demand: %d\n", stats.getOnDemandCreations()));
        report.append(String.format("  • Infraestructura paralela: %s\n", 
                stats.isParallelInfrastructure() ? "✅ Habilitada" : "❌ Deshabilitada"));
        
        if (stats.getOnDemandCreations() > 0) {
            double usageRate = (double) stats.getOnDemandCreations() / (stats.getLazyBeanCount() + stats.getEagerBeanCount());
            report.append(String.format("  • Tasa de uso real: %.1f%%\n", usageRate * 100));
            report.append(String.format("  • Ahorro estimado: %d beans no inicializados\n", 
                    stats.getLazyBeanCount() - stats.getOnDemandCreations()));
        }
        
        report.append("\n📈 ESTADÍSTICAS DETALLADAS:\n");
        report.append(lazyBeanRegistry.generateStatsReport());
        
        return report.toString();
    }
    
    // ===== Getters auxiliares =====
    
    public int getLazyBeanCount() {
        return (int) lazyBeanRegistry.listRegisteredBeans().stream()
            .filter(name -> !eagerBeans.contains(name))
            .count();
    }
    
    public int getEagerBeanCount() {
        return eagerBeans.size();
    }
    
    public LazyBeanRegistry getLazyBeanRegistry() {
        return lazyBeanRegistry;
    }
    
    public OnDemandInitializationContext getOnDemandContext() {
        return onDemandContext;
    }
    
    /**
     * 🧹 SHUTDOWN
     */
    public void shutdown() {
        log.log(Level.INFO, "🧹 Cerrando ZeroStartupBeanLoader...");
        
        if (phasesManager != null) {
            phasesManager.shutdown();
        }
        
        if (onDemandContext != null) {
            onDemandContext.cleanup();
        }
        
        log.log(Level.FINE, "✅ ZeroStartupBeanLoader cerrado");
    }
    
    // ===== CLASES DE RESULTADO =====
    
    /**
     * 📊 RESULTADO DE INICIALIZACIÓN DE INFRAESTRUCTURA
     */
    public static class InfrastructureInitResult {
        private final List<String> initializedComponents;
        private final long initializationTimeNs;
        private final boolean success;
        
        public InfrastructureInitResult(List<String> initializedComponents, 
                                     long initializationTimeNs, boolean success) {
            this.initializedComponents = initializedComponents;
            this.initializationTimeNs = initializationTimeNs;
            this.success = success;
        }
        
        public List<String> getInitializedComponents() { return initializedComponents; }
        public long getInitializationTimeNs() { return initializationTimeNs; }
        public boolean isSuccess() { return success; }
        public long getInitializationTimeMs() { return initializationTimeNs / 1_000_000; }
    }
    
    /**
     * 📊 RESULTADO COMPLETO DE ZERO STARTUP
     */
    public static class ZeroStartupResult {
        private final long totalTimeNs;
        private final InfrastructureInitResult infrastructureResult;
        private final ZeroStartupStats stats;
        private final boolean parallelEnabled;
        
        public ZeroStartupResult(long totalTimeNs, InfrastructureInitResult infrastructureResult,
                               ZeroStartupStats stats, boolean parallelEnabled) {
            this.totalTimeNs = totalTimeNs;
            this.infrastructureResult = infrastructureResult;
            this.stats = stats;
            this.parallelEnabled = parallelEnabled;
        }
        
        public long getTotalTimeNs() { return totalTimeNs; }
        public long getTotalTimeMs() { return totalTimeNs / 1_000_000; }
        public InfrastructureInitResult getInfrastructureResult() { return infrastructureResult; }
        public ZeroStartupStats getStats() { return stats; }
        public boolean isParallelEnabled() { return parallelEnabled; }
        
        @Override
        public String toString() {
            return String.format("ZeroStartupResult{time=%dms, components=%d, success=%s}", 
                    getTotalTimeMs(), infrastructureResult.getInitializedComponents().size(),
                    infrastructureResult.isSuccess());
        }
    }
    
    /**
     * 📊 CONFIGURACIÓN DE BEAN LAZY
     */
    public static class LazyBeanConfig {
        private final String beanName;
        private final Class<?> beanType;
        private final boolean eager;
        private final List<String> dependencies;
        private final Map<String, Object> properties;
        
        public LazyBeanConfig(String beanName, Class<?> beanType, boolean eager) {
            this(beanName, beanType, eager, new ArrayList<>(), new HashMap<>());
        }
        
        public LazyBeanConfig(String beanName, Class<?> beanType, boolean eager,
                            List<String> dependencies, Map<String, Object> properties) {
            this.beanName = beanName;
            this.beanType = beanType;
            this.eager = eager;
            this.dependencies = dependencies;
            this.properties = properties;
        }
        
        // Getters
        public String getBeanName() { return beanName; }
        public Class<?> getBeanType() { return beanType; }
        public boolean isEager() { return eager; }
        public List<String> getDependencies() { return dependencies; }
        public Map<String, Object> getProperties() { return properties; }
    }
    
    /**
     * 📊 ESTADÍSTICAS DE ZERO STARTUP
     */
    public static class ZeroStartupStats {
        private final int lazyBeanCount;
        private final int eagerBeanCount;
        private final int onDemandCreations;
        private final int parallelInfrastructureInit;
        private final int zeroStartupSavings;
        private final boolean parallelEnabled;
        
        public ZeroStartupStats(int lazyBeanCount, int eagerBeanCount, int onDemandCreations,
                              int parallelInfrastructureInit, int zeroStartupSavings, boolean parallelEnabled) {
            this.lazyBeanCount = lazyBeanCount;
            this.eagerBeanCount = eagerBeanCount;
            this.onDemandCreations = onDemandCreations;
            this.parallelInfrastructureInit = parallelInfrastructureInit;
            this.zeroStartupSavings = zeroStartupSavings;
            this.parallelEnabled = parallelEnabled;
        }
        
        // Getters
        public int getLazyBeanCount() { return lazyBeanCount; }
        public int getEagerBeanCount() { return eagerBeanCount; }
        public int getOnDemandCreations() { return onDemandCreations; }
        public int getParallelInfrastructureInit() { return parallelInfrastructureInit; }
        public int getZeroStartupSavings() { return zeroStartupSavings; }
        public boolean isParallelInfrastructure() { return parallelEnabled; }
        
        @Override
        public String toString() {
            return String.format("ZeroStartupStats{lazy=%d, eager=%d, created=%d, infra=%d}", 
                    lazyBeanCount, eagerBeanCount, onDemandCreations, parallelInfrastructureInit);
        }
    }
}