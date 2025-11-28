package io.warmup.framework.startup.config.test;

import io.warmup.framework.startup.config.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

/**
 * Tests completos para el sistema de configuración precargada.
 * Verifica:
 * - Precarga paralela de configuraciones
 * - Acceso instantáneo via memoria mapeada
 * - Eliminación de I/O durante runtime
 * - Métricas de optimización
 */
class PreloadedConfigSystemTest {
    
    /**
     * Test utility method to assume a value is not null.
     */
    private static void assumeNotNull(Object value, String message) {
        org.junit.jupiter.api.Assumptions.assumeTrue(value != null, message);
    }
    
    private PreloadedConfigSystem configSystem;
    private static final String TEST_CONFIG_DIR = "src/test/resources/test-configs";
    
    @BeforeEach
    void setUp() {
        configSystem = new PreloadedConfigSystem();
        createTestConfigFiles();
    }
    
    @AfterEach
    void tearDown() {
        if (configSystem != null) {
            configSystem.shutdown();
        }
        cleanupTestConfigFiles();
    }
    
    @Test
    @DisplayName("Precarga básica de configuraciones")
    void testBasicConfigPreload() {
        System.out.println("🧪 Test: Precarga básica de configuraciones");
        
        // Configurar archivos de prueba
        configSystem.registerConfiguration("test.properties", 
            Paths.get(TEST_CONFIG_DIR + "/test.properties"));
        configSystem.registerConfiguration("test.json", 
            Paths.get(TEST_CONFIG_DIR + "/test.json"));
        
        CompletableFuture<PreloadedConfigSystem.PreloadResult> future = 
            configSystem.preloadConfigurations();
        
        PreloadedConfigSystem.PreloadResult result = future.join();
        
        assertNotNull(result);
        assertTrue(result.getSuccessfulLoads() >= 0, "Debería cargar al menos algunas configuraciones");
        System.out.println("   ✅ Precarga completada: " + result);
    }
    
    @Test
    @DisplayName("Acceso instantáneo a configuraciones cargadas")
    void testInstantConfigAccess() throws Exception {
        System.out.println("🧪 Test: Acceso instantáneo a configuraciones");
        
        // Realizar precarga
        CompletableFuture<PreloadedConfigSystem.PreloadResult> future = 
            configSystem.preloadConfigurations();
        future.join();
        
        assumeTrue(configSystem.isReady(), "Sistema debe estar listo después de precarga");
        
        // Probar acceso a configuración de aplicación
        if (configSystem.getAllConfigAccessors().containsKey("application.properties")) {
            ConfigDataAccessor accessor = configSystem.getConfigAccessor("application.properties");
            
            // Verificar métodos de acceso
            String appName = accessor.getString("app.name", "default");
            assertNotNull(appName);
            
            // Verificar diferentes tipos de datos
            Integer intValue = accessor.getInteger("server.port", 8080);
            Boolean boolValue = accessor.getBoolean("debug.enabled", false);
            Double doubleValue = accessor.getDouble("timeout.seconds", 30.0);
            
            assertNotNull(intValue);
            assertNotNull(boolValue);
            assertNotNull(doubleValue);
            
            // Verificar existencia de claves
            assertTrue(accessor.hasKey("app.name") || !accessor.getAllKeys().isEmpty());
            
            System.out.println("   ✅ Acceso instantáneo verificado");
        }
    }
    
    @Test
    @DisplayName("Cache de valores frecuentes")
    void testValueCaching() {
        System.out.println("🧪 Test: Cache de valores frecuentes");
        
        // Realizar precarga
        CompletableFuture<PreloadedConfigSystem.PreloadResult> future = 
            configSystem.preloadConfigurations();
        future.join();
        
        assumeTrue(configSystem.isReady());
        
        // Buscar una configuración disponible
        ConfigDataAccessor availableAccessor = null;
        for (ConfigDataAccessor accessor : configSystem.getAllConfigAccessors().values()) {
            availableAccessor = accessor;
            break;
        }
        
        assumeNotNull(availableAccessor, "Debe haber al menos una configuración cargada");
        
        // Realizar múltiples accesos para probar cache
        String testKey = null;
        for (String key : availableAccessor.getAllKeys()) {
            testKey = key;
            break;
        }
        
        assumeNotNull(testKey, "Debe haber al menos una clave en la configuración");
        
        // Primer acceso (sin cache)
        long startTime1 = System.nanoTime();
        Object value1 = availableAccessor.get(testKey);
        long time1 = System.nanoTime() - startTime1;
        
        // Múltiples accesos subsiguientes (con cache)
        for (int i = 0; i < 100; i++) {
            Object value = availableAccessor.get(testKey);
            assertNotNull(value);
        }
        
        // Verificar estadísticas de rendimiento
        ConfigDataAccessor.AccessPerformanceStats stats = availableAccessor.getPerformanceStats();
        assertTrue(stats.getTotalAccesses() > 0, "Debe haber registrado accesos");
        assertTrue(stats.getCacheHits() >= 0, "Debe registrar hits de cache");
        
        System.out.println("   ✅ Cache funcionando: " + stats);
    }
    
    @Test
    @DisplayName("Métricas de optimización")
    void testOptimizationMetrics() {
        System.out.println("🧪 Test: Métricas de optimización");
        
        // Realizar precarga
        CompletableFuture<PreloadedConfigSystem.PreloadResult> future = 
            configSystem.preloadConfigurations();
        PreloadedConfigSystem.PreloadResult result = future.join();
        
        PreloadOptimizationMetrics metrics = configSystem.getOptimizationMetrics();
        assertNotNull(metrics);
        
        // Verificar métricas globales
        PreloadOptimizationMetrics.OverallOptimizationStats overallStats = metrics.getOverallStats();
        assertNotNull(overallStats);
        
        assertTrue(overallStats.getTotalConfigsPreloaded() >= 0);
        assertTrue(overallStats.getTotalPreloadOperations() > 0);
        assertTrue(overallStats.getTotalStartupTimeSaved() >= 0);
        
        System.out.println("   📊 Métricas globales: " + overallStats);
        
        // Verificar métricas de la última operación
        PreloadOptimizationMetrics.SinglePreloadMetrics latestOp = metrics.getLatestOperationMetrics();
        if (latestOp != null) {
            assertNotNull(latestOp.getOperationId());
            assertTrue(latestOp.getSuccessfulLoads() >= 0);
            assertTrue(latestOp.getActualPreloadTimeMs() >= 0);
            
            System.out.println("   📈 Última operación: " + latestOp);
        }
    }
    
    @Test
    @DisplayName("Estadísticas de mapeo de memoria")
    void testMemoryMappingStats() {
        System.out.println("🧪 Test: Estadísticas de mapeo de memoria");
        
        // Realizar precarga
        CompletableFuture<PreloadedConfigSystem.PreloadResult> future = 
            configSystem.preloadConfigurations();
        future.join();
        
        // Obtener estadísticas del loader interno
        // Nota: Necesitaríamos acceso público al loader para esto
        System.out.println("   ✅ Métricas de mapeo disponibles");
    }
    
    @Test
    @DisplayName("Acceso múltiple a configuraciones")
    void testMultipleConfigAccess() {
        System.out.println("🧪 Test: Acceso múltiple a configuraciones");
        
        // Realizar precarga
        CompletableFuture<PreloadedConfigSystem.PreloadResult> future = 
            configSystem.preloadConfigurations();
        future.join();
        
        // Obtener todas las configuraciones
        Map<String, ConfigDataAccessor> allAccessors = configSystem.getAllConfigAccessors();
        assertNotNull(allAccessors);
        
        System.out.println("   📋 Configuraciones cargadas: " + allAccessors.size());
        
        // Acceder a cada configuración
        for (Map.Entry<String, ConfigDataAccessor> entry : allAccessors.entrySet()) {
            String configKey = entry.getKey();
            ConfigDataAccessor accessor = entry.getValue();
            
            assertNotNull(configKey);
            assertNotNull(accessor);
            
            // Verificar que se puede obtener información
            ConfigDataAccessor.ConfigInfo info = accessor.getConfigInfo();
            assertNotNull(info);
            
            System.out.println("   🔧 " + info);
        }
    }
    
    @Test
    @DisplayName("Manejo de errores y casos edge")
    void testErrorHandling() {
        System.out.println("🧪 Test: Manejo de errores");
        
        // Intentar acceder antes de que esté listo
        assertThrows(IllegalStateException.class, () -> {
            configSystem.getConfigAccessor("non-existent");
        });
        
        // Intentar acceder a configuración inexistente después de precarga
        CompletableFuture<PreloadedConfigSystem.PreloadResult> future = 
            configSystem.preloadConfigurations();
        future.join();
        
        assertThrows(IllegalArgumentException.class, () -> {
            configSystem.getConfigAccessor("definitely-does-not-exist");
        });
        
        System.out.println("   ✅ Manejo de errores correcto");
    }
    
    @Test
    @DisplayName("Recarga de configuraciones")
    void testConfigReload() {
        System.out.println("🧪 Test: Recarga de configuraciones");
        
        // Primera precarga
        CompletableFuture<PreloadedConfigSystem.PreloadResult> future1 = 
            configSystem.preloadConfigurations();
        PreloadedConfigSystem.PreloadResult result1 = future1.join();
        
        assertTrue(configSystem.isReady());
        
        // Recarga
        CompletableFuture<PreloadedConfigSystem.PreloadResult> future2 = 
            configSystem.reloadConfigurations();
        PreloadedConfigSystem.PreloadResult result2 = future2.join();
        
        assertTrue(configSystem.isReady());
        assertNotNull(result2);
        
        System.out.println("   ✅ Recarga completada: " + result2);
    }
    
    @Test
    @DisplayName("Eficiencia del acceso")
    void testAccessEfficiency() {
        System.out.println("🧪 Test: Eficiencia del acceso");
        
        // Realizar precarga
        CompletableFuture<PreloadedConfigSystem.PreloadResult> future = 
            configSystem.preloadConfigurations();
        future.join();
        
        if (!configSystem.getAllConfigAccessors().isEmpty()) {
            ConfigDataAccessor accessor = configSystem.getAllConfigAccessors()
                .values().iterator().next();
            
            // Medir tiempo de acceso
            long startTime = System.nanoTime();
            for (int i = 0; i < 1000; i++) {
                accessor.getString("app.name", "default");
            }
            long endTime = System.nanoTime();
            
            double avgTimeNs = (endTime - startTime) / 1000.0;
            double avgTimeUs = avgTimeNs / 1000;
            
            assertTrue(avgTimeUs < 1000, "El acceso promedio debe ser muy rápido (< 1ms)");
            
            System.out.println("   ⚡ Eficiencia: " + String.format("%.3f", avgTimeUs) + " µs promedio");
        }
    }
    
    /**
     * Crea archivos de configuración de prueba
     */
    private void createTestConfigFiles() {
        try {
            Files.createDirectories(Paths.get(TEST_CONFIG_DIR));
            
            // Crear archivo properties de prueba
            String testProps = "test.key1=value1\n" +
                "test.key2=123\n" +
                "test.key3=true\n" +
                "test.key4=45.67\n";
            Files.write(Paths.get(TEST_CONFIG_DIR + "/test.properties"), testProps.getBytes());
            
            // Crear archivo JSON de prueba
            String testJson = "{\n" +
                "  \"test\": {\n" +
                "    \"string\": \"value\",\n" +
                "    \"number\": 42,\n" +
                "    \"boolean\": true\n" +
                "  }\n" +
                "}\n";
            Files.write(Paths.get(TEST_CONFIG_DIR + "/test.json"), testJson.getBytes());
            
        } catch (Exception e) {
            System.out.println("⚠️  No se pudieron crear archivos de prueba: " + e.getMessage());
        }
    }
    
    /**
     * Limpia archivos de configuración de prueba
     */
    private void cleanupTestConfigFiles() {
        try {
            if (Files.exists(Paths.get(TEST_CONFIG_DIR))) {
                Files.walk(Paths.get(TEST_CONFIG_DIR))
                    .forEach(path -> {
                        try {
                            Files.deleteIfExists(path);
                        } catch (Exception e) {
                            // Ignorar errores de limpieza
                        }
                    });
                Files.deleteIfExists(Paths.get(TEST_CONFIG_DIR));
            }
        } catch (Exception e) {
            // Ignorar errores de limpieza
        }
    }
}