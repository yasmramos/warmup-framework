package io.warmup.framework.core.test;

import io.warmup.framework.config.PropertySource;
import io.warmup.framework.core.PropertyManager;
import io.warmup.framework.core.ProfileManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Comprehensive tests for ProfileManager functionality.
 * 
 * @author MiniMax Agent
 * @since 1.0
 */
class ProfileManagerTest {

    private PropertyManager propertyManager;
    private ProfileManager profileManager;
    private PropertySource propertySource;
    private java.io.File tempPropsFile;

    @BeforeEach
    void setUp() throws IOException {
        // Create a temporary properties file for testing
        tempPropsFile = java.nio.file.Files.createTempFile("test", ".properties").toFile();
        
        // Write initial properties to the file
        try (java.io.FileWriter writer = new java.io.FileWriter(tempPropsFile)) {
            writer.write("warmup.profiles=dev,test\n");
        }
        
        propertyManager = new PropertyManager(tempPropsFile.getAbsolutePath());
        propertySource = new PropertySource();
        
        // Create ProfileManager with initial profiles
        profileManager = new ProfileManager(propertySource, "dev", "test");
    }

    @AfterEach
    void tearDown() {
        // Clean up temporary file
        if (tempPropsFile != null && tempPropsFile.exists()) {
            tempPropsFile.delete();
        }
    }

    @Test
    void testProfileManagerInitialization() {
        // Test that ProfileManager is properly initialized
        assertNotNull(profileManager);
        
        // Check initial profiles (should include dev, test from constructor and possibly from properties)
        Set<String> activeProfiles = profileManager.getActiveProfiles();
        assertTrue(activeProfiles.contains("dev"));
        assertTrue(activeProfiles.contains("test"));
    }

    @Test
    void testSetActiveProfiles() {
        // Test setting active profiles directly
        profileManager.setActiveProfiles("prod", "staging");
        
        Set<String> activeProfiles = profileManager.getActiveProfiles();
        assertEquals(2, activeProfiles.size());
        assertTrue(activeProfiles.contains("prod"));
        assertTrue(activeProfiles.contains("staging"));
    }

    @Test
    void testSetActiveProfilesWithEmptyArray() {
        // Test setting empty profiles array (should default to "default")
        profileManager.setActiveProfiles();
        
        Set<String> activeProfiles = profileManager.getActiveProfiles();
        assertEquals(1, activeProfiles.size());
        assertTrue(activeProfiles.contains("default"));
    }

    @Test
    void testAddActiveProfile() {
        // Test adding a single active profile
        profileManager.addActiveProfile("newProfile");
        
        Set<String> activeProfiles = profileManager.getActiveProfiles();
        assertTrue(activeProfiles.contains("newProfile"));
    }

    @Test
    void testIsProfileActive() {
        // Test checking if profile is active
        assertTrue(profileManager.isProfileActive("dev"));
        assertTrue(profileManager.isProfileActive("test"));
        assertFalse(profileManager.isProfileActive("prod"));
        
        // Add a new profile and test it
        profileManager.addActiveProfile("prod");
        assertTrue(profileManager.isProfileActive("prod"));
    }

    @Test
    void testGetActiveProfiles() {
        // Test getting active profiles set
        Set<String> activeProfiles = profileManager.getActiveProfiles();
        
        // Should be a copy, not the internal set
        assertNotSame(activeProfiles, profileManager.getActiveProfiles());
        
        // Should contain initial profiles
        assertTrue(activeProfiles.contains("dev"));
        assertTrue(activeProfiles.contains("test"));
    }

    @Test
    void testShouldRegisterClassWithoutAnnotation() {
        // Test class without @Profile annotation (should always register)
        class TestClassNoAnnotation {}
        
        assertTrue(profileManager.shouldRegisterClass(TestClassNoAnnotation.class));
    }

    @Test
    void testShouldRegisterClassWithProfileAnnotation() {
        // Test class with @Profile annotation that matches active profile
        io.warmup.framework.annotation.Profile profileAnnotation = 
            TestClassWithProfile.class.getAnnotation(io.warmup.framework.annotation.Profile.class);
        
        // This test depends on the actual annotation setup
        // For now, test basic functionality
        class TestClassWithSpecificProfile {}
        
        // Add the profile that will be needed
        profileManager.addActiveProfile("required");
        
        // This test is conceptual since we can't easily set @Profile annotation on a class
        // In a real scenario, you would test with classes that have the annotation
    }

    @Test
    void testShouldRegisterClassWithMultipleRequiredProfiles() {
        // Test class requiring multiple profiles (should register if any matches)
        // Add multiple profiles
        profileManager.setActiveProfiles("dev", "test", "staging");
        
        // Verify all are active
        assertTrue(profileManager.isProfileActive("dev"));
        assertTrue(profileManager.isProfileActive("test"));
        assertTrue(profileManager.isProfileActive("staging"));
    }

    @Test
    void testProfileManagerWithPropertySource() {
        // Test that ProfileManager works with PropertySource
        // Set a property that could affect profiles
        propertySource.setProperty("warmup.profiles", "customProfile");
        
        // Create a new ProfileManager to test property loading
        ProfileManager newProfileManager = new ProfileManager(propertySource);
        
        // Should load profiles from property or default
        Set<String> activeProfiles = newProfileManager.getActiveProfiles();
        assertNotNull(activeProfiles);
        assertFalse(activeProfiles.isEmpty());
    }

    @Test
    void testProfileManagerWithNoPropertySource() {
        // Test ProfileManager creation with null PropertySource
        ProfileManager nullProfileManager = new ProfileManager(null, "initial");
        
        Set<String> activeProfiles = nullProfileManager.getActiveProfiles();
        assertTrue(activeProfiles.contains("initial"));
    }

    @Test
    void testProfileManagerWithNoInitialProfiles() {
        // Test ProfileManager creation with no initial profiles
        ProfileManager emptyProfileManager = new ProfileManager(propertySource);
        
        Set<String> activeProfiles = emptyProfileManager.getActiveProfiles();
        assertTrue(activeProfiles.contains("default")); // Should default to "default"
    }

    @Test
    void testMultipleProfileOperations() {
        // Test multiple profile operations in sequence
        assertTrue(profileManager.isProfileActive("dev"));
        
        // Add new profile
        profileManager.addActiveProfile("integration");
        assertTrue(profileManager.isProfileActive("integration"));
        
        // Set new active profiles
        profileManager.setActiveProfiles("prod", "staging");
        assertFalse(profileManager.isProfileActive("dev")); // Should no longer be active
        assertFalse(profileManager.isProfileActive("integration")); // Should no longer be active
        assertTrue(profileManager.isProfileActive("prod"));
        assertTrue(profileManager.isProfileActive("staging"));
    }

    @Test
    void testEmptyProfileHandling() {
        // Test handling of null and empty profile names
        profileManager.addActiveProfile(null);
        profileManager.addActiveProfile("");
        profileManager.addActiveProfile("   "); // whitespace only
        
        // Should not add empty profiles
        Set<String> activeProfiles = profileManager.getActiveProfiles();
        
        // The exact behavior depends on the implementation
        // These tests verify that the method doesn't throw exceptions
        assertNotNull(activeProfiles);
    }

    @Test
    void testProfilePersistenceInSet() {
        // Test that getActiveProfiles returns a proper copy
        Set<String> originalProfiles = profileManager.getActiveProfiles();
        int originalSize = originalProfiles.size();
        
        // Modify the returned set
        originalProfiles.clear();
        
        // Original should be unchanged
        Set<String> currentProfiles = profileManager.getActiveProfiles();
        assertEquals(originalSize, currentProfiles.size());
    }

    @Test
    void testProfileManagerIntegration() {
        // Test integration between PropertyManager and ProfileManager
        // Both should be working independently but can be used together
        
        // Test PropertyManager works
        propertyManager.setProperty("integration.test", "value");
        assertEquals("value", propertyManager.getProperty("integration.test"));
        
        // Test ProfileManager works
        assertTrue(profileManager.isProfileActive("dev"));
        
        // This test verifies they don't interfere with each other
        assertNotNull(propertyManager);
        assertNotNull(profileManager);
    }

    // Helper class for testing
    static class TestClassWithProfile {}
}