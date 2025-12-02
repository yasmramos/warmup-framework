package io.warmup.framework.validation;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * A report containing all constraint violations found during validation.
 * 
 * @author MiniMax Agent
 * @since 1.0
 */
public class ViolationReport<T> {
    
    private final T validatedObject;
    private final List<ConstraintViolation<T>> violations;
    
    public ViolationReport(T validatedObject) {
        this.validatedObject = validatedObject;
        this.violations = new ArrayList<>();
    }
    
    /**
     * Add a constraint violation to the report.
     * 
     * @param violation the violation to add
     */
    public void addViolation(ConstraintViolation<T> violation) {
        violations.add(violation);
    }
    
    /**
     * Add all violations from another report.
     * 
     * @param other the other report
     */
    public void addAll(ViolationReport<T> other) {
        violations.addAll(other.violations);
    }
    
    /**
     * Check if there are any violations.
     * 
     * @return true if there are violations, false otherwise
     */
    public boolean hasViolations() {
        return !violations.isEmpty();
    }
    
    /**
     * Check if the validation passed (no violations).
     * 
     * @return true if validation passed (no violations), false otherwise
     */
    public boolean isValid() {
        return !hasViolations();
    }
    
    /**
     * Get the number of violations.
     * 
     * @return the number of violations
     */
    public int getViolationCount() {
        return violations.size();
    }
    
    /**
     * Get all violations.
     * 
     * @return list of all violations
     */
    public List<ConstraintViolation<T>> getViolations() {
        return new ArrayList<>(violations);
    }
    
    /**
     * Get violations for a specific property.
     * 
     * @param propertyName the property name
     * @return list of violations for the property
     */
    public List<ConstraintViolation<T>> getViolationsForProperty(String propertyName) {
        return violations.stream()
                .filter(v -> v.getPropertyName().equals(propertyName))
                .collect(Collectors.toList());
    }
    
    /**
     * Get the validated object.
     * 
     * @return the validated object
     */
    public T getValidatedObject() {
        return validatedObject;
    }
    
    /**
     * Get a formatted message with all violations.
     * 
     * @return formatted message
     */
    public String getFormattedMessage() {
        if (!hasViolations()) {
            return "No validation violations found.";
        }
        
        StringBuilder sb = new StringBuilder();
        sb.append("Validation failed with ").append(violations.size()).append(" violation(s):\n");
        
        for (int i = 0; i < violations.size(); i++) {
            ConstraintViolation<T> violation = violations.get(i);
            sb.append(i + 1).append(". ").append(violation.getMessage());
            if (violation.getPropertyName() != null && !violation.getPropertyName().isEmpty()) {
                sb.append(" (property: ").append(violation.getPropertyName()).append(")");
            }
            if (violation.getInvalidValue() != null) {
                sb.append(" (value: ").append(violation.getInvalidValue()).append(")");
            }
            sb.append("\n");
        }
        
        return sb.toString();
    }
    
    @Override
    public String toString() {
        return getFormattedMessage();
    }
}