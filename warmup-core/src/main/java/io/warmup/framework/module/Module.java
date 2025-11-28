package io.warmup.framework.module;

public interface Module {

    default String getName() {
        return this.getClass().getSimpleName();
    }

    default int getOrder() {
        return 0;
    }

    default boolean isEnabled() {
        return true;
    }

    void configure(Binder binder);

    default void shutdown() throws Exception {
        /* no-op */ }
}
