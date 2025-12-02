package io.warmup.framework.annotation;

import java.lang.annotation.*;

/**
 * Defines a scope where a bean exists during the entire HTTP session.
 * 
 * Beans with @SessionScope are created once per HTTP session and are destroyed
 * when the session expires or is invalidated. This scope is ideal for:
 * - User-specific data (e.g., user preferences, shopping cart, authentication info)
 * - Session-level caches
 * - Stateful components that need to maintain state across multiple requests
 * 
 * The bean must be serializable since sessions may be persisted.
 * 
 * @author MiniMax Agent
 * @since 1.2
 */
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface SessionScope {
    
    /**
     * The bean name for EL resolution. If empty, defaults to class name.
     * 
     * @return the bean name, defaults to empty string
     */
    String value() default "";
}