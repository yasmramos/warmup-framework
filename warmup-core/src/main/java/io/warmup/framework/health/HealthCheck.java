package io.warmup.framework.health;

public interface HealthCheck {

    HealthResult check();

    default String getName() {
        return this.getClass().getSimpleName();
    }

    default long getTimeout() {
        return 5000; // 5 seconds default timeout
    }
}
