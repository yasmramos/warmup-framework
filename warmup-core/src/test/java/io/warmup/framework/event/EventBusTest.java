package io.warmup.framework.event;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Comprehensive tests for EventBus functionality.
 * Tests use the real EventBus API with registerListener, publishEvent, etc.
 * 
 * @author MiniMax Agent
 * @since 1.0
 */
class EventBusTest {

    private EventBus eventBus;
    private ExecutorService executorService;

    @BeforeEach
    void setUp() {
        eventBus = new EventBus();
        executorService = Executors.newCachedThreadPool();
    }

    @AfterEach
    void tearDown() {
        if (executorService != null && !executorService.isShutdown()) {
            executorService.shutdown();
        }
        eventBus.clearAllListeners();
    }

    @Test
    void testBasicEventPublishing() throws InterruptedException {
        // Test basic event publishing and listening using real API
        CountDownLatch latch = new CountDownLatch(1);
        TestEventListener listener = new TestEventListener(latch);
        
        // Use real API: registerListener
        eventBus.registerListener(TestEvent.class, listener);
        
        // Publish event using real API: publishEvent
        TestEvent event = new TestEvent("test message");
        eventBus.publishEvent(event);
        
        // Wait for event processing
        assertTrue(latch.await(1, TimeUnit.SECONDS));
        assertEquals("test message", listener.getLastMessage());
    }

    @Test
    void testMultipleListeners() throws InterruptedException {
        // Test multiple listeners for the same event
        CountDownLatch latch1 = new CountDownLatch(1);
        CountDownLatch latch2 = new CountDownLatch(1);
        
        TestEventListener listener1 = new TestEventListener(latch1);
        TestEventListener listener2 = new TestEventListener(latch2);
        
        eventBus.registerListener(TestEvent.class, listener1);
        eventBus.registerListener(TestEvent.class, listener2);
        
        TestEvent event = new TestEvent("multiple listeners test");
        eventBus.publishEvent(event);
        
        // Both listeners should receive the event
        assertTrue(latch1.await(1, TimeUnit.SECONDS));
        assertTrue(latch2.await(1, TimeUnit.SECONDS));
        assertEquals("multiple listeners test", listener1.getLastMessage());
        assertEquals("multiple listeners test", listener2.getLastMessage());
    }

    @Test
    void testListenerUnregistration() throws InterruptedException {
        // Test listener unregistration
        CountDownLatch latch = new CountDownLatch(1);
        TestEventListener listener = new TestEventListener(latch);
        
        eventBus.registerListener(TestEvent.class, listener);
        eventBus.unregisterListener(TestEvent.class, listener);
        
        TestEvent event = new TestEvent("should not be received");
        eventBus.publishEvent(event);
        
        // Listener should not receive event after unregistration
        assertFalse(latch.await(500, TimeUnit.MILLISECONDS));
        assertNull(listener.getLastMessage());
    }

    @Test
    void testEventListenerCount() {
        // Test listener count tracking
        assertEquals(0, eventBus.getListenerCount());
        
        TestEventListener listener1 = new TestEventListener(new CountDownLatch(1));
        TestEventListener listener2 = new TestEventListener(new CountDownLatch(1));
        
        eventBus.registerListener(TestEvent.class, listener1);
        assertEquals(1, eventBus.getListenerCount());
        
        eventBus.registerListener(TestEvent.class, listener2);
        assertEquals(2, eventBus.getListenerCount());
        
        eventBus.unregisterListener(TestEvent.class, listener1);
        assertEquals(1, eventBus.getListenerCount());
    }

    @Test
    void testAsyncEventPublishing() throws InterruptedException {
        // Test async event publishing
        CountDownLatch latch = new CountDownLatch(1);
        TestEventListener listener = new TestEventListener(latch);
        
        eventBus.registerListener(TestEvent.class, listener);
        
        TestEvent event = new TestEvent("async test");
        eventBus.publishEventAsync(event);
        
        // Wait for async event processing
        assertTrue(latch.await(2, TimeUnit.SECONDS));
        assertEquals("async test", listener.getLastMessage());
    }

    @Test
    void testEventSubscriptionWithLambda() {
        // Test event subscription using lambda (Consumer)
        AtomicInteger callCount = new AtomicInteger(0);
        
        // Use real API: subscribe with Consumer
        eventBus.subscribe(TestEvent.class, event -> {
            callCount.incrementAndGet();
        });
        
        TestEvent event1 = new TestEvent("event 1");
        TestEvent event2 = new TestEvent("event 2");
        
        eventBus.publishEvent(event1);
        eventBus.publishEvent(event2);
        
        assertEquals(2, callCount.get());
    }

    @Test
    void testEventStatistics() throws InterruptedException {
        // Test event statistics tracking
        TestEventListener listener = new TestEventListener(new CountDownLatch(2));
        eventBus.registerListener(TestEvent.class, listener);
        
        TestEvent event1 = new TestEvent("stats test 1");
        TestEvent event2 = new TestEvent("stats test 2");
        
        eventBus.publishEvent(event1);
        eventBus.publishEvent(event2);
        
        // Wait for processing
        Thread.sleep(100);
        
        EventStatistics stats = eventBus.getEventStatistics(TestEvent.class);
        assertNotNull(stats);
        assertEquals(2, stats.getPublishedCount());
    }

    @Test
    void testClearAllListeners() throws InterruptedException {
        // Test clearing all listeners
        CountDownLatch latch = new CountDownLatch(1);
        TestEventListener listener = new TestEventListener(latch);
        
        eventBus.registerListener(TestEvent.class, listener);
        eventBus.registerListener(AnotherTestEvent.class, new EventListener<AnotherTestEvent>() {
            @Override
            public void onEvent(AnotherTestEvent event) {
                latch.countDown();
            }
        });
        
        assertEquals(2, eventBus.getListenerCount());
        
        eventBus.clearAllListeners();
        
        assertEquals(0, eventBus.getListenerCount());
        
        // Events should not be processed after clearing
        eventBus.publishEvent(new TestEvent("should not be received"));
        assertFalse(latch.await(500, TimeUnit.MILLISECONDS));
    }

    @Test
    void testEventWithExceptionHandling() throws InterruptedException {
        // Test that exceptions in listeners don't break other listeners
        CountDownLatch normalLatch = new CountDownLatch(1);
        TestEventListener normalListener = new TestEventListener(normalLatch);
        
        TestEventListener exceptionListener = new TestEventListener(new CountDownLatch(1)) {
            @Override
            public void onEvent(TestEvent event) {
                super.onEvent(event);
                throw new RuntimeException("Test exception in listener");
            }
        };
        
        eventBus.registerListener(TestEvent.class, normalListener);
        eventBus.registerListener(TestEvent.class, exceptionListener);
        
        TestEvent event = new TestEvent("exception test");
        eventBus.publishEvent(event);
        
        // Normal listener should still receive the event
        assertTrue(normalLatch.await(1, TimeUnit.SECONDS));
        assertEquals("exception test", normalListener.getLastMessage());
    }

    @Test
    void testEventBusStatusReport() {
        // Test status report generation
        eventBus.registerListener(TestEvent.class, new TestEventListener(new CountDownLatch(1)));
        
        // Should not throw exception
        assertDoesNotThrow(() -> {
            eventBus.printStatusReport();
        });
    }

    @Test
    @Timeout(5)
    void testConcurrentEventPublishing() throws InterruptedException {
        // Test concurrent event publishing from multiple threads
        int threadCount = 5;
        CountDownLatch latch = new CountDownLatch(threadCount * 2); // Each thread publishes 2 events
        AtomicInteger totalEventsReceived = new AtomicInteger(0);
        
        // Create listeners to track all events
        eventBus.subscribe(TestEvent.class, event -> {
            totalEventsReceived.incrementAndGet();
            latch.countDown();
        });
        
        // Publish events from multiple threads concurrently
        CompletableFuture<Void>[] futures = new CompletableFuture[threadCount];
        for (int i = 0; i < threadCount; i++) {
            final int threadIndex = i;
            futures[i] = CompletableFuture.runAsync(() -> {
                try {
                    // Each thread publishes 2 events
                    eventBus.publishEvent(new TestEvent("concurrent event " + threadIndex + "-1"));
                    eventBus.publishEvent(new TestEvent("concurrent event " + threadIndex + "-2"));
                } catch (Exception e) {
                    // Log but don't fail the test
                    e.printStackTrace();
                }
            }, executorService);
        }
        
        // Wait for all threads to complete
        CompletableFuture.allOf(futures).join();
        
        // Wait for all events to be processed
        assertTrue(latch.await(3, TimeUnit.SECONDS));
        
        // All events should be processed
        assertEquals(threadCount * 2, totalEventsReceived.get());
    }

    @Test
    void testEventOrdering() throws InterruptedException {
        // Test that events are processed in order
        String[] receivedEvents = new String[3];
        CountDownLatch latch = new CountDownLatch(3);
        AtomicInteger index = new AtomicInteger(0);
        
        eventBus.subscribe(TestEvent.class, event -> {
            int currentIndex = index.getAndIncrement();
            receivedEvents[currentIndex] = event.getMessage();
            latch.countDown();
        });
        
        // Publish events in specific order
        eventBus.publishEvent(new TestEvent("first"));
        eventBus.publishEvent(new TestEvent("second"));
        eventBus.publishEvent(new TestEvent("third"));
        
        // Wait for all events
        assertTrue(latch.await(1, TimeUnit.SECONDS));
        
        // Events should be received in order
        assertEquals("first", receivedEvents[0]);
        assertEquals("second", receivedEvents[1]);
        assertEquals("third", receivedEvents[2]);
    }

    @Test
    void testGetListeners() {
        // Test getting all listeners
        TestEventListener listener1 = new TestEventListener(new CountDownLatch(1));
        TestEventListener listener2 = new TestEventListener(new CountDownLatch(1));
        
        eventBus.registerListener(TestEvent.class, listener1);
        eventBus.registerListener(TestEvent.class, listener2);
        
        java.util.Map<Class<?>, java.util.List<EventListener<?>>> listeners = eventBus.getListeners();
        assertNotNull(listeners);
        assertTrue(listeners.containsKey(TestEvent.class));
        
        java.util.List<EventListener<?>> testEventListeners = listeners.get(TestEvent.class);
        assertNotNull(testEventListeners);
        assertEquals(2, testEventListeners.size());
        assertTrue(testEventListeners.contains(listener1));
        assertTrue(testEventListeners.contains(listener2));
    }

    @Test
    void testEventStatisticsReset() throws InterruptedException {
        // Test resetting event statistics
        TestEventListener listener = new TestEventListener(new CountDownLatch(2));
        eventBus.registerListener(TestEvent.class, listener);
        
        eventBus.publishEvent(new TestEvent("event 1"));
        eventBus.publishEvent(new TestEvent("event 2"));
        
        // Wait for processing
        Thread.sleep(100);
        
        EventStatistics stats = eventBus.getEventStatistics(TestEvent.class);
        assertNotNull(stats);
        assertEquals(2, stats.getPublishedCount());
        
        // Reset statistics
        eventBus.resetEventStatistics();
        
        stats = eventBus.getEventStatistics(TestEvent.class);
        assertEquals(0, stats.getPublishedCount());
    }

    // Test event classes

    static class TestEvent extends Event {
        private final String message;

        public TestEvent(String message) {
            this.message = message;
        }

        public String getMessage() {
            return message;
        }

        @Override
        public String toString() {
            return "TestEvent{message='" + message + "'}";
        }
    }

    static class AnotherTestEvent extends Event {
        private final String data;

        public AnotherTestEvent(String data) {
            this.data = data;
        }

        public String getData() {
            return data;
        }
    }

    // Test listener class

    static class TestEventListener implements EventListener<TestEvent> {
        private final CountDownLatch latch;
        private volatile String lastMessage;

        public TestEventListener(CountDownLatch latch) {
            this.latch = latch;
        }

        @Override
        public void onEvent(TestEvent event) {
            this.lastMessage = event.getMessage();
            if (latch != null) {
                latch.countDown();
            }
        }

        public String getLastMessage() {
            return lastMessage;
        }
    }
}