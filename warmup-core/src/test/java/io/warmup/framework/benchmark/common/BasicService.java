package io.warmup.framework.benchmark.common;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;

/**
 * Servicio básico para benchmarks de inyección de dependencias
 * Compatible con todos los frameworks: Warmup, Dagger, Guice, Spring
 */
@Singleton
public class BasicService {
    
    private final String name;
    private final int value;
    
    @Inject
    public BasicService(String name, int value) {
        this.name = name;
        this.value = value;
    }
    
    public String getName() {
        return name;
    }
    
    public int getValue() {
        return value;
    }
    
    public String process(String input) {
        return name + ": " + input + " (value: " + value + ")";
    }
    
    public void performOperation() {
        // Simular trabajo
        for (int i = 0; i < 100; i++) {
            String result = process("operation-" + i);
        }
    }
}