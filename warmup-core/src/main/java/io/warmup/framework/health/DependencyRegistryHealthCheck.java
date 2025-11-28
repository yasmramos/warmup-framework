package io.warmup.framework.health;

import io.warmup.framework.core.DependencyRegistry;

/**
 * Health check for DependencyRegistry
 */
public class DependencyRegistryHealthCheck implements HealthCheck {
    
    private final DependencyRegistry dependencyRegistry;
    
    public DependencyRegistryHealthCheck(DependencyRegistry dependencyRegistry) {
        this.dependencyRegistry = dependencyRegistry;
    }
    
    @Override
    public HealthResult check() {
        try {
            if (dependencyRegistry != null) {
                return HealthResult.up("DependencyRegistry is healthy");
            } else {
                return HealthResult.down("DependencyRegistry is null");
            }
        } catch (Exception e) {
            return HealthResult.down("DependencyRegistry health check failed: " + e.getMessage());
        }
    }
    
    @Override
    public String getName() {
        return "DependencyRegistryHealthCheck";
    }
}