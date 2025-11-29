package io.warmup.examples.startup.critical;

import io.warmup.framework.startup.critical.ServiceDataClasses.ServiceCriticality;
import io.warmup.framework.startup.critical.ServiceDataClasses.ServiceState;
import io.warmup.framework.startup.critical.ServiceDataClasses.CriticalSeparationMetrics;
import io.warmup.framework.startup.critical.ServiceDataClasses.ServiceInfo;

import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;
import java.util.stream.Collectors;
import java.time.Instant;
import java.time.Duration;
import java.util.logging.Logger;
import java.util.logging.Level;

/**
 * 📊 MÉTRICAS AVANZADAS DEL SISTEMA DE SEPARACIÓN CRÍTICA
 * 
 * Proporciona métricas detalladas, reportes ejecutivos y análisis de rendimiento
 * para el sistema de separación de servicios críticos vs no críticos.
 * 
 * Características:
 * - Métricas en tiempo real
 * - Reportes ejecutivos
 * - Análisis de tendencias
 * - Alertas de performance
 * 
 * @author MiniMax Agent
 * @version 1.0
 */
public class CriticalSeparationMetricsAdvanced {
    
    private static final Logger log = Logger.getLogger(CriticalSeparationMetricsAdvanced.class.getName());
    
    private final CriticalSeparationMetrics baseMetrics;
    private final Map<String, ServiceMetrics> serviceMetricsMap = new ConcurrentHashMap<>();
    private final Map<ServiceCriticality, Long> criticalityStartTimes = new ConcurrentHashMap<>();
    private final Map<ServiceState, AtomicLong> stateTransitionCounts = new ConcurrentHashMap<>();
    
    // Métricas de performance
    private final AtomicLong totalCriticalTimeSpent = new AtomicLong(0);
    private final AtomicLong totalBackgroundTimeSpent = new AtomicLong(0);
    private final AtomicLong totalRetries = new AtomicLong(0);
    private final AtomicLong totalTimeouts = new AtomicLong(0);
    
    // Métricas de memoria y recursos
    private final AtomicLong estimatedMemoryUsage = new AtomicLong(0);
    private final AtomicLong peakMemoryUsage = new AtomicLong(0);
    
    // Métricas de usuario
    private final AtomicLong firstResponseTime = new AtomicLong(0);
    private final AtomicLong fullReadinessTime = new AtomicLong(0);
    
    public CriticalSeparationMetricsAdvanced(CriticalSeparationMetrics baseMetrics) {
        this.baseMetrics = baseMetrics;
        
        // Inicializar contadores de transición de estado
        for (ServiceState state : ServiceState.values()) {
            stateTransitionCounts.put(state, new AtomicLong(0));
        }
    }
    
    /**
     * 📊 REGISTRAR INICIO DE CRITICIDAD
     */
    public void recordCriticalityStart(ServiceCriticality criticality) {
        criticalityStartTimes.put(criticality, System.currentTimeMillis());
    }
    
    /**
     * 📊 REGISTRAR TRANSICIÓN DE ESTADO
     */
    public void recordStateTransition(ServiceInfo service, ServiceState fromState, ServiceState toState) {
        String serviceId = service.getServiceId();
        
        // Registrar en métricas del servicio
        ServiceMetrics serviceMetrics = serviceMetricsMap.computeIfAbsent(serviceId, 
            k -> new ServiceMetrics(service));
        
        serviceMetrics.recordStateTransition(fromState, toState);
        
        // Actualizar contadores globales
        if (stateTransitionCounts.containsKey(toState)) {
            stateTransitionCounts.get(toState).incrementAndGet();
        }
        
        log.log(Level.FINE, "Service {0} transitioned from {1} to {2}", 
                new Object[]{serviceId, fromState, toState});
    }
    
    /**
     * ⏱️ REGISTRAR TIEMPO DE RESPUESTA INICIAL
     */
    public void recordFirstResponseTime(long timeMs) {
        firstResponseTime.compareAndSet(0, timeMs);
    }
    
    /**
     * ⏱️ REGISTRAR TIEMPO DE COMPLETA PREPARACIÓN
     */
    public void recordFullReadinessTime(long timeMs) {
        fullReadinessTime.set(timeMs);
    }
    
    /**
     * 🔄 REGISTRAR REINTENTO
     */
    public void recordRetry(ServiceInfo service) {
        totalRetries.incrementAndGet();
        
        ServiceMetrics serviceMetrics = serviceMetricsMap.get(service.getServiceId());
        if (serviceMetrics != null) {
            serviceMetrics.incrementRetries();
        }
    }
    
    /**
     * ⏰ REGISTRAR TIMEOUT
     */
    public void recordTimeout(ServiceInfo service) {
        totalTimeouts.incrementAndGet();
        
        ServiceMetrics serviceMetrics = serviceMetricsMap.get(service.getServiceId());
        if (serviceMetrics != null) {
            serviceMetrics.incrementTimeouts();
        }
    }
    
    /**
     * 💾 ESTIMAR USO DE MEMORIA
     */
    public void estimateMemoryUsage(long bytes) {
        long current = estimatedMemoryUsage.addAndGet(bytes);
        
        // Actualizar pico de memoria
        while (true) {
            long currentPeak = peakMemoryUsage.get();
            if (current <= currentPeak || peakMemoryUsage.compareAndSet(currentPeak, current)) {
                break;
            }
        }
    }
    
    /**
     * 🎯 CLASIFICAR PERFORMANCE DE UN SERVICIO
     */
    public ServicePerformanceGrade gradeServicePerformance(ServiceInfo service) {
        ServiceMetrics metrics = serviceMetricsMap.get(service.getServiceId());
        if (metrics == null) {
            return ServicePerformanceGrade.UNKNOWN;
        }
        
        double performanceScore = metrics.calculatePerformanceScore();
        
        if (performanceScore >= 0.9) {
            return ServicePerformanceGrade.EXCELLENT;
        } else if (performanceScore >= 0.8) {
            return ServicePerformanceGrade.GOOD;
        } else if (performanceScore >= 0.6) {
            return ServicePerformanceGrade.ACCEPTABLE;
        } else if (performanceScore >= 0.4) {
            return ServicePerformanceGrade.POOR;
        } else {
            return ServicePerformanceGrade.CRITICAL;
        }
    }
    
    /**
     * 📈 GENERAR REPORTE EJECUTIVO
     */
    public String generateExecutiveReport() {
        StringBuilder sb = new StringBuilder();
        
        sb.append("\n🏆 === EXECUTIVE REPORT: CRITICAL SERVICE SEPARATION ===\n");
        sb.append(String.format("Report Generated: %s\n", Instant.now()));
        
        // Métricas clave de negocio
        sb.append("\n📊 KEY BUSINESS METRICS:\n");
        sb.append(String.format("• First Response Time: %dms (%s)\n", 
                firstResponseTime.get(), 
                firstResponseTime.get() <= 2 ? "✅ TARGET ACHIEVED" : "⚠️ TARGET MISSED"));
        sb.append(String.format("• Full Readiness Time: %dms\n", fullReadinessTime.get()));
        sb.append(String.format("• Application Usable: %s\n", 
                firstResponseTime.get() > 0 ? "✅ YES" : "❌ NO"));
        
        // Métricas de performance
        sb.append("\n⚡ PERFORMANCE METRICS:\n");
        sb.append(String.format("• Critical Phase Duration: %dms\n", baseMetrics.getCriticalPhaseDurationMs()));
        sb.append(String.format("• Background Phase Duration: %dms\n", baseMetrics.getBackgroundPhaseDurationMs()));
        sb.append(String.format("• Critical Services Success Rate: %.1f%%\n", 
                baseMetrics.getCriticalServicesSuccessRate() * 100));
        sb.append(String.format("• Non-Critical Services Success Rate: %.1f%%\n", 
                baseMetrics.getNonCriticalServicesSuccessRate() * 100));
        
        // Métricas de calidad
        sb.append("\n🎯 QUALITY METRICS:\n");
        sb.append(String.format("• Total Services: %d\n", baseMetrics.getTotalServicesCount()));
        sb.append(String.format("• Critical Services Loaded: %d\n", baseMetrics.getCriticalServicesLoaded()));
        sb.append(String.format("• Non-Critical Services Loaded: %d\n", baseMetrics.getNonCriticalServicesLoaded()));
        sb.append(String.format("• Failed Services: %d\n", 
                baseMetrics.getCriticalServicesFailed() + baseMetrics.getNonCriticalServicesFailed()));
        sb.append(String.format("• Retry Count: %d\n", totalRetries.get()));
        sb.append(String.format("• Timeout Count: %d\n", totalTimeouts.get()));
        
        // Métricas de recursos
        sb.append("\n💾 RESOURCE METRICS:\n");
        sb.append(String.format("• Estimated Memory Usage: %.2f MB\n", estimatedMemoryUsage.get() / 1024.0 / 1024.0));
        sb.append(String.format("• Peak Memory Usage: %.2f MB\n", peakMemoryUsage.get() / 1024.0 / 1024.0));
        
        // Distribución por criticidad
        sb.append("\n📋 SERVICE DISTRIBUTION BY CRITICALITY:\n");
        Map<ServiceCriticality, Long> criticalityDistribution = serviceMetricsMap.values().stream()
            .collect(Collectors.groupingBy(
                sm -> sm.service.getCriticality(),
                Collectors.counting()
            ));
        
        for (Map.Entry<ServiceCriticality, Long> entry : criticalityDistribution.entrySet()) {
            sb.append(String.format("• %s: %d services\n", entry.getKey(), entry.getValue()));
        }
        
        // Distribución por estado
        sb.append("\n🔄 SERVICE STATE DISTRIBUTION:\n");
        for (Map.Entry<ServiceState, Long> entry : getCurrentStateDistribution().entrySet()) {
            if (entry.getValue() > 0) {
                sb.append(String.format("• %s: %d services\n", entry.getKey(), entry.getValue()));
            }
        }
        
        // Alertas y recomendaciones
        sb.append("\n🚨 ALERTS & RECOMMENDATIONS:\n");
        List<String> alerts = generateAlerts();
        List<String> recommendations = generateRecommendations();
        
        if (alerts.isEmpty()) {
            sb.append("• ✅ No critical alerts\n");
        } else {
            for (String alert : alerts) {
                sb.append(String.format("• ⚠️ %s\n", alert));
            }
        }
        
        if (!recommendations.isEmpty()) {
            for (String recommendation : recommendations) {
                sb.append(String.format("• 💡 %s\n", recommendation));
            }
        }
        
        // Score general
        sb.append("\n🏅 OVERALL SYSTEM SCORE:\n");
        double overallScore = calculateOverallSystemScore();
        sb.append(String.format("Overall Performance Score: %.2f/10.0 (%s)\n", 
                overallScore, getOverallScoreGrade(overallScore)));
        
        return sb.toString();
    }
    
    /**
     * 📈 GENERAR REPORTE TÉCNICO DETALLADO
     */
    public String generateTechnicalReport() {
        StringBuilder sb = new StringBuilder();
        
        sb.append("\n🔧 === TECHNICAL REPORT: CRITICAL SERVICE SEPARATION ===\n");
        sb.append(String.format("Generated: %s\n", Instant.now()));
        
        // Métricas base
        sb.append("\n📊 BASE METRICS:\n");
        sb.append(baseMetrics.generateSummaryReport());
        
        // Métricas de servicios individuales
        sb.append("\n🔍 INDIVIDUAL SERVICE METRICS:\n");
        for (ServiceMetrics serviceMetrics : serviceMetricsMap.values()) {
            sb.append(String.format("\n--- Service: %s ---\n", serviceMetrics.service.getServiceId()));
            sb.append(serviceMetrics.generateDetailedReport());
        }
        
        // Transiciones de estado
        sb.append("\n🔄 STATE TRANSITION ANALYSIS:\n");
        for (Map.Entry<ServiceState, AtomicLong> entry : stateTransitionCounts.entrySet()) {
            long count = entry.getValue().get();
            if (count > 0) {
                sb.append(String.format("%s: %d transitions\n", entry.getKey(), count));
            }
        }
        
        // Métricas de criticidad
        sb.append("\n⏱️ CRITICALITY TIMING ANALYSIS:\n");
        for (Map.Entry<ServiceCriticality, Long> entry : criticalityStartTimes.entrySet()) {
            if (entry.getValue() > 0) {
                long duration = System.currentTimeMillis() - entry.getValue();
                sb.append(String.format("%s: %dms since start\n", entry.getKey(), duration));
            }
        }
        
        // Métricas avanzadas
        sb.append("\n📈 ADVANCED METRICS:\n");
        sb.append(String.format("Total Critical Time Spent: %dms\n", totalCriticalTimeSpent.get()));
        sb.append(String.format("Total Background Time Spent: %dms\n", totalBackgroundTimeSpent.get()));
        sb.append(String.format("Total Retries: %d\n", totalRetries.get()));
        sb.append(String.format("Total Timeouts: %d\n", totalTimeouts.get()));
        sb.append(String.format("Estimated Memory Usage: %d bytes\n", estimatedMemoryUsage.get()));
        sb.append(String.format("Peak Memory Usage: %d bytes\n", peakMemoryUsage.get()));
        
        return sb.toString();
    }
    
    /**
     * 📊 GENERAR ALERTAS
     */
    private List<String> generateAlerts() {
        List<String> alerts = new ArrayList<>();
        
        // Alertas de performance
        if (firstResponseTime.get() > 2) {
            alerts.add(String.format("First response time exceeded target: %dms > 2ms", 
                    firstResponseTime.get()));
        }
        
        if (baseMetrics.getCriticalServicesFailed() > 0) {
            alerts.add(String.format("Critical services failed: %d", 
                    baseMetrics.getCriticalServicesFailed()));
        }
        
        if (baseMetrics.getCriticalPhaseDurationMs() > 2) {
            alerts.add(String.format("Critical phase duration exceeded: %dms > 2ms", 
                    baseMetrics.getCriticalPhaseDurationMs()));
        }
        
        // Alertas de calidad
        double criticalSuccessRate = baseMetrics.getCriticalServicesSuccessRate();
        if (criticalSuccessRate < 0.95) {
            alerts.add(String.format("Low critical services success rate: %.1f%%", 
                    criticalSuccessRate * 100));
        }
        
        // Alertas de recursos
        long memoryMB = estimatedMemoryUsage.get() / 1024 / 1024;
        if (memoryMB > 100) {
            alerts.add(String.format("High memory usage: %.2f MB", memoryMB / 1024.0));
        }
        
        return alerts;
    }
    
    /**
     * 💡 GENERAR RECOMENDACIONES
     */
    private List<String> generateRecommendations() {
        List<String> recommendations = new ArrayList<>();
        
        // Recomendaciones de optimización
        if (totalRetries.get() > baseMetrics.getTotalServicesCount() * 0.1) {
            recommendations.add("Consider reducing service complexity to minimize retries");
        }
        
        if (totalTimeouts.get() > 0) {
            recommendations.add("Review timeout configurations for better reliability");
        }
        
        // Recomendaciones de distribución
        long criticalCount = serviceMetricsMap.values().stream()
            .filter(sm -> sm.service.getCriticality().isCritical())
            .count();
            
        if (criticalCount < 3) {
            recommendations.add("Consider marking more services as critical for better functionality");
        } else if (criticalCount > 10) {
            recommendations.add("Too many critical services may impact startup time");
        }
        
        // Recomendaciones de memoria
        if (peakMemoryUsage.get() > estimatedMemoryUsage.get() * 1.5) {
            recommendations.add("High memory usage variance detected - review memory management");
        }
        
        return recommendations;
    }
    
    /**
     * 🎯 CALCULAR SCORE GENERAL DEL SISTEMA
     */
    private double calculateOverallSystemScore() {
        double score = 10.0;
        
        // Penalizar por tiempo de respuesta
        if (firstResponseTime.get() > 2) {
            score -= 2.0;
        } else if (firstResponseTime.get() > 1) {
            score -= 1.0;
        }
        
        // Penalizar por fallos críticos
        score -= baseMetrics.getCriticalServicesFailed() * 0.5;
        
        // Penalizar por baja tasa de éxito
        double criticalSuccessRate = baseMetrics.getCriticalServicesSuccessRate();
        score -= (1.0 - criticalSuccessRate) * 2.0;
        
        // Penalizar por reintentos y timeouts
        score -= totalRetries.get() * 0.1;
        score -= totalTimeouts.get() * 0.2;
        
        // Recompensar por uso eficiente de memoria
        if (estimatedMemoryUsage.get() < 50 * 1024 * 1024) { // < 50MB
            score += 0.5;
        }
        
        return Math.max(0.0, Math.min(10.0, score));
    }
    
    /**
     * 🏆 OBTENER GRADO DEL SCORE GENERAL
     */
    private String getOverallScoreGrade(double score) {
        if (score >= 9.0) return "EXCELLENT";
        if (score >= 8.0) return "VERY GOOD";
        if (score >= 7.0) return "GOOD";
        if (score >= 6.0) return "ACCEPTABLE";
        if (score >= 4.0) return "POOR";
        return "CRITICAL";
    }
    
    /**
     * 📊 OBTENER DISTRIBUCIÓN ACTUAL DE ESTADOS
     */
    private Map<ServiceState, Long> getCurrentStateDistribution() {
        return serviceMetricsMap.values().stream()
            .collect(Collectors.groupingBy(
                sm -> sm.service.getState(),
                Collectors.counting()
            ));
    }
    
    /**
     * 📋 CLASE INTERNA: MÉTRICAS DE SERVICIO INDIVIDUAL
     */
    private static class ServiceMetrics {
        final ServiceInfo service;
        final AtomicLong stateTransitionCount = new AtomicLong(0);
        final AtomicLong retryCount = new AtomicLong(0);
        final AtomicLong timeoutCount = new AtomicLong(0);
        long firstTransitionTime;
        long lastTransitionTime;
        
        ServiceMetrics(ServiceInfo service) {
            this.service = service;
        }
        
        void recordStateTransition(ServiceState fromState, ServiceState toState) {
            long currentTime = System.currentTimeMillis();
            
            if (firstTransitionTime == 0) {
                firstTransitionTime = currentTime;
            }
            lastTransitionTime = currentTime;
            stateTransitionCount.incrementAndGet();
        }
        
        void incrementRetries() {
            retryCount.incrementAndGet();
        }
        
        void incrementTimeouts() {
            timeoutCount.incrementAndGet();
        }
        
        double calculatePerformanceScore() {
            double score = 1.0;
            
            // Penalizar por múltiples transiciones
            if (stateTransitionCount.get() > 3) {
                score -= 0.2;
            }
            
            // Penalizar por reintentos
            score -= retryCount.get() * 0.1;
            
            // Penalizar por timeouts
            score -= timeoutCount.get() * 0.2;
            
            // Recompensar por estar en estado READY
            if (service.getState() == ServiceState.READY) {
                score += 0.1;
            }
            
            return Math.max(0.0, Math.min(1.0, score));
        }
        
        String generateDetailedReport() {
            StringBuilder sb = new StringBuilder();
            
            sb.append(String.format("Service ID: %s\n", service.getServiceId()));
            sb.append(String.format("Service Name: %s\n", service.getServiceName()));
            sb.append(String.format("Criticality: %s\n", service.getCriticality()));
            sb.append(String.format("Current State: %s\n", service.getState()));
            sb.append(String.format("State Transitions: %d\n", stateTransitionCount.get()));
            sb.append(String.format("Retries: %d\n", retryCount.get()));
            sb.append(String.format("Timeouts: %d\n", timeoutCount.get()));
            sb.append(String.format("Performance Score: %.2f\n", calculatePerformanceScore()));
            sb.append(String.format("First Transition: %s\n", 
                    firstTransitionTime > 0 ? Instant.ofEpochMilli(firstTransitionTime).toString() : "N/A"));
            sb.append(String.format("Last Transition: %s\n", 
                    lastTransitionTime > 0 ? Instant.ofEpochMilli(lastTransitionTime).toString() : "N/A"));
            
            return sb.toString();
        }
    }
    
    /**
     * 🏆 ENUM: GRADOS DE PERFORMANCE DE SERVICIOS
     */
    public enum ServicePerformanceGrade {
        EXCELLENT("Excelente"),
        GOOD("Bueno"),
        ACCEPTABLE("Aceptable"),
        POOR("Pobre"),
        CRITICAL("Crítico"),
        UNKNOWN("Desconocido");
        
        private final String description;
        
        ServicePerformanceGrade(String description) {
            this.description = description;
        }
        
        public String getDescription() {
            return description;
        }
    }
}