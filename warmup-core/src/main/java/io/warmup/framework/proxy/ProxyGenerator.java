package io.warmup.framework.proxy;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;
import org.objectweb.asm.*;

import io.warmup.framework.asm.AsmCoreUtils;

public class ProxyGenerator {

    private static final Logger log = Logger.getLogger(ProxyGenerator.class.getName());
    private static final AtomicInteger COUNTER = new AtomicInteger(0);
    private static final Map<String, Class<?>> CLASS_CACHE = new ConcurrentHashMap<>();
    private static final String PROXY_PACKAGE = "io.proxycraft.generated.";

    private final Class<?> targetClass;
    private final List<Class<?>> interfaces = new ArrayList<>();
    private Class<?> superClass = Object.class;
    private InvocationHandler defaultHandler;
    private String className;
    private boolean callSuperConstructor = true;

    public ProxyGenerator(Class<?> targetClass) {
        this.targetClass = targetClass;
        this.superClass = AsmCoreUtils.isInterface(targetClass) ? Object.class : targetClass;
        if (AsmCoreUtils.isInterface(targetClass)) {
            this.interfaces.add(targetClass);
        }
    }

    public ProxyGenerator callSuperConstructor(boolean callSuper) {
        this.callSuperConstructor = callSuper;
        return this;
    }

    public ProxyGenerator intercept(InvocationHandler handler) {
        this.defaultHandler = handler;
        return this;
    }

    public ProxyGenerator implement(Class<?>... interfaces) {
        Collections.addAll(this.interfaces, interfaces);
        return this;
    }

    public ProxyGenerator extend(Class<?> superClass) {
        if (AsmCoreUtils.isInterface(superClass)) {
            throw new IllegalArgumentException("Cannot extend interface: " + superClass.getName());
        }
        this.superClass = superClass;
        return this;
    }

    public ProxyGenerator name(String name) {
        this.className = name;
        return this;
    }

    @SuppressWarnings("unchecked")
    public <T> T build() {
        try {
            Class<T> proxyClass = buildClass();
            T instance;

            if (callSuperConstructor) {
                // 1) Constructor normal: ejecuta super()
                // ✅ FASE 6: Invocación progresiva del constructor - ASM → MethodHandle → Reflection
                @SuppressWarnings("unchecked")
                T tempInstance = (T) AsmCoreUtils.invokeConstructorProgressive(proxyClass.getDeclaredConstructor());
                instance = tempInstance;
            } else {
                // 2) Constructor que NO llama a super() - requiere manejo especial
                instance = allocateInstanceWithoutSuper(proxyClass);
            }

            // Inyectar handler
            AsmCoreUtils.invokeMethod(instance, "setHandler", defaultHandler);

            return instance;
        } catch (Throwable e) {
            throw new RuntimeException("Error creating proxy instance", e);
        }
    }

    @SuppressWarnings("unchecked")
    private <T> T allocateInstanceWithoutSuper(Class<T> clazz) {
        try {
            // Usar Unsafe para crear instancia sin llamar al constructor
            return allocateWithUnsafe(clazz);
        } catch (Exception e) {
            // Fallback: usar constructor vacío (que ahora no llamará a super)
            try {
                // ✅ FASE 6: Invocación progresiva del constructor - ASM → MethodHandle → Reflection
                @SuppressWarnings("unchecked")
                T result = (T) AsmCoreUtils.invokeConstructorProgressive(clazz.getDeclaredConstructor());
                return result;
            } catch (Throwable ex) {
                throw new RuntimeException("Failed to create instance without super constructor", ex);
            }
        }
    }

    @SuppressWarnings("unchecked")
    private <T> T allocateWithUnsafe(Class<T> clazz) throws Exception {
        // Implementación usando sun.misc.Unsafe (solo para JDK 8-10)
        try {
            Class<?> unsafeClass = Class.forName("sun.misc.Unsafe");
            java.lang.reflect.Field theUnsafeField = unsafeClass.getDeclaredField("theUnsafe");
            theUnsafeField.setAccessible(true);
            Object unsafe = theUnsafeField.get(null);
            java.lang.reflect.Method allocateInstance = unsafeClass.getMethod("allocateInstance", Class.class);
            return (T) allocateInstance.invoke(unsafe, clazz);
        } catch (ClassNotFoundException | IllegalAccessException | IllegalArgumentException | NoSuchFieldException | NoSuchMethodException | InvocationTargetException e) {
            // Fallback para JDK 11+ - usar MethodHandles
            return allocateWithMethodHandles(clazz);
        }
    }

    @SuppressWarnings("unchecked")
    private <T> T allocateWithMethodHandles(Class<T> clazz) throws Exception {
        try {
            java.lang.invoke.MethodHandles.Lookup lookup = java.lang.invoke.MethodHandles.lookup();
            java.lang.invoke.MethodHandle allocate = lookup.findConstructor(clazz,
                    java.lang.invoke.MethodType.methodType(void.class));
            return (T) allocate.invoke();
        } catch (Exception e) {
            throw new RuntimeException("Failed to allocate instance with MethodHandles", e);
        } catch (Throwable ex) {
            log.severe(ex.getMessage());
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    public <T> Class<T> buildClass() {
        String fullClassName = generateClassName();

        if (CLASS_CACHE.containsKey(fullClassName)) {
            return (Class<T>) CLASS_CACHE.get(fullClassName);
        }

        try {
            ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_FRAMES);
            String internalClassName = fullClassName.replace('.', '/');
            String superInternalName = Type.getInternalName(superClass);

            // Definir la clase
            cw.visit(Opcodes.V1_8, Opcodes.ACC_PUBLIC,
                    internalClassName,
                    null,
                    superInternalName,
                    getInterfacesInternalNames());

            // Campo para el handler
            cw.visitField(Opcodes.ACC_PRIVATE, "handler",
                    Type.getDescriptor(InvocationHandler.class), null, null);

            // Generar constructor con o sin llamada a super()
            generateConstructor(cw, internalClassName, superInternalName);
            generateMethods(cw, internalClassName);
            generateSetHandlerMethod(cw, internalClassName);

            cw.visitEnd();

            byte[] classBytes = cw.toByteArray();
            saveClassForDebugging(fullClassName, classBytes);

            // Cargar la clase usando el ClassLoader correcto
            Class<?> proxyClass = new ProxyClassLoader(targetClass.getClassLoader())
                    .defineClass(fullClassName, classBytes);

            CLASS_CACHE.put(fullClassName, proxyClass);
            return (Class<T>) proxyClass;

        } catch (Exception e) {
            throw new RuntimeException("Error generating proxy class", e);
        }
    }

    private void generateConstructor(ClassWriter cw, String internalClassName, String superInternalName) {
        MethodVisitor mv = cw.visitMethod(Opcodes.ACC_PUBLIC, "<init>", "()V", null, null);
        mv.visitCode();
        // SIEMPRE invocamos super() – bytecode válido para Lookup.defineClass
        mv.visitVarInsn(Opcodes.ALOAD, 0);
        mv.visitMethodInsn(Opcodes.INVOKESPECIAL, superInternalName, "<init>", "()V", false);
        mv.visitInsn(Opcodes.RETURN);
        mv.visitMaxs(1, 1);
        mv.visitEnd();
    }

    // ... (el resto de los métodos permanecen igual que en la versión anterior)
    private void generateMethods(ClassWriter cw, String internalClassName) {
        Set<java.lang.reflect.Method> allMethods = getAllMethods();

        for (java.lang.reflect.Method method : allMethods) {
            if (shouldSkipMethod(method)) {
                continue;
            }

            generateInterceptedMethod(cw, method, internalClassName);
        }
    }

    private void generateInterceptedMethod(ClassWriter cw, java.lang.reflect.Method method, String internalClassName) {
        String methodDescriptor = Type.getMethodDescriptor(method);
        String[] exceptions = getExceptionsInternalNames(method);

        MethodVisitor mv = cw.visitMethod(Opcodes.ACC_PUBLIC, AsmCoreUtils.getName(method), methodDescriptor, null, exceptions);
        mv.visitCode();

        int argsCount = AsmCoreUtils.getParameterCount(method);

        /* 1.  handler  (InvocationHandler)  */
        mv.visitVarInsn(Opcodes.ALOAD, 0);
        mv.visitFieldInsn(Opcodes.GETFIELD, internalClassName, "handler",
                Type.getDescriptor(InvocationHandler.class));

        /* 2.  proxy object (this)  */
        mv.visitVarInsn(Opcodes.ALOAD, 0);

        /* 3.  method name (String)  */
        mv.visitLdcInsn(AsmCoreUtils.getName(method));

        /* 4.  args[]  (Object[])  – justo antes de la llamada  */
        mv.visitIntInsn(Opcodes.BIPUSH, argsCount);
        mv.visitTypeInsn(Opcodes.ANEWARRAY, "java/lang/Object");
        for (int i = 0; i < argsCount; i++) {
            mv.visitInsn(Opcodes.DUP);
            mv.visitIntInsn(Opcodes.BIPUSH, i);
            loadParameter(mv, AsmCoreUtils.getParameterTypes(method)[i], i + 1);
            boxPrimitive(mv, AsmCoreUtils.getParameterTypes(method)[i]);
            mv.visitInsn(Opcodes.AASTORE);
        }

        /* 5.  invoke handler: handler.invoke(proxy, methodName, args)  */
        mv.visitMethodInsn(Opcodes.INVOKEINTERFACE,
                Type.getInternalName(InvocationHandler.class),
                "invoke",
                "(Ljava/lang/Object;Ljava/lang/String;[Ljava/lang/Object;)Ljava/lang/Object;",
                true);

        /* 6.  manejar el valor de retorno  */
        handleReturnValue(mv, AsmCoreUtils.getReturnType(method));

        mv.visitMaxs(0, 0);   // ASM calculará
        mv.visitEnd();
    }

    private void loadParameter(MethodVisitor mv, Class<?> paramType, int index) {
        if (paramType == int.class || paramType == boolean.class || paramType == byte.class
                || paramType == short.class || paramType == char.class) {
            mv.visitVarInsn(Opcodes.ILOAD, index);
        } else if (paramType == long.class) {
            mv.visitVarInsn(Opcodes.LLOAD, index);
        } else if (paramType == double.class) {
            mv.visitVarInsn(Opcodes.DLOAD, index);
        } else if (paramType == float.class) {
            mv.visitVarInsn(Opcodes.FLOAD, index);
        } else {
            mv.visitVarInsn(Opcodes.ALOAD, index);
        }
    }

    private void boxPrimitive(MethodVisitor mv, Class<?> type) {
        if (!AsmCoreUtils.isPrimitive(type)) {
            return;
        }

        if (type == int.class) {
            mv.visitMethodInsn(Opcodes.INVOKESTATIC, "java/lang/Integer", "valueOf", "(I)Ljava/lang/Integer;", false);
        } else if (type == long.class) {
            mv.visitMethodInsn(Opcodes.INVOKESTATIC, "java/lang/Long", "valueOf", "(J)Ljava/lang/Long;", false);
        } else if (type == double.class) {
            mv.visitMethodInsn(Opcodes.INVOKESTATIC, "java/lang/Double", "valueOf", "(D)Ljava/lang/Double;", false);
        } else if (type == float.class) {
            mv.visitMethodInsn(Opcodes.INVOKESTATIC, "java/lang/Float", "valueOf", "(F)Ljava/lang/Float;", false);
        } else if (type == boolean.class) {
            mv.visitMethodInsn(Opcodes.INVOKESTATIC, "java/lang/Boolean", "valueOf", "(Z)Ljava/lang/Boolean;", false);
        } else if (type == byte.class) {
            mv.visitMethodInsn(Opcodes.INVOKESTATIC, "java/lang/Byte", "valueOf", "(B)Ljava/lang/Byte;", false);
        } else if (type == short.class) {
            mv.visitMethodInsn(Opcodes.INVOKESTATIC, "java/lang/Short", "valueOf", "(S)Ljava/lang/Short;", false);
        } else if (type == char.class) {
            mv.visitMethodInsn(Opcodes.INVOKESTATIC, "java/lang/Character", "valueOf", "(C)Ljava/lang/Character;", false);
        }
    }

    private void handleReturnValue(MethodVisitor mv, Class<?> returnType) {
        if (returnType == void.class) {
            mv.visitInsn(Opcodes.POP);
            mv.visitInsn(Opcodes.RETURN);
        } else if (AsmCoreUtils.isPrimitive(returnType)) {
            if (returnType == int.class) {
                mv.visitTypeInsn(Opcodes.CHECKCAST, "java/lang/Number");
                mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/Number", "intValue", "()I", false);
                mv.visitInsn(Opcodes.IRETURN);
            } else if (returnType == long.class) {
                mv.visitTypeInsn(Opcodes.CHECKCAST, "java/lang/Number");
                mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/Number", "longValue", "()J", false);
                mv.visitInsn(Opcodes.LRETURN);
            } else if (returnType == double.class) {
                mv.visitTypeInsn(Opcodes.CHECKCAST, "java/lang/Number");
                mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/Number", "doubleValue", "()D", false);
                mv.visitInsn(Opcodes.DRETURN);
            } else if (returnType == float.class) {
                mv.visitTypeInsn(Opcodes.CHECKCAST, "java/lang/Number");
                mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/Number", "floatValue", "()F", false);
                mv.visitInsn(Opcodes.FRETURN);
            } else if (returnType == boolean.class) {
                mv.visitTypeInsn(Opcodes.CHECKCAST, "java/lang/Boolean");
                mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/Boolean", "booleanValue", "()Z", false);
                mv.visitInsn(Opcodes.IRETURN);
            } else if (returnType == byte.class) {
                mv.visitTypeInsn(Opcodes.CHECKCAST, "java/lang/Number");
                mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/Number", "byteValue", "()B", false);
                mv.visitInsn(Opcodes.IRETURN);
            } else if (returnType == short.class) {
                mv.visitTypeInsn(Opcodes.CHECKCAST, "java/lang/Number");
                mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/Number", "shortValue", "()S", false);
                mv.visitInsn(Opcodes.IRETURN);
            } else if (returnType == char.class) {
                mv.visitTypeInsn(Opcodes.CHECKCAST, "java/lang/Character");
                mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/Character", "charValue", "()C", false);
                mv.visitInsn(Opcodes.IRETURN);
            }
        } else {
            if (returnType != Object.class) {
                mv.visitTypeInsn(Opcodes.CHECKCAST, Type.getInternalName(returnType));
            }
            mv.visitInsn(Opcodes.ARETURN);
        }
    }

    private void generateSetHandlerMethod(ClassWriter cw, String internalClassName) {
        MethodVisitor mv = cw.visitMethod(Opcodes.ACC_PUBLIC, "setHandler",
                "(Lio/warmup/framework/proxy/InvocationHandler;)V", null, null);
        mv.visitCode();
        mv.visitVarInsn(Opcodes.ALOAD, 0);
        mv.visitVarInsn(Opcodes.ALOAD, 1);
        mv.visitFieldInsn(Opcodes.PUTFIELD, internalClassName, "handler", Type.getDescriptor(InvocationHandler.class));
        mv.visitInsn(Opcodes.RETURN);
        mv.visitMaxs(2, 2);
        mv.visitEnd();
    }

    private Set<java.lang.reflect.Method> getAllMethods() {
        Set<java.lang.reflect.Method> methods = new HashSet<>();

        for (Class<?> iface : interfaces) {
            // iface.getMethods() mantenido para compatibilidad con ASM
            methods.addAll(Arrays.asList(iface.getMethods()));
        }

        if (!AsmCoreUtils.isInterface(targetClass)) {
            // targetClass.getMethods() mantenido para compatibilidad con ASM
            methods.addAll(Arrays.asList(targetClass.getMethods()));
        }

        return methods;
    }

    private boolean shouldSkipMethod(java.lang.reflect.Method method) {
        if (AsmCoreUtils.getDeclaringClass(method) == Object.class) {
            return true;
        }

        if (AsmCoreUtils.isFinal(method)) {
            return true;
        }

        if (AsmCoreUtils.isSynthetic(method)) {
            return true;
        }

        return false;
    }

    private String[] getInterfacesInternalNames() {
        return interfaces.stream()
                .map(clazz -> Type.getInternalName(clazz))
                .toArray(String[]::new);
    }

    private String[] getExceptionsInternalNames(java.lang.reflect.Method method) {
        return Arrays.stream(AsmCoreUtils.getExceptionTypes(method))
                .map(Type::getInternalName)
                .toArray(String[]::new);
    }

    private String generateClassName() {
        if (className != null) {
            return PROXY_PACKAGE + className;
        }
        return PROXY_PACKAGE + "Proxy" + COUNTER.incrementAndGet();
    }

    private void saveClassForDebugging(String className, byte[] classBytes) {
        try {
            File file = new File("debug/" + className.replace('.', '/') + ".class");
            file.getParentFile().mkdirs();
            try (FileOutputStream fos = new FileOutputStream(file)) {
                fos.write(classBytes);
            }
        } catch (IOException e) {
            // Ignorar errores de debug
        }
    }

    private static class ProxyClassLoader extends ClassLoader {

        public ProxyClassLoader(ClassLoader parent) {
            super(parent);
        }

        public Class<?> defineClass(String name, byte[] b) {
            return defineClass(name, b, 0, b.length);
        }
    }
}
