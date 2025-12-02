import io.warmup.framework.core.WarmupContainer;
import io.warmup.framework.annotation.*;

public class SimpleLifecycleTest {
    
    public static void main(String[] args) {
        System.out.println("=== SIMPLE LIFECYCLE TEST ===");
        
        try {
            WarmupContainer container = new WarmupContainer();
            
            // Registrar la clase de configuración
            container.register(LifecycleConfig.class, true);
            container.initializeAllComponents();
            
            System.out.println("Container initialized successfully");
            
            // Obtener el servicio de lifecycle
            TestLifecycleService service = container.get(TestLifecycleService.class);
            
            if (service == null) {
                System.out.println("ERROR: service is null");
                return;
            }
            
            System.out.println("Retrieved service: " + service);
            System.out.println("Service hashcode: " + System.identityHashCode(service));
            System.out.println("Service initialized: " + service.isInitialized());
            
            if (service.isInitialized()) {
                System.out.println("✅ SUCCESS: init method worked correctly!");
            } else {
                System.out.println("❌ FAILURE: init method did not work");
            }
            
            container.shutdown();
            System.out.println("Test completed successfully");
            
        } catch (Exception e) {
            System.out.println("ERROR: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    @Configuration
    public static class LifecycleConfig {
        
        @Bean(initMethod = "init", destroyMethod = "destroy")
        public TestLifecycleService lifecycleService() {
            System.out.println("Creating TestLifecycleService instance");
            return new TestLifecycleService();
        }
    }
    
    public static class TestLifecycleService {
        private boolean initialized = false;
        private boolean destroyed = false;
        
        public TestLifecycleService() {
            System.out.println("TestLifecycleService constructor called");
        }
        
        public void init() {
            System.out.println("TestLifecycleService.init() called - setting initialized=true");
            this.initialized = true;
            System.out.println("TestLifecycleService.init() completed - initialized=" + this.initialized);
        }
        
        public void destroy() {
            System.out.println("TestLifecycleService.destroy() called");
            this.destroyed = true;
        }
        
        public boolean isInitialized() {
            System.out.println("TestLifecycleService.isInitialized() called - returning " + initialized);
            return initialized;
        }
        
        public boolean isDestroyed() {
            return destroyed;
        }
    }
}