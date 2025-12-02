package io.warmup.examples;

import io.warmup.framework.core.Warmup;
import io.warmup.framework.core.WarmupContainer;
import io.warmup.framework.annotation.Component;
import io.warmup.framework.annotation.Inject;
import io.warmup.framework.annotation.EventListener;
import io.warmup.framework.annotation.Profile;
import io.warmup.framework.annotation.Async;
import io.warmup.framework.core.EventManager;
import io.warmup.framework.event.Event;
import io.warmup.framework.metrics.ContainerMetrics;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

/**
 * 🎯 EJEMPLOS DE USO - WARMUP FRAMEWORK API PRINCIPAL
 * 
 * Este archivo demuestra todos los patrones de uso comunes de Warmup Framework
 * usando la API principal de la clase Warmup.java
 */
public class WarmupExamples {
    
    // === EJEMPLO 1: APLICACIÓN BÁSICA ===
    
    /**
     * Aplicación simple con un servicio y un repositorio
     */
    public static void basicApplication() throws Exception {
        System.out.println("\n=== EJEMPLO 1: APLICACIÓN BÁSICA ===");
        
        // Punto de entrada simple
        WarmupContainer container = Warmup.create()
                                        .scanPackages("io.warmup.examples")
                                        .start();
        
        // Obtener bean y usar
        MyService service = container.getBean(MyService.class);
        service.doSomething();
        
        System.out.println("Bean count: " + container.getActiveInstancesCount());
        
        // Cleanup
        container.shutdown();
    }
    
    // === EJEMPLO 2: CON ARGUMENTOS DE LÍNEA DE COMANDOS ===
    
    /**
     * Aplicación que procesa argumentos de línea de comandos
     */
    public static void commandLineApplication(String[] args) throws Exception {
        System.out.println("\n=== EJEMPLO 2: ARGUMENTOS DE LÍNEA DE COMANDOS ===");
        
        // Parse automático de argumentos
        WarmupContainer container = Warmup.run(args)
                                        .scanPackages("io.warmup.examples")
                                        .withProperty("app.name", "MyApp")
                                        .start();
        
        // Obtener configuraciones desde argumentos
        String profile = container.getProperty("spring.profiles.active");
        boolean debug = Boolean.parseBoolean(container.getProperty("debug", "false"));
        String appName = container.getProperty("app.name");
        
        System.out.println("App name: " + appName);
        System.out.println("Profile: " + profile);
        System.out.println("Debug mode: " + debug);
        
        container.shutdown();
    }
    
    // === EJEMPLO 3: CON PERFILES DE CONFIGURACIÓN ===
    
    /**
     * Aplicación con diferentes configuraciones por perfil
     */
    public static void profileBasedApplication(String profile) throws Exception {
        System.out.println("\n=== EJEMPLO 3: PERFILES DE CONFIGURACIÓN ===");
        
        WarmupContainer container = Warmup.create()
                                        .withProfile(profile)
                                        .scanPackages("io.warmup.examples")
                                        .start();
        
        // Los beans se activarán según el perfil
        ExampleDatabaseService dbService = container.getBean(ExampleDatabaseService.class);
        dbService.connect();
        
        container.shutdown();
    }
    
    // === EJEMPLO 4: INICIO RÁPIDO ===
    
    /**
     * Inicialización rápida para casos simples
     */
    public static void quickStartExample() throws Exception {
        System.out.println("\n=== EJEMPLO 4: INICIO RÁPIDO ===");
        
        // Una línea para aplicaciones simples
        WarmupContainer container = Warmup.quickStart();
        
        MyService service = container.getBean(MyService.class);
        service.doSomething();
        
        container.shutdown();
    }
    
    // === EJEMPLO 5: EVENTOS O(1) ===
    
    /**
     * Ejemplo de sistema de eventos O(1)
     */
    public static void eventExample() throws Exception {
        System.out.println("\n=== EJEMPLO 5: EVENTOS O(1) ===");
        
        WarmupContainer container = Warmup.create()
                                        .scanPackages("io.warmup.examples")
                                        .start();
        
        EventManager eventManager = (EventManager) container.getEventManager();
        
        // Los eventos se distribuyen a O(1) - constante independiente del número de listeners
        UserRegisteredEvent event = new UserRegisteredEvent(1L, "john@example.com");
        eventManager.dispatchEvent(event);
        
        container.shutdown();
    }
    
    // === EJEMPLO 6: MÉTODOS DE ACCESO RÁPIDO ===
    
    /**
     * Uso de métodos de acceso directo en Warmup
     */
    public static void quickAccessExample() throws Exception {
        System.out.println("\n=== EJEMPLO 6: ACCESO RÁPIDO ===");
        
        Warmup warmup = Warmup.create()
                             .withProperty("server.port", "8080")
                             .scanPackages("io.warmup.examples");
        
        WarmupContainer container = warmup.start();
        
        // Métodos de acceso directo sin pasar por contenedor
        MyService service = warmup.getBean(MyService.class);
        boolean hasRepo = warmup.hasBean(MyRepository.class);
        String port = warmup.getProperty("server.port");
        boolean isDev = warmup.isProfileActive("development");
        
        System.out.println("Has repository: " + hasRepo);
        System.out.println("Server port: " + port);
        System.out.println("Is development: " + isDev);
        
        service.doSomething();
        
        container.shutdown();
    }
    
    // === EJEMPLO 7: MÉTODOS DE LIFECYCLE ===
    
    /**
     * Control completo del lifecycle
     */
    public static void lifecycleExample() throws Exception {
        System.out.println("\n=== EJEMPLO 7: LIFECYCLE ===");
        
        Warmup warmup = Warmup.create()
                             .scanPackages("io.warmup.examples");
        
        WarmupContainer container = warmup.start();
        
        // Verificar estado
        System.out.println("Estado: " + warmup.getState());
        System.out.println("Beans: " + warmup.getBeanCount());
        System.out.println("Version: " + warmup.getVersion());
        System.out.println("Running: " + warmup.isRunning());
        
        // Obtener métricas
        ContainerMetrics metrics = warmup.getMetrics();
        System.out.println("Active beans: " + warmup.getBeanCount());
        
        container.shutdown();
        
        System.out.println("Stopped: " + warmup.isStopped());
    }
    
    // === EJEMPLO 8: REGISTRO MANUAL DE BEANS ===
    
    /**
     * Registro manual de beans en tiempo de ejecución
     */
    public static void manualBeanRegistration() throws Exception {
        System.out.println("\n=== EJEMPLO 8: REGISTRO MANUAL ===");
        
        Warmup warmup = Warmup.create();
        
        // Registro condicional basado en perfil
        warmup.registerBeanIfProfile(MyService.class, new MyService(), "development")
              .registerBean(MyConfig.class, new MyConfig())
              .scanPackages("io.warmup.examples");
        
        WarmupContainer container = warmup.start();
        
        // Beans registrados automáticamente
        MyService service = warmup.getBean(MyService.class);
        service.doSomething();
        
        container.shutdown();
    }
    
    // === EJEMPLO 9: MODOS PREDEFINIDOS ===
    
    /**
     * Usar modos predefinidos para diferentes entornos
     */
    public static void predefinedModes() throws Exception {
        System.out.println("\n=== EJEMPLO 9: MODOS PREDEFINIDOS ===");
        
        // Modo test - configuración optimizada para testing
        System.out.println("Iniciando modo test...");
        WarmupContainer testContainer = Warmup.testMode();
        testContainer.shutdown();
        
        // Modo desarrollo - con auto-scan y debug
        System.out.println("Iniciando modo desarrollo...");
        WarmupContainer devContainer = Warmup.devMode();
        devContainer.shutdown();
        
        // Modo producción - optimizado para rendimiento
        System.out.println("Iniciando modo producción...");
        WarmupContainer prodContainer = Warmup.prodMode();
        prodContainer.shutdown();
    }
    
    // === EJEMPLO 10: INICIO ASÍNCRONO ===
    
    /**
     * Inicialización asíncrona para mejor performance
     */
    public static void asyncStartup() {
        System.out.println("\n=== EJEMPLO 10: INICIO ASÍNCRONO ===");
        
        Warmup warmup = Warmup.create()
                             .scanPackages("io.warmup.examples");
        
        // Inicio asíncrono
        warmup.startAsync().thenAccept(container -> {
            System.out.println("Framework inicializado asíncronamente");
            
            MyService service = container.getBean(MyService.class);
            service.doSomething();
            
            // Cleanup cuando termine
            warmup.stop();
        });
    }
    
    // === EJEMPLO 11: CONFIGURACIÓN AVANZADA ===
    
    /**
     * Configuración avanzada con múltiples opciones
     */
    public static void advancedConfiguration() throws Exception {
        System.out.println("\n=== EJEMPLO 11: CONFIGURACIÓN AVANZADA ===");
        
        Warmup warmup = Warmup.create()
                             // Configuración de propiedades
                             .withProperty("db.url", "jdbc:h2:mem:test")
                             .withProperty("server.port", "8080")
                             .withProperty("cache.enabled", "true")
                             // Configuración de perfiles
                             .withProfiles("development", "logging")
                             // Configuración de paquetes
                             .scanPackages("io.warmup.examples")
                             // Configuración de comportamiento
                             .withAutoScan(true)
                             .withLazyInit(false)
                             .withShutdownTimeout(60, TimeUnit.SECONDS);
        
        WarmupContainer container = warmup.start();
        
        // Información completa del framework
        Map<String, Object> info = warmup.getInfo();
        System.out.println("Información del framework:");
        info.forEach((key, value) -> System.out.println("  " + key + ": " + value));
        
        container.shutdown();
    }
    
    // === CLASES DE SOPORTE PARA LOS EJEMPLOS ===
    
    @Component
    public static class MyService {
        @Inject
        private MyRepository repository;
        
        public void doSomething() {
            System.out.println("MyService doing something with " + repository);
        }
    }
    
    @Component
    @Profile("development")
    public static class MyRepository {
        public String toString() { return "Development Repository"; }
    }
    
    @Component
    public static class MyConfig {
        public String getConfig() { return "Configuration"; }
    }
    
    public static class UserRegisteredEvent extends Event {
        private final Long userId;
        private final String email;
        
        public UserRegisteredEvent(Long userId, String email) {
            this.userId = userId;
            this.email = email;
        }
        
        public Long getUserId() { return userId; }
        public String getEmail() { return email; }
    }
    
    @Component
    public static class UserEventListener {
        @EventListener
        public void handleUserRegistered(UserRegisteredEvent event) {
            System.out.println("User registered: " + event.getUserId());
        }
    }
    
    @Component
    public static class ExampleDatabaseService {
        @Inject
        private DatabaseConfig config;
        
        public void connect() {
            System.out.println("ExampleDatabaseService connecting to: " + config.getConnectionString());
        }
    }
    
    @Component
    @Profile("development")
    public static class DatabaseConfig {
        public String getConnectionString() {
            return "jdbc:h2:mem:dev";
        }
    }
    
    @Component
    @Profile("production")
    public static class ProductionDatabaseConfig {
        public String getConnectionString() {
            return "jdbc:postgresql://prod-db:5432/app";
        }
    }
    
    // === MAIN PARA DEMOSTRACIONES ===
    
    public static void main(String[] args) throws Exception {
        System.out.println("=================================================");
        System.out.println("    WARMUP FRAMEWORK API EXAMPLES");
        System.out.println("=================================================");
        
        try {
            // Ejecutar ejemplos individuales
            basicApplication();
            commandLineApplication(args);
            profileBasedApplication("development");
            quickStartExample();
            eventExample();
            quickAccessExample();
            lifecycleExample();
            manualBeanRegistration();
            predefinedModes();
            advancedConfiguration();
            
            System.out.println("\n=================================================");
            System.out.println("         ALL EXAMPLES COMPLETED SUCCESSFULLY");
            System.out.println("=================================================");
            
        } catch (Exception e) {
            System.err.println("Error running examples: " + e.getMessage());
            e.printStackTrace();
        }
    }
}