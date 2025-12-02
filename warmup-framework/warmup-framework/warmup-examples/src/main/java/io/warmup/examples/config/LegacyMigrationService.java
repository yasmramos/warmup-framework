package io.warmup.examples.config;

/**
 * Legacy migration service for handling legacy system migrations.
 */
public class LegacyMigrationService {
    
    public void migrateLegacyData() {
        System.out.println("Migrating legacy data...");
    }
    
    public void rollbackLegacyData() {
        System.out.println("Rolling back legacy data...");
    }
}