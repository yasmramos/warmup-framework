package io.warmup.framework.startup.examples;

import io.warmup.framework.core.WarmupContainer;
import io.warmup.framework.annotation.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

/**
 * Mock implementation of WarmupContainer for testing and demonstration purposes.
 * Provides basic functionality needed by example classes without the complexity
 * of the full WarmupContainer implementation.
 * 
 * @author MiniMax Agent
 * @version 1.0
 */
public class MockWarmupContainer extends WarmupContainer {
    
    private static final Logger log = Logger.getLogger(MockWarmupContainer.class.getName());
    
    private final Map<String, Object> registeredBeans = new ConcurrentHashMap<>();
    private final Set<String> criticalComponents = new HashSet<>();
    private boolean initialized = false;
    
    public MockWarmupContainer() {
        super();
        log.info("MockWarmupContainer initialized");
    }
    
    /**
     * Register a bean in the mock container
     */
    public void registerBean(String name, Object bean) {
        registeredBeans.put(name, bean);
        log.fine("Bean registered: " + name);
    }
    
    /**
     * Get a bean from the mock container
     */
    public Object getBean(String name) {
        return registeredBeans.get(name);
    }
    
    /**
     * Register a critical component
     */
    public void registerCriticalComponent(String name) {
        criticalComponents.add(name);
        log.fine("Critical component registered: " + name);
    }
    
    /**
     * Check if a component is marked as critical
     */
    public boolean isCriticalComponent(String name) {
        return criticalComponents.contains(name);
    }
    
    /**
     * Initialize the mock container
     */
    public void initialize() {
        if (!initialized) {
            initialized = true;
            log.info("MockWarmupContainer initialized successfully");
        }
    }
    
    /**
     * Shutdown the mock container
     */
    public void shutdown() {
        registeredBeans.clear();
        criticalComponents.clear();
        initialized = false;
        log.info("MockWarmupContainer shutdown completed");
    }
    
    /**
     * Get initialization status
     */
    public boolean isInitialized() {
        return initialized;
    }
    
    /**
     * Get number of registered beans
     */
    public int getBeanCount() {
        return registeredBeans.size();
    }
    
    /**
     * Get number of critical components
     */
    public int getCriticalComponentCount() {
        return criticalComponents.size();
    }
}