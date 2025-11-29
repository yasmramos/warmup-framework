package io.warmup.examples.profile;

import io.warmup.framework.annotation.Bean;
import io.warmup.framework.annotation.Configuration;
import io.warmup.framework.annotation.Profile;
import io.warmup.framework.core.WarmupContainer;

import java.util.logging.Logger;

/**
 * Example demonstrating @Profile validation in @Bean methods.
 * Shows how beans are conditionally registered based on active profiles.
 * 
 * @author MiniMax Agent
 * @since 1.0
 */
public class ProfileBeanExample {
    
    private static final Logger logger = Logger.getLogger(ProfileBeanExample.class.getName());
    
    public static void main(String[] args) {
        System.out.println("🚀 Starting @Profile @Bean validation example");
        
        // Test 1: Container with "dev" profile
        System.out.println("\n📋 Test 1: Container with 'dev' profile");
        testDevProfile();
        
        // Test 2: Container with "prod" profile  
        System.out.println("\n📋 Test 2: Container with 'prod' profile");
        testProdProfile();
        
        // Test 3: Container with "test" profile
        System.out.println("\n📋 Test 3: Container with 'test' profile");
        testTestProfile();
        
        // Test 4: Container with default profile
        System.out.println("\n📋 Test 4: Container with 'default' profile");
        testDefaultProfile();
        
        System.out.println("\n✅ All profile validation tests completed!");
    }
    
    private static void testDevProfile() {
        WarmupContainer container = new WarmupContainer(null, "dev");
        
        // Should be available in dev profile
        Object devDb = getBeanSafely(container, "devDatabaseService");
        Object multiProfile = getBeanSafely(container, "multiProfileService");
        
        // Should NOT be available in dev profile
        Object prodDb = getBeanSafely(container, "productionDatabaseService");
        Object defaultDb = getBeanSafely(container, "defaultDatabaseService");
        Object testDb = getBeanSafely(container, "testDatabaseService");
        
        // Should always be available (no @Profile)
        Object commonService = getBeanSafely(container, "commonService");
        
        System.out.println("  ✅ Dev DB Service: " + (devDb != null ? "AVAILABLE" : "NOT AVAILABLE"));
        System.out.println("  ✅ Multi-Profile Service: " + (multiProfile != null ? "AVAILABLE (dev matches)" : "NOT AVAILABLE"));
        System.out.println("  ❌ Prod DB Service: " + (prodDb != null ? "AVAILABLE (should be NOT)" : "NOT AVAILABLE"));
        System.out.println("  ❌ Default DB Service: " + (defaultDb != null ? "AVAILABLE (should be NOT)" : "NOT AVAILABLE"));
        System.out.println("  ❌ Test DB Service: " + (testDb != null ? "AVAILABLE (should be NOT)" : "NOT AVAILABLE"));
        System.out.println("  ✅ Common Service: " + (commonService != null ? "AVAILABLE (no @Profile)" : "NOT AVAILABLE"));
    }
    
    private static void testProdProfile() {
        WarmupContainer container = new WarmupContainer(null, "prod");
        
        // Should be available in prod profile
        Object prodDb = getBeanSafely(container, "productionDatabaseService");
        Object multiProfile = getBeanSafely(container, "multiProfileService");
        
        // Should NOT be available in prod profile
        Object devDb = getBeanSafely(container, "devDatabaseService");
        Object defaultDb = getBeanSafely(container, "defaultDatabaseService");
        Object testDb = getBeanSafely(container, "testDatabaseService");
        
        // Should always be available (no @Profile)
        Object commonService = getBeanSafely(container, "commonService");
        
        System.out.println("  ❌ Dev DB Service: " + (devDb != null ? "AVAILABLE (should be NOT)" : "NOT AVAILABLE"));
        System.out.println("  ✅ Multi-Profile Service: " + (multiProfile != null ? "AVAILABLE (test doesn't match)" : "NOT AVAILABLE (test doesn't match dev or prod)"));
        System.out.println("  ✅ Prod DB Service: " + (prodDb != null ? "AVAILABLE" : "NOT AVAILABLE"));
        System.out.println("  ❌ Default DB Service: " + (defaultDb != null ? "AVAILABLE (should be NOT)" : "NOT AVAILABLE"));
        System.out.println("  ❌ Test DB Service: " + (testDb != null ? "AVAILABLE (should be NOT)" : "NOT AVAILABLE"));
        System.out.println("  ✅ Common Service: " + (commonService != null ? "AVAILABLE (no @Profile)" : "NOT AVAILABLE"));
    }
    
    private static void testTestProfile() {
        WarmupContainer container = new WarmupContainer(null, "test");
        
        // Should be available in test profile
        Object testDb = getBeanSafely(container, "testDatabaseService");
        Object multiProfile = getBeanSafely(container, "multiProfileService");
        
        // Should NOT be available in test profile
        Object devDb = getBeanSafely(container, "devDatabaseService");
        Object prodDb = getBeanSafely(container, "productionDatabaseService");
        Object defaultDb = getBeanSafely(container, "defaultDatabaseService");
        
        // Should always be available (no @Profile)
        Object commonService = getBeanSafely(container, "commonService");
        
        System.out.println("  ❌ Dev DB Service: " + (devDb != null ? "AVAILABLE (should be NOT)" : "NOT AVAILABLE"));
        System.out.println("  ✅ Multi-Profile Service: " + (multiProfile != null ? "AVAILABLE (test matches)" : "NOT AVAILABLE"));
        System.out.println("  ❌ Prod DB Service: " + (prodDb != null ? "AVAILABLE (should be NOT)" : "NOT AVAILABLE"));
        System.out.println("  ❌ Default DB Service: " + (defaultDb != null ? "AVAILABLE (should be NOT)" : "NOT AVAILABLE"));
        System.out.println("  ✅ Test DB Service: " + (testDb != null ? "AVAILABLE" : "NOT AVAILABLE"));
        System.out.println("  ✅ Common Service: " + (commonService != null ? "AVAILABLE (no @Profile)" : "NOT AVAILABLE"));
    }
    
    private static void testDefaultProfile() {
        WarmupContainer container = new WarmupContainer(null, "default");
        
        // Should be available in default profile
        Object defaultDb = getBeanSafely(container, "defaultDatabaseService");
        
        // Should NOT be available in default profile
        Object devDb = getBeanSafely(container, "devDatabaseService");
        Object prodDb = getBeanSafely(container, "productionDatabaseService");
        Object testDb = getBeanSafely(container, "testDatabaseService");
        
        // Should always be available (no @Profile)
        Object commonService = getBeanSafely(container, "commonService");
        
        System.out.println("  ❌ Dev DB Service: " + (devDb != null ? "AVAILABLE (should be NOT)" : "NOT AVAILABLE"));
        System.out.println("  ❌ Prod DB Service: " + (prodDb != null ? "AVAILABLE (should be NOT)" : "NOT AVAILABLE"));
        System.out.println("  ❌ Test DB Service: " + (testDb != null ? "AVAILABLE (should be NOT)" : "NOT AVAILABLE"));
        System.out.println("  ✅ Default DB Service: " + (defaultDb != null ? "AVAILABLE" : "NOT AVAILABLE"));
        System.out.println("  ✅ Common Service: " + (commonService != null ? "AVAILABLE (no @Profile)" : "NOT AVAILABLE"));
    }
    
    private static Object getBeanSafely(WarmupContainer container, String beanName) {
        try {
            return container.getBean(beanName, Object.class);
        } catch (Exception e) {
            return null;
        }
    }
    
    // Configuration class with @Profile validation
    
    @Configuration
    public static class DatabaseConfig {
        
        @Bean
        public CommonService commonService() {
            return new CommonService("Available in all profiles");
        }
        
        @Bean
        @Profile("dev")
        public DatabaseService devDatabaseService() {
            return new DatabaseService("dev", "H2 Development Database");
        }
        
        @Bean
        @Profile("prod")
        public DatabaseService productionDatabaseService() {
            return new DatabaseService("prod", "PostgreSQL Production Database");
        }
        
        @Bean
        @Profile("test")
        public DatabaseService testDatabaseService() {
            return new DatabaseService("test", "In-Memory Test Database");
        }
        
        @Bean
        @Profile("default")
        public DatabaseService defaultDatabaseService() {
            return new DatabaseService("default", "Default H2 Database");
        }
        
        // Bean with multiple profiles - should be available if ANY profile matches
        @Bean
        @Profile({"dev", "test"})
        public DatabaseService multiProfileService() {
            return new DatabaseService("multi-profile", "Available in both dev and test profiles");
        }
    }
    
    // Supporting classes
    
    public static class DatabaseService {
        private final String profile;
        private final String description;
        
        public DatabaseService(String profile, String description) {
            this.profile = profile;
            this.description = description;
        }
        
        public String getProfile() {
            return profile;
        }
        
        public String getDescription() {
            return description;
        }
        
        @Override
        public String toString() {
            return "DatabaseService{profile='" + profile + "', description='" + description + "'}";
        }
    }
    
    public static class CommonService {
        private final String description;
        
        public CommonService(String description) {
            this.description = description;
        }
        
        public String getDescription() {
            return description;
        }
        
        @Override
        public String toString() {
            return "CommonService{description='" + description + "'}";
        }
    }
}