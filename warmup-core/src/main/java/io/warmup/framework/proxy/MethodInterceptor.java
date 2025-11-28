package io.warmup.framework.proxy;

import java.lang.reflect.Method;

@FunctionalInterface
public interface MethodInterceptor {

    Object intercept(Object obj, Method method, Object[] args, MethodCall superCall) throws Throwable;
}
