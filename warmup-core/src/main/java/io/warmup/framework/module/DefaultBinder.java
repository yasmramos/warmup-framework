package io.warmup.framework.module;

import io.warmup.framework.core.WarmupContainer;
import java.util.ArrayList;
import java.util.List;

public class DefaultBinder implements Binder {

    private final WarmupContainer container;
    private final List<DefaultScopedBindingBuilder<?>> pending = new ArrayList<>();

    public DefaultBinder(WarmupContainer container) {
        this.container = container;
    }

    @Override
    public <T> ScopedBindingBuilder<T> bind(Class<T> type) {
        return bind(type, null);
    }

    @Override
    public <T> ScopedBindingBuilder<T> bind(Class<T> type, String qualifier) {
        DefaultScopedBindingBuilder<T> builder = new DefaultScopedBindingBuilder<>(container, type, qualifier);
        pending.add(builder);
        return builder;
    }

    @Override
    public void install(Module module) {
        if (!module.isEnabled()) {
            return;
        }
        module.configure(this);
        // Aplicar todos los bindings pendientes del módulo instalado
        applyPendingBindings();
    }

    /**
     * Aplica todos los bindings pendientes y limpia la lista
     */
    public void applyPendingBindings() {
        for (DefaultScopedBindingBuilder<?> builder : pending) {
            builder.apply();
        }
        pending.clear();
    }

    @Override
    public WarmupContainer container() {
        return container;
    }

    @Override
    public <T> void requireBinding(Class<T> type) {
        requireBinding(type, null);
    }

    @Override
    public <T> void requireBinding(Class<T> type, String name) {
        if (!container.hasBinding(type, name)) {
            String bindingInfo = name != null
                    ? type.getSimpleName() + " with name '" + name + "'"
                    : type.getSimpleName();
            throw new IllegalStateException("Required binding not found: " + bindingInfo);
        }
    }

    /**
     * Clase interna para construir bindings con scope
     */
    private static class DefaultScopedBindingBuilder<T> implements ScopedBindingBuilder<T> {

        private final WarmupContainer container;
        private final Class<T> type;
        private final String qualifier;

        private Class<? extends T> implementation;
        private T instance;
        private boolean singleton = false;

        public DefaultScopedBindingBuilder(WarmupContainer container, Class<T> type, String qualifier) {
            this.container = container;
            this.type = type;
            this.qualifier = qualifier;
        }

        @Override
        public ScopedBindingBuilder<T> to(Class<? extends T> implementation) {
            this.implementation = implementation;
            return this;
        }

        @Override
        public ScopedBindingBuilder<T> toInstance(T instance) {
            this.instance = instance;
            return this;
        }

        @Override
        public ScopedBindingBuilder<T> toProvider(Provider<? extends T> provider) {
            // Para simplificar, tratamos el provider como una instancia
            // En una implementación más completa, necesitarías soporte específico para providers
            this.instance = provider.get();
            return this;
        }

        @Override
        public ScopedBindingBuilder<T> annotatedWith(String qualifier) {
            // En esta implementación, el qualifier se pasa en el constructor
            // Podrías lanzar una excepción o implementar lógica adicional si es necesario
            throw new UnsupportedOperationException("Qualifier should be specified in bind() method");
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

        /**
         * Aplica el binding al container
         */
        public void apply() {
            if (instance != null) {
                if (qualifier != null) {
                    container.registerBean(qualifier, type, instance);
                } else {
                    container.registerBean(type.getSimpleName().toLowerCase(), type, instance);
                }
            } else if (implementation != null) {
                if (qualifier != null) {
                    container.registerImplementation(type, implementation, singleton);
                } else {
                    if (type.isInterface()) {
                        container.registerImplementation(type, implementation, singleton);
                    } else {
                        container.register(implementation, singleton);
                    }
                }
            } else {
                // Binding sin implementación - registrar el tipo como self
                if (qualifier != null) {
                    container.registerBean(qualifier, type, null);
                } else {
                    container.register(type, singleton);
                }
            }
        }
    }
}
