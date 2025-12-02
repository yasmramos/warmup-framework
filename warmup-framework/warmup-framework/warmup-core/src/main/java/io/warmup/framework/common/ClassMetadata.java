package io.warmup.framework.common;

import java.util.*;

public class ClassMetadata {

    public String className;
    public boolean isComponent = false;
    public boolean isSingleton = true;
    public String namedValue = null;
    public Set<String> profiles = new HashSet<>();
    public boolean isLazy = false;
    public boolean hasAsyncMethods = false;
    public Set<String> interfaces = new HashSet<>();
    public String superClassName = null;

    // Constructor por defecto
    public ClassMetadata() {
    }

    // Constructor con className
    public ClassMetadata(String className) {
        this.className = className;
    }
}
