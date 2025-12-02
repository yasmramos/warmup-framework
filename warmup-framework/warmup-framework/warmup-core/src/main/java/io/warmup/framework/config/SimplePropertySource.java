package io.warmup.framework.config;

import java.io.IOException;

/**
 * Simple implementation of PropertySource for basic configuration.
 * Compatible with Java 8 and provides a minimal implementation.
 */
public class SimplePropertySource extends PropertySource {
    
    public SimplePropertySource() {
        super();
    }
    
    public SimplePropertySource(String propertyFile) throws IOException {
        super(propertyFile);
    }
    
    /**
     * Get all property names (compatible wrapper)
     */
    public java.util.Set<String> getPropertyNames() {
        return properties.stringPropertyNames();
    }
}