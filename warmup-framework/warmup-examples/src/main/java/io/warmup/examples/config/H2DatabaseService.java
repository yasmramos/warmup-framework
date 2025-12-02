package io.warmup.examples.config;

import io.warmup.examples.DatabaseService;

/**
 * H2 database service implementation.
 */
public class H2DatabaseService implements DatabaseService {
    
    @Override
    public String getType() {
        return "H2";
    }
    
    @Override
    public void connect() {
        System.out.println("Connecting to H2 database...");
    }
}