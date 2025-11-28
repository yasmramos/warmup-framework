package io.warmup.framework.examples.services;

public interface MonitoringService {
    void startMonitoring();
    void stopMonitoring();
    String getStatus();
}