package io.warmup.framework.aop;

import io.warmup.framework.core.WarmupContainer;
import java.util.logging.Logger;

public class AspectDecorator {

    private static final Logger log = Logger.getLogger(AspectDecorator.class.getName());

    @SuppressWarnings("unchecked")
    public static <T> T createDecorator(T target, Class<T> targetClass, WarmupContainer container) {
        return AspectProxyASM.createProxy(target, targetClass, container);
    }

    /**
     * Método específico para crear decoradores cuando el tipo no es conocido exactamente.
     */
    @SuppressWarnings("unchecked")
    public static Object createDecoratorForObject(Object target, Class<?> targetClass, WarmupContainer container) {
        return AspectProxyASM.createProxyForObject(target, targetClass, container);
    }
}
