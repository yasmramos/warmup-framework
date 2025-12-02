package io.warmup.framework.core.metadata;

/**
 * Metadata about a Java class
 */
public class ClassMetadata {
    private final String className;
    private final String superClass;
    private final String[] interfaces;
    private final boolean isAbstract;
    private final boolean isFinal;
    private final boolean isInterface;

    public ClassMetadata(String className, String superClass, String[] interfaces, 
                        boolean isAbstract, boolean isFinal, boolean isInterface) {
        this.className = className;
        this.superClass = superClass;
        this.interfaces = interfaces != null ? interfaces : new String[0];
        this.isAbstract = isAbstract;
        this.isFinal = isFinal;
        this.isInterface = isInterface;
    }

    public String getClassName() {
        return className;
    }

    public String getSuperClass() {
        return superClass;
    }

    public String[] getInterfaces() {
        return interfaces;
    }

    public boolean isAbstract() {
        return isAbstract;
    }

    public boolean isFinal() {
        return isFinal;
    }

    public boolean isInterface() {
        return isInterface;
    }

    /**
     * Get constructors (for compatibility with existing code)
     */
    public io.warmup.framework.metadata.ConstructorMetadata[] getConstructors() {
        // Return empty array as fallback
        return new io.warmup.framework.metadata.ConstructorMetadata[0];
    }

    /**
     * Get methods of this class
     */
    public io.warmup.framework.metadata.MethodMetadata[] getMethods() {
        // Return empty array as fallback
        return new io.warmup.framework.metadata.MethodMetadata[0];
    }

    /**
     * Get fields of this class (placeholder for future implementation)
     */
    public java.lang.reflect.Field[] getFields() {
        // Return empty array as fallback
        return new java.lang.reflect.Field[0];
    }
    
    /**
     * Convert from io.warmup.framework.metadata.ClassMetadata to this type
     */
    public static io.warmup.framework.core.metadata.ClassMetadata fromMetadataRegistry(io.warmup.framework.metadata.ClassMetadata source) {
        if (source == null) {
            return null;
        }
        return new io.warmup.framework.core.metadata.ClassMetadata(
            source.getClassName(),
            source.getSuperClass(), 
            source.getInterfaces().toArray(new String[0]),
            source.isAbstract(),
            source.isFinal(),
            source.isInterface()
        );
    }
}