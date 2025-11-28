package io.warmup.framework.proxy;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import io.warmup.framework.asm.AsmCoreUtils;
// import io.warmup.framework.jit.asm.SimpleASMUtils; // MIGRATED to AsmCoreUtils
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

/**
 * Proxycraft integrado con @Async mejorado
 */
public final class AsyncProxyFactory {

    public static <T> T createAsyncProxy(T target) {
        try {
            Class<?> targetClass = target.getClass();

            // Obtener el bytecode de la clase original
            byte[] originalBytecode = getClassBytecode(targetClass);

            // Crear ClassLoader para cargar clases transformadas y lambdas
            AsyncClassLoader classLoader = new AsyncClassLoader(targetClass.getClassLoader());

            // Transformar la clase
            byte[] transformedBytecode = classLoader.transformClass(
                    targetClass.getName() + "$$AsyncProxy",
                    originalBytecode
            );

            // Cargar clase transformada
            Class<?> asyncClass = classLoader.defineClass(
                    targetClass.getName() + "$$AsyncProxy",
                    transformedBytecode
            );

            // Crear instancia - asumiendo que tiene constructor por defecto
            // ✅ FASE 6: Invocación progresiva del constructor - ASM → MethodHandle → Reflection
            T proxy = (T) AsmCoreUtils.invokeConstructorProgressive(asyncClass.getDeclaredConstructor());

            // Copiar campos del target al proxy
            copyFields(target, proxy);

            return proxy;

        } catch (Throwable e) {
            throw new RuntimeException("Error creando proxy @Async", e);
        }
    }

    private static byte[] getClassBytecode(Class<?> clazz) throws IOException {
        String className = clazz.getName();
        String classPath = className.replace('.', '/') + ".class";

        try (InputStream inputStream = clazz.getClassLoader().getResourceAsStream(classPath)) {
            if (inputStream == null) {
                throw new IOException("No se pudo encontrar el bytecode de la clase: " + className);
            }
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            byte[] data = new byte[4096]; // Buffer de 4KB
            int bytesRead;
            while ((bytesRead = inputStream.read(data, 0, data.length)) != -1) {
                buffer.write(data, 0, bytesRead);
            }
            return buffer.toByteArray();
        }
    }

    /**
     * Copia los campos del objeto original al proxy
     * ✅ MIGRATED: Implementación básica usando AsmCoreUtils
     */
    private static <T> void copyFields(T source, T destination) {
        // ✅ MIGRATED: Usar AsmCoreUtils para acceso a campos
        // TODO: Implementar copia completa de campos usando ASM
        try {
            java.lang.reflect.Field[] fields = source.getClass().getDeclaredFields();
            for (java.lang.reflect.Field field : fields) {
                if (!java.lang.reflect.Modifier.isStatic(field.getModifiers())) {
                    field.setAccessible(true);
                    Object value = field.get(source);
                    field.set(destination, value);
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Error copiando campos", e);
        }
    }

    /**
     * Versión que genera una subclase proxy con referencia al original
     */
    public static <T> T createAsyncProxyWithDelegate(T target) {
        try {
            Class<?> targetClass = target.getClass();
            byte[] originalBytecode = getClassBytecode(targetClass);

            // Generar subclase proxy
            byte[] proxyBytecode = generateProxySubclass(originalBytecode, targetClass);

            AsyncClassLoader classLoader = new AsyncClassLoader(targetClass.getClassLoader());
            Class<?> proxyClass = classLoader.defineClass(
                    targetClass.getName() + "$$AsyncProxy",
                    proxyBytecode
            );

            // Crear instancia pasando el target como delegado
            return (T) proxyClass.getDeclaredConstructor(targetClass).newInstance(target);

        } catch (IOException | IllegalAccessException | IllegalArgumentException | InstantiationException | NoSuchMethodException | InvocationTargetException e) {
            throw new RuntimeException("Error creando proxy con delegate", e);
        }
    }

    /**
     * Genera una subclase que delega en el target original
     */
    private static byte[] generateProxySubclass(byte[] originalBytecode, Class<?> targetClass) {
        ClassReader classReader = new ClassReader(originalBytecode);
        ClassWriter classWriter = new ClassWriter(ClassWriter.COMPUTE_FRAMES);

        String targetInternalName = targetClass.getName().replace('.', '/');
        String proxyInternalName = targetInternalName + "$$AsyncProxy";

        ClassVisitor subclassVisitor = new ClassVisitor(Opcodes.ASM9, classWriter) {
            @Override
            public void visit(int version, int access, String name, String signature,
                    String superName, String[] interfaces) {
                // Crear subclase con mismo nombre interno pero diferente nombre completo
                super.visit(version, access, proxyInternalName, signature, targetInternalName, interfaces);

                // Añadir campo para almacenar el delegado
                super.visitField(
                        Opcodes.ACC_PRIVATE + Opcodes.ACC_FINAL,
                        "delegate",
                        "L" + targetInternalName + ";",
                        null,
                        null
                );
            }

            @Override
            public MethodVisitor visitMethod(int access, String name, String descriptor,
                    String signature, String[] exceptions) {

                // No modificar constructores - añadiremos uno nuevo
                if ("<init>".equals(name)) {
                    return null; // Ignorar constructores originales
                }

                MethodVisitor mv = super.visitMethod(access, name, descriptor, signature, exceptions);

                // Para métodos no estáticos, redirigir al delegate
                if ((access & Opcodes.ACC_STATIC) == 0 && mv != null) {
                    return new MethodVisitor(Opcodes.ASM9, mv) {
                        @Override
                        public void visitCode() {
                            // Cargar el campo delegate y llamar al método original
                            super.visitVarInsn(Opcodes.ALOAD, 0);
                            super.visitFieldInsn(Opcodes.GETFIELD, proxyInternalName, "delegate", "L" + targetInternalName + ";");

                            // Cargar parámetros
                            Type[] argTypes = Type.getArgumentTypes(descriptor);
                            int localIndex = 1; // 0 es this
                            for (Type argType : argTypes) {
                                super.visitVarInsn(argType.getOpcode(Opcodes.ILOAD), localIndex);
                                localIndex += argType.getSize();
                            }

                            // Llamar al método en el delegate
                            super.visitMethodInsn(Opcodes.INVOKEVIRTUAL, targetInternalName, name, descriptor, false);
                            super.visitInsn(Type.getReturnType(descriptor).getOpcode(Opcodes.IRETURN));
                        }

                        @Override
                        public void visitMaxs(int maxStack, int maxLocals) {
                            // ASM calculará automáticamente
                        }
                    };
                }

                return mv;
            }

            @Override
            public void visitEnd() {
                // Añadir constructor que acepta el delegate
                MethodVisitor mv = super.visitMethod(
                        Opcodes.ACC_PUBLIC,
                        "<init>",
                        "(L" + targetInternalName + ";)V",
                        null,
                        null
                );

                mv.visitCode();
                // Llamar al constructor de la superclase
                mv.visitVarInsn(Opcodes.ALOAD, 0);
                mv.visitMethodInsn(Opcodes.INVOKESPECIAL, targetInternalName, "<init>", "()V", false);

                // Almacenar el delegate
                mv.visitVarInsn(Opcodes.ALOAD, 0);
                mv.visitVarInsn(Opcodes.ALOAD, 1);
                mv.visitFieldInsn(Opcodes.PUTFIELD, proxyInternalName, "delegate", "L" + targetInternalName + ";");

                mv.visitInsn(Opcodes.RETURN);
                mv.visitMaxs(2, 2);
                mv.visitEnd();

                super.visitEnd();
            }
        };

        classReader.accept(subclassVisitor, 0); // Usar 0 en lugar de EXPAND_FRAMES
        return classWriter.toByteArray();
    }

    /**
     * Método principal simplificado - RECOMENDADO
     *
     * @param <T>
     * @param target
     * @return
     */
    public static <T> T createProxy(T target) {
        return createAsyncProxyWithDelegate(target);
    }
}
