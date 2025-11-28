package io.warmup.framework.startup;

import io.warmup.framework.core.*;
import io.warmup.framework.config.PropertySource;
import io.warmup.framework.core.ProfileManager;
import io.warmup.framework.cache.ASMCacheManager;
import io.warmup.framework.cache.CacheConfig;
import io.warmup.framework.event.EventBus;
import io.warmup.framework.event.EventPublisher;
import io.warmup.framework.health.HealthCheckManager;
import io.warmup.framework.metrics.MetricsManager;
import io.warmup.framework.asm.AsmCoreUtils;
import io.warmup.framework.jit.asm.AsmDependencyEngine;
import io.warmup.framework.startup.critical.CriticalPhaseOptimizer;
import io.warmup.framework.event.EventBusResolver;

import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * 🎯 FASE CRÍTICA DE STARTUP OPTIMIZADA - Target: < 2ms
 * 
 * Inicializa solo los componentes esenciales usando CriticalPhaseOptimizer:
 * - DependencyRegistry básico con lock-free operations
 * - ProfileManager y PropertySource con thread pools
 * - Managers esenciales con parallel initialization
 * - JIT ASM Engine con pre-warmed caches
 * - Componentes framework básicos O(1)
 * 
 * @author MiniMax Agent
 * @version 2.0 - Optimizada
 */
public class CriticalStartupPhase {
    
    private static final Logger log = Logger.getLogger(CriticalStartupPhase.class.getName());
    
    private final WarmupContainer container;
    private final DependencyRegistry dependencyRegistry;
    private final PropertySource propertySource;
    private final Set<String> activeProfiles;
    
    // ⚡ Critical Phase Optimizer - New in v2.0
    private final CriticalPhaseOptimizer optimizer;
    
    public CriticalStartupPhase(WarmupContainer container) {
        this.container = container;
        this.dependencyRegistry = (DependencyRegistry) container.getDependencyRegistry();
        this.propertySource = (PropertySource) container.getPropertySource();
        this.activeProfiles = new HashSet<>(Arrays.asList((String[]) container.getActiveProfiles()));
        
        // ⚡ Initialize Critical Phase Optimizer
        this.optimizer = new CriticalPhaseOptimizer(container);
    }
    
    /**
     * 🎯 PASO 1 OPTIMIZADO: Inicializar componentes esenciales del container
     * 
     * ⚡ Improvements v2.0:
     * - Usa CriticalPhaseOptimizer para parallel initialization
     * - Elimina synchronized blocks en hot path
     * - Pre-configured thread pools para mejor performance
     */
    public void initializeEssentialContainerComponents() {
        log.log(Level.FINE, "🔧 Inicializando componentes esenciales del container (optimizado)...");
        
        try {
            // ⚡ OPTIMIZACIÓN: Use fast optimizer instead of sequential checks
            optimizer.executeCriticalPhase();
            
            // Quick validation that critical infrastructure is ready
            if (!verifyCriticalInfrastructureReady()) {
                throw new StartupPhaseException("Critical infrastructure verification failed", null);
            }
            
            log.log(Level.FINE, "✅ Componentes esenciales del container inicializados (optimizado)");
            
        } catch (Exception e) {
            log.log(Level.WARNING, "⚠️ Error en inicialización optimizada: {0}", e.getMessage());
            // Fallback to minimal setup
            executeMinimalCriticalSetup();
        }
    }
    
    /**
     * ⚡ Verify critical infrastructure is ready (fast check)
     */
    private boolean verifyCriticalInfrastructureReady() {
        try {
            // Quick O(1) checks
            return dependencyRegistry != null 
                && container.getShutdownManager() != null
                && verifyOptimizerHealth();
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * ⚡ Verify optimizer health
     */
    private boolean verifyOptimizerHealth() {
        try {
            // Fast health check
            return optimizer != null;
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * 🔧 Minimal critical setup fallback
     */
    private void executeMinimalCriticalSetup() {
        try {
            // Use EventBusResolver for reliable bean registration
            EventBusResolver.ensureEventBusRegistered(dependencyRegistry, container);
            
            // Basic container registration
            dependencyRegistry.register(WarmupContainer.class, container);
            
            log.log(Level.WARNING, "⚠️ Using minimal critical setup (fallback)");
        } catch (Exception e) {
            log.log(Level.SEVERE, "❌ Minimal critical setup failed: {0}", e.getMessage());
            throw new StartupPhaseException("Critical setup failed completely", e);
        }
    }
    
    /**
     * 🎯 PASO 2: Inicializar DependencyRegistry básico
     */
    public void initializeCoreDependencyRegistry() {
        log.log(Level.FINE, "🔧 Inicializando DependencyRegistry básico...");
        
        // Verificar que DependencyRegistry esté disponible
        if (dependencyRegistry == null) {
            throw new StartupPhaseException("DependencyRegistry no disponible en fase crítica", null);
        }
        
        // Registrar dependencias críticas básicas
        registerEssentialDependencies();
        
        log.log(Level.FINE, "✅ DependencyRegistry básico inicializado");
    }
    
    /**
     * 🎯 PASO 3: Inicializar configuración core (ProfileManager + PropertySource)
     */
    public void initializeCoreConfiguration() {
        log.log(Level.FINE, "🔧 Inicializando configuración core...");
        
        // Verificar ProfileManager
        ProfileManager profileManager = (ProfileManager) container.getProfileManager();
        if (profileManager == null) {
            throw new StartupPhaseException("ProfileManager no disponible en fase crítica", null);
        }
        
        // Verificar PropertySource
        if (propertySource == null) {
            throw new StartupPhaseException("PropertySource no disponible en fase crítica", null);
        }
        
        // Solo configuración básica, sin procesamiento complejo
        log.log(Level.FINE, "✅ Configuración core inicializada");
    }
    
    /**
     * 🎯 PASO 4 OPTIMIZADO: Inicializar optimizaciones JIT críticas
     * 
     * ⚡ Improvements v2.0:
     * - Elimina expensive reflection calls
     * - Usa pre-warmed caches para O(1) lookups
     * - Parallel precompilation using thread pools
     */
    public void initializeCriticalJitOptimizations() {
        log.log(Level.FINE, "🔧 Inicializando optimizaciones JIT críticas (optimizado)...");
        
        try {
            // ⚡ OPTIMIZACIÓN: Use optimizer's fast precompilation
            optimizer.precompileCriticalComponentsFast();
            
            log.log(Level.FINE, "✅ Optimizaciones JIT críticas inicializadas (optimizado)");
            
        } catch (Exception e) {
            log.log(Level.FINE, "⚠️ Error en optimizaciones JIT críticas optimizadas: {0}", e.getMessage());
            // Don't fail startup due to JIT errors
        }
    }
    
    /**
     * 🎯 PASO 5 OPTIMIZADO: Inicializar componentes críticos identificados
     * 
     * ⚡ Improvements v2.0:
     * - Elimina expensive component identification loops
     * - Usa pre-classified component cache (O(1))
     * - Parallel initialization using critical executor
     */
    public void initializeCriticalComponents() {
        log.log(Level.FINE, "🔧 Inicializando componentes críticos (optimizado)...");
        
        try {
            // ⚡ OPTIMIZACIÓN: Use optimizer's fast classification
            optimizer.classifyCriticalComponentsFast();
            
            // Quick validation of critical components
            int validated = validateCriticalComponents();
            
            log.log(Level.INFO, "✅ {0} componentes críticos inicializados (optimizado)", validated);
            
        } catch (Exception e) {
            log.log(Level.FINE, "⚠️ Error en inicialización de componentes críticos optimizada: {0}", e.getMessage());
            // Don't fail startup due to component errors
        }
    }
    
    /**
     * ⚡ Fast critical components validation
     */
    private int validateCriticalComponents() {
        int validated = 0;
        
        // Use optimizer's fast component cache
        if (optimizer != null) {
            // Fast O(1) validation using pre-warmed caches
            try {
                validated = optimizer.getValidatedCriticalComponents();
            } catch (Exception e) {
                log.log(Level.FINE, "⚠️ Fast component validation failed: {0}", e.getMessage());
            }
        }
        
        return validated;
    }
    
    /**
     * 🔍 Verificar que los managers esenciales estén disponibles
     */
    private void ensureManagersAvailable() {
        // Verificar managers críticos
        if (container.getShutdownManager() == null) {
            throw new StartupPhaseException("ShutdownManager no disponible", null);
        }
        
        if (container.getModuleManager() == null) {
            log.log(Level.FINE, "⚠️ ModuleManager no disponible, saltando verificación");
        }
        
        if (container.getWebScopeContext() == null) {
            log.log(Level.FINE, "⚠️ WebScopeContext no disponible, saltando verificación");
        }
    }
    
    /**
     * 📝 Registrar dependencias esenciales del framework
     */
    private void registerEssentialDependencies() {
        try {
            // EventBus y EventPublisher básicos
            if (dependencyRegistry.getBean(EventBus.class) == null) {
                EventBus eventBus = new EventBus();
                dependencyRegistry.register(EventBus.class, eventBus);
            }
            
            EventBus eventBus = dependencyRegistry.getBean(EventBus.class);
            if (eventBus != null && dependencyRegistry.getBean(EventPublisher.class) == null) {
                EventPublisher eventPublisher = new EventPublisher(eventBus);
                dependencyRegistry.register(EventPublisher.class, eventPublisher);
            }
            
            // Asegurar que WarmupContainer esté registrado
            if (dependencyRegistry.getBean(WarmupContainer.class) == null) {
                dependencyRegistry.register(WarmupContainer.class, container);
            }
            
        } catch (Exception e) {
            log.log(Level.WARNING, "⚠️ Error registrando dependencias esenciales: {0}", e.getMessage());
            // No fallar por errores de registro de dependencias
        }
    }
    
    /**
     * 🔍 Identificar componentes críticos para startup
     */
    private List<Class<?>> identifyCriticalComponents() {
        List<Class<?>> critical = new ArrayList<>();
        
        try {
            // Componentes del framework siempre críticos
            if (dependencyRegistry != null) {
                for (Dependency dependency : dependencyRegistry.getDependencies().values()) {
                    Class<?> type = dependency.getType();
                    
                    // Framework components
                    if (isFrameworkComponent(type)) {
                        critical.add(type);
                    }
                    // Event-related components
                    else if (EventBus.class.isAssignableFrom(type) || 
                            EventPublisher.class.isAssignableFrom(type)) {
                        critical.add(type);
                    }
                    // Health check essentials
                    else if (isHealthComponent(type)) {
                        critical.add(type);
                    }
                }
            }
            
            // Remover duplicados
            Set<Class<?>> uniqueCritical = new HashSet<>(critical);
            return new ArrayList<>(uniqueCritical);
            
        } catch (Exception e) {
            log.log(Level.WARNING, "⚠️ Error identificando componentes críticos: {0}", e.getMessage());
            return new ArrayList<>(); // Retornar lista vacía en caso de error
        }
    }
    
    /**
     * 🔍 Verificar si es componente del framework
     */
    private boolean isFrameworkComponent(Class<?> type) {
        return type.getName().startsWith("io.warmup.framework.")
                && (type.getName().contains(".core.")
                || type.getName().contains(".event.")
                || type.getName().contains(".health."));
    }
    
    /**
     * 🔍 Verificar si es componente de health
     */
    private boolean isHealthComponent(Class<?> type) {
        return type.getName().contains("Health")
                || type.getName().contains("Check");
    }
    
    /**
     * 🔍 Verificar si debe inicializar componente
     */
    private boolean shouldInitializeComponent(Class<?> clazz) {
        if (clazz == WarmupContainer.class || clazz == EventPublisher.class) {
            return false;
        }
        
        if (isLazyComponent(clazz)) {
            return false;
        }
        
        return true;
    }
    
    /**
     * 🔍 Verificar si es componente lazy
     */
    private boolean isLazyComponent(Class<?> clazz) {
        try {
            return AsmCoreUtils.isAnnotationPresentProgressive(clazz, 
                io.warmup.framework.annotation.Lazy.class);
        } catch (Exception e) {
            return false;
        }
    }
}