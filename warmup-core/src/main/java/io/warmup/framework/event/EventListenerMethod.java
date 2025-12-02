package io.warmup.framework.event;

import java.lang.reflect.Method;

public class EventListenerMethod {

    private Object instance;
    private Method method;

    public EventListenerMethod(Object instance, Method method) {
        this.instance = instance;
        this.method = method;
    }

    public Object getInstance() {
        return instance;
    }

    public void setInstance(Object instance) {
        this.instance = instance;
    }

    public Method getMethod() {
        return method;
    }

    public void setMethod(Method method) {
        this.method = method;
    }

}
