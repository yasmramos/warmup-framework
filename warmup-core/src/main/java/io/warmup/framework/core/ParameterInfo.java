// ParameterInfo.java
package io.warmup.framework.core;

import java.lang.annotation.Annotation;
import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Type;

public class ParameterInfo extends AccessibleObject {

    private final Class<?> type;
    private final Annotation[] annotations;
    private final Type parameterizedType; 

    public ParameterInfo(Class<?> type, Annotation[] annotations, Type parameterizedType) {
        this.type = type;
        // Manejar posibles nulls
        this.annotations = annotations != null ? annotations : new Annotation[0];
        this.parameterizedType = parameterizedType; // Puede ser null, y eso está bien.
    }

    // Getters
    public Class<?> getType() {
        return type;
    }

    public Type getParameterizedType() {
        return parameterizedType;
    }

    @Override
    public <T extends Annotation> T getAnnotation(Class<T> annotationClass) {
        for (Annotation ann : annotations) {
            if (annotationClass.isInstance(ann)) {
                return annotationClass.cast(ann);
            }
        }
        return null;
    }

    @Override
    public Annotation[] getAnnotations() {
        return annotations;
    }

    @Override
    public Annotation[] getDeclaredAnnotations() {
        return annotations;
    }
}