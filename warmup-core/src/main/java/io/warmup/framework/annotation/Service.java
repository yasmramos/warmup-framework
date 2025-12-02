package io.warmup.framework.annotation;

import java.lang.annotation.*;

/**
 * Anotación para marcar clases como servicios de negocio.
 * Un servicio encapsula la lógica de negocio de la aplicación.
 * 
 * @author MiniMax Agent
 * @version 1.0
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Component
public @interface Service {
    
    /**
     * Nombre del servicio
     * @return nombre del servicio
     */
    String value() default "";
    
    /**
     * Si el servicio es singleton
     * @return true si es singleton
     */
    boolean singleton() default true;
    
    /**
     * Nivel de servicio
     * @return nivel de servicio
     */
    ServiceLevel level() default ServiceLevel.STANDARD;
    
    /**
     * Tiempo estimado de inicialización
     * @return tiempo estimado en ms
     */
    long estimatedInitTime() default 20;
    
    /**
     * Niveles de servicio disponibles
     */
    enum ServiceLevel {
        CRITICAL,
        HIGH,
        STANDARD,
        LOW,
        BACKGROUND
    }
}