package io.warmup.framework.validation.validators;

import io.warmup.framework.annotation.validation.Size;
import io.warmup.framework.validation.ConstraintValidator;

/**
 * Validator for the {@link Size} constraint.
 * Validates that the annotated element size is between the specified boundaries.
 * Supports String, Collection, Map, and arrays.
 * 
 * @author MiniMax Agent
 * @since 1.0
 */
public class SizeValidator implements ConstraintValidator<Object, Size> {
    
    @Override
    public boolean isValid(Object value, Size constraint) {
        if (value == null) {
            return true; // null values are considered valid, use @NotNull for non-null constraint
        }
        
        int size = getSize(value);
        return size >= constraint.min() && size <= constraint.max();
    }
    
    /**
     * Get the size of the given value based on its type.
     * 
     * @param value the value to get size from
     * @return the size
     */
    private int getSize(Object value) {
        if (value instanceof CharSequence) {
            return ((CharSequence) value).length();
        } else if (value instanceof java.util.Collection) {
            return ((java.util.Collection<?>) value).size();
        } else if (value instanceof java.util.Map) {
            return ((java.util.Map<?, ?>) value).size();
        } else if (value.getClass().isArray()) {
            return java.lang.reflect.Array.getLength(value);
        }
        
        // For unsupported types, return 0 (invalid, but we'll handle with error)
        return 0;
    }
    
    @Override
    public String getMessage(Size constraint, String fieldName, Object invalidValue) {
        String message = constraint.message();
        int size = getSize(invalidValue);
        
        // Replace placeholders
        if (message.contains("{min}")) {
            message = message.replace("{min}", String.valueOf(constraint.min()));
        }
        if (message.contains("{max}")) {
            message = message.replace("{max}", String.valueOf(constraint.max()));
        }
        if (message.contains("{size}") && invalidValue != null) {
            message = message.replace("{size}", String.valueOf(size));
        }
        if (message.contains("{fieldName}") && fieldName != null) {
            message = message.replace("{fieldName}", fieldName);
        }
        
        return message;
    }
}