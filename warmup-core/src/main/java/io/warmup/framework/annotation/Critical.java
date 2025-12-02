package io.warmup.framework.annotation;

import java.lang.annotation.*;

/**
 * Anotación para marcar componentes críticos del sistema.
 * Los componentes marcados como críticos tienen máxima prioridad
 * durante el startup y no pueden fallar.
 * 
 * @author MiniMax Agent
 * @version 1.0
 */
@Target({ElementType.TYPE, ElementType.METHOD, ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Critical {
    
    /**
     * Nivel de criticidad del componente
     * @return nivel de criticidad
     */
    CriticalLevel value() default CriticalLevel.CRITICAL;
    
    /**
     * Tiempo máximo estimado de inicialización en milisegundos
     * @return tiempo máximo estimado
     */
    long maxInitTime() default 1;
    
    /**
     * Si el componente es bloqueante para el startup
     * @return true si es bloqueante
     */
    boolean blocking() default true;
    
    /**
     * Niveles de criticidad disponibles
     */
    enum CriticalLevel {
        CRITICAL, HIGH, MEDIUM, LOW, BACKGROUND
    }
}