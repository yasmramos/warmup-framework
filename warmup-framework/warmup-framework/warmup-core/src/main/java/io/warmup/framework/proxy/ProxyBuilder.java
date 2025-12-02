package io.warmup.framework.proxy;

public final class ProxyBuilder<T> {

    private final ProxyGenerator generator;
    private final Class<T> targetClass;

    private ProxyBuilder(Class<T> targetClass) {
        this.targetClass = targetClass;
        this.generator = new ProxyGenerator(this.targetClass);
    }

    public static <T> ProxyBuilder<T> createProxy(Class<T> targetClass) {
        return new ProxyBuilder<>(targetClass);
    }

    public static <T> ProxyBuilder<T> createSubclass(Class<T> superClass) {
        return new ProxyBuilder<>(superClass).extend(superClass);
    }

    public static ProxyBuilder<Object> createInterfaceProxy(Class<?>... interfaces) {
        return new ProxyBuilder<>(Object.class).implement(interfaces);
    }

    public ProxyBuilder<T> intercept(InvocationHandler handler) {
        generator.intercept(handler);
        return this;
    }

    public ProxyBuilder<T> implement(Class<?>... interfaces) {
        generator.implement(interfaces);
        return this;
    }

    public ProxyBuilder<T> extend(Class<?> superClass) {
        generator.extend(superClass);
        return this;
    }

    public ProxyBuilder<T> name(String name) {
        generator.name(name);
        return this;
    }

    public ProxyBuilder<T> callSuperConstructor(boolean flag) {
        generator.callSuperConstructor(flag);
        return this;
    }

    @SuppressWarnings("unchecked")
    public T build() {
        return (T) generator.build();
    }

    @SuppressWarnings("unchecked")
    public Class<T> buildClass() {
        return generator.buildClass();
    }
}
