package io.warmup.test.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Anotación para forzar el uso de un bean real en lugar de un mock.
 * 
 * Útil en tests de integración donde queremos usar implementaciones reales
 * que están disponibles en el contexto de la aplicación.
 * 
 * Ejemplo:
 * ```java
 * @WarmupTest(mode = TestMode.INTEGRATION)
 * public class PaymentIntegrationTest {
 *     @Mock
 *     private BankGateway bankGateway;  // Mock el gateway externo
 *     
 *     @RealBean  // Usar implementación real disponible
 *     private TransactionRepository txRepo;
 * }
 * ```
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface RealBean {
    
    /**
     * Nombre del bean real en el contexto de la aplicación.
     * Si no se especifica, se busca por tipo.
     * 
     * @return nombre del bean
     */
    String name() default "";
    
    /**
     * Clase específica del bean si hay múltiples implementaciones.
     * 
     * @return clase del bean
     */
    Class<?> type() default Object.class;
}