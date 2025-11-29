package io.warmup.examples.startup.critical;

import io.warmup.framework.startup.critical.ServiceDataClasses.ServiceCriticality;
import io.warmup.framework.startup.critical.ServiceDataClasses.ServiceInfo;
import io.warmup.framework.annotation.*;
import io.warmup.framework.startup.critical.ServiceDataClasses.ServiceCriticality;
import io.warmup.framework.startup.critical.ServiceDataClasses.ServiceInfo;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.AnnotatedElement;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;
import java.util.Set;
import java.util.HashSet;
import java.util.List;
import java.util.ArrayList;
import java.util.logging.Logger;
import java.util.logging.Level;
import java.util.regex.Pattern;

/**
 * 🎯 CLASIFICADOR DE CRITICIDAD DE SERVICIOS
 * 
 * Analiza y clasifica servicios en críticos y no críticos basado en:
 * - Anotaciones @Critical, @High, @Background, etc.
 * - Patrones de nombre (Database*, Cache*, Security*, etc.)
 * - Análisis de dependencias
 * - Estimación de tiempo de inicialización
 * 
 * @author MiniMax Agent
 * @version 1.0
 */
public class ServiceCriticalityClassifier {
    
    private static final Logger log = Logger.getLogger(ServiceCriticalityClassifier.class.getName());
    
    private final Map<String, ServiceInfo> serviceRegistry = new ConcurrentHashMap<>();
    private final Map<String, Set<String>> serviceDependencies = new ConcurrentHashMap<>();
    
    // Patrones para identificar automáticamente servicios críticos
    private static final Pattern CRITICAL_PATTERNS = Pattern.compile(
        "(?i)(database|security|auth|user|account|payment|cart|order|product|config|property|source|container|registry|dependency|injector)"
    );
    
    private static final Pattern HIGH_PRIORITY_PATTERNS = Pattern.compile(
        "(?i)(cache|monitoring|health|metrics|logger|logging|profile|profile|memory|jvm|runtime)"
    );
    
    private static final Pattern BACKGROUND_PATTERNS = Pattern.compile(
        "(?i)(background|task|scheduler|report|analytics|backup|cleanup|maintenance|watcher|listener|async)"
    );
    
    /**
     * 🎯 CLASIFICAR UN SERVICIO POR NOMBRE Y CLASE
     */
    public ServiceInfo classifyService(String serviceId, String serviceName, String serviceClass) {
        ServiceCriticality criticality;
        long estimatedInitTime;
        
        try {
            // Análisis primario por nombre del servicio
            criticality = classifyByName(serviceName, serviceClass);
            estimatedInitTime = estimateInitializationTime(serviceClass, criticality);
            
        } catch (Exception e) {
            log.log(Level.WARNING, "Error classifying service {0}: {1}", 
                    new Object[]{serviceId, e.getMessage()});
            // Fallback: clasificar como MEDIUM con tiempo estimado estándar
            criticality = ServiceCriticality.MEDIUM;
            estimatedInitTime = 50; // 50ms por defecto
        }
        
        ServiceInfo serviceInfo = new ServiceInfo(serviceId, serviceName, criticality, 
                                                estimatedInitTime, serviceClass);
        
        // Registrar en el registry
        serviceRegistry.put(serviceId, serviceInfo);
        
        log.log(Level.FINE, "Classified service {0} as {1} with init time {2}ms", 
                new Object[]{serviceId, criticality.getDescription(), estimatedInitTime});
        
        return serviceInfo;
    }
    
    /**
     * 🎯 CLASIFICAR POR NOMBRE Y PATRONES
     */
    private ServiceCriticality classifyByName(String serviceName, String serviceClass) {
        String name = serviceName.toLowerCase();
        String className = serviceClass.toLowerCase();
        
        // Verificar anotaciones primero
        ServiceCriticality annotationBased = classifyByAnnotations(serviceClass);
        if (annotationBased != null) {
            return annotationBased;
        }
        
        // Verificar patrones críticos
        if (CRITICAL_PATTERNS.matcher(name).find() || CRITICAL_PATTERNS.matcher(className).find()) {
            return ServiceCriticality.CRITICAL;
        }
        
        // Verificar patrones de alta prioridad
        if (HIGH_PRIORITY_PATTERNS.matcher(name).find() || HIGH_PRIORITY_PATTERNS.matcher(className).find()) {
            return ServiceCriticality.HIGH;
        }
        
        // Verificar patrones de background
        if (BACKGROUND_PATTERNS.matcher(name).find() || BACKGROUND_PATTERNS.matcher(className).find()) {
            return ServiceCriticality.BACKGROUND;
        }
        
        // Clasificación por sufijos conocidos
        if (serviceName.endsWith("Service") || serviceName.endsWith("Manager")) {
            return ServiceCriticality.MEDIUM;
        }
        
        if (serviceName.endsWith("Worker") || serviceName.endsWith("Handler")) {
            return ServiceCriticality.LOW;
        }
        
        if (serviceName.endsWith("BackgroundTask") || serviceName.endsWith("AsyncTask")) {
            return ServiceCriticality.BACKGROUND;
        }
        
        // Fallback: MEDIUM
        return ServiceCriticality.MEDIUM;
    }
    
    /**
     * 🔍 CLASIFICAR POR ANOTACIONES
     */
    private ServiceCriticality classifyByAnnotations(String serviceClass) {
        try {
            Class<?> clazz = Class.forName(serviceClass);
            
            // Buscar anotaciones de criticidad
            if (clazz.isAnnotationPresent(Critical.class)) {
                return ServiceCriticality.CRITICAL;
            }
            
            if (clazz.isAnnotationPresent(io.warmup.framework.annotation.HighPriority.class)) {
                return ServiceCriticality.HIGH;
            }
            
            if (clazz.isAnnotationPresent(io.warmup.framework.annotation.Background.class)) {
                return ServiceCriticality.BACKGROUND;
            }
            
            if (clazz.isAnnotationPresent(io.warmup.framework.annotation.LowPriority.class)) {
                return ServiceCriticality.LOW;
            }
            
            // Buscar anotaciones específicas de startup
            return classifyByStartupAnnotations(clazz);
            
        } catch (ClassNotFoundException e) {
            log.log(Level.FINE, "Could not load class {0} for annotation analysis", serviceClass);
            return null;
        } catch (Exception e) {
            log.log(Level.WARNING, "Error analyzing annotations for {0}: {1}", 
                    new Object[]{serviceClass, e.getMessage()});
            return null;
        }
    }
    
    /**
     * 🔍 CLASIFICAR POR ANOTACIONES DE STARTUP
     */
    private ServiceCriticality classifyByStartupAnnotations(Class<?> clazz) {
        // @PostConstruct - sugiere que es importante
        if (clazz.isAnnotationPresent(javax.annotation.PostConstruct.class)) {
            return ServiceCriticality.MEDIUM;
        }
        
        // @Component - componente estándar
        if (clazz.isAnnotationPresent(Component.class)) {
            return ServiceCriticality.MEDIUM;
        }
        
        // @Service - servicio estándar
        if (clazz.isAnnotationPresent(Service.class)) {
            return ServiceCriticality.MEDIUM;
        }
        
        // @Configuration - configuración crítica
        if (clazz.isAnnotationPresent(Configuration.class)) {
            return ServiceCriticality.HIGH;
        }
        
        return null;
    }
    
    /**
     * ⏱️ ESTIMAR TIEMPO DE INICIALIZACIÓN
     */
    private long estimateInitializationTime(String serviceClass, ServiceCriticality criticality) {
        try {
            Class<?> clazz = Class.forName(serviceClass);
            
            // Tiempo base por criticidad
            long baseTime = getBaseTimeByCriticality(criticality);
            
            // Ajustes por complejidad del servicio
            long complexityAdjustment = estimateComplexityAdjustment(clazz);
            
            // Ajustes por dependencias
            long dependencyAdjustment = estimateDependencyAdjustment(clazz);
            
            // Ajustes por recursos (DB, memoria, etc.)
            long resourceAdjustment = estimateResourceAdjustment(clazz);
            
            long totalTime = baseTime + complexityAdjustment + dependencyAdjustment + resourceAdjustment;
            
            // Asegurar mínimo y máximo razonables
            totalTime = Math.max(1, Math.min(totalTime, 10_000)); // 1ms - 10s
            
            return totalTime;
            
        } catch (Exception e) {
            log.log(Level.FINE, "Could not estimate init time for {0}, using default", serviceClass);
            return getBaseTimeByCriticality(criticality);
        }
    }
    
    /**
     * ⏱️ TIEMPO BASE POR CRITICIDAD
     */
    private long getBaseTimeByCriticality(ServiceCriticality criticality) {
        switch (criticality) {
            case CRITICAL:   return 1;   // 1ms - máximo
            case HIGH:       return 5;   // 5ms
            case MEDIUM:     return 20;  // 20ms
            case LOW:        return 50;  // 50ms
            case BACKGROUND: return 100; // 100ms
            default:         return 25;  // 25ms default
        }
    }
    
    /**
     * 🔍 ESTIMAR COMPLEJIDAD
     */
    private long estimateComplexityAdjustment(Class<?> clazz) {
        long adjustment = 0;
        
        // Métodos expensive en inicialización
        Method[] methods = clazz.getDeclaredMethods();
        for (Method method : methods) {
            String methodName = method.getName().toLowerCase();
            
            if (methodName.contains("init") || methodName.contains("start") || 
                methodName.contains("connect") || methodName.contains("load")) {
                adjustment += 10;
            }
            
            if (methodName.contains("connect") || methodName.contains("execute")) {
                adjustment += 20;
            }
            
            if (methodName.contains("load") || methodName.contains("build") || 
                methodName.contains("create")) {
                adjustment += 15;
            }
        }
        
        // Campos que sugieren complejidad
        Field[] fields = clazz.getDeclaredFields();
        for (Field field : fields) {
            String fieldName = field.getName().toLowerCase();
            String fieldType = field.getType().getSimpleName().toLowerCase();
            
            if (fieldName.contains("cache") || fieldName.contains("pool") || 
                fieldName.contains("queue") || fieldName.contains("manager")) {
                adjustment += 5;
            }
            
            if (fieldType.contains("connection") || fieldType.contains("session") ||
                fieldType.contains("factory")) {
                adjustment += 15;
            }
        }
        
        return adjustment;
    }
    
    /**
     * 🔗 ESTIMAR DEPENDENCIAS
     */
    private long estimateDependencyAdjustment(Class<?> clazz) {
        long adjustment = 0;
        
        try {
            // Contar inyecciones de dependencia
            Field[] fields = clazz.getDeclaredFields();
            for (Field field : fields) {
                if (field.isAnnotationPresent(Inject.class)) {
                    adjustment += 5;
                }
            }
            
            // Contar setters que sugieren dependencias
            Method[] methods = clazz.getDeclaredMethods();
            for (Method method : methods) {
                if (method.getName().startsWith("set") && method.getParameterCount() == 1) {
                    adjustment += 3;
                }
            }
            
        } catch (Exception e) {
            log.log(Level.FINE, "Could not estimate dependencies for {0}", clazz.getName());
        }
        
        return adjustment;
    }
    
    /**
     * 💾 ESTIMAR RECURSOS
     */
    private long estimateResourceAdjustment(Class<?> clazz) {
        long adjustment = 0;
        
        String className = clazz.getName().toLowerCase();
        
        // Recursos expensive
        if (className.contains("database") || className.contains("jdbc") || 
            className.contains("repository")) {
            adjustment += 100; // Database connections are expensive
        }
        
        if (className.contains("redis") || className.contains("cache")) {
            adjustment += 50; // Cache connections
        }
        
        if (className.contains("message") || className.contains("queue") || 
            className.contains("mq")) {
            adjustment += 75; // Message queues
        }
        
        if (className.contains("http") || className.contains("client") || 
            className.contains("service")) {
            adjustment += 25; // HTTP clients
        }
        
        if (className.contains("file") || className.contains("fs") || 
            className.contains("io")) {
            adjustment += 30; // File I/O
        }
        
        return adjustment;
    }
    
    /**
     * 📋 REGISTRAR DEPENDENCIA DE SERVICIO
     */
    public void registerServiceDependency(String serviceId, String dependsOn) {
        Set<String> dependencies = serviceDependencies.computeIfAbsent(serviceId, k -> new HashSet<>());
        dependencies.add(dependsOn);
        
        // Actualizar el ServiceInfo
        ServiceInfo serviceInfo = serviceRegistry.get(serviceId);
        if (serviceInfo != null) {
            serviceInfo.addDependency(dependsOn);
        }
    }
    
    /**
     * 🎯 CLASIFICAR LOTE DE SERVICIOS
     */
    public Map<String, ServiceInfo> classifyServicesBatch(List<ServiceInfo> services) {
        Map<String, ServiceInfo> classifiedServices = new ConcurrentHashMap<>();
        
        for (ServiceInfo service : services) {
            try {
                ServiceInfo classified = classifyService(
                    service.getServiceId(),
                    service.getServiceName(),
                    service.getServiceClass()
                );
                classifiedServices.put(service.getServiceId(), classified);
            } catch (Exception e) {
                log.log(Level.WARNING, "Failed to classify service {0}: {1}", 
                        new Object[]{service.getServiceId(), e.getMessage()});
                // Usar información original como fallback
                classifiedServices.put(service.getServiceId(), service);
            }
        }
        
        return classifiedServices;
    }
    
    /**
     * 🔍 OBTENER SERVICIOS POR CRITICIDAD
     */
    public List<ServiceInfo> getServicesByCriticality(ServiceCriticality criticality) {
        List<ServiceInfo> services = new ArrayList<>();
        
        for (ServiceInfo service : serviceRegistry.values()) {
            if (service.getCriticality() == criticality) {
                services.add(service);
            }
        }
        
        return services;
    }
    
    /**
     * 🔍 OBTENER SERVICIOS CRÍTICOS
     */
    public List<ServiceInfo> getCriticalServices() {
        return getServicesByCriticality(ServiceCriticality.CRITICAL);
    }
    
    /**
     * 🔍 OBTENER SERVICIOS NO CRÍTICOS
     */
    public List<ServiceInfo> getNonCriticalServices() {
        List<ServiceInfo> nonCritical = new ArrayList<>();
        
        for (ServiceInfo service : serviceRegistry.values()) {
            if (!service.getCriticality().isCritical()) {
                nonCritical.add(service);
            }
        }
        
        return nonCritical;
    }
    
    /**
     * 🔍 OBTENER SERVICIO POR ID
     */
    public ServiceInfo getServiceById(String serviceId) {
        return serviceRegistry.get(serviceId);
    }
    
    /**
     * 📊 GENERAR REPORTE DE CLASIFICACIÓN
     */
    public String generateClassificationReport() {
        StringBuilder sb = new StringBuilder();
        sb.append("\n=== SERVICE CRITICALITY CLASSIFICATION REPORT ===\n");
        sb.append(String.format("Total Services Classified: %d\n", serviceRegistry.size()));
        
        // Contar por criticidad
        Map<ServiceCriticality, Integer> countByCriticality = new ConcurrentHashMap<>();
        for (ServiceInfo service : serviceRegistry.values()) {
            ServiceCriticality criticality = service.getCriticality();
            countByCriticality.put(criticality, countByCriticality.getOrDefault(criticality, 0) + 1);
        }
        
        sb.append("\n=== SERVICES BY CRITICALITY ===\n");
        for (Map.Entry<ServiceCriticality, Integer> entry : countByCriticality.entrySet()) {
            sb.append(String.format("%s: %d services\n", entry.getKey(), entry.getValue()));
        }
        
        // Mostrar servicios críticos
        sb.append("\n=== CRITICAL SERVICES ===\n");
        for (ServiceInfo service : getCriticalServices()) {
            sb.append(String.format("- %s (%s) - Est: %dms\n", 
                    service.getServiceName(), service.getServiceId(), 
                    service.getEstimatedInitTimeMs()));
        }
        
        // Mostrar servicios de alta prioridad
        sb.append("\n=== HIGH PRIORITY SERVICES ===\n");
        for (ServiceInfo service : getServicesByCriticality(ServiceCriticality.HIGH)) {
            sb.append(String.format("- %s (%s) - Est: %dms\n", 
                    service.getServiceName(), service.getServiceId(), 
                    service.getEstimatedInitTimeMs()));
        }
        
        return sb.toString();
    }
    
    /**
     * 📊 ESTADÍSTICAS DE CLASIFICACIÓN
     */
    public Map<String, Object> getClassificationStatistics() {
        Map<String, Object> stats = new ConcurrentHashMap<>();
        
        stats.put("totalServices", serviceRegistry.size());
        
        for (ServiceCriticality criticality : ServiceCriticality.values()) {
            long count = serviceRegistry.values().stream()
                .filter(s -> s.getCriticality() == criticality)
                .count();
            stats.put(criticality.name().toLowerCase() + "Services", count);
        }
        
        // Tiempo total estimado
        long totalEstimatedTime = serviceRegistry.values().stream()
            .mapToLong(ServiceInfo::getEstimatedInitTimeMs)
            .sum();
        stats.put("totalEstimatedTimeMs", totalEstimatedTime);
        
        // Tiempo estimado solo para críticos
        long criticalTime = serviceRegistry.values().stream()
            .filter(ServiceInfo::isCritical)
            .mapToLong(ServiceInfo::getEstimatedInitTimeMs)
            .sum();
        stats.put("criticalEstimatedTimeMs", criticalTime);
        
        return stats;
    }
}