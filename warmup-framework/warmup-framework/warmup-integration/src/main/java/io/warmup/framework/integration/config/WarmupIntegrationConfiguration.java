package io.warmup.framework.integration.config;

import io.warmup.framework.integration.spring.WarmupValidationAutoConfiguration;
import io.warmup.framework.integration.spring.SpringValidationBridge;
import io.warmup.framework.integration.jakartaee.WarmupValidatorFactory;
import io.warmup.framework.integration.hibernate.WarmupHibernateValidatorFactory;
import io.warmup.framework.validation.Validator;
import io.warmup.framework.validation.DefaultValidator;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;

import jakarta.validation.ValidatorFactory;

/**
 * Main configuration class for all Warmup Framework integrations.
 * Provides unified configuration for Spring Boot, Jakarta EE, and Hibernate Validator.
 * 
 * @author MiniMax Agent
 * @since 1.0
 */
@Configuration
@EnableConfigurationProperties(WarmupIntegrationProperties.class)
public class WarmupIntegrationConfiguration {

    /**
     * Primary Validator bean that works with all frameworks.
     */
    @Bean
    @Primary
    public Validator warmupValidator() {
        return new DefaultValidator();
    }

    /**
     * Jakarta EE ValidatorFactory for Java EE environments.
     */
    @Bean
    public ValidatorFactory jakartaValidatorFactory() {
        return new WarmupValidatorFactory();
    }

    /**
     * Hibernate Validator factory for advanced validation scenarios.
     */
    @Bean
    public WarmupHibernateValidatorFactory hibernateValidatorFactory() {
        return new WarmupHibernateValidatorFactory();
    }

    /**
     * Spring validation bridge for Spring Boot applications.
     */
    @Bean
    public SpringValidationBridge springValidationBridge() {
        return new SpringValidationBridge();
    }

    /**
     * Integration manager that coordinates all validation systems.
     */
    @Bean
    public WarmupIntegrationManager integrationManager(Validator validator,
                                                      ValidatorFactory jakartaFactory,
                                                      WarmupHibernateValidatorFactory hibernateFactory,
                                                      SpringValidationBridge springBridge) {
        return new WarmupIntegrationManager(validator, jakartaFactory, hibernateFactory, springBridge);
    }

    /**
     * Configuration properties for integration settings.
     */
    @Bean
    public WarmupIntegrationProperties integrationProperties() {
        return new WarmupIntegrationProperties();
    }
}

/**
 * Manages coordination between different validation frameworks.
 * Provides unified API for using Warmup Framework with Spring, Jakarta EE, and Hibernate.
 * 
 * @author MiniMax Agent
 * @since 1.0
 */
class WarmupIntegrationManager {
    
    private final Validator warmupValidator;
    private final ValidatorFactory jakartaValidatorFactory;
    private final WarmupHibernateValidatorFactory hibernateValidatorFactory;
    private final SpringValidationBridge springValidationBridge;
    
    public WarmupIntegrationManager(Validator validator,
                                  ValidatorFactory jakartaFactory,
                                  WarmupHibernateValidatorFactory hibernateFactory,
                                  SpringValidationBridge springBridge) {
        this.warmupValidator = validator;
        this.jakartaValidatorFactory = jakartaFactory;
        this.hibernateValidatorFactory = hibernateFactory;
        this.springValidationBridge = springBridge;
    }
    
    /**
     * Get the Warmup Framework validator for direct use.
     */
    public Validator getWarmupValidator() {
        return warmupValidator;
    }
    
    /**
     * Get Jakarta EE validator for Java EE compatibility.
     */
    public jakarta.validation.Validator getJakartaValidator() {
        return jakartaValidatorFactory.getValidator();
    }
    
    /**
     * Get Hibernate validator for advanced validation features.
     */
    public org.hibernate.validator.HibernateValidator getHibernateValidator() {
        return hibernateValidatorFactory.usingContext();
    }
    
    /**
     * Get Spring validation bridge for Spring Boot integration.
     */
    public SpringValidationBridge getSpringValidationBridge() {
        return springValidationBridge;
    }
    
    /**
     * Validate an object using the preferred validation framework.
     * 
     * @param object object to validate
     * @param validationFramework preferred framework (WARMUP, JAKARTA, HIBERNATE, SPRING)
     * @return validation result
     */
    public WarmupValidationResult validate(Object object, ValidationFramework framework) {
        if (object == null) {
            return WarmupValidationResult.valid();
        }
        
        switch (framework) {
            case WARMUP:
                boolean warmupValid = warmupValidator.isValid(object);
                return warmupValid ? 
                    WarmupValidationResult.valid() : 
                    WarmupValidationResult.invalid(warmupValidator.getViolationReport(object));
                    
            case JAKARTA:
                var jakartaViolations = jakartaValidatorFactory.getValidator().validate(object);
                return jakartaViolations.isEmpty() ? 
                    WarmupValidationResult.valid() : 
                    WarmupValidationResult.invalid(jakartaViolations);
                    
            case HIBERNATE:
                var hibernateViolations = hibernateValidatorFactory.usingContext().validate(object);
                return hibernateViolations.isEmpty() ? 
                    WarmupValidationResult.valid() : 
                    WarmupValidationResult.invalid(hibernateViolations);
                    
            case SPRING:
                var springBinding = springValidationBridge.validateWithSpringBinding(object, "object");
                return springBinding.getAllErrors().isEmpty() ? 
                    WarmupValidationResult.valid() : 
                    WarmupValidationResult.invalid(springBinding.getAllErrors());
                    
            default:
                throw new IllegalArgumentException("Unsupported validation framework: " + framework);
        }
    }
    
    /**
     * Supported validation frameworks.
     */
    public enum ValidationFramework {
        WARMUP,
        JAKARTA,
        HIBERNATE,
        SPRING
    }
}

/**
 * Result wrapper for validation operations across different frameworks.
 * 
 * @author MiniMax Agent
 * @since 1.0
 */
class WarmupValidationResult {
    private final boolean valid;
    private final Object violations;
    
    private WarmupValidationResult(boolean valid, Object violations) {
        this.valid = valid;
        this.violations = violations;
    }
    
    public static WarmupValidationResult valid() {
        return new WarmupValidationResult(true, null);
    }
    
    public static WarmupValidationResult invalid(Object violations) {
        return new WarmupValidationResult(false, violations);
    }
    
    public boolean isValid() {
        return valid;
    }
    
    public Object getViolations() {
        return violations;
    }
}

/**
 * Configuration properties for Warmup Framework integrations.
 * 
 * @author MiniMax Agent
 * @since 1.0
 */
class WarmupIntegrationProperties {
    
    private boolean validationEnabled = true;
    private boolean springIntegrationEnabled = true;
    private boolean jakartaIntegrationEnabled = true;
    private boolean hibernateIntegrationEnabled = true;
    private boolean autoConfigurationEnabled = true;
    private String defaultValidationFramework = "WARMUP";
    
    public boolean isValidationEnabled() {
        return validationEnabled;
    }
    
    public void setValidationEnabled(boolean validationEnabled) {
        this.validationEnabled = validationEnabled;
    }
    
    public boolean isSpringIntegrationEnabled() {
        return springIntegrationEnabled;
    }
    
    public void setSpringIntegrationEnabled(boolean springIntegrationEnabled) {
        this.springIntegrationEnabled = springIntegrationEnabled;
    }
    
    public boolean isJakartaIntegrationEnabled() {
        return jakartaIntegrationEnabled;
    }
    
    public void setJakartaIntegrationEnabled(boolean jakartaIntegrationEnabled) {
        this.jakartaIntegrationEnabled = jakartaIntegrationEnabled;
    }
    
    public boolean isHibernateIntegrationEnabled() {
        return hibernateIntegrationEnabled;
    }
    
    public void setHibernateIntegrationEnabled(boolean hibernateIntegrationEnabled) {
        this.hibernateIntegrationEnabled = hibernateIntegrationEnabled;
    }
    
    public boolean isAutoConfigurationEnabled() {
        return autoConfigurationEnabled;
    }
    
    public void setAutoConfigurationEnabled(boolean autoConfigurationEnabled) {
        this.autoConfigurationEnabled = autoConfigurationEnabled;
    }
    
    public String getDefaultValidationFramework() {
        return defaultValidationFramework;
    }
    
    public void setDefaultValidationFramework(String defaultValidationFramework) {
        this.defaultValidationFramework = defaultValidationFramework;
    }
}
