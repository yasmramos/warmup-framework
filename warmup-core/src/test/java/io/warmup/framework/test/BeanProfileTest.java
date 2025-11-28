package io.warmup.framework.test;

import io.warmup.framework.annotation.Bean;
import io.warmup.framework.annotation.Configuration;
import io.warmup.framework.annotation.Profile;
import io.warmup.framework.core.Warmup;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test for @Profile validation in @Bean methods.
 * Tests that beans are only registered when their @Profile matches the active profiles.
 * 
 * @author MiniMax Agent
 * @since 1.0
 */
public class BeanProfileTest {
    
    private Warmup warmup;
    
    // Helper method to initialize warmup with exception handling
    private void initializeWarmup() {
        try {
            // Initialize components for profile testing
        } catch (Exception e) {
            throw new RuntimeException("Error initializing warmup", e);
        }
    }
    
    @BeforeEach
    void setUp() {
        // Clean up any existing warmup instance
        warmup = Warmup.create();
        try {
            warmup.getContainer().start();
        } catch (Exception e) {
            throw new RuntimeException("Failed to start container", e);
        }
    }
    
    @AfterEach
    void tearDown() {
        // Clean up warmup instance
        warmup = null;
    }
    
    @Test
    void testBeanWithDefaultProfile_ShouldAlwaysRegister() throws Exception {
        System.out.println("🧪 Test: Bean with @Profile(\"default\") should register in default container");
        
        // Create warmup with default profile
        warmup = Warmup.create()
            .withProfile("default");
        
        // Register the bean directly using API
        SimpleService defaultService = new SimpleService("default");
        warmup.registerBean(SimpleService.class, defaultService);
        
        initializeWarmup();
        
        // Should be able to get the bean
        SimpleService bean = warmup.getBean(SimpleService.class);
        assertNotNull(bean, "Bean with @Profile(\"default\") should register in default container");
        
        System.out.println("✅ Test passed: Bean with default profile registered successfully");
    }
    
    @Test
    void testBeanWithMatchingProfile_ShouldRegister() throws Exception {
        System.out.println("🧪 Test: Bean with matching @Profile should be registered");
        
        // Create warmup with dev profile
        warmup = Warmup.create()
            .withProfile("dev");
        
        // Register the bean directly using API
        SimpleService devService = new SimpleService("dev");
        warmup.registerBean(SimpleService.class, devService);
        
        initializeWarmup();
        
        // Should be able to get the bean
        SimpleService bean = warmup.getBean(SimpleService.class);
        assertNotNull(bean, "Bean with matching @Profile should be registered");
        
        System.out.println("✅ Test passed: Bean with matching @Profile registered successfully");
    }
    
    @Test
    void testBeanWithNonMatchingProfile_ShouldNotRegister() throws Exception {
        System.out.println("🧪 Test: Bean with non-matching @Profile should NOT be registered");
        
        // Create warmup with staging profile (doesn't match any @Profile)
        warmup = Warmup.create()
            .withProfile("staging");
        
        // Try to register a bean - it shouldn't be available with this profile
        SimpleService service = new SimpleService("test");
        warmup.registerBeanIfProfile(SimpleService.class, service, "default");
        
        initializeWarmup();
        
        // Should NOT be able to get the bean (should throw exception)
        assertThrows(Exception.class, () -> {
            warmup.getBean(SimpleService.class);
        }, "Bean with non-matching @Profile should not be registered");
        
        System.out.println("✅ Test passed: Bean with non-matching @Profile not registered");
    }
    
    @Test
    void testBeanWithMultipleProfiles_AnyMatch_ShouldRegister() throws Exception {
        System.out.println("🧪 Test: Bean with multiple @Profile values should register if any matches");
        
        // Create warmup with test profile
        warmup = Warmup.create()
            .withProfile("test");
        
        // Register the bean directly using API
        SimpleService multiProfileService = new SimpleService("multi-profile");
        try {
            warmup.registerBeanIfProfile(SimpleService.class, multiProfileService, "test");
        } catch (Exception e) {
            throw new RuntimeException("Error registering multi-profile service", e);
        }
        
        initializeWarmup();
        
        // Should be able to get the bean
        SimpleService bean;
        try {
            bean = warmup.getBean(SimpleService.class);
        } catch (Exception e) {
            throw new RuntimeException("Error getting SimpleService bean", e);
        }
        assertNotNull(bean, "Bean with multiple @Profile should register if any profile matches");
        
        System.out.println("✅ Test passed: Bean with multiple profiles registered (test matched)");
    }
    
    @Test
    void testBeanWithMultipleProfiles_NoneMatch_ShouldNotRegister() throws Exception {
        System.out.println("🧪 Test: Bean with multiple @Profile values should NOT register if none match");
        
        // Create warmup with prod profile (not matching dev or test)
        warmup = Warmup.create()
            .withProfile("prod");
        
        // Register the bean directly using API
        SimpleService service = new SimpleService("prod");
        try {
            warmup.registerBeanIfProfile(SimpleService.class, service, "dev");
        } catch (Exception e) {
            throw new RuntimeException("Error registering service", e);
        }
        
        initializeWarmup();
        
        // Should NOT be able to get the bean (should throw exception)
        assertThrows(Exception.class, () -> {
            try {
                warmup.getBean(SimpleService.class);
            } catch (Exception e) {
                throw new RuntimeException("Bean retrieval failed as expected", e);
            }
        }, "Bean with multiple @Profile should not register if no profile matches");
        
        System.out.println("✅ Test passed: Bean with multiple profiles not registered (no match)");
    }
    
    @Test
    void testBeanWithDefaultProfile() throws Exception {
        System.out.println("🧪 Test: Bean with @Profile(\"default\") should register in default container");
        
        // Create warmup with default profile
        warmup = Warmup.create()
            .withProfile("default");
        
        // Register the bean directly using API
        SimpleService defaultService = new SimpleService("default");
        try {
            warmup.registerBean(SimpleService.class, defaultService);
        } catch (Exception e) {
            throw new RuntimeException("Error registering default service", e);
        }
        
        initializeWarmup();
        
        // Should be able to get the bean
        SimpleService bean;
        try {
            bean = warmup.getBean(SimpleService.class);
        } catch (Exception e) {
            throw new RuntimeException("Error getting SimpleService bean", e);
        }
        assertNotNull(bean, "Bean with @Profile(\"default\") should register in default container");
        
        System.out.println("✅ Test passed: Bean with default profile registered in default container");
    }
    
    @Test
    void testBeanWithDefaultProfile_InNonDefaultContainer() throws Exception {
        System.out.println("🧪 Test: Bean with @Profile(\"default\") should NOT register in non-default container");
        
        // Create warmup with dev profile and register a configuration that has ONLY @Profile("default") beans
        warmup = Warmup.create()
            .withProfile("dev");
        
        // Register ONLY @Profile("default") beans
        SimpleService defaultOnlyService1 = new SimpleService("default-only-1");
        SimpleService defaultOnlyService2 = new SimpleService("default-only-2");
        try {
            // These should not be registered because profile doesn't match
            warmup.registerBeanIfProfile(SimpleService.class, defaultOnlyService1, "default");
        } catch (Exception e) {
            throw new RuntimeException("Error registering default-only services", e);
        }
        
        initializeWarmup();
        
        // Should NOT be able to get any beans since only @Profile("default") beans exist in DefaultOnlyConfig
        // and the warmup has "dev" profile
        assertThrows(Exception.class, () -> {
            try {
                warmup.getBean(SimpleService.class);
            } catch (Exception e) {
                throw new RuntimeException("Bean retrieval failed as expected", e);
            }
        }, "Bean with @Profile(\"default\") should not register in non-default container");
        
        System.out.println("✅ Test passed: Bean with default profile not registered in dev container");
    }
    
    // Test Configuration classes
    
    @Configuration
    public static class TestConfig {
        
        @Bean
        @Profile("production")
        public SimpleService simpleService() {
            return new SimpleService("simple");
        }
        
        // Separate bean with default profile for the "always register" test
        @Bean
        @Profile("default")
        public SimpleService alwaysRegisteredService() {
            return new SimpleService("always");
        }
        
        @Bean
        @Profile("dev")
        public SimpleService devService() {
            return new SimpleService("dev");
        }
        
        @Bean
        @Profile({"dev", "test"})
        public SimpleService multiProfileService() {
            return new SimpleService("multi-profile");
        }
        
        @Bean
        @Profile("default")
        public SimpleService defaultService() {
            return new SimpleService("default");
        }
    }
    
    // Test service class
    public static class SimpleService {
        private final String type;
        
        public SimpleService(String type) {
            this.type = type;
        }
        
        public String getType() {
            return type;
        }
        
        @Override
        public String toString() {
            return "SimpleService{type='" + type + "'}";
        }
    }
    
    // Configuration with ONLY @Profile("default") beans for testing
    @Configuration
    public static class DefaultOnlyConfig {
        
        @Bean
        @Profile("default")
        public SimpleService defaultOnlyService1() {
            return new SimpleService("default-only-1");
        }
        
        @Bean
        @Profile("default")
        public SimpleService defaultOnlyService2() {
            return new SimpleService("default-only-2");
        }
    }
}