package io.warmup.framework.integration.spring;

import io.warmup.framework.validation.Validator;
import io.warmup.framework.validation.DefaultValidator;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import jakarta.validation.ValidatorFactory;

/**
 * Auto-configuration for Warmup Framework integration with Spring Boot.
 * Provides automatic setup of validation beans and validators.
 * 
 * @author MiniMax Agent
 * @since 1.0
 */
@Configuration
@ConditionalOnClass(Validator.class)
@ConditionalOnProperty(name = "warmup.validation.enabled", havingValue = "true", matchIfMissing = true)
public class WarmupValidationAutoConfiguration {

    /**
     * Primary Validator bean for Spring Boot integration.
     * This allows Spring's @Validated annotation and JSR-303 validation
     * to work seamlessly with Warmup Framework.
     */
    @Bean
    @Primary
    @ConditionalOnMissingBean
    public Validator warmupValidator() {
        return new DefaultValidator();
    }

    /**
     * Warmup Validator Factory for Jakarta EE compatibility.
     * Bridges between Jakarta Validation and Warmup Framework.
     */
    @Bean
    @ConditionalOnMissingBean
    public ValidatorFactory warmupValidatorFactory() {
        return new WarmupValidatorFactory();
    }

    /**
     * Configuration for Spring's ValidationUtils with Warmup Framework.
     */
    @Bean
    @ConditionalOnMissingBean
    public SpringValidationBridge springValidationBridge() {
        return new SpringValidationBridge();
    }
}
