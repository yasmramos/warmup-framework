package io.warmup.framework.lazy;

import io.warmup.framework.asm.AsmCoreUtils; // MIGRATED from SimpleASMUtils
import io.warmup.framework.proxy.InvocationHandler;
import io.warmup.framework.proxy.Proxies;
import io.warmup.framework.proxy.ProxyBuilder;
import java.util.function.Supplier;

/**
 * Factory para proxies lazy que evita problemas de genéricos Implementación con
 * Proxycraft (100 % ASM, sin dependencias externas).
 */
public final class LazyFactory {

    /**
     * Crea un proxy lazy para cualquier tipo (interface o clase concreta).
     *
     * @param targetType tipo que implementará/extenderá el proxy
     * @param instanceSupplier proveedor de la instancia real (se invocará solo
     * una vez)
     * @return proxy lazy listo para usar
     */
    @SuppressWarnings("unchecked")
    public static <T> T createLazyProxy(Class<T> targetType, Supplier<T> instanceSupplier) {
        if (targetType.isInterface()) {
            System.out.println("🔧 Usando Proxycraft para interface: " + targetType.getSimpleName());
            return Proxies.createProxy(targetType, new LazyHandler<>(targetType, instanceSupplier));
        } else {
            System.out.println("🔧 Usando Proxycraft para clase concreta: " + targetType.getSimpleName());
            return ProxyBuilder.<T>createSubclass(targetType)
                    .callSuperConstructor(false) // sin <init> del padre
                    .intercept(new LazyHandler<>(targetType, instanceSupplier))
                    .build();
        }
    }

    /* ---------------------------------------------------------------------- */
 /*  Handler universal (interface & clase)                                 */
 /* ---------------------------------------------------------------------- */
    private static final class LazyHandler<T> implements InvocationHandler {

        private final Class<T> targetType;
        private final Supplier<T> supplier;
        private volatile T real;
        private volatile boolean initialized = false;
        private final Object lock = new Object();

        LazyHandler(Class<T> targetType, Supplier<T> supplier) {
            this.targetType = targetType;
            this.supplier = supplier;
        }

        @Override
        public Object invoke(Object proxy, String methodName, Object[] args) throws Throwable {
            // Métodos de Object los resolvemos sin inicializar
            if ("toString".equals(methodName)) {
                return "LazyProxy{Proxycraft-" + targetType.getSimpleName() + ", initialized=" + initialized + "}";
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
                        System.out.println("🐢 Proxycraft - Inicializando perezosamente: " + targetType.getSimpleName());
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
}
