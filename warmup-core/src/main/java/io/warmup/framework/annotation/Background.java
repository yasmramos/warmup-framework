package io.warmup.framework.annotation;

import java.lang.annotation.*;

/**
 * Anotación para marcar componentes que se ejecutan en background.
 * Los componentes de background no bloquean el startup principal
 * y se pueden inicializar de forma asíncrona.
 * 
 * @author MiniMax Agent
 * @version 1.0
 */
@Target({ElementType.TYPE, ElementType.METHOD, ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Background {
    
    /**
     * Tipo de componente de background
     * @return tipo de background
     */
    BackgroundType type() default BackgroundType.GENERAL;
    
    /**
     * Prioridad dentro de los componentes de background
     * @return prioridad
     */
    int priority() default 0;
    
    /**
     * Tiempo estimado de inicialización en milisegundos
     * @return tiempo estimado
     */
    long estimatedTime() default 100;
    
    /**
     * Si debe ejecutarse en un thread separado
     * @return true si debe ejecutarse en thread separado
     */
    boolean separateThread() default true;
    
    /**
     * Si es opcional para el startup
     * @return true si es opcional
     */
    boolean optional() default false;
    
    /**
     * Tipos de componentes de background disponibles
     */
    enum BackgroundType {
        GENERAL,
        MONITORING,
        CLEANUP,
        ANALYTICS,
        BACKUP,
        MAINTENANCE,
        ASYNC_TASK,
        SCHEDULED_TASK
    }
}