package io.warmup.framework.examples.configuration;

import io.warmup.framework.annotation.*;
import io.warmup.framework.core.WarmupContainer;

/**
 * Simple examples of @Configuration and @Bean usage.
 * These examples demonstrate basic patterns for configuration.
 * 
 * @author MiniMax Agent
 * @since 1.0
 */
public class ConfigurationExamples {
    
    // ================ BASIC CONFIGURATION EXAMPLE ================
    
    @Configuration
    public static class BasicConfig {
        
        @Bean
        public String message() {
            return "Hello from Configuration!";
        }
        
        @Bean
        public Integer number() {
            return 42;
        }
        
        @Bean
        public Counter counter() {
            return new Counter();
        }
    }
    
    // ================ SIMPLE SERVICE CONFIGURATION ================
    
    @Configuration
    public static class ServiceConfig {
        
        @Bean
        public Calculator calculator() {
            return new Calculator();
        }
        
        @Bean
        public Printer printer() {
            return new Printer();
        }
    }
    
    // ================ EXAMPLE USAGE ================
    
    public static void main(String[] args) {
        System.out.println("=== Warmup Framework @Configuration/@Bean Examples ===");
        
        // Create container
        WarmupContainer container = new WarmupContainer();
        
        // Register configuration classes
        System.out.println("Registering configuration classes...");
        container.register(BasicConfig.class, true);
        container.register(ServiceConfig.class, true);
        
        // Initialize container (processes @Configuration classes)
        System.out.println("Initializing container...");
        try {
            container.initializeAllComponents();
        } catch (Exception e) {
            System.err.println("Failed to initialize container: " + e.getMessage());
            e.printStackTrace();
            return;
        }
        
        // Use beans
        System.out.println("\n=== Testing Configuration Beans ===");
        
        String message = container.getBean(String.class);
        Integer number = container.getBean(Integer.class);
        Calculator calculator = container.getBean(Calculator.class);
        Printer printer = container.getBean(Printer.class);
        Counter counter = container.getBean(Counter.class);
        
        if (message != null) System.out.println("Message: " + message);
        if (number != null) System.out.println("Number: " + number);
        if (calculator != null) System.out.println("Calculator available");
        if (printer != null) System.out.println("Printer available");
        if (counter != null) System.out.println("Counter available");
        
        System.out.println("\n✅ Configuration examples working!");
        
        // Shutdown
        try {
            container.shutdown();
        } catch (Exception e) {
            System.err.println("Error during shutdown: " + e.getMessage());
        }
    }
    
    // ================ SIMPLE SUPPORTING CLASSES ================
    
    public static class Counter {
        private int count = 0;
        
        public void increment() {
            count++;
        }
        
        public int getCount() {
            return count;
        }
    }
    
    public static class Calculator {
        public int add(int a, int b) {
            return a + b;
        }
        
        public int multiply(int a, int b) {
            return a * b;
        }
    }
    
    public static class Printer {
        public void print(String text) {
            System.out.println("Printer: " + text);
        }
    }
}