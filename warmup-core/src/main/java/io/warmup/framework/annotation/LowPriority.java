package io.warmup.framework.annotation;

import java.lang.annotation.*;

/**
 * Anotación para marcar componentes de baja prioridad.
 * Los componentes de baja prioridad se inicializan al final del proceso
 * y pueden ser lazy-loaded si es necesario.
 * 
 * @author MiniMax Agent
 * @version 1.0
 */
@Target({ElementType.TYPE, ElementType.METHOD, ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface LowPriority {
    
    /**
     * Nivel de prioridad específico
     * @return nivel de prioridad
     */
    PriorityLevel level() default PriorityLevel.LOW;
    
    /**
     * Tiempo estimado de inicialización en milisegundos
     * @return tiempo estimado
     */
    long estimatedTime() default 50;
    
    /**
     * Si permite lazy loading
     * @return true si permite lazy loading
     */
    boolean lazyLoadable() default true;
    
    /**
     * Si puede ejecutarse en background
     * @return true si puede ejecutarse en background
     */
    boolean backgroundable() default false;
    
    /**
     * Niveles de prioridad disponibles
     */
    enum PriorityLevel {
        CRITICAL, HIGH, MEDIUM, LOW, BACKGROUND
    }
}