package io.warmup.framework.validation;

import io.warmup.framework.asm.AsmCoreUtils;
import java.lang.annotation.Annotation;

/**
 * Defines the logic to validate a given constraint annotation for a given type.
 * Implementations must be thread-safe.
 * 
 * @param <T> the type of objects to validate
 * @param <A> the annotation type this validator validates
 * 
 * @author MiniMax Agent
 * @since 1.0
 */
public interface ConstraintValidator<T, A extends Annotation> {
    
    /**
     * Initialize the validator in preparation for isValid calls.
     * The constraint annotation for a given constraint declaration is
     * passed.
     * <p/>
     * This method is typically called when the validator is being initialized.
     * <p/>
     * Implementations should not rely on normal traversal of the object graph,
     * this initialization is to be called once before an isValid method is called.
     *
     * @param constraintAnnotation annotation instance for a given constraint declaration
     */
    default void initialize(A constraintAnnotation) {
        // Default implementation does nothing
    }
    
    /**
     * Implement validation logic for object values. The value parameter is never null.
     * This method should return false if the value is invalid, true otherwise.
     *
     * @param value object to validate
     * @param constraint annotation that triggered the validation
     * @return true if the value is valid, false otherwise
     */
    boolean isValid(T value, A constraint);
    
    /**
     * Get the message to be used when validation fails.
     * This method will replace any placeholders in the constraint's message template
     * with the actual values.
     *
     * @param constraint the constraint annotation
     * @param fieldName the name of the field being validated
     * @param invalidValue the value that failed validation
     * @return the formatted message
     */
    default String getMessage(A constraint, String fieldName, Object invalidValue) {
        // Try to get message from constraint annotation
        try {
            java.lang.reflect.Method messageMethod = constraint.getClass().getMethod("message");
            // ✅ FASE 6: Invocación progresiva del método - ASM → MethodHandle → Reflection
            Object message = AsmCoreUtils.invokeMethodObjectProgressive(messageMethod, constraint);
            return message != null ? message.toString() : "Validation failed for field: " + fieldName;
        } catch (Throwable e) {
            // If no message method exists, return default message
            return "Validation failed for field: " + fieldName;
        }
    }
}