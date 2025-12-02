package io.warmup.framework.core.metadata;

/**
 * Metadata about a Java constructor
 */
public class ConstructorMetadata {
    private final String[] parameterTypes;
    private final String[] exceptionTypes;
    private final boolean isPrivate;
    private final boolean isProtected;
    private final boolean isPublic;
    private final String[] annotations;
    private final int modifiers;

    public ConstructorMetadata(String[] parameterTypes, String[] exceptionTypes,
                             boolean isPrivate, boolean isProtected, boolean isPublic,
                             String[] annotations, int modifiers) {
        this.parameterTypes = parameterTypes != null ? parameterTypes : new String[0];
        this.exceptionTypes = exceptionTypes != null ? exceptionTypes : new String[0];
        this.isPrivate = isPrivate;
        this.isProtected = isProtected;
        this.isPublic = isPublic;
        this.annotations = annotations != null ? annotations : new String[0];
        this.modifiers = modifiers;
    }

    public String[] getParameterTypes() {
        return parameterTypes;
    }

    public String[] getExceptionTypes() {
        return exceptionTypes;
    }

    public boolean isPrivate() {
        return isPrivate;
    }

    public boolean isProtected() {
        return isProtected;
    }

    public boolean isPublic() {
        return isPublic;
    }

    public String[] getAnnotations() {
        return annotations;
    }

    public int getModifiers() {
        return modifiers;
    }

    /**
     * Get constructor name (always "<init>" for constructors)
     */
    public String getName() {
        return "<init>";
    }

    /**
     * Get parameter count
     */
    public int getParameterCount() {
        return parameterTypes.length;
    }

    /**
     * Get declaring class
     */
    public Class<?> getDeclaringClass() {
        // This would need to be set during construction, for now return Object.class
        return Object.class;
    }

    /**
     * Get descriptor (JVM descriptor for constructor)
     */
    public String getDescriptor() {
        StringBuilder descriptor = new StringBuilder("(");
        for (String paramType : parameterTypes) {
            descriptor.append(paramType.replace('.', '/'));
        }
        descriptor.append(")V"); // Constructor returns void
        return descriptor.toString();
    }

    /**
     * Get this constructor as a Method object (for compatibility with existing code)
     */
    public java.lang.reflect.Constructor<?> getMethod() {
        try {
            // Try to find the actual constructor using reflection
            // This is a fallback implementation
            return getDeclaringClass().getDeclaredConstructor(toClassArray(parameterTypes));
        } catch (NoSuchMethodException e) {
            // If not found, return null or throw appropriate exception
            return null;
        }
    }

    /**
     * Convert string type names to Class array
     */
    private Class<?>[] toClassArray(String[] typeNames) {
        if (typeNames == null || typeNames.length == 0) {
            return new Class<?>[0];
        }
        Class<?>[] classes = new Class[typeNames.length];
        for (int i = 0; i < typeNames.length; i++) {
            try {
                classes[i] = Class.forName(typeNames[i]);
            } catch (ClassNotFoundException e) {
                // Use Object.class as fallback
                classes[i] = Object.class;
            }
        }
        return classes;
    }

    /**
     * Get constructor parameters (for compatibility with existing code)
     */
    public io.warmup.framework.metadata.ParameterMetadata[] getParameters() {
        // Return empty array as fallback
        return new io.warmup.framework.metadata.ParameterMetadata[0];
    }
    
    /**
     * Convert array from io.warmup.framework.metadata.ConstructorMetadata to this type
     */
    public static io.warmup.framework.core.metadata.ConstructorMetadata[] fromMetadataRegistryArray(io.warmup.framework.metadata.ConstructorMetadata[] sourceArray) {
        if (sourceArray == null) {
            return new io.warmup.framework.core.metadata.ConstructorMetadata[0];
        }
        
        io.warmup.framework.core.metadata.ConstructorMetadata[] result = new io.warmup.framework.core.metadata.ConstructorMetadata[sourceArray.length];
        for (int i = 0; i < sourceArray.length; i++) {
            result[i] = fromMetadataRegistry(sourceArray[i]);
        }
        return result;
    }
    
    /**
     * Convert from io.warmup.framework.metadata.ConstructorMetadata to this type
     */
    public static io.warmup.framework.core.metadata.ConstructorMetadata fromMetadataRegistry(io.warmup.framework.metadata.ConstructorMetadata source) {
        if (source == null) {
            return null;
        }
        return new io.warmup.framework.core.metadata.ConstructorMetadata(
            source.getParameterTypesArray(), // Use compatibility method
            source.getExceptionTypes(),      // Use compatibility method
            source.isPrivate(),
            source.isProtected(),
            source.isPublic(),
            source.getAnnotationsArray(),    // Use compatibility method
            source.getModifiers()           // Use compatibility method
        );
    }
}