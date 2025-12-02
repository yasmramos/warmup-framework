package io.warmup.framework.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to mark a method as a test teardown method that should be executed
 * after each test case in a test class.
 * 
 * <p>Methods annotated with {@code @After} will be run automatically after the execution
 * of each test method in the test class. This is typically used for cleanup operations
 * such as releasing resources, resetting state, or closing database connections.
 * 
 * <p>The annotation supports an optional value parameter that can be used to specify
 * a particular test method or group that this teardown method should run after.
 * When no value is specified, the method will run after every test method in the class.
 * 
 * <p><b>Usage Examples:</b>
 * <pre>
 * // Run after every test method
 * {@literal @}After
 * public void tearDown() {
 *     // Cleanup code here
 * }
 * 
 * // Run only after specific test methods
 * {@literal @}After("testDatabaseOperation")
 * public void cleanupDatabase() {
 *     // Database cleanup code
 * }
 * </pre>
 * 
 * <p><b>Requirements:</b>
 * <ul>
 *   <li>Annotated methods must be {@code public}</li>
 *   <li>Annotated methods must take no parameters</li>
 *   <li>Annotated methods must return {@code void}</li>
 *   <li>Multiple {@code @After} methods are supported and will be executed in deterministic order</li>
 * </ul>
 * 
 * @see org.junit.After
 * @see Before
 * @see Test
 * 
 * @author Warmup Framework
 * @version 1.0
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface After {
    /**
     * Optional specification of which test method(s) this teardown method should run after.
     * When empty (default), the method runs after every test method in the class.
     * 
     * @return the name of the test method or test group that this teardown method applies to
     */
    String value() default "";
}