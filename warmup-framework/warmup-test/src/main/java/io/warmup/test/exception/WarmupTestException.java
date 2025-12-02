package io.warmup.test.exception;

/**
 * Excepción principal del framework de testing Warmup.
 * Se lanza cuando ocurren errores durante la configuración automática
 * de tests zero-config.
 */
public class WarmupTestException extends RuntimeException {
    
    public WarmupTestException(String message) {
        super(message);
    }
    
    public WarmupTestException(String message, Throwable cause) {
        super(message, cause);
    }
    
    public WarmupTestException(Throwable cause) {
        super(cause);
    }
}