package io.warmup.examples.benchmark;


import io.warmup.framework.config.PropertySource;
import java.util.concurrent.TimeUnit;
import org.openjdk.jmh.annotations.*;

@State(Scope.Benchmark)
@BenchmarkMode(Mode.Throughput)
@OutputTimeUnit(TimeUnit.SECONDS)
@Warmup(iterations = 2, time = 1)
@Measurement(iterations = 3, time = 2)
@Fork(1)
public class ScalabilityBenchmark {

    @Param({"1", "2", "3"})
    private int packageCount;

    private PropertySource propertySource;

    @Setup
    public void setup() {
        propertySource = new PropertySource();
        propertySource.setProperty("app.name", "ScalabilityTest");
        propertySource.setProperty("profiles.active", "dev");
    }

}