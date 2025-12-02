package io.warmup.framework.module;

public interface ScopedBindingBuilder<T> {

    ScopedBindingBuilder<T> annotatedWith(String name);

    ScopedBindingBuilder<T> to(Class<? extends T> impl);

    ScopedBindingBuilder<T> toInstance(T instance);

    ScopedBindingBuilder<T> toProvider(Provider<? extends T> provider);

    ScopedBindingBuilder<T> asSingleton();

    void done(); // terminal opcional
}
