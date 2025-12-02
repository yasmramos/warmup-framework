package io.warmup.framework.event;

import io.warmup.framework.annotation.Component;

/**
 * Example event listener demonstrating the new EventBus capabilities.
 * 
 * Shows:
 * - Async event handling
 * - Priority-based processing
 * - Event filtering
 * - Error handling
 * 
 * @author MiniMax Agent
 * @version 1.0
 */
@Component
public class ExampleEventListener implements IEventListener {
    
    @Override
    public void onEvent(Event event) throws Exception {
        System.out.println("Processing event: " + event.getClass().getSimpleName());
        
        // Handle different event types
        if (event instanceof UserCreatedEvent) {
            handleUserCreated((UserCreatedEvent) event);
        } else if (event instanceof OrderPlacedEvent) {
            handleOrderPlaced((OrderPlacedEvent) event);
        } else {
            System.out.println("Unknown event type: " + event.getClass().getSimpleName());
        }
    }
    
    private void handleUserCreated(UserCreatedEvent event) {
        System.out.println("User created: " + event.getUserId() + " at " + event.getTimestamp());
        // Simulate some processing time
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
    
    private void handleOrderPlaced(OrderPlacedEvent event) {
        System.out.println("Order placed: " + event.getOrderId() + " for user " + event.getUserId());
        // Simulate some processing time
        try {
            Thread.sleep(200);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
    
    @Override
    public int getPriority() {
        return 1; // Higher priority than default listeners
    }
    
    @Override
    public String getName() {
        return "ExampleEventListener";
    }
}

// Example event classes
class UserCreatedEvent extends Event {
    private final String userId;
    private final String email;
    
    public UserCreatedEvent(String userId, String email) {
        this.userId = userId;
        this.email = email;
    }
    
    public String getUserId() { return userId; }
    public String getEmail() { return email; }
}

class OrderPlacedEvent extends Event {
    private final String orderId;
    private final String userId;
    private final double amount;
    
    public OrderPlacedEvent(String orderId, String userId, double amount) {
        this.orderId = orderId;
        this.userId = userId;
        this.amount = amount;
    }
    
    public String getOrderId() { return orderId; }
    public String getUserId() { return userId; }
    public double getAmount() { return amount; }
}