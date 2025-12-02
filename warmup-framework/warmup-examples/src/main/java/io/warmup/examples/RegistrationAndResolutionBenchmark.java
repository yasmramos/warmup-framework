package io.warmup.examples;

import io.warmup.framework.core.Warmup;
import java.util.*;
import java.util.stream.DoubleStream;

/**
 * Benchmark real: registro + resolución de dependencias.
 */
public class RegistrationAndResolutionBenchmark {

    private static final int[] ITERATIONS_SET = {20_000, 50_000};
    private static final int[] BINDINGS_SET = {5, 10, 20};

    public static void main(String[] args) throws Exception {
        System.out.println("🔥 BENCHMARK REGISTRO + RESOLUCIÓN");
        System.out.println("===================================\n");

        extendedWarmup();

        for (int iterations : ITERATIONS_SET) {
            System.out.printf("\n🎯 %,d ITERACIONES%n", iterations);
            for (int numBindings : BINDINGS_SET) {
                runTest(iterations, numBindings);
            }
        }
        printFooter();
    }

    /* ---------- warm-up ---------- */
    private static void extendedWarmup() {
        System.out.println("🔥 Warm-up (registro + resolución)...");
        for (int i = 0; i < 3_000; i++) {
            Warmup injector = Warmup.create();
            registerBindings(injector, 10);
            resolveAll(injector, 10);
        }
        forceGc();
    }

    /* ---------- test completo ---------- */
    private static void runTest(int iterations, int numBindings) {
        DoubleStream.Builder registerNs = DoubleStream.builder();
        DoubleStream.Builder resolveNs = DoubleStream.builder();

        for (int i = 0; i < iterations; i++) {
            Warmup injector = Warmup.create();

            long t1 = System.nanoTime();
            registerBindings(injector, numBindings);
            long t2 = System.nanoTime();
            resolveAll(injector, numBindings);
            long t3 = System.nanoTime();

            registerNs.add((t2 - t1) / (double) numBindings);
            resolveNs.add((t3 - t2) / (double) numBindings);

            if (i % 5_000 == 0) {
                forceGc();   // evitar GC durante la medición
            }
        }

        forceGc();
        long mem = usedMB();

        // Convertir a arrays antes de procesar para evitar el problema del stream
        double[] registerTimes = registerNs.build().toArray();
        double[] resolveTimes = resolveNs.build().toArray();

        Summary reg = summarize(registerTimes);
        Summary res = summarize(resolveTimes);

        System.out.printf("\n📦 %2d bindings | mem %3d MB%n", numBindings, mem);
        System.out.printf("  Registrar:  %9.2f ns/binding  (±%6.2f)%n", reg.avg, reg.sd);
        System.out.printf("  Resolver:   %9.2f ns/getBean   (±%6.2f)%n", res.avg, res.sd);
        System.out.printf("  Throughput: %9.0f resoluciones/seg%n", 1_000_000_000 / res.avg);
    }

    private static void registerBindings(Warmup injector, int n) {
        for (int j = 0; j < n; j++) {
            switch (j % 5) {
                case 0:
                    injector.bind(Service.class).to(ServiceImpl.class).asSingleton().register();
                    break;
                case 1:
                    injector.bind(Repository.class).to(RepositoryImpl.class).asSingleton().register();
                    break;
                case 2:
                    injector.bind(Controller.class).to(ControllerImpl.class).asSingleton().register();
                    break;
                case 3:
                    injector.bind(Factory.class).to(FactoryImpl.class).asSingleton().register();
                    break;
                default:
                    injector.bind(Validator.class).to(ValidatorImpl.class).asSingleton().register();
                    break;
            }
        }
    }

    private static void resolveAll(Warmup injector, int n) {
        for (int j = 0; j < n; j++) {
            switch (j % 5) {
                case 0:
                    injector.getBean(Service.class);
                    break;
                case 1:
                    injector.getBean(Repository.class);
                    break;
                case 2:
                    injector.getBean(Controller.class);
                    break;
                case 3:
                    injector.getBean(Factory.class);
                    break;
                default:
                    injector.getBean(Validator.class);
                    break;
            }
        }
    }

    private static void forceGc() {
        System.gc();
        try {
            Thread.sleep(50);
        } catch (InterruptedException ignored) {
        }
    }

    private static long usedMB() {
        Runtime rt = Runtime.getRuntime();
        return (rt.totalMemory() - rt.freeMemory()) / 1024 / 1024;
    }

    // MÉTODO CORREGIDO - usar arrays en lugar de streams reutilizados
    private static Summary summarize(double[] times) {
        if (times.length == 0) {
            return new Summary(0, 0);
        }

        // Calcular promedio
        double sum = 0;
        for (double time : times) {
            sum += time;
        }
        double avg = sum / times.length;

        // Calcular desviación estándar
        double variance = 0;
        for (double time : times) {
            variance += (time - avg) * (time - avg);
        }
        double sd = Math.sqrt(variance / times.length);

        return new Summary(avg, sd);
    }

    // Versión alternativa usando DoubleSummaryStatistics (más eficiente)
    private static Summary summarizeWithStats(double[] times) {
        DoubleSummaryStatistics stats = Arrays.stream(times).summaryStatistics();
        double avg = stats.getAverage();

        // Calcular desviación estándar
        double variance = 0;
        for (double time : times) {
            variance += (time - avg) * (time - avg);
        }
        double sd = Math.sqrt(variance / times.length);

        return new Summary(avg, sd);
    }

    private static void printFooter() {
        System.out.println("\n✅ Benchmark finalizado.");
        System.out.println(" Métricas: ns/getBean, desviación estándar, memoria Java post-GC.");
    }

    private static class Summary {
        private final double avg;
        private final double sd;

        public Summary(double avg, double sd) {
            this.avg = avg;
            this.sd = sd;
        }
    }

    /* ---------- dummy types ---------- */
    public interface Service {
    }

    public static class ServiceImpl implements Service {

    }

    public interface Repository {
    }

    public static class RepositoryImpl implements Repository {
    }

    public interface Controller {
    }

    public static class ControllerImpl implements Controller {
    }

    public interface Factory {
    }

    public static class FactoryImpl implements Factory {
    }

    public interface Validator {
    }

    public static class ValidatorImpl implements Validator {
    }
}
