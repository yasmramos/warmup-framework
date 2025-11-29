package io.warmup.examples.startup.config.examples;

import io.warmup.framework.startup.config.*;
import java.nio.file.Paths;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * Ejemplo completo del sistema de configuración precargada.
 * Demuestra:
 * - Precarga paralela de múltiples archivos de configuración
 * - Acceso instantáneo vía OS page cache
 * - Eliminación de parsing/IO durante runtime
 * - Métricas de optimización
 */
public class PreloadedConfigSystemExample {
    
    public static void main(String[] args) {
        System.out.println("=== DEMO: Sistema de Configuración Precargada ===\n");
        
        // Paso 1: Crear el sistema
        PreloadedConfigSystem configSystem = new PreloadedConfigSystem();
        
        // Paso 2: Registrar archivos de configuración adicionales
        registerAdditionalConfigurations(configSystem);
        
        // Paso 3: Iniciar precarga en paralelo
        System.out.println("🔄 Iniciando precarga de configuraciones...");
        CompletableFuture<PreloadedConfigSystem.PreloadResult> preloadFuture = 
            configSystem.preloadConfigurations();
        
        // Paso 4: Procesar resultados
        preloadFuture.thenAccept(result -> {
            System.out.println("\n✅ Precarga completada!");
            System.out.println("📊 Resultados: " + result);
            
            if (result.isAlreadyInProgress()) {
                System.out.println("⚠️  Sistema ya estaba procesando una precarga");
                return;
            }
            
            if (result.isAlreadyReady()) {
                System.out.println("⚠️  Sistema ya estaba listo");
                return;
            }
            
            // Paso 5: Demostrar acceso instantáneo
            demonstrateInstantAccess(configSystem);
            
            // Paso 6: Mostrar métricas de optimización
            showOptimizationMetrics(configSystem);
            
            // Paso 7: Demostrar eficiencia
            demonstrateEfficiency(configSystem);
            
            // Paso 8: Limpiar recursos
            configSystem.shutdown();
            System.out.println("\n🧹 Sistema limpiado y cerrado");
        });
        
        // Esperar completación (en aplicación real, esto sería asíncrono)
        try {
            preloadFuture.join();
        } catch (Exception e) {
            System.err.println("❌ Error durante precarga: " + e.getMessage());
        }
    }
    
    /**
     * Registra configuraciones adicionales del ejemplo
     */
    private static void registerAdditionalConfigurations(PreloadedConfigSystem configSystem) {
        System.out.println("📝 Registrando archivos de configuración adicionales...");
        
        // Crear algunos archivos de ejemplo si no existen
        createExampleConfigFiles();
        
        // Registrar archivos
        configSystem.registerConfiguration("database.properties", 
            Paths.get("src/main/resources/database.properties"));
        configSystem.registerConfiguration("api.config.json", 
            Paths.get("src/main/resources/api.config.json"));
        configSystem.registerConfiguration("cache.config", 
            Paths.get("src/main/resources/cache.config"));
        configSystem.registerConfiguration("feature-flags.yaml", 
            Paths.get("src/main/resources/feature-flags.yaml"));
        
        System.out.println("✅ Archivos registrados para precarga");
    }
    
    /**
     * Crea archivos de configuración de ejemplo
     */
    private static void createExampleConfigFiles() {
        try {
            java.nio.file.Files.createDirectories(
                java.nio.file.Paths.get("src/main/resources"));
            
            // database.properties
            String dbConfig = "database.url=jdbc:mysql://localhost:3306/warmup\n" +
                "database.username=admin\n" +
                "database.password=secret123\n" +
                "database.pool.size=20\n" +
                "database.timeout=5000\n" +
                "database.ssl.enabled=true\n";
            java.nio.file.Files.write(
                java.nio.file.Paths.get("src/main/resources/database.properties"), 
                dbConfig.getBytes());
            
            // api.config.json
            String apiConfig = "{\n" +
                "  \"api\": {\n" +
                "    \"baseUrl\": \"https://api.warmup.io\",\n" +
                "    \"version\": \"v1\",\n" +
                "    \"timeout\": 30000,\n" +
                "    \"retryCount\": 3,\n" +
                "    \"compression\": true\n" +
                "  },\n" +
                "  \"rateLimit\": {\n" +
                "    \"requests\": 1000,\n" +
                "    \"window\": 3600\n" +
                "  }\n" +
                "}\n";
            java.nio.file.Files.write(
                java.nio.file.Paths.get("src/main/resources/api.config.json"), 
                apiConfig.getBytes());
            
            // cache.config (texto plano)
            String cacheConfig = "cache.provider=redis\n" +
                "cache.host=localhost\n" +
                "cache.port=6379\n" +
                "cache.max.size=10000\n" +
                "cache.ttl=3600\n" +
                "cache.eviction.policy=lru\n";
            java.nio.file.Files.write(
                java.nio.file.Paths.get("src/main/resources/cache.config"), 
                cacheConfig.getBytes());
            
            // feature-flags.yaml
            String featureFlags = "features:\n" +
                "  new-ui: true\n" +
                "  api-v2: false\n" +
                "  experimental-ml: true\n" +
                "  beta-reporting: false\n" +
                "\n" +
                "toggles:\n" +
                "  maintenance-mode: false\n" +
                "  debug-logging: true\n" +
                "  performance-monitoring: true\n";
            java.nio.file.Files.write(
                java.nio.file.Paths.get("src/main/resources/feature-flags.yaml"), 
                featureFlags.getBytes());
            
            System.out.println("✅ Archivos de ejemplo creados");
            
        } catch (Exception e) {
            System.out.println("⚠️  No se pudieron crear archivos de ejemplo: " + e.getMessage());
        }
    }
    
    /**
     * Demuestra acceso instantáneo a configuraciones
     */
    private static void demonstrateInstantAccess(PreloadedConfigSystem configSystem) {
        System.out.println("\n🚀 DEMOSTRANDO ACCESO INSTANTÁNEO:");
        
        try {
            // Acceso a configuración de aplicación
            ConfigDataAccessor appConfig = configSystem.getConfigAccessor("application.properties");
            String appName = appConfig.getString("app.name", "Warmup Framework");
            String appVersion = appConfig.getString("app.version", "1.0.0");
            
            System.out.println("📱 Configuración de Aplicación:");
            System.out.println("   Nombre: " + appName);
            System.out.println("   Versión: " + appVersion);
            
            // Acceso a configuración de base de datos
            if (configSystem.isReady() && appConfig.hasKey("database.url")) {
                String dbUrl = appConfig.getString("database.url");
                Integer poolSize = appConfig.getInteger("database.pool.size", 10);
                
                System.out.println("\n🗄️  Configuración de Base de Datos:");
                System.out.println("   URL: " + dbUrl);
                System.out.println("   Pool Size: " + poolSize);
            }
            
            // Acceso múltiple
            Map<String, ConfigDataAccessor> multipleConfigs = configSystem.getConfigAccessors(
                "application.properties", "application.yml"
            );
            
            System.out.println("\n🔧 Configuraciones Cargadas:");
            for (String key : multipleConfigs.keySet()) {
                ConfigDataAccessor accessor = multipleConfigs.get(key);
                ConfigDataAccessor.ConfigInfo info = accessor.getConfigInfo();
                System.out.println("   " + info);
            }
            
        } catch (Exception e) {
            System.out.println("⚠️  Error accediendo configuraciones: " + e.getMessage());
        }
    }
    
    /**
     * Muestra métricas de optimización
     */
    private static void showOptimizationMetrics(PreloadedConfigSystem configSystem) {
        System.out.println("\n📊 MÉTRICAS DE OPTIMIZACIÓN:");
        
        PreloadOptimizationMetrics.OverallOptimizationStats overallStats = 
            configSystem.getOptimizationMetrics().getOverallStats();
        
        System.out.println("⏱️  Tiempo Total Ahorrado: " + overallStats.getFormattedTotalSavings());
        System.out.println("📁 Configuraciones Preloaded: " + overallStats.getTotalConfigsPreloaded());
        System.out.println("💾 Operaciones I/O Evitadas: " + overallStats.getTotalIooOperationsAvoided());
        System.out.println("🔄 Operaciones de Parsing Evitadas: " + overallStats.getTotalParsingOperationsAvoided());
        System.out.println("📈 Eficiencia Promedio: " + String.format("%.2f", overallStats.getAverageEfficiency()));
        
        // Métricas de throughput
        PreloadOptimizationMetrics.ThroughputMetrics throughput = overallStats.getThroughputMetrics();
        System.out.println("⚡ Throughput: " + throughput);
    }
    
    /**
     * Demuestra la eficiencia del sistema
     */
    private static void demonstrateEfficiency(PreloadedConfigSystem configSystem) {
        System.out.println("\n🎯 DEMOSTRANDO EFICIENCIA DEL SISTEMA:");
        
        // Simular acceso repetido para demostrar cache
        try {
            ConfigDataAccessor accessor = configSystem.getConfigAccessor("application.properties");
            
            System.out.println("🔄 Realizando 1000 accesos a la misma configuración...");
            
            long startTime = System.nanoTime();
            for (int i = 0; i < 1000; i++) {
                String value = accessor.getString("app.name", "Default");
                // El valor viene del cache después del primer acceso
            }
            long endTime = System.nanoTime();
            
            double totalTimeMs = (endTime - startTime) / 1_000_000.0;
            double avgTimeUs = (endTime - startTime) / 1000.0 / 1000;
            
            System.out.println("✅ 1000 accesos completados en " + String.format("%.2f", totalTimeMs) + " ms");
            System.out.println("⚡ Tiempo promedio por acceso: " + String.format("%.3f", avgTimeUs) + " µs");
            
            // Mostrar estadísticas de rendimiento
            ConfigDataAccessor.AccessPerformanceStats perfStats = accessor.getPerformanceStats();
            System.out.println("📈 Estadísticas de Rendimiento: " + perfStats);
            
            // Demostrar que no hay I/O durante runtime
            System.out.println("\n💡 BENEFICIOS CLAVE:");
            System.out.println("   ✅ Zero I/O durante acceso a configuración");
            System.out.println("   ✅ Zero parsing durante acceso a configuración");
            System.out.println("   ✅ Acceso directo via OS page cache");
            System.out.println("   ✅ Cache automático de valores frecuentes");
            System.out.println("   ✅ Thread-safe para entornos concurrentes");
            
        } catch (Exception e) {
            System.out.println("⚠️  Error en prueba de eficiencia: " + e.getMessage());
        }
    }
}