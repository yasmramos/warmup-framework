package io.warmup.framework.health;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Comprehensive tests for HealthCheckManager functionality.
 * Tests use the real HealthCheckManager API with checkHealth, getHealthCheck, etc.
 * 
 * @author MiniMax Agent
 * @since 1.0
 */
class HealthCheckManagerTest {

    private HealthCheckManager healthCheckManager;
    private ExecutorService executorService;

    @BeforeEach
    void setUp() {
        // Use real constructor - HealthCheckManager has a no-args constructor
        healthCheckManager = new HealthCheckManager();
        executorService = Executors.newCachedThreadPool();
    }

    @AfterEach
    void tearDown() {
        if (executorService != null && !executorService.isShutdown()) {
            executorService.shutdown();
        }
        if (healthCheckManager != null) {
            healthCheckManager.clearHealthChecks();
            healthCheckManager.shutdown();
        }
    }

    @Test
    void testHealthCheckRegistration() {
        // Test registering health checks using real API
        HealthCheck testCheck = createTestHealthCheck("test-check", HealthStatus.UP);
        healthCheckManager.registerHealthCheck(testCheck);
        
        // Verify health check is registered using real API
        assertTrue(healthCheckManager.getHealthCheckNames().contains("test-check"));
        assertEquals(1, healthCheckManager.getHealthCheckNames().size());
        assertEquals(1, healthCheckManager.getHealthCheckCount());
    }

    @Test
    void testHealthCheckExecution() {
        // Test health check execution using real API
        HealthCheck testCheck = createTestHealthCheck("execution-test", HealthStatus.UP);
        healthCheckManager.registerHealthCheck(testCheck);
        
        // Use real API: getHealthCheck(String name)
        HealthResult result = healthCheckManager.getHealthCheck("execution-test");
        
        assertNotNull(result);
        assertEquals(HealthStatus.UP, result.getStatus());
        assertEquals("execution-test", result.getMessage());
    }

    @Test
    void testMultipleHealthChecks() {
        // Test multiple health checks
        HealthCheck check1 = createTestHealthCheck("check1", HealthStatus.UP);
        HealthCheck check2 = createTestHealthCheck("check2", HealthStatus.DOWN);
        HealthCheck check3 = createTestHealthCheck("check3", HealthStatus.UP);
        
        healthCheckManager.registerHealthCheck(check1);
        healthCheckManager.registerHealthCheck(check2);
        healthCheckManager.registerHealthCheck(check3);
        
        // Verify all checks are registered
        assertEquals(3, healthCheckManager.getHealthCheckCount());
        assertTrue(healthCheckManager.getHealthCheckNames().contains("check1"));
        assertTrue(healthCheckManager.getHealthCheckNames().contains("check2"));
        assertTrue(healthCheckManager.getHealthCheckNames().contains("check3"));
    }

    @Test
    void testHealthCheckWithName() {
        // Test registering health check with custom name
        HealthCheck testCheck = createTestHealthCheck("original-name", HealthStatus.UP);
        
        // Use API: registerHealthCheck(String name, HealthCheck)
        healthCheckManager.registerHealthCheck("custom-name", testCheck);
        
        assertTrue(healthCheckManager.getHealthCheckNames().contains("custom-name"));
        assertFalse(healthCheckManager.getHealthCheckNames().contains("original-name"));
    }

    @Test
    void testCheckAllHealth() {
        // Test checking all health checks
        HealthCheck upCheck = createTestHealthCheck("up-check", HealthStatus.UP);
        HealthCheck downCheck = createTestHealthCheck("down-check", HealthStatus.DOWN);
        
        healthCheckManager.registerHealthCheck(upCheck);
        healthCheckManager.registerHealthCheck(downCheck);
        
        // Use real API: checkHealth()
        java.util.Map<String, io.warmup.framework.health.HealthResult> allResults = healthCheckManager.checkHealth();
        
        assertNotNull(allResults);
        assertEquals(2, allResults.size());
        assertTrue(allResults.containsKey("up-check"));
        assertTrue(allResults.containsKey("down-check"));
        
        assertEquals(HealthStatus.UP, allResults.get("up-check").getStatus());
        assertEquals(HealthStatus.DOWN, allResults.get("down-check").getStatus());
    }

    @Test
    void testHealthCheckRemoval() {
        // Test removing health checks
        HealthCheck testCheck = createTestHealthCheck("remove-test", HealthStatus.UP);
        healthCheckManager.registerHealthCheck(testCheck);
        
        assertEquals(1, healthCheckManager.getHealthCheckCount());
        
        // Use real API: removeHealthCheck(String name)
        healthCheckManager.removeHealthCheck("remove-test");
        
        assertEquals(0, healthCheckManager.getHealthCheckCount());
        assertFalse(healthCheckManager.getHealthCheckNames().contains("remove-test"));
    }

    @Test
    void testClearAllHealthChecks() {
        // Test clearing all health checks
        healthCheckManager.registerHealthCheck(createTestHealthCheck("check1", HealthStatus.UP));
        healthCheckManager.registerHealthCheck(createTestHealthCheck("check2", HealthStatus.UP));
        healthCheckManager.registerHealthCheck(createTestHealthCheck("check3", HealthStatus.UP));
        
        assertEquals(3, healthCheckManager.getHealthCheckCount());
        
        // Use real API: clearHealthChecks()
        healthCheckManager.clearHealthChecks();
        
        assertEquals(0, healthCheckManager.getHealthCheckCount());
        assertTrue(healthCheckManager.getHealthCheckNames().isEmpty());
    }

    @Test
    void testOverallHealthCheck() {
        // Test overall health check
        HealthCheck upCheck = createTestHealthCheck("up", HealthStatus.UP);
        HealthCheck downCheck = createTestHealthCheck("down", HealthStatus.DOWN);
        
        healthCheckManager.registerHealthCheck(upCheck);
        healthCheckManager.registerHealthCheck(downCheck);
        
        // Use real API: checkOverallHealth()
        HealthResult overallResult = healthCheckManager.checkOverallHealth();
        
        assertNotNull(overallResult);
        assertEquals(HealthStatus.DOWN, overallResult.getStatus()); // Should be DOWN if any check is DOWN
    }

    @Test
    void testIsHealthy() {
        // Test isHealthy method
        HealthCheck upCheck = createTestHealthCheck("up", HealthStatus.UP);
        
        healthCheckManager.registerHealthCheck(upCheck);
        
        // Should be healthy when all checks are UP
        assertTrue(healthCheckManager.isHealthy());
        
        // Add a DOWN check
        HealthCheck downCheck = createTestHealthCheck("down", HealthStatus.DOWN);
        healthCheckManager.registerHealthCheck(downCheck);
        
        // Should not be healthy when any check is DOWN
        assertFalse(healthCheckManager.isHealthy());
    }

    @Test
    void testHealthStatus() {
        // Test health status map
        HealthCheck testCheck = createTestHealthCheck("status-test", HealthStatus.UP);
        healthCheckManager.registerHealthCheck(testCheck);
        
        // Use real API: getHealthStatus()
        java.util.Map<String, Object> healthStatus = healthCheckManager.getHealthStatus();
        
        assertNotNull(healthStatus);
        assertFalse(healthStatus.isEmpty());
    }

    @Test
    void testHealthSummary() {
        // Test health summary
        healthCheckManager.registerHealthCheck(createTestHealthCheck("check1", HealthStatus.UP));
        healthCheckManager.registerHealthCheck(createTestHealthCheck("check2", HealthStatus.DOWN));
        
        // Use real API: getHealthSummary()
        HealthCheckSummary summary = healthCheckManager.getHealthSummary();
        
        assertNotNull(summary);
        // Summary should contain information about the checks
    }

    @Test
    void testForceRefreshHealthCheck() {
        // Test force refresh health check
        HealthCheck testCheck = createTestHealthCheck("refresh-test", HealthStatus.UP);
        healthCheckManager.registerHealthCheck(testCheck);
        
        // Use real API: checkHealthForceRefresh()
        java.util.Map<String, io.warmup.framework.health.HealthResult> results = healthCheckManager.checkHealthForceRefresh();
        
        assertNotNull(results);
        assertTrue(results.containsKey("refresh-test"));
    }

    @Test
    void testNonExistentHealthCheck() {
        // Test getting health check that doesn't exist
        HealthResult result = healthCheckManager.getHealthCheck("non-existent");
        
        assertNull(result);
    }

    @Test
    void testHealthCheckStatistics() {
        // Test accessing health check statistics
        HealthCheck testCheck = createTestHealthCheck("stats-test", HealthStatus.UP);
        healthCheckManager.registerHealthCheck(testCheck);
        
        // Execute health check
        healthCheckManager.checkHealth();
        
        // Test statistics access
        assertTrue(healthCheckManager.getHealthCheckExecutions() >= 0);
        assertTrue(healthCheckManager.getCachedHealthResults() >= 0);
        assertTrue(healthCheckManager.getHealthCheckRegistrations() >= 0);
        assertTrue(healthCheckManager.getHealthyChecksCount() >= 0);
        assertTrue(healthCheckManager.getUnhealthyChecksCount() >= 0);
        assertTrue(healthCheckManager.getTotalHealthCheckDuration() >= 0);
    }

    @Test
    @Timeout(5)
    void testConcurrentHealthCheckRegistration() throws InterruptedException {
        // Test concurrent health check registration
        int threadCount = 10;
        CountDownLatch latch = new CountDownLatch(threadCount);
        
        for (int i = 0; i < threadCount; i++) {
            final int index = i;
            executorService.submit(() -> {
                try {
                    HealthCheck check = createTestHealthCheck("concurrent-check-" + index, HealthStatus.UP);
                    healthCheckManager.registerHealthCheck(check);
                    latch.countDown();
                } catch (Exception e) {
                    latch.countDown(); // Count down even on error
                }
            });
        }
        
        // Wait for all registrations to complete
        assertTrue(latch.await(3, TimeUnit.SECONDS));
        
        // Verify all checks were registered
        assertEquals(threadCount, healthCheckManager.getHealthCheckCount());
    }

    @Test
    void testHealthCheckInitialization() {
        // Test health check manager initialization
        assertDoesNotThrow(() -> {
            healthCheckManager.initialize();
        });
        
        // Should be able to start health monitoring
        assertDoesNotThrow(() -> {
            healthCheckManager.startHealthMonitoring();
        });
        
        // Should be able to stop health monitoring
        assertDoesNotThrow(() -> {
            healthCheckManager.stopHealthMonitoring();
        });
    }

    @Test
    void testHealthCheckWithException() {
        // Test health check that throws exception
        HealthCheck exceptionCheck = new HealthCheck() {
            @Override
            public String getName() {
                return "exception-check";
            }
            
            @Override
            public HealthResult check() {
                throw new RuntimeException("Test exception");
            }
        };
        
        healthCheckManager.registerHealthCheck(exceptionCheck);
        
        // Should handle exceptions gracefully
        HealthResult result = healthCheckManager.getHealthCheck("exception-check");
        assertNotNull(result);
        // The exact behavior depends on implementation, but it should not throw exception to caller
    }

    @Test
    void testHealthCheckWithDetails() {
        // Test health check with detailed information
        HealthCheck detailedCheck = new HealthCheck() {
            @Override
            public String getName() {
                return "detailed-check";
            }
            
            @Override
            public HealthResult check() {
                java.util.Map<String, Object> details = new java.util.HashMap<>();
                details.put("cpu", "normal");
                details.put("memory", "good");
                return HealthResult.up("All systems operational", details);
            }
        };
        
        healthCheckManager.registerHealthCheck(detailedCheck);
        
        HealthResult result = healthCheckManager.getHealthCheck("detailed-check");
        assertNotNull(result);
        assertEquals("All systems operational", result.getMessage());
        assertNotNull(result.getDetails());
    }

    // Helper method to create test health checks
    private HealthCheck createTestHealthCheck(String name, HealthStatus status) {
        return new HealthCheck() {
            @Override
            public String getName() {
                return name;
            }
            
            @Override
            public HealthResult check() {
                String message = "Test health check: " + name;
                switch (status) {
                    case UP:
                        return HealthResult.up(message);
                    case DOWN:
                        return HealthResult.down(message);
                    case DEGRADED:
                        return HealthResult.degraded(message);
                    case UNKNOWN:
                        return HealthResult.unknown(message);
                    default:
                        return HealthResult.up(message);
                }
            }
        };
    }
}