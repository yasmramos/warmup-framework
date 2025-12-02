package io.warmup.framework.cache;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;
import java.util.stream.Collectors;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Cache para operaciones de reflexión costosas
 */
public class ReflectionCache {

    private final Map<Class<?>, Constructor<?>> injectConstructors = new ConcurrentHashMap<>();
    private final Map<Class<?>, List<Field>> injectFields = new ConcurrentHashMap<>();
    private final Map<Class<?>, List<Method>> postConstructMethods = new ConcurrentHashMap<>();
    private final Map<Class<?>, List<Method>> preDestroyMethods = new ConcurrentHashMap<>();
    private final Map<Class<?>, Annotation[][]> parameterAnnotations = new ConcurrentHashMap<>();

    public Constructor<?> getInjectConstructor(Class<?> clazz) {
        return injectConstructors.computeIfAbsent(clazz, this::findInjectConstructor);
    }

    public List<Field> getInjectFields(Class<?> clazz) {
        return injectFields.computeIfAbsent(clazz, this::findInjectFields);
    }

    public List<Method> getPostConstructMethods(Class<?> clazz) {
        return postConstructMethods.computeIfAbsent(clazz, this::findPostConstructMethods);
    }

    public List<Method> getPreDestroyMethods(Class<?> clazz) {
        return preDestroyMethods.computeIfAbsent(clazz, this::findPreDestroyMethods);
    }

    public Annotation[][] getParameterAnnotations(Constructor<?> constructor) {
        return parameterAnnotations.computeIfAbsent(constructor.getDeclaringClass(),
                clazz -> constructor.getParameterAnnotations());
    }

    private Constructor<?> findInjectConstructor(Class<?> clazz) {
        Constructor<?>[] constructors = clazz.getDeclaredConstructors();

        // Buscar constructor con @Inject
        List<Constructor<?>> injectConstructors = Arrays.stream(constructors)
                .filter(c -> c.isAnnotationPresent(io.warmup.framework.annotation.Inject.class))
                .collect(Collectors.toList());

        if (injectConstructors.size() == 1) {
            Constructor<?> constructor = injectConstructors.get(0);
            constructor.setAccessible(true);
            return constructor;
        }

        if (injectConstructors.size() > 1) {
            throw new IllegalArgumentException(
                    "Múltiples constructores con @Inject encontrados en " + clazz.getName() + ". Solo debe haber uno."
            );
        }

        // Si no hay @Inject, buscar constructor sin parámetros
        List<Constructor<?>> noArgConstructors = Arrays.stream(constructors)
                .filter(c -> c.getParameterCount() == 0)
                .collect(Collectors.toList());

        if (noArgConstructors.size() == 1) {
            Constructor<?> constructor = noArgConstructors.get(0);
            constructor.setAccessible(true);
            return constructor;
        }

        // Si hay exactamente un constructor, usarlo
        if (constructors.length == 1) {
            Constructor<?> constructor = constructors[0];
            constructor.setAccessible(true);
            return constructor;
        }

        throw new IllegalArgumentException(
                "Múltiples constructores encontrados en " + clazz.getName()
                + ". Debe anotar uno con @Inject o proveer un constructor sin parámetros."
        );
    }

    private List<Field> findInjectFields(Class<?> clazz) {
        List<Field> fields = new ArrayList<>();
        Class<?> currentClass = clazz;

        while (currentClass != null) {
            for (Field field : currentClass.getDeclaredFields()) {
                if (field.isAnnotationPresent(io.warmup.framework.annotation.Inject.class)
                        || field.isAnnotationPresent(io.warmup.framework.annotation.Value.class)) {
                    field.setAccessible(true);
                    fields.add(field);
                }
            }
            currentClass = currentClass.getSuperclass();
        }

        return Collections.unmodifiableList(fields);
    }

    private List<Method> findPostConstructMethods(Class<?> clazz) {
        return findLifecycleMethods(clazz, io.warmup.framework.annotation.PostConstruct.class);
    }

    private List<Method> findPreDestroyMethods(Class<?> clazz) {
        return findLifecycleMethods(clazz, io.warmup.framework.annotation.PreDestroy.class);
    }

    private List<Method> findLifecycleMethods(Class<?> clazz, Class<? extends java.lang.annotation.Annotation> annotation) {
        List<Method> methods = new ArrayList<>();
        Class<?> currentClass = clazz;

        while (currentClass != null) {
            for (Method method : currentClass.getDeclaredMethods()) {
                if (method.isAnnotationPresent(annotation)) {
                    if (method.getParameterCount() == 0 && method.getReturnType() == void.class) {
                        method.setAccessible(true);
                        methods.add(method);
                    }
                }
            }
            currentClass = currentClass.getSuperclass();
        }

        return Collections.unmodifiableList(methods);
    }

    /**
     * Limpiar cache (útil para testing y hot reload)
     */
    public void clear() {
        injectConstructors.clear();
        injectFields.clear();
        postConstructMethods.clear();
        preDestroyMethods.clear();
        parameterAnnotations.clear();
    }
}