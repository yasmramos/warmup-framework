package io.warmup.framework.cache;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

class CacheHealth {

    public final boolean healthy;
    public final List<String> warnings;
    public final List<String> errors;
    public final Map<String, Object> metrics;

    public CacheHealth(boolean healthy, List<String> warnings, List<String> errors, Map<String, Object> metrics) {
        this.healthy = healthy;
        this.warnings = Collections.unmodifiableList(new ArrayList<>(warnings));
        this.errors = Collections.unmodifiableList(new ArrayList<>(errors));
        this.metrics = Collections.unmodifiableMap(new HashMap<>(metrics));
    }

    public boolean isHealthy() {
        return healthy && errors.isEmpty();
    }

    public void printReport() {
        System.out.println("Cache Health Report:");
        System.out.println("Overall Status: " + (isHealthy() ? "HEALTHY" : "UNHEALTHY"));

        if (!errors.isEmpty()) {
            System.out.println("\nErrors:");
            errors.forEach(error -> System.out.println("  " + error));
        }

        if (!warnings.isEmpty()) {
            System.out.println("\nWarnings:");
            warnings.forEach(warning -> System.out.println("  " + warning));
        }

        if (!metrics.isEmpty()) {
            System.out.println("\nMetrics:");
            metrics.forEach((key, value) -> System.out.println("  " + key + ": " + value));
        }
    }
}