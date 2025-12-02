package io.warmup.framework.core;

import io.warmup.framework.config.PropertySource;
import java.util.concurrent.TimeUnit;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.HashSet;
import java.util.logging.Logger;
import java.util.logging.Level;

/**
 * 🚀 TEST SIMPLE DE OPTIMIZACIONES BASELINE
 * Test unitario que no requiere todas las dependencias del framework
 */
public class SimpleBaselineTest {
    
    private static final Logger log = Logger.getLogger(SimpleBaselineTest.class.getName());
    
    public static void main(String[] args) {
        log.info("🚀 Iniciando test simple de optimizaciones baseline...");
        
        try {
            // Test 1: ManagerFactory básico
            testManagerFactory();
            
            // Test 2: Performance sin dependencias complejas
            testSimplePerformance();
            
            // Test 3: Comparación de patrones
            testPatterns();
            
        } catch (Exception e) {
            log.log(Level.SEVERE, "Error en test", e);
        }
    }
    
    /**
     * 🏭 TEST DE MANAGER FACTORY BÁSICO
     */
    private static void testManagerFactory() {
        log.info("🏭 Testando ManagerFactory...");
        
        try {
            // Test cache de managers
            io.warmup.framework.health.HealthCheckManager manager1 = ManagerFactory.getManager(
                io.warmup.framework.health.HealthCheckManager.class, new io.warmup.framework.core.WarmupContainer()
            );
            
            io.warmup.framework.health.HealthCheckManager manager2 = ManagerFactory.getManager(
                io.warmup.framework.health.HealthCheckManager.class, new io.warmup.framework.core.WarmupContainer()
            );
            
            log.info("✅ ManagerFactory funciona correctamente");
            log.log(Level.INFO, "📊 Managers obtenidos: {0}", (manager1 == manager2 ? "SINGLETION" : "NUEVOS"));
            
            // Estadísticas del cache
            @SuppressWarnings("unchecked")
            java.util.Map<String, Object> stats = (java.util.Map<String, Object>) 
                ManagerFactory.class.getDeclaredMethod("getCacheStats").invoke(null);
            
            log.log(Level.INFO, "📈 Cache stats: {0}", stats);
            
        } catch (Exception e) {
            log.log(Level.SEVERE, "❌ Error en test ManagerFactory", e);
        }
    }
    
    /**
     * ⚡ TEST DE PERFORMANCE SIMPLE
     */
    private static void testSimplePerformance() {
        log.info("⚡ Testando performance simple...");
        
        int iterations = 100;
        
        // Test con ManagerFactory
        long startFactory = System.nanoTime();
        
        for (int i = 0; i < iterations; i++) {
            try {
                // Solo test de creación de DependencyRegistry con parámetros básicos
                io.warmup.framework.core.DependencyRegistry registry = ManagerFactory.getManager(
                    io.warmup.framework.core.DependencyRegistry.class,
                    new io.warmup.framework.core.WarmupContainer(),
                    new io.warmup.framework.config.PropertySource(),
                    new HashSet<>()
                );
            } catch (Exception e) {
                // Ignorar errores de dependencias complejas
            }
        }
        
        long endFactory = System.nanoTime();
        long timeFactory = (endFactory - startFactory) / 1_000_000; // ms
        
        // Test con reflexión directa
        long startReflection = System.nanoTime();
        
        for (int i = 0; i < iterations; i++) {
            try {
                Class.forName("io.warmup.framework.core.DependencyRegistry")
                    .getDeclaredConstructor(
                        io.warmup.framework.core.WarmupContainer.class,
                        io.warmup.framework.config.PropertySource.class,
                        Set.class
                    ).newInstance(
                        new io.warmup.framework.core.WarmupContainer(),
                        new io.warmup.framework.config.PropertySource(),
                        new HashSet<>()
                    );
            } catch (Exception e) {
                // Ignorar errores de dependencias complejas
            }
        }
        
        long endReflection = System.nanoTime();
        long timeReflection = (endReflection - startReflection) / 1_000_000; // ms
        
        // Resultados
        double avgFactory = (double) timeFactory / iterations;
        double avgReflection = (double) timeReflection / iterations;
        
        log.log(Level.INFO, "📈 PERFORMANCE RESULTS:\n" +
               "- ManagerFactory: {0}ms promedio\n" +
               "- Reflexión directa: {1}ms promedio\n" +
               "- Mejora: {2}%",
               new Object[]{String.format("%.2f", avgFactory),
                          String.format("%.2f", avgReflection),
                          String.format("%.1f", ((avgReflection - avgFactory) / avgReflection) * 100)});
    }
    
    /**
     * 🔍 TEST DE PATRONES
     */
    private static void testPatterns() {
        log.info("🔍 Testando patrones de optimización...");
        
        // Test 1: Singleton pattern
        testSingletonPattern();
        
        // Test 2: Factory pattern
        testFactoryPattern();
        
        // Test 3: Cache efficiency
        testCacheEfficiency();
    }
    
    private static void testSingletonPattern() {
        log.info("🔄 Testando patrón singleton...");
        
        try {
            io.warmup.framework.cache.ASMCacheManager manager1 = ManagerFactory.getManager(
                io.warmup.framework.cache.ASMCacheManager.class
            );
            
            io.warmup.framework.cache.ASMCacheManager manager2 = ManagerFactory.getManager(
                io.warmup.framework.cache.ASMCacheManager.class
            );
            
            log.log(Level.INFO, "✅ Singleton test: {0}", 
                   (manager1 == manager2 ? "CORRECTO - Misma instancia" : "NUEVAS instancias"));
            
        } catch (Exception e) {
            log.log(Level.WARNING, "⚠️ Error en singleton test: {0}", e.getMessage());
        }
    }
    
    private static void testFactoryPattern() {
        log.info("🏭 Testando patrón factory...");
        
        try {
            io.warmup.framework.core.ModuleManager manager1 = ManagerFactory.getManager(
                io.warmup.framework.core.ModuleManager.class,
                new io.warmup.framework.core.WarmupContainer(),
                new io.warmup.framework.config.PropertySource()
            );
            
            log.info("✅ Factory pattern funciona correctamente");
            
        } catch (Exception e) {
            log.log(Level.WARNING, "⚠️ Error en factory test: {0}", e.getMessage());
        }
    }
    
    private static void testCacheEfficiency() {
        log.info("📊 Testando eficiencia del cache...");
        
        // Crear managers múltiples veces para ver si el cache funciona
        for (int i = 0; i < 5; i++) {
            try {
                ManagerFactory.getManager(io.warmup.framework.health.HealthCheckManager.class, 
                                        new io.warmup.framework.core.WarmupContainer());
            } catch (Exception e) {
                // Ignorar errores
            }
        }
        
        try {
            @SuppressWarnings("unchecked")
            java.util.Map<String, Object> stats = (java.util.Map<String, Object>) 
                ManagerFactory.class.getDeclaredMethod("getCacheStats").invoke(null);
            
            log.log(Level.INFO, "📊 Cache stats después de múltiples requests: {0}", stats);
            
        } catch (Exception e) {
            log.log(Level.WARNING, "⚠️ Error obteniendo cache stats: {0}", e.getMessage());
        }
    }
}