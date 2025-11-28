package io.warmup.framework.examples.services;

/**
 * Servicio de monitoreo básico.
 */
public class BasicMonitoringService implements MonitoringService {
    @Override
    public void startMonitoring() {
        System.out.println("🔍 Basic Monitoring: Starting basic monitoring");
    }
    
    @Override
    public void stopMonitoring() {
        System.out.println("🔍 Basic Monitoring: Stopping monitoring");
    }
    
    @Override
    public String getStatus() {
        return "Basic Monitoring Service - Simple health checks";
    }
}