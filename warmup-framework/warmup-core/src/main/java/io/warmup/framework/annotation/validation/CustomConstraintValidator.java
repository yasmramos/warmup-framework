package io.warmup.framework.annotation.validation;

import java.util.List;

/**
 * Interface for custom validation logic.
 * Implementations must be thread-safe and stateless.
 * 
 * <p>This interface allows users to create complex validation rules
 * that are not covered by the built-in constraints.
 * 
 * <p>Example implementation:
 * <pre>
 * public class EmailValidator implements CustomConstraintValidator {
 *     private static final Pattern EMAIL_PATTERN = 
 *         Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$");
 *     
 *     {@literal @}Override
 *     public boolean isValid(Object value, Object... parameters) {
 *         if (value == null) {
 *             return true; // Use {@literal @}NotNull for null checks
 *         }
 *         return EMAIL_PATTERN.matcher(value.toString()).matches();
 *     }
 *     
 *     {@literal @}Override
 *     public String getMessage(Object invalidValue, String fieldName, Object... parameters) {
 *         return String.format("Invalid email format for field %s: %s", 
 *                            fieldName, invalidValue);
 *     }
 * }
 * </pre>
 * 
 * @author MiniMax Agent
 * @since 1.0
 */
public interface CustomConstraintValidator {
    
    /**
     * Validate the given value according to custom logic.
     * 
     * <p>Implementations should:
     * <ul>
     * <li>Be thread-safe and stateless</li>
     * <li>Handle null values appropriately (usually return true)</li>
     * <li>Use parameters array for configuration if needed</li>
     * <li>Perform only the specific validation logic</li>
     * </ul>
     * 
     * @param value the value to validate (never null in this method)
     * @param parameters optional parameters for configuration
     * @return true if validation passes, false otherwise
     */
    boolean isValid(Object value, Object... parameters);
    
    /**
     * Get a descriptive error message for validation failure.
     * 
     * @param invalidValue the value that failed validation
     * @param fieldName the name of the field being validated
     * @param parameters optional parameters used during validation
     * @return a descriptive error message
     */
    String getMessage(Object invalidValue, String fieldName, Object... parameters);
    
    /**
     * Optional initialization method called once when validator is registered.
     * Use this to compile patterns, load resources, or perform expensive setup.
     * 
     * @param parameters parameters from the constraint annotation
     */
    default void initialize(Object... parameters) {
        // Default implementation does nothing
    }
}