package io.warmup.framework.module;

import io.warmup.framework.core.WarmupContainer;

public interface Binder {

    WarmupContainer container();

    <T> ScopedBindingBuilder<T> bind(Class<T> type);

    <T> ScopedBindingBuilder<T> bind(Class<T> type, String name);

    void install(Module module);

    <T> void requireBinding(Class<T> type);

    <T> void requireBinding(Class<T> type, String name);
}
