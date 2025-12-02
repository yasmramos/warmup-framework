package io.warmup.framework.core;

import io.warmup.framework.annotation.*;
import io.warmup.framework.asm.AsmFieldInjector;
import io.warmup.framework.cache.ASMCacheManager;
import io.warmup.framework.cache.ReflectionCache;
import io.warmup.framework.jit.asm.AsmDependencyEngine;
// import io.warmup.framework.jit.asm.SimpleASMUtils; // NOT USED - MIGRATED to AsmCoreUtils
import io.warmup.framework.lazy.LazyFactory;
import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import io.warmup.framework.asm.*;
import io.warmup.framework.core.ScopeManager.ScopeType;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Dependency {

    private static final Logger log = Logger.getLogger(Dependency.class.getName());
    private static final ASMCacheManager CACHE_MANAGER = ASMCacheManager.getInstance();

    // Cache ASM optimizado para métodos @Inject
    private static final Map<Class<?>, List<io.warmup.framework.asm.AsmCoreUtils.AsmMethodInfo>> injectMethodsCache = new ConcurrentHashMap<>();

    private Class<?> type;
    private Object instance;
    private ScopeManager.ScopeType scopeType;
    private boolean instanceCreated = false;
    private volatile boolean lazyInitialized = false;
    private Object lazyInstance;
    private boolean precompiled;

    // Cache de análisis de la clase
    private final ClassAnalysis classAnalysis;

    // JIT ASM INTEGRATION
    private final AsmDependencyEngine asmEngine;
    private Supplier<?> jitSupplier;
    private boolean jitOptimized = false;

    // Constructor pre-cacheado
    private Constructor<?> injectConstructor;
    private Class<?>[] constructorParamTypes;
    private Annotation[][] constructorParamAnnotations;
    private Type[] constructorGenericTypes;

    // Priority for @Primary annotation resolution
    private int priority = 0;

    // Priority and alternative metadata
    private int primaryPriority = 0;
    private boolean isPrimary = false;
    private boolean isAlternative = false;
    private String alternativeProfile = "";

    // ✅ METHODS for setting @Primary and @Alternative annotations
    public void setPrimary(boolean primary) {
        this.isPrimary = primary;
    }
    
    public void setPrimaryPriority(int priority) {
        this.primaryPriority = priority;
        this.priority = priority; // Also set the legacy priority field for compatibility
    }
    
    public void setAlternative(boolean alternative) {
        this.isAlternative = alternative;
    }
    
    public void setAlternativeProfile(String profile) {
        this.alternativeProfile = profile;
    }

    public Dependency(Class<?> type, boolean singleton) {
        this.type = type;
        
        // ✅ SCOPE DETECTION: Usar detección automática de scope basada en anotaciones
        // Solo usar el parámetro singleton como fallback si no hay anotaciones específicas de scope
        ScopeType autoScope = ScopeManager.getScopeType(type);
        
        // Si el scope detectado es SINGLETON o PROTOTYPE, usar el parámetro singleton
        // Para scopes específicos (@RequestScope, @SessionScope, @ApplicationScope), usar la detección automática
        if (autoScope == ScopeType.SINGLETON || autoScope == ScopeType.PROTOTYPE) {
            this.scopeType = singleton ? ScopeManager.ScopeType.SINGLETON : ScopeManager.ScopeType.PROTOTYPE;
        } else {
            // Usar el scope detectado automáticamente para anotaciones específicas
            this.scopeType = autoScope;
        }

        // ✅ INICIALIZAR JIT ASM ENGINE
        this.asmEngine = new AsmDependencyEngine();

        // ✅ OPTIMIZADO: Pre-computar análisis de clase
        this.classAnalysis = new ClassAnalysis(type);

        // ✅ ASM OPTIMIZADO: Pre-obtener constructor y parámetros
        initializeConstructor();

        // ✅ JIT ASM: Intentar pre-compilar supplier
        initializeJitSupplier();

        // 🔍 DEBUG: Extraer valores de anotaciones @Primary y @Alternative
        extractAnnotationMetadata();



        log.log(Level.FINE, "✅ Dependency creada para: {0} (scope: {1}, jit: {2})",
                new Object[]{type.getSimpleName(), scopeType, jitOptimized});
    }

    /**
     * 🔧 FIX: Constructor que acepta ScopeType directamente para prototype beans
     */
    public Dependency(Class<?> type, ScopeManager.ScopeType scopeType) {
        this.type = type;
        this.scopeType = scopeType;

        // ✅ INICIALIZAR JIT ASM ENGINE
        this.asmEngine = new AsmDependencyEngine();

        // ✅ OPTIMIZADO: Pre-computar análisis de clase
        this.classAnalysis = new ClassAnalysis(type);

        // ✅ ASM OPTIMIZADO: Pre-obtener constructor y parámetros
        initializeConstructor();

        // ✅ JIT ASM: Intentar pre-compilar supplier
        initializeJitSupplier();

        // 🔍 DEBUG: Extraer valores de anotaciones @Primary y @Alternative
        extractAnnotationMetadata();

        log.log(Level.FINE, "✅ Dependency creada para: {0} (scope: {1}, jit: {2})",
                new Object[]{type.getSimpleName(), scopeType, jitOptimized});
    }

    public Dependency(Class<?> type, boolean singleton, Object instance) {
        this.type = type;
        this.instance = instance;
        
        // ✅ SCOPE DETECTION: Respetar el parámetro singleton para clases sin anotaciones de scope explícitas
        ScopeType autoScope = ScopeManager.getScopeType(type);
        
        // Si el scope detectado es SINGLETON o PROTOTYPE, usar el parámetro singleton
        // Para scopes específicos (@RequestScope, @SessionScope, @ApplicationScope), usar la detección automática
        if (autoScope == ScopeType.SINGLETON || autoScope == ScopeType.PROTOTYPE) {
            this.scopeType = singleton ? ScopeManager.ScopeType.SINGLETON : ScopeManager.ScopeType.PROTOTYPE;
        } else {
            // Usar el scope detectado automáticamente para anotaciones específicas
            this.scopeType = autoScope;
        }
        
        this.instanceCreated = true;

        // ✅ INICIALIZAR JIT ASM ENGINE
        this.asmEngine = new AsmDependencyEngine();

        // ✅ ASM OPTIMIZADO: Pre-computar análisis incluso para instancias
        this.classAnalysis = new ClassAnalysis(type);
        
        // ✅ ASM OPTIMIZADO: Pre-obtener constructor y parámetros
        initializeConstructor();
        this.constructorParamAnnotations = injectConstructor != null ? injectConstructor.getParameterAnnotations() : new Annotation[0][];
        this.constructorGenericTypes = injectConstructor != null ? injectConstructor.getGenericParameterTypes() : new Type[0];

        if (this.injectConstructor != null) {
            this.injectConstructor.setAccessible(true);
        }

        this.jitOptimized = false;

        // 🔍 DEBUG: Extraer valores de anotaciones @Primary y @Alternative
        extractAnnotationMetadata();



        log.log(Level.FINE, "✅ Dependency con instancia pre-creada: {0} (scope: {1})", 
                new Object[]{type.getSimpleName(), scopeType});
    }
    
    /**
     * Constructor que acepta un Supplier como factory para crear instancias lazyly.
     * Este constructor es usado por registerWithSupplier() en DependencyRegistry.
     *
     * @param type el tipo del bean
     * @param supplier el factory/supplier que creará el bean
     * @param singleton true para singleton, false para prototype
     */
    @SuppressWarnings("unchecked")
    public Dependency(Class<?> type, java.util.function.Supplier<?> supplier, boolean singleton) {
        this.type = type;
        
        // ✅ SCOPE DETECTION: Usar detección automática de scope basada en anotaciones
        ScopeType autoScope = ScopeManager.getScopeType(type);
        
        // Si el scope detectado es SINGLETON o PROTOTYPE, usar el parámetro singleton
        // Para scopes específicos (@RequestScope, @SessionScope, @ApplicationScope), usar la detección automática
        if (autoScope == ScopeType.SINGLETON || autoScope == ScopeType.PROTOTYPE) {
            this.scopeType = singleton ? ScopeManager.ScopeType.SINGLETON : ScopeManager.ScopeType.PROTOTYPE;
        } else {
            // Usar el scope detectado automáticamente para anotaciones específicas
            this.scopeType = autoScope;
        }

        // ✅ INICIALIZAR JIT ASM ENGINE
        this.asmEngine = new AsmDependencyEngine();

        // ✅ OPTIMIZADO: Pre-computar análisis de clase
        this.classAnalysis = new ClassAnalysis(type);

        // ✅ ASM OPTIMIZADO: Pre-obtener constructor y parámetros
        initializeConstructor();

        // ✅ SUPPLIER: Usar el supplier como JIT supplier para creación lazy
        this.jitSupplier = supplier;
        this.jitOptimized = true; // Marcamos como optimizado ya que tenemos supplier

        // 🔍 DEBUG: Extraer valores de anotaciones @Primary y @Alternative
        extractAnnotationMetadata();

        log.log(Level.FINE, "✅ Dependency con Supplier creada para: {0} (scope: {1}, supplier: {2})",
                new Object[]{type.getSimpleName(), scopeType, supplier.getClass().getSimpleName()});
    }

    // ✅ JIT ASM: Inicializar supplier optimizado
    private void initializeJitSupplier() {
        try {
            
            // Verificar si es una inner class no-estática
            if (type.isMemberClass() && !Modifier.isStatic(type.getModifiers())) {
                this.jitSupplier = null;
                this.jitOptimized = false;
                return;
            }
            
            // Solo generar JIT para clases que no sean lazy y sean singletons para mejor performance
            // Y que sean compatibles con JIT usando la verificación del AsmDependencyEngine
            if (!classAnalysis.isLazy && scopeType == ScopeManager.ScopeType.SINGLETON
                    && !type.isInterface() && !Modifier.isAbstract(type.getModifiers())
                    && AsmDependencyEngine.isJitCompatible(type)) {

                this.jitSupplier = asmEngine.createInstanceSupplier(type);
                this.jitOptimized = true;

                log.log(Level.FINE, "✅ Supplier JIT generado para: {0}", type.getSimpleName());
            } else {
                this.jitSupplier = null;
                this.jitOptimized = false;
            }
        } catch (Exception e) {
            log.log(Level.FINE, "⚠️ No se pudo generar supplier JIT para {0}: {1}",
                    new Object[]{type.getSimpleName(), e.getMessage()});
            this.jitSupplier = null;
            this.jitOptimized = false;
        }
    }

    // ✅ OPTIMIZADO: Clase de análisis cacheado
    private static class ClassAnalysis {

        final boolean isLazy;
        final boolean hasInjectFields;
        final List<io.warmup.framework.asm.AsmCoreUtils.AsmMethodInfo> postConstructMethods;
        final List<io.warmup.framework.asm.AsmCoreUtils.AsmMethodInfo> preDestroyMethods;
        final List<io.warmup.framework.asm.AsmCoreUtils.AsmMethodInfo> injectMethods;

        ClassAnalysis(Class<?> clazz) {
            // ✅ ASM: Usar AsmCoreUtils para análisis sin reflexión
            this.isLazy = AsmCoreUtils.hasAnnotation(clazz, "io.warmup.framework.annotation.Lazy");
            this.hasInjectFields = hasInjectFieldsCached(clazz);
            // ✅ ASM DIRECTO: Usar utilidades ASM para métodos lifecycle sin conversión
            this.postConstructMethods = AsmCoreUtils.getPostConstructMethods(clazz);
            this.preDestroyMethods = AsmCoreUtils.getPreDestroyMethods(clazz);
            this.injectMethods = getInjectMethodsCached(clazz);
        }
    }

    private static boolean hasInjectFieldsCached(Class<?> clazz) {
        // ✅ ASM DIRECTO: Usar AsmCoreUtils para obtener campos con @Inject sin conversión
        List<io.warmup.framework.asm.AsmCoreUtils.AsmFieldInfo> injectFieldsAsm = AsmCoreUtils.getInjectFields(clazz);
        
        return !injectFieldsAsm.isEmpty();
    }

    private static List<io.warmup.framework.asm.AsmCoreUtils.AsmMethodInfo> getInjectMethodsCached(Class<?> clazz) {
        return injectMethodsCache.computeIfAbsent(clazz, Dependency::findInjectMethodsImpl);
    }

    private static List<io.warmup.framework.asm.AsmCoreUtils.AsmMethodInfo> findInjectMethodsImpl(Class<?> clazz) {
        // ✅ ASM DIRECTO: Usar AsmCoreUtils para obtener métodos con @Inject sin conversión
        return AsmCoreUtils.getInjectMethods(clazz);
    }
    private Object createInstanceOptimized(IContainer container, Set<Class<?>> dependencyChain) throws Exception {
        
        Object[] params = new Object[constructorParamTypes.length];

        for (int i = 0; i < constructorParamTypes.length; i++) {
            try {
                params[i] = resolveDependency(
                        constructorParamTypes[i],
                        constructorParamAnnotations[i],
                        constructorGenericTypes[i],
                        container,
                        dependencyChain
                );
            } catch (Exception e) {
                throw e;
            }
        }

        log.log(Level.FINER, "🔨 Creando instancia via ASM optimizado: {0}", type.getSimpleName());
        return AsmCoreUtils.newInstance(type, params);
    }

    public Object getCachedInstance() {
        return instance; // Retorna directamente la instancia
    }

    /**
     * Limpia la instancia cacheada (para hot reload)
     */
    public void clearCache() {
        this.instance = null;
        this.instanceCreated = false;
        log.log(Level.FINE, "Cleared cache for dependency: {0}", type.getSimpleName());
    }

    public boolean isInstanceCreated() {
        return instanceCreated && instance != null; // ✅ Verifica ambos
    }

    public String getDebugInfo() {
        return String.format("Dependency{type=%s, scope=%s, instanceCreated=%s, instance=%s, jitOptimized=%s}",
                type.getSimpleName(),
                scopeType,
                instanceCreated,
                instance != null ? instance.getClass().getSimpleName() : "null",
                jitOptimized);
    }

    public String getInstanceStatus() {
        if (instance == null) {
            return jitOptimized ? "JIT_READY" : "NOT_CREATED";
        }
        return scopeType.getValue();
    }

    /**
     * Helper method to safely apply AOP to instances.
     * This method handles the generic type issues that occur when calling applyAopIfNeeded.
     */
    @SuppressWarnings("unchecked")
    private <T> T applyAopSafely(T instance, IContainer container) {
        if (instance == null) {
            return null;
        }
        
        try {
            // ✅ CRITICAL FIX: Aplicar AOP real usando AopHandler del container
            if (container != null && container instanceof WarmupContainer) {
                Object aopHandlerObj = ((WarmupContainer) container).getAopHandler();
                if (aopHandlerObj instanceof AopHandler) {
                    AopHandler aopHandler = (AopHandler) aopHandlerObj;
                    return (T) aopHandler.applyAopIfNeeded(instance, (Class<T>) instance.getClass());
                }
            }
            return instance;
        } catch (Exception e) {
            // Log the error but don't fail the instance creation
            log.log(Level.WARNING, "⚠️ Failed to apply AOP to instance of {0}: {1}", 
                    new Object[]{type.getSimpleName(), e.getMessage()});
            return instance; // Return original instance if AOP fails
        }
    }

    public Class<?> getType() {
        return type;
    }

    public Object getInstance(IContainer container, Set<Class<?>> dependencyChain) {
        log.log(Level.FINER, "Obteniendo instancia de: {0}, scope: {1}, creada: {2}",
                new Object[]{type.getSimpleName(), scopeType, instanceCreated});

        // Verificar si es una dependencia lazy
        if (classAnalysis.isLazy) {
            return getLazyInstance(container, dependencyChain);
        }

        // Verificar ciclo de dependencias
        if (dependencyChain.contains(type)) {
            throw new IllegalStateException("Ciclo de dependencias detectado: " + buildCycleMessage(dependencyChain, type));
        }

        // ✅ SCOPE MANAGEMENT: Request/Session scope son manejados por WarmupContainer, no por Dependency
        if (scopeType == ScopeManager.ScopeType.REQUEST_SCOPE) {
            throw new IllegalStateException("Request-scoped beans should not reach Dependency.getInstance(). Use container.getRequestScopedBean() instead.");
        }
        
        if (scopeType == ScopeManager.ScopeType.SESSION_SCOPE) {
            throw new IllegalStateException("Session-scoped beans require sessionId parameter. Use container.getSessionScopedBean(type, sessionId) instead.");
        }
        
        // ✅ FIX: PROTOTYPE beans can be created directly even with constructor dependencies
        // The prototype scope means new instance every time, so constructor dependencies are fine
        // Only throw exception for specific cases that would cause issues

        // ✅ Scope-aware: Retornar instancia existente solo para scopes que se cachean
        if (instanceCreated && instance != null && shouldCacheInstance()) {
            log.log(Level.FINER, "Retornando instancia existente de: {0}", type.getSimpleName());
            // ✅ AOP: Aplicar AOP a la instancia antes de devolverla
            return applyAopSafely(instance, container);
        }

        // Agregar a la cadena de dependencias
        dependencyChain.add(type);

        try {
            Object newInstance;

            // ✅ JIT ASM: Usar supplier optimizado si está disponible
            if (jitOptimized && jitSupplier != null) {
                newInstance = createInstanceWithJit(container, dependencyChain);
            } else {
                // ✅ OPTIMIZADO: Crear instancia usando constructor pre-cacheado
                newInstance = createInstanceOptimized(container, dependencyChain);
            }

            // ✅ Scope-aware: Asignar la instancia solo para cacheable scopes
            if (shouldCacheInstance()) {
                this.instance = newInstance;
                this.instanceCreated = true;
                log.log(Level.FINE, "Instancia {0} asignada para: {1}", 
                        new Object[]{scopeType.getValue(), type.getSimpleName()});
            }

            // ✅ OPTIMIZADO: Inyectar dependencias
            injectFieldsOptimized(newInstance, container, dependencyChain);
            injectMethodsOptimized(newInstance, container, dependencyChain);

            // ✅ OPTIMIZADO: Ejecutar métodos @PostConstruct pre-cacheados
            invokePostConstructMethodsOptimized(newInstance);

            // ✅ MÉTODOS OPTIMIZADOS: Registrar métodos @PreDestroy usando estrategia progresiva
            if (shouldCacheInstance() && !classAnalysis.preDestroyMethods.isEmpty()) {
                // ✅ MÉTODO OPTIMIZADO: Registrar sin conversión a reflexión
                for (io.warmup.framework.asm.AsmCoreUtils.AsmMethodInfo preDestroyMethod : classAnalysis.preDestroyMethods) {
                    // TODO: Agregar método en WarmupContainer para manejar AsmMethodInfo directamente
                    // Por ahora usamos la estrategia progresiva para invocar el método cuando sea necesario
                }
            }

            // Registrar listeners de eventos
            registerInstanceForEvents(newInstance, container);

            log.log(Level.FINE, "Instancia {0} creada exitosamente: {1}", 
                    new Object[]{scopeType.getValue(), type.getSimpleName()});
            
            // ✅ AOP: Aplicar AOP a la instancia antes de devolverla
            return applyAopSafely(newInstance, container);

        } catch (Exception ex) {
            log.log(Level.SEVERE, "Error creando instancia de {0}: {1}",
                    new Object[]{type.getName(), ex.getMessage()});
            throw new RuntimeException("Failed to create instance of " + type.getName(), ex);
        } finally {
            dependencyChain.remove(type);
        }
    }

    // ✅ JIT ASM: Crear instancia usando supplier optimizado
    private Object createInstanceWithJit(IContainer container, Set<Class<?>> dependencyChain) {
        try {
            long startTime = System.nanoTime();
            Object instance = jitSupplier.get();
            
            // 🔧 NUEVA VERIFICACIÓN: Si el JIT supplier retorna null, usar fallback
            if (instance == null) {
                jitOptimized = false;
                return createInstanceOptimized(container, dependencyChain);
            }
            
            long duration = System.nanoTime() - startTime;

            log.log(Level.FINEST, "🚀 JIT Instance created: {0} in {1} ns",
                    new Object[]{type.getSimpleName(), duration});

            // ✅ AOP: Aplicar AOP a la instancia JIT antes de devolverla
            return applyAopSafely(instance, container);
        } catch (Exception e) {
            log.log(Level.WARNING, "🔄 JIT failed, falling back to reflection for: {0}", type.getSimpleName());
            jitOptimized = false;
            try {
    
                return createInstanceOptimized(container, dependencyChain);
            } catch (Exception ex) {
                throw new RuntimeException("Both JIT and reflection failed for: " + type.getName(), ex);
            }
        }
    }

    private void injectFieldsOptimized(Object instance, IContainer container, Set<Class<?>> dependencyChain) throws Exception {
        // Optimización de inyección de campos usando ASM
        
        // ✅ VERIFICACIÓN EN TIEMPO DE RUNTIME: No confiar solo en el cache
        // Si el cache indica que no hay campos @Inject, intentar verificar en runtime
        if (isPrecompiled()) {
            // Clase pre-compilada, saltando inyección
            return;
        }

        // ✅ SIEMPRE INTENTAR DETECTAR CAMPOS EN TIEMPO DE RUNTIME
        // usando ASM directo sin conversión a reflexión
        List<io.warmup.framework.asm.AsmCoreUtils.AsmFieldInfo> runtimeFieldsAsm = AsmCoreUtils.getInjectFields(type);
        // Verificación runtime para campos @Inject
        
        System.err.println("🔍 [DEBUG] Campos @Inject detectados para " + type.getName() + ": " + runtimeFieldsAsm.size());
        
        if (runtimeFieldsAsm.isEmpty()) {
            // No se encontraron campos @Inject en tiempo de runtime
            return;
        }

        if (AsmFieldInjector.hasInjectFields(type.getName(), type.getClassLoader())) {
            System.out.println("🔍 DEBUG: AsmFieldInjector tiene campos @Inject para " + type.getSimpleName());
            Map<String, Object> dependencies = new HashMap<>();

            // ✅ ASM DIRECTO: Usar los campos detectados en tiempo de runtime usando AsmFieldInfo
            for (io.warmup.framework.asm.AsmCoreUtils.AsmFieldInfo field : runtimeFieldsAsm) {
                System.out.println("🔍 DEBUG: Procesando campo " + field.name + " (tipo: " + field.type + ") con anotaciones: " + Arrays.toString(field.annotations));
                // ✅ ASM: Verificar anotaciones usando datos de AsmFieldInfo
                boolean hasInject = false;
                boolean hasValue = false;
                
                for (String annotationName : field.annotations) {
                    if (annotationName.contains("Inject")) {
                        hasInject = true;
                    } else if (annotationName.contains("Value")) {
                        hasValue = true;
                    }
                }
                
                if (hasInject) {
                    // Obtener tipo de campo desde descriptor ASM
                    Class<?> fieldType = AsmCoreUtils.getClassFromDescriptor(field.type);
                    System.out.println("🔍 DEBUG: Resolviendo dependencia para campo " + field.name + " de tipo " + fieldType.getName());
                    Object dependency = resolveDependency(fieldType, field, container, dependencyChain);
                    System.out.println("🔍 DEBUG: Dependencia resuelta para campo " + field.name + ": " + (dependency != null ? dependency.getClass().getName() : "NULL"));
                    dependencies.put(field.name, dependency);
                } else if (hasValue) {
                    // ✅ MÉTODOS OPTIMIZADOS: Para @Value, usar estrategia ASM → MethodHandle → Reflection
                    try {
                        Class<?> fieldType = AsmCoreUtils.getClassFromDescriptor(field.type);
                        // ✅ MÉTODO OPTIMIZADO: Usar estrategia progresiva para obtener anotaciones
                        Annotation[] annotations = AsmCoreUtils.getFieldAnnotationsProgressive(type.getDeclaredField(field.name));
                        
                        Value valueAnnotation = null;
                        for (Annotation annotation : annotations) {
                            if (annotation instanceof Value) {
                                valueAnnotation = (Value) annotation;
                                break;
                            }
                        }
                        
                        String valueExpression = valueAnnotation != null ? valueAnnotation.value() : "";
                        String resolvedValue = container.resolvePropertyValue(valueExpression);
                        Object convertedValue = Convert.convertStringToType(resolvedValue, fieldType);
                        dependencies.put(field.name, convertedValue);
                    } catch (Exception e) {
                        System.err.println("❌ Error procesando anotación @Value para campo: " + field.name + " - " + e.getMessage());
                    }
                }
            }

            if (!dependencies.isEmpty()) {
                // Antes de injectFieldsWithASM
                for (Map.Entry<String, Object> entry : dependencies.entrySet()) {
                    // Campo inyectado
                }
                try {
                    // Intentando inyección ASM...
                    injectFieldsWithASM(instance, dependencies);
                    // Inyección ASM completada exitosamente
                } catch (Exception e) {
                    System.err.println("❌ [CRITICAL] Inyección ASM falló silenciosamente: " + e.getMessage());
                    e.printStackTrace();
                    throw new RuntimeException("Inyección ASM falló para: " + type.getSimpleName(), e);
                }
            }
        } else {
            // ✅ FALLBACK: Inyección tradicional por reflexión cuando ASM no está disponible
            // ASM no disponible, usando fallback por reflexión
            
            // ✅ USAR LOS CAMPOS DETECTADOS EN TIEMPO DE RUNTIME
            for (io.warmup.framework.asm.AsmCoreUtils.AsmFieldInfo asmField : runtimeFieldsAsm) {
                try {
                    // Obtener el Field de reflexión para poder establecer valores
                    Field field = type.getDeclaredField(asmField.name);
                    field.setAccessible(true);
                    
                    // Verificar anotaciones usando datos de ASM
                    boolean hasInject = false;
                    boolean hasValue = false;
                    
                    for (String annotation : asmField.annotations) {
                        if (annotation.contains("Inject")) {
                            hasInject = true;
                        } else if (annotation.contains("Value")) {
                            hasValue = true;
                        }
                    }
                    
                    if (hasInject) {
                        // Fallback: Inyectando campo @Inject
                        // Usar la versión correcta de resolveDependency
                        Class<?> fieldType = AsmCoreUtils.getClassFromDescriptor(asmField.type);
                        Object dependency = resolveDependency(fieldType, asmField, container, dependencyChain);
                        
                        // ✅ MÉTODOS OPTIMIZADOS: Usar estrategia ASM → MethodHandle → Reflection
                        AsmCoreUtils.setFieldProgressive(instance, asmField.name, dependency);
                        // Fallback: Campo inyectado exitosamente
                    } else if (hasValue) {
                        // Fallback: Inyectando campo @Value
                        // Para @Value usar estrategia optimizada para obtener anotaciones
                        Annotation[] annotations = AsmCoreUtils.getFieldAnnotationsProgressive(type.getDeclaredField(asmField.name));
                        String valueExpression = "";
                        
                        for (Annotation annotation : annotations) {
                            if (annotation instanceof Value) {
                                valueExpression = ((Value) annotation).value();
                                break;
                            }
                        }
                        
                        String resolvedValue = container.resolvePropertyValue(valueExpression);
                        Class<?> fieldType = AsmCoreUtils.getClassFromDescriptor(asmField.type);
                        Object convertedValue = Convert.convertStringToType(resolvedValue, fieldType);
                        
                        // ✅ MÉTODOS OPTIMIZADOS: Usar estrategia ASM → MethodHandle → Reflection
                        AsmCoreUtils.setFieldProgressive(instance, asmField.name, convertedValue);
                        // Fallback: Campo inyectado con valor @Value
                    }
                } catch (Exception e) {
                    System.err.println("❌ [ERROR] Fallback injection falló para campo: " + asmField.name + " - " + e.getMessage());
                    e.printStackTrace();
                    throw new RuntimeException("Error en fallback injection para campo: " + asmField.name, e);
                }
            }
        }
        
        // 🔍 VERIFICACIÓN POST-INYECCIÓN: Confirmar que la inyección fue exitosa
        // Verificación post-inyección
        // Identidad del objeto verificada
        
        // Verificar campos @Inject específicamente usando métodos optimizados
        for (io.warmup.framework.asm.AsmCoreUtils.AsmFieldInfo asmField : runtimeFieldsAsm) {
            try {
                // ✅ MÉTODOS OPTIMIZADOS: Usar estrategia progresiva para verificar el campo
                if (AsmCoreUtils.hasAnnotation(type.getDeclaredField(asmField.name), "io.warmup.framework.annotation.Inject")) {
                    // ✅ MÉTODO OPTIMIZADO: Usar estrategia ASM → MethodHandle → Reflection
                    Object fieldValue = AsmCoreUtils.getFieldProgressive(instance, asmField.name);
                    String fieldName = asmField.name;
                    // Campo verificado después de inyección
                    
                    if (fieldValue == null && fieldName.equals("testService")) {
                        System.err.println("❌ [CRITICAL] Campo 'testService' sigue siendo null después de la inyección para: " + type.getSimpleName());
                    }
                }
            } catch (Exception e) {
                System.err.println("❌ [ERROR] No se pudo verificar el campo '" + asmField.name + "' después de inyección: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    private void injectMethodsOptimized(Object instance, IContainer container, Set<Class<?>> dependencyChain) throws Exception {
        if (isPrecompiled()) {
            return;
        }

        for (io.warmup.framework.asm.AsmCoreUtils.AsmMethodInfo method : classAnalysis.injectMethods) {
            // ✅ MÉTODOS OPTIMIZADOS: Usar estrategia ASM → MethodHandle → Reflection
            
            if (method.parameterTypes.length > 0) {
                Object[] args = new Object[method.parameterTypes.length];
                for (int i = 0; i < method.parameterTypes.length; i++) {
                    Class<?> paramType = AsmCoreUtils.getClassFromDescriptor(method.parameterTypes[i]);
                    args[i] = container.getDependency(paramType, dependencyChain);
                }
                
                // ✅ MÉTODO OPTIMIZADO: ASM → MethodHandle → Reflection
                AsmCoreUtils.invokeMethodProgressive(instance, method.name, args);
            } else {
                // Para métodos sin parámetros
                AsmCoreUtils.invokeMethodProgressive(instance, method.name);
            }
        }
    }

    private void invokePostConstructMethodsOptimized(Object instance) throws Exception {
        // ✅ MÉTODOS OPTIMIZADOS: Usar estrategia ASM → MethodHandle → Reflection
        for (io.warmup.framework.asm.AsmCoreUtils.AsmMethodInfo method : classAnalysis.postConstructMethods) {
            // ✅ MÉTODO OPTIMIZADO: ASM → MethodHandle → Reflection
            AsmCoreUtils.invokeMethodProgressive(instance, method.name);
        }
    }

    // ✅ MÉTODOS LAZY OPTIMIZADOS
    @SuppressWarnings("unchecked")
    private <T> T getLazyInstance(IContainer container, Set<Class<?>> dependencyChain) {
        if (classAnalysis.isLazy) {
            synchronized (this) {
                if (lazyInstance == null) {
                    Supplier<T> mainSupplier = () -> {
                        try {
                            if (jitOptimized && jitSupplier != null) {
                                return (T) jitSupplier.get();
                            } else {
                                return (T) createInstanceInternal(container, new HashSet<>());
                            }
                        } catch (Exception e) {
                            throw new RuntimeException("Error en supplier principal: " + type.getName(), e);
                        }
                    };
                    lazyInstance = LazyFactory.createLazyProxy((Class<T>) type, mainSupplier);
                }
            }
            return (T) lazyInstance;
        }

        if (type.isInterface()) {
            synchronized (this) {
                if (lazyInstance == null) {
                    lazyInstance = createLazyProxy((Class<T>) type, container, dependencyChain);
                }
            }
            return (T) lazyInstance;
        }

        synchronized (this) {
            if (!lazyInitialized) {
                if (jitOptimized && jitSupplier != null) {
                    lazyInstance = jitSupplier.get();
                } else {
                    lazyInstance = createInstanceInternal(container, dependencyChain);
                }
                lazyInitialized = true;
            }
        }
        // ✅ AOP: Aplicar AOP a la instancia lazy antes de devolverla
        @SuppressWarnings("unchecked")
        T result = (T) lazyInstance;
        return result;
    }

    private Object createInstanceInternal(IContainer container, Set<Class<?>> dependencyChain) {
        // ✅ SCOPE MANAGEMENT: Request/Session scope son manejados por WarmupContainer, no por Dependency
        if (scopeType == ScopeManager.ScopeType.REQUEST_SCOPE) {
            throw new IllegalStateException("Request-scoped beans should not reach Dependency.createInstanceInternal(). Use container.getRequestScopedBean() instead.");
        }
        
        if (scopeType == ScopeManager.ScopeType.SESSION_SCOPE) {
            throw new IllegalStateException("Session-scoped beans require sessionId parameter. Use container.getSessionScopedBean(type, sessionId) instead.");
        }

        if (instance != null && instanceCreated) {
            log.log(Level.FINER, "Retornando instancia existente de: {0}", type.getSimpleName());
            // ✅ AOP: Aplicar AOP a la instancia antes de devolverla
            return applyAopSafely(instance, container);
        }

        if (dependencyChain.contains(type)) {
            throw new IllegalStateException("Ciclo de dependencias detectado: " + buildCycleMessage(dependencyChain, type));
        }

        dependencyChain.add(type);

        try {
            synchronized (this) {
                // ✅ SCOPE MANAGEMENT: Request/Session scope son manejados por WarmupContainer, no por Dependency (segundo chequeo)
                if (scopeType == ScopeManager.ScopeType.REQUEST_SCOPE) {
                    throw new IllegalStateException("Request-scoped beans should not reach Dependency.createInstanceInternal(). Use container.getRequestScopedBean() instead.");
                }
                
                if (scopeType == ScopeManager.ScopeType.SESSION_SCOPE) {
                    throw new IllegalStateException("Session-scoped beans require sessionId parameter. Use container.getSessionScopedBean(type, sessionId) instead.");
                }

                if (instance != null && instanceCreated) {
                    log.log(Level.FINER, "Retornando instancia existente de: {0}", type.getSimpleName());
                    // ✅ AOP: Aplicar AOP a la instancia antes de devolverla
                    return applyAopSafely(instance, container);
                }

                Object newInstance;

                if (jitOptimized && jitSupplier != null) {
                    newInstance = jitSupplier.get();
                } else {
                    newInstance = createInstanceOptimized(container, dependencyChain);
                }

                // ✅ SCOPE-AWARE: Asignar instancia solo para cacheable scopes
                if (shouldCacheInstance()) {
                    this.instance = newInstance;
                    this.instanceCreated = true;
                }

                injectFieldsOptimized(newInstance, container, dependencyChain);
                injectMethodsOptimized(newInstance, container, dependencyChain);
                invokePostConstructMethodsOptimized(newInstance);

                if (shouldCacheInstance() && !classAnalysis.preDestroyMethods.isEmpty()) {
                    // ✅ MÉTODOS OPTIMIZADOS: Registrar métodos @PreDestroy usando estrategia progresiva
                    // Registrando métodos PreDestroy
                    for (io.warmup.framework.asm.AsmCoreUtils.AsmMethodInfo preDestroyMethod : classAnalysis.preDestroyMethods) {
                        // Método PreDestroy registrado
                        // TODO: Implementar registro optimizado de PreDestroy
                    }
                }

                registerInstanceForEvents(newInstance, container);

                // ✅ AOP: Aplicar AOP a la instancia antes de devolverla
                return applyAopSafely(newInstance, container);
            }
        } catch (Exception ex) {
            log.log(Level.SEVERE, "Error in createInstanceInternal for {0}: {1}",
                    new Object[]{type.getName(), ex.getMessage()});
            throw new RuntimeException("Failed to create instance internally", ex);
        } finally {
            dependencyChain.remove(type);
        }
    }

    // ✅ MÉTODOS AUXILIARES
    private void injectFieldsWithASM(Object instance, Map<String, Object> dependencies) {
        // injectFieldsWithASM iniciado
        for (Map.Entry<String, Object> entry : dependencies.entrySet()) {
            // Estableciendo campo con valor
            try {
                // ✅ ASM OPTIMIZADO: Usar AsmFieldAccessor para inyección directa
                AsmFieldAccessor.setField(instance, entry.getKey(), entry.getValue());
                // Campo establecido exitosamente
            } catch (Exception e) {
                System.err.println("❌ [ERROR] Error inyectando campo con ASM: " + entry.getKey() + " - " + e.getMessage());
                e.printStackTrace();
                throw new RuntimeException("Error inyectando campo con ASM: " + entry.getKey(), e);
            }
        }
        // injectFieldsWithASM completado
    }

    private String extractParamName(Annotation[] annotations) {
        for (Annotation ann : annotations) {
            if (ann instanceof Named) {
                return ((Named) ann).value();
            } else if (ann instanceof Qualifier) {
                return ((Qualifier) ann).value();
            }
        }
        return null;
    }

    // ✅ CORREGIDO: Métodos resolveDependency sobrecargados
    private Object resolveDependency(Class<?> type, Annotation[] annotations, Type genericType,
            IContainer container, Set<Class<?>> dependencyChain) throws Exception {
        // Resolución de dependencia iniciada

        if (type.isPrimitive()) {
            // Tipo primitivo, retornando valor por defecto
            return getDefaultPrimitiveValue(type);
        }

        String paramName = extractParamName(annotations);
        if (paramName != null) {
            // Parámetro con nombre, llamando container.getNamed()
            return container.getNamedDependency(type, paramName, dependencyChain);
        }

        // Llamando container.get() sin nombre
        Object result = container.getDependency(type, dependencyChain);
        // container.get() retornó resultado
        return result;
    }

    private Object resolveDependency(Class<?> type, io.warmup.framework.asm.AsmCoreUtils.AsmFieldInfo field,
            IContainer container, Set<Class<?>> dependencyChain) throws Exception {

        if (type.isPrimitive()) {
            return getDefaultPrimitiveValue(type);
        }

        // ✅ ASM DIRECTO: Verificar anotaciones usando datos de AsmFieldInfo
        boolean hasNamed = false;
        boolean hasQualifier = false;
        boolean hasValue = false;
        String namedValue = "";
        String qualifierValue = "";
        String valueExpression = "";
        
        for (String annotationName : field.annotations) {
            if (annotationName.contains("Named")) {
                hasNamed = true;
                // ✅ MÉTODOS OPTIMIZADOS: Usar estrategia progresiva para obtener anotaciones
                try {
                    Annotation[] annotations = AsmCoreUtils.getFieldAnnotationsProgressive(type.getDeclaredField(field.name));
                    for (Annotation annotation : annotations) {
                        if (annotation instanceof Named) {
                            namedValue = ((Named) annotation).value();
                            break;
                        }
                    }
                } catch (Exception e) {
                    // Continuar sin valor named
                }
            } else if (annotationName.contains("Qualifier")) {
                hasQualifier = true;
                // ✅ MÉTODOS OPTIMIZADOS: Usar estrategia progresiva para obtener anotaciones
                try {
                    Annotation[] annotations = AsmCoreUtils.getFieldAnnotationsProgressive(type.getDeclaredField(field.name));
                    for (Annotation annotation : annotations) {
                        if (annotation instanceof Qualifier) {
                            qualifierValue = ((Qualifier) annotation).value();
                            break;
                        }
                    }
                } catch (Exception e) {
                    // Continuar sin valor qualifier
                }
            } else if (annotationName.contains("Value")) {
                hasValue = true;
                // ✅ MÉTODOS OPTIMIZADOS: Usar estrategia progresiva para obtener anotaciones
                try {
                    Annotation[] annotations = AsmCoreUtils.getFieldAnnotationsProgressive(type.getDeclaredField(field.name));
                    for (Annotation annotation : annotations) {
                        if (annotation instanceof Value) {
                            valueExpression = ((Value) annotation).value();
                            break;
                        }
                    }
                } catch (Exception e) {
                    // Continuar sin valor de expresión
                }
            }
        }
        
        if (hasNamed) {
            return container.getNamedDependency(type, namedValue, dependencyChain);
        }

        if (hasQualifier) {
            return container.getNamedDependency(type, qualifierValue, dependencyChain);
        }

        if (hasValue) {
            String resolvedValue = container.resolvePropertyValue(valueExpression);
            return Convert.convertStringToType(resolvedValue, type);
        }

        // Intentando obtener dependencia desde container
        
        // Verificando si es interfaz
        if (type.isInterface()) {
            // Interfaz detectada - buscando implementaciones
            
            // ✅ NUEVA LÓGICA: Resolver interfaces buscando implementaciones con @Primary/@Alternative
            try {
                Object resolvedImplementation = container.getBestImplementation(type);
                // Implementación resuelta para interfaz
                return resolvedImplementation;
            } catch (Exception e) {
                // Error al resolver interfaz
                // Si no se puede resolver como interfaz, continuar con la lógica normal
            }
        }
        
        return container.getDependency(type, dependencyChain);
    }

    private Object getDefaultPrimitiveValue(Class<?> primitiveType) {
        if (primitiveType == int.class) {
            return 0;
        }
        if (primitiveType == long.class) {
            return 0L;
        }
        if (primitiveType == boolean.class) {
            return false;
        }
        if (primitiveType == double.class) {
            return 0.0;
        }
        if (primitiveType == float.class) {
            return 0.0f;
        }
        if (primitiveType == byte.class) {
            return (byte) 0;
        }
        if (primitiveType == short.class) {
            return (short) 0;
        }
        if (primitiveType == char.class) {
            return '\0';
        }
        return null;
    }

    private String buildCycleMessage(Set<Class<?>> chain, Class<?> cycleClass) {
        StringBuilder sb = new StringBuilder();
        boolean found = false;

        for (Class<?> clazz : chain) {
            if (clazz == cycleClass) {
                found = true;
            }
            if (found) {
                sb.append(clazz.getSimpleName()).append(" -> ");
            }
        }
        sb.append(cycleClass.getSimpleName());
        return sb.toString();
    }

    private void registerInstanceForEvents(Object instance, IContainer container) {
        if (instance != null) {
            Class<?> clazz = instance.getClass();
            container.registerEventListeners(clazz, instance);
        }
    }

    @SuppressWarnings("unchecked")
    private <T> T createLazyProxy(Class<T> type, IContainer container, Set<Class<?>> dependencyChain) {
        Supplier<T> instanceSupplier = () -> {
            try {
                if (instance != null && instanceCreated) {
                    return (T) instance;
                }
                if (jitOptimized && jitSupplier != null) {
                    return (T) jitSupplier.get();
                } else {
                    return (T) createInstanceInternal(container, dependencyChain);
                }
            } catch (Exception e) {
                throw new RuntimeException("Error inicializando lazy dependency: " + type.getName(), e);
            }
        };
        return LazyFactory.createLazyProxy(type, instanceSupplier);
    }

    // ✅ SCOPE MANAGEMENT
    /**
     * Determines if this dependency should cache its instance.
     * 
     * @return true if instance should be cached by the container
     */
    public boolean shouldCacheInstance() {
        return scopeType == ScopeManager.ScopeType.SINGLETON || 
               scopeType == ScopeManager.ScopeType.APPLICATION_SCOPE;
    }
    
    public ScopeManager.ScopeType getScopeType() {
        return scopeType;
    }

    /**
     * Crear instancia para Request/Session scope sin usar cache.
     * Este método es usado por WarmupContainer para beans request/session scoped
     * para evitar la recursión infinita.
     */
    public Object createInstanceForScope(IContainer container, Set<Class<?>> dependencyChain) throws Exception {
        // Para Request/Session scope, crear instancia directamente sin cache ni excepciones
        // Creando instancia para scope
        
        // Crear instancia usando el método optimizado
        Object newInstance = createInstanceOptimized(container, dependencyChain);
        
        // Inyectar dependencias
        injectFieldsOptimized(newInstance, container, dependencyChain);
        injectMethodsOptimized(newInstance, container, dependencyChain);
        
        // Ejecutar @PostConstruct
        invokePostConstructMethodsOptimized(newInstance);
        
        // Registrar para eventos
        registerInstanceForEvents(newInstance, container);
        
        // Instancia for scope creada exitosamente
        
        return applyAopSafely(newInstance, container);
    }

    // Priority management for @Primary annotation resolution
    public int getPriority() {
        return priority;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

    // ✅ GETTERS Y SETTERS
    public Object getInstance() {
        return instance;
    }

    public void setInstance(Object instance) {
        this.instance = instance;
        this.instanceCreated = (instance != null);
        log.log(Level.FINE, "✅ Instancia asignada manualmente: {0}", type.getSimpleName());
    }

    /**
     * 🔧 FIX: Clear instance for prototype beans after registration
     * This ensures prototype beans are created on demand
     */
    public void clearInstanceForPrototype() {
        if (this.scopeType == ScopeManager.ScopeType.PROTOTYPE) {
            this.instance = null;
            this.instanceCreated = false;
            log.log(Level.FINE, "🔧 PROTOTYPE instance cleared for: {0}", type.getSimpleName());
        }
    }

    public boolean isSingleton() {
        return scopeType == ScopeManager.ScopeType.SINGLETON;
    }

    public void setSingleton(boolean singleton) {
        // Legacy compatibility - convert boolean to scope type
        this.scopeType = singleton ? ScopeManager.ScopeType.SINGLETON : ScopeManager.ScopeType.PROTOTYPE;
    }

    public void reset() {
        // Only reset prototype scope instances
        if (scopeType == ScopeManager.ScopeType.PROTOTYPE) {
            instance = null;
            instanceCreated = false;
            lazyInstance = null;
            lazyInitialized = false;
            log.log(Level.FINE, "✅ Dependency reseteada: {0}", type.getSimpleName());
        }
    }

    public void forceCleanup() {
        instance = null;
        instanceCreated = false;
        lazyInstance = null;
        lazyInitialized = false;
        log.log(Level.FINE, "✅ Dependency limpiada forzadamente: {0}", type.getSimpleName());
    }

    protected boolean isPrecompiled() {
        return precompiled;
    }

    public void setPrecompiled(boolean precompiled) {
        this.precompiled = precompiled;
    }

    // ✅ JIT ASM: Métodos adicionales
    public boolean isJitOptimized() {
        return jitOptimized;
    }

    public void enableJitOptimization() {
        if (!jitOptimized) {
            initializeJitSupplier();
        }
    }

    public void disableJitOptimization() {
        this.jitOptimized = false;
        this.jitSupplier = null;
    }

    private String getFieldDescriptor(Class<?> fieldType) {
        if (fieldType.isPrimitive()) {
            if (fieldType == int.class) {
                return "I";
            }
            if (fieldType == long.class) {
                return "J";
            }
            if (fieldType == boolean.class) {
                return "Z";
            }
            if (fieldType == double.class) {
                return "D";
            }
            if (fieldType == float.class) {
                return "F";
            }
            if (fieldType == byte.class) {
                return "B";
            }
            if (fieldType == short.class) {
                return "S";
            }
            if (fieldType == char.class) {
                return "C";
            }
        }
        return "L" + fieldType.getName().replace('.', '/') + ";";
    }

    // 🔍 DEBUG: Método para extraer valores de anotaciones @Primary y @Alternative
    private void extractAnnotationMetadata() {
        // Analizando anotaciones para clase

        // Extraer @Primary
        Primary primary = type.getAnnotation(Primary.class);
        if (primary != null) {
            this.isPrimary = true;
            this.primaryPriority = primary.value();
            // @Primary encontrada con prioridad
        } else {
            this.isPrimary = false;
            this.primaryPriority = 0;
            // @Primary no encontrada
        }

        // Extraer @Alternative
        Alternative alternative = type.getAnnotation(Alternative.class);
        if (alternative != null) {
            this.isAlternative = true;
            this.alternativeProfile = alternative.profile();
            // @Alternative encontrada con perfil
        } else {
            this.isAlternative = false;
            this.alternativeProfile = "";
            // @Alternative no encontrada
        }

        // Resultado final del análisis
    }

    // Getters para los nuevos campos
    public boolean isPrimary() {
        return isPrimary;
    }

    public int getPrimaryPriority() {
        return primaryPriority;
    }

    public boolean isAlternative() {
        return isAlternative;
    }

    /**
     * Inicializa el constructor de inyección y parámetros
     */
    private void initializeConstructor() {
        try {
            this.injectConstructor = type.getDeclaredConstructor();
            this.injectConstructor.setAccessible(true);
        } catch (Exception e) {
            this.injectConstructor = null;
        }
        this.constructorParamTypes = injectConstructor != null ? injectConstructor.getParameterTypes() : new Class[0];
        this.constructorParamAnnotations = injectConstructor != null ? injectConstructor.getParameterAnnotations() : new Annotation[0][];
        this.constructorGenericTypes = injectConstructor != null ? injectConstructor.getGenericParameterTypes() : new Type[0];
    }

    public String getAlternativeProfile() {
        return alternativeProfile;
    }
    
    /**
     * 🚨 NUEVA VERIFICACIÓN: Verifica si una clase tiene constructores con parámetros que requieren inyección
     * Esta es la misma lógica que está en ConfigurationProcessor para mantener consistencia
     */
    private boolean hasConstructorWithInjectableParameters() {
        try {
            Constructor<?>[] constructors = type.getDeclaredConstructors();
            
            for (Constructor<?> constructor : constructors) {
                if (constructor.getParameterCount() > 0) {
                    Class<?>[] paramTypes = constructor.getParameterTypes();
                    Annotation[][] paramAnnotations = constructor.getParameterAnnotations();
                    
                    // Verificar si algún parámetro requiere inyección
                    for (int i = 0; i < paramTypes.length; i++) {
                        // Si el parámetro tiene @Inject o es una clase que probablemente necesite inyección
                        if (hasInjectAnnotation(paramAnnotations[i]) || isLikelyInjectableType(paramTypes[i])) {
                            return true;
                        }
                    }
                }
            }
            return false;
        } catch (Exception e) {
            log.log(Level.WARNING, "Error checking constructor parameters for " + type.getName() + ": {0}", e.getMessage());
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
}
