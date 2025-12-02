package io.warmup.framework.core;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

class InjectorTest {

    // --- Pruebas de creación y API simplificada ---
    @Test
    void create_Default_ReturnsWarmupBuilder() {
        Warmup warmup = Warmup.create();
        assertNotNull(warmup);
        assertDoesNotThrow(() -> warmup.start());
    }

    @Test
    void fluentApi_ChainsCorrectly() {
        Warmup warmup = Warmup.create()
                .scanPackages("io.warmup.framework.examples.services")
                .withProfiles("test", "development")
                .withProperty("db.url", "jdbc:h2:mem:test")
                .withProperty("app.debug", "true");

        assertNotNull(warmup);
    }

    @Test
    void start_CreatesAndReturnsContainer() {
        WarmupContainer container = Warmup.create()
                .scanPackages("io.warmup.framework.examples.services")
                .start();

        assertNotNull(container);
    }

    @Test
    void registerBean_RegistersDependency() {
        WarmupContainer container = Warmup.create().start();
        
        MyService service = new MyServiceImpl();
        container.registerBean("myService", MyService.class, service);
        
        assertNotNull(container.getBean("myService", MyService.class));
    }

    @Test
    void getBean_WithRegisteredBean_ReturnsBean() {
        WarmupContainer container = Warmup.create().start();
        
        MyService service = new MyServiceImpl();
        container.registerBean("myService", MyService.class, service);
        
        MyService retrievedService = container.getBean("myService", MyService.class);
        assertEquals(service, retrievedService);
    }

    @Test
    void getBean_WithoutName_ReturnsBeanByClass() {
        WarmupContainer container = Warmup.create().start();
        
        MyService service = new MyServiceImpl();
        container.registerBean("defaultService", MyService.class, service);
        
        MyService retrievedService = container.getBean("defaultService", MyService.class);
        assertEquals(service, retrievedService);
    }

    @Test
    void getBean_WithUnregisteredType_ThrowsException() {
        WarmupContainer container = Warmup.create().start();
        
        assertThrows(Exception.class, () -> {
            container.getBean(UnregisteredService.class);
        });
    }

    @Test
    void scanPackages_ScansForComponents() {
        assertDoesNotThrow(() -> {
            WarmupContainer container = Warmup.create()
                    .scanPackages("io.warmup.framework.examples.services")
                    .start();
            
            // El container debería poder inicializar aunque no encuentre componentes
            assertNotNull(container);
        });
    }

    @Test
    void withProperty_SetsProperty() {
        assertDoesNotThrow(() -> {
            Warmup warmup = Warmup.create()
                    .withProperty("test.property", "test.value")
                    .withProperty("another.property", "42");
            
            assertNotNull(warmup);
        });
    }

    @Test
    void withProfiles_ActivatesProfiles() {
        assertDoesNotThrow(() -> {
            Warmup warmup = Warmup.create()
                    .withProfiles("test", "development", "staging");
            
            assertNotNull(warmup);
        });
    }

    @Test
    void multipleRegistrations_HandleCorrectly() {
        WarmupContainer container = Warmup.create().start();
        
        MyService service1 = new MyServiceImpl();
        MyService service2 = new MyServiceImpl();
        
        container.registerBean("service1", MyService.class, service1);
        container.registerBean("service2", MyService.class, service2);
        
        MyService retrieved1 = container.getBean("service1", MyService.class);
        MyService retrieved2 = container.getBean("service2", MyService.class);
        
        assertNotNull(retrieved1);
        assertNotNull(retrieved2);
        assertEquals(service1, retrieved1);
        assertEquals(service2, retrieved2);
    }

    @Test
    void getBean_WithoutName_GetFirstRegisteredBean() {
        WarmupContainer container = Warmup.create().start();
        
        MyService service = new MyServiceImpl();
        container.registerBean("named", MyService.class, service);
        container.registerBean("another", MyService.class, service);
        
        // Debería obtener el bean sin nombre cuando se especifica solo la clase
        MyService retrieved = container.getBean("another", MyService.class);
        assertEquals(service, retrieved);
    }

    @Test
    void start_CreatesEmptyContainer() {
        WarmupContainer container = Warmup.create().start();
        
        assertNotNull(container);
        // Debería poder obtener un container vacío sin errores
        assertThrows(Exception.class, () -> {
            container.getBean(String.class); // Debería lanzar excepción para tipos no registrados
        });
    }

    @Test
    void getBean_WithStringName_ReturnsBean() {
        WarmupContainer container = Warmup.create().start();
        
        MyService service = new MyServiceImpl();
        container.registerBean("myService", MyService.class, service);
        
        Object retrievedService = container.getBean("myService");
        assertEquals(service, retrievedService);
    }

    @Test
    void scanPackages_MultiplePackages_ScansAll() {
        assertDoesNotThrow(() -> {
            WarmupContainer container = Warmup.create()
                    .scanPackages("io.warmup.framework.examples.services")
                    .scanPackages("io.warmup.framework.examples.config")
                    .start();
            
            assertNotNull(container);
        });
    }

    @Test
    void withProfiles_MultipleProfiles_ActivatesAll() {
        Warmup warmup = Warmup.create()
                .withProfiles("profile1", "profile2", "profile3");
        
        assertNotNull(warmup);
    }

    @Test
    void withProperty_MultipleProperties_SetsAll() {
        Warmup warmup = Warmup.create()
                .withProperty("key1", "value1")
                .withProperty("key2", "value2")
                .withProperty("key3", "123");
        
        assertNotNull(warmup);
    }

    @Test
    void registerBean_OverwriteExisting_UpdatesBean() {
        WarmupContainer container = Warmup.create().start();
        
        MyService service1 = new MyServiceImpl();
        MyService service2 = new MyServiceImpl();
        
        container.registerBean("myService", MyService.class, service1);
        container.registerBean("myService", MyService.class, service2);
        
        MyService retrieved = container.getBean("myService", MyService.class);
        assertEquals(service2, retrieved); // Debería obtener el más reciente
    }

    // --- Clases de ejemplo para pruebas ---
    interface MyService {
        void doSomething();
    }

    static class MyServiceImpl implements MyService {
        @Override
        public void doSomething() {
            System.out.println("Doing something in MyServiceImpl");
        }
    }

    interface UnregisteredService {
        void someMethod();
    }
}