package io.warmup.framework.annotation;

import java.lang.annotation.*;

/**
 * Container annotation for multiple {@link ConditionalOnProperty} annotations.
 * Allows multiple conditional annotations on the same element.
 * 
 * @author MiniMax Agent
 * @since 1.0
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface ConditionalOnPropertyList {
    
    /**
     * The array of conditional property annotations.
     * 
     * @return array of conditional annotations
     */
    ConditionalOnProperty[] value();
}