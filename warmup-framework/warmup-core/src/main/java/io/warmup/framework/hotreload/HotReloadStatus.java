package io.warmup.framework.hotreload;

import java.util.HashMap;
import java.util.Map;

/**
 * Status class for hot reload operations.
 * Contains information about the result of hot reload operations.
 */
public class HotReloadStatus {
    
    private String className;
    private boolean success;
    private String message;
    private long startTime;
    private long endTime;
    private Map<String, Object> metadata;
    private Exception error;
    
    public HotReloadStatus() {
        this.metadata = new HashMap<>();
    }
    
    public HotReloadStatus(boolean success, String message) {
        this();
        this.success = success;
        this.message = message;
    }
    
    /**
     * Constructor for advanced status with additional parameters.
     * 
     * @param enabled whether hot reload is enabled
     * @param running whether hot reload is running
     * @param pendingReloads number of pending reloads
     * @param monitoredFilesCount count of monitored files
     * @param monitoredDirectories set of monitored directories
     */
    public HotReloadStatus(boolean enabled, boolean running, int pendingReloads, int monitoredFilesCount, java.util.Set<java.nio.file.Path> monitoredDirectories) {
        this(true, "Advanced hot reload status");
        addMetadata("enabled", enabled);
        addMetadata("running", running);
        setPendingReloads(pendingReloads);
        addMetadata("monitoredFilesCount", monitoredFilesCount);
        setMonitoredDirectories(monitoredDirectories);
    }
    
    public String getClassName() {
        return className;
    }
    
    public void setClassName(String className) {
        this.className = className;
    }
    
    public boolean isSuccess() {
        return success;
    }
    
    public void setSuccess(boolean success) {
        this.success = success;
    }
    
    public String getMessage() {
        return message;
    }
    
    public void setMessage(String message) {
        this.message = message;
    }
    
    public long getStartTime() {
        return startTime;
    }
    
    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }
    
    public long getEndTime() {
        return endTime;
    }
    
    public void setEndTime(long endTime) {
        this.endTime = endTime;
    }
    
    public long getDuration() {
        if (startTime > 0 && endTime > 0) {
            return endTime - startTime;
        }
        return 0;
    }
    
    public Map<String, Object> getMetadata() {
        return metadata;
    }
    
    public void setMetadata(Map<String, Object> metadata) {
        this.metadata = metadata;
    }
    
    public Exception getError() {
        return error;
    }
    
    public void setError(Exception error) {
        this.error = error;
    }
    
    public boolean hasError() {
        return error != null;
    }
    
    public void addMetadata(String key, Object value) {
        if (metadata == null) {
            metadata = new HashMap<>();
        }
        metadata.put(key, value);
    }
    
    // Additional methods for advanced hot reload functionality
    
    /**
     * Gets the monitored files (for advanced functionality).
     * 
     * @return set of monitored files
     */
    public java.util.Set<java.nio.file.Path> getMonitoredFiles() {
        return (java.util.Set<java.nio.file.Path>) metadata.get("monitoredFiles");
    }
    
    /**
     * Sets the monitored files.
     * 
     * @param monitoredFiles the monitored files
     */
    public void setMonitoredFiles(java.util.Set<java.nio.file.Path> monitoredFiles) {
        addMetadata("monitoredFiles", monitoredFiles);
    }
    
    /**
     * Gets the pending reloads (for advanced functionality).
     * 
     * @return number of pending reloads
     */
    public int getPendingReloads() {
        Object value = metadata.get("pendingReloads");
        return value instanceof Integer ? (Integer) value : 0;
    }
    
    /**
     * Sets the pending reloads count.
     * 
     * @param pendingReloads the pending reloads count
     */
    public void setPendingReloads(int pendingReloads) {
        addMetadata("pendingReloads", pendingReloads);
    }
    
    /**
     * Gets the monitored directories (for advanced functionality).
     * 
     * @return set of monitored directories
     */
    public java.util.Set<java.nio.file.Path> getMonitoredDirectories() {
        return (java.util.Set<java.nio.file.Path>) metadata.get("monitoredDirectories");
    }
    
    /**
     * Sets the monitored directories.
     * 
     * @param monitoredDirectories the monitored directories
     */
    public void setMonitoredDirectories(java.util.Set<java.nio.file.Path> monitoredDirectories) {
        addMetadata("monitoredDirectories", monitoredDirectories);
    }
    
    /**
     * Checks if hot reload is enabled (for test compatibility).
     * 
     * @return true if enabled
     */
    public boolean isEnabled() {
        Object enabled = metadata.get("enabled");
        return enabled instanceof Boolean ? (Boolean) enabled : false;
    }
    
    /**
     * Sets the enabled state (for test compatibility).
     * 
     * @param enabled the enabled state
     */
    public void setEnabled(boolean enabled) {
        addMetadata("enabled", enabled);
    }
    
    /**
     * Checks if hot reload is running (for test compatibility).
     * 
     * @return true if running
     */
    public boolean isRunning() {
        Object running = metadata.get("running");
        return running instanceof Boolean ? (Boolean) running : false;
    }
    
    /**
     * Sets the running state (for test compatibility).
     * 
     * @param running the running state
     */
    public void setRunning(boolean running) {
        addMetadata("running", running);
    }
    
    /**
     * Checks if hot reload can be performed (for test compatibility).
     * 
     * @return true if can reload
     */
    public boolean canReload() {
        return isEnabled() && !hasError();
    }
    
    /**
     * Gets the monitored files count (for test compatibility).
     * 
     * @return number of monitored files
     */
    public int getMonitoredFilesCount() {
        Object count = metadata.get("monitoredFilesCount");
        return count instanceof Integer ? (Integer) count : 0;
    }
    
    /**
     * Gets the status (for compatibility with AdvancedHotReloadManager).
     * 
     * @return this status
     */
    public HotReloadStatus getStatus() {
        return this;
    }
    
    @Override
    public String toString() {
        return "HotReloadStatus{" +
                "className='" + className + '\'' +
                ", success=" + success +
                ", message='" + message + '\'' +
                ", startTime=" + startTime +
                ", endTime=" + endTime +
                ", duration=" + getDuration() +
                ", hasError=" + hasError() +
                '}';
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        
        HotReloadStatus that = (HotReloadStatus) o;
        
        if (success != that.success) return false;
        if (startTime != that.startTime) return false;
        if (endTime != that.endTime) return false;
        if (className != null ? !className.equals(that.className) : that.className != null) return false;
        if (message != null ? !message.equals(that.message) : that.message != null) return false;
        return error != null ? error.equals(that.error) : that.error == null;
    }
    
    @Override
    public int hashCode() {
        int result = className != null ? className.hashCode() : 0;
        result = 31 * result + (success ? 1 : 0);
        result = 31 * result + (message != null ? message.hashCode() : 0);
        result = 31 * result + (int) (startTime ^ (startTime >>> 32));
        result = 31 * result + (int) (endTime ^ (endTime >>> 32));
        result = 31 * result + (error != null ? error.hashCode() : 0);
        return result;
    }
}