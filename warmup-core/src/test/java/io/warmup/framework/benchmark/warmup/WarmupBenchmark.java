package io.warmup.framework.benchmark.warmup;

import io.warmup.framework.annotation.Bean;
import io.warmup.framework.annotation.Component;
import io.warmup.framework.annotation.Configuration;
import io.warmup.framework.annotation.Profile;
import io.warmup.framework.annotation.Value;
import io.warmup.framework.benchmark.common.ApplicationComponent;
import io.warmup.framework.benchmark.common.BasicService;
import io.warmup.framework.benchmark.common.ConfigService;
import io.warmup.framework.benchmark.common.ConfigValue;
import io.warmup.framework.core.WarmupContainer;
import io.warmup.framework.hotreload.HotReloadManager;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.results.format.ResultFormatType;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.util.concurrent.TimeUnit;

/**
 * 🚀 BENCHMARK DE RENDIMIENTO WARMUP FRAMEWORK (OPTIMIZADO CON ASM)
 * 
 * Mide el rendimiento de:
 * - Inicialización del contenedor
 * - Inyección de dependencias  
 * - Creación de instancias
 * - Resolución de beans
 * - Hot reload (optimizado con ASM)
 * 
 * vs Dagger, Guice y Spring Framework
 */
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MICROSECONDS)
@State(Scope.Benchmark)
@Fork(value = 1, jvmArgs = {"-Xmx2G", "-Xms1G"})
@Warmup(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 10, time = 2, timeUnit = TimeUnit.SECONDS)
public class WarmupBenchmark {
    
    private WarmupContainer container;
    private HotReloadManager hotReloadManager;
    
    // Beans para testing
    @Component
    public static class BenchmarkBasicService {
        private final String name;
        private final int value;
        
        public BenchmarkBasicService(@Value("app.name") String name, @Value("app.value") int value) {
            this.name = name;
            this.value = value;
        }
        
        public String getName() { return name; }
        public int getValue() { return value; }
        public String process(String input) { return name + ": " + input; }
        public void performOperation() { for (int i = 0; i < 100; i++) process("op-" + i); }
    }
    
    @Component
    public static class BenchmarkApplicationComponent {
        private final BenchmarkBasicService basicService;
        
        public BenchmarkApplicationComponent(BenchmarkBasicService basicService) {
            this.basicService = basicService;
        }
        
        public void start() {
            String result = basicService.process("Application starting");
            basicService.performOperation();
        }
        
        public String getServiceStatus() {
            return "Status: " + basicService.getName();
        }
    }
    
    @Configuration
    @Profile("benchmark")
    public static class BenchmarkConfiguration {
        @Bean
        public String appName() {
            return "Warmup-Benchmark";
        }
        
        @Bean
        public int appValue() {
            return 42;
        }
        
        @Bean
        public boolean debugMode() {
            return false;
        }
    }
    
    @Setup
    public void setup() {
        container = new WarmupContainer();
        hotReloadManager = new HotReloadManager(container, container.getEventBus());
    }
    
    @TearDown
    public void tearDown() {
        if (container != null) {
            try {
                container.shutdown();
            } catch (Exception e) {
                Thread.currentThread().interrupt();
            }
        }
    }
    
    @Benchmark
    public void testContainerInitialization() {
        try {
            WarmupContainer warmupContainer = new WarmupContainer();
            warmupContainer.shutdown();
        } catch (Exception e) {
            Thread.currentThread().interrupt();
        }
    }
    
    @Benchmark
    public BasicService testBasicServiceCreation() {
        // Simular inyección básica de dependencias
        BasicService service = container.createInstanceJit(BasicService.class, "Benchmark Service", 123);
        return service;
    }
    
    @Benchmark
    public ApplicationComponent testComplexComponentInjection() {
        // Simular componente complejo con múltiples dependencias
        ApplicationComponent component = container.createInstanceJit(ApplicationComponent.class);
        component.start();
        return component;
    }
    
    @Benchmark
    public void testHotReloadOptimization() {
        // 🚀 Benchmark del hot reload optimizado con ASM
        try {
            hotReloadManager.enable();
            
            // Simular recarga de clase
            String testClassName = "io.warmup.framework.benchmark.warmup.WarmupBenchmark$BenchmarkBasicService";
            container.reloadClass(testClassName);
            
            hotReloadManager.disable();
        } catch (Exception e) {
            // Ignorar errores en benchmark
        }
    }
    
    @Benchmark
    public void testCacheInvalidation() {
        // Benchmark de la optimización ASM para cache invalidation
        try {
            // Simular clase para cache
            String testClass = "io.warmup.framework.benchmark.common.BasicService";
            io.warmup.framework.asm.AsmCoreUtils.AsmClassInfo asmClassInfo = 
                io.warmup.framework.asm.AsmCoreUtils.getClassInfo(testClass);
            io.warmup.framework.asm.AsmCoreUtils.ClassInfo classInfo = new io.warmup.framework.asm.AsmCoreUtils.ClassInfo(
                asmClassInfo.className,
                asmClassInfo.interfaces,
                asmClassInfo.superClass,
                asmClassInfo.isInterface,
                asmClassInfo.isAbstract,
                asmClassInfo.isFinal,
                asmClassInfo.annotations,
                asmClassInfo.methods,
                asmClassInfo.fields,
                asmClassInfo.constructors
            );
            
            // Limpiar cache (como hace hot reload)
            hotReloadManager.clearAsmCaches();
            
        } catch (Exception e) {
            // Ignorar errores en benchmark
        }
    }
    
    @Benchmark
    public String testAsmClassLoading() {
        // Benchmark de carga de clases con ASM optimizado
        String className = "io.warmup.framework.benchmark.common.BasicService";
        try {
            io.warmup.framework.asm.AsmCoreUtils.AsmClassInfo asmClassInfo = 
                io.warmup.framework.asm.AsmCoreUtils.getClassInfo(className);
            io.warmup.framework.asm.AsmCoreUtils.ClassInfo classInfo = new io.warmup.framework.asm.AsmCoreUtils.ClassInfo(
                asmClassInfo.className,
                asmClassInfo.interfaces,
                asmClassInfo.superClass,
                asmClassInfo.isInterface,
                asmClassInfo.isAbstract,
                asmClassInfo.isFinal,
                asmClassInfo.annotations,
                asmClassInfo.methods,
                asmClassInfo.fields,
                asmClassInfo.constructors
            );
            return classInfo != null ? classInfo.className : null;
        } catch (Exception e) {
            return null;
        }
    }
    
    @Benchmark
    public Object testAsmMethodInvocation() {
        // Benchmark de invocación de métodos con AsmCoreUtils
        BasicService service = new BasicService("Benchmark", 100);
        try {
            return io.warmup.framework.asm.AsmCoreUtils.invokeMethod(service, "getName");
        } catch (Exception e) {
            return service.getName(); // Fallback
        }
    }
    
    @Benchmark
    public void testAsmNewInstance() {
        // Benchmark de creación de instancias con AsmCoreUtils
        try {
            BasicService service = io.warmup.framework.asm.AsmCoreUtils.newInstance(
                BasicService.class, "AsmInstance", 999);
            // Force compilation
            if (service != null) service.getName();
        } catch (Exception e) {
            // Ignore errors in benchmark
        }
    }
    
    public static void main(String[] args) throws RunnerException {
        Options opt = new OptionsBuilder()
            .include(WarmupBenchmark.class.getSimpleName())
            .result("warmup-benchmark-results.json")
            .resultFormat(ResultFormatType.JSON)
            .build();
        
        new Runner(opt).run();
    }
}