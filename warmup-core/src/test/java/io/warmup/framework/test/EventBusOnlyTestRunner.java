package io.warmup.framework.test;

import io.warmup.framework.event.*;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Ultra-simplified test runner for EventBus functionality only
 * Tests only the EventBus classes we successfully compiled
 */
public class EventBusOnlyTestRunner {

    private static int testsRun = 0;
    private static int testsPassed = 0;
    private static int testsFailed = 0;

    public static void main(String[] args) {
        System.out.println("🚀 Running EventBus-Specific Integration Tests");
        System.out.println("===============================================");

        try {
            testEventBusBasic();
            testEventBusFiltering();
            testEventBusAsyncPublishing();
            testEventBusErrorHandling();

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
            System.out.println("\n🎉 All EventBus tests passed! EventBus implementation is working correctly.");
        } else {
            System.out.println("\n⚠️  Some EventBus tests failed. Please check the implementation.");
        }
    }

    private static void testEventBusBasic() {
        System.out.println("\n📡 Testing EventBus Basic Functionality");
        System.out.println("--------------------------------------");

        try {
            // Test 1: Create EventBus and verify it's instantiable
            testCase("EventBus Instantiation", () -> {
                EventBus eventBus = new EventBus();
                assertNotNull(eventBus, "EventBus should be instantiable");
            });

            // Test 2: Subscribe and publish basic event
            testCase("EventBus Subscribe and Publish", () -> {
                EventBus eventBus = new EventBus();
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
                    
                    @Override
                    public boolean canHandleEvent(Class<?> eventType) {
                        return true;
                    }
                    
                    @Override
                    public boolean isEnabled() {
                        return true;
                    }
                };
                
                // Subscribe to TestEvent
                eventBus.subscribe(TestEvent.class, event -> {
                    try {
                        listener.onEvent(event);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });
                
                // Create and publish test event
                TestEvent event = new TestEvent("test message", 42);
                eventBus.publishEvent(event);
                
                // Verify listener was called
                assertEquals(1, eventCount.get(), "Listener should receive the event");
            });

            // Test 3: Multiple listeners for same event
            testCase("EventBus Multiple Listeners", () -> {
                EventBus eventBus = new EventBus();
                AtomicInteger count1 = new AtomicInteger(0);
                AtomicInteger count2 = new AtomicInteger(0);
                
                IEventListener listener1 = new IEventListener() {
                    @Override
                    public void onEvent(Event event) { count1.incrementAndGet(); }
                    @Override public String getName() { return "listener1"; }
                    @Override public int getPriority() { return 0; }
                    @Override public boolean canHandleEvent(Class<?> eventType) { return true; }
                    @Override public boolean isEnabled() { return true; }
                };
                
                IEventListener listener2 = new IEventListener() {
                    @Override
                    public void onEvent(Event event) { count2.incrementAndGet(); }
                    @Override public String getName() { return "listener2"; }
                    @Override public int getPriority() { return 0; }
                    @Override public boolean canHandleEvent(Class<?> eventType) { return true; }
                    @Override public boolean isEnabled() { return true; }
                };
                
                eventBus.subscribe(TestEvent.class, event -> {
                    try {
                        listener1.onEvent(event);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });
                eventBus.subscribe(TestEvent.class, event -> {
                    try {
                        listener2.onEvent(event);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });
                
                eventBus.publishEvent(new TestEvent("multi-listener test", 100));
                
                assertEquals(1, count1.get(), "Listener 1 should receive event");
                assertEquals(1, count2.get(), "Listener 2 should receive event");
            });

        } catch (Exception e) {
            System.err.println("❌ EventBus basic tests failed: " + e.getMessage());
        }
    }

    private static void testEventBusFiltering() {
        System.out.println("\n🔍 Testing EventBus Filtering");
        System.out.println("-----------------------------");

        try {
            // Test 1: Event type filtering
            testCase("EventBus Event Type Filtering", () -> {
                EventBus eventBus = new EventBus();
                AtomicInteger testEventCount = new AtomicInteger(0);
                AtomicInteger otherEventCount = new AtomicInteger(0);
                
                IEventListener testListener = new IEventListener() {
                    @Override
                    public void onEvent(Event event) { testEventCount.incrementAndGet(); }
                    @Override public String getName() { return "testListener"; }
                    @Override public int getPriority() { return 0; }
                    @Override public boolean canHandleEvent(Class<?> eventType) { 
                        return eventType == TestEvent.class; 
                    }
                    @Override public boolean isEnabled() { return true; }
                };
                
                IEventListener otherListener = new IEventListener() {
                    @Override
                    public void onEvent(Event event) { otherEventCount.incrementAndGet(); }
                    @Override public String getName() { return "otherListener"; }
                    @Override public int getPriority() { return 0; }
                    @Override public boolean canHandleEvent(Class<?> eventType) { 
                        return eventType == OtherEvent.class; 
                    }
                    @Override public boolean isEnabled() { return true; }
                };
                
                eventBus.subscribe(TestEvent.class, event -> {
                    try {
                        testListener.onEvent(event);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });
                eventBus.subscribe(OtherEvent.class, event -> {
                    try {
                        otherListener.onEvent(event);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });
                
                // Publish different event types
                eventBus.publishEvent(new TestEvent("test", 1));
                eventBus.publishEvent(new OtherEvent("other", 2));
                eventBus.publishEvent(new TestEvent("test2", 3));
                
                assertEquals(2, testEventCount.get(), "Should receive only TestEvent");
                assertEquals(1, otherEventCount.get(), "Should receive only OtherEvent");
            });

        } catch (Exception e) {
            System.err.println("❌ EventBus filtering tests failed: " + e.getMessage());
        }
    }

    private static void testEventBusAsyncPublishing() {
        System.out.println("\n🔄 Testing EventBus Async Publishing");
        System.out.println("-----------------------------------");

        try {
            // Test 1: Async event publishing
            testCase("EventBus Async Publishing", () -> {
                EventBus eventBus = new EventBus();
                AtomicInteger asyncCount = new AtomicInteger(0);
                
                IEventListener asyncListener = new IEventListener() {
                    @Override
                    public void onEvent(Event event) { 
                        // Simulate some async processing
                        try {
                            Thread.sleep(50);
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                        }
                        asyncCount.incrementAndGet(); 
                    }
                    @Override public String getName() { return "asyncListener"; }
                    @Override public int getPriority() { return 0; }
                    @Override public boolean canHandleEvent(Class<?> eventType) { return true; }
                    @Override public boolean isEnabled() { return true; }
                };
                
                eventBus.subscribe(TestEvent.class, event -> {
                    try {
                        asyncListener.onEvent(event);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });
                
                // Publish async event
                eventBus.publishEventAsync(new TestEvent("async test", 200));
                
                // Wait for async processing
                Thread.sleep(200);
                
                assertEquals(1, asyncCount.get(), "Async event should be processed");
            });

        } catch (Exception e) {
            System.err.println("❌ EventBus async tests failed: " + e.getMessage());
        }
    }

    private static void testEventBusErrorHandling() {
        System.out.println("\n⚠️  Testing EventBus Error Handling");
        System.out.println("----------------------------------");

        try {
            // Test 1: EventBus unsubscription
            testCase("EventBus Unsubscription", () -> {
                EventBus eventBus = new EventBus();
                AtomicInteger subscribeCount = new AtomicInteger(0);
                
                IEventListener removableListener = new IEventListener() {
                    @Override
                    public void onEvent(Event event) { subscribeCount.incrementAndGet(); }
                    @Override public String getName() { return "removableListener"; }
                    @Override public int getPriority() { return 0; }
                    @Override public boolean canHandleEvent(Class<?> eventType) { return true; }
                    @Override public boolean isEnabled() { return true; }
                };
                
                // Subscribe, publish, then unsubscribe
                eventBus.subscribe(TestEvent.class, event -> {
                    try {
                        removableListener.onEvent(event);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });
                eventBus.publishEvent(new TestEvent("before unsubscribe", 300));
                eventBus.unregisterListener(TestEvent.class, removableListener);
                eventBus.publishEvent(new TestEvent("after unsubscribe", 400));
                
                assertEquals(1, subscribeCount.get(), "Should only receive event before unsubscription");
            });

        } catch (Exception e) {
            System.err.println("❌ EventBus error handling tests failed: " + e.getMessage());
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

    // Test event classes
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

    private static class OtherEvent extends Event {
        private final String name;
        private final int id;

        public OtherEvent(String name, int id) {
            this.name = name;
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public int getId() {
            return id;
        }
    }

    // Functional interface for tests
    @FunctionalInterface
    private interface TestRunnable {
        void run() throws Exception;
    }
}