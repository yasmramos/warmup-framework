package io.warmup.examples.benchmark;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.util.*;

public class BenchmarkResultAnalyzer {
    
    public static void main(String[] args) throws Exception {
        System.out.println("ANALIZADOR DE RESULTADOS JMH - WARMUP\n");
        
        analyzeDirectory("results");
    }
    
    private static void analyzeDirectory(String directoryPath) throws Exception {
        File dir = new File(directoryPath);
        if (!dir.exists()) {
            System.out.println("Directorio no encontrado: " + directoryPath);
            return;
        }
        
        File[] resultFiles = dir.listFiles((d, name) -> name.endsWith("-results.json"));
        if (resultFiles == null || resultFiles.length == 0) {
            System.out.println("No se encontraron archivos de resultados en: " + directoryPath);
            return;
        }
        
        for (File resultFile : resultFiles) {
            analyzeResults(resultFile);
        }
    }
    
    private static void analyzeResults(File resultFile) throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree(resultFile);
        
        System.out.println("\n" + resultFile.getName().replace("-results.json", "").toUpperCase());
        System.out.println("============================================================");
        
        List<BenchmarkResult> results = new ArrayList<>();
        
        for (JsonNode benchmark : root) {
            String benchmarkName = benchmark.get("benchmark").asText();
            double score = benchmark.get("primaryMetric").get("score").asDouble();
            String unit = benchmark.get("primaryMetric").get("scoreUnit").asText();
            double error = benchmark.get("primaryMetric").get("scoreError").asDouble();
            
            results.add(new BenchmarkResult(benchmarkName, score, unit, error));
            
            System.out.printf("🏃 %-40s: %8.3f %s ±%.3f%n", 
                getSimpleName(benchmarkName), score, unit, error);
        }
        
        // Análisis comparativo si hay múltiples resultados
        if (results.size() > 1) {
            performComparativeAnalysis(results);
        }
    }
    
    private static void performComparativeAnalysis(List<BenchmarkResult> results) {
        System.out.println("\nANÁLISIS COMPARATIVO:");
        
        // Encontrar el mejor resultado (menor tiempo o mayor throughput)
        BenchmarkResult best = results.get(0);
        for (BenchmarkResult result : results) {
            if (result.unit.equals("ms/op") && result.score < best.score) {
                best = result; // Menor tiempo es mejor
            } else if (result.unit.equals("ops/s") && result.score > best.score) {
                best = result; // Mayor throughput es mejor
            }
        }
        
        System.out.println("   Mejor rendimiento: " + getSimpleName(best.name) + 
                          " (" + String.format("%.3f", best.score) + " " + best.unit + ")");
        
        // Calcular mejoras relativas
        for (BenchmarkResult result : results) {
            if (!result.name.equals(best.name)) {
                double improvement = 0;
                if (result.unit.equals("ms/op")) {
                    improvement = ((result.score - best.score) / result.score) * 100;
                } else {
                    improvement = ((best.score - result.score) / result.score) * 100;
                }
                System.out.printf("   %s vs %s: %+.1f%%%n", 
                    getSimpleName(best.name), getSimpleName(result.name), improvement);
            }
        }
    }
    
    private static String getSimpleName(String fullName) {
        return fullName.substring(fullName.lastIndexOf('.') + 1);
    }
    
    static class BenchmarkResult {
        String name;
        double score;
        String unit;
        double error;
        
        BenchmarkResult(String name, double score, String unit, double error) {
            this.name = name;
            this.score = score;
            this.unit = unit;
            this.error = error;
        }
    }
}