package io.warmup.test.config;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Configuración avanzada para anotaciones @Spy.
 * 
 * Permite personalizar el comportamiento de los spies creados.
 */
public @interface SpyConfig {
    
    /**
     * Usar constructor específico para crear la instancia real.
     * 
     * @return clase del constructor
     */
    Class<?> constructor() default Object.class;
    
    /**
     * Argumentos para el constructor si es necesario.
     * 
     * @return argumentos del constructor
     */
    String[] constructorArgs() default {};
    
    /**
     * Configuración de auto-inyección para el spy.
     * 
     * @return configuración de inyección
     */
    InjectConfig injectConfig() default @InjectConfig;
    
    /**
     * Comportamiento por defecto del spy.
     * 
     * @return comportamiento por defecto
     */
    SpyBehavior behavior() default SpyBehavior.REAL_BY_DEFAULT;
    
    /**
     * Logging level para el spy.
     * 
     * @return nivel de logging
     */
    LogLevel logLevel() default LogLevel.NONE;
    
    /**
     * Habilitar tracking de llamadas para este spy.
     * 
     * @return true para tracking
     */
    boolean trackCalls() default false;
}

/**
 * Comportamientos disponibles para spies.
 */
enum SpyBehavior {
    
    /**
     * Usar implementación real por defecto, mockear cuando sea necesario.
     */
    REAL_BY_DEFAULT,
    
    /**
     * Usar mock por defecto, llamar métodos reales solo cuando se stubee explícitamente.
     */
    MOCK_BY_DEFAULT,
    
    /**
     * Always call real methods, never mock.
     */
    ALWAYS_REAL,
    
    /**
     * Always mock methods unless explicitly configured otherwise.
     */
    ALWAYS_MOCK
}

/**
 * Configuración de inyección para spies.
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@interface InjectConfig {
    
    /**
     * Permitir auto-inyección basada en tipos compatibles.
     * 
     * @return true para auto-inyección
     */
    boolean allowAutoInject() default true;
    
    /**
     * Resolver conflictos de tipos usando nombres de campos.
     * 
     * @return resolución por nombre
     */
    boolean resolveByName() default false;
    
    /**
     * Permitir inyección de nulls para dependencias faltantes.
     * 
     * @return true para permitir nulls
     */
    boolean allowNullInjection() default false;
    
    /**
     * Configuración de setter injection.
     * 
     * @return configuración de setters
     */
    SetterConfig setterConfig() default @SetterConfig;
}

/**
 * Configuración de setter injection.
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@interface SetterConfig {
    boolean useSetters() default true;
    boolean useFields() default true;
    boolean useConstructors() default true;
    boolean strictMatching() default false;
}