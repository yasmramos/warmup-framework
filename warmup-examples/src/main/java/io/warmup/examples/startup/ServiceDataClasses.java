package io.warmup.examples.startup;

import java.util.List;
import java.util.Map;

/**
 * Data classes for startup services
 */
public class ServiceDataClasses {
    
    public static class ServiceDefinition {
        private final String serviceId;
        private final String serviceName;
        private final Class<?> serviceClass;
        private final Map<String, Object> serviceConfig;
        
        public ServiceDefinition(String serviceId, String serviceName, Class<?> serviceClass, Map<String, Object> serviceConfig) {
            this.serviceId = serviceId;
            this.serviceName = serviceName;
            this.serviceClass = serviceClass;
            this.serviceConfig = serviceConfig;
        }
        
        public String getServiceId() { return serviceId; }
        public String getServiceName() { return serviceName; }
        public Class<?> getServiceClass() { return serviceClass; }
        public Map<String, Object> getServiceConfig() { return serviceConfig; }
    }
    
    public static class ServiceInfo {
        private final String serviceId;
        private final String serviceName;
        private final ServiceState serviceState;
        private final long startupTime;
        
        public ServiceInfo(String serviceId, String serviceName, ServiceState serviceState, long startupTime) {
            this.serviceId = serviceId;
            this.serviceName = serviceName;
            this.serviceState = serviceState;
            this.startupTime = startupTime;
        }
        
        public String getServiceId() { return serviceId; }
        public String getServiceName() { return serviceName; }
        public ServiceState getServiceState() { return serviceState; }
        public long getStartupTime() { return startupTime; }
    }
    
    public enum ServiceState {
        PENDING, INITIALIZING, RUNNING, STOPPED, FAILED
    }
}