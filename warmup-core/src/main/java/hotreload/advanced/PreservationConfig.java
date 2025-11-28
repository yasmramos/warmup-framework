package io.warmup.framework.hotreload.advanced;

import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * Configuration for state preservation during hot reload operations.
 * Controls how object state is preserved when classes are reloaded.
 * 
 * @author MiniMax Agent
 * @since 2.0
 */
public class PreservationConfig {
    
    private boolean enabled = true;
    private int maxPreservedInstances = 1000;
    private long preservationTimeoutMs = 5000;
    private Set<String> preserveFields;
    private Set<String> excludeFields;
    private boolean deepCopy = true;
    private boolean backupBeforeReload = true;
    private int maxBackupCount = 3;
    private TimeUnit timeoutUnit = TimeUnit.MILLISECONDS;
    
    public PreservationConfig() {
        // Default configuration
    }
    
    public PreservationConfig(boolean enabled, int maxPreservedInstances, long preservationTimeoutMs) {
        this.enabled = enabled;
        this.maxPreservedInstances = maxPreservedInstances;
        this.preservationTimeoutMs = preservationTimeoutMs;
    }
    
    /**
     * Checks if preservation is enabled.
     * 
     * @return true if enabled
     */
    public boolean isEnabled() {
        return enabled;
    }
    
    /**
     * Sets whether preservation is enabled.
     * 
     * @param enabled the enabled flag
     * @return this config
     */
    public PreservationConfig setEnabled(boolean enabled) {
        this.enabled = enabled;
        return this;
    }
    
    /**
     * Gets maximum number of preserved instances.
     * 
     * @return max preserved instances
     */
    public int getMaxPreservedInstances() {
        return maxPreservedInstances;
    }
    
    /**
     * Sets maximum number of preserved instances.
     * 
     * @param maxPreservedInstances the max count
     * @return this config
     */
    public PreservationConfig setMaxPreservedInstances(int maxPreservedInstances) {
        this.maxPreservedInstances = maxPreservedInstances;
        return this;
    }
    
    /**
     * Gets preservation timeout.
     * 
     * @return timeout value
     */
    public long getPreservationTimeoutMs() {
        return preservationTimeoutMs;
    }
    
    /**
     * Sets preservation timeout.
     * 
     * @param preservationTimeoutMs the timeout in milliseconds
     * @return this config
     */
    public PreservationConfig setPreservationTimeoutMs(long preservationTimeoutMs) {
        this.preservationTimeoutMs = preservationTimeoutMs;
        return this;
    }
    
    /**
     * Gets preserve fields set.
     * 
     * @return preserve fields
     */
    public Set<String> getPreserveFields() {
        return preserveFields;
    }
    
    /**
     * Sets preserve fields.
     * 
     * @param preserveFields the fields to preserve
     * @return this config
     */
    public PreservationConfig setPreserveFields(Set<String> preserveFields) {
        this.preserveFields = preserveFields;
        return this;
    }
    
    /**
     * Gets exclude fields set.
     * 
     * @return exclude fields
     */
    public Set<String> getExcludeFields() {
        return excludeFields;
    }
    
    /**
     * Sets exclude fields.
     * 
     * @param excludeFields the fields to exclude
     * @return this config
     */
    public PreservationConfig setExcludeFields(Set<String> excludeFields) {
        this.excludeFields = excludeFields;
        return this;
    }
    
    /**
     * Checks if deep copy is enabled.
     * 
     * @return true if deep copy enabled
     */
    public boolean isDeepCopy() {
        return deepCopy;
    }
    
    /**
     * Sets whether deep copy is enabled.
     * 
     * @param deepCopy the deep copy flag
     * @return this config
     */
    public PreservationConfig setDeepCopy(boolean deepCopy) {
        this.deepCopy = deepCopy;
        return this;
    }
    
    /**
     * Checks if backup before reload is enabled.
     * 
     * @return true if backup enabled
     */
    public boolean isBackupBeforeReload() {
        return backupBeforeReload;
    }
    
    /**
     * Sets whether backup before reload is enabled.
     * 
     * @param backupBeforeReload the backup flag
     * @return this config
     */
    public PreservationConfig setBackupBeforeReload(boolean backupBeforeReload) {
        this.backupBeforeReload = backupBeforeReload;
        return this;
    }
    
    /**
     * Gets maximum backup count.
     * 
     * @return max backup count
     */
    public int getMaxBackupCount() {
        return maxBackupCount;
    }
    
    /**
     * Sets maximum backup count.
     * 
     * @param maxBackupCount the max backup count
     * @return this config
     */
    public PreservationConfig setMaxBackupCount(int maxBackupCount) {
        this.maxBackupCount = maxBackupCount;
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
    public PreservationConfig setTimeoutUnit(TimeUnit timeoutUnit) {
        this.timeoutUnit = timeoutUnit;
        return this;
    }
    
    /**
     * Gets timeout in milliseconds.
     * 
     * @return timeout in milliseconds
     */
    public long getPreservationTimeoutInMillis() {
        return timeoutUnit.toMillis(preservationTimeoutMs);
    }
    
    @Override
    public String toString() {
        return String.format(
            "PreservationConfig{enabled=%s, maxInstances=%d, timeout=%dms, deepCopy=%s, backup=%s}",
            enabled,
            maxPreservedInstances,
            preservationTimeoutMs,
            deepCopy,
            backupBeforeReload
        );
    }
}