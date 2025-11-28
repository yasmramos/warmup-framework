#!/bin/bash
# Script para compilar y ejecutar benchmarks del módulo warmup-benchmark

echo "=== Warmup Framework Benchmark Module Build Script ==="
echo "Directorio actual: $(pwd)"

# Navegar al módulo de benchmarks
cd /workspace/warmup-framework/warmup-benchmark

echo "Compilando módulo warmup-benchmark..."
mvn clean compile

if [ $? -eq 0 ]; then
    echo "✓ Compilación exitosa"
else
    echo "✗ Error en compilación"
    exit 1
fi

echo ""
echo "=== Opciones de Ejecución ==="
echo "1. Ejecutar benchmark rápido (desarrollo): mvn test -Dbenchmark.mode=quick"
echo "2. Ejecutar benchmark completo: mvn test -Dbenchmark.mode=full"
echo "3. Ejecutar comparación de frameworks: mvn test -Dbenchmark.mode=comparison"
echo "4. Ejecutar manualmente con JMH: mvn exec:java -Dexec.mainClass='org.openjdk.jmh.Main'"
echo ""
echo "Para ejecutar un benchmark específico, usar patrones JMH:"
echo "Ejemplo: mvn exec:java -Dexec.mainClass='org.openjdk.jmh.Main' -Dexec.args='.*O1.*'"
echo ""
echo "Archivos de configuración disponibles:"
echo "- jmh.conf: Configuración JMH optimizada"
echo "- pom.xml: Configuración Maven con perfiles de benchmarks"

# Mostrar estructura de archivos Java encontrados
echo ""
echo "=== Benchmarks Disponibles ==="
find . -name "*.java" -type f | head -20