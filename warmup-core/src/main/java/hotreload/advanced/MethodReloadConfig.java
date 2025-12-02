package io.warmup.framework.hotreload.advanced;

import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * Configuration for method-level hot reload operations.
 * Controls how individual methods are reloaded without affecting the entire class.
 * 
 * @author MiniMax Agent
 * @since 2.0
 */
public class MethodReloadConfig {
    
    private boolean enabled = true;
    private Set<String> reloadableMethods;
    private Set<String> excludedMethods;
    private long methodReloadTimeoutMs = 2000;
    private boolean preserveMethodState = true;
    private boolean validateSignature = true;
    private boolean backupBeforeMethodReload = true;
    private int maxMethodReloadRetries = 3;
    private TimeUnit timeoutUnit = TimeUnit.MILLISECONDS;
    private boolean asyncReload = false;
    private int maxConcurrentMethodReloads = 5;
    
    public MethodReloadConfig() {
        // Default configuration
    }
    
    public MethodReloadConfig(boolean enabled, long methodReloadTimeoutMs) {
        this.enabled = enabled;
        this.methodReloadTimeoutMs = methodReloadTimeoutMs;
    }
    
    /**
     * Checks if method reload is enabled.
     * 
     * @return true if enabled
     */
    public boolean isEnabled() {
        return enabled;
    }
    
    /**
     * Sets whether method reload is enabled.
     * 
     * @param enabled the enabled flag
     * @return this config
     */
    public MethodReloadConfig setEnabled(boolean enabled) {
        this.enabled = enabled;
        return this;
    }
    
    /**
     * Gets reloadable methods set.
     * 
     * @return reloadable methods
     */
    public Set<String> getReloadableMethods() {
        return reloadableMethods;
    }
    
    /**
     * Sets reloadable methods.
     * 
     * @param reloadableMethods the methods that can be reloaded
     * @return this config
     */
    public MethodReloadConfig setReloadableMethods(Set<String> reloadableMethods) {
        this.reloadableMethods = reloadableMethods;
        return this;
    }
    
    /**
     * Gets excluded methods set.
     * 
     * @return excluded methods
     */
    public Set<String> getExcludedMethods() {
        return excludedMethods;
    }
    
    /**
     * Sets excluded methods.
     * 
     * @param excludedMethods the methods to exclude from reload
     * @return this config
     */
    public MethodReloadConfig setExcludedMethods(Set<String> excludedMethods) {
        this.excludedMethods = excludedMethods;
        return this;
    }
    
    /**
     * Gets method reload timeout.
     * 
     * @return timeout value
     */
    public long getMethodReloadTimeoutMs() {
        return methodReloadTimeoutMs;
    }
    
    /**
     * Sets method reload timeout.
     * 
     * @param methodReloadTimeoutMs the timeout in milliseconds
     * @return this config
     */
    public MethodReloadConfig setMethodReloadTimeoutMs(long methodReloadTimeoutMs) {
        this.methodReloadTimeoutMs = methodReloadTimeoutMs;
        return this;
    }
    
    /**
     * Checks if method state preservation is enabled.
     * 
     * @return true if preserve method state
     */
    public boolean isPreserveMethodState() {
        return preserveMethodState;
    }
    
    /**
     * Sets whether method state preservation is enabled.
     * 
     * @param preserveMethodState the preserve flag
     * @return this config
     */
    public MethodReloadConfig setPreserveMethodState(boolean preserveMethodState) {
        this.preserveMethodState = preserveMethodState;
        return this;
    }
    
    /**
     * Checks if signature validation is enabled.
     * 
     * @return true if validate signature
     */
    public boolean isValidateSignature() {
        return validateSignature;
    }
    
    /**
     * Sets whether signature validation is enabled.
     * 
     * @param validateSignature the validate flag
     * @return this config
     */
    public MethodReloadConfig setValidateSignature(boolean validateSignature) {
        this.validateSignature = validateSignature;
        return this;
    }
    
    /**
     * Checks if backup before method reload is enabled.
     * 
     * @return true if backup enabled
     */
    public boolean isBackupBeforeMethodReload() {
        return backupBeforeMethodReload;
    }
    
    /**
     * Sets whether backup before method reload is enabled.
     * 
     * @param backupBeforeMethodReload the backup flag
     * @return this config
     */
    public MethodReloadConfig setBackupBeforeMethodReload(boolean backupBeforeMethodReload) {
        this.backupBeforeMethodReload = backupBeforeMethodReload;
        return this;
    }
    
    /**
     * Gets maximum method reload retries.
     * 
     * @return max retries
     */
    public int getMaxMethodReloadRetries() {
        return maxMethodReloadRetries;
    }
    
    /**
     * Sets maximum method reload retries.
     * 
     * @param maxMethodReloadRetries the max retries
     * @return this config
     */
    public MethodReloadConfig setMaxMethodReloadRetries(int maxMethodReloadRetries) {
        this.maxMethodReloadRetries = maxMethodReloadRetries;
        return this;
    }
    
    /**
     * Gets timeout unit.
     * 
     * @return timeout unit
     */
    public TimeUnit getTimeoutUnit() {
        return timeoutUnit;
    }
    
    /**
     * Sets timeout unit.
     * 
     * @param timeoutUnit the timeout unit
     * @return this config
     */
    public MethodReloadConfig setTimeoutUnit(TimeUnit timeoutUnit) {
        this.timeoutUnit = timeoutUnit;
        return this;
    }
    
    /**
     * Checks if async reload is enabled.
     * 
     * @return true if async reload enabled
     */
    public boolean isAsyncReload() {
        return asyncReload;
    }
    
    /**
     * Sets whether async reload is enabled.
     * 
     * @param asyncReload the async flag
     * @return this config
     */
    public MethodReloadConfig setAsyncReload(boolean asyncReload) {
        this.asyncReload = asyncReload;
        return this;
    }
    
    /**
     * Gets maximum concurrent method reloads.
     * 
     * @return max concurrent reloads
     */
    public int getMaxConcurrentMethodReloads() {
        return maxConcurrentMethodReloads;
    }
    
    /**
     * Sets maximum concurrent method reloads.
     * 
     * @param maxConcurrentMethodReloads the max concurrent reloads
     * @return this config
     */
    public MethodReloadConfig setMaxConcurrentMethodReloads(int maxConcurrentMethodReloads) {
        this.maxConcurrentMethodReloads = maxConcurrentMethodReloads;
        return this;
    }
    
    /**
     * Gets timeout in milliseconds.
     * 
     * @return timeout in milliseconds
     */
    public long getMethodReloadTimeoutInMillis() {
        return timeoutUnit.toMillis(methodReloadTimeoutMs);
    }
    
    /**
     * Checks if a method can be reloaded based on this configuration.
     * 
     * @param methodName the method name
     * @return true if the method can be reloaded
     */
    public boolean canReloadMethod(String methodName) {
        if (!enabled) {
            return false;
        }
        
        if (excludedMethods != null && excludedMethods.contains(methodName)) {
            return false;
        }
        
        if (reloadableMethods != null && !reloadableMethods.isEmpty()) {
            return reloadableMethods.contains(methodName);
        }
        
        return true; // If no specific reloadable methods set, allow all
    }
    
    @Override
    public String toString() {
        return String.format(
            "MethodReloadConfig{enabled=%s, timeout=%dms, preserveState=%s, async=%s, maxConcurrent=%d}",
            enabled,
            methodReloadTimeoutMs,
            preserveMethodState,
            asyncReload,
            maxConcurrentMethodReloads
        );
    }
}