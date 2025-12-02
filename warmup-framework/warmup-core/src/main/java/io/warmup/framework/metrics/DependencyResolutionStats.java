package io.warmup.framework.metrics;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Estadísticas de resolución de dependencias: tiempos medios, extremos y
 * percentiles (P50, P95, P99).
 */
public final class DependencyResolutionStats {

    private final Map<Class<?>, Double> averageTimes;
    private final Class<?> slowestDependency;
    private final double slowestAverageTime;
    private final Class<?> fastestDependency;
    private final double fastestAverageTime;
    private final List<Class<?>> topSlowestDependencies;

    /* NUEVOS: percentiles globales */
    private final double p50;
    private final double p95;
    private final double p99;

    /* Constructor vacío (valores por defecto) */
    public DependencyResolutionStats() {
        this(Collections.emptyMap(), null, 0.0, null, 0.0, Collections.emptyList(), 0.0, 0.0, 0.0);
    }

    /* Constructor completo */
    public DependencyResolutionStats(Map<Class<?>, Double> averageTimes,
            Class<?> slowestDependency, double slowestAverageTime,
            Class<?> fastestDependency, double fastestAverageTime,
            List<Class<?>> topSlowestDependencies,
            double p50, double p95, double p99) {
        this.averageTimes = Collections.unmodifiableMap(averageTimes);
        this.slowestDependency = slowestDependency;
        this.slowestAverageTime = slowestAverageTime;
        this.fastestDependency = fastestDependency;
        this.fastestAverageTime = fastestAverageTime;
        this.topSlowestDependencies = Collections.unmodifiableList(topSlowestDependencies);
        this.p50 = p50;
        this.p95 = p95;
        this.p99 = p99;
    }

    /* Getters existentes */
    public Map<Class<?>, Double> getAverageTimes() {
        return averageTimes;
    }

    public Class<?> getSlowestDependency() {
        return slowestDependency;
    }

    public double getSlowestAverageTime() {
        return slowestAverageTime;
    }

    public Class<?> getFastestDependency() {
        return fastestDependency;
    }

    public double getFastestAverageTime() {
        return fastestAverageTime;
    }

    public List<Class<?>> getTopSlowestDependencies() {
        return topSlowestDependencies;
    }

    /* Nuevos getters percentiles */
    public double getP50() {
        return p50;
    }

    public double getP95() {
        return p95;
    }

    public double getP99() {
        return p99;
    }

    /* Helpers (ms) */
    public double getP50Ms() {
        return p50 / 1_000_000.0;
    }

    public double getP95Ms() {
        return p95 / 1_000_000.0;
    }

    public double getP99Ms() {
        return p99 / 1_000_000.0;
    }

    @Override
    public String toString() {
        return "DependencyResolutionStats{"
                + "averageTimes=" + averageTimes.size()
                + ", slowest=" + slowestDependency + " (" + slowestAverageTime + " ns)"
                + ", fastest=" + fastestDependency + " (" + fastestAverageTime + " ns)"
                + ", top5=" + topSlowestDependencies.size()
                + ", P50=" + p50 + " ns, P95=" + p95 + " ns, P99=" + p99 + " ns}";
    }
}
