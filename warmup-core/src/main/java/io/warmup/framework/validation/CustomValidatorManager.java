package io.warmup.framework.validation;

import io.warmup.framework.annotation.validation.CustomConstraint;
import io.warmup.framework.annotation.validation.CustomConstraintValidator;
import io.warmup.framework.asm.AsmCoreUtils;

import java.lang.reflect.Field;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

/**
 * Manages the registration and execution of custom validators.
 * Provides methods to register, unregister, and execute custom validation logic.
 * 
 * @author MiniMax Agent
 * @since 1.0
 */
public class CustomValidatorManager {
    
    private static final Logger logger = Logger.getLogger(CustomValidatorManager.class.getName());
    
    // Registry of custom validators by constraint type (for backward compatibility)
    private final Map<CustomConstraint, CustomValidatorInstance> customValidators;
    
    // Registry of custom validators by validator class (for registerValidatorByClass)
    private final Map<Class<? extends CustomConstraintValidator>, CustomValidatorInstance> customValidatorsByClass;
    
    public CustomValidatorManager() {
        this.customValidators = new ConcurrentHashMap<>();
        this.customValidatorsByClass = new ConcurrentHashMap<>();
    }
    
    /**
     * Register a custom validator instance for a constraint.
     * 
     * @param constraint the custom constraint annotation
     * @param validator the validator instance
     */
    public void registerValidator(CustomConstraint constraint, CustomConstraintValidator validator) {
        if (constraint == null || validator == null) {
            throw new IllegalArgumentException("Constraint and validator cannot be null");
        }
        
        try {
            CustomValidatorInstance instance = new CustomValidatorInstance(validator, constraint.parameters());
            
            // Initialize validator with parameters if it supports it
            validator.initialize(constraint.parameters());
            
            // Store in both registries for consistency
            customValidators.put(constraint, instance);
            customValidatorsByClass.put(constraint.validator(), instance);
            
            logger.info("Custom validator registered: " + AsmCoreUtils.getSimpleClassName(validator.getClass()));
            
        } catch (Exception e) {
            throw new RuntimeException("Failed to register custom validator: " + 
                                     AsmCoreUtils.getClassName(validator.getClass()), e);
        }
    }
    
    /**
     * Unregister a custom validator.
     * 
     * @param constraint the custom constraint to unregister
     */
    public void unregisterValidator(CustomConstraint constraint) {
        if (constraint != null) {
            CustomValidatorInstance removed = customValidators.remove(constraint);
            if (removed != null) {
                customValidatorsByClass.remove(constraint.validator());
                logger.info("Custom validator unregistered");
            }
        }
    }
    
    /**
     * Validate a field with custom constraints.
     * 
     * @param object the object containing the field
     * @param field the field to validate
     * @param fieldName the name of the field
     * @return list of violations, empty if validation passes
     */
    public <T> List<ConstraintViolation<T>> validateCustomConstraints(T object, Field field, String fieldName) {
        List<ConstraintViolation<T>> violations = new ArrayList<>();
        
        try {
            field.setAccessible(true);
            Object fieldValue = AsmCoreUtils.getFieldValue(object, field.getName());
            
            CustomConstraint[] customConstraints = field.getAnnotationsByType(CustomConstraint.class);
            
            for (CustomConstraint customConstraint : customConstraints) {
                CustomValidatorInstance validatorInstance = findValidator(customConstraint);
                
                if (validatorInstance != null) {
                    boolean isValid = validatorInstance.validate(fieldValue);
                    
                    if (!isValid) {
                        String message = validatorInstance.getMessage(fieldValue, fieldName);
                        
                        ConstraintViolation<T> violation = new ConstraintViolation<>(
                            message,
                            customConstraint.toString(),
                            object,
                            fieldName,
                            fieldValue,
                            AsmCoreUtils.getSimpleClassName(customConstraint.getClass()),
                            field.getName()
                        );
                        
                        violations.add(violation);
                        logger.fine("Custom validation violation: " + violation);
                    }
                } else {
                    // Log warning if validator not found
                    logger.warning("No custom validator registered for: " + 
                                 AsmCoreUtils.getSimpleClassName(customConstraint.validator()));
                }
            }
            
        } catch (Exception e) {
            logger.warning("Error during custom validation for field " + fieldName + ": " + 
                         e.getMessage());
        }
        
        return violations;
    }
    
    /**
     * Find the appropriate validator instance for a constraint.
     */
    private CustomValidatorInstance findValidator(CustomConstraint constraint) {
        // First, try to find by validator class (primary method for registerValidatorByClass)
        Class<? extends CustomConstraintValidator> validatorClass = constraint.validator();
        CustomValidatorInstance byClass = customValidatorsByClass.get(validatorClass);
        if (byClass != null) {
            logger.fine("Found validator by class: " + validatorClass.getSimpleName());
            return byClass;
        }
        
        // Fallback to constraint-based lookup (for backward compatibility)
        CustomValidatorInstance byConstraint = customValidators.get(constraint);
        if (byConstraint != null) {
            logger.fine("Found validator by constraint");
            return byConstraint;
        }
        
        logger.warning("No custom validator registered for: " + validatorClass.getSimpleName());
        return null;
    }
    
    /**
     * Register a custom validator by class.
     * Convenience method for registering validators directly by class type.
     * 
     * @param validatorClass the validator class
     * @param parameters optional parameters
     */
    public void registerValidatorByClass(Class<? extends CustomConstraintValidator> validatorClass, 
                                       Object... parameters) {
        if (!AsmCoreUtils.isAssignableFrom(CustomConstraintValidator.class, validatorClass)) {
            throw new IllegalArgumentException("Class must implement CustomConstraintValidator");
        }
        
        try {
            // Create a dummy constraint for registration
            CustomConstraint dummyConstraint = new CustomConstraint() {
                @Override
                public Class<? extends CustomConstraintValidator> validator() {
                    return validatorClass;
                }
                
                @Override
                public String message() {
                    return "Custom validation failed";
                }
                
                @Override
                public String[] parameters() {
                    String[] paramArray = new String[parameters.length];
                    for (int i = 0; i < parameters.length; i++) {
                        paramArray[i] = parameters[i].toString();
                    }
                    return paramArray;
                }
                
                @Override
                public Class<?>[] groups() {
                    return new Class<?>[0];
                }
                
                @Override
                public Class<? extends java.lang.annotation.Annotation> annotationType() {
                    return CustomConstraint.class;
                }
            };
            
            CustomConstraintValidator validator = AsmCoreUtils.newInstance(validatorClass);
            
            // Convert parameters to string array for the instance
            String[] paramArray = new String[parameters.length];
            for (int i = 0; i < parameters.length; i++) {
                paramArray[i] = parameters[i].toString();
            }
            
            CustomValidatorInstance instance = new CustomValidatorInstance(validator, paramArray);
            
            // Initialize validator with parameters if it supports it
            validator.initialize(parameters);
            
            // Store in both registries
            customValidators.put(dummyConstraint, instance);
            customValidatorsByClass.put(validatorClass, instance);
            

            logger.info("Custom validator registered by class: " + validatorClass.getSimpleName());
            
        } catch (Exception e) {
            throw new RuntimeException("Failed to create validator instance: " + 
                                     validatorClass.getName(), e);
        }
    }
    
    /**
     * Get the count of registered custom validators.
     * 
     * @return the number of registered validators
     */
    public int getValidatorCount() {
        return customValidatorsByClass.size();
    }
    
    /**
     * Clear all registered custom validators.
     */
    public void clearAllValidators() {
        customValidators.clear();
        customValidatorsByClass.clear();
        logger.info("All custom validators cleared");
    }
    
    /**
     * Internal class representing a validator instance with its configuration.
     */
    private static class CustomValidatorInstance {
        private final CustomConstraintValidator validator;
        private final Object[] parameters;
        
        public CustomValidatorInstance(CustomConstraintValidator validator, String[] parameters) {
            this.validator = validator;
            this.parameters = new Object[parameters.length];
            for (int i = 0; i < parameters.length; i++) {
                this.parameters[i] = parameters[i];
            }
        }
        
        public boolean validate(Object value) {
            return validator.isValid(value, parameters);
        }
        
        public String getMessage(Object invalidValue, String fieldName) {
            return validator.getMessage(invalidValue, fieldName, parameters);
        }
    }
}