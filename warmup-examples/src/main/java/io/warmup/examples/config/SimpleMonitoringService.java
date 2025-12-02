package io.warmup.examples.config;

/**
 * Simple monitoring service for conditional property examples.
 */
public interface SimpleMonitoringService {
    void startMonitoring();
    void stopMonitoring();
    boolean isMonitoring();
}