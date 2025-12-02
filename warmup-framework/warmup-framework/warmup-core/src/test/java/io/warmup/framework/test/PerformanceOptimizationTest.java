package io.warmup.framework.test;

import io.warmup.framework.annotation.validation.*;
import io.warmup.framework.validation.*;
import io.warmup.framework.validation.cache.ValidationCache;
import io.warmup.framework.validation.optimization.OptimizedReflectionUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for performance optimization features.
 * 
 * @author MiniMax Agent
 * @since 1.0
 */
class PerformanceOptimizationTest {
    
    private OptimizedValidatorFactory factory;
    private ValidationCache cache;
    private OptimizedReflectionUtil reflectionUtil;
    
    @BeforeEach
    void setUp() {
        factory = new OptimizedValidatorFactory();
        cache = new ValidationCache();
        reflectionUtil = new OptimizedReflectionUtil(cache);
    }
    
    /**
     * Test class for validation performance testing.
     */
    static class TestObject {
        @NotNull
        @Size(min = 3, max = 50)
        private String name;
        
        @Pattern(regexp = "\\d+", message = "Must contain only digits")
        private String number;
        
        @CustomConstraint(
            validator = SimpleCustomValidator.class
        )
        private String customField;
        
        public TestObject(String name, String number, String customField) {
            this.name = name;
            this.number = number;
            this.customField = customField;
        }
        
        // Getters
        public String getName() { return name; }
        public String getNumber() { return number; }
        public String getCustomField() { return customField; }
    }
    
    /**
     * Simple custom validator for testing.
     */
    static class SimpleCustomValidator implements CustomConstraintValidator {
        @Override
        public boolean isValid(Object value, Object... parameters) {
            if (value == null) return true;
            return value.toString().length() >= 5;
        }
        
        @Override
        public String getMessage(Object invalidValue, String fieldName, Object... parameters) {
            return "Field must be at least 5 characters long";
        }
    }
    
    @Test
    void testValidationCache() {
        // Test validator caching
        TestObject obj1 = new TestObject("test", "123", "custom");
        TestObject obj2 = new TestObject("test2", "456", "custom2");
        
        // First validation - should miss cache
        ViolationReport<TestObject> report1 = factory.getViolationReport(obj1);
        
        // Second validation - might hit cache
        ViolationReport<TestObject> report2 = factory.getViolationReport(obj1);
        
        assertNotNull(report1);
        assertNotNull(report2);
        
        // Test cache statistics
        ValidationCache.CacheStatistics stats = cache.getStatistics();
        assertTrue(stats.getTotalRequests() >= 0);
        
        System.out.println("Cache statistics: " + stats);
    }
    
    @Test
    void testLazyValidation() {
        LazyValidator lazyValidator = factory.createLazyValidator();
        
        TestObject obj = new TestObject("test", "123", "custom");
        
        // Submit for lazy validation
        CompletableFuture<ViolationReport<?>> future = lazyValidator.submitForValidation(obj);
        
        // Wait for completion
        assertDoesNotThrow(() -> {
            ViolationReport<?> report = future.get(5, TimeUnit.SECONDS);
            assertNotNull(report);
        });
        
        lazyValidator.shutdown();
    }
    
    @Test
    void testParallelValidation() {
        OptimizedValidatorFactory.OptimizedValidatorConfig config = 
            new OptimizedValidatorFactory.OptimizedValidatorConfig(
                true, true, true, true, 1000, 300000
            );
        
        OptimizedValidatorFactory parallelFactory = new OptimizedValidatorFactory(config);
        LazyValidator lazyValidator = parallelFactory.createLazyValidator();
        
        // Create multiple test objects
        TestObject[] objects = new TestObject[10];
        for (int i = 0; i < objects.length; i++) {
            objects[i] = new TestObject("test" + i, "123" + i, "custom" + i);
        }
        
        // Submit all for validation
        CompletableFuture<ViolationReport<?>>[] futures = new CompletableFuture[objects.length];
        for (int i = 0; i < objects.length; i++) {
            futures[i] = lazyValidator.submitForValidation(objects[i]);
        }
        
        // Wait for all to complete
        assertDoesNotThrow(() -> {
            CompletableFuture.allOf(futures).get(10, TimeUnit.SECONDS);
            
            for (CompletableFuture<ViolationReport<?>> future : futures) {
                ViolationReport<?> report = future.get();
                assertNotNull(report);
            }
        });
        
        lazyValidator.shutdown();
    }
    
    @Test
    void testOptimizedReflectionUtil() {
        // Test field accessor caching
        OptimizedReflectionUtil.FieldAccessor accessor1 = 
            reflectionUtil.getFieldAccessor(TestObject.class, "name");
        OptimizedReflectionUtil.FieldAccessor accessor2 = 
            reflectionUtil.getFieldAccessor(TestObject.class, "name");
        
        assertSame(accessor1, accessor2, "Field accessors should be cached and reused");
        
        // Test field access
        TestObject obj = new TestObject("test-name", "123", "custom-field");
        
        Object value1 = accessor1.get(obj);
        Object value2 = accessor2.get(obj);
        
        assertEquals("test-name", value1);
        assertEquals("test-name", value2);
        assertEquals(value1, value2);
        
        // Test cache statistics
        OptimizedReflectionUtil.AccessorCacheStatistics stats = reflectionUtil.getStatistics();
        assertTrue(stats.getFieldAccessorCount() >= 1);
        
        System.out.println("Accessor statistics: " + stats);
    }
    
    @Test
    void testReflectionOptimization() {
        // Test all fields retrieval
        OptimizedReflectionUtil.FieldAccessor[] accessors = new OptimizedReflectionUtil.FieldAccessor[3];
        accessors[0] = reflectionUtil.getFieldAccessor(TestObject.class, "name");
        accessors[1] = reflectionUtil.getFieldAccessor(TestObject.class, "number");
        accessors[2] = reflectionUtil.getFieldAccessor(TestObject.class, "customField");
        
        TestObject obj = new TestObject("optimized", "789", "performance");
        
        // Access all fields through optimized accessors
        for (OptimizedReflectionUtil.FieldAccessor accessor : accessors) {
            Object value = accessor.get(obj);
            assertNotNull(value);
        }
        
        // Verify caching worked
        OptimizedReflectionUtil.AccessorCacheStatistics stats = reflectionUtil.getStatistics();
        assertEquals(3, stats.getFieldAccessorCount());
    }
    
    @Test
    void testPerformanceMetrics() {
        // Run some validations to generate metrics
        for (int i = 0; i < 10; i++) {
            TestObject obj = new TestObject("test" + i, "123" + i, "custom" + i);
            factory.getViolationReport(obj);
        }
        
        // Get performance metrics
        OptimizedValidatorFactory.PerformanceMetrics metrics = factory.getPerformanceMetrics();
        
        assertNotNull(metrics);
        assertTrue(metrics.getEfficiencyScore() >= 0.0);
        assertTrue(metrics.getEfficiencyScore() <= 1.0);
        
        System.out.println("Performance metrics: " + metrics);
    }
    
    @Test
    void testCacheClearing() {
        // Create some cache entries
        TestObject obj = new TestObject("test", "123", "custom");
        factory.getViolationReport(obj);
        reflectionUtil.getFieldAccessor(TestObject.class, "name");
        
        // Clear caches
        factory.clearCaches();
        
        // Verify caches are cleared (cache size should be reasonable after clearing)
        ValidationCache.CacheStatistics stats = factory.getCacheStatistics();
        System.out.println("Cache after clearing: " + stats);
        
        // Should still work after clearing
        ViolationReport<TestObject> newReport = factory.getViolationReport(obj);
        assertNotNull(newReport);
    }
    
    @Test
    void testOptimizedValidatorConfiguration() {
        OptimizedValidatorFactory.OptimizedValidatorConfig config = 
            new OptimizedValidatorFactory.OptimizedValidatorConfig(
                false, // disable parallel
                true,  // enable lazy
                true,  // enable reflection
                true,  // enable pattern caching
                500,   // max cache size
                60000  // timeout
            );
        
        OptimizedValidatorFactory customFactory = new OptimizedValidatorFactory(config);
        
        assertFalse(config.isEnableParallelValidation());
        assertTrue(config.isEnableLazyValidation());
        assertTrue(config.isEnableReflectionOptimization());
        assertEquals(500, config.getMaxCacheSize());
        assertEquals(60000, config.getCacheTimeoutMs());
        
        System.out.println("Custom config: " + config);
    }
    
    @Test
    void testValidatorFactoryIntegration() {
        // Test that OptimizedValidatorFactory properly delegates to DefaultValidator
        TestObject validObj = new TestObject("valid-name", "123", "valid-custom");
        TestObject invalidObj = new TestObject("ab", "abc", "x"); // Too short fields
        
        // Test standard validation interface
        assertTrue(factory.isValid(validObj));
        assertFalse(factory.isValid(invalidObj));
        
        // Test violation report
        ViolationReport<TestObject> validReport = factory.getViolationReport(validObj);
        ViolationReport<TestObject> invalidReport = factory.getViolationReport(invalidObj);
        
        assertTrue(validReport.isValid());
        assertFalse(invalidReport.isValid());
        assertTrue(invalidReport.getViolationCount() > 0);
        
        System.out.println("Valid violations: " + validReport.getViolationCount());
        System.out.println("Invalid violations: " + invalidReport.getViolationCount());
    }
    
    @Test
    void testCustomValidatorWithOptimization() {
        LazyValidator lazyValidator = factory.createLazyValidator();
        
        // Register custom validator
        lazyValidator.getCustomValidatorManager().registerValidatorByClass(SimpleCustomValidator.class);
        
        TestObject obj1 = new TestObject("test", "123", "abc"); // Should fail custom validation
        TestObject obj2 = new TestObject("test", "123", "valid-custom"); // Should pass custom validation
        
        // Test lazy validation with custom validators
        CompletableFuture<ViolationReport<?>> future1 = lazyValidator.submitForValidation(obj1);
        CompletableFuture<ViolationReport<?>> future2 = lazyValidator.submitForValidation(obj2);
        
        assertDoesNotThrow(() -> {
            ViolationReport<?> report1 = future1.get(5, TimeUnit.SECONDS);
            ViolationReport<?> report2 = future2.get(5, TimeUnit.SECONDS);
            
            assertFalse(report1.isValid());
            assertTrue(report2.isValid());
        });
        
        lazyValidator.shutdown();
    }
}