package io.warmup.examples.config;

/**
 * Relaxed security service for less strict security operations.
 */
public class RelaxedSecurityService {
    
    public boolean authenticate(String username, String password) {
        System.out.println("Authenticating user with relaxed security: " + username);
        return true; // Simplified for example
    }
    
    public void authorize(String user, String action) {
        System.out.println("Authorizing action with relaxed security: " + user + " -> " + action);
    }
}