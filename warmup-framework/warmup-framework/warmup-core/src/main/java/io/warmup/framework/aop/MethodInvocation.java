package io.warmup.framework.aop;

@FunctionalInterface
public interface MethodInvocation {

    Object proceed() throws Throwable;
}
