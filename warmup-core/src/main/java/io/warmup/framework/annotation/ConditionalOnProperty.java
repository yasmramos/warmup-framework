package io.warmup.framework.annotation;

import java.lang.annotation.*;

/**
 * Conditional annotation that allows a @Bean method to be registered only when specific
 * property conditions are met. This annotation supports:
 * - Property name matching
 * - Value comparison (havingValue)
 * - Missing property handling (matchIfMissing)
 * - Property existence checking
 * 
 * Example usage:
 * <pre>
 * {@literal @}Configuration
 * public class AppConfig {
 *     
 *     {@literal @}Bean
 *     {@literal @}ConditionalOnProperty(name = "feature.cache.enabled", havingValue = "true")
 *     public CacheService cacheService() {
 *         return new RedisCacheService();
 *     }
 *     
 *     {@literal @}Bean
 *     {@literal @}ConditionalOnProperty(name = "database.type", havingValue = "mysql")
 *     public DataSource mysqlDataSource() {
 *         return new MysqlDataSource();
 *     }
 *     
 *     {@literal @}Bean
 *     {@literal @}ConditionalOnProperty(name = "feature.oauth.enabled", matchIfMissing = "false")
 *     public OAuthService oauthService() {
 *         return new OAuth2Service();
 *     }
 * }
 * </pre>
 * 
 * @author MiniMax Agent
 * @since 1.0
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Repeatable(ConditionalOnPropertyList.class)
public @interface ConditionalOnProperty {
    
    /**
     * The property name to check. Required.
     * 
     * @return the property name
     */
    String name();
    
    /**
     * The expected property value for the condition to match.
     * If specified, the property value must exactly equal this string.
     * 
     * @return the expected property value
     */
    String havingValue() default "";
    
    /**
     * Whether to match if the property is missing.
     * 
     * @return true to match when property is missing, false otherwise
     */
    boolean matchIfMissing() default false;
    
    /**
     * Whether the property must exist for the condition to match.
     * If true and property doesn't exist, condition fails.
     * 
     * @return true if property existence is required
     */
    boolean requireProperty() default true;
    
    /**
     * Additional property names to check as alternatives.
     * If any of these properties exist with a non-null value, the condition is satisfied.
     * 
     * @return array of alternative property names
     */
    String[] anyOf() default {};
    
    /**
     * Property names that must NOT have the specified value.
     * If any of these properties match the havingValue, the condition fails.
     * 
     * @return array of property names to exclude
     */
    String[] notHavingValue() default {};
    
    /**
     * Whether to invert the overall condition result.
     * If true, the bean is registered when the condition is false.
     * 
     * @return true to invert the condition
     */
    boolean invert() default false;
}