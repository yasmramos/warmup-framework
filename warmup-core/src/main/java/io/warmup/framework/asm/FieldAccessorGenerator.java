package io.warmup.framework.asm;

import io.warmup.framework.cache.ASMCacheManager;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.objectweb.asm.*;

public class FieldAccessorGenerator {
    private static final Logger log = Logger.getLogger(FieldAccessorGenerator.class.getName());
    private static final ASMCacheManager cacheManager = ASMCacheManager.getInstance();

    public static void setField(Object instance, String fieldName, Object value, String fieldDescriptor) {
        try {
            Class<?> targetClass = instance.getClass();
            String className = targetClass.getName();
            String internalName = className.replace('.', '/');

            // ✅ NUEVO: Generar clave de cache única
            String accessorClassName = className + "$$FieldAccessor$$" + fieldName;
            String cacheKey = accessorClassName;
            
            // ✅ NUEVO: Usar información de la clase target para el hash
            byte[] classData = getClassBytecode(targetClass);
            String sourceHash = cacheManager.calculateSourceHash(classData);
            
            // ✅ NUEVO: Buscar bytecode cacheado
            byte[] cachedBytecode = cacheManager.getCachedBytecode(cacheKey, sourceHash);
            Class<?> accessorClass;
            
            if (cachedBytecode != null) {
                log.log(Level.FINE, "Cache hit para field accessor: {0}.{1}", new Object[]{className, fieldName});
                // Cargar el accessor desde cache
                DynamicClassLoader loader = new DynamicClassLoader(targetClass.getClassLoader());
                accessorClass = loader.defineClass(accessorClassName, cachedBytecode);
            } else {
                log.log(Level.FINE, "Generando nuevo field accessor para: {0}.{1}", new Object[]{className, fieldName});
                // Generar clase accessor con ASM  
                byte[] bytecode = generateAccessorClass(internalName, fieldName, fieldDescriptor);
                
                // ✅ NUEVO: Cachear el bytecode generado
                cacheManager.cacheBytecode(cacheKey, sourceHash, bytecode);
                
                // Cargar el accessor  
                DynamicClassLoader loader = new DynamicClassLoader(targetClass.getClassLoader());
                accessorClass = loader.defineClass(accessorClassName, bytecode);
            }

            // NUEVO: Usar MethodHandle en lugar de reflexión  
            java.lang.invoke.MethodHandles.Lookup lookup = java.lang.invoke.MethodHandles.lookup();
            java.lang.invoke.MethodType methodType = java.lang.invoke.MethodType.methodType(
                    void.class,
                    Object.class,
                    Object.class
            );

            java.lang.invoke.MethodHandle setHandle = lookup.findStatic(
                    accessorClass,
                    "set",
                    methodType
            );

            // Invocar usando MethodHandle  
            setHandle.invoke(instance, value);

        } catch (Throwable e) {
            throw new RuntimeException("Error inyectando campo " + fieldName + " con ASM", e);
        }
    }
    
    /**
     * ✅ NUEVO: Obtener bytecode de la clase para el hash
     */
    private static byte[] getClassBytecode(Class<?> clazz) {
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

    private static byte[] generateAccessorClass(String targetClassInternal, String fieldName, String fieldDescriptor) {
        String accessorClassName = targetClassInternal + "$$FieldAccessor$$" + fieldName;

        ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_FRAMES);
        cw.visit(Opcodes.V1_8, Opcodes.ACC_PUBLIC, accessorClassName, null, "java/lang/Object", null);

        // Generar método: public static void set(Object instance, Object value)  
        MethodVisitor mv = cw.visitMethod(
                Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC,
                "set",
                "(Ljava/lang/Object;Ljava/lang/Object;)V",
                null,
                null
        );

        mv.visitCode();

        // Cast instance al tipo correcto  
        mv.visitVarInsn(Opcodes.ALOAD, 0);
        mv.visitTypeInsn(Opcodes.CHECKCAST, targetClassInternal);

        // Cargar value y hacer cast/unbox según el tipo  
        mv.visitVarInsn(Opcodes.ALOAD, 1);

        if (fieldDescriptor.startsWith("L")) {
            // Tipo objeto - hacer cast  
            String fieldType = fieldDescriptor.substring(1, fieldDescriptor.length() - 1);
            mv.visitTypeInsn(Opcodes.CHECKCAST, fieldType);
        } else {
            // Tipo primitivo - unboxing  
            unboxPrimitive(mv, fieldDescriptor);
        }

        // Asignar al campo usando PUTFIELD  
        mv.visitFieldInsn(Opcodes.PUTFIELD, targetClassInternal, fieldName, fieldDescriptor);

        mv.visitInsn(Opcodes.RETURN);
        mv.visitMaxs(2, 2);
        mv.visitEnd();

        cw.visitEnd();
        return cw.toByteArray();
    }

    private static void unboxPrimitive(MethodVisitor mv, String descriptor) {
        switch (descriptor) {
            case "I":
                mv.visitTypeInsn(Opcodes.CHECKCAST, "java/lang/Integer");
                mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/Integer", "intValue", "()I", false);
                break;
            case "J":
                mv.visitTypeInsn(Opcodes.CHECKCAST, "java/lang/Long");
                mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/Long", "longValue", "()J", false);
                break;
            case "Z":
                mv.visitTypeInsn(Opcodes.CHECKCAST, "java/lang/Boolean");
                mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/Boolean", "booleanValue", "()Z", false);
                break;
            case "D":
                mv.visitTypeInsn(Opcodes.CHECKCAST, "java/lang/Double");
                mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/Double", "doubleValue", "()D", false);
                break;
            case "F":
                mv.visitTypeInsn(Opcodes.CHECKCAST, "java/lang/Float");
                mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/Float", "floatValue", "()F", false);
                break;
            case "B":
                mv.visitTypeInsn(Opcodes.CHECKCAST, "java/lang/Byte");
                mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/Byte", "byteValue", "()B", false);
                break;
            case "S":
                mv.visitTypeInsn(Opcodes.CHECKCAST, "java/lang/Short");
                mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/Short", "shortValue", "()S", false);
                break;
            case "C":
                mv.visitTypeInsn(Opcodes.CHECKCAST, "java/lang/Character");
                mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/Character", "charValue", "()C", false);
                break;
        }
    }
    
    /**
     * ✅ NUEVO: Limpiar cache para una clase específica
     */
    public static void clearCacheForClass(Class<?> clazz) {
        // Invalidar todos los field accessors de la clase
        cacheManager.invalidatePackage(clazz.getPackage().getName() + "." + clazz.getSimpleName());
    }
    
    /**
     * ✅ NUEVO: Limpiar todo el cache de field accessors
     */
    public static void clearAllCache() {
        cacheManager.clearCache();
    }
    
    /**
     * ✅ NUEVO: Obtener estadísticas del cache
     */
    public static void printCacheStats() {
        cacheManager.getStats();
    }

    private static class DynamicClassLoader extends ClassLoader {

        public DynamicClassLoader(ClassLoader parent) {
            super(parent);
        }

        public Class<?> defineClass(String name, byte[] bytecode) {
            return defineClass(name, bytecode, 0, bytecode.length);
        }
    }
}
