package io.warmup.test.annotation;

import io.warmup.test.config.TestMode;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Anotación principal que activa el modo zero-config para toda la clase de test.
 * 
 * Esta anotación elimina completamente la necesidad de:
 * - Clases @TestConfiguration
 * - Métodos @BeforeEach con setup manual
 * - Configuración de mocks con MockitoAnnotations.openMocks()
 * - Inyección manual de dependencias
 * 
 * El framework automáticamente:
 * 1. Escanea todos los campos @Mock y @Spy
 * 2. Crea los mocks y spies  
 * 3. Inyecta dependencias automáticamente
 * 4. Maneja el lifecycle completo
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface WarmupTest {
    
    /**
     * Modo de testing que determina el comportamiento de mocks y spies.
     * 
     * @return modo de test (UNIT, INTEGRATION, SYSTEM)
     */
    TestMode mode() default TestMode.UNIT;
    
    /**
     * Auto-mockear dependencias no declaradas en la clase de test.
     * 
     * @return true para auto-mockear dependencias faltantes
     */
    boolean autoMock() default true;
    
    /**
     * Tiempo de warm-up antes de ejecutar los tests.
     * Formato: "1s", "500ms", "2s500ms"
     * 
     * @return tiempo de warm-up
     */
    String warmupTime() default "1s";
    
    /**
     * Habilitar verbose logging para debugging.
     * 
     * @return true para logging detallado
     */
    boolean verbose() default false;
}