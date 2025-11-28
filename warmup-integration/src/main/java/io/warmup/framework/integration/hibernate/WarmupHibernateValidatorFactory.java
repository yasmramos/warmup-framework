package io.warmup.framework.integration.hibernate;

import io.warmup.framework.validation.Validator;
import io.warmup.framework.validation.ViolationReport;
import org.hibernate.validator.HibernateValidator;
import org.hibernate.validator.HibernateValidatorFactory;
import org.hibernate.validator.cfg.ConstraintMapping;
import org.hibernate.validator.cfg.defs.NotNullDef;
import org.hibernate.validator.cfg.defs.SizeDef;
import org.hibernate.validator.internal.engine.ConfigurationImpl;
import org.hibernate.validator.internal.engine ConstraintViolationImpl;
import org.hibernate.validator.internal.engine.path.NodeImpl;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.MessageInterpolator;
import jakarta.validation.ParameterNameProvider;
import jakarta.validation.TraversableResolver;
import jakarta.validation.ValidatorFactory;
import jakarta.validation.spi.ValidationProvider;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Hibernate Validator bridge implementation that allows Hibernate Validator
 * to use Warmup Framework for validation logic while maintaining compatibility
 * with Hibernate's advanced features.
 * 
 * @author MiniMax Agent
 * @since 1.0
 */
public class WarmupHibernateValidatorFactory implements HibernateValidatorFactory {
    
    private final Validator warmupValidator;
    private final HibernateValidatorFactory hibernateFactory;
    
    public WarmupHibernateValidatorFactory() {
        this.warmupValidator = new io.warmup.framework.validation.DefaultValidator();
        // Create a basic Hibernate Validator Factory for fallback
        var configuration = new ConfigurationImpl();
        configuration.messageInterpolator(new WarmupMessageInterpolator());
        configuration.parameterNameProvider(new WarmupParameterNameProvider());
        configuration.traversableResolver(new WarmupTraversableResolver());
        this.hibernateFactory = configuration.buildValidatorFactory();
    }
    
    public WarmupHibernateValidatorFactory(Validator validator) {
        this.warmupValidator = validator;
        var configuration = new ConfigurationImpl();
        configuration.messageInterpolator(new WarmupMessageInterpolator());
        configuration.parameterNameProvider(new WarmupParameterNameProvider());
        configuration.traversableResolver(new WarmupTraversableResolver());
        this.hibernateFactory = configuration.buildValidatorFactory();
    }
    
    @Override
    public HibernateValidator usingContext() {
        return new WarmupHibernateValidator(warmupValidator, hibernateFactory.usingContext());
    }
    
    @Override
    public ValidatorFactory unwrap(Class<?> type) {
        return hibernateFactory.unwrap(type);
    }
    
    @Override
    public void close() {
        hibernateFactory.close();
    }
    
    /**
     * Hibernate Validator implementation using Warmup Framework.
     */
    private static class WarmupHibernateValidator implements HibernateValidator {
        
        private final Validator warmupValidator;
        private final HibernateValidator.ValidatorContext context;
        
        public WarmupHibernateValidator(Validator warmupValidator, HibernateValidator.ValidatorContext context) {
            this.warmupValidator = warmupValidator;
            this.context = context;
        }
        
        @Override
        public Set<ConstraintViolation<Object>> validate(Object object, Class<?>... groups) {
            if (object == null) {
                return Set.of();
            }
            
            ViolationReport<Object> report = warmupValidator.getViolationReport(object);
            
            return report.getViolations().stream()
                .map(violation -> createHibernateViolation(violation))
                .collect(Collectors.toSet());
        }
        
        @Override
        public Set<ConstraintViolation<Object>> validateProperty(Object object, String propertyName, Class<?>... groups) {
            if (object == null || propertyName == null) {
                return Set.of();
            }
            
            ViolationReport<Object> report = warmupValidator.getViolationReport(object);
            
            return report.getViolations().stream()
                .filter(violation -> violation.getPropertyPath().equals(propertyName))
                .map(violation -> createHibernateViolation(violation))
                .collect(Collectors.toSet());
        }
        
        @Override
        public Set<ConstraintViolation<Object>> validateValue(Class<?> beanType, String propertyName, Object value, Class<?>... groups) {
            if (beanType == null || propertyName == null) {
                return Set.of();
            }
            
            try {
                Object instance = beanType.getDeclaredConstructor().newInstance();
                setProperty(instance, propertyName, value);
                return validate(instance, groups);
            } catch (Exception e) {
                return Set.of();
            }
        }
        
        @Override
        public HibernateValidator ignoreXmlConfiguration() {
            return new WarmupHibernateValidator(warmupValidator, context.ignoreXmlConfiguration());
        }
        
        @Override
        public HibernateValidator messageInterpolator(MessageInterpolator interpolator) {
            return new WarmupHibernateValidator(warmupValidator, context.messageInterpolator(interpolator));
        }
        
        @Override
        public HibernateValidator traversableResolver(TraversableResolver resolver) {
            return new WarmupHibernateValidator(warmupValidator, context.traversableResolver(resolver));
        }
        
        @Override
        public HibernateValidator parameterNameProvider(ParameterNameProvider provider) {
            return new WarmupHibernateValidator(warmupValidator, context.parameterNameProvider(provider));
        }
        
        @Override
        public HibernateValidator constraintValidatorFactory(org.hibernate.validator.spi.ConstraintValidatorFactory constraintValidatorFactory) {
            return new WarmupHibernateValidator(warmupValidator, context.constraintValidatorFactory(constraintValidatorFactory));
        }
        
        @Override
        public HibernateValidator addMapping(ConstraintMapping mapping) {
            return new WarmupHibernateValidator(warmupValidator, context.addMapping(mapping));
        }
        
        @Override
        public HibernateValidator failFast(boolean failFast) {
            return new WarmupHibernateValidator(warmupValidator, context.failFast(failFast));
        }
        
        @Override
        public HibernateValidator allowOverridingMethodDefaultConstraintMappings(boolean allowOverridingMethodDefaultConstraintMappings) {
            return new WarmupHibernateValidator(warmupValidator, 
                context.allowOverridingMethodDefaultConstraintMappings(allowOverridingMethodDefaultConstraintMappings));
        }
        
        @Override
        public HibernateValidator allowMultipleCascadedValidationOnReturnValues(boolean allowMultipleCascadedValidationOnReturnValues) {
            return new WarmupHibernateValidator(warmupValidator, 
                context.allowMultipleCascadedValidationOnReturnValues(allowMultipleCascadedValidationOnReturnValues));
        }
        
        @Override
        public HibernateValidator allowParallelMethodsDefineCrossParameterConstraints(boolean allowParallelMethodsDefineCrossParameterConstraints) {
            return new WarmupHibernateValidator(warmupValidator, 
                context.allowParallelMethodsDefineCrossParameterConstraints(allowParallelMethodsDefineCrossParameterConstraints));
        }
        
        @Override
        public HibernateValidator constraintValidatorFactory(org.hibernate.validator.spi.ConstraintValidatorFactory constraintValidatorFactory) {
            return new WarmupHibernateValidator(warmupValidator, context.constraintValidatorFactory(constraintValidatorFactory));
        }
        
        private ConstraintViolation<Object> createHibernateViolation(io.warmup.framework.validation.ConstraintViolation<Object> violation) {
            try {
                var violationClass = Class.forName("org.hibernate.validator.internal.engine.ConstraintViolationImpl");
                var constructor = violationClass.getDeclaredConstructor(
                    String.class, String.class, String.class, Object.class, Object.class,
                    Object.class, Object.class, Class.class, Class.class,
                    Collections.class, Collections.class, Collections.class,
                    String.class, String.class, Collections.class
                );
                
                return (ConstraintViolation<Object>) constructor.newInstance(
                    violation.getMessage(), // messageTemplate
                    violation.getMessage(), // interpolatedMessage
                    violation.getRootObject().getClass().getName(), // rootBeanClassName
                    violation.getRootObject(), // rootBean
                    violation.getPropertyPath(), // propertyPath
                    violation.getInvalidValue(), // invalidValue
                    violation.getRootObject(), // leafBean
                    violation.getRootObjectType(), // rootBeanClass
                    violation.getConstraintType(), // constraintClass
                    Collections.emptyList(), // constraintDescriptor
                    Collections.emptyList(), // messageParameters
                    Collections.emptyList(), // messageVariables
                    violation.getPropertyPath(), // propertyName
                    violation.getMessage(), // messageTemplate
                    Collections.emptyList() // dynamicPayload
                );
            } catch (Exception e) {
                // Fallback to simple violation
                return new WarmupHibernateViolation(violation);
            }
        }
        
        private void setProperty(Object object, String propertyName, Object value) {
            try {
                Field field = object.getClass().getDeclaredField(propertyName);
                field.setAccessible(true);
                field.set(object, value);
            } catch (Exception e) {
                // Ignore if property can't be set
            }
        }
    }
    
    /**
     * Basic Hibernate ConstraintViolation implementation.
     */
    private static class WarmupHibernateViolation implements ConstraintViolation<Object> {
        
        private final io.warmup.framework.validation.ConstraintViolation<Object> violation;
        
        public WarmupHibernateViolation(io.warmup.framework.validation.ConstraintViolation<Object> violation) {
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
        public Object getRootBean() {
            return violation.getRootObject();
        }
        
        @Override
        public Class<Object> getRootBeanClass() {
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
        public Object unwrap(Class<?> type) {
            if (type.isInstance(this)) {
                return type.cast(this);
            }
            throw new IllegalArgumentException("Cannot unwrap to type: " + type);
        }
    }
    
    /**
     * Custom message interpolator that can process messages using Warmup Framework logic.
     */
    private static class WarmupMessageInterpolator implements MessageInterpolator {
        
        @Override
        public String interpolate(String messageTemplate, Context context) {
            // For now, return the message as-is
            // Could be enhanced to process variables in the message
            return messageTemplate;
        }
        
        @Override
        public String interpolate(String messageTemplate, Context context, java.util.Locale locale) {
            return interpolate(messageTemplate, context);
        }
    }
    
    /**
     * Parameter name provider using Java 8+ parameter reflection.
     */
    private static class WarmupParameterNameProvider implements ParameterNameProvider {
        
        @Override
        public java.util.List<String> getParameterNames(java.lang.reflect.Method method) {
            if (method.getParameterCount() == 0) {
                return java.util.Collections.emptyList();
            }
            
            return java.util.stream.IntStream.range(0, method.getParameterCount())
                .mapToObj(i -> "arg" + i)
                .collect(java.util.stream.Collectors.toList());
        }
        
        @Override
        public java.util.List<String> getParameterNames(java.lang.reflect.Constructor<?> constructor) {
            if (constructor.getParameterCount() == 0) {
                return java.util.Collections.emptyList();
            }
            
            return java.util.stream.IntStream.range(0, constructor.getParameterCount())
                .mapToObj(i -> "arg" + i)
                .collect(java.util.stream.Collectors.toList());
        }
    }
    
    /**
     * Basic traversable resolver that checks if properties are traversable.
     */
    private static class WarmupTraversableResolver implements TraversableResolver {
        
        @Override
        public boolean isReachable(Object traversableObject, String traversableProperty, Class<?> rootBeanType, String pathToTraversableObject, jakarta.validation.Path.Node traversablePropertyNode, jakarta.validation.ElementKind traversablePropertyElementKind) {
            // Assume all properties are reachable
            return true;
        }
        
        @Override
        public boolean isCascadable(Object traversableObject, String traversableProperty, Class<?> rootBeanType, String pathToTraversableObject, jakarta.validation.Path.Node traversablePropertyNode, jakarta.validation.ElementKind traversablePropertyElementKind) {
            // Assume properties marked with @Valid are cascadable
            try {
                Field field = rootBeanType.getDeclaredField(traversableProperty);
                return field.isAnnotationPresent(jakarta.validation.Valid.class);
            } catch (Exception e) {
                return false;
            }
        }
    }
}
