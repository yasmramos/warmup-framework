package io.warmup.framework.metrics;

import io.warmup.framework.annotation.Around;
import io.warmup.framework.annotation.Aspect;
import io.warmup.framework.annotation.Timed;
import io.warmup.framework.aop.ProceedingJoinPoint;
import io.warmup.framework.core.WarmupContainer;

@Aspect
public class MetricsAspect {

    private final MethodMetrics methodMetrics;
    private final WarmupContainer container;

    public MetricsAspect(WarmupContainer container, MethodMetrics methodMetrics) {
        this.container = container;
        this.methodMetrics = methodMetrics;
    }

    @Around("@annotation(io.warmup.framework.annotation.Timed)")
    public Object measureMethodExecution(ProceedingJoinPoint pjp, Timed timed) throws Throwable {
        String methodName = timed.name().isEmpty()
                ? pjp.getMethod().getName()
                : timed.name();

        String fullMethodName = pjp.getTarget().getClass().getSimpleName() + "." + methodName;

        long startTime = System.nanoTime();
        boolean success = false;

        try {
            Object result = pjp.proceed();
            success = true;
            return result;
        } finally {
            long duration = (System.nanoTime() - startTime) / 1_000_000; // Convert to milliseconds
            methodMetrics.recordMethodCall(fullMethodName, duration, success);

            // Also record in container metrics
            ((ContainerMetrics) container.getMetrics()).recordRequest(success);
        }
    }
}
