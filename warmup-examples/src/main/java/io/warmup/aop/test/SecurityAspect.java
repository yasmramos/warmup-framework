package io.warmup.aop.test;

import io.warmup.framework.annotation.Aspect;
import io.warmup.framework.annotation.Before;
import io.warmup.framework.annotation.Order;
import io.warmup.framework.aop.JoinPoint;

@Aspect
@Order(5)   // menor valor → mayor prioridad
public class SecurityAspect {

    @Before("execution(* io.warmup.aop.test.PaymentService.*(..))")
    public void check(JoinPoint jp) {
        System.out.println("[SECURITY] checking " + jp.getMethod().getName());
    }
}