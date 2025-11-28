package io.warmup.aop.test;

import io.warmup.framework.annotation.After;
import io.warmup.framework.annotation.AfterReturning;
import io.warmup.framework.annotation.AfterThrowing;
import io.warmup.framework.annotation.Around;
import io.warmup.framework.annotation.Aspect;
import io.warmup.framework.annotation.Before;
import io.warmup.framework.annotation.Order;
import io.warmup.framework.annotation.Pointcut;
import io.warmup.framework.aop.JoinPoint;
import io.warmup.framework.aop.ProceedingJoinPoint;
import java.util.Arrays;

@Aspect
@Order(10)
public class LoggingAspect {

    /* ---------- reusable pointcut ---------- */
    @Pointcut("execution(* io.warmup.aop.test.PaymentService.*(..))")
    public void businessMethods() {}

    /* ---------- @Before ---------- */
    @Before("businessMethods()")
    public void logStart(JoinPoint jp) {
        System.out.println("[LOG] >>> " + jp.getMethod().getName() + " - " + Arrays.toString(jp.getArgs()));
    }

    /* ---------- @After ---------- */
    @After("businessMethods()")
    public void logEnd(JoinPoint jp) {
        System.out.println("[LOG] <<< " + jp.getMethod().getName());
    }

    /* ---------- @Around (medición de tiempo) ---------- */
    @Around("businessMethods()")
    public Object timer(ProceedingJoinPoint pjp) throws Throwable {
        long t0 = System.nanoTime();
        try {
            return pjp.proceed();
        } finally {
            long ms = (System.nanoTime() - t0) / 1_000_000;
            System.out.println("[TIMER] " + pjp.getMethod().getName() + " took " + ms + " ms");
        }
    }

    /* ---------- @AfterReturning ---------- */
    @AfterReturning(pointcut = "businessMethods()", returning = "result")
    public void logResult(JoinPoint jp, Object result) {
        System.out.println("[RETURN] " + jp.getMethod().getName() + " -> " + result);
    }

    /* ---------- @AfterThrowing ---------- */
    @AfterThrowing(pointcut = "businessMethods()", throwing = "ex")
    public void logError(JoinPoint jp, Throwable ex) {
        System.out.println("[ERROR] " + jp.getMethod().getName() + " threw " + ex.getClass().getSimpleName());
    }

    /* ---------- @annotation pointcut ---------- */
    @Around("@annotation(io.warmup.framework.annotation.Timed)")
    public Object timedAnnotation(ProceedingJoinPoint pjp) throws Throwable {
        System.out.println("[TIMED] start " + pjp.getMethod().getName());
        try {
            return pjp.proceed();
        } finally {
            System.out.println("[TIMED] end " + pjp.getMethod().getName());
        }
    }
}