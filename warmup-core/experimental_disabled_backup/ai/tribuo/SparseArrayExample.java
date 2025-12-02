package io.warmup.framework.ai.tribuo;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Implementación de SparseArrayExample para compatibilidad con Tribuo
 * 
 * @author MiniMax Agent
 */
public class SparseArrayExample<T> {
    
    private final Map<String, Double> features;
    private T label;
    private String id;
    private long timestamp;
    
    public SparseArrayExample() {
        this.features = new HashMap<>();
        this.timestamp = System.currentTimeMillis();
    }
    
    public SparseArrayExample(T label, String id) {
        this();
        this.label = label;
        this.id = id;
    }
    
    /**
     * Agrega una característica con valor
     */
    public void add(double value, String name) {
        features.put(name, value);
    }
    
    /**
     * Agrega múltiples características
     */
    public void addAll(Map<String, Double> featureMap) {
        features.putAll(featureMap);
    }
    
    /**
     * Obtiene el valor de una característica
     */
    public double get(String name) {
        return features.getOrDefault(name, 0.0);
    }
    
    /**
     * Verifica si tiene una característica
     */
    public boolean has(String name) {
        return features.containsKey(name);
    }
    
    /**
     * Obtiene todas las características
     */
    public Map<String, Double> getFeatures() {
        return new HashMap<>(features);
    }
    
    /**
     * Obtiene el número de características no cero
     */
    public int size() {
        return features.size();
    }
    
    /**
     * Verifica si está vacía
     */
    public boolean isEmpty() {
        return features.isEmpty();
    }
    
    // Getters and Setters
    public T getLabel() {
        return label;
    }
    
    public void setLabel(T label) {
        this.label = label;
    }
    
    public String getId() {
        return id;
    }
    
    public void setId(String id) {
        this.id = id;
    }
    
    public long getTimestamp() {
        return timestamp;
    }
    
    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        
        SparseArrayExample<?> that = (SparseArrayExample<?>) o;
        return Objects.equals(features, that.features) &&
               Objects.equals(label, that.label) &&
               Objects.equals(id, that.id);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(features, label, id);
    }
    
    @Override
    public String toString() {
        return String.format("SparseArrayExample{label=%s, features=%d, id='%s'}", 
                           label, features.size(), id);
    }
}