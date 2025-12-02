package io.warmup.benchmark;

import io.warmup.framework.core.WarmupContainer;

public class DiagnosticTest {
    public static void main(String[] args) {
        System.out.println("Starting diagnostic test...");
        
        try {
            System.out.println("Creating WarmupContainer...");
            WarmupContainer container = new WarmupContainer();
            System.out.println("✓ Container created successfully");
            
            System.out.println("Getting container methods...");
            System.out.println("Available methods:");
            for (Method method : container.getClass().getDeclaredMethods()) {
                System.out.println("  - " + method.getName());
            }
            
            System.out.println("Getting bean registry...");
            var registry = container.getBeanRegistry();
            System.out.println("✓ BeanRegistry obtained: " + registry.getClass().getSimpleName());
            
            System.out.println("Diagnostic test completed!");
            
        } catch (Exception e) {
            System.err.println("❌ Error during diagnostic test:");
            e.printStackTrace();
        } finally {
            System.out.println("Test finished.");
        }
    }
}