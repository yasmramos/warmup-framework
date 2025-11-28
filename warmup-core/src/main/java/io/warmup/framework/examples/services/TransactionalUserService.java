package io.warmup.framework.examples.services;

import io.warmup.framework.annotation.Bean;
import io.warmup.framework.annotation.Component;
import io.warmup.framework.annotation.Inject;
import io.warmup.framework.annotation.Singleton;
import io.warmup.framework.annotation.Transactional;
import io.warmup.framework.core.TransactionManager;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/**
 * Example service demonstrating transactional operations.
 * 
 * <p>
 * This service shows various transactional scenarios including:
 * <ul>
 * <li>Basic transaction management</li>
 * <li>Read-only transactions</li>
 * <li>Transaction propagation</li>
 * <li>Exception handling and rollback</li>
 * <li>Nested transactions</li>
 * </ul>
 *
 * @author MiniMax Agent
 * @version 1.0
 */
@Component
@Singleton
public class TransactionalUserService {

    private static final Logger log = Logger.getLogger(TransactionalUserService.class.getName());

    private final List<User> users = new ArrayList<>();
    private final AuditService auditService;

    @Inject
    public TransactionalUserService(AuditService auditService) {
        this.auditService = auditService;
    }

    /**
     * Creates a new user within a transaction.
     * This method will commit if successful, rollback if exception occurs.
     */
    @Transactional
    public User createUser(String name, String email) {
        log.info("Creating user: " + name);
        
        User user = new User(name, email);
        users.add(user);
        
        // Log the creation
        auditService.logUserCreation(user);
        
        return user;
    }

    /**
     * Updates user information with read-only transaction optimization.
     */
    @Transactional(readOnly = true)
    public User findUserById(Long id) {
        log.info("Finding user with ID: " + id);
        
        return users.stream()
            .filter(user -> user.getId().equals(id))
            .findFirst()
            .orElse(null);
    }

    /**
     * Deletes a user with manual rollback handling.
     */
    @Transactional
    public boolean deleteUser(Long id) {
        log.info("Deleting user with ID: " + id);
        
        User user = findUserById(id);
        if (user == null) {
            log.warning("User not found for deletion: " + id);
            return false;
        }
        
        // Validate deletion
        if (!isUserDeletable(user)) {
            log.warning("User cannot be deleted: " + id);
            return false;
        }
        
        users.remove(user);
        auditService.logUserDeletion(user);
        
        return true;
    }

    /**
     * Batch operation with requires new propagation.
     * Each operation in the batch is independent.
     */
    @Transactional(propagation = Transactional.Propagation.REQUIRES_NEW)
    public void batchCreateUsers(List<String> userNames) {
        log.info("Batch creating " + userNames.size() + " users");
        
        for (String userName : userNames) {
            try {
                User user = new User(userName, userName + "@example.com");
                users.add(user);
                auditService.logUserCreation(user);
            } catch (Exception e) {
                log.severe("Failed to create user: " + userName);
                // Continue with next user, this transaction will handle rollback if needed
            }
        }
    }

    /**
     * Complex operation with nested transaction support.
     */
    @Transactional
    public void transferUserData(User fromUser, User toUser) {
        log.info("Transferring data from user: " + fromUser.getName() + " to: " + toUser.getName());
        
        try {
            // Step 1: Archive from user
            archiveUser(fromUser);
            
            // Step 2: Restore to user
            restoreUser(toUser);
            
            log.info("User data transfer completed successfully");
            
        } catch (Exception e) {
            log.severe("User data transfer failed: " + e.getMessage());
            throw new RuntimeException("Transfer failed", e);
        }
    }

    /**
     * Nested transaction operation.
     */
    @Transactional(propagation = Transactional.Propagation.NESTED)
    private void archiveUser(User user) {
        log.info("Archiving user: " + user.getName());
        auditService.logUserArchive(user);
        // Archive logic here
    }

    /**
     * Another nested transaction operation.
     */
    @Transactional(propagation = Transactional.Propagation.NESTED)
    private void restoreUser(User user) {
        log.info("Restoring user: " + user.getName());
        auditService.logUserRestore(user);
        // Restore logic here
    }

    /**
     * Operation with custom rollback rules.
     */
    @Transactional(
        rollbackFor = IllegalArgumentException.class,
        noRollbackFor = RuntimeException.class
    )
    public User updateUserEmail(Long id, String newEmail) {
        log.info("Updating user email for ID: " + id + " to: " + newEmail);
        
        User user = findUserById(id);
        if (user == null) {
            throw new IllegalArgumentException("User not found: " + id);
        }
        
        // Validate email format
        if (!isValidEmail(newEmail)) {
            throw new IllegalArgumentException("Invalid email format: " + newEmail);
        }
        
        // Update email (this won't rollback for RuntimeException)
        try {
            user.setEmail(newEmail);
            auditService.logUserEmailUpdate(user);
        } catch (Exception e) {
            throw new RuntimeException("Email update failed", e);
        }
        
        return user;
    }

    /**
     * Helper method to check if user can be deleted.
     */
    private boolean isUserDeletable(User user) {
        // Business logic: users with certain criteria cannot be deleted
        return !user.getName().equals("admin");
    }

    /**
     * Helper method to validate email format.
     */
    private boolean isValidEmail(String email) {
        return email != null && email.contains("@") && email.contains(".");
    }

    /**
     * Gets all users.
     */
    public List<User> getAllUsers() {
        return new ArrayList<>(users);
    }

    /**
     * Simple User entity class.
     */
    public static class User {
        private static long nextId = 1;
        
        private final Long id;
        private String name;
        private String email;

        public User(String name, String email) {
            this.id = nextId++;
            this.name = name;
            this.email = email;
        }

        public Long getId() {
            return id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }

        @Override
        public String toString() {
            return "User{id=" + id + ", name='" + name + "', email='" + email + "'}";
        }
    }
}