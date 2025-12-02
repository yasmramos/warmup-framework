package io.warmup.framework.benchmark.spring;

import io.warmup.framework.benchmark.common.ApplicationComponent;
import io.warmup.framework.benchmark.common.BasicService;
import io.warmup.framework.benchmark.common.ConfigService;
import io.warmup.framework.benchmark.common.ConfigValue;
import org.openjdk.jmh.annotations.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.concurrent.TimeUnit;

/**
 * 🌸 BENCHMARK SPRING FRAMEWORK - Comparación de rendimiento
 * 
 * Mide el rendimiento de Spring Framework para comparación directa
 * con Warmup Framework optimizado
 */
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MICROSECONDS)
@State(Scope.Benchmark)
@Fork(value = 1, jvmArgs = {"-Xmx2G", "-Xms1G"})
@Warmup(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 10, time = 2, timeUnit = TimeUnit.SECONDS)
public class SpringBenchmark {
    
    // Spring Configuration
    @Configuration
    @ComponentScan
    public static class SpringBenchmarkConfiguration {
        
        @Bean
        @Singleton
        public String appName() {
            return "Spring-Benchmark";
        }
        
        @Bean
        @Singleton
        public int appValue() {
            return 42;
        }
        
        @Bean
        @Singleton
        public boolean debugMode() {
            return false;
        }
    }
    
    // Spring Services
    @Singleton
    public static class SpringBasicService {
        private final String name;
        private final int value;
        
        @Inject
        public SpringBasicService(@Value("Spring-Benchmark") String name, @Value("42") int value) {
            this.name = name;
            this.value = value;
        }
        
        public String getName() { return name; }
        public int getValue() { return value; }
        public String process(String input) { return name + ": " + input; }
        public void performOperation() { for (int i = 0; i < 100; i++) process("op-" + i); }
    }
    
    @Singleton
    public static class SpringConfigService {
        private final String appName;
        private final int maxConnections;
        private final boolean debugMode;
        
        @Inject
        public SpringConfigService(@Value("Spring-Benchmark") String appName, 
                                 @Value("100") int maxConnections, 
                                 @Value("false") boolean debugMode) {
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
    public static class SpringApplicationComponent {
        private final SpringBasicService basicService;
        private final SpringConfigService configService;
        
        @Inject
        public SpringApplicationComponent(SpringBasicService basicService, SpringConfigService configService) {
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
        
        public SpringBasicService getBasicService() {
            return basicService;
        }
    }
    
    private AnnotationConfigApplicationContext context;
    
    @Setup
    public void setup() {
        context = new AnnotationConfigApplicationContext(SpringBenchmarkConfiguration.class);
    }
    
    @TearDown
    public void tearDown() {
        if (context != null) {
            context.close();
        }
    }
    
    @Benchmark
    public void testContextCreation() {
        AnnotationConfigApplicationContext newContext = 
            new AnnotationConfigApplicationContext(SpringBenchmarkConfiguration.class);
        newContext.close();
    }
    
    @Benchmark
    public BasicService testBasicServiceInjection() {
        return context.getBean(BasicService.class);
    }
    
    @Benchmark
    public ConfigService testConfigServiceInjection() {
        return context.getBean(ConfigService.class);
    }
    
    @Benchmark
    public void testComplexComponentInjection() {
        ApplicationComponent appComponent = context.getBean(ApplicationComponent.class);
        appComponent.start();
    }
    
    @Benchmark
    public String testServiceMethodCall() {
        BasicService service = context.getBean(BasicService.class);
        return service.process("Spring benchmark test");
    }
    
    @Benchmark
    public SpringBasicService testDirectBean() {
        return context.getBean(SpringBasicService.class);
    }
    
    public static void main(String[] args) {
        System.out.println("Spring Benchmark setup complete");
    }
}