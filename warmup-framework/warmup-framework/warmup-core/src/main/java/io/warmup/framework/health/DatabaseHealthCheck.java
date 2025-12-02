package io.warmup.framework.health;

import java.sql.Connection;
import java.sql.DriverManager;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class DatabaseHealthCheck implements HealthCheck {

    private final String url;
    private final String username;
    private final String password;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    public DatabaseHealthCheck(String url, String username, String password) {
        this.url = url;
        this.username = username;
        this.password = password;
    }

    @Override
    public HealthResult check() {
        try {
            Future<HealthResult> future = executor.submit(() -> {
                try (Connection connection = DriverManager.getConnection(url, username, password)) {
                    boolean isValid = connection.isValid(2); // 2 second timeout

                    Map<String, Object> details = new HashMap<>();
                    details.put("url", url);
                    details.put("isValid", isValid);
                    details.put("catalog", connection.getCatalog());

                    return isValid
                            ? HealthResult.up("Database connection is valid").withDetail("database", details)
                            : HealthResult.degraded("Database connection is invalid").withDetail("database", details);
                }
            });

            return future.get(getTimeout(), TimeUnit.MILLISECONDS);
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            return HealthResult.down("Database health check failed: " + e.getMessage(), e);
        }
    }

    @Override
    public String getName() {
        return "database";
    }

    @Override
    public long getTimeout() {
        return 1000; // 1 seconds for database checks
    }

    public void shutdown() {
        executor.shutdown();
    }
}
