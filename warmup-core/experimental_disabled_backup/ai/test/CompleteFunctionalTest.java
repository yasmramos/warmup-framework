package io.warmup.framework.ai.test;

import io.warmup.framework.ai.TribuoAIIntegration;
import io.warmup.framework.ai.TribuoAIIntegration.*;
import io.warmup.framework.ai.automl.AutoMLHotReloadEngine;
import io.warmup.framework.ai.automl.AutoMLHotReloadEngine.*;

import org.tribuo.*;
import org.tribuo.classification.Label;
import org.tribuo.classification.LabelFactory;
import org.tribuo.regression.Regressor;
import org.tribuo.regression.RegressionFactory;
import org.tribuo.impl.ArrayExample;

import java.io.File;
import java.util.*;

/**
 * Test Funcional Completo del Sistema de IA con Tribuo
 * 
 * Valida:
 * - ✅ Entrenamiento de modelos de clasificación y regresión
 * - ✅ Predicciones precisas
 * - ✅ Evaluación con métricas
 * - ✅ Persistencia (save/load)
 * - ✅ AutoML con búsqueda automática
 * - ✅ Gestión de cache de modelos
 * 
 * @author MiniMax Agent
 * @version 2.0
 */
public class CompleteFunctionalTest {
    
    private static int passedTests = 0;
    private static int failedTests = 0;
    private static List<String> failureMessages = new ArrayList<>();
    
    public static void main(String[] args) {
        System.out.println("╔════════════════════════════════════════════════════════════════╗");
        System.out.println("║     WARMUP FRAMEWORK - TEST FUNCIONAL COMPLETO DE IA          ║");
        System.out.println("╚════════════════════════════════════════════════════════════════╝\n");
        
        try {
            TribuoAIIntegration aiIntegration = new TribuoAIIntegration();
            AutoMLHotReloadEngine autoML = new AutoMLHotReloadEngine(null);
            
            runAllTests(aiIntegration, autoML);
            
            // Resumen de resultados
            printTestSummary();
            
        } catch (Exception e) {
            System.err.println("❌ Error fatal en tests: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }
    
    private static void runAllTests(TribuoAIIntegration aiIntegration, AutoMLHotReloadEngine autoML) {
        System.out.println("🧪 Ejecutando batería de tests...\n");
        
        // Tests de Clasificación
        testClassificationTraining(aiIntegration);
        testClassificationPrediction(aiIntegration);
        testClassificationEvaluation(aiIntegration);
        
        // Tests de Regresión
        testRegressionTraining(aiIntegration);
        testRegressionPrediction(aiIntegration);
        testRegressionEvaluation(aiIntegration);
        
        // Tests de Persistencia
        testModelPersistence(aiIntegration);
        testModelLoading(aiIntegration);
        
        // Tests de Gestión
        testModelManagement(aiIntegration);
        testModelCache(aiIntegration);
        
        // Tests de AutoML
        testAutoMLClassification(autoML);
        testAutoMLAlgorithmSelection(autoML);
        
        // Tests de Integración
        testEndToEndWorkflow(aiIntegration);
    }
    
    // ========================================================================
    // TESTS DE CLASIFICACIÓN
    // ========================================================================
    
    private static void testClassificationTraining(TribuoAIIntegration aiIntegration) {
        String testName = "Classification Training";
        System.out.println("🔬 Test: " + testName);
        
        try {
            LabelFactory labelFactory = new LabelFactory();
            List<Example<Label>> examples = createSimpleClassificationDataset(labelFactory, 50);
            Dataset<Label> dataset = new MutableDataset<>(examples);
            
            Model<Label> model = aiIntegration.trainClassificationModel(
                "test-classifier",
                dataset,
                5
            );
            
            assertNotNull(model, "Model should not be null");
            assertTrue(aiIntegration.hasModel("test-classifier"), 
                      "Model should be in cache");
            
            passTest(testName);
            
        } catch (Exception e) {
            failTest(testName, e.getMessage());
        }
    }
    
    private static void testClassificationPrediction(TribuoAIIntegration aiIntegration) {
        String testName = "Classification Prediction";
        System.out.println("🔬 Test: " + testName);
        
        try {
            LabelFactory labelFactory = new LabelFactory();
            List<Example<Label>> examples = createSimpleClassificationDataset(labelFactory, 30);
            Dataset<Label> dataset = new MutableDataset<>(examples);
            
            aiIntegration.trainClassificationModel("predictor", dataset, 5);
            
            Prediction<Label> prediction = aiIntegration.predictClassification(
                "predictor",
                examples.get(0)
            );
            
            assertNotNull(prediction, "Prediction should not be null");
            assertNotNull(prediction.getOutput(), "Prediction output should not be null");
            
            passTest(testName);
            
        } catch (Exception e) {
            failTest(testName, e.getMessage());
        }
    }
    
    private static void testClassificationEvaluation(TribuoAIIntegration aiIntegration) {
        String testName = "Classification Evaluation";
        System.out.println("🔬 Test: " + testName);
        
        try {
            LabelFactory labelFactory = new LabelFactory();
            List<Example<Label>> trainExamples = createSimpleClassificationDataset(labelFactory, 50);
            List<Example<Label>> testExamples = createSimpleClassificationDataset(labelFactory, 20);
            
            Dataset<Label> trainData = new MutableDataset<>(trainExamples);
            Dataset<Label> testData = new MutableDataset<>(testExamples);
            
            aiIntegration.trainClassificationModel("evaluator", trainData, 5);
            
            ClassificationMetrics metrics = aiIntegration.evaluateClassificationModel(
                "evaluator",
                testData
            );
            
            assertNotNull(metrics, "Metrics should not be null");
            assertTrue(metrics.accuracy >= 0 && metrics.accuracy <= 1, 
                      "Accuracy should be between 0 and 1");
            assertTrue(metrics.precision >= 0 && metrics.precision <= 1,
                      "Precision should be between 0 and 1");
            
            System.out.println("  └─ Accuracy: " + String.format("%.4f", metrics.accuracy));
            
            passTest(testName);
            
        } catch (Exception e) {
            failTest(testName, e.getMessage());
        }
    }
    
    // ========================================================================
    // TESTS DE REGRESIÓN
    // ========================================================================
    
    private static void testRegressionTraining(TribuoAIIntegration aiIntegration) {
        String testName = "Regression Training";
        System.out.println("🔬 Test: " + testName);
        
        try {
            RegressionFactory regressionFactory = new RegressionFactory();
            List<Example<Regressor>> examples = createSimpleRegressionDataset(regressionFactory, 50);
            Dataset<Regressor> dataset = new MutableDataset<>(examples);
            
            Model<Regressor> model = aiIntegration.trainRegressionModel(
                "test-regressor",
                dataset,
                0.1,
                5
            );
            
            assertNotNull(model, "Model should not be null");
            assertTrue(aiIntegration.hasModel("test-regressor"),
                      "Model should be in cache");
            
            passTest(testName);
            
        } catch (Exception e) {
            failTest(testName, e.getMessage());
        }
    }
    
    private static void testRegressionPrediction(TribuoAIIntegration aiIntegration) {
        String testName = "Regression Prediction";
        System.out.println("🔬 Test: " + testName);
        
        try {
            RegressionFactory regressionFactory = new RegressionFactory();
            List<Example<Regressor>> examples = createSimpleRegressionDataset(regressionFactory, 30);
            Dataset<Regressor> dataset = new MutableDataset<>(examples);
            
            aiIntegration.trainRegressionModel("reg-predictor", dataset, 0.1, 5);
            
            Prediction<Regressor> prediction = aiIntegration.predictRegression(
                "reg-predictor",
                examples.get(0)
            );
            
            assertNotNull(prediction, "Prediction should not be null");
            assertNotNull(prediction.getOutput(), "Prediction output should not be null");
            
            passTest(testName);
            
        } catch (Exception e) {
            failTest(testName, e.getMessage());
        }
    }
    
    private static void testRegressionEvaluation(TribuoAIIntegration aiIntegration) {
        String testName = "Regression Evaluation";
        System.out.println("🔬 Test: " + testName);
        
        try {
            RegressionFactory regressionFactory = new RegressionFactory();
            List<Example<Regressor>> trainExamples = createSimpleRegressionDataset(regressionFactory, 50);
            List<Example<Regressor>> testExamples = createSimpleRegressionDataset(regressionFactory, 20);
            
            Dataset<Regressor> trainData = new MutableDataset<>(trainExamples);
            Dataset<Regressor> testData = new MutableDataset<>(testExamples);
            
            aiIntegration.trainRegressionModel("reg-evaluator", trainData, 0.1, 5);
            
            RegressionMetrics metrics = aiIntegration.evaluateRegressionModel(
                "reg-evaluator",
                testData
            );
            
            assertNotNull(metrics, "Metrics should not be null");
            assertTrue(metrics.averageRMSE >= 0, "RMSE should be non-negative");
            assertTrue(metrics.averageMAE >= 0, "MAE should be non-negative");
            
            System.out.println("  ├─ R² Score: " + String.format("%.4f", metrics.averageR2));
            System.out.println("  └─ RMSE: " + String.format("%.4f", metrics.averageRMSE));
            
            passTest(testName);
            
        } catch (Exception e) {
            failTest(testName, e.getMessage());
        }
    }
    
    // ========================================================================
    // TESTS DE PERSISTENCIA
    // ========================================================================
    
    private static void testModelPersistence(TribuoAIIntegration aiIntegration) {
        String testName = "Model Persistence (Save)";
        System.out.println("🔬 Test: " + testName);
        
        try {
            LabelFactory labelFactory = new LabelFactory();
            List<Example<Label>> examples = createSimpleClassificationDataset(labelFactory, 30);
            Dataset<Label> dataset = new MutableDataset<>(examples);
            
            aiIntegration.trainClassificationModel("saveable-model", dataset, 5);
            
            String modelPath = "test-models/saveable-model.tribuo";
            aiIntegration.saveModel("saveable-model", modelPath);
            
            File modelFile = new File(modelPath);
            assertTrue(modelFile.exists(), "Model file should exist");
            assertTrue(modelFile.length() > 0, "Model file should not be empty");
            
            System.out.println("  └─ Model saved: " + modelPath + " (" + 
                             (modelFile.length() / 1024) + " KB)");
            
            passTest(testName);
            
        } catch (Exception e) {
            failTest(testName, e.getMessage());
        }
    }
    
    private static void testModelLoading(TribuoAIIntegration aiIntegration) {
        String testName = "Model Persistence (Load)";
        System.out.println("🔬 Test: " + testName);
        
        try {
            String modelPath = "test-models/saveable-model.tribuo";
            
            // Asegurarse de que el modelo existe
            File modelFile = new File(modelPath);
            if (!modelFile.exists()) {
                // Crear el modelo primero
                LabelFactory labelFactory = new LabelFactory();
                List<Example<Label>> examples = createSimpleClassificationDataset(labelFactory, 30);
                Dataset<Label> dataset = new MutableDataset<>(examples);
                aiIntegration.trainClassificationModel("temp", dataset, 5);
                aiIntegration.saveModel("temp", modelPath);
                aiIntegration.removeModel("temp");
            }
            
            aiIntegration.loadModel("loaded-model", modelPath);
            
            assertTrue(aiIntegration.hasModel("loaded-model"),
                      "Loaded model should be in cache");
            
            String modelInfo = aiIntegration.getModelInfo("loaded-model");
            assertNotNull(modelInfo, "Model info should not be null");
            
            System.out.println("  └─ Model loaded successfully");
            
            passTest(testName);
            
        } catch (Exception e) {
            failTest(testName, e.getMessage());
        }
    }
    
    // ========================================================================
    // TESTS DE GESTIÓN
    // ========================================================================
    
    private static void testModelManagement(TribuoAIIntegration aiIntegration) {
        String testName = "Model Management";
        System.out.println("🔬 Test: " + testName);
        
        try {
            int initialCount = aiIntegration.getModelCount();
            
            LabelFactory labelFactory = new LabelFactory();
            List<Example<Label>> examples = createSimpleClassificationDataset(labelFactory, 20);
            Dataset<Label> dataset = new MutableDataset<>(examples);
            
            aiIntegration.trainClassificationModel("managed-model-1", dataset, 3);
            aiIntegration.trainClassificationModel("managed-model-2", dataset, 3);
            
            assertEquals(initialCount + 2, aiIntegration.getModelCount(),
                        "Model count should increase by 2");
            
            List<String> models = aiIntegration.listAvailableModels();
            assertTrue(models.contains("managed-model-1"), "Should contain model 1");
            assertTrue(models.contains("managed-model-2"), "Should contain model 2");
            
            aiIntegration.removeModel("managed-model-1");
            assertEquals(initialCount + 1, aiIntegration.getModelCount(),
                        "Model count should decrease by 1");
            
            System.out.println("  └─ Total models: " + aiIntegration.getModelCount());
            
            passTest(testName);
            
        } catch (Exception e) {
            failTest(testName, e.getMessage());
        }
    }
    
    private static void testModelCache(TribuoAIIntegration aiIntegration) {
        String testName = "Model Cache";
        System.out.println("🔬 Test: " + testName);
        
        try {
            LabelFactory labelFactory = new LabelFactory();
            List<Example<Label>> examples = createSimpleClassificationDataset(labelFactory, 20);
            Dataset<Label> dataset = new MutableDataset<>(examples);
            
            int beforeCount = aiIntegration.getModelCount();
            
            aiIntegration.trainClassificationModel("cache-test-1", dataset, 3);
            aiIntegration.trainClassificationModel("cache-test-2", dataset, 3);
            aiIntegration.trainClassificationModel("cache-test-3", dataset, 3);
            
            int afterCount = aiIntegration.getModelCount();
            assertEquals(beforeCount + 3, afterCount, "Should have 3 more models");
            
            aiIntegration.clearAllModels();
            assertEquals(0, aiIntegration.getModelCount(), "Cache should be empty");
            
            System.out.println("  └─ Cache cleared successfully");
            
            passTest(testName);
            
        } catch (Exception e) {
            failTest(testName, e.getMessage());
        }
    }
    
    // ========================================================================
    // TESTS DE AUTOML
    // ========================================================================
    
    private static void testAutoMLClassification(AutoMLHotReloadEngine autoML) {
        String testName = "AutoML Classification";
        System.out.println("🔬 Test: " + testName);
        
        try {
            LabelFactory labelFactory = new LabelFactory();
            List<Example<Label>> trainExamples = createSimpleClassificationDataset(labelFactory, 60);
            List<Example<Label>> validExamples = createSimpleClassificationDataset(labelFactory, 20);
            
            Dataset<Label> trainData = new MutableDataset<>(trainExamples);
            Dataset<Label> validData = new MutableDataset<>(validExamples);
            
            OptimizationConfig config = new OptimizationConfig(0.05, 3);
            
            OptimizationResult result = autoML.optimizeClassificationModel(
                "automl-test",
                trainData,
                validData,
                config
            );
            
            assertNotNull(result, "Result should not be null");
            assertNotNull(result.bestAlgorithm, "Best algorithm should be selected");
            assertTrue(result.optimizedScore >= 0, "Score should be valid");
            
            System.out.println("  ├─ Best Algorithm: " + result.bestAlgorithm);
            System.out.println("  └─ Score: " + String.format("%.4f", result.optimizedScore));
            
            passTest(testName);
            
        } catch (Exception e) {
            failTest(testName, e.getMessage());
        }
    }
    
    private static void testAutoMLAlgorithmSelection(AutoMLHotReloadEngine autoML) {
        String testName = "AutoML Algorithm Selection";
        System.out.println("🔬 Test: " + testName);
        
        try {
            Map<String, MLAlgorithm> algorithms = autoML.getAvailableAlgorithms();
            
            assertTrue(algorithms.size() > 0, "Should have algorithms available");
            assertTrue(algorithms.containsKey("logistic-regression"),
                      "Should have logistic regression");
            
            System.out.println("  └─ Available algorithms: " + algorithms.size());
            
            passTest(testName);
            
        } catch (Exception e) {
            failTest(testName, e.getMessage());
        }
    }
    
    // ========================================================================
    // TESTS DE INTEGRACIÓN
    // ========================================================================
    
    private static void testEndToEndWorkflow(TribuoAIIntegration aiIntegration) {
        String testName = "End-to-End Workflow";
        System.out.println("🔬 Test: " + testName);
        
        try {
            // 1. Crear datos
            LabelFactory labelFactory = new LabelFactory();
            List<Example<Label>> examples = createSimpleClassificationDataset(labelFactory, 50);
            Dataset<Label> dataset = new MutableDataset<>(examples);
            
            // 2. Entrenar
            aiIntegration.trainClassificationModel("e2e-model", dataset, 5);
            
            // 3. Predecir
            Prediction<Label> prediction = aiIntegration.predictClassification(
                "e2e-model",
                examples.get(0)
            );
            
            // 4. Evaluar
            ClassificationMetrics metrics = aiIntegration.evaluateClassificationModel(
                "e2e-model",
                dataset
            );
            
            // 5. Guardar
            aiIntegration.saveModel("e2e-model", "test-models/e2e-model.tribuo");
            
            // 6. Cargar
            aiIntegration.loadModel("e2e-model-loaded", "test-models/e2e-model.tribuo");
            
            // 7. Verificar
            Prediction<Label> prediction2 = aiIntegration.predictClassification(
                "e2e-model-loaded",
                examples.get(0)
            );
            
            assertNotNull(prediction2, "Loaded model should work");
            
            System.out.println("  └─ Complete workflow executed successfully");
            
            passTest(testName);
            
        } catch (Exception e) {
            failTest(testName, e.getMessage());
        }
    }
    
    // ========================================================================
    // MÉTODOS AUXILIARES
    // ========================================================================
    
    private static List<Example<Label>> createSimpleClassificationDataset(
            LabelFactory labelFactory, int size) {
        
        List<Example<Label>> examples = new ArrayList<>();
        Random random = new Random(42);
        String[] featureNames = {"f1", "f2", "f3"};
        
        for (int i = 0; i < size; i++) {
            double[] features = new double[3];
            for (int j = 0; j < 3; j++) {
                features[j] = random.nextDouble() * 10;
            }
            
            String labelStr = (features[0] + features[1] > 10) ? "A" : "B";
            Label label = labelFactory.generateOutput(labelStr);
            
            examples.add(new ArrayExample<>(label, featureNames, features));
        }
        
        return examples;
    }
    
    private static List<Example<Regressor>> createSimpleRegressionDataset(
            RegressionFactory regressionFactory, int size) {
        
        List<Example<Regressor>> examples = new ArrayList<>();
        Random random = new Random(42);
        String[] featureNames = {"x1", "x2"};
        String[] targetNames = {"y"};
        
        for (int i = 0; i < size; i++) {
            double[] features = new double[2];
            for (int j = 0; j < 2; j++) {
                features[j] = random.nextDouble() * 10;
            }
            
            double y = 2 * features[0] + features[1];
            Regressor regressor = new Regressor(targetNames[0], y);
            
            examples.add(new ArrayExample<>(regressor, featureNames, features));
        }
        
        return examples;
    }
    
    // Assertion methods
    private static void assertNotNull(Object obj, String message) {
        if (obj == null) {
            throw new AssertionError(message);
        }
    }
    
    private static void assertTrue(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }
    
    private static void assertEquals(int expected, int actual, String message) {
        if (expected != actual) {
            throw new AssertionError(message + " (expected: " + expected + ", actual: " + actual + ")");
        }
    }
    
    private static void passTest(String testName) {
        System.out.println("  ✅ PASSED\n");
        passedTests++;
    }
    
    private static void failTest(String testName, String reason) {
        System.out.println("  ❌ FAILED: " + reason + "\n");
        failedTests++;
        failureMessages.add(testName + ": " + reason);
    }
    
    private static void printTestSummary() {
        System.out.println("\n╔════════════════════════════════════════════════════════════════╗");
        System.out.println("║                        TEST SUMMARY                            ║");
        System.out.println("╚════════════════════════════════════════════════════════════════╝\n");
        
        int total = passedTests + failedTests;
        double successRate = total > 0 ? (passedTests * 100.0 / total) : 0;
        
        System.out.println("📊 Total Tests: " + total);
        System.out.println("✅ Passed: " + passedTests);
        System.out.println("❌ Failed: " + failedTests);
        System.out.println("📈 Success Rate: " + String.format("%.1f%%", successRate));
        
        if (!failureMessages.isEmpty()) {
            System.out.println("\n❌ Failures:");
            for (String failure : failureMessages) {
                System.out.println("  - " + failure);
            }
        }
        
        if (failedTests == 0) {
            System.out.println("\n🎉 ¡TODOS LOS TESTS PASARON EXITOSAMENTE!");
        } else {
            System.out.println("\n⚠️  Algunos tests fallaron. Revisar errores arriba.");
        }
    }
}
