package io.warmup.framework.core.plugin;

import io.warmup.framework.asm.AsmCoreUtils;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.stream.Collectors;
import java.util.Collections;
import java.util.stream.Collectors;
import java.util.List;
import java.util.stream.Collectors;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public final class ExtensionRegistry {

    private final Map<String, List<Extension>> points = new ConcurrentHashMap<>();

    public void addExtension(Extension ext) {
        points.computeIfAbsent(ext.extensionPoint(), k -> new ArrayList<>()).add(ext);
    }

    public <T> List<T> getExtensions(String point, Class<T> type) {
        return points.getOrDefault(point, Collections.emptyList()).stream()
                .filter(e -> type.isAssignableFrom(e.type()))
                .map(e -> createInstance(e.type(), type))
                .filter(instance -> instance != null)
                .collect(Collectors.toList());
    }

    @SuppressWarnings("unchecked")
    private <T> T createInstance(Class<?> implementationType, Class<T> expectedType) {
        try {
            // ✅ FASE 6: Invocación progresiva del constructor - ASM → MethodHandle → Reflection
            Object instance = AsmCoreUtils.invokeConstructorProgressive(implementationType.getDeclaredConstructor());
            if (expectedType.isInstance(instance)) {
                return (T) instance;
            } else {
                System.err.println("Created instance of " + implementationType.getName()
                        + " is not assignable to " + expectedType.getName());
                return null;
            }
        } catch (Throwable e) {
            System.err.println("Failed to create instance of " + implementationType.getName()
                    + ": " + e.getMessage());
            return null;
        }
    }

    /**
     * Versión alternativa que lanza excepción en lugar de retornar null
     */
    public <T> List<T> getExtensionsOrThrow(String point, Class<T> type) {
        return points.getOrDefault(point, Collections.emptyList()).stream()
                .filter(e -> type.isAssignableFrom(e.type()))
                .map(e -> {
                    try {
                        // ✅ FASE 6: Invocación progresiva del constructor - ASM → MethodHandle → Reflection
                        Object instance = AsmCoreUtils.invokeConstructorProgressive(e.type().getDeclaredConstructor());
                        return type.cast(instance);
                    } catch (Throwable ex) {
                        throw new RuntimeException("Failed to create extension instance: "
                                + e.type().getName(), ex);
                    }
                })
                .collect(Collectors.toList());
    }

    /**
     * Obtiene extensiones filtradas por una anotación específica
     */
    public <T, A extends Annotation> List<T> getExtensionsWithAnnotation(String point,
            Class<T> type, Class<A> annotationClass) {  // More type-safe
        return points.getOrDefault(point, Collections.emptyList()).stream()
                .filter(e -> type.isAssignableFrom(e.type()))
                .filter(e -> e.type().isAnnotationPresent(annotationClass))
                .map(e -> createInstance(e.type(), type))
                .filter(instance -> instance != null)
                .collect(Collectors.toList());
    }

    /**
     * Verifica si hay extensiones disponibles para un punto específico
     */
    public boolean hasExtensions(String point) {
        return points.containsKey(point) && !points.get(point).isEmpty();
    }

    /**
     * Verifica si hay extensiones de un tipo específico disponibles
     */
    public <T> boolean hasExtensions(String point, Class<T> type) {
        return points.containsKey(point)
                && points.get(point).stream()
                        .anyMatch(e -> type.isAssignableFrom(e.type()));
    }

    /**
     * Elimina todas las extensiones de un punto específico
     */
    public void removeExtensions(String point) {
        points.remove(point);
    }

    /**
     * Elimina una extensión específica
     */
    public boolean removeExtension(Extension ext) {
        List<Extension> extensions = points.get(ext.extensionPoint());
        if (extensions != null) {
            return extensions.remove(ext);
        }
        return false;
    }

    /**
     * Obtiene todos los puntos de extensión registrados
     */
    public List<String> getExtensionPoints() {
        return new ArrayList<>(points.keySet());
    }

    /**
     * Obtiene el número de extensiones para un punto específico
     */
    public int getExtensionCount(String point) {
        List<Extension> extensions = points.get(point);
        return extensions != null ? extensions.size() : 0;
    }

    /**
     * Limpia todo el registro
     */
    public void clear() {
        points.clear();
    }
}
