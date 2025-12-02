package io.warmup.examples.config;

import io.warmup.framework.annotation.Bean;
import io.warmup.framework.annotation.ConditionalOnProperty;
import io.warmup.framework.annotation.Configuration;
import io.warmup.framework.annotation.Singleton;
import io.warmup.framework.services.CacheService;
import io.warmup.framework.services.SimpleCacheService;
import io.warmup.framework.services.EmailService;
import io.warmup.examples.config.SimpleEmailService;
// import io.warmup.framework.services.SimpleEmailService;
import io.warmup.examples.DatabaseService;
import io.warmup.examples.config.PostgreSqlDatabaseService;
import io.warmup.examples.config.MySqlDatabaseService;
import io.warmup.examples.config.H2DatabaseService;
import io.warmup.examples.config.LegacyMigrationService;
import io.warmup.examples.config.NotificationServiceImpl;
import io.warmup.examples.config.RelaxedSecurityService;

/**
 * Ejemplo de configuración que demuestra el uso de @ConditionalOnProperty
 * para registro condicional de beans basado en propiedades de configuración.
 */
@Configuration
public class ConditionalPropertyConfiguration {

    /**
     * Servicio de cache - solo se registra si la propiedad feature.cache.enabled = true
     */
    @Bean
    @Singleton
    @ConditionalOnProperty(name = "feature.cache.enabled", havingValue = "true")
    public CacheService cacheService() {
        System.out.println("🔧 Creando CacheService (condition met: cache enabled)");
        return new SimpleCacheService();
    }

    /**
     * Alternativa de servicio de cache - se registra si cache está deshabilitado
     */
    @Bean
    @Singleton
    @ConditionalOnProperty(name = "feature.cache.enabled", havingValue = "false")
    public CacheService simpleCacheService() {
        System.out.println("🔧 Creando SimpleCacheService (condition met: cache disabled)");
        return new SimpleCacheService();
    }

    /**
     * Servicio de email - se registra según el tipo de proveedor configurado
     */
    @Bean
    @Singleton
    @ConditionalOnProperty(name = "email.provider", havingValue = "smtp")
    public EmailService smtpEmailService() {
        System.out.println("🔧 Creating SmtpEmailService (condition met: smtp provider)");
        return new SimpleEmailService("SMTP");
    }

    @Bean
    @Singleton
    @ConditionalOnProperty(name = "email.provider", havingValue = "sendgrid")
    public EmailService sendgridEmailService() {
        System.out.println("🔧 Creating SendgridEmailService (condition met: sendgrid provider)");
        return new SimpleEmailService("SendGrid");
    }

    @Bean
    @Singleton
    @ConditionalOnProperty(name = "email.provider", havingValue = "ses")
    public EmailService sesEmailService() {
        System.out.println("🔧 Creating SesEmailService (condition met: ses provider)");
        return new SimpleEmailService("SES");
    }

    /*
     * Monitoring services - commented out temporarily until SimpleMonitoringService is fully implemented
     */
    /*
    @Bean
    @Singleton
    @ConditionalOnProperty(name = "monitoring.advanced.enabled", havingValue = "true", requireProperty = true)
    public MonitoringService advancedMonitoringService() {
        System.out.println("🔧 Creando AdvancedMonitoringService (condition met: advanced monitoring enabled)");
        return new AdvancedMonitoringService();
    }

    @Bean
    @Singleton
    @ConditionalOnProperty(name = "monitoring.advanced.enabled", havingValue = "false")
    public MonitoringService basicMonitoringService() {
        System.out.println("🔧 Creando BasicMonitoringService (condition met: basic monitoring)");
        return new BasicMonitoringService();
    }
    */

    /*
     * Experimental service - commented out temporarily
     */
    /*
    @Bean
    @Singleton
    @ConditionalOnProperty(name = "feature.experimental.enabled", matchIfMissing = true)
    public ExperimentalService experimentalService() {
        System.out.println("🔧 Creando ExperimentalService (condition met: experimental feature enabled or missing)");
        return new ExperimentalService();
    }
    */

    /**
     * Configuración de base de datos múltiple usando anyOf
     */
    @Bean
    @Singleton
    @ConditionalOnProperty(
        name = "database.primary.type", 
        havingValue = "postgresql",
        anyOf = {"database.postgres.url", "postgres.url"}
    )
    public DatabaseService postgresqlDatabaseService() {
        System.out.println("🔧 Creando PostgreSqlDatabaseService (condition met: postgresql configured)");
        return new PostgreSqlDatabaseService();
    }

    @Bean
    @Singleton
    @ConditionalOnProperty(
        name = "database.primary.type", 
        havingValue = "mysql",
        anyOf = {"database.mysql.url", "mysql.url"}
    )
    public DatabaseService mysqlDatabaseService() {
        System.out.println("🔧 Creando MySqlDatabaseService (condition met: mysql configured)");
        return new MySqlDatabaseService();
    }

    @Bean
    @Singleton
    @ConditionalOnProperty(
        name = "database.primary.type", 
        havingValue = "h2"
    )
    public DatabaseService h2DatabaseService() {
        System.out.println("🔧 Creando H2DatabaseService (condition met: h2 configured)");
        return new H2DatabaseService();
    }

    /**
     * Uso de notHavingValue para evitar conflictos
     */
    @Bean
    @Singleton
    @ConditionalOnProperty(
        name = "feature.legacy.enabled", 
        notHavingValue = {"database.legacy.mode", "legacy.mode"}
    )
    public LegacyMigrationService legacyMigrationService() {
        System.out.println("🔧 Creando LegacyMigrationService (condition met: no legacy mode)");
        return new LegacyMigrationService();
    }

    /**
     * Servicio de notificaciones - se registra si cualquier propiedad de notificación está presente
     */
    @Bean
    @Singleton
    @ConditionalOnProperty(
        name = "notifications.enabled",
        havingValue = "true",
        anyOf = {"webhook.url", "slack.token", "discord.webhook"}
    )
    public NotificationServiceImpl notificationService() {
        System.out.println("🔧 Creando NotificationServiceImpl (condition met: notifications enabled with channels)");
        return new NotificationServiceImpl();
    }

    /**
     * Uso de invert para lógica negada
     */
    @Bean
    @Singleton
    @ConditionalOnProperty(
        name = "security.strict.mode", 
        havingValue = "true", 
        invert = true
    )
    public RelaxedSecurityService relaxedSecurityService() {
        System.out.println("🔧 Creando RelaxedSecurityService (condition met: strict mode disabled)");
        return new RelaxedSecurityService();
    }
}