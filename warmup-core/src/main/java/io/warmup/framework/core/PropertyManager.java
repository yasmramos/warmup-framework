package io.warmup.framework.core;

import io.warmup.framework.config.PropertySource;
import java.io.IOException;

public class PropertyManager {

    private final PropertySource propertySource;

    // No-args constructor for framework dependency injection
    public PropertyManager() throws IOException {
        this.propertySource = new PropertySource();
    }

    public PropertyManager(String propertyFile) throws IOException {
        this.propertySource = new PropertySource(propertyFile);
    }

    public String getProperty(String key) {
        return propertySource.getProperty(key);
    }

    public String getProperty(String key, String defaultValue) {
        return propertySource.getProperty(key, defaultValue);
    }

    public void setProperty(String key, String value) {
        propertySource.setProperty(key, value);
    }

    public String resolvePropertyValue(String valueExpression) {
        return resolvePropertyValue(valueExpression, propertySource);
    }

    public static String resolvePropertyValue(String valueExpression, PropertySource propertySource) {
        if (valueExpression == null || !valueExpression.startsWith("${") || !valueExpression.endsWith("}")) {
            return valueExpression;
        }

        // Extraer la expresión: ${key:default} o ${key}
        String expression = valueExpression.substring(2, valueExpression.length() - 1);
        String[] parts = expression.split(":", 2);
        String key = parts[0];
        String defaultValue = parts.length > 1 ? parts[1] : null;

        if (defaultValue != null) {
            return propertySource.getProperty(key, defaultValue);
        } else {
            String value = propertySource.getProperty(key);
            if (value == null) {
                throw new IllegalArgumentException("Propiedad no encontrada: " + key);
            }
            return value;
        }
    }
}
