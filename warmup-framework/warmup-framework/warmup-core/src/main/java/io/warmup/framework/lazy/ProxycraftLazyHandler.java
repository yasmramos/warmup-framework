package io.warmup.framework.lazy;

import io.warmup.framework.asm.AsmCoreUtils; // MIGRATED from SimpleASMUtils
import io.warmup.framework.proxy.InvocationHandler;
import java.util.function.Supplier;

public final class ProxycraftLazyHandler<T> implements InvocationHandler {

    private final Class<T> type;
    private final Supplier<T> supplier;
    private volatile T real;
    private volatile boolean initialized = false;
    private final Object lock = new Object();

    public ProxycraftLazyHandler(Class<T> type, Supplier<T> supplier) {
        this.type = type;
        this.supplier = supplier;
    }

    @Override
    public Object invoke(Object proxy, String methodName, Object[] args) throws Throwable {
        // Resolver métodos de Object sin inicializar
        if ("toString".equals(methodName)) {
            return "LazyProxy{Proxycraft-" + type.getSimpleName() + ", initialized=" + initialized + "}";
        }
        if ("hashCode".equals(methodName)) {
            return System.identityHashCode(proxy);
        }
        if ("equals".equals(methodName)) {
            return proxy == args[0];
        }

        // Inicialización bajo demanda
        if (!initialized) {
            synchronized (lock) {
                if (!initialized) {
                    System.out.println("🐢 Proxycraft - Inicializando perezosamente: " + type.getSimpleName());
                    real = supplier.get();
                    initialized = true;
                    System.out.println("✅ Proxycraft - Inicialización lazy completada");
                }
            }
        }

        // Delegar al objeto real
        // ✅ REFACTORIZADO: Usar ASM en lugar de reflexión
        return AsmCoreUtils.invokeMethod(real, methodName, args);
    }


}
