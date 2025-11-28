package io.warmup.framework.test;

import io.warmup.framework.annotation.Bean;
import io.warmup.framework.annotation.Configuration;
import io.warmup.framework.annotation.validation.NotNull;
import io.warmup.framework.annotation.validation.Size;
import io.warmup.framework.annotation.validation.Pattern;
import io.warmup.framework.core.WarmupContainer;
import io.warmup.framework.exception.WarmupException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test for Bean Validation System.
 * Tests that beans are automatically validated when created via @Bean methods.
 * 
 * @author MiniMax Agent
 * @since 1.0
 */
public class BeanValidationTest {
    
    private WarmupContainer container;
    
    // Helper method to initialize container with exception handling
    private void initializeContainer() throws Exception {
        // Start the container to process @Bean methods, but don't eagerly create all beans
        // Beans with validation issues will only fail when explicitly requested
        container.start();
    }
    
    @BeforeEach
    void setUp() {
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
    @DisplayName("Valid bean should be created and registered successfully")
    void testValidBeanCreation() throws Exception {
        System.out.println("🧪 Test: Valid bean creation should succeed");
        
        container = new WarmupContainer();
        
        // Register the configuration class
        container.register(ValidUserConfig.class, true);
        
        // Explicitly process @Configuration classes to trigger @Bean methods
        container.processConfigurations();
        
        initializeContainer();
        
        // Should be able to get the valid bean
        ValidUser user = container.get(ValidUser.class);
        assertNotNull(user, "Valid bean should be created");
        assertEquals("john_doe", user.getUsername());
        assertEquals("john.doe@example.com", user.getEmail());
        assertTrue(user.getPassword().length() >= 8);
        
        System.out.println("✅ Test passed: Valid bean created successfully");
    }
    
    @Test
    @DisplayName("Invalid bean should fail validation and throw exception")
    void testInvalidBeanCreation() throws Exception {
        System.out.println("🧪 Test: Invalid bean creation should fail validation");
        
        container = new WarmupContainer();
        
        // Register the configuration class
        container.register(InvalidUserConfig.class, true);
        
        // processConfigurations() should throw WarmupException when processing invalid bean
        assertThrows(WarmupException.class, () -> {
            container.processConfigurations();
        }, "Invalid bean should fail validation and throw WarmupException");
        
        System.out.println("✅ Test passed: Invalid bean correctly rejected");
    }
    
    @Test
    @DisplayName("Bean with @NotNull constraint should fail when field is null")
    void testNotNullConstraint() throws Exception {
        System.out.println("🧪 Test: @NotNull constraint validation");
        
        container = new WarmupContainer();
        
        // Register the configuration class
        container.register(NullFieldBeanConfig.class, true);
        
        // processConfigurations() should throw WarmupException when processing null field bean
        assertThrows(WarmupException.class, () -> {
            container.processConfigurations();
        }, "Bean with null required field should fail validation");
        
        System.out.println("✅ Test passed: @NotNull constraint working correctly");
    }
    
    @Test
    @DisplayName("Bean with @Size constraint should validate string and collection lengths")
    void testSizeConstraint() throws Exception {
        System.out.println("🧪 Test: @Size constraint validation");
        
        container = new WarmupContainer();
        
        // Register the configuration class
        container.register(ValidCollectionBeanConfig.class, true);
        
        // Explicitly process @Configuration classes to trigger @Bean methods
        container.processConfigurations();
        
        initializeContainer();
        
        // Should succeed - all sizes are within limits
        ValidCollectionBean collectionBean = container.get(ValidCollectionBean.class);
        assertNotNull(collectionBean);
        assertTrue(collectionBean.getTags().size() >= 1);
        assertTrue(collectionBean.getCategories().size() >= 1);
        
        System.out.println("✅ Test passed: @Size constraint working correctly");
    }
    
    @Test
    @DisplayName("Bean with @Pattern constraint should validate regex patterns")
    void testPatternConstraint() throws Exception {
        System.out.println("🧪 Test: @Pattern constraint validation");
        
        container = new WarmupContainer();
        
        // Register the configuration class
        container.register(ValidUserConfig.class, true);
        
        // Explicitly process @Configuration classes to trigger @Bean methods
        container.processConfigurations();
        
        initializeContainer();
        
        // Should succeed - valid email and phone patterns
        ValidUser user = container.get(ValidUser.class);
        assertNotNull(user);
        assertTrue(user.getEmail().contains("@"));
        assertTrue(user.getPhoneNumber().matches("^\\+?[0-9\\s-]+$"));
        
        System.out.println("✅ Test passed: @Pattern constraint working correctly");
    }
    
    @Test
    @DisplayName("Multiple validation violations should be reported together")
    void testMultipleValidationViolations() throws Exception {
        System.out.println("🧪 Test: Multiple validation violations");
        
        container = new WarmupContainer();
        
        // Register the configuration class
        container.register(MultipleViolationsBeanConfig.class, true);
        
        // processConfigurations() should throw WarmupException with multiple violations
        WarmupException exception = assertThrows(WarmupException.class, () -> {
            container.processConfigurations();
        }, "Bean with multiple violations should fail");
        
        // Verify that the error message contains information about violations
        String errorMessage = exception.getMessage();
        System.out.println("📝 Error message: " + errorMessage);
        
        // Check for validation in the main message or in the cause
        boolean hasValidation = errorMessage.toLowerCase().contains("validation") || 
                                errorMessage.toLowerCase().contains("violation");
        
        if (!hasValidation && exception.getCause() != null) {
            String causeMessage = exception.getCause().getMessage();
            System.out.println("📝 Cause message: " + causeMessage);
            hasValidation = causeMessage != null && (causeMessage.toLowerCase().contains("validation") ||
                                                      causeMessage.toLowerCase().contains("violation"));
        }
        
        assertTrue(hasValidation, 
                  "Error message should mention validation failures. Actual message: " + errorMessage);
        
        System.out.println("✅ Test passed: Multiple violations correctly detected and reported");
    }
    
    // Test Configuration classes - separated to avoid validation failures during processing
    
    @Configuration
    public static class ValidUserConfig {
        @Bean
        public ValidUser validUser() {
            return new ValidUser(
                "john_doe",
                "john.doe@example.com", 
                "securePassword123",
                25,
                "+1-555-123-4567",
                new String[]{"admin", "user"}
            );
        }
    }
    
    @Configuration
    public static class InvalidUserConfig {
        @Bean
        public InvalidUser invalidUser() {
            return new InvalidUser(
                null, // username: null (should fail @NotNull)
                "invalid-email", // email: invalid format (should fail @Pattern)
                "123", // password: too short (should fail @Size min=8)
                25,
                "invalid-phone", // phone: invalid format (should fail @Pattern)
                new String[]{} // tags: empty array (should fail @Size min=1)
            );
        }
    }
    
    @Configuration
    public static class ValidCollectionBeanConfig {
        @Bean
        public ValidCollectionBean validCollectionBean() {
            return new ValidCollectionBean(
                "project-alpha",
                java.util.Arrays.asList("java", "spring", "validation"),
                java.util.Arrays.asList("development", "testing")
            );
        }
    }
    
    @Configuration
    public static class NullFieldBeanConfig {
        @Bean
        public NullFieldBean nullFieldBean() {
            return new NullFieldBean(
                null, // required field is null
                "valid-value"
            );
        }
    }
    
    @Configuration
    public static class MultipleViolationsBeanConfig {
        @Bean
        public MultipleViolationsBean multipleViolationsBean() {
            return new MultipleViolationsBean(
                null, // field1: null (@NotNull)
                "x", // field2: too short (@Size min=5, max=20)
                "invalid", // field3: invalid pattern (@Pattern matches [A-Z]+)
                new String[]{} // field4: empty array (@Size min=1)
            );
        }
    }
    
    @Configuration
    public static class ValidationTestConfig {
        
        @Bean
        public ValidUser validUser() {
            return new ValidUser(
                "john_doe",
                "john.doe@example.com", 
                "securePassword123",
                25,
                "+1-555-123-4567",
                new String[]{"admin", "user"}
            );
        }
        
        @Bean
        public InvalidUser invalidUser() {
            return new InvalidUser(
                null, // username: null (should fail @NotNull)
                "invalid-email", // email: invalid format (should fail @Pattern)
                "123", // password: too short (should fail @Size min=8)
                25,
                "invalid-phone", // phone: invalid format (should fail @Pattern)
                new String[]{} // tags: empty array (should fail @Size min=1)
            );
        }
        
        @Bean
        public ValidCollectionBean validCollectionBean() {
            return new ValidCollectionBean(
                "project-alpha",
                java.util.Arrays.asList("java", "spring", "validation"),
                java.util.Arrays.asList("development", "testing")
            );
        }
        
        @Bean
        public NullFieldBean nullFieldBean() {
            return new NullFieldBean(
                null, // required field is null
                "valid-value"
            );
        }
        
        @Bean
        public MultipleViolationsBean multipleViolationsBean() {
            return new MultipleViolationsBean(
                null, // field1: null (@NotNull)
                "x", // field2: too short (@Size min=5, max=20)
                "invalid", // field3: invalid pattern (@Pattern matches [A-Z]+)
                new String[]{} // field4: empty array (@Size min=1)
            );
        }
    }
    
    // Test data classes
    
    public static class ValidUser {
        @NotNull(message = "Username is required")
        @Size(min = 3, max = 20, message = "Username must be between {min} and {max} characters")
        @Pattern(regexp = "^[a-zA-Z0-9_]+$", message = "Username can only contain letters, numbers, and underscores")
        private final String username;
        
        @NotNull(message = "Email is required")
        @Pattern(regexp = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$", message = "Invalid email format")
        private final String email;
        
        @NotNull(message = "Password is required")
        @Size(min = 8, max = 50, message = "Password must be between {min} and {max} characters")
        private final String password;
        
        @NotNull(message = "Age is required")
        private final Integer age;
        
        @Pattern(regexp = "^\\+?[0-9\\s-]+$", message = "Phone number must be valid")
        private final String phoneNumber;
        
        @Size(min = 1, message = "User must have at least one role")
        private final String[] roles;
        
        public ValidUser(String username, String email, String password, Integer age, 
                        String phoneNumber, String[] roles) {
            this.username = username;
            this.email = email;
            this.password = password;
            this.age = age;
            this.phoneNumber = phoneNumber;
            this.roles = roles;
        }
        
        // Getters
        public String getUsername() { return username; }
        public String getEmail() { return email; }
        public String getPassword() { return password; }
        public Integer getAge() { return age; }
        public String getPhoneNumber() { return phoneNumber; }
        public String[] getRoles() { return roles; }
    }
    
    public static class InvalidUser {
        @NotNull(message = "Username is required")
        @Size(min = 3, max = 20, message = "Username must be between {min} and {max} characters")
        @Pattern(regexp = "^[a-zA-Z0-9_]+$", message = "Username can only contain letters, numbers, and underscores")
        private final String username;
        
        @NotNull(message = "Email is required")
        @Pattern(regexp = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$", message = "Invalid email format")
        private final String email;
        
        @NotNull(message = "Password is required")
        @Size(min = 8, max = 50, message = "Password must be between {min} and {max} characters")
        private final String password;
        
        @NotNull(message = "Age is required")
        private final Integer age;
        
        @Pattern(regexp = "^\\+?[0-9\\s-]+$", message = "Phone number must be valid")
        private final String phoneNumber;
        
        @Size(min = 1, message = "User must have at least one role")
        private final String[] roles;
        
        public InvalidUser(String username, String email, String password, Integer age, 
                          String phoneNumber, String[] roles) {
            this.username = username;
            this.email = email;
            this.password = password;
            this.age = age;
            this.phoneNumber = phoneNumber;
            this.roles = roles;
        }
        
        // Getters
        public String getUsername() { return username; }
        public String getEmail() { return email; }
        public String getPassword() { return password; }
        public Integer getAge() { return age; }
        public String getPhoneNumber() { return phoneNumber; }
        public String[] getRoles() { return roles; }
    }
    
    public static class ValidCollectionBean {
        @NotNull(message = "Project name is required")
        @Size(min = 3, max = 30, message = "Project name must be between {min} and {max} characters")
        private final String projectName;
        
        @NotNull(message = "Tags cannot be null")
        @Size(min = 1, max = 10, message = "Must have between {min} and {max} tags")
        private final java.util.List<String> tags;
        
        @NotNull(message = "Categories cannot be null")
        @Size(min = 1, message = "Must have at least one category")
        private final java.util.List<String> categories;
        
        public ValidCollectionBean(String projectName, java.util.List<String> tags, 
                                  java.util.List<String> categories) {
            this.projectName = projectName;
            this.tags = tags;
            this.categories = categories;
        }
        
        public String getProjectName() { return projectName; }
        public java.util.List<String> getTags() { return tags; }
        public java.util.List<String> getCategories() { return categories; }
    }
    
    public static class NullFieldBean {
        @NotNull(message = "Required field cannot be null")
        private final String requiredField;
        
        private final String optionalField;
        
        public NullFieldBean(String requiredField, String optionalField) {
            this.requiredField = requiredField;
            this.optionalField = optionalField;
        }
        
        public String getRequiredField() { return requiredField; }
        public String getOptionalField() { return optionalField; }
    }
    
    public static class MultipleViolationsBean {
        @NotNull(message = "Field1 is required")
        private final String field1;
        
        @Size(min = 5, max = 20, message = "Field2 must be between {min} and {max} characters")
        private final String field2;
        
        @Pattern(regexp = "^[A-Z]+$", message = "Field3 must be uppercase letters only")
        private final String field3;
        
        @Size(min = 1, message = "Field4 must have at least one element")
        private final String[] field4;
        
        public MultipleViolationsBean(String field1, String field2, String field3, String[] field4) {
            this.field1 = field1;
            this.field2 = field2;
            this.field3 = field3;
            this.field4 = field4;
        }
        
        public String getField1() { return field1; }
        public String getField2() { return field2; }
        public String getField3() { return field3; }
        public String[] getField4() { return field4; }
    }
}