package io.warmup.framework.health;

import io.warmup.framework.annotation.Health;
import io.warmup.framework.asm.AsmCoreUtils; // MIGRATED from SimpleASMUtils
import java.lang.reflect.Method;

/**
 * ✅ HEALTH CHECK SIN REFLEXIÓN DIRECTA
 * 
 * Esta implementación usa SimpleASMUtils que reduce significativamente
 * el uso de reflexión, aunque mantiene algunos usos para compatibilidad.
 * 
 * En una implementación completa, toda la reflexión sería reemplazada
 * con bytecode ASM puro generado dinámicamente.
 * 
 * @author MiniMax Agent
 * @version 1.0
 */
public class MethodHealthCheck implements HealthCheck {
    private final Object instance;
    private final io.warmup.framework.metadata.MethodMetadata methodMetadata;
    private final String name;
    private final long timeout;
    
    public MethodHealthCheck(Object instance, io.warmup.framework.metadata.MethodMetadata methodMetadata, Health healthAnnotation) {
        this.instance = instance;
        this.methodMetadata = methodMetadata;
        this.name = healthAnnotation.name().isEmpty() ? 
            instance.getClass().getSimpleName() + "." + methodMetadata.getSimpleName() : 
            healthAnnotation.name();
        this.timeout = healthAnnotation.timeout();
        
        // ✅ SIMPLIFICADO: SimpleASMUtils controla internamente el acceso a métodos privados
    }
    
    @Override
    public HealthResult check() {
        try {
            // ✅ MEJORADO: Usa SimpleASMUtils para encapsular la invocación
            // En el futuro, esto se reemplazaría con bytecode ASM puro
            Object result = AsmCoreUtils.invokeMethod(instance, methodMetadata.getSimpleName());
            
            if (result instanceof HealthResult) {
                return (HealthResult) result;
            } else if (result instanceof Boolean) {
                return (Boolean) result ? 
                    HealthResult.up("Health check passed") : 
                    HealthResult.down("Health check failed");
            } else if (result instanceof HealthStatus) {
                return new HealthResult((HealthStatus) result, "Health check completed");
            } else {
                return HealthResult.up("Health check returned: " + result);
            }
        } catch (Exception e) {
            return HealthResult.down("Health check method failed: " + e.getMessage(), e);
        }
    }
    
    @Override
    public String getName() {
        return name;
    }
    
    @Override
    public long getTimeout() {
        return timeout;
    }
}