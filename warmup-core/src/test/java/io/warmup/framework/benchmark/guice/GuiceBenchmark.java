package io.warmup.framework.benchmark.guice;

import com.google.inject.*;
import io.warmup.framework.benchmark.common.ApplicationComponent;
import io.warmup.framework.benchmark.common.BasicService;
import io.warmup.framework.benchmark.common.ConfigService;
import io.warmup.framework.benchmark.common.ConfigValue;
import org.openjdk.jmh.annotations.*;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.concurrent.TimeUnit;

/**
 * 🔧 BENCHMARK GOOGLE GUICE - Comparación de rendimiento
 * 
 * Mide el rendimiento de Guice para comparación directa
 * con Warmup Framework optimizado
 */
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MICROSECONDS)
@State(org.openjdk.jmh.annotations.Scope.Benchmark)
@Fork(value = 1, jvmArgs = {"-Xmx2G", "-Xms1G"})
@Warmup(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 10, time = 2, timeUnit = TimeUnit.SECONDS)
public class GuiceBenchmark {
    
    // Guice Modules
    public static class GuiceBenchmarkModule extends AbstractModule {
        @Override
        protected void configure() {
            bind(String.class).toInstance("Guice-Benchmark");
            bind(Integer.class).toInstance(42);
            bind(Boolean.class).toInstance(false);
        }
    }
    
    // Guice Services
    @Singleton
    public static class GuiceBasicService {
        private final String name;
        private final int value;
        
        @Inject
        public GuiceBasicService(String name, int value) {
            this.name = name;
            this.value = value;
        }
        
        public String getName() { return name; }
        public int getValue() { return value; }
        public String process(String input) { return name + ": " + input; }
        public void performOperation() { for (int i = 0; i < 100; i++) process("op-" + i); }
    }
    
    @Singleton
    public static class GuiceConfigService {
        private final String appName;
        private final int maxConnections;
        private final boolean debugMode;
        
        @Inject
        public GuiceConfigService(String appName, int maxConnections, boolean debugMode) {
            this.appName = appName;
            this.maxConnections = maxConnections;
            this.debugMode = debugMode;
        }
        
        public String getValue(String key) {
            switch (key) {
                case "app.name": return appName;
                case "app.max.connections": return String.valueOf(maxConnections);
                case "app.debug.mode": return String.valueOf(debugMode);
                default: return "unknown";
            }
        }
    }
    
    @Singleton
    public static class GuiceApplicationComponent {
        private final GuiceBasicService basicService;
        private final GuiceConfigService configService;
        
        @Inject
        public GuiceApplicationComponent(GuiceBasicService basicService, GuiceConfigService configService) {
            this.basicService = basicService;
            this.configService = configService;
        }
        
        public void start() {
            String config = configService.getValue("app.name");
            String result = basicService.process("App starting: " + config);
            basicService.performOperation();
        }
        
        public String getServiceStatus() {
            return "ApplicationComponent: " + basicService.getName() + " configured";
        }
        
        public GuiceBasicService getBasicService() {
            return basicService;
        }
    }
    
    private Injector injector;
    
    @Setup
    public void setup() {
        injector = Guice.createInjector(new GuiceBenchmarkModule());
    }
    
    @TearDown
    public void tearDown() {
        injector = null;
    }
    
    @Benchmark
    public void testInjectorCreation() {
        Injector newInjector = Guice.createInjector(new GuiceBenchmarkModule());
    }
    
    @Benchmark
    public BasicService testBasicServiceInjection() {
        return injector.getInstance(BasicService.class);
    }
    
    @Benchmark
    public ConfigService testConfigServiceInjection() {
        return injector.getInstance(ConfigService.class);
    }
    
    @Benchmark
    public void testComplexComponentInjection() {
        ApplicationComponent appComponent = injector.getInstance(ApplicationComponent.class);
        appComponent.start();
    }
    
    @Benchmark
    public String testServiceMethodCall() {
        BasicService service = injector.getInstance(BasicService.class);
        return service.process("Guice benchmark test");
    }
    
    @Benchmark
    public GuiceBasicService testDirectBinding() {
        return injector.getInstance(GuiceBasicService.class);
    }
    
    public static void main(String[] args) {
        System.out.println("Guice Benchmark setup complete");
    }
}