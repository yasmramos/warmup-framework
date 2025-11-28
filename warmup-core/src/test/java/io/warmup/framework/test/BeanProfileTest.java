package io.warmup.framework.test;

import io.warmup.framework.annotation.Bean;
import io.warmup.framework.annotation.Configuration;
import io.warmup.framework.annotation.Profile;
import io.warmup.framework.core.WarmupContainer;
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
    
    private WarmupContainer container;
    
    // Helper method to initialize container with exception handling
    private void initializeContainer() {
        try {
            container.initializeAllComponents();
        } catch (Exception e) {
            throw new RuntimeException("Error initializing container", e);
        }
    }
    
    @BeforeEach
    void setUp() {
        // Clean up any existing container
        container = null;
    }
    
    @AfterEach
    void tearDown() {
        if (container != null) {
            try {
                container.shutdown();
            } catch (Exception e) {
                Thread.currentThread().interrupt();
                System.err.println("Error during container shutdown: " + e.getMessage());
            }
        }
    }
    
    @Test
    void testBeanWithDefaultProfile_ShouldAlwaysRegister() throws Exception {
        System.out.println("🧪 Test: Bean with @Profile(\"default\") should register in default container");
        
        // Create container with default profile
        container = new WarmupContainer(null, "default");
        
        // Register the configuration class
        TestConfig config = new TestConfig();
        container.register(TestConfig.class, true);
        initializeContainer();
        
        // Should be able to get the bean
        SimpleService bean = container.get(SimpleService.class);
        assertNotNull(bean, "Bean with @Profile(\"default\") should register in default container");
        
        System.out.println("✅ Test passed: Bean with default profile registered successfully");
    }
    
    @Test
    void testBeanWithMatchingProfile_ShouldRegister() throws Exception {
        System.out.println("🧪 Test: Bean with matching @Profile should be registered");
        
        // Create container with dev profile
        container = new WarmupContainer(null, "dev");
        
        // Register the configuration class
        TestConfig config = new TestConfig();
        container.register(TestConfig.class, true);
        initializeContainer();
        
        // Should be able to get the bean
        SimpleService bean = container.get(SimpleService.class);
        assertNotNull(bean, "Bean with matching @Profile should be registered");
        
        System.out.println("✅ Test passed: Bean with matching @Profile registered successfully");
    }
    
    @Test
    void testBeanWithNonMatchingProfile_ShouldNotRegister() throws Exception {
        System.out.println("🧪 Test: Bean with non-matching @Profile should NOT be registered");
        
        // Create container with staging profile (doesn't match any @Profile)
        container = new WarmupContainer(null, "staging");
        
        // Register the configuration class
        TestConfig config = new TestConfig();
        container.register(TestConfig.class, true);
        initializeContainer();
        
        // Should NOT be able to get the bean (should throw exception)
        assertThrows(Exception.class, () -> {
            container.get(SimpleService.class);
        }, "Bean with non-matching @Profile should not be registered");
        
        System.out.println("✅ Test passed: Bean with non-matching @Profile not registered");
    }
    
    @Test
    void testBeanWithMultipleProfiles_AnyMatch_ShouldRegister() throws Exception {
        System.out.println("🧪 Test: Bean with multiple @Profile values should register if any matches");
        
        // Create container with test profile
        container = new WarmupContainer(null, "test");
        
        // Register the configuration class
        TestConfig config = new TestConfig();
        try {
            container.register(TestConfig.class, true);
        } catch (Exception e) {
            throw new RuntimeException("Error registering TestConfig", e);
        }
        initializeContainer();
        
        // Should be able to get the bean
        SimpleService bean;
        try {
            bean = container.get(SimpleService.class);
        } catch (Exception e) {
            throw new RuntimeException("Error getting SimpleService bean", e);
        }
        assertNotNull(bean, "Bean with multiple @Profile should register if any profile matches");
        
        System.out.println("✅ Test passed: Bean with multiple profiles registered (test matched)");
    }
    
    @Test
    void testBeanWithMultipleProfiles_NoneMatch_ShouldNotRegister() throws Exception {
        System.out.println("🧪 Test: Bean with multiple @Profile values should NOT register if none match");
        
        // Create container with prod profile (not matching dev or test)
        container = new WarmupContainer(null, "prod");
        
        // Register the configuration class
        TestConfig config = new TestConfig();
        try {
            container.register(TestConfig.class, true);
        } catch (Exception e) {
            throw new RuntimeException("Error registering TestConfig", e);
        }
        initializeContainer();
        
        // Should NOT be able to get the bean (should throw exception)
        assertThrows(Exception.class, () -> {
            try {
                container.get(SimpleService.class);
            } catch (Exception e) {
                throw new RuntimeException("Bean retrieval failed as expected", e);
            }
        }, "Bean with multiple @Profile should not register if no profile matches");
        
        System.out.println("✅ Test passed: Bean with multiple profiles not registered (no match)");
    }
    
    @Test
    void testBeanWithDefaultProfile() throws Exception {
        System.out.println("🧪 Test: Bean with @Profile(\"default\") should register in default container");
        
        // Create container with default profile
        container = new WarmupContainer(null, "default");
        
        // Register the configuration class
        TestConfig config = new TestConfig();
        try {
            container.register(TestConfig.class, true);
        } catch (Exception e) {
            throw new RuntimeException("Error registering TestConfig", e);
        }
        initializeContainer();
        
        // Should be able to get the bean
        SimpleService bean;
        try {
            bean = container.get(SimpleService.class);
        } catch (Exception e) {
            throw new RuntimeException("Error getting SimpleService bean", e);
        }
        assertNotNull(bean, "Bean with @Profile(\"default\") should register in default container");
        
        System.out.println("✅ Test passed: Bean with default profile registered in default container");
    }
    
    @Test
    void testBeanWithDefaultProfile_InNonDefaultContainer() throws Exception {
        System.out.println("🧪 Test: Bean with @Profile(\"default\") should NOT register in non-default container");
        
        // Create container with dev profile
        container = new WarmupContainer(null, "dev");
        
        // Register the configuration class
        TestConfig config = new TestConfig();
        try {
            container.register(TestConfig.class, true);
        } catch (Exception e) {
            throw new RuntimeException("Error registering TestConfig", e);
        }
        initializeContainer();
        
        // When using a container with "dev" profile, the beans with @Profile("default") 
        // should not be registered, so calling get(SimpleService.class) should find
        // the beans that DO match (devService and multiProfileService), but we want to verify
        // that the @Profile("default") beans are NOT registered.
        // 
        // The way to verify this is to create a configuration where ONLY @Profile("default") 
        // beans exist, so if any bean is found, it means @Profile("default") is being incorrectly registered.
        
        // Create a container with a different configuration that has ONLY @Profile("default") beans
        WarmupContainer defaultOnlyContainer = new WarmupContainer(null, "dev");
        DefaultOnlyConfig defaultOnlyConfig = new DefaultOnlyConfig();
        defaultOnlyContainer.register(DefaultOnlyConfig.class, true);
        defaultOnlyContainer.initializeAllComponents();
        
        // Should NOT be able to get any beans since only @Profile("default") beans exist in DefaultOnlyConfig
        // and the container has "dev" profile
        assertThrows(Exception.class, () -> {
            try {
                defaultOnlyContainer.get(SimpleService.class);
            } catch (Exception e) {
                throw new RuntimeException("Bean retrieval failed as expected", e);
            }
        }, "Bean with @Profile(\"default\") should not register in non-default container");
        
        defaultOnlyContainer.shutdown();
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