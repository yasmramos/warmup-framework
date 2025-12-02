package io.warmup.framework.processor;

import java.util.Objects;

public class ComponentInfo {

    public final String className;
    public final boolean singleton;
    public final String named;
    public final boolean lazy;

    public ComponentInfo(String className, boolean singleton, String named, boolean lazy) {
        this.className = Objects.requireNonNull(className, "ClassName cannot be null");
        this.singleton = singleton;
        this.named = named; // Puede ser null
        this.lazy = lazy;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ComponentInfo that = (ComponentInfo) o;
        return className.equals(that.className);
    }

    @Override
    public int hashCode() {
        return className.hashCode();
    }

    @Override
    public String toString() {
        return "ComponentInfo{"
                + "className='" + className + '\''
                + ", singleton=" + singleton
                + ", named='" + named + '\''
                + ", lazy=" + lazy
                + '}';
    }
}
