package io.warmup.framework.module;

import io.warmup.framework.annotation.Factory;
import io.warmup.framework.annotation.Named;
import io.warmup.framework.annotation.Provides;
import io.warmup.framework.annotation.Qualifier;
import io.warmup.framework.annotation.Value;
import io.warmup.framework.asm.AsmCoreUtils;
import io.warmup.framework.core.Convert;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Objects;

public abstract class AbstractModule implements Module {

    private Binder binder;

    @Override
    public final void configure(Binder binder) {
        if (this.binder != null) {
            throw new IllegalStateException("Re-entry is not allowed.");
        }
        this.binder = Objects.requireNonNull(binder, "binder");
        try {
            configure();
            registerProvidesMethods();
            registerFactoryClasses();
        } finally {
            this.binder = null;
        }
    }

    /* ---------- método protegido que vos sobrescribís ---------- */
    protected void configure() {
        /* no-op */ }

    /* ---------- registro de @Provides methods ---------- */
    private void registerProvidesMethods() {
        for (Method m : getClass().getDeclaredMethods()) {
            if (m.isAnnotationPresent(Provides.class)) {
                registerProvidesMethod(m);
            }
        }
    }

    private void registerProvidesMethod(Method m) {
        registerProvidesMethod(m, this); // <-- usa "this" por defecto
    }

    @SuppressWarnings("unchecked")
    private <T> void registerProvidesMethod(Method m, Object instance) {
        Class<T> rawType = (Class<T>) m.getReturnType();

        T provided = new Provider<T>() {
            @Override
            public T get() {
                try {
                    Object[] args = resolveProvidesParameters(m);
                    m.setAccessible(true);
                    // ✅ FASE 6: Invocación progresiva del método - ASM → MethodHandle → Reflection
                    Object result = AsmCoreUtils.invokeMethodObjectProgressive(m, instance, args);

                    // ✅ Manejar tipos primitivos
                    if (rawType.isPrimitive()) {
                        return (T) result; // Esto debería funcionar para autoboxing
                    } else {
                        return rawType.cast(result);
                    }
                } catch (Throwable e) {
                    throw new RuntimeException("Error en @Provides " + m, e);
                }
            }
        }.get();

        String name = extractName(m);
        boolean singleton = m.isAnnotationPresent(jakarta.inject.Singleton.class)
                || m.isAnnotationPresent(io.warmup.framework.annotation.Singleton.class);

        if (name == null) {
            binder().container().register(rawType, singleton);
        } else {
            binder().container().registerNamed(rawType, name, singleton);
            binder().container().getNamedBeans().put(name, provided);
        }
    }

    /* ---------- registro de clases @Factory ---------- */
    private void registerFactoryClasses() {
        for (Class<?> clazz : getClass().getDeclaredClasses()) {
            if (clazz.isAnnotationPresent(Factory.class)) {
                registerFactory(clazz);
            }
        }
    }

    private void registerFactory(Class<?> factoryClass) {
        try {
            // ✅ FASE 6: Invocación progresiva del constructor - ASM → MethodHandle → Reflection
            Object factory = AsmCoreUtils.invokeConstructorProgressive(factoryClass.getDeclaredConstructor());
            for (Method m : factoryClass.getDeclaredMethods()) {
                if (m.isAnnotationPresent(Provides.class)) {
                    registerProvidesMethod(m, factory);
                }
            }
        } catch (Throwable e) {
            throw new RuntimeException("Error creando @Factory " + factoryClass.getName(), e);
        }
    }

    /* ---------- resolución de parámetros de @Provides ---------- */
    private Object[] resolveProvidesParameters(Method m) throws Exception {
        Class<?>[] types = m.getParameterTypes();
        Object[] args = new Object[types.length];
        for (int i = 0; i < types.length; i++) {
            String name = extractQualifier(m, i);
            if (name != null) {
                args[i] = binder().container().getNamed(types[i], name);
            } else if (m.getParameters()[i].isAnnotationPresent(Value.class)) {
                String expr = AsmCoreUtils.getAnnotationProgressive(m.getParameters()[i], Value.class).value();
                args[i] = Convert.convertStringToType(binder().container().resolvePropertyValue(expr), types[i]);
            } else {
                args[i] = binder().container().get(types[i]);
            }
        }
        return args;
    }

    /* ---------- helpers ---------- */
    private String extractName(Method m) {
        if (m.isAnnotationPresent(Named.class)) {
            return AsmCoreUtils.getAnnotationProgressive(m, Named.class).value();
        }
        if (m.isAnnotationPresent(Qualifier.class)) {
            return AsmCoreUtils.getAnnotationProgressive(m, Qualifier.class).value();
        }
        return null;
    }

    private String extractQualifier(Method m, int paramIndex) {
        java.lang.reflect.Parameter p = m.getParameters()[paramIndex];
        if (p.isAnnotationPresent(Named.class)) {
            return AsmCoreUtils.getAnnotationProgressive(p, Named.class).value();
        }
        if (p.isAnnotationPresent(Qualifier.class)) {
            return AsmCoreUtils.getAnnotationProgressive(p, Qualifier.class).value();
        }
        return null;
    }

    /* ---------- métodos protegidos que vos usás ---------- */
    protected final Binder binder() {
        if (binder == null) {
            throw new IllegalStateException("binder() solo dentro de configure()");
        }
        return binder;
    }

    protected final <T> ScopedBindingBuilder<T> bind(Class<T> type) {
        return bind(type, null);
    }

    protected final <T> ScopedBindingBuilder<T> bind(Class<T> type, String name) {
        return binder().bind(type, name);
    }

    protected final void install(Module module) {
        binder().install(module);
    }
}
