package io.warmup.framework.annotation;

import java.lang.annotation.*;

/**
 * Annotation that indicates a class can be used as a source of bean definitions.
 * Classes annotated with @Configuration are processed by the Warmup container
 * to generate bean definitions from @Bean methods.
 * 
 * This annotation is similar to Spring's @Configuration and Jakarta CDI's @Configuration.
 * 
 * Example usage:
 * <pre>
 * {@literal @}Configuration
 * public class AppConfig {
 *     
 *     {@literal @}Bean
 *     public UserService userService() {
 *         return new UserServiceImpl();
 *     }
 * }
 * </pre>
 * 
 * @author MiniMax Agent
 * @since 1.0
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Component
public @interface Configuration {
    
    /**
     * The bean name prefix for beans defined in this configuration class.
     * If not specified, the configuration class name is used as prefix.
     * 
     * @return the bean name prefix
     */
    String value() default "";
    
    /**
     * Whether the configuration class should be processed in lite mode.
     * In lite mode, @Bean methods are processed as regular methods without
     * creating a proxy. This provides better performance but no method interception.
     * 
     * @return true if lite mode should be used
     */
    boolean lite() default false;
    
    /**
     * The order in which this configuration should be processed.
     * Lower values have higher priority.
     * 
     * @return the order value
     */
    int order() default 0;
}