package io.warmup.framework.health;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class ResilientHealthCheck implements HealthCheck {

    private final HealthCheck delegate;
    private final int failureThreshold;
    private final long resetTimeout;

    private int consecutiveFailures = 0;
    private long lastFailureTime = 0;
    private HealthStatus forcedStatus = null;

    public ResilientHealthCheck(HealthCheck delegate, int failureThreshold, long resetTimeout) {
        this.delegate = delegate;
        this.failureThreshold = failureThreshold;
        this.resetTimeout = resetTimeout;
    }

    @Override
    public HealthResult check() {
        if (forcedStatus != null) {
            if (System.currentTimeMillis() - lastFailureTime > resetTimeout) {
                forcedStatus = null;
                consecutiveFailures = 0;
            } else {
                // ✅ Usar factory method
                return HealthResult.down("Circuit breaker active")
                        .withDetail("forcedStatus", forcedStatus)
                        .withDetail("lastFailureTime", new Date(lastFailureTime));
            }
        }

        try {
            HealthResult result = delegate.check();

            if (result.isHealthy()) {
                consecutiveFailures = 0;
                forcedStatus = null;
            } else {
                consecutiveFailures++;
                if (consecutiveFailures >= failureThreshold) {
                    forcedStatus = HealthStatus.DOWN;
                    lastFailureTime = System.currentTimeMillis();
                    return new HealthResult(forcedStatus,
                            "Circuit breaker triggered after " + consecutiveFailures + " failures");
                }
            }

            return result;
        } catch (Exception e) {
            consecutiveFailures++;
            if (consecutiveFailures >= failureThreshold) {
                forcedStatus = HealthStatus.DOWN;
                lastFailureTime = System.currentTimeMillis();

                Map<String, Object> details = new HashMap<>();
                details.put("exception", e.getMessage());
                details.put("exceptionType", e.getClass().getSimpleName());
                details.put("failureCount", consecutiveFailures);

                return new HealthResult(forcedStatus,
                        "Circuit breaker triggered after " + consecutiveFailures + " failures", details, e);
            }
            // ✅ Para fallas que no activan el circuit breaker
            Map<String, Object> details = new HashMap<>();
            details.put("exception", e.getMessage());
            details.put("failureCount", consecutiveFailures);

            return new HealthResult(HealthStatus.DOWN,
                    "Health check failed: " + e.getMessage(),
                    details, e);
        }
    }
}
