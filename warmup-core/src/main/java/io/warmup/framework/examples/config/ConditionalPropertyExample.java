package io.warmup.framework.examples.config;

import io.warmup.framework.core.Warmup;
import io.warmup.framework.examples.services.*;

import java.util.Scanner;

/**
 * Ejemplo práctico que demuestra el uso de @ConditionalOnProperty
 * para configurar diferentes servicios según las propiedades del sistema.
 * 
 * Este ejemplo muestra cómo los beans se registran condicionalmente basado en:
 * - feature.cache.enabled: true/false para cache service
 * - email.provider: smtp/sendgrid/ses para email service
 * - database.primary.type: postgresql/mysql/h2 para database service
 * - monitoring.advanced.enabled: true/false para monitoring service
 * - Y más...
 * 
 * USO:
 * 1. Configura las propiedades del sistema antes de ejecutar
 * 2. Ejecuta el ejemplo para ver qué beans se registran
 * 3. Cambia las propiedades y vuelve a ejecutar para ver la diferencia
 */
public class ConditionalPropertyExample {
    
    public static void main(String[] args) {
        try {
            System.out.println("=== DEMO: @ConditionalOnProperty ===");
            System.out.println("Este ejemplo demuestra cómo @ConditionalOnProperty controla");
            System.out.println("qué beans se registran basado en propiedades del sistema.\n");
            
            // Crear warmup instance usando API pública
            Warmup warmup = Warmup.create();
            
            // Configurar propiedades del sistema
            configureSystemProperties();
            
            // Mostrar propiedades configuradas
            showConfiguredProperties();
            
            // Mostrar qué beans fueron registrados
            showRegisteredBeans(warmup);
            
            // Demostrar el uso de los beans registrados
            demonstrateBeanUsage(warmup);
            
            // Esperar input del usuario para continuar
            promptForNextDemo(warmup);
            
        } catch (Exception e) {
            System.err.println("❌ Error ejecutando ejemplo: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Configurar propiedades del sistema que controlan la registro condicional.
     */
    private static void configureSystemProperties() {
        System.out.println("📋 Configurando propiedades del sistema...");
        
        // Configuración principal - cache habilitado
        System.setProperty("feature.cache.enabled", "true");
        
        // Configuración de email - usando SendGrid
        System.setProperty("email.provider", "sendgrid");
        
        // Configuración de base de datos - usando MySQL
        System.setProperty("database.primary.type", "mysql");
        
        // Configuración de monitoreo - avanzado habilitado
        System.setProperty("monitoring.advanced.enabled", "true");
        
        // Configuración de notificaciones
        System.setProperty("notifications.enabled", "true");
        System.setProperty("webhook.url", "https://hooks.slack.com/...");
        
        // Configuración de seguridad - modo estricto DEShabilitado (usando invert)
        System.setProperty("security.strict.mode", "true");
        
        // PROPIEDADES NO CONFIGURADAS (se usará matchIfMissing o default):
        // - feature.experimental.enabled (matchIfMissing = true)
        // - database.legacy.mode (notHavingValue)
    }
    
    /**
     * Mostrar todas las propiedades configuradas.
     */
    private static void showConfiguredProperties() {
        System.out.println("\n📊 Propiedades configuradas:");
        String[] keys = {
            "feature.cache.enabled",
            "email.provider", 
            "database.primary.type",
            "monitoring.advanced.enabled",
            "notifications.enabled",
            "webhook.url",
            "security.strict.mode"
        };
        
        for (String key : keys) {
            String value = System.getProperty(key, "NO_CONFIGURADO");
            System.out.println("   " + key + " = " + value);
        }
        
        System.out.println("\nPropiedades que NO están configuradas (se evaluarán con matchIfMissing):");
        System.out.println("   feature.experimental.enabled");
        System.out.println("   database.legacy.mode");
    }
    
    /**
     * Mostrar qué beans fueron registrados exitosamente.
     */
    private static void showRegisteredBeans(Warmup warmup) {
        System.out.println("\n✅ Beans registrados condicionalmente:");
        
        // Cache Service (cache.enabled = true)
        tryShowBean(warmup, CacheService.class, "Cache Service");
        
        // Email Service (email.provider = sendgrid)
        tryShowBean(warmup, EmailService.class, "Email Service");
        
        // Database Service (database.primary.type = mysql)
        tryShowBean(warmup, DatabaseService.class, "Database Service");
        
        // Monitoring Service (monitoring.advanced.enabled = true)
        tryShowBean(warmup, MonitoringService.class, "Monitoring Service");
        
        // Notification Service
        tryShowBean(warmup, NotificationService.class, "Notification Service");
        
        // Security Service (security.strict.mode = true, invert = true)
        tryShowBean(warmup, RelaxedSecurityService.class, "Relaxed Security Service");
        
        // Experimental Service (feature.experimental.enabled no existe, matchIfMissing = true)
        tryShowBean(warmup, ExperimentalService.class, "Experimental Service");
        
        // Legacy Migration Service (database.legacy.mode no existe, notHavingValue)
        tryShowBean(warmup, LegacyMigrationService.class, "Legacy Migration Service");
    }
    
    /**
     * Intentar mostrar un bean si está registrado.
     */
    private static <T> void tryShowBean(Warmup warmup, Class<T> type, String description) {
        try {
            T bean = warmup.getBean(type);
            System.out.println("   ✓ " + description + " (" + type.getSimpleName() + ")");
        } catch (Exception e) {
            System.out.println("   ✗ " + description + " - No registrado (condición no cumplida)");
        }
    }
    
    /**
     * Demostrar el uso de los beans registrados.
     */
    private static void demonstrateBeanUsage(Warmup warmup) {
        System.out.println("\n🎯 Demostrando funcionalidad de beans registrados:");
        
        // Usar Cache Service
        try {
            CacheService cache = warmup.getBean(CacheService.class);
            cache.put("test-key", "test-value");
            cache.get("test-key");
            System.out.println("   Cache Info: " + cache.getInfo());
        } catch (Exception e) {
            System.out.println("   Cache Service no disponible");
        }
        
        // Usar Email Service
        try {
            EmailService email = warmup.getBean(EmailService.class);
            email.sendEmail("user@example.com", "Test Subject", "Test Body");
            System.out.println("   Email Provider: " + email.getProviderInfo());
        } catch (Exception e) {
            System.out.println("   Email Service no disponible");
        }
        
        // Usar Database Service
        try {
            DatabaseService db = warmup.getBean(DatabaseService.class);
            db.connect();
            System.out.println("   DB Info: " + db.getDatabaseInfo());
            db.disconnect();
        } catch (Exception e) {
            System.out.println("   Database Service no disponible");
        }
    }
    
    /**
     * Prompt para demostrar diferentes configuraciones.
     */
    private static void promptForNextDemo(Warmup warmup) {
        System.out.println("\n============================================================");
        System.out.println("🎛️  DEMOSTRACIÓN INTERACTIVA");
        System.out.println("Vamos a cambiar algunas propiedades y ver cómo esto afecta");
        System.out.println("qué beans se registran en el contenedor.");
        System.out.println("============================================================");
        
        Scanner scanner = new Scanner(System.in);
        
        try {
            System.out.println("\nPresiona ENTER para continuar con la demo interactiva...");
            scanner.nextLine();
            
            // Demo 1: Deshabilitar cache
            System.out.println("\n📝 Demo 1: Deshabilitando cache (feature.cache.enabled = false)");
            System.setProperty("feature.cache.enabled", "false");
            
            warmup = Warmup.create();
            
            tryShowBean(warmup, CacheService.class, "Cache Service");
            
            System.out.println("\nPresiona ENTER para la siguiente demo...");
            scanner.nextLine();
            
            // Demo 2: Cambiar email provider
            System.out.println("\n📝 Demo 2: Cambiando email provider a SMTP");
            System.setProperty("email.provider", "smtp");
            
            warmup = Warmup.create();
            
            tryShowBean(warmup, EmailService.class, "Email Service");
            
            System.out.println("\nPresiona ENTER para la demo final...");
            scanner.nextLine();
            
            // Demo 3: Habilitar modo estricto de seguridad
            System.out.println("\n📝 Demo 3: Deshabilitando modo relajado de seguridad");
            System.setProperty("security.strict.mode", "false");
            
            warmup = Warmup.create();
            
            tryShowBean(warmup, RelaxedSecurityService.class, "Relaxed Security Service");
            
            System.out.println("\n✅ ¡Demo completa! @ConditionalOnProperty funciona correctamente.");
            
        } finally {
            scanner.close();
        }
    }
}