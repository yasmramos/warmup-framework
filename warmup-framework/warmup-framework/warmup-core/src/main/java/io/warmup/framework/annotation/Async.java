package io.warmup.framework.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation indicating that a method should be executed asynchronously.
 * Methods annotated with {@code @Async} will be executed in a separate thread
 * using the specified executor.
 *
 * <p>
 * Usage examples:
 * <pre>
 * // Basic async execution with default executor
 * {@literal @}Async
 * public void processDataAsync(String data) {
 *     // This will run in a separate thread
 * }
 *
 * // Custom executor with timeout
 * {@literal @}Async(value = "customExecutor", timeout = 5000)
 * public CompletableFuture<String> fetchDataAsync(String url) {
 *     // This will use custom executor and timeout after 5 seconds
 * }
 *
 * // Async with exception handling policy
 * {@literal @}Async(exceptionHandling = ExceptionHandling.RETURN_NULL)
 * public String safeAsyncOperation() {
 *     // If exception occurs, returns null instead of throwing
 * }
 * </pre>
 *
 * @author Warmup Framework
 * @version 1.0
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Async {

    /**
     * The name of the executor to use for asynchronous execution. If not
     * specified, the default executor will be used.
     *
     * @return the executor name, defaults to "default"
     */
    String value() default "default";

    /**
     * Timeout in milliseconds for the asynchronous operation. A value of 0
     * means no timeout (wait indefinitely).
     *
     * @return timeout in milliseconds, defaults to 0 (no timeout)
     */
    long timeout() default 0;

    /**
     * Number of retry attempts for failed asynchronous operations.
     *
     * @return number of retries, defaults to 0 (no retries)
     */
    int retryCount() default 0;

    /**
     * Delay in milliseconds between retry attempts.
     *
     * @return retry delay in milliseconds, defaults to 1000ms
     */
    long retryDelayMs() default 1000;

    /**
     * Defines how exceptions should be handled during asynchronous execution.
     *
     * @return the exception handling strategy, defaults to
     * COMPLETE_EXCEPTIONALLY
     */
    ExceptionHandling exceptionHandling() default ExceptionHandling.COMPLETE_EXCEPTIONALLY;

    /**
     * Enumeration defining exception handling strategies for asynchronous
     * methods.
     */
    enum ExceptionHandling {
        /**
         * Completes the future exceptionally with the thrown exception. This is
         * the default behavior and follows CompletableFuture conventions.
         */
        COMPLETE_EXCEPTIONALLY,
        /**
         * Returns a null value when an exception occurs. Useful for
         * fire-and-forget scenarios where exceptions should be suppressed.
         */
        RETURN_NULL,
        /**
         * Retries the operation when an exception occurs. Note: This feature is
         * planned for future implementation.
         */
        RETRY
    }
}
