package io.warmup.examples.startup.lazy.examples;

import io.warmup.framework.core.WarmupContainer;
import io.warmup.framework.startup.lazy.*;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * ⚡ EJEMPLO DE ZERO COST STARTUP
 * 
 * Demuestra el concepto revolucionario de "cero inicialización hasta el primer uso real".
 * 
 * Concepto clave: 
 * - 🚀 Infraestructura se inicializa en paralelo (lo que sí necesita el framework)
 * - 🛡️ Beans de aplicación se crean solo cuando se solicitan (on-demand)
 * - 💰 Zero startup cost: Solo pagas por lo que realmente usas
 * 
 * @author MiniMax Agent
 * @version 1.0
 */
public class ZeroCostStartupExample {
    
    private static final Logger log = Logger.getLogger(ZeroCostStartupExample.class.getName());
    
    /**
     * 🎯 EJEMPLO PRINCIPAL
     */
    public static void main(String[] args) {
        log.log(Level.INFO, "⚡ EJEMPLO DE ZERO COST STARTUP");
        log.log(Level.INFO, "=================================");
        
        try {
            demonstrateZeroCostStartup();
            
            log.log(Level.INFO, "✅ Ejemplo completado exitosamente");
            
        } catch (Exception e) {
            log.log(Level.SEVERE, "❌ Error en ejemplo: {0}", e.getMessage());
        }
    }
    
    /**
     * ⚡ DEMOSTRACIÓN DE ZERO COST STARTUP
     */
    private static void demonstrateZeroCostStartup() throws Exception {
        // PASO 1: Crear container
        log.log(Level.INFO, "\n📦 PASO 1: Creando container del framework");
        WarmupContainer container = new WarmupContainer();
        
        // PASO 2: Crear zero cost startup loader
        log.log(Level.INFO, "\n🎯 PASO 2: Creando ZeroStartupBeanLoader");
        ZeroStartupBeanLoader zeroStartupLoader = new ZeroStartupBeanLoader(container);
        
        // PASO 3: Ejecutar zero cost startup
        log.log(Level.INFO, "\n⚡ PASO 3: Ejecutando ZERO COST STARTUP");
        log.log(Level.INFO, "  🚀 Infraestructura se inicializa en paralelo");
        log.log(Level.INFO, "  📝 Beans se registran como lazy (sin crear)");
        log.log(Level.INFO, "  💰 ZERO cost para beans no utilizados");
        
        CompletableFuture<ZeroStartupBeanLoader.ZeroStartupResult> startupFuture = 
            zeroStartupLoader.executeZeroCostStartup();
        
        ZeroStartupBeanLoader.ZeroStartupResult startupResult = startupFuture.get(30, TimeUnit.SECONDS);
        
        log.log(Level.INFO, "✅ ZERO COST STARTUP COMPLETADO en {0}ms", startupResult.getTotalTimeMs());
        
        // PASO 4: Demostrar que NO se crearon beans innecesariamente
        log.log(Level.INFO, "\n🧪 PASO 4: Verificando que beans NO se crearon automáticamente");
        ZeroStartupBeanLoader.ZeroStartupStats stats = startupResult.getStats();
        
        log.log(Level.INFO, "  📊 Beans registrados como lazy: {0}", stats.getLazyBeanCount());
        log.log(Level.INFO, "  📊 Beans registrados como eager: {0}", stats.getEagerBeanCount());
        log.log(Level.INFO, "  📊 Beans creados on-demand: {0}", stats.getOnDemandCreations());
        
        if (stats.getOnDemandCreations() == 0) {
            log.log(Level.INFO, "  ✅ PERFECTO: Ningún bean se creó automáticamente - ZERO COST CONFIRMADO");
        } else {
            log.log(Level.WARNING, "  ⚠️ Algunos beans se crearon: {0}", stats.getOnDemandCreations());
        }
        
        // PASO 5: Solicitar beans específicos (on-demand)
        log.log(Level.INFO, "\n🎯 PASO 5: Solicitando beans específicos (on-demand)");
        
        // Simular solicitud de beans que la aplicación realmente necesita
        requestSpecificBeans(zeroStartupLoader);
        
        // PASO 6: Mostrar estadísticas finales
        log.log(Level.INFO, "\n📊 PASO 6: Estadísticas finales de zero cost startup");
        
        ZeroStartupBeanLoader.ZeroStartupStats finalStats = zeroStartupLoader.getZeroStartupStats();
        log.log(Level.INFO, "  📊 Beans lazy registrados: {0}", finalStats.getLazyBeanCount());
        log.log(Level.INFO, "  📊 Beans eager registrados: {0}", finalStats.getEagerBeanCount());
        log.log(Level.INFO, "  📊 Beans creados on-demand: {0}", finalStats.getOnDemandCreations());
        log.log(Level.INFO, "  📊 Infraestructura paralela: {0}", 
                finalStats.isParallelInfrastructure() ? "✅ Habilitada" : "❌ Deshabilitada");
        
        // Calcular savings
        int totalBeans = finalStats.getLazyBeanCount() + finalStats.getEagerBeanCount();
        int beansUsed = finalStats.getOnDemandCreations();
        int savingsPercent = totalBeans > 0 ? ((totalBeans - beansUsed) * 100 / totalBeans) : 0;
        
        log.log(Level.INFO, "\n💰 AHORRO DE STARTUP:");
        log.log(Level.INFO, "  • Beans totales registrados: {0}", totalBeans);
        log.log(Level.INFO, "  • Beans realmente utilizados: {0}", beansUsed);
        log.log(Level.INFO, "  • Ahorro de inicialización: {0}%", savingsPercent);
        
        if (savingsPercent > 50) {
            log.log(Level.INFO, "  🎉 EXCELENTE: Startup {0}% más eficiente", savingsPercent);
        }
        
        // PASO 7: Generar reporte completo
        log.log(Level.INFO, "\n📋 PASO 7: Reporte completo de zero cost startup");
        String report = zeroStartupLoader.generateZeroStartupReport();
        log.log(Level.INFO, "\n{0}", report);
        
        // Cleanup
        zeroStartupLoader.shutdown();
        
        log.log(Level.INFO, "\n✅ DEMOSTRACIÓN COMPLETADA");
        log.log(Level.INFO, "💡 CONCLUSIÓN: Zero cost startup elimina completamente el costo");
        log.log(Level.INFO, "    de inicialización de beans no utilizados, pagando solo");
        log.log(Level.INFO, "    por lo que realmente se usa en la aplicación.");
    }
    
    /**
     * 🎯 SOLICITAR BEANS ESPECÍFICOS (ON-DEMAND)
     */
    private static void requestSpecificBeans(ZeroStartupBeanLoader loader) {
        log.log(Level.INFO, "  🎯 Solicitando UserService (on-demand)...");
        try {
            Object userService = loader.getBean("UserService", Object.class);
            if (userService != null) {
                log.log(Level.INFO, "    ✅ UserService creado on-demand");
            }
        } catch (Exception e) {
            log.log(Level.INFO, "    ⚠️ UserService no disponible (esperado en demo): {0}", e.getMessage());
        }
        
        log.log(Level.INFO, "  🎯 Solicitando OrderService (on-demand)...");
        try {
            Object orderService = loader.getBean("OrderService", Object.class);
            if (orderService != null) {
                log.log(Level.INFO, "    ✅ OrderService creado on-demand");
            }
        } catch (Exception e) {
            log.log(Level.INFO, "    ⚠️ OrderService no disponible (esperado en demo): {0}", e.getMessage());
        }
        
        log.log(Level.INFO, "  🎯 Solicitando infrastructure beans (críticos)...");
        try {
            Object dependencyRegistry = loader.getBean("DependencyRegistry", Object.class);
            if (dependencyRegistry != null) {
                log.log(Level.INFO, "    ✅ DependencyRegistry disponible (inicializado eager)");
            }
            
            Object propertySource = loader.getBean("PropertySource", Object.class);
            if (propertySource != null) {
                log.log(Level.INFO, "    ✅ PropertySource disponible (inicializado eager)");
            }
            
        } catch (Exception e) {
            log.log(Level.INFO, "    ⚠️ Error obteniendo infrastructure beans: {0}", e.getMessage());
        }
    }
    
    /**
     * 🔍 UTILIDAD: Mostrar información del concepto zero cost startup
     */
    private static void explainZeroCostStartup() {
        log.log(Level.INFO, "\n💡 CONCEPTO ZERO COST STARTUP:");
        log.log(Level.INFO, "===============================");
        
        log.log(Level.INFO, "🎯 PROBLEMA QUE RESUELVE:");
        log.log(Level.INFO, "  • Los frameworks tradicionales inicializan TODOS los beans al startup");
        log.log(Level.INFO, "  • Esto es lento y consume memoria para beans que nunca se usan");
        log.log(Level.INFO, "  • Startup time aumenta proporcionalmente al número de beans");
        
        log.log(Level.INFO, "\n🚀 SOLUCIÓN ZERO COST:");
        log.log(Level.INFO, "  1. Infraestructura crítica se inicializa en paralelo");
        log.log(Level.INFO, "  2. Beans de aplicación se registran como LAZY suppliers");
        log.log(Level.INFO, "  3. Beans se crean SOLO cuando se solicitan (on-demand)");
        log.log(Level.INFO, "  4. Zero cost inicial para beans no utilizados");
        
        log.log(Level.INFO, "\n💰 BENEFICIOS:");
        log.log(Level.INFO, "  • Startup inmediato (solo infraestructura)");
        log.log(Level.INFO, "  • Memoria eficiente (solo beans usados)");
        log.log(Level.INFO, "  • Escalabilidad (crece con uso real, no con beans totales)");
        log.log(Level.INFO, "  • Responsiveness (app disponible inmediatamente)");
        
        log.log(Level.INFO, "\n🎯 CASOS DE USO IDEALES:");
        log.log(Level.INFO, "  • Microservicios con beans especializados");
        log.log(Level.INFO, "  • Aplicaciones que no usan todos los módulos");
        log.log(Level.INFO, "  • Sistemas con muchos beans opcionales");
        log.log(Level.INFO, "  • Aplicaciones que necesitan respuesta inmediata");
    }
}