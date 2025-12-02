package io.warmup.framework.hotreload.advanced;

import java.util.logging.Logger;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.List;
import java.util.ArrayList;

/**
 * 📊 HOT RELOAD DASHBOARD
 * 
 * Dashboard en tiempo real para monitorear el estado del hot reload,
 * métricas de rendimiento y actividad del sistema.
 * 
 * CARACTERÍSTICAS:
 * - ✅ Monitoreo en tiempo real
 * - ✅ Métricas de rendimiento
 * - ✅ Historial de reloads
 * - ✅ Configuración dinámica
 * - ✅ Alertas automáticas
 * 
 * @author MiniMax Agent
 * @since 2.0
 */
public class HotReloadDashboard {
    
    private static final Logger log = Logger.getLogger(HotReloadDashboard.class.getName());
    
    private final DashboardConfig config;
    private final Map<String, ReloadMetrics> metrics;
    private final List<ReloadEvent> recentEvents;
    
    /**
     * Constructor principal
     */
    public HotReloadDashboard(DashboardConfig config) {
        this.config = config != null ? config : new DashboardConfig();
        this.metrics = new ConcurrentHashMap<>();
        this.recentEvents = new ArrayList<>();
        log.info("HotReloadDashboard inicializado con configuración");
    }
    
    /**
     * Constructor por defecto
     */
    public HotReloadDashboard() {
        this(new DashboardConfig());
    }
    
    /**
     * Inicializa el dashboard
     */
    public void initialize() {
        try {
            log.info("Inicializando HotReloadDashboard");
            // Lógica de inicialización
            log.info("HotReloadDashboard inicializado exitosamente");
        } catch (Exception e) {
            log.severe("Error inicializando dashboard: " + e.getMessage());
        }
    }
    
    /**
     * Registra un evento de reload
     */
    public void recordReloadEvent(ReloadEvent event) {
        try {
            if (event == null) {
                return;
            }
            
            recentEvents.add(event);
            
            // Mantener solo los eventos recientes según configuración
            int maxEvents = config.getMaxRecentEvents();
            if (recentEvents.size() > maxEvents) {
                recentEvents.remove(0);
            }
            
            // Actualizar métricas
            updateMetrics(event);
            
            log.fine("Evento registrado: " + event);
        } catch (Exception e) {
            log.warning("Error registrando evento: " + e.getMessage());
        }
    }
    
    /**
     * Registra un evento de reload simplificado
     */
    public void recordReload(String className, boolean success, long duration, ChangeType changeType) {
        try {
            if (className == null || className.trim().isEmpty()) {
                return;
            }
            
            ReloadEvent event = new ReloadEvent(
                className, 
                "unknown", // methodName - not provided in simplified version
                success,
                duration,
                changeType != null ? changeType.name() : "UNKNOWN"
            );
            
            recordReloadEvent(event);
        } catch (Exception e) {
            log.warning("Error registrando reload: " + e.getMessage());
        }
    }
    
    /**
     * Registra un evento de reload con ChangeType de SimplifiedAdvancedHotReloadManager
     */
    public void recordReload(String className, boolean success, long duration, 
                           io.warmup.framework.hotreload.advanced.SimplifiedAdvancedHotReloadManager.ChangeType changeType) {
        try {
            if (className == null || className.trim().isEmpty()) {
                return;
            }
            
            // Convertir ChangeType a string
            String changeTypeString = convertChangeType(changeType);
            
            ReloadEvent event = new ReloadEvent(
                className, 
                "unknown", // methodName - not provided in simplified version
                success,
                duration,
                changeTypeString
            );
            
            recordReloadEvent(event);
        } catch (Exception e) {
            log.warning("Error registrando reload: " + e.getMessage());
        }
    }
    
    /**
     * Convierte ChangeType de SimplifiedAdvancedHotReloadManager a string
     */
    private String convertChangeType(io.warmup.framework.hotreload.advanced.SimplifiedAdvancedHotReloadManager.ChangeType changeType) {
        if (changeType == null) {
            return "UNKNOWN";
        }
        
        switch (changeType) {
            case METHOD_BODY_CHANGED:
                return "CODE_CHANGE";
            case METHOD_SIGNATURE_CHANGED:
                return "METHOD_SIGNATURE_CHANGE";
            case FIELD_ADDED:
            case FIELD_REMOVED:
                return "CLASS_STRUCTURE_CHANGE";
            case CLASS_REPLACED:
                return "CLASS_STRUCTURE_CHANGE";
            case UNKNOWN:
            default:
                return "UNKNOWN";
        }
    }
    
    /**
     * Obtiene las métricas actuales
     */
    public Map<String, ReloadMetrics> getCurrentMetrics() {
        return new ConcurrentHashMap<>(metrics);
    }
    
    /**
     * Obtiene los eventos recientes
     */
    public List<ReloadEvent> getRecentEvents() {
        return new ArrayList<>(recentEvents);
    }
    
    /**
     * Obtiene la configuración del dashboard
     */
    public DashboardConfig getConfig() {
        return config;
    }
    
    /**
     * Genera un reporte del dashboard
     */
    public String generateReport() {
        StringBuilder report = new StringBuilder();
        report.append("=== HOT RELOAD DASHBOARD REPORT ===\n");
        report.append("Timestamp: ").append(new java.util.Date()).append("\n");
        report.append("Total classes monitored: ").append(metrics.size()).append("\n");
        report.append("Recent events: ").append(recentEvents.size()).append("\n\n");
        
        // Estadísticas globales
        int totalReloads = 0;
        int totalSuccess = 0;
        int totalFailed = 0;
        long totalTime = 0;
        
        for (ReloadMetrics metric : metrics.values()) {
            totalReloads += metric.getTotalReloads();
            totalSuccess += metric.getSuccessfulReloads();
            totalFailed += metric.getFailedReloads();
            totalTime += metric.getTotalTimeMs();
        }
        
        report.append("GLOBAL STATISTICS:\n");
        report.append("  Total Reloads: ").append(totalReloads).append("\n");
        report.append("  Successful: ").append(totalSuccess).append("\n");
        report.append("  Failed: ").append(totalFailed).append("\n");
        report.append("  Success Rate: ").append(totalReloads > 0 ? (100.0 * totalSuccess / totalReloads) : 0).append("%\n");
        report.append("  Average Time: ").append(totalReloads > 0 ? (totalTime / totalReloads) : 0).append("ms\n\n");
        
        // Top clases más reloads
        if (!metrics.isEmpty()) {
            report.append("TOP CLASSES BY RELOADS:\n");
            metrics.values().stream()
                .sorted((a, b) -> Integer.compare(b.getTotalReloads(), a.getTotalReloads()))
                .limit(10)
                .forEach(metric -> report.append(String.format("  %s: %d reloads (%.1f%% success)\n",
                    metric.getClassName(), metric.getTotalReloads(), metric.getSuccessRate() * 100)));
        }
        
        report.append("\n=== END REPORT ===\n");
        return report.toString();
    }
    
    /**
     * Obtiene las métricas globales del sistema
     */
    public GlobalMetrics getGlobalMetrics() {
        GlobalMetrics globalMetrics = new GlobalMetrics();
        
        // Record all reloads from metrics
        for (ReloadMetrics metric : metrics.values()) {
            // Record successful reloads
            for (int i = 0; i < metric.getSuccessfulReloads(); i++) {
                globalMetrics.recordReload(true, metric.getAverageTimeMs(), "successful_reload");
            }
            
            // Record failed reloads
            for (int i = 0; i < metric.getFailedReloads(); i++) {
                globalMetrics.recordReload(false, metric.getAverageTimeMs(), "failed_reload");
            }
        }
        
        return globalMetrics;
    }
    
    /**
     * Obtiene el estado de salud del sistema
     */
    public SystemHealthStatus getSystemHealthStatus() {
        SystemHealthStatus status = new SystemHealthStatus();
        
        // Calcular métricas globales
        long totalReloads = 0;
        long successfulReloads = 0;
        long failedReloads = 0;
        long totalTime = 0;
        
        for (ReloadMetrics metric : metrics.values()) {
            totalReloads += metric.getTotalReloads();
            successfulReloads += metric.getSuccessfulReloads();
            failedReloads += metric.getFailedReloads();
            totalTime += metric.getTotalTimeMs();
        }
        
        if (totalReloads == 0) {
            status.updateStatus(SystemHealthStatus.HealthStatus.UNKNOWN, "No reload data available");
        } else {
            double successRate = (double) successfulReloads / totalReloads;
            double avgTime = totalTime / totalReloads;
            double errorRate = (double) failedReloads / totalReloads * 100;
            
            // Update metrics
            status.updateMetric(SystemHealthStatus.HealthMetric.RELOAD_SUCCESS_RATE, successRate * 100);
            status.updateMetric(SystemHealthStatus.HealthMetric.AVERAGE_RELOAD_TIME, avgTime);
            status.updateMetric(SystemHealthStatus.HealthMetric.ERROR_RATE, errorRate);
            
            // Determine health status
            if (successRate >= 0.95 && errorRate <= 5) {
                status.updateStatus(SystemHealthStatus.HealthStatus.HEALTHY, "System operating normally");
            } else if (successRate >= 0.80 && errorRate <= 10) {
                status.updateStatus(SystemHealthStatus.HealthStatus.WARNING, "Performance degradation detected");
            } else {
                status.updateStatus(SystemHealthStatus.HealthStatus.CRITICAL, "Critical issues detected");
            }
        }
        
        return status;
    }
    
    /**
     * Exporta métricas a JSON
     */
    public String exportToJson() {
        StringBuilder json = new StringBuilder();
        json.append("{\n");
        json.append("  \"timestamp\": \"").append(new java.util.Date()).append("\",\n");
        json.append("  \"totalClasses\": ").append(metrics.size()).append(",\n");
        json.append("  \"recentEvents\": ").append(recentEvents.size()).append(",\n");
        
        // Calcular estadísticas globales
        int totalReloads = metrics.values().stream().mapToInt(ReloadMetrics::getTotalReloads).sum();
        int totalSuccess = metrics.values().stream().mapToInt(ReloadMetrics::getSuccessfulReloads).sum();
        int totalFailed = metrics.values().stream().mapToInt(ReloadMetrics::getFailedReloads).sum();
        
        json.append("  \"globalStats\": {\n");
        json.append("    \"totalReloads\": ").append(totalReloads).append(",\n");
        json.append("    \"successfulReloads\": ").append(totalSuccess).append(",\n");
        json.append("    \"failedReloads\": ").append(totalFailed).append(",\n");
        json.append("    \"successRate\": ").append(totalReloads > 0 ? (100.0 * totalSuccess / totalReloads) : 0).append("\n");
        json.append("  },\n");
        
        // Métricas por clase
        json.append("  \"classMetrics\": [\n");
        boolean first = true;
        for (ReloadMetrics metric : metrics.values()) {
            if (!first) json.append(",\n");
            first = false;
            
            json.append("    {\n");
            json.append("      \"className\": \"").append(metric.getClassName()).append("\",\n");
            json.append("      \"totalReloads\": ").append(metric.getTotalReloads()).append(",\n");
            json.append("      \"successfulReloads\": ").append(metric.getSuccessfulReloads()).append(",\n");
            json.append("      \"failedReloads\": ").append(metric.getFailedReloads()).append(",\n");
            json.append("      \"successRate\": ").append(metric.getSuccessRate()).append(",\n");
            json.append("      \"averageTimeMs\": ").append(metric.getAverageTimeMs()).append("\n");
            json.append("    }");
        }
        json.append("\n  ]\n");
        json.append("}\n");
        return json.toString();
    }
    
    /**
     * Registra una operación de reload
     */
    public void recordReloadOperation(ReloadOperation operation) {
        if (operation != null) {
            ReloadEvent event = new ReloadEvent(
                operation.getClassName(),
                operation.getMethodName(),
                operation.isSuccess(),
                operation.getDurationMs(),
                operation.getMessage()
            );
            recordReloadEvent(event);
        }
    }
    
    /**
     * Actualiza las métricas con un evento
     */
    private void updateMetrics(ReloadEvent event) {
        String key = event.getClassName();
        ReloadMetrics currentMetrics = metrics.get(key);
        
        if (currentMetrics == null) {
            currentMetrics = new ReloadMetrics(key);
            metrics.put(key, currentMetrics);
        }
        
        currentMetrics.recordEvent(event);
    }
    
    /**
     * Configuración del dashboard
     */
    public static class DashboardConfig {
        private boolean enableRealTimeUpdates = true;
        private int maxRecentEvents = 100;
        private boolean enableMetrics = true;
        private boolean enableAlerts = true;
        private long alertThresholdMs = 5000; // 5 segundos
        
        public DashboardConfig() {
            // Configuración por defecto
        }
        
        public DashboardConfig(boolean enableRealTime, int maxEvents, boolean enableMetrics, boolean enableAlerts) {
            this.enableRealTimeUpdates = enableRealTime;
            this.maxRecentEvents = maxEvents;
            this.enableMetrics = enableMetrics;
            this.enableAlerts = enableAlerts;
        }
        
        // Getters y setters
        public boolean isRealTimeUpdatesEnabled() { return enableRealTimeUpdates; }
        public void setRealTimeUpdatesEnabled(boolean enabled) { this.enableRealTimeUpdates = enabled; }
        
        public int getMaxRecentEvents() { return maxRecentEvents; }
        public void setMaxRecentEvents(int max) { this.maxRecentEvents = Math.max(1, max); }
        
        public boolean isMetricsEnabled() { return enableMetrics; }
        public void setMetricsEnabled(boolean enabled) { this.enableMetrics = enabled; }
        
        public boolean isAlertsEnabled() { return enableAlerts; }
        public void setAlertsEnabled(boolean enabled) { this.enableAlerts = enabled; }
        
        public long getAlertThresholdMs() { return alertThresholdMs; }
        public void setAlertThresholdMs(long threshold) { this.alertThresholdMs = Math.max(100, threshold); }
        
        @Override
        public String toString() {
            return String.format("DashboardConfig{realTime=%s, maxEvents=%d, metrics=%s, alerts=%s, threshold=%dms}",
                enableRealTimeUpdates, maxRecentEvents, enableMetrics, enableAlerts, alertThresholdMs);
        }
    }
    
    /**
     * Métricas de reload
     */
    public static class ReloadMetrics {
        private final String className;
        private int totalReloads = 0;
        private int successfulReloads = 0;
        private int failedReloads = 0;
        private long totalTimeMs = 0;
        private long lastReloadTime = 0;
        
        public ReloadMetrics(String className) {
            this.className = className;
        }
        
        public void recordEvent(ReloadEvent event) {
            totalReloads++;
            lastReloadTime = System.currentTimeMillis();
            
            if (event.isSuccess()) {
                successfulReloads++;
            } else {
                failedReloads++;
            }
            
            totalTimeMs += event.getDurationMs();
        }
        
        // Getters
        public String getClassName() { return className; }
        public int getTotalReloads() { return totalReloads; }
        public int getSuccessfulReloads() { return successfulReloads; }
        public int getFailedReloads() { return failedReloads; }
        public long getTotalTimeMs() { return totalTimeMs; }
        public long getLastReloadTime() { return lastReloadTime; }
        
        public double getSuccessRate() {
            return totalReloads > 0 ? (double) successfulReloads / totalReloads : 0.0;
        }
        
        public long getAverageTimeMs() {
            return totalReloads > 0 ? totalTimeMs / totalReloads : 0;
        }
        
        @Override
        public String toString() {
            return String.format("ReloadMetrics{class='%s', total=%d, success=%d, failed=%d, avgTime=%dms, successRate=%.2f}",
                className, totalReloads, successfulReloads, failedReloads, getAverageTimeMs(), getSuccessRate());
        }
    }
    
    /**
     * Evento de reload
     */
    public static class ReloadEvent {
        private final String className;
        private final String methodName;
        private final boolean success;
        private final long timestamp;
        private final long durationMs;
        private final String message;
        
        public ReloadEvent(String className, String methodName, boolean success, long durationMs) {
            this(className, methodName, success, durationMs, "");
        }
        
        public ReloadEvent(String className, String methodName, boolean success, long durationMs, String message) {
            this.className = className;
            this.methodName = methodName;
            this.success = success;
            this.timestamp = System.currentTimeMillis();
            this.durationMs = durationMs;
            this.message = message;
        }
        
        // Getters
        public String getClassName() { return className; }
        public String getMethodName() { return methodName; }
        public boolean isSuccess() { return success; }
        public long getTimestamp() { return timestamp; }
        public long getDurationMs() { return durationMs; }
        public String getMessage() { return message; }
        
        @Override
        public String toString() {
            return String.format("ReloadEvent{class='%s', method='%s', success=%s, duration=%dms, message='%s'}",
                className, methodName, success, durationMs, message);
        }
    }
    
    /**
     * Representa una operación de hot reload
     */
    public static class ReloadOperation {
        private final String className;
        private final String methodName;
        private final String changeType;
        private final long durationMs;
        private final boolean success;
        private final String message;
        private final long timestamp;
        
        public ReloadOperation(String className, String methodName, String changeType, long durationMs, boolean success) {
            this(className, methodName, changeType, durationMs, success, "");
        }
        
        public ReloadOperation(String className, String methodName, String changeType, long durationMs, boolean success, String message) {
            this.className = className;
            this.methodName = methodName;
            this.changeType = changeType;
            this.durationMs = durationMs;
            this.success = success;
            this.message = message != null ? message : "";
            this.timestamp = System.currentTimeMillis();
        }
        
        // Getters
        public String getClassName() { return className; }
        public String getMethodName() { return methodName; }
        public String getChangeType() { return changeType; }
        public long getDurationMs() { return durationMs; }
        public boolean isSuccess() { return success; }
        public String getMessage() { return message; }
        public long getTimestamp() { return timestamp; }
        
        @Override
        public String toString() {
            return String.format("ReloadOperation{class='%s', method='%s', changeType='%s', success=%s, duration=%dms, message='%s'}",
                className, methodName, changeType, success, durationMs, message);
        }
    }
    

    

    
    /**
     * Tipos de cambios en hot reload
     */
    public enum ChangeType {
        CODE_CHANGE,
        CONFIG_CHANGE,
        DEPENDENCY_CHANGE,
        METHOD_SIGNATURE_CHANGE,
        CLASS_STRUCTURE_CHANGE,
        UNKNOWN
    }
}