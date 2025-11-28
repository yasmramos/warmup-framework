package io.warmup.framework.annotation.validation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * The annotated element size must be between the specified boundaries (inclusive).
 * Supported types:
 * - CharSequence (length of character sequence is evaluated)
 * - Collection (collection size is evaluated)
 * - Map (map size is evaluated)
 * - Array (array length is evaluated)
 * 
 * @author MiniMax Agent
 * @since 1.0
 */
@Target({ElementType.FIELD, ElementType.PARAMETER, ElementType.METHOD, ElementType.CONSTRUCTOR})
@Retention(RetentionPolicy.RUNTIME)
public @interface Size {
    
    /**
     * The default message key for this constraint.
     */
    String message() default "size must be between {min} and {max}";
    
    /**
     * The constraint groups this constraint belongs to.
     */
    Class<?>[] groups() default {};
    
    /**
     * Maximum number of characters the string or collection/map size.
     */
    int max() default Integer.MAX_VALUE;
    
    /**
     * Minimum number of characters the string or collection/map size.
     */
    int min() default 0;
}