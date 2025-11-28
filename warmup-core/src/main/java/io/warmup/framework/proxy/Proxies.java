package io.warmup.framework.proxy;

public final class Proxies {

    private Proxies() {
    }

    @SuppressWarnings("unchecked")
    public static <T> T createProxy(Class<T> targetClass, InvocationHandler handler) {
        return (T) ProxyBuilder.createProxy(targetClass)
                .intercept(handler)
                .build();
    }

    @SuppressWarnings("unchecked")
    public static <T> T createSubclassProxy(Class<T> superClass, InvocationHandler handler) {
        return (T) ProxyBuilder.createSubclass(superClass)
                .intercept(handler)
                .build();
    }

    public static Object createInterfaceProxy(InvocationHandler handler, Class<?>... interfaces) {
        return ProxyBuilder.createInterfaceProxy(interfaces)
                .intercept(handler)
                .build();
    }
}
