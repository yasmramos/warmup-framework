package io.warmup.framework.health;

import java.util.HashMap;
import java.util.Map;

public class HealthResult {

    private final HealthStatus status;
    private final String message;
    private final Map<String, Object> details;
    private final Throwable error;
    private final long timestamp;

    public HealthResult(HealthStatus status, String message) {
        this(status, message, new HashMap<>(), null);
    }

    public HealthResult(HealthStatus status, String message, Map<String, Object> details) {
        this(status, message, details, null);
    }

    public HealthResult(HealthStatus status, String message, Map<String, Object> details, Throwable error) {
        this.status = status;
        this.message = message;
        this.details = new HashMap<>(details);
        this.error = error;
        this.timestamp = System.currentTimeMillis();
    }

    // Factory methods
    public static HealthResult up() {
        return new HealthResult(HealthStatus.UP, "Service is healthy");
    }

    public static HealthResult up(String message) {
        return new HealthResult(HealthStatus.UP, message);
    }

    public static HealthResult up(String message, Map<String, Object> details) {
        return new HealthResult(HealthStatus.UP, message, details);
    }

    public static HealthResult down(String message) {
        return new HealthResult(HealthStatus.DOWN, message);
    }

    public static HealthResult down(String message, Throwable error) {
        return new HealthResult(HealthStatus.DOWN, message, new HashMap<>(), error);
    }

    public static HealthResult degraded(String message) {
        return new HealthResult(HealthStatus.DEGRADED, message);
    }

    public static HealthResult degraded(String message, Map<String, Object> details) {
        return new HealthResult(HealthStatus.DEGRADED, message, details);
    }

    public static HealthResult unknown(String message) {
        return new HealthResult(HealthStatus.UNKNOWN, message);
    }

    public HealthStatus getStatus() {
        return status;
    }

    public String getMessage() {
        return message;
    }

    public Map<String, Object> getDetails() {
        return new HashMap<>(details);
    }

    public Throwable getError() {
        return error;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public boolean isHealthy() {
        return status.isHealthy();
    }

    public HealthResult withDetail(String key, Object value) {
        this.details.put(key, value);
        return this;
    }

    public HealthResult withError(Throwable error) {
        return new HealthResult(status, message, details, error);
    }
}
