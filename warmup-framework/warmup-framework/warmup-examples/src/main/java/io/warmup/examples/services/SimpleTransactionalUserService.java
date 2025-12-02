package io.warmup.examples.services;

/**
 * Simple example transactional user service.
 * This is a simplified version for demonstration purposes.
 */
public class SimpleTransactionalUserService {
    
    private final SimpleAuditService auditService;
    
    public SimpleTransactionalUserService(SimpleAuditService auditService) {
        this.auditService = auditService;
    }
    
    public void createUser(String username) {
        auditService.logOperation("Creating user: " + username);
        // Simulate user creation
        System.out.println("User created: " + username);
    }
    
    public static class User {
        private final String username;
        
        public User(String username) {
            this.username = username;
        }
        
        public String getUsername() {
            return username;
        }
    }
}