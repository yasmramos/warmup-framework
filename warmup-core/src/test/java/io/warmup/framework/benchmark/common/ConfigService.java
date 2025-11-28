package io.warmup.framework.benchmark.common;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;

/**
 * Servicio de configuración para benchmarks
 */
@Singleton
public class ConfigService {
    
    private final String appName;
    private final int maxConnections;
    private final boolean debugMode;
    
    @Inject
    public ConfigService(
        @ConfigValue("app.name") String appName,
        @ConfigValue("app.max.connections") int maxConnections,
        @ConfigValue("app.debug.mode") boolean debugMode
    ) {
        this.appName = appName;
        this.maxConnections = maxConnections;
        this.debugMode = debugMode;
    }
    
    public String getValue(String key) {
        switch (key) {
            case "app.name": return appName;
            case "app.max.connections": return String.valueOf(maxConnections);
            case "app.debug.mode": return String.valueOf(debugMode);
            default: return "unknown";
        }
    }
    
    public String getAppName() { return appName; }
    public int getMaxConnections() { return maxConnections; }
    public boolean isDebugMode() { return debugMode; }
}