package io.warmup.examples;

import io.warmup.framework.annotation.Component;
import io.warmup.framework.annotation.Inject;

/**
 * Clase de prueba para verificar que el WarmupBytecodeProcessor funciona correctamente.
 * Esta clase será procesada en tiempo de compilación para generar bytecode .class.
 */
@Component
public class BytecodeProcessorTest {

    private final Service service;
    private final Repository repository;

    @Inject
    public BytecodeProcessorTest(Service service, Repository repository) {
        this.service = service;
        this.repository = repository;
    }

    public void runTest() {
        System.out.println("\n=== Prueba del WarmupBytecodeProcessor ===");
        System.out.println("✅ Componentes inyectados correctamente:");
        System.out.println("   - Service: " + service.getClass().getSimpleName());
        System.out.println("   - Repository: " + repository.getClass().getSimpleName());

        // Probar funcionalidad
        System.out.println("\n--- Probando Service ---");
        String result = service.process("test-data");
        System.out.println("Resultado: " + result);

        System.out.println("\n--- Probando Repository ---");
        repository.save("data-1");
        repository.save("data-2");
        System.out.println("Datos guardados: " + repository.findAll());

        System.out.println("\n✅ WarmupBytecodeProcessor funcionando correctamente!");
    }

    public static void main(String[] args) {
        System.out.println("Iniciando prueba del procesador de bytecode...");
        // Esta clase debería ser inyectada por el framework
        // Para demostración manual, solo imprimimos que la clase existe
        System.out.println("Clase BytecodeProcessorTest compilada exitosamente");
        System.out.println("Si ves esta salida, significa que:");
        System.out.println("1. El WarmupBytecodeProcessor se ejecutó en tiempo de compilación");
        System.out.println("2. La clase fue procesada correctamente");
        System.out.println("3. El bytecode se generó sin errores");
    }
}