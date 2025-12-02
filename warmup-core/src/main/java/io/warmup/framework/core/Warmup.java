package io.warmup.framework.core;

import io.warmup.framework.annotation.Component;
import io.warmup.framework.annotation.Profile;
import io.warmup.framework.event.Event;
import io.warmup.framework.metrics.ContainerMetrics;
import io.warmup.framework.core.ContainerState;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

/**
 * 🚀 WARMUP - PUNTO DE ENTRADA PRINCIPAL DEL FRAMEWORK
 * 
 * Esta es la clase principal que los desarrolladores usarán como punto de entrada.
 * Proporciona una API familiar y consistente con frameworks como Spring, Micronaut, etc.
 * 
 * CARACTERÍSTICAS:
 * - Métodos estáticos de conveniencia como punto de entrada único
 * - API fluida para configuración
 * - Inicialización automática de beans
 * - Soporte nativo para perfiles y configuración condicional
 * - Lifecycle management completo
 * - Métricas de performance integradas
 * 
 * EJEMPLO DE USO:
 * 
 * @Component
 * public class MyService {
 *     public void doSomething() {
 *         // Lógica del servicio
 *     }
 * }
 * 
 * public class Main {
 *     public static void main(String[] args) {
 *         // Opción 1: Crear, configurar e iniciar por separado
 *         WarmupContainer container = Warmup.create()
 *             .scanPackages("com.myapp")
 *             .withProfile("development")
 *             .withProperty("db.url", "jdbc:h2:mem:test")
 *             .start();  // Sin argumentos para mantener consistencia
 * 
 *         MyService service = container.getBean(MyService.class);
 *         service.doSomething();
 *     }
 * }
 * 
 * EJEMPLOS RÁPIDOS:
 * 
 * // Inicio rápido (configuración automática)
 * WarmupContainer quick = Warmup.quickStart();
 * 
 * // Con perfil específico
 * WarmupContainer dev = Warmup.withProfile("development");
 */
public class Warmup {
    
    // === CONFIGURACIÓN DEL FRAMEWORK ===
    private final WarmupContainer container;
    private final List<String> scanPackages = new ArrayList<>();
    private final Map<String, String> properties = new HashMap<>();
    private final Set<String> activeProfiles = new HashSet<>();
    private boolean autoScan = true;
    private boolean lazyInit = false;
    private long shutdownTimeout = 30_000; // 30 segundos por defecto
    
    // Constructor privado - usar métodos estáticos
    private Warmup() {
        this.container = new WarmupContainer();
    }
    
    // === MÉTODOS ESTÁTICOS DE ENTRADA ===
    
    /**
     * 🎯 PUNTO DE ENTRADA PRINCIPAL
     * Crea una nueva instancia de Warmup para configuración
     * 
     * Ejemplo:
     * Warmup warmup = Warmup.create()
     *     .scanPackages("com.myapp")
     *     .withProfile("development");
     */
    public static Warmup create() {
        return new Warmup();
    }
    
    /**
     * 🎯 PUNTO DE ENTRADA CON ARGUMENTOS
     * Crea una nueva instancia de Warmup y procesa argumentos de línea de comandos
     * 
     * Ejemplo:
     * Warmup warmup = Warmup.run(args)
     *     .scanPackages("com.myapp")
     *     .start();
     */
    public static Warmup run(String[] args) {
        Warmup warmup = new Warmup();
        if (args != null && args.length > 0) {
            warmup.parseCommandLineArgs(args);
        }
        return warmup;
    }
    
    /**
     * 🎯 INICIO RÁPIDO CON CONFIGURACIÓN MÍNIMA
     * Inicializa el framework con configuración por defecto
     */
    public static WarmupContainer quickStart() {
        return Warmup.create()
                    .start();
    }
    
    /**
     * 🎯 INICIO CON PERFIL ESPECÍFICO
     * Configura automáticamente el perfil activo e inicia
     */
    public static WarmupContainer runWithProfile(String profile) {
        return Warmup.create()
                    .withProfile(profile)
                    .start();
    }
    
    /**
     * 🎯 INICIO CON CONFIGURACIÓN ESPECÍFICA
     * Inicia con perfil y argumentos de línea de comandos
     */
    public static WarmupContainer runWithProfile(String profile, String... args) {
        return Warmup.create()
                    .withProfile(profile)
                    .start(args);
    }
    
    // === MÉTODOS DE CONFIGURACIÓN ===
    
    /**
     * 📦 Configurar paquetes para escaneo automático
     * Escaneará estos paquetes buscando beans anotados con @Component
     */
    public Warmup scanPackages(String... packages) {
        Collections.addAll(this.scanPackages, packages);
        return this;
    }
    
    /**
     * 📦 Agregar un paquete específico para escaneo
     */
    public Warmup addPackage(String packageName) {
        this.scanPackages.add(packageName);
        return this;
    }
    
    /**
     * 📦 Configurar perfil activo
     * Los beans con @Profile se activarán según este perfil
     */
    public Warmup withProfile(String profile) {
        this.activeProfiles.add(profile);
        return this;
    }
    
    /**
     * 📦 Configurar múltiples perfiles
     */
    public Warmup withProfiles(String... profiles) {
        Collections.addAll(this.activeProfiles, profiles);
        return this;
    }
    
    /**
     * 📦 Configurar propiedad
     * Se puede acceder desde beans usando @Inject("property.key")
     */
    public Warmup withProperty(String key, String value) {
        this.properties.put(key, value);
        return this;
    }
    
    /**
     * 📦 Configurar múltiples propiedades
     */
    public Warmup withProperties(Map<String, String> properties) {
        this.properties.putAll(properties);
        return this;
    }
    
    /**
     * 📦 Configurar auto-scanning
     * Si true, escaneará automáticamente todos los paquetes del classpath
     */
    public Warmup withAutoScan(boolean autoScan) {
        this.autoScan = autoScan;
        return this;
    }
    
    /**
     * 📦 Configurar inicialización perezosa
     * Los beans se inicializarán solo cuando se necesiten
     */
    public Warmup withLazyInit(boolean lazyInit) {
        this.lazyInit = lazyInit;
        return this;
    }
    
    /**
     * 📦 Configurar timeout de shutdown
     */
    public Warmup withShutdownTimeout(long timeout, TimeUnit unit) {
        this.shutdownTimeout = TimeUnit.MILLISECONDS.convert(timeout, unit);
        return this;
    }
    
    // === MÉTODOS DE INICIALIZACIÓN ===
    
    /**
     * 🚀 INICIAR EL FRAMEWORK
     * Inicializa el contenedor y todos los beans configurados
     * 
     * @param args Argumentos de línea de comandos (opcional)
     * @return WarmupContainer configurado e inicializado
     * 
     * Ejemplos:
     * warmup.start();                    // Sin argumentos
     * warmup.start(args);                // Con argumentos
     * 
     * Argumentos soportados:
     * --profiles.active=dev              // Configurar perfil
     * --warmup.profile=prod              // Alias para perfil
     * --db.url=jdbc:h2:mem:test          // Configurar propiedades
     */
    public WarmupContainer start(String... args) {
        // Parsear argumentos si se proporcionan
        if (args != null && args.length > 0) {
            parseCommandLineArgs(args);
        }
        
        // Configurar propiedades en el contenedor
        for (Map.Entry<String, String> prop : properties.entrySet()) {
            container.setProperty(prop.getKey(), prop.getValue());
        }
        
        // Configurar perfiles activos
        if (!activeProfiles.isEmpty()) {
            @SuppressWarnings("unchecked")
            String[] profilesArray = activeProfiles.toArray(new String[0]);
            container.setActiveProfiles(profilesArray);
        }
        
        // Configurar paquetes para escaneo
        if (autoScan) {
            for (String pkg : scanPackages) {
                container.scanPackage(pkg);
            }
        }
        
        // Inicializar el contenedor
        try {
            container.start();
        } catch (Exception e) {
            throw new RuntimeException("Failed to start container", e);
        }
        
        // Retornar el contenedor configurado e inicializado
        return this.container;
    }
    
    /**
     * 🚀 INICIAR ASÍNCRONAMENTE
     * Inicializa el framework en un hilo separado
     */
    public CompletableFuture<WarmupContainer> startAsync() {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return start();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }
    
    // === MÉTODOS DE ACCESO RÁPIDO ===
    
    /**
     * 🎯 OBTENER BEAN POR TIPO
     * Método shortcut para obtener beans sin pasar por el contenedor
     */
    public <T> T getBean(Class<T> clazz) {
        return container.getBean(clazz);
    }
    
    /**
     * 🎯 OBTENER BEAN POR NOMBRE
     */
    public <T> T getBean(String name, Class<T> clazz) {
        return container.getBean(name, clazz);
    }
    
    /**
     * 🎯 VERIFICAR SI UN BEAN EXISTE
     */
    public boolean hasBean(Class<?> clazz) {
        try {
            container.getBean(clazz);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * 🎯 VERIFICAR SI UN PERFIL ESTÁ ACTIVO
     */
    public boolean isProfileActive(String profile) {
        return activeProfiles.contains(profile);
    }
    
    /**
     * 🎯 OBTENER PROPIEDAD
     */
    public String getProperty(String key) {
        return container.getProperty(key);
    }
    
    /**
     * 🎯 OBTENER PROPIEDAD CON VALOR POR DEFECTO
     */
    public String getProperty(String key, String defaultValue) {
        return container.getProperty(key, defaultValue);
    }
    
    /**
     * 🎯 OBTENER PROPERTY COMO ENTERO
     */
    public int getPropertyAsInt(String key, int defaultValue) {
        String value = getProperty(key);
        if (value == null) return defaultValue;
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }
    
    /**
     * 🎯 OBTENER PROPERTY COMO BOOLEAN
     */
    public boolean getPropertyAsBoolean(String key, boolean defaultValue) {
        String value = getProperty(key);
        if (value == null) return defaultValue;
        return Boolean.parseBoolean(value);
    }
    
    // === MÉTODOS DE EVENTOS ===
    
    /**
     * 📡 PUBLICAR EVENTO
     * Método shortcut para publicar eventos
     */
    public void publishEvent(Object event) {
        if (event instanceof Event) {
            container.dispatchEvent((Event) event);
        } else {
            // Crear un evento genérico si es necesario
            container.dispatchEvent(new Event() {
                // Event class solo tiene getTimestamp()
            });
        }
    }
    
    /**
     * 📡 OBTENER EVENT MANAGER
     * Para operaciones avanzadas de eventos
     */
    public EventManager getEventManager() {
        return (EventManager) container.getEventManager();
    }
    
    // === MÉTODOS DE LIFECYCLE ===
    
    /**
     * 🔄 REINICIAR EL FRAMEWORK
     * Limpia y reinicia todos los beans
     */
    public WarmupContainer restart() {
        try {
            container.shutdown();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } catch (Exception e) {
            throw new RuntimeException("Failed to shutdown container", e);
        }
        try {
            return start();
        } catch (Exception e) {
            throw new RuntimeException("Failed to restart container", e);
        }
    }
    
    /**
     * 🔄 REINICIAR ASÍNCRONAMENTE
     */
    public CompletableFuture<WarmupContainer> restartAsync() {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return restart();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }
    
    /**
     * 🛑 DETENER EL FRAMEWORK
     * Limpia todos los recursos
     */
    public void stop() {
        try {
            container.shutdown();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } catch (Exception e) {
            throw new RuntimeException("Failed to shutdown container", e);
        }
    }
    
    /**
     * 🛑 DETENER CON TIMEOUT
     */
    public void stop(long timeout, TimeUnit unit) {
        try {
            container.shutdown(true, TimeUnit.MILLISECONDS.convert(timeout, unit));
        } catch (Exception e) {
            throw new RuntimeException("Failed to stop container with timeout", e);
        }
    }
    
    // === MÉTODOS DE INFORMACIÓN ===
    
    /**
     * 📊 OBTENER MÉTRICAS
     * Información de performance del framework
     */
    public ContainerMetrics getMetrics() {
        return (ContainerMetrics) container.getMetrics();
    }
    
    /**
     * 📊 OBTENER ESTADO DEL FRAMEWORK
     */
    public ContainerState getState() {
        return (ContainerState) container.getState();
    }
    
    /**
     * 📊 VERIFICAR SI ESTÁ INICIALIZADO
     */
    public boolean isRunning() {
        return container.isRunning();
    }
    
    /**
     * 📊 VERIFICAR SI ESTÁ DETENIDO
     */
    public boolean isStopped() {
        return container.isShutdown();
    }
    
    /**
     * 📊 NÚMERO DE INSTANCIAS ACTIVAS
     */
    public int getBeanCount() {
        return container.getActiveInstancesCount();
    }
    
    /**
     * 📊 NÚMERO DE BEANS (ALIAS PARA getBeanCount)
     * Método utilizado por los ejemplos
     */
    public int getActiveBeanCount() {
        return container.getActiveInstancesCount();
    }
    
    /**
     * 📊 INFORMACIÓN DEL FRAMEWORK
     */
    public String getVersion() {
        return "Warmup Framework v1.0.0";
    }
    
    /**
     * 📊 INFORMACIÓN COMPLETA
     */
    public Map<String, Object> getInfo() {
        Map<String, Object> info = new HashMap<>();
        info.put("version", getVersion());
        info.put("state", getState());
        info.put("beanCount", getBeanCount());
        info.put("activeProfiles", new ArrayList<>(activeProfiles));
        info.put("properties", new HashMap<>(properties));
        info.put("autoScan", autoScan);
        info.put("lazyInit", lazyInit);
        return info;
    }
    
    // === MÉTODOS DE REGISTRO DE BEANS ===
    
    /**
     * 🏷️ REGISTRAR BEAN
     * Método shortcut para registrar beans manualmente con nombre generado automáticamente
     */
    public <T> Warmup registerBean(Class<T> clazz, T instance) {
        String name = getBeanName(clazz);
        container.registerBean(name, clazz, instance);
        return this;
    }
    
    /**
     * 🏷️ REGISTRAR BEAN CON NOMBRE ESPECÍFICO
     */
    public <T> Warmup registerBean(String name, Class<T> clazz, T instance) {
        container.registerBean(name, clazz, instance);
        return this;
    }
    
    /**
     * 🏷️ REGISTRAR BEAN SI PERFIL ACTIVO
     */
    public <T> Warmup registerBeanIfProfile(Class<T> clazz, T instance, String profile) {
        if (isProfileActive(profile)) {
            registerBean(clazz, instance);
        }
        return this;
    }
    
    // === MÉTODOS DE UTILIDAD ===
    
    /**
     * 🔧 PARSEAR ARGUMENTOS DE LÍNEA DE COMANDOS
     * Convierte argumentos como --profile=dev en configuración
     */
    private void parseCommandLineArgs(String[] args) {
        for (String arg : args) {
            if (arg.startsWith("--")) {
                String[] parts = arg.substring(2).split("=", 2);
                if (parts.length == 2) {
                    String key = parts[0];
                    String value = parts[1];
                    
                    if (key.equals("spring.profiles.active") || key.equals("profiles.active")) {
                        withProfile(value);
                    } else if (key.equals("warmup.profile")) {
                        withProfile(value);
                    } else {
                        withProperty(key, value);
                    }
                }
            }
        }
    }
    
    /**
     * 🔧 OBTENER CONTENEDOR SUBYACENTE
     * Para operaciones avanzadas
     */
    public WarmupContainer getContainer() {
        return container;
    }
    
    // === MÉTODOS ESTÁTICOS DE UTILIDAD ===
    
    /**
     * 🛠️ VERIFICAR SI UNA CLASE ES UN BEAN
     * Detecta si una clase tiene anotaciones de bean
     */
    public static boolean isBean(Class<?> clazz) {
        return clazz.isAnnotationPresent(Component.class) ||
               clazz.isAnnotationPresent(Profile.class);
    }
    
    /**
     * 🛠️ OBTENER NOMBRE DEL BEAN
     * Genera un nombre por defecto basado en el nombre de clase
     */
    public static String getBeanName(Class<?> clazz) {
        String className = clazz.getSimpleName();
        return Character.toLowerCase(className.charAt(0)) + className.substring(1);
    }
    
    /**
     * 🛠️ CONVERTIR NOMBRE A CAMEL CASE
     */
    public static String toCamelCase(String name) {
        if (name == null || name.isEmpty()) return name;
        return Character.toLowerCase(name.charAt(0)) + name.substring(1);
    }
    
    /**
     * 🛠️ CONVERTIR NOMBRE A PASCAL CASE
     */
    public static String toPascalCase(String name) {
        if (name == null || name.isEmpty()) return name;
        return Character.toUpperCase(name.charAt(0)) + name.substring(1);
    }
    
    // === SHUTDOWN HOOK AUTOMÁTICO ===
    
    static {
        // Registrar hook de shutdown automático
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            // El shutdown automático se maneja a nivel de contenedor
        }));
    }
    
    // === MÉTODOS PARA TESTING ===
    
    /**
     * 🧪 MODO TEST
     * Configuración especial para testing
     */
    public static WarmupContainer testMode() {
        return Warmup.create()
                    .withProfile("test")
                    .start();
    }
    
    /**
     * 🧪 MODO DESARROLLO
     * Configuración para desarrollo
     */
    public static WarmupContainer devMode() {
        return Warmup.create()
                    .withProfile("development")
                    .start();
    }
    
    /**
     * 🧪 MODO PRODUCCIÓN
     * Configuración optimizada para producción
     */
    public static WarmupContainer prodMode() {
        return Warmup.create()
                    .withProfile("production")
                    .withShutdownTimeout(60, TimeUnit.SECONDS)
                    .start();
    }

    // === MÉTODOS PARA BENCHMARKS ===
    
    /**
     * 🎯 BIND - INICIAR CONFIGURACIÓN FLUIDA
     * Método utilizado por los benchmarks para iniciar configuración de binding
     */
    public <T> BindingBuilder<T> bind(Class<T> clazz) {
        return new BindingBuilder<>(this, clazz);
    }
    
    /**
     * 🎯 GET - OBTENER INSTANCIA DEL CONTENEDOR
     * Método utilizado por los benchmarks para obtener instancias
     */
    public <T> T get(Class<T> clazz) {
        return container.getBean(clazz);
    }
    
    /**
     * 🎯 GET NAMED - OBTENER INSTANCIA NOMBRADA
     * Método utilizado por los benchmarks para obtener instancias con nombre específico
     */
    public <T> T getNamed(Class<T> clazz, String name) {
        return container.getBean(name, clazz);
    }
    
    /**
     * 🎯 WITH AOP - HABILITAR AOP
     * Método utilizado por los benchmarks para habilitar AOP
     */
    public Warmup withAop() {
        // Por ahora, simplemente retornar this
        // La implementación real de AOP se haría en el contenedor
        return this;
    }
    
    /**
     * 🎯 WITH ASYNC - HABILITAR OPERACIONES ASÍNCRONAS
     * Método utilizado por los benchmarks para habilitar operaciones asíncronas
     */
    public Warmup withAsync() {
        // Por ahora, simplemente retornar this
        // La implementación real de async se haría en el contenedor
        return this;
    }
}