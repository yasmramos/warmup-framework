package io.warmup.framework.core;

import io.warmup.framework.aop.AspectDecorator;
import io.warmup.framework.aop.AspectInfo;
import io.warmup.framework.aop.AspectManager;
import io.warmup.framework.asm.AsmCoreUtils;
import io.warmup.framework.asm.AsmMethodInvoker;
import java.lang.reflect.Method;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class AopHandler {

    private static final Logger log = Logger.getLogger(AopHandler.class.getName());

    private AspectManager aspectManager;
    private WarmupContainer container; // Necesario para pasar al decorador y para otras operaciones
    private boolean aopEnabled = true; // Mantener la configuración aquí

    public AopHandler(WarmupContainer container) {
        this.container = container;
        // Use constructor without container if null to break circular dependency
        this.aspectManager = container != null ? 
            new AspectManager(container) : 
            new AspectManager();
    }
    
    /**
     * Set container reference after initialization to break circular dependency
     */
    public void setContainer(WarmupContainer container) {
        this.container = container;
        this.aspectManager = new AspectManager(container);
    }

    public void enableAop(boolean enabled) {
        this.aopEnabled = enabled;
        log.log(Level.INFO, "AOP {0}", enabled ? "habilitado" : "deshabilitado");
    }

    public boolean isAopEnabled() {
        return aopEnabled;
    }

    public boolean isEnabled() {
        return aopEnabled;
    }

    public <T> T applyAopIfNeeded(T instance, Class<T> type) {
        if (!aopEnabled || !shouldApplyAopToClass(type) || aspectManager.getAspects().isEmpty()) {
            log.log(Level.FINE, "AOP no aplicado a: {0}", type.getSimpleName());
            return instance;
        }
        log.log(Level.FINE, "Aplicando AOP a: {0}", type.getSimpleName());
        return (T) AspectDecorator.createDecorator(instance, type, container);
    }

    /**
     * Método específico para aplicar AOP cuando el tipo no es conocido exactamente.
     * Este método maneja Object de manera segura sin problemas de tipos genéricos.
     */
    public Object applyAopToObject(Object instance) {
        if (!aopEnabled || instance == null) {
            return instance;
        }
        
        Class<?> actualType = instance.getClass();
        if (!shouldApplyAopToClass(actualType) || aspectManager.getAspects().isEmpty()) {
            log.log(Level.FINE, "AOP no aplicado a: {0}", actualType.getSimpleName());
            return instance;
        }
        
        log.log(Level.FINE, "Aplicando AOP a: {0}", actualType.getSimpleName());
        try {
            return AspectDecorator.createDecoratorForObject(instance, actualType, container);
        } catch (Exception e) {
            log.log(Level.WARNING, "Error aplicando AOP a instancia: " + actualType.getSimpleName(), e);
            return instance;
        }
    }

    public boolean shouldApplyAopToClass(Class<?> clazz) {
        return aspectManager.shouldApplyAopToClass(clazz);
    }

    public Object invokeWithAspects(Object target, Method method, Object[] args) throws Throwable {
        // ✅ ASM OPTIMIZADO: Usar AsmMethodInvoker para invocación optimizada
        return aspectManager.invokeWithAspects(target, method, args);
    }

    public io.warmup.framework.aop.AspectManager getAspectManager() {
        return aspectManager;
    }

    public Object invokeMethodWithAspects(Object target, String methodName, Object... args) throws Throwable {
        // 🚀 MIGRACIÓN FASE 1: Estrategia Progresiva Optimizada
        // 1. MethodHandle como primera opción (Java 8+)
        // 2. Fallback automático a ASM existente
        // 3. Último recurso: reflexión pura
        
        Class<?>[] paramTypes = getParameterTypesFromArgs(args);
        
        try {
            // ✅ MÉTODO OPTIMIZADO: Usar estrategia progresiva existente
            Object result = AsmCoreUtils.invokeMethodProgressive(target, methodName, args);
            
            // Crear Method reflexión para AOP
            Method reflectionMethod = findMethodReflection(target.getClass(), methodName, paramTypes);
            
            return invokeWithAspects(target, reflectionMethod, args);
            
        } catch (RuntimeException e) {
            // Fallback manual si la estrategia automática falla
            return invokeMethodWithAspectsFallback(target, methodName, args);
        }
    }
    
    /**
     * Método de fallback usando reflexión (solo para casos extremos)
     */
    private Object invokeMethodWithAspectsFallback(Object target, String methodName, Object[] args) throws Throwable {
        // ❌ REFLEXIÓN: Solo como último recurso
        Class<?>[] paramTypes = new Class[args.length];
        for (int i = 0; i < args.length; i++) {
            paramTypes[i] = args[i].getClass();
        }
        
        try {
            Method method = target.getClass().getDeclaredMethod(methodName, paramTypes);
            return invokeWithAspects(target, method, args);
        } catch (NoSuchMethodException e) {
            // Búsqueda iterativa como último recurso
            Method[] methods = target.getClass().getDeclaredMethods();
            for (Method method : methods) {
                if (method.getName().equals(methodName) && method.getParameterCount() == args.length) {
                    return invokeWithAspects(target, method, args);
                }
            }
            throw new RuntimeException("Método no encontrado: " + methodName, e);
        }
    }
    
    /**
     * 🚀 NUEVO: Método especializado para invocación con AOP usando ASM existente
     * Usa los métodos ASM ya implementados
     */
    public Object invokeMethodWithAspectsASM(Object target, String methodName, Object... args) throws Throwable {
        try {
            Class<?>[] paramTypes = getParameterTypesFromArgs(args);
            
            // Usar el método ASM existente
            Object result = AsmCoreUtils.invokeMethod(target, methodName, args);
            
            // Crear Method reflexión para AOP
            Method reflectionMethod = findMethodReflection(target.getClass(), methodName, paramTypes);
            
            return invokeWithAspects(target, reflectionMethod, args);
            
        } catch (Exception e) {
            throw new RuntimeException("ASM invocation failed: " + methodName, e);
        }
    }
    
    /**
     * 🚀 NUEVO: Método especializado para invocación con AOP usando estrategia progresiva
     * Balance óptimo entre rendimiento y compatibilidad
     */
    public Object invokeMethodWithAspectsMethodHandle(Object target, String methodName, Object... args) throws Throwable {
        try {
            // Usar estrategia progresiva con MethodHandle
            Object result = AsmCoreUtils.invokeMethodProgressive(target, methodName, args);
            
            Class<?>[] paramTypes = getParameterTypesFromArgs(args);
            Method reflectionMethod = findMethodReflection(target.getClass(), methodName, paramTypes);
            
            return invokeWithAspects(target, reflectionMethod, args);
            
        } catch (Exception e) {
            // Fallback a ASM
            try {
                return invokeMethodWithAspectsASM(target, methodName, args);
            } catch (Exception e2) {
                // Último recurso: reflexión
                return invokeMethodWithAspectsFallback(target, methodName, args);
            }
        }
    }
    
    /**
     * Helper para encontrar método usando reflexión
     */
    private Method findMethodReflection(Class<?> clazz, String methodName, Class<?>[] paramTypes) {
        try {
            return clazz.getDeclaredMethod(methodName, paramTypes);
        } catch (NoSuchMethodException e) {
            // Búsqueda por nombre y conteo de parámetros
            for (Method method : clazz.getDeclaredMethods()) {
                if (method.getName().equals(methodName) && method.getParameterCount() == paramTypes.length) {
                    return method;
                }
            }
            throw new RuntimeException("Método no encontrado: " + methodName + " en " + clazz.getName(), e);
        }
    }
    
    /**
     * Helper para obtener tipos de parámetros
     */
    private Class<?>[] getParameterTypesFromArgs(Object[] args) {
        if (args == null || args.length == 0) return new Class<?>[0];
        
        Class<?>[] paramTypes = new Class[args.length];
        for (int i = 0; i < args.length; i++) {
            paramTypes[i] = args[i] != null ? args[i].getClass() : Object.class;
        }
        return paramTypes;
    }

    public boolean matchesPointcut(Method method, String pointcutExpression) {
        return aspectManager.matchesPointcut(method, pointcutExpression);
    }

    // Métodos para registrar aspectos (esto podría ocurrir durante el escaneo)
    public void registerAspects(Class<?> clazz, Object instance) {
        aspectManager.registerAspect(clazz, instance);
    }

    public List<AspectInfo> getAspects() {
        return aspectManager.getAspects();
    }

    public void setAspects(List<AspectInfo> aspects) {
        aspectManager.setAspects(aspects);
    }

    /**
     * Initializes the AOP handler
     */
    public void initialize() {
        log.log(Level.INFO, "Initializing AOP Handler");
        
        // Initialize aspect manager if needed
        if (aspectManager != null) {
            log.log(Level.FINE, "AOP Handler initialized with {0} aspects", 
                   aspectManager.getAspects() != null ? aspectManager.getAspects().size() : 0);
        }
    }
    
    /**
     * Warmup aspects
     */
    public void warmupAspects() {
        log.log(Level.INFO, "Warming up AOP aspects");
        
        // Pre-initialize aspects
        if (aspectManager != null && aspectManager.getAspects() != null) {
            log.log(Level.FINE, "Warming up {0} aspects", aspectManager.getAspects().size());
        }
    }

}
