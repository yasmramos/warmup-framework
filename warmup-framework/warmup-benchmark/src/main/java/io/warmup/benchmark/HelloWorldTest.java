package io.warmup.benchmark;

public class HelloWorldTest {
    public static void main(String[] args) {
        System.out.println("🔥 HELLO WORLD TEST - Java is working!");
        System.out.println("✅ This is a simple test to verify basic functionality");
        
        // Test basic math
        int result = 5 + 3;
        System.out.println("🧮 5 + 3 = " + result);
        
        if (result == 8) {
            System.out.println("🎯 Math operations work correctly!");
        } else {
            System.err.println("❌ Math test failed!");
            System.exit(1);
        }
        
        System.out.println("🎉 Hello World Test completed successfully!");
    }
}