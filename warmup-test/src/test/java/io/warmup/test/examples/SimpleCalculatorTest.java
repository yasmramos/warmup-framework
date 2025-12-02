package io.warmup.test.examples;

import io.warmup.test.annotation.*;
import io.warmup.test.core.WarmupTestExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.mockito.Mockito.*;
import static org.assertj.core.api.Assertions.*;

/**
 * Test simple standalone para demostrar warmup-test sin dependencias del core.
 * Este test no depende de warmup-core y funciona de forma independiente.
 */
@ExtendWith(WarmupTestExtension.class)
@WarmupTest
public class SimpleCalculatorTest {
    
    // Sistema bajo test - Calculadora simple como ejemplo
    @Spy
    private Calculator calculator;
    
    // Dependencia externa - Logger (mockeado)
    @Mock
    private CalculatorLogger logger;
    
    @Test
    public void add_twoNumbers_returnsSum() {
        // Arrange
        int a = 5;
        int b = 3;
        when(logger.logOperation("ADD", a, b)).thenReturn(true);
        
        // Act
        int result = calculator.add(a, b);
        
        // Assert
        assertThat(result).isEqualTo(8);
        verify(logger).logOperation("ADD", a, b);
        verify(calculator).logResult(result);
    }
    
    @Test
    public void subtract_twoNumbers_returnsDifference() {
        // Arrange
        int a = 10;
        int b = 4;
        when(logger.logOperation("SUBTRACT", a, b)).thenReturn(true);
        
        // Act
        int result = calculator.subtract(a, b);
        
        // Assert
        assertThat(result).isEqualTo(6);
        verify(logger).logOperation("SUBTRACT", a, b);
        verify(calculator).logResult(result);
    }
    
    @Test
    public void multiply_twoNumbers_returnsProduct() {
        // Arrange
        int a = 7;
        int b = 8;
        when(logger.logOperation("MULTIPLY", a, b)).thenReturn(true);
        
        // Act
        int result = calculator.multiply(a, b);
        
        // Assert
        assertThat(result).isEqualTo(56);
        verify(logger).logOperation("MULTIPLY", a, b);
        verify(calculator).logResult(result);
    }
    
    // Clases de ejemplo para el test
    public static class Calculator {
        private final CalculatorLogger logger;
        
        public Calculator(CalculatorLogger logger) {
            this.logger = logger;
        }
        
        public int add(int a, int b) {
            int result = a + b;
            logResult(result);
            logger.logOperation("ADD", a, b);
            return result;
        }
        
        public int subtract(int a, int b) {
            int result = a - b;
            logResult(result);
            logger.logOperation("SUBTRACT", a, b);
            return result;
        }
        
        public int multiply(int a, int b) {
            int result = a * b;
            logResult(result);
            logger.logOperation("MULTIPLY", a, b);
            return result;
        }
        
        public double divide(int a, int b) {
            if (b == 0) {
                throw new IllegalArgumentException("Cannot divide by zero");
            }
            double result = (double) a / b;
            logResult(result);
            logger.logOperation("DIVIDE", a, b);
            return result;
        }
        
        public void logResult(Object result) {
            // Simple logging method
            System.out.println("Result: " + result);
        }
    }
    
    public interface CalculatorLogger {
        boolean logOperation(String operation, int a, int b);
    }
}