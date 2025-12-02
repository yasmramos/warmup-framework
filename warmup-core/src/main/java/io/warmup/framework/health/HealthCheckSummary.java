package io.warmup.framework.health;

import java.util.Map;

public class HealthCheckSummary {

    private final long totalChecks;
    private final long healthyChecks;
    private final long unhealthyChecks;
    private final Map<String, HealthResult> details;
    private final double healthPercentage;

    public HealthCheckSummary(long total, long healthy, long unhealthy,
            Map<String, HealthResult> details) {
        this.totalChecks = total;
        this.healthyChecks = healthy;
        this.unhealthyChecks = unhealthy;
        this.details = details;
        this.healthPercentage = total > 0 ? (healthy * 100.0) / total : 100.0;
    }

    public boolean isFullyHealthy() {
        return unhealthyChecks == 0;
    }

    public boolean isPartiallyHealthy() {
        return healthyChecks > 0;
    }

    public String getHealthStatus() {
        return isFullyHealthy() ? "HEALTHY"
                : isPartiallyHealthy() ? "DEGRADED" : "DOWN";
    }
}
