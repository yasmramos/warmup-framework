package io.warmup.framework.ai.examples;

import io.warmup.framework.ai.TribuoAIIntegration;
import io.warmup.framework.ai.TribuoAIIntegration.*;
import io.warmup.framework.ai.automl.AutoMLHotReloadEngine;
import io.warmup.framework.ai.automl.AutoMLHotReloadEngine.*;
import io.warmup.framework.hotreload.HotReloadManager;

import org.tribuo.*;
import org.tribuo.classification.Label;
import org.tribuo.classification.LabelFactory;
import org.tribuo.regression.Regressor;
import org.tribuo.regression.RegressionFactory;
import org.tribuo.impl.ArrayExample;

import java.util.*;

/**
 * Ejemplo Completo de Uso del Sistema de IA
 * 
 * Demuestra:
 * 1. Entrenamiento de modelos de clasificación
 * 2. Entrenamiento de modelos de regresión
 * 3. AutoML con búsqueda automática de algoritmos
 * 4. Persistencia y carga de modelos
 * 5. Evaluación con métricas completas
 * 6. Integración con Hot Reload
 * 
 * @author MiniMax Agent
 * @version 2.0
 */
public class CompleteAIExample {
    
    public static void main(String[] args) {
        System.out.println("╔═══════════════════════════════════════════════════════════╗");
        System.out.println("║   WARMUP FRAMEWORK - SISTEMA DE IA COMPLETO CON TRIBUO   ║");
        System.out.println("╚═══════════════════════════════════════════════════════════╝\n");
        
        try {
            // Inicializar componentes
            TribuoAIIntegration aiIntegration = new TribuoAIIntegration();
            HotReloadManager hotReloadManager = new HotReloadManager();
            AutoMLHotReloadEngine autoML = new AutoMLHotReloadEngine(hotReloadManager);
            
            // Ejecutar ejemplos
            System.out.println("📋 Ejecutando ejemplos de IA...\n");
            
            exampleClassification(aiIntegration);
            System.out.println("\n" + "=".repeat(70) + "\n");
            
            exampleRegression(aiIntegration);
            System.out.println("\n" + "=".repeat(70) + "\n");
            
            exampleAutoML(autoML);
            System.out.println("\n" + "=".repeat(70) + "\n");
            
            examplePersistence(aiIntegration);
            System.out.println("\n" + "=".repeat(70) + "\n");
            
            System.out.println("✅ Todos los ejemplos ejecutados exitosamente");
            
        } catch (Exception e) {
            System.err.println("❌ Error en ejemplo: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Ejemplo 1: Clasificación con Logistic Regression
     */
    private static void exampleClassification(TribuoAIIntegration aiIntegration) {
        System.out.println("📊 EJEMPLO 1: CLASIFICACIÓN CON LOGISTIC REGRESSION");
        System.out.println("─".repeat(70));
        
        try {
            // Crear dataset sintético para clasificación binaria
            LabelFactory labelFactory = new LabelFactory();
            
            // Training data
            List<Example<Label>> trainingExamples = createClassificationDataset(
                labelFactory, 100, 0.8
            );
            Dataset<Label> trainingData = new MutableDataset<>(trainingExamples);
            
            // Test data
            List<Example<Label>> testExamples = createClassificationDataset(
                labelFactory, 30, 0.2
            );
            Dataset<Label> testData = new MutableDataset<>(testExamples);
            
            System.out.println("📈 Dataset creado:");
            System.out.println("  - Training: " + trainingData.size() + " ejemplos");
            System.out.println("  - Test: " + testData.size() + " ejemplos");
            
            // Entrenar modelo
            System.out.println("\n🔧 Entrenando modelo de clasificación...");
            Model<Label> model = aiIntegration.trainClassificationModel(
                "iris-classifier",
                trainingData,
                10  // iterations
            );
            
            System.out.println("✅ Modelo entrenado: " + model.getName());
            
            // Evaluar modelo
            System.out.println("\n📊 Evaluando modelo...");
            ClassificationMetrics metrics = aiIntegration.evaluateClassificationModel(
                "iris-classifier",
                testData
            );
            
            System.out.println("\n📈 Resultados de Evaluación:");
            System.out.println("  ├─ Accuracy:   " + String.format("%.4f", metrics.accuracy));
            System.out.println("  ├─ Precision:  " + String.format("%.4f", metrics.precision));
            System.out.println("  ├─ Recall:     " + String.format("%.4f", metrics.recall));
            System.out.println("  ├─ Macro F1:   " + String.format("%.4f", metrics.macroF1));
            System.out.println("  └─ Micro F1:   " + String.format("%.4f", metrics.microF1));
            
            // Hacer predicción
            System.out.println("\n🔮 Haciendo predicción en nuevo ejemplo...");
            Example<Label> newExample = trainingExamples.get(0); // Usar primer ejemplo
            Prediction<Label> prediction = aiIntegration.predictClassification(
                "iris-classifier",
                newExample
            );
            
            System.out.println("  Predicción: " + prediction.getOutput());
            System.out.println("  Confianza: " + 
                String.format("%.4f", prediction.getOutput().getScore()));
            
        } catch (Exception e) {
            System.err.println("❌ Error en ejemplo de clasificación: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Ejemplo 2: Regresión con Linear SGD
     */
    private static void exampleRegression(TribuoAIIntegration aiIntegration) {
        System.out.println("📉 EJEMPLO 2: REGRESIÓN CON LINEAR SGD");
        System.out.println("─".repeat(70));
        
        try {
            // Crear dataset sintético para regresión
            RegressionFactory regressionFactory = new RegressionFactory();
            
            // Training data
            List<Example<Regressor>> trainingExamples = createRegressionDataset(
                regressionFactory, 100
            );
            Dataset<Regressor> trainingData = new MutableDataset<>(trainingExamples);
            
            // Test data
            List<Example<Regressor>> testExamples = createRegressionDataset(
                regressionFactory, 30
            );
            Dataset<Regressor> testData = new MutableDataset<>(testExamples);
            
            System.out.println("📈 Dataset creado:");
            System.out.println("  - Training: " + trainingData.size() + " ejemplos");
            System.out.println("  - Test: " + testData.size() + " ejemplos");
            
            // Entrenar modelo
            System.out.println("\n🔧 Entrenando modelo de regresión...");
            Model<Regressor> model = aiIntegration.trainRegressionModel(
                "price-predictor",
                trainingData,
                0.1,  // learning rate
                10    // epochs
            );
            
            System.out.println("✅ Modelo entrenado: " + model.getName());
            
            // Evaluar modelo
            System.out.println("\n📊 Evaluando modelo...");
            RegressionMetrics metrics = aiIntegration.evaluateRegressionModel(
                "price-predictor",
                testData
            );
            
            System.out.println("\n📈 Resultados de Evaluación:");
            System.out.println("  ├─ R² Score:     " + String.format("%.4f", metrics.averageR2));
            System.out.println("  ├─ RMSE:         " + String.format("%.4f", metrics.averageRMSE));
            System.out.println("  └─ MAE:          " + String.format("%.4f", metrics.averageMAE));
            
            // Detalles por dimensión
            System.out.println("\n  📊 Métricas por dimensión:");
            for (Map.Entry<String, DimensionMetrics> entry : metrics.dimensionMetrics.entrySet()) {
                DimensionMetrics dm = entry.getValue();
                System.out.println("    " + entry.getKey() + ":");
                System.out.println("      R²: " + String.format("%.4f", dm.r2));
                System.out.println("      RMSE: " + String.format("%.4f", dm.rmse));
            }
            
            // Hacer predicción
            System.out.println("\n🔮 Haciendo predicción en nuevo ejemplo...");
            Example<Regressor> newExample = trainingExamples.get(0);
            Prediction<Regressor> prediction = aiIntegration.predictRegression(
                "price-predictor",
                newExample
            );
            
            System.out.println("  Predicción: " + prediction.getOutput());
            
        } catch (Exception e) {
            System.err.println("❌ Error en ejemplo de regresión: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Ejemplo 3: AutoML con búsqueda automática
     */
    private static void exampleAutoML(AutoMLHotReloadEngine autoML) {
        System.out.println("🤖 EJEMPLO 3: AUTOML CON BÚSQUEDA AUTOMÁTICA");
        System.out.println("─".repeat(70));
        
        try {
            // Crear dataset para AutoML
            LabelFactory labelFactory = new LabelFactory();
            
            List<Example<Label>> trainingExamples = createClassificationDataset(
                labelFactory, 150, 1.0
            );
            Dataset<Label> trainingData = new MutableDataset<>(trainingExamples);
            
            List<Example<Label>> validationExamples = createClassificationDataset(
                labelFactory, 50, 0.5
            );
            Dataset<Label> validationData = new MutableDataset<>(validationExamples);
            
            System.out.println("📈 Dataset creado para AutoML:");
            System.out.println("  - Training: " + trainingData.size() + " ejemplos");
            System.out.println("  - Validation: " + validationData.size() + " ejemplos");
            
            // Configurar optimización
            OptimizationConfig config = new OptimizationConfig(0.05, 5);
            
            // Ejecutar AutoML
            System.out.println("\n🚀 Iniciando AutoML...");
            System.out.println("  Buscando el mejor algoritmo automáticamente...");
            
            OptimizationResult result = autoML.optimizeClassificationModel(
                "automl-model",
                trainingData,
                validationData,
                config
            );
            
            System.out.println("\n🏆 AutoML Completado:");
            System.out.println("  ├─ Mejor algoritmo: " + result.bestAlgorithm);
            System.out.println("  ├─ Score original:  " + String.format("%.4f", result.originalScore));
            System.out.println("  ├─ Score optimizado:" + String.format("%.4f", result.optimizedScore));
            System.out.println("  ├─ Mejora:          " + String.format("%.2f%%", result.improvementPercentage));
            System.out.println("  └─ Tiempo:          " + result.optimizationTimeMs + "ms");
            
            // Obtener métricas detalladas
            if (result.metrics instanceof ClassificationMetrics) {
                ClassificationMetrics metrics = (ClassificationMetrics) result.metrics;
                System.out.println("\n  📊 Métricas del modelo optimizado:");
                System.out.println("    Accuracy: " + String.format("%.4f", metrics.accuracy));
                System.out.println("    F1 Score: " + String.format("%.4f", metrics.macroF1));
            }
            
        } catch (Exception e) {
            System.err.println("❌ Error en ejemplo de AutoML: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Ejemplo 4: Persistencia de modelos
     */
    private static void examplePersistence(TribuoAIIntegration aiIntegration) {
        System.out.println("💾 EJEMPLO 4: PERSISTENCIA DE MODELOS");
        System.out.println("─".repeat(70));
        
        try {
            // Crear y entrenar modelo
            LabelFactory labelFactory = new LabelFactory();
            List<Example<Label>> trainingExamples = createClassificationDataset(
                labelFactory, 80, 1.0
            );
            Dataset<Label> trainingData = new MutableDataset<>(trainingExamples);
            
            System.out.println("🔧 Entrenando modelo para guardar...");
            aiIntegration.trainClassificationModel(
                "persistent-model",
                trainingData,
                10
            );
            
            // Guardar modelo
            String modelPath = "models/persistent-model.tribuo";
            System.out.println("\n💾 Guardando modelo en: " + modelPath);
            aiIntegration.saveModel("persistent-model", modelPath);
            System.out.println("✅ Modelo guardado exitosamente");
            
            // Eliminar del cache
            System.out.println("\n🗑️  Eliminando modelo del cache...");
            aiIntegration.removeModel("persistent-model");
            System.out.println("✅ Modelo eliminado del cache");
            
            // Cargar modelo
            System.out.println("\n📂 Cargando modelo desde disco...");
            aiIntegration.loadModel("persistent-model-loaded", modelPath);
            System.out.println("✅ Modelo cargado exitosamente");
            
            // Verificar modelo cargado
            String modelInfo = aiIntegration.getModelInfo("persistent-model-loaded");
            System.out.println("\n📋 Información del modelo cargado:");
            System.out.println(modelInfo);
            
            // Hacer predicción con modelo cargado
            System.out.println("🔮 Haciendo predicción con modelo cargado...");
            Prediction<Label> prediction = aiIntegration.predictClassification(
                "persistent-model-loaded",
                trainingExamples.get(0)
            );
            System.out.println("  Predicción: " + prediction.getOutput());
            System.out.println("✅ Modelo cargado funciona correctamente");
            
        } catch (Exception e) {
            System.err.println("❌ Error en ejemplo de persistencia: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    // ========================================================================
    // MÉTODOS AUXILIARES PARA CREAR DATASETS
    // ========================================================================
    
    /**
     * Crea dataset sintético para clasificación binaria
     */
    private static List<Example<Label>> createClassificationDataset(
            LabelFactory labelFactory, int size, double ratio) {
        
        List<Example<Label>> examples = new ArrayList<>();
        Random random = new Random(42);
        
        String[] featureNames = {"feature1", "feature2", "feature3", "feature4"};
        
        for (int i = 0; i < size; i++) {
            // Generar características aleatorias
            double[] features = new double[4];
            for (int j = 0; j < 4; j++) {
                features[j] = random.nextDouble() * 10;
            }
            
            // Determinar label basado en regla simple
            String labelStr;
            if (features[0] + features[1] > 10) {
                labelStr = "positive";
            } else {
                labelStr = "negative";
            }
            
            Label label = labelFactory.generateOutput(labelStr);
            
            // Crear ejemplo
            ArrayExample<Label> example = new ArrayExample<>(label, featureNames, features);
            examples.add(example);
        }
        
        return examples;
    }
    
    /**
     * Crea dataset sintético para regresión
     */
    private static List<Example<Regressor>> createRegressionDataset(
            RegressionFactory regressionFactory, int size) {
        
        List<Example<Regressor>> examples = new ArrayList<>();
        Random random = new Random(42);
        
        String[] featureNames = {"x1", "x2", "x3"};
        String[] targetNames = {"y"};
        
        for (int i = 0; i < size; i++) {
            // Generar características
            double[] features = new double[3];
            for (int j = 0; j < 3; j++) {
                features[j] = random.nextDouble() * 10;
            }
            
            // Calcular target: y = 2*x1 + 3*x2 - x3 + noise
            double y = 2 * features[0] + 3 * features[1] - features[2] 
                     + (random.nextDouble() - 0.5) * 2;
            
            Regressor regressor = new Regressor(targetNames[0], y);
            
            // Crear ejemplo
            ArrayExample<Regressor> example = new ArrayExample<>(
                regressor, featureNames, features
            );
            examples.add(example);
        }
        
        return examples;
    }
}
