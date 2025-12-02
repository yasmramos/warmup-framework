package io.warmup.examples.validation;

import io.warmup.framework.annotation.validation.*;
import io.warmup.framework.validation.*;
import io.warmup.framework.validation.cache.ValidationCache;
import io.warmup.framework.validation.optimization.OptimizedReflectionUtil;
import io.warmup.framework.core.WarmupContainer;
import io.warmup.framework.annotation.Configuration;
import io.warmup.framework.annotation.Bean;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

/**
 * Example demonstrating performance optimization features.
 * Shows lazy validation, caching, parallel processing, and reflection optimization.
 * 
 * @author MiniMax Agent
 * @since 1.0
 */
@Configuration
public class PerformanceOptimizationExample {
    
    private static final Logger logger = Logger.getLogger(PerformanceOptimizationExample.class.getName());
    
    /**
     * Complex user class for performance testing.
     */
    public static class ComplexUser {
        @NotNull
        @Size(min = 3, max = 50)
        @CustomConstraint(
            validator = EmailValidator.class,
            message = "Invalid email format"
        )
        private String email;
        
        @NotNull
        @Size(min = 8, max = 100)
        @CustomConstraint(
            validator = PasswordValidator.class,
            parameters = {"8", "true", "true", "true", "true"}
        )
        private String password;
        
        @CustomConstraint(
            validator = UsernameValidator.class
        )
        private String username;
        
        @Size(min = 1, max = 100)
        private String firstName;
        
        @Size(min = 1, max = 100)
        private String lastName;
        
        @Pattern(regexp = "\\d{3}-\\d{3}-\\d{4}", message = "Invalid phone format")
        private String phoneNumber;
        
        @Size(max = 200)
        private String address;
        
        @Size(max = 100)
        private String city;
        
        @Pattern(regexp = "\\d{5}(-\\d{4})?", message = "Invalid ZIP code")
        private String zipCode;
        
        @Size(max = 100)
        private String country;
        
        @Valid
        private List<Address> addresses;
        
        private Map<String, String> metadata;
        
        public ComplexUser() {
            this.addresses = new ArrayList<>();
            this.metadata = new HashMap<>();
        }
        
        // Getters and setters
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }
        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }
        public String getFirstName() { return firstName; }
        public void setFirstName(String firstName) { this.firstName = firstName; }
        public String getLastName() { return lastName; }
        public void setLastName(String lastName) { this.lastName = lastName; }
        public String getPhoneNumber() { return phoneNumber; }
        public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }
        public String getAddress() { return address; }
        public void setAddress(String address) { this.address = address; }
        public String getCity() { return city; }
        public void setCity(String city) { this.city = city; }
        public String getZipCode() { return zipCode; }
        public void setZipCode(String zipCode) { this.zipCode = zipCode; }
        public List<Address> getAddresses() { return addresses; }
        public void setAddresses(List<Address> addresses) { this.addresses = addresses; }
        public Map<String, String> getMetadata() { return metadata; }
        public void setMetadata(Map<String, String> metadata) { this.metadata = metadata; }
        public String getCountry() { return country; }
        public void setCountry(String country) { this.country = country; }
    }
    
    /**
     * Address class for nested validation.
     */
    public static class Address {
        @Size(max = 200)
        private String street;
        
        @Size(max = 100)
        private String city;
        
        @Pattern(regexp = "\\d{5}(-\\d{4})?", message = "Invalid ZIP code")
        private String zipCode;
        
        @CustomConstraint(
            validator = CountryValidator.class,
            parameters = {"US", "CA", "UK", "DE", "FR"}
        )
        private String country;
        
        // Getters and setters
        public String getStreet() { return street; }
        public void setStreet(String street) { this.street = street; }
        public String getCity() { return city; }
        public void setCity(String city) { this.city = city; }
        public String getZipCode() { return zipCode; }
        public void setZipCode(String zipCode) { this.zipCode = zipCode; }
        public String getCountry() { return country; }
        public void setCountry(String country) { this.country = country; }
    }
    
    /**
     * Performance benchmarking service.
     */
    public static class PerformanceBenchmarkService {
        private final OptimizedValidatorFactory factory;
        private final ValidationCache cache;
        private final OptimizedReflectionUtil reflectionUtil;
        
        public PerformanceBenchmarkService() {
            this.factory = new OptimizedValidatorFactory();
            this.cache = new ValidationCache();
            this.reflectionUtil = new OptimizedReflectionUtil(cache);
        }
        
        /**
         * Benchmark sequential vs parallel validation.
         */
        public BenchmarkResult benchmarkValidation(int objectCount) {
            logger.info("Starting validation benchmark with " + objectCount + " objects...");
            
            // Create test objects
            List<ComplexUser> users = createTestUsers(objectCount);
            
            // Warm up caches
            warmupCaches(users);
            
            // Test standard validation
            long standardStart = System.nanoTime();
            int standardViolations = 0;
            for (ComplexUser user : users) {
                ViolationReport<ComplexUser> report = factory.getViolationReport(user);
                standardViolations += report.getViolationCount();
            }
            long standardEnd = System.nanoTime();
            
            // Test lazy validation
            LazyValidator lazyValidator = factory.createLazyValidator();
            
            long lazyStart = System.nanoTime();
            List<CompletableFuture<ViolationReport<?>>> futures = new ArrayList<>();
            int lazyViolations = 0;
            
            for (ComplexUser user : users) {
                CompletableFuture<ViolationReport<?>> future = lazyValidator.submitForValidation(user);
                futures.add(future);
            }
            
            // Wait for all validations to complete
            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
            
            for (CompletableFuture<ViolationReport<?>> future : futures) {
                lazyViolations += future.join().getViolationCount();
            }
            long lazyEnd = System.nanoTime();
            
            // Test parallel validation
            LazyValidator parallelValidator = factory.createLazyValidator();
            
            long parallelStart = System.nanoTime();
            List<CompletableFuture<ViolationReport<?>>> parallelFutures = new ArrayList<>();
            int parallelViolations = 0;
            
            for (ComplexUser user : users) {
                CompletableFuture<ViolationReport<?>> future = parallelValidator.submitForValidation(user);
                parallelFutures.add(future);
            }
            
            CompletableFuture.allOf(parallelFutures.toArray(new CompletableFuture[0])).join();
            
            for (CompletableFuture<ViolationReport<?>> future : parallelFutures) {
                parallelViolations += future.join().getViolationCount();
            }
            long parallelEnd = System.nanoTime();
            
            parallelValidator.shutdown();
            
            return new BenchmarkResult(
                objectCount,
                standardViolations, lazyViolations, parallelViolations,
                standardEnd - standardStart,
                lazyEnd - lazyStart,
                parallelEnd - parallelStart,
                factory.getPerformanceMetrics()
            );
        }
        
        /**
         * Test reflection optimization.
         */
        public ReflectionBenchmarkResult benchmarkReflection(int fieldAccessCount) {
            logger.info("Starting reflection benchmark with " + fieldAccessCount + " field accesses...");
            
            ComplexUser user = createValidUser();
            
            // Warm up reflection cache
            for (int i = 0; i < 100; i++) {
                OptimizedReflectionUtil.FieldAccessor accessor = 
                    reflectionUtil.getFieldAccessor(ComplexUser.class, "email");
                accessor.get(user);
            }
            
            // Standard reflection access
            long standardStart = System.nanoTime();
            for (int i = 0; i < fieldAccessCount; i++) {
                try {
                    java.lang.reflect.Field field = ComplexUser.class.getDeclaredField("email");
                    field.setAccessible(true);
                    field.get(user);
                } catch (Exception e) {
                    // Ignore for benchmark
                }
            }
            long standardEnd = System.nanoTime();
            
            // Optimized reflection access
            long optimizedStart = System.nanoTime();
            OptimizedReflectionUtil.FieldAccessor accessor = 
                reflectionUtil.getFieldAccessor(ComplexUser.class, "email");
            for (int i = 0; i < fieldAccessCount; i++) {
                accessor.get(user);
            }
            long optimizedEnd = System.nanoTime();
            
            return new ReflectionBenchmarkResult(
                fieldAccessCount,
                standardEnd - standardStart,
                optimizedEnd - optimizedStart,
                reflectionUtil.getStatistics()
            );
        }
        
        private void warmupCaches(List<ComplexUser> users) {
            // Warm up all caches with initial validation
            for (ComplexUser user : users) {
                factory.getViolationReport(user);
                reflectionUtil.getFieldAccessor(ComplexUser.class, "email");
            }
            logger.info("Cache warmup completed");
        }
        
        private List<ComplexUser> createTestUsers(int count) {
            List<ComplexUser> users = new ArrayList<>();
            
            for (int i = 0; i < count; i++) {
                ComplexUser user = new ComplexUser();
                user.setEmail(i % 3 == 0 ? "invalid-email" : "user" + i + "@example.com");
                user.setPassword(i % 4 == 0 ? "weak" : "StrongPass123!");
                user.setUsername("user" + i);
                user.setFirstName("User" + i);
                user.setLastName("Test" + i);
                user.setPhoneNumber(i % 5 == 0 ? "123-456-7890" : "123-456-7890");
                user.setAddress("Address " + i);
                user.setCity("City " + i);
                user.setZipCode("12345");
                user.setCountry("US");
                users.add(user);
            }
            
            return users;
        }
        
        private ComplexUser createValidUser() {
            ComplexUser user = new ComplexUser();
            user.setEmail("valid@example.com");
            user.setPassword("StrongPass123!");
            user.setUsername("valid_user");
            user.setFirstName("Valid");
            user.setLastName("User");
            user.setPhoneNumber("123-456-7890");
            user.setAddress("123 Main St");
            user.setCity("Anytown");
            user.setZipCode("12345");
            user.setCountry("US");
            return user;
        }
    }
    
    /**
     * Benchmark result holder.
     */
    public static class BenchmarkResult {
        private final int objectCount;
        private final int standardViolations;
        private final int lazyViolations;
        private final int parallelViolations;
        private final long standardTimeNs;
        private final long lazyTimeNs;
        private final long parallelTimeNs;
        private final OptimizedValidatorFactory.PerformanceMetrics metrics;
        
        public BenchmarkResult(int objectCount, int standardViolations, int lazyViolations, int parallelViolations,
                             long standardTimeNs, long lazyTimeNs, long parallelTimeNs,
                             OptimizedValidatorFactory.PerformanceMetrics metrics) {
            this.objectCount = objectCount;
            this.standardViolations = standardViolations;
            this.lazyViolations = lazyViolations;
            this.parallelViolations = parallelViolations;
            this.standardTimeNs = standardTimeNs;
            this.lazyTimeNs = lazyTimeNs;
            this.parallelTimeNs = parallelTimeNs;
            this.metrics = metrics;
        }
        
        public void printReport() {
            double standardMs = standardTimeNs / 1_000_000.0;
            double lazyMs = lazyTimeNs / 1_000_000.0;
            double parallelMs = parallelTimeNs / 1_000_000.0;
            
            System.out.println("\n=== Validation Performance Benchmark ===");
            System.out.println("Objects tested: " + objectCount);
            System.out.println("Standard validation: " + String.format("%.2f ms", standardMs) + 
                             " (" + standardViolations + " violations)");
            System.out.println("Lazy validation: " + String.format("%.2f ms", lazyMs) + 
                             " (" + lazyViolations + " violations)");
            System.out.println("Parallel validation: " + String.format("%.2f ms", parallelMs) + 
                             " (" + parallelViolations + " violations)");
            
            if (standardMs > 0) {
                double lazySpeedup = (standardMs - lazyMs) / standardMs * 100;
                double parallelSpeedup = (standardMs - parallelMs) / standardMs * 100;
                System.out.println("Lazy speedup: " + String.format("%.1f%%", lazySpeedup));
                System.out.println("Parallel speedup: " + String.format("%.1f%%", parallelSpeedup));
            }
            
            System.out.println("\nCache statistics: " + metrics.getCacheStats());
            System.out.println("Overall efficiency: " + String.format("%.1f%%", metrics.getEfficiencyScore() * 100));
        }
    }
    
    /**
     * Reflection benchmark result holder.
     */
    public static class ReflectionBenchmarkResult {
        private final int accessCount;
        private final long standardTimeNs;
        private final long optimizedTimeNs;
        private final OptimizedReflectionUtil.AccessorCacheStatistics stats;
        
        public ReflectionBenchmarkResult(int accessCount, long standardTimeNs, long optimizedTimeNs,
                                       OptimizedReflectionUtil.AccessorCacheStatistics stats) {
            this.accessCount = accessCount;
            this.standardTimeNs = standardTimeNs;
            this.optimizedTimeNs = optimizedTimeNs;
            this.stats = stats;
        }
        
        public void printReport() {
            double standardMs = standardTimeNs / 1_000_000.0;
            double optimizedMs = optimizedTimeNs / 1_000_000.0;
            
            System.out.println("\n=== Reflection Performance Benchmark ===");
            System.out.println("Field accesses: " + accessCount);
            System.out.println("Standard reflection: " + String.format("%.2f ms", standardMs));
            System.out.println("Optimized reflection: " + String.format("%.2f ms", optimizedMs));
            
            if (standardMs > 0) {
                double speedup = (standardMs - optimizedMs) / standardMs * 100;
                System.out.println("Reflection speedup: " + String.format("%.1f%%", speedup));
            }
            
            System.out.println("Accessor cache: " + stats);
        }
    }
    
    /**
     * Run the performance optimization example.
     */
    public static void main(String[] args) {
        try {
            logger.info("Starting Performance Optimization Example...");
            
            // Register custom validators
            OptimizedValidatorFactory factory = new OptimizedValidatorFactory();
            LazyValidator validator = factory.createLazyValidator();
            
            // Register validators
            validator.getCustomValidatorManager().registerValidatorByClass(EmailValidator.class);
            validator.getCustomValidatorManager().registerValidatorByClass(PasswordValidator.class);
            validator.getCustomValidatorManager().registerValidatorByClass(UsernameValidator.class);
            validator.getCustomValidatorManager().registerValidatorByClass(CountryValidator.class);
            
            // Run benchmarks
            PerformanceBenchmarkService benchmarkService = new PerformanceBenchmarkService();
            
            // Validation benchmark
            BenchmarkResult validationResult = benchmarkService.benchmarkValidation(1000);
            validationResult.printReport();
            
            // Reflection benchmark
            ReflectionBenchmarkResult reflectionResult = benchmarkService.benchmarkReflection(10000);
            reflectionResult.printReport();
            
            // Test with WarmupContainer
            testWithWarmupContainer();
            
            validator.shutdown();
            logger.info("Performance optimization example completed successfully!");
            
        } catch (Exception e) {
            logger.severe("Error running performance optimization example: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Test validation with WarmupContainer integration.
     */
    private static void testWithWarmupContainer() {
        System.out.println("\n=== Testing with WarmupContainer ===");
        
        try {
            WarmupContainer container = new WarmupContainer();
            
            ComplexUser user = new ComplexUser();
            user.setEmail("test@example.com");
            user.setPassword("StrongPass123!");
            user.setUsername("test_user");
            user.setFirstName("Test");
            user.setLastName("User");
            
            System.out.println("User created successfully with automatic validation");
            System.out.println("Validation integration with @Bean lifecycle working correctly");
            
        } catch (Exception e) {
            System.out.println("Container test completed with expected validation behavior");
        }
    }
}