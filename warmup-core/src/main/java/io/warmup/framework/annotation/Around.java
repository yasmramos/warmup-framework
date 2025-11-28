package io.warmup.framework.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to mark a method as an aspect that wraps around a matched method
 * execution, providing full control over the method invocation.
 *
 * <p>
 * This annotation is used in aspect-oriented programming to define advice that
 * surrounds a join point. Around advice is the most powerful type of advice, as
 * it can perform custom behavior before and after the method execution, and
 * even control whether the method executes at all.
 *
 * <p>
 * <b>Key Features:</b>
 * <ul>
 * <li>Full control over whether and when the target method executes</li>
 * <li>Ability to modify arguments, return value, or suppress exceptions</li>
 * <li>Can execute custom code before and after the method invocation</li>
 * <li>Can handle exceptions and prevent propagation</li>
 * <li>Can short-circuit the method execution and return a custom value</li>
 * </ul>
 *
 * <p>
 * <b>Method Requirements:</b>
 * <ul>
 * <li>Advice method must be {@code public}</li>
 * <li>Method must have a {@link ProceedingJoinPoint} parameter (or framework
 * equivalent)</li>
 * <li>Method must call {@code proceed()} on the join point to invoke the target
 * method</li>
 * <li>Method return type should match the target method's return type</li>
 * </ul>
 *
 * <p>
 * <b>Usage Examples:</b>
 * <pre>
 * // Basic around advice with timing
 * {@literal @}Around("execution(* com.example.Service.*(..))")
 * public Object measureExecutionTime(ProceedingJoinPoint joinPoint) throws Throwable {
 *     long start = System.currentTimeMillis();
 *     try {
 *         return joinPoint.proceed(); // Execute the target method
 *     } finally {
 *         long duration = System.currentTimeMillis() - start;
 *         System.out.println("Method executed in: " + duration + "ms");
 *     }
 * }
 *
 * // Around advice with conditional execution
 * {@literal @}Around("execution(* com.example.Service.getData(..))")
 * public Object cacheOrExecute(ProceedingJoinPoint joinPoint) throws Throwable {
 *     String cacheKey = generateCacheKey(joinPoint);
 *     Object cached = cache.get(cacheKey);
 *     if (cached != null) {
 *         return cached; // Return cached value without executing method
 *     }
 *     Object result = joinPoint.proceed(); // Execute method and cache result
 *     cache.put(cacheKey, result);
 *     return result;
 * }
 *
 * // Exception handling around advice
 * {@literal @}Around("execution(* com.example.Service.riskyOperation(..))")
 * public Object handleExceptions(ProceedingJoinPoint joinPoint) {
 *     try {
 *         return joinPoint.proceed();
 *     } catch (SpecificException ex) {
 *         // Handle exception and return fallback value
 *         return getFallbackValue();
 *     }
 *     // Other exceptions will propagate normally
 * }
 *
 * // Argument modification
 * {@literal @}Around("execution(* com.example.Service.process(..)) && args(input)")
 * public Object validateAndProcess(ProceedingJoinPoint joinPoint, String input) throws Throwable {
 *     if (input == null || input.trim().isEmpty()) {
 *         throw new IllegalArgumentException("Input cannot be empty");
 *     }
 *     // Modify arguments before proceeding
 *     Object[] args = joinPoint.getArgs();
 *     args[0] = input.trim(); // Clean up input
 *     return joinPoint.proceed(args); // Proceed with modified arguments
 * }
 * </pre>
 *
 * <p>
 * <b>JoinPoint Methods:</b>
 * The {@code ProceedingJoinPoint} parameter provides access to:
 * <ul>
 * <li>{@code proceed()} - Execute the target method</li>
 * <li>{@code proceed(Object[] args)} - Execute with modified arguments</li>
 * <li>{@code getArgs()} - Get method arguments</li>
 * <li>{@code getTarget()} - Get target object</li>
 * <li>{@code getSignature()} - Get method signature</li>
 * </ul>
 *
 * <p>
 * <b>Important Notes:</b>
 * <ul>
 * <li>Around advice MUST call {@code proceed()} to execute the target method
 * (unless intentionally short-circuiting)</li>
 * <li>Return value should be compatible with the target method's return
 * type</li>
 * <li>Exception handling is the responsibility of the around advice</li>
 * <li>Multiple around advices can form an invocation chain</li>
 * </ul>
 *
 * @see Before
 * @see After
 * @see AfterReturning
 * @see AfterThrowing
 * @see ProceedingJoinPoint
 *
 * @author Warmup Framework
 * @version 1.0
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Around {

    /**
     * Pointcut expression defining the join points where this advice should be
     * applied. The expression follows pointcut syntax to match method
     * executions based on method patterns, class patterns, annotations, etc.
     *
     * <p>
     * This is an alias for {@code pointcut()} attribute and follows the same
     * semantics.
     *
     * @return the pointcut expression string
     */
    String value() default "";
}
