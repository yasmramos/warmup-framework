package io.warmup.framework.integration.examples;

import io.warmup.framework.annotation.validation.*;
import io.warmup.framework.annotation.Bean;
import io.warmup.framework.annotation.Configuration;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.validation.annotation.Validated;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.logging.Logger;

/**
 * Example Spring Boot application demonstrating Warmup Framework integration.
 * Shows how to use Warmup Framework with Spring Boot's validation features.
 * 
 * @author MiniMax Agent
 * @since 1.0
 */
@SpringBootApplication
@Validated
public class WarmupFrameworkSpringExample implements CommandLineRunner {

    private static final Logger logger = Logger.getLogger(WarmupFrameworkSpringExample.class.getName());

    public static void main(String[] args) {
        SpringApplication.run(WarmupFrameworkSpringExample.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
        logger.info("Starting Warmup Framework Spring Boot Example...");

        // Example 1: Basic validation with Warmup Framework annotations
        validateUser(new User("John", "john@email.com", "password123"));
        
        // Example 2: Invalid user
        validateUser(new User("", "invalid-email", "weak"));

        // Example 3: Spring's @Valid annotation
        Order order = new Order("ORD-001", 2);
        validateOrder(order);

        logger.info("Spring Boot integration example completed successfully!");
    }

    @Bean
    public UserValidator userValidator() {
        return new UserValidator();
    }

    private void validateUser(User user) {
        logger.info("Validating user: " + user.getUsername());
        
        try {
            // Use Spring's validation bridge
            var binding = getSpringValidationBridge().validateWithSpringBinding(user, "user");
            
            if (binding.getAllErrors().isEmpty()) {
                logger.info("User validation passed!");
            } else {
                logger.warning("User validation failed:");
                binding.getAllErrors().forEach(error -> 
                    logger.warning("- " + error.getField() + ": " + error.getDefaultMessage()));
            }
        } catch (Exception e) {
            logger.warning("Validation error: " + e.getMessage());
        }
    }

    private void validateOrder(@Valid Order order) {
        logger.info("Validating order: " + order.getOrderNumber());
        // Spring's @Valid annotation will trigger validation
        logger.info("Order validation completed!");
    }

    // Example method that uses Spring's validation
    public void processUserData(@Valid User user) {
        // This method will automatically validate the user parameter
        logger.info("Processing valid user: " + user.getUsername());
    }

    // Inject the Spring validation bridge
    private io.warmup.framework.integration.spring.SpringValidationBridge getSpringValidationBridge() {
        // In a real application, this would be injected via @Autowired
        return new io.warmup.framework.integration.spring.SpringValidationBridge();
    }
}

/**
 * Example user class using Warmup Framework annotations.
 * This demonstrates how to use Warmup Framework's custom validation annotations.
 */
class User {
    @NotNull(message = "Username cannot be null")
    @Size(min = 3, max = 50, message = "Username must be 3-50 characters")
    private String username;

    @NotNull(message = "Email cannot be null")
    @Size(min = 5, max = 100, message = "Email must be 5-100 characters")
    @Email(message = "Invalid email format")
    private String email;

    @NotNull(message = "Password cannot be null")
    @Size(min = 8, message = "Password must be at least 8 characters")
    @CustomConstraint(validatedBy = PasswordValidator.class)
    private String password;

    public User(String username, String email, String password) {
        this.username = username;
        this.email = email;
        this.password = password;
    }

    public String getUsername() { return username; }
    public String getEmail() { return email; }
    public String getPassword() { return password; }

    public void setUsername(String username) { this.username = username; }
    public void setEmail(String email) { this.email = email; }
    public void setPassword(String password) { this.password = password; }
}

/**
 * Example order class using Jakarta validation annotations.
 * This demonstrates compatibility with standard JSR-380 annotations.
 */
class Order {
    @NotNull(message = "Order number cannot be null")
    @Size(min = 3, max = 20, message = "Order number must be 3-20 characters")
    private String orderNumber;

    @NotNull(message = "Quantity cannot be null")
    @Min(value = 1, message = "Quantity must be at least 1")
    private Integer quantity;

    public Order(String orderNumber, Integer quantity) {
        this.orderNumber = orderNumber;
        this.quantity = quantity;
    }

    public String getOrderNumber() { return orderNumber; }
    public Integer getQuantity() { return quantity; }

    public void setOrderNumber(String orderNumber) { this.orderNumber = orderNumber; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }
}

/**
 * Spring configuration with Warmup Framework integration.
 */
@Configuration
class WarmupSpringConfiguration {
    
    @Bean
    public UserService userService(io.warmup.framework.validation.Validator validator) {
        return new UserService(validator);
    }
    
    @Bean
    public OrderService orderService() {
        return new OrderService();
    }
}

/**
 * Example service using Warmup Framework validator.
 */
class UserService {
    private final io.warmup.framework.validation.Validator validator;
    private static final Logger logger = Logger.getLogger(UserService.class.getName());

    public UserService(io.warmup.framework.validation.Validator validator) {
        this.validator = validator;
    }

    public void createUser(User user) {
        if (validator.isValid(user)) {
            logger.info("Creating user: " + user.getUsername());
            // Create user logic here
        } else {
            var violations = validator.getViolationReport(user);
            violations.getViolations().forEach(violation ->
                logger.warning("Validation error: " + violation.getMessage()));
            throw new IllegalArgumentException("User validation failed");
        }
    }
}

/**
 * Example service using Spring's @Valid annotation.
 */
class OrderService {
    private static final Logger logger = Logger.getLogger(OrderService.class.getName());

    public void createOrder(@Valid Order order) {
        logger.info("Creating order: " + order.getOrderNumber() + " with quantity: " + order.getQuantity());
        // Create order logic here
    }
}

/**
 * Custom validator for password validation.
 * This demonstrates custom constraint validation with Warmup Framework.
 */
class PasswordValidator implements io.warmup.framework.annotation.validation.CustomConstraintValidator<String, CustomConstraint> {
    
    @Override
    public void initialize(CustomConstraint annotation) {
        // Initialize with annotation attributes if needed
    }
    
    @Override
    public boolean isValid(String value, CustomConstraint annotation, io.warmup.framework.validation.ValidationContext context) {
        if (value == null) {
            return true; // null values are handled by @NotNull
        }
        
        // Check minimum length
        if (value.length() < 8) {
            return false;
        }
        
        // Check for at least one uppercase letter
        if (!value.matches(".*[A-Z].*")) {
            return false;
        }
        
        // Check for at least one digit
        if (!value.matches(".*\\d.*")) {
            return false;
        }
        
        // Check for at least one special character
        if (!value.matches(".*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>\\/?].*")) {
            return false;
        }
        
        return true;
    }
    
    @Override
    public String getMessage(CustomConstraint annotation, String fieldName, String invalidValue) {
        return "Password must be at least 8 characters long and contain at least one uppercase letter, one digit, and one special character";
    }
}
