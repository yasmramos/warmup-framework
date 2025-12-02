package io.warmup.framework.proxy;

@FunctionalInterface
public interface InvocationHandler {

    Object invoke(Object proxy, String methodName, Object[] args) throws Throwable;
}
