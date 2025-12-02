package io.warmup.framework.core;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

public class ContainerTest {

    @Test
    public void testDependencyManagement() throws Exception {
        WarmupContainer container = new WarmupContainer();
        container.disableAutoShutdown();          // evita hook en los tests

        // 1. Registrar una dependencia manualmente
        container.register(TestService.class, true);

        // 2. Obtener el objeto Dependency para inspeccionar estado
        Dependency dep = container.getDependencyState(TestService.class);
        assertNotNull(dep, "Dependency no debería ser null");

        // 3. Estado inicial
        System.out.println("Estado inicial: " + dep.getDebugInfo());
        assertNull(dep.getCachedInstance(), "Instancia cacheada debe ser null inicialmente");
        assertFalse(dep.isInstanceCreated(), "isInstanceCreated debe ser false inicialmente");

        // 4. Crear instancia (aquí se produciría el StackOverflow si hubiera ciclo)
        TestService service = container.get(TestService.class);
        assertNotNull(service, "Service no debe ser null");

        // 5. Estado tras la resolución
        System.out.println("Estado después de get(): " + dep.getDebugInfo());
        assertNotNull(dep.getCachedInstance(), "getCachedInstance() no debe ser null tras crear instancia");
        assertTrue(dep.isInstanceCreated(), "isInstanceCreated() debe ser true tras crear instancia");

        // 6. Limpiar
        container.shutdownNow();
    }

    /* ---------- clase interna de prueba ---------- */
    public static class TestService {

        public TestService() {
        }
    }
}
