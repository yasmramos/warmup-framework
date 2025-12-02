package io.warmup.framework.module;

import io.warmup.framework.core.WarmupContainer;
import java.util.logging.Logger;

class DefaultScopedBindingBuilder<T> implements ScopedBindingBuilder<T> {

    private static final Logger log = Logger.getLogger(DefaultScopedBindingBuilder.class.getName());

    private final WarmupContainer container;
    private final Class<T> type;
    private String qualifier;
    private boolean singleton = false;
    private Class<? extends T> implType;
    private T instance;
    private Provider<? extends T> provider;

    DefaultScopedBindingBuilder(WarmupContainer container, Class<T> type, String qualifier) {
        this.container = container;
        this.type = type;
        this.qualifier = qualifier;
    }

    @Override
    public ScopedBindingBuilder<T> annotatedWith(String name) {
        this.qualifier = name;
        return this;
    }

    @Override
    public ScopedBindingBuilder<T> to(Class<? extends T> impl) {
        this.implType = impl;
        return this;
    }

    @Override
    public ScopedBindingBuilder<T> toInstance(T inst) {
        this.instance = inst;
        return this;
    }

    @Override
    public ScopedBindingBuilder<T> toProvider(Provider<? extends T> provider) {
        this.provider = provider;
        return this;
    }

    @Override
    public ScopedBindingBuilder<T> asSingleton() {
        this.singleton = true;
        return this;
    }

    @Override
    public void done() {
        apply();
    }

    /* aplicar el binding (se llama desde DefaultBinder si no usás done()) */
    void apply() {
        if (provider != null) {
            // registrar el provider como bean
            if (qualifier == null) {
                container.registerBean(type.getSimpleName(), type, provider.get());
            } else {
                container.registerBean(qualifier, type, provider.get());
            }
            return;
        }
        if (instance != null) {
            if (qualifier == null) {
                container.registerBean(type.getSimpleName().toLowerCase(), type, instance);
            } else {
                container.registerBean(qualifier, type, instance);
            }
            return;
        }
        if (implType == null) {
            throw new IllegalStateException("No se especificó impl ni instance para " + type);
        }
        if (qualifier == null) {
            container.registerImplementation(type, implType, singleton);
        } else {
            container.registerImplementation(type, implType, singleton);
        }
    }
}
