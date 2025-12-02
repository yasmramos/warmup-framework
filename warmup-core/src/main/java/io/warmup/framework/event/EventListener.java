package io.warmup.framework.event;

/**
 * Functional interface for event listeners.
 * 
 * @param <T> The type of event this listener handles
 */
@FunctionalInterface
public interface EventListener<T> {
    
    /**
     * Called when an event is published.
     * 
     * @param event The published event
     */
    void onEvent(T event);
}