package io.warmup.framework.annotation.validation;

import java.lang.annotation.*;

/**
 * Defines a custom validation constraint.
 * This annotation allows users to define their own validation rules
 * by specifying a custom validator class.
 * 
 * <p>Example usage:
 * <pre>
 * {@literal @}CustomConstraint(validator = EmailValidator.class)
 * private String email;
 * 
 * {@literal @}CustomConstraint(
 *     validator = PasswordValidator.class,
 *     message = "Password must be at least 8 characters long"
 * )
 * private String password;
 * </pre>
 * 
 * @author MiniMax Agent
 * @since 1.0
 */
@Target({ElementType.FIELD, ElementType.METHOD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface CustomConstraint {
    
    /**
     * The custom validator class that will perform the validation.
     * The class must implement CustomConstraintValidator interface.
     * 
     * @return the validator class
     */
    Class<? extends CustomConstraintValidator> validator();
    
    /**
     * The error message when validation fails.
     * Supports placeholders: {fieldName}, {value}
     * 
     * @return the error message
     */
    String message() default "Custom validation failed";
    
    /**
     * Additional parameters for the validator.
     * These will be passed to the validator during initialization.
     * 
     * @return the parameters as a string array
     */
    String[] parameters() default {};
    
    /**
     * The validation groups this constraint belongs to.
     * 
     * @return the validation groups
     */
    Class<?>[] groups() default {};
}