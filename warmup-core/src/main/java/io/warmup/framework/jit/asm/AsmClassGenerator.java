package io.warmup.framework.jit.asm;

import io.warmup.framework.asm.AsmCoreUtils;
import io.warmup.framework.cache.ASMCacheManager;
import io.warmup.framework.core.metadata.ConstructorMetadata;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.objectweb.asm.*;
import static org.objectweb.asm.Opcodes.*;
import org.objectweb.asm.commons.Method;

public class AsmClassGenerator {
    private static final Logger log = Logger.getLogger(AsmClassGenerator.class.getName());
    private static final String SUPPLIER_DESC = Type.getDescriptor(Supplier.class);
    private static final Method GET_METHOD = Method.getMethod("Object get()");
    private static final ASMCacheManager cacheManager = ASMCacheManager.getInstance();
    
    // ✅ NUEVO: Set para rastrear clases ya invalidadas y evitar StackOverflow
    private static final ConcurrentMap<String, Boolean> invalidationTracking = new ConcurrentHashMap<>();
    private static final int MAX_INVALIDATION_ATTEMPTS = 3;
    
    public <T> Supplier<T> createInstanceSupplier(Class<T> clazz, Object... dependencies) {
        try {
            // ✅ MEJORADO: Validar que la clase se puede instanciar
            validateClassForInstantiation(clazz);
            
            String className = clazz.getName().replace('.', '/');
            String supplierClassName = className + "_JITSupplier";
            String cacheKey = supplierClassName;
            
            // ✅ NUEVO: Verificar caché usando información de la clase original
            byte[] originalClassData = getClassBytecode(clazz);
            String sourceHash = cacheManager.calculateSourceHash(originalClassData);
            
            // Buscar bytecode cacheado
            byte[] cachedBytecode = cacheManager.getCachedBytecode(cacheKey, sourceHash);
            if (cachedBytecode != null) {
                // ✅ NUEVO: Validar y limpiar caché corrupto automáticamente
                try {
                    cachedBytecode = validateAndFixCachedBytecode(cacheKey, clazz.getName(), cachedBytecode);
                    
                    if (cachedBytecode != null) {
                        log.log(Level.FINE, "Cache hit válido para JIT supplier: {0}", clazz.getName());
                        Class<?> cachedClass = defineClass(supplierClassName.replace('/', '.'), cachedBytecode);
                        return (Supplier<T>) AsmCoreUtils.newInstance(cachedClass);
                    }
                    // Si el caché era corrupto, se devolverá null y se regenerará
                } catch (IllegalStateException e) {
                    // ✅ NUEVO: Error crítico - el bytecode no se puede generar correctamente
                    log.log(Level.SEVERE, "Fallo crítico generando supplier para {0}: {1}. " +
                            "El bytecode generado es persistentemente corrupto.", 
                            new Object[]{clazz.getName(), e.getMessage()});
                    throw new RuntimeException(
                        "No se puede generar JIT supplier para " + clazz.getName() + 
                        ". Error: " + e.getMessage(), e);
                }
            }
            
            log.log(Level.FINE, "Generando nuevo JIT supplier para: {0}", clazz.getName());
            
            // Generar bytecode si no está en caché
            ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_FRAMES);
            cw.visit(V1_8, ACC_PUBLIC, supplierClassName, 
                    "Ljava/lang/Object;L" + SUPPLIER_DESC + ";", 
                    "java/lang/Object", new String[]{"java/util/function/Supplier"});
            
            // Constructor
            generateConstructor(cw);
            
            // Método get()
            generateGetMethod(cw, className, clazz, dependencies);
            
            cw.visitEnd();
            
            byte[] bytecode = cw.toByteArray();
            
            // ✅ NUEVO: Cachear el bytecode generado
            cacheManager.cacheBytecode(cacheKey, sourceHash, bytecode);
            
            // Definir y cargar la clase
            Class<?> supplierClass = defineClass(supplierClassName.replace('/', '.'), bytecode);
            // ✅ REFACTORIZADO: Usar ASM en lugar de reflexión
            return (Supplier<T>) AsmCoreUtils.newInstance(supplierClass);
            
        } catch (Exception e) {
            // ✅ MEJORADO: Proporcionar información de depuración más detallada
            StringBuilder errorDetails = new StringBuilder();
            errorDetails.append("Failed to generate JIT supplier for: ").append(clazz.getName()).append("\n");
            errorDetails.append("Reason: ").append(e.getMessage()).append("\n");
            errorDetails.append("Class modifiers: ").append(Integer.toHexString(clazz.getModifiers())).append("\n");
            errorDetails.append("Is interface: ").append(clazz.isInterface()).append("\n");
            errorDetails.append("Is abstract: ").append(Modifier.isAbstract(clazz.getModifiers())).append("\n");
            errorDetails.append("Is member class: ").append(clazz.isMemberClass()).append("\n");
            errorDetails.append("Is static: ").append(Modifier.isStatic(clazz.getModifiers())).append("\n");
            
            try {
                Constructor<?>[] constructors = clazz.getDeclaredConstructors();
                errorDetails.append("Available constructors: ").append(constructors.length).append("\n");
                for (Constructor<?> ctor : constructors) {
                    errorDetails.append("  - ").append(ctor).append("\n");
                }
            } catch (Exception constructorError) {
                errorDetails.append("Error checking constructors: ").append(constructorError.getMessage()).append("\n");
            }
            
            throw new RuntimeException(errorDetails.toString(), e);
        }
    }
    
    /**
     * ✅ NUEVO: Obtener bytecode de la clase original para el hash
     */
    private byte[] getClassBytecode(Class<?> clazz) {
        try {
            String classPath = clazz.getName().replace('.', '/') + ".class";
            try (java.io.InputStream is = clazz.getClassLoader().getResourceAsStream(classPath)) {
                if (is == null) {
                    throw new RuntimeException("No se pudo cargar bytecode de: " + classPath);
                }
                java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream();
                byte[] buffer = new byte[4096];
                int bytesRead;
                while ((bytesRead = is.read(buffer)) != -1) {
                    baos.write(buffer, 0, bytesRead);
                }
                return baos.toByteArray();
            }
        } catch (Exception e) {
            // Si no se puede cargar el bytecode, usar el nombre de clase como fallback
            return clazz.getName().getBytes();
        }
    }
    
    // Validar que la clase se puede instanciar
    private <T> void validateClassForInstantiation(Class<T> clazz) {
        if (clazz.isInterface()) {
            throw new IllegalArgumentException("No se puede instanciar interfaz: " + clazz.getName());
        }
        
        if (Modifier.isAbstract(clazz.getModifiers())) {
            throw new IllegalArgumentException("No se puede instanciar clase abstracta: " + clazz.getName());
        }
        
        // Verificar si es una clase interna no estática
        if (clazz.isMemberClass() && !Modifier.isStatic(clazz.getModifiers())) {
            throw new IllegalArgumentException(
                "Clase interna no estática: " + clazz.getName() + 
                ". Las clases internas deben ser estáticas para la inyección de dependencias.");
        }
    }
    
    private void generateConstructor(ClassWriter cw) {
        MethodVisitor mv = cw.visitMethod(ACC_PUBLIC, "<init>", "()V", null, null);
        mv.visitCode();
        mv.visitVarInsn(ALOAD, 0);
        mv.visitMethodInsn(INVOKESPECIAL, "java/lang/Object", "<init>", "()V", false);
        mv.visitInsn(RETURN);
        mv.visitMaxs(1, 1);
        mv.visitEnd();
    }
    
    private <T> void generateGetMethod(ClassWriter cw, String targetClassName, Class<T> clazz, Object[] dependencies) {
        MethodVisitor mv = cw.visitMethod(ACC_PUBLIC, "get", "()Ljava/lang/Object;", null, null);
        mv.visitCode();
        
        ConstructorMetadata constructor = ConstructorFinder.findInjectableConstructor(clazz);
        String[] paramTypeNames = constructor.getParameterTypes();
        
        // NEW instruction - crear nueva instancia
        mv.visitTypeInsn(NEW, targetClassName);
        mv.visitInsn(DUP);
        
        // Cargar parámetros del constructor (valores por defecto)
        for (String paramTypeName : paramTypeNames) {
            try {
                Class<?> paramType = Class.forName(paramTypeName.replace('/', '.'));
                loadDefaultValue(mv, paramType);
            } catch (ClassNotFoundException e) {
                throw new RuntimeException("Cannot load class: " + paramTypeName, e);
            }
        }
        
        // INVOKESPECIAL constructor
        String constructorDesc = "(";
        for (String paramTypeName : paramTypeNames) {
            try {
                Class<?> paramType = Class.forName(paramTypeName.replace('/', '.'));
                constructorDesc += Type.getDescriptor(paramType);
            } catch (ClassNotFoundException e) {
                throw new RuntimeException("Cannot load class: " + paramTypeName, e);
            }
        }
        constructorDesc += ")V";
        mv.visitMethodInsn(INVOKESPECIAL, targetClassName, "<init>", constructorDesc, false);
        
        // RETURN
        mv.visitInsn(ARETURN);
        mv.visitMaxs(paramTypeNames.length + 2, 1);
        mv.visitEnd();
    }
    
    private void loadDefaultValue(MethodVisitor mv, Class<?> type) {
        if (type == boolean.class || type == Boolean.class) {
            mv.visitInsn(ICONST_0); // false
        } else if (type == byte.class || type == Byte.class) {
            mv.visitInsn(ICONST_0);
        } else if (type == char.class || type == Character.class) {
            mv.visitInsn(ICONST_0);
        } else if (type == short.class || type == Short.class) {
            mv.visitInsn(ICONST_0);
        } else if (type == int.class || type == Integer.class) {
            mv.visitInsn(ICONST_0);
        } else if (type == long.class || type == Long.class) {
            mv.visitInsn(LCONST_0);
        } else if (type == float.class || type == Float.class) {
            mv.visitInsn(FCONST_0);
        } else if (type == double.class || type == Double.class) {
            mv.visitInsn(DCONST_0);
        } else {
            mv.visitInsn(ACONST_NULL); // null para objetos
        }
    }
    
    private Class<?> defineClass(String name, byte[] bytecode) throws Exception {
        try {
            // Usar reflection para ClassLoader.defineClass
            return defineClassWithReflection(name, bytecode);
        } catch (Exception e) {
            throw new RuntimeException("Failed to define class: " + name, e);
        }
    }
    
    private Class<?> defineClassWithReflection(String name, byte[] bytecode) throws Exception {
        try {
            // ✅ NUEVO: Validar magic number antes de definir la clase
            validateBytecodeMagicNumber(bytecode);
            
            java.lang.reflect.Method defineClass = ClassLoader.class.getDeclaredMethod(
                "defineClass", String.class, byte[].class, int.class, int.class);
            defineClass.setAccessible(true);
            return (Class<?>) defineClass.invoke(
                Thread.currentThread().getContextClassLoader(), 
                name, bytecode, 0, bytecode.length);
        } catch (IllegalAccessException | NoSuchMethodException | InvocationTargetException e) {
            throw new RuntimeException("Failed to define class with reflection: " + name, e);
        }
    }
    
    /**
     * ✅ NUEVO: Validar que el bytecode tiene el magic number correcto para una clase Java
     */
    private void validateBytecodeMagicNumber(byte[] bytecode) {
        if (bytecode == null || bytecode.length < 4) {
            throw new IllegalArgumentException("Bytecode inválido: datos nulos o muy pequeños");
        }
        
        // Magic number correcto para archivos .class: 0xCAFEBABE
        int magic1 = (bytecode[0] & 0xFF) << 24 | (bytecode[1] & 0xFF) << 16 | 
                     (bytecode[2] & 0xFF) << 8 | (bytecode[3] & 0xFF);
        
        if (magic1 != 0xCAFEBABE) {
            // Mostrar información de debugging
            String actualHex = String.format("0x%08X", magic1);
            log.log(Level.SEVERE, "Magic number inválido en bytecode. Esperado: 0xCAFEBABE, Encontrado: {0}", actualHex);
            
            // Si es GZIP (0x1F8B...), significa que los datos están comprimidos
            if (magic1 == 0x1F8B0000) {
                throw new IllegalStateException(
                    "Bytecode comprimido detectado. El ASMCacheManager devolvió datos GZIP en lugar de bytecode puro. " +
                    "Posibles causas: compresión activada pero datos no descomprimidos correctamente.");
            }
            
            throw new IllegalStateException(
                "Bytecode corrupto detectado. Magic number inválido: " + actualHex + ". " +
                "El caché puede estar corrupto y debe ser limpiado.");
        }
    }
    
    /**
     * ✅ NUEVO: Validar y limpiar bytecode del caché con protección contra StackOverflow
     */
    private byte[] validateAndFixCachedBytecode(String cacheKey, String className, byte[] cachedBytecode) {
        try {
            // ✅ NUEVO: Logging detallado al inicio de la validación
            log.log(Level.FINE, "Validando bytecode del caché para clase: {0}", className);
            
            validateBytecodeMagicNumber(cachedBytecode);
            
            // ✅ NUEVO: Si llegamos aquí, el bytecode es válido - limpiar tracking si existe
            if (invalidationTracking.containsKey(className)) {
                log.log(Level.FINE, "Bytecode válido detectado para clase previamente invalidada: {0}", className);
                invalidationTracking.remove(className);
            }
            
            return cachedBytecode;
        } catch (IllegalStateException e) {
            // ✅ NUEVO: Sistema de tracking para evitar StackOverflow
            Boolean previousAttempt = invalidationTracking.putIfAbsent(className, Boolean.TRUE);
            
            if (previousAttempt != null) {
                // Esta clase ya fue invalidada antes - evitar ciclo infinito
                log.log(Level.SEVERE, "StackOverflow evitado: clase {0} ya fue invalidada previamente. " +
                        "El bytecode generado es persistentemente corrupto. Error: {1}", 
                        new Object[]{className, e.getMessage()});
                
                throw new IllegalStateException(
                    "Fallo crítico: No se puede generar bytecode válido para la clase " + className + 
                    ". El bytecode generado es persistentemente corrupto y no se puede cachear. " +
                    "Error original: " + e.getMessage() + ". Se requiere revisión manual del generador de bytecode.");
            }
            
            // Primera vez que encontramos esta clase corrupta - proceder con limpieza
            log.log(Level.WARNING, "Bytecode corrupto detectado para clase: {0}. Error: {1}. " +
                    "Intentando limpiar caché...", 
                    new Object[]{className, e.getMessage()});
            
            // Mostrar información de debugging adicional
            if (cachedBytecode != null && cachedBytecode.length >= 4) {
                int magic1 = (cachedBytecode[0] & 0xFF) << 24 | (cachedBytecode[1] & 0xFF) << 16 | 
                             (cachedBytecode[2] & 0xFF) << 8 | (cachedBytecode[3] & 0xFF);
                String actualHex = String.format("0x%08X", magic1);
                log.log(Level.WARNING, "Magic number problemático detectado: {0} para clase: {1}", 
                        new Object[]{actualHex, className});
            }
            
            try {
                cacheManager.invalidate(cacheKey);
                log.log(Level.INFO, "Caché corrupto limpiado exitosamente para: {0}. " +
                        "Se forzará la regeneración del bytecode.", className);
            } catch (Exception cleanupError) {
                log.log(Level.WARNING, "Error durante la limpieza del caché para {0}: {1}", 
                        new Object[]{className, cleanupError.getMessage()});
            }
            
            // Devolver null para forzar regeneración una sola vez
            return null;
        }
    }
    public void clearCacheForClass(Class<?> clazz) {
        String supplierClassName = clazz.getName().replace('.', '/') + "_JITSupplier";
        cacheManager.invalidate(supplierClassName);
    }
    
    /**
     * ✅ NUEVO: Limpiar todo el cache JIT
     */
    public void clearAllCache() {
        cacheManager.clearCache();
        // ✅ NUEVO: Limpiar también el tracking de invalidaciones
        invalidationTracking.clear();
        log.log(Level.INFO, "Cache JIT y tracking de invalidaciones limpiados completamente");
    }
    
    /**
     * ✅ NUEVO: Obtener estadísticas del cache
     */
    public void printCacheStats() {
        cacheManager.getStats();
        log.log(Level.INFO, "Tracking de invalidaciones activas: {0} clases", invalidationTracking.size());
    }
    
    /**
     * ✅ NUEVO: Limpiar tracking de invalidaciones manualmente
     */
    public void clearInvalidationTracking() {
        int cleared = invalidationTracking.size();
        invalidationTracking.clear();
        log.log(Level.INFO, "Tracking de invalidaciones limpiado: {0} entradas removidas", cleared);
    }
}