package io.warmup.framework.aop;

// import io.warmup.framework.jit.asm.SimpleASMUtils; // MIGRATED to AsmCoreUtils
import io.warmup.framework.asm.AsmCoreUtils;
import java.lang.reflect.Method;

/**
 * Representa un punto de ejecución *
 */
public class JoinPoint {

    private final Object target;
    private final Method method;
    private final Object[] args;

    public JoinPoint(final Object target, final Method method, final Object[] args) {
        this.target = target;
        this.method = method;
        this.args = args;
    }

    public Object getTarget() {
        return target;
    }

    public Method getMethod() {
        return method;
    }

    public String getMethodName() {
        return AsmCoreUtils.getName(method);
    }

    public Object[] getArgs() {
        return args;
    }

    public Object proceed() throws Exception {
        // ✅ REFACTORIZADO: Usar ASM optimizado en lugar de reflexión
        return AsmCoreUtils.invokeMethod(target, AsmCoreUtils.getName(method), args);
    }
}
