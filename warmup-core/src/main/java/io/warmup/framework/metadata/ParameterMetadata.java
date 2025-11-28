package io.warmup.framework.metadata;

/**
 * Metadata about a Java parameter (legacy compatibility)
 */
public class ParameterMetadata {
    private final String name;
    private final String type;
    private final boolean isFinal;
    private final boolean isVarargs;
    
    public ParameterMetadata(String name, String type, boolean isFinal, boolean isVarargs) {
        this.name = name;
        this.type = type;
        this.isFinal = isFinal;
        this.isVarargs = isVarargs;
    }
    
    public String getName() {
        return name;
    }
    
    public String getType() {
        return type;
    }
    
    public boolean isFinal() {
        return isFinal;
    }
    
    public boolean isVarargs() {
        return isVarargs;
    }

    /**
     * Get qualified type name (same as getType() for compatibility)
     */
    public String getQualifiedType() {
        return type;
    }

    /**
     * Get annotations (placeholder implementation)
     */
    public java.util.List<java.lang.annotation.Annotation> getAnnotations() {
        return new java.util.ArrayList<>();
    }

    /**
     * Get annotations as Map<String, String> (placeholder implementation)
     */
    public java.util.Map<String, String> getAnnotationsAsMap() {
        return new java.util.HashMap<>();
    }

    /**
     * Check if has annotation (placeholder implementation)
     */
    public boolean hasAnnotation(String annotationName) {
        return false;
    }

    /**
     * Get annotation value (placeholder implementation)
     */
    public Object getAnnotationValue(String annotationName) {
        return null;
    }
}