package io.warmup.benchmark;

public class BasicTest {
    public static void main(String[] args) {
        System.out.println("Starting basic test...");
        
        // Test básico
        int sum = 0;
        for (int i = 0; i < 1000; i++) {
            sum += i;
        }
        
        System.out.println("Test completed. Sum: " + sum);
    }
}