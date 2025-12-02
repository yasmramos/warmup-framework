package io.warmup.framework.core.metadata;

/**
 * Metadata about a Java parameter
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
}