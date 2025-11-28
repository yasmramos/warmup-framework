package io.warmup.framework.startup.critical.examples;

import io.warmup.framework.startup.critical.*;
import io.warmup.framework.startup.critical.ServiceDataClasses.*;
import io.warmup.framework.core.WarmupContainer;
import io.warmup.framework.core.BeanRegistry;
import io.warmup.framework.core.DependencyRegistry;
import io.warmup.framework.annotation.*;

import java.util.*;
import java.util.concurrent.*;
import java.util.logging.Logger;
import java.util.logging.Level;
import java.util.stream.Collectors;

/**
 * 🚀 EJEMPLO COMPLETO DEL SISTEMA DE SEPARACIÓN CRÍTICA
 * 
 * Demuestra todos los aspectos del sistema de separación de servicios críticos:
 * - Clasificación automática de servicios
 * - Carga crítica en < 2ms
 * - Calentamiento asíncrono en segundo plano
 * - Monitoreo y métricas
 * - Manejo de errores
 * 
 * Casos de uso incluidos:
 * 1. Aplicación de e-commerce
 * 2. Sistema de microservicios
 * 3. API REST con cache
 * 4. Sistema de notificaciones
 * 
 * @author MiniMax Agent
 * @version 1.0
 */
public class CriticalSeparationExample {
    
    private static final Logger log = Logger.getLogger(CriticalSeparationExample.class.getName());
    
    private final WarmupContainer container;
    private final CriticalSeparationSystem separationSystem;
    private final List<ServiceInfo> demoServices = new ArrayList<>();
    
    public CriticalSeparationExample() {
        this.container = new WarmupContainer();
        this.separationSystem = new CriticalSeparationSystem(container);
        setupDemoServices();
    }
    
    /**
     * 🚀 EJEMPLO 1: E-COMMERCE CON SEPARACIÓN CRÍTICA
     */
    public void runEcommerceSeparationExample() {
        log.log(Level.INFO, "\n🛒 === EJEMPLO 1: E-COMMERCE CRITICAL SEPARATION ===");
        
        try {
            // Crear servicios típicos de e-commerce
            List<ServiceInfo> ecommerceServices = createEcommerceServices();
            
            // Ejecutar separación crítica
            CriticalSeparationResult result = separationSystem.executeCriticalSeparation(ecommerceServices);
            
            // Mostrar resultados
            log.log(Level.INFO, "E-commerce separation completed:");
            log.log(Level.INFO, result.generateReport());
            
            // Verificar que la aplicación es usable en < 2ms
            boolean usable = separationSystem.isApplicationUsable();
            log.log(Level.INFO, "Application usable in < 2ms: {0}", usable ? "✅ YES" : "❌ NO");
            
            // Monitorear calentamiento de background
            monitorBackgroundWarming();
            
        } catch (Exception e) {
            log.log(Level.SEVERE, "Error in e-commerce example: {0}", e.getMessage());
        }
    }
    
    /**
     * 🏢 EJEMPLO 2: MICROSERVICIOS CON PRIORIZACIÓN
     */
    public void runMicroservicesSeparationExample() {
        log.log(Level.INFO, "\n🏢 === EJEMPLO 2: MICROSERVICES CRITICAL SEPARATION ===");
        
        try {
            // Crear servicios de microservicios
            List<ServiceInfo> microservicesServices = createMicroservicesServices();
            
            // Usar configuración agresiva
            CriticalSeparationConfig aggressiveConfig = CriticalSeparationConfig.aggressive();
            CriticalSeparationSystem aggressiveSystem = new CriticalSeparationSystem(container, aggressiveConfig);
            
            CriticalSeparationResult result = aggressiveSystem.executeCriticalSeparation(microservicesServices);
            
            log.log(Level.INFO, "Microservices separation completed:");
            log.log(Level.INFO, result.generateReport());
            
            // Verificar tiempo de respuesta
            boolean fastResponse = separationSystem.isCriticalPhaseCompleted();
            log.log(Level.INFO, "Fast response achieved: {0}", fastResponse ? "✅ YES" : "❌ NO");
            
            aggressiveSystem.cleanup();
            
        } catch (Exception e) {
            log.log(Level.SEVERE, "Error in microservices example: {0}", e.getMessage());
        }
    }
    
    /**
     * 🌐 EJEMPLO 3: API REST CON CACHE ASÍNCRONO
     */
    public void runRestApiSeparationExample() {
        log.log(Level.INFO, "\n🌐 === EJEMPLO 3: REST API CRITICAL SEPARATION ===");
        
        try {
            // Crear servicios de API REST
            List<ServiceInfo> restApiServices = createRestApiServices();
            
            // Usar configuración conservadora
            CriticalSeparationConfig conservativeConfig = CriticalSeparationConfig.conservative();
            CriticalSeparationSystem conservativeSystem = new CriticalSeparationSystem(container, conservativeConfig);
            
            CriticalSeparationResult result = conservativeSystem.executeCriticalSeparation(restApiServices);
            
            log.log(Level.INFO, "REST API separation completed:");
            log.log(Level.INFO, result.generateReport());
            
            // Verificar aplicación completamente lista
            boolean fullyReady = result.isApplicationFullyReady();
            log.log(Level.INFO, "Application fully ready: {0}", fullyReady ? "✅ YES" : "❌ NO");
            
            conservativeSystem.cleanup();
            
        } catch (Exception e) {
            log.log(Level.SEVERE, "Error in REST API example: {0}", e.getMessage());
        }
    }
    
    /**
     * 📧 EJEMPLO 4: SISTEMA DE NOTIFICACIONES
     */
    public void runNotificationSeparationExample() {
        log.log(Level.INFO, "\n📧 === EJEMPLO 4: NOTIFICATION SYSTEM CRITICAL SEPARATION ===");
        
        try {
            // Crear servicios de notificaciones
            List<ServiceInfo> notificationServices = createNotificationServices();
            
            // Convertir lista a mapa para compatibilidad con la API
            Map<String, ServiceInfo> servicesMap = notificationServices.stream()
                .collect(Collectors.toMap(ServiceInfo::getServiceName, s -> s));
            
            // Ejecutar fase crítica únicamente
            Map<String, ServiceInfo> criticalOnly = separationSystem.executeCriticalPhaseSync(servicesMap);
            
            log.log(Level.INFO, "Critical phase completed with {0} services", criticalOnly.size());
            
            // Ejecutar calentamiento de background por separado
            List<ServiceInfo> nonCritical = notificationServices.stream()
                .filter(s -> !s.getCriticality().isCritical())
                .collect(ArrayList::new, ArrayList::add, ArrayList::addAll);
            
            Map<String, ServiceInfo> nonCriticalMap = nonCritical.stream()
                .collect(Collectors.toMap(ServiceInfo::getName, s -> s));
            
            separationSystem.executeBackgroundWarmingOnly(nonCriticalMap);
            
            log.log(Level.INFO, "Background warming initiated for {0} services", nonCritical.size());
            
            // Monitorear progreso
            while (!separationSystem.isBackgroundPhaseCompleted()) {
                log.log(Level.FINE, "Background warming progress: {0}%", 
                        getCurrentWarmingProgress());
                Thread.sleep(500);
            }
            
            log.log(Level.INFO, "Notification system separation completed");
            
        } catch (Exception e) {
            log.log(Level.SEVERE, "Error in notification example: {0}", e.getMessage());
        }
    }
    
    /**
     * ⚡ EJEMPLO 5: SEPARACIÓN ASÍNCRONA CON MONITOREO
     */
    public void runAsyncSeparationWithMonitoring() {
        log.log(Level.INFO, "\n⚡ === EJEMPLO 5: ASYNC SEPARATION WITH REAL-TIME MONITORING ===");
        
        try {
            List<ServiceInfo> complexServices = createComplexServices();
            
            Map<String, ServiceInfo> complexServicesMap = complexServices.stream()
                .collect(Collectors.toMap(ServiceInfo::getServiceName, s -> s));
            
            // Ejecutar fase crítica asíncronamente
            CompletableFuture<Map<String, ServiceInfo>> criticalFuture = 
                separationSystem.executeCriticalPhaseAsync(complexServicesMap);
            
            // Mostrar progreso mientras se ejecuta
            while (!criticalFuture.isDone()) {
                log.log(Level.INFO, "Critical phase in progress... Applications usable: {0}", 
                        separationSystem.isApplicationUsable());
                Thread.sleep(100);
            }
            
            // Obtener resultado
            Map<String, ServiceInfo> criticalServices = criticalFuture.get();
            
            log.log(Level.INFO, "Critical phase completed with {0} services", criticalServices.size());
            log.log(Level.INFO, "Application is now usable: {0}", 
                    separationSystem.isApplicationUsable() ? "✅ YES" : "❌ NO");
            
            // Iniciar fase de background y monitorear
            List<ServiceInfo> backgroundServices = complexServices.stream()
                .filter(s -> !criticalServices.containsKey(s.getServiceName()))
                .collect(ArrayList::new, ArrayList::add, ArrayList::addAll);
            
            Map<String, ServiceInfo> backgroundServicesMap = backgroundServices.stream()
                .collect(Collectors.toMap(ServiceInfo::getServiceName, s -> s));
            
            separationSystem.executeBackgroundWarmingOnly(backgroundServicesMap);
            
            // Monitorear en tiempo real
            monitorRealTimeProgress();
            
        } catch (Exception e) {
            log.log(Level.SEVERE, "Error in async separation example: {0}", e.getMessage());
        }
    }
    
    /**
     * 📊 EJEMPLO 6: ANÁLISIS COMPLETO DE MÉTRICAS
     */
    public void runComprehensiveMetricsAnalysis() {
        log.log(Level.INFO, "\n📊 === EJEMPLO 6: COMPREHENSIVE METRICS ANALYSIS ===");
        
        try {
            List<ServiceInfo> allServices = createMixedApplicationServices();
            
            CriticalSeparationResult result = separationSystem.executeCriticalSeparation(allServices);
            
            // Generar reporte completo del sistema
            log.log(Level.INFO, separationSystem.generateSystemReport());
            
            // Obtener métricas detalladas
            Map<String, Object> systemMetrics = separationSystem.getSystemMetrics();
            
            log.log(Level.INFO, "\n=== KEY METRICS SUMMARY ===");
            log.log(Level.INFO, "Total Services: {0}", systemMetrics.get("totalServices"));
            log.log(Level.INFO, "Critical Phase Duration: {0}ms", systemMetrics.get("criticalPhaseDurationMs"));
            log.log(Level.INFO, "Background Phase Duration: {0}ms", systemMetrics.get("backgroundPhaseDurationMs"));
            log.log(Level.INFO, "Application Usable: {0}", systemMetrics.get("applicationUsable"));
            log.log(Level.INFO, "Fully Ready: {0}", systemMetrics.get("fullyReady"));
            
            // Clasificar performance de servicios críticos
            Map<String, ServiceInfo> criticalServices = separationSystem.getLoadedCriticalServices();
            log.log(Level.INFO, "\n=== CRITICAL SERVICES PERFORMANCE ===");
            for (ServiceInfo service : criticalServices.values()) {
                log.log(Level.INFO, "Service: {0} - State: {1}", 
                        new Object[]{service.getServiceName(), service.getState()});
            }
            
        } catch (Exception e) {
            log.log(Level.SEVERE, "Error in metrics analysis example: {0}", e.getMessage());
        }
    }
    
    /**
     * 🧪 CONFIGURAR SERVICIOS DE DEMOSTRACIÓN
     */
    private void setupDemoServices() {
        demoServices.addAll(createMixedApplicationServices());
    }
    
    /**
     * 🛒 CREAR SERVICIOS DE E-COMMERCE
     */
    private List<ServiceInfo> createEcommerceServices() {
        return Arrays.asList(
            new ServiceInfo("userService", "UserManagementService", ServiceCriticality.CRITICAL, 1, 
                          "com.example.ecommerce.UserManagementService"),
            new ServiceInfo("productService", "ProductCatalogService", ServiceCriticality.CRITICAL, 2, 
                          "com.example.ecommerce.ProductCatalogService"),
            new ServiceInfo("cartService", "ShoppingCartService", ServiceCriticality.HIGH, 5, 
                          "com.example.ecommerce.ShoppingCartService"),
            new ServiceInfo("orderService", "OrderProcessingService", ServiceCriticality.HIGH, 10, 
                          "com.example.ecommerce.OrderProcessingService"),
            new ServiceInfo("paymentService", "PaymentGatewayService", ServiceCriticality.CRITICAL, 1, 
                          "com.example.ecommerce.PaymentGatewayService"),
            new ServiceInfo("inventoryService", "InventoryManagementService", ServiceCriticality.MEDIUM, 15, 
                          "com.example.ecommerce.InventoryManagementService"),
            new ServiceInfo("analyticsService", "AnalyticsService", ServiceCriticality.BACKGROUND, 20, 
                          "com.example.ecommerce.AnalyticsService"),
            new ServiceInfo("recommendationService", "RecommendationEngine", ServiceCriticality.LOW, 30, 
                          "com.example.ecommerce.RecommendationEngine"),
            new ServiceInfo("notificationService", "NotificationService", ServiceCriticality.MEDIUM, 10, 
                          "com.example.ecommerce.NotificationService"),
            new ServiceInfo("searchService", "SearchService", ServiceCriticality.HIGH, 5, 
                          "com.example.ecommerce.SearchService")
        );
    }
    
    /**
     * 🏢 CREAR SERVICIOS DE MICROSERVICIOS
     */
    private List<ServiceInfo> createMicroservicesServices() {
        return Arrays.asList(
            new ServiceInfo("apiGateway", "ApiGatewayService", ServiceCriticality.CRITICAL, 1, 
                          "com.example.microservice.ApiGatewayService"),
            new ServiceInfo("authService", "AuthenticationService", ServiceCriticality.CRITICAL, 2, 
                          "com.example.microservice.AuthenticationService"),
            new ServiceInfo("userMs", "UserMicroservice", ServiceCriticality.HIGH, 5, 
                          "com.example.microservice.UserMicroservice"),
            new ServiceInfo("orderMs", "OrderMicroservice", ServiceCriticality.HIGH, 8, 
                          "com.example.microservice.OrderMicroservice"),
            new ServiceInfo("inventoryMs", "InventoryMicroservice", ServiceCriticality.MEDIUM, 12, 
                          "com.example.microservice.InventoryMicroservice"),
            new ServiceInfo("notificationMs", "NotificationMicroservice", ServiceCriticality.MEDIUM, 10, 
                          "com.example.microservice.NotificationMicroservice"),
            new ServiceInfo("analyticsMs", "AnalyticsMicroservice", ServiceCriticality.LOW, 25, 
                          "com.example.microservice.AnalyticsMicroservice"),
            new ServiceInfo("monitoringService", "MonitoringService", ServiceCriticality.BACKGROUND, 15, 
                          "com.example.microservice.MonitoringService")
        );
    }
    
    /**
     * 🌐 CREAR SERVICIOS DE API REST
     */
    private List<ServiceInfo> createRestApiServices() {
        return Arrays.asList(
            new ServiceInfo("restController", "RestController", ServiceCriticality.CRITICAL, 1, 
                          "com.example.rest.RestController"),
            new ServiceInfo("userRepository", "UserRepository", ServiceCriticality.CRITICAL, 1, 
                          "com.example.rest.UserRepository"),
            new ServiceInfo("cacheManager", "CacheManager", ServiceCriticality.HIGH, 3, 
                          "com.example.rest.CacheManager"),
            new ServiceInfo("validator", "RequestValidator", ServiceCriticality.HIGH, 2, 
                          "com.example.rest.RequestValidator"),
            new ServiceInfo("databaseService", "DatabaseService", ServiceCriticality.HIGH, 10, 
                          "com.example.rest.DatabaseService"),
            new ServiceInfo("metricsCollector", "MetricsCollector", ServiceCriticality.MEDIUM, 5, 
                          "com.example.rest.MetricsCollector"),
            new ServiceInfo("loggingService", "LoggingService", ServiceCriticality.LOW, 8, 
                          "com.example.rest.LoggingService"),
            new ServiceInfo("backupService", "BackupService", ServiceCriticality.BACKGROUND, 20, 
                          "com.example.rest.BackupService")
        );
    }
    
    /**
     * 📧 CREAR SERVICIOS DE NOTIFICACIONES
     */
    private List<ServiceInfo> createNotificationServices() {
        return Arrays.asList(
            new ServiceInfo("notificationEngine", "NotificationEngine", ServiceCriticality.CRITICAL, 1, 
                          "com.example.notification.NotificationEngine"),
            new ServiceInfo("emailService", "EmailService", ServiceCriticality.HIGH, 8, 
                          "com.example.notification.EmailService"),
            new ServiceInfo("smsService", "SmsService", ServiceCriticality.MEDIUM, 12, 
                          "com.example.notification.SmsService"),
            new ServiceInfo("pushService", "PushNotificationService", ServiceCriticality.MEDIUM, 10, 
                          "com.example.notification.PushNotificationService"),
            new ServiceInfo("templateService", "TemplateService", ServiceCriticality.LOW, 15, 
                          "com.example.notification.TemplateService"),
            new ServiceInfo("queueService", "NotificationQueueService", ServiceCriticality.HIGH, 5, 
                          "com.example.notification.NotificationQueueService"),
            new ServiceInfo("analyticsService", "NotificationAnalytics", ServiceCriticality.BACKGROUND, 20, 
                          "com.example.notification.NotificationAnalytics")
        );
    }
    
    /**
     * 🧪 CREAR SERVICIOS COMPLEJOS
     */
    private List<ServiceInfo> createComplexServices() {
        return Arrays.asList(
            new ServiceInfo("coreService", "CoreService", ServiceCriticality.CRITICAL, 1, 
                          "com.example.complex.CoreService"),
            new ServiceInfo("configService", "ConfigService", ServiceCriticality.CRITICAL, 1, 
                          "com.example.complex.ConfigService"),
            new ServiceInfo("securityService", "SecurityService", ServiceCriticality.CRITICAL, 2, 
                          "com.example.complex.SecurityService"),
            new ServiceInfo("databaseService", "DatabaseService", ServiceCriticality.HIGH, 15, 
                          "com.example.complex.DatabaseService"),
            new ServiceInfo("cacheService", "CacheService", ServiceCriticality.HIGH, 5, 
                          "com.example.complex.CacheService"),
            new ServiceInfo("externalApi", "ExternalApiService", ServiceCriticality.MEDIUM, 25, 
                          "com.example.complex.ExternalApiService"),
            new ServiceInfo("messageQueue", "MessageQueueService", ServiceCriticality.MEDIUM, 20, 
                          "com.example.complex.MessageQueueService"),
            new ServiceInfo("workerService", "BackgroundWorkerService", ServiceCriticality.LOW, 30, 
                          "com.example.complex.BackgroundWorkerService"),
            new ServiceInfo("analyticsService", "AnalyticsService", ServiceCriticality.BACKGROUND, 40, 
                          "com.example.complex.AnalyticsService"),
            new ServiceInfo("reportingService", "ReportingService", ServiceCriticality.BACKGROUND, 50, 
                          "com.example.complex.ReportingService")
        );
    }
    
    /**
     * 🔀 CREAR SERVICIOS DE APLICACIÓN MIXTA
     */
    private List<ServiceInfo> createMixedApplicationServices() {
        List<ServiceInfo> services = new ArrayList<>();
        services.addAll(createEcommerceServices());
        services.addAll(createMicroservicesServices());
        services.addAll(createRestApiServices());
        services.addAll(createNotificationServices());
        services.addAll(createComplexServices());
        return services;
    }
    
    /**
     * 📊 MONITOREAR CALENTAMIENTO DE BACKGROUND
     */
    private void monitorBackgroundWarming() {
        log.log(Level.INFO, "Monitoring background warming progress...");
        
        try {
            int maxWaitSeconds = 30;
            int elapsedSeconds = 0;
            
            while (elapsedSeconds < maxWaitSeconds && !separationSystem.isBackgroundPhaseCompleted()) {
                double progress = getCurrentWarmingProgress();
                log.log(Level.INFO, "Background warming progress: {0}%", String.format("%.1f", progress));
                
                Thread.sleep(1000);
                elapsedSeconds++;
            }
            
            log.log(Level.INFO, "Background warming completed or timed out after {0} seconds", elapsedSeconds);
            
        } catch (InterruptedException e) {
            log.log(Level.WARNING, "Monitoring interrupted: {0}", e.getMessage());
            Thread.currentThread().interrupt();
        }
    }
    
    /**
     * 📊 OBTENER PROGRESO ACTUAL DE CALENTAMIENTO
     */
    private double getCurrentWarmingProgress() {
        Map<String, ServiceInfo> warmingServices = separationSystem.getWarmingServices();
        
        if (warmingServices.isEmpty()) {
            return 100.0; // No hay servicios calentándose, asumir completo
        }
        
        long readyCount = warmingServices.values().stream()
            .filter(ServiceInfo::isReady)
            .count();
        
        return (readyCount * 100.0) / warmingServices.size();
    }
    
    /**
     * 📊 MONITOREAR PROGRESO EN TIEMPO REAL
     */
    private void monitorRealTimeProgress() {
        log.log(Level.INFO, "Starting real-time monitoring...");
        
        try {
            while (!separationSystem.isFullyReady()) {
                Map<String, Object> metrics = separationSystem.getSystemMetrics();
                
                log.log(Level.INFO, "Real-time status - Usable: {0}, Critical Done: {0}, Background Done: {1}", 
                        new Object[]{
                            metrics.get("applicationUsable"),
                            metrics.get("backgroundPhaseCompleted")
                        });
                
                Thread.sleep(2000);
            }
            
            log.log(Level.INFO, "✅ All systems fully ready!");
            
        } catch (InterruptedException e) {
            log.log(Level.WARNING, "Real-time monitoring interrupted: {0}", e.getMessage());
            Thread.currentThread().interrupt();
        }
    }
    
    /**
     * 🚀 EJECUTAR TODOS LOS EJEMPLOS
     */
    public void runAllExamples() {
        log.log(Level.INFO, "🚀 INICIANDO TODOS LOS EJEMPLOS DE SEPARACIÓN CRÍTICA");
        
        try {
            // Ejecutar todos los ejemplos secuencialmente
            runEcommerceSeparationExample();
            Thread.sleep(1000);
            
            runMicroservicesSeparationExample();
            Thread.sleep(1000);
            
            runRestApiSeparationExample();
            Thread.sleep(1000);
            
            runNotificationSeparationExample();
            Thread.sleep(1000);
            
            runAsyncSeparationWithMonitoring();
            Thread.sleep(1000);
            
            runComprehensiveMetricsAnalysis();
            
            log.log(Level.INFO, "✅ TODOS LOS EJEMPLOS COMPLETADOS EXITOSAMENTE");
            
        } catch (Exception e) {
            log.log(Level.SEVERE, "❌ Error running examples: {0}", e.getMessage());
            e.printStackTrace();
        } finally {
            // Limpiar recursos
            separationSystem.cleanup();
        }
    }
    
    /**
     * 🧹 LIMPIAR RECURSOS
     */
    public void cleanup() {
        separationSystem.cleanup();
        demoServices.clear();
    }
    
    /**
     * 🎯 MÉTODO PRINCIPAL PARA DEMOSTRACIÓN
     */
    public static void main(String[] args) {
        log.log(Level.INFO, "🚀 INICIANDO DEMOSTRACIÓN DEL SISTEMA DE SEPARACIÓN CRÍTICA");
        
        try {
            CriticalSeparationExample example = new CriticalSeparationExample();
            
            // Ejecutar todos los ejemplos
            example.runAllExamples();
            
        } catch (Exception e) {
            log.log(Level.SEVERE, "Error in main demonstration: {0}", e.getMessage());
            e.printStackTrace();
        }
    }
}