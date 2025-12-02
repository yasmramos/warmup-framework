package io.warmup.benchmark;

import io.warmup.framework.core.WarmupContainer;

public class SimpleTest {
    public static void main(String[] args) {
        System.out.println("Testing WarmupContainer initialization...");
        
        WarmupContainer container = new WarmupContainer();
        try {
            System.out.println("Container created successfully");
            
            // Test basic registration
            container.registerBean("testBean", String.class, "Hello Warmup");
            System.out.println("Bean registered successfully");
            
            // Test retrieval
            String bean = container.getBean("testBean", String.class);
            System.out.println("Bean retrieved: " + bean);
            
            System.out.println("Test completed successfully!");
            
        } catch (Exception e) {
            System.err.println("Error during test: " + e.getMessage());
            e.printStackTrace();
        } finally {
            if (container != null) {
                try {
                    container.shutdown();
                    System.out.println("Container shutdown successfully");
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }
    }
}