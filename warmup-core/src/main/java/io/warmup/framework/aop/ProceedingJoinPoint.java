package io.warmup.framework.aop;

import java.lang.reflect.Method;
import java.util.logging.Logger;

public class ProceedingJoinPoint extends JoinPoint {

    private static final Logger log = Logger.getLogger(ProceedingJoinPoint.class.getName());
    private final MethodInvocation invocation;

    public ProceedingJoinPoint(Object target, Method method, Object[] args, MethodInvocation invocation) {
        super(target, method, args);
        this.invocation = invocation;
    }

    @Override
    public Object proceed() {
        if (invocation == null) {
            throw new IllegalStateException("No MethodInvocation available");
        }
        try {
            return invocation.proceed();
        } catch (Throwable ex) {
            log.severe(ex.getMessage());
        }
        return null;
    }

    @Override
    public Method getMethod() {
        return super.getMethod();
    }
}
