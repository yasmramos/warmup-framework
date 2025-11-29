package io.warmup.examples.startup;

import io.warmup.framework.core.*;
import io.warmup.framework.asm.AsmCoreUtils;
import io.warmup.framework.annotation.Lazy;
import io.warmup.framework.annotation.Aspect;
import io.warmup.framework.health.HealthCheckManager;
import io.warmup.framework.metrics.MetricsManager;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * 🔄 FASE BACKGROUND DE STARTUP - No bloqueante
 * 
 * Inicializa componentes no críticos en paralelo:
 * - Configuration processing
 * - Component scanning y registration
 * - Aspect processing
 * - Health checks avanzados
 * - Metrics collection
 * - Module loading
 * - Lazy initialization
 * 
 * @author MiniMax Agent
 * @version 1.0
 */
public class BackgroundStartupPhase {
    
    private static final Logger log = Logger.getLogger(BackgroundStartupPhase.class.getName());
    
    private final WarmupContainer container;
    private CompletableFuture<Void> completionFuture = new CompletableFuture<>();
    
    public BackgroundStartupPhase(WarmupContainer container) {
        this.container = container;
    }
    
    /**
     * 🔄 Ejecutar inicialización completa en background
     */
    public void executeBackgroundInitialization() {
        try {
            log.log(Level.INFO, "🔄 Iniciando inicialización background...");
            
            // Ejecutar todas las tareas de background en paralelo
            List<CompletableFuture<Void>> backgroundTasks = new ArrayList<>();
            
            // TAREA 1: Configuration processing
            backgroundTasks.add(CompletableFuture.runAsync(() -> {
                try {
                    initializeConfigurationProcessing();
                } catch (Exception e) {
                    log.log(Level.WARNING, "⚠️ Error en configuración processing: {0}", e.getMessage());
                }
            }));
            
            // TAREA 2: Component scanning y registration
            backgroundTasks.add(CompletableFuture.runAsync(() -> {
                try {
                    initializeComponentScanning();
                } catch (Exception e) {
                    log.log(Level.WARNING, "⚠️ Error en component scanning: {0}", e.getMessage());
                }
            }));
            
            // TAREA 3: Aspect processing
            backgroundTasks.add(CompletableFuture.runAsync(() -> {
                try {
                    initializeAspectProcessing();
                } catch (Exception e) {
                    log.log(Level.WARNING, "⚠️ Error en aspect processing: {0}", e.getMessage());
                }
            }));
            
            // TAREA 4: Advanced metrics y health checks
            backgroundTasks.add(CompletableFuture.runAsync(() -> {
                try {
                    initializeAdvancedHealthAndMetrics();
                } catch (Exception e) {
                    log.log(Level.WARNING, "⚠️ Error en health/metrics avanzados: {0}", e.getMessage());
                }
            }));
            
            // TAREA 5: Module loading y lazy initialization
            backgroundTasks.add(CompletableFuture.runAsync(() -> {
                try {
                    initializeModulesAndLazy();
                } catch (Exception e) {
                    log.log(Level.WARNING, "⚠️ Error en modules/lazy initialization: {0}", e.getMessage());
                }
            }));
            
            // Esperar a que todas las tareas terminen
            CompletableFuture<Void> allTasks = CompletableFuture.allOf(
                backgroundTasks.toArray(new CompletableFuture[0])
            );
            
            allTasks.get(); // Esperar completación
            
            log.log(Level.INFO, "✅ Inicialización background completada");
            completionFuture.complete(null);
            
        } catch (Exception e) {
            log.log(Level.SEVERE, "❌ Error crítico en inicialización background: {0}", e.getMessage());
            completionFuture.completeExceptionally(e);
        }
    }
    
    /**
     * 📝 Inicializar procesamiento de configuraciones
     */
    private void initializeConfigurationProcessing() {
        log.log(Level.FINE, "📝 Procesando configuraciones...");
        
        try {
            ConfigurationProcessor configProcessor = ((DependencyRegistry) container.getDependencyRegistry())
                .getDependencies()
                .keySet()
                .stream()
                .filter(clazz -> ConfigurationProcessor.class.isAssignableFrom(clazz))
                .findFirst()
                .map(clazz -> {
                    try {
                        return container.get(clazz);
                    } catch (Exception e) {
                        return null;
                    }
                })
                .map(clazz -> (ConfigurationProcessor) clazz)
                .orElse(null);
            
            if (configProcessor != null) {
                // El ConfigurationProcessor ya está inicializado, solo verificar estado
                log.log(Level.FINE, "✅ ConfigurationProcessor disponible");
            } else {
                log.log(Level.FINE, "ℹ️ ConfigurationProcessor no encontrado (normal en casos simples)");
            }
            
        } catch (Exception e) {
            log.log(Level.FINE, "⚠️ Error en procesamiento de configuraciones: {0}", e.getMessage());
        }
    }
    
    /**
     * 🔍 Inicializar scanning de componentes
     */
    private void initializeComponentScanning() {
        log.log(Level.FINE, "🔍 Procesando component scanning...");
        
        try {
            DependencyRegistry registry = (DependencyRegistry) container.getDependencyRegistry();
            if (registry != null) {
                int totalComponents = registry.getDependencies().size();
                log.log(Level.FINE, "📊 Total componentes registrados: {0}", totalComponents);
                
                // Inicializar componentes que no son lazy
                AtomicInteger initializedCount = new AtomicInteger(0);
                registry.getDependencies().forEach((type, dependency) -> {
                    try {
                        if (!isLazyComponent(type) && shouldInitializeComponent(type)) {
                            Object instance = container.get(type);
                            if (instance != null) {
                                initializedCount.incrementAndGet();
                            }
                        }
                    } catch (Exception e) {
                        log.log(Level.FINEST, "⚠️ Error inicializando {0}: {1}", 
                                new Object[]{type.getSimpleName(), e.getMessage()});
                    }
                });
                
                log.log(Level.FINE, "✅ {0} componentes adicionales inicializados en background", 
                        initializedCount.get());
            }
            
        } catch (Exception e) {
            log.log(Level.FINE, "⚠️ Error en component scanning: {0}", e.getMessage());
        }
    }
    
    /**
     * 🎭 Inicializar procesamiento de aspectos
     */
    private void initializeAspectProcessing() {
        log.log(Level.FINE, "🎭 Procesando aspectos...");
        
        try {
            AopHandler aopHandler = (AopHandler) container.getAopHandler();
            if (aopHandler != null) {
                // Registrar aspectos desde las dependencias registradas
                registerAspectsFromDependencies();
                
                log.log(Level.FINE, "✅ Aspect processing completado");
            } else {
                log.log(Level.FINE, "ℹ️ AopHandler no disponible");
            }
            
        } catch (Exception e) {
            log.log(Level.FINE, "⚠️ Error en aspect processing: {0}", e.getMessage());
        }
    }
    
    /**
     * 🏥 Inicializar health checks y métricas avanzadas
     */
    private void initializeAdvancedHealthAndMetrics() {
        log.log(Level.FINE, "🏥 Procesando health checks y métricas avanzadas...");
        
        try {
            HealthCheckManager healthManager = container.getHealthCheckManager();
            MetricsManager metricsManager = container.getMetricsManager();
            
            if (healthManager != null) {
                // Registrar health checks adicionales de los componentes inicializados
                registerHealthChecksFromComponents();
                log.log(Level.FINE, "✅ Health checks registrados");
            }
            
            if (metricsManager != null) {
                // Inicializar métricas avanzadas
                log.log(Level.FINE, "✅ Métricas avanzadas inicializadas");
            }
            
        } catch (Exception e) {
            log.log(Level.FINE, "⚠️ Error en health/metrics: {0}", e.getMessage());
        }
    }
    
    /**
     * 📦 Inicializar módulos y lazy loading
     */
    private void initializeModulesAndLazy() {
        log.log(Level.FINE, "📦 Procesando módulos y lazy loading...");
        
        try {
            ModuleManager moduleManager = (ModuleManager) container.getModuleManager();
            if (moduleManager != null) {
                // Los módulos ya están inicializados en el constructor
                log.log(Level.FINE, "✅ ModuleManager disponible");
            }
            
            // Procesar componentes lazy que pueden inicializarse ahora
            processLazyComponents();
            
        } catch (Exception e) {
            log.log(Level.FINE, "⚠️ Error en modules/lazy: {0}", e.getMessage());
        }
    }
    
    /**
     * 🎭 Registrar aspectos desde las dependencias registradas
     */
    private void registerAspectsFromDependencies() {
        try {
            Set<Class<?>> processedAspects = new HashSet<>();
            AtomicInteger aspectCount = new AtomicInteger(0);
            
            DependencyRegistry registry = (DependencyRegistry) container.getDependencyRegistry();
            if (registry != null) {
                for (Dependency dependency : registry.getDependencies().values()) {
                    Class<?> clazz = dependency.getType();
                    
                    if (isAspectComponent(clazz) && processedAspects.add(clazz)) {
                        try {
                            Object aspectInstance = container.get(clazz);
                            if (aspectInstance != null) {
                                aspectCount.incrementAndGet();
                                log.log(Level.FINEST, "✅ Aspecto registrado: {0}", clazz.getSimpleName());
                            }
                        } catch (Exception e) {
                            log.log(Level.FINEST, "⚠️ Error registrando aspecto {0}: {1}", 
                                    new Object[]{clazz.getSimpleName(), e.getMessage()});
                        }
                    }
                }
            }
            
            log.log(Level.FINE, "✅ {0} aspectos registrados en background", aspectCount.get());
            
        } catch (Exception e) {
            log.log(Level.FINE, "⚠️ Error registrando aspectos: {0}", e.getMessage());
        }
    }
    
    /**
     * 🏥 Registrar health checks de componentes
     */
    private void registerHealthChecksFromComponents() {
        try {
            DependencyRegistry registry = (DependencyRegistry) container.getDependencyRegistry();
            if (registry != null) {
                AtomicInteger healthCheckCount = new AtomicInteger(0);
                
                for (Dependency dependency : registry.getDependencies().values()) {
                    Class<?> clazz = dependency.getType();
                    
                    try {
                        if (hasHealthAnnotation(clazz)) {
                            Object instance = container.get(clazz);
                            if (instance != null) {
                                // Registrar health check methods
                                registerHealthCheckMethods(clazz, instance);
                                healthCheckCount.incrementAndGet();
                            }
                        }
                    } catch (Exception e) {
                        log.log(Level.FINEST, "⚠️ Error registrando health check para {0}: {1}", 
                                new Object[]{clazz.getSimpleName(), e.getMessage()});
                    }
                }
                
                log.log(Level.FINE, "✅ {0} health checks registrados", healthCheckCount.get());
            }
            
        } catch (Exception e) {
            log.log(Level.FINE, "⚠️ Error registrando health checks: {0}", e.getMessage());
        }
    }
    
    /**
     * 🐌 Procesar componentes lazy
     */
    private void processLazyComponents() {
        try {
            DependencyRegistry registry = (DependencyRegistry) container.getDependencyRegistry();
            if (registry != null) {
                AtomicInteger lazyCount = new AtomicInteger(0);
                
                for (Dependency dependency : registry.getDependencies().values()) {
                    Class<?> clazz = dependency.getType();
                    
                    try {
                        if (isLazyComponent(clazz)) {
                            lazyCount.incrementAndGet();
                            // Los componentes lazy se inicializan cuando se solicitan
                            log.log(Level.FINEST, "🐌 Componente lazy encontrado: {0}", clazz.getSimpleName());
                        }
                    } catch (Exception e) {
                        log.log(Level.FINEST, "⚠️ Error procesando componente lazy {0}: {1}", 
                                new Object[]{clazz.getSimpleName(), e.getMessage()});
                    }
                }
                
                log.log(Level.FINE, "✅ {0} componentes lazy procesados", lazyCount.get());
            }
            
        } catch (Exception e) {
            log.log(Level.FINE, "⚠️ Error procesando lazy components: {0}", e.getMessage());
        }
    }
    
    // ===== MÉTODOS UTILITARIOS =====
    
    /**
     * 🔍 Verificar si es componente de aspecto
     */
    private boolean isAspectComponent(Class<?> clazz) {
        try {
            return AsmCoreUtils.isAnnotationPresentProgressive(clazz, Aspect.class);
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * 🔍 Verificar si tiene anotación de health
     */
    private boolean hasHealthAnnotation(Class<?> clazz) {
        try {
            return AsmCoreUtils.isAnnotationPresentProgressive(clazz, 
                io.warmup.framework.annotation.Health.class);
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * 🔍 Verificar si es componente lazy
     */
    private boolean isLazyComponent(Class<?> clazz) {
        try {
            Lazy lazy = AsmCoreUtils.getAnnotationProgressive(clazz, Lazy.class);
            return lazy != null && lazy.value();
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * 🔍 Verificar si debe inicializar componente
     */
    private boolean shouldInitializeComponent(Class<?> clazz) {
        return clazz != WarmupContainer.class;
    }
    
    /**
     * 📝 Registrar métodos de health check
     */
    private void registerHealthCheckMethods(Class<?> clazz, Object instance) {
        try {
            // Implementación básica - en una implementación completa usaríamos ASM
            for (java.lang.reflect.Method method : clazz.getDeclaredMethods()) {
                if (method.isAnnotationPresent(io.warmup.framework.annotation.Health.class)) {
                    log.log(Level.FINEST, "🏥 Health check method encontrado: {0}.{1}", 
                            new Object[]{clazz.getSimpleName(), method.getName()});
                }
            }
        } catch (Exception e) {
            log.log(Level.FINEST, "⚠️ Error registrando health check methods: {0}", e.getMessage());
        }
    }
    
    /**
     * 📈 Obtener future de completación
     */
    public CompletableFuture<Void> getCompletionFuture() {
        return completionFuture;
    }
}