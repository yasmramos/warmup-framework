#!/bin/bash

# 🚀 EJECUTOR DE BENCHMARK EXTREMO - STARTUP SUB-10MS
# Utiliza todas las optimizaciones de startup implementadas en el framework

echo "🚀 INICIANDO BENCHMARK EXTREMO DE STARTUP"
echo "🎯 Objetivo: < 10ms startup time (100x mejor que 73.553ms actual)"
echo "==========================================="

# Compilar el benchmark
echo "📦 Compilando benchmark extremo..."
mvn test-compile -q

# Configurar classpath con todas las dependencias
CLASSPATH="target/test-classes"
for jar in target/lib/*.jar; do
    CLASSPATH="$CLASSPATH:$jar"
done

# Añadir classes del framework warmup
CLASSPATH="$CLASSPATH:../warmup-core/target/classes:../warmup-aop/target/classes:../warmup-processor/target/classes"

echo "🚀 Ejecutando benchmark extremo con optimizaciones..."
echo "⚡ Fase Crítica + Paralelización + Zero Cost Startup + Optimizaciones Extremas"
echo "================================================================"

# Ejecutar benchmark con configuración extrema
java -Xmx512m -XX:+UseG1GC -cp "$CLASSPATH" org.openjdk.jmh.Main "io.warmup.benchmark.startup.ExtremeStartupBenchmark" -t 1 -f 1 -wi 2 -i 3 -bm singleShotTime -tu ms

echo ""
echo "🏁 BENCHMARK EXTREMO COMPLETADO"
echo "📊 Verificar resultados vs objetivo sub-10ms"