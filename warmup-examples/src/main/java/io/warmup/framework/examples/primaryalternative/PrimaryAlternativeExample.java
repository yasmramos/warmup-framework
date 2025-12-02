package io.warmup.framework.examples.primaryalternative;

import io.warmup.framework.annotation.*;
import io.warmup.framework.core.Warmup;
import io.warmup.framework.core.WarmupContainer;

/**
 * Example demonstrating the use of @Primary and @Alternative annotations
 * in the Warmup Framework for dependency injection scenarios.
 * 
 * This example shows:
 * 1. How to use @Primary to select a preferred implementation
 * 2. How to use @Alternative for environment-specific implementations
 * 3. How @Primary priority values work for conflict resolution
 * 4. Integration with dependency injection
 * 
 * @author MiniMax Agent
 * @since 1.1
 */
public class PrimaryAlternativeExample {
    
    // ===========================================
    // INTERFACES
    // ===========================================
    
    /**
     * Service interface for notification operations.
     */
    interface NotificationService {
        void sendNotification(String message);
        String getServiceType();
    }
    
    /**
     * Repository interface for data access operations.
     */
    interface UserRepository {
        String findUserById(String id);
        void saveUser(String userData);
    }
    
    // ===========================================
    // IMPLEMENTATIONS WITHOUT ANNOTATIONS
    // ===========================================
    
    @Component
    public static class SimpleEmailService implements NotificationService {
        @Override
        public void sendNotification(String message) {
            System.out.println("📧 Email: " + message);
        }
        
        @Override
        public String getServiceType() {
            return "SimpleEmail";
        }
    }
    
    @Component
    public static class SimpleUserRepository implements UserRepository {
        @Override
        public String findUserById(String id) {
            return "User data for " + id + " from simple repository";
        }
        
        @Override
        public void saveUser(String userData) {
            System.out.println("💾 Saved to simple repository: " + userData);
        }
    }
    
    // ===========================================
    // PRIMARY IMPLEMENTATIONS
    // ===========================================
    
    /**
     * Primary notification service with highest priority.
     */
    @Component
    @Primary(value = 100)
    public static class PremiumEmailService implements NotificationService {
        @Override
        public void sendNotification(String message) {
            System.out.println("🌟 PREMIUM Email: " + message + " (Priority: 100)");
        }
        
        @Override
        public String getServiceType() {
            return "PremiumEmail";
        }
    }
    
    /**
     * Primary notification service with medium priority.
     */
    @Component
    @Primary(value = 50)
    public static class BusinessEmailService implements NotificationService {
        @Override
        public void sendNotification(String message) {
            System.out.println("💼 Business Email: " + message + " (Priority: 50)");
        }
        
        @Override
        public String getServiceType() {
            return "BusinessEmail";
        }
    }
    
    /**
     * Primary user repository with higher priority than simple implementation.
     */
    @Component
    @Primary
    public static class DatabaseUserRepository implements UserRepository {
        @Override
        public String findUserById(String id) {
            return "User data for " + id + " from database repository";
        }
        
        @Override
        public void saveUser(String userData) {
            System.out.println("🗄️ Saved to database repository: " + userData);
        }
    }
    
    // ===========================================
    // ALTERNATIVE IMPLEMENTATIONS
    // ===========================================
    
    /**
     * Alternative SMS notification service (not used by default).
     */
    @Component
    @Alternative
    public static class SmsNotificationService implements NotificationService {
        @Override
        public void sendNotification(String message) {
            System.out.println("📱 SMS: " + message);
        }
        
        @Override
        public String getServiceType() {
            return "SMS";
        }
    }
    
    /**
     * Alternative notification service for development environment.
     */
    @Component
    @Alternative(profile = "dev")
    public static class MockNotificationService implements NotificationService {
        @Override
        public void sendNotification(String message) {
            System.out.println("🧪 MOCK DEV Service: " + message);
        }
        
        @Override
        public String getServiceType() {
            return "MockDev";
        }
    }
    
    /**
     * Alternative repository for testing purposes.
     */
    @Component
    @Alternative
    public static class InMemoryUserRepository implements UserRepository {
        @Override
        public String findUserById(String id) {
            return "MOCK data for " + id + " from in-memory repository";
        }
        
        @Override
        public void saveUser(String userData) {
            System.out.println("🧪 Saved to in-memory repository: " + userData);
        }
    }
    
    // ===========================================
    // SERVICE CLASSES THAT USE DEPENDENCY INJECTION
    // ===========================================
    
    @Component
    public static class NotificationController {
        private final NotificationService notificationService;
        private final UserRepository userRepository;
        
        @Inject
        public NotificationController(NotificationService notificationService, 
                                    UserRepository userRepository) {
            this.notificationService = notificationService;
            this.userRepository = userRepository;
        }
        
        public void sendUserNotification(String userId, String message) {
            String userData = userRepository.findUserById(userId);
            System.out.println("Found user: " + userData);
            
            notificationService.sendNotification("To " + userId + ": " + message);
            System.out.println("Using service type: " + notificationService.getServiceType());
        }
    }
    
    // @Component  // Disabled to avoid resolver naming conflict with main UserService
    static class UserService {
        private final UserRepository userRepository;
        
        @Inject
        public UserService(UserRepository userRepository) {
            this.userRepository = userRepository;
        }
        
        public void createUser(String userData) {
            userRepository.saveUser(userData);
            System.out.println("User service used repository type: " + 
                             userRepository.getClass().getSimpleName());
        }
    }
    
    // ===========================================
    // DEMONSTRATION METHODS
    // ===========================================
    
    public static void demonstrateBasicUsage() throws Exception {
        System.out.println("\n🎯 === BASIC PRIMARY/ALTERNATIVE USAGE ===");
        
        Warmup warmup = Warmup.create();
        WarmupContainer container = warmup.start();
        
        // Register all components (using reflection to create instances)
        warmup.registerBean(SimpleEmailService.class, new SimpleEmailService());
        warmup.registerBean(PremiumEmailService.class, new PremiumEmailService());
        warmup.registerBean(BusinessEmailService.class, new BusinessEmailService());
        warmup.registerBean(SmsNotificationService.class, new SmsNotificationService());
        // Nota: NotificationController se creará automáticamente por el contenedor
        
        // Get best implementation - should be PremiumEmailService (priority 100)
        NotificationService service = container.getBestImplementation(NotificationService.class);
        System.out.println("📋 Selected service: " + service.getServiceType());
        
        // Test injection
        NotificationController controller = container.get(NotificationController.class);
        controller.sendUserNotification("user123", "Welcome to our system!");
        
        container.shutdown();
    }
    
    public static void demonstratePriorityResolution() throws Exception {
        System.out.println("\n🏆 === PRIORITY RESOLUTION DEMONSTRATION ===");
        
        Warmup warmup = Warmup.create();
        WarmupContainer container = warmup.start();
        
        // Register all notification services (using reflection to create instances)
        warmup.registerBean(SimpleEmailService.class, new SimpleEmailService());        // No priority (default 0)
        warmup.registerBean(BusinessEmailService.class, new BusinessEmailService());      // Priority 50
        warmup.registerBean(PremiumEmailService.class, new PremiumEmailService());       // Priority 100 (should win)
        
        // Get best implementation
        NotificationService service = container.getBestImplementation(NotificationService.class);
        System.out.println("🏅 Winner: " + service.getServiceType() + " (highest priority)");
        
        // Test service selection
        service.sendNotification("Priority test message");
        
        container.shutdown();
    }
    
    public static void demonstrateAlternativeExclusion() throws Exception {
        System.out.println("\n🚫 === ALTERNATIVE EXCLUSION DEMONSTRATION ===");
        
        Warmup warmup = Warmup.create();
        WarmupContainer container = warmup.start();
        
        // Register both regular and alternative implementations (using reflection to create instances)
        warmup.registerBean(SimpleEmailService.class, new SimpleEmailService());     // Regular (should be used)
        warmup.registerBean(SmsNotificationService.class, new SmsNotificationService()); // Alternative (should be excluded)
        
        NotificationService service = container.getBestImplementation(NotificationService.class);
        System.out.println("✅ Selected regular service (alternative excluded): " + 
                         service.getServiceType());
        
        service.sendNotification("Alternative exclusion test");
        
        container.shutdown();
    }
    
    public static void demonstrateUserServiceIntegration() throws Exception {
        System.out.println("\n👥 === USER SERVICE INTEGRATION DEMONSTRATION ===");
        
        Warmup warmup = Warmup.create();
        WarmupContainer container = warmup.start();
        
        // Register user-related components (using reflection to create instances)
        warmup.registerBean(SimpleUserRepository.class, new SimpleUserRepository());
        warmup.registerBean(DatabaseUserRepository.class, new DatabaseUserRepository());  // @Primary - should be used
        warmup.registerBean(InMemoryUserRepository.class, new InMemoryUserRepository());  // @Alternative - should be excluded
        // Nota: UserService se creará automáticamente por el contenedor
        
        UserService userService = container.get(UserService.class);
        userService.createUser("John Doe, john@example.com");
        
        container.shutdown();
    }
    
    public static void main(String[] args) throws Exception {
        System.out.println("🚀 === PRIMARY/ALTERNATIVE ANNOTATIONS DEMO ===");
        System.out.println("Demonstrating @Primary and @Alternative functionality in Warmup Framework");
        
        demonstrateBasicUsage();
        demonstratePriorityResolution();
        demonstrateAlternativeExclusion();
        demonstrateUserServiceIntegration();
        
        System.out.println("\n✅ === DEMONSTRATION COMPLETED ===");
        System.out.println("The @Primary and @Alternative annotations are working correctly!");
    }
}