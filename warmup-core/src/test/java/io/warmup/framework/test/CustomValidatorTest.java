package io.warmup.framework.test;

import io.warmup.framework.annotation.validation.*;
import io.warmup.framework.validation.DefaultValidator;
import io.warmup.framework.validation.ViolationReport;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for custom validator functionality.
 * 
 * @author MiniMax Agent
 * @since 1.0
 */
class CustomValidatorTest {
    
    private DefaultValidator validator;
    
    @BeforeEach
    void setUp() {
        validator = new DefaultValidator();
    }
    
    /**
     * Test class with custom validation.
     */
    static class TestUser {
        @CustomConstraint(
            validator = EmailValidator.class,
            message = "Invalid email format"
        )
        private String email;
        
        @CustomConstraint(
            validator = PasswordValidator.class,
            parameters = {"8", "true", "true", "true", "true"}
        )
        private String password;
        
        @CustomConstraint(
            validator = UsernameValidator.class
        )
        private String username;
        
        @CustomConstraint(
            validator = RangeValidator.class,
            parameters = {"1", "100"}
        )
        private Integer age;
        
        public TestUser(String email, String password, String username, Integer age) {
            this.email = email;
            this.password = password;
            this.username = username;
            this.age = age;
        }
        
        // Getters and setters
        public String getEmail() { return email; }
        public String getPassword() { return password; }
        public String getUsername() { return username; }
        public Integer getAge() { return age; }
        
        public void setEmail(String email) { this.email = email; }
        public void setPassword(String password) { this.password = password; }
        public void setUsername(String username) { this.username = username; }
        public void setAge(Integer age) { this.age = age; }
    }
    
    /**
     * Email validator for testing.
     */
    static class EmailValidator implements CustomConstraintValidator {
        private static final java.util.regex.Pattern EMAIL_PATTERN = 
            java.util.regex.Pattern.compile("^[a-zA-Z0-9+_.-]+@[a-zA-Z0-9.-]+$");
        
        @Override
        public boolean isValid(Object value, Object... parameters) {
            if (value == null) return true;
            return EMAIL_PATTERN.matcher(value.toString()).matches();
        }
        
        @Override
        public String getMessage(Object invalidValue, String fieldName, Object... parameters) {
            return "Invalid email format";
        }
    }
    
    /**
     * Password validator for testing.
     */
    static class PasswordValidator implements CustomConstraintValidator {
        private int minLength = 8;
        
        @Override
        public void initialize(Object... parameters) {
            if (parameters.length > 0 && parameters[0] instanceof String) {
                minLength = Integer.parseInt((String) parameters[0]);
            }
        }
        
        @Override
        public boolean isValid(Object value, Object... parameters) {
            if (value == null) return true;
            return value.toString().length() >= minLength;
        }
        
        @Override
        public String getMessage(Object invalidValue, String fieldName, Object... parameters) {
            return "Password too short";
        }
    }
    
    /**
     * Username validator for testing.
     */
    static class UsernameValidator implements CustomConstraintValidator {
        @Override
        public boolean isValid(Object value, Object... parameters) {
            if (value == null) return true;
            String username = value.toString();
            return username.matches("^[a-zA-Z0-9_]+$");
        }
        
        @Override
        public String getMessage(Object invalidValue, String fieldName, Object... parameters) {
            return "Invalid username format";
        }
    }
    
    /**
     * Range validator for testing.
     */
    static class RangeValidator implements CustomConstraintValidator {
        private int min = 0;
        private int max = 100;
        
        @Override
        public void initialize(Object... parameters) {
            if (parameters.length >= 1) {
                min = Integer.parseInt(parameters[0].toString());
            }
            if (parameters.length >= 2) {
                max = Integer.parseInt(parameters[1].toString());
            }
        }
        
        @Override
        public boolean isValid(Object value, Object... parameters) {
            if (value == null) return true;
            if (!(value instanceof Number)) return false;
            int numValue = ((Number) value).intValue();
            return numValue >= min && numValue <= max;
        }
        
        @Override
        public String getMessage(Object invalidValue, String fieldName, Object... parameters) {
            return String.format("Value %s must be between %d and %d", invalidValue, min, max);
        }
    }
    
    @Test
    void testValidEmail() {
        // Register custom validators
        validator.getCustomValidatorManager().registerValidatorByClass(EmailValidator.class);
        
        TestUser user = new TestUser("test@example.com", "password123", "valid_user", 25);
        
        ViolationReport<TestUser> report = validator.getViolationReport(user);
        assertTrue(report.isValid(), "Valid email should pass validation");
        
        // Check that custom validator was called
        boolean emailViolationFound = report.getViolations().stream()
            .anyMatch(v -> v.getPropertyPath().contains("email"));
        assertFalse(emailViolationFound, "No email violations should be found");
    }
    
    @Test
    void testInvalidEmail() {
        validator.getCustomValidatorManager().registerValidatorByClass(EmailValidator.class);
        
        TestUser user = new TestUser("invalid-email", "password123", "valid_user", 25);
        
        ViolationReport<TestUser> report = validator.getViolationReport(user);
        assertFalse(report.isValid(), "Invalid email should fail validation");
        
        // Check that email violation was found
        boolean emailViolationFound = report.getViolations().stream()
            .anyMatch(v -> v.getPropertyPath().contains("email"));
        assertTrue(emailViolationFound, "Email violation should be found");
    }
    
    @Test
    void testValidPassword() {
        validator.getCustomValidatorManager().registerValidatorByClass(PasswordValidator.class);
        
        TestUser user = new TestUser("test@example.com", "password123", "valid_user", 25);
        
        ViolationReport<TestUser> report = validator.getViolationReport(user);
        assertTrue(report.isValid(), "Valid password should pass validation");
    }
    
    @Test
    void testInvalidPassword() {
        validator.getCustomValidatorManager().registerValidatorByClass(PasswordValidator.class);
        
        TestUser user = new TestUser("test@example.com", "short", "valid_user", 25);
        
        ViolationReport<TestUser> report = validator.getViolationReport(user);
        assertFalse(report.isValid(), "Invalid password should fail validation");
        
        // Check that password violation was found
        boolean passwordViolationFound = report.getViolations().stream()
            .anyMatch(v -> v.getPropertyPath().contains("password"));
        assertTrue(passwordViolationFound, "Password violation should be found");
    }
    
    @Test
    void testValidUsername() {
        validator.getCustomValidatorManager().registerValidatorByClass(UsernameValidator.class);
        
        TestUser user = new TestUser("test@example.com", "password123", "valid_user_123", 25);
        
        ViolationReport<TestUser> report = validator.getViolationReport(user);
        assertTrue(report.isValid(), "Valid username should pass validation");
    }
    
    @Test
    void testInvalidUsername() {
        validator.getCustomValidatorManager().registerValidatorByClass(UsernameValidator.class);
        
        TestUser user = new TestUser("test@example.com", "password123", "invalid@user", 25);
        
        ViolationReport<TestUser> report = validator.getViolationReport(user);
        assertFalse(report.isValid(), "Invalid username should fail validation");
    }
    
    @Test
    void testValidRange() {
        validator.getCustomValidatorManager().registerValidatorByClass(RangeValidator.class);
        
        TestUser user = new TestUser("test@example.com", "password123", "valid_user", 50);
        
        ViolationReport<TestUser> report = validator.getViolationReport(user);
        assertTrue(report.isValid(), "Valid range should pass validation");
    }
    
    @Test
    void testInvalidRange() {
        validator.getCustomValidatorManager().registerValidatorByClass(RangeValidator.class);
        
        TestUser user = new TestUser("test@example.com", "password123", "valid_user", 150);
        
        ViolationReport<TestUser> report = validator.getViolationReport(user);
        assertFalse(report.isValid(), "Invalid range should fail validation");
    }
    
    @Test
    void testMultipleCustomValidators() {
        // Register all custom validators
        validator.getCustomValidatorManager().registerValidatorByClass(EmailValidator.class);
        validator.getCustomValidatorManager().registerValidatorByClass(PasswordValidator.class);
        validator.getCustomValidatorManager().registerValidatorByClass(UsernameValidator.class);
        validator.getCustomValidatorManager().registerValidatorByClass(RangeValidator.class);
        
        // Test with multiple validations
        TestUser validUser = new TestUser("test@example.com", "password123", "valid_user", 25);
        ViolationReport<TestUser> validReport = validator.getViolationReport(validUser);
        assertTrue(validReport.isValid(), "All validations should pass for valid user");
        
        TestUser invalidUser = new TestUser("invalid-email", "short", "invalid@user", 150);
        ViolationReport<TestUser> invalidReport = validator.getViolationReport(invalidUser);
        assertFalse(invalidReport.isValid(), "Multiple validations should fail for invalid user");
        
        // Should have violations for email, password, username, and age
        List<String> violationTypes = invalidReport.getViolations().stream()
            .map(v -> v.getPropertyPath())
            .collect(Collectors.toList());
        
        assertTrue(violationTypes.contains("email"), "Email violation should be found");
        assertTrue(violationTypes.contains("password"), "Password violation should be found");
        assertTrue(violationTypes.contains("username"), "Username violation should be found");
        assertTrue(violationTypes.contains("age"), "Age violation should be found");
    }
    
    @Test
    void testNullValues() {
        validator.getCustomValidatorManager().registerValidatorByClass(EmailValidator.class);
        validator.getCustomValidatorManager().registerValidatorByClass(PasswordValidator.class);
        
        TestUser user = new TestUser(null, null, null, null);
        
        ViolationReport<TestUser> report = validator.getViolationReport(user);
        assertTrue(report.isValid(), "Null values should be considered valid by custom validators");
    }
    
    @Test
    void testCustomValidatorManager() {
        assertEquals(0, validator.getCustomValidatorManager().getValidatorCount());
        
        validator.getCustomValidatorManager().registerValidatorByClass(EmailValidator.class);
        assertEquals(1, validator.getCustomValidatorManager().getValidatorCount());
        
        validator.getCustomValidatorManager().clearAllValidators();
        assertEquals(0, validator.getCustomValidatorManager().getValidatorCount());
    }
}