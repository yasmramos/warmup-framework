package io.warmup.framework.security;

/**
 * Security manager for the Warmup Framework.
 * Manages security policies and permissions.
 * 
 * @author MiniMax Agent
 * @version 1.0
 */
public class SecurityManager {
    
    private static SecurityManager instance;
    
    public static synchronized SecurityManager getInstance() {
        if (instance == null) {
            instance = new SecurityManager();
        }
        return instance;
    }
    
    public SecurityManager() {
        // Constructor
    }
    
    public boolean hasPermission(String permission) {
        // Simple permission check
        return true; // Allow all for now
    }
}