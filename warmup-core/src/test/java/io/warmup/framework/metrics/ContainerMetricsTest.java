package io.warmup.framework.metrics;

import io.warmup.framework.core.WarmupContainer;
import java.util.Map;
import java.util.stream.LongStream;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ContainerMetricsTest {

    private WarmupContainer container;
    private ContainerMetrics metrics;

    @BeforeEach
    void setUp() {
        container = new WarmupContainer();
        container.disableAutoShutdown();
        metrics = container.getMetricsManager().getContainerMetrics();
    }

    @Test
    void nuevosCamposYPercentiles() {
        /* 1. Insertamos exactamente 1000 muestras:
              - 500 × 100 ns  → posiciones 0..499
              - 300 × 200 ns  → posiciones 500..799
              - 200 × 1000 ns → posiciones 800..999
           Con esto:
              P50 = posición 499 → 100 ns
              P95 = posición 949 → 1000 ns (corregido: 949 está en el rango 800-999)
              P99 = posición 989 → 1000 ns
         */
        LongStream.range(0, 500).forEach(i -> metrics.recordResolution(String.class, 100L, true));
        LongStream.range(500, 800).forEach(i -> metrics.recordResolution(Integer.class, 200L, true));
        LongStream.range(800, 1000).forEach(i -> metrics.recordResolution(Double.class, 1000L, true));

        // 2. Campos nuevos
        assertEquals(1000, metrics.getTotalRequests());
        long expectedTotal = 500L * 100L + 300L * 200L + 200L * 1000L; // 50,000 + 60,000 + 200,000 = 310,000 ns
        assertEquals(expectedTotal, metrics.getTotalResolutionTime());
        assertEquals(310_000L / 1000.0, metrics.getOverallAverageResolutionTime(), 1.0); // 310 ns

        // 3. Percentiles (tolerancia ±1 ns)
        assertEquals(100.0, metrics.getPercentile(0.50), 1.0);   // P50
        assertEquals(1000.0, metrics.getPercentile(0.95), 1.0);  // P95 corregido: 949 está en rango 800-999
        assertEquals(1000.0, metrics.getPercentile(0.99), 1.0);  // P99

        // 4. Snapshot
        ContainerMetrics.MetricsSnapshot snap = metrics.getSnapshot();
        assertEquals(1000, snap.getTotalRequests());
        assertEquals(expectedTotal, snap.getTotalResolutionTime());
        assertEquals(310.0, snap.getOverallAverageResolutionTime(), 1.0); // Corregido: 310.0 en lugar de 360.0
        assertEquals(100.0, snap.getResolutionStats().getP50(), 1.0);
        assertEquals(1000.0, snap.getResolutionStats().getP95(), 1.0); // Corregido
        assertEquals(1000.0, snap.getResolutionStats().getP99(), 1.0);

        // 5. Top 5 más lentos
        assertEquals(3, snap.getResolutionStats().getTopSlowestDependencies().size());
        assertEquals(Double.class, snap.getResolutionStats().getTopSlowestDependencies().get(0));
        assertEquals(Integer.class, snap.getResolutionStats().getTopSlowestDependencies().get(1));
        assertEquals(String.class, snap.getResolutionStats().getTopSlowestDependencies().get(2));

        // 6. Mapa de promedios por tipo
        Map<Class<?>, Double> avg = snap.getResolutionStats().getAverageTimes();
        assertEquals(100.0, avg.get(String.class), 1.0);
        assertEquals(200.0, avg.get(Integer.class), 1.0);
        assertEquals(1000.0, avg.get(Double.class), 1.0);
    }

    @Test
    void successRate() {
        metrics.recordSuccess(); // +1 ok
        metrics.recordSuccess(); // +1 ok
        metrics.recordFailure(); // +1 fail

        assertEquals(3, metrics.getTotalRequests());
        assertEquals(2, metrics.getSuccessfulRequests());
        assertEquals(1, metrics.getFailedRequests());
        assertEquals(2.0 / 3.0 * 100.0, metrics.getSuccessRate(), 0.01);
    }

    @Test
    void emptyMetrics() {
        ContainerMetrics.MetricsSnapshot snap = metrics.getSnapshot();
        assertEquals(0, snap.getTotalRequests());
        assertEquals(0.0, snap.getOverallAverageResolutionTime(), 0.01);
        assertEquals(0.0, snap.getResolutionStats().getP50(), 0.01);
        assertEquals(0.0, snap.getResolutionStats().getP95(), 0.01);
        assertEquals(0.0, snap.getResolutionStats().getP99(), 0.01);
        assertTrue(snap.getResolutionStats().getTopSlowestDependencies().isEmpty());
    }
}
