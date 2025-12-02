package io.warmup.framework.cache;

import java.io.Serializable;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Cache especializado para operaciones de reflexión en Java.
 * Optimiza el rendimiento del framework Warmup evitando recalcular 
 * metadatos de reflexión costosos.
 * 
 * @author MiniMax Agent
 * @since 2.0
 */
public class ReflectionCache implements Serializable {
    
    private static final Logger log = Logger.getLogger(ReflectionCache.class.getName());
    private static final long serialVersionUID = 1L;
    
    // Cache para diferentes tipos de metadatos
    private final ConcurrentMap<String, Method[]> methodsCache;
    private final ConcurrentMap<String, Constructor<?>[]> constructorsCache;
    private final ConcurrentMap<String, Field[]> fieldsCache;
    private final ConcurrentMap<String, Class<?>[]> declaredClassesCache;
    private final ConcurrentMap<String, Boolean> classExistsCache;
    
    // Estadísticas
    private long methodHits = 0;
    private long methodMisses = 0;
    private long constructorHits = 0;
    private long constructorMisses = 0;
    private long fieldHits = 0;
    private long fieldMisses = 0;
    private long classHits = 0;
    private long classMisses = 0;
    
    private static ReflectionCache instance;
    
    /**
     * Constructor privado para patrón Singleton
     */
    private ReflectionCache() {
        this.methodsCache = new ConcurrentHashMap<>();
        this.constructorsCache = new ConcurrentHashMap<>();
        this.fieldsCache = new ConcurrentHashMap<>();
        this.declaredClassesCache = new ConcurrentHashMap<>();
        this.classExistsCache = new ConcurrentHashMap<>();
        log.log(Level.INFO, "ReflectionCache inicializado");
    }
    
    /**
     * Obtiene la instancia singleton del ReflectionCache
     */
    public static synchronized ReflectionCache getInstance() {
        if (instance == null) {
            instance = new ReflectionCache();
        }
        return instance;
    }
    
    /**
     * Cachea métodos de una clase
     */
    public Method[] getMethods(Class<?> clazz) {
        if (clazz == null) {
            return new Method[0];
        }
        
        String key = clazz.getName();
        Method[] methods = methodsCache.get(key);
        
        if (methods == null) {
            try {
                methods = clazz.getDeclaredMethods();
                Method[] existing = methodsCache.putIfAbsent(key, methods);
                if (existing != null) {
                    methods = existing; // Another thread beat us to it
                }
                methodMisses++;
            } catch (Exception e) {
                log.log(Level.WARNING, "Error cacheando métodos para {0}: {1}", 
                        new Object[]{clazz.getName(), e.getMessage()});
                return new Method[0];
            }
        } else {
            methodHits++;
        }
        
        return methods;
    }
    
    /**
     * Cachea constructores de una clase
     */
    public Constructor<?>[] getConstructors(Class<?> clazz) {
        if (clazz == null) {
            return new Constructor[0];
        }
        
        String key = clazz.getName();
        Constructor<?>[] constructors = constructorsCache.get(key);
        
        if (constructors == null) {
            try {
                constructors = clazz.getDeclaredConstructors();
                Constructor<?>[] existing = constructorsCache.putIfAbsent(key, constructors);
                if (existing != null) {
                    constructors = existing; // Another thread beat us to it
                }
                constructorMisses++;
            } catch (Exception e) {
                log.log(Level.WARNING, "Error cacheando constructores para {0}: {1}", 
                        new Object[]{clazz.getName(), e.getMessage()});
                return new Constructor[0];
            }
        } else {
            constructorHits++;
        }
        
        return constructors;
    }
    
    /**
     * Cachea campos de una clase
     */
    public Field[] getFields(Class<?> clazz) {
        if (clazz == null) {
            return new Field[0];
        }
        
        String key = clazz.getName();
        Field[] fields = fieldsCache.get(key);
        
        if (fields == null) {
            try {
                fields = clazz.getDeclaredFields();
                Field[] existing = fieldsCache.putIfAbsent(key, fields);
                if (existing != null) {
                    fields = existing; // Another thread beat us to it
                }
                fieldMisses++;
            } catch (Exception e) {
                log.log(Level.WARNING, "Error cacheando campos para {0}: {1}", 
                        new Object[]{clazz.getName(), e.getMessage()});
                return new Field[0];
            }
        } else {
            fieldHits++;
        }
        
        return fields;
    }
    
    /**
     * Cachea clases declaradas de una clase
     */
    public Class<?>[] getDeclaredClasses(Class<?> clazz) {
        if (clazz == null) {
            return new Class[0];
        }
        
        String key = clazz.getName();
        Class<?>[] declaredClasses = declaredClassesCache.get(key);
        
        if (declaredClasses == null) {
            try {
                declaredClasses = clazz.getDeclaredClasses();
                Class<?>[] existing = declaredClassesCache.putIfAbsent(key, declaredClasses);
                if (existing != null) {
                    declaredClasses = existing; // Another thread beat us to it
                }
                classMisses++;
            } catch (Exception e) {
                log.log(Level.WARNING, "Error cacheando clases declaradas para {0}: {1}", 
                        new Object[]{clazz.getName(), e.getMessage()});
                return new Class[0];
            }
        } else {
            classHits++;
        }
        
        return declaredClasses;
    }
    
    /**
     * Verifica si una clase existe (cachea el resultado)
     */
    public boolean classExists(String className) {
        if (className == null || className.trim().isEmpty()) {
            return false;
        }
        
        Boolean exists = classExistsCache.get(className);
        
        if (exists == null) {
            try {
                Class.forName(className);
                exists = Boolean.TRUE;
            } catch (ClassNotFoundException e) {
                exists = Boolean.FALSE;
            } catch (NoClassDefFoundError e) {
                exists = Boolean.FALSE;
            }
            
            Boolean existing = classExistsCache.putIfAbsent(className, exists);
            if (existing != null) {
                exists = existing; // Another thread beat us to it
            }
            classMisses++;
        } else {
            classHits++;
        }
        
        return exists;
    }
    
    /**
     * Encuentra un método por nombre y parámetros
     */
    public Method findMethod(Class<?> clazz, String methodName, Class<?>... parameterTypes) {
        if (clazz == null || methodName == null) {
            return null;
        }
        
        Method[] methods = getMethods(clazz);
        
        for (Method method : methods) {
            if (method.getName().equals(methodName)) {
                Class<?>[] params = method.getParameterTypes();
                if (params.length == parameterTypes.length) {
                    boolean match = true;
                    for (int i = 0; i < params.length; i++) {
                        if (params[i] != parameterTypes[i]) {
                            match = false;
                            break;
                        }
                    }
                    if (match) {
                        return method;
                    }
                }
            }
        }
        
        return null;
    }
    
    /**
     * Encuentra un constructor por parámetros
     */
    public Constructor<?> findConstructor(Class<?> clazz, Class<?>... parameterTypes) {
        if (clazz == null) {
            return null;
        }
        
        Constructor<?>[] constructors = getConstructors(clazz);
        
        for (Constructor<?> constructor : constructors) {
            Class<?>[] params = constructor.getParameterTypes();
            if (params.length == parameterTypes.length) {
                boolean match = true;
                for (int i = 0; i < params.length; i++) {
                    if (params[i] != parameterTypes[i]) {
                        match = false;
                        break;
                    }
                }
                if (match) {
                    return constructor;
                }
            }
        }
        
        return null;
    }
    
    /**
     * Encuentra un campo por nombre
     */
    public Field findField(Class<?> clazz, String fieldName) {
        if (clazz == null || fieldName == null) {
            return null;
        }
        
        Field[] fields = getFields(clazz);
        
        for (Field field : fields) {
            if (field.getName().equals(fieldName)) {
                return field;
            }
        }
        
        return null;
    }
    
    /**
     * Limpia todas las entradas del cache
     */
    public void clearAll() {
        int methodCount = methodsCache.size();
        int constructorCount = constructorsCache.size();
        int fieldCount = fieldsCache.size();
        int classCount = declaredClassesCache.size();
        int existsCount = classExistsCache.size();
        
        methodsCache.clear();
        constructorsCache.clear();
        fieldsCache.clear();
        declaredClassesCache.clear();
        classExistsCache.clear();
        
        log.log(Level.INFO, "ReflectionCache limpiado. Entradas eliminadas: métodos={0}, constructores={1}, campos={2}, clases={3}, exists={4}", 
                new Object[]{methodCount, constructorCount, fieldCount, classCount, existsCount});
    }
    
    /**
     * Limpia el cache para una clase específica
     */
    public void clearClass(Class<?> clazz) {
        if (clazz == null) {
            return;
        }
        
        String className = clazz.getName();
        methodsCache.remove(className);
        constructorsCache.remove(className);
        fieldsCache.remove(className);
        declaredClassesCache.remove(className);
        classExistsCache.remove(className);
        
        log.log(Level.FINE, "Cache limpiado para clase: {0}", className);
    }
    
    /**
     * Obtiene las estadísticas del cache
     */
    public ReflectionCacheStats getStats() {
        long totalMethod = methodHits + methodMisses;
        long totalConstructor = constructorHits + constructorMisses;
        long totalField = fieldHits + fieldMisses;
        long totalClass = classHits + classMisses;
        
        double methodHitRate = totalMethod > 0 ? (double) methodHits / totalMethod * 100 : 0;
        double constructorHitRate = totalConstructor > 0 ? (double) constructorHits / totalConstructor * 100 : 0;
        double fieldHitRate = totalField > 0 ? (double) fieldHits / totalField * 100 : 0;
        double classHitRate = totalClass > 0 ? (double) classHits / totalClass * 100 : 0;
        
        return new ReflectionCacheStats(
            methodsCache.size(), methodHits, methodMisses, (long) methodHitRate,
            constructorsCache.size(), constructorHits, constructorMisses, (long) constructorHitRate,
            fieldsCache.size(), fieldHits, fieldMisses, (long) fieldHitRate,
            declaredClassesCache.size(), classHits, classMisses, (long) classHitRate
        );
    }
    
    /**
     * Imprime las estadísticas en el log
     */
    public void printStats() {
        ReflectionCacheStats stats = getStats();
        log.log(Level.INFO, stats.toString());
    }
    
    /**
     * Clase para representar estadísticas del cache
     */
    public static class ReflectionCacheStats implements Serializable {
        private static final long serialVersionUID = 1L;
        
        private final long methodEntries, methodHits, methodMisses, methodHitRate;
        private final long constructorEntries, constructorHits, constructorMisses, constructorHitRate;
        private final long fieldEntries, fieldHits, fieldMisses, fieldHitRate;
        private final long classEntries, classHits, classMisses, classHitRate;
        
        public ReflectionCacheStats(long methodEntries, long methodHits, long methodMisses, long methodHitRate,
                                   long constructorEntries, long constructorHits, long constructorMisses, long constructorHitRate,
                                   long fieldEntries, long fieldHits, long fieldMisses, long fieldHitRate,
                                   long classEntries, long classHits, long classMisses, long classHitRate) {
            this.methodEntries = methodEntries;
            this.methodHits = methodHits;
            this.methodMisses = methodMisses;
            this.methodHitRate = methodHitRate;
            this.constructorEntries = constructorEntries;
            this.constructorHits = constructorHits;
            this.constructorMisses = constructorMisses;
            this.constructorHitRate = constructorHitRate;
            this.fieldEntries = fieldEntries;
            this.fieldHits = fieldHits;
            this.fieldMisses = fieldMisses;
            this.fieldHitRate = fieldHitRate;
            this.classEntries = classEntries;
            this.classHits = classHits;
            this.classMisses = classMisses;
            this.classHitRate = classHitRate;
        }
        
        // Getters
        public long getMethodEntries() { return methodEntries; }
        public long getMethodHits() { return methodHits; }
        public long getMethodMisses() { return methodMisses; }
        public long getMethodHitRate() { return methodHitRate; }
        public long getConstructorEntries() { return constructorEntries; }
        public long getConstructorHits() { return constructorHits; }
        public long getConstructorMisses() { return constructorMisses; }
        public long getConstructorHitRate() { return constructorHitRate; }
        public long getFieldEntries() { return fieldEntries; }
        public long getFieldHits() { return fieldHits; }
        public long getFieldMisses() { return fieldMisses; }
        public long getFieldHitRate() { return fieldHitRate; }
        public long getClassEntries() { return classEntries; }
        public long getClassHits() { return classHits; }
        public long getClassMisses() { return classMisses; }
        public long getClassHitRate() { return classHitRate; }
        
        @Override
        public String toString() {
            return String.format(
                "ReflectionCache Stats:\n" +
                "  Methods: %d entries, %d hits, %d misses (%.2f%% hit rate)\n" +
                "  Constructors: %d entries, %d hits, %d misses (%.2f%% hit rate)\n" +
                "  Fields: %d entries, %d hits, %d misses (%.2f%% hit rate)\n" +
                "  Classes: %d entries, %d hits, %d misses (%.2f%% hit rate)",
                methodEntries, methodHits, methodMisses, methodHitRate,
                constructorEntries, constructorHits, constructorMisses, constructorHitRate,
                fieldEntries, fieldHits, fieldMisses, fieldHitRate,
                classEntries, classHits, classMisses, classHitRate
            );
        }
    }
}