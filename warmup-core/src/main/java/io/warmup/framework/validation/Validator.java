package io.warmup.framework.validation;

import java.util.List;

/**
 * Validates bean instances using the Bean Validation annotations.
 * This is the main entry point for bean validation in the Warmup framework.
 * 
 * @author MiniMax Agent
 * @since 1.0
 */
public interface Validator {
    
    /**
     * Validate the specified object.
     * The specified object must be a validatable bean instance.
     *
     * @param <T> the type of the object to validate
     * @param object the object to validate
     * @param validationGroups the group or list of groups targeted for validation
     * @return list of constraint violations or an empty list if none
     * @throws IllegalArgumentException if object is null or of an unsupported type
     */
    <T> List<ConstraintViolation<T>> validate(T object, Class<?>... validationGroups);
    
    /**
     * Validate the specified object property.
     *
     * @param <T> the type of the object containing the property to validate
     * @param object the object containing the property to validate
     * @param propertyName the property name (relative to object)
     * @param validationGroups the group or list of groups targeted for validation
     * @return list of constraint violations or an empty list if none
     * @throws IllegalArgumentException if object is null or propertyName is null or empty
     */
    <T> List<ConstraintViolation<T>> validateProperty(T object, String propertyName, Class<?>... validationGroups);
    
    /**
     * Validate all constraints on the specified object.
     * 
     * @param <T> the type of the object to validate
     * @param object the object to validate
     * @param validationGroups the group or list of groups targeted for validation
     * @return true if no violations are found, false otherwise
     * @throws IllegalArgumentException if object is null or of an unsupported type
     */
    <T> boolean isValid(T object, Class<?>... validationGroups);
    
    /**
     * Get a violation report of all constraint violations.
     *
     * @param <T> the type of the object to validate
     * @param object the object to validate
     * @param validationGroups the group or list of groups targeted for validation
     * @return a ViolationReport containing all constraint violations
     */
    <T> ViolationReport<T> getViolationReport(T object, Class<?>... validationGroups);
}