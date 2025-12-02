package io.warmup.framework.validation.optimization;

import io.warmup.framework.asm.AsmCoreUtils;
import io.warmup.framework.validation.cache.ValidationCache;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

/**
 * Optimized reflection utilities for improved validation performance.
 * Uses caching and bytecode manipulation hints to reduce reflection overhead.
 * 
 * @author MiniMax Agent
 * @since 1.0
 */
public class OptimizedReflectionUtil {
    
    private static final Logger logger = Logger.getLogger(OptimizedReflectionUtil.class.getName());
    
    private final ValidationCache cache;
    
    // Cache for field accessors
    private final ConcurrentHashMap<String, FieldAccessor> fieldAccessorCache;
    
    // Cache for method accessors
    private final ConcurrentHashMap<String, MethodAccessor> methodAccessorCache;
    
    public OptimizedReflectionUtil(ValidationCache cache) {
        this.cache = cache;
        this.fieldAccessorCache = new ConcurrentHashMap<>();
        this.methodAccessorCache = new ConcurrentHashMap<>();
    }
    
    /**
     * Get optimized field accessor with caching.
     * 
     * @param clazz the class containing the field
     * @param fieldName the field name
     * @return optimized field accessor
     */
    public FieldAccessor getFieldAccessor(Class<?> clazz, String fieldName) {
        String accessorKey = clazz.getName() + "." + fieldName;
        
        FieldAccessor cached = fieldAccessorCache.get(accessorKey);
        if (cached != null) {
            return cached;
        }
        
        // Create new field accessor
        FieldAccessor newAccessor = createFieldAccessor(clazz, fieldName);
        
        // Cache it
        FieldAccessor existing = fieldAccessorCache.putIfAbsent(accessorKey, newAccessor);
        return existing != null ? existing : newAccessor;
    }
    
    /**
     * Create optimized field accessor.
     */
    private FieldAccessor createFieldAccessor(Class<?> clazz, String fieldName) {
        try {
            Field field = findField(clazz, fieldName);
            if (field == null) {
                return new NullFieldAccessor(fieldName);
            }
            
            field.setAccessible(true);
            
            // Check cache for field metadata
            String cacheKey = ValidationCache.generateFieldKey(clazz.getName(), fieldName);
            ValidationCache.FieldMetadata metadata = cache.getCachedFieldMetadata(cacheKey);
            
            if (metadata == null) {
                // Cache field metadata
                metadata = new ValidationCache.FieldMetadata(
                    field.getType(), 
                    field.isAccessible(), 
                    field.getName()
                );
                cache.cacheFieldMetadata(cacheKey, metadata);
            }
            
            // Choose the most efficient accessor based on field characteristics
            if (field.getType().isPrimitive()) {
                return createPrimitiveFieldAccessor(field);
            } else if (isSimpleType(field.getType())) {
                return createSimpleFieldAccessor(field);
            } else {
                return createObjectFieldAccessor(field);
            }
            
        } catch (Exception e) {
            logger.warning("Error creating field accessor for " + clazz.getName() + "." + fieldName + ": " + e.getMessage());
            return new NullFieldAccessor(fieldName);
        }
    }
    
    /**
     * Find field in class hierarchy with caching.
     */
    private Field findField(Class<?> clazz, String fieldName) {
        String cacheKey = "field." + clazz.getName() + "." + fieldName;
        
        @SuppressWarnings("unchecked")
        Field cached = (Field) cache.getCachedPattern(cacheKey, Field.class);
        if (cached != null) {
            return cached;
        }
        
        // Search in class hierarchy
        Class<?> currentClass = clazz;
        while (currentClass != null && currentClass != Object.class) {
            try {
                Field field = currentClass.getDeclaredField(fieldName);
                cache.cacheCompiledPattern(cacheKey, field);
                return field;
            } catch (NoSuchFieldException e) {
                currentClass = currentClass.getSuperclass();
            }
        }
        
        return null;
    }
    
    /**
     * Create primitive field accessor.
     */
    private FieldAccessor createPrimitiveFieldAccessor(Field field) {
        Class<?> fieldType = field.getType();
        
        if (fieldType == int.class) {
            return (target) -> safeGetInt(field, target);
        } else if (fieldType == long.class) {
            return (target) -> safeGetLong(field, target);
        } else if (fieldType == double.class) {
            return (target) -> safeGetDouble(field, target);
        } else if (fieldType == float.class) {
            return (target) -> safeGetFloat(field, target);
        } else if (fieldType == boolean.class) {
            return (target) -> safeGetBoolean(field, target);
        } else if (fieldType == byte.class) {
            return (target) -> safeGetByte(field, target);
        } else if (fieldType == char.class) {
            return (target) -> safeGetChar(field, target);
        } else if (fieldType == short.class) {
            return (target) -> safeGetShort(field, target);
        }
        
        // Fallback to object accessor
        return createObjectFieldAccessor(field);
    }
    
    // Helper methods to safely get primitive values
    private int safeGetInt(Field field, Object target) {
        try {
            return field.getInt(target);
        } catch (IllegalAccessException e) {
            throw new RuntimeException("Failed to access field: " + field.getName(), e);
        }
    }
    
    private long safeGetLong(Field field, Object target) {
        try {
            return field.getLong(target);
        } catch (IllegalAccessException e) {
            throw new RuntimeException("Failed to access field: " + field.getName(), e);
        }
    }
    
    private double safeGetDouble(Field field, Object target) {
        try {
            return field.getDouble(target);
        } catch (IllegalAccessException e) {
            throw new RuntimeException("Failed to access field: " + field.getName(), e);
        }
    }
    
    private float safeGetFloat(Field field, Object target) {
        try {
            return field.getFloat(target);
        } catch (IllegalAccessException e) {
            throw new RuntimeException("Failed to access field: " + field.getName(), e);
        }
    }
    
    private boolean safeGetBoolean(Field field, Object target) {
        try {
            return field.getBoolean(target);
        } catch (IllegalAccessException e) {
            throw new RuntimeException("Failed to access field: " + field.getName(), e);
        }
    }
    
    private byte safeGetByte(Field field, Object target) {
        try {
            return field.getByte(target);
        } catch (IllegalAccessException e) {
            throw new RuntimeException("Failed to access field: " + field.getName(), e);
        }
    }
    
    private char safeGetChar(Field field, Object target) {
        try {
            return field.getChar(target);
        } catch (IllegalAccessException e) {
            throw new RuntimeException("Failed to access field: " + field.getName(), e);
        }
    }
    
    private short safeGetShort(Field field, Object target) {
        try {
            return field.getShort(target);
        } catch (IllegalAccessException e) {
            throw new RuntimeException("Failed to access field: " + field.getName(), e);
        }
    }
    
    /**
     * Create simple field accessor for common types.
     */
    private FieldAccessor createSimpleFieldAccessor(Field field) {
        Class<?> fieldType = field.getType();
        
        if (fieldType == String.class) {
            return (target) -> safeGet(field, target);
        } else if (Number.class.isAssignableFrom(fieldType)) {
            return (target) -> safeGet(field, target);
        } else {
            return createObjectFieldAccessor(field);
        }
    }
    
    // Helper method to safely get object field values
    private Object safeGet(Field field, Object target) {
        try {
            return field.get(target);
        } catch (IllegalAccessException e) {
            throw new RuntimeException("Failed to access field: " + field.getName(), e);
        }
    }
    
    /**
     * Create object field accessor.
     */
    private FieldAccessor createObjectFieldAccessor(Field field) {
        return (target) -> {
            try {
                return field.get(target);
            } catch (IllegalAccessException e) {
                logger.warning("Error accessing field " + field.getName() + ": " + e.getMessage());
                return null;
            }
        };
    }
    
    /**
     * Check if type is simple (doesn't require complex validation).
     */
    private boolean isSimpleType(Class<?> clazz) {
            return clazz.isPrimitive() ||
               clazz.equals(String.class) ||
               Number.class.isAssignableFrom(clazz) ||
               clazz.equals(Boolean.class) ||
               clazz.equals(Character.class) ||
               clazz.equals(Class.class);
    }
    
    /**
     * Get optimized method accessor with caching.
     */
    public MethodAccessor getMethodAccessor(Class<?> clazz, String methodName, Class<?>... parameterTypes) {
        String accessorKey = clazz.getName() + "." + methodName + "(" + 
                            Arrays.toString(parameterTypes) + ")";
        
        MethodAccessor cached = methodAccessorCache.get(accessorKey);
        if (cached != null) {
            return cached;
        }
        
        Method method = findMethod(clazz, methodName, parameterTypes);
        if (method != null) {
            method.setAccessible(true);
            MethodAccessor accessor = new ReflectiveMethodAccessor(method);
            methodAccessorCache.put(accessorKey, accessor);
            return accessor;
        }
        
        return new NullMethodAccessor(methodName);
    }
    
    /**
     * Find method in class hierarchy.
     */
    private Method findMethod(Class<?> clazz, String methodName, Class<?>... parameterTypes) {
        try {
            return clazz.getDeclaredMethod(methodName, parameterTypes);
        } catch (NoSuchMethodException e) {
            // Search in superclasses
                Class<?> superclass = clazz.getSuperclass();
            if (superclass != null) {
                return findMethod(superclass, methodName, parameterTypes);
            }
        }
        return null;
    }
    
    /**
     * Get all fields from class hierarchy with caching.
     */
    public Field[] getAllFields(Class<?> clazz) {
        String cacheKey = "allFields." + clazz.getName();
        
        @SuppressWarnings("unchecked")
        Field[] cached = (Field[]) cache.getCachedPattern(cacheKey, Field[].class);
        if (cached != null) {
            return cached;
        }
        
        // Collect fields from class hierarchy
        java.util.List<Field> allFields = new java.util.ArrayList<>();
        Class<?> currentClass = clazz;
        
        while (currentClass != null && currentClass != Object.class) {
            Field[] declaredFields = currentClass.getDeclaredFields();
            for (Field field : declaredFields) {
                if (!allFields.stream().anyMatch(f -> f.getName().equals(field.getName()))) {
                    allFields.add(field);
                }
            }
            currentClass = currentClass.getSuperclass();
        }
        
        Field[] result = allFields.toArray(new Field[0]);
        cache.cacheCompiledPattern(cacheKey, result);
        return result;
    }
    
    /**
     * Clear all caches.
     */
    public void clearCaches() {
        fieldAccessorCache.clear();
        methodAccessorCache.clear();
        logger.fine("Reflection accessor caches cleared");
    }
    
    /**
     * Get cache statistics.
     */
    public AccessorCacheStatistics getStatistics() {
        return new AccessorCacheStatistics(
            fieldAccessorCache.size(),
            methodAccessorCache.size()
        );
    }
    
    /**
     * Field accessor interface for optimized field access.
     */
    @FunctionalInterface
    public interface FieldAccessor {
        Object get(Object target);
        default String getFieldName() {
            return "";
        }
    }
    
    /**
     * Method accessor interface for optimized method access.
     */
    @FunctionalInterface
    public interface MethodAccessor {
        Object invoke(Object target, Object... args) throws Exception;
        default String getMethodName() {
            return "";
        }
    }
    
    /**
     * Reflective field accessor implementation.
     */
    private static class ReflectiveFieldAccessor implements FieldAccessor {
        private final Field field;
        
        public ReflectiveFieldAccessor(Field field) {
            this.field = field;
        }
        
        @Override
        public Object get(Object target) {
            try {
                return field.get(target);
            } catch (IllegalAccessException e) {
                throw new RuntimeException("Error accessing field " + field.getName(), e);
            }
        }
        
        @Override
        public String getFieldName() {
            return field.getName();
        }
    }
    
    /**
     * Optimized primitive field accessor.
     */
    private static class OptimizedPrimitiveFieldAccessor implements FieldAccessor {
        private final Field field;
        
        public OptimizedPrimitiveFieldAccessor(Field field) {
            this.field = field;
        }
        
        @Override
        public Object get(Object target) {
            try {
                return field.get(target);
            } catch (IllegalAccessException e) {
                throw new RuntimeException("Error accessing primitive field " + field.getName(), e);
            }
        }
        
        @Override
        public String getFieldName() {
            return field.getName();
        }
    }
    
    /**
     * Reflective method accessor implementation.
     */
    private static class ReflectiveMethodAccessor implements MethodAccessor {
        private final Method method;
        
        public ReflectiveMethodAccessor(Method method) {
            this.method = method;
        }
        
        @Override
        public Object invoke(Object target, Object... args) throws Exception {
            return method.invoke(target, args);
        }
        
        @Override
        public String getMethodName() {
            return method.getName();
        }
    }
    
    /**
     * Null field accessor for missing fields.
     */
    private static class NullFieldAccessor implements FieldAccessor {
        private final String fieldName;
        
        public NullFieldAccessor(String fieldName) {
            this.fieldName = fieldName;
        }
        
        @Override
        public Object get(Object target) {
            return null;
        }
        
        @Override
        public String getFieldName() {
            return fieldName;
        }
    }
    
    /**
     * Null method accessor for missing methods.
     */
    private static class NullMethodAccessor implements MethodAccessor {
        private final String methodName;
        
        public NullMethodAccessor(String methodName) {
            this.methodName = methodName;
        }
        
        @Override
        public Object invoke(Object target, Object... args) {
            throw new UnsupportedOperationException("Method " + methodName + " not found");
        }
        
        @Override
        public String getMethodName() {
            return methodName;
        }
    }
    
    /**
     * Accessor cache statistics.
     */
    public static class AccessorCacheStatistics {
        private final int fieldAccessorCount;
        private final int methodAccessorCount;
        
        public AccessorCacheStatistics(int fieldAccessorCount, int methodAccessorCount) {
            this.fieldAccessorCount = fieldAccessorCount;
            this.methodAccessorCount = methodAccessorCount;
        }
        
        public int getFieldAccessorCount() { return fieldAccessorCount; }
        public int getMethodAccessorCount() { return methodAccessorCount; }
        
        @Override
        public String toString() {
            return String.format("AccessorStats{fieldAccessors=%d, methodAccessors=%d}", 
                               fieldAccessorCount, methodAccessorCount);
        }
    }
}