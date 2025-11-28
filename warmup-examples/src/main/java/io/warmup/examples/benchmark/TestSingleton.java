package io.warmup.examples.benchmark;

public class TestSingleton implements TestInterface {

    private static int instanceCount = 0;

    public TestSingleton() {
        instanceCount++;
    }

    @Override
    public String getName() {
        return "TestSingleton-instance-" + instanceCount;
    }

    public static int getInstanceCount() {
        return instanceCount;
    }
}
