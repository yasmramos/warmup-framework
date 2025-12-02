package io.warmup.framework.jit.asm;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

public class AsmDependencyEngine {

    private final Map<Class<?>, Supplier<?>> instanceSuppliers = new ConcurrentHashMap<>();
    private final AsmClassGenerator classGenerator = new AsmClassGenerator();

    public <T> Supplier<T> createInstanceSupplier(Class<T> clazz, Object... dependencies) {
        return classGenerator.createInstanceSupplier(clazz, dependencies);
    }

    public <T> T createInstance(Class<T> clazz, Object... dependencies) {
        try {
            Supplier<?> supplier = instanceSuppliers.computeIfAbsent(clazz,
                    k -> {
                        try {
                            return classGenerator.createInstanceSupplier(clazz, dependencies);
                        } catch (Exception e) {
                            // Retornar un supplier que lance la excepción
                            return () -> {
                                throw new RuntimeException("JIT supplier creation failed", e);
                            };
                        }
                    });

            return clazz.cast(supplier.get());

        } catch (RuntimeException e) {
            //Proporcionar mejor mensaje de error
            if (e.getCause() != null) {
                Throwable cause = e.getCause();
                throw new RuntimeException("Failed to create instance of " + clazz.getName()
                        + ". Reason: " + cause.getMessage(), cause);
            }
            throw new RuntimeException("Failed to create instance: " + clazz.getName(), e);
        }
    }

    //Método para verificar si una clase es compatible con JIT
    public static boolean isJitCompatible(Class<?> clazz) {
        if (clazz.isInterface() || clazz.isAnnotation() || clazz.isEnum()) {
            return false;
        }

        if (java.lang.reflect.Modifier.isAbstract(clazz.getModifiers())) {
            return false;
        }

        // Verificar clases internas no estáticas
        if (clazz.isMemberClass() && !java.lang.reflect.Modifier.isStatic(clazz.getModifiers())) {
            return false;
        }

        try {
            ConstructorFinder.findInjectableConstructor(clazz);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    // Limpiar cache
    public void clearCache() {
        instanceSuppliers.clear();
    }

    //Limpiar clase específica
    public void clearCache(Class<?> clazz) {
        instanceSuppliers.remove(clazz);
    }
}
