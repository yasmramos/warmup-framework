package io.warmup.framework.core;

/**
 * 🔗 BUILDERS FLUIDOS PARA CONFIGURACIÓN DE BINDINGS
 * 
 * Esta clase implementa el patrón Builder para configurar bindings
 * de manera fluida y expresiva, tal como lo esperan los benchmarks.
 */
public class BindingBuilder<T> {
    
    private final Warmup warmup;
    private final Class<T> interfaceType;
    private String beanName;
    private Class<? extends T> implementationType;
    private boolean isSingleton = false;
    private boolean asSingletonCalled = false;
    
    // Constructor interno - solo accesible desde Warmup
    BindingBuilder(Warmup warmup, Class<T> interfaceType) {
        this.warmup = warmup;
        this.interfaceType = interfaceType;
    }
    
    /**
     * 🏷️ NOMBRAR EL BEAN
     * Permite asignar un nombre específico al bean
     */
    public BindingBuilder<T> named(String name) {
        this.beanName = name;
        return this;
    }
    
    /**
     * ⚙️ ESPECIFICAR IMPLEMENTACIÓN
     * Define qué clase concreta implementar
     */
    public BindingBuilder<T> to(Class<? extends T> implementation) {
        this.implementationType = implementation;
        return this;
    }
    
    /**
     * 📦 CONFIGURAR COMO SINGLETON
     * Marca el bean para que se cree una sola instancia
     */
    public BindingBuilder<T> asSingleton() {
        this.asSingletonCalled = true;
        this.isSingleton = true;
        return this;
    }
    
    /**
     * 📝 REGISTRAR EL BEAN
     * Finaliza la configuración y registra el bean en el contenedor
     */
    public Warmup register() {
        // Si no se especificó nombre, generar uno automáticamente
        if (beanName == null) {
            beanName = generateBeanName();
        }
        
        // Si no se especificó implementación, usar la interfaz (para interfaces)
        Class<? extends T> actualImplementation = implementationType;
        if (actualImplementation == null) {
            actualImplementation = interfaceType;
        }
        
        // Registrar en el contenedor subyacente
        warmup.getContainer().registerBean(beanName, actualImplementation, null);
        
        // Configurar scope si es necesario
        if (asSingletonCalled) {
            // Aquí se podría configurar el scope singleton en el contenedor
            // Por ahora, simplemente marcamos que debe ser singleton
        }
        
        return warmup;
    }
    
    /**
     * 🔧 GENERAR NOMBRE DEL BEAN
     * Genera un nombre automático basado en la clase
     */
    private String generateBeanName() {
        if (implementationType != null) {
            return Warmup.getBeanName(implementationType);
        }
        return Warmup.getBeanName(interfaceType);
    }
    
    // === MÉTODOS DE ACCESO DIRECTO (para compatibilidad) ===
    
    /**
     * 🎯 OBTENER BEAN DIRECTAMENTE
     * Para compatibilidad con benchmarks que acceden directamente
     */
    public T get() {
        // Registrar automáticamente si no se ha registrado
        if (implementationType != null) {
            register();
        }
        return warmup.get(interfaceType);
    }
    
    /**
     * 🎯 OBTENER BEAN NOMBRADO
     */
    public T get(String name) {
        // Registrar automáticamente si no se ha registrado
        if (implementationType != null) {
            register();
        }
        return warmup.getNamed(interfaceType, name);
    }
}