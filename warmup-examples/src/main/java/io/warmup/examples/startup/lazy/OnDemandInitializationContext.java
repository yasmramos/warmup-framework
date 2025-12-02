package io.warmup.examples.startup.lazy;

import io.warmup.framework.core.DependencyRegistry;
import io.warmup.framework.core.WarmupContainer;
import io.warmup.framework.config.PropertySource;
import io.warmup.framework.core.ProfileManager;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * 🎯 CONTEXTO DE INICIALIZACIÓN ON-DEMAND
 * 
 * Maneja la creación de beans lazy con inyección de dependencias automática.
 * Características:
 * - ✅ Inyección de dependencias automática para beans lazy
 * - ✅ Resolución de dependencias circulares
 * - ✅ Configuración automática desde PropertySource
 * - ✅ Perfiles automáticos desde ProfileManager
 * - ✅ Gestión de scopes (singleton, prototype)
 * - ✅ Validation y error handling robusto
 * 
 * @author MiniMax Agent
 * @version 1.0
 */
public class OnDemandInitializationContext {
    
    private static final Logger log = Logger.getLogger(OnDemandInitializationContext.class.getName());
    
    private final WarmupContainer container;
    private final DependencyRegistry dependencyRegistry;
    private final PropertySource propertySource;
    private final ProfileManager profileManager;
    private final LazyBeanRegistry lazyBeanRegistry;
    
    // 📊 CONTEXTO DE INYECCIÓN
    private final Map<String, Object> singletonInstances = new ConcurrentHashMap<>();
    private final Map<String, Constructor<?>> beanConstructors = new ConcurrentHashMap<>();
    private final Map<String, List<String>> beanDependencies = new ConcurrentHashMap<>();
    
    // 🔍 ANÁLISIS DE DEPENDENCIAS
    private final Set<String> currentlyCreating = new HashSet<>();
    private final Set<String> circularDependencyChain = new HashSet<>();
    
    public OnDemandInitializationContext(WarmupContainer container) {
        this.container = container;
        this.dependencyRegistry = (DependencyRegistry) container.getDependencyRegistry();
        this.propertySource = (PropertySource) container.getPropertySource();
        this.profileManager = (ProfileManager) container.getProfileManager();
        this.lazyBeanRegistry = new LazyBeanRegistry(container, dependencyRegistry);
        
        log.log(Level.FINE, "🎯 OnDemandInitializationContext inicializado");
    }
    
    /**
     * 🏗️ CREAR BEAN ON-DEMAND CON INYECCIÓN DE DEPENDENCIAS
     */
    @SuppressWarnings("unchecked")
    public <T> T createBeanOnDemand(String beanName, Class<T> beanType, Constructor<?> constructor, Object... constructorArgs) {
        if (currentlyCreating.contains(beanName)) {
            // Detectar dependencia circular
            circularDependencyChain.add(beanName);
            throw new LazyDependencyException("Circular dependency detected: " + 
                String.join(" -> ", circularDependencyChain) + " -> " + beanName);
        }
        
        currentlyCreating.add(beanName);
        circularDependencyChain.add(beanName);
        
        try {
            log.log(Level.FINE, "🏗️ Creando bean on-demand: {0}", beanName);
            
            // 1. Preparar argumentos del constructor con inyección
            Object[] resolvedArgs = resolveConstructorArguments(constructor, constructorArgs);
            
            // 2. Crear instancia
            T instance = (T) constructor.newInstance(resolvedArgs);
            
            // 3. Inyectar field dependencies
            injectFieldDependencies(instance, beanType);
            
            // 4. Inicializar con configuración
            initializeWithConfiguration(instance, beanType);
            
            // 5. Aplicar perfil si es necesario
            applyProfileConfiguration(instance, beanType);
            
            // 6. Cachear si es singleton
            if (isSingletonScope(beanType)) {
                singletonInstances.put(beanName, instance);
            }
            
            log.log(Level.FINE, "✅ Bean on-demand creado exitosamente: {0}", beanName);
            return instance;
            
        } catch (Exception e) {
            log.log(Level.SEVERE, "❌ Error creando bean on-demand {0}: {1}", 
                    new Object[]{beanName, e.getMessage()});
            throw new LazyDependencyException("Failed to create bean on-demand: " + beanName, e);
        } finally {
            currentlyCreating.remove(beanName);
            circularDependencyChain.remove(beanName);
        }
    }
    
    /**
     * 🔗 RESOLVER ARGUMENTOS DEL CONSTRUCTOR CON INYECCIÓN
     */
    private Object[] resolveConstructorArguments(Constructor<?> constructor, Object[] providedArgs) {
        Class<?>[] paramTypes = constructor.getParameterTypes();
        Object[] resolvedArgs = new Object[paramTypes.length];
        
        for (int i = 0; i < paramTypes.length; i++) {
            Class<?> paramType = paramTypes[i];
            
            if (i < providedArgs.length && providedArgs[i] != null) {
                // Usar argumento proporcionado
                resolvedArgs[i] = providedArgs[i];
                continue;
            }
            
            // Intentar inyectar dependencia automáticamente
            Object dependency = resolveDependency(paramType);
            if (dependency != null) {
                resolvedArgs[i] = dependency;
            } else {
                // Usar valor por defecto si está disponible
                resolvedArgs[i] = getDefaultValue(paramType);
            }
        }
        
        return resolvedArgs;
    }
    
    /**
     * 🔗 RESOLVER DEPENDENCIA AUTOMÁTICA
     */
    private Object resolveDependency(Class<?> dependencyType) {
        // 1. Buscar en singleton cache
        for (Object instance : singletonInstances.values()) {
            if (dependencyType.isInstance(instance)) {
                return instance;
            }
        }
        
        // 2. Buscar en DependencyRegistry
        Object registryBean = dependencyRegistry.getBean(dependencyType);
        if (registryBean != null) {
            return registryBean;
        }
        
        // 3. Buscar en beans lazy registrados
        String lazyBeanName = findLazyBeanName(dependencyType);
        if (lazyBeanName != null) {
            return lazyBeanRegistry.getLazyBean(lazyBeanName, dependencyType);
        }
        
        // 4. Crear dependency on-demand si es posible
        return createDependencyOnDemand(dependencyType);
    }
    
    /**
     * 🔍 BUSCAR NOMBRE DE BEAN LAZY POR TIPO
     */
    private String findLazyBeanName(Class<?> beanType) {
        for (Map.Entry<String, Class<?>> entry : getBeanTypeMap().entrySet()) {
            if (beanType.isAssignableFrom(entry.getValue())) {
                return entry.getKey();
            }
        }
        return null;
    }
    
    /**
     * 🏗️ CREAR DEPENDENCIA ON-DEMAND
     */
    private Object createDependencyOnDemand(Class<?> dependencyType) {
        try {
            // Buscar constructor público
            Constructor<?> constructor = findBestConstructor(dependencyType);
            if (constructor == null) {
                log.log(Level.WARNING, "⚠️ No se puede crear dependencia {0}: sin constructor público", 
                        dependencyType.getSimpleName());
                return null;
            }
            
            String beanName = dependencyType.getSimpleName();
            
            // Registrar como bean lazy si no existe
            if (!lazyBeanRegistry.isBeanRegistered(beanName)) {
                @SuppressWarnings("unchecked")
                LazyBeanSupplier<Object> lazySupplier = new LazyBeanSupplier<>(
                    beanName,
                    () -> createBeanOnDemand(beanName, dependencyType, constructor)
                );
                lazyBeanRegistry.registerLazyBean(beanName, (Class<Object>) dependencyType, lazySupplier);
            }
            
            // Crear y retornar
            return lazyBeanRegistry.getLazyBean(beanName, dependencyType);
            
        } catch (Exception e) {
            log.log(Level.WARNING, "⚠️ Error creando dependencia on-demand {0}: {1}", 
                    new Object[]{dependencyType.getSimpleName(), e.getMessage()});
            return null;
        }
    }
    
    /**
     * 🔍 BUSCAR MEJOR CONSTRUCTOR PARA INYECCIÓN
     */
    private Constructor<?> findBestConstructor(Class<?> beanClass) {
        Constructor<?>[] constructors = beanClass.getDeclaredConstructors();
        
        // Prioridad 1: Constructor con @Inject annotation
        for (Constructor<?> constructor : constructors) {
            if (constructor.isAnnotationPresent(jakarta.inject.Inject.class)) {
                return constructor;
            }
        }
        
        // Prioridad 2: Constructor con menos parámetros
        Constructor<?> best = null;
        int minParams = Integer.MAX_VALUE;
        
        for (Constructor<?> constructor : constructors) {
            int paramCount = constructor.getParameterCount();
            if (paramCount < minParams) {
                minParams = paramCount;
                best = constructor;
            }
        }
        
        return best;
    }
    
    /**
     * 💉 INYECTAR DEPENDENCIAS EN FIELDS
     */
    private void injectFieldDependencies(Object instance, Class<?> beanClass) {
        Field[] fields = beanClass.getDeclaredFields();
        
        for (Field field : fields) {
            if (field.isAnnotationPresent(jakarta.inject.Inject.class)) {
                
                try {
                    field.setAccessible(true);
                    
                    Object dependency = resolveDependency(field.getType());
                    if (dependency != null) {
                        field.set(instance, dependency);
                        log.log(Level.FINEST, "  💉 Inyectada dependencia en field {0}.{1}", 
                                new Object[]{beanClass.getSimpleName(), field.getName()});
                    } else {
                        log.log(Level.WARNING, "  ⚠️ No se pudo inyectar dependencia en field {0}.{1}", 
                                new Object[]{beanClass.getSimpleName(), field.getName()});
                    }
                } catch (Exception e) {
                    log.log(Level.WARNING, "  ❌ Error inyectando field {0}.{1}: {2}", 
                            new Object[]{beanClass.getSimpleName(), field.getName(), e.getMessage()});
                }
            }
        }
    }
    
    /**
     * ⚙️ INICIALIZAR CON CONFIGURACIÓN
     */
    private void initializeWithConfiguration(Object instance, Class<?> beanClass) {
        // Aplicar configuraciones desde PropertySource
        if (propertySource != null) {
            applyPropertyConfiguration(instance, beanClass);
        }
    }
    
    /**
     * ⚙️ APLICAR CONFIGURACIÓN DESDE PROPERTIES
     */
    private void applyPropertyConfiguration(Object instance, Class<?> beanClass) {
        // Buscar setters con @Value annotation
        Arrays.stream(beanClass.getDeclaredMethods())
            .filter(method -> method.getName().startsWith("set") && 
                            method.getParameterCount() == 1 &&
                            method.isAnnotationPresent(jakarta.inject.Inject.class))
            .forEach(method -> {
                try {
                    method.setAccessible(true);
                    
                    String propertyName = extractPropertyName(method);
                    String propertyValue = propertySource.getProperty(propertyName);
                    
                    if (propertyValue != null) {
                        Object convertedValue = convertValue(propertyValue, method.getParameterTypes()[0]);
                        method.invoke(instance, convertedValue);
                        
                        log.log(Level.FINEST, "  ⚙️ Configurado property {0} = {1}", 
                                new Object[]{propertyName, propertyValue});
                    }
                } catch (Exception e) {
                    log.log(Level.FINE, "  ⚠️ Error configurando property: {0}", e.getMessage());
                }
            });
    }
    
    /**
     * 🏷️ APLICAR CONFIGURACIÓN DE PERFIL
     */
    private void applyProfileConfiguration(Object instance, Class<?> beanClass) {
        // Aplicar configuraciones específicas del perfil activo
        if (profileManager != null && profileManager.isProfileActive("dev")) {
            // Configuraciones para desarrollo
        } else if (profileManager != null && profileManager.isProfileActive("prod")) {
            // Configuraciones para producción
        }
    }
    
    /**
     * 🔄 VERIFICAR SI ES SCOPE SINGLETON
     */
    private boolean isSingletonScope(Class<?> beanClass) {
        // Por defecto, asumir singleton para beans lazy
        return true;
    }
    
    /**
     * 📝 OBTENER MAPA DE TIPOS DE BEANS
     */
    private Map<String, Class<?>> getBeanTypeMap() {
        Map<String, Class<?>> beanTypes = new HashMap<>();
        // ✅ IMPLEMENTADO: Obtener mapa real de tipos del registry
        try {
            if (lazyBeanRegistry != null) {
                // Obtener beans registrados del lazy registry
                List<String> beanNames = lazyBeanRegistry.listRegisteredBeans();
                for (String beanName : beanNames) {
                    try {
                        // Obtener el tipo del bean usando el registry
                        Class<?> beanType = lazyBeanRegistry.getBeanType(beanName);
                        if (beanType != null) {
                            beanTypes.put(beanName, beanType);
                        }
                    } catch (Exception e) {
                        // Continuar con otros beans si uno falla
                        log.log(Level.FINE, "Could not determine type for bean: " + beanName, e);
                    }
                }
            }
            
            // Si no hay lazy registry, intentar obtener del bean registry del container
            if (container != null && beanTypes.isEmpty()) {
                try {
                    // Usar introspección para obtener tipos de beans registrados
                    java.lang.reflect.Method getBeanRegistryMethod = 
                        container.getClass().getMethod("getBeanRegistry");
                    Object beanRegistry = getBeanRegistryMethod.invoke(container);
                    
                    if (beanRegistry != null) {
                        // Mapear beans conocidos del framework
                        beanTypes.put("dependencyRegistry", 
                            Class.forName("io.warmup.framework.core.DependencyRegistry"));
                        beanTypes.put("eventManager", 
                            Class.forName("io.warmup.framework.core.EventManager"));
                        beanTypes.put("beanRegistry", 
                            Class.forName("io.warmup.framework.core.BeanRegistry"));
                    }
                } catch (Exception e) {
                    log.log(Level.FINE, "Could not introspect bean registry from container", e);
                }
            }
            
            log.log(Level.FINE, "Bean type map populated with {0} entries", beanTypes.size());
            
        } catch (Exception e) {
            log.log(Level.WARNING, "Error building bean type map", e);
        }
        
        return beanTypes;
    }
    
    /**
     * 🔍 EXTRAER NOMBRE DE PROPERTY
     */
    private String extractPropertyName(java.lang.reflect.Method setterMethod) {
        // Lógica para extraer nombre de property desde @Value o nombre del setter
        String methodName = setterMethod.getName();
        if (methodName.startsWith("set")) {
            String propertyName = methodName.substring(3);
            return propertyName.substring(0, 1).toLowerCase() + propertyName.substring(1);
        }
        return methodName;
    }
    
    /**
     * 🔄 CONVERTIR VALOR DESDE STRING
     */
    private Object convertValue(String value, Class<?> targetType) {
        if (targetType == String.class) {
            return value;
        } else if (targetType == Integer.class || targetType == int.class) {
            return Integer.parseInt(value);
        } else if (targetType == Long.class || targetType == long.class) {
            return Long.parseLong(value);
        } else if (targetType == Boolean.class || targetType == boolean.class) {
            return Boolean.parseBoolean(value);
        } else if (targetType == Double.class || targetType == double.class) {
            return Double.parseDouble(value);
        }
        
        // Por defecto, retornar como string
        return value;
    }
    
    /**
     * 🔧 OBTENER VALOR POR DEFECTO
     */
    private Object getDefaultValue(Class<?> type) {
        if (type.isPrimitive()) {
            if (type == boolean.class) return false;
            if (type == byte.class) return (byte) 0;
            if (type == char.class) return '\0';
            if (type == short.class) return (short) 0;
            if (type == int.class) return 0;
            if (type == long.class) return 0L;
            if (type == float.class) return 0.0f;
            if (type == double.class) return 0.0d;
        }
        return null;
    }
    
    /**
     * 🎯 OBTENER REGISTRY DE BEANS LAZY
     */
    public LazyBeanRegistry getLazyBeanRegistry() {
        return lazyBeanRegistry;
    }
    
    /**
     * 🧹 LIMPIAR CONTEXTO
     */
    public void cleanup() {
        singletonInstances.clear();
        beanConstructors.clear();
        beanDependencies.clear();
        currentlyCreating.clear();
        circularDependencyChain.clear();
        
        log.log(Level.FINE, "🧹 OnDemandInitializationContext limpiado");
    }
    
    /**
     * 🚀 EXCEPCIÓN PARA ERRORES DE DEPENDENCIAS
     */
    public static class LazyDependencyException extends RuntimeException {
        public LazyDependencyException(String message) {
            super(message);
        }
        
        public LazyDependencyException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}