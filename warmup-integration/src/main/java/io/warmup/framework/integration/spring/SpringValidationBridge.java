package io.warmup.framework.integration.spring;

import io.warmup.framework.validation.Validator;
import io.warmup.framework.validation.ViolationReport;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.FieldError;
import org.springframework.validation.ValidatorAdapter;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Bridge between Spring's validation API and Warmup Framework validation.
 * Enables Spring's @Validated annotation to work with Warmup Framework validators.
 * 
 * @author MiniMax Agent
 * @since 1.0
 */
public class SpringValidationBridge {
    
    private final Validator warmupValidator;
    
    public SpringValidationBridge() {
        this.warmupValidator = new DefaultValidator();
    }
    
    public SpringValidationBridge(Validator validator) {
        this.warmupValidator = validator;
    }
    
    /**
     * Validate an object using Warmup Framework and convert results to Spring's BindingResult.
     */
    public BeanPropertyBindingResult validateWithSpringBinding(Object object, String objectName) {
        BeanPropertyBindingResult bindingResult = new BeanPropertyBindingResult(object, objectName);
        
        ViolationReport<Object> report = warmupValidator.getViolationReport(object);
        
        // Convert Warmup violations to Spring field errors
        for (io.warmup.framework.validation.ConstraintViolation<Object> violation : report.getViolations()) {
            String fieldName = violation.getPropertyPath();
            String errorMessage = violation.getMessage();
            Object rejectedValue = violation.getInvalidValue();
            
            FieldError fieldError = new FieldError(
                objectName,
                fieldName,
                rejectedValue,
                false, // bindingFailure
                new String[]{errorMessage},
                null, // codes
                errorMessage,
                null  // arguments
            );
            
            bindingResult.addError(fieldError);
        }
        
        return bindingResult;
    }
    
    /**
     * Check if an object is valid according to Warmup Framework rules.
     */
    public boolean isValid(Object object) {
        if (object == null) {
            return true; // null objects are considered valid by Spring
        }
        
        return warmupValidator.isValid(object);
    }
    
    /**
     * Get validation messages for display purposes.
     */
    public List<String> getValidationMessages(Object object) {
        ViolationReport<Object> report = warmupValidator.getViolationReport(object);
        
        return report.getViolations().stream()
            .map(violation -> violation.getPropertyPath() + ": " + violation.getMessage())
            .collect(Collectors.toList());
    }
    
    /**
     * Validate and return detailed violation information.
     */
    public List<SpringViolation> getDetailedViolations(Object object) {
        ViolationReport<Object> report = warmupValidator.getViolationReport(object);
        
        return report.getViolations().stream()
            .map(violation -> new SpringViolation(
                violation.getPropertyPath(),
                violation.getMessage(),
                violation.getInvalidValue(),
                violation.getConstraintType()
            ))
            .collect(Collectors.toList());
    }
    
    /**
     * DTO for Spring-specific violation information.
     */
    public static class SpringViolation {
        private final String propertyPath;
        private final String message;
        private final Object invalidValue;
        private final Class<?> constraintType;
        
        public SpringViolation(String propertyPath, String message, Object invalidValue, Class<?> constraintType) {
            this.propertyPath = propertyPath;
            this.message = message;
            this.invalidValue = invalidValue;
            this.constraintType = constraintType;
        }
        
        public String getPropertyPath() { return propertyPath; }
        public String getMessage() { return message; }
        public Object getInvalidValue() { return invalidValue; }
        public Class<?> getConstraintType() { return constraintType; }
    }
}
