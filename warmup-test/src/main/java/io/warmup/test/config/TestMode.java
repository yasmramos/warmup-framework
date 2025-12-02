package io.warmup.test.config;

/**
 * Modos de testing que determinan el comportamiento de mocks y spies.
 * 
 * Cada modo define qué tipo de instancias crear y cómo inyectar dependencias:
 */
public enum TestMode {
    
    /**
     * Modo unit test: Máximo uso de mocks.
     * - @Mock: Mocks completos
     * - @Spy: Spies sobre instancias reales con mocks inyectados
     * - Dependencias externas siempre mockeadas
     * - Ideal para tests unitarios puros
     */
    UNIT,
    
    /**
     * Modo integration test: Mix de real y mock.
     * - @Mock: Mocks para dependencias externas
     * - @Spy: Spies que usan implementaciones reales cuando es posible
     * - @RealBean: Usa beans reales del contexto de la aplicación
     * - Ideal para tests de integración parcial
     */
    INTEGRATION,
    
    /**
     * Modo system test: Máximo uso de implementaciones reales.
     * - @Mock: Solo para dependencias no disponibles
     * - @Spy: Spies sobre implementaciones reales
     * - Intenta usar todas las implementaciones reales posibles
     * - Ideal para tests end-to-end
     */
    SYSTEM
}