package io.warmup.framework.asm;

// import io.warmup.framework.jit.asm.SimpleASMUtils; // MIGRATED to AsmCoreUtils
import java.io.IOException;
import java.util.*;
import org.objectweb.asm.*;
import org.objectweb.asm.commons.GeneratorAdapter;
import org.objectweb.asm.commons.Method;

public class AsmFieldInjector {

    private static final String JAKARTA_INJECT_DESC = "Ljakarta/inject/Inject;";
    private static final String WARMUP_INJECT_DESC = "Lio/warmup/framework/annotation/Inject;";

    public static void injectField(Object instance, String fieldName, Object value) {
        try {
            Class<?> clazz = instance.getClass();
            String className = clazz.getName();
            String internalName = className.replace('.', '/');

            // Generar una clase helper que hace la asignación  
            String helperClassName = className + "$$FieldInjector$$" + fieldName;
            byte[] helperBytecode = generateFieldInjectorClass(internalName, fieldName, value);

            // Cargar y ejecutar el helper  
            DynamicClassLoader loader = new DynamicClassLoader(clazz.getClassLoader());
            Class<?> helperClass = loader.defineClass(helperClassName, helperBytecode);

            // El helper tiene un método estático inject(Object instance, Object value)  
            java.lang.reflect.Method injectMethod = helperClass.getMethod("inject", Object.class, Object.class);
            AsmCoreUtils.invokeMethod(helperClass, "inject", instance, value);

        } catch (Exception e) {
            throw new RuntimeException("Error inyectando campo " + fieldName + " con ASM", e);
        }
    }

    private static byte[] generateFieldInjectorClass(String targetClassInternal, String fieldName, Object value) {
        String helperClassName = targetClassInternal + "$$FieldInjector$$" + fieldName;

        ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_FRAMES);
        cw.visit(Opcodes.V1_8, Opcodes.ACC_PUBLIC, helperClassName, null, "java/lang/Object", null);

        // Generar método: public static void inject(Object instance, Object value)  
        MethodVisitor mv = cw.visitMethod(
                Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC,
                "inject",
                "(Ljava/lang/Object;Ljava/lang/Object;)V",
                null,
                null
        );

        mv.visitCode();

        // Cast instance al tipo correcto  
        mv.visitVarInsn(Opcodes.ALOAD, 0); // Cargar instance  
        mv.visitTypeInsn(Opcodes.CHECKCAST, targetClassInternal);

        // Cargar value  
        mv.visitVarInsn(Opcodes.ALOAD, 1);

        // Determinar el tipo del campo y hacer cast apropiado  
        String fieldDescriptor = determineFieldDescriptor(value);
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

    private static String determineFieldDescriptor(Object value) {
        if (value == null) {
            return "Ljava/lang/Object;";
        }

        Class<?> valueClass = value.getClass();
        if (valueClass == Integer.class) {
            return "I";
        }
        if (valueClass == Long.class) {
            return "J";
        }
        if (valueClass == Boolean.class) {
            return "Z";
        }
        if (valueClass == Double.class) {
            return "D";
        }
        if (valueClass == Float.class) {
            return "F";
        }
        if (valueClass == Byte.class) {
            return "B";
        }
        if (valueClass == Short.class) {
            return "S";
        }
        if (valueClass == Character.class) {
            return "C";
        }

        return "L" + valueClass.getName().replace('.', '/') + ";";
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

    public static Object createInjectedInstance(String className,
            Map<String, Object> dependencies,
            ClassLoader classLoader) {
        try {
            String injectedClassName = className + "$$WarmupInjected";
            byte[] injectedBytecode = generateInjectedClass(className, dependencies, classLoader);
            return instantiateFromBytecode(injectedClassName, injectedBytecode, classLoader);
        } catch (Exception e) {
            throw new RuntimeException("ASM injection failed for: " + className, e);
        }
    }

    private static byte[] generateInjectedClass(String originalClassName,
            Map<String, Object> dependencies,
            ClassLoader classLoader) throws Exception {
        String injectedClassName = originalClassName + "$$WarmupInjected";
        String classInternalName = injectedClassName.replace('.', '/');
        String originalInternalName = originalClassName.replace('.', '/');

        ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_FRAMES);

        cw.visit(Opcodes.V1_8, Opcodes.ACC_PUBLIC, classInternalName, null,
                originalInternalName, null);

        // Generar constructor que inyecta dependencias
        generateInjectionConstructor(cw, originalInternalName, originalClassName, dependencies, classLoader);

        cw.visitEnd();

        return cw.toByteArray();
    }

    private static void generateInjectionConstructor(ClassWriter cw, String superClass,
            String className, Map<String, Object> dependencies,
            ClassLoader classLoader) {
        Method initMethod = Method.getMethod("void <init> ()");
        GeneratorAdapter mg = new GeneratorAdapter(Opcodes.ACC_PUBLIC, initMethod, null, null, cw);

        mg.visitCode();

        // Llamar al constructor padre
        mg.loadThis();
        mg.invokeConstructor(Type.getObjectType(superClass), initMethod);

        // Inyectar campos @Inject analizando el bytecode
        injectFields(mg, className, dependencies, classLoader);

        mg.returnValue();
        mg.endMethod();
    }

    private static void injectFields(GeneratorAdapter mg, String className,
            Map<String, Object> dependencies, ClassLoader classLoader) {
        try {
            String resourcePath = className.replace('.', '/') + ".class";
            try (java.io.InputStream inputStream = classLoader.getResourceAsStream(resourcePath)) {
                if (inputStream == null) {
                    return;
                }

                byte[] classData = readAllBytes(inputStream);
                ClassReader classReader = new ClassReader(classData);
                FieldInjectionVisitor fieldVisitor = new FieldInjectionVisitor(mg, className, dependencies);
                classReader.accept(fieldVisitor, ClassReader.SKIP_DEBUG | ClassReader.SKIP_FRAMES);
            }
        } catch (IOException e) {
            // Silently continue if field analysis fails
        }
    }

    private static byte[] readAllBytes(java.io.InputStream inputStream) throws java.io.IOException {
        java.io.ByteArrayOutputStream buffer = new java.io.ByteArrayOutputStream();
        int nRead;
        byte[] data = new byte[4096];

        while ((nRead = inputStream.read(data, 0, data.length)) != -1) {
            buffer.write(data, 0, nRead);
        }

        return buffer.toByteArray();
    }

    private static Object instantiateFromBytecode(String className, byte[] bytecode, ClassLoader classLoader) {
        try {
            DynamicClassLoader dynamicLoader = new DynamicClassLoader(classLoader);
            Class<?> clazz = dynamicLoader.defineClass(className, bytecode);
            return AsmCoreUtils.newInstance(clazz);
        } catch (Exception e) {
            throw new RuntimeException("Failed to instantiate injected class: " + className, e);
        }
    }

    // Visitor para analizar campos con anotaciones @Inject
    private static class FieldInjectionVisitor extends ClassVisitor {

        private final GeneratorAdapter methodGenerator;
        private final String className;
        private final Map<String, Object> dependencies;
        private String currentClassInternalName;

        public FieldInjectionVisitor(GeneratorAdapter mg, String className, Map<String, Object> dependencies) {
            super(Opcodes.ASM9);
            this.methodGenerator = mg;
            this.className = className;
            this.dependencies = dependencies;
            this.currentClassInternalName = className.replace('.', '/');
        }

        @Override
        public void visit(int version, int access, String name, String signature,
                String superName, String[] interfaces) {
            this.currentClassInternalName = name;
        }

        @Override
        public FieldVisitor visitField(int access, String name, String descriptor,
                String signature, Object value) {
            return new InjectFieldVisitor(methodGenerator, currentClassInternalName,
                    name, descriptor, dependencies);
        }
    }

    // Visitor para campos individuales
    private static class InjectFieldVisitor extends FieldVisitor {

        private final GeneratorAdapter methodGenerator;
        private final String classInternalName;
        private final String fieldName;
        private final String fieldDescriptor;
        private final Map<String, Object> dependencies;
        private boolean hasInjectAnnotation = false;

        public InjectFieldVisitor(GeneratorAdapter mg, String classInternalName,
                String fieldName, String fieldDescriptor,
                Map<String, Object> dependencies) {
            super(Opcodes.ASM9);
            this.methodGenerator = mg;
            this.classInternalName = classInternalName;
            this.fieldName = fieldName;
            this.fieldDescriptor = fieldDescriptor;
            this.dependencies = dependencies;
        }

        @Override
        public AnnotationVisitor visitAnnotation(String descriptor, boolean visible) {
            // Verificar anotaciones @Inject
            if (JAKARTA_INJECT_DESC.equals(descriptor) || WARMUP_INJECT_DESC.equals(descriptor)) {
                hasInjectAnnotation = true;
            }
            return null;
        }

        @Override
        public void visitEnd() {
            if (hasInjectAnnotation) {
                injectFieldValue();
            }
        }

        private void injectFieldValue() {
            // Buscar dependencia por tipo
            String fieldTypeName = convertDescriptorToClassName(fieldDescriptor);
            Object dependency = findDependencyByType(fieldTypeName);

            if (dependency != null) {
                methodGenerator.loadThis();

                // Cargar la dependencia en el stack basado en su tipo
                loadDependencyValue(methodGenerator, dependency, fieldDescriptor);

                // Asignar al campo
                methodGenerator.putField(Type.getObjectType(classInternalName), fieldName, Type.getType(fieldDescriptor));
            }
        }

        private Object findDependencyByType(String fieldTypeName) {
            // Buscar por nombre de tipo exacto
            Object dependency = dependencies.get(fieldTypeName);
            if (dependency != null) {
                return dependency;
            }

            // Buscar por nombre simple (fallback)
            String simpleName = getSimpleClassName(fieldTypeName);
            for (Map.Entry<String, Object> entry : dependencies.entrySet()) {
                if (getSimpleClassName(entry.getKey()).equals(simpleName)) {
                    return entry.getValue();
                }
            }

            return null;
        }
    }

    // Métodos utilitarios para manejo de tipos y valores
    private static String convertDescriptorToClassName(String descriptor) {
        Type type = Type.getType(descriptor);
        return type.getClassName();
    }

    private static String getSimpleClassName(String className) {
        int lastDot = className.lastIndexOf('.');
        return lastDot >= 0 ? className.substring(lastDot + 1) : className;
    }

    private static void loadDependencyValue(GeneratorAdapter mg, Object value, String descriptor) {
        if (value == null) {
            mg.visitInsn(Opcodes.ACONST_NULL);
            return;
        }

        Type fieldType = Type.getType(descriptor); // CORREGIDO: usar 'descriptor' en lugar de 'Descriptor'
        switch (fieldType.getSort()) {
            case Type.BOOLEAN:
                mg.push((Boolean) value);
                break;
            case Type.CHAR:
                mg.push((Character) value);
                break;
            case Type.BYTE:
                mg.push((Byte) value);
                break;
            case Type.SHORT:
                mg.push((Short) value);
                break;
            case Type.INT:
                mg.push((Integer) value);
                break;
            case Type.FLOAT:
                mg.push((Float) value);
                break;
            case Type.LONG:
                mg.push((Long) value);
                break;
            case Type.DOUBLE:
                mg.push((Double) value);
                break;
            case Type.OBJECT:
            case Type.ARRAY:
                mg.visitLdcInsn(value);
                break;
            default:
                mg.visitInsn(Opcodes.ACONST_NULL);
        }
    }

    // Método adicional para inyección con resolución automática de dependencias
    public static Object createInjectedInstanceWithResolver(String className,
            DependencyResolver resolver,
            ClassLoader classLoader) {
        Map<String, Object> dependencies = resolveDependencies(className, resolver, classLoader);
        return createInjectedInstance(className, dependencies, classLoader);
    }

    private static Map<String, Object> resolveDependencies(String className,
            DependencyResolver resolver,
            ClassLoader classLoader) {
        Map<String, Object> dependencies = new HashMap<>();
        try {
            String resourcePath = className.replace('.', '/') + ".class";
            try (java.io.InputStream inputStream = classLoader.getResourceAsStream(resourcePath)) {
                if (inputStream == null) {
                    return dependencies;
                }

                byte[] classData = readAllBytes(inputStream);
                ClassReader classReader = new ClassReader(classData);
                DependencyResolverVisitor visitor = new DependencyResolverVisitor(resolver);
                classReader.accept(visitor, ClassReader.SKIP_DEBUG | ClassReader.SKIP_FRAMES);
                return visitor.getResolvedDependencies();
            }
        } catch (IOException e) {
            return dependencies;
        }
    }

    // Interface para resolver dependencias
    public interface DependencyResolver {

        Object resolveDependency(String fieldType);
    }

    // Visitor para resolver dependencias
    private static class DependencyResolverVisitor extends ClassVisitor {

        private final DependencyResolver resolver;
        private final Map<String, Object> resolvedDependencies = new HashMap<>();

        public DependencyResolverVisitor(DependencyResolver resolver) {
            super(Opcodes.ASM9);
            this.resolver = resolver;
        }

        @Override
        public FieldVisitor visitField(int access, String name, String descriptor,
                String signature, Object value) {
            return new DependencyFieldVisitor(resolver, resolvedDependencies, descriptor);
        }

        public Map<String, Object> getResolvedDependencies() {
            return resolvedDependencies;
        }
    }

    private static class DependencyFieldVisitor extends FieldVisitor {

        private final DependencyResolver resolver;
        private final Map<String, Object> dependencies;
        private final String fieldDescriptor;
        private boolean hasInjectAnnotation = false;

        public DependencyFieldVisitor(DependencyResolver resolver,
                Map<String, Object> dependencies, String fieldDescriptor) {
            super(Opcodes.ASM9);
            this.resolver = resolver;
            this.dependencies = dependencies;
            this.fieldDescriptor = fieldDescriptor;
        }

        @Override
        public AnnotationVisitor visitAnnotation(String descriptor, boolean visible) {
            if (JAKARTA_INJECT_DESC.equals(descriptor) || WARMUP_INJECT_DESC.equals(descriptor)) {
                hasInjectAnnotation = true;
            }
            return null;
        }

        @Override
        public void visitEnd() {
            if (hasInjectAnnotation) {
                String fieldTypeName = convertDescriptorToClassName(fieldDescriptor);
                Object dependency = resolver.resolveDependency(fieldTypeName);
                if (dependency != null) {
                    dependencies.put(fieldTypeName, dependency);
                }
            }
        }
    }

    // ClassLoader personalizado
    private static class DynamicClassLoader extends ClassLoader {

        public DynamicClassLoader(ClassLoader parent) {
            super(parent);
        }

        public Class<?> defineClass(String name, byte[] bytecode) {
            return defineClass(name, bytecode, 0, bytecode.length);
        }
    }

    public static boolean hasInjectFields(String className, ClassLoader classLoader) {
        try {
            String resourcePath = className.replace('.', '/') + ".class";
            try (java.io.InputStream inputStream = classLoader.getResourceAsStream(resourcePath)) {
                if (inputStream == null) {
                    return false;
                }

                byte[] classData = readAllBytes(inputStream);
                ClassReader classReader = new ClassReader(classData);
                InjectFieldDetector detector = new InjectFieldDetector();
                classReader.accept(detector, ClassReader.SKIP_DEBUG | ClassReader.SKIP_FRAMES);
                return detector.hasInjectFields();
            }
        } catch (IOException e) {
            return false;
        }
    }

    private static class InjectFieldDetector extends ClassVisitor {

        private boolean hasInjectFields = false;

        public InjectFieldDetector() {
            super(Opcodes.ASM9);
        }

        @Override
        public FieldVisitor visitField(int access, String name, String descriptor,
                String signature, Object value) {
            return new FieldVisitor(Opcodes.ASM9) {
                private boolean hasInject = false;

                @Override
                public AnnotationVisitor visitAnnotation(String desc, boolean visible) {
                    if (JAKARTA_INJECT_DESC.equals(desc) || WARMUP_INJECT_DESC.equals(desc)) {
                        hasInject = true;
                        hasInjectFields = true;
                    }
                    return null;
                }
            };
        }

        public boolean hasInjectFields() {
            return hasInjectFields;
        }
    }
}
