package io.warmup.benchmarks;

import io.warmup.framework.core.Warmup;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;

@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MICROSECONDS)
@State(Scope.Benchmark)
@org.openjdk.jmh.annotations.Warmup(iterations = 5, time = 2, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 10, time = 3, timeUnit = TimeUnit.SECONDS)
@Fork(3)
public class WarmupStartupBenchmark {

    // ===== INTERFACES Y CLASES PARA TESTING =====
    
    public interface Service { 
        String execute(); 
    }
    
    public interface Repository { 
        String findData(); 
    }
    
    public interface Controller { 
        String handleRequest(); 
    }
    
    public interface Factory { 
        String create(); 
    }
    
    public interface Validator { 
        boolean validate(String input); 
    }
    
    public interface Cache { 
        void put(String key, Object value);
        Object get(String key);
    }

    public static class ServiceImpl implements Service {
        @Override 
        public String execute() { 
            return "service-executed"; 
        }
    }

    public static class RepositoryImpl implements Repository {
        @Override 
        public String findData() { 
            return "repository-data"; 
        }
    }

    public static class ControllerImpl implements Controller {
        @Override 
        public String handleRequest() { 
            return "controller-response"; 
        }
    }

    public static class FactoryImpl implements Factory {
        @Override 
        public String create() { 
            return "factory-product"; 
        }
    }

    public static class ValidatorImpl implements Validator {
        @Override 
        public boolean validate(String input) { 
            return input != null && !input.isEmpty(); 
        }
    }

    public static class CacheImpl implements Cache {
        private final java.util.Map<String, Object> storage = new java.util.HashMap<>();
        
        @Override
        public void put(String key, Object value) {
            storage.put(key, value);
        }
        
        @Override
        public Object get(String key) {
            return storage.get(key);
        }
    }

    // ===== CONFIGURACIONES PREDEFINIDAS =====

    @State(Scope.Benchmark)
    public static class StartupState {
        public Map<Class<?>, Class<?>> bindingMappings;
        
        @Setup(Level.Trial)
        public void setup() {
            bindingMappings = new HashMap<>();
            bindingMappings.put(Service.class, ServiceImpl.class);
            bindingMappings.put(Repository.class, RepositoryImpl.class);
            bindingMappings.put(Controller.class, ControllerImpl.class);
            bindingMappings.put(Factory.class, FactoryImpl.class);
            bindingMappings.put(Validator.class, ValidatorImpl.class);
            bindingMappings.put(Cache.class, CacheImpl.class);
        }
    }

    @State(Scope.Benchmark)
    public static class DynamicBindingsState {
        public Class<?>[] interfaces;
        public Class<?>[] implementations;
        
        @Setup(Level.Trial)
        public void setup() {
            interfaces = new Class<?>[] {
                Service.class, Repository.class, Controller.class, 
                Factory.class, Validator.class, Cache.class
            };
            
            implementations = new Class<?>[] {
                ServiceImpl.class, RepositoryImpl.class, ControllerImpl.class,
                FactoryImpl.class, ValidatorImpl.class, CacheImpl.class
            };
        }
    }

    // ===== BENCHMARKS DE STARTUP BÁSICO =====

    @Benchmark
    public void pureStartup(Blackhole bh) {
        Warmup warmup = Warmup.create();
        bh.consume(warmup);
    }

    @Benchmark
    public void startupWithSingleBinding(Blackhole bh) {
        Warmup warmup = Warmup.create();
        warmup.bind(Service.class).to(ServiceImpl.class).register();
        bh.consume(warmup);
    }

    @Benchmark
    public void startupWithFiveBindings(Blackhole bh) {
        Warmup warmup = Warmup.create();
        
        warmup.bind(Service.class).to(ServiceImpl.class).register()
              .bind(Repository.class).to(RepositoryImpl.class).register()
              .bind(Controller.class).to(ControllerImpl.class).register()
              .bind(Factory.class).to(FactoryImpl.class).register()
              .bind(Validator.class).to(ValidatorImpl.class).register();
        
        bh.consume(warmup);
    }

    @Benchmark
    public void startupWithTenBindings(Blackhole bh) {
        Warmup warmup = Warmup.create();
        
        warmup.bind(Service.class).to(ServiceImpl.class).register()
              .bind(Repository.class).to(RepositoryImpl.class).register()
              .bind(Controller.class).to(ControllerImpl.class).register()
              .bind(Factory.class).to(FactoryImpl.class).register()
              .bind(Validator.class).to(ValidatorImpl.class).register()
              .bind(Cache.class).to(CacheImpl.class).register()
              .bind(Service.class).named("secondary").to(ServiceImpl.class).register()
              .bind(Repository.class).named("backup").to(RepositoryImpl.class).register()
              .bind(Controller.class).named("api").to(ControllerImpl.class).register()
              .bind(Factory.class).named("prototype").to(FactoryImpl.class).register();
        
        bh.consume(warmup);
    }

    // ===== BENCHMARKS DE STARTUP CON DIFERENTES CONFIGURACIONES =====

    @Benchmark
    public void startupWithSingletonBindings(Blackhole bh) {
        Warmup warmup = Warmup.create();
        
        warmup.bind(Service.class).to(ServiceImpl.class).asSingleton().register()
              .bind(Repository.class).to(RepositoryImpl.class).asSingleton().register()
              .bind(Controller.class).to(ControllerImpl.class).asSingleton().register()
              .bind(Cache.class).to(CacheImpl.class).asSingleton().register();
        
        bh.consume(warmup);
    }

    @Benchmark
    public void startupWithMixedScopeBindings(Blackhole bh) {
        Warmup warmup = Warmup.create();
        
        // Singletons
        warmup.bind(Service.class).to(ServiceImpl.class).asSingleton().register()
              .bind(Repository.class).to(RepositoryImpl.class).asSingleton().register();
        
        // Prototypes (default)
        warmup.bind(Controller.class).to(ControllerImpl.class).register()
              .bind(Factory.class).to(FactoryImpl.class).register()
              .bind(Validator.class).to(ValidatorImpl.class).register();
        
        bh.consume(warmup);
    }

    @Benchmark
    public void startupWithNamedBindings(Blackhole bh) {
        Warmup warmup = Warmup.create();
        
        warmup.bind(Service.class).named("primary").to(ServiceImpl.class).register()
              .bind(Service.class).named("secondary").to(ServiceImpl.class).register()
              .bind(Service.class).named("backup").to(ServiceImpl.class).register()
              .bind(Repository.class).named("main").to(RepositoryImpl.class).register()
              .bind(Repository.class).named("cache").to(RepositoryImpl.class).register();
        
        bh.consume(warmup);
    }

    // ===== BENCHMARKS DE STARTUP CON CARACTERÍSTICAS AVANZADAS =====

    @Benchmark
    public void startupWithAopEnabled(Blackhole bh) {
        Warmup warmup = Warmup.create().withAop();
        
        warmup.bind(Service.class).to(ServiceImpl.class).register()
              .bind(Repository.class).to(RepositoryImpl.class).register()
              .bind(Controller.class).to(ControllerImpl.class).register();
        
        bh.consume(warmup);
    }

    @Benchmark
    public void startupWithAsyncEnabled(Blackhole bh) {
        Warmup warmup = Warmup.create().withAsync();
        
        warmup.bind(Service.class).to(ServiceImpl.class).register()
              .bind(Repository.class).to(RepositoryImpl.class).register()
              .bind(Controller.class).to(ControllerImpl.class).register();
        
        bh.consume(warmup);
    }

    @Benchmark
    public void startupWithAllFeatures(Blackhole bh) {
        Warmup warmup = Warmup.create().withAop().withAsync();
        
        warmup.bind(Service.class).to(ServiceImpl.class).asSingleton().register()
              .bind(Repository.class).to(RepositoryImpl.class).asSingleton().register()
              .bind(Controller.class).to(ControllerImpl.class).register()
              .bind(Factory.class).to(FactoryImpl.class).register()
              .bind(Validator.class).to(ValidatorImpl.class).register()
              .bind(Cache.class).to(CacheImpl.class).asSingleton().register();
        
        bh.consume(warmup);
    }

    // ===== BENCHMARKS DE STARTUP + RESOLUCIÓN INICIAL =====

    @Benchmark
    public void startupAndResolveSingle(Blackhole bh) {
        Warmup warmup = Warmup.create();
        warmup.bind(Service.class).to(ServiceImpl.class).register();
        
        Service service = warmup.get(Service.class);
        bh.consume(warmup);
        bh.consume(service);
    }

    @Benchmark
    public void startupAndResolveMultiple(Blackhole bh) {
        Warmup warmup = Warmup.create();
        
        warmup.bind(Service.class).to(ServiceImpl.class).register()
              .bind(Repository.class).to(RepositoryImpl.class).register()
              .bind(Controller.class).to(ControllerImpl.class).register();
        
        bh.consume(warmup.get(Service.class));
        bh.consume(warmup.get(Repository.class));
        bh.consume(warmup.get(Controller.class));
        bh.consume(warmup);
    }

    // ===== BENCHMARKS CORREGIDOS - SIN ERRORES DE TIPO =====

    @Benchmark
    public void startupWithPredefinedMappings(StartupState state, Blackhole bh) {
        Warmup warmup = Warmup.create();
        
        for (Map.Entry<Class<?>, Class<?>> entry : state.bindingMappings.entrySet()) {
            bindToWarmup(warmup, entry.getKey(), entry.getValue());
        }
        
        bh.consume(warmup);
    }

    @Benchmark
    public void startupWithDynamicBindings(DynamicBindingsState state, Blackhole bh) {
        Warmup warmup = Warmup.create();
        
        for (int i = 0; i < state.interfaces.length; i++) {
            if (i % 2 == 0) { // Bindear solo interfaces pares
                bindToWarmup(warmup, state.interfaces[i], state.implementations[i]);
            }
        }
        
        bh.consume(warmup);
    }

    @Benchmark
    public void startupWithPartialBindings(DynamicBindingsState state, Blackhole bh) {
        Warmup warmup = Warmup.create();
        
        // Bindear solo los primeros 3
        for (int i = 0; i < Math.min(3, state.interfaces.length); i++) {
            bindToWarmup(warmup, state.interfaces[i], state.implementations[i]);
        }
        
        bh.consume(warmup);
    }

    // ===== BENCHMARKS DE STARTUP EN CALIENTE (HOT STARTUP) =====

    @State(Scope.Thread)
    public static class HotStartupState {
        public Warmup templateWarmup;
        
        @Setup(Level.Iteration)
        public void setup() {
            templateWarmup = Warmup.create();
            templateWarmup.bind(Service.class).to(ServiceImpl.class).asSingleton().register()
                         .bind(Repository.class).to(RepositoryImpl.class).asSingleton().register()
                         .bind(Controller.class).to(ControllerImpl.class).register();
        }
        
        @TearDown(Level.Iteration)
        public void tearDown() {
            templateWarmup = null;
        }
    }

    @Benchmark
    public void hotStartupWithSameConfiguration(HotStartupState state, Blackhole bh) {
        Warmup warmup = Warmup.create();
        
        warmup.bind(Service.class).to(ServiceImpl.class).asSingleton().register()
              .bind(Repository.class).to(RepositoryImpl.class).asSingleton().register()
              .bind(Controller.class).to(ControllerImpl.class).register();
        
        bh.consume(warmup);
    }

    // ===== BENCHMARKS DE MEMORIA Y CLEANUP =====

    @Benchmark
    public void startupMemoryFootprint(Blackhole bh) {
        Runtime runtime = Runtime.getRuntime();
        
        System.gc();
        long memoryBefore = runtime.totalMemory() - runtime.freeMemory();
        
        Warmup warmup = Warmup.create();
        warmup.bind(Service.class).to(ServiceImpl.class).register()
              .bind(Repository.class).to(RepositoryImpl.class).register()
              .bind(Controller.class).to(ControllerImpl.class).register();
        
        long memoryAfter = runtime.totalMemory() - runtime.freeMemory();
        long memoryUsed = memoryAfter - memoryBefore;
        
        bh.consume(warmup);
        bh.consume(memoryUsed);
    }

    // ===== BENCHMARKS COMPARATIVOS =====

    @Benchmark
    public void startupComparisonBaselineVsAop(Blackhole bh) {
        Warmup baseline = Warmup.create();
        Warmup withAop = Warmup.create().withAop();
        
        baseline.bind(Service.class).to(ServiceImpl.class).register();
        withAop.bind(Service.class).to(ServiceImpl.class).register();
        
        bh.consume(baseline);
        bh.consume(withAop);
    }

    @Benchmark
    public void startupComparisonSingletonVsPrototype(Blackhole bh) {
        Warmup singletonWarmup = Warmup.create();
        Warmup prototypeWarmup = Warmup.create();
        
        singletonWarmup.bind(Service.class).to(ServiceImpl.class).asSingleton().register()
                      .bind(Repository.class).to(RepositoryImpl.class).asSingleton().register();
        
        prototypeWarmup.bind(Service.class).to(ServiceImpl.class).register()
                      .bind(Repository.class).to(RepositoryImpl.class).register();
        
        bh.consume(singletonWarmup);
        bh.consume(prototypeWarmup);
    }

    // ===== MÉTODO AUXILIAR TYPE-SAFE PARA BINDING =====
    
    @SuppressWarnings("unchecked")
    private <T> void bindToWarmup(Warmup warmup, Class<T> interfaceClass, Class<?> implementationClass) {
        warmup.bind(interfaceClass).to((Class<? extends T>) implementationClass).register();
    }

}