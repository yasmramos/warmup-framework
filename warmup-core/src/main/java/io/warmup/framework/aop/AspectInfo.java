package io.warmup.framework.aop;

import java.lang.reflect.Method;

/**
 * Clase para almacenar información de aspectos*
 */
public class AspectInfo implements Comparable<AspectInfo> {

    private final Object aspectInstance;
    private final Method adviceMethod;
    private final String pointcutExpression;
    private final Class<?> annotationType; // Before, After, Around, AfterReturning, AfterThrowing
    private final String pointcutName; // Nombre del pointcut reutilizable
    private final String returningParameter; // Para @AfterReturning
    private final String throwingParameter; // Para @AfterThrowing
    private final int order; // Orden de ejecución

    public AspectInfo(Object aspectInstance, Method adviceMethod, String pointcutExpression,
            Class<?> annotationType, String pointcutName, String returningParameter,
            String throwingParameter, int order) {
        this.aspectInstance = aspectInstance;
        this.adviceMethod = adviceMethod;
        this.pointcutExpression = pointcutExpression;
        this.annotationType = annotationType;
        this.pointcutName = pointcutName;
        this.returningParameter = returningParameter;
        this.throwingParameter = throwingParameter;
        this.order = order;
    }

    public Object getAspectInstance() {
        return aspectInstance;
    }

    public Method getAdviceMethod() {
        return adviceMethod;
    }

    public String getPointcutExpression() {
        return pointcutExpression;
    }

    public Class<?> getAnnotationType() {
        return annotationType;
    }

    public String getPointcutName() {
        return pointcutName;
    }

    public String getReturningParameter() {
        return returningParameter;
    }

    public String getThrowingParameter() {
        return throwingParameter;
    }

    public int getOrder() {
        return order;
    }

    @Override
    public int compareTo(AspectInfo other) {
        return Integer.compare(this.order, other.order);
    }
}
