package io.warmup.benchmark.startup;

import io.warmup.framework.core.WarmupContainer;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.results.format.ResultFormatType;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 * Benchmark para medir el rendimiento baseline del framework WarmupContainer
 * SIN las optimizaciones extremas activas.
 */
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@State(Scope.Thread)
@Warmup(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Fork(1)
@Threads(1)
public class BaselineStartupBenchmark {

    // Test bean class
    public static class TestBean {
        private String name;
        private int value;
        
        public TestBean(String name, int value) {
            this.name = name;
            this.value = value;
        }
        
        public String getName() { return name; }
        public int getValue() { return value; }
    }

    @Setup
    public void setup() {
        // Configuración baseline - solo setup básico
        System.setProperty("warmup.startup.parallel", "false");
        System.setProperty("warmup.startup.unsafe", "false");
        System.setProperty("warmup.startup.critical", "false");
        System.setProperty("warmup.startup.gc", "false");
        System.setProperty("warmup.startup.analysis", "false");
    }

    @Benchmark
    public void baselineContainerCreation() {
        // Crear container baseline sin optimizaciones extremas
        try (WarmupContainer container = new WarmupContainer()) {
            // Registros básicos
            container.registerBean("testBean1", new TestBean("Test1", 100));
            container.registerBean("testBean2", new TestBean("Test2", 200));
            container.registerBean("testBean3", new TestBean("Test3", 300));
            
            // Inicialización básica
            container.initialize();
        }
    }

    @Benchmark
    public void baselineWithBeanProcessing() {
        // Test con procesamiento de beans
        try (WarmupContainer container = new WarmupContainer()) {
            for (int i = 0; i < 50; i++) {
                container.registerBean("bean_" + i, new TestBean("Bean" + i, i));
            }
            
            container.initialize();
            
            // Acceso a beans
            for (int i = 0; i < 50; i++) {
                TestBean bean = container.getBean("bean_" + i, TestBean.class);
                if (bean != null) {
                    bean.getValue();
                }
            }
        }
    }

    public static void main(String[] args) throws RunnerException {
        // Crear directorio de resultados
        File resultsDir = new File("benchmark-results");
        if (!resultsDir.exists()) {
            resultsDir.mkdirs();
        }

        Options opt = new OptionsBuilder()
                .include(BaselineStartupBenchmark.class.getSimpleName())
                .result("benchmark-results/baseline-startup-results.json")
                .resultFormat(ResultFormatType.JSON)
                .build();

        System.out.println("Ejecutando baseline startup benchmark...");
        System.out.println("Configuración: WarmupContainer estándar SIN optimizaciones extremas");
        
        new Runner(opt).run();
        
        System.out.println("Benchmark completado. Resultados guardados en benchmark-results/");
    }
}