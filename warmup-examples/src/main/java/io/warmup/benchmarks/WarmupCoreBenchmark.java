package io.warmup.benchmarks;

import io.warmup.framework.core.Warmup;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;

@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@State(Scope.Benchmark)
@org.openjdk.jmh.annotations.Warmup(iterations = 5, time = 5, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 10, time = 5, timeUnit = TimeUnit.SECONDS)
@Fork(3)
public class WarmupCoreBenchmark {

    // Interfaces para testing
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

        boolean validate();
    }

    public interface Processor {

        String process(String input);
    }

    public interface Cache {

        void put(String key, String value);

        String get(String key);
    }

    // Implementaciones simples
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
        public boolean validate() {
            return true;
        }
    }

    public static class ProcessorImpl implements Processor {

        @Override
        public String process(String input) {
            return "processed-" + input;
        }
    }

    public static class CacheImpl implements Cache {

        private final java.util.Map<String, String> storage = new java.util.HashMap<>();

        @Override
        public void put(String key, String value) {
            storage.put(key, value);
        }

        @Override
        public String get(String key) {
            return storage.get(key);
        }
    }

    // ===== OPERACIONES BÁSICAS SEPARADAS =====
    @Benchmark
    public void createContainerOnly(Blackhole bh) {
        Warmup warmup = Warmup.create();
        bh.consume(warmup);
    }

    @Benchmark
    public void bindingOnly(Blackhole bh) {
        Warmup warmup = Warmup.create();
        Object binding = warmup.bind(Service.class).to(ServiceImpl.class);
        bh.consume(binding);
        bh.consume(warmup);
    }

    @Benchmark
    public void registrationOnly(Blackhole bh) {
        Warmup warmup = Warmup.create();
        warmup.bind(Service.class).to(ServiceImpl.class).register();
        bh.consume(warmup);
    }

    @Benchmark
    public void resolutionOnly(Blackhole bh) {
        Warmup warmup = Warmup.create();
        warmup.bind(Service.class).to(ServiceImpl.class).register();

        Service service = warmup.get(Service.class);
        bh.consume(service);
    }

    // ===== CONFIGURACIONES PREDEFINIDAS =====
    @State(Scope.Thread)
    public static class PreconfiguredContainer {

        public Warmup warmup;

        @Setup(Level.Iteration)
        public void setup() {
            warmup = Warmup.create();
            warmup.bind(Service.class).to(ServiceImpl.class).asSingleton().register();
            warmup.bind(Repository.class).to(RepositoryImpl.class).register();
            warmup.bind(Controller.class).to(ControllerImpl.class).register();
        }

        @TearDown(Level.Iteration)
        public void tearDown() {
            warmup = null;
        }
    }

    @State(Scope.Thread)
    public static class ComplexPreconfiguredContainer {

        public Warmup warmup;

        @Setup(Level.Iteration)
        public void setup() {
            warmup = Warmup.create();
            warmup.bind(Service.class).to(ServiceImpl.class).asSingleton().register();
            warmup.bind(Repository.class).to(RepositoryImpl.class).asSingleton().register();
            warmup.bind(Controller.class).to(ControllerImpl.class).asSingleton().register();
            warmup.bind(Factory.class).to(FactoryImpl.class).register();
            warmup.bind(Validator.class).to(ValidatorImpl.class).register();
            warmup.bind(Processor.class).to(ProcessorImpl.class).register();
            warmup.bind(Cache.class).to(CacheImpl.class).asSingleton().register();
        }

        @TearDown(Level.Iteration)
        public void tearDown() {
            warmup = null;
        }
    }

    // ===== BENCHMARKS CON CONFIGURACIÓN PREDEFINIDA =====
    @Benchmark
    public void resolutionFromPreconfigured(PreconfiguredContainer state, Blackhole bh) {
        Service service = state.warmup.get(Service.class);
        bh.consume(service);
    }

    @Benchmark
    public void multipleResolutionsFromPreconfigured(PreconfiguredContainer state, Blackhole bh) {
        bh.consume(state.warmup.get(Service.class));
        bh.consume(state.warmup.get(Repository.class));
        bh.consume(state.warmup.get(Controller.class));
    }

    @Benchmark
    public void complexResolutionFromPreconfigured(ComplexPreconfiguredContainer state, Blackhole bh) {
        bh.consume(state.warmup.get(Service.class));
        bh.consume(state.warmup.get(Repository.class));
        bh.consume(state.warmup.get(Controller.class));
        bh.consume(state.warmup.get(Factory.class));
        bh.consume(state.warmup.get(Validator.class));
        bh.consume(state.warmup.get(Processor.class));
        bh.consume(state.warmup.get(Cache.class));
    }

    // ===== BENCHMARKS ESPECÍFICOS PARA PROBLEMAS IDENTIFICADOS =====
    @Benchmark
    public void namedBindingOptimized(Blackhole bh) {
        Warmup warmup = Warmup.create();
        warmup.bind(Service.class).named("mainService").to(ServiceImpl.class).register();
        warmup.bind(Service.class).named("backupService").to(ServiceImpl.class).register();

        Service mainService = warmup.getNamed(Service.class, "mainService");
        Service backupService = warmup.getNamed(Service.class, "backupService");

        bh.consume(mainService);
        bh.consume(backupService);
    }

    @Benchmark
    public void singletonVsPrototype(Blackhole bh) {
        Warmup warmup = Warmup.create();

        // Singleton
        warmup.bind(Service.class).to(ServiceImpl.class).asSingleton().register();
        Service singleton1 = warmup.get(Service.class);
        Service singleton2 = warmup.get(Service.class);

        // Prototype (default)
        warmup.bind(Repository.class).to(RepositoryImpl.class).register();
        Repository proto1 = warmup.get(Repository.class);
        Repository proto2 = warmup.get(Repository.class);

        bh.consume(singleton1 == singleton2); // Debería ser true
        bh.consume(proto1 != proto2); // Debería ser true
    }

    @Benchmark
    public void aopOverheadMeasurement(Blackhole bh) {
        Warmup warmupWithAop = Warmup.create().withAop();
        Warmup warmupWithoutAop = Warmup.create();

        warmupWithAop.bind(Service.class).to(ServiceImpl.class).register();
        warmupWithoutAop.bind(Service.class).to(ServiceImpl.class).register();

        Service withAop = warmupWithAop.get(Service.class);
        Service withoutAop = warmupWithoutAop.get(Service.class);

        bh.consume(withAop);
        bh.consume(withoutAop);
    }

    @Benchmark
    public void asyncOverheadMeasurement(Blackhole bh) {
        Warmup warmupWithAsync = Warmup.create().withAsync();
        Warmup warmupWithoutAsync = Warmup.create();

        warmupWithAsync.bind(Service.class).to(ServiceImpl.class).register();
        warmupWithoutAsync.bind(Service.class).to(ServiceImpl.class).register();

        Service withAsync = warmupWithAsync.get(Service.class);
        Service withoutAsync = warmupWithoutAsync.get(Service.class);

        bh.consume(withAsync);
        bh.consume(withoutAsync);
    }

    // ===== BENCHMARKS DE RENDIMIENTO EN ESCALABILIDAD =====
    @Benchmark
    public void dependencyResolutionPerformance(Blackhole bh) {
        Warmup warmup = Warmup.create();
        warmup.bind(Service.class).to(ServiceImpl.class).asSingleton().register();
        warmup.bind(Repository.class).to(RepositoryImpl.class).register();
        warmup.bind(Controller.class).to(ControllerImpl.class).register();

        // Medir performance de resolución repetida
        for (int i = 0; i < 100; i++) {
            bh.consume(warmup.get(Service.class));
            if (i % 3 == 0) {
                bh.consume(warmup.get(Repository.class));
            }
            if (i % 5 == 0) {
                bh.consume(warmup.get(Controller.class));
            }
        }
    }

    @Benchmark
    public void multipleBindingsPerformance(Blackhole bh) {
        Warmup warmup = Warmup.create();

        // Registrar múltiples bindings
        for (int i = 0; i < 10; i++) {
            warmup.bind(Service.class).named("service" + i).to(ServiceImpl.class).register();
        }

        // Resolver múltiples instancias
        for (int i = 0; i < 10; i++) {
            Service service = warmup.getNamed(Service.class, "service" + i);
            bh.consume(service);
        }
    }

    @Benchmark
    public void memoryUsageOverIterations(Blackhole bh) {
        List<Service> services = new ArrayList<>();

        for (int i = 0; i < 100; i++) {
            Warmup warmup = Warmup.create();
            warmup.bind(Service.class).to(ServiceImpl.class).register();
            Service service = warmup.get(Service.class);
            services.add(service);
            bh.consume(service);
        }

        bh.consume(services.size());
    }

    // ===== ESCENARIOS COMPLEJOS =====
    @Benchmark
    public void complexScenario(Blackhole bh) {
        Warmup warmup = Warmup.create();

        warmup.bind(Service.class).to(ServiceImpl.class).asSingleton().register()
                .bind(Repository.class).to(RepositoryImpl.class).register()
                .bind(Controller.class).to(ControllerImpl.class).asSingleton().register()
                .bind(Factory.class).to(FactoryImpl.class).register()
                .bind(Validator.class).to(ValidatorImpl.class).register()
                .bind(Processor.class).to(ProcessorImpl.class).register()
                .bind(Cache.class).to(CacheImpl.class).asSingleton().register();

        bh.consume(warmup.get(Service.class));
        bh.consume(warmup.get(Repository.class));
        bh.consume(warmup.get(Controller.class));
        bh.consume(warmup.get(Factory.class));
        bh.consume(warmup.get(Validator.class));
        bh.consume(warmup.get(Processor.class));
        bh.consume(warmup.get(Cache.class));
    }

    @Benchmark
    public void withAopEnabled(Blackhole bh) {
        Warmup warmup = Warmup.create().withAop();
        warmup.bind(Service.class).to(ServiceImpl.class).register();
        warmup.bind(Repository.class).to(RepositoryImpl.class).register();

        bh.consume(warmup.get(Service.class));
        bh.consume(warmup.get(Repository.class));
    }

    @Benchmark
    public void withAsyncEnabled(Blackhole bh) {
        Warmup warmup = Warmup.create().withAsync();
        warmup.bind(Service.class).to(ServiceImpl.class).register();
        warmup.bind(Repository.class).to(RepositoryImpl.class).register();

        bh.consume(warmup.get(Service.class));
        bh.consume(warmup.get(Repository.class));
    }

    @Benchmark
    public void mixedFeatures(Blackhole bh) {
        Warmup warmup = Warmup.create().withAop().withAsync();

        warmup.bind(Service.class).to(ServiceImpl.class).asSingleton().register()
                .bind(Repository.class).to(RepositoryImpl.class).register()
                .bind(Controller.class).to(ControllerImpl.class).asSingleton().register();

        bh.consume(warmup.get(Service.class));
        bh.consume(warmup.get(Repository.class));
        bh.consume(warmup.get(Controller.class));
    }

    // ===== BENCHMARKS DE CONCURRENCIA =====
    @Benchmark
    @Threads(4)
    public void concurrentResolution(PreconfiguredContainer state, Blackhole bh) {
        Service service = state.warmup.get(Service.class);
        bh.consume(service);
    }

    @Benchmark
    @Threads(4)
    public void concurrentMixedOperations(Blackhole bh) {
        Warmup warmup = Warmup.create();
        warmup.bind(Service.class).to(ServiceImpl.class).asSingleton().register();
        warmup.bind(Repository.class).to(RepositoryImpl.class).register();

        bh.consume(warmup.get(Service.class));
        bh.consume(warmup.get(Repository.class));

        // Simular alguna operación adicional
        warmup.bind(Controller.class).to(ControllerImpl.class).register();
        bh.consume(warmup.get(Controller.class));
    }

    // ===== MÉTODOS DE COMPARACIÓN DIRECTA =====
    @Benchmark
    public void singleBindingAndResolve(Blackhole bh) {
        Warmup warmup = Warmup.create();
        warmup.bind(Service.class).to(ServiceImpl.class).register();
        Service service = warmup.get(Service.class);
        bh.consume(service);
        bh.consume(warmup);
    }

    @Benchmark
    public void singletonBinding(Blackhole bh) {
        Warmup warmup = Warmup.create();
        warmup.bind(Service.class).to(ServiceImpl.class).asSingleton().register();

        Service instance1 = warmup.get(Service.class);
        Service instance2 = warmup.get(Service.class);

        bh.consume(instance1);
        bh.consume(instance2);
        bh.consume(instance1 == instance2); // Should be true for singleton
    }

    @Benchmark
    public void multipleBindings(Blackhole bh) {
        Warmup warmup = Warmup.create();

        warmup.bind(Service.class).to(ServiceImpl.class).register()
                .bind(Repository.class).to(RepositoryImpl.class).register()
                .bind(Controller.class).to(ControllerImpl.class).register();

        bh.consume(warmup.get(Service.class));
        bh.consume(warmup.get(Repository.class));
        bh.consume(warmup.get(Controller.class));
    }

    @Benchmark
    public void namedBinding(Blackhole bh) {
        Warmup warmup = Warmup.create();
        warmup.bind(Service.class).named("mainService").to(ServiceImpl.class).register();

        Service service = warmup.getNamed(Service.class, "mainService");
        bh.consume(service);
    }

}
