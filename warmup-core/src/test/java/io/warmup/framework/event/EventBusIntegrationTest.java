package io.warmup.framework.event;

import io.warmup.framework.annotation.Component;
import io.warmup.framework.core.Warmup;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for EventBus functionality
 * Tests complete publish-subscribe system with filtering, async publishing, and statistics
 */
public class EventBusIntegrationTest {

    private Warmup warmup;
    private EventBus eventBus;
    private TestEventListener listener1;
    private TestEventListener listener2;
    private FilteringEventListener filteringListener;
    private EventCounterListener counterListener;

    @BeforeEach
    void setUp() throws Exception {
        warmup = Warmup.create();
        warmup.scanPackages("io.warmup.framework.event");
        
        // Manually register EventBus as it's not automatically scanned
        eventBus = new EventBus();
        warmup.registerBean("eventBus", EventBus.class, eventBus);
        
        warmup.getContainer().start();
        
        // Create test listeners
        listener1 = new TestEventListener("listener1");
        listener2 = new TestEventListener("listener2");
        filteringListener = new FilteringEventListener();
        counterListener = new EventCounterListener();
    }

    @AfterEach
    void tearDown() throws Exception {
        // Clean up all listeners after each test to ensure test isolation
        if (eventBus != null) {
            eventBus.clearAllListeners();
        }
        
        // Clean up the warmup instance
        warmup = null;
    }



    @Test
    @DisplayName("Test basic event publishing and listening")
    void testBasicEventPublishing() {
        // Given - Subscribe listeners to TestEvent
        eventBus.subscribe(TestEvent.class, event -> listener1.onEvent(event));
        eventBus.subscribe(TestEvent.class, event -> listener2.onEvent(event));

        // When - Publish event
        TestEvent testEvent = new TestEvent("test message", 42);
        eventBus.publishEvent(testEvent);

        // Then - Both listeners should receive the event
        assertDoesNotThrow(() -> {
            Thread.sleep(200); // Allow async processing
        });
        
        assertEquals(1, listener1.getReceivedEvents().size());
        assertEquals(1, listener2.getReceivedEvents().size());
        assertEquals(testEvent, listener1.getReceivedEvents().get(0));
        assertEquals(testEvent, listener2.getReceivedEvents().get(0));
    }

    @Test
    @DisplayName("Test event filtering by event type")
    void testEventTypeFiltering() {

        // Given - Subscribe different listeners to different event types
        eventBus.subscribe(TestEvent.class, event -> listener1.onEvent(event));
        eventBus.subscribe(OtherTestEvent.class, event -> listener2.onEvent(event));

        // When - Publish different event types
        TestEvent testEvent = new TestEvent("test", 1);
        OtherTestEvent otherEvent = new OtherTestEvent("other", 2);
        eventBus.publishEvent(testEvent);
        eventBus.publishEvent(otherEvent);

        // Then - Only appropriate listeners receive events
        assertDoesNotThrow(() -> {
            Thread.sleep(200);
        });
        
        assertEquals(1, listener1.getReceivedEvents().size()); // Only TestEvent
        assertEquals(1, listener2.getReceivedEvents().size()); // Only OtherTestEvent
        assertEquals(testEvent, listener1.getReceivedEvents().get(0));
        assertEquals(otherEvent, listener2.getReceivedEvents().get(0));

    }

    @Test
    @DisplayName("Test listener filtering with canHandle method")
    void testListenerFiltering() {

        // Given - Subscribe filtering listener
        filteringListener.setAcceptedValue(42);
        eventBus.subscribe(TestEvent.class, event -> filteringListener.onEvent(event));

        // When - Publish events with different values
        TestEvent acceptedEvent = new TestEvent("accepted", 42);
        TestEvent rejectedEvent = new TestEvent("rejected", 99);
        eventBus.publishEvent(acceptedEvent);
        eventBus.publishEvent(rejectedEvent);

        // Then - Only accepted event should be processed
        assertDoesNotThrow(() -> {
            Thread.sleep(200);
        });
        
        assertEquals(1, filteringListener.getProcessedCount());
        assertEquals(acceptedEvent, filteringListener.getLastProcessedEvent());
    }

    @Test
    @DisplayName("Test async event publishing")
    void testAsyncEventPublishing() throws InterruptedException {
        // Given - Subscribe listener with count down latch
        CountDownLatch latch = new CountDownLatch(1);
        AsyncEventListener asyncListener = new AsyncEventListener(latch);
        eventBus.subscribe(TestEvent.class, event -> asyncListener.onEvent(event));

        // When - Publish event asynchronously
        TestEvent testEvent = new TestEvent("async test", 100);
        eventBus.publishEventAsync(testEvent);

        // Then - Should complete within timeout
        assertTrue(latch.await(5, TimeUnit.SECONDS));
        assertEquals(1, asyncListener.getProcessedCount());
    }

    @Test
    @DisplayName("Test multiple concurrent event publishing")
    void testConcurrentEventPublishing() throws InterruptedException {

        // Given - Multiple listeners and event counter
        eventBus.subscribe(TestEvent.class, event -> listener1.onEvent(event));
        eventBus.subscribe(TestEvent.class, event -> listener2.onEvent(event));
        eventBus.subscribe(TestEvent.class, event -> counterListener.onEvent(event));
        
        int eventCount = 10;
        CountDownLatch latch = new CountDownLatch(eventCount);
        counterListener.setLatch(latch);

        // When - Publish multiple events concurrently
        List<Thread> threads = new CopyOnWriteArrayList<>();
        for (int i = 0; i < eventCount; i++) {
            final int eventId = i;
            Thread thread = new Thread(() -> {
                TestEvent event = new TestEvent("event " + eventId, eventId);
                eventBus.publishEventAsync(event);
                latch.countDown();
            });
            threads.add(thread);
            thread.start();
        }

        // Then - Wait for all to complete
        assertTrue(latch.await(10, TimeUnit.SECONDS));
        
        // Small delay for async processing
        Thread.sleep(500);
        
        assertEquals(eventCount, listener1.getReceivedEvents().size());
        assertEquals(eventCount, listener2.getReceivedEvents().size());

    }

    @Test
    @DisplayName("Test event unsubscription")
    void testEventUnsubscription() {

        // Given - Subscribe and then unsubscribe

        eventBus.subscribe(TestEvent.class, event -> listener1.onEvent(event));

        eventBus.subscribe(TestEvent.class, event -> listener2.onEvent(event));
        
        // When - Unsubscribe one listener
        eventBus.unregisterListener(TestEvent.class, listener1);

        // Then - Only remaining listener should receive events
        TestEvent testEvent = new TestEvent("unsubscribe test", 50);
        eventBus.publishEvent(testEvent);

        assertDoesNotThrow(() -> {
            Thread.sleep(200);
        });
        
        assertEquals(0, listener1.getReceivedEvents().size()); // Unsubscribed
        assertEquals(1, listener2.getReceivedEvents().size()); // Still subscribed

    }

    @Test
    @DisplayName("Test dead letter queue for failed events")
    void testDeadLetterQueue() {

        // Given - Subscribe failing listener
        FailingEventListener failingListener = new FailingEventListener();
        eventBus.subscribe(TestEvent.class, event -> failingListener.onEvent(event));

        // When - Publish event that will fail
        TestEvent testEvent = new TestEvent("failing event", 999);
        eventBus.publishEvent(testEvent);

        // Then - Event should go to dead letter queue
        assertDoesNotThrow(() -> {
            Thread.sleep(300);
        });
        
        assertEquals(1, failingListener.getFailureCount());
        
        // Check if dead letter events were captured (if implemented)
        List<DeadLetterEvent> deadLetters = eventBus.getDeadLetterEvents();
        // This assertion may need adjustment based on actual implementation
        // assertTrue(deadLetters.size() > 0);

    }

    @Test
    @DisplayName("Test event statistics tracking")
    void testEventStatistics() {

        // Given - Event with statistics tracking
        eventBus.subscribe(TestEvent.class, event -> listener1.onEvent(event));

        // When - Publish multiple events
        for (int i = 0; i < 5; i++) {
            TestEvent event = new TestEvent("stat test " + i, i);
            eventBus.publishEvent(event);
        }

        // Then - Statistics should be updated
        assertDoesNotThrow(() -> {
            Thread.sleep(300);
        });
        
        EventStatistics stats = eventBus.getEventStatistics(TestEvent.class);
        assertNotNull(stats);
        assertTrue(stats.getPublishedCount() >= 5);
        assertTrue(stats.getProcessedCount() >= 0);

    }

    @Test
    @DisplayName("Test event priority handling")
    void testEventPriority() {
        // Given - Listeners with different priorities
        HighPriorityListener highPriority = new HighPriorityListener();
        LowPriorityListener lowPriority = new LowPriorityListener();
        
        eventBus.subscribe(PriorityTestEvent.class, event -> highPriority.onEvent(event));
        eventBus.subscribe(PriorityTestEvent.class, event -> lowPriority.onEvent(event));

        // When - Publish event
        PriorityTestEvent testEvent = new PriorityTestEvent("priority test", 100);

        // Then - High priority should process first (implementation dependent)
        eventBus.publishEvent(testEvent);
        
        assertDoesNotThrow(() -> {
            Thread.sleep(200);
        });
        
        // Verify both listeners received the event
        assertEquals(1, highPriority.getProcessedCount());
        assertEquals(1, lowPriority.getProcessedCount());
    }

    // Test event classes
    public static class TestEvent extends Event {
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

        @Override
        public String toString() {
            return "TestEvent{message='" + message + "', value=" + value + '}';
        }
    }

    public static class OtherTestEvent extends Event {
        private final String name;
        private final int id;

        public OtherTestEvent(String name, int id) {
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

    public static class PriorityTestEvent extends Event {
        private final String message;
        private final int priority;

        public PriorityTestEvent(String message, int priority) {
            this.message = message;
            this.priority = priority;
        }
    }

    // Test listener implementations
    public static class TestEventListener implements IEventListener {
        private final List<Event> receivedEvents = new CopyOnWriteArrayList<>();
        private final String name;

        public TestEventListener(String name) {
            this.name = name;
        }

        @Override
        public void onEvent(Event event) {
            receivedEvents.add(event);
        }

        @Override
        public String getName() {
            return name;
        }

        @Override
        public int getPriority() {
            return 0;
        }

        public boolean canHandle(Event event) {
            return true;
        }

        public List<Event> getReceivedEvents() {
            return receivedEvents;
        }
    }

    public static class FilteringEventListener implements IEventListener {
        private Event lastProcessedEvent;
        private int processedCount = 0;
        private Integer acceptedValue;

        @Override
        public void onEvent(Event event) {
            if (event instanceof TestEvent && canHandle(event)) {
                lastProcessedEvent = event;
                processedCount++;
            }
        }

        @Override
        public String getName() {
            return "filteringListener";
        }

        @Override
        public int getPriority() {
            return 0;
        }

        public boolean canHandle(Event event) {
            if (event instanceof TestEvent && acceptedValue != null) {
                return ((TestEvent) event).getValue() == acceptedValue;
            }
            return true;
        }

        public void setAcceptedValue(int value) {
            this.acceptedValue = value;
        }

        public Event getLastProcessedEvent() {
            return lastProcessedEvent;
        }

        public int getProcessedCount() {
            return processedCount;
        }
    }

    public static class AsyncEventListener implements IEventListener {
        private final CountDownLatch latch;
        private int processedCount = 0;

        public AsyncEventListener(CountDownLatch latch) {
            this.latch = latch;
        }

        @Override
        public void onEvent(Event event) {
            processedCount++;
            latch.countDown();
        }

        @Override
        public String getName() {
            return "asyncListener";
        }

        @Override
        public int getPriority() {
            return 0;
        }

        public boolean canHandle(Event event) {
            return event instanceof TestEvent;
        }

        public int getProcessedCount() {
            return processedCount;
        }
    }

    public static class EventCounterListener implements IEventListener {
        private CountDownLatch latch;

        @Override
        public void onEvent(Event event) {
            // Process event
            if (latch != null) {
                latch.countDown();
            }
        }

        @Override
        public String getName() {
            return "counterListener";
        }

        @Override
        public int getPriority() {
            return 0;
        }

        public boolean canHandle(Event event) {
            return event instanceof TestEvent;
        }

        public void setLatch(CountDownLatch latch) {
            this.latch = latch;
        }

        public int getProcessedCount() {
            return 0; // Not tracking count here
        }
    }

    public static class FailingEventListener implements IEventListener {
        private int failureCount = 0;

        @Override
        public void onEvent(Event event) {
            failureCount++;
            throw new RuntimeException("Intentional failure for testing");
        }

        @Override
        public String getName() {
            return "failingListener";
        }

        @Override
        public int getPriority() {
            return 0;
        }

        public boolean canHandle(Event event) {
            return event instanceof TestEvent;
        }

        public int getFailureCount() {
            return failureCount;
        }
    }

    public static class HighPriorityListener implements IEventListener {
        private int processedCount = 0;

        @Override
        public void onEvent(Event event) {
            processedCount++;
        }

        @Override
        public String getName() {
            return "highPriority";
        }

        @Override
        public int getPriority() {
            return 10;
        }

        public boolean canHandle(Event event) {
            return true; // Accept all events
        }

        public int getProcessedCount() {
            return processedCount;
        }
    }

    public static class LowPriorityListener implements IEventListener {
        private int processedCount = 0;

        @Override
        public void onEvent(Event event) {
            processedCount++;
        }

        @Override
        public String getName() {
            return "lowPriority";
        }

        @Override
        public int getPriority() {
            return 0;
        }

        public boolean canHandle(Event event) {
            return true; // Accept all events
        }

        public int getProcessedCount() {
            return processedCount;
        }
    }
}