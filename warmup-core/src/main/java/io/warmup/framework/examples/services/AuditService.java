package io.warmup.framework.examples.services;

import io.warmup.framework.annotation.Bean;
import io.warmup.framework.annotation.Component;
import io.warmup.framework.annotation.Inject;
import io.warmup.framework.annotation.Singleton;
import io.warmup.framework.annotation.Transactional;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/**
 * Audit service for logging transactional operations.
 * 
 * <p>
 * This service demonstrates transactional behavior in audit logging scenarios.
 * It includes methods with different transaction requirements to show various
 * propagation behaviors and rollback scenarios.
 *
 * @author MiniMax Agent
 * @version 1.0
 */
@Component
@Singleton
public class AuditService {

    private static final Logger log = Logger.getLogger(AuditService.class.getName());

    private final List<AuditLog> auditLogs = new ArrayList<>();

    /**
     * Logs user creation - always succeeds, commits transaction.
     */
    @Transactional
    public void logUserCreation(TransactionalUserService.User user) {
        log.info("Logging user creation: " + user);
        AuditLog logEntry = new AuditLog("USER_CREATED", user.getId(), 
            "Created user: " + user.getName());
        auditLogs.add(logEntry);
    }

    /**
     * Logs user deletion - commits or rolls back with parent transaction.
     */
    @Transactional
    public void logUserDeletion(TransactionalUserService.User user) {
        log.info("Logging user deletion: " + user);
        AuditLog logEntry = new AuditLog("USER_DELETED", user.getId(), 
            "Deleted user: " + user.getName());
        auditLogs.add(logEntry);
    }

    /**
     * Logs user archive with nested transaction.
     */
    @Transactional(propagation = Transactional.Propagation.NESTED)
    public void logUserArchive(TransactionalUserService.User user) {
        log.info("Logging user archive: " + user);
        AuditLog logEntry = new AuditLog("USER_ARCHIVED", user.getId(), 
            "Archived user: " + user.getName());
        auditLogs.add(logEntry);
    }

    /**
     * Logs user restore with nested transaction.
     */
    @Transactional(propagation = Transactional.Propagation.NESTED)
    public void logUserRestore(TransactionalUserService.User user) {
        log.info("Logging user restore: " + user);
        AuditLog logEntry = new AuditLog("USER_RESTORED", user.getId(), 
            "Restored user: " + user.getName());
        auditLogs.add(logEntry);
    }

    /**
     * Logs email update - uses requires new to be independent.
     */
    @Transactional(propagation = Transactional.Propagation.REQUIRES_NEW)
    public void logUserEmailUpdate(TransactionalUserService.User user) {
        log.info("Logging email update: " + user);
        AuditLog logEntry = new AuditLog("EMAIL_UPDATED", user.getId(), 
            "Updated email to: " + user.getEmail());
        auditLogs.add(logEntry);
    }

    /**
     * Logs system operations - supports existing transactions.
     */
    @Transactional(propagation = Transactional.Propagation.SUPPORTS)
    public void logSystemOperation(String operation, String details) {
        log.info("Logging system operation: " + operation);
        AuditLog logEntry = new AuditLog(operation, 0L, details);
        auditLogs.add(logEntry);
    }

    /**
     * Gets all audit logs.
     */
    public List<AuditLog> getAuditLogs() {
        return new ArrayList<>(auditLogs);
    }

    /**
     * Gets audit logs by user ID.
     */
    public List<AuditLog> getAuditLogsByUserId(Long userId) {
        List<AuditLog> userLogs = new ArrayList<>();
        for (AuditLog log : auditLogs) {
            if (log.getUserId().equals(userId)) {
                userLogs.add(log);
            }
        }
        return userLogs;
    }

    /**
     * Audit log entry.
     */
    public static class AuditLog {
        private final String operation;
        private final Long userId;
        private final String details;
        private final long timestamp;

        public AuditLog(String operation, Long userId, String details) {
            this.operation = operation;
            this.userId = userId;
            this.details = details;
            this.timestamp = System.currentTimeMillis();
        }

        public String getOperation() {
            return operation;
        }

        public Long getUserId() {
            return userId;
        }

        public String getDetails() {
            return details;
        }

        public long getTimestamp() {
            return timestamp;
        }

        @Override
        public String toString() {
            return String.format("AuditLog{operation='%s', userId=%s, details='%s', timestamp=%d}",
                operation, userId, details, timestamp);
        }
    }
}