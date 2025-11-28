package io.warmup.test.config;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Configuración avanzada para anotaciones @Mock.
 * 
 * Permite personalizar el comportamiento de los mocks creados.
 */
public @interface MockConfig {
    
    /**
     * Habilitar logging verbose para este mock específico.
     * 
     * @return true para logging detallado
     */
    boolean verbose() default false;
    
    /**
     * Crear mocks con nombres para debugging.
     * 
     * @return true para naming automático
     */
    boolean named() default false;
    
    /**
     * Serializar el mock (para casos especiales).
     * 
     * @return true para mock serializable
     */
    boolean serializable() default false;
    
    /**
     * Logging level para el mock.
     * 
     * @return nivel de logging
     */
    LogLevel logLevel() default LogLevel.NONE;
    
    /**
     * Configuración de stubbing personalizado.
     * 
     * @return configuración de stub
     */
    StubConfig stubConfig() default @StubConfig();
}

/**
 * Niveles de logging disponibles para mocks.
 */
enum LogLevel {
    NONE,      // Sin logging
    BASIC,     // Información básica
    VERBOSE,   // Logging detallado
    DEBUG      // Debug completo
}

/**
 * Configuración de stubbing para mocks.
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@interface StubConfig {
    
    /**
     * Stubbing por defecto para métodos void.
     * 
     * @return true para stubbing automático
     */
    boolean stubVoidMethods() default true;
    
    /**
     * Retornar valores por defecto para tipos primitivos.
     * 
     * @return true para defaults automáticos
     */
    boolean returnDefaultValues() default true;
    
    /**
     * Configuración de argumentos matching.
     * 
     * @return configuración de args
     */
    ArgsConfig argsConfig() default @ArgsConfig;
}

/**
 * Configuración de matching de argumentos.
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@interface ArgsConfig {
    boolean strict() default false;
    boolean anyString() default true;
    boolean anyCollection() default true;
}