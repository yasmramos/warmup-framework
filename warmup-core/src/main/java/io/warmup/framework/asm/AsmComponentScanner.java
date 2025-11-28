package io.warmup.framework.asm;

import io.warmup.framework.common.ClassMetadata;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import org.objectweb.asm.*;

public class AsmComponentScanner {

    private static final String COMPONENT_DESC = "Lio/warmup/framework/annotation/Component;";
    private static final String NAMED_DESC = "Lio/warmup/framework/annotation/Named;";
    private static final String PROFILE_DESC = "Lio/warmup/framework/annotation/Profile;";
    private static final String LAZY_DESC = "Lio/warmup/framework/annotation/Lazy;";
    private static final String ASYNC_DESC = "Lio/warmup/framework/annotation/Async;";

    public static ClassMetadata processClassWithAsm(String className, ClassLoader classLoader) {
        try {
            String resourcePath = className.replace('.', '/') + ".class";
            try (InputStream inputStream = classLoader.getResourceAsStream(resourcePath)) {
                if (inputStream == null) {
                    System.out.println("[ASM DEBUG] Could not find resource: " + resourcePath);
                    return null;
                }

                byte[] classData = readAllBytes(inputStream);
                ClassReader classReader = new ClassReader(classData);
                ClassMetadataCollector collector = new ClassMetadataCollector(className);
                classReader.accept(collector, ClassReader.SKIP_DEBUG | ClassReader.SKIP_FRAMES);

                ClassMetadata metadata = collector.getMetadata();
                System.out.println("[ASM DEBUG] Processed class: " + className + ", isComponent: " + metadata.isComponent);
                return metadata;
            }
        } catch (IOException e) {
            System.out.println("[ASM DEBUG] IOException for class " + className + ": " + e.getMessage());
            return null;
        } catch (Exception e) {
            System.out.println("[ASM DEBUG] Exception for class " + className + ": " + e.getMessage());
            // Fallback silencioso
            return null;
        }
    }

    private static byte[] readAllBytes(InputStream inputStream) throws IOException {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        int nRead;
        byte[] data = new byte[4096];

        while ((nRead = inputStream.read(data, 0, data.length)) != -1) {
            buffer.write(data, 0, nRead);
        }

        return buffer.toByteArray();
    }

    private static class ClassMetadataCollector extends ClassVisitor {

        private final ClassMetadata metadata;
        private String currentClassName;

        public ClassMetadataCollector(String className) {
            super(Opcodes.ASM9);
            this.metadata = new ClassMetadata();
            this.metadata.className = className;
            this.currentClassName = className;
        }

        @Override
        public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
            this.currentClassName = name.replace('/', '.');

            // Registrar superclase
            if (superName != null && !"java/lang/Object".equals(superName)) {
                metadata.superClassName = superName.replace('/', '.');
            }

            // Registrar interfaces
            if (interfaces != null) {
                for (String iface : interfaces) {
                    metadata.interfaces.add(iface.replace('/', '.'));
                }
            }
        }

        @Override
        public AnnotationVisitor visitAnnotation(String descriptor, boolean visible) {
            System.out.println("[ASM DEBUG] Checking annotation: " + descriptor + " (visible: " + visible + ")");
            System.out.println("[ASM DEBUG] COMPONENT_DESC is: " + COMPONENT_DESC);
            
            // Verificar anotación @Component
            if (COMPONENT_DESC.equals(descriptor)) {
                System.out.println("[ASM DEBUG] Found @Component annotation!");
                metadata.isComponent = true;
                return new ComponentAnnotationVisitor();
            }

            // Verificar anotación @Named
            if (NAMED_DESC.equals(descriptor)) {
                return new NamedAnnotationVisitor();
            }

            // Verificar anotación @Profile
            if (PROFILE_DESC.equals(descriptor)) {
                return new ProfileAnnotationVisitor();
            }

            // Verificar anotación @Lazy
            if (LAZY_DESC.equals(descriptor)) {
                metadata.isLazy = true;
            }

            return null;
        }

        @Override
        public MethodVisitor visitMethod(int access, String name, String descriptor, String signature, String[] exceptions) {
            // Buscar métodos con anotaciones @Async
            if ((access & Opcodes.ACC_PUBLIC) != 0 && !name.startsWith("<")) {
                return new AsyncMethodVisitor();
            }
            return null;
        }

        @Override
        public FieldVisitor visitField(int access, String name, String descriptor, String signature, Object value) {
            // Opcional: procesar anotaciones en campos si es necesario
            return null;
        }

        public ClassMetadata getMetadata() {
            return metadata;
        }

        private class ComponentAnnotationVisitor extends AnnotationVisitor {

            public ComponentAnnotationVisitor() {
                super(Opcodes.ASM9);
            }

            @Override
            public void visit(String name, Object value) {
                if ("singleton".equals(name)) {
                    metadata.isSingleton = Boolean.TRUE.equals(value);
                }
            }
        }

        private class NamedAnnotationVisitor extends AnnotationVisitor {

            public NamedAnnotationVisitor() {
                super(Opcodes.ASM9);
            }

            @Override
            public void visit(String name, Object value) {
                if ("value".equals(name) && value instanceof String) {
                    metadata.namedValue = (String) value;
                }
            }
        }

        private class ProfileAnnotationVisitor extends AnnotationVisitor {

            public ProfileAnnotationVisitor() {
                super(Opcodes.ASM9);
            }

            @Override
            public void visit(String name, Object value) {
                if ("value".equals(name)) {
                    if (value instanceof String[]) {
                        String[] profileArray = (String[]) value;
                        Collections.addAll(metadata.profiles, profileArray);
                    } else if (value instanceof String) {
                        metadata.profiles.add((String) value);
                    }
                }
            }
        }

        private class AsyncMethodVisitor extends MethodVisitor {

            public AsyncMethodVisitor() {
                super(Opcodes.ASM9);
            }

            @Override
            public AnnotationVisitor visitAnnotation(String descriptor, boolean visible) {
                // Detectar anotación @Async específicamente
                if (ASYNC_DESC.equals(descriptor)) {
                    metadata.hasAsyncMethods = true;
                }
                return null;
            }
        }
    }

    // Método auxiliar para escanear múltiples clases
    public List<ClassMetadata> scanPackages(Set<String> packageNames, ClassLoader classLoader) {
        List<ClassMetadata> results = new ArrayList<>();

        for (String packageName : packageNames) {
            List<String> classNames = findClassesInPackage(packageName, classLoader);
            for (String className : classNames) {
                ClassMetadata metadata = processClassWithAsm(className, classLoader);
                if (metadata != null && metadata.isComponent) {
                    results.add(metadata);
                }
            }
        }

        return results;
    }

    // Método para encontrar clases en un paquete (sin usar reflexión)
    private List<String> findClassesInPackage(String packageName, ClassLoader classLoader) {
        List<String> classNames = new ArrayList<>();
        String path = packageName.replace('.', '/');

        try {
            java.net.URL resource = classLoader.getResource(path);
            if (resource != null) {
                java.io.File directory = new java.io.File(resource.getFile());
                if (directory.exists()) {
                    findClassesInDirectory(packageName, directory, classNames);
                }
            }
        } catch (Exception e) {
            // Manejo silencioso de errores
        }

        return classNames;
    }

    private void findClassesInDirectory(String packageName, File directory, List<String> classNames) {
        File[] files = directory.listFiles();
        if (files == null) {
            return;
        }

        for (File file : files) {
            if (file.isDirectory()) {
                findClassesInDirectory(packageName + "." + file.getName(), file, classNames);
            } else if (file.getName().endsWith(".class")) {
                String className = packageName + '.' + file.getName().substring(0, file.getName().length() - 6);
                classNames.add(className);
            }
        }
    }
}
