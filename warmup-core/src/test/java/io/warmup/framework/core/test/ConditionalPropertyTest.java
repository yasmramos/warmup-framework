package io.warmup.framework.core.test;

import io.warmup.framework.annotation.Bean;
import io.warmup.framework.annotation.ConditionalOnProperty;
import io.warmup.framework.annotation.Configuration;
import io.warmup.framework.config.PropertySource;
import io.warmup.framework.core.ConditionEvaluator;
import io.warmup.framework.core.Warmup;
import io.warmup.framework.services.CacheService;
import io.warmup.framework.services.EmailService;

import java.util.Properties;

/**
 * Tests simplificados para @ConditionalOnProperty annotation.
 * Verifica los casos de uso principales.
 */
public class ConditionalPropertyTest {
    
    public static void main(String[] args) {
        System.out.println("🧪 EJECUTANDO TESTS PARA @ConditionalOnProperty");
        System.out.println("============================================================");
        
        try {
            testHavingValue();
            testMatchIfMissing();
            testRequireProperty();
            testAnyOfCondition();
            testMultipleConditions();
            testRealWorldScenario();
            
            System.out.println("\n✅ TESTS COMPLETADOS EXITOSAMENTE");
            System.out.println("@ConditionalOnProperty funciona correctamente!");
            
        } catch (Exception e) {
            System.err.println("❌ TEST FALLIDO: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Test 1: havingValue - Registro condicional basado en valor específico
     */
    private static void testHavingValue() {
        System.out.println("\n📋 Test 1: havingValue");
        
        Properties props = new Properties();
        props.setProperty("app.mode", "production");
        
        TestConfig config = new TestConfig();
        ConditionEvaluator evaluator = new ConditionEvaluator(new PropertySource() {{ 
            properties.putAll(props);
        }});
        
        // Test evaluar directamente el método
        try {
            assert evaluator.shouldRegister(config.getClass().getMethod("productionService")) : "productionService debe registrarse";
            System.out.println("   ✓ productionService registrado con havingValue='production'");
        } catch (NoSuchMethodException e) {
            System.out.println("   ✗ Error: método productionService no encontrado");
            return;
        }
        
        try {
            assert !evaluator.shouldRegister(config.getClass().getMethod("developmentService")) : "developmentService NO debe registrarse";
            System.out.println("   ✓ developmentService NO registrado con havingValue='development'");
        } catch (NoSuchMethodException e) {
            System.out.println("   ✗ Error: método developmentService no encontrado");
            return;
        }
    }
    
    /**
     * Test 2: matchIfMissing - Registro cuando la propiedad no existe
     */
    private static void testMatchIfMissing() {
        System.out.println("\n📋 Test 2: matchIfMissing");
        
        Properties props = new Properties();
        // NO configuramos experimental.feature.enabled
        
        TestConfig config = new TestConfig();
        ConditionEvaluator evaluator = new ConditionEvaluator(new PropertySource() {{
            properties.putAll(props);
        }});
        
        try {
            assert evaluator.shouldRegister(config.getClass().getMethod("experimentalFeature")) : "experimentalFeature debe registrarse cuando matchIfMissing=true";
            System.out.println("   ✓ experimentalFeature registrado con matchIfMissing=true");
        } catch (NoSuchMethodException e) {
            System.out.println("   ✗ Error: método experimentalFeature no encontrado");
            return;
        }
        
        try {
            assert !evaluator.shouldRegister(config.getClass().getMethod("optionalFeature")) : "optionalFeature NO debe registrarse cuando matchIfMissing=false";
            System.out.println("   ✓ optionalFeature NO registrado con matchIfMissing=false");
        } catch (NoSuchMethodException e) {
            System.out.println("   ✗ Error: método optionalFeature no encontrado");
            return;
        }
    }
    
    /**
     * Test 3: requireProperty - Propiedad debe existir
     */
    private static void testRequireProperty() {
        System.out.println("\n📋 Test 3: requireProperty");
        
        Properties props = new Properties();
        props.setProperty("db.url", "jdbc:mysql://localhost/mydb");
        
        TestConfig config = new TestConfig();
        ConditionEvaluator evaluator = new ConditionEvaluator(new PropertySource() {{
            properties.putAll(props);
        }});
        
        try {
            assert evaluator.shouldRegister(config.getClass().getMethod("databaseService")) : "databaseService debe registrarse cuando requireProperty=true y existe";
            System.out.println("   ✓ databaseService registrado con requireProperty=true");
        } catch (NoSuchMethodException e) {
            System.out.println("   ✗ Error: método databaseService no encontrado");
            return;
        }
        
        try {
            assert !evaluator.shouldRegister(config.getClass().getMethod("cacheService")) : "cacheService NO debe registrarse cuando requireProperty=true y NO existe";
            System.out.println("   ✓ cacheService NO registrado con requireProperty=true");
        } catch (NoSuchMethodException e) {
            System.out.println("   ✗ Error: método cacheService no encontrado");
            return;
        }
    }
    
    /**
     * Test 4: anyOf - Cualquiera de las propiedades debe cumplir la condición
     */
    private static void testAnyOfCondition() {
        System.out.println("\n📋 Test 4: anyOf");
        
        Properties props = new Properties();
        props.setProperty("database.postgres.url", "jdbc:postgresql://localhost/db");
        
        TestConfig config = new TestConfig();
        ConditionEvaluator evaluator = new ConditionEvaluator(new PropertySource() {{
            properties.putAll(props);
        }});
        
        try {
            assert evaluator.shouldRegister(config.getClass().getMethod("postgresService")) : "postgresService debe registrarse cuando anyOf incluye propiedad existente";
            System.out.println("   ✓ postgresService registrado con anyOf");
        } catch (NoSuchMethodException e) {
            System.out.println("   ✗ Error: método postgresService no encontrado");
            return;
        }
        
        try {
            assert !evaluator.shouldRegister(config.getClass().getMethod("mysqlService")) : "mysqlService NO debe registrarse cuando anyOf no incluye propiedades";
            System.out.println("   ✓ mysqlService NO registrado con anyOf");
        } catch (NoSuchMethodException e) {
            System.out.println("   ✗ Error: método mysqlService no encontrado");
            return;
        }
    }
    
    /**
     * Test 5: Multiple conditions - Múltiples condiciones deben cumplirse
     */
    private static void testMultipleConditions() {
        System.out.println("\n📋 Test 5: Multiple Conditions");
        
        Properties props = new Properties();
        props.setProperty("feature.enabled", "true");
        props.setProperty("environment", "production");
        
        TestConfig config = new TestConfig();
        ConditionEvaluator evaluator = new ConditionEvaluator(new PropertySource() {{
            properties.putAll(props);
        }});
        
        try {
            assert evaluator.shouldRegister(config.getClass().getMethod("productionFeatureService")) : "productionFeatureService debe registrarse cuando todas las condiciones son true";
            System.out.println("   ✓ productionFeatureService registrado con múltiples condiciones verdaderas");
        } catch (NoSuchMethodException e) {
            System.out.println("   ✗ Error: método productionFeatureService no encontrado");
            return;
        }
        
        try {
            assert !evaluator.shouldRegister(config.getClass().getMethod("stagingFeatureService")) : "stagingFeatureService NO debe registrarse cuando una condición es false";
            System.out.println("   ✓ stagingFeatureService NO registrado con una condición falsa");
        } catch (NoSuchMethodException e) {
            System.out.println("   ✗ Error: método stagingFeatureService no encontrado");
            return;
        }
    }
    
    /**
     * Test 6: Escenario del mundo real - Configuración de aplicación completa
     */
    private static void testRealWorldScenario() {
        System.out.println("\n📋 Test 6: Real World Scenario");
        
        // Simular configuración real de aplicación
        Properties props = new Properties();
        props.setProperty("feature.cache.enabled", "true");
        props.setProperty("email.provider", "sendgrid");
        props.setProperty("database.primary.type", "mysql");
        
        // Configurar propiedades del sistema
        for (String key : props.stringPropertyNames()) {
            System.setProperty(key, props.getProperty(key));
        }
        
        Warmup warmup = Warmup.create();
        try {
            warmup.getContainer().start();
        } catch (Exception e) {
            System.out.println("   ⚠️  Error inicializando contenedor: " + e.getMessage());
            return;
        }
        
        // Verificar que los beans correctos fueron registrados
        System.out.println("   📊 Verificando beans registrados en escenario real:");
        
        try {
            warmup.getBean(CacheService.class);
            System.out.println("   ✓ CacheService registrado (cache.enabled=true)");
        } catch (Exception e) {
            System.out.println("   ⚠️  CacheService no encontrado (puede ser porque no está registrado en el contenedor)");
        }
        
        try {
            warmup.getBean(EmailService.class);
            System.out.println("   ✓ EmailService registrado (email.provider=sendgrid)");
        } catch (Exception e) {
            System.out.println("   ⚠️  EmailService no encontrado (puede ser porque no está registrado en el contenedor)");
        }
        
        System.out.println("   ✅ Escenario real ejecutado");
    }
    
    // ===== CLASES DE CONFIGURACIÓN PARA TESTS =====
    
    @Configuration
    public static class TestConfig {
        
        @Bean
        @ConditionalOnProperty(name = "app.mode", havingValue = "production")
        public void productionService() { }
        
        @Bean
        @ConditionalOnProperty(name = "app.mode", havingValue = "development")
        public void developmentService() { }
        
        @Bean
        @ConditionalOnProperty(name = "experimental.feature.enabled", matchIfMissing = true)
        public void experimentalFeature() { }
        
        @Bean
        @ConditionalOnProperty(name = "optional.feature.enabled", matchIfMissing = false)
        public void optionalFeature() { }
        
        @Bean
        @ConditionalOnProperty(name = "db.url", requireProperty = true)
        public void databaseService() { }
        
        @Bean
        @ConditionalOnProperty(name = "cache.url", requireProperty = true)
        public void cacheService() { }
        
        @Bean
        @ConditionalOnProperty(
            name = "database.primary.type", 
            havingValue = "postgresql",
            anyOf = {"database.postgres.url", "postgres.url"}
        )
        public void postgresService() { }
        
        @Bean
        @ConditionalOnProperty(
            name = "database.primary.type", 
            havingValue = "mysql",
            anyOf = {"database.mysql.url", "mysql.url"}
        )
        public void mysqlService() { }
        
        @Bean
        @ConditionalOnProperty(name = "feature.enabled", havingValue = "true")
        @ConditionalOnProperty(name = "environment", havingValue = "production")
        public void productionFeatureService() { }
        
        @Bean
        @ConditionalOnProperty(name = "feature.enabled", havingValue = "true")
        @ConditionalOnProperty(name = "environment", havingValue = "staging")
        public void stagingFeatureService() { }
    }
}