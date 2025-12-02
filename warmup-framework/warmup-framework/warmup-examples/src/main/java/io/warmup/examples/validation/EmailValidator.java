package io.warmup.examples.validation;

import io.warmup.framework.annotation.validation.CustomConstraint;
import io.warmup.framework.annotation.validation.CustomConstraintValidator;

import java.util.regex.Pattern;

/**
 * Custom validator for email addresses.
 * Validates email format using a standard pattern.
 * 
 * @author MiniMax Agent
 * @since 1.0
 */
public class EmailValidator implements CustomConstraintValidator {
    
    // RFC 5322 simplified email pattern
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
        "^[a-zA-Z0-9.!#$%&'*+/=?^_`{|}~-]+@[a-zA-Z0-9](?:[a-zA-Z0-9-]{0,61}[a-zA-Z0-9])?(?:\\.[a-zA-Z0-9](?:[a-zA-Z0-9-]{0,61}[a-zA-Z0-9])?)*$"
    );
    
    @Override
    public boolean isValid(Object value, Object... parameters) {
        if (value == null) {
            return true; // Use @NotNull for null checks
        }
        
        String email = value.toString().trim();
        if (email.isEmpty()) {
            return true; // Empty strings are handled by @Size
        }
        
        return EMAIL_PATTERN.matcher(email).matches();
    }
    
    @Override
    public String getMessage(Object invalidValue, String fieldName, Object... parameters) {
        return String.format("Invalid email format for field '%s': '%s'", 
                           fieldName, invalidValue);
    }
}