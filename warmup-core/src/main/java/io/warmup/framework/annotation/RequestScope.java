package io.warmup.framework.annotation;

import java.lang.annotation.*;

/**
 * Defines a scope where a bean exists during the processing of a single HTTP request.
 * 
 * Beans with @RequestScope are created once per HTTP request and are destroyed
 * when the request processing is complete. This scope is ideal for:
 * - Request-specific data (e.g., user context, request ID, request parameters)
 * - Transaction-like operations that should be cleaned up after request
 * - Request-scoped caches
 * 
 * The bean must be serializable if the application server requires passivation.
 * 
 * @author MiniMax Agent
 * @since 1.2
 */
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RequestScope {
    
    /**
     * The bean name for EL resolution. If empty, defaults to class name.
     * 
     * @return the bean name, defaults to empty string
     */
    String value() default "";
}