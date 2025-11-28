package io.warmup.framework.validation;

/**
 * Describes a validation constraint violation.
 * Contains all information about the violation including the property path,
 * the invalid value, and the constraint that was violated.
 * 
 * @author MiniMax Agent
 * @since 1.0
 */
public class ConstraintViolation<T> {
    
    private final String message;
    private final String messageTemplate;
    private final T rootBean;
    private final String propertyPath;
    private final Object invalidValue;
    private final String constraintAnnotationType;
    private final String propertyName;
    
    public ConstraintViolation(String message, String messageTemplate, T rootBean, 
                             String propertyPath, Object invalidValue, 
                             String constraintAnnotationType, String propertyName) {
        this.message = message;
        this.messageTemplate = messageTemplate;
        this.rootBean = rootBean;
        this.propertyPath = propertyPath;
        this.invalidValue = invalidValue;
        this.constraintAnnotationType = constraintAnnotationType;
        this.propertyName = propertyName;
    }
    
    /**
     * Get the formatted message for this violation.
     * 
     * @return the formatted message
     */
    public String getMessage() {
        return message;
    }
    
    /**
     * Get the message template (unformatted).
     * 
     * @return the message template
     */
    public String getMessageTemplate() {
        return messageTemplate;
    }
    
    /**
     * Get the root bean that was validated.
     * 
     * @return the root bean
     */
    public T getRootBean() {
        return rootBean;
    }
    
    /**
     * Get the property path to the invalid value.
     * 
     * @return the property path
     */
    public String getPropertyPath() {
        return propertyPath;
    }
    
    /**
     * Get the invalid value that caused the violation.
     * 
     * @return the invalid value
     */
    public Object getInvalidValue() {
        return invalidValue;
    }
    
    /**
     * Get the type of the constraint annotation that was violated.
     * 
     * @return the constraint annotation type
     */
    public String getConstraintAnnotationType() {
        return constraintAnnotationType;
    }
    
    /**
     * Get the property name that was validated.
     * 
     * @return the property name
     */
    public String getPropertyName() {
        return propertyName;
    }
    
    @Override
    public String toString() {
        return "ConstraintViolation{" +
                "message='" + message + '\'' +
                ", propertyPath='" + propertyPath + '\'' +
                ", invalidValue=" + invalidValue +
                ", constraint='" + constraintAnnotationType + '\'' +
                '}';
    }
}