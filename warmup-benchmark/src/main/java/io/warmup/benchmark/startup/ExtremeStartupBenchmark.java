package io.warmup.benchmark.startup;

import io.warmup.framework.core.WarmupContainer;

import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.results.format.ResultFormatType;

import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * 🚀 BENCHMARK DE STARTUP EXTREMO - Versión Simplificada sin Simulaciones
 * 
 * Versión limpia que solo usa WarmupContainer básico sin simulaciones.
 * Objetivo: Medir performance real de inicialización del container.
 * 
 * @author MiniMax Agent
 */
@BenchmarkMode(Mode.SingleShotTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@Warmup(iterations = 2, time = 1)
@Measurement(iterations = 3, time = 2)
@Fork(0)
@State(Scope.Benchmark)
public class ExtremeStartupBenchmark {

    private static final Logger log = Logger.getLogger(ExtremeStartupBenchmark.class.getName());

    @Setup
    public void setup() {
        log.log(Level.INFO, "🚀 Configurando benchmark de startup extremo (sin simulaciones)");
    }

    @Benchmark
    public void benchmarkExtremeStartup() throws Exception {
        long startTime = System.nanoTime();
        
        try {
            // 🚀 Crear y inicializar WarmupContainer
            WarmupContainer container = new WarmupContainer();
            
            // Registrar algunos beans para simular uso real
            container.registerBean("configBean", String.class, "extreme-config");
            container.registerBean("serviceBean", Integer.class, 42);
            container.registerBean("factoryBean", Boolean.class, true);
            
            // Inicializar container
            container.initializeAllComponents();
            
            // Recuperar beans para verificar funcionamiento
            String config = container.getBean("configBean", String.class);
            Integer service = container.getBean("serviceBean", Integer.class);
            Boolean factory = container.getBean("factoryBean", Boolean.class);
            
            // Validar que todo funciona
            if (config == null || service == null || factory == null) {
                throw new IllegalStateException("Bean retrieval failed");
            }
            
            long totalTime = System.nanoTime() - startTime;
            long totalTimeMs = totalTime / 1_000_000;
            
            log.log(Level.INFO, "🏁 STARTUP EXTREMO COMPLETADO en {0}ms", totalTimeMs);
            
        } catch (Exception e) {
            log.log(Level.SEVERE, "❌ Error en startup extremo: {0}", e.getMessage());
            throw e;
        } finally {
            // Cleanup
            if (container != null) {
                try {
                    container.shutdown();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }
    }
}