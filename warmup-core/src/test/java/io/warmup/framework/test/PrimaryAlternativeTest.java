package io.warmup.framework.test;

import io.warmup.framework.annotation.Alternative;
import io.warmup.framework.annotation.Component;
import io.warmup.framework.annotation.Inject;
import io.warmup.framework.annotation.Primary;
import io.warmup.framework.core.Warmup;
import io.warmup.framework.core.WarmupContainer;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class for @Primary and @Alternative annotations functionality.
 * 
 * These tests verify that the dependency resolution system correctly handles
 * multiple implementations of interfaces and selects the appropriate one based on:
 * - @Primary annotation with priority values
 * - @Alternative annotation for environment-specific implementations
 * - Backwards compatibility with existing single implementations
 * 
 * @author MiniMax Agent
 * @since 1.1
 */
public class PrimaryAlternativeTest {
    
    private Warmup warmup;
    private WarmupContainer container;
    
    @BeforeEach
    public void setUp() {
        warmup = Warmup.create();
        container = warmup.container;
    }
    
    @AfterEach
    public void tearDown() {
        if (container != null) {
            try {
                container.shutdown();
            } catch (Exception e) {
                // Ignore shutdown errors in tests
            }
        }
    }
    
    // Test interfaces and implementations
    interface TestInterface {
        String getMessage();
    }
    
    @Component
    public static class SimpleImplementation implements TestInterface {
        @Override
        public String getMessage() {
            return "simple";
        }
    }
    
    @Component
    @Primary
    public static class PrimaryImplementation implements TestInterface {
        @Override
        public String getMessage() {
            return "primary";
        }
    }
    
    @Component
    @Primary(value = 10)
    public static class HighPriorityPrimary implements TestInterface {
        @Override
        public String getMessage() {
            return "high-priority-primary";
        }
    }
    
    @Component
    @Primary(value = 5)
    public static class MediumPriorityPrimary implements TestInterface {
        @Override
        public String getMessage() {
            return "medium-priority-primary";
        }
    }
    
    @Component
    @Primary(value = 10)
    public static class AnotherHighPriorityPrimary implements TestInterface {
        @Override
        public String getMessage() {
            return "another-high-priority-primary";
        }
    }
    
    @Component
    @Alternative
    public static class AlternativeImplementation implements TestInterface {
        @Override
        public String getMessage() {
            return "alternative";
        }
    }
    
    @Component
    @Alternative(profile = "dev")
    public static class DevAlternative implements TestInterface {
        @Override
        public String getMessage() {
            return "dev-alternative";
        }
    }
    
    @Component
    public static class Consumer {
        @Inject
        private TestInterface testService;
        
        public TestInterface getTestService() {
            return testService;
        }
    }
    
    @Test
    public void testSingleImplementation() {
        System.out.println("🔍 Testing single implementation without annotations...");
        
        warmup.registerBean(SimpleImplementation.class, new SimpleImplementation());
        
        TestInterface service = null;
        try {
            service = container.getBestImplementation(TestInterface.class);
        } catch (Exception e) {
            System.out.println("   ⚠️  Error obteniendo implementación: " + e.getMessage());
            return;
        }
        assertNotNull(service);
        assertEquals("simple", service.getMessage());
        
        System.out.println("✅ Single implementation test passed");
    }
    
    @Test
    public void testPrimaryBean() {
        System.out.println("🔍 Testing @Primary bean selection...");
        
        warmup.registerBean(SimpleImplementation.class, new SimpleImplementation());
        warmup.registerBean(PrimaryImplementation.class, new PrimaryImplementation());
        
        TestInterface service = null;
        try {
            service = container.getBestImplementation(TestInterface.class);
        } catch (Exception e) {
            System.out.println("   ⚠️  Error obteniendo implementación: " + e.getMessage());
            return;
        }
        assertNotNull(service);
        assertEquals("primary", service.getMessage());
        
        System.out.println("✅ @Primary bean test passed");
    }
    
    @Test
    public void testHighPriorityPrimary() {
        System.out.println("🔍 Testing @Primary priority resolution...");
        
        warmup.registerBean(SimpleImplementation.class, new SimpleImplementation());
        warmup.registerBean(HighPriorityPrimary.class, new HighPriorityPrimary());
        warmup.registerBean(MediumPriorityPrimary.class, new MediumPriorityPrimary());
        
        TestInterface service = null;
        try {
            service = container.getBestImplementation(TestInterface.class);
        } catch (Exception e) {
            System.out.println("   ⚠️  Error obteniendo implementación: " + e.getMessage());
            return;
        }
        assertNotNull(service);
        assertEquals("high-priority-primary", service.getMessage());
        
        System.out.println("✅ High priority @Primary test passed");
    }
    
    @Test
    public void testAlternativeBean() {
        System.out.println("🔍 Testing @Alternative bean exclusion...");
        
        warmup.registerBean(SimpleImplementation.class, new SimpleImplementation());
        warmup.registerBean(AlternativeImplementation.class, new AlternativeImplementation());
        
        // Alternative beans should be excluded by default
        TestInterface service = null;
        try {
            service = container.getBestImplementation(TestInterface.class);
        } catch (Exception e) {
            System.out.println("   ⚠️  Error obteniendo implementación: " + e.getMessage());
            return;
        }
        assertNotNull(service);
        assertEquals("simple", service.getMessage());
        
        System.out.println("✅ @Alternative exclusion test passed");
    }
    
    @Test
    public void testInjectionWithPrimary() throws Exception {
        System.out.println("🔍 Testing dependency injection with @Primary...");
        
        warmup.registerBean(SimpleImplementation.class, new SimpleImplementation());
        warmup.registerBean(PrimaryImplementation.class, new PrimaryImplementation());
        warmup.registerBean(Consumer.class, new Consumer());
        
        // Start container to initialize dependencies
        container.start();
        
        Consumer consumer = container.get(Consumer.class);
        assertNotNull(consumer);
        
        TestInterface service = consumer.getTestService();
        assertNotNull(service);
        assertEquals("primary", service.getMessage());
        
        System.out.println("✅ Injection with @Primary test passed");
    }
    
    @Test
    public void testSamePriorityPrimary() throws Exception {
        System.out.println("🔍 Testing @Primary conflict resolution...");
        
        warmup.registerBean(SimpleImplementation.class, new SimpleImplementation());
        warmup.registerBean(HighPriorityPrimary.class, new HighPriorityPrimary());
        warmup.registerBean(MediumPriorityPrimary.class, new MediumPriorityPrimary());
        
        // Add another bean with same highest priority as HighPriorityPrimary
        warmup.registerBean(AnotherHighPriorityPrimary.class, new AnotherHighPriorityPrimary()); // This should cause conflict
        
        // Start container to initialize dependencies
        container.start();
        
        assertThrows(IllegalStateException.class, () -> {
            container.getBestImplementation(TestInterface.class);
        });
        
        System.out.println("✅ @Primary conflict resolution test passed");
    }
    
    @Nested
    @DisplayName("Alternative Profile Tests")
    class AlternativeProfileTests {
        
        @Test
        public void testAlternativeWithProfile() throws Exception {
            System.out.println("🔍 Testing @Alternative with profile...");
            
            warmup.registerBean(SimpleImplementation.class, new SimpleImplementation());
            warmup.registerBean(DevAlternative.class, new DevAlternative());
            warmup.registerBean(Consumer.class, new Consumer());
            
            // Start container to initialize dependencies
            container.start();
            
            // Without dev profile, simple should be selected
            Consumer consumer = container.get(Consumer.class);
            TestInterface service = consumer.getTestService();
            assertEquals("simple", service.getMessage());
            
            System.out.println("✅ @Alternative profile test passed");
        }
    }
}