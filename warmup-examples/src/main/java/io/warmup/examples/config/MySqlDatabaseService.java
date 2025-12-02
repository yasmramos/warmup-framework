package io.warmup.examples.config;

import io.warmup.examples.DatabaseService;

/**
 * MySQL database service implementation.
 */
public class MySqlDatabaseService implements DatabaseService {
    
    @Override
    public String getType() {
        return "MySQL";
    }
    
    @Override
    public void connect() {
        System.out.println("Connecting to MySQL database...");
    }
}