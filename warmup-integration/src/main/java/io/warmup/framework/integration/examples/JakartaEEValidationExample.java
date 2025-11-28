package io.warmup.framework.integration.examples;

import io.warmup.framework.integration.jakartaee.WarmupValidatorFactory;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import jakarta.validation.groups.Default;
import jakarta.validation.metadata.BeanDescriptor;
import jakarta.validation.metadata.ConstraintDescriptor;

import java.util.Set;
import java.util.logging.Logger;

/**
 * Example demonstrating Warmup Framework integration with Jakarta EE.
 * Shows how to use Warmup Framework with standard Jakarta Validation API.
 * 
 * @author MiniMax Agent
 * @since 1.0
 */
public class JakartaEEValidationExample {

    private static final Logger logger = Logger.getLogger(JakartaEEValidationExample.class.getName());

    public static void main(String[] args) {
        logger.info("Starting Jakarta EE Validation Example...");

        // Create Warmup-based ValidatorFactory
        ValidatorFactory factory = new WarmupValidatorFactory();
        Validator validator = factory.getValidator();

        // Example 1: Basic validation
        validateCustomer(validator);

        // Example 2: Property-specific validation
        validateProperty(validator);

        // Example 3: Validation with groups
        validateWithGroups(validator);

        // Example 4: Bean descriptor inspection
        inspectBeanDescriptor(validator);

        factory.close();
        logger.info("Jakarta EE integration example completed successfully!");
    }

    /**
     * Example 1: Basic validation of a Customer bean.
     */
    private static void validateCustomer(Validator validator) {
        logger.info("\n=== Example 1: Basic Validation ===");

        // Valid customer
        Customer validCustomer = new Customer("John Doe", "john@example.com", 25);
        Set<ConstraintViolation<Customer>> violations = validator.validate(validCustomer);

        if (violations.isEmpty()) {
            logger.info("Customer validation passed!");
        } else {
            logger.warning("Customer validation failed:");
            violations.forEach(violation ->
                logger.warning("- " + violation.getPropertyPath() + ": " + violation.getMessage()));
        }

        // Invalid customer
        Customer invalidCustomer = new Customer("", "invalid-email", -5);
        Set<ConstraintViolation<Customer>> invalidViolations = validator.validate(invalidCustomer);

        logger.warning("Invalid customer violations:");
        invalidViolations.forEach(violation ->
            logger.warning("- " + violation.getPropertyPath() + ": " + violation.getMessage()));
    }

    /**
     * Example 2: Property-specific validation.
     */
    private static void validateProperty(Validator validator) {
        logger.info("\n=== Example 2: Property-Specific Validation ===");

        Customer customer = new Customer("Jane Doe", "jane@example.com", 25);

        // Validate only the email property
        Set<ConstraintViolation<Customer>> emailViolations = 
            validator.validateProperty(customer, "email");

        if (emailViolations.isEmpty()) {
            logger.info("Email validation passed!");
        } else {
            logger.warning("Email validation failed:");
            emailViolations.forEach(violation ->
                logger.warning("- " + violation.getPropertyPath() + ": " + violation.getMessage()));
        }

        // Validate with a bad email
        Set<ConstraintViolation<Customer>> badEmailViolations = 
            validator.validateProperty(customer, "email", new Object(), "bad-email");

        badEmailViolations.forEach(violation ->
            logger.warning("- " + violation.getPropertyPath() + ": " + violation.getMessage()));
    }

    /**
     * Example 3: Validation with groups.
     */
    private static void validateWithGroups(Validator validator) {
        logger.info("\n=== Example 3: Validation with Groups ===");

        Customer customer = new Customer("", "test@example.com", 15);

        // Validate with default group
        Set<ConstraintViolation<Customer>> defaultViolations = 
            validator.validate(customer, Default.class);

        logger.info("Default group violations:");
        defaultViolations.forEach(violation ->
            logger.warning("- " + violation.getPropertyPath() + ": " + violation.getMessage()));

        // Validate with basic info group
        Set<ConstraintViolation<Customer>> basicViolations = 
            validator.validate(customer, BasicInfo.class);

        logger.info("BasicInfo group violations:");
        basicViolations.forEach(violation ->
            logger.warning("- " + violation.getPropertyPath() + ": " + violation.getMessage()));
    }

    /**
     * Example 4: Bean descriptor inspection.
     */
    private static void inspectBeanDescriptor(Validator validator) {
        logger.info("\n=== Example 4: Bean Descriptor Inspection ===");

        BeanDescriptor descriptor = validator.getConstraintsForClass(Customer.class);

        logger.info("Bean descriptor for Customer:");
        logger.info("- Has constraints: " + descriptor.hasConstraints());
        logger.info("- Constrained properties: " + descriptor.getConstrainedProperties());

        // Inspect each constrained property
        descriptor.getConstrainedProperties().forEach(propertyName -> {
            var propertyDescriptor = descriptor.getConstraintsForProperty(propertyName);
            if (propertyDescriptor != null) {
                logger.info("Property '" + propertyName + "':");
                logger.info("  - Has constraints: " + propertyDescriptor.hasConstraints());
                logger.info("  - Validation target: " + propertyDescriptor.getValidationTarget());
                
                propertyDescriptor.getConstraintDescriptors().forEach(constraintDescriptor -> {
                    logger.info("  - Constraint: " + constraintDescriptor.getMessageTemplate());
                    logger.info("    Groups: " + constraintDescriptor.getGroups());
                    logger.info("    Payload: " + constraintDescriptor.getPayload());
                });
            }
        });
    }
}

/**
 * Customer bean demonstrating Jakarta EE validation annotations.
 * This class uses standard JSR-380 annotations that work with Warmup Framework.
 */
class Customer {
    
    @NotNull(message = "Name is required", groups = {Default.class, BasicInfo.class})
    @Size(min = 2, max = 100, message = "Name must be 2-100 characters", groups = {Default.class, BasicInfo.class})
    private String name;

    @NotNull(message = "Email is required", groups = {Default.class})
    @Size(min = 5, max = 100, message = "Email must be 5-100 characters", groups = {Default.class})
    private String email;

    @NotNull(message = "Age is required", groups = {Default.class, AdvancedInfo.class})
    private Integer age;

    public Customer(String name, String email, Integer age) {
        this.name = name;
        this.email = email;
        this.age = age;
    }

    public String getName() { return name; }
    public String getEmail() { return email; }
    public Integer getAge() { return age; }

    public void setName(String name) { this.name = name; }
    public void setEmail(String email) { this.email = email; }
    public void setAge(Integer age) { this.age = age; }
}

/**
 * Validation group for basic information.
 */
interface BasicInfo {
}

/**
 * Validation group for advanced information.
 */
interface AdvancedInfo {
}

/**
 * Jakarta EE service demonstrating validation integration.
 * Shows how to use Warmup Framework validator in enterprise applications.
 */
class CustomerService {
    
    private final Validator validator;
    private static final Logger logger = Logger.getLogger(CustomerService.class.getName());

    public CustomerService() {
        ValidatorFactory factory = new WarmupValidatorFactory();
        this.validator = factory.getValidator();
    }

    public void createCustomer(Customer customer) {
        // Validate the customer using Jakarta EE API
        Set<ConstraintViolation<Customer>> violations = validator.validate(customer);
        
        if (violations.isEmpty()) {
            logger.info("Creating customer: " + customer.getName());
            // Business logic for creating customer
        } else {
            logger.warning("Customer validation failed:");
            violations.forEach(violation ->
                logger.warning("- " + violation.getPropertyPath() + ": " + violation.getMessage()));
            
            throw new IllegalArgumentException("Customer validation failed");
        }
    }

    public void updateCustomerInfo(String customerId, Customer updatedCustomer) {
        // Validate only basic info group when updating basic information
        Set<ConstraintViolation<Customer>> basicViolations = 
            validator.validate(updatedCustomer, BasicInfo.class);
        
        if (basicViolations.isEmpty()) {
            logger.info("Updating basic info for customer: " + customerId);
            // Business logic for updating customer
        } else {
            throw new IllegalArgumentException("Basic info validation failed");
        }
    }

    public void validateAge(Integer age) {
        // Validate value without creating object
        ValidatorFactory factory = new WarmupValidatorFactory();
        Validator validator = factory.getValidator();
        
        Set<ConstraintViolation<Customer>> violations = 
            validator.validateValue(Customer.class, "age", age);
        
        if (violations.isEmpty()) {
            logger.info("Age validation passed: " + age);
        } else {
            violations.forEach(violation ->
                logger.warning("Age validation failed: " + violation.getMessage()));
        }
    }
}
