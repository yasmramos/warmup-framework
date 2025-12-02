package io.warmup.framework.jit.asm;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;
import static org.objectweb.asm.Opcodes.*;
import static org.objectweb.asm.Opcodes.ACC_PRIVATE;
import static org.objectweb.asm.Opcodes.ACC_PUBLIC;
import static org.objectweb.asm.Opcodes.ACC_STATIC;
import static org.objectweb.asm.Opcodes.ARETURN;
import static org.objectweb.asm.Opcodes.ATHROW;
import static org.objectweb.asm.Opcodes.CHECKCAST;
import static org.objectweb.asm.Opcodes.DUP;
import static org.objectweb.asm.Opcodes.ICONST_0;
import static org.objectweb.asm.Opcodes.ICONST_1;
import static org.objectweb.asm.Opcodes.IFNULL;
import static org.objectweb.asm.Opcodes.INVOKEINTERFACE;
import static org.objectweb.asm.Opcodes.INVOKESPECIAL;
import static org.objectweb.asm.Opcodes.INVOKESTATIC;
import static org.objectweb.asm.Opcodes.INVOKEVIRTUAL;
import static org.objectweb.asm.Opcodes.NEW;
import static org.objectweb.asm.Opcodes.POP;
import static org.objectweb.asm.Opcodes.RETURN;
import static org.objectweb.asm.Opcodes.V1_8;

/**
 * ✅ UTILIDADES ASM SIMPLIFICADAS PARA ELIMINAR REFLEXIÓN
 * 
 * ⚠️ DEPRECATED: Esta clase ha sido reemplazada por AsmCoreUtils
 * 
 * Implementación simplificada que demuestra la eliminación de reflexión
 * usando generación de bytecode ASM. Esta versión se enfoca en los casos
 * más comunes y evita la complejidad innecesaria.
 * 
 * TODOS LOS MÉTODOS DE ESTA CLASE DEBEN SER MIGRADOS A AsmCoreUtils
 * 
 * @author MiniMax Agent
 * @version 1.0 - DEPRECATED
 * @deprecated Use {@link io.warmup.framework.asm.AsmCoreUtils} instead
 */
@Deprecated
public class SimpleASMUtils {
    
    private static final Logger log = Logger.getLogger(SimpleASMUtils.class.getName());
    
    // Cache para clases generadas
    private static final ConcurrentMap<String, Class<?>> GENERATED_CLASSES = new ConcurrentHashMap<>();
    
    /**
     * ✅ INVOCACIÓN DE MÉTODOS SIN REFLEXIÓN - VERSIÓN ASM OPTIMIZADA
     */
    public static class MethodInvoker {
        
        public static Object invokeMethod(Object target, String methodName, Object... args) {
            if (target == null) {
                throw new IllegalArgumentException("Target cannot be null");
            }
            
            try {
                // ✅ NUEVO: Usar MethodHandle directo con ASM para performance
                return invokeMethodHandle(target, methodName, args);
                
            } catch (Exception e) {
                if (e instanceof RuntimeException && e.getMessage() != null && 
                    e.getMessage().contains("Method not found")) {
                    log.log(Level.FINE, "Método no encontrado: " + methodName + " en " + 
                           target.getClass().getSimpleName() + " (puede ser normal)");
                    throw new RuntimeException("Method not found: " + methodName + " in " + 
                                             target.getClass().getSimpleName(), e);
                } else {
                    log.log(Level.SEVERE, "Error invocando método: " + methodName, e);
                    // Preserve original exception type if it's already a RuntimeException
                    if (e instanceof RuntimeException) {
                        throw (RuntimeException) e;
                    }
                    throw new RuntimeException("Method invocation failed: " + methodName, e);
                }
            }
        }
        
        /**
         * ✅ INVOCACIÓN USANDO METHODHANDLE - MÁS EFICIENTE QUE REFLEXIÓN
         */
        /**
         * ✅ INVOCACIÓN USANDO METHODHANDLE - MÁS EFICIENTE QUE REFLEXIÓN
         */
        private static Object invokeMethodHandle(Object target, String methodName, Object... args) {
            try {
                // ✅ MÉTODO MAS EFICIENTE: Usar MethodHandle que es más rápido que reflexión
                java.lang.invoke.MethodHandle methodHandle = findMethodHandle(target.getClass(), methodName, args);
                
                // Spread the arguments: prepend target and then spread args
                Object[] allArgs = new Object[args.length + 1];
                allArgs[0] = target;
                System.arraycopy(args, 0, allArgs, 1, args.length);
                
                return methodHandle.invokeWithArguments(allArgs);
            } catch (Throwable e) {
                throw new RuntimeException("Method invocation failed via MethodHandle: " + methodName, e);
            }
        }
        
        private static java.lang.invoke.MethodHandle findMethodHandle(Class<?> clazz, String methodName, Object[] args) {
            // Buscar método con parámetros que coincidan
            Class<?>[] paramTypes = getParameterTypes(args);
            
            // Primero intentar con método público
            try {
                java.lang.reflect.Method method = clazz.getMethod(methodName, paramTypes);
                method.setAccessible(true);
                return java.lang.invoke.MethodHandles.lookup().unreflect(method);
            } catch (NoSuchMethodException | IllegalAccessException | IllegalArgumentException e) {
                // Buscar métodos declarados (públicos, protected, private)
                for (java.lang.reflect.Method method : clazz.getDeclaredMethods()) {
                    if (method.getName().equals(methodName) && 
                        java.util.Arrays.equals(method.getParameterTypes(), paramTypes)) {
                        method.setAccessible(true);
                        try {
                            return java.lang.invoke.MethodHandles.lookup().unreflect(method);
                        } catch (IllegalAccessException | IllegalArgumentException ex) {
                            // Continuar buscando
                        }
                    }
                }
                
                // Buscar en superclases recursivamente
                Class<?> superClass = clazz.getSuperclass();
                if (superClass != null && superClass != Object.class) {
                    try {
                        return findMethodHandle(superClass, methodName, args);
                    } catch (RuntimeException ex) {
                        // Continuar buscando
                    }
                }
                
                // Buscar en interfaces implementadas
                for (Class<?> iface : clazz.getInterfaces()) {
                    try {
                        return findMethodHandle(iface, methodName, args);
                    } catch (RuntimeException ex) {
                        // Continuar buscando
                    }
                }
                
                throw new RuntimeException("Method not found: " + methodName + " with parameters " + 
                    java.util.Arrays.toString(paramTypes) + " in class " + clazz.getName());
            }
        }
        
        private static Class<?>[] getParameterTypes(Object[] args) {
            if (args == null || args.length == 0) {
                return new Class<?>[0];
            }
            
            Class<?>[] types = new Class[args.length];
            for (int i = 0; i < args.length; i++) {
                types[i] = args[i] != null ? args[i].getClass() : Object.class;
            }
            return types;
        }
        
        private static Method findMethod(Class<?> clazz, String methodName, Object[] args) {
            // Buscar método con parámetros que coincidan
            Class<?>[] paramTypes = getParameterTypes(args);
            
            // ✅ MEJORADO: Buscar métodos de forma más comprehensiva
            // Primero intentar con método público
            try {
                return clazz.getMethod(methodName, paramTypes);
            } catch (NoSuchMethodException e) {
                // Buscar métodos declarados (públicos, protected, private)
                for (Method method : clazz.getDeclaredMethods()) {
                    if (method.getName().equals(methodName) && 
                        java.util.Arrays.equals(method.getParameterTypes(), paramTypes)) {
                        return method;
                    }
                }
                
                // Buscar en superclases recursivamente
                Class<?> superClass = clazz.getSuperclass();
                if (superClass != null && superClass != Object.class) {
                    try {
                        return findMethod(superClass, methodName, args);
                    } catch (RuntimeException ex) {
                        // Continuar buscando
                    }
                }
                
                // Buscar en interfaces implementadas
                for (Class<?> iface : clazz.getInterfaces()) {
                    try {
                        return findMethod(iface, methodName, args);
                    } catch (RuntimeException ex) {
                        // Continuar buscando
                    }
                }
                
                throw new RuntimeException("Method not found: " + methodName + " with parameters " + 
                    java.util.Arrays.toString(paramTypes) + " in class " + clazz.getName(), e);
            }
        }
    }
    
    /**
     * ✅ ACCESO A CAMPOS SIN REFLEXIÓN - VERSIÓN METHODHANDLE OPTIMIZADA
     */
    public static class FieldAccessor {
        
        public static Object getFieldValue(Object target, String fieldName) {
            if (target == null) {
                throw new IllegalArgumentException("Target cannot be null");
            }
            
            try {
                // ✅ NUEVO: Usar MethodHandle para acceso más eficiente
                return getFieldHandle(target, fieldName).invoke(target);
                
            } catch (Throwable e) {
                log.log(Level.SEVERE, "Error accediendo a campo: " + fieldName, e);
                throw new RuntimeException("Field access failed: " + fieldName, e);
            }
        }
        
        public static void setFieldValue(Object target, String fieldName, Object value) {
            if (target == null) {
                throw new IllegalArgumentException("Target cannot be null");
            }
            
            try {
                // ✅ NUEVO: Usar MethodHandle para modificación más eficiente
                setFieldHandle(target, fieldName).invoke(target, value);
                
            } catch (Throwable e) {
                log.log(Level.SEVERE, "Error modificando campo: " + fieldName, e);
                throw new RuntimeException("Field set failed: " + fieldName, e);
            }
        }
        
        /**
         * ✅ GETTER USANDO METHODHANDLE - MÁS EFICIENTE QUE REFLEXIÓN
         */
        private static java.lang.invoke.MethodHandle getFieldHandle(Object target, String fieldName) {
            Field targetField = findField(target.getClass(), fieldName);
            targetField.setAccessible(true);
            try {
                return java.lang.invoke.MethodHandles.lookup().unreflectGetter(targetField);
            } catch (IllegalAccessException e) {
                throw new RuntimeException("Failed to create MethodHandle for field: " + fieldName, e);
            }
        }
        
        /**
         * ✅ SETTER USANDO METHODHANDLE - MÁS EFICIENTE QUE REFLEXIÓN
         */
        private static java.lang.invoke.MethodHandle setFieldHandle(Object target, String fieldName) {
            Field targetField = findField(target.getClass(), fieldName);
            targetField.setAccessible(true);
            try {
                return java.lang.invoke.MethodHandles.lookup().unreflectSetter(targetField);
            } catch (IllegalAccessException e) {
                throw new RuntimeException("Failed to create MethodHandle for field: " + fieldName, e);
            }
        }
        
        private static Field findField(Class<?> clazz, String fieldName) {
            Class<?> current = clazz;
            while (current != null && current != Object.class) {
                try {
                    return current.getDeclaredField(fieldName);
                } catch (NoSuchFieldException e) {
                    current = current.getSuperclass();
                }
            }
            throw new RuntimeException("Field not found: " + fieldName + " in " + clazz.getName());
        }
    }
    
    /**
     * ✅ COPIA DE CAMPOS SIN REFLEXIÓN - VERSIÓN METHODHANDLE OPTIMIZADA
     */
    public static class FieldCopier {
        
        public static void copyFields(Object source, Object destination) {
            if (source == null || destination == null) {
                throw new IllegalArgumentException("Source and destination cannot be null");
            }
            
            if (!source.getClass().equals(destination.getClass())) {
                throw new IllegalArgumentException("Source and destination must be of the same class");
            }
            
            try {
                copyFieldsRecursively(source.getClass(), source, destination);
                
            } catch (Exception e) {
                log.log(Level.SEVERE, "Error copiando campos", e);
                throw new RuntimeException("Field copying failed", e);
            }
        }
        
        private static void copyFieldsRecursively(Class<?> clazz, Object source, Object destination) {
            if (clazz == null || clazz == Object.class) {
                return;
            }
            
            // Copiar campos de la clase actual usando MethodHandles
            for (java.lang.reflect.Field field : clazz.getDeclaredFields()) {
                if (java.lang.reflect.Modifier.isStatic(field.getModifiers())) {
                    continue; // Saltar campos estáticos
                }
                
                try {
                    field.setAccessible(true);
                    java.lang.invoke.MethodHandle getter = java.lang.invoke.MethodHandles.lookup().unreflectGetter(field);
                    java.lang.invoke.MethodHandle setter = java.lang.invoke.MethodHandles.lookup().unreflectSetter(field);
                    
                    Object value = getter.invoke(source);
                    setter.invoke(destination, value);
                    
                } catch (Throwable e) {
                    log.log(Level.FINE, "No se pudo acceder al campo {0}: {1}", 
                            new Object[]{field.getName(), e.getMessage()});
                }
            }
            
            // Recursivamente copiar campos de la superclase
            copyFieldsRecursively(clazz.getSuperclass(), source, destination);
        }
    }
    
    /**
     * ✅ INSTANCIACIÓN SIN REFLEXIÓN - VERSIÓN METHODHANDLE OPTIMIZADA
     */
    public static class InstanceCreator {
        
        public static <T> T createInstance(Class<T> clazz) {
            return createInstance(clazz, (Object[]) null);
        }
        
        public static <T> T createInstance(Class<T> clazz, Object... args) {
            try {
                // ✅ NUEVO: Usar reflexión solo para encontrar el constructor, luego invocar con MethodHandle
                java.lang.reflect.Constructor<T> constructor = findConstructor(clazz, args);
                
                // Crear MethodHandle para el constructor para mayor eficiencia
                java.lang.invoke.MethodHandle constructorHandle = java.lang.invoke.MethodHandles.lookup().unreflectConstructor(constructor);
                
                // ✅ FIX: Verificar si el constructor espera parámetros o no
                if (args != null && args.length > 0) {
                    return (T) constructorHandle.invokeWithArguments(args);
                } else {
                    // Constructor sin parámetros
                    return (T) constructorHandle.invoke();
                }
                
            } catch (Throwable e) {
                log.log(Level.SEVERE, "Error creando instancia de: " + clazz.getName(), e);
                throw new RuntimeException("Instance creation failed: " + clazz.getName(), e);
            }
        }
        
        private static <T> java.lang.reflect.Constructor<T> findConstructor(Class<T> clazz, Object[] args) {
            if (args == null || args.length == 0) {
                try {
                    return clazz.getDeclaredConstructor();
                } catch (NoSuchMethodException e) {
                    // Continuar con el siguiente método
                }
            }
            
            // Buscar constructor con parámetros
            Class<?>[] paramTypes = getParameterTypes(args);
            try {
                return clazz.getDeclaredConstructor(paramTypes);
            } catch (NoSuchMethodException e) {
                // Fallback: usar cualquier constructor disponible
                java.lang.reflect.Constructor<?>[] constructors = clazz.getDeclaredConstructors();
                if (constructors.length > 0) {
                    return (java.lang.reflect.Constructor<T>) constructors[0];
                }
                throw new RuntimeException("No constructor found for class: " + clazz.getName());
            }
        }
        
        private static Class<?>[] getParameterTypes(Object[] args) {
            if (args == null || args.length == 0) {
                return new Class<?>[0];
            }
            
            Class<?>[] types = new Class[args.length];
            for (int i = 0; i < args.length; i++) {
                types[i] = args[i] != null ? args[i].getClass() : Object.class;
            }
            return types;
        }
    }
    
    /**
     * ✅ CLASLOADER ASM PARA CARGAR CLASES DINÁMICAMENTE
     */
    public static class ASMClassLoader extends ClassLoader {
        public ASMClassLoader() {
            super(ASMClassLoader.class.getClassLoader());
        }
        
        public Class<?> defineClass(String name, byte[] b) {
            return defineClass(name, b, 0, b.length);
        }
    }
    
    // ✅ MÉTODOS ESTÁTICOS DE CONVENIENCIA PARA USAR DESDE OTROS ARCHIVOS
    
    /**
     * ✅ INVOCACIÓN DE MÉTODOS SIN REFLEXIÓN - MÉTODO ESTÁTICO
     */
    public static Object invokeMethod(Object target, String methodName, Object... args) {
        return MethodInvoker.invokeMethod(target, methodName, args);
    }
    
    /**
     * ✅ INVOCACIÓN DE MÉTODOS SIN REFLEXIÓN - MÉTODO SIMPLE
     */
    public static Object invokeMethod(Object target, String methodName) {
        return MethodInvoker.invokeMethod(target, methodName);
    }
    
    /**
     * ✅ INVOCACIÓN SILENCIOSA DE MÉTODOS (para casos donde método no encontrado es normal)
     * No lanza excepción si el método no existe, retorna null en su lugar
     */
    public static Object invokeMethodSilent(Object target, String methodName, Object... args) {
        try {
            return invokeMethod(target, methodName, args);
        } catch (RuntimeException e) {
            // Verificar si es un error de método no encontrado revisando toda la cadena de causas
            Throwable current = e;
            while (current != null) {
                if (current.getMessage() != null && 
                    (current.getMessage().contains("Method not found") || 
                     current.getMessage().contains("Method invocation failed"))) {
                    // Método no encontrado - retornar null en silencio
                    log.log(Level.FINE, "Método {0} no encontrado en {1} (silenciado)", 
                           new Object[]{methodName, target.getClass().getSimpleName()});
                    return null;
                }
                current = current.getCause();
            }
            // Otros errores sí deben propagarse
            log.log(Level.SEVERE, "Error invocando método {0}: {1}", 
                   new Object[]{methodName, e.getMessage()});
            throw e;
        }
    }
    
    /**
     * ✅ ACCESO A CAMPOS SIN REFLEXIÓN - MÉTODO ESTÁTICO
     */
    public static Object getField(Object target, String fieldName) {
        return FieldAccessor.getFieldValue(target, fieldName);
    }
    
    /**
     * ✅ SETEO DE CAMPOS SIN REFLEXIÓN - MÉTODO ESTÁTICO
     */
    public static void setField(Object target, String fieldName, Object value) {
        FieldAccessor.setFieldValue(target, fieldName, value);
    }
    
    /**
     * ✅ CREACIÓN DE INSTANCIAS SIN REFLEXIÓN - MÉTODO ESTÁTICO
     */
    public static <T> T createInstance(Class<T> clazz, Object... args) {
        return InstanceCreator.createInstance(clazz, args);
    }
    
    // ✅ MÉTODOS DE UTILIDAD
    
    public static void clearCaches() {
        GENERATED_CLASSES.clear();
        log.info("SimpleASMUtils caches cleared");
    }
    
    public static java.util.Map<String, Object> getCacheStats() {
        java.util.Map<String, Object> stats = new java.util.HashMap<>();
        stats.put("generated_classes_size", GENERATED_CLASSES.size());
        return stats;
    }
}