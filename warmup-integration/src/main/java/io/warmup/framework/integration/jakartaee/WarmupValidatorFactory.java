package io.warmup.framework.integration.jakartaee;

import io.warmup.framework.validation.Validator;
import io.warmup.framework.validation.ViolationReport;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validation;
import jakarta.validation.ValidatorFactory;
import jakarta.validation.metadata.BeanDescriptor;
import jakarta.validation.metadata.ConstraintDescriptor;
import jakarta.validation.spi.ValidationProvider;

import java.util.Set;

/**
 * Jakarta EE ValidatorFactory implementation that bridges with Warmup Framework.
 * Provides compatibility with Jakarta Validation API (JSR-380).
 * 
 * @author MiniMax Agent
 * @since 1.0
 */
public class WarmupValidatorFactory implements ValidatorFactory {
    
    private final Validator warmupValidator;
    
    public WarmupValidatorFactory() {
        this.warmupValidator = new io.warmup.framework.validation.DefaultValidator();
    }
    
    public WarmupValidatorFactory(Validator validator) {
        this.warmupValidator = validator;
    }
    
    @Override
    public jakarta.validation.Validator getValidator() {
        return new WarmupJakartaValidator(warmupValidator);
    }
    
    @Override
    public void close() {
        // Clean up resources if needed
    }
    
    /**
     * Jakarta Validator implementation that uses Warmup Framework underneath.
     */
    private static class WarmupJakartaValidator implements jakarta.validation.Validator {
        
        private final Validator warmupValidator;
        
        public WarmupJakartaValidator(Validator warmupValidator) {
            this.warmupValidator = warmupValidator;
        }
        
        @Override
        public <T> Set<ConstraintViolation<T>> validate(T object, Class<?>... groups) {
            if (object == null) {
                return Set.of(); // null objects are considered valid
            }
            
            ViolationReport<T> report = warmupValidator.getViolationReport(object);
            
            return report.getViolations().stream()
                .map(violation -> new WarmupConstraintViolation<>(violation))
                .collect(java.util.stream.Collectors.toSet());
        }
        
        @Override
        public <T> Set<ConstraintViolation<T>> validateProperty(T object, String propertyName, Class<?>... groups) {
            if (object == null || propertyName == null) {
                return Set.of();
            }
            
            ViolationReport<T> report = warmupValidator.getViolationReport(object);
            
            return report.getViolations().stream()
                .filter(violation -> violation.getPropertyPath().equals(propertyName))
                .map(violation -> new WarmupConstraintViolation<>(violation))
                .collect(java.util.stream.Collectors.toSet());
        }
        
        @Override
        public <T> Set<ConstraintViolation<T>> validateValue(Class<T> beanType, String propertyName, Object value, Class<?>... groups) {
            if (beanType == null || propertyName == null) {
                return Set.of();
            }
            
            try {
                T instance = beanType.getDeclaredConstructor().newInstance();
                setProperty(instance, propertyName, value);
                return validate(instance, groups);
            } catch (Exception e) {
                // If we can't create instance, return empty set
                return Set.of();
            }
        }
        
        @Override
        public BeanDescriptor getConstraintsForClass(Class<?> clazz) {
            // Return a basic bean descriptor - could be enhanced to analyze Warmup annotations
            return new WarmupBeanDescriptor(clazz);
        }
        
        @Override
        public <T> T unwrap(Class<T> type) {
            if (type.isInstance(this)) {
                return type.cast(this);
            }
            throw new IllegalArgumentException("Cannot unwrap to type: " + type);
        }
        
        private void setProperty(Object object, String propertyName, Object value) {
            try {
                var field = object.getClass().getDeclaredField(propertyName);
                field.setAccessible(true);
                field.set(object, value);
            } catch (Exception e) {
                // Ignore if property can't be set
            }
        }
    }
    
    /**
     * Jakarta ConstraintViolation implementation using Warmup violation data.
     */
    private static class WarmupConstraintViolation<T> implements ConstraintViolation<T> {
        
        private final io.warmup.framework.validation.ConstraintViolation<T> violation;
        
        public WarmupConstraintViolation(io.warmup.framework.validation.ConstraintViolation<T> violation) {
            this.violation = violation;
        }
        
        @Override
        public String getMessage() {
            return violation.getMessage();
        }
        
        @Override
        public String getMessageTemplate() {
            return violation.getMessage();
        }
        
        @Override
        public T getRootBean() {
            return violation.getRootObject();
        }
        
        @Override
        public Class<T> getRootBeanClass() {
            return violation.getRootObjectType();
        }
        
        @Override
        public Object getInvalidValue() {
            return violation.getInvalidValue();
        }
        
        @Override
        public String getPropertyPath() {
            return violation.getPropertyPath();
        }
        
        @Override
        public ConstraintDescriptor<?> getConstraintDescriptor() {
            // Return a basic constraint descriptor
            return new WarmupConstraintDescriptor(violation);
        }
        
        @Override
        public Object[] getExecutableParameters() {
            return new Object[0]; // Not applicable for bean validation
        }
        
        @Override
        public Object getExecutableReturnValue() {
            return null; // Not applicable for bean validation
        }
        
        @Override
        public Object unwrap(Class<?> type) {
            if (type.isInstance(this)) {
                return type.cast(this);
            }
            throw new IllegalArgumentException("Cannot unwrap to type: " + type);
        }
    }
    
    /**
     * Basic Jakarta ConstraintDescriptor implementation.
     */
    private static class WarmupConstraintDescriptor<T> implements ConstraintDescriptor<T> {
        
        private final io.warmup.framework.validation.ConstraintViolation<T> violation;
        
        public WarmupConstraintDescriptor(io.warmup.framework.validation.ConstraintViolation<T> violation) {
            this.violation = violation;
        }
        
        @Override
        public T getAnnotation() {
            return null; // Not available from Warmup violations
        }
        
        @Override
        public String getMessageTemplate() {
            return violation.getMessage();
        }
        
        @Override
        public Set<Class<?>> getGroups() {
            return Set.of(); // Could be enhanced to track groups
        }
        
        @Override
        public Set<Class<?>> getPayload() {
            return Set.of(); // Could be enhanced to track payloads
        }
        
        @Override
        public jakarta.validation.metadata.ConstraintTarget getValidationTarget() {
            return jakarta.validation.metadata.ConstraintTarget.PROPERTY;
        }
        
        @Override
        public java.util.Map<String, Object> getAttributes() {
            return java.util.Map.of(); // Could be enhanced to include annotation attributes
        }
        
        @Override
        public Set<ConstraintDescriptor<?>> getComposingConstraints() {
            return Set.of(); // No composing constraints
        }
        
        @Override
        public boolean isReportAsSingleViolation() {
            return false;
        }
    }
    
    /**
     * Basic Jakarta BeanDescriptor implementation.
     */
    private static class WarmupBeanDescriptor implements jakarta.validation.metadata.BeanDescriptor {
        
        private final Class<?> beanClass;
        
        public WarmupBeanDescriptor(Class<?> beanClass) {
            this.beanClass = beanClass;
        }
        
        @Override
        public boolean hasConstraints() {
            // Check if class has any validation annotations
            return java.util.Arrays.stream(beanClass.getDeclaredFields())
                .anyMatch(field -> field.isAnnotationPresent(jakarta.validation.constraints.NotNull.class) ||
                                 field.isAnnotationPresent(jakarta.validation.constraints.Size.class) ||
                                 field.isAnnotationPresent(jakarta.validation.constraints.Pattern.class));
        }
        
        @Override
        public jakarta.validation.metadata.PropertyDescriptor getConstraintsForProperty(String propertyName) {
            try {
                var field = beanClass.getDeclaredField(propertyName);
                return new WarmupPropertyDescriptor(field);
            } catch (Exception e) {
                return null;
            }
        }
        
        @Override
        public java.util.Set<String> getConstrainedProperties() {
            return java.util.Arrays.stream(beanClass.getDeclaredFields())
                .filter(field -> field.isAnnotationPresent(jakarta.validation.constraints.NotNull.class) ||
                               field.isAnnotationPresent(jakarta.validation.constraints.Size.class) ||
                               field.isAnnotationPresent(jakarta.validation.constraints.Pattern.class))
                .map(java.lang.reflect.Field::getName)
                .collect(java.util.stream.Collectors.toSet());
        }
        
        @Override
        public jakarta.validation.metadata.ConstraintDescriptor<?> getConstraintDescriptor() {
            return null; // Not applicable for bean descriptor
        }
    }
    
    /**
     * Basic Jakarta PropertyDescriptor implementation.
     */
    private static class WarmupPropertyDescriptor implements jakarta.validation.metadata.PropertyDescriptor {
        
        private final java.lang.reflect.Field field;
        
        public WarmupPropertyDescriptor(java.lang.reflect.Field field) {
            this.field = field;
        }
        
        @Override
        public String getPropertyName() {
            return field.getName();
        }
        
        @Override
        public boolean hasConstraints() {
            return field.isAnnotationPresent(jakarta.validation.constraints.NotNull.class) ||
                   field.isAnnotationPresent(jakarta.validation.constraints.Size.class) ||
                   field.isAnnotationPresent(jakarta.validation.constraints.Pattern.class);
        }
        
        @Override
        public Set<ConstraintDescriptor<?>> getConstraintDescriptors() {
            return java.util.Arrays.stream(field.getAnnotations())
                .filter(annotation -> annotation.annotationType().isAnnotationPresent(jakarta.validation.Constraint.class))
                .map(annotation -> new WarmupConstraintDescriptorAnnotation(annotation))
                .collect(java.util.stream.Collectors.toSet());
        }
        
        @Override
        public jakarta.validation.metadata.ConstraintTarget getValidationTarget() {
            return jakarta.validation.metadata.ConstraintTarget.PROPERTY;
        }
    }
    
    /**
     * Constraint descriptor for actual Jakarta constraint annotations.
     */
    private static class WarmupConstraintDescriptorAnnotation implements ConstraintDescriptor<jakarta.validation.Constraint> {
        
        private final jakarta.validation.Constraint annotation;
        
        public WarmupConstraintDescriptorAnnotation(jakarta.validation.Constraint annotation) {
            this.annotation = annotation;
        }
        
        @Override
        public jakarta.validation.Constraint getAnnotation() {
            return annotation;
        }
        
        @Override
        public String getMessageTemplate() {
            return annotation.message();
        }
        
        @Override
        public Set<Class<?>> getGroups() {
            return java.util.Set.of(annotation.groups());
        }
        
        @Override
        public Set<Class<?>> getPayload() {
            return java.util.Set.of(annotation.payload());
        }
        
        @Override
        public jakarta.validation.metadata.ConstraintTarget getValidationTarget() {
            return jakarta.validation.metadata.ConstraintTarget.PROPERTY;
        }
        
        @Override
        public java.util.Map<String, Object> getAttributes() {
            java.util.Map<String, Object> attributes = new java.util.HashMap<>();
            attributes.put("message", annotation.message());
            attributes.put("groups", annotation.groups());
            attributes.put("payload", annotation.payload());
            return attributes;
        }
        
        @Override
        public Set<ConstraintDescriptor<?>> getComposingConstraints() {
            return java.util.Set.of(); // No composing constraints
        }
        
        @Override
        public boolean isReportAsSingleViolation() {
            return annotation.message().contains("{") || annotation.message().contains("}");
        }
    }
}
