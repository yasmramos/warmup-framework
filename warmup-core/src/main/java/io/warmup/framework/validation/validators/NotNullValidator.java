package io.warmup.framework.validation.validators;

import io.warmup.framework.annotation.validation.NotNull;
import io.warmup.framework.validation.ConstraintValidator;
import java.util.Objects;

/**
 * Validator for the {@link NotNull} constraint.
 * Validates that the annotated element is not null.
 * 
 * @author MiniMax Agent
 * @since 1.0
 */
public class NotNullValidator implements ConstraintValidator<Object, NotNull> {
    
    @Override
    public boolean isValid(Object value, NotNull constraint) {
        // Check if the value is not null
        return value != null;
    }
    
    @Override
    public String getMessage(NotNull constraint, String fieldName, Object invalidValue) {
        String message = constraint.message();
        if (message.contains("{fieldName}") && fieldName != null) {
            message = message.replace("{fieldName}", fieldName);
        }
        return message;
    }
}