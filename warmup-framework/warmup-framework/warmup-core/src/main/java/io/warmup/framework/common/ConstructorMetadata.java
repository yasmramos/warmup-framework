package io.warmup.framework.common;

import java.util.Objects;

public class ConstructorMetadata {

    public final String className;
    public final String[] parameterTypeNames;
    public final String[] parameterInternalNames;
    public final int parameterCount;
    public final boolean isPublic;

    public ConstructorMetadata(String className, String[] parameterTypeNames,
            String[] parameterInternalNames, int parameterCount, boolean isPublic) {

        this.className = Objects.requireNonNull(className, "ClassName cannot be null");
        this.parameterTypeNames = parameterTypeNames != null ? parameterTypeNames.clone() : new String[0];
        this.parameterInternalNames = parameterInternalNames != null ? parameterInternalNames.clone() : new String[0];
        this.parameterCount = parameterCount;
        this.isPublic = isPublic;

        validateConsistency();
    }

    private void validateConsistency() {
        if (parameterTypeNames.length != parameterCount) {
            throw new IllegalArgumentException(
                    "Parameter type names length (" + parameterTypeNames.length
                    + ") doesn't match parameter count (" + parameterCount + ")");
        }

        if (parameterInternalNames.length != parameterCount) {
            throw new IllegalArgumentException(
                    "Parameter internal names length (" + parameterInternalNames.length
                    + ") doesn't match parameter count (" + parameterCount + ")");
        }

        if (parameterCount < 0) {
            throw new IllegalArgumentException("Parameter count cannot be negative: " + parameterCount);
        }
    }

    public boolean hasParameters() {
        return parameterCount > 0;
    }

    public boolean isDefaultConstructor() {
        return parameterCount == 0;
    }

    @Override
    public String toString() {
        return String.format("ConstructorMetadata{className='%s', parameters=%d, public=%s}",
                className, parameterCount, isPublic);
    }
}
