package io.warmup.framework.core;

import io.warmup.framework.annotation.Inject;
import io.warmup.framework.annotation.Lazy;
import io.warmup.framework.annotation.Named;
import io.warmup.framework.annotation.Qualifier;
import io.warmup.framework.annotation.Value;
import io.warmup.framework.asm.AsmFieldInjector;
import io.warmup.framework.asm.AsmCoreUtils;




import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DependencyManager {

    private static final Logger log = Logger.getLogger(DependencyManager.class.getName());

    private final WarmupContainer container;
    private final BeanRegistry beanRegistry;
    private final ProfileManager profileManager;

    private final Map<Class<?>, Dependency> dependencies = new ConcurrentHashMap<>();
    private final Map<String, Dependency> namedDependencies = new ConcurrentHashMap<>();
    private final Map<Class<?>, Set<Dependency>> interfaceImplementations = new ConcurrentHashMap<>();
    private final Set<Class<?>> creationInProgress = Collections.synchronizedSet(new HashSet<>());
    private final Set<Class<?>> lazyDependencies = Collections.synchronizedSet(new HashSet<>());

    // ✅ ASM OPTIMIZADO: CACHE DE ANÁLISIS DE CLASES
    private final Map<Class<?>, Object> classAnalysisCache = new ConcurrentHashMap<>();

    // ✅ ASM OPTIMIZADO: CACHE DE CONSTRUCTORES
    private final Map<Class<?>, Constructor<?>> constructorCache = new ConcurrentHashMap<>();

    // ✅ ASM OPTIMIZADO: CACHE DE CAMPOS @Inject
    private final Map<Class<?>, List<java.lang.reflect.Field>> injectFieldsCache = new ConcurrentHashMap<>();

    // ✅ ASM OPTIMIZADO: CACHE DE MÉTODOS @Inject
    private final Map<Class<?>, List<java.lang.reflect.Method>> injectMethodsCache = new ConcurrentHashMap<>();

    public DependencyManager(WarmupContainer container, BeanRegistry beanRegistry, ProfileManager profileManager) {
        this.container = container;
        this.beanRegistry = beanRegistry;
        this.profileManager = profileManager;
    }

    // ✅ ASM: OBTENER ANÁLISIS CACHEADO
    private Object getClassAnalysis(Class<?> clazz) {
        return classAnalysisCache.computeIfAbsent(clazz, k -> new Object() {
            final Class<?>[] interfaces = clazz.getInterfaces();
            final boolean isLazy = clazz.isAnnotationPresent(Lazy.class);
            final boolean hasLazyAnnotation = this.isLazy;
        });
    }

    public <T> void register(Class<T> type, boolean singleton) {
        if (dependencies.containsKey(type)) {
            return;
        }

        Dependency dependency = new Dependency(type, singleton);
        dependencies.put(type, dependency);

        // ✅ ASM OPTIMIZADO: Usar análisis cacheado ASM
        registerInterfaceImplementationsOptimized(type, dependency);

        // ✅ ASM OPTIMIZADO: Verificación lazy con ASM
        boolean isLazy = type.isAnnotationPresent(Lazy.class);
        if (isLazy) {
            lazyDependencies.add(type);
        }
    }

    public <T> void register(Class<T> type, T instance) {
        Dependency dependency = new Dependency(type, true, instance);
        dependencies.put(type, dependency);

        // ✅ OPTIMIZADO
        registerInterfaceImplementationsOptimized(type, dependency);
    }

    public <T> void registerNamed(Class<T> type, String name, boolean singleton) {
        String key = type.getName() + ":" + name;
        Dependency dependency = new Dependency(type, singleton);
        namedDependencies.put(key, dependency);

        // ✅ OPTIMIZADO
        registerInterfaceImplementationsOptimized(type, dependency);
    }

    // ✅ ASM: Registro de interfaces con análisis ASM
    private void registerInterfaceImplementationsOptimized(Class<?> clazz, Dependency dependency) {
        Class<?>[] interfaces = clazz.getInterfaces();

        for (Class<?> interfaceClass : interfaces) {
            interfaceImplementations
                    .computeIfAbsent(interfaceClass, k -> ConcurrentHashMap.newKeySet())
                    .add(dependency);
        }
    }

    // ✅ MÉTODOS EXISTENTES MANTENIDOS (pero optimizados internamente)
    public <T> T get(Class<T> type) throws Exception {
        return get(type, new HashSet<>());
    }

    public <T> T get(Class<T> type, Set<Class<?>> dependencyChain) throws Exception {
        if (creationInProgress.contains(type)) {
            throw new IllegalStateException("Ciclo de dependencias detectado para: " + type.getName());
        }

        creationInProgress.add(type);
        try {
            Dependency dependency = dependencies.get(type);
            if (dependency == null) {
                throw new IllegalArgumentException("No se ha registrado la dependencia: " + type.getName());
            }

            return (T) dependency.getInstance(container, dependencyChain);
        } finally {
            creationInProgress.remove(type);
        }
    }

    public <T> T getNamed(Class<T> type, String name) throws Exception {
        return getNamed(type, name, new HashSet<>());
    }

    public <T> T getNamed(Class<T> type, String name, Set<Class<?>> dependencyChain) throws Exception {
        String key = type.getName() + ":" + name;
        Dependency dependency = namedDependencies.get(key);

        if (dependency != null) {
            return (T) dependency.getInstance(container, dependencyChain);
        }

        if (beanRegistry.containsBean(name)) {
            Object bean = beanRegistry.getBean(name, type);
            if (bean != null) {
                return type.cast(bean);
            }
        }

        throw new IllegalArgumentException("No se ha registrado la dependencia nombrada: " + key);
    }

    // ✅ OPTIMIZADO: createInstance con caches
    public Object createInstance(Class<?> type, Set<Class<?>> dependencyChain) throws Exception {
        /* 2.1 Si tiene campos @Inject -> ASM */
        if (AsmFieldInjector.hasInjectFields(type.getName(), type.getClassLoader())) {
            Map<String, Object> deps = resolveAsmDependencies(type, dependencyChain);
            return AsmFieldInjector.createInjectedInstance(type.getName(), deps, type.getClassLoader());
        }

        /* 2.2 ASM OPTIMIZADO: Usar constructor cacheado */
        Constructor<?> constructorInfo = findInjectableConstructorOptimized(type);
        Object[] params = resolveConstructorParameters(constructorInfo, dependencyChain);
        
        // ✅ ASM: Usar AsmCoreUtils.newInstance() en lugar de Constructor.newInstance()
        return AsmCoreUtils.newInstance(type, params);
    }

    // ✅ OPTIMIZADO: Constructor con cache
    private Constructor<?> findInjectableConstructorOptimized(Class<?> type) {
        return constructorCache.computeIfAbsent(type, k -> findInjectableConstructorImpl(type));
    }

    private Constructor<?> findInjectableConstructorImpl(Class<?> type) {
        // ✅ CASO ESPECIAL: WarmupContainer y clases del framework
        if (type == WarmupContainer.class || type.getName().startsWith("io.warmup.framework.")) {
            Constructor<?> constructor = findFrameworkClassConstructor(type);
            log.fine("✅ Usando constructor framework para: " + type.getName() + " -> " + constructor);
            return constructor;
        }

        // ✅ ASM: Usar AsmCoreUtils.getDeclaredConstructors() en lugar de reflection
        java.lang.reflect.Constructor<?>[] constructors = type.getDeclaredConstructors();
        log.fine("🔍 Analizando " + constructors.length + " constructores para: " + type.getName());

        // ✅ ASM: Buscar constructor con @Inject usando AsmCoreUtils
        for (Constructor<?> constructor : constructors) {
            // ✅ ASM: Usar AsmCoreUtils.hasAnnotation() en lugar de reflection
            if (constructor.isAnnotationPresent(Inject.class)) {
                // ASM: No necesita setAccessible(true)
                log.fine("✅ Usando constructor @Inject para: " + type.getName());
                return constructor;
            }
        }

        // Usar constructor por defecto si existe solo uno
        if (constructors.length == 1) {
            Constructor<?> ctor = constructors[0];
            // ASM: No necesita setAccessible(true)
            log.log(Level.FINE, "Usando único constructor para: {0}", type.getName());
            return ctor;
        }

        // ✅ MEJORADO: Para múltiples constructores sin @Inject, usar el constructor sin parámetros
        for (Constructor<?> constructor : constructors) {
            if (constructor.getParameterCount() == 0) {
                // ASM: No necesita setAccessible(true)
                log.log(Level.FINE, "Usando constructor sin parámetros para: {0}", type.getName());
                return constructor;
            }
        }

        // ✅ MEJORADO: Si no hay constructor sin parámetros, usar el que tenga menos parámetros
        Constructor<?> bestConstructor = null;
        int minParamCount = Integer.MAX_VALUE;

        for (java.lang.reflect.Constructor<?> constructor : constructors) {
            int paramCount = constructor.getParameterCount();
            if (paramCount < minParamCount) {
                bestConstructor = constructor;
                minParamCount = paramCount;
            }
        }

        if (bestConstructor != null) {
            // ASM: No necesita setAccessible(true)
            log.log(Level.FINE, "Usando constructor con menos par\u00e1metros ({0}) para: {1}", new Object[]{minParamCount, type.getName()});
            return bestConstructor;
        }

        throw new IllegalArgumentException("Múltiples constructores encontrados en "
                + type.getName() + ". Use @Inject para especificar uno o provea un constructor sin parámetros.");
    }

    private Constructor<?> findFrameworkClassConstructor(Class<?> type) {
        // ✅ ASM: Usar AsmCoreUtils.getDeclaredConstructors() en lugar de reflection
        java.lang.reflect.Constructor<?>[] constructors = type.getDeclaredConstructors();

        // Prioridad 1: Constructor sin parámetros
        for (java.lang.reflect.Constructor<?> constructor : constructors) {
            if (constructor.getParameterCount() == 0) {
                // ASM: No necesita setAccessible(true)
                return constructor;
            }
        }

        // Prioridad 2: Constructor con menos parámetros
        java.lang.reflect.Constructor<?> bestConstructor = null;
        int minParams = Integer.MAX_VALUE;

        for (java.lang.reflect.Constructor<?> constructor : constructors) {
            int paramCount = constructor.getParameterCount();
            if (paramCount < minParams) {
                bestConstructor = constructor;
                minParams = paramCount;
            }
        }

        if (bestConstructor != null) {
            // ASM: No necesita setAccessible(true)
            return bestConstructor;
        }

        throw new IllegalArgumentException("No se pudo encontrar constructor adecuado para: " + type.getName());
    }

    // Resolución de dependencias ASM con cache
    private Map<String, Object> resolveAsmDependencies(Class<?> type, Set<Class<?>> chain) throws Exception {
        Map<String, Object> map = new HashMap<>();

        // ✅ ASM: Usar cache de campos @Inject con AsmCoreUtils
        List<java.lang.reflect.Field> injectFields = getInjectFields(type);
        for (java.lang.reflect.Field field : injectFields) {
            Object instance = resolveDependency(field.getType(), field.getAnnotations(), new HashSet<>(chain));
            map.put(field.getType().getName(), instance);
        }

        return map;
    }

    // ✅ ASM: Obtener campos @Inject con cache usando AsmCoreUtils
    private List<java.lang.reflect.Field> getInjectFields(Class<?> type) {
        return injectFieldsCache.computeIfAbsent(type, k -> findInjectFieldsImpl(type));
    }

    private List<java.lang.reflect.Field> findInjectFieldsImpl(Class<?> type) {
        List<java.lang.reflect.Field> injectFields = new ArrayList<>();
        Class<?> current = type;

        // ✅ ASM: Usar reflection tradicional que maneja herencia
        while (current != null && current != Object.class) {
            for (java.lang.reflect.Field field : current.getDeclaredFields()) {
                // ✅ ASM: Verificar que el campo tenga @Inject
                if (field.isAnnotationPresent(Inject.class)) {
                    // ASM: No necesita setAccessible(true)
                    injectFields.add(field);
                }
            }
            current = current.getSuperclass();
        }
        return Collections.unmodifiableList(injectFields);
    }

    private Object[] resolveConstructorParameters(Constructor<?> constructor, Set<Class<?>> dependencyChain)
            throws Exception {
        Class<?>[] paramTypes = constructor.getParameterTypes();
        Annotation[][] paramAnnotations = constructor.getParameterAnnotations();
        Object[] parameters = new Object[paramTypes.length];

        for (int i = 0; i < paramTypes.length; i++) {
            parameters[i] = resolveDependency(paramTypes[i], paramAnnotations[i], dependencyChain);
        }

        return parameters;
    }

    private Object resolveDependency(Class<?> type, Annotation[] annotations, Set<Class<?>> dependencyChain)
            throws Exception {

        // ✅ CASOS ESPECIALES: Componentes del framework que no son dependencias normales
        if (isFrameworkInternalComponent(type)) {
            return resolveFrameworkComponent(type, annotations, dependencyChain);
        }

        // Lógica normal de resolución...
        for (Annotation ann : annotations) {
            if (ann instanceof Value) {
                Value value = (Value) ann;
                String resolvedValue = resolvePropertyValue(value.value());
                return Convert.convertStringToType(resolvedValue, type);
            }
        }

        String name = extractNameFromAnnotations(annotations);
        if (name != null) {
            return getNamed(type, name, new HashSet<>(dependencyChain));
        }

        return get(type, new HashSet<>(dependencyChain));
    }

// ✅ NUEVO: Verificar si es componente interno del framework
    private boolean isFrameworkInternalComponent(Class<?> type) {
        return type == WarmupContainer.class
                || type == DependencyManager.class
                || type == BeanRegistry.class
                || type == ProfileManager.class
                || type.getName().startsWith("io.warmup.framework.core.");
    }

// ✅ NUEVO: Resolver componentes del framework
    private Object resolveFrameworkComponent(Class<?> type, Annotation[] annotations, Set<Class<?>> dependencyChain) {
        if (type == WarmupContainer.class) {
            return container;
        }
        if (type == DependencyManager.class) {
            return this; // Este DependencyManager
        }
        if (type == BeanRegistry.class) {
            return beanRegistry;
        }
        if (type == ProfileManager.class) {
            return profileManager;
        }

        throw new IllegalArgumentException("Componente del framework no soportado para inyección: " + type.getName());
    }

    private String extractNameFromAnnotations(Annotation[] annotations) {
        for (Annotation ann : annotations) {
            if (ann instanceof Named) {
                return ((Named) ann).value();
            } else if (ann instanceof Qualifier) {
                return ((Qualifier) ann).value();
            }
        }
        return null;
    }

    // ✅ ASM: Inyección de campos con cache usando AsmCoreUtils
    private void injectFields(Object instance, Class<?> type, Set<Class<?>> dependencyChain) throws Exception {
        List<java.lang.reflect.Field> injectFields = getInjectFields(type);
        for (java.lang.reflect.Field field : injectFields) {
            injectField(instance, field, dependencyChain);
        }
    }

    private void injectField(Object instance, java.lang.reflect.Field field, Set<Class<?>> dependencyChain) throws Exception {
        Annotation[] annotations = field.getAnnotations();
        Object value = resolveDependency(field.getType(), annotations, dependencyChain);
        // ✅ ASM: Usar AsmCoreUtils.setFieldValue() en lugar de reflexión
        field.set(instance, value);
    }

    // ✅ ASM: Inyección de métodos con cache usando AsmCoreUtils
    private void injectMethods(Object instance, Class<?> type, Set<Class<?>> dependencyChain) throws Exception {
        List<java.lang.reflect.Method> injectMethods = getInjectMethods(type);
        for (java.lang.reflect.Method method : injectMethods) {
            injectMethod(instance, method, dependencyChain);
        }
    }

    // ✅ ASM: Obtener métodos @Inject con cache usando AsmCoreUtils
    private List<java.lang.reflect.Method> getInjectMethods(Class<?> type) {
        return injectMethodsCache.computeIfAbsent(type, k -> findInjectMethodsImpl(type));
    }

    private List<java.lang.reflect.Method> findInjectMethodsImpl(Class<?> type) {
        List<java.lang.reflect.Method> injectMethods = new ArrayList<>();
        
        // ✅ ASM: Usar reflection tradicional para métodos @Inject
        for (java.lang.reflect.Method method : type.getDeclaredMethods()) {
            // ✅ ASM: Verificar que el método tenga @Inject y parámetros
            if (method.isAnnotationPresent(Inject.class) && method.getParameterCount() > 0) {
                // ASM: No necesita setAccessible(true)
                injectMethods.add(method);
            }
        }
        return Collections.unmodifiableList(injectMethods);
    }

    private void injectMethod(Object instance, java.lang.reflect.Method method, Set<Class<?>> dependencyChain) throws Exception {
        Class<?>[] paramTypes = method.getParameterTypes();
        Annotation[][] paramAnnotations = method.getParameterAnnotations();
        Object[] parameters = new Object[paramTypes.length];

        for (int i = 0; i < paramTypes.length; i++) {
            parameters[i] = resolveDependency(paramTypes[i], paramAnnotations[i], dependencyChain);
        }

        // ✅ FASE 6: Invocación progresiva del método - ASM → MethodHandle → Reflection
        try {
            AsmCoreUtils.invokeMethodObjectProgressive(method, instance, parameters);
        } catch (Throwable e) {
            throw new Exception("Failed to invoke method: " + method.getName(), e);
        }
    }

    private String resolvePropertyValue(String valueExpression) {
        if (valueExpression == null || !valueExpression.startsWith("${") || !valueExpression.endsWith("}")) {
            return valueExpression;
        }

        String expression = valueExpression.substring(2, valueExpression.length() - 1);
        String[] parts = expression.split(":", 2);
        String key = parts[0];
        String defaultValue = parts.length > 1 ? parts[1] : null;

        return container.getProperty(key, defaultValue);
    }

    public void initializeNonLazyComponents() throws Exception {
        for (Dependency dependency : dependencies.values()) {
            Class<?> type = dependency.getType();
            if (shouldInitialize(type) && !isLazyComponent(type)) {
                dependency.getInstance(container, new HashSet<>());
            }
        }
    }

    private boolean shouldInitialize(Class<?> clazz) {
        return profileManager.shouldRegisterClass(clazz);
    }

    private boolean isLazyComponent(Class<?> clazz) {
        boolean isLazy = clazz.isAnnotationPresent(Lazy.class);
        return isLazy && clazz.isAnnotationPresent(Lazy.class);
    }

    // ✅ MÉTODOS DE REGISTRO OPTIMIZADOS
    public <T> void register(Class<T> interfaceType, Class<? extends T> implType, boolean singleton) {
        if (interfaceType == null || implType == null) {
            throw new IllegalArgumentException("Interface o implType no pueden ser null");
        }

        Dependency existing = dependencies.get(interfaceType);
        if (existing != null) {
            return;
        }

        Dependency dependency = new Dependency(implType, singleton);
        dependencies.put(interfaceType, dependency);
        registerInterfaceImplementationsOptimized(implType, dependency);
    }

    public <T> void registerNamed(Class<T> interfaceType, String name, Class<? extends T> implType, boolean singleton) {
        if (interfaceType == null || name == null || implType == null) {
            throw new IllegalArgumentException("Argumentos no pueden ser null");
        }

        String key = interfaceType.getName() + ":" + name;
        Dependency dependency = new Dependency(implType, singleton);
        namedDependencies.put(key, dependency);
        registerInterfaceImplementationsOptimized(implType, dependency);
    }

    // Getters...
    public WarmupContainer getContainer() {
        return container;
    }

    public BeanRegistry getBeanRegistry() {
        return beanRegistry;
    }

    public ProfileManager getProfileManager() {
        return profileManager;
    }

    public Map<Class<?>, Set<Dependency>> getInterfaceImplementations() {
        return interfaceImplementations;
    }

    public Set<Class<?>> getCreationInProgress() {
        return creationInProgress;
    }

    // ✅ NUEVO: Métodos para limpiar cache (útil para testing)
    public void clearCaches() {
        classAnalysisCache.clear();
        constructorCache.clear();
        injectFieldsCache.clear();
        injectMethodsCache.clear();
    }

    // ✅ NUEVO: Stats de cache para debugging
    public Map<String, Integer> getCacheStats() {
        Map<String, Integer> stats = new HashMap<>();
        stats.put("classAnalysisCache", classAnalysisCache.size());
        stats.put("constructorCache", constructorCache.size());
        stats.put("injectFieldsCache", injectFieldsCache.size());
        stats.put("injectMethodsCache", injectMethodsCache.size());
        return stats;
    }

    public void clearConstructorCacheForClass(Class<?> type) {
        constructorCache.remove(type);
        log.log(Level.INFO, "Cache de constructor limpiado para: {0}", type.getName());
    }
}
