package io.warmup.examples.configuration;

import io.warmup.framework.annotation.*;
import io.warmup.framework.core.Warmup;

/**
 * Simple practical example showing @Configuration and @Bean usage.
 * This demonstrates the basic functionality in a clean, understandable way.
 * 
 * @author MiniMax Agent
 * @since 1.0
 */
public class SimpleConfigurationExample {
    
    public static void main(String[] args) {
        System.out.println("=== Warmup Framework @Configuration/@Bean Example ===");
        
        try {
            // Create warmup instance using public API
            Warmup warmup = Warmup.create();
            
            // Register configuration classes as beans
            System.out.println("Registering configuration classes...");
            warmup.registerBean(AppConfig.class, new AppConfig());
            warmup.registerBean(DatabaseConfig.class, new DatabaseConfig());
            warmup.registerBean(ServiceConfig.class, new ServiceConfig());
            
            // Get beans
            System.out.println("\n=== Testing Bean Resolution ===");
            
            // Test basic @Bean
            GreetingService greetingService = warmup.getBean(GreetingService.class);
            System.out.println("Greeting Service: " + greetingService.getGreeting("World"));
            
            // Test @Bean with dependencies
            UserService userService = warmup.getBean(UserService.class);
            userService.createUser("John Doe");
            
            // Test @Bean with custom name
            MessageSender emailSender = warmup.getBean("emailSender", MessageSender.class);
            System.out.println("Email Sender: " + emailSender.getType());
            
            MessageSender smsSender = warmup.getBean("smsSender", MessageSender.class);
            System.out.println("SMS Sender: " + smsSender.getType());
            
            // Test bean resolution
            DataSource dataSource = warmup.getBean(DataSource.class);
            System.out.println("DataSource Type: " + dataSource.getClass().getSimpleName());
            
            System.out.println("\n=== All tests completed successfully! ===");
            
        } catch (Exception e) {
            System.err.println("Error during execution: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    // ================ CONFIGURATION CLASSES ================
    
    @Configuration
    public static class AppConfig {
        
        @Bean
        public GreetingService greetingService() {
            return new EnglishGreetingService();
        }
        
        @Bean(name = "emailSender")
        public MessageSender emailSender() {
            return new EmailMessageSender();
        }
        
        @Bean(name = "smsSender")
        public MessageSender smsSender() {
            return new SmsMessageSender();
        }
    }
    
    @Configuration
    public static class DatabaseConfig {
        
        @Bean
        @Primary
        public DataSource dataSource() {
            return new H2Database();
        }
        
        @Bean
        public UserRepository userRepository(DataSource dataSource) {
            return new UserRepositoryImpl(dataSource);
        }
    }
    
    @Configuration
    public static class ServiceConfig {
        
        @Bean
        @Singleton
        public UserService userService(UserRepository userRepository, MessageSender emailSender) {
            return new UserServiceImpl(userRepository, emailSender);
        }
    }
    
    // ================ SERVICE INTERFACES AND IMPLEMENTATIONS ================
    
    public interface GreetingService {
        String getGreeting(String name);
    }
    
    public static class EnglishGreetingService implements GreetingService {
        @Override
        public String getGreeting(String name) {
            return "Hello, " + name + "!";
        }
    }
    
    public interface MessageSender {
        void send(String message);
        String getType();
    }
    
    public static class EmailMessageSender implements MessageSender {
        @Override
        public void send(String message) {
            System.out.println("📧 Sending email: " + message);
        }
        
        @Override
        public String getType() {
            return "Email";
        }
    }
    
    public static class SmsMessageSender implements MessageSender {
        @Override
        public void send(String message) {
            System.out.println("📱 Sending SMS: " + message);
        }
        
        @Override
        public String getType() {
            return "SMS";
        }
    }
    
    public interface DataSource {
        Object getConnection();
    }
    
    public static class H2Database implements DataSource {
        @Override
        public Object getConnection() {
            return "H2 Database Connection";
        }
    }
    
    public interface UserRepository {
        void saveUser(String userName);
    }
    
    public static class UserRepositoryImpl implements UserRepository {
        private final DataSource dataSource;
        
        public UserRepositoryImpl(DataSource dataSource) {
            this.dataSource = dataSource;
        }
        
        @Override
        public void saveUser(String userName) {
            System.out.println("💾 Saving user to database: " + userName);
        }
    }
    
    public interface UserService {
        void createUser(String userName);
    }
    
    public static class UserServiceImpl implements UserService {
        private final UserRepository userRepository;
        private final MessageSender messageSender;
        
        public UserServiceImpl(UserRepository userRepository, MessageSender messageSender) {
            this.userRepository = userRepository;
            this.messageSender = messageSender;
        }
        
        @Override
        public void createUser(String userName) {
            System.out.println("👤 Creating user: " + userName);
            
            // Save to database
            userRepository.saveUser(userName);
            
            // Send notification
            messageSender.send("Welcome " + userName + "!");
        }
    }
}