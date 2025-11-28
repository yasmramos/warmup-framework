package io.warmup.test.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Anotación para marcar explícitamente que un @Spy es el sistema bajo test
 * y debe recibir inyección de dependencias de los @Mock disponibles.
 * 
 * Esta anotación es opcional ya que @Spy ya incluye auto-inyección, pero
 * se puede usar para mayor claridad semántica.
 * 
 * Ejemplo:
 * ```java
 * @WarmupTest
 * public class ServiceTest {
 *     @Spy
 *     @InjectMocks  // ← Opcional para claridad
 *     private UserService userService;
 *     
 *     @Mock
 *     private UserRepository userRepo;  // Auto-inyectado en userService
 * }
 * ```
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface InjectMocks {
}