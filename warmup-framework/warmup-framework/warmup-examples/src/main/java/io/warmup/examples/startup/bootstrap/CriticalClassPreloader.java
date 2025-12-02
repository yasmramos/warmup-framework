/*
 * Copyright (c) 2025 Warmup Framework. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.warmup.examples.startup.bootstrap;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Logger;

/**
 * Critical Class Preloader - Main coordinator for JVM bootstrap class preloading.
 * This class orchestrates the entire process of preloading the 20-30 most
 * performance-critical classes to eliminate class loading overhead during runtime.
 * 
 * Key Features:
 * - Dependency-aware class loading with 3-tier priority system
 * - Parallel execution across all available CPU cores
 * - Advanced metrics and performance tracking
 * - Memory optimization and cache management
 * - Integration with existing Warmup Framework systems
 * - Comprehensive error handling and recovery
 * 
 * Architecture:
 * 1. Validation Phase: Verify class registry and dependencies
 * 2. Preload Phase: Execute parallel class loading with metrics
 * 3. Verification Phase: Ensure all critical classes are available
 * 4. Optimization Phase: Provide recommendations and insights
 * 
 * @author Warmup Framework Team
 * @version 1.0.0
 */
public final class CriticalClassPreloader {
    
    private static final Logger logger = Logger.getLogger(CriticalClassPreloader.class.getName());
    
    // Core components
    private final BootstrapClassLoader bootstrapLoader;
    private final ClassPreloadMetrics metrics;
    
    // Configuration and state
    private final PreloadConfiguration configuration;
    private final AtomicReference<PreloadState> currentState = new AtomicReference<>(PreloadState.IDLE);
    
    // Preload results cache
    private final Map<String, PreloadResult> cachedResults = new ConcurrentHashMap<>();
    
    // Thread safety
    private volatile boolean initialized = false;
    
    /**
     * Creates a new CriticalClassPreloader with default configuration.
     */
    public CriticalClassPreloader() {
        this(PreloadConfiguration.defaultConfiguration());
    }
    
    /**
     * Creates a new CriticalClassPreloader with custom configuration.
     * 
     * @param configuration Custom preload configuration
     */
    public CriticalClassPreloader(PreloadConfiguration configuration) {
        this.configuration = configuration;
        this.bootstrapLoader = new BootstrapClassLoader(getClass().getClassLoader());
        this.metrics = new ClassPreloadMetrics();
        
        logger.info("CriticalClassPreloader initialized with custom configuration");
    }
    
    /**
     * Executes a complete critical class preloading operation.
 * This is the main entry point for bootstrap class preloading.
 * 
 * @return CompletableFuture containing the preload result
 */
    public CompletableFuture<PreloadResult> preloadCriticalClasses() {
        return CompletableFuture.supplyAsync(() -> {
            try {
                // Validate preconditions
                validatePreconditions();
                
                // Update state
                updateState(PreloadState.VALIDATING);
                
                // Validation phase
                ValidationResult validation = performValidation();
                if (!validation.isValid()) {
                    throw new PreloadException("Validation failed: " + validation.getErrorMessage());
                }
                
                // Start metrics tracking
                String sessionId = generateSessionId();
                int totalClasses = CriticalClassRegistry.getTotalCriticalClassCount();
                metrics.startPreloadSession(sessionId, totalClasses);
                
                updateState(PreloadState.PRELOADING);
                
                // Execute preload
                PreloadResult result = executePreload();
                
                // Verification phase
                updateState(PreloadState.VERIFYING);
                VerificationResult verification = performVerification(result);
                result.setVerificationResult(verification);
                
                // Finalize metrics
                updateState(PreloadState.FINALIZING);
                ClassPreloadMetrics.PreloadMetricsReport finalReport = metrics.finalizeSession();
                result.setMetricsReport(finalReport);
                
                // Cache result
                cachedResults.put(sessionId, result);
                
                // Update state
                updateState(PreloadState.COMPLETED);
                
                logger.info(String.format("Critical class preloading completed successfully: %s", result));
                
                return result;
                
            } catch (Exception e) {
                updateState(PreloadState.ERROR);
                logger.severe(String.format("Critical class preloading failed: %s", e.getMessage()));
                throw new PreloadException("Failed to preload critical classes", e);
            }
        });
    }
    
    /**
     * Executes the actual preload operation with parallel processing.
 * 
 * @return PreloadResult containing detailed results
 */
    private PreloadResult executePreload() {
        long startTime = System.nanoTime();
        
        // Get dependency-ordered class list
        List<String> criticalClasses = CriticalClassRegistry.getDependencyOrderedClasses();
        
        logger.info(String.format("Starting preload of %d critical classes", criticalClasses.size()));
        
        // Record tier start for each tier
        for (int tier = 1; tier <= 3; tier++) {
            Set<String> tierClasses = CriticalClassRegistry.getClassesByTier(tier);
            if (!tierClasses.isEmpty()) {
                metrics.recordTierStart(tier, tierClasses.size());
            }
        }
        
        // Execute parallel preload
        BootstrapClassLoader.PreloadResult preloadResult = bootstrapLoader.preloadAllCriticalClasses().join();
        
        long endTime = System.nanoTime();
        long totalTimeNanos = endTime - startTime;
        
        // Build comprehensive result
        PreloadResult result = new PreloadResult();
        result.setSessionId(generateSessionId());
        result.setStartTime(System.currentTimeMillis());
        result.setEndTime(System.currentTimeMillis());
        result.setTotalTimeMs(totalTimeNanos / 1_000_000);
        result.setTotalClasses(preloadResult.getLoadedCount() + preloadResult.getFailedCount());
        result.setSuccessfullyLoaded(preloadResult.getLoadedCount());
        result.setFailedLoads(preloadResult.getFailedCount());
        result.setSuccessRate(preloadResult.getSuccessRate());
        result.setLoadedClasses(preloadResult.getLoadedClasses());
        result.setFailedClasses(preloadResult.getFailedClasses());
        
        // Add performance insights
        Map<String, Object> bootstrapStats = bootstrapLoader.getPreloadStatistics();
        result.setPerformanceInsights(bootstrapStats);
        
        return result;
    }
    
    /**
     * Validates all preconditions before starting preloading.
 * 
 * @throws PreloadException if validation fails
 */
    private void validatePreconditions() throws PreloadException {
        // Check if already initialized
        if (!initialized) {
            initialize();
        }
        
        // Validate configuration
        if (configuration == null) {
            throw new PreloadException("Configuration is null");
        }
        
        // Check if preloading is enabled
        if (!configuration.isEnabled()) {
            throw new PreloadException("Critical class preloading is disabled in configuration");
        }
        
        // Validate class registry
        Map<String, Object> registryStats = CriticalClassRegistry.getRegistryStatistics();
        int totalClasses = (Integer) registryStats.get("totalCount");
        
        if (totalClasses == 0) {
            throw new PreloadException("No critical classes found in registry");
        }
        
        if (totalClasses < configuration.getMinClassCount()) {
            throw new PreloadException(String.format("Insufficient critical classes: %d < %d minimum required", 
                    totalClasses, configuration.getMinClassCount()));
        }
        
        // Validate critical classes can be loaded
        Map<String, Boolean> validationResults = CriticalClassRegistry.validateCriticalClasses();
        long failedValidations = validationResults.values().stream().filter(success -> !success).count();
        
        if (failedValidations > 0) {
            long validationFailureRate = (failedValidations * 100) / validationResults.size();
            if (validationFailureRate > configuration.getMaxValidationFailureRate()) {
                throw new PreloadException(String.format("Too many classes failed validation: %.1f%% > %.1f%% maximum", 
                        validationFailureRate, configuration.getMaxValidationFailureRate()));
            }
            
            logger.warning(String.format("Some classes failed validation: %d/%d classes", 
                    failedValidations, validationResults.size()));
        }
        
        logger.info("Precondition validation completed successfully");
    }
    
    /**
     * Performs validation of the class registry and dependencies.
 * 
 * @return ValidationResult containing validation status
 */
    private ValidationResult performValidation() {
        ValidationResult result = new ValidationResult();
        
        try {
            // Check registry statistics
            Map<String, Object> stats = CriticalClassRegistry.getRegistryStatistics();
            int tier1Count = (Integer) stats.get("tier1Count");
            int tier2Count = (Integer) stats.get("tier2Count");
            int tier3Count = (Integer) stats.get("tier3Count");
            int totalCount = (Integer) stats.get("totalCount");
            
            result.setRegistryStats(stats);
            
            // Validate tier distribution
            if (tier1Count == 0) {
                result.addIssue("No Tier 1 (absolute critical) classes found");
            }
            
            if (tier2Count == 0) {
                result.addIssue("No Tier 2 (high priority) classes found");
            }
            
            if (totalCount < 10) {
                result.addIssue("Very few critical classes (" + totalCount + "), consider expanding registry");
            }
            
            // Validate class loading
            Map<String, Boolean> loadability = CriticalClassRegistry.validateCriticalClasses();
            long loadableCount = loadability.values().stream().mapToInt(success -> success ? 1 : 0).sum();
            double loadabilityRate = (loadableCount * 100.0) / loadability.size();
            
            result.setLoadabilityRate(loadabilityRate);
            result.setLoadabilityResults(loadability);
            
            if (loadabilityRate < 95.0) {
                result.addIssue(String.format("Low loadability rate: %.1f%%", loadabilityRate));
            }
            
            // Dependency analysis
            List<String> dependencyOrder = CriticalClassRegistry.getDependencyOrderedClasses();
            result.setDependencyOrder(dependencyOrder);
            
            if (dependencyOrder.size() != totalCount) {
                result.addIssue("Dependency order size doesn't match total class count");
            }
            
            // Performance validation
            if (totalCount > configuration.getMaxClassCount()) {
                result.addIssue(String.format("Too many critical classes: %d > %d maximum", 
                        totalCount, configuration.getMaxClassCount()));
            }
            
            result.setValid(result.getIssues().isEmpty());
            
        } catch (Exception e) {
            result.setValid(false);
            result.addIssue("Exception during validation: " + e.getMessage());
        }
        
        return result;
    }
    
    /**
 * Performs verification that critical classes are properly loaded.
 * 
 * @param preloadResult The preload result to verify
 * @return VerificationResult containing verification status
 */
    private VerificationResult performVerification(PreloadResult preloadResult) {
        VerificationResult result = new VerificationResult();
        
        try {
            // Verify loaded classes
            Set<String> loadedClasses = preloadResult.getLoadedClasses();
            for (String className : loadedClasses) {
                try {
                    Class.forName(className);
                    result.recordSuccessfulVerification(className);
                } catch (ClassNotFoundException | NoClassDefFoundError e) {
                    result.recordFailedVerification(className, e.getMessage());
                }
            }
            
            // Verify system classes
            Set<String> systemClassesToVerify = new HashSet<>(Arrays.asList(
                "java.util.HashMap",
                "java.util.concurrent.CompletableFuture",
                "java.lang.String",
                "java.lang.Thread"
            ));
            
            for (String systemClass : systemClassesToVerify) {
                try {
                    Class.forName(systemClass);
                    result.recordSystemClassVerification(systemClass, true);
                } catch (ClassNotFoundException e) {
                    result.recordSystemClassVerification(systemClass, false);
                }
            }
            
            // Verify tier distribution
            Map<Integer, Integer> tierCounts = new HashMap<>();
            for (String className : loadedClasses) {
                int tier = CriticalClassRegistry.getClassPriority(className);
                tierCounts.merge(tier, 1, Integer::sum);
            }
            result.setTierDistribution(tierCounts);
            
            // Performance verification
            double avgLoadTime = preloadResult.getAverageLoadTimeMs();
            if (avgLoadTime > configuration.getMaxAverageLoadTime()) {
                result.addPerformanceIssue(String.format("High average load time: %.2fms", avgLoadTime));
            }
            
            // Success rate verification
            if (preloadResult.getSuccessRate() < configuration.getMinSuccessRate()) {
                result.addPerformanceIssue(String.format("Low success rate: %.1f%%", preloadResult.getSuccessRate()));
            }
            
            result.setVerificationPassed(result.getFailedVerifications().isEmpty() && 
                                       result.getPerformanceIssues().isEmpty());
            
        } catch (Exception e) {
            result.setVerificationPassed(false);
            result.addIssue("Exception during verification: " + e.getMessage());
        }
        
        return result;
    }
    
    /**
     * Initializes the preloader with configuration.
     */
    private void initialize() {
        if (!initialized) {
            synchronized (this) {
                if (!initialized) {
                    // Apply configuration
                    applyConfiguration();
                    initialized = true;
                    
                    logger.info("CriticalClassPreloader initialization completed");
                }
            }
        }
    }
    
    /**
     * Applies configuration settings to the preloader.
     */
    private void applyConfiguration() {
        // Configure bootstrap loader if needed
        // Apply metrics configuration
        // Set up caching strategies
        // Configure error handling
        
        logger.info("Configuration applied: " + configuration);
    }
    
    /**
     * Updates the current preloading state.
     * 
     * @param newState The new state to transition to
 */
    private void updateState(PreloadState newState) {
        PreloadState oldState = currentState.getAndSet(newState);
        logger.fine(String.format("State transition: %s -> %s", oldState, newState));
    }
    
    /**
     * Generates a unique session identifier.
     * 
     * @return Unique session ID
     */
    private String generateSessionId() {
        return "preload-" + System.currentTimeMillis() + "-" + Thread.currentThread().getId();
    }
    
    /**
     * Gets cached preload result for a session.
     * 
     * @param sessionId The session identifier
     * @return PreloadResult if cached, null otherwise
     */
    public PreloadResult getCachedResult(String sessionId) {
        return cachedResults.get(sessionId);
    }
    
    /**
     * Gets the current preloading state.
     * 
     * @return Current preloading state
     */
    public PreloadState getCurrentState() {
        return currentState.get();
    }
    
    /**
     * Gets current metrics.
     * 
     * @return Current metrics instance
     */
    public ClassPreloadMetrics getMetrics() {
        return metrics;
    }
    
    /**
     * Gets preload configuration.
     * 
     * @return Current configuration
     */
    public PreloadConfiguration getConfiguration() {
        return configuration;
    }
    
    /**
     * Checks if a class is considered critical.
     * 
     * @param className The fully qualified class name
     * @return true if the class is critical, false otherwise
     */
    public boolean isCriticalClass(String className) {
        return CriticalClassRegistry.isCriticalClass(className);
    }
    
    /**
     * Gets the priority tier for a class.
     * 
     * @param className The fully qualified class name
     * @return Priority tier (1=critical, 2=high, 3=medium) or -1 if not found
     */
    public int getClassPriority(String className) {
        return CriticalClassRegistry.getClassPriority(className);
    }
    
    /**
     * Gets comprehensive preloader statistics.
     * 
     * @return Map containing preloader statistics
     */
    public Map<String, Object> getPreloaderStatistics() {
        Map<String, Object> stats = new HashMap<>();
        
        stats.put("currentState", currentState.get().toString());
        stats.put("initialized", initialized);
        stats.put("cachedResults", cachedResults.size());
        stats.put("bootstrapLoaderStats", bootstrapLoader.getPreloadStatistics());
        stats.put("registryStats", CriticalClassRegistry.getRegistryStatistics());
        stats.put("currentSession", metrics.getCurrentSession());
        
        return Collections.unmodifiableMap(stats);
    }
    
    /**
     * Shuts down the preloader and cleans up resources.
     */
    public void shutdown() {
        logger.info("Shutting down CriticalClassPreloader...");
        
        bootstrapLoader.shutdown();
        metrics.resetMetrics();
        cachedResults.clear();
        
        logger.info("CriticalClassPreloader shutdown completed");
    }
    
    // ======== Supporting Classes ========
    
    /**
     * Configuration for critical class preloading.
     */
    public static final class PreloadConfiguration {
        private final boolean enabled;
        private final int minClassCount;
        private final int maxClassCount;
        private final double maxValidationFailureRate;
        private final double minSuccessRate;
        private final double maxAverageLoadTime;
        private final boolean enableMetrics;
        private final boolean enableCaching;
        private final boolean enableVerification;
        
        private PreloadConfiguration(boolean enabled, int minClassCount, int maxClassCount, 
                                   double maxValidationFailureRate, double minSuccessRate, 
                                   double maxAverageLoadTime, boolean enableMetrics, 
                                   boolean enableCaching, boolean enableVerification) {
            this.enabled = enabled;
            this.minClassCount = minClassCount;
            this.maxClassCount = maxClassCount;
            this.maxValidationFailureRate = maxValidationFailureRate;
            this.minSuccessRate = minSuccessRate;
            this.maxAverageLoadTime = maxAverageLoadTime;
            this.enableMetrics = enableMetrics;
            this.enableCaching = enableCaching;
            this.enableVerification = enableVerification;
        }
        
        public static PreloadConfiguration defaultConfiguration() {
            return new PreloadConfiguration(
                true,   // enabled
                10,     // minClassCount
                100,    // maxClassCount
                10.0,   // maxValidationFailureRate
                95.0,   // minSuccessRate
                5.0,    // maxAverageLoadTime
                true,   // enableMetrics
                true,   // enableCaching
                true    // enableVerification
            );
        }
        
        public static PreloadConfiguration performanceOptimized() {
            return new PreloadConfiguration(
                true,   // enabled
                20,     // minClassCount (more aggressive)
                80,     // maxClassCount (more focused)
                5.0,    // maxValidationFailureRate (stricter)
                98.0,   // minSuccessRate (higher bar)
                3.0,    // maxAverageLoadTime (faster)
                true,   // enableMetrics
                true,   // enableCaching
                true    // enableVerification
            );
        }
        
        // Getters
        public boolean isEnabled() { return enabled; }
        public int getMinClassCount() { return minClassCount; }
        public int getMaxClassCount() { return maxClassCount; }
        public double getMaxValidationFailureRate() { return maxValidationFailureRate; }
        public double getMinSuccessRate() { return minSuccessRate; }
        public double getMaxAverageLoadTime() { return maxAverageLoadTime; }
        public boolean isEnableMetrics() { return enableMetrics; }
        public boolean isEnableCaching() { return enableCaching; }
        public boolean isEnableVerification() { return enableVerification; }
        
        @Override
        public String toString() {
            return String.format("PreloadConfiguration[enabled=%s, min=%d, max=%d, minSuccessRate=%.1f%%, maxAvgLoadTime=%.1fms]",
                    enabled, minClassCount, maxClassCount, minSuccessRate, maxAverageLoadTime);
        }
    }
    
    /**
     * State of the preloader operation.
     */
    public enum PreloadState {
        IDLE, VALIDATING, PRELOADING, VERIFYING, FINALIZING, COMPLETED, ERROR
    }
    
    /**
     * Preload operation exception.
     */
    public static final class PreloadException extends RuntimeException {
        public PreloadException(String message) {
            super(message);
        }
        
        public PreloadException(String message, Throwable cause) {
            super(message, cause);
        }
    }
    
    /**
     * Validation result for preloading.
     */
    public static final class ValidationResult {
        private boolean valid = false;
        private final List<String> issues = new ArrayList<>();
        private Map<String, Object> registryStats;
        private double loadabilityRate;
        private Map<String, Boolean> loadabilityResults;
        private List<String> dependencyOrder;
        
        public boolean isValid() { return valid; }
        public void setValid(boolean valid) { this.valid = valid; }
        public List<String> getIssues() { return Collections.unmodifiableList(issues); }
        public void addIssue(String issue) { issues.add(issue); }
        public Map<String, Object> getRegistryStats() { return registryStats; }
        public void setRegistryStats(Map<String, Object> registryStats) { this.registryStats = registryStats; }
        public double getLoadabilityRate() { return loadabilityRate; }
        public void setLoadabilityRate(double loadabilityRate) { this.loadabilityRate = loadabilityRate; }
        public Map<String, Boolean> getLoadabilityResults() { return loadabilityResults; }
        public void setLoadabilityResults(Map<String, Boolean> loadabilityResults) { this.loadabilityResults = loadabilityResults; }
        public List<String> getDependencyOrder() { return dependencyOrder; }
        public void setDependencyOrder(List<String> dependencyOrder) { this.dependencyOrder = dependencyOrder; }
        public String getErrorMessage() { return String.join(", ", issues); }
    }
    
    /**
     * Verification result for preloading.
     */
    public static final class VerificationResult {
        private boolean verificationPassed = false;
        private final Map<String, String> successfulVerifications = new HashMap<>();
        private final Map<String, String> failedVerifications = new HashMap<>();
        private final Map<String, Boolean> systemClassVerifications = new HashMap<>();
        private final Map<String, String> performanceIssues = new HashMap<>();
        private final List<String> issues = new ArrayList<>();
        private Map<Integer, Integer> tierDistribution;
        
        public boolean isVerificationPassed() { return verificationPassed; }
        public void setVerificationPassed(boolean verificationPassed) { this.verificationPassed = verificationPassed; }
        public Map<String, String> getSuccessfulVerifications() { return Collections.unmodifiableMap(successfulVerifications); }
        public void recordSuccessfulVerification(String className) { successfulVerifications.put(className, "Verified"); }
        public Map<String, String> getFailedVerifications() { return Collections.unmodifiableMap(failedVerifications); }
        public void recordFailedVerification(String className, String reason) { failedVerifications.put(className, reason); }
        public Map<String, Boolean> getSystemClassVerifications() { return Collections.unmodifiableMap(systemClassVerifications); }
        public void recordSystemClassVerification(String className, boolean success) { systemClassVerifications.put(className, success); }
        public Map<String, String> getPerformanceIssues() { return Collections.unmodifiableMap(performanceIssues); }
        public void addPerformanceIssue(String issue) { performanceIssues.put("performance", issue); }
        public List<String> getIssues() { return Collections.unmodifiableList(issues); }
        public void addIssue(String issue) { issues.add(issue); }
        public Map<Integer, Integer> getTierDistribution() { return tierDistribution; }
        public void setTierDistribution(Map<Integer, Integer> tierDistribution) { this.tierDistribution = tierDistribution; }
    }
    
    /**
     * Complete result of a critical class preloading operation.
     */
    public static final class PreloadResult {
        private String sessionId;
        private long startTime;
        private long endTime;
        private long totalTimeMs;
        private int totalClasses;
        private int successfullyLoaded;
        private int failedLoads;
        private double successRate;
        private Set<String> loadedClasses;
        private Set<String> failedClasses;
        private Map<String, Object> performanceInsights;
        private VerificationResult verificationResult;
        private ClassPreloadMetrics.PreloadMetricsReport metricsReport;
        
        // Getters and setters
        public String getSessionId() { return sessionId; }
        public void setSessionId(String sessionId) { this.sessionId = sessionId; }
        public long getStartTime() { return startTime; }
        public void setStartTime(long startTime) { this.startTime = startTime; }
        public long getEndTime() { return endTime; }
        public void setEndTime(long endTime) { this.endTime = endTime; }
        public long getTotalTimeMs() { return totalTimeMs; }
        public void setTotalTimeMs(long totalTimeMs) { this.totalTimeMs = totalTimeMs; }
        public int getTotalClasses() { return totalClasses; }
        public void setTotalClasses(int totalClasses) { this.totalClasses = totalClasses; }
        public int getSuccessfullyLoaded() { return successfullyLoaded; }
        public void setSuccessfullyLoaded(int successfullyLoaded) { this.successfullyLoaded = successfullyLoaded; }
        public int getFailedLoads() { return failedLoads; }
        public void setFailedLoads(int failedLoads) { this.failedLoads = failedLoads; }
        public double getSuccessRate() { return successRate; }
        public void setSuccessRate(double successRate) { this.successRate = successRate; }
        public Set<String> getLoadedClasses() { return loadedClasses; }
        public void setLoadedClasses(Set<String> loadedClasses) { this.loadedClasses = loadedClasses; }
        public Set<String> getFailedClasses() { return failedClasses; }
        public void setFailedClasses(Set<String> failedClasses) { this.failedClasses = failedClasses; }
        public Map<String, Object> getPerformanceInsights() { return performanceInsights; }
        public void setPerformanceInsights(Map<String, Object> performanceInsights) { this.performanceInsights = performanceInsights; }
        public VerificationResult getVerificationResult() { return verificationResult; }
        public void setVerificationResult(VerificationResult verificationResult) { this.verificationResult = verificationResult; }
        public ClassPreloadMetrics.PreloadMetricsReport getMetricsReport() { return metricsReport; }
        public void setMetricsReport(ClassPreloadMetrics.PreloadMetricsReport metricsReport) { this.metricsReport = metricsReport; }
        
        public double getAverageLoadTimeMs() {
            return successfullyLoaded > 0 ? (totalTimeMs * 1000.0) / successfullyLoaded : 0.0;
        }
        
        public double getClassesPerSecond() {
            double seconds = totalTimeMs / 1000.0;
            return seconds > 0 ? totalClasses / seconds : 0.0;
        }
        
        @Override
        public String toString() {
            return String.format("PreloadResult[sessionId=%s, loaded=%d/%d (%.1f%%), time=%dms, avgTime=%.2fms]",
                    sessionId, successfullyLoaded, totalClasses, successRate, totalTimeMs, getAverageLoadTimeMs());
        }
    }
}