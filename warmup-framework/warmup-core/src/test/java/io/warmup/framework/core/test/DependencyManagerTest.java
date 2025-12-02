package io.warmup.framework.core.test;

import io.warmup.framework.core.WarmupContainer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 🚀 TEST BASIC DEPENDENCY MANAGEMENT - Verificación de gestión básica de dependencias
 * 
 * Test suite simplificado para funcionalidad básica de gestión de dependencias
 * usando solo la API simplificada disponible en WarmupContainer.
 */
public class DependencyManagerTest {

    private WarmupContainer container;

    @BeforeEach
    void setUp() {
        container = new WarmupContainer();
    }

    @Test
    void testBasicBeanRegistrationAndResolution() {
        // Test registro y resolución básica de beans usando la API simplificada
        SimpleService service = new SimpleService();
        container.registerBean("myService", SimpleService.class, service);
        
        SimpleService resolved = container.getBean(SimpleService.class);
        
        assertNotNull(resolved);
        assertEquals(service, resolved);
    }

    @Test
    void testMultipleBeanRegistration() {
        // Test registro de múltiples beans
        SimpleService service1 = new SimpleService();
        AnotherService service2 = new AnotherService();
        TestComponent component = new TestComponent();
        
        container.registerBean("service1", SimpleService.class, service1);
        container.registerBean("service2", AnotherService.class, service2);
        container.registerBean("component", TestComponent.class, component);
        
        SimpleService resolved1 = container.getBean("service1", SimpleService.class);
        AnotherService resolved2 = container.getBean("service2", AnotherService.class);
        TestComponent resolved3 = container.getBean("component", TestComponent.class);
        
        assertNotNull(resolved1);
        assertNotNull(resolved2);
        assertNotNull(resolved3);
        assertEquals(service1, resolved1);
        assertEquals(service2, resolved2);
        assertEquals(component, resolved3);
    }

    @Test
    void testBeanResolutionByName() {
        // Test resolución de beans por nombre específico
        SimpleService service = new SimpleService();
        container.registerBean("namedService", SimpleService.class, service);
        
        SimpleService resolved = container.getBean("namedService", SimpleService.class);
        
        assertNotNull(resolved);
        assertEquals(service, resolved);
    }

    @Test
    void testNonExistentBeanResolution() {
        // Test que se lance excepción al resolver bean inexistente
        assertThrows(Exception.class, () -> {
            container.getBean(NonExistentService.class);
        });
    }

    @Test
    void testBeanTypeSafety() {
        // Test seguridad de tipos en bean resolution
        SimpleService service = new SimpleService();
        container.registerBean("service", SimpleService.class, service);
        
        SimpleService resolved = container.getBean(SimpleService.class);
        
        assertNotNull(resolved);
        assertInstanceOf(SimpleService.class, resolved);
        assertEquals("Simple Service", resolved.getMessage());
    }

    @Test
    void testSimpleDependencyChain() {
        // Test cadena simple de dependencias
        SimpleService simpleService = new SimpleService();
        TestComponent component = new TestComponent();
        component.setSimpleService(simpleService);
        
        container.registerBean("simpleService", SimpleService.class, simpleService);
        container.registerBean("component", TestComponent.class, component);
        
        TestComponent resolvedComponent = container.getBean(TestComponent.class);
        SimpleService resolvedSimple = container.getBean("simpleService", SimpleService.class);
        
        assertNotNull(resolvedComponent);
        assertNotNull(resolvedSimple);
        assertEquals(simpleService, resolvedComponent.getSimpleService());
    }

    @Test
    void testBeanOverwrite() {
        // Test sobrescritura de bean existente
        SimpleService originalService = new SimpleService();
        SimpleService newService = new SimpleService();
        
        container.registerBean("service", SimpleService.class, originalService);
        SimpleService resolved1 = container.getBean("service", SimpleService.class);
        
        container.registerBean("service", SimpleService.class, newService);
        SimpleService resolved2 = container.getBean("service", SimpleService.class);
        
        assertEquals(originalService, resolved1);
        assertEquals(newService, resolved2);
        assertNotEquals(resolved1, resolved2);
    }

    @Test
    void testBeanNameConflict() {
        // Test manejo de nombres duplicados
        SimpleService service1 = new SimpleService();
        SimpleService service2 = new SimpleService();
        
        container.registerBean("conflict", SimpleService.class, service1);
        container.registerBean("conflict", SimpleService.class, service2);
        
        SimpleService resolved = container.getBean("conflict", SimpleService.class);
        
        // El último registro debe prevalecer
        assertEquals(service2, resolved);
    }

    /**
     * 🎯 CLASES DE SOPORTE PARA TESTING
     */
    
    public static class SimpleService {
        public String getMessage() { 
            return "Simple Service"; 
        }
    }
    
    public static class AnotherService {
        public String getName() { 
            return "Another Service"; 
        }
    }
    
    public static class TestComponent {
        private SimpleService simpleService;
        private AnotherService anotherService;
        
        public SimpleService getSimpleService() { 
            return simpleService; 
        }
        
        public AnotherService getAnotherService() { 
            return anotherService; 
        }
        
        public void setSimpleService(SimpleService service) { 
            this.simpleService = service; 
        }
        
        public void setAnotherService(AnotherService service) { 
            this.anotherService = service; 
        }
    }
    
    // Bean que no está registrado para testing de errores
    public static class NonExistentService {
        public String getMessage() { 
            return "Non-existent Service"; 
        }
    }
}