package io.warmup.framework.core.metadata;

/**
 * Metadata about a Java field
 */
public class FieldMetadata {
    private final String name;
    private final String type;
    private final boolean isStatic;
    private final boolean isFinal;
    private final boolean isPrivate;
    private final boolean isProtected;
    private final boolean isPublic;
    private final String[] annotations;
    private final int modifiers;

    public FieldMetadata(String name, String type, boolean isStatic, boolean isFinal,
                        boolean isPrivate, boolean isProtected, boolean isPublic,
                        String[] annotations, int modifiers) {
        this.name = name;
        this.type = type;
        this.isStatic = isStatic;
        this.isFinal = isFinal;
        this.isPrivate = isPrivate;
        this.isProtected = isProtected;
        this.isPublic = isPublic;
        this.annotations = annotations != null ? annotations : new String[0];
        this.modifiers = modifiers;
    }

    public String getName() {
        return name;
    }

    public String getType() {
        return type;
    }

    public boolean isStatic() {
        return isStatic;
    }

    public boolean isFinal() {
        return isFinal;
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
}