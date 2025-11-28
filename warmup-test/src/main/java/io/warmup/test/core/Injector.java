package io.warmup.test.core;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Map;

/**
 * Inyector de dependencias que maneja la inyección automática de mocks
 * en spies basándose en análisis de tipos y constructores.
 */
class Injector {
    
    /**
     * Inyectar dependencias en un objeto basándose en un mapa de objetos compatibles.
     */
    public void injectDependencies(Object target, Map<Class<?>, Object> dependencies) {
        if (target == null || dependencies == null || dependencies.isEmpty()) {
            return;
        }
        
        // Intentar inyección por constructor primero
        injectViaConstructor(target, dependencies);
        
        // Luego inyección por setters
        injectViaSetters(target, dependencies);
        
        // Finalmente inyección directa por campos
        injectViaFields(target, dependencies);
    }
    
    private void injectViaConstructor(Object target, Map<Class<?>, Object> dependencies) {
        Class<?> targetType = target.getClass();
        
        try {
            Constructor<?>[] constructors = targetType.getDeclaredConstructors();
            
            // Buscar constructor con parámetros compatibles
            Constructor<?> suitableConstructor = findSuitableConstructor(constructors, dependencies);
            
            if (suitableConstructor != null) {
                Object[] args = buildConstructorArguments(suitableConstructor, dependencies);
                suitableConstructor.setAccessible(true);
                suitableConstructor.newInstance(args);
            }
        } catch (Exception e) {
            // Constructor injection falló, continuar con otros métodos
        }
    }
    
    private Constructor<?> findSuitableConstructor(Constructor<?>[] constructors, 
                                                  Map<Class<?>, Object> dependencies) {
        
        return Arrays.stream(constructors)
                .filter(ctor -> {
                    Class<?>[] paramTypes = ctor.getParameterTypes();
                    
                    // Verificar si todos los parámetros tienen dependencias compatibles
                    return Arrays.stream(paramTypes)
                            .allMatch(paramType -> hasCompatibleDependency(paramType, dependencies));
                })
                .max((a, b) -> Integer.compare(a.getParameterCount(), b.getParameterCount()))
                .orElse(null);
    }
    
    private Object[] buildConstructorArguments(Constructor<?> constructor, 
                                             Map<Class<?>, Object> dependencies) {
        
        Class<?>[] paramTypes = constructor.getParameterTypes();
        Object[] args = new Object[paramTypes.length];
        
        for (int i = 0; i < paramTypes.length; i++) {
            args[i] = findCompatibleInstance(paramTypes[i], dependencies);
        }
        
        return args;
    }
    
    private void injectViaSetters(Object target, Map<Class<?>, Object> dependencies) {
        Class<?> targetType = target.getClass();
        
        for (Method method : targetType.getDeclaredMethods()) {
            if (isSetterMethod(method)) {
                Class<?> paramType = method.getParameterTypes()[0];
                Object dependency = findCompatibleInstance(paramType, dependencies);
                
                if (dependency != null) {
                    try {
                        method.setAccessible(true);
                        method.invoke(target, dependency);
                    } catch (Exception e) {
                        // Setter injection falló, continuar
                    }
                }
            }
        }
    }
    
    private void injectViaFields(Object target, Map<Class<?>, Object> dependencies) {
        Class<?> targetType = target.getClass();
        
        for (Field field : targetType.getDeclaredFields()) {
            if (shouldInjectField(field)) {
                Object dependency = findCompatibleInstance(field.getType(), dependencies);
                
                if (dependency != null) {
                    try {
                        field.setAccessible(true);
                        field.set(target, dependency);
                    } catch (Exception e) {
                        // Field injection falló, continuar
                    }
                }
            }
        }
    }
    
    private boolean hasCompatibleDependency(Class<?> paramType, Map<Class<?>, Object> dependencies) {
        return dependencies.keySet().stream()
                .anyMatch(depType -> isCompatibleType(paramType, depType));
    }
    
    private Object findCompatibleInstance(Class<?> requiredType, Map<Class<?>, Object> dependencies) {
        // Buscar dependencia exacta primero
        if (dependencies.containsKey(requiredType)) {
            return dependencies.get(requiredType);
        }
        
        // Buscar tipo compatible
        return dependencies.entrySet().stream()
                .filter(entry -> isCompatibleType(requiredType, entry.getKey()))
                .map(Map.Entry::getValue)
                .findFirst()
                .orElse(null);
    }
    
    private boolean isCompatibleType(Class<?> required, Class<?> available) {
        // Verificar si los tipos son compatibles
        return required.isAssignableFrom(available) || available.isAssignableFrom(required);
    }
    
    private boolean isSetterMethod(Method method) {
        return method.getName().startsWith("set") 
               && method.getParameterCount() == 1
               && method.getReturnType() == void.class
               && !method.getName().equals("set");
    }
    
    private boolean shouldInjectField(Field field) {
        // Inyectar todos los campos excepto static y final
        return !java.lang.reflect.Modifier.isStatic(field.getModifiers()) 
               && !java.lang.reflect.Modifier.isFinal(field.getModifiers());
    }
}