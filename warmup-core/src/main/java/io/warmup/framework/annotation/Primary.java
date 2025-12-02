package io.warmup.framework.annotation;

import java.lang.annotation.*;

/**
 * Indicates that a bean should be given preference when multiple beans of the 
 * same type are candidates for autowiring.
 * 
 * This annotation can be applied to classes or methods.
 * When multiple beans of the same type exist, the bean marked with @Primary
 * will be selected as the default for injection.
 * 
 * @author MiniMax Agent
 * @since 1.1
 */
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Primary {
    
    /**
     * Specifies the priority value for this primary bean.
     * Higher values indicate higher priority.
     * 
     * @return the priority value, defaults to 0
     */
    int value() default 0;
}