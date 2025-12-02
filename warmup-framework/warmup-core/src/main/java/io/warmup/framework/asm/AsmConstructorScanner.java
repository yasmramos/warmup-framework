package io.warmup.framework.asm;

import io.warmup.framework.common.ConstructorMetadata;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import org.objectweb.asm.*;

public class AsmConstructorScanner {

    private static final Map<String, List<ConstructorMetadata>> constructorCache = new ConcurrentHashMap<>();

    public static ConstructorMetadata findInjectableConstructor(String className, ClassLoader classLoader) {
        try {
            String classPath = className.replace('.', '/');
            InputStream classStream = classLoader.getResourceAsStream(classPath + ".class");
            if (classStream == null) {
                throw new IOException("Class resource not found: " + classPath);
            }
            ClassReader cr = new ClassReader(classStream);
            ConstructorFinder visitor = new ConstructorFinder(className);
            cr.accept(visitor, ClassReader.SKIP_DEBUG | ClassReader.SKIP_FRAMES);
            return visitor.getBestConstructor();
        } catch (IOException e) {
            // Log the exception for debugging
            System.err.println("Failed to scan constructors for " + className + ": " + e.getMessage());
            return new ConstructorMetadata(className, new String[0], new String[0], 0, true);
        }
    }

    public static ConstructorMetadata findConstructorWithAnnotation(String className, ClassLoader classLoader,
            String annotationDescriptor) {
        try {
            String classPath = className.replace('.', '/');
            ClassReader cr = new ClassReader(classLoader.getResourceAsStream(classPath + ".class"));
            AnnotatedConstructorFinder visitor = new AnnotatedConstructorFinder(className, annotationDescriptor);
            cr.accept(visitor, ClassReader.SKIP_DEBUG | ClassReader.SKIP_FRAMES);
            return visitor.getAnnotatedConstructor();
        } catch (IOException e) {
            return null;
        }
    }

    public static void clearCache() {
        constructorCache.clear();
    }

    public static void removeFromCache(String className) {
        constructorCache.remove(className);
    }

    public static List<ConstructorMetadata> findAllConstructors(String className, ClassLoader classLoader) {
        return constructorCache.computeIfAbsent(className, key -> {
            try {
                String classPath = className.replace('.', '/');
                ClassReader cr = new ClassReader(classLoader.getResourceAsStream(classPath + ".class"));
                FullConstructorFinder visitor = new FullConstructorFinder(className);
                cr.accept(visitor, ClassReader.SKIP_DEBUG | ClassReader.SKIP_FRAMES);
                return visitor.getAllConstructors();
            } catch (IOException e) {
                return Collections.emptyList();
            }
        });
    }

    // Método para encontrar el constructor con más parámetros (útil para DI)
    public static ConstructorMetadata findMostParameterizedConstructor(String className, ClassLoader classLoader) {
        List<ConstructorMetadata> constructors = findAllConstructors(className, classLoader);

        if (constructors.isEmpty()) {
            return new ConstructorMetadata(className, new String[0], new String[0], 0, true);
        }

        ConstructorMetadata mostParameters = constructors.get(0);
        for (ConstructorMetadata constructor : constructors) {
            if (constructor.parameterCount > mostParameters.parameterCount) {
                mostParameters = constructor;
            }
        }

        return mostParameters;
    }

    // Método para verificar si una clase tiene constructor público sin parámetros
    public static boolean hasPublicNoArgConstructor(String className, ClassLoader classLoader) {
        List<ConstructorMetadata> constructors = findAllConstructors(className, classLoader);

        for (ConstructorMetadata constructor : constructors) {
            if (constructor.isPublic && constructor.parameterCount == 0) {
                return true;
            }
        }

        return false;
    }
}
