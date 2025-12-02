package io.warmup.framework.event;

/**
 * Interface for event listeners in the publish-subscribe event system.
 * 
 * Event listeners are notified when events of specific types are published
 * through the EventBus. Implementations should be thread-safe and handle
 * exceptions gracefully to prevent event processing failures.
 * 
 * <p>
 * Key features:
 * <ul>
 * <li>Type-safe event handling</li>
 * <li>Exception handling with error reporting</li>
 * <li>Event filtering support</li>
 * <li>Priority-based processing (future enhancement)</li>
 * </ul>
 * 
 * <p>
 * Example usage:
 * <pre>
 * {@literal @}Component
 * public class UserEventListener implements IEventListener {
 * 
 *     public void onEvent(Event event) {
 *         if (event instanceof UserCreatedEvent) {
 *             UserCreatedEvent userEvent = (UserCreatedEvent) event;
 *             // Handle user creation event
 *             sendWelcomeEmail(userEvent.getUser());
 *         }
 *     }
 * }
 * </pre>
 * 
 * @author MiniMax Agent
 * @version 1.0
 */
@FunctionalInterface
public interface IEventListener {
    
    /**
     * Handle an event.
     * 
     * <p>
     * This method is called when an event of a specific type is published.
     * Implementations should:
     * <ul>
     * <li>Handle the event appropriately</li>
     * <li>Not throw exceptions (log them instead)</li>
     * <li>Be thread-safe</li>
     * <li>Complete quickly to avoid blocking other listeners</li>
     * </ul>
     * 
     * @param event the event to handle
     * @throws Exception if the event handling fails (will be logged and handled by EventBus)
     */
    void onEvent(Event event) throws Exception;
    
    /**
     * Get the priority of this listener.
     * Lower numbers indicate higher priority.
     * Default implementation returns 0.
     * 
     * @return the priority (default 0)
     */
    default int getPriority() {
        return 0;
    }
    
    /**
     * Check if this listener can handle the given event type.
     * Default implementation returns true for all events.
     * 
     * @param eventType the event type to check
     * @return true if this listener can handle the event type
     */
    default boolean canHandleEvent(Class<?> eventType) {
        return eventType != null;
    }
    
    /**
     * Check if this listener can handle the given event instance.
     * Default implementation checks the event type using canHandleEvent.
     * 
     * @param event the event instance to check
     * @return true if this listener can handle the event
     */
    default boolean canHandle(Event event) {
        return event != null && canHandleEvent(event.getClass());
    }
    
    /**
     * Get a descriptive name for this event listener.
     * Default implementation returns the class simple name.
     * 
     * @return the listener name
     */
    default String getName() {
        return this.getClass().getSimpleName();
    }
    
    /**
     * Check if this listener is enabled and should receive events.
     * Default implementation returns true.
     * 
     * @return true if the listener is enabled
     */
    default boolean isEnabled() {
        return true;
    }
    
    /**
     * Enable or disable this event listener.
     * Default implementation does nothing.
     * 
     * @param enabled the enabled state
     */
    default void setEnabled(boolean enabled) {
        // Default implementation does nothing
    }
}