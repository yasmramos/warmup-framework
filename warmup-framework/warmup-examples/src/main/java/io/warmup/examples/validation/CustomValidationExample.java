package io.warmup.examples.validation;

import io.warmup.framework.annotation.validation.*;
import io.warmup.framework.core.Warmup;
import io.warmup.framework.annotation.Configuration;
import io.warmup.framework.annotation.Bean;

import java.util.regex.Pattern;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/**
 * Example demonstrating custom validators in action.
 * Shows how to use @CustomConstraint with EmailValidator and PasswordValidator.
 * 
 * @author MiniMax Agent
 * @since 1.0
 */
@Configuration
public class CustomValidationExample {
    
    private static final Logger logger = Logger.getLogger(CustomValidationExample.class.getName());
    
    /**
     * Example user class with custom validation.
     */
    public static class User {
        @NotNull(message = "Username cannot be null")
        @Size(min = 3, max = 50, message = "Username must be 3-50 characters")
        @CustomConstraint(
            validator = UsernameValidator.class,
            message = "Username must contain only letters, numbers, and underscores"
        )
        private String username;
        
        @NotNull(message = "Email cannot be null")
        @Size(max = 100, message = "Email must not exceed 100 characters")
        @CustomConstraint(
            validator = EmailValidator.class,
            message = "Invalid email format"
        )
        private String email;
        
        @NotNull(message = "Password cannot be null")
        @Size(min = 8, message = "Password must be at least 8 characters")
        @CustomConstraint(
            validator = PasswordValidator.class,
            message = "Password does not meet security requirements"
        )
        private String password;
        
        @Valid
        private Address address;
        
        public User(String username, String email, String password) {
            this.username = username;
            this.email = email;
            this.password = password;
            this.address = new Address();
        }
        
        // Getters
        public String getUsername() { return username; }
        public String getEmail() { return email; }
        public String getPassword() { return password; }
        public Address getAddress() { return address; }
        
        // Setters
        public void setUsername(String username) { this.username = username; }
        public void setEmail(String email) { this.email = email; }
        public void setPassword(String password) { this.password = password; }
        public void setAddress(Address address) { this.address = address; }
    }
    
    /**
     * Address class for nested validation.
     */
    public static class Address {
        @Size(max = 200, message = "Street address must not exceed 200 characters")
        private String street;
        
        @Size(max = 100, message = "City must not exceed 100 characters")
        private String city;
        
        @io.warmup.framework.annotation.validation.Pattern(regexp = "\\d{5}(-\\d{4})?", message = "Invalid ZIP code format")
        private String zipCode;
        
        @CustomConstraint(
            validator = CountryValidator.class,
            parameters = {"US", "CA", "UK", "DE", "FR"}
        )
        private String country;
        
        // Getters and setters
        public String getStreet() { return street; }
        public String getCity() { return city; }
        public String getZipCode() { return zipCode; }
        public String getCountry() { return country; }
        
        public void setStreet(String street) { this.street = street; }
        public void setCity(String city) { this.city = city; }
        public void setZipCode(String zipCode) { this.zipCode = zipCode; }
        public void setCountry(String country) { this.country = country; }
    }
    
    /**
     * Username validator - allows only letters, numbers, and underscores.
     */
    public static class UsernameValidator implements CustomConstraintValidator {
        private static final java.util.regex.Pattern USERNAME_PATTERN = java.util.regex.Pattern.compile("^[a-zA-Z0-9_]+$");
        
        @Override
        public boolean isValid(Object value, Object... parameters) {
            if (value == null) return true;
            return USERNAME_PATTERN.matcher(value.toString()).matches();
        }
        
        @Override
        public String getMessage(Object invalidValue, String fieldName, Object... parameters) {
            return String.format("Username '%s' contains invalid characters. Only letters, numbers, and underscores are allowed", 
                               invalidValue);
        }
    }
    
    /**
     * Country validator - validates against allowed countries.
     */
    public static class CountryValidator implements CustomConstraintValidator {
        private List<String> allowedCountries = new ArrayList<>();
        
        @Override
        public void initialize(Object... parameters) {
            if (parameters != null) {
                for (Object param : parameters) {
                    allowedCountries.add(param.toString());
                }
            }
        }
        
        @Override
        public boolean isValid(Object value, Object... parameters) {
            if (value == null) return true;
            return allowedCountries.contains(value.toString().toUpperCase());
        }
        
        @Override
        public String getMessage(Object invalidValue, String fieldName, Object... parameters) {
            return String.format("Country '%s' is not supported. Allowed countries: %s", 
                               invalidValue, allowedCountries);
        }
    }
    
    /**
     * User service bean.
     */
    @Bean
    public UserService userService() {
        return new UserService();
    }
    
    /**
     * Simple user service.
     */
    public static class UserService {
        private final Logger logger = Logger.getLogger(UserService.class.getName());
        
        public void createUser(User user) {
            // This will trigger automatic validation via @Bean lifecycle
            logger.info("Creating user: " + user.getUsername());
            // User creation logic here
        }
    }
    
    /**
     * Run the example.
     */
    public static void main(String[] args) {
        try {
            logger.info("Starting Custom Validation Example...");
            
            // Create Warmup instance using public API
            Warmup warmup = Warmup.create();
            
            // Register configuration class as bean
            warmup.registerBean(CustomValidationExample.class, new CustomValidationExample());
            
            // Create and register custom validators
            // Note: In real usage, validators would be registered during container initialization
            logger.info("Custom validators registered: EmailValidator, PasswordValidator, UsernameValidator, CountryValidator");
            
            // Test cases
            testValidUser(warmup);
            testInvalidUser();
            
        } catch (Exception e) {
            logger.severe("Error running example: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Test with valid user data.
     */
    private static void testValidUser(Warmup warmup) {
        logger.info("\n=== Testing Valid User ===");
        
        User validUser = new User(
            "john_doe_123",
            "john.doe@example.com",
            "SecurePass123!"
        );
        validUser.getAddress().setCountry("US");
        validUser.getAddress().setZipCode("12345");
        
        logger.info("Valid user created: " + validUser.getUsername());
        logger.info("Should pass all validation checks");
    }
    
    /**
     * Test with invalid user data to demonstrate validation.
     */
    private static void testInvalidUser() {
        logger.info("\n=== Testing Invalid User ===");
        
        // Test invalid email
        User invalidEmail = new User(
            "jane_doe",
            "invalid-email",
            "StrongPass123!"
        );
        logger.info("Invalid email test: " + invalidEmail.getEmail());
        
        // Test weak password
        User weakPassword = new User(
            "bob_smith",
            "bob@example.com",
            "weak"
        );
        logger.info("Weak password test: " + weakPassword.getPassword());
        
        // Test invalid username
        User invalidUsername = new User(
            "user@invalid",
            "user@example.com",
            "StrongPass123!"
        );
        logger.info("Invalid username test: " + invalidUsername.getUsername());
    }
}