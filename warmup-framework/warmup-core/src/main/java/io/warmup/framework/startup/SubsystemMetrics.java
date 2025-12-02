package io.warmup.framework.startup;

/**
 * 📊 MÉTRICAS DE INICIALIZACIÓN DE UN SUBSISTEMA
 * 
 * Tracking detallado del rendimiento de inicialización de cada subsistema
 * en el sistema de startup paralelo.
 * 
 * @author MiniMax Agent
 * @version 1.0
 */
public class SubsystemMetrics {
    
    private final String name;
    private final boolean success;
    private final long durationNs;
    private final Exception error;
    private final long startTimeNs;
    
    public SubsystemMetrics(String name, boolean success, long durationNs, Exception error) {
        this.name = name;
        this.success = success;
        this.durationNs = durationNs;
        this.error = error;
        this.startTimeNs = System.nanoTime() - durationNs;
    }
    
    public SubsystemMetrics(String name, long startTimeNs, long endTimeNs, Exception error) {
        this.name = name;
        this.success = (error == null);
        this.durationNs = endTimeNs - startTimeNs;
        this.error = error;
        this.startTimeNs = startTimeNs;
    }
    
    /**
     * 🎯 OBTENER NOMBRE DEL SUBSISTEMA
     */
    public String getName() {
        return name;
    }
    
    /**
     * ✅ VERIFICAR SI LA INICIALIZACIÓN FUE EXITOSA
     */
    public boolean isSuccess() {
        return success;
    }
    
    /**
     * ⏱️ OBTENER DURACIÓN EN NANOSEGUNDOS
     */
    public long getDurationNs() {
        return durationNs;
    }
    
    /**
     * ⏱️ OBTENER DURACIÓN EN MILISEGUNDOS
     */
    public long getDurationMs() {
        return durationNs / 1_000_000;
    }
    
    /**
     * ⏱️ OBTENER DURACIÓN EN MICROSEGUNDOS
     */
    public long getDurationMicros() {
        return durationNs / 1_000;
    }
    
    /**
     * ❌ OBTENER ERROR (SI EXISTE)
     */
    public Exception getError() {
        return error;
    }
    
    /**
     * 🎯 OBTENER MENSAJE DE ERROR DESCRIPTIVO
     */
    public String getErrorMessage() {
        return error != null ? error.getMessage() : "Sin errores";
    }
    
    /**
     * 📊 OBTENER ESTADÍSTICAS FORMATADAS
     */
    public String getFormattedStats() {
        if (success) {
            return String.format("✅ %s: %dms", name, getDurationMs());
        } else {
            return String.format("❌ %s: ERROR - %s (%dms)", 
                    name, getErrorMessage(), getDurationMs());
        }
    }
    
    /**
     * 🎯 OBTENER VELOCIDAD RELATIVA (1.0 = promedio)
     */
    public double getRelativeSpeed() {
        // Benchmark: 10ms promedio por subsistema
        final double benchmarkMs = 10.0;
        return benchmarkMs / getDurationMs();
    }
    
    /**
     * 📊 COMPARAR CON OTRO SUBSISTEMA
     */
    public int compareSpeed(SubsystemMetrics other) {
        return Long.compare(other.getDurationNs(), this.getDurationNs());
    }
    
    @Override
    public String toString() {
        return getFormattedStats();
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        
        SubsystemMetrics that = (SubsystemMetrics) obj;
        return name.equals(that.name) && durationNs == that.durationNs;
    }
    
    @Override
    public int hashCode() {
        return name.hashCode() * 31 + Long.hashCode(durationNs);
    }
}