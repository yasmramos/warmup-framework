package io.warmup.framework.validation.validators;

import io.warmup.framework.annotation.validation.Pattern;
import io.warmup.framework.validation.ConstraintValidator;
import java.util.regex.PatternSyntaxException;
import java.util.regex.Matcher;

/**
 * Validator for the {@link Pattern} constraint.
 * Validates that the annotated String matches the specified regular expression.
 * 
 * @author MiniMax Agent
 * @since 1.0
 */
public class PatternValidator implements ConstraintValidator<String, Pattern> {
    
    private java.util.regex.Pattern pattern;
    
    @Override
    public void initialize(Pattern constraint) {
        try {
            this.pattern = java.util.regex.Pattern.compile(constraint.regexp());
        } catch (PatternSyntaxException e) {
            throw new IllegalArgumentException(
                "Invalid regular expression in @Pattern constraint: " + constraint.regexp(), e);
        }
    }
    
    @Override
    public boolean isValid(String value, Pattern constraint) {
        if (value == null) {
            return true; // null values are considered valid, use @NotNull for non-null constraint
        }
        
        if (pattern == null) {
            throw new IllegalStateException("Pattern validator not initialized. Call initialize() first.");
        }
        
        Matcher matcher = pattern.matcher(value);
        return matcher.matches();
    }
    
    @Override
    public String getMessage(Pattern constraint, String fieldName, Object invalidValue) {
        String message = constraint.message();
        
        if (message.contains("{regexp}")) {
            message = message.replace("{regexp}", constraint.regexp());
        }
        if (message.contains("{fieldName}") && fieldName != null) {
            message = message.replace("{fieldName}", fieldName);
        }
        if (message.contains("{value}") && invalidValue != null) {
            message = message.replace("{value}", String.valueOf(invalidValue));
        }
        
        return message;
    }
}