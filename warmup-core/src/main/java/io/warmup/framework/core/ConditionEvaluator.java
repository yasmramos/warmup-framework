package io.warmup.framework.core;

import io.warmup.framework.annotation.ConditionalOnProperty;
import io.warmup.framework.annotation.ConditionalOnPropertyList;
import io.warmup.framework.config.PropertySource;
import io.warmup.framework.exception.WarmupException;

import java.lang.reflect.AnnotatedElement;
import java.util.Arrays;
import java.util.logging.Logger;

/**
 * Evaluates {@link ConditionalOnProperty} annotations to determine if beans should be registered.
 * Supports complex property-based conditional logic including:
 * - Value matching (havingValue)
 * - Missing property handling (matchIfMissing)
 * - Property existence checking
 * - Multiple conditions (anyOf, notHavingValue)
 * - Condition inversion
 * 
 * @author MiniMax Agent
 * @since 1.0
 */
public class ConditionEvaluator {
    
    private static final Logger logger = Logger.getLogger(ConditionEvaluator.class.getName());
    
    private final PropertySource propertySource;
    
    public ConditionEvaluator(PropertySource propertySource) {
        this.propertySource = propertySource;
    }
    
    /**
     * Evaluate if a bean with conditional annotations should be registered.
     * 
     * @param element the annotated element (method or class)
     * @return true if the bean should be registered, false otherwise
     */
    public boolean shouldRegister(AnnotatedElement element) {
        // Get all @ConditionalOnProperty annotations (including from container)
        ConditionalOnProperty[] conditions = getConditionalAnnotations(element);
        
        if (conditions.length == 0) {
            // No conditions, always register
            return true;
        }
        
        // Evaluate all conditions - ALL must pass for registration
        for (ConditionalOnProperty condition : conditions) {
            if (!evaluateCondition(condition)) {
                logger.fine("Bean registration blocked by condition: " + condition.name());
                return false;
            }
        }
        
        logger.fine("All conditions passed, bean will be registered");
        return true;
    }
    
    /**
     * Evaluate a single condition.
     */
    private boolean evaluateCondition(ConditionalOnProperty condition) {
        try {
            // Get the property value
            String propertyValue = propertySource.getProperty(condition.name());
            
            // Check if property exists
            boolean propertyExists = (propertyValue != null);
            
            // Handle matchIfMissing case
            if (!propertyExists && condition.matchIfMissing()) {
                return !condition.invert();
            }
            
            // If property doesn't exist and is required
            if (!propertyExists && condition.requireProperty()) {
                return condition.invert();
            }
            
            // Check anyOf condition (alternative properties)
            if (condition.anyOf().length > 0) {
                boolean anyMatches = false;
                for (String altProperty : condition.anyOf()) {
                    String altValue = propertySource.getProperty(altProperty);
                    if (altValue != null && matchesHavingValue(altValue, condition.havingValue())) {
                        anyMatches = true;
                        break;
                    }
                }
                if (!anyMatches) {
                    return condition.invert();
                }
            }
            
            // Check notHavingValue condition
            if (condition.notHavingValue().length > 0) {
                for (String excludedProperty : condition.notHavingValue()) {
                    String excludedValue = propertySource.getProperty(excludedProperty);
                    if (excludedValue != null && matchesHavingValue(excludedValue, condition.havingValue())) {
                        return condition.invert();
                    }
                }
            }
            
            // Check havingValue condition
            if (!condition.havingValue().isEmpty()) {
                if (!propertyExists) {
                    return condition.invert();
                }
                if (!matchesHavingValue(propertyValue, condition.havingValue())) {
                    return condition.invert();
                }
            }
            
            // Property exists and no specific value constraint, or value matches
            return !condition.invert();
            
        } catch (Exception e) {
            logger.warning("Error evaluating condition for property: " + condition.name() + " - " + e.getMessage());
            // If evaluation fails, don't register the bean to be safe
            return false;
        }
    }
    
    /**
     * Check if a property value matches the expected havingValue.
     * Handles different data types and comparison scenarios.
     */
    private boolean matchesHavingValue(String actualValue, String expectedValue) {
        if (expectedValue.isEmpty()) {
            return actualValue != null;
        }
        
        if (actualValue == null) {
            return false;
        }
        
        // Exact string match
        if (expectedValue.equals(actualValue)) {
            return true;
        }
        
        // Boolean true/false variations
        String normalizedExpected = expectedValue.toLowerCase().trim();
        String normalizedActual = actualValue.toLowerCase().trim();
        
        if (normalizedExpected.equals("true")) {
            return normalizedActual.equals("true") || 
                   normalizedActual.equals("1") || 
                   normalizedActual.equals("yes") ||
                   normalizedActual.equals("on");
        } else if (normalizedExpected.equals("false")) {
            return normalizedActual.equals("false") || 
                   normalizedActual.equals("0") || 
                   normalizedActual.equals("no") ||
                   normalizedActual.equals("off");
        }
        
        // Numeric comparison (if both are numbers)
        try {
            double expectedNum = Double.parseDouble(expectedValue);
            double actualNum = Double.parseDouble(actualValue);
            return Math.abs(expectedNum - actualNum) < 1e-10;
        } catch (NumberFormatException e) {
            // Not numeric, return false for string comparison
            return false;
        }
    }
    
    /**
     * Get all @ConditionalOnProperty annotations from an element.
     * Handles both direct annotations and container annotations.
     */
    private ConditionalOnProperty[] getConditionalAnnotations(AnnotatedElement element) {
        // Get direct annotations
        ConditionalOnProperty directAnnotation = element.getAnnotation(ConditionalOnProperty.class);
        
        // Get container annotations
        ConditionalOnPropertyList containerAnnotation = element.getAnnotation(ConditionalOnPropertyList.class);
        
        if (directAnnotation != null && containerAnnotation != null) {
            // Combine both
            ConditionalOnProperty[] containerConditions = containerAnnotation.value();
            ConditionalOnProperty[] allConditions = new ConditionalOnProperty[containerConditions.length + 1];
            System.arraycopy(containerConditions, 0, allConditions, 0, containerConditions.length);
            allConditions[containerConditions.length] = directAnnotation;
            return allConditions;
        } else if (directAnnotation != null) {
            return new ConditionalOnProperty[]{directAnnotation};
        } else if (containerAnnotation != null) {
            return containerAnnotation.value();
        }
        
        return new ConditionalOnProperty[0];
    }
    
    /**
     * Create a ConditionEvaluator that uses system properties as source.
     */
    public static ConditionEvaluator createWithSystemProperties() {
        PropertySource systemProps = new PropertySource();
        // Copy system properties
        for (String key : System.getProperties().stringPropertyNames()) {
            systemProps.setProperty(key, System.getProperty(key));
        }
        return new ConditionEvaluator(systemProps);
    }
}