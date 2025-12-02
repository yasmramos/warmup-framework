package io.warmup.framework.hotreload.advanced;

import java.nio.file.Path;
import java.nio.file.WatchEvent;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * Configuration for bytecode change detection.
 * Controls how file changes are monitored and classified for hot reload operations.
 * 
 * @author MiniMax Agent
 * @since 2.0
 */
public class ChangeDetectionConfig {
    
    private boolean enabled = true;
    private Set<Path> monitoredDirectories;
    private Set<String> fileExtensions = java.util.Collections.singleton(".java");
    private long detectionIntervalMs = 1000;
    private boolean recursiveMonitoring = true;
    private boolean ignoreHiddenFiles = true;
    private Set<WatchEvent.Kind<Path>> watchEvents = java.util.Collections.singleton(
        java.nio.file.StandardWatchEventKinds.ENTRY_MODIFY
    );
    private long fileStabilityTimeoutMs = 500;
    private int maxConcurrentDetections = 10;
    private boolean checksumValidation = true;
    private boolean byteLevelComparison = false;
    private TimeUnit timeoutUnit = TimeUnit.MILLISECONDS;
    
    public ChangeDetectionConfig() {
        // Default configuration
    }
    
    public ChangeDetectionConfig(boolean enabled, long detectionIntervalMs) {
        this.enabled = enabled;
        this.detectionIntervalMs = detectionIntervalMs;
    }
    
    /**
     * Checks if change detection is enabled.
     * 
     * @return true if enabled
     */
    public boolean isEnabled() {
        return enabled;
    }
    
    /**
     * Sets whether change detection is enabled.
     * 
     * @param enabled the enabled flag
     * @return this config
     */
    public ChangeDetectionConfig setEnabled(boolean enabled) {
        this.enabled = enabled;
        return this;
    }
    
    /**
     * Gets monitored directories.
     * 
     * @return monitored directories
     */
    public Set<Path> getMonitoredDirectories() {
        return monitoredDirectories;
    }
    
    /**
     * Sets monitored directories.
     * 
     * @param monitoredDirectories the directories to monitor
     * @return this config
     */
    public ChangeDetectionConfig setMonitoredDirectories(Set<Path> monitoredDirectories) {
        this.monitoredDirectories = monitoredDirectories;
        return this;
    }
    
    /**
     * Gets file extensions to monitor.
     * 
     * @return file extensions
     */
    public Set<String> getFileExtensions() {
        return fileExtensions;
    }
    
    /**
     * Sets file extensions to monitor.
     * 
     * @param fileExtensions the file extensions
     * @return this config
     */
    public ChangeDetectionConfig setFileExtensions(Set<String> fileExtensions) {
        this.fileExtensions = fileExtensions;
        return this;
    }
    
    /**
     * Gets detection interval.
     * 
     * @return detection interval in milliseconds
     */
    public long getDetectionIntervalMs() {
        return detectionIntervalMs;
    }
    
    /**
     * Sets detection interval.
     * 
     * @param detectionIntervalMs the interval in milliseconds
     * @return this config
     */
    public ChangeDetectionConfig setDetectionIntervalMs(long detectionIntervalMs) {
        this.detectionIntervalMs = detectionIntervalMs;
        return this;
    }
    
    /**
     * Checks if recursive monitoring is enabled.
     * 
     * @return true if recursive monitoring enabled
     */
    public boolean isRecursiveMonitoring() {
        return recursiveMonitoring;
    }
    
    /**
     * Sets whether recursive monitoring is enabled.
     * 
     * @param recursiveMonitoring the recursive flag
     * @return this config
     */
    public ChangeDetectionConfig setRecursiveMonitoring(boolean recursiveMonitoring) {
        this.recursiveMonitoring = recursiveMonitoring;
        return this;
    }
    
    /**
     * Checks if hidden files are ignored.
     * 
     * @return true if ignore hidden files
     */
    public boolean isIgnoreHiddenFiles() {
        return ignoreHiddenFiles;
    }
    
    /**
     * Sets whether hidden files are ignored.
     * 
     * @param ignoreHiddenFiles the ignore flag
     * @return this config
     */
    public ChangeDetectionConfig setIgnoreHiddenFiles(boolean ignoreHiddenFiles) {
        this.ignoreHiddenFiles = ignoreHiddenFiles;
        return this;
    }
    
    /**
     * Gets watch events.
     * 
     * @return watch events
     */
    public Set<WatchEvent.Kind<Path>> getWatchEvents() {
        return watchEvents;
    }
    
    /**
     * Sets watch events.
     * 
     * @param watchEvents the watch events
     * @return this config
     */
    public ChangeDetectionConfig setWatchEvents(Set<WatchEvent.Kind<Path>> watchEvents) {
        this.watchEvents = watchEvents;
        return this;
    }
    
    /**
     * Gets file stability timeout.
     * 
     * @return stability timeout in milliseconds
     */
    public long getFileStabilityTimeoutMs() {
        return fileStabilityTimeoutMs;
    }
    
    /**
     * Sets file stability timeout.
     * 
     * @param fileStabilityTimeoutMs the timeout in milliseconds
     * @return this config
     */
    public ChangeDetectionConfig setFileStabilityTimeoutMs(long fileStabilityTimeoutMs) {
        this.fileStabilityTimeoutMs = fileStabilityTimeoutMs;
        return this;
    }
    
    /**
     * Gets maximum concurrent detections.
     * 
     * @return max concurrent detections
     */
    public int getMaxConcurrentDetections() {
        return maxConcurrentDetections;
    }
    
    /**
     * Sets maximum concurrent detections.
     * 
     * @param maxConcurrentDetections the max concurrent detections
     * @return this config
     */
    public ChangeDetectionConfig setMaxConcurrentDetections(int maxConcurrentDetections) {
        this.maxConcurrentDetections = maxConcurrentDetections;
        return this;
    }
    
    /**
     * Checks if checksum validation is enabled.
     * 
     * @return true if checksum validation enabled
     */
    public boolean isChecksumValidation() {
        return checksumValidation;
    }
    
    /**
     * Sets whether checksum validation is enabled.
     * 
     * @param checksumValidation the checksum validation flag
     * @return this config
     */
    public ChangeDetectionConfig setChecksumValidation(boolean checksumValidation) {
        this.checksumValidation = checksumValidation;
        return this;
    }
    
    /**
     * Checks if byte-level comparison is enabled.
     * 
     * @return true if byte-level comparison enabled
     */
    public boolean isByteLevelComparison() {
        return byteLevelComparison;
    }
    
    /**
     * Sets whether byte-level comparison is enabled.
     * 
     * @param byteLevelComparison the byte-level comparison flag
     * @return this config
     */
    public ChangeDetectionConfig setByteLevelComparison(boolean byteLevelComparison) {
        this.byteLevelComparison = byteLevelComparison;
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
    public ChangeDetectionConfig setTimeoutUnit(TimeUnit timeoutUnit) {
        this.timeoutUnit = timeoutUnit;
        return this;
    }
    
    /**
     * Gets detection interval in milliseconds.
     * 
     * @return detection interval in milliseconds
     */
    public long getDetectionIntervalInMillis() {
        return timeoutUnit.toMillis(detectionIntervalMs);
    }
    
    /**
     * Gets file stability timeout in milliseconds.
     * 
     * @return stability timeout in milliseconds
     */
    public long getFileStabilityTimeoutInMillis() {
        return timeoutUnit.toMillis(fileStabilityTimeoutMs);
    }
    
    /**
     * Checks if a file should be monitored based on this configuration.
     * 
     * @param filePath the file path
     * @return true if the file should be monitored
     */
    public boolean shouldMonitorFile(Path filePath) {
        if (!enabled) {
            return false;
        }
        
        if (filePath == null) {
            return false;
        }
        
        // Check file extension
        String fileName = filePath.getFileName().toString();
        boolean hasValidExtension = fileExtensions.stream()
            .anyMatch(fileName::endsWith);
        
        if (!hasValidExtension) {
            return false;
        }
        
        // Check if hidden file should be ignored
        if (ignoreHiddenFiles && fileName.startsWith(".")) {
            return false;
        }
        
        return true;
    }
    
    @Override
    public String toString() {
        return String.format(
            "ChangeDetectionConfig{enabled=%s, interval=%dms, recursive=%s, extensions=%s}",
            enabled,
            detectionIntervalMs,
            recursiveMonitoring,
            fileExtensions
        );
    }
}