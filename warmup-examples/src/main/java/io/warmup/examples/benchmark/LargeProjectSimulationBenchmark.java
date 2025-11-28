package io.warmup.examples.benchmark;


import io.warmup.framework.config.PropertySource;
import java.util.concurrent.TimeUnit;
import org.openjdk.jmh.annotations.*;

@State(Scope.Benchmark)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
public class LargeProjectSimulationBenchmark {

    @Param({"50", "100", "200"}) // Simular diferentes tamaños de proyecto
    private int simulatedClassCount;
    
    private PropertySource propertySource;

    @Setup
    public void setup() {
        propertySource = new PropertySource();
        propertySource.setProperty("app.name", "LargeProjectSimulation");
    }


}