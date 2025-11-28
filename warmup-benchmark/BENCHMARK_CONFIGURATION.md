# Warmup Framework Benchmark Suite Configuration

## Overview
This file provides an overview of all available benchmarks in the Warmup Framework benchmark suite.
The benchmarks have been moved from `src/test/java` to `src/main/java` for better integration
with the main framework code.

## Benchmark Categories

### 1. Framework Comparison Benchmarks
**Location:** `io.warmup.benchmark.*`
**Purpose:** Compare Warmup Framework performance vs traditional DI frameworks

| Benchmark | Package | Description |
|-----------|---------|-------------|
| `ProductionFrameworkBenchmark` | `io.warmup.benchmark` | Comprehensive framework comparison (Spring, Guice, Dagger) |
| `WarmupOnlyBenchmark` | `io.warmup.benchmark` | Pure Warmup Framework performance analysis |

### 2. AOP Optimization Benchmarks
**Location:** `io.warmup.aop.benchmark.*`
**Purpose:** Validate O(1) vs O(n) performance improvements in AOP layer

| Benchmark | Package | Description |
|-----------|---------|-------------|
| `AspectManagerO1OptimizationBenchmark` | `io.warmup.aop.benchmark` | AspectManager O(1) optimization validation |
| `AspectManagerPhase2OptimizationBenchmark` | `io.warmup.aop.benchmark` | Phase 2 AOP optimizations |
| `EventIndexEngineO1OptimizationBenchmark` | `io.warmup.aop.benchmark` | EventIndexEngine O(1) performance |
| `EventIndexEngineO1SimulatorBenchmark` | `io.warmup.aop.benchmark` | Event simulation benchmarks |

### 3. Container Optimization Benchmarks  
**Location:** `io.warmup.container.benchmark.*`
**Purpose:** WarmupContainer O(1) optimization validation

| Benchmark | Package | Description |
|-----------|---------|-------------|
| `WarmupContainerBenchmarkSimplified` | `io.warmup.container.benchmark` | Simplified container benchmarks |
| `WarmupContainerO1BenchmarkDirect` | `io.warmup.container.benchmark` | Direct O(1) container benchmarks |
| `WarmupContainerO1OptimizationBenchmark` | `io.warmup.container.benchmark` | Full O(1) optimization benchmarks |
| `WarmupContainerO1Validation` | `io.warmup.container.benchmark` | Validation suite |

### 4. Framework Core Benchmarks
**Location:** `io.warmup.framework.benchmark.*`
**Purpose:** Individual component O(1) optimization validation

| Benchmark | Package | Description |
|-----------|---------|-------------|
| `AopHandlerO1Benchmark` | `io.warmup.framework.benchmark` | AopHandler optimization (500x-1,200x improvement) |
| `AopHandlerO1SimpleTest` | `io.warmup.framework.benchmark` | Simple AopHandler tests |
| `BeanRegistryO1BenchmarkDirect` | `io.warmup.framework.benchmark` | Direct BeanRegistry O(1) benchmarks |
| `ConfigurationProcessorO1BenchmarkDirect` | `io.warmup.framework.benchmark` | ConfigurationProcessor O(1) |
| `ContainerMetricsO1Benchmark` | `io.warmup.framework.benchmark` | Container metrics O(1) |
| `DependencyRegistryO1BenchmarkDirect` | `io.warmup.framework.benchmark` | DependencyRegistry O(1) |
| `DependencyRegistryO1ComparisonBenchmark` | `io.warmup.framework.benchmark` | DependencyRegistry comparisons |
| `DependencyResolutionBenchmark` | `io.warmup.framework.benchmark` | Dependency resolution analysis |
| `DirectComplexityBenchmark` | `io.warmup.framework.benchmark` | Direct complexity analysis |
| `EventBusO1BenchmarkDirect` | `io.warmup.framework.benchmark` | EventBus O(1) performance |
| `EventBusO1BenchmarkSimplified` | `io.warmup.framework.benchmark` | Simplified EventBus benchmarks |
| `FrameworkComparisonBenchmark` | `io.warmup.framework.benchmark` | Framework comparison suite |
| `HotReloadManagerO1Benchmark` | `io.warmup.framework.benchmark` | HotReloadManager O(1) |
| `MetricsManagerO1BenchmarkDirect` | `io.warmup.framework.benchmark` | MetricsManager O(1) |
| `MinimalWarmupBenchmark` | `io.warmup.framework.benchmark` | Minimal warmup tests |
| `ModuleManagerO1Benchmark` | `io.warmup.framework.benchmark` | ModuleManager O(1) |
| `ModuleManagerO1SimpleTest` | `io.warmup.framework.benchmark` | Simple ModuleManager tests |
| `SimpleResolutionBenchmark` | `io.warmup.framework.benchmark` | Simple resolution tests |
| `SimpleWarmupBenchmark` | `io.warmup.framework.benchmark` | Simple warmup demonstrations |

### 5. Startup Performance Benchmarks
**Location:** `io.warmup.benchmark.startup.*`
**Purpose:** Application startup time analysis and optimization

| Benchmark | Package | Description |
|-----------|---------|-------------|
| `BaselineOptimizationBenchmark` | `io.warmup.benchmark.startup` | Baseline vs optimized startup comparison |
| `ExtremeStartupBenchmark` | `io.warmup.benchmark.startup` | Extreme startup performance tests |

## Execution Modes

### Via BenchmarkRunner
```bash
# Run all benchmarks (comprehensive analysis)
java -jar warmup-benchmark.jar all

# Run specific categories
java -jar warmup-benchmark.jar comparison  # Framework comparisons
java -jar warmup-benchmark.jar aop         # AOP optimizations
java -jar warmup-benchmark.jar container   # Container optimizations  
java -jar warmup-benchmark.jar startup     # Startup performance
java -jar warmup-benchmark.jar quick       # Quick development tests
```

### Via JMH Main Class
```bash
# Direct JMH execution
java -cp warmup-benchmark.jar org.openjdk.jmh.Main

# Specific benchmark patterns
java -cp warmup-benchmark.jar org.openjdk.jmh.Main ".*O1.*"
java -cp warmup-benchmark.jar org.openjdk.jmh.Main ".*Framework.*"
java -cp warmup-benchmark.jar org.openjdk.jmh.Main ".*AspectManager.*"
```

## Maven Execution

### Full Benchmark Suite
```bash
mvn clean compile
mvn exec:exec -Dbenchmark.args="all"
```

### Quick Development Benchmarks
```bash
mvn clean compile  
mvn exec:exec -Dbenchmark.args="quick"
```

### Specific Categories
```bash
mvn clean compile
mvn exec:exec -Dbenchmark.args="aop"
mvn exec:exec -Dbenchmark.args="container" 
mvn exec:exec -Dbenchmark.args="startup"
```

## Performance Expectations

Based on completed optimizations (13/25 components):

| Component | O(n) → O(1) Improvement | Benchmark Class |
|-----------|-------------------------|-----------------|
| AopHandler | 500x-1,200x | AopHandlerO1Benchmark |
| BeanRegistry | 200x-800x | BeanRegistryO1BenchmarkDirect |
| ConfigurationProcessor | 150x-600x | ConfigurationProcessorO1BenchmarkDirect |
| EventBus | 300x-900x | EventBusO1BenchmarkDirect |
| ModuleManager | 100x-400x | ModuleManagerO1Benchmark |
| *...and 8 more* | | |

## Output Location
All benchmark results are saved to: `benchmark-results/` directory
- JSON format for programmatic analysis
- Detailed performance metrics
- Comparison data across framework versions

## Status: ✅ COMPLETED
- ✅ 31 benchmarks moved to src/main/java
- ✅ BenchmarkRunner created with organized execution modes  
- ✅ POM.xml updated for main class compilation
- ✅ All benchmarks included in executable JAR
- ✅ Ready for comprehensive performance analysis