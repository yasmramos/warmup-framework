package io.warmup.test.core;

import org.mockito.Mockito;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Repositorio centralizado para manejar todos los mocks y spies creados.
 * Proporciona funcionalidades para limpiar y resetear mocks automáticamente.
 */
class MockRepository {
    
    private final Map<String, Object> mocks = new ConcurrentHashMap<>();
    private final Map<String, Object> spies = new ConcurrentHashMap<>();
    private final Map<String, Object> realBeans = new ConcurrentHashMap<>();
    
    /**
     * Registrar un mock con nombre único.
     */
    public void registerMock(String name, Object mock) {
        mocks.put(name, mock);
    }
    
    /**
     * Registrar un spy con nombre único.
     */
    public void registerSpy(String name, Object spy) {
        spies.put(name, spy);
    }
    
    /**
     * Registrar un bean real con nombre único.
     */
    public void registerRealBean(String name, Object bean) {
        realBeans.put(name, bean);
    }
    
    /**
     * Obtener un mock por nombre.
     */
    public Object getMock(String name) {
        return mocks.get(name);
    }
    
    /**
     * Obtener un spy por nombre.
     */
    public Object getSpy(String name) {
        return spies.get(name);
    }
    
    /**
     * Obtener un bean real por nombre.
     */
    public Object getRealBean(String name) {
        return realBeans.get(name);
    }
    
    /**
     * Resetear todos los mocks y spies.
     */
    public void resetAll() {
        mocks.values().forEach(this::resetObject);
        spies.values().forEach(this::resetObject);
    }
    
    /**
     * Limpiar completamente el repositorio.
     */
    public void clearAll() {
        mocks.clear();
        spies.clear();
        realBeans.clear();
    }
    
    private void resetObject(Object obj) {
        try {
            if (obj != null) {
                Mockito.reset(obj);
            }
        } catch (Exception e) {
            // Log warning but don't fail the test
            System.err.println("Warning: Could not reset mock/spy: " + e.getMessage());
        }
    }
}