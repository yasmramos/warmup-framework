package io.warmup.framework.annotation;

import java.lang.annotation.*;

/**
 * Annotation that indicates a method produces a bean to be managed by the Warmup container.
 * Methods annotated with @Bean are processed during configuration class initialization
 * and their return values are registered as beans in the container.
 * 
 * This annotation is similar to Spring's @Bean and Jakarta CDI's @Produces.
 * 
 * Example usage:
 * <pre>
 * {@literal @}Configuration
 * public class AppConfig {
 *     
 *     {@literal @}Bean
 *     {@literal @}Singleton
 *     public UserService userService() {
 *         return new UserServiceImpl();
 *     }
 *     
 *     {@literal @}Bean(name = "customName")
 *     public DataSource dataSource() {
 *         return new HikariDataSource();
 *     }
 * }
 * </pre>
 * 
 * @author MiniMax Agent
 * @since 1.0
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Bean {
    
    /**
     * The bean name. If not specified, the method name is used.
     * 
     * @return the bean name
     */
    String name() default "";
    
    /**
     * Alias for {@link #name()}. If both are specified, name takes precedence.
     * 
     * @return the bean name alias
     */
    String value() default "";
    
    /**
     * The bean init method name. Called after the bean is created and dependencies are injected.
     * 
     * @return the init method name
     */
    String initMethod() default "";
    
    /**
     * The bean destroy method name. Called before the bean is destroyed during container shutdown.
     * 
     * @return the destroy method name
     */
    String destroyMethod() default "";
    
    /**
     * Whether this bean should be lazily initialized.
     * 
     * @return true if the bean should be lazy
     */
    boolean lazy() default false;
    
    /**
     * The bean scope. If not specified, singleton scope is used.
     * Can reference a ScopeType enum value.
     * 
     * @return the scope type
     */
    String scope() default "";
    
    /**
     * Whether this bean should be automatically created during startup.
     * 
     * @return true if eager initialization is desired
     */
    boolean eager() default false;
}