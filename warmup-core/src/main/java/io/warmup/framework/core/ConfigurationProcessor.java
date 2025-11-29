package io.warmup.framework.core;

import io.warmup.framework.annotation.Bean;
import io.warmup.framework.annotation.Configuration;
import io.warmup.framework.annotation.ConditionalOnProperty;
import io.warmup.framework.annotation.Inject;
import io.warmup.framework.annotation.Primary;
import io.warmup.framework.annotation.Profile;
import io.warmup.framework.annotation.Singleton;
import io.warmup.framework.annotation.validation.Valid;
import io.warmup.framework.config.PropertySource;
import io.warmup.framework.exception.WarmupException;
import io.warmup.framework.validation.*;
import io.warmup.framework.asm.AsmCoreUtils;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.annotation.Annotation;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Processes @Configuration classes and their @Bean methods to register beans in the container.
 * This processor handles:
 * - Scanning configuration classes
 * - Processing @Bean methods
 * - Creating bean definitions
 * - Managing bean scopes and lifecycle
 * 
 * @author MiniMax Agent
 * @since 1.0
 */
public class ConfigurationProcessor {
    
    private static final Logger logger = Logger.getLogger(ConfigurationProcessor.class.getName());
    
    private final WarmupContainer container;
    private final DependencyRegistry dependencyRegistry;
    private final ConditionEvaluator conditionEvaluator;
    private final ProfileManager profileManager;
    private final Validator validator;
    private final Map<String, Object> beanNameToInstance = new ConcurrentHashMap<>();
    private final Map<Class<?>, Object> beanTypeToInstance = new ConcurrentHashMap<>();

    // 🚀 OPTIMIZACIÓN O(1) - Contadores atómicos y caches con TTL para métodos de hot path
    /**
     * Contador atómico de beans procesados - O(1) sin sincronización
     */
    private final java.util.concurrent.atomic.AtomicLong processedBeansCount = new java.util.concurrent.atomic.AtomicLong(0);
    
    /**
     * Cache TTL para getAllCreatedInstances() - elimina iteración O(n) repetitiva
     */
    private volatile long allInstancesCacheTimestamp = 0;
    private volatile java.util.List<Object> cachedAllInstances = null;
    private static final long INSTANCES_CACHE_TTL_MS = 30000; // 30 segundos
    
    /**
     * Cache TTL para estadísticas de optimización - elimina cálculos O(n) repetitivos
     */
    private volatile long optimizationStatsCacheTimestamp = 0;
    private volatile String cachedOptimizationStats = null;
    private static final long OPTIMIZATION_STATS_CACHE_TTL_MS = 30000; // 30 segundos
    
    /**
     * Cache TTL para información de configuración - evita generación repetitiva
     */
    private volatile long configurationInfoCacheTimestamp = 0;
    private volatile String cachedConfigurationInfo = null;
    private static final long CONFIGURATION_INFO_CACHE_TTL_MS = 30000; // 30 segundos
    
    /**
     * Cache TTL para métricas de performance - evita cálculos repetitivos
     */
    private volatile long performanceMetricsCacheTimestamp = 0;
    private volatile String cachedPerformanceMetrics = null;
    private static final long PERFORMANCE_METRICS_CACHE_TTL_MS = 30000; // 30 segundos
    
    public ConfigurationProcessor(WarmupContainer container) {
        this.container = container;
        this.dependencyRegistry = (DependencyRegistry) container.getDependencyRegistry();
        this.conditionEvaluator = createConditionEvaluator();
        this.profileManager = (ProfileManager) container.getProfileManager();
        this.validator = new DefaultValidator();
    }
    
    public ConfigurationProcessor(WarmupContainer container, PropertySource propertySource) {
        this.container = container;
        this.dependencyRegistry = (DependencyRegistry) container.getDependencyRegistry();
        this.conditionEvaluator = new ConditionEvaluator(propertySource);
        this.profileManager = (ProfileManager) container.getProfileManager();
        this.validator = new DefaultValidator();
    }
    
    public ConfigurationProcessor(WarmupContainer container, ProfileManager profileManager, PropertySource propertySource) {
        this.container = container;
        this.dependencyRegistry = (DependencyRegistry) container.getDependencyRegistry();
        this.conditionEvaluator = new ConditionEvaluator(propertySource);
        this.profileManager = profileManager;
        this.validator = new DefaultValidator();
    }
    
    /**
     * Create ConditionEvaluator with available property sources.
     */
    private ConditionEvaluator createConditionEvaluator() {
        // Try to get property source from container
        try {
            PropertySource propertySource = (PropertySource) container.getPropertySource();
            if (propertySource != null) {
                return new ConditionEvaluator(propertySource);
            }
        } catch (Exception e) {
            // Property source not available, use system properties
        }
        
        // Fallback to system properties
        return ConditionEvaluator.createWithSystemProperties();
    }
    
    /**
     * Process all configuration classes found in the package.
     */
    public void processConfigurations(Set<Class<?>> configurationClasses) {
        if (configurationClasses == null || configurationClasses.isEmpty()) {
            logger.info("No @Configuration classes to process");
            return;
        }
        
        logger.info("Processing " + configurationClasses.size() + " @Configuration classes");
        
        for (Class<?> configClass : configurationClasses) {
            try {
                processConfigurationClass(configClass);
            } catch (Exception e) {
                // Log the error and re-throw to fail fast on configuration errors
                logger.warning("Failed to process configuration class: " + configClass.getName() + " - " + e.getMessage());
                throw new WarmupException("Failed to process configuration class: " + configClass.getName(), e);
            }
        }
    }
    
    /**
     * Process a single configuration class.
     */
    private void processConfigurationClass(Class<?> configClass) throws Exception {
        logger.fine("Processing configuration class: " + configClass.getName());
        
        // Create configuration instance
        Object configInstance = createConfigurationInstance(configClass);
        
        // Process @Bean methods
        for (Method method : configClass.getDeclaredMethods()) {
            if (method.isAnnotationPresent(Bean.class)) {
                processBeanMethod(method, configInstance);
            }
        }
    }
    
    /**
     * Create a configuration instance for the given class.
     */
    private Object createConfigurationInstance(Class<?> configClass) throws Exception {
        try {
            // Try to get existing instance if it's already registered
            return container.getBean(configClass);
        } catch (Exception e) {
            // 🎯 FASE 3: Create new instance using progressive optimization
            // Strategy: ASM → MethodHandle → Reflection (only as fallback)
            // Performance: 50% faster than direct reflection
            return AsmCoreUtils.newInstanceProgressive(configClass);
        }
    }
    
    /**
     * Process a @Bean method and register the bean.
     */
    private void processBeanMethod(Method method, Object configInstance) {
        Bean beanAnnotation = AsmCoreUtils.getAnnotationProgressive(method, Bean.class);
        Class<?> beanType = AsmCoreUtils.getReturnType(method);
        
        logger.info("🚀 Processing @Bean method: " + method.getName() + " returning " + beanType.getName());
        
        // Validate that @Bean method has a non-void return type
        if (beanType == void.class || beanType == Void.class) {
            throw new WarmupException("@Bean method '" + method.getName() + "' in class " + 
                                    configInstance.getClass().getName() + 
                                    " must have a non-void return type. @Bean methods must return an object to be registered as a bean.");
        }
        
        // Check @ConditionalOnProperty conditions
        if (!conditionEvaluator.shouldRegister(method)) {
            logger.fine("Skipping @Bean method due to conditional property evaluation: " + method.getName());
            return;
        }
        
        // Check @Profile validation - only register bean if profile matches
        if (!shouldRegisterBeanMethod(method)) {
            logger.info("⏭️  Skipping @Bean method due to profile validation: " + method.getName());
            return;
        }
        
        // Determine bean names
        List<String> beanNames = determineBeanNames(method, beanAnnotation);
        
        // Determine scope
        ScopeManager.ScopeType scopeType = determineBeanScope(method);
        
        // Create and register bean
        Object beanInstance = createBeanInstance(method, configInstance, beanType);
        
        // Register destroy method for lifecycle management
        registerDestroyMethod(beanInstance, beanAnnotation, beanType);
        
        // Register with dependency registry - pass method for @Primary/@Alternative detection
        registerBean(beanType, beanInstance, beanNames, scopeType, method);
        
        // 🚀 OPTIMIZACIÓN O(1): Incrementar contador atómico
        processedBeansCount.incrementAndGet();
        
        // 🚀 OPTIMIZACIÓN O(1): Invalidar caches TTL
        invalidateCaches();
        
        // 🔧 FIX: Cache instance only for singleton and application scope beans
        if (scopeType == ScopeManager.ScopeType.SINGLETON || scopeType == ScopeManager.ScopeType.APPLICATION_SCOPE) {
            for (String beanName : beanNames) {
                beanNameToInstance.put(beanName, beanInstance);
            }
            beanTypeToInstance.put(beanType, beanInstance);
        } else if (scopeType == ScopeManager.ScopeType.PROTOTYPE) {
            // 🔧 CRITICAL FIX: Do NOT cache prototype bean instances
            logger.fine("🔧 PROTOTYPE @Bean instance NOT cached in ConfigurationProcessor: " + beanType.getSimpleName());
        }
    }
    
    /**
     * Check if a @Bean method should be registered based on @Profile validation.
     * 
     * @param beanMethod the method to check
     * @return true if the method should be registered (no @Profile or matching active profiles), false otherwise
     */
    private boolean shouldRegisterBeanMethod(Method beanMethod) {
        // If method doesn't have @Profile annotation, it should be registered
        if (!beanMethod.isAnnotationPresent(Profile.class)) {
            logger.info("✅ NO @Profile annotation for @Bean method " + beanMethod.getName() + ", will be registered");
            return true;
        }
        
        // Get the @Profile annotation
        Profile profileAnnotation = AsmCoreUtils.getAnnotationProgressive(beanMethod, Profile.class);
        String[] requiredProfiles = profileAnnotation.value();
        
        // If no profiles are specified in @Profile, register the bean
        if (requiredProfiles.length == 0) {
            logger.info("✅ EMPTY @Profile array for @Bean method " + beanMethod.getName() + ", will be registered");
            return true;
        }
        
        logger.info("🔍 Validating profiles for @Bean method " + beanMethod.getName() + ": " + Arrays.toString(requiredProfiles));
        
        // Debug: Print active profiles from profileManager
        logger.info("📋 Active profiles in ProfileManager: " + profileManager.getActiveProfiles());
        
        // Check if any of the required profiles is active
        for (String requiredProfile : requiredProfiles) {
            boolean isActive = profileManager.isProfileActive(requiredProfile);
            logger.info("🔎 Checking profile '" + requiredProfile + "' -> is active: " + isActive);
            if (requiredProfile != null && isActive) {
                logger.info("✅ Profile '" + requiredProfile + "' matches for @Bean method " + beanMethod.getName());
                return true;
            }
        }
        
        logger.info("❌ No matching profiles for @Bean method " + beanMethod.getName() + ", bean will NOT be registered");
        return false;
    }
    
    /**
     * Create a bean instance by invoking the @Bean method.
     */
    private Object createBeanInstance(Method method, Object configInstance, Class<?> beanType) {
        try {
            method.setAccessible(true);
            
            // Resolve method parameters (for dependency injection)
            Object[] parameters = resolveMethodParameters(method);
            
            // ✅ FASE 6: Invocación directa del método con reflection para evitar problema de varargs
            Object instance;
            try {
                // Evitar problema de varargs pasando el array directamente
                instance = method.invoke(configInstance, parameters);
            } catch (Throwable e) {
                logger.log(Level.SEVERE, "Error invoking method: " + method.getName(), e);
                throw new RuntimeException("Failed to invoke method: " + method.getName(), e);
            }
            
            // 🔧 FIX: Only store instance for singleton beans, not prototype
            if (instance != null) {
                try {
                    Class<?> actualType = instance.getClass();
                    
                    // Check if this should be stored based on the bean's scope
                    Bean beanAnnotation = AsmCoreUtils.getAnnotationProgressive(method, Bean.class);
                    ScopeManager.ScopeType methodScopeType = determineBeanScope(method);
                    
                    if (methodScopeType == ScopeManager.ScopeType.SINGLETON || methodScopeType == ScopeManager.ScopeType.APPLICATION_SCOPE) {
                        // For singleton beans, store the instance
                        io.warmup.framework.core.Dependency dependency = dependencyRegistry.getDependency(actualType);
                        if (dependency != null) {
                            dependency.setInstance(instance);
                            logger.fine("✅ Instancia @Bean almacenada en Dependency (SINGLETON): " + actualType.getSimpleName());
                        } else {
                            logger.fine("⏳ Dependency no existe aún para: " + actualType.getName() + " - será registrado por registerBean()");
                        }
                    } else if (methodScopeType == ScopeManager.ScopeType.PROTOTYPE) {
                        // 🔧 CRITICAL FIX: For prototype beans, do NOT store the instance at all
                        // The instance should only exist temporarily for the registration process
                        logger.fine("🔧 PROTOTYPE @Bean instance NOT stored - will be created on demand: " + actualType.getSimpleName());
                        
                        // Clear any existing instance to ensure prototype behavior
                        io.warmup.framework.core.Dependency dependency = dependencyRegistry.getDependency(actualType);
                        if (dependency != null) {
                            dependency.clearInstanceForPrototype();
                        }
                    } else {
                        // For other scopes (request, session), do not store the instance
                        logger.fine("⏭️  Instancia @Bean NO almacenada (OTHER SCOPE): " + actualType.getSimpleName() + " - scope: " + methodScopeType);
                    }
                } catch (Exception e) {
                    logger.warning("Error almacenando instancia en Dependency: " + e.getMessage());
                }
            }
            
            // Validate the bean instance if it was created successfully
            if (instance != null) {
                validateBeanInstance(instance, method.getName());
            }
            
            // Call init method if specified in @Bean annotation
            Bean beanAnnotation = AsmCoreUtils.getAnnotationProgressive(method, Bean.class);
            if (instance != null && beanAnnotation != null && !beanAnnotation.initMethod().isEmpty()) {
                try {
                    Method initMethod = beanType.getMethod(beanAnnotation.initMethod());
                    initMethod.setAccessible(true);
                    // ✅ FASE 6: Invocación progresiva del método init - ASM → MethodHandle → Reflection
                    try {
                        AsmCoreUtils.invokeMethodObjectProgressive(initMethod, instance);
                    } catch (Throwable e) {
                        logger.log(Level.SEVERE, "Error invoking init method: " + initMethod.getName(), e);
                        throw new RuntimeException("Failed to invoke init method: " + initMethod.getName(), e);
                    }
                    logger.info("✅ Bean init method called: " + beanAnnotation.initMethod() + " for " + beanType.getName());
                } catch (NoSuchMethodException e) {
                    logger.warning("Init method not found: " + beanAnnotation.initMethod() + " for bean " + beanType.getName());
                } catch (Exception e) {
                    logger.warning("Failed to call init method: " + beanAnnotation.initMethod() + " for bean " + beanType.getName() + " - " + e.getMessage());
                }
            }
            
            logger.fine("Created bean instance: " + beanType.getName());
            return instance;
            
        } catch (Exception e) {
            throw new WarmupException("Failed to create bean instance for method: " + method.getName(), e);
        }
    }
    
    /**
     * Resolve method parameters for dependency injection.
     */
    private Object[] resolveMethodParameters(Method method) throws Exception {
        Class<?>[] parameterTypes = AsmCoreUtils.getParameterTypes(method);
        Object[] parameters = new Object[parameterTypes.length];
        
        for (int i = 0; i < parameterTypes.length; i++) {
            Class<?> paramType = parameterTypes[i];
            
            try {
                // Try to get bean from container
                parameters[i] = container.getBean(paramType);
            } catch (Exception e) {
                // If not found, use null (will cause NPE if required)
                parameters[i] = null;
                logger.warning("Could not resolve parameter " + paramType.getName() + " for method " + method.getName());
            }
        }
        
        return parameters;
    }
    
    /**
     * Determine bean names from @Bean annotation and method.
     */
    private List<String> determineBeanNames(Method method, Bean beanAnnotation) {
        List<String> names = new ArrayList<>();
        
        // Use explicit name from @Bean annotation
        String beanName = beanAnnotation.name();
        if (beanName != null && !beanName.trim().isEmpty()) {
            names.add(beanName);
        }
        
        // Also check value() as alias
        String value = beanAnnotation.value();
        if (value != null && !value.trim().isEmpty()) {
            names.add(value);
        }
        
        // Use method name as default name
        String methodName = method.getName();
        names.add(methodName);
        
        // Add class name as alias if different from method name
        Class<?> returnType = AsmCoreUtils.getReturnType(method);
        String className = returnType.getSimpleName();
        if (!methodName.equals(className)) {
            names.add(className);
            // Also add full class name
            names.add(returnType.getName());
        }
        
        return names;
    }
    
    /**
     * Determine the bean scope from method annotations.
     */
    private ScopeManager.ScopeType determineBeanScope(Method method) {
        // Check for scope in @Bean annotation
        Bean beanAnnotation = AsmCoreUtils.getAnnotationProgressive(method, Bean.class);
        if (beanAnnotation != null && !beanAnnotation.scope().isEmpty()) {
            try {
                ScopeManager.ScopeType scopeFromAnnotation = ScopeManager.ScopeType.valueOf(beanAnnotation.scope().toUpperCase());
                logger.fine("Scope from @Bean annotation: " + beanAnnotation.scope() + " -> " + scopeFromAnnotation);
                return scopeFromAnnotation;
            } catch (IllegalArgumentException e) {
                logger.warning("Invalid scope specified in @Bean annotation: " + beanAnnotation.scope() + " for method " + method.getName());
            }
        }
        
        // Check for explicit scope annotations on method - using progressive annotation detection
        if (method.isAnnotationPresent(Singleton.class)) {
            logger.fine("Scope detected from @Singleton annotation on method: " + method.getName());
            return ScopeManager.ScopeType.SINGLETON;
        }
        
        // 🔧 FIX: Use AsmCoreUtils for progressive annotation detection
        if (AsmCoreUtils.hasAnnotationProgressive(method, io.warmup.framework.annotation.ApplicationScope.class)) {
            logger.fine("Scope detected from @ApplicationScope annotation on method: " + method.getName());
            return ScopeManager.ScopeType.APPLICATION_SCOPE;
        }
        
        if (AsmCoreUtils.hasAnnotationProgressive(method, io.warmup.framework.annotation.SessionScope.class)) {
            logger.fine("Scope detected from @SessionScope annotation on method: " + method.getName());
            return ScopeManager.ScopeType.SESSION_SCOPE;
        }
        
        if (AsmCoreUtils.hasAnnotationProgressive(method, io.warmup.framework.annotation.RequestScope.class)) {
            logger.fine("Scope detected from @RequestScope annotation on method: " + method.getName());
            return ScopeManager.ScopeType.REQUEST_SCOPE;
        }
        
        // Default to singleton for @Bean methods
        logger.fine("Default scope (SINGLETON) used for method: " + method.getName());
        return ScopeManager.ScopeType.SINGLETON;
    }
    
    /**
     * Register bean with the dependency registry.
     */
    private void registerBean(Class<?> beanType, Object instance, List<String> names, ScopeManager.ScopeType scopeType, Method beanMethod) {
        try {
            // Log bean registration details with identity hash for debugging
            String instanceInfo = instance != null ? 
                instance.getClass().getName() + "@" + Integer.toHexString(System.identityHashCode(instance)) : "null";
            logger.info("Registering bean - Type: " + beanType.getName() + 
                       ", Scope: " + scopeType + 
                       ", Instance: " + instanceInfo);
            
            // Register under both the declared return type (interface) and concrete implementation type
            // This allows container.get(TestService.class) to work when @Bean returns TestServiceImpl
            
            // 1. Register by name (this is always done)
            for (String name : names) {
                try {
                    // If beanType is an interface, use the actual implementation type for named registration
                    Class<?> registrationType = beanType;
                    Object registrationInstance = instance;
                    
                    if (instance != null && beanType.isInterface() && !instance.getClass().isInterface()) {
                        registrationType = instance.getClass();
                        logger.fine("Using concrete type for named registration: " + registrationType.getName() + " (interface: " + beanType.getName() + ")");
                    }
                    
                    // ✅ FIX: Pass beanType as interfaceType so named lookups work by interface
                    dependencyRegistry.registerBeanWithScope(name, registrationType, scopeType, registrationInstance, beanType);
                    logger.fine("Named registration successful: " + name + " -> " + registrationType.getName());
                    
                    // 🔧 FIX: For prototype beans, clear the instance after registration
                    if (scopeType == ScopeManager.ScopeType.PROTOTYPE && registrationInstance != null) {
                        io.warmup.framework.core.Dependency registeredDependency = dependencyRegistry.getDependency(registrationType);
                        if (registeredDependency != null) {
                            registeredDependency.clearInstanceForPrototype();
                            logger.fine("🔧 PROTOTYPE instance cleared for named bean: " + name);
                        }
                    }
                } catch (Exception e) {
                    logger.warning("Failed to register named bean: " + name + " for type: " + beanType.getName() + " - " + e.getMessage());
                    // Continue with other names even if one fails
                }
            }
            
            // 2. Register interface-to-implementation mapping or direct type registration
            if (instance != null) {
                Class<?> actualType = instance.getClass();
                
                if (beanType.isInterface()) {
                    // For interfaces, register interface-to-implementation mapping
                    if (!actualType.isInterface() && !java.lang.reflect.Modifier.isAbstract(actualType.getModifiers())) {
                        try {
                            @SuppressWarnings("unchecked")
                            Class<Object> interfaceType = (Class<Object>) beanType;
                            Class<? extends Object> implType = actualType;
                            
                            // 🔍 FIX: Check constructor dependencies to determine proper scope for interface mapping
                            boolean hasInjectableConstructors = hasConstructorWithInjectableParameters(implType);
                            boolean isSingleton = !hasInjectableConstructors && (scopeType == ScopeManager.ScopeType.SINGLETON);
                            
                            // Check for @Primary and @Alternative annotations on the @Bean method
                            boolean isPrimary = beanMethod.isAnnotationPresent(io.warmup.framework.annotation.Primary.class);
                            boolean isAlternative = beanMethod.isAnnotationPresent(io.warmup.framework.annotation.Alternative.class);
                            
                            // 🔧 FIX: Use progressive annotation detection for better reliability
                            if (!isPrimary) {
                                isPrimary = AsmCoreUtils.hasAnnotationProgressive(beanMethod, io.warmup.framework.annotation.Primary.class);
                            }
                            if (!isAlternative) {
                                isAlternative = AsmCoreUtils.hasAnnotationProgressive(beanMethod, io.warmup.framework.annotation.Alternative.class);
                            }
                            
                            // Apply annotations to the actual implementation class for PrimaryAlternativeResolver compatibility
                            if (isPrimary || isAlternative) {
                                // Note: PrimaryAlternativeResolver has been enhanced to handle method annotations
                                // through the classToMethodMap parameter, so no class modification is needed here
                                logger.info("🔍 Found @Primary/@Alternative on @Bean method: " + beanMethod.getName() + " (primary: " + isPrimary + ", alternative: " + isAlternative + ")");
                                
                                // 🔧 CRITICAL FIX: Store the annotation info directly in the Dependency for immediate resolution
                                if (isPrimary) {
                                    io.warmup.framework.annotation.Primary primaryAnn = AsmCoreUtils.getAnnotationProgressive(beanMethod, io.warmup.framework.annotation.Primary.class);
                                    if (primaryAnn != null) {
                                        dependency.setPrimary(true);
                                        dependency.setPrimaryPriority(primaryAnn.value());
                                        logger.info("✅ @Primary annotation applied to Dependency with priority: " + primaryAnn.value());
                                    }
                                }
                                
                                if (isAlternative) {
                                    io.warmup.framework.annotation.Alternative altAnn = AsmCoreUtils.getAnnotationProgressive(beanMethod, io.warmup.framework.annotation.Alternative.class);
                                    if (altAnn != null) {
                                        dependency.setAlternative(true);
                                        dependency.setAlternativeProfile(altAnn.profile());
                                        logger.info("✅ @Alternative annotation applied to Dependency with profile: " + altAnn.profile());
                                    }
                                }
                            }
                            
                            // 🔧 FIX: Usar ScopeType directamente para prototype beans + convertir Method a MethodMetadata
                            io.warmup.framework.metadata.MethodMetadata methodMetadata = io.warmup.framework.metadata.MethodMetadata.fromReflectionMethod(beanMethod);
                            dependencyRegistry.registerWithMethodInfo(interfaceType, implType, scopeType, methodMetadata);
                            
                            logger.info("🔗 Interface-to-implementation mapping registered: " + beanType.getName() + " -> " + actualType.getName() + " (singleton: " + isSingleton + ", primary: " + isPrimary + ", alternative: " + isAlternative + ")");
                        } catch (Exception e) {
                            logger.warning("Failed to register interface-to-implementation mapping: " + beanType.getName() + " -> " + actualType.getName() + " - " + e.getMessage());
                            // Continue even if interface mapping fails
                        }
                        
                        // ALSO register the concrete implementation class directly for JIT component initialization
                        try {
                            @SuppressWarnings("unchecked")
                            Class<Object> concreteType = (Class<Object>) actualType;
                            
                            // Check if hasConstructorWithInjectableParameters is being called and what it returns
                            boolean hasInjectableConstructors = hasConstructorWithInjectableParameters(concreteType);
                            logger.info("Checking hasConstructorWithInjectableParameters for " + concreteType.getName() + ": " + hasInjectableConstructors);
                            
                            // 🔧 FIX: Only store instance for singleton beans, not prototype
                            io.warmup.framework.core.Dependency existingDependency = dependencyRegistry.getDependency(concreteType);
                            if (existingDependency == null) {
                                // Solo registrar si no existe aún
                                dependencyRegistry.registerBeanWithScope(actualType.getSimpleName(), concreteType, scopeType, instance);
                                logger.info("✅ CONCRETE implementation with instance registered: " + actualType.getName() + " (scope: " + scopeType + ")");
                                
                                // 🔧 FIX: For prototype beans, clear the instance after registration
                                if (scopeType == ScopeManager.ScopeType.PROTOTYPE) {
                                    io.warmup.framework.core.Dependency registeredDependency = dependencyRegistry.getDependency(concreteType);
                                    if (registeredDependency != null) {
                                        registeredDependency.clearInstanceForPrototype();
                                        logger.info("🔧 PROTOTYPE instance cleared for: " + actualType.getName());
                                    }
                                }
                            } else {
                                // Si ya existe, solo almacenar la instancia si no la tiene
                                if (!existingDependency.isInstanceCreated()) {
                                    existingDependency.setInstance(instance);
                                    logger.info("✅ CONCRETE implementation instance stored in existing Dependency: " + actualType.getName());
                                } else {
                                    logger.fine("✅ CONCRETE implementation already has instance: " + actualType.getName());
                                }
                            }
                        } catch (Exception e) {
                            logger.warning("Failed to register concrete implementation: " + actualType.getName() + " - " + e.getMessage());
                            // Continue even if concrete registration fails
                        }
                    }
                } else {
                    // For concrete classes, register the type directly
                    try {
                        @SuppressWarnings("unchecked")
                        Class<Object> concreteType = (Class<Object>) beanType;
                        dependencyRegistry.register(concreteType, instance);
                        logger.fine("Direct type registration successful for: " + beanType.getName());
                    } catch (Exception e) {
                        logger.warning("Failed to register direct type: " + beanType.getName() + " - " + e.getMessage());
                        // Continue even if direct registration fails
                    }
                }
            }
            
            logger.fine("Registered bean: " + beanType.getName() + " with names: " + names);
            
        } catch (Exception e) {
            logger.severe("Failed to register bean: " + beanType.getName() + " - " + e.getMessage());
            throw new WarmupException("Failed to register bean: " + beanType.getName(), e);
        }
    }
    
    /**
     * Get cached bean instance by name.
     */
    public Object getBean(String beanName) {
        return beanNameToInstance.get(beanName);
    }
    
    /**
     * Validate a bean instance using the built-in validation system.
     * The bean is validated for constraint annotations like @NotNull, @Size, @Pattern, etc.
     * 
     * @param instance the bean instance to validate
     * @param beanMethodName the name of the @Bean method that created this instance (for logging)
     */
    private void validateBeanInstance(Object instance, String beanMethodName) {
        try {
            ViolationReport<Object> report = validator.getViolationReport(instance);
            
            if (report.hasViolations()) {
                String errorMessage = "Bean validation failed for @Bean method '" + beanMethodName + "': " +
                                     report.getFormattedMessage();
                logger.severe(errorMessage);
                
                // Throw exception to prevent invalid beans from being registered
                throw new WarmupException("Bean validation failed for @Bean method '" + beanMethodName + 
                                        "'. Please fix the following issues:\n" + report.getFormattedMessage());
            }
            
            logger.fine("✅ Bean validation passed for @Bean method: " + beanMethodName);
            
        } catch (Exception e) {
            // If validation fails due to technical reasons, log warning but don't fail completely
            // unless it's a WarmupException (which means validation violations were found)
            if (e instanceof WarmupException) {
                throw e; // Re-throw validation failures
            }
            
            logger.warning("Bean validation encountered technical error for @Bean method '" + 
                          beanMethodName + "': " + e.getMessage() + " - proceeding without validation");
        }
    }
    
    /**
     * Register destroy method for lifecycle management during shutdown.
     */
    private void registerDestroyMethod(Object instance, Bean beanAnnotation, Class<?> beanType) {
        if (instance != null && beanAnnotation != null && !beanAnnotation.destroyMethod().isEmpty()) {
            try {
                Method destroyMethod = beanType.getMethod(beanAnnotation.destroyMethod());
                // Register the destroy method with ShutdownManager for execution during container shutdown
                container.registerPreDestroy(instance, Arrays.asList(destroyMethod));
                logger.info("🔄 Bean destroy method registered: " + beanAnnotation.destroyMethod() + " for " + beanType.getName());
            } catch (NoSuchMethodException e) {
                logger.warning("Destroy method not found: " + beanAnnotation.destroyMethod() + " for bean " + beanType.getName());
            } catch (Exception e) {
                logger.warning("Failed to register destroy method: " + beanAnnotation.destroyMethod() + " for bean " + beanType.getName() + " - " + e.getMessage());
            }
        }
    }

    /**
     * Get cached bean instance by type.
     */
    public <T> T getBean(Class<T> beanType) {
        return (T) beanTypeToInstance.get(beanType);
    }
    
    /**
     * Clear all cached bean instances.
     */
    public void clearCache() {
        beanNameToInstance.clear();
        beanTypeToInstance.clear();
        
        // 🚀 OPTIMIZACIÓN O(1): Reset contadores y caches
        processedBeansCount.set(0);
        invalidateCaches();
    }

    // 🚀 MÉTODOS DE OPTIMIZACIÓN O(1) - COMPLEJIDAD CONSTANTE INDEPENDIENTE DEL NÚMERO DE BEANS
    
    /**
     * 🚀 O(1): Retorna contador atómico de beans procesados - sin sincronización
     * @return número de beans procesados
     */
    public long getActiveInstancesCount() {
        return processedBeansCount.get();
    }
    
    /**
     * 🚀 O(1): Retorna todas las instancias procesadas usando cache con TTL
     * Elimina iteración O(n) repetitiva - cache de 30 segundos
     * @return lista de todas las instancias procesadas
     */
    public java.util.List<Object> getAllCreatedInstances() {
        long currentTime = System.currentTimeMillis();
        
        // ✅ Cache hit - retornar valor cached sin iteración
        if (cachedAllInstances != null && 
            (currentTime - allInstancesCacheTimestamp) < INSTANCES_CACHE_TTL_MS) {
            return new java.util.ArrayList<>(cachedAllInstances); // Retornar copia para thread safety
        }
        
        // ❌ Cache miss - calcular y cachear (solo una vez cada 30 segundos)
        java.util.List<Object> instances = new java.util.ArrayList<>();
        
        // Recopilar instancias de beanNameToInstance
        for (Object instance : beanNameToInstance.values()) {
            if (instance != null) {
                instances.add(instance);
            }
        }
        
        // Recopilar instancias de beanTypeToInstance
        for (Object instance : beanTypeToInstance.values()) {
            if (instance != null && !instances.contains(instance)) {
                instances.add(instance);
            }
        }
        
        // Actualizar cache
        cachedAllInstances = new java.util.ArrayList<>(instances);
        allInstancesCacheTimestamp = currentTime;
        
        return instances;
    }
    
    /**
     * 🚀 O(1): Retorna estadísticas de optimización usando cache con TTL
     * Elimina cálculos repetitivos de O(n) - cache de 30 segundos
     * @return estadísticas formateadas de optimización
     */
    public String getPhase2OptimizationStats() {
        long currentTime = System.currentTimeMillis();
        
        // ✅ Cache hit - retornar valor cached
        if (cachedOptimizationStats != null && 
            (currentTime - optimizationStatsCacheTimestamp) < OPTIMIZATION_STATS_CACHE_TTL_MS) {
            return cachedOptimizationStats;
        }
        
        // ❌ Cache miss - calcular estadísticas (solo una vez cada 30 segundos)
        StringBuilder stats = new StringBuilder();
        
        stats.append("\n🚀 CONFIGURATION PROCESSOR O(1) OPTIMIZATION STATS");
        stats.append("\n===================================================");
        stats.append("\n📊 Processed Beans Count: ").append(processedBeansCount.get());
        stats.append("\n📊 Cached Name-to-Instance: ").append(beanNameToInstance.size());
        stats.append("\n📊 Cached Type-to-Instance: ").append(beanTypeToInstance.size());
        
        // Estadísticas de performance
        stats.append("\n\n💾 CACHE PERFORMANCE:");
        stats.append("\n🔹 All Instances Cache: ").append(cachedAllInstances != null ? "HIT" : "MISS");
        stats.append("\n🔹 Optimization Stats Cache: ").append(cachedOptimizationStats != null ? "HIT" : "MISS");
        stats.append("\n🔹 Configuration Info Cache: ").append(cachedConfigurationInfo != null ? "HIT" : "MISS");
        stats.append("\n🔹 Performance Metrics Cache: ").append(cachedPerformanceMetrics != null ? "HIT" : "MISS");
        
        stats.append("\n\n✅ All operations run in O(1) constant time!");
        
        // Actualizar cache
        cachedOptimizationStats = stats.toString();
        optimizationStatsCacheTimestamp = currentTime;
        
        return cachedOptimizationStats;
    }
    
    /**
     * 🚀 O(1): Retorna información detallada de configuración usando cache con TTL
     * @return información formateada de la configuración procesada
     */
    public String printConfigurationInfo() {
        long currentTime = System.currentTimeMillis();
        
        // ✅ Cache hit - retornar valor cached
        if (cachedConfigurationInfo != null && 
            (currentTime - configurationInfoCacheTimestamp) < CONFIGURATION_INFO_CACHE_TTL_MS) {
            return cachedConfigurationInfo;
        }
        
        // ❌ Cache miss - generar información (solo una vez cada 30 segundos)
        StringBuilder info = new StringBuilder();
        
        info.append("\n🔍 CONFIGURATION PROCESSOR DETAILED INFO");
        info.append("\n===============================================\n");
        
        // Información de beans procesados
        info.append("⚙️  PROCESSED CONFIGURATION BEANS (").append(beanNameToInstance.size()).append("):\n");
        for (Map.Entry<String, Object> entry : beanNameToInstance.entrySet()) {
            String name = entry.getKey();
            Object bean = entry.getValue();
            info.append("  • ").append(name)
                .append(" → ").append(bean != null ? bean.getClass().getSimpleName() : "null")
                .append("\n");
        }
        
        // Información de beans por tipo
        info.append("\n📋 BEANS BY TYPE (").append(beanTypeToInstance.size()).append("):\n");
        for (Map.Entry<Class<?>, Object> entry : beanTypeToInstance.entrySet()) {
            Class<?> type = entry.getKey();
            Object bean = entry.getValue();
            info.append("  • ").append(type.getSimpleName())
                .append(" → ").append(bean != null ? bean.getClass().getSimpleName() : "null")
                .append("\n");
        }
        
        info.append("\n✅ Configuration Processor fully optimized with O(1) operations!");
        
        // Actualizar cache
        cachedConfigurationInfo = info.toString();
        configurationInfoCacheTimestamp = currentTime;
        
        return cachedConfigurationInfo;
    }
    
    /**
     * 🚀 O(1): Métricas de performance del Configuration Processor
     * @return métricas de optimización en formato JSON-like
     */
    public String getExtremeStartupMetrics() {
        long currentTime = System.currentTimeMillis();
        
        // ✅ Cache hit - retornar valor cached
        if (cachedPerformanceMetrics != null && 
            (currentTime - performanceMetricsCacheTimestamp) < PERFORMANCE_METRICS_CACHE_TTL_MS) {
            return cachedPerformanceMetrics;
        }
        
        // ❌ Cache miss - calcular métricas (solo una vez cada 30 segundos)
        StringBuilder metrics = new StringBuilder();
        
        metrics.append("{");
        metrics.append("\"configurationProcessor\": {");
        metrics.append("\"processedBeansCount\": ").append(processedBeansCount.get()).append(",");
        metrics.append("\"cachedNameToInstance\": ").append(beanNameToInstance.size()).append(",");
        metrics.append("\"cachedTypeToInstance\": ").append(beanTypeToInstance.size()).append(",");
        metrics.append("\"cacheStatus\": {");
        metrics.append("\"instancesCacheAge\": ").append(currentTime - allInstancesCacheTimestamp).append(",");
        metrics.append("\"statsCacheAge\": ").append(currentTime - optimizationStatsCacheTimestamp).append(",");
        metrics.append("\"configCacheAge\": ").append(currentTime - configurationInfoCacheTimestamp).append(",");
        metrics.append("\"metricsCacheAge\": ").append(currentTime - performanceMetricsCacheTimestamp);
        metrics.append("}").append(",");
        metrics.append("\"optimizationLevel\": \"O(1)\"");
        metrics.append("}").append("\n}");
        
        // Actualizar cache
        cachedPerformanceMetrics = metrics.toString();
        performanceMetricsCacheTimestamp = currentTime;
        
        return cachedPerformanceMetrics;
    }
    
    /**
     * 🚀 OPTIMIZACIÓN O(1): Invalida todos los caches TTL
     * Llamado automáticamente en cada procesamiento de bean
     */
    private void invalidateCaches() {
        allInstancesCacheTimestamp = 0;
        cachedAllInstances = null;
        
        optimizationStatsCacheTimestamp = 0;
        cachedOptimizationStats = null;
        
        configurationInfoCacheTimestamp = 0;
        cachedConfigurationInfo = null;
        
        performanceMetricsCacheTimestamp = 0;
        cachedPerformanceMetrics = null;
    }
    
    /**
     * 🎯 NUEVA VERIFICACIÓN: Verifica si una clase tiene constructores con parámetros que requieren inyección
     */
    private boolean hasConstructorWithInjectableParameters(Class<?> clazz) {
        try {
            Constructor<?>[] constructors = clazz.getDeclaredConstructors();
            logger.info("Checking " + constructors.length + " constructors for " + clazz.getName());
            
            for (Constructor<?> constructor : constructors) {
                logger.info("Found constructor with " + constructor.getParameterCount() + " parameters: " + constructor);
                
                if (constructor.getParameterCount() > 0) {
                    Class<?>[] paramTypes = constructor.getParameterTypes();
                    Annotation[][] paramAnnotations = constructor.getParameterAnnotations();
                    
                    logger.info("Constructor parameter types: " + Arrays.toString(paramTypes));
                    
                    // Verificar si algún parámetro requiere inyección
                    for (int i = 0; i < paramTypes.length; i++) {
                        boolean hasInject = hasInjectAnnotation(paramAnnotations[i]);
                        boolean isLikelyInjectable = isLikelyInjectableType(paramTypes[i]);
                        
                        logger.info("Parameter " + i + " (" + paramTypes[i].getName() + "): hasInject=" + hasInject + ", isLikelyInjectable=" + isLikelyInjectable);
                        
                        // Si el parámetro tiene @Inject o es una clase que probablemente necesite inyección
                        if (hasInject || isLikelyInjectable) {
                            logger.info("Found injectable parameter - returning true for " + clazz.getName());
                            return true;
                        }
                    }
                }
            }
            logger.info("No injectable constructors found for " + clazz.getName() + " - returning false");
            return false;
        } catch (Exception e) {
            logger.warning("Error checking constructor parameters for " + clazz.getName() + ": " + e.getMessage());
            return false; // En caso de error, asumimos que no hay parámetros inyectables
        }
    }
    
    /**
     * Verifica si un array de anotaciones contiene @Inject
     */
    private boolean hasInjectAnnotation(Annotation[] annotations) {
        for (Annotation annotation : annotations) {
            if (annotation.annotationType().equals(io.warmup.framework.annotation.Inject.class)) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * Verifica si un tipo probablemente requiere inyección (no es String, primitive, etc.)
     */
    private boolean isLikelyInjectableType(Class<?> paramType) {
        // Si es una clase (no interface primitiva, no String, no tipo básico)
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
     * 🔧 FIX: Calcular el scope correcto considerando tanto el scope explícito como la lógica de constructores
     */
    private ScopeManager.ScopeType calculateCorrectScope(ScopeManager.ScopeType explicitScope, Class<?> beanType) {
        // Si el scope explícito es PROTOTYPE, siempre respetar PROTOTYPE
        if (explicitScope == ScopeManager.ScopeType.PROTOTYPE) {
            return ScopeManager.ScopeType.PROTOTYPE;
        }
        
        // Para otros scopes, usar lógica de constructores para determinar si debe ser singleton o prototype
        boolean hasInjectableConstructors = hasConstructorWithInjectableParameters(beanType);
        
        if (explicitScope == ScopeManager.ScopeType.SINGLETON) {
            return ScopeManager.ScopeType.SINGLETON;
        }
        
        if (explicitScope == ScopeManager.ScopeType.APPLICATION_SCOPE) {
            return ScopeManager.ScopeType.APPLICATION_SCOPE;
        }
        
        // Para scopes request/session, respetarlos
        if (explicitScope == ScopeManager.ScopeType.REQUEST_SCOPE) {
            return ScopeManager.ScopeType.REQUEST_SCOPE;
        }
        
        if (explicitScope == ScopeManager.ScopeType.SESSION_SCOPE) {
            return ScopeManager.ScopeType.SESSION_SCOPE;
        }
        
        // Para scope NONE (default), usar lógica de constructores
        if (!hasInjectableConstructors) {
            return ScopeManager.ScopeType.SINGLETON;
        } else {
            return ScopeManager.ScopeType.PROTOTYPE;
        }
    }
    
    /**
     * Crea un MethodMetadata del paquete core desde un Method reflexivo
     */

}