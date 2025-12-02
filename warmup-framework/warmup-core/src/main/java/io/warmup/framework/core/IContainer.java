package io.warmup.framework.core;

import java.util.Set;

/**
 * 🔧 INTERFAZ COMÚN PARA RESOLUCIÓN DE DEPENDENCIAS
 * 
 * Esta interfaz define los métodos necesarios para la resolución de dependencias
 * que tanto WarmupContainer como CoreContainer necesitan implementar.
 * 
 * Resuelve el problema de incompatibilidad de tipos entre CoreContainer y WarmupContainer
 * en la llamada a Dependency.getInstance().
 * 
 * @author Warmup Framework
 * @version 2.0
 */
public interface IContainer {
    
    /**
     * Obtener una dependencia por tipo
     * 
     * @param <T> el tipo de dependencia
     * @param type el tipo de dependencia
     * @param dependencyChain la cadena de dependencias para detectar ciclos
     * @return la instancia de la dependencia
     * @throws Exception si hay error en la resolución
     */
    <T> T getDependency(Class<T> type, Set<Class<?>> dependencyChain) throws Exception;
    
    /**
     * Obtener una dependencia con nombre específico
     * 
     * @param <T> el tipo de dependencia
     * @param type el tipo de dependencia
     * @param name el nombre de la dependencia
     * @param dependencyChain la cadena de dependencias para detectar ciclos
     * @return la instancia de la dependencia
     * @throws Exception si hay error en la resolución
     */
    <T> T getNamedDependency(Class<T> type, String name, Set<Class<?>> dependencyChain) throws Exception;
    
    /**
     * Resolver un valor de propiedad
     * 
     * @param expression la expresión de la propiedad
     * @return el valor resuelto de la propiedad
     */
    String resolvePropertyValue(String expression);
    
    /**
     * Obtener la mejor implementación para una interfaz
     * 
     * @param <T> el tipo de la interfaz
     * @param interfaceType el tipo de interfaz
     * @return la mejor implementación disponible
     * @throws Exception si no hay implementación disponible
     */
    <T> T getBestImplementation(Class<T> interfaceType) throws Exception;
    
    /**
     * Registrar listeners de eventos para una instancia
     * 
     * @param clazz la clase de la instancia
     * @param instance la instancia
     */
    void registerEventListeners(Class<?> clazz, Object instance);
    
    /**
     * Registrar una dependencia por tipo
     * 
     * @param <T> el tipo de dependencia
     * @param type el tipo de dependencia
     * @param singleton true si debe ser singleton
     */
    <T> void register(Class<T> type, boolean singleton);
    
    /**
     * Registrar una dependencia con nombre específico
     * 
     * @param <T> el tipo de dependencia
     * @param type el tipo de dependencia
     * @param name el nombre de la dependencia
     * @param singleton true si debe ser singleton
     */
    <T> void registerNamed(Class<T> type, String name, boolean singleton);
    
    /**
     * Obtener una dependencia por tipo (alias para getDependency)
     * 
     * @param <T> el tipo de dependencia
     * @param type el tipo de dependencia
     * @return la instancia de la dependencia
     */
    <T> T get(Class<T> type);
    
    /**
     * Obtener una dependencia con nombre específico (alias para getNamedDependency)
     * 
     * @param <T> el tipo de dependencia
     * @param type el tipo de dependencia
     * @param name el nombre de la dependencia
     * @return la instancia de la dependencia
     */
    <T> T getNamed(Class<T> type, String name);
}