package io.warmup.framework.validation;

import java.lang.annotation.Annotation;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

/**
 * Factory for creating and managing validator instances.
 * Ensures validators are properly initialized for each constraint.
 * 
 * @author MiniMax Agent
 * @since 1.0
 */
public class ValidatorFactory {
    
    private static final Logger logger = Logger.getLogger(ValidatorFactory.class.getName());
    
    private final Map<Class<?>, ValidatorInstance<?>> validators;
    
    public ValidatorFactory() {
        this.validators = new ConcurrentHashMap<>();
    }
    
    /**
     * Get or create a validator instance for the given constraint type and annotation.
     * 
     * @param constraintType the constraint annotation type
     * @param annotation the constraint annotation instance
     * @return the validator instance
     */
    @SuppressWarnings("unchecked")
    public <T, A extends Annotation> ValidatorInstance<T> getValidator(Class<A> constraintType, A annotation) {
        ValidatorInstance<?> rawInstance = validators.get(constraintType);
        ValidatorInstance<T> instance = (ValidatorInstance<T>) rawInstance;
        
        if (instance == null) {
            ValidatorInstance<?> newInstance = createValidatorInstance(constraintType);
            validators.put(constraintType, newInstance);
            instance = (ValidatorInstance<T>) newInstance;
        }
        
        // Initialize the validator with the specific annotation
        instance.initialize(annotation);
        
        return instance;
    }
    
    /**
     * Create a validator instance for the given constraint type.
     */
    @SuppressWarnings("unchecked")
    private <A extends Annotation> ValidatorInstance<?> createValidatorInstance(Class<A> constraintType) {
        if (constraintType == io.warmup.framework.annotation.validation.NotNull.class) {
            return new ValidatorInstance<>(new io.warmup.framework.validation.validators.NotNullValidator());
        } else if (constraintType == io.warmup.framework.annotation.validation.Size.class) {
            return new ValidatorInstance<>(new io.warmup.framework.validation.validators.SizeValidator());
        } else if (constraintType == io.warmup.framework.annotation.validation.Pattern.class) {
            return new ValidatorInstance<>(new io.warmup.framework.validation.validators.PatternValidator());
        } else {
            throw new IllegalArgumentException("No validator registered for constraint type: " + constraintType.getName());
        }
    }
    
    /**
     * Wrapper class for validator instances that handles initialization.
     */
    public static class ValidatorInstance<T> {
        private final ConstraintValidator<T, ?> validator;
        
        public ValidatorInstance(ConstraintValidator<T, ?> validator) {
            this.validator = validator;
        }
        
        @SuppressWarnings("unchecked")
        public void initialize(Annotation annotation) {
            // Call the initialize method with the specific annotation type
            try {
                java.lang.reflect.Method initMethod = validator.getClass()
                    .getMethod("initialize", annotation.annotationType());
                initMethod.invoke(validator, annotation);
            } catch (Exception e) {
                // If initialize method doesn't exist or fails, ignore
                logger.fine("Validator initialization failed or not supported: " + e.getMessage());
            }
        }
        
        @SuppressWarnings("unchecked")
        public boolean isValid(Object value, Annotation annotation) {
            try {
                return ((ConstraintValidator<T, Annotation>) validator).isValid((T) value, annotation);
            } catch (Exception e) {
                logger.warning("Validation error: " + e.getMessage());
                return false; // Consider invalid if there's an error
            }
        }
        
        public String getMessage(Annotation annotation, String fieldName, Object invalidValue) {
            try {
                java.lang.reflect.Method getMessageMethod = validator.getClass()
                    .getMethod("getMessage", annotation.annotationType(), String.class, Object.class);
                return (String) getMessageMethod.invoke(validator, annotation, fieldName, invalidValue);
            } catch (Exception e) {
                // Fallback to annotation message
                try {
                    java.lang.reflect.Method messageMethod = annotation.annotationType().getMethod("message");
                    return (String) messageMethod.invoke(annotation);
                } catch (Exception ex) {
                    return "Validation failed";
                }
            }
        }
    }
}