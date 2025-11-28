package io.warmup.framework.validation;

import io.warmup.framework.annotation.validation.*;
import io.warmup.framework.validation.validators.NotNullValidator;
import io.warmup.framework.validation.validators.PatternValidator;
import io.warmup.framework.validation.validators.SizeValidator;
import io.warmup.framework.asm.AsmCoreUtils;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

/**
 * Default implementation of the Validator interface.
 * Validates objects using reflection and constraint annotations.
 * 
 * @author MiniMax Agent
 * @since 1.0
 */
public class DefaultValidator implements Validator {
    
    private static final Logger logger = Logger.getLogger(DefaultValidator.class.getName());
    
    // Registry of validators for constraint types
    private final Map<Class<?>, ConstraintValidator<?, ? extends Annotation>> validatorRegistry;
    
    // Manager for custom validators
    private final CustomValidatorManager customValidatorManager;
    
    public DefaultValidator() {
        this.validatorRegistry = new ConcurrentHashMap<>();
        this.customValidatorManager = new CustomValidatorManager();
        initializeDefaultValidators();
    }
    
    /**
     * Initialize the default validators for built-in constraints.
     */
    private void initializeDefaultValidators() {
        registerValidator(NotNull.class, new NotNullValidator());
        registerValidator(Size.class, new SizeValidator());
        registerValidator(Pattern.class, new PatternValidator());
        
        logger.fine("Default validators initialized");
    }
    
    /**
     * Register a validator for a specific constraint type.
     * 
     * @param constraintType the constraint annotation type
     * @param validator the validator implementation
     */
    public <T, A extends Annotation> void registerValidator(Class<A> constraintType, 
                                                            ConstraintValidator<T, A> validator) {
        validatorRegistry.put(constraintType, validator);
    }
    
    /**
     * Get the custom validator manager for registering custom validators.
     * 
     * @return the custom validator manager
     */
    public CustomValidatorManager getCustomValidatorManager() {
        return customValidatorManager;
    }
    
    @Override
    public <T> List<ConstraintViolation<T>> validate(T object, Class<?>... validationGroups) {
        if (object == null) {
            throw new IllegalArgumentException("Object to validate cannot be null");
        }
        
        ViolationReport<T> report = getViolationReport(object, validationGroups);
        return report.getViolations();
    }
    
    @Override
    public <T> List<ConstraintViolation<T>> validateProperty(T object, String propertyName, Class<?>... validationGroups) {
        if (object == null) {
            throw new IllegalArgumentException("Object to validate cannot be null");
        }
        if (propertyName == null || propertyName.trim().isEmpty()) {
            throw new IllegalArgumentException("Property name cannot be null or empty");
        }
        
        ViolationReport<T> report = new ViolationReport<>(object);
        validateField(object, object.getClass(), propertyName, propertyName, report, validationGroups);
        return report.getViolations();
    }
    
    @Override
    public <T> boolean isValid(T object, Class<?>... validationGroups) {
        if (object == null) {
            return true; // null objects are considered valid
        }
        
        List<ConstraintViolation<T>> violations = validate(object, validationGroups);
        return violations.isEmpty();
    }
    
    @Override
    public <T> ViolationReport<T> getViolationReport(T object, Class<?>... validationGroups) {
        if (object == null) {
            throw new IllegalArgumentException("Object to validate cannot be null");
        }
        
        ViolationReport<T> report = new ViolationReport<>(object);
        validateObject(object, object.getClass(), "", report, validationGroups);
        
        logger.fine("Validation completed for object " + object.getClass().getSimpleName() + 
                   " with " + report.getViolationCount() + " violations");
        
        return report;
    }
    
    /**
     * Validate the entire object and its nested objects.
     */
    private <T> void validateObject(T object, Class<?> clazz, String prefix, 
                                   ViolationReport<T> report, Class<?>[] validationGroups) {
        // Get all declared fields (including inherited) using ASM
        List<Field> allFields = AsmCoreUtils.asList(AsmCoreUtils.getAllFields(clazz));
        for (Field field : allFields) {
            validateField(object, clazz, field.getName(), prefix + (prefix.isEmpty() ? "" : ".") + field.getName(), 
                        report, validationGroups);
        }
    }
    
    /**
     * Validate a specific field.
     */
    @SuppressWarnings("unchecked")
    private <T> void validateField(T object, Class<?> clazz, String fieldName, String propertyPath,
                                  ViolationReport<T> report, Class<?>[] validationGroups) {
        try {
            Field field = findField(clazz, fieldName);
            if (field == null) {
                return;
            }
            
            // Get field value using ASM
            Object fieldValue = AsmCoreUtils.getFieldValue(object, field.getName());
            
            // Get all annotations on the field using ASM
            Annotation[] annotations = AsmCoreUtils.getFieldAnnotations(field);
            
            for (Annotation annotation : annotations) {
                Class<? extends Annotation> annotationType = annotation.annotationType();
                
                // Skip @Valid annotation, handle it separately for nested validation
                if (annotationType.equals(Valid.class)) {
                    validateNestedObject(fieldValue, propertyPath, report, validationGroups);
                    continue;
                }
                
                // Handle custom constraints
                if (annotationType.equals(CustomConstraint.class)) {
                    List<ConstraintViolation<T>> customViolations = 
                        customValidatorManager.validateCustomConstraints(object, field, propertyPath);
                    for (ConstraintViolation<T> violation : customViolations) {
                        report.addViolation(violation);
                    }
                    continue;
                }
                
                // Check if we have a validator for this annotation type
                ConstraintValidator<?, ?> validator = validatorRegistry.get(annotationType);
                if (validator != null) {
                    validateFieldWithConstraint(object, field, fieldValue, propertyPath, 
                                              annotationType, annotation, validator, report);
                }
            }
            
        } catch (Exception e) {
            logger.warning("Error validating field " + fieldName + ": " + e.getMessage());
        }
    }
    
    /**
     * Find a field by name in the class hierarchy using ASM.
     */
    private Field findField(Class<?> clazz, String fieldName) {
        return AsmCoreUtils.findDeclaredField(clazz, fieldName);
    }
    
    /**
     * Validate a field with a specific constraint annotation.
     */
    @SuppressWarnings("unchecked")
    private <T> void validateFieldWithConstraint(T object, Field field, Object fieldValue, String propertyPath,
                                                Class<? extends Annotation> annotationType, Annotation annotation,
                                                ConstraintValidator<?, ?> validator, ViolationReport<T> report) {
        
        try {
            // Initialize validator if needed
            if (validator instanceof DefaultValidator.HasInitialize) {
                ((DefaultValidator.HasInitialize) validator).initialize();
            }
            
            // Check if validation passes
            boolean isValid = ((ConstraintValidator<Object, Annotation>) validator).isValid(fieldValue, annotation);
            
            if (!isValid) {
                // Create violation
                String message = getFormattedMessage(validator, annotation, field.getName(), fieldValue);
                String constraintTypeName = AsmCoreUtils.getAnnotationSimpleName(annotationType);
                
                ConstraintViolation<T> violation = new ConstraintViolation<>(
                    message,
                    annotation.toString(),
                    object,
                    propertyPath,
                    fieldValue,
                    constraintTypeName,
                    field.getName()
                );
                
                report.addViolation(violation);
                logger.fine("Validation violation: " + violation);
            }
            
        } catch (Exception e) {
            logger.warning("Error validating field " + field.getName() + " with constraint " + 
                          AsmCoreUtils.getAnnotationSimpleName(annotationType) + ": " + e.getMessage());
        }
    }
    
    /**
     * Validate nested object marked with @Valid.
     */
    private <T> void validateNestedObject(Object object, String propertyPath,
                                        ViolationReport<T> report, Class<?>[] validationGroups) {
        if (object == null) {
            return; // null objects are considered valid
        }
        
        // Recursively validate the nested object
        Class<?> objectClass = object.getClass();
        String basePath = propertyPath;
        
        // Get all declared fields (including inherited) using ASM
        List<Field> allFields = AsmCoreUtils.asList(AsmCoreUtils.getAllFields(objectClass));
        for (Field field : allFields) {
            @SuppressWarnings("unchecked")
            T typedObject = (T) object;
            validateField(typedObject, objectClass, field.getName(), 
                        basePath + "." + field.getName(), report, validationGroups);
        }
    }
    
    /**
     * Get formatted message from validator with field name and value.
     */
    private String getFormattedMessage(ConstraintValidator<?, ?> validator, Annotation annotation, 
                                      String fieldName, Object invalidValue) {
        try {
            // Use ASM to call the generic getMessage method
            return (String) AsmCoreUtils.invokeMethod(validator, "getMessage", annotation, fieldName, invalidValue);
        } catch (Exception e) {
            // Fallback to annotation message
            try {
                return (String) AsmCoreUtils.invokeMethod(annotation, "message");
            } catch (Exception ex) {
                return "Validation failed";
            }
        }
    }
    
    /**
     * Interface for validators that need initialization.
     */
    public interface HasInitialize {
        void initialize();
    }
}