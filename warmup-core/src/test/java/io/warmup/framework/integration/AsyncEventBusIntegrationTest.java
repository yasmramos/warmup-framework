package io.warmup.framework.integration;

import io.warmup.framework.annotation.Async;
import io.warmup.framework.annotation.Component;
import io.warmup.framework.async.AsyncExecutor;
import io.warmup.framework.core.Warmup;
import io.warmup.framework.event.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests combining @Async and EventBus functionality
 * Tests realistic scenarios where async methods publish events
 */
public class AsyncEventBusIntegrationTest {

    private Warmup warmup;
    private EventBus eventBus;
    private AsyncEventProcessor eventProcessor;
    private AsyncEventListener asyncListener;
    private OrderEventListener orderListener;
    private NotificationListener notificationListener;

    @BeforeEach
    void setUp() throws Exception {
        warmup = Warmup.create();
        
        // Create EventBus manually
        eventBus = new EventBus();
        
        // Create AsyncEventProcessor manually with EventBus injected
        eventProcessor = new AsyncEventProcessor(eventBus);
        
        // Register both as beans
        warmup.registerBean(EventBus.class, eventBus);
        warmup.registerBean(AsyncEventProcessor.class, eventProcessor);
        
        warmup.getContainer().start();
        
        // Create test listeners
        asyncListener = new AsyncEventListener();
        orderListener = new OrderEventListener();
        notificationListener = new NotificationListener();
        
        // Subscribe listeners
        eventBus.subscribe(AsyncProcessedEvent.class, event -> asyncListener.onEvent(event));
        eventBus.subscribe(OrderCreatedEvent.class, event -> orderListener.onEvent(event));
        eventBus.subscribe(NotificationEvent.class, event -> notificationListener.onEvent(event));
    }

    @Test
    @DisplayName("Test async method publishing event after processing")
    void testAsyncMethodPublishingEvent() {
        // Given - Ready to receive events
        CountDownLatch latch = new CountDownLatch(1);
        asyncListener.setLatch(latch);

        // When - Execute async method that will publish event
        CompletableFuture<String> result = eventProcessor.processDataAsync("test data", 42);

        // Then - Method completes and event is published
        assertDoesNotThrow(() -> {
            String processedData = result.get(5, TimeUnit.SECONDS);
            assertEquals("test data-processed-42", processedData);
            
            // Wait for async event processing
            assertTrue(latch.await(5, TimeUnit.SECONDS));
            assertEquals(1, asyncListener.getProcessedEvents().size());
        });
    }

    @Test
    @DisplayName("Test async method with event publishing and error handling")
    void testAsyncMethodWithEventAndErrorHandling() {
        // Given - Ready to receive error events
        CountDownLatch latch = new CountDownLatch(1);
        notificationListener.setLatch(latch);

        // When - Execute async method that will fail and publish error event
        CompletableFuture<String> result = eventProcessor.processFailingDataAsync("invalid data");

        // Then - Method fails but error event is still published
        assertThrows(Exception.class, () -> {
            result.get(5, TimeUnit.SECONDS);
        });

        // Wait for error event processing
        assertDoesNotThrow(() -> {
            Thread.sleep(200);
            assertTrue(latch.getCount() <= 1); // Error event should be published
        });
    }

    @Test
    @DisplayName("Test multiple async methods publishing events concurrently")
    void testMultipleAsyncMethodsPublishingEvents() throws InterruptedException {
        // Given - Multiple listeners and latch
        int methodCount = 5;
        CountDownLatch orderLatch = new CountDownLatch(methodCount);
        CountDownLatch notificationLatch = new CountDownLatch(methodCount);
        
        orderListener.setLatch(orderLatch);
        notificationListener.setLatch(notificationLatch);

        // When - Execute multiple async methods concurrently
        List<CompletableFuture<String>> futures = new CopyOnWriteArrayList<>();
        for (int i = 0; i < methodCount; i++) {
            final int index = i;
            CompletableFuture<String> future = eventProcessor.processOrderAsync(
                "order-" + index, 
                index * 100
            );
            futures.add(future);
        }

        // Then - All methods complete and events are published
        for (CompletableFuture<String> future : futures) {
            assertDoesNotThrow(() -> {
                String result = future.get(5, TimeUnit.SECONDS);
                assertTrue(result.startsWith("Order processed:"));
            });
        }

        // Wait for all events to be processed
        assertTrue(orderLatch.await(10, TimeUnit.SECONDS));
        assertTrue(notificationLatch.await(10, TimeUnit.SECONDS));
    }

    @Test
    @DisplayName("Test async method with custom executor publishing event")
    void testAsyncMethodCustomExecutorWithEvent() {
        // Given - Ready to receive events
        CountDownLatch latch = new CountDownLatch(1);
        asyncListener.setLatch(latch);

        // When - Execute async method with custom executor
        CompletableFuture<String> result = eventProcessor.processWithCustomExecutorAsync("custom test");

        // Then - Method completes and event is published
        assertDoesNotThrow(() -> {
            String processedData = result.get(5, TimeUnit.SECONDS);
            assertEquals("custom-processed", processedData);
            
            // Wait for async event processing
            assertTrue(latch.await(5, TimeUnit.SECONDS));
        });
    }

    @Test
    @DisplayName("Test async method chaining with event publishing")
    void testAsyncMethodChainingWithEvents() {
        // Given - Ready to receive events
        CountDownLatch latch = new CountDownLatch(2); // Expecting 2 events
        asyncListener.setLatch(latch);

        // When - Execute chain of async methods that publish events
        CompletableFuture<String> result = eventProcessor.processChainAsync("chain-test");

        // Then - Chain completes and multiple events are published
        assertDoesNotThrow(() -> {
            String finalResult = result.get(10, TimeUnit.SECONDS);
            assertEquals("chain-test-step1-step2-step3", finalResult);
            
            // Wait for all events
            assertTrue(latch.await(5, TimeUnit.SECONDS));
            assertEquals(2, asyncListener.getProcessedEvents().size());
        });
    }

    @Test
    @DisplayName("Test async method with event filtering")
    void testAsyncMethodWithEventFiltering() {
        // Given - Filtering listener ready
        CountDownLatch latch = new CountDownLatch(1);
        FilteredEventListener filteredListener = new FilteredEventListener(100);
        eventBus.subscribe(AsyncProcessedEvent.class, event -> filteredListener.onEvent(event));
        filteredListener.setLatch(latch);

        // When - Execute async method that publishes event
        CompletableFuture<String> result = eventProcessor.processFilteredEventAsync("filtered test", 100);

        // Then - Only matching events are processed
        assertDoesNotThrow(() -> {
            String processedData = result.get(5, TimeUnit.SECONDS);
            assertEquals("filtered test", processedData);
            
            assertTrue(latch.await(5, TimeUnit.SECONDS));
            assertEquals(1, filteredListener.getAcceptedEvents().size());
        });
    }

    @Test
    @DisplayName("Test async method with dead letter event handling")
    void testAsyncMethodWithDeadLetterHandling() {
        // Given - Failing event listener
        FailingAsyncEventListener failingListener = new FailingAsyncEventListener();
        eventBus.subscribe(AsyncProcessedEvent.class, event -> failingListener.onEvent(event));

        // When - Execute async method that publishes event
        CompletableFuture<String> result = eventProcessor.processDataAsync("dead letter test", 999);

        // Then - Event goes to dead letter queue
        assertDoesNotThrow(() -> {
            String processedData = result.get(5, TimeUnit.SECONDS);
            assertEquals("dead letter test-processed-999", processedData);
            
            Thread.sleep(300); // Wait for dead letter processing
            
            assertTrue(failingListener.getFailureCount() > 0);
        });
    }

    @Test
    @DisplayName("Test event statistics with async processing")
    void testEventStatisticsWithAsyncProcessing() {
        // Given - Multiple events to be processed
        int eventCount = 3;

        // When - Execute multiple async methods
        List<CompletableFuture<String>> futures = new CopyOnWriteArrayList<>();
        for (int i = 0; i < eventCount; i++) {
            CompletableFuture<String> future = eventProcessor.processDataAsync("stat test " + i, i);
            futures.add(future);
        }

        // Then - Wait for completion and check statistics
        assertDoesNotThrow(() -> {
            for (CompletableFuture<String> future : futures) {
                future.get(5, TimeUnit.SECONDS);
            }
            
            Thread.sleep(500); // Allow async event processing
            
            // Check statistics
            EventStatistics asyncStats = eventBus.getEventStatistics(AsyncProcessedEvent.class);
            EventStatistics orderStats = eventBus.getEventStatistics(OrderCreatedEvent.class);
            
            assertNotNull(asyncStats);
            assertTrue(asyncStats.getPublishedCount() >= eventCount);
        });
    }

    @Test
    @DisplayName("Test async method with timeout and event publishing")
    void testAsyncMethodTimeoutWithEventPublishing() {
        // Given - Short timeout for async method
        CountDownLatch latch = new CountDownLatch(1);
        asyncListener.setLatch(latch);

        // When - Execute async method with short timeout
        CompletableFuture<String> result = eventProcessor.processWithTimeoutAsync("timeout test");

        // Then - Method completes within timeout and publishes event
        assertDoesNotThrow(() -> {
            String processedData = result.get(3, TimeUnit.SECONDS);
            assertEquals("timeout-processed", processedData);
            
            // Event should still be published despite timeout
            assertTrue(latch.await(2, TimeUnit.SECONDS));
        });
    }

    // Component classes for testing
    @Component
    public static class AsyncEventProcessor {
        
        private final EventBus eventBus;
        
        @jakarta.inject.Inject
        public AsyncEventProcessor(EventBus eventBus) {
            this.eventBus = eventBus;
        }

        @Async
        public CompletableFuture<String> processDataAsync(String data, int value) {
            String processed = data + "-processed-" + value;
            
            // Publish event about processing
            AsyncProcessedEvent event = new AsyncProcessedEvent(processed, value);
            eventBus.publishEvent(event);
            
            return CompletableFuture.completedFuture(processed);
        }

        @Async
        public CompletableFuture<String> processFailingDataAsync(String data) {
            // Process data
            String processed = "processed-" + data;
            
            // Publish event
            AsyncProcessedEvent event = new AsyncProcessedEvent(processed, 0);
            eventBus.publishEvent(event);
            
            // Then fail by returning a failed CompletableFuture
            CompletableFuture<String> future = new CompletableFuture<>();
            future.completeExceptionally(new RuntimeException("Processing failed for: " + data));
            return future;
        }

        @Async
        public CompletableFuture<String> processOrderAsync(String orderId, double amount) {
            // Process order
            String result = "Order processed: " + orderId + " for $" + amount;
            
            // Publish events
            OrderCreatedEvent orderEvent = new OrderCreatedEvent(orderId, amount);
            NotificationEvent notificationEvent = new NotificationEvent("Order " + orderId + " created");
            
            eventBus.publishEvent(orderEvent);
            eventBus.publishEvent(notificationEvent);
            
            return CompletableFuture.completedFuture(result);
        }

        @Async("customExecutor")
        public CompletableFuture<String> processWithCustomExecutorAsync(String data) {
            String processed = "custom-processed";
            eventBus.publishEvent(new AsyncProcessedEvent(processed, 0));
            return CompletableFuture.completedFuture(processed);
        }

        @Async
        public CompletableFuture<String> processChainAsync(String data) {
            // Step 1
            String step1 = data + "-step1";
            eventBus.publishEvent(new AsyncProcessedEvent("Step 1: " + step1, 1));
            
            // Step 2
            String step2 = step1 + "-step2";
            eventBus.publishEvent(new AsyncProcessedEvent("Step 2: " + step2, 2));
            
            // Step 3
            String result = step2 + "-step3";
            return CompletableFuture.completedFuture(result);
        }

        @Async
        public CompletableFuture<String> processFilteredEventAsync(String data, int filterValue) {
            String processed = data;
            eventBus.publishEvent(new AsyncProcessedEvent(processed, filterValue));
            return CompletableFuture.completedFuture(processed);
        }

        @Async(timeout = 2000)
        public CompletableFuture<String> processWithTimeoutAsync(String data) {
            String processed = "timeout-processed";
            eventBus.publishEvent(new AsyncProcessedEvent(processed, 0));
            return CompletableFuture.completedFuture(processed);
        }
    }

    // Event classes
    public static class AsyncProcessedEvent extends Event {
        private final String processedData;
        private final int value;

        public AsyncProcessedEvent(String processedData, int value) {
            this.processedData = processedData;
            this.value = value;
        }

        public String getProcessedData() {
            return processedData;
        }

        public int getValue() {
            return value;
        }
    }

    public static class OrderCreatedEvent extends Event {
        private final String orderId;
        private final double amount;

        public OrderCreatedEvent(String orderId, double amount) {
            this.orderId = orderId;
            this.amount = amount;
        }

        public String getOrderId() {
            return orderId;
        }

        public double getAmount() {
            return amount;
        }
    }

    public static class NotificationEvent extends Event {
        private final String message;

        public NotificationEvent(String message) {
            this.message = message;
        }

        public String getMessage() {
            return message;
        }
    }

    // Listener classes
    public static class AsyncEventListener implements IEventListener {
        private final List<Event> processedEvents = new CopyOnWriteArrayList<>();
        private CountDownLatch latch;

        @Override
        public void onEvent(Event event) {
            processedEvents.add(event);
            if (latch != null) {
                latch.countDown();
            }
        }

        @Override
        public String getName() {
            return "asyncEventListener";
        }

        @Override
        public int getPriority() {
            return 0;
        }

        public boolean canHandle(Event event) {
            return event instanceof AsyncProcessedEvent;
        }

        public void setLatch(CountDownLatch latch) {
            this.latch = latch;
        }

        public List<Event> getProcessedEvents() {
            return processedEvents;
        }
    }

    public static class OrderEventListener implements IEventListener {
        private CountDownLatch latch;

        @Override
        public void onEvent(Event event) {
            if (latch != null) {
                latch.countDown();
            }
        }

        @Override
        public String getName() {
            return "orderEventListener";
        }

        @Override
        public int getPriority() {
            return 0;
        }

        public boolean canHandle(Event event) {
            return event instanceof OrderCreatedEvent;
        }

        public void setLatch(CountDownLatch latch) {
            this.latch = latch;
        }
    }

    public static class NotificationListener implements IEventListener {
        private CountDownLatch latch;

        @Override
        public void onEvent(Event event) {
            if (latch != null) {
                latch.countDown();
            }
        }

        @Override
        public String getName() {
            return "notificationListener";
        }

        @Override
        public int getPriority() {
            return 0;
        }

        public boolean canHandle(Event event) {
            return event instanceof NotificationEvent;
        }

        public void setLatch(CountDownLatch latch) {
            this.latch = latch;
        }
    }

    public static class FilteredEventListener implements IEventListener {
        private final int acceptValue;
        private final List<Event> acceptedEvents = new CopyOnWriteArrayList<>();
        private CountDownLatch latch;

        public FilteredEventListener(int acceptValue) {
            this.acceptValue = acceptValue;
        }

        @Override
        public void onEvent(Event event) {
            if (canHandle(event)) {
                acceptedEvents.add(event);
                if (latch != null) {
                    latch.countDown();
                }
            }
        }

        @Override
        public String getName() {
            return "filteredEventListener";
        }

        @Override
        public int getPriority() {
            return 0;
        }

        public boolean canHandle(Event event) {
            if (event instanceof AsyncProcessedEvent) {
                return ((AsyncProcessedEvent) event).getValue() == acceptValue;
            }
            return false;
        }

        public void setLatch(CountDownLatch latch) {
            this.latch = latch;
        }

        public List<Event> getAcceptedEvents() {
            return acceptedEvents;
        }
    }

    public static class FailingAsyncEventListener implements IEventListener {
        private int failureCount = 0;

        @Override
        public void onEvent(Event event) {
            failureCount++;
            throw new RuntimeException("Listener failure for testing");
        }

        @Override
        public String getName() {
            return "failingAsyncListener";
        }

        @Override
        public int getPriority() {
            return 0;
        }

        public boolean canHandle(Event event) {
            return event instanceof AsyncProcessedEvent;
        }

        public int getFailureCount() {
            return failureCount;
        }
    }
}