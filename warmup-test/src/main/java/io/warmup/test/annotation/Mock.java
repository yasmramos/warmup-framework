package io.warmup.test.annotation;

import io.warmup.test.config.MockConfig;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Anotación para crear mocks automáticamente.
 * 
 * Un @Mock crea un mock completo donde todos los métodos retornan valores por defecto.
 * Ideal para dependencias externas y servicios que no son el sistema bajo test.
 * 
 * Ejemplo:
 * ```java
 * @WarmupTest
 * public class UserServiceTest {
 *     @Mock
 *     private UserRepository userRepo;  // Mock completo
 *     
 *     @Spy  // Sistema bajo test
 *     private UserService userService;  // Spy con instancias reales
 * }
 * ```
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface Mock {
    
    /**
     * Configuración avanzada para el mock.
     * 
     * @return configuración del mock
     */
    MockConfig config() default @MockConfig();
    
    /**
     * Nombre específico para el mock.
     * Si no se especifica, se usa el nombre del campo.
     * 
     * @return nombre del mock
     */
    String name() default "";
    
    /**
     * Mockear automáticamente todos los métodos del tipo.
     * 
     * @return true para mockear todos los métodos
     */
    boolean stubAllMethods() default true;
}