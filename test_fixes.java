import io.warmup.framework.core.Warmup;
import io.warmup.framework.annotation.*;

/**
 * Test simple para verificar las correcciones de los 3 problemas en ConfigurationBeanTest
 */
public class test_fixes {
    
    public static void main(String[] args) {
        try {
            System.out.println("=== Testing ConfigurationBeanTest Fixes ===\n");
            
            // Test 1: Prototype Scope
            testPrototypeScope();
            
            // Test 2: Application Scope  
            testApplicationScope();
            
            // Test 3: Primary/Alternative
            testPrimaryAlternativeIntegration();
            
            System.out.println("\n=== All Tests Completed ===");
            
        } catch (Exception e) {
            System.err.println("Test failed with exception: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    // Test 1: Prototype Scope
    static void testPrototypeScope() {
        System.out.println("1. Testing Prototype Scope...");
        
        Warmup warmup = Warmup.create();
        warmup.getContainer().start();
        
        // Register prototype config
        PrototypeConfig config = new PrototypeConfig();
        warmup.registerBean(PrototypeConfig.class, config);
        
        // Request same bean multiple times
        TestService service1 = warmup.getBean(TestService.class);
        TestService service2 = warmup.getBean(TestService.class);
        
        // Should be different instances (prototype)
        if (service1 != service2) {
            System.out.println("   ✅ PASS: Prototype beans are different instances");
        } else {
            System.out.println("   ❌ FAIL: Prototype beans are same instance");
        }
        
        warmup.shutdown();
    }
    
    // Test 2: Application Scope
    static void testApplicationScope() {
        System.out.println("2. Testing Application Scope...");
        
        Warmup warmup = Warmup.create();
        warmup.getContainer().start();
        
        // Register application scope config
        ApplicationScopeConfig config = new ApplicationScopeConfig();
        warmup.registerBean(ApplicationScopeConfig.class, config);
        
        try {
            TestService service = warmup.getBean(TestService.class);
            if (service != null) {
                System.out.println("   ✅ PASS: ApplicationScopedTestService found: " + service.getClass().getSimpleName());
            } else {
                System.out.println("   ❌ FAIL: ApplicationScopedTestService not found");
            }
        } catch (Exception e) {
            System.out.println("   ❌ FAIL: Exception getting ApplicationScopedTestService: " + e.getMessage());
        }
        
        warmup.shutdown();
    }
    
    // Test 3: Primary/Alternative
    static void testPrimaryAlternativeIntegration() {
        System.out.println("3. Testing Primary/Alternative Integration...");
        
        Warmup warmup = Warmup.create();
        warmup.getContainer().start();
        
        // Register primary/alternative config
        PrimaryAltConfig config = new PrimaryAltConfig();
        warmup.registerBean(PrimaryAltConfig.class, config);
        
        try {
            // Should get the @Primary implementation
            TestService primaryService = warmup.getBean(TestService.class);
            if (primaryService instanceof PrimaryTestServiceImpl) {
                System.out.println("   ✅ PASS: @Primary implementation resolved correctly");
            } else {
                System.out.println("   ❌ FAIL: Wrong implementation resolved: " + 
                    (primaryService != null ? primaryService.getClass().getSimpleName() : "null"));
            }
        } catch (Exception e) {
            System.out.println("   ❌ FAIL: Exception resolving @Primary: " + e.getMessage());
        }
        
        warmup.shutdown();
    }
    
    // Configuration classes for testing
    @Configuration
    public static class PrototypeConfig {
        @Bean(scope = "prototype")
        public TestService prototypeService() {
            return new TestServiceImpl();
        }
    }
    
    @Configuration
    public static class ApplicationScopeConfig {
        @Bean
        @ApplicationScope
        public TestService applicationScopedService() {
            return new ApplicationScopedTestService();
        }
    }
    
    @Configuration
    public static class PrimaryAltConfig {
        @Bean
        @Primary
        public TestService primaryService() {
            return new PrimaryTestServiceImpl();
        }
        
        @Bean
        @Alternative
        public TestService alternativeService() {
            return new TestServiceImpl();
        }
    }
    
    // Test service classes
    public interface TestService {
        void doSomething();
    }
    
    public static class TestServiceImpl implements TestService {
        public TestServiceImpl() {}
        public void doSomething() {}
    }
    
    public static class PrimaryTestServiceImpl implements TestService {
        public PrimaryTestServiceImpl() {}
        public void doSomething() {}
    }
    
    public static class ApplicationScopedTestService implements TestService {
        public ApplicationScopedTestService() {}
        public void doSomething() {}
    }
}