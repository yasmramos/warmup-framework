package io.warmup.framework.annotation;

import java.lang.annotation.*;

/**
 * Anotación para marcar componentes de alta prioridad.
 * Los componentes de alta prioridad se inicializan después de los críticos
 * pero antes de los componentes estándar.
 * 
 * @author MiniMax Agent
 * @version 1.0
 */
@Target({ElementType.TYPE, ElementType.METHOD, ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface HighPriority {
    
    /**
     * Nivel de prioridad específico
     * @return nivel de prioridad
     */
    PriorityLevel level() default PriorityLevel.HIGH;
    
    /**
     * Tiempo estimado de inicialización en milisegundos
     * @return tiempo estimado
     */
    long estimatedTime() default 5;
    
    /**
     * Si permite paralelización
     * @return true si permite paralelización
     */
    boolean parallelizable() default true;
    
    /**
     * Niveles de prioridad disponibles
     */
    enum PriorityLevel {
        CRITICAL, HIGH, MEDIUM, LOW, BACKGROUND
    }
}