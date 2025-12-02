package io.warmup.framework.health;

public enum HealthStatus {
    UP("UP", "Service is healthy"),
    DOWN("DOWN", "Service is unavailable"),
    UNKNOWN("UNKNOWN", "Service status is unknown"),
    DEGRADED("DEGRADED", "Service is working but with degraded performance");
    
    private final String code;
    private final String description;
    
    HealthStatus(String code, String description) {
        this.code = code;
        this.description = description;
    }
    
    public String getCode() { return code; }
    public String getDescription() { return description; }
    
    public boolean isHealthy() {
        return this == UP || this == DEGRADED;
    }
}