package io.warmup.framework.security;

/**
 * Security-related data classes for Warmup Framework.
 * Contains security configuration and data structures used across the framework.
 * 
 * @author MiniMax Agent
 * @version 1.0
 */
public class ServiceDataClasses {
    
    /**
     * Security configuration for warmup operations
     */
    public static class SecurityConfig {
        private final boolean enableEncryption;
        private final boolean validatePermissions;
        private final String securityLevel;
        
        public SecurityConfig(boolean enableEncryption, boolean validatePermissions, String securityLevel) {
            this.enableEncryption = enableEncryption;
            this.validatePermissions = validatePermissions;
            this.securityLevel = securityLevel;
        }
        
        public boolean isEncryptionEnabled() {
            return enableEncryption;
        }
        
        public boolean isPermissionsValidationEnabled() {
            return validatePermissions;
        }
        
        public String getSecurityLevel() {
            return securityLevel;
        }
    }
    
    /**
     * Permission levels for warmup operations
     */
    public enum PermissionLevel {
        READ_ONLY,
        READ_WRITE,
        ADMIN,
        SYSTEM
    }
    
    /**
     * Security context for tracking permissions
     */
    public static class SecurityContext {
        private final String userId;
        private final PermissionLevel permissionLevel;
        private final long timestamp;
        
        public SecurityContext(String userId, PermissionLevel permissionLevel) {
            this.userId = userId;
            this.permissionLevel = permissionLevel;
            this.timestamp = System.currentTimeMillis();
        }
        
        public String getUserId() {
            return userId;
        }
        
        public PermissionLevel getPermissionLevel() {
            return permissionLevel;
        }
        
        public long getTimestamp() {
            return timestamp;
        }
    }
}