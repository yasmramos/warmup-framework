package io.warmup.framework.annotation;

import java.lang.annotation.*;

@Target({ElementType.TYPE, ElementType.FIELD, ElementType.PARAMETER, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface Lazy {
    boolean value() default true;
}