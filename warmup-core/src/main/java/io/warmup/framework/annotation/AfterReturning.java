package io.warmup.framework.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to mark a method as an aspect that should be executed after the
 * successful return of a matched method execution.
 *
 * <p>
 * This annotation is used in aspect-oriented programming to define advice that
 * runs only when a target method completes normally and returns a value. It
 * provides access to the returned value for post-processing or logging
 * purposes.
 *
 * <p>
 * <b>Key Features:</b>
 * <ul>
 * <li>Executes only on successful method completion (not when exceptions are
 * thrown)</li>
 * <li>Provides access to the return value through method parameters</li>
 * <li>Supports pointcut expressions to match specific method executions</li>
 * </ul>
 *
 * <p>
 * <b>Parameters:</b>
 * <ul>
 * <li><b>pointcut</b> - Expression defining which method executions to
 * match</li>
 * <li><b>returning</b> - Parameter name in the advice method that will receive
 * the return value</li>
 * </ul>
 *
 * <p>
 * <b>Usage Examples:</b>
 * <pre>
 * // Basic usage with pointcut expression
 * {@literal @}AfterReturning(pointcut = "execution(* com.example.Service.*(..))")
 * public void logSuccessfulExecution() {
 *     System.out.println("Method executed successfully");
 * }
 *
 * // Accessing return value
 * {@literal @}AfterReturning(
 *     pointcut = "execution(* com.example.Service.getUser(..))",
 *     returning = "user"
 * )
 * public void logUserRetrieval(Object user) {
 *     System.out.println("Retrieved user: " + user);
 * }
 *
 * // With specific return type
 * {@literal @}AfterReturning(
 *     pointcut = "execution(* com.example.Service.calculate*(..))",
 *     returning = "result"
 * )
 * public void logCalculationResult(Integer result) {
 *     System.out.println("Calculation result: " + result);
 * }
 * </pre>
 *
 * <p>
 * <b>Method Requirements:</b>
 * <ul>
 * <li>Advice method must be {@code public}</li>
 * <li>Method can have a parameter to receive the return value (must match
 * 'returning' name)</li>
 * <li>Method return type is typically {@code void}</li>
 * <li>Parameter type should be compatible with the actual return type (use
 * {@code Object} for any type)</li>
 * </ul>
 *
 * <p>
 * <b>Note:</b> If the 'returning' parameter name is specified, the advice
 * method must declare a parameter with the same name to receive the return
 * value. The type of this parameter can be specific or {@code Object} to match
 * any return type.
 *
 * @see After
 * @see AfterThrowing
 * @see Before
 * @see Around
 *
 * @author Warmup Framework
 * @version 1.0
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface AfterReturning {

    /**
     * Pointcut expression defining the join points where this advice should be
     * applied. The expression follows pointcut syntax to match method
     * executions based on method patterns, class patterns, annotations, etc.
     *
     * @return the pointcut expression string
     */
    String pointcut() default "";

    /**
     * Name of the parameter in the advice method that will bind the return
     * value from the target method execution. This allows the advice method to
     * access and process the returned value.
     *
     * <p>
     * If specified, the advice method must have a parameter with exactly this
     * name. The parameter type should be compatible with the return type of the
     * target method.
     *
     * @return the name of the returning parameter
     */
    String returning() default "";
}
