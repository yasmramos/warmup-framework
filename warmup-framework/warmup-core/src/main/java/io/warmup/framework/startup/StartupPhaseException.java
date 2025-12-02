package io.warmup.framework.startup;

/**
 * 🚨 Excepción específica para errores en fases de startup
 * 
 * @author MiniMax Agent
 * @version 1.0
 */
public class StartupPhaseException extends RuntimeException {
    
    private final String phase;
    private final long durationMs;
    
    public StartupPhaseException(String message, Throwable cause) {
        super(message, cause);
        this.phase = "unknown";
        this.durationMs = 0;
    }
    
    public StartupPhaseException(String phase, String message, Throwable cause) {
        super(message, cause);
        this.phase = phase;
        this.durationMs = 0;
    }
    
    public StartupPhaseException(String phase, long durationMs, String message, Throwable cause) {
        super(message, cause);
        this.phase = phase;
        this.durationMs = durationMs;
    }
    
    public String getPhase() {
        return phase;
    }
    
    public long getDurationMs() {
        return durationMs;
    }
}