package io.warmup.framework.annotation;

import java.lang.annotation.*;

/**
 * Indicates that a bean is an alternative implementation.
 * 
 * Alternative beans are only activated when explicitly selected by the application
 * configuration or through profiles. This allows for different implementations
 * to be used in different environments without changing the application code.
 * 
 * By default, @Alternative beans are not eligible for injection unless explicitly
 * enabled through configuration.
 * 
 * @author MiniMax Agent
 * @since 1.1
 */
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Alternative {
    
    /**
     * The alternative identifier. Can be used to select this alternative
     * explicitly when multiple alternatives exist.
     * 
     * @return the alternative identifier, defaults to empty string
     */
    String value() default "";
    
    /**
     * The profile name for this alternative.
     * When specified, this alternative is only active in the given profile.
     * 
     * @return the profile name, defaults to empty string
     */
    String profile() default "";
}