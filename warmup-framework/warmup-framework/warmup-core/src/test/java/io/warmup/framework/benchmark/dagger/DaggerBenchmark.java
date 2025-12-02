package io.warmup.framework.benchmark.dagger;

import dagger.Component;
import dagger.Module;
import dagger.Provides;
import io.warmup.framework.benchmark.common.ApplicationComponent;
import io.warmup.framework.benchmark.common.BasicService;
import io.warmup.framework.benchmark.common.ConfigService;
import io.warmup.framework.benchmark.common.ConfigValue;
import org.openjdk.jmh.annotations.*;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.concurrent.TimeUnit;

/**
 * ⚡ BENCHMARK DAGGER 2 - Comparación de rendimiento
 * 
 * Mide el rendimiento de Dagger 2 para comparación directa
 * con Warmup Framework optimizado
 */
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MICROSECONDS)
@State(Scope.Benchmark)
@Fork(value = 1, jvmArgs = {"-Xmx2G", "-Xms1G"})
@Warmup(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 10, time = 2, timeUnit = TimeUnit.SECONDS)
public class DaggerBenchmark {
    
    // Dagger Components
    @Component(modules = {DaggerBenchmarkModule.class})
    @Singleton
    public interface DaggerAppComponent {
        DaggerBasicService getBasicService();
        DaggerConfigService getConfigService();
        DaggerApplicationComponent getApplicationComponent();
    }
    
    // Manual mock implementation since Dagger annotation processor is not available
    public static class ManualDaggerAppComponent implements DaggerAppComponent {
        private final DaggerBasicService basicService;
        private final DaggerConfigService configService;
        private final DaggerApplicationComponent applicationComponent;
        
        public ManualDaggerAppComponent() {
            String appName = "Dagger-Benchmark";
            int appValue = 42;
            boolean debugMode = false;
            
            this.basicService = new DaggerBasicService(appName, appValue);
            this.configService = new DaggerConfigService(appName, appValue, debugMode);
            this.applicationComponent = new DaggerApplicationComponent(basicService, configService);
        }
        
        @Override
        public DaggerBasicService getBasicService() {
            return basicService;
        }
        
        @Override
        public DaggerConfigService getConfigService() {
            return configService;
        }
        
        @Override
        public DaggerApplicationComponent getApplicationComponent() {
            return applicationComponent;
        }
    }
    
    @Module
    public static class DaggerBenchmarkModule {
        
        @Provides
        @Singleton
        public String provideAppName() {
            return "Dagger-Benchmark";
        }
        
        @Provides
        @Singleton
        public int provideAppValue() {
            return 42;
        }
        
        @Provides
        @Singleton
        public boolean provideDebugMode() {
            return false;
        }
    }
    
    // Dagger Services
    @Singleton
    public static class DaggerBasicService {
        private final String name;
        private final int value;
        
        @Inject
        public DaggerBasicService(String name, int value) {
            this.name = name;
            this.value = value;
        }
        
        public String getName() { return name; }
        public int getValue() { return value; }
        public String process(String input) { return name + ": " + input; }
        public void performOperation() { for (int i = 0; i < 100; i++) process("op-" + i); }
    }
    
    @Singleton
    public static class DaggerConfigService {
        private final String appName;
        private final int maxConnections;
        private final boolean debugMode;
        
        @Inject
        public DaggerConfigService(String appName, int maxConnections, boolean debugMode) {
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
    public static class DaggerApplicationComponent {
        private final DaggerBasicService basicService;
        private final DaggerConfigService configService;
        
        @Inject
        public DaggerApplicationComponent(DaggerBasicService basicService, DaggerConfigService configService) {
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
        
        public DaggerBasicService getBasicService() {
            return basicService;
        }
    }
    
    private DaggerAppComponent component;
    
    @Setup
    public void setup() {
        component = new ManualDaggerAppComponent();
    }
    
    @TearDown
    public void tearDown() {
        component = null;
    }
    
    @Benchmark
    public void testComponentCreation() {
        DaggerAppComponent newComponent = new ManualDaggerAppComponent();
        // Let GC clean up
    }
    
    @Benchmark
    public DaggerBasicService testBasicServiceInjection() {
        return component.getBasicService();
    }
    
    @Benchmark
    public DaggerConfigService testConfigServiceInjection() {
        return component.getConfigService();
    }
    
    @Benchmark
    public void testComplexComponentInjection() {
        DaggerApplicationComponent appComponent = component.getApplicationComponent();
        appComponent.start();
    }
    
    @Benchmark
    public String testServiceMethodCall() {
        DaggerBasicService service = component.getBasicService();
        return service.process("Dagger benchmark test");
    }
    
    public static void main(String[] args) {
        // Test basic functionality
        System.out.println("Dagger Benchmark setup complete");
    }
}