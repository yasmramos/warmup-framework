package io.warmup.framework.core.test;

import io.warmup.framework.config.PropertySource;
import io.warmup.framework.core.PropertyManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Comprehensive tests for PropertyManager functionality.
 * 
 * @author MiniMax Agent
 * @since 1.0
 */
class PropertyManagerTest {

    private PropertyManager propertyManager;
    private java.io.File tempPropsFile;

    @BeforeEach
    void setUp() throws IOException {
        // Create a temporary properties file for testing
        tempPropsFile = java.nio.file.Files.createTempFile("test", ".properties").toFile();
        
        // Write initial properties to the file
        try (java.io.FileWriter writer = new java.io.FileWriter(tempPropsFile)) {
            writer.write("test.key=test.value\n");
            writer.write("test.number=42\n");
            writer.write("test.boolean=true\n");
            writer.write("base.property=resolved.value\n");
            writer.write("server.host=localhost\n");
            writer.write("server.port=8080\n");
        }
        
        propertyManager = new PropertyManager(tempPropsFile.getAbsolutePath());
    }

    @AfterEach
    void tearDown() {
        // Clean up temporary file
        if (tempPropsFile != null && tempPropsFile.exists()) {
            tempPropsFile.delete();
        }
    }

    @Test
    void testGetProperty() {
        // These properties were set up in the temporary file
        assertEquals("test.value", propertyManager.getProperty("test.key"));
        assertEquals("42", propertyManager.getProperty("test.number"));
        assertEquals("true", propertyManager.getProperty("test.boolean"));
        
        // Test getting non-existing property
        assertNull(propertyManager.getProperty("non.existent"));
    }

    @Test
    void testGetPropertyWithDefault() {
        // Test getting existing property with default (default should be ignored)
        assertEquals("test.value", propertyManager.getProperty("test.key", "default"));
        
        // Test getting non-existing property with default
        assertEquals("default", propertyManager.getProperty("non.existent", "default"));
    }

    @Test
    void testSetProperty() {
        // Set a new property
        propertyManager.setProperty("new.property", "new.value");
        
        // Verify the property is set (it will be persisted to the file)
        // Since we're testing with a real PropertyManager, this will work
        assertEquals("new.value", propertyManager.getProperty("new.property"));
    }

    @Test
    void testResolvePropertyValueDirect() {
        // Test resolving a direct string value
        String result = propertyManager.resolvePropertyValue("direct.value");
        assertEquals("direct.value", result);
    }

    @Test
    void testResolvePropertyValueWithExistingProperty() {
        // Test resolving a property expression (base.property is in the temp file)
        String result = propertyManager.resolvePropertyValue("${base.property}");
        assertEquals("resolved.value", result);
    }

    @Test
    void testResolvePropertyValueWithDefault() throws IOException {
        // Test resolving a property expression with default value
        String result = propertyManager.resolvePropertyValue("${non.existent:default.value}");
        assertEquals("default.value", result);
    }

    @Test
    void testResolvePropertyValueRequired() {
        // This should resolve correctly because we have base.property in the temp file
        String result = propertyManager.resolvePropertyValue("${base.property}");
        assertEquals("resolved.value", result);
    }

    @Test
    void testResolvePropertyValueRequiredMissing() {
        // Test resolving a required property that doesn't exist (should throw exception)
        assertThrows(IllegalArgumentException.class, () -> {
            propertyManager.resolvePropertyValue("${non.existent.required}");
        });
    }

    @Test
    void testInvalidPropertyExpression() {
        // Test various invalid property expressions
        assertEquals("${incomplete", propertyManager.resolvePropertyValue("${incomplete"));
        assertEquals("normal.text", propertyManager.resolvePropertyValue("normal.text"));
        assertEquals("test${no.closing.bracket", propertyManager.resolvePropertyValue("test${no.closing.bracket"));
        assertNull(propertyManager.resolvePropertyValue(null));
    }

    @Test
    void testComplexPropertyExpression() {
        // Test resolving complex expression (these properties are in the temp file)
        String result = propertyManager.resolvePropertyValue("jdbc:mysql://${server.host}:${server.port}/mydb");
        assertEquals("jdbc:mysql://localhost:8080/mydb", result);
    }

    @Test
    void testPropertySourceIntegration() {
        // Test that PropertyManager properly integrates with the underlying PropertySource
        // This is verified through the existing test properties in the file
        assertNotNull(propertyManager.getProperty("test.key"));
        assertEquals("test.value", propertyManager.getProperty("test.key"));
        
        // Test that setProperty works correctly
        propertyManager.setProperty("integration.test", "integration.value");
        assertEquals("integration.value", propertyManager.getProperty("integration.test"));
    }

    @Test
    void testEmptyPropertySource() throws IOException {
        // Create a completely empty property file
        java.io.File emptyFile = java.nio.file.Files.createTempFile("empty", ".properties").toFile();
        try (java.io.FileWriter writer = new java.io.FileWriter(emptyFile)) {
            // Write nothing, just create an empty file
        }
        
        PropertyManager emptyManager = new PropertyManager(emptyFile.getAbsolutePath());
        
        try {
            // All properties should be null
            assertNull(emptyManager.getProperty("any.key"));
            assertEquals("default", emptyManager.getProperty("any.key", "default"));
            
            // Resolving missing required property should throw exception
            assertThrows(IllegalArgumentException.class, () -> {
                emptyManager.resolvePropertyValue("${missing.required}");
            });
            
            // Resolving with default should work
            String result = emptyManager.resolvePropertyValue("${missing.with.default:default.value}");
            assertEquals("default.value", result);
        } finally {
            emptyFile.delete();
        }
    }

    @Test
    void testPropertyOverriding() {
        // Test that setting a property overrides the original value
        // Set an initial property in the file
        propertyManager.setProperty("override.test", "original");
        
        // Override the property
        propertyManager.setProperty("override.test", "modified");
        
        // Verify the override worked
        assertEquals("modified", propertyManager.getProperty("override.test"));
    }

    @Test
    void testTypeConversionSimulation() {
        // Since PropertyManager only returns strings, demonstrate type conversion patterns
        
        // Simulate boolean conversion with existing properties
        String trueValue = propertyManager.getProperty("test.boolean");
        String falseValue = "false"; // Add this to demonstrate false case
        
        // Simulate boolean conversion logic (common pattern)
        boolean trueBool = "true".equalsIgnoreCase(trueValue) || "1".equals(trueValue);
        boolean falseBool = "false".equalsIgnoreCase(falseValue) || "0".equals(falseValue);
        
        assertTrue(trueBool);
        assertFalse(falseBool);
        
        // Simulate integer conversion
        String intValue = propertyManager.getProperty("test.number");
        
        try {
            int parsedInt = Integer.parseInt(intValue);
            assertEquals(42, parsedInt);
        } catch (NumberFormatException e) {
            fail("Should be able to parse valid integer: " + intValue);
        }
        
        // Test invalid integer - set one and retrieve it
        propertyManager.setProperty("int.invalid", "not.a.number");
        String invalidValue = propertyManager.getProperty("int.invalid");
        
        assertThrows(NumberFormatException.class, () -> {
            Integer.parseInt(invalidValue);
        });
    }
}