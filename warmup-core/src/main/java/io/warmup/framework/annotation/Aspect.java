package io.warmup.framework.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to mark a class as an aspect containing cross-cutting concerns.
 * 
 * <p>Classes annotated with {@code @Aspect} define modularized cross-cutting functionality
 * that can be applied across multiple parts of an application. Aspects contain advice
 * methods annotated with various advice annotations ({@code @Before}, {@code @After},
 * {@code @Around}, etc.) and pointcut expressions that define where the advice should be applied.
 * 
 * <p><b>Key Concepts:</b>
 * <ul>
 *   <li><b>Aspect</b> - A modularization of a concern that cuts across multiple classes (this annotation)</li>
 *   <li><b>Advice</b> - Action taken by an aspect at a particular join point ({@code @Before}, {@code @After}, etc.)</li>
 *   <li><b>Pointcut</b> - Expression that matches join points where advice should be applied</li>
 *   <li><b>Join Point</b> - A point during program execution, such as method execution</li>
 * </ul>
 * 
 * <p><b>Aspect Class Requirements:</b>
 * <ul>
 *   <li>Must be a concrete class (not interface or abstract class in most implementations)</li>
 *   <li>Should contain one or more advice methods with advice annotations</li>
 *   <li>May contain pointcut definitions and helper methods</li>
 *   <li>Typically managed by the framework (instantiated, dependency injection, etc.)</li>
 * </ul>
 * 
 * <p><b>Usage Examples:</b>
 * <pre>
 * // Basic logging aspect
 * {@literal @}Aspect
 * public class LoggingAspect {
 *     
 *     {@literal @}Before("execution(* com.example.service.*.*(..))")
 *     public void logMethodCall(JoinPoint joinPoint) {
 *         System.out.println("Calling method: " + joinPoint.getSignature().getName());
 *     }
 *     
 *     {@literal @}Around("execution(* com.example.service.*.*(..))")
 *     public Object measurePerformance(ProceedingJoinPoint joinPoint) throws Throwable {
 *         long start = System.currentTimeMillis();
 *         try {
 *             return joinPoint.proceed();
 *         } finally {
 *             long duration = System.currentTimeMillis() - start;
 *             System.out.println("Execution time: " + duration + "ms");
 *         }
 *     }
 * }
 * 
 * // Security aspect with pointcut reuse
 * {@literal @}Aspect
 * public class SecurityAspect {
 *     
 *     {@literal @}Pointcut("execution(* com.example.service.AdminService.*(..))")
 *     public void adminOperations() {}
 *     
 *     {@literal @}Pointcut("execution(* com.example.service.UserService.*(..))")
 *     public void userOperations() {}
 *     
 *     {@literal @}Before("adminOperations()")
 *     public void checkAdminPermissions(JoinPoint joinPoint) {
 *         if (!SecurityContext.isAdmin()) {
 *             throw new SecurityException("Admin permissions required");
 *         }
 *     }
 *     
 *     {@literal @}Before("userOperations()")
 *     public void checkUserAuthentication(JoinPoint joinPoint) {
 *         if (!SecurityContext.isAuthenticated()) {
 *             throw new SecurityException("Authentication required");
 *         }
 *     }
 * }
 * 
 * // Exception handling aspect
 * {@literal @}Aspect
 * public class ExceptionHandlingAspect {
 *     
 *     {@literal @}AfterThrowing(
 *         pointcut = "execution(* com.example.integration.*.*(..))", 
 *         throwing = "ex"
 *     )
 *     public void handleIntegrationExceptions(Exception ex) {
 *         System.err.println("Integration exception occurred: " + ex.getMessage());
 *         // Additional exception handling logic
 *     }
 *     
 *     {@literal @}Around("execution(* com.example.service.*.*(..))")
 *     public Object retryOnFailure(ProceedingJoinPoint joinPoint) throws Throwable {
 *         int maxAttempts = 3;
 *         int attempt = 0;
 *         while (attempt < maxAttempts) {
 *             try {
 *                 return joinPoint.proceed();
 *             } catch (TemporaryException ex) {
 *                 attempt++;
 *                 if (attempt == maxAttempts) throw ex;
 *                 Thread.sleep(1000 * attempt); // Exponential backoff
 *             }
 *         }
 *         return null; // Should not reach here
 *     }
 * }
 * </pre>
 * 
 * <p><b>Common Aspect Use Cases:</b>
 * <ul>
 *   <li><b>Logging</b> - Method call tracing and execution logging</li>
 *   <li><b>Security</b> - Authentication and authorization checks</li>
 *   <li><b>Transactions</b> - Transaction management and boundaries</li>
 *   <li><b>Performance Monitoring</b> - Execution timing and metrics collection</li>
 *   <li><b>Caching</b> - Result caching and cache management</li>
 *   <li><b>Error Handling</b> - Exception translation and retry logic</li>
 *   <li><b>Validation</b> - Input validation and sanitization</li>
 * </ul>
 * 
 * <p><b>Framework Integration:</b>
 * Aspects are typically detected and processed by the framework's aspect weaver,
 * which can be implemented through:
 * <ul>
 *   <li>Compile-time weaving (during compilation)</li>
 *   <li>Load-time weaving (during class loading)</li>
 *   <li>Runtime weaving (through proxies)</li>
 * </ul>
 * 
 * @see Before
 * @see After
 * @see Around
 * @see AfterReturning
 * @see AfterThrowing
 * @see Pointcut
 * @see JoinPoint
 * @see ProceedingJoinPoint
 * 
 * @author Warmup Framework
 * @version 1.0
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Aspect {
}