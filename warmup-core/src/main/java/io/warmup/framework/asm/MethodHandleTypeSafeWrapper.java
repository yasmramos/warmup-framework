package io.warmup.framework.asm;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * 🎯 METHODHANDLE TYPE-SAFE WRAPPER - Optimizado v2.0
 * 
 * Resuelve WrongMethodTypeException mediante:
 * - Type-safe MethodHandle wrapper con validation
 * - Runtime type checking y correction
 * - Cache de MethodHandle con MethodType compatibility
 * - Automatic type adaptation sin reflexión costosa
 * 
 * @author MiniMax Agent
 * @version 2.0
 */
public class MethodHandleTypeSafeWrapper {
    
    private static final Logger log = Logger.getLogger(MethodHandleTypeSafeWrapper.class.getName());
    
    // ⚡ Cache de MethodHandles type-safe (O(1) lookup)
    private static final ConcurrentHashMap<String, TypeSafeMethodHandle> typeSafeCache = 
        new ConcurrentHashMap<>(32, 0.75f, 4);
    
    /**
     * ⚡ Type-safe MethodHandle wrapper
     */
    private static class TypeSafeMethodHandle {
        private final MethodHandle methodHandle;
        private final MethodType expectedType;
        private final String cacheKey;
        
        public TypeSafeMethodHandle(MethodHandle methodHandle, MethodType expectedType, String cacheKey) {
            this.methodHandle = methodHandle;
            this.expectedType = expectedType;
            this.cacheKey = cacheKey;
        }
        
        /**
         * ⚡ Invoke con type safety guarantee
         */
        public Object invoke(Object target, Object... args) throws Throwable {
            try {
                // Validate argument types
                validateArgumentTypes(args);
                
                // Apply type adaptations si necesario
                Object[] adaptedArgs = adaptArguments(args);
                
                // Invoke con type-safe MethodHandle
                // CORRECCIÓN: MethodHandle es UNBOUND, necesita target + argumentos
                Object[] allArgs = new Object[adaptedArgs.length + 1];
                allArgs[0] = target;
                System.arraycopy(adaptedArgs, 0, allArgs, 1, adaptedArgs.length);
                return methodHandle.invokeWithArguments(allArgs);
                
            } catch (IllegalArgumentException e) {
                // Type mismatch - attempt recovery
                log.log(Level.FINE, "⚠️ Type mismatch en MethodHandle {0}: {1}", 
                        new Object[]{cacheKey, e.getMessage()});
                
                return invokeWithTypeRecovery(target, args);
                
            } catch (Exception e) {
                log.log(Level.WARNING, "⚠️ MethodHandle invocation failed para {0}: {1}", 
                        new Object[]{cacheKey, e.getMessage()});
                throw e;
            }
        }
        
        /**
         * ⚡ Validate argument types compatibility
         */
        private void validateArgumentTypes(Object[] args) {
            Class<?>[] parameterTypes = expectedType.parameterArray();
            
            if (args.length != parameterTypes.length - 1) { // -1 for target object
                throw new IllegalArgumentException("Argument count mismatch: expected " + 
                    (parameterTypes.length - 1) + ", got " + args.length);
            }
            
            for (int i = 0; i < args.length; i++) {
                Class<?> expectedType = parameterTypes[i + 1]; // +1 para skip target
                Class<?> actualType = args[i] != null ? args[i].getClass() : Object.class;
                
                if (!isCompatibleType(expectedType, actualType)) {
                    throw new IllegalArgumentException("Type mismatch: expected " + 
                        expectedType.getName() + ", got " + actualType.getName());
                }
            }
        }
        
        /**
         * ⚡ Adapt arguments to match expected types
         */
        private Object[] adaptArguments(Object[] args) {
            Class<?>[] parameterTypes = expectedType.parameterArray();
            
            // CORRECCIÓN: Los args YA son los argumentos del método, no incluir target
            // El target se maneja por separado en la invocación del MethodHandle
            Object[] adapted = new Object[args.length];
            
            // Adapt arguments - parameterTypes incluye solo los parámetros del método (no el target)
            for (int i = 0; i < args.length; i++) {
                // parameterTypes[i] corresponde directamente a args[i] 
                // (no parameterTypes[i+1] como estaba antes)
                Class<?> expectedType = parameterTypes[i];
                adapted[i] = adaptType(args[i], expectedType);
            }
            
            return adapted;
        }
        
        /**
         * 🔧 Type compatibility check
         */
        private boolean isCompatibleType(Class<?> expected, Class<?> actual) {
            // Exact match
            if (expected.equals(actual)) {
                return true;
            }
            
            // Primitive boxing compatibility
            if (isBoxingCompatible(expected, actual)) {
                return true;
            }
            
            // Inheritance compatibility
            return expected.isAssignableFrom(actual);
        }
        
        /**
         * 🔧 Box primitive types compatibility
         */
        private boolean isBoxingCompatible(Class<?> expected, Class<?> actual) {
            return (expected == Integer.class && actual == int.class) ||
                   (expected == Long.class && actual == long.class) ||
                   (expected == Double.class && actual == double.class) ||
                   (expected == Float.class && actual == float.class) ||
                   (expected == Boolean.class && actual == boolean.class) ||
                   (expected == Byte.class && actual == byte.class) ||
                   (expected == Character.class && actual == char.class) ||
                   (expected == Short.class && actual == short.class);
        }
        
        /**
         * 🔧 Type adaptation
         */
        private Object adaptType(Object value, Class<?> targetType) {
            if (value == null) {
                return null;
            }
            
            Class<?> sourceType = value.getClass();
            
            // If already compatible, return as-is
            if (targetType.isAssignableFrom(sourceType)) {
                return value;
            }
            
            // Primitive boxing
            if (isBoxingCompatible(targetType, sourceType)) {
                return value;
            }
            
            // String to primitive
            if (sourceType == String.class && isPrimitiveType(targetType)) {
                return convertStringToPrimitive((String) value, targetType);
            }
            
            // For now, return original value (caller will handle type errors)
            return value;
        }
        
        /**
         * 🔧 Check if primitive type
         */
        private boolean isPrimitiveType(Class<?> type) {
            return type.isPrimitive() || 
                   type == Integer.class || type == Long.class || 
                   type == Double.class || type == Float.class ||
                   type == Boolean.class || type == Byte.class ||
                   type == Character.class || type == Short.class;
        }
        
        /**
         * 🔧 Convert string to primitive con conversion más robusta
         */
        private Object convertStringToPrimitive(String value, Class<?> targetType) {
            if (value == null) {
                return null;
            }
            
            try {
                if (targetType == int.class || targetType == Integer.class) {
                    return Integer.valueOf(value.trim());
                } else if (targetType == long.class || targetType == Long.class) {
                    return Long.valueOf(value.trim());
                } else if (targetType == double.class || targetType == Double.class) {
                    return Double.valueOf(value.trim());
                } else if (targetType == float.class || targetType == Float.class) {
                    return Float.valueOf(value.trim());
                } else if (targetType == boolean.class || targetType == Boolean.class) {
                    return Boolean.valueOf(value.trim().toLowerCase());
                } else if (targetType == byte.class || targetType == Byte.class) {
                    return Byte.valueOf(value.trim());
                } else if (targetType == char.class || targetType == Character.class) {
                    String trimmed = value.trim();
                    if (trimmed.length() == 1) {
                        return trimmed.charAt(0);
                    }
                    // For multiple characters, return first character
                    return trimmed.charAt(0);
                } else if (targetType == short.class || targetType == Short.class) {
                    return Short.valueOf(value.trim());
                }
            } catch (NumberFormatException e) {
                log.log(Level.FINE, "⚠️ Failed to convert string '{0}' to {1}: {2}", 
                        new Object[]{value, targetType.getSimpleName(), e.getMessage()});
            } catch (Exception e) {
                log.log(Level.FINE, "⚠️ Unexpected error converting string '{0}' to {1}: {2}", 
                        new Object[]{value, targetType.getSimpleName(), e.getMessage()});
            }
            
            return null;
        }
        
        /**
         * 🔧 Recovery con type adaptation
         */
        private Object invokeWithTypeRecovery(Object target, Object[] args) throws Throwable {
            try {
                // Attempt to adapt MethodHandle types
                MethodHandle adaptedHandle = adaptMethodHandleTypes(target.getClass(), args);
                
                Object[] adaptedArgs = new Object[args.length + 1];
                adaptedArgs[0] = target;
                System.arraycopy(args, 0, adaptedArgs, 1, args.length);
                
                return adaptedHandle.invokeWithArguments(adaptedArgs);
                
            } catch (Exception e) {
                log.log(Level.WARNING, "⚠️ MethodHandle recovery failed para {0}: {1}", 
                        new Object[]{cacheKey, e.getMessage()});
                throw e;
            }
        }
        
        /**
         * 🔧 Adapt MethodHandle types
         */
        private MethodHandle adaptMethodHandleTypes(Class<?> targetClass, Object[] args) throws Throwable {
            MethodType originalType = methodHandle.type();
            
            // Build new parameter types
            Class<?>[] newParameterTypes = new Class[args.length + 1];
            newParameterTypes[0] = targetClass;
            
            for (int i = 0; i < args.length; i++) {
                newParameterTypes[i + 1] = args[i] != null ? args[i].getClass() : Object.class;
            }
            
            MethodType newType = MethodType.methodType(originalType.returnType(), newParameterTypes);
            
            return methodHandle.asType(newType);
        }
    }
    
    /**
     * 🎯 Type-safe MethodHandle invocation
     */
    public static Object invokeMethodHandleTypeSafe(
            MethodHandle methodHandle, 
            String cacheKey, 
            Object target, 
            Object... args) throws Throwable {
        
        // Create or get cached TypeSafeMethodHandle
        TypeSafeMethodHandle typeSafeHandle = typeSafeCache.computeIfAbsent(cacheKey, key -> 
            createTypeSafeMethodHandle(methodHandle, key));
        
        try {
            return typeSafeHandle.invoke(target, args);
        } catch (Throwable t) {
            log.log(Level.FINEST, "⚠️ Type-safe MethodHandle invocation failed para {0}", cacheKey);
            throw t;
        }
    }
    
    /**
     * 🎯 Type-safe MethodHandle invocation para constructor
     */
    public static Object invokeConstructorTypeSafe(
            MethodHandle methodHandle, 
            String cacheKey, 
            Object... args) throws Throwable {
        
        TypeSafeMethodHandle typeSafeHandle = typeSafeCache.computeIfAbsent(cacheKey, key -> 
            createTypeSafeMethodHandle(methodHandle, key));
        
        try {
            return typeSafeHandle.invoke(null, args); // null for constructor (no target)
        } catch (Throwable t) {
            log.log(Level.FINEST, "⚠️ Type-safe constructor invocation failed para {0}", cacheKey);
            throw t;
        }
    }
    
    /**
     * 🎯 Type-safe MethodHandle invocation para static method
     */
    public static Object invokeStaticTypeSafe(
            MethodHandle methodHandle, 
            String cacheKey, 
            Class<?> targetClass,
            Object... args) throws Throwable {
        
        TypeSafeMethodHandle typeSafeHandle = typeSafeCache.computeIfAbsent(cacheKey, key -> 
            createTypeSafeMethodHandle(methodHandle, key));
        
        try {
            Object[] allArgs = new Object[args.length + 1];
            allArgs[0] = targetClass; // Class for static method
            System.arraycopy(args, 0, allArgs, 1, args.length);
            
            return typeSafeHandle.invoke(null, allArgs);
        } catch (Throwable t) {
            log.log(Level.FINEST, "⚠️ Type-safe static invocation failed para {0}", cacheKey);
            throw t;
        }
    }
    
    /**
     * 🔧 Create TypeSafeMethodHandle
     */
    private static TypeSafeMethodHandle createTypeSafeMethodHandle(MethodHandle methodHandle, String cacheKey) {
        MethodType expectedType = methodHandle.type();
        
        return new TypeSafeMethodHandle(methodHandle, expectedType, cacheKey);
    }
    
    /**
     * 🧹 Clear type-safe cache
     */
    public static void clearCache() {
        typeSafeCache.clear();
    }
    
    /**
     * 🧹 Get cache statistics
     */
    public static int getCacheSize() {
        return typeSafeCache.size();
    }
}