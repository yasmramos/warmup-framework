package io.warmup.framework.annotation;

import java.lang.annotation.*;

/**
 * Defines a scope where a bean exists for the entire lifetime of the web application.
 * 
 * Beans with @ApplicationScope are created once when the application starts and
 * destroyed when the application shuts down. This scope is similar to @Singleton
 * but specifically designed for web applications. This scope is ideal for:
 * - Application-wide configuration
 * - Shared caches across all users
 * - Application-level services and utilities
 * 
 * The bean must be serializable since the application may be clustered.
 * 
 * @author MiniMax Agent
 * @since 1.2
 */
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface ApplicationScope {
    
    /**
     * The bean name for EL resolution. If empty, defaults to class name.
     * 
     * @return the bean name, defaults to empty string
     */
    String value() default "";
}