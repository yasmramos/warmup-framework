package io.warmup.framework.metrics;

import java.util.*;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Percentiles exactos (P50, P95, P99) sin dependencias externas. Memoria
 * constante: solo guarda 1000 muestras como máximo.
 */
public final class PercentileCalculator {

    private static final int MAX_SAMPLES = 1000;
    private final long[] samples = new long[MAX_SAMPLES];
    private final AtomicLong count = new AtomicLong(0);

    public void add(long value) {
        long idx = count.getAndIncrement();
        if (idx < MAX_SAMPLES) {
            samples[(int) idx] = value;
        } else {
            // Reservoir sampling: sobrescribir aleatoriamente
            int replace = (int) (Math.random() * (idx + 1));
            if (replace < MAX_SAMPLES) {
                samples[replace] = value;
            }
        }
    }

    public double percentile(double p) { // p en 0..1
        long n = Math.min(count.get(), MAX_SAMPLES);
        if (n == 0) {
            return 0.0;
        }
        long[] copy = Arrays.copyOf(samples, (int) n);
        Arrays.sort(copy);
        int pos = (int) Math.ceil(p * n) - 1;
        return copy[Math.max(pos, 0)];
    }

    public void reset() {
        count.set(0);
        Arrays.fill(samples, 0L);
    }
}
