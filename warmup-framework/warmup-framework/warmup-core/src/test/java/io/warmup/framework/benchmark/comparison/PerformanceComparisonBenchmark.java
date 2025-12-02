package io.warmup.framework.benchmark.comparison;

import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.results.format.ResultFormatType;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.ConcurrentHashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Set;
import java.util.HashSet;

/**
 * 🚀 BENCHMARK DE RENDIMIENTO - O(1) vs O(n)
 * 
 * Benchmark directo que demuestra las ventajas de:
 * - Índices O(1) con ConcurrentHashMap (Warmup approach)
 * - Búsquedas lineales O(n) en listas (Spring/Guice approach)
 * - Algoritmos de complejidad logarítmica O(log n)
 * 
 * Este benchmark simula las operaciones críticas de un framework DI
 */
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MICROSECONDS)
@State(Scope.Benchmark)
@Fork(value = 2, jvmArgs = {"-Xmx2G", "-Xms1G", "-XX:+UseG1GC"})
@Warmup(iterations = 5, time = 2, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 10, time = 3, timeUnit = TimeUnit.SECONDS)
public class PerformanceComparisonBenchmark {

    // ===== TEST DATA SETUP =====
    private static final int DATASET_SIZE = 1000;
    private static final String[] SERVICE_IDS = new String[DATASET_SIZE];
    private static final String[] INTERFACE_IDS = new String[DATASET_SIZE];
    
    // O(1) Data Structures (Warmup approach)
    private ConcurrentHashMap<String, TestService> serviceIndex;
    private ConcurrentHashMap<Class<?>, TestService> interfaceIndex;
    private ConcurrentHashMap<String, Set<String>> interfaceImplementationIndex;
    
    // O(n) Data Structures (Traditional approach)
    private List<TestService> linearServiceList;
    private List<Class<?>> linearInterfaceList;
    private List<ServiceBinding> linearBindings;
    
    // O(log n) Data Structures (Balanced approach)
    private java.util.TreeMap<String, TestService> treeServiceMap;
    private java.util.TreeMap<String, ServiceBinding> treeBindingMap;

    @Setup
    public void setup() {
        // Initialize test data
        initializeTestData();
        
        // Initialize O(1) structures
        initializeO1Structures();
        
        // Initialize O(n) structures
        initializeONStructures();
        
        // Initialize O(log n) structures
        initializeOLogNStructures();
    }

    private void initializeTestData() {
        for (int i = 0; i < DATASET_SIZE; i++) {
            SERVICE_IDS[i] = "service-" + i;
            INTERFACE_IDS[i] = "Interface" + i;
        }
    }

    private void initializeO1Structures() {
        serviceIndex = new ConcurrentHashMap<>();
        interfaceIndex = new ConcurrentHashMap<>();
        interfaceImplementationIndex = new ConcurrentHashMap<>();
        
        for (int i = 0; i < DATASET_SIZE; i++) {
            TestService service = new TestService(SERVICE_IDS[i]);
            serviceIndex.put(SERVICE_IDS[i], service);
            
            try {
                Class<?> interfaceClass = Class.forName("java.lang.Runnable"); // Using built-in interface
                interfaceIndex.put(interfaceClass, service);
                
                Set<String> implementations = interfaceImplementationIndex.computeIfAbsent(
                    interfaceClass.getName(), k -> new HashSet<>()
                );
                implementations.add(SERVICE_IDS[i]);
            } catch (ClassNotFoundException e) {
                // Use Runnable interface for all
                Class<?> interfaceClass = Runnable.class;
                interfaceIndex.put(interfaceClass, service);
                
                Set<String> implementations = interfaceImplementationIndex.computeIfAbsent(
                    interfaceClass.getName(), k -> new HashSet<>()
                );
                implementations.add(SERVICE_IDS[i]);
            }
        }
    }

    private void initializeONStructures() {
        linearServiceList = new ArrayList<>();
        linearInterfaceList = new ArrayList<>();
        linearBindings = new ArrayList<>();
        
        for (int i = 0; i < DATASET_SIZE; i++) {
            TestService service = new TestService(SERVICE_IDS[i]);
            linearServiceList.add(service);
            linearInterfaceList.add(Runnable.class);
            linearBindings.add(new ServiceBinding(SERVICE_IDS[i], Runnable.class, service));
        }
    }

    private void initializeOLogNStructures() {
        treeServiceMap = new java.util.TreeMap<>();
        treeBindingMap = new java.util.TreeMap<>();
        
        for (int i = 0; i < DATASET_SIZE; i++) {
            TestService service = new TestService(SERVICE_IDS[i]);
            treeServiceMap.put(SERVICE_IDS[i], service);
            treeBindingMap.put(SERVICE_IDS[i], new ServiceBinding(SERVICE_IDS[i], Runnable.class, service));
        }
    }

    // ===== BENCHMARK TESTS =====

    @Benchmark
    public void testO1ServiceLookup() {
        // O(1) direct index lookup (Warmup approach)
        TestService service = serviceIndex.get(SERVICE_IDS[500]);
        if (service != null) service.execute();
    }

    @Benchmark
    public void testO1InterfaceResolution() {
        // O(1) interface to implementation lookup
        TestService service = interfaceIndex.get(Runnable.class);
        if (service != null) service.execute();
    }

    @Benchmark
    public void testO1BulkLookups() {
        // O(1) bulk operations
        for (int i = 0; i < 100; i++) {
            TestService service = serviceIndex.get(SERVICE_IDS[i]);
            if (service != null) service.execute();
        }
    }

    @Benchmark
    public void testONServiceLookup() {
        // O(n) linear search in list
        for (TestService service : linearServiceList) {
            if (service.id.equals(SERVICE_IDS[500])) {
                service.execute();
                break;
            }
        }
    }

    @Benchmark
    public void testONInterfaceResolution() {
        // O(n) linear search in interface list
        for (int i = 0; i < linearInterfaceList.size(); i++) {
            if (linearInterfaceList.get(i) == Runnable.class) {
                TestService service = linearServiceList.get(i);
                service.execute();
                break;
            }
        }
    }

    @Benchmark
    public void testONBulkSearches() {
        // O(n) bulk linear searches
        for (int i = 0; i < 100; i++) {
            for (TestService service : linearServiceList) {
                if (service.id.equals(SERVICE_IDS[i])) {
                    service.execute();
                    break;
                }
            }
        }
    }

    @Benchmark
    public void testOLogNServiceLookup() {
        // O(log n) tree map lookup
        TestService service = treeServiceMap.get(SERVICE_IDS[500]);
        if (service != null) service.execute();
    }

    @Benchmark
    public void testOLogNBulkLookups() {
        // O(log n) bulk tree operations
        for (int i = 0; i < 100; i++) {
            TestService service = treeServiceMap.get(SERVICE_IDS[i]);
            if (service != null) service.execute();
        }
    }

    @Benchmark
    public void testO1ParallelLookups() {
        // Parallel O(1) lookups
        Thread[] threads = new Thread[10];
        for (int i = 0; i < threads.length; i++) {
            final int index = i * 100;
            threads[i] = new Thread(() -> {
                TestService service = serviceIndex.get(SERVICE_IDS[index % DATASET_SIZE]);
                if (service != null) service.execute();
            });
            threads[i].start();
        }
        for (Thread thread : threads) {
            try { thread.join(); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
        }
    }

    @Benchmark
    public void testONParallelSearches() {
        // Parallel O(n) searches
        Thread[] threads = new Thread[10];
        for (int i = 0; i < threads.length; i++) {
            final int index = i * 100;
            threads[i] = new Thread(() -> {
                for (TestService service : linearServiceList) {
                    if (service.id.equals(SERVICE_IDS[index % DATASET_SIZE])) {
                        service.execute();
                        break;
                    }
                }
            });
            threads[i].start();
        }
        for (Thread thread : threads) {
            try { thread.join(); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
        }
    }

    @Benchmark
    public void testMemoryUsageO1() {
        // Memory overhead of O(1) structures
        ConcurrentHashMap<String, TestService> tempMap = new ConcurrentHashMap<>();
        for (int i = 0; i < 100; i++) {
            tempMap.put("key-" + i, new TestService("service-" + i));
        }
        // Force some memory operations
        tempMap.size();
    }

    @Benchmark
    public void testMemoryUsageON() {
        // Memory overhead of O(n) structures
        List<TestService> tempList = new ArrayList<>();
        for (int i = 0; i < 100; i++) {
            tempList.add(new TestService("service-" + i));
        }
        // Force some memory operations
        tempList.size();
    }

    // ===== TEST CLASSES =====
    public static class TestService {
        public final String id;
        
        public TestService(String id) {
            this.id = id;
        }
        
        public void execute() {
            // Simular trabajo de servicio
            long result = 0;
            for (int i = 0; i < 50; i++) {
                result += i * Math.random();
            }
        }
    }

    public static class ServiceBinding {
        public final String serviceId;
        public final Class<?> interfaceClass;
        public final TestService service;
        
        public ServiceBinding(String serviceId, Class<?> interfaceClass, TestService service) {
            this.serviceId = serviceId;
            this.interfaceClass = interfaceClass;
            this.service = service;
        }
    }

    public static void main(String[] args) throws RunnerException {
        Options opt = new OptionsBuilder()
            .include(PerformanceComparisonBenchmark.class.getSimpleName())
            .result("performance-comparison-benchmark.json")
            .resultFormat(ResultFormatType.JSON)
            .build();
        
        new Runner(opt).run();
    }
}