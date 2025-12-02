package io.warmup.framework.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation indicating that a constructor, field, or method should be injected
 * with dependencies by the dependency injection framework.
 * 
 * <p>This annotation can be applied to:
 * <ul>
 *   <li>Constructors - for constructor injection</li>
 *   <li>Fields - for field injection</li>
 *   <li>Methods - for setter/method injection</li>
 * </ul>
 * 
 * <p>Usage examples:
 * <pre>
 * // Constructor injection
 * {@literal @}Inject
 * public MyService(@Named("default") OtherService service) {
 *     this.service = service;
 * }
 * 
 * // Field injection
 * {@literal @}Inject
 * private DataSource dataSource;
 * 
 * // Method injection
 * {@literal @}Inject
 * public void setLogger(Logger logger) {
 *     this.logger = logger;
 * }
 * </pre>
 * 
 * @author Warmup Framework
 * @version 1.0
 * @see javax.inject.Inject
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.CONSTRUCTOR, ElementType.FIELD, ElementType.METHOD})
public @interface Inject {
    // Marker annotation - no attributes needed
}