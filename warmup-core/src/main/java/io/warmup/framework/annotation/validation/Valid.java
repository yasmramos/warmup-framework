package io.warmup.framework.annotation.validation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a field, method parameter, or method return value to be validated.
 * When used on a method parameter, the parameter will be validated before method execution.
 * When used on a method return value, the returned object will be validated after method execution.
 * 
 * This annotation follows the Bean Validation standard.
 * 
 * @author MiniMax Agent
 * @since 1.0
 */
@Target({ElementType.FIELD, ElementType.PARAMETER, ElementType.METHOD, ElementType.CONSTRUCTOR})
@Retention(RetentionPolicy.RUNTIME)
public @interface Valid {
}