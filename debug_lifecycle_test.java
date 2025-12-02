import io.warmup.framework.core.WarmupContainer;
import io.warmup.framework.annotation.*;

public class debug_lifecycle_test {
    
    public static void main(String[] args) {
        System.out.println("=== DEBUG LIFECYCLE TEST ===");
        
        WarmupContainer container = new WarmupContainer();
        
        try {
            // Registrar la clase de configuración
            container.register(LifecycleConfig.class, true);
            container.initializeAllComponents();
            
            System.out.println("Container initialized successfully");
            
            // Obtener el servicio de lifecycle
            LifecycleService service = container.get(LifecycleService.class);
            System.out.println("Retrieved service: " + service);
            System.out.println("Service class: " + service.getClass().getName());
            System.out.println("Service initialized state: " + service.isInitialized());
            System.out.println("Service hashcode: " + System.identityHashCode(service));
            
            // Verificar si el método init fue llamado
            System.out.println("init() method called - checking field...");
            try {
                java.lang.reflect.Field initializedField = LifecycleService.class.getDeclaredField("initialized");
                initializedField.setAccessible(true);
                Boolean initialized = (Boolean) initializedField.get(service);
                System.out.println("Direct field access - initialized: " + initialized);
            } catch (Exception e) {
                System.out.println("Error accessing field: " + e.getMessage());
            }
            
            container.shutdown();
            
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    @Configuration
    public static class LifecycleConfig {
        
        @Bean(initMethod = "init", destroyMethod = "destroy")
        public LifecycleService lifecycleService() {
            System.out.println("Creating LifecycleService instance");
            return new LifecycleService();
        }
    }
    
    public static class LifecycleService {
        private boolean initialized = false;
        private boolean destroyed = false;
        
        public void init() {
            System.out.println("init() method called!");
            this.initialized = true;
            System.out.println("initialized field set to: " + this.initialized);
        }
        
        public void destroy() {
            this.destroyed = true;
        }
        
        public boolean isInitialized() {
            System.out.println("isInitialized() called, returning: " + initialized);
            return initialized;
        }
        
        public boolean isDestroyed() {
            return destroyed;
        }
    }
}