package io.warmup.examples.config;

import io.warmup.examples.DatabaseService;

/**
 * PostgreSQL database service implementation.
 */
public class PostgreSqlDatabaseService implements DatabaseService {
    
    @Override
    public String getType() {
        return "PostgreSQL";
    }
    
    @Override
    public void connect() {
        System.out.println("Connecting to PostgreSQL database...");
    }
}