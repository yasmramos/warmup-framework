package io.warmup.framework.examples.config;

import io.warmup.framework.annotation.Inject;
import io.warmup.framework.core.Warmup;
import io.warmup.framework.examples.services.AuditService;
import io.warmup.framework.examples.services.TransactionalUserService;
import io.warmup.framework.examples.services.TransactionalUserService.User;
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
    private TransactionalUserService userService;

    @Inject
    private AuditService auditService;

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
            User user1 = userService.createUser("John Doe", "john@example.com");
            log.info("Created user successfully: " + user1);
            
            User user2 = userService.createUser("Jane Smith", "jane@example.com");
            log.info("Created user successfully: " + user2);
            
            // Update email - should commit successfully
            User updatedUser = userService.updateUserEmail(user1.getId(), "john.doe@example.com");
            log.info("Updated email successfully: " + updatedUser);
            
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
            // Get existing users for transfer demonstration
            List<User> allUsers = userService.getAllUsers();
            if (allUsers.size() < 2) {
                log.warning("Not enough users for transfer demonstration");
                return;
            }
            
            User fromUser = allUsers.get(0);
            User toUser = allUsers.get(1);
            
            // Transfer user data - demonstrates nested transactions
            log.info("Transferring user data from " + fromUser.getName() + " to " + toUser.getName());
            userService.transferUserData(fromUser, toUser);
            log.info("User data transfer completed");
            
            // Batch create users - demonstrates REQUIRES_NEW propagation
            List<String> batchUsers = Arrays.asList("Batch User 1", "Batch User 2", "Batch User 3");
            log.info("Batch creating users: " + batchUsers);
            userService.batchCreateUsers(batchUsers);
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
            // Try to delete non-existent user - should fail but rollback gracefully
            log.info("Attempting to delete non-existent user");
            boolean deleted = userService.deleteUser(999L);
            log.info("Delete result: " + deleted);
            
            // Try to update with invalid email - should rollback due to IllegalArgumentException
            List<User> allUsers = userService.getAllUsers();
            if (!allUsers.isEmpty()) {
                User user = allUsers.get(0);
                log.info("Attempting to update user " + user.getName() + " with invalid email");
                try {
                    userService.updateUserEmail(user.getId(), "invalid-email");
                } catch (IllegalArgumentException e) {
                    log.info("Caught expected IllegalArgumentException: " + e.getMessage());
                }
            }
            
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
            // Find user by ID - read-only transaction for optimization
            List<User> allUsers = userService.getAllUsers();
            if (!allUsers.isEmpty()) {
                User user = userService.findUserById(allUsers.get(0).getId());
                log.info("Found user with read-only transaction: " + user);
            }
            
        } catch (Exception e) {
            log.severe("Read-only transaction demonstration failed: " + e.getMessage());
        }
    }

    /**
     * Prints summary of operations performed.
     */
    private void printSummary() {
        log.info("\n=== OPERATIONS SUMMARY ===");
        
        // Print users
        List<User> allUsers = userService.getAllUsers();
        log.info("Total users created: " + allUsers.size());
        for (User user : allUsers) {
            log.info("  - " + user);
        }
        
        // Print audit logs
        List<AuditService.AuditLog> auditLogs = auditService.getAuditLogs();
        log.info("Total audit log entries: " + auditLogs.size());
        for (AuditService.AuditLog auditLog : auditLogs) {
            log.info("  - " + auditLog);
        }
    }
}