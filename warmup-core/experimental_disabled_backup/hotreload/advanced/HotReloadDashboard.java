package io.warmup.framework.hotreload.advanced;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Dashboard de Monitoreo para Hot Reload Avanzado
 * Proporciona métricas en tiempo real y alertas del sistema
 * 
 * @author MiniMax Agent
 */
public class HotReloadDashboard {
    
    private static final Logger log = Logger.getLogger(HotReloadDashboard.class.getName());
    
    // Métricas del sistema
    private final AtomicLong totalReloads = new AtomicLong(0);
    private final AtomicLong successfulReloads = new AtomicLong(0);
    private final AtomicLong failedReloads = new AtomicLong(0);
    private final AtomicLong totalReloadTime = new AtomicLong(0);
    private final AtomicInteger activeReloads = new AtomicInteger(0);
    
    // Cache de métricas por clase
    private final Map<String, ClassMetrics> classMetrics;
    private final Map<String, Long> lastActivity;
    
    // Configuración de alertas
    private final AlertThresholds alertThresholds;
    
    public HotReloadDashboard() {
        this.classMetrics = new ConcurrentHashMap<>();
        this.lastActivity = new ConcurrentHashMap<>();
        this.alertThresholds = new AlertThresholds();
    }
    
    /**
     * Registra un reload en el dashboard
     */
    public void recordReload(String className, boolean success, long durationMs, 
                           SimplifiedAdvancedHotReloadManager.ChangeType changeType) {
        // Actualizar contadores globales
        totalReloads.incrementAndGet();
        totalReloadTime.addAndGet(durationMs);
        
        if (success) {
            successfulReloads.incrementAndGet();
        } else {
            failedReloads.incrementAndGet();
        }
        
        // Actualizar métricas por clase
        ClassMetrics metrics = classMetrics.computeIfAbsent(className, k -> new ClassMetrics());
        metrics.recordReload(success, durationMs, changeType);
        
        // Actualizar última actividad
        lastActivity.put(className, System.currentTimeMillis());
        
        // Verificar alertas
        checkAlerts();
        
        log.log(Level.FINE, "Reload registrado: {0} - {1} ({2}ms)", 
               new Object[]{className, success ? "ÉXITO" : "FALLO", durationMs});
    }
    
    /**
     * Registra métricas generales del sistema
     */
    public void recordMetrics(Map<String, Object> metrics) {
        // Procesar métricas adicionales si es necesario
        log.log(Level.FINE, "Métricas del sistema actualizadas: {0} entradas", metrics.size());
    }
    
    /**
     * Obtiene el estado actual del dashboard
     */
    public DashboardSnapshot getSnapshot() {
        return new DashboardSnapshot(
            getGlobalMetrics(),
            getClassMetricsMap(),
            getSystemHealthStatus(),
            System.currentTimeMillis()
        );
    }
    
    /**
     * Obtiene métricas globales
     */
    public GlobalMetrics getGlobalMetrics() {
        long total = totalReloads.get();
        long successful = successfulReloads.get();
        long failed = failedReloads.get();
        long totalTime = totalReloadTime.get();
        
        return new GlobalMetrics(
            total,
            successful,
            failed,
            total > 0 ? totalTime / total : 0,
            total > 0 ? (double) successful / total : 0.0,
            activeReloads.get(),
            getSystemUptime()
        );
    }
    
    /**
     * Obtiene métricas por clase
     */
    public Map<String, ClassMetrics> getClassMetricsMap() {
        return new HashMap<>(classMetrics);
    }
    
    /**
     * Obtiene el estado de salud del sistema
     */
    public SystemHealthStatus getSystemHealthStatus() {
        GlobalMetrics global = getGlobalMetrics();
        
        HealthLevel healthLevel = HealthLevel.HEALTHY;
        List<String> warnings = new ArrayList<>();
        
        // Verificar tasa de éxito
        if (global.getSuccessRate() < 0.90) {
            healthLevel = HealthLevel.WARNING;
            warnings.add("Tasa de éxito baja: " + String.format("%.1f%%", global.getSuccessRate() * 100));
        }
        
        // Verificar número de fallos recientes
        if (global.getFailedReloads() > 10) {
            healthLevel = HealthLevel.CRITICAL;
            warnings.add("Muchos fallos detectados: " + global.getFailedReloads());
        }
        
        // Verificar actividad reciente
        long timeSinceLastActivity = System.currentTimeMillis() - getLastActivityTime();
        if (timeSinceLastActivity > 300000) { // 5 minutos
            warnings.add("Sin actividad reciente");
        }
        
        return new SystemHealthStatus(healthLevel, warnings);
    }
    
    /**
     * Verifica alertas del sistema
     */
    private void checkAlerts() {
        GlobalMetrics metrics = getGlobalMetrics();
        
        // Alerta por tasa de éxito baja
        if (metrics.getSuccessRate() < alertThresholds.getMinSuccessRate()) {
            log.log(Level.WARNING, "ALERTA: Tasa de éxito baja ({0}%)", 
                   String.format("%.1f", metrics.getSuccessRate() * 100));
        }
        
        // Alerta por demasiados fallos
        if (metrics.getFailedReloads() > alertThresholds.getMaxFailedReloads()) {
            log.log(Level.WARNING, "ALERTA: Demasiados fallos ({0})", metrics.getFailedReloads());
        }
        
        // Alerta por tiempo de reload alto
        if (metrics.getAverageReloadTime() > alertThresholds.getMaxAverageReloadTime()) {
            log.log(Level.WARNING, "ALERTA: Tiempo de reload alto ({0}ms)", metrics.getAverageReloadTime());
        }
    }
    
    /**
     * Obtiene el tiempo desde la última actividad
     */
    private long getLastActivityTime() {
        return lastActivity.values().stream()
            .mapToLong(Long::longValue)
            .max()
            .orElse(0L);
    }
    
    /**
     * Obtiene el tiempo de actividad del sistema
     */
    private long getSystemUptime() {
        // En una implementación real, esto sería el tiempo desde que se inició el dashboard
        return System.currentTimeMillis();
    }
    
    /**
     * Exporta reporte en formato texto
     */
    public String exportReport() {
        GlobalMetrics global = getGlobalMetrics();
        StringBuilder report = new StringBuilder();
        
        report.append("=== HOT RELOAD DASHBOARD REPORT ===\n\n");
        
        // Métricas globales
        report.append("MÉTRICAS GLOBALES:\n");
        report.append(String.format("Total Reloads: %d\n", global.getTotalReloads()));
        report.append(String.format("Reloads Exitosos: %d\n", global.getSuccessfulReloads()));
        report.append(String.format("Reloads Fallidos: %d\n", global.getFailedReloads()));
        report.append(String.format("Tasa de Éxito: %.1f%%\n", global.getSuccessRate() * 100));
        report.append(String.format("Tiempo Promedio: %dms\n", global.getAverageReloadTime()));
        report.append(String.format("Reloads Activos: %d\n\n", global.getActiveReloads()));
        
        // Métricas por clase
        if (!classMetrics.isEmpty()) {
            report.append("MÉTRICAS POR CLASE:\n");
            for (Map.Entry<String, ClassMetrics> entry : classMetrics.entrySet()) {
                ClassMetrics cm = entry.getValue();
                report.append(String.format("%s:\n", entry.getKey()));
                report.append(String.format("  Total: %d, Éxito: %.1f%%, Tiempo Promedio: %dms\n",
                    cm.getTotalReloads(),
                    cm.getSuccessRate() * 100,
                    cm.getAverageReloadTime()
                ));
            }
            report.append("\n");
        }
        
        // Estado del sistema
        SystemHealthStatus health = getSystemHealthStatus();
        report.append("ESTADO DEL SISTEMA:\n");
        report.append(String.format("Salud General: %s\n", health.getHealthLevel()));
        if (!health.getWarnings().isEmpty()) {
            report.append("Advertencias:\n");
            for (String warning : health.getWarnings()) {
                report.append(String.format("  - %s\n", warning));
            }
        }
        
        return report.toString();
    }
    
    /**
     * Export dashboard metrics to JSON format
     * 
     * @return JSON string representation of metrics
     */
    public String exportToJson() {
        StringBuilder json = new StringBuilder();
        json.append("{");
        json.append("\"totalReloads\":").append(totalReloads.get()).append(",");
        json.append("\"successfulReloads\":").append(successfulReloads.get()).append(",");
        json.append("\"failedReloads\":").append(failedReloads.get()).append(",");
        json.append("\"activeReloads\":").append(activeReloads.get()).append(",");
        json.append("\"classesMonitored\":").append(classMetrics.size());
        json.append("}");
        return json.toString();
    }
    
    /**
     * Limpia métricas (útil para testing)
     */
    public void clearMetrics() {
        totalReloads.set(0);
        successfulReloads.set(0);
        failedReloads.set(0);
        totalReloadTime.set(0);
        activeReloads.set(0);
        classMetrics.clear();
        lastActivity.clear();
        
        log.log(Level.INFO, "Métricas del dashboard limpiadas");
    }
    
    /**
     * Configuración de umbrales para alertas
     */
    public static class AlertThresholds {
        private double minSuccessRate = 0.90;
        private int maxFailedReloads = 10;
        private long maxAverageReloadTime = 5000; // 5 segundos
        
        public double getMinSuccessRate() { return minSuccessRate; }
        public void setMinSuccessRate(double minSuccessRate) { this.minSuccessRate = minSuccessRate; }
        
        public int getMaxFailedReloads() { return maxFailedReloads; }
        public void setMaxFailedReloads(int maxFailedReloads) { this.maxFailedReloads = maxFailedReloads; }
        
        public long getMaxAverageReloadTime() { return maxAverageReloadTime; }
        public void setMaxAverageReloadTime(long maxAverageReloadTime) { this.maxAverageReloadTime = maxAverageReloadTime; }
    }
    
    /**
     * Snapshot del estado del dashboard
     */
    public static class DashboardSnapshot {
        private final GlobalMetrics globalMetrics;
        private final Map<String, ClassMetrics> classMetrics;
        private final SystemHealthStatus healthStatus;
        private final long timestamp;
        
        public DashboardSnapshot(GlobalMetrics globalMetrics, Map<String, ClassMetrics> classMetrics,
                               SystemHealthStatus healthStatus, long timestamp) {
            this.globalMetrics = globalMetrics;
            this.classMetrics = classMetrics;
            this.healthStatus = healthStatus;
            this.timestamp = timestamp;
        }
        
        public Map<String, ClassMetrics> getClassMetrics() { return classMetrics; }
        public SystemHealthStatus getHealthStatus() { return healthStatus; }
        public long getTimestamp() { return timestamp; }
    }
    
    /**
     * Métricas globales del sistema
     */
    public static class GlobalMetrics {
        public final long totalOperations;      // Alias para totalReloads para compatibilidad
        public final long successfulOperations; // Alias para successfulReloads para compatibilidad
        public final long failedOperations;     // Alias para failedReloads para compatibilidad
        public final double successRate;
        public final long averageReloadTime;
        public final long uptime;               // Alias para systemUptime para compatibilidad
        public final int activeReloads;
        public final long systemUptime;
        public final AsmStats asmStats;         // Estadísticas de ASM para compatibilidad
        
        // Campos privados originales para mantener funcionalidad
        private final long totalReloads;
        private final long successfulReloads;
        private final long failedReloads;
        
        public GlobalMetrics(long totalReloads, long successfulReloads, long failedReloads,
                           long averageReloadTime, double successRate, int activeReloads, long systemUptime) {
            // Asignar campos de compatibilidad
            this.totalOperations = totalReloads;
            this.successfulOperations = successfulReloads;
            this.failedOperations = failedReloads;
            this.successRate = successRate;
            this.averageReloadTime = averageReloadTime;
            this.uptime = systemUptime;
            this.activeReloads = activeReloads;
            this.systemUptime = systemUptime;
            
            // Mantener campos originales
            this.totalReloads = totalReloads;
            this.successfulReloads = successfulReloads;
            this.failedReloads = failedReloads;
            
            // Crear estadísticas ASM placeholder
            this.asmStats = new AsmStats(0, 0, 0);
        }
        
        public long getTotalReloads() { return totalReloads; }
        public long getSuccessfulReloads() { return successfulReloads; }
        public long getFailedReloads() { return failedReloads; }
        public long getAverageReloadTime() { return averageReloadTime; }
        public double getSuccessRate() { return successRate; }
        public int getActiveReloads() { return activeReloads; }
        public long getSystemUptime() { return systemUptime; }
    }
    
    /**
     * Estadísticas de ASM para compatibilidad
     */
    public static class AsmStats {
        public final int asmOptimizationsUsed;
        public final int reflectionCallsAvoided;
        public final int bytecodeTransformationsApplied;
        
        public AsmStats(int asmOptimizationsUsed, int reflectionCallsAvoided, int bytecodeTransformationsApplied) {
            this.asmOptimizationsUsed = asmOptimizationsUsed;
            this.reflectionCallsAvoided = reflectionCallsAvoided;
            this.bytecodeTransformationsApplied = bytecodeTransformationsApplied;
        }
    }
    
    /**
     * Métricas por clase
     */
    public static class ClassMetrics {
        private long totalReloads = 0;
        private long successfulReloads = 0;
        private long totalReloadTime = 0;
        private final Map<SimplifiedAdvancedHotReloadManager.ChangeType, Integer> changeTypeCounts = new HashMap<>();
        
        public void recordReload(boolean success, long duration, SimplifiedAdvancedHotReloadManager.ChangeType changeType) {
            totalReloads++;
            totalReloadTime += duration;
            
            if (success) {
                successfulReloads++;
            }
            
            changeTypeCounts.merge(changeType, 1, Integer::sum);
        }
        
        public long getTotalReloads() { return totalReloads; }
        public long getSuccessfulReloads() { return successfulReloads; }
        public long getTotalReloadTime() { return totalReloadTime; }
        public long getAverageReloadTime() { 
            return totalReloads > 0 ? totalReloadTime / totalReloads : 0; 
        }
        public double getSuccessRate() {
            return totalReloads > 0 ? (double) successfulReloads / totalReloads : 0.0;
        }
        public Map<SimplifiedAdvancedHotReloadManager.ChangeType, Integer> getChangeTypeCounts() { 
            return new HashMap<>(changeTypeCounts); 
        }
    }
    
    /**
     * Estado de salud del sistema
     */
    public static class SystemHealthStatus {
        private final HealthLevel healthLevel;
        private final List<String> warnings;
        
        public SystemHealthStatus(HealthLevel healthLevel, List<String> warnings) {
            this.healthLevel = healthLevel;
            this.warnings = new ArrayList<>(warnings);
        }
        
        public HealthLevel getHealthLevel() { return healthLevel; }
        public List<String> getWarnings() { return new ArrayList<>(warnings); }
    }
    
    /**
     * Generate a comprehensive report of hot reload metrics
     * 
     * @return report string with current system status and metrics
     */
    public String generateReport() {
        StringBuilder report = new StringBuilder();
        report.append("=== Hot Reload Dashboard Report ===\n");
        report.append("Total Reloads: ").append(totalReloads.get()).append("\n");
        report.append("Successful Reloads: ").append(successfulReloads.get()).append("\n");
        report.append("Failed Reloads: ").append(failedReloads.get()).append("\n");
        report.append("Active Reloads: ").append(activeReloads.get()).append("\n");
        
        if (totalReloads.get() > 0) {
            double successRate = (successfulReloads.get() * 100.0) / totalReloads.get();
            report.append("Success Rate: ").append(String.format("%.2f%%", successRate)).append("\n");
        }
        
        if (totalReloadTime.get() > 0) {
            double avgTime = totalReloadTime.get() / 1000.0 / totalReloads.get();
            report.append("Average Reload Time: ").append(String.format("%.2f ms", avgTime)).append("\n");
        }
        
        report.append("Classes Monitored: ").append(classMetrics.size()).append("\n");
        report.append("Last Activity: ").append(lastActivity.size()).append(" classes\n");
        
        return report.toString();
    }
    
    /**
     * Record a reload operation in the dashboard
     * 
     * @param operation the reload operation to record
     */
    public void recordReloadOperation(ReloadOperation operation) {
        totalReloads.incrementAndGet();
        if (operation.isSuccess()) {
            successfulReloads.incrementAndGet();
        } else {
            failedReloads.incrementAndGet();
        }
        totalReloadTime.addAndGet(operation.getDuration());
        
        log.info("Recorded reload operation: " + operation.getClassName() + "." + 
                 operation.getMethodName() + " - " + (operation.isSuccess() ? "SUCCESS" : "FAILED"));
    }
    
    /**
     * Nivel de salud del sistema
     */
    public enum HealthLevel {
        HEALTHY("Saludable"),
        WARNING("Advertencia"),
        CRITICAL("Crítico");
        
        private final String description;
        
        HealthLevel(String description) {
            this.description = description;
        }
        
        public String getDescription() { return description; }
        
        @Override
        public String toString() { return description; }
    }
    
    /**
     * Represents a single reload operation with metrics
     */
    public static class ReloadOperation {
        private final String className;
        private final String methodName;
        private final String changeType;
        private final long duration;
        private final boolean success;
        private final long timestamp;
        
        public ReloadOperation(String className, String methodName, String changeType, long duration, boolean success) {
            this.className = className;
            this.methodName = methodName;
            this.changeType = changeType;
            this.duration = duration;
            this.success = success;
            this.timestamp = System.currentTimeMillis();
        }
        
        public String getClassName() { return className; }
        public String getMethodName() { return methodName; }
        public String getChangeType() { return changeType; }
        public long getDuration() { return duration; }
        public boolean isSuccess() { return success; }
        public long getTimestamp() { return timestamp; }
    }
}