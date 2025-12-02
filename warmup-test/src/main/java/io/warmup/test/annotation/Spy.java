package io.warmup.test.annotation;

import io.warmup.test.config.SpyConfig;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Anotación para crear spies automáticamente con inyección de dependencias.
 * 
 * Un @Spy crea un spy sobre una instancia real del objeto, permitiendo:
 * - Ejecutar métodos reales por defecto
 * - Mockear métodos específicos cuando sea necesario
 * - Auto-inyección de dependencias basada en los @Mock disponibles
 * 
 * Ideal para el sistema bajo test (SUT) cuando queremos mantener comportamiento real
 * pero poder stubear métodos específicos.
 * 
 * Ejemplo:
 * ```java
 * @WarmupTest
 * public class OrderServiceTest {
 *     @Spy
 *     private OrderService orderService;  // Spy con inyección automática
 *     
 *     @Mock  // Auto-inyectado en OrderService
 *     private PaymentService paymentService;
 *     
 *     @Mock  // Auto-inyectado en OrderService
 *     private InventoryManager inventoryManager;
 * }
 * ```
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface Spy {
    
    /**
     * Configuración avanzada para el spy.
     * 
     * @return configuración del spy
     */
    SpyConfig config() default @SpyConfig();
    
    /**
     * Clase de implementación real a usar.
     * Si no se especifica, se usa el tipo del campo.
     * 
     * @return clase de implementación
     */
    Class<?> implementation() default Object.class;
    
    /**
     * Usar implementación real vs mock.
     * 
     * @return true para usar implementación real (spy real)
     *         false para usar mock (spy sobre mock)
     */
    boolean realImplementation() default true;
    
    /**
     * Auto-inyectar dependencias basadas en @Mock disponibles.
     * 
     * @return true para auto-inyección
     */
    boolean autoInject() default true;
}