package io.warmup.framework.config;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

/**
 * A property source that loads configuration properties from files.
 * This class provides a flexible way to load properties from either the filesystem
 * or the classpath, with filesystem having priority.
 * 
 * <p>Usage examples:
 * <pre>
 * // Load from specific file
 * PropertySource props = new PropertySource("config.properties");
 * 
 * // Empty constructor for manual property management
 * PropertySource props = new PropertySource();
 * props.setProperty("key", "value");
 * </pre>
 * 
 * @author Warmup Framework
 * @version 1.0
 */
public class PropertySource {

    /**
     * Internal Properties object storing all configuration key-value pairs
     * Made public for test access
     */
    public final Properties properties = new Properties();

    /**
     * Default constructor creating an empty PropertySource.
     * Properties can be added later using setProperty() method.
     */
    public PropertySource() {
        // Intentionally empty - properties object is already initialized
    }

    /**
     * Constructor that automatically loads properties from the specified file.
     * The file is searched first in the filesystem, then in the classpath.
     *
     * @param propertyFile the path to the property file or classpath resource name
     * @throws IOException if an error occurs while reading the property file
     */
    public PropertySource(String propertyFile) throws IOException {
        loadPropertiesFromFile(propertyFile);
    }

    /**
     * Loads properties from a file, searching first in the filesystem 
     * and then in the classpath if not found.
     * 
     * <p>Search order:
     * <ol>
     *   <li>Filesystem (absolute or relative path)</li>
     *   <li>Classpath resources</li>
     * </ol>
     *
     * @param filename the file path or classpath resource name to load properties from
     * @throws IOException if an error occurs while reading the file
     */
    private void loadPropertiesFromFile(String filename) throws IOException {
        // 1. First attempt: Load from filesystem
        Path filePath = Paths.get(filename);
        if (Files.exists(filePath) && !Files.isDirectory(filePath)) {
            try (InputStream input = new FileInputStream(filePath.toFile())) {
                properties.load(input);
                System.out.println("Loaded properties from (filesystem): " + filename);
                return; // Successfully loaded from filesystem, exit method
            }
        }

        // 2. Second attempt: Load from classpath
        try (InputStream input = getClass().getClassLoader().getResourceAsStream(filename)) {
            if (input != null) {
                properties.load(input);
                System.out.println("Loaded properties from (classpath): " + filename);
                return;
            }
        }

        // 3. File not found in either location
        System.out.println("Property file not found (neither in filesystem nor classpath): " + filename);
    }

    /**
     * Gets the property value for the specified key.
     *
     * @param key the property key to look up
     * @return the property value, or null if the key is not found
     */
    public String getProperty(String key) {
        return properties.getProperty(key);
    }

    /**
     * Gets the property value for the specified key, with a default value
     * returned if the key is not found.
     *
     * @param key the property key to look up
     * @param defaultValue the default value to return if key is not found
     * @return the property value, or defaultValue if the key is not found
     */
    public String getProperty(String key, String defaultValue) {
        return properties.getProperty(key, defaultValue);
    }

    /**
     * Sets a property value. If the property already exists, it will be overwritten.
     *
     * @param key the property key to set
     * @param value the property value to set
     */
    public void setProperty(String key, String value) {
        properties.setProperty(key, value);
    }

    /**
     * Checks if the property source contains the specified key.
     *
     * @param key the property key to check
     * @return true if the property exists, false otherwise
     */
    public boolean containsProperty(String key) {
        return properties.containsKey(key);
    }

    /**
     * Removes all properties from this property source.
     * After calling this method, the PropertySource will be empty.
     */
    public void clear() {
        properties.clear();
    }
}