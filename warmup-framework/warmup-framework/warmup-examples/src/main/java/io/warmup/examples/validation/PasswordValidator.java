package io.warmup.examples.validation;

import io.warmup.framework.annotation.validation.CustomConstraint;
import io.warmup.framework.annotation.validation.CustomConstraintValidator;

/**
 * Custom validator for password strength.
 * Validates that passwords meet complexity requirements.
 * 
 * <p>Parameters:
 * <ul>
 * <li>Min length (default: 8)</li>
 * <li>Require uppercase (default: true)</li>
 * <li>Require lowercase (default: true)</li>
 * <li>Require digits (default: true)</li>
 * <li>Require special chars (default: true)</li>
 * </ul>
 * 
 * @author MiniMax Agent
 * @since 1.0
 */
public class PasswordValidator implements CustomConstraintValidator {
    
    private int minLength = 8;
    private boolean requireUppercase = true;
    private boolean requireLowercase = true;
    private boolean requireDigits = true;
    private boolean requireSpecialChars = true;
    
    @Override
    public void initialize(Object... parameters) {
        if (parameters != null && parameters.length > 0) {
            // Parse parameters: minLength, requireUppercase, requireLowercase, requireDigits, requireSpecialChars
            if (parameters.length > 0 && parameters[0] instanceof Number) {
                minLength = ((Number) parameters[0]).intValue();
            }
            if (parameters.length > 1 && parameters[1] instanceof Boolean) {
                requireUppercase = (Boolean) parameters[1];
            }
            if (parameters.length > 2 && parameters[2] instanceof Boolean) {
                requireLowercase = (Boolean) parameters[2];
            }
            if (parameters.length > 3 && parameters[3] instanceof Boolean) {
                requireDigits = (Boolean) parameters[3];
            }
            if (parameters.length > 4 && parameters[4] instanceof Boolean) {
                requireSpecialChars = (Boolean) parameters[4];
            }
        }
    }
    
    @Override
    public boolean isValid(Object value, Object... parameters) {
        if (value == null) {
            return true; // Use @NotNull for null checks
        }
        
        String password = value.toString();
        
        // Check minimum length
        if (password.length() < minLength) {
            return false;
        }
        
        // Check character requirements
        boolean hasUppercase = !requireUppercase || password.chars().anyMatch(Character::isUpperCase);
        boolean hasLowercase = !requireLowercase || password.chars().anyMatch(Character::isLowerCase);
        boolean hasDigits = !requireDigits || password.chars().anyMatch(Character::isDigit);
        boolean hasSpecialChars = !requireSpecialChars || 
                                 password.chars().anyMatch(ch -> "!@#$%^&*()_+-=[]{}|;:,.<>?".indexOf(ch) >= 0);
        
        return hasUppercase && hasLowercase && hasDigits && hasSpecialChars;
    }
    
    @Override
    public String getMessage(Object invalidValue, String fieldName, Object... parameters) {
        StringBuilder message = new StringBuilder();
        message.append("Password requirements not met for field '").append(fieldName).append("': ");
        
        if (invalidValue instanceof String) {
            String password = (String) invalidValue;
            if (password.length() < minLength) {
                message.append("minimum length ").append(minLength).append("; ");
            }
            if (requireUppercase && !password.chars().anyMatch(Character::isUpperCase)) {
                message.append("uppercase letters; ");
            }
            if (requireLowercase && !password.chars().anyMatch(Character::isLowerCase)) {
                message.append("lowercase letters; ");
            }
            if (requireDigits && !password.chars().anyMatch(Character::isDigit)) {
                message.append("digits; ");
            }
            if (requireSpecialChars && !password.chars().anyMatch(ch -> "!@#$%^&*()_+-=[]{}|;:,.<>?".indexOf(ch) >= 0)) {
                message.append("special characters; ");
            }
        }
        
        // Remove trailing "; " if present
        String result = message.toString();
        if (result.endsWith("; ")) {
            result = result.substring(0, result.length() - 2);
        }
        
        return result;
    }
}