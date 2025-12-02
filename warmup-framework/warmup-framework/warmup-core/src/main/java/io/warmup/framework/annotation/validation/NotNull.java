package io.warmup.framework.annotation.validation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * The annotated element must not be null.
 * Supported types: primitive types, CharSequence, Collection, Map, arrays.
 * 
 * @author MiniMax Agent
 * @since 1.0
 */
@Target({ElementType.FIELD, ElementType.PARAMETER, ElementType.METHOD, ElementType.CONSTRUCTOR})
@Retention(RetentionPolicy.RUNTIME)
public @interface NotNull {
    
    /**
     * The default message key for this constraint.
     */
    String message() default "must not be null";
    
    /**
     * The constraint groups this constraint belongs to.
     */
    Class<?>[] groups() default {};
}