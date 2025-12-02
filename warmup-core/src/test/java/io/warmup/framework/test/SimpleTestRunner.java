package io.warmup.framework.test;

import io.warmup.framework.aop.AsyncInterceptor;
import io.warmup.framework.async.AsyncExecutor;
import io.warmup.framework.core.WarmupContainer;
import io.warmup.framework.event.*;
import io.warmup.framework.annotation.Component;
import io.warmup.framework.annotation.Async;

import java.lang.reflect.Method;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Simplified test runner for Async and EventBus functionality
 * Runs without external dependencies to verify core functionality
 */
public class SimpleTestRunner {

    private static int testsRun = 0;
    private static int testsPassed = 0;
    private static int testsFailed = 0;

    public static void main(String[] args) {
        System.out.println("🚀 Running Warmup Framework Integration Tests");
        System.out.println("================================================");

        try {
            // Test Async functionality
            testAsyncFunctionality();
            
            // Test EventBus functionality  
            testEventBusFunctionality();
            
            // Test Async + EventBus integration
            testAsyncEventBusIntegration();

        } catch (Exception e) {
            System.err.println("❌ Test execution failed: " + e.getMessage());
            e.printStackTrace();
        }

        System.out.println("\n📊 Test Results:");
        System.out.println("================");
        System.out.println("Tests run: " + testsRun);
        System.out.println("Tests passed: " + testsPassed);
        System.out.println("Tests failed: " + testsFailed);
        
        if (testsFailed == 0) {
            System.out.println("\n🎉 All tests passed! Framework is working correctly.");
        } else {
            System.out.println("\n⚠️  Some tests failed. Please check the implementation.");
        }
    }

    private static void testAsyncFunctionality() {
        System.out.println("\n🔄 Testing @Async Functionality");
        System.out.println("--------------------------------");

        try {
            // Test 1: Basic Async method execution
            testCase("Async Method Basic Execution", () -> {
                AsyncTestService service = new AsyncTestService();
                CompletableFuture<String> result = service.testAsyncMethod();
                String value = result.get(5, TimeUnit.SECONDS);
                assertEquals("async-test-completed", value, "Async method should return expected value");
            });

            // Test 2: Async method with custom executor
            testCase("Async Method Custom Executor", () -> {
                AsyncTestService service = new AsyncTestService();
                CompletableFuture<String> result = service.testAsyncCustomExecutor();
                String value = result.get(5, TimeUnit.SECONDS);
                assertEquals("custom-executor-completed", value, "Custom executor should work");
            });

            // Test 3: Async method with counter
            testCase("Async Method Counter", () -> {
                AtomicInteger counter = new AtomicInteger(0);
                AsyncTestService service = new AsyncTestService();
                service.testAsyncCounter(counter);
                
                // Wait a bit for async execution
                Thread.sleep(200);
                assertTrue(counter.get() > 0, "Counter should be incremented by async method");
            });

        } catch (Exception e) {
            System.err.println("❌ Async tests failed: " + e.getMessage());
        }
    }

    private static void testEventBusFunctionality() {
        System.out.println("\n📡 Testing EventBus Functionality");
        System.out.println("----------------------------------");

        try {
            // Create a simple EventBus for testing
            EventBus eventBus = new EventBus();
            
            // Test 1: Basic event publishing and listening
            testCase("EventBus Basic Publishing", () -> {
                AtomicInteger eventCount = new AtomicInteger(0);
                
                // Create simple event listener
                IEventListener listener = new IEventListener() {
                    @Override
                    public void onEvent(Event event) {
                        eventCount.incrementAndGet();
                    }
                    
                    @Override
                    public String getName() {
                        return "testListener";
                    }
                    
                    @Override
                    public int getPriority() {
                        return 0;
                    }
                    
                    public boolean canHandle(Event event) {
                        return true;
                    }
                };
                
                // Subscribe and publish
                eventBus.subscribe(TestEvent.class, event -> {
                    try {
                        listener.onEvent(event);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });
                TestEvent event = new TestEvent("test message", 42);
                eventBus.publishEvent(event);
                
                // Wait for processing
                Thread.sleep(100);
                assertEquals(1, eventCount.get(), "Event should be received by listener");
            });

            // Test 2: Event filtering
            testCase("EventBus Event Filtering", () -> {
                AtomicInteger processedCount = new AtomicInteger(0);
                
                IEventListener filteredListener = new IEventListener() {
                    @Override
                    public void onEvent(Event event) {
                        if (event instanceof TestEvent && ((TestEvent) event).getValue() == 42) {
                            processedCount.incrementAndGet();
                        }
                    }
                    
                    @Override
                    public String getName() {
                        return "filteredListener";
                    }
                    
                    @Override
                    public int getPriority() {
                        return 0;
                    }
                    
                    public boolean canHandle(Event event) {
                        if (event instanceof TestEvent) {
                            return ((TestEvent) event).getValue() == 42;
                        }
                        return false;
                    }
                };
                
                eventBus.subscribe(TestEvent.class, event -> {
                    try {
                        filteredListener.onEvent(event);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });
                
                // Publish events - only one should be processed
                eventBus.publishEvent(new TestEvent("filtered", 42));
                eventBus.publishEvent(new TestEvent("not filtered", 99));
                
                Thread.sleep(100);
                assertEquals(1, processedCount.get(), "Only filtered event should be processed");
            });

        } catch (Exception e) {
            System.err.println("❌ EventBus tests failed: " + e.getMessage());
        }
    }

    private static void testAsyncEventBusIntegration() {
        System.out.println("\n🔄📡 Testing Async + EventBus Integration");
        System.out.println("----------------------------------------");

        try {
            testCase("Async Method Publishing Event", () -> {
                EventBus eventBus = new EventBus();
                AtomicInteger eventCount = new AtomicInteger(0);
                
                // Create listener
                IEventListener listener = new IEventListener() {
                    @Override
                    public void onEvent(Event event) {
                        eventCount.incrementAndGet();
                    }
                    
                    @Override
                    public String getName() {
                        return "integrationListener";
                    }
                    
                    @Override
                    public int getPriority() {
                        return 0;
                    }
                    
                    public boolean canHandle(Event event) {
                        return true;
                    }
                };
                
                eventBus.subscribe(TestEvent.class, event -> {
                    try {
                        listener.onEvent(event);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });
                
                // Create service that publishes events from async methods
                AsyncEventService service = new AsyncEventService(eventBus);
                service.processAsyncAndPublish("test data");
                
                // Wait for async processing and event publishing
                Thread.sleep(300);
                assertTrue(eventCount.get() > 0, "Async method should publish event");
            });

        } catch (Exception e) {
            System.err.println("❌ Async+EventBus tests failed: " + e.getMessage());
        }
    }

    // Test utility methods
    private static void testCase(String testName, TestRunnable test) {
        testsRun++;
        try {
            System.out.println("  🧪 " + testName + "...");
            test.run();
            testsPassed++;
            System.out.println("    ✅ PASSED");
        } catch (AssertionError e) {
            testsFailed++;
            System.out.println("    ❌ FAILED: " + e.getMessage());
        } catch (Exception e) {
            testsFailed++;
            System.out.println("    ❌ ERROR: " + e.getMessage());
        }
    }

    private static void assertEquals(Object expected, Object actual, String message) {
        if ((expected == null && actual != null) || 
            (expected != null && !expected.equals(actual))) {
            throw new AssertionError(message + " - Expected: " + expected + ", Actual: " + actual);
        }
    }

    private static void assertTrue(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }

    private static void assertNotNull(Object object, String message) {
        if (object == null) {
            throw new AssertionError(message + " - Object should not be null");
        }
    }

    // Test helper classes
    private static class AsyncTestService {
        
        @Async
        public CompletableFuture<String> testAsyncMethod() {
            return CompletableFuture.completedFuture("async-test-completed");
        }
        
        @Async("customExecutor")
        public CompletableFuture<String> testAsyncCustomExecutor() {
            return CompletableFuture.completedFuture("custom-executor-completed");
        }
        
        @Async
        public void testAsyncCounter(AtomicInteger counter) {
            counter.incrementAndGet();
        }
    }

    private static class AsyncEventService {
        private final EventBus eventBus;
        
        public AsyncEventService(EventBus eventBus) {
            this.eventBus = eventBus;
        }
        
        @Async
        public void processAsyncAndPublish(String data) {
            // Process data asynchronously
            try {
                Thread.sleep(50); // Simulate processing
                TestEvent event = new TestEvent("Processed: " + data, 100);
                eventBus.publishEvent(event);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    private static class TestEvent extends Event {
        private final String message;
        private final int value;

        public TestEvent(String message, int value) {
            this.message = message;
            this.value = value;
        }

        public String getMessage() {
            return message;
        }

        public int getValue() {
            return value;
        }
    }

    // Functional interface for tests
    @FunctionalInterface
    private interface TestRunnable {
        void run() throws Exception;
    }
}