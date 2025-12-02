package io.warmup.framework.ai.tribuo;

import java.util.Objects;

/**
 * Implementación de Regressor para compatibilidad con Tribuo
 * 
 * @author MiniMax Agent
 */
public class Regressor {
    
    private final String name;
    private final double value;
    private final long timestamp;
    
    public Regressor() {
        this("regression", 0.0);
    }
    
    public Regressor(String name) {
        this(name, 0.0);
    }
    
    public Regressor(String name, double value) {
        this.name = Objects.requireNonNull(name, "Regressor name cannot be null");
        this.value = value;
        this.timestamp = System.currentTimeMillis();
    }
    
    /**
     * Obtiene el nombre del regressor
     */
    public String getName() {
        return name;
    }
    
    /**
     * Obtiene el valor predicho
     */
    public double getValue() {
        return value;
    }
    
    /**
     * Obtiene el timestamp de creación
     */
    public long getTimestamp() {
        return timestamp;
    }
    
    /**
     * Crea un nuevo Regressor con un valor diferente
     */
    public Regressor withValue(double newValue) {
        return new Regressor(name, newValue);
    }
    
    /**
     * Suma un valor a este regressor
     */
    public Regressor add(double addend) {
        return new Regressor(name, value + addend);
    }
    
    /**
     * Resta un valor de este regressor
     */
    public Regressor subtract(double subtrahend) {
        return new Regressor(name, value - subtrahend);
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        
        Regressor regressor = (Regressor) o;
        return Objects.equals(name, regressor.name);
    }
    
    @Override
    public int hashCode() {
        return name.hashCode();
    }
    
    @Override
    public String toString() {
        return String.format("Regressor{name='%s', value=%.3f}", name, value);
    }
}