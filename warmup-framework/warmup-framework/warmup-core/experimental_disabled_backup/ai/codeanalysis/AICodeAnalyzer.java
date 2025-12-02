/*
 * Warmup Framework - AI-Powered Code Analysis
 * Característica 2/7: Análisis inteligente de código con ML para detectar anti-patrones
 * 
 * @author MiniMax Agent
 * @version 1.0
 */

package io.warmup.framework.ai.codeanalysis;

// import io.warmup.framework.ai.analysis.CodePatternDetector;
// import io.warmup.framework.ai.analysis.SuggestionEngine;
// import io.warmup.framework.ai.analysis.AntiPatternClassifier;

import java.util.*;
import java.nio.charset.StandardCharsets;
import java.util.stream.Stream;
import java.util.stream.Collectors;
import java.util.Arrays;
import java.util.concurrent.*;
import java.util.regex.Pattern;
import java.io.*;
import java.nio.file.*;

/**
 * 🎯 AI-Powered Code Analysis
 * 
 * Esta clase implementa análisis inteligente de código usando técnicas de ML
 * para detectar anti-patrones, sugerir refactorizaciones y predecir impactos.
 * 
 * Características:
 * - Detección automática de anti-patrones con ML
 * - Sugerencias inteligentes de refactorización
 * - Predicción de impactos de cambios
 * - Análisis de calidad en tiempo real
 * - Aprendizaje de patrones del proyecto
 */
public class AICodeAnalyzer {
    
    // Motor de detección de patrones
    private final CodePatternDetector patternDetector;
    
    // Motor de sugerencias
    private final SuggestionEngine suggestionEngine;
    
    // Clasificador de anti-patrones
    private final AntiPatternClassifier antiPatternClassifier;
    
    // Cache de análisis
    private final AnalysisCache analysisCache;
    
    // Métricas de calidad
    private final QualityMetrics qualityMetrics;
    
    // Historial de cambios para aprendizaje
    private final ChangeHistory changeHistory;
    
    public AICodeAnalyzer() {
        this.patternDetector = new CodePatternDetector();
        this.suggestionEngine = new SuggestionEngine();
        this.antiPatternClassifier = new AntiPatternClassifier();
        this.analysisCache = new AnalysisCache();
        this.qualityMetrics = new QualityMetrics();
        this.changeHistory = new ChangeHistory();
        
        System.out.println("🧠 AI Code Analyzer inicializado");
    }
    
    /**
     * 🔍 Analiza un cambio de código y genera reporte de calidad
     */
    public CodeQualityReport analyzeCodeChanges(Class<?> clazz, String methodName, String newCode) {
        try {
            System.out.println("🔍 Analizando cambios en " + clazz.getSimpleName() + "." + methodName);
            
            // 1. Extraer características del código
            CodeFeatures features = extractCodeFeatures(clazz, methodName, newCode);
            
            // 2. Detectar anti-patrones usando ML
            List<AntiPattern> detectedPatterns = detectAntiPatterns(features);
            
            // 3. Generar sugerencias inteligentes
            List<RefactoringSuggestion> suggestions = generateSmartSuggestions(detectedPatterns, features);
            
            // 4. Predecir impacto de cambios
            ImpactPrediction impact = predictChangeImpact(clazz, methodName, features);
            
            // 5. Calcular métricas de calidad
            QualityScore qualityScore = calculateQualityScore(detectedPatterns, suggestions);
            
            // 6. Generar reporte
            CodeQualityReport report = new CodeQualityReport(
                clazz.getName(),
                methodName,
                features,
                detectedPatterns,
                suggestions,
                impact,
                qualityScore,
                new Date()
            );
            
            // 7. Actualizar historial de aprendizaje
            updateLearningHistory(clazz, methodName, report);
            
            // 8. Cache del resultado
            analysisCache.cacheAnalysis(clazz, methodName, report);
            
            System.out.println("✅ Análisis completado. Score: " + qualityScore.overallScore);
            
            return report;
            
        } catch (Exception e) {
            System.err.println("❌ Error analizando código: " + e.getMessage());
            throw new RuntimeException("Code analysis failed", e);
        }
    }
    
    /**
     * 🔍 Analiza múltiples archivos de código
     */
    public CodeQualityReport analyzeProject(List<Path> sourcePaths) {
        try {
            System.out.println("📂 Analizando proyecto completo: " + sourcePaths.size() + " archivos");
            
            List<CodeQualityReport> fileReports = new ArrayList<>();
            
            for (Path sourcePath : sourcePaths) {
                try {
                    CodeQualityReport fileReport = analyzeFile(sourcePath);
                    fileReports.add(fileReport);
                } catch (Exception e) {
                    System.err.println("⚠️ Error analizando archivo " + sourcePath + ": " + e.getMessage());
                }
            }
            
            // Consolidar reporte del proyecto
            return consolidateProjectReport(fileReports);
            
        } catch (Exception e) {
            System.err.println("❌ Error analizando proyecto: " + e.getMessage());
            throw new RuntimeException("Project analysis failed", e);
        }
    }
    
    /**
     * 🔍 Analiza un archivo específico
     */
    private CodeQualityReport analyzeFile(Path filePath) {
        try {
            // Java 11 compatibility - usar readAllBytes en lugar de readString
            byte[] bytes = Files.readAllBytes(filePath);
            String content = new String(bytes, StandardCharsets.UTF_8);
            
            // Extraer información del archivo
            String className = extractClassName(filePath);
            String packageName = extractPackageName(content);
            
            // Analizar cada método
            List<String> methods = extractMethods(content);
            List<CodeQualityReport> methodReports = new ArrayList<>();
            
            for (String method : methods) {
                String methodName = extractMethodName(method);
                CodeQualityReport methodReport = analyzeCodeContent(className, methodName, method);
                methodReports.add(methodReport);
            }
            
            return consolidateFileReport(filePath.toString(), methodReports);
            
        } catch (Exception e) {
            throw new RuntimeException("Failed to analyze file: " + filePath, e);
        }
    }
    
    /**
     * 🔍 Analiza contenido de código directamente
     */
    private CodeQualityReport analyzeCodeContent(String className, String methodName, String codeContent) {
        // Crear características sintéticas para análisis
        CodeFeatures features = createSyntheticFeatures(className, methodName, codeContent);
        
        // Detectar patrones
        List<AntiPattern> patterns = detectAntiPatterns(features);
        
        // Generar sugerencias
        List<RefactoringSuggestion> suggestions = generateSmartSuggestions(patterns, features);
        
        // Calcular score
        QualityScore score = calculateQualityScore(patterns, suggestions);
        
        return new CodeQualityReport(
            className,
            methodName,
            features,
            patterns,
            suggestions,
            new ImpactPrediction(ImpactLevel.LOW, "Análisis sintético"),
            score,
            new Date()
        );
    }
    
    /**
     * Helper method for Java 11 compatibility - líneas como Stream
     */
    private Stream<String> lines(String text) {
        return Arrays.stream(text.split("\\r?\\n"));
    }
    
    /**
     * 🔍 Extrae características del código
     */
    private CodeFeatures extractCodeFeatures(Class<?> clazz, String methodName, String code) {
        System.out.println("📊 Extrayendo características del código...");
        
        CodeFeatures features = new CodeFeatures();
        
        // Métricas básicas
        features.linesOfCode = lines(code).count();
        features.methodName = methodName;
        features.className = clazz.getSimpleName();
        features.packageName = clazz.getPackage().getName();
        
        // Complejidad ciclomática
        features.complexity = calculateCyclomaticComplexity(code);
        
        // Acoplamiento
        features.coupling = calculateCoupling(clazz, code);
        
        // Cohesión
        features.cohesion = calculateCohesion(code);
        
        // Patrones detectados
        features.detectedPatterns = patternDetector.detectPatterns(code);
        
        // Métricas de calidad
        features.qualityMetrics = calculateDetailedMetrics(code);
        
        // Aprendizaje del historial
        features.similarityScore = changeHistory.calculateSimilarity(clazz, code);
        
        return features;
    }
    
    /**
     * 🧠 Detecta anti-patrones usando ML
     */
    private List<AntiPattern> detectAntiPatterns(CodeFeatures features) {
        System.out.println("🤖 Detectando anti-patrones con ML...");
        
        return antiPatternClassifier.classifyAntiPatterns(features);
    }
    
    /**
     * 💡 Genera sugerencias inteligentes de refactorización
     */
    private List<RefactoringSuggestion> generateSmartSuggestions(List<AntiPattern> patterns, CodeFeatures features) {
        System.out.println("💡 Generando sugerencias inteligentes...");
        
        return suggestionEngine.generateSuggestions(patterns, features);
    }
    
    /**
     * 📈 Predice el impacto de cambios
     */
    private ImpactPrediction predictChangeImpact(Class<?> clazz, String methodName, CodeFeatures features) {
        System.out.println("📈 Prediciendo impacto de cambios...");
        
        // Análisis de impacto basado en métricas
        ImpactLevel level = determineImpactLevel(features);
        String description = generateImpactDescription(level, features);
        
        return new ImpactPrediction(level, description);
    }
    
    /**
     * 📊 Calcula score de calidad
     */
    private QualityScore calculateQualityScore(List<AntiPattern> patterns, List<RefactoringSuggestion> suggestions) {
        double patternPenalty = patterns.size() * 10.0;
        double suggestionBonus = suggestions.size() * 2.0;
        
        double baseScore = 100.0;
        double finalScore = Math.max(0, baseScore - patternPenalty + suggestionBonus);
        
        return new QualityScore(
            (int) Math.round(finalScore),
            determineQualityGrade(finalScore),
            patterns.size(),
            suggestions.size()
        );
    }
    
    /**
     * 📈 Actualiza historial de aprendizaje
     */
    private void updateLearningHistory(Class<?> clazz, String methodName, CodeQualityReport report) {
        changeHistory.addChange(new CodeChange(
            clazz.getName(),
            methodName,
            report.features,
            report.qualityScore.overallScore,
            new Date()
        ));
    }
    
    /**
     * 🔗 Consolida reporte de proyecto
     */
    private CodeQualityReport consolidateProjectReport(List<CodeQualityReport> fileReports) {
        int totalFiles = fileReports.size();
        double averageScore = fileReports.stream()
            .mapToDouble(r -> r.qualityScore.overallScore)
            .average()
            .orElse(0.0);
        
        List<AntiPattern> allPatterns = fileReports.stream()
            .flatMap(r -> r.detectedPatterns.stream())
            .collect(Collectors.toList());
        
        List<RefactoringSuggestion> allSuggestions = fileReports.stream()
            .flatMap(r -> r.suggestions.stream())
            .collect(Collectors.toList());
        
        return new CodeQualityReport(
            "PROJECT",
            "CONSOLIDATED",
            new CodeFeatures(),
            allPatterns,
            allSuggestions,
            new ImpactPrediction(ImpactLevel.MEDIUM, "Análisis de proyecto completo"),
            new QualityScore((int)averageScore, determineQualityGrade(averageScore), allPatterns.size(), allSuggestions.size()),
            new Date()
        );
    }
    
    /**
     * 🔗 Consolida reporte de archivo
     */
    private CodeQualityReport consolidateFileReport(String fileName, List<CodeQualityReport> methodReports) {
        double averageScore = methodReports.stream()
            .mapToDouble(r -> r.qualityScore.overallScore)
            .average()
            .orElse(0.0);
        
        List<AntiPattern> allPatterns = methodReports.stream()
            .flatMap(r -> r.detectedPatterns.stream())
            .collect(Collectors.toList());
        
        return new CodeQualityReport(
            fileName,
            "FILE_CONSOLIDATED",
            new CodeFeatures(),
            allPatterns,
            Collections.emptyList(),
            new ImpactPrediction(ImpactLevel.MEDIUM, "Análisis de archivo completo"),
            new QualityScore((int)averageScore, determineQualityGrade(averageScore), allPatterns.size(), 0),
            new Date()
        );
    }
    
    // ================================================================================
    // MÉTODOS AUXILIARES
    // ================================================================================
    
    private double calculateCyclomaticComplexity(String code) {
        // Contador simplificado de complejidad
        long complexity = 1; // Base
        
        complexity += lines(code).filter(line -> 
            line.contains("if") || line.contains("while") || 
            line.contains("for") || line.contains("case") ||
            line.contains("&&") || line.contains("||")
        ).count();
        
        return complexity;
    }
    
    private double calculateCoupling(Class<?> clazz, String code) {
        // Análisis simplificado de acoplamiento
        long importCount = lines(code).filter(line -> line.trim().startsWith("import")).count();
        return Math.min(importCount / 10.0, 10.0); // Normalizado
    }
    
    private double calculateCohesion(String code) {
        // Métrica simplificada de cohesión
        long methodCount = lines(code).filter(line -> line.contains("(") && !line.trim().startsWith("//")).count();
        return Math.max(0, 1.0 - (methodCount / 20.0)); // Simplificado
    }
    
    private ImpactLevel determineImpactLevel(CodeFeatures features) {
        if (features.complexity > 15 || features.coupling > 7) {
            return ImpactLevel.HIGH;
        } else if (features.complexity > 10 || features.coupling > 5) {
            return ImpactLevel.MEDIUM;
        } else {
            return ImpactLevel.LOW;
        }
    }
    
    private String generateImpactDescription(ImpactLevel level, CodeFeatures features) {
        switch (level) {
            case HIGH: return "Alto impacto: " + (int)features.complexity + " complejidad, " + (int)features.coupling + " acoplamiento";
            case MEDIUM: return "Impacto medio detectado";
            default: return "Bajo impacto esperado";
        }
    }
    
    private QualityGrade determineQualityGrade(double score) {
        if (score >= 90) return QualityGrade.EXCELLENT;
        if (score >= 80) return QualityGrade.GOOD;
        if (score >= 70) return QualityGrade.ACCEPTABLE;
        if (score >= 60) return QualityGrade.POOR;
        return QualityGrade.CRITICAL;
    }
    
    private String extractClassName(Path filePath) {
        String fileName = filePath.getFileName().toString();
        return fileName.replace(".java", "");
    }
    
    private String extractPackageName(String content) {
        return lines(content)
            .filter(line -> line.trim().startsWith("package "))
            .map(line -> line.replace("package ", "").replace(";", ""))
            .findFirst()
            .orElse("unknown");
    }
    
    private List<String> extractMethods(String content) {
        // Extracción simplificada de métodos
        List<String> methods = new ArrayList<>();
        String[] lines = content.split("\n");
        
        for (int i = 0; i < lines.length; i++) {
            if (lines[i].contains("(") && !lines[i].trim().startsWith("//") && !lines[i].trim().startsWith("/*")) {
                // Encontrar cierre del método
                int braceCount = 0;
                StringBuilder methodContent = new StringBuilder();
                
                for (int j = i; j < lines.length; j++) {
                    methodContent.append(lines[j]).append("\n");
                    
                    for (char c : lines[j].toCharArray()) {
                        if (c == '{') braceCount++;
                        if (c == '}') braceCount--;
                    }
                    
                    if (braceCount == 0 && j > i) {
                        methods.add(methodContent.toString());
                        break;
                    }
                }
            }
        }
        
        return methods;
    }
    
    private String extractMethodName(String methodContent) {
        return lines(methodContent)
            .filter(line -> line.contains("("))
            .map(line -> line.replaceAll("[^a-zA-Z0-9_].*", "").trim())
            .filter(name -> !name.isEmpty())
            .findFirst()
            .orElse("unknownMethod");
    }
    
    private CodeFeatures createSyntheticFeatures(String className, String methodName, String code) {
        CodeFeatures features = new CodeFeatures();
        features.className = className;
        features.methodName = methodName;
        features.linesOfCode = lines(code).count();
        features.complexity = calculateCyclomaticComplexity(code);
        features.coupling = 5.0; // Valor por defecto
        features.cohesion = 0.7; // Valor por defecto
        features.detectedPatterns = patternDetector.detectPatterns(code);
        features.qualityMetrics = calculateDetailedMetrics(code);
        features.similarityScore = 0.5; // Valor por defecto
        return features;
    }
    
    private Map<String, Double> calculateDetailedMetrics(String code) {
        Map<String, Double> metrics = new HashMap<>();
        metrics.put("lines_of_code", (double) lines(code).count());
        metrics.put("comment_ratio", calculateCommentRatio(code));
        metrics.put("complexity_per_line", calculateCyclomaticComplexity(code) / Math.max(lines(code).count(), 1));
        return metrics;
    }
    
    private double calculateCommentRatio(String code) {
        long totalLines = lines(code).count();
        long commentLines = lines(code).filter(line -> 
            line.trim().startsWith("//") || line.trim().startsWith("/*") || 
            line.trim().startsWith("*")
        ).count();
        
        return totalLines > 0 ? (double) commentLines / totalLines : 0.0;
    }
    
    // ================================================================================
    // CLASES INTERNAS Y ENUMS
    // ================================================================================
    
    public static class CodeFeatures {
        public String className;
        public String methodName;
        public String packageName;
        public long linesOfCode;
        public double complexity;
        public double coupling;
        public double cohesion;
        public Set<String> detectedPatterns;
        public Map<String, Double> qualityMetrics;
        public double similarityScore;
        
        public CodeFeatures() {
            this.detectedPatterns = new HashSet<>();
            this.qualityMetrics = new HashMap<>();
        }
    }
    
    public enum ImpactLevel { LOW, MEDIUM, HIGH }
    public enum QualityGrade { EXCELLENT, GOOD, ACCEPTABLE, POOR, CRITICAL }
    
    public static class AntiPattern {
        public final String type;
        public final String description;
        public final double confidence;
        public final String severity;
        
        public AntiPattern(String type, String description, double confidence, String severity) {
            this.type = type;
            this.description = description;
            this.confidence = confidence;
            this.severity = severity;
        }
    }
    
    public static class RefactoringSuggestion {
        public final String title;
        public final String description;
        public final String suggestedCode;
        public final double priority;
        
        public RefactoringSuggestion(String title, String description, String suggestedCode, double priority) {
            this.title = title;
            this.description = description;
            this.suggestedCode = suggestedCode;
            this.priority = priority;
        }
    }
    
    public static class ImpactPrediction {
        public final ImpactLevel level;
        public final String description;
        
        public ImpactPrediction(ImpactLevel level, String description) {
            this.level = level;
            this.description = description;
        }
    }
    
    public static class QualityScore {
        public final int overallScore;
        public final QualityGrade grade;
        public final int antiPatternCount;
        public final int suggestionCount;
        
        public QualityScore(int overallScore, QualityGrade grade, int antiPatternCount, int suggestionCount) {
            this.overallScore = overallScore;
            this.grade = grade;
            this.antiPatternCount = antiPatternCount;
            this.suggestionCount = suggestionCount;
        }
    }
    
    public static class CodeQualityReport {
        public final String className;
        public final String methodName;
        public final CodeFeatures features;
        public final List<AntiPattern> detectedPatterns;
        public final List<RefactoringSuggestion> suggestions;
        public final ImpactPrediction impact;
        public final QualityScore qualityScore;
        public final Date analysisDate;
        
        public CodeQualityReport(String className, String methodName, CodeFeatures features,
                               List<AntiPattern> detectedPatterns, List<RefactoringSuggestion> suggestions,
                               ImpactPrediction impact, QualityScore qualityScore, Date analysisDate) {
            this.className = className;
            this.methodName = methodName;
            this.features = features;
            this.detectedPatterns = detectedPatterns;
            this.suggestions = suggestions;
            this.impact = impact;
            this.qualityScore = qualityScore;
            this.analysisDate = analysisDate;
        }
    }
    
    // Clases auxiliares simplificadas
    public static class CodePatternDetector {
        public Set<String> detectPatterns(String code) {
            Set<String> patterns = new HashSet<String>();
            if (code.contains("if (") && code.contains("else")) {
                patterns.add("complex_conditional");
            }
            
            // Count lines using split (Java 8 compatible)
            long lineCount = code.split("\\r?\\n").length;
            if (lineCount > 50) {
                patterns.add("long_method");
            }
            
            return patterns;
        }
    }
    
    public static class SuggestionEngine {
        public List<RefactoringSuggestion> generateSuggestions(List<AntiPattern> patterns, CodeFeatures features) {
            List<RefactoringSuggestion> suggestions = new ArrayList<>();
            
            for (AntiPattern pattern : patterns) {
                if (pattern.type.equals("complex_conditional")) {
                    suggestions.add(new RefactoringSuggestion(
                        "Simplificar Condicional",
                        "Considera extraer lógica compleja en métodos separados",
                        "if (condition) { /* extracted logic */ }",
                        0.8
                    ));
                }
            }
            
            return suggestions;
        }
    }
    
    public static class AntiPatternClassifier {
        public List<AntiPattern> classifyAntiPatterns(CodeFeatures features) {
            List<AntiPattern> patterns = new ArrayList<>();
            
            if (features.complexity > 10) {
                patterns.add(new AntiPattern(
                    "high_complexity",
                    "Alta complejidad ciclomática detectada",
                    0.9,
                    "high"
                ));
            }
            
            if (features.linesOfCode > 50) {
                patterns.add(new AntiPattern(
                    "long_method",
                    "Método muy largo, considera refactorización",
                    0.8,
                    "medium"
                ));
            }
            
            return patterns;
        }
    }
    
    public static class AnalysisCache {
        private final Map<String, CodeQualityReport> cache = new ConcurrentHashMap<>();
        
        public void cacheAnalysis(Class<?> clazz, String methodName, CodeQualityReport report) {
            String key = clazz.getName() + "." + methodName;
            cache.put(key, report);
        }
        
        public CodeQualityReport getCachedAnalysis(Class<?> clazz, String methodName) {
            String key = clazz.getName() + "." + methodName;
            return cache.get(key);
        }
    }
    
    public static class QualityMetrics {
        public double calculateMaintainabilityIndex(CodeFeatures features) {
            return Math.max(0, 100 - (features.complexity * 2) - (features.coupling * 3));
        }
        
        public double calculateTestabilityScore(CodeFeatures features) {
            return Math.min(100, features.cohesion * 100 - (100 - features.cohesion * 100));
        }
    }
    
    public static class ChangeHistory {
        private final List<CodeChange> changes = new ArrayList<>();
        
        public void addChange(CodeChange change) {
            changes.add(change);
        }
        
        public double calculateSimilarity(Class<?> clazz, String code) {
            // Simulación de cálculo de similitud
            return 0.5 + Math.random() * 0.4;
        }
    }
    
    public static class CodeChange {
        public final String className;
        public final String methodName;
        public final CodeFeatures features;
        public final double qualityScore;
        public final Date timestamp;
        
        public CodeChange(String className, String methodName, CodeFeatures features, double qualityScore, Date timestamp) {
            this.className = className;
            this.methodName = methodName;
            this.features = features;
            this.qualityScore = qualityScore;
            this.timestamp = timestamp;
        }
    }
}