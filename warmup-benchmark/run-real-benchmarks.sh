#!/bin/bash
# Script para ejecutar benchmarks JMH y obtener resultados reales

echo "=== EJECUTANDO BENCHMARKS JMH REALES ==="
echo "Objetivo: Obtener comparaciones O(1) vs O(n) con métricas actuales"

# Ir al directorio de benchmarks
cd /workspace/warmup-framework/warmup-benchmark

echo "1. Compilando benchmarks..."
mvn compile -q

if [ $? -eq 0 ]; then
    echo "✓ Compilación exitosa"
else
    echo "✗ Error en compilación, intentando con JAR existente..."
fi

echo ""
echo "2. Ejecutando benchmark rápido de comparación..."
echo "Configuración: 1 fork, 1 warmup, 1 measurement (máximo 30 segundos)"

# Ejecutar benchmark específico de comparación con timeout
timeout 30s mvn exec:java \
  -Dexec.mainClass="org.openjdk.jmh.Main" \
  -Dexec.args=".*Comparison.* -f 1 -wi 1 -i 1 -o quick-results.json" 2>&1

echo ""
echo "3. Ejecutando benchmark específico O(1) vs O(n)..."
timeout 30s mvn exec:java \
  -Dexec.mainClass="org.openjdk.jmh.Main" \
  -Dexec.args=".*O1.* -f 1 -wi 1 -i 1" 2>&1

echo ""
echo "4. Intentando con benchmark standalone existente..."
cd /workspace/standalone-benchmark/standalone-complexity

if [ -f "target/standalone-complexity-1.0-SNAPSHOT.jar" ]; then
    echo "Usando JAR existente para benchmark rápido..."
    timeout 30s java -cp "target/standalone-complexity-1.0-SNAPSHOT.jar:$(find ~/.m2 -name 'jmh-core-*.jar' | head -1)" \
      org.openjdk.jmh.Main -f 1 -wi 1 -i 1 2>&1
else
    echo "JAR no encontrado, compilando..."
    timeout 60s mvn package -q
    if [ $? -eq 0 ]; then
        echo "Compilación exitosa, ejecutando benchmark..."
        timeout 30s java -cp "target/standalone-complexity-1.0-SNAPSHOT.jar:$(find ~/.m2 -name 'jmh-core-*.jar' | head -1)" \
          org.openjdk.jmh.Main -f 1 -wi 1 -i 1 2>&1
    else
        echo "Error en compilación"
    fi
fi

echo ""
echo "=== RESULTADOS DISPONIBLES ==="
echo "Archivo de resultados previo: /workspace/standalone-benchmark/standalone-complexity/jmh-result.json"
echo "Reporte completo: /workspace/warmup-framework/JMH_REAL_RESULTS_O1_VS_ON.md"

echo ""
echo "Para ver resultados reales, revisar:"
echo "- jmh-result.json (datos crudos)"
echo "- JMH_REAL_RESULTS_O1_VS_ON.md (análisis completo)"