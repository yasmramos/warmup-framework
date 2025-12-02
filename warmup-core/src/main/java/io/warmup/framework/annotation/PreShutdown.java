package io.warmup.framework.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Anotación para métodos que deben ejecutarse antes del shutdown Se ejecuta
 * antes de los métodos @PreDestroy
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface PreShutdown {

    /**
     * Descripción opcional del método
     */
    String value() default "";

    /**
     * Orden de ejecución (menor número primero)
     */
    int order() default 0;

    /**
     * Tiempo máximo de ejecución en milisegundos 0 = sin timeout
     */
    long timeout() default 0;
}
