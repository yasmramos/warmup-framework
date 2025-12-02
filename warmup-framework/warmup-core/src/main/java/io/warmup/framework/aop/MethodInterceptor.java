package io.warmup.framework.aop;

/**
 * Interfaz para interceptar métodos*
 */
public interface MethodInterceptor {

    Object invoke(final ProceedingJoinPoint joinPoint) throws Throwable;
}
