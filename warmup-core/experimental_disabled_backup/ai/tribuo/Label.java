package io.warmup.framework.ai.tribuo;

import java.util.Objects;

/**
 * Implementación de Label para compatibilidad con Tribuo
 * 
 * @author MiniMax Agent
 */
public class Label {
    
    private final String label;
    private final double score;
    private final long timestamp;
    
    public Label(String label) {
        this(label, 1.0);
    }
    
    public Label(String label, double score) {
        this.label = Objects.requireNonNull(label, "Label cannot be null");
        this.score = score;
        this.timestamp = System.currentTimeMillis();
    }
    
    /**
     * Obtiene el nombre de la etiqueta
     */
    public String getLabel() {
        return label;
    }
    
    /**
     * Obtiene el score/confianza
     */
    public double getScore() {
        return score;
    }
    
    /**
     * Obtiene el timestamp de creación
     */
    public long getTimestamp() {
        return timestamp;
    }
    
    /**
     * Compara si es igual a otra etiqueta
     */
    public boolean equals(Label other) {
        if (this == other) return true;
        return label.equals(other.label);
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        
        Label label1 = (Label) o;
        return label.equals(label1.label);
    }
    
    @Override
    public int hashCode() {
        return label.hashCode();
    }
    
    @Override
    public String toString() {
        return String.format("Label{label='%s', score=%.3f}", label, score);
    }
}