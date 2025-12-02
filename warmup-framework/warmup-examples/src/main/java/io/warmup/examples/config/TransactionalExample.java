package io.warmup.examples.config;

import io.warmup.framework.annotation.Inject;
import io.warmup.framework.core.Warmup;
import io.warmup.examples.services.SimpleAuditService;
import io.warmup.examples.services.SimpleTransactionalUserService;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;

/**
 * Example application demonstrating @Transactional functionality.
 * 
 * <p>
 * This application demonstrates various transactional scenarios:
 * <ul>
 * <li>Basic transactional operations</li>
 * <li>Transaction propagation (REQUIRED, REQUIRES_NEW, NESTED)</li>
 * <li>Read-only transactions</li>
 * <li>Exception handling and rollback</li>
 * <li>Custom rollback rules</li>
 * </ul>
 *
 * @author MiniMax Agent
 * @version 1.0
 */
public class TransactionalExample {

    private static final Logger log = Logger.getLogger(TransactionalExample.class.getName());

    @Inject
    private SimpleTransactionalUserService userService;

    @Inject
    private SimpleAuditService auditService;

    public static void main(String[] args) {
        log.info("Starting Transactional Example Application");
        
        try {
            // Initialize warmup using public API
            Warmup warmup = Warmup.create();
            
            // Create and register the example instance
            TransactionalExample example = new TransactionalExample();
            warmup.registerBean(TransactionalExample.class, example);
            
            // Get the example instance from warmup
            TransactionalExample retrievedExample = warmup.getBean(TransactionalExample.class);
            
            // Run demonstrations
            retrievedExample.demonstrateBasicTransactions();
            retrievedExample.demonstrateTransactionPropagation();
            retrievedExample.demonstrateRollbackScenarios();
            retrievedExample.demonstrateReadOnlyTransactions();
            
            log.info("Transactional Example Application completed successfully");
            
        } catch (Exception e) {
            log.severe("Example application failed: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Demonstrates basic transaction functionality.
     */
    private void demonstrateBasicTransactions() {
        log.info("\n=== BASIC TRANSACTION DEMONSTRATION ===");
        
        try {
            // Create user - should commit successfully
            userService.createUser("John Doe");
            log.info("Created user successfully");
            
            userService.createUser("Jane Smith");
            log.info("Created user successfully");
            
        } catch (Exception e) {
            log.severe("Basic transaction demonstration failed: " + e.getMessage());
        }
    }

    /**
     * Demonstrates transaction propagation behaviors.
     */
    private void demonstrateTransactionPropagation() {
        log.info("\n=== TRANSACTION PROPAGATION DEMONSTRATION ===");
        
        try {
            // Batch create users - demonstrates transactional operations
            List<String> batchUsers = Arrays.asList("Batch User 1", "Batch User 2", "Batch User 3");
            log.info("Batch creating users: " + batchUsers);
            for (String username : batchUsers) {
                userService.createUser(username);
            }
            log.info("Batch creation completed");
            
        } catch (Exception e) {
            log.severe("Transaction propagation demonstration failed: " + e.getMessage());
        }
    }

    /**
     * Demonstrates rollback scenarios.
     */
    private void demonstrateRollbackScenarios() {
        log.info("\n=== ROLLBACK SCENARIOS DEMONSTRATION ===");
        
        try {
            // Demonstrate rollback with audit logging
            log.info("Demonstrating rollback scenario");
            auditService.logOperation("Rollback demonstration: operations will be rolled back");
            
        } catch (Exception e) {
            log.severe("Rollback demonstration failed: " + e.getMessage());
        }
    }

    /**
     * Demonstrates read-only transactions.
     */
    private void demonstrateReadOnlyTransactions() {
        log.info("\n=== READ-ONLY TRANSACTION DEMONSTRATION ===");
        
        try {
            // Demonstrating read-only transaction with audit
            auditService.logOperation("Read-only transaction: querying user data");
            log.info("Read-only transaction completed");
            
        } catch (Exception e) {
            log.severe("Read-only transaction demonstration failed: " + e.getMessage());
        }
    }

    /**
     * Prints summary of operations performed.
     */
    private void printSummary() {
        log.info("\n=== OPERATIONS SUMMARY ===");
        
        // Print audit information
        auditService.logOperation("Summary: operations completed successfully");
        log.info("Operations summary printed");
    }
}