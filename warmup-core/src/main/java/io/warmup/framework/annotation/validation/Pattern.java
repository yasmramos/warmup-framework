package io.warmup.framework.annotation.validation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * The annotated String must match the specified regular expression.
 * The regular expression follows the Java regular expression conventions.
 * 
 * @author MiniMax Agent
 * @since 1.0
 */
@Target({ElementType.FIELD, ElementType.PARAMETER, ElementType.METHOD, ElementType.CONSTRUCTOR})
@Retention(RetentionPolicy.RUNTIME)
public @interface Pattern {
    
    /**
     * The default message key for this constraint.
     */
    String message() default "must match \"{regexp}\"";
    
    /**
     * The constraint groups this constraint belongs to.
     */
    Class<?>[] groups() default {};
    
    /**
     * The regular expression the annotated element must match.
     */
    String regexp();
}