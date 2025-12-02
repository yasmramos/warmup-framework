package io.warmup.framework.aop;

import io.warmup.framework.core.AopHandler;
import io.warmup.framework.core.WarmupContainer;
import io.warmup.framework.proxy.InvocationHandler;
import io.warmup.framework.proxy.Proxies;
import io.warmup.framework.proxy.ProxyBuilder;
import io.warmup.framework.asm.AsmCoreUtils; // MIGRATED from SimpleASMUtils
import java.util.logging.Level;
import java.util.logging.Logger;

public final class AspectProxy {

    private static final Logger log = Logger.getLogger(AspectProxy.class.getName());

    /**
     * Crea proxy AOP con Proxycraft (ASM puro).
     *
     * @param target instancia real a envolver
     * @param targetClass clase que implementa/extiende el proxy
     * @param container contenedor (de donde obtenemos AopHandler)
     * @return proxy listo para usar
     */
    @SuppressWarnings("unchecked")
    public static <T> T createProxy(T target, Class<T> targetClass, WarmupContainer container) {
        // Obtenemos el AopHandler del container
        AopHandler aopHandler = (AopHandler) container.getAopHandler(); // Asumiendo que agregas este getter en WarmupContainer

        // Si no hay aspectos que aplicar, devolver la instancia original
        if (!aopHandler.shouldApplyAopToClass(targetClass)) {
            log.log(Level.FINE, "No hay aspectos que apliquen a {0}, retornando instancia original",
                    targetClass.getSimpleName());
            return target;
        }

        log.log(Level.INFO, "Creando proxy Proxycraft para: {0}", targetClass.getSimpleName());

        try {
            if (targetClass.isInterface()) {
                // Proxy de interfaz
                return Proxies.createProxy(targetClass, new ProxycraftAspectHandler<>(target, aopHandler));
            } else {
                // Proxy de clase concreta (sin constructor padre)
                return ProxyBuilder.<T>createSubclass(targetClass)
                        .callSuperConstructor(false)
                        .intercept(new ProxycraftAspectHandler<>(target, aopHandler))
                        .build();
            }
        } catch (Exception e) {
            log.log(Level.SEVERE, "Error creando proxy con Proxycraft: {0}", e.getMessage());
            return target; // fallback: instancia original
        }
    }

    /* ------------------------------------------------------------------ */
    /*  Handler universal (interface & clase)                              */
    /* ------------------------------------------------------------------ */
    private static final class ProxycraftAspectHandler<T> implements InvocationHandler {

        private final T target;
        private final AopHandler aopHandler; // Cambiado de WarmupContainer a AopHandler

        ProxycraftAspectHandler(T target, AopHandler aopHandler) { // Cambiado el constructor
            this.target = target;
            this.aopHandler = aopHandler;
        }

        @Override
        public Object invoke(Object proxy, String methodName, Object[] args) throws Throwable {
            log.log(Level.FINE, "Interceptando método con Proxycraft: {0}.{1}",
                    new Object[]{target.getClass().getSimpleName(), methodName});

            try {
                // Usar ASM directamente para invocar el método
                return AsmCoreUtils.invokeMethod(target, methodName, args);
            } catch (Exception e) {
                log.log(Level.SEVERE, "Error invocando método {0}.{1}: {2}",
                        new Object[]{target.getClass().getSimpleName(), methodName, e.getMessage()});
                throw e;
            }
        }


    }
}