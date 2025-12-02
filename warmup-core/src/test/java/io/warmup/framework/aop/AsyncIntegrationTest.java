package io.warmup.framework.aop;

import io.warmup.framework.annotation.Async;
import io.warmup.framework.annotation.Component;
import io.warmup.framework.core.Warmup;
import io.warmup.framework.asm.AsmCoreUtils;
import io.warmup.framework.core.optimized.ManagerFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for @Async AOP functionality
 * Tests complete integration with AspectManager and AsyncExecutor
 */
public class AsyncIntegrationTest {

    private Warmup warmup;
    private AsyncTestService testService;

    @BeforeEach
    void setUp() throws Exception {
        // 🚀 SETUP FRESH: Asegurar contexto completamente nuevo para cada test
        warmup = Warmup.create();

        // 🔍 Solo escanear el paquete específico del test para evitar interferencias
        warmup.scanPackages("io.warmup.framework.aop");
        warmup.getContainer().start();

        // ✅ AUTOMATIC AOP: Registrar bean manualmente - AOP se aplica automáticamente
        // 🎯 Crear nueva instancia para evitar estado compartido
        AsyncTestService serviceInstance = new AsyncTestService();
        warmup.registerBean("asyncTestService", AsyncTestService.class, serviceInstance);

        // 🎯 Obtener el bean decorado con AOP
        testService = warmup.getBean(AsyncTestService.class);

        // ✅ AOP proxy generado automáticamente por Warmup framework
        System.out.println("🔍 [DEBUG] AOP Setup Complete - testService class: " + testService.getClass().getName());
    }

    @AfterEach
    void tearDown() throws Exception {
        // 🧹 LIMPIAR ESTADO: Cerrar correctamente Warmup para evitar interferencia entre tests
        if (warmup != null) {
            warmup.stop();
        }
    }

    @Test
    @DisplayName("Test @Async method execution with default executor")
    void testAsyncMethodDefaultExecutor() {
        // Given
        AtomicInteger counter = new AtomicInteger(0);

        // When - Execute async method
        CompletableFuture<String> result = testService.asyncMethodWithCounter(counter);

        // Then - Wait for completion and verify result
        assertDoesNotThrow(() -> {
            String value = result.get(5, TimeUnit.SECONDS);
            assertEquals("Counter incremented to: 1", value);
            assertEquals(1, counter.get());
        });
    }

    @Test
    @DisplayName("Test @Async method with custom executor name")
    void testAsyncMethodCustomExecutor() {
        // Given
        AtomicInteger counter = new AtomicInteger(0);

        // When - Execute async method with custom executor
        CompletableFuture<String> result = testService.asyncMethodCustomExecutor(counter);

        // Then - Verify execution
        assertDoesNotThrow(() -> {
            String value = result.get(5, TimeUnit.SECONDS);
            assertEquals("Executed with custom executor", value);
        });
    }

    @Test
    @DisplayName("Test @Async method with timeout")
    void testAsyncMethodWithTimeout() {
        // When - Execute async method with short timeout
        CompletableFuture<String> result = testService.asyncMethodWithTimeout();

        // Then - Should complete successfully within timeout
        assertDoesNotThrow(() -> {
            String value = result.get(3, TimeUnit.SECONDS);
            assertEquals("Quick execution", value);
        });
    }

    @Test
    @DisplayName("Test @Async method with timeout exceeded")
    void testAsyncMethodTimeoutExceeded() {
        // When - Execute async method that will timeout
        CompletableFuture<String> result = testService.asyncMethodWithLongExecution();

        // Then - Should throw ExecutionException wrapping TimeoutException (standard CompletableFuture behavior)
        ExecutionException exception = assertThrows(ExecutionException.class, () -> {
            result.get(1, TimeUnit.SECONDS);
        });
        
        // Verify the cause is TimeoutException
        assertTrue(exception.getCause() instanceof TimeoutException, 
                   "Expected TimeoutException as cause of ExecutionException");
    }

    @Test
    @DisplayName("Test @Async method with exception handling - PROPAGATE")
    void testAsyncMethodExceptionPropagate() {
        System.out.println("🔍 DEBUG: testAsyncMethodExceptionPropagate - bean class: " + testService.getClass().getSimpleName());

        // When - Execute async method that throws exception
        CompletableFuture<String> result = testService.asyncMethodWithException();

        // Then - Should propagate exception
        assertThrows(ExecutionException.class, () -> {
            result.get(5, TimeUnit.SECONDS);
        });
    }

    @Test
    @DisplayName("Test @Async method with exception handling - IGNORE")
    void testAsyncMethodExceptionIgnore() {
        System.out.println("🔍 DEBUG: testAsyncMethodExceptionIgnore - bean class: " + testService.getClass().getSimpleName());

        // When - Execute async method that throws exception with IGNORE handling
        CompletableFuture<String> result = testService.asyncMethodWithExceptionIgnore();

        // Then - Should return default value or null
        assertDoesNotThrow(() -> {
            String value = result.get(5, TimeUnit.SECONDS);
            assertNull(value); // Expected for IGNORE handling
        });
    }

    @Test
    @DisplayName("Test @Async method returning void")
    void testAsyncMethodVoidReturn() {
        // Given
        AtomicInteger counter = new AtomicInteger(0);

        // When - Execute void async method
        CompletableFuture<Void> result = testService.asyncVoidMethod(counter);

        // Then - Should complete successfully
        assertDoesNotThrow(() -> {
            result.get(5, TimeUnit.SECONDS);
            // Small delay to ensure async execution completes
            Thread.sleep(100);
            assertTrue(counter.get() > 0);
        });
    }

    @Test
    @DisplayName("Test multiple @Async methods executing concurrently")
    void testMultipleAsyncMethods() {
        // Given
        AtomicInteger counter1 = new AtomicInteger(0);
        AtomicInteger counter2 = new AtomicInteger(0);
        AtomicInteger counter3 = new AtomicInteger(0);

        // When - Execute multiple async methods concurrently
        CompletableFuture<String> result1 = testService.asyncMethodWithCounter(counter1);
        CompletableFuture<String> result2 = testService.asyncMethodWithCounter(counter2);
        CompletableFuture<String> result3 = testService.asyncMethodWithCounter(counter3);

        // Then - All should complete successfully
        assertDoesNotThrow(() -> {
            String value1 = result1.get(5, TimeUnit.SECONDS);
            String value2 = result2.get(5, TimeUnit.SECONDS);
            String value3 = result3.get(5, TimeUnit.SECONDS);

            assertEquals("Counter incremented to: 1", value1);
            assertEquals("Counter incremented to: 1", value2);
            assertEquals("Counter incremented to: 1", value3);
        });
    }

    @Test
    @DisplayName("Test @Async method with complex return type")
    void testAsyncMethodComplexReturn() {
        // When - Execute async method returning complex object
        CompletableFuture<AsyncTestService.TestResult> result = testService.asyncMethodComplexReturn();

        // Then - Should return complex result
        assertDoesNotThrow(() -> {
            AsyncTestService.TestResult testResult = result.get(5, TimeUnit.SECONDS);
            assertNotNull(testResult);
            assertEquals("test", testResult.getName());
            assertEquals(42, testResult.getValue());
        });
    }

    @Test
    @DisplayName("Test @Async method accessibility and method resolution")
    void testAsyncMethodAccessibility() {
        // Test private method accessibility
        assertDoesNotThrow(() -> {
            CompletableFuture<String> result = testService.asyncPrivateMethod();
            String value = result.get(5, TimeUnit.SECONDS);
            assertEquals("Private async method executed", value);
        });
    }

    @Component
    public static class AsyncTestService {

        @Async
        public CompletableFuture<String> asyncMethodWithCounter(AtomicInteger counter) {
            counter.incrementAndGet();
            return CompletableFuture.completedFuture("Counter incremented to: " + counter.get());
        }

        @Async("customExecutor")
        public CompletableFuture<String> asyncMethodCustomExecutor(AtomicInteger counter) {
            counter.incrementAndGet();
            return CompletableFuture.completedFuture("Executed with custom executor");
        }

        @Async(timeout = 2000)
        public CompletableFuture<String> asyncMethodWithTimeout() {
            return CompletableFuture.completedFuture("Quick execution");
        }

        @Async(timeout = 500)
        public CompletableFuture<String> asyncMethodWithLongExecution() {
            try {
                Thread.sleep(1000); // Will timeout
                return CompletableFuture.completedFuture("Long execution");
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                CompletableFuture<String> future = new CompletableFuture<>();
                future.completeExceptionally(e);
                return future;
            }
        }

        @Async(exceptionHandling = Async.ExceptionHandling.COMPLETE_EXCEPTIONALLY)
        public CompletableFuture<String> asyncMethodWithException() {
            throw new RuntimeException("Test exception");
        }

        @Async(exceptionHandling = Async.ExceptionHandling.RETURN_NULL)
        public CompletableFuture<String> asyncMethodWithExceptionIgnore() {
            throw new RuntimeException("Test exception to ignore");
        }

        @Async
        public CompletableFuture<Void> asyncVoidMethod(AtomicInteger counter) {
            counter.incrementAndGet();
            return CompletableFuture.completedFuture(null);
        }

        @Async
        public CompletableFuture<TestResult> asyncMethodComplexReturn() {
            return CompletableFuture.completedFuture(new TestResult("test", 42));
        }

        @Async
        public CompletableFuture<String> asyncPrivateMethod() {
            return CompletableFuture.completedFuture("Private async method executed");
        }

        public static class TestResult {
            private final String name;
            private final int value;

            public TestResult(String name, int value) {
                this.name = name;
                this.value = value;
            }

            public String getName() {
                return name;
            }

            public int getValue() {
                return value;
            }
        }
    }
}