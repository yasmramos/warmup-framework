package io.warmup.framework.proxy;

@FunctionalInterface
public interface MethodCall {

    Object call() throws Throwable;
}
