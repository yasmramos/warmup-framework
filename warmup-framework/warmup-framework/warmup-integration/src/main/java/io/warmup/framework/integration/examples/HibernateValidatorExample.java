package io.warmup.framework.integration.examples;

import io.warmup.framework.integration.hibernate.WarmupHibernateValidatorFactory;
import org.hibernate.validator.HibernateValidator;
import org.hibernate.validator.cfg.ConstraintMapping;
import org.hibernate.validator.cfg.defs.NotNullDef;
import org.hibernate.validator.cfg.defs.SizeDef;
import org.hibernate.validator.group.GroupSequence;
import org.hibernate.validator.group.SequenceProvider;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import jakarta.validation.groups.Default;
import jakarta.validation.metadata.BeanDescriptor;
import jakarta.validation.metadata.ConstraintDescriptor;

import java.util.Set;
import java.util.logging.Logger;

/**
 * Example demonstrating Warmup Framework integration with Hibernate Validator.
 * Shows advanced Hibernate Validator features like groups, sequences, and custom constraints.
 * 
 * @author MiniMax Agent
 * @since 1.0
 */
public class HibernateValidatorExample {

    private static final Logger logger = Logger.getLogger(HibernateValidatorExample.class.getName());

    public static void main(String[] args) {
        logger.info("Starting Hibernate Validator Example...");

        // Create Warmup-based Hibernate Validator
        WarmupHibernateValidatorFactory factory = new WarmupHibernateValidatorFactory();
        HibernateValidator validator = factory.usingContext().failFast(false).getValidator();

        // Example 1: Basic validation
        validateUser(validator);

        // Example 2: Group sequences
        validateWithGroupSequences(validator);

        // Example 3: Programmatic constraint definition
        validateWithProgrammaticConstraints(validator);

        // Example 4: Method validation
        validateMethod(validator);

        factory.close();
        logger.info("Hibernate Validator integration example completed successfully!");
    }

    /**
     * Example 1: Basic validation with Hibernate Validator features.
     */
    private static void validateUser(HibernateValidator validator) {
        logger.info("\n=== Example 1: Basic User Validation ===");

        // Valid user
        User validUser = new User("john_doe", "john@example.com", 25);
        Set<ConstraintViolation<User>> violations = validator.validate(validUser);

        if (violations.isEmpty()) {
            logger.info("User validation passed!");
        } else {
            logger.warning("User validation failed:");
            violations.forEach(violation ->
                logger.warning("- " + violation.getPropertyPath() + ": " + violation.getMessage()));
        }

        // Invalid user
        User invalidUser = new User("J", "invalid-email", 5);
        Set<ConstraintViolation<User>> invalidViolations = validator.validate(invalidUser);

        logger.warning("Invalid user violations:");
        invalidViolations.forEach(violation ->
            logger.warning("- " + violation.getPropertyPath() + ": " + violation.getMessage()));
    }

    /**
     * Example 2: Validation with group sequences.
     */
    private static void validateWithGroupSequences(HibernateValidator validator) {
        logger.info("\n=== Example 2: Group Sequences ===");

        User user = new User("", "test@example.com", 15);

        // Validate with basic group sequence
        Set<ConstraintViolation<User>> basicViolations = validator.validate(user, BasicValidation.class);

        logger.info("Basic validation violations:");
        basicViolations.forEach(violation ->
            logger.warning("- " + violation.getPropertyPath() + ": " + violation.getMessage()));

        // Validate with complete group sequence
        Set<ConstraintViolation<User>> completeViolations = validator.validate(user, CompleteValidation.class);

        logger.info("Complete validation violations:");
        completeViolations.forEach(violation ->
            logger.warning("- " + violation.getPropertyPath() + ": " + violation.getMessage()));
    }

    /**
     * Example 3: Programmatic constraint definition.
     */
    private static void validateWithProgrammaticConstraints(HibernateValidator validator) {
        logger.info("\n=== Example 3: Programmatic Constraints ===");

        // Create validator with programmatic constraints
        var validatorWithConstraints = createValidatorWithConstraints();

        Product product = new Product("", "P001", -5);
        Set<ConstraintViolation<Product>> violations = validatorWithConstraints.validate(product);

        if (violations.isEmpty()) {
            logger.info("Product validation passed!");
        } else {
            logger.warning("Product validation failed:");
            violations.forEach(violation ->
                logger.warning("- " + violation.getPropertyPath() + ": " + violation.getMessage()));
        }
    }

    /**
     * Example 4: Method validation.
     */
    private static void validateMethod(HibernateValidator validator) {
        logger.info("\n=== Example 4: Method Validation ===");

        ProductService service = new ProductService();

        try {
            // This will validate the method parameters
            service.createProduct("Product", "PROD-001", 10);
            logger.info("Method validation passed!");
        } catch (Exception e) {
            logger.warning("Method validation failed: " + e.getMessage());
        }

        try {
            // This should fail validation
            service.createProduct("", "", -5);
        } catch (Exception e) {
            logger.info("Expected validation failure: " + e.getMessage());
        }
    }

    /**
     * Create validator with programmatic constraint definitions.
     */
    private static HibernateValidator createValidatorWithConstraints() {
        WarmupHibernateValidatorFactory factory = new WarmupHibernateValidatorFactory();
        
        var validatorContext = factory.usingContext();
        
        // Add programmatic constraints
        var mapping = createProductConstraintMapping();
        validatorContext.addMapping(mapping);
        
        return validatorContext.getValidator();
    }

    /**
     * Create constraint mapping for Product class.
     */
    private static ConstraintMapping createProductConstraintMapping() {
        ConstraintMapping mapping = new ConstraintMapping();
        
        mapping.type(Product.class)
            .property("name", org.hibernate.validator.spi.PropertyConstraint.class)
            .constraint(new NotNullDef().message("Product name is required"))
            .constraint(new SizeDef().min(3).max(100).message("Product name must be 3-100 characters"));
        
        mapping.type(Product.class)
            .property("code", org.hibernate.validator.spi.PropertyConstraint.class)
            .constraint(new NotNullDef().message("Product code is required"))
            .constraint(new SizeDef().min(3).max(20).message("Product code must be 3-20 characters"));
        
        mapping.type(Product.class)
            .property("price", org.hibernate.validator.spi.PropertyConstraint.class)
            .constraint(new NotNullDef().message("Product price is required"))
            .constraint(new org.hibernate.validator.cfg.defs.DecimalMinDef()
                .value("0")
                .message("Price must be greater than or equal to 0"));
        
        return mapping;
    }
}

/**
 * User class with Hibernate Validator group sequences.
 */
class User {
    
    @NotNull(message = "Username is required", groups = {BasicValidation.class})
    @Size(min = 3, max = 50, message = "Username must be 3-50 characters", groups = {BasicValidation.class})
    private String username;

    @NotNull(message = "Email is required", groups = {BasicValidation.class})
    @Size(min = 5, max = 100, message = "Email must be 5-100 characters", groups = {BasicValidation.class})
    private String email;

    @NotNull(message = "Age is required", groups = {AdvancedValidation.class})
    private Integer age;

    public User(String username, String email, Integer age) {
        this.username = username;
        this.email = email;
        this.age = age;
    }

    public String getUsername() { return username; }
    public String getEmail() { return email; }
    public Integer getAge() { return age; }

    public void setUsername(String username) { this.username = username; }
    public void setEmail(String email) { this.email = email; }
    public void setAge(Integer age) { this.age = age; }
}

/**
 * Product class for programmatic constraints.
 */
class Product {
    private String name;
    private String code;
    private Double price;

    public Product(String name, String code, Double price) {
        this.name = name;
        this.code = code;
        this.price = price;
    }

    public String getName() { return name; }
    public String getCode() { return code; }
    public Double getPrice() { return price; }

    public void setName(String name) { this.name = name; }
    public void setCode(String code) { this.code = code; }
    public void setPrice(Double price) { this.price = price; }
}

/**
 * Validation groups for Hibernate Validator sequences.
 */
interface BasicValidation {
}

interface AdvancedValidation {
}

@GroupSequence({BasicValidation.class, AdvancedValidation.class, Default.class})
interface CompleteValidation {
}

/**
 * Sequence provider for complex validation scenarios.
 */
interface ValidationSequenceProvider extends SequenceProvider<CompleteValidation> {
}

/**
 * Product service demonstrating method validation.
 */
class ProductService {
    private static final Logger logger = Logger.getLogger(ProductService.class.getName());

    public void createProduct(
            @NotNull(message = "Product name is required")
            @Size(min = 3, max = 100, message = "Product name must be 3-100 characters")
            String name,

            @NotNull(message = "Product code is required")
            @Size(min = 3, max = 20, message = "Product code must be 3-20 characters")
            String code,

            @NotNull(message = "Quantity is required")
            Integer quantity) {
        
        logger.info("Creating product: " + name + " (Code: " + code + ", Qty: " + quantity + ")");
        // Business logic for creating product
    }

    public Product updateProduct(
            Product product,
            @NotNull(message = "Updated name is required")
            @Size(min = 3, max = 100, message = "Product name must be 3-100 characters")
            String newName) {
        
        logger.info("Updating product: " + product.getName() + " to: " + newName);
        product.setName(newName);
        return product;
    }
}

/**
 * Hibernate Validator service demonstrating advanced features.
 */
class HibernateValidationService {
    
    private final HibernateValidator validator;
    private static final Logger logger = Logger.getLogger(HibernateValidationService.class.getName());

    public HibernateValidationService() {
        WarmupHibernateValidatorFactory factory = new WarmupHibernateValidatorFactory();
        this.validator = factory.usingContext().failFast(false).getValidator();
    }

    public void processUserWithSequences(User user) {
        // Validate with complete group sequence
        Set<ConstraintViolation<User>> violations = validator.validate(user, CompleteValidation.class);
        
        if (violations.isEmpty()) {
            logger.info("User processing completed successfully!");
        } else {
            logger.warning("User processing failed:");
            violations.forEach(violation -> {
                logger.warning("- " + violation.getPropertyPath() + ": " + violation.getMessage());
                logger.warning("  Message template: " + violation.getMessageTemplate());
            });
        }
    }

    public void validateProductConstraints(Product product) {
        var validatorWithConstraints = createProductValidator();
        Set<ConstraintViolation<Product>> violations = validatorWithConstraints.validate(product);
        
        if (violations.isEmpty()) {
            logger.info("Product constraints validation passed!");
        } else {
            violations.forEach(violation ->
                logger.warning("- " + violation.getPropertyPath() + ": " + violation.getMessage()));
        }
    }

    private HibernateValidator createProductValidator() {
        WarmupHibernateValidatorFactory factory = new WarmupHibernateValidatorFactory();
        
        var mapping = new ConstraintMapping();
        mapping.type(Product.class)
            .property("name", org.hibernate.validator.spi.PropertyConstraint.class)
            .constraint(new NotNullDef().message("Name is required"))
            .constraint(new SizeDef().min(2).max(100).message("Name size violation"));
        
        var validatorContext = factory.usingContext();
        validatorContext.addMapping(mapping);
        
        return validatorContext.getValidator();
    }

    public void validateMethodParameters(String productName, String productCode, Double price) {
        Set<ConstraintViolation<ProductService>> violations = 
            validator.forExecutables(ProductService.class)
                .validateParameters(
                    ProductService.class.getMethod("createProduct", String.class, String.class, Integer.class),
                    new Object[]{productName, productCode, price != null ? price.intValue() : null}
                );
        
        if (violations.isEmpty()) {
            logger.info("Method parameter validation passed!");
        } else {
            violations.forEach(violation ->
                logger.warning("- Parameter: " + violation.getPropertyPath() + ": " + violation.getMessage()));
        }
    }
}
