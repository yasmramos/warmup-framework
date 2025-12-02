package io.warmup.framework.metadata;

import java.lang.annotation.Annotation;

/**
 * Metadata about a Java annotation
 */
public class AnnotationMetadata {
    private final String name;
    private final java.util.Map<String, String> properties;
    
    public AnnotationMetadata(String name, java.util.Map<String, String> properties) {
        this.name = name;
        this.properties = properties != null ? properties : new java.util.HashMap<>();
    }
    
    public String getName() {
        return name;
    }
    
    public java.util.Map<String, String> getProperties() {
        return properties;
    }
    
    /**
     * Get annotation properties as list (for compatibility)
     */
    public java.util.List<Annotation> getAnnotations() {
        java.util.List<Annotation> result = new java.util.ArrayList<>();
        // Convert properties to annotations - placeholder implementation
        return result;
    }
    
    /**
     * Create annotation instance (for compatibility)
     */
    public <T> T createAnnotation(Class<T> annotationType) {
        // Return this object as compatibility - actual annotation creation would require reflection
        return (T) this;
    }
}