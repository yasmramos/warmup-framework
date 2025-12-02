package io.warmup.framework.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to mark a method as an aspect that should be executed after a
 * matched method execution throws an exception.
 *
 * <p>
 * This annotation is used in aspect-oriented programming to define advice that
 * runs only when a target method exits by throwing an exception. It provides
 * access to the thrown exception for error handling, logging, or recovery
 * purposes.
 *
 * <p>
 * <b>Key Features:</b>
 * <ul>
 * <li>Executes only when method exits with an exception (not on successful
 * return)</li>
 * <li>Provides access to the thrown exception through method parameters</li>
 * <li>Supports pointcut expressions to match specific method executions</li>
 * <li>Can filter by exception type using pointcut expressions</li>
 * </ul>
 *
 * <p>
 * <b>Parameters:</b>
 * <ul>
 * <li><b>pointcut</b> - Expression defining which method executions to
 * match</li>
 * <li><b>throwing</b> - Parameter name in the advice method that will receive
 * the thrown exception</li>
 * </ul>
 *
 * <p>
 * <b>Usage Examples:</b>
 * <pre>
 * // Basic exception logging
 * {@literal @}AfterThrowing(pointcut = "execution(* com.example.Service.*(..))")
 * public void logException() {
 *     System.out.println("Exception occurred in service method");
 * }
 *
 * // Accessing thrown exception with default parameter name
 * {@literal @}AfterThrowing(
 *     pointcut = "execution(* com.example.Service.*(..))"
 * )
 * public void logExceptionDetails(Exception ex) {
 *     System.out.println("Exception caught: " + ex.getMessage());
 *     ex.printStackTrace();
 * }
 *
 * // Accessing exception with custom parameter name
 * {@literal @}AfterThrowing(
 *     pointcut = "execution(* com.example.Service.*(..))",
 *     throwing = "exception"
 * )
 * public void handleServiceException(RuntimeException exception) {
 *     System.out.println("Runtime exception: " + exception.getMessage());
 *     // Custom exception handling logic
 * }
 *
 * // Filtering by specific exception type in pointcut
 * {@literal @}AfterThrowing(
 *     pointcut = "execution(* com.example.Service.*(..)) && args(..) throws IOException",
 *     throwing = "ioEx"
 * )
 * public void handleIOException(IOException ioEx) {
 *     System.out.println("IO Exception handled: " + ioEx.getMessage());
 *     // IO-specific error handling
 * }
 * </pre>
 *
 * <p>
 * <b>Method Requirements:</b>
 * <ul>
 * <li>Advice method must be {@code public}</li>
 * <li>Method can have a parameter to receive the thrown exception (must match
 * 'throwing' name)</li>
 * <li>Method return type is typically {@code void}</li>
 * <li>Parameter type should be compatible with the thrown exception type (use
 * {@code Exception} or {@code Throwable} for any exception)</li>
 * </ul>
 *
 * <p>
 * <b>Important Notes:</b>
 * <ul>
 * <li>This advice does NOT prevent the exception from propagating - it only
 * executes after the exception is thrown</li>
 * <li>To handle exceptions and prevent propagation, use {@code @Around} advice
 * instead</li>
 * <li>The default 'throwing' value is "ex" if not specified</li>
 * <li>Pointcut expressions can include exception type filters using
 * {@code throws} clause</li>
 * </ul>
 *
 * @see After
 * @see AfterReturning
 * @see Before
 * @see Around
 *
 * @author Warmup Framework
 * @version 1.0
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface AfterThrowing {

    /**
     * Pointcut expression defining the join points where this advice should be
     * applied. The expression follows pointcut syntax to match method
     * executions based on method patterns, class patterns, annotations, etc.
     * Can include exception type filters to match only specific exceptions.
     *
     * @return the pointcut expression string
     */
    String pointcut() default "";

    /**
     * Name of the parameter in the advice method that will bind the thrown
     * exception from the target method execution. This allows the advice method
     * to access and process the exception.
     *
     * <p>
     * If specified, the advice method must have a parameter with exactly this
     * name. The parameter type should be compatible with the exception type
     * thrown by the target method. Default value is "ex".
     *
     * @return the name of the throwing parameter
     */
    String throwing() default "ex";
}
