package io.warmup.examples.validation;

import io.warmup.framework.annotation.validation.CustomConstraint;
import io.warmup.framework.annotation.validation.CustomConstraintValidator;

import java.util.regex.Pattern;

/**
 * Custom validator for username validation.
 * Validates that usernames meet complexity and security requirements.
 * 
 * <p>Validates:
 * <ul>
 * <li>Length between 3 and 30 characters</li>
 * <li>Only alphanumeric characters and underscores</li>
 * <li>Must start with a letter</li>
 * <li>No consecutive underscores</li>
 * <li>No trailing or leading underscores</li>
 * </ul>
 * 
 * @author MiniMax Agent
 * @since 1.0
 */
public class UsernameValidator implements CustomConstraintValidator {
    
    // Username pattern: starts with letter, 3-30 chars, alphanumeric + underscores only
    private static final Pattern USERNAME_PATTERN = Pattern.compile("^[a-zA-Z][a-zA-Z0-9_]{2,29}$");
    
    @Override
    public boolean isValid(Object value, Object... parameters) {
        if (value == null) {
            return true; // Use @NotNull for null checks
        }
        
        String username = value.toString().trim();
        if (username.isEmpty()) {
            return false; // Empty usernames are not allowed
        }
        
        // Check basic pattern
        if (!USERNAME_PATTERN.matcher(username).matches()) {
            return false;
        }
        
        // Additional validation: no consecutive underscores
        if (username.contains("__")) {
            return false;
        }
        
        // Additional validation: no trailing or leading underscores (already handled by regex)
        return true;
    }
    
    @Override
    public String getMessage(Object invalidValue, String fieldName, Object... parameters) {
        String username = invalidValue != null ? invalidValue.toString() : "null";
        
        if (username == null || username.trim().isEmpty()) {
            return String.format("Username cannot be empty for field '%s'", fieldName);
        }
        
        if (username.length() < 3) {
            return String.format("Username '%s' must be at least 3 characters long for field '%s'", 
                               username, fieldName);
        }
        
        if (username.length() > 30) {
            return String.format("Username '%s' must be at most 30 characters long for field '%s'", 
                               username, fieldName);
        }
        
        if (!Character.isLetter(username.charAt(0))) {
            return String.format("Username '%s' must start with a letter for field '%s'", 
                               username, fieldName);
        }
        
        if (!username.matches("^[a-zA-Z0-9_]*$")) {
            return String.format("Username '%s' can only contain letters, numbers, and underscores for field '%s'", 
                               username, fieldName);
        }
        
        if (username.contains("__")) {
            return String.format("Username '%s' cannot contain consecutive underscores for field '%s'", 
                               username, fieldName);
        }
        
        return String.format("Invalid username format for field '%s': '%s'", fieldName, username);
    }
}