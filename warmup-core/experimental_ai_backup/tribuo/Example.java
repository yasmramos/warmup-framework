package io.warmup.framework.ai.tribuo;

import java.util.Objects;

/**
 * Implementación de Example para compatibilidad con Tribuo
 * 
 * @author MiniMax Agent
 */
public class Example<T> {
    
    private final SparseArrayExample<T> features;
    private final T label;
    private final String id;
    private final long timestamp;
    
    public Example(SparseArrayExample<T> features, T label, String id) {
        this(features, label, id, System.currentTimeMillis());
    }
    
    public Example(SparseArrayExample<T> features, T label, String id, long timestamp) {
        this.features = Objects.requireNonNull(features, "Features cannot be null");
        this.label = Objects.requireNonNull(label, "Label cannot be null");
        this.id = Objects.requireNonNull(id, "ID cannot be null");
        this.timestamp = timestamp;
    }
    
    /**
     * Constructor para ejemplos sin etiqueta (testing)
     */
    public Example(SparseArrayExample<T> features, String id) {
        this(features, null, id, System.currentTimeMillis());
    }
    
    /**
     * Obtiene las características
     */
    public SparseArrayExample<T> getFeatures() {
        return features;
    }
    
    /**
     * Obtiene la etiqueta
     */
    public T getLabel() {
        return label;
    }
    
    /**
     * Obtiene el ID
     */
    public String getId() {
        return id;
    }
    
    /**
     * Obtiene el timestamp
     */
    public long getTimestamp() {
        return timestamp;
    }
    
    /**
     * Verifica si tiene etiqueta
     */
    public boolean hasLabel() {
        return label != null;
    }
    
    /**
     * Verifica si es de tipo clasificación
     */
    public boolean isClassificationExample() {
        return hasLabel() && (label instanceof Label);
    }
    
    /**
     * Verifica si es de tipo regresión
     */
    public boolean isRegressionExample() {
        return hasLabel() && (label instanceof Regressor);
    }
    
    /**
     * Obtiene el valor de una característica
     */
    public double getFeatureValue(String name) {
        return features.get(name);
    }
    
    /**
     * Verifica si tiene una característica
     */
    public boolean hasFeature(String name) {
        return features.has(name);
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        
        Example<?> example = (Example<?>) o;
        return Objects.equals(features, example.features) &&
               Objects.equals(label, example.label) &&
               Objects.equals(id, example.id);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(features, label, id);
    }
    
    @Override
    public String toString() {
        return String.format("Example{id='%s', label=%s, features=%d}", 
                           id, label, features.size());
    }
}